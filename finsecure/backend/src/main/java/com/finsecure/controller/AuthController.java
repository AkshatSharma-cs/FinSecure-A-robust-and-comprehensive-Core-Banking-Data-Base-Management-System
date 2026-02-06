package com.finsecure.controller;

import static com.finsecure.dto.DTOs.*;

import com.finsecure.entity.User;
import com.finsecure.entity.Customer;
import com.finsecure.entity.Otp;
import com.finsecure.repository.UserRepository;
import com.finsecure.repository.CustomerRepository;
import com.finsecure.repository.OtpRepository;
import com.finsecure.service.EmailService;
import com.finsecure.service.AuditService;
import com.finsecure.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * Authentication Controller
 * Handles user registration, login, OTP verification
 */
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = {"https://localhost:3000", "https://localhost:3001"})
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Value("${otp.length}")
    private int otpLength;

    @Value("${otp.expiry.minutes}")
    private int otpExpiryMinutes;

    /**
     * Register new customer
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        try {
            // Validate email
            if (userRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Registration failed", "Email already registered"));
            }

            // Create user
            User user = new User();
            user.setEmail(request.getEmail());
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            user.setRole(User.Role.CUSTOMER);
            user.setIsActive(true);
            user = userRepository.save(user);

            // Create customer profile
            Customer customer = new Customer();
            customer.setUser(user);
            customer.setFirstName(request.getFirstName());
            customer.setLastName(request.getLastName());
            customer.setDateOfBirth(request.getDateOfBirth());
            customer.setPhone(request.getPhone());
            customer.setAddress(request.getAddress());
            customer.setCity(request.getCity());
            customer.setState(request.getState());
            customer.setPostalCode(request.getPostalCode());
            customer.setAccountType(Customer.AccountType.valueOf(request.getAccountType()));
            customer.setKycStatus(Customer.KycStatus.PENDING);

            // Handle guardian for minor accounts
            if (request.getGuardianCustomerId() != null) {
                Customer guardian = customerRepository.findById(request.getGuardianCustomerId())
                    .orElseThrow(() -> new RuntimeException("Guardian not found"));
                customer.setGuardian(guardian);
            }

            customer = customerRepository.save(customer);

            // Log registration
            auditService.logAction(user.getUserId(), AuditLog.ActionType.ACCOUNT_CREATE, 
                "Customer", customer.getCustomerId(), "Customer registered: " + request.getEmail(),
                httpRequest);

            // Send welcome email
            emailService.sendWelcomeEmail(request.getEmail(), request.getFirstName());

            return ResponseEntity.ok(ApiResponse.success(
                "Registration successful. Please complete KYC verification.",
                customer.getCustomerId()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Registration failed", e.getMessage()));
        }
    }

    /**
     * Login with email and password
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        try {
            // Authenticate user
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            // Get user details
            User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

            if (!user.getIsActive()) {
                auditService.logAction(user.getUserId(), AuditLog.ActionType.FAILED_LOGIN,
                    "User", user.getUserId(), "Login attempt for inactive account",
                    httpRequest);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Login failed", "Account is inactive"));
            }

            // Generate OTP for login verification (optional based on settings)
            String otp = generateOtp();
            Otp otpEntity = new Otp();
            otpEntity.setUser(user);
            otpEntity.setOtpCode(otp);
            otpEntity.setOtpType(Otp.OtpType.LOGIN);
            otpEntity.setEmail(user.getEmail());
            otpEntity.setExpiresAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes));
            otpRepository.save(otpEntity);

            // Send OTP email
            emailService.sendOtpEmail(user.getEmail(), otp, "Login Verification");

            return ResponseEntity.ok(ApiResponse.success(
                "OTP sent to your email. Please verify to complete login.",
                null
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Login failed", "Invalid credentials"));
        }
    }

    /**
     * Verify OTP and complete login
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpVerificationRequest request, 
                                       HttpServletRequest httpRequest) {
        try {
            User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

            // Find valid OTP
            Otp otp = otpRepository.findByUserIdAndOtpCodeAndOtpTypeAndIsVerifiedFalse(
                user.getUserId(), 
                request.getOtpCode(), 
                Otp.OtpType.valueOf(request.getOtpType())
            ).orElseThrow(() -> new RuntimeException("Invalid OTP"));

            // Check expiration
            if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("OTP verification failed", "OTP expired"));
            }

            // Mark OTP as verified
            otp.setIsVerified(true);
            otpRepository.save(otp);

            // Generate JWT tokens
            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
            String accessToken = jwtUtil.generateToken(userDetails, user.getRole().name());
            String refreshToken = jwtUtil.generateRefreshToken(userDetails);

            // Update last login
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            // Log successful login
            auditService.logAction(user.getUserId(), AuditLog.ActionType.LOGIN,
                "User", user.getUserId(), "Successful login",
                httpRequest);

            LoginResponse response = new LoginResponse(
                accessToken,
                refreshToken,
                user.getEmail(),
                user.getRole().name(),
                user.getUserId(),
                "Login successful"
            );

            return ResponseEntity.ok(ApiResponse.success("Login successful", response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("OTP verification failed", e.getMessage()));
        }
    }

    /**
     * Request OTP for various operations
     */
    @PostMapping("/request-otp")
    public ResponseEntity<?> requestOtp(@RequestBody OtpRequest request) {
        try {
            User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

            String otp = generateOtp();
            Otp otpEntity = new Otp();
            otpEntity.setUser(user);
            otpEntity.setOtpCode(otp);
            otpEntity.setOtpType(Otp.OtpType.valueOf(request.getOtpType()));
            otpEntity.setEmail(user.getEmail());
            otpEntity.setExpiresAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes));
            otpRepository.save(otpEntity);

            // Send OTP email
            String purpose = request.getOtpType().replace("_", " ");
            emailService.sendOtpEmail(user.getEmail(), otp, purpose);

            return ResponseEntity.ok(ApiResponse.success(
                "OTP sent to your email",
                null
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("OTP request failed", e.getMessage()));
        }
    }

    /**
     * Logout
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest httpRequest) {
        try {
            // Extract user from JWT (if needed for logging)
            // Implement token blacklisting if required
            
            return ResponseEntity.ok(ApiResponse.success("Logout successful", null));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Logout failed", e.getMessage()));
        }
    }

    /**
     * Generate random OTP
     */
    private String generateOtp() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }
}
