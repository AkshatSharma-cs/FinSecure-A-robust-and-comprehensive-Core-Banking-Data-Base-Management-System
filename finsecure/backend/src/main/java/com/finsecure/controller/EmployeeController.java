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

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Employee Portal Controller
 * All endpoints require EMPLOYEE role
 */
@RestController
@RequestMapping("/employee")
@PreAuthorize("hasRole('EMPLOYEE')")
@CrossOrigin(origins = "https://localhost:3001")
public class EmployeeController {

    @Autowired private UserRepository userRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private KycDocumentRepository kycDocumentRepository;
    @Autowired private LoanRepository loanRepository;
    @Autowired private AccountRepository accountRepository;
    
    @Autowired private NotificationService notificationService;
    @Autowired private AuditService auditService;
    @Autowired private EmailService emailService;

    /**
     * Get employee dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(Authentication auth) {
        try {
            Employee employee = getEmployeeFromAuth(auth);

            // Get statistics based on department
            Long pendingKyc = (long) kycDocumentRepository.findPendingDocuments().size();
            Long pendingLoans = (long) loanRepository.findByStatus(Loan.Status.APPLIED).size();
            Long pendingCustomers = (long) customerRepository.findPendingKycCustomers().size();

            var dashboardData = new Object() {
                public final String employeeName = employee.getFirstName() + " " + employee.getLastName();
                public final String department = employee.getDepartment().name();
                public final String designation = employee.getDesignation();
                public final Long pendingKycDocuments = pendingKyc;
                public final Long pendingLoanApplications = pendingLoans;
                public final Long pendingKycApprovals = pendingCustomers;
            };

            return ResponseEntity.ok(ApiResponse.success("Dashboard loaded", dashboardData));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to load dashboard", e.getMessage()));
        }
    }

    /**
     * Get all pending KYC documents for verification
     */
    @GetMapping("/kyc/pending")
    public ResponseEntity<?> getPendingKycDocuments(Authentication auth) {
        try {
            List<KycDocument> documents = kycDocumentRepository.findPendingDocuments();
            
            List<KycDocumentResponse> response = documents.stream()
                .map(this::buildKycResponse)
                .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("Pending KYC documents loaded", response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to load KYC documents", e.getMessage()));
        }
    }

    /**
     * Get KYC documents for a specific customer
     */
    @GetMapping("/kyc/customer/{customerId}")
    public ResponseEntity<?> getCustomerKycDocuments(@PathVariable Long customerId, Authentication auth) {
        try {
            Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

            List<KycDocument> documents = kycDocumentRepository.findByCustomerId(customerId);
            
            var responseData = new Object() {
                public final CustomerProfileResponse customer = buildCustomerProfile(customer);
                public final List<KycDocumentResponse> documents = documents.stream()
                    .map(EmployeeController.this::buildKycResponse)
                    .collect(Collectors.toList());
            };

            return ResponseEntity.ok(ApiResponse.success("Customer KYC documents loaded", responseData));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to load KYC documents", e.getMessage()));
        }
    }

    /**
     * Verify KYC document (Approve/Reject)
     */
    @PostMapping("/kyc/verify")
    public ResponseEntity<?> verifyKycDocument(@RequestBody KycVerificationRequest request,
                                               Authentication auth,
                                               HttpServletRequest httpRequest) {
        try {
            Employee employee = getEmployeeFromAuth(auth);

            // Verify employee has permission (KYC_VERIFICATION department)
            if (employee.getDepartment() != Employee.Department.KYC_VERIFICATION &&
                employee.getDepartment() != Employee.Department.MANAGEMENT) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("Access denied", "Only KYC verification team can verify documents"));
            }

            KycDocument document = kycDocumentRepository.findById(request.getKycId())
                .orElseThrow(() -> new RuntimeException("KYC document not found"));

            Customer customer = document.getCustomer();

            // Update document status
            document.setVerificationStatus(
                KycDocument.VerificationStatus.valueOf(request.getStatus())
            );
            document.setVerifiedBy(employee);
            document.setVerificationDate(LocalDateTime.now());

            if (request.getStatus().equals("REJECTED")) {
                document.setRejectionReason(request.getRejectionReason());
            }

            kycDocumentRepository.save(document);

            // Check if all required documents are approved
            List<KycDocument> allDocs = kycDocumentRepository.findByCustomerId(customer.getCustomerId());
            boolean allApproved = allDocs.stream()
                .allMatch(doc -> doc.getVerificationStatus() == KycDocument.VerificationStatus.APPROVED);

            // Update customer KYC status
            if (allApproved && allDocs.size() >= 3) { // Require at least 3 documents
                customer.setKycStatus(Customer.KycStatus.APPROVED);
                customerRepository.save(customer);

                // Activate all pending accounts
                List<Account> accounts = accountRepository.findByCustomerIdAndStatus(
                    customer.getCustomerId(), 
                    Account.Status.PENDING
                );
                accounts.forEach(account -> {
                    account.setStatus(Account.Status.ACTIVE);
                    accountRepository.save(account);
                });

                // Send approval notification
                notificationService.sendKycApprovalNotification(customer.getUser());
                emailService.sendKycApprovalEmail(customer.getUser().getEmail(), customer.getFirstName());

            } else if (request.getStatus().equals("REJECTED")) {
                customer.setKycStatus(Customer.KycStatus.REJECTED);
                customerRepository.save(customer);

                // Send rejection notification
                notificationService.sendKycRejectionNotification(
                    customer.getUser(), 
                    request.getRejectionReason()
                );
                emailService.sendKycRejectionEmail(
                    customer.getUser().getEmail(), 
                    customer.getFirstName(),
                    request.getRejectionReason()
                );
            }

            // Log action
            AuditLog.ActionType actionType = request.getStatus().equals("APPROVED") 
                ? AuditLog.ActionType.KYC_APPROVE 
                : AuditLog.ActionType.KYC_REJECT;
            
            auditService.logAction(employee.getUser().getUserId(), actionType,
                "KycDocument", document.getKycId(),
                "KYC " + request.getStatus() + " for customer: " + customer.getCustomerId(),
                httpRequest);

            KycDocumentResponse response = buildKycResponse(document);
            return ResponseEntity.ok(ApiResponse.success("KYC verification completed", response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("KYC verification failed", e.getMessage()));
        }
    }

