package com.finsecure.controller;

import com.finsecure.dto.*;
import com.finsecure.entity.*;
import com.finsecure.repository.*;
import com.finsecure.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Customer Portal Controller
 * All endpoints require CUSTOMER role
 */
@RestController
@RequestMapping("/customer")
@PreAuthorize("hasRole('CUSTOMER')")
@CrossOrigin(origins = "https://localhost:3000")
public class CustomerController {

    @Autowired private UserRepository userRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private CardRepository cardRepository;
    @Autowired private LoanRepository loanRepository;
    @Autowired private KycDocumentRepository kycDocumentRepository;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private OtpRepository otpRepository;
    
    @Autowired private TransactionService transactionService;
    @Autowired private CardService cardService;
    @Autowired private NotificationService notificationService;
    @Autowired private AuditService auditService;

    /**
     * Get customer dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(Authentication auth) {
        try {
            Customer customer = getCustomerFromAuth(auth);

            // Get dashboard statistics
            List<Account> accounts = accountRepository.findActiveAccountsByCustomerId(customer.getCustomerId());
            BigDecimal totalBalance = accounts.stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            List<Card> cards = cardRepository.findActiveCardsByCustomerId(customer.getCustomerId());
            List<Loan> loans = loanRepository.findActiveLoansByCustomerId(customer.getCustomerId());
            Long unreadNotifications = notificationRepository.countByUserIdAndIsReadFalse(customer.getUser().getUserId());

            // Build response
            CustomerProfileResponse profile = buildCustomerProfile(customer);
            DashboardResponse dashboard = new DashboardResponse(
                profile,
                accounts.size(),
                totalBalance,
                cards.size(),
                loans.size(),
                unreadNotifications.intValue()
            );

            return ResponseEntity.ok(ApiResponse.success("Dashboard loaded", dashboard));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to load dashboard", e.getMessage()));
        }
    }

    /**
     * Get customer profile
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication auth) {
        try {
            Customer customer = getCustomerFromAuth(auth);
            CustomerProfileResponse profile = buildCustomerProfile(customer);
            return ResponseEntity.ok(ApiResponse.success("Profile loaded", profile));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to load profile", e.getMessage()));
        }
    }

    /**
     * Get all accounts
     */
    @GetMapping("/accounts")
    public ResponseEntity<?> getAccounts(Authentication auth) {
        try {
            Customer customer = getCustomerFromAuth(auth);
            List<Account> accounts = accountRepository.findByCustomerId(customer.getCustomerId());
            
            List<AccountResponse> response = accounts.stream()
                .map(this::buildAccountResponse)
                .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("Accounts loaded", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to load accounts", e.getMessage()));
        }
    }

    /**
     * Get account transactions
     */
    @GetMapping("/accounts/{accountId}/transactions")
    public ResponseEntity<?> getTransactions(@PathVariable Long accountId, 
                                            @RequestParam(required = false) String startDate,
                                            @RequestParam(required = false) String endDate,
                                            Authentication auth) {
        try {
            Customer customer = getCustomerFromAuth(auth);
            
            // Verify account ownership
            Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
            
            if (!account.getCustomer().getCustomerId().equals(customer.getCustomerId())) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("Access denied", "Not your account"));
            }

            List<Transaction> transactions;
            if (startDate != null && endDate != null) {
                LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
                LocalDateTime end = LocalDate.parse(endDate).atTime(23, 59, 59);
                transactions = transactionRepository.findByAccountIdAndDateRange(accountId, start, end);
            } else {
                transactions = transactionRepository.findByAccountId(accountId);
            }

            List<TransactionResponse> response = transactions.stream()
                .map(this::buildTransactionResponse)
                .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("Transactions loaded", response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to load transactions", e.getMessage()));
        }
    }

    /**
     * Make a transaction (requires OTP)
     */
    @PostMapping("/transactions")
    public ResponseEntity<?> makeTransaction(@RequestBody TransactionRequest request, 
                                            Authentication auth,
                                            HttpServletRequest httpRequest) {
        try {
            Customer customer = getCustomerFromAuth(auth);

            // Verify OTP
            Otp otp = otpRepository.findByUserIdAndOtpCodeAndOtpTypeAndIsVerifiedFalse(
                customer.getUser().getUserId(),
                request.getOtpCode(),
                Otp.OtpType.TRANSACTION
            ).orElseThrow(() -> new RuntimeException("Invalid OTP"));

            if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Transaction failed", "OTP expired"));
            }

            // Mark OTP as verified
            otp.setIsVerified(true);
            otpRepository.save(otp);

            // Process transaction
            Transaction transaction = transactionService.processTransaction(request, customer);

            // Log transaction
            auditService.logAction(customer.getUser().getUserId(), AuditLog.ActionType.TRANSACTION,
                "Transaction", transaction.getTransactionId(),
                "Transaction: " + request.getTransactionType() + " - " + request.getAmount(),
                httpRequest);

            // Send notification
            notificationService.sendTransactionNotification(customer.getUser(), transaction);

            TransactionResponse response = buildTransactionResponse(transaction);
            return ResponseEntity.ok(ApiResponse.success("Transaction successful", response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Transaction failed", e.getMessage()));
        }
    }

    /**
     * Get all cards
     */
    @GetMapping("/cards")
    public ResponseEntity<?> getCards(Authentication auth) {
        try {
            Customer customer = getCustomerFromAuth(auth);
            List<Card> cards = cardRepository.findByCustomerId(customer.getCustomerId());
            
            List<CardResponse> response = cards.stream()
                .map(this::buildCardResponse)
                .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("Cards loaded", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to load cards", e.getMessage()));
        }
    }

    /**
     * Card action (block, unblock, replace - requires OTP)
     */
    @PostMapping("/cards/action")
    public ResponseEntity<?> cardAction(@RequestBody CardActionRequest request,
                                       Authentication auth,
                                       HttpServletRequest httpRequest) {
        try {
            Customer customer = getCustomerFromAuth(auth);

            // Verify OTP
            Otp otp = otpRepository.findByUserIdAndOtpCodeAndOtpTypeAndIsVerifiedFalse(
                customer.getUser().getUserId(),
                request.getOtpCode(),
                Otp.OtpType.CARD_BLOCK
            ).orElseThrow(() -> new RuntimeException("Invalid OTP"));

            if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Action failed", "OTP expired"));
            }

            otp.setIsVerified(true);
            otpRepository.save(otp);

            // Perform card action
            Card card = cardService.performCardAction(request, customer);

            // Log action
            AuditLog.ActionType actionType = request.getAction().equals("BLOCK") 
                ? AuditLog.ActionType.CARD_BLOCK 
                : (request.getAction().equals("REPLACE") 
                    ? AuditLog.ActionType.CARD_REPLACE 
                    : AuditLog.ActionType.CARD_UNBLOCK);

            auditService.logAction(customer.getUser().getUserId(), actionType,
                "Card", card.getCardId(), "Card action: " + request.getAction(),
                httpRequest);

            // Send notification
            notificationService.sendCardActionNotification(customer.getUser(), card, request.getAction());

            CardResponse response = buildCardResponse(card);
            return ResponseEntity.ok(ApiResponse.success("Card action successful", response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Card action failed", e.getMessage()));
        }
    }

    /**
     * Get all loans
     */
    @GetMapping("/loans")
    public ResponseEntity<?> getLoans(Authentication auth) {
        try {
            Customer customer = getCustomerFromAuth(auth);
            List<Loan> loans = loanRepository.findByCustomerId(customer.getCustomerId());
            
            List<LoanResponse> response = loans.stream()
                .map(this::buildLoanResponse)
                .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("Loans loaded", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to load loans", e.getMessage()));
        }
    }

    /**
     * Upload KYC document
     */
    @PostMapping("/kyc/upload")
    public ResponseEntity<?> uploadKycDocument(@RequestBody KycDocumentUploadRequest request,
                                               Authentication auth,
                                               HttpServletRequest httpRequest) {
        try {
            Customer customer = getCustomerFromAuth(auth);

            KycDocument document = new KycDocument();
            document.setCustomer(customer);
            document.setDocumentType(KycDocument.DocumentType.valueOf(request.getDocumentType()));
            document.setDocumentNumber(request.getDocumentNumber());
            document.setDocumentUrl(request.getDocumentUrl());
            document.setVerificationStatus(KycDocument.VerificationStatus.PENDING);
            document = kycDocumentRepository.save(document);

            // Log KYC upload
            auditService.logAction(customer.getUser().getUserId(), AuditLog.ActionType.KYC_UPLOAD,
                "KycDocument", document.getKycId(),
                "KYC document uploaded: " + request.getDocumentType(),
                httpRequest);

            // Send notification
            notificationService.sendKycUploadNotification(customer.getUser());

            KycDocumentResponse response = buildKycResponse(document);
            return ResponseEntity.ok(ApiResponse.success("KYC document uploaded", response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("KYC upload failed", e.getMessage()));
        }
    }

    /**
     * Get KYC documents
     */
    @GetMapping("/kyc/documents")
    public ResponseEntity<?> getKycDocuments(Authentication auth) {
        try {
            Customer customer = getCustomerFromAuth(auth);
            List<KycDocument> documents = kycDocumentRepository.findByCustomerId(customer.getCustomerId());
            
            List<KycDocumentResponse> response = documents.stream()
                .map(this::buildKycResponse)
                .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("KYC documents loaded", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to load KYC documents", e.getMessage()));
        }
    }

    /**
     * Get notifications
     */
    @GetMapping("/notifications")
    public ResponseEntity<?> getNotifications(Authentication auth) {
        try {
            Customer customer = getCustomerFromAuth(auth);
            List<Notification> notifications = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(customer.getUser().getUserId());
            
            List<NotificationResponse> response = notifications.stream()
                .map(this::buildNotificationResponse)
                .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("Notifications loaded", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to load notifications", e.getMessage()));
        }
    }

    /**
     * Mark notification as read
     */
    @PutMapping("/notifications/{notificationId}/read")
    public ResponseEntity<?> markNotificationRead(@PathVariable Long notificationId, Authentication auth) {
        try {
            Customer customer = getCustomerFromAuth(auth);
            Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

            if (!notification.getUser().getUserId().equals(customer.getUser().getUserId())) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("Access denied", "Not your notification"));
            }

            notification.setIsRead(true);
            notificationRepository.save(notification);

            return ResponseEntity.ok(ApiResponse.success("Notification marked as read", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to mark notification", e.getMessage()));
        }
    }

    // ========== Helper Methods ==========

    private Customer getCustomerFromAuth(Authentication auth) {
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return customerRepository.findByUserId(user.getUserId())
            .orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    private CustomerProfileResponse buildCustomerProfile(Customer customer) {
        return new CustomerProfileResponse(
            customer.getCustomerId(),
            customer.getUser().getEmail(),
            customer.getFirstName(),
            customer.getLastName(),
            customer.getDateOfBirth(),
            customer.getPhone(),
            customer.getAddress(),
            customer.getCity(),
            customer.getState(),
            customer.getCountry(),
            customer.getPostalCode(),
            customer.getAccountType().name(),
            customer.getKycStatus().name(),
            customer.getProfileImageUrl()
        );
    }

    private AccountResponse buildAccountResponse(Account account) {
        return new AccountResponse(
            account.getAccountId(),
            account.getAccountNumber(),
            account.getAccountType().name(),
            account.getBalance(),
            account.getCurrency(),
            account.getStatus().name(),
            account.getInterestRate(),
            account.getOpenedDate()
        );
    }

    private TransactionResponse buildTransactionResponse(Transaction transaction) {
        return new TransactionResponse(
            transaction.getTransactionId(),
            transaction.getAccount().getAccountNumber(),
            transaction.getTransactionType().name(),
            transaction.getAmount(),
            transaction.getBalanceAfter(),
            transaction.getCounterpartyAccount(),
            transaction.getDescription(),
            transaction.getReferenceNumber(),
            transaction.getStatus().name(),
            transaction.getTransactionDate()
        );
    }

    private CardResponse buildCardResponse(Card card) {
        // Mask card number
        String maskedNumber = "**** **** **** " + card.getCardNumber().substring(card.getCardNumber().length() - 4);
        
        return new CardResponse(
            card.getCardId(),
            maskedNumber,
            card.getCardType().name(),
            card.getCardHolderName(),
            card.getExpiryDate(),
            card.getStatus().name(),
            card.getCreditLimit(),
            card.getAvailableCredit(),
            card.getIsInternationalEnabled(),
            card.getIsOnlineEnabled()
        );
    }

    private LoanResponse buildLoanResponse(Loan loan) {
        return new LoanResponse(
            loan.getLoanId(),
            loan.getLoanType().name(),
            loan.getLoanAmount(),
            loan.getInterestRate(),
            loan.getTenureMonths(),
            loan.getEmiAmount(),
            loan.getOutstandingAmount(),
            loan.getStatus().name(),
            loan.getApplicationDate(),
            loan.getDisbursementDate()
        );
    }

    private KycDocumentResponse buildKycResponse(KycDocument doc) {
        return new KycDocumentResponse(
            doc.getKycId(),
            doc.getDocumentType().name(),
            doc.getDocumentNumber(),
            doc.getDocumentUrl(),
            doc.getVerificationStatus().name(),
            doc.getUploadDate(),
            doc.getVerificationDate(),
            doc.getRejectionReason()
        );
    }

    private NotificationResponse buildNotificationResponse(Notification notification) {
        return new NotificationResponse(
            notification.getNotificationId(),
            notification.getTitle(),
            notification.getMessage(),
            notification.getNotificationType().name(),
            notification.getIsRead(),
            notification.getCreatedAt()
        );
    }
}