    /**
     * Get all customers with pending KYC
     */
    @GetMapping("/customers/pending-kyc")
    public ResponseEntity<?> getPendingKycCustomers(Authentication auth) {
        try {
            List<Customer> customers = customerRepository.findPendingKycCustomers();
            
            List<CustomerProfileResponse> response = customers.stream()
                .map(this::buildCustomerProfile)
                .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("Pending KYC customers loaded", response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to load customers", e.getMessage()));
        }
    }

    /**
     * Get customer details
     */
    @GetMapping("/customers/{customerId}")
    public ResponseEntity<?> getCustomerDetails(@PathVariable Long customerId, Authentication auth) {
        try {
            Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

            List<Account> accounts = accountRepository.findByCustomerId(customerId);
            List<KycDocument> kycDocs = kycDocumentRepository.findByCustomerId(customerId);

            var responseData = new Object() {
                public final CustomerProfileResponse profile = buildCustomerProfile(customer);
                public final List<AccountResponse> accounts = accounts.stream()
                    .map(EmployeeController.this::buildAccountResponse)
                    .collect(Collectors.toList());
                public final List<KycDocumentResponse> kycDocuments = kycDocs.stream()
                    .map(EmployeeController.this::buildKycResponse)
                    .collect(Collectors.toList());
            };

            return ResponseEntity.ok(ApiResponse.success("Customer details loaded", responseData));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to load customer details", e.getMessage()));
        }
    }

    /**
     * Get all pending loan applications
     */
    @GetMapping("/loans/pending")
    public ResponseEntity<?> getPendingLoans(Authentication auth) {
        try {
            Employee employee = getEmployeeFromAuth(auth);

            // Verify employee has permission
            if (employee.getDepartment() != Employee.Department.LOAN_PROCESSING &&
                employee.getDepartment() != Employee.Department.MANAGEMENT) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("Access denied", "Only loan processing team can view loan applications"));
            }

            List<Loan> loans = loanRepository.findByStatus(Loan.Status.APPLIED);
            
            List<LoanResponse> response = loans.stream()
                .map(this::buildLoanResponse)
                .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("Pending loan applications loaded", response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to load loan applications", e.getMessage()));
        }
    }

    /**
     * Search customers
     */
    @GetMapping("/customers/search")
    public ResponseEntity<?> searchCustomers(@RequestParam String query, Authentication auth) {
        try {
            // Simple search implementation - can be enhanced with full-text search
            List<Customer> customers = customerRepository.findAll().stream()
                .filter(c -> 
                    c.getFirstName().toLowerCase().contains(query.toLowerCase()) ||
                    c.getLastName().toLowerCase().contains(query.toLowerCase()) ||
                    c.getUser().getEmail().toLowerCase().contains(query.toLowerCase()) ||
                    c.getPhone().contains(query)
                )
                .limit(20)
                .collect(Collectors.toList());

            List<CustomerProfileResponse> response = customers.stream()
                .map(this::buildCustomerProfile)
                .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("Search results", response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Search failed", e.getMessage()));
        }
    }

    // ========== Helper Methods ==========

    private Employee getEmployeeFromAuth(Authentication auth) {
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return employeeRepository.findByUserId(user.getUserId())
            .orElseThrow(() -> new RuntimeException("Employee not found"));
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
}
