package com.finsecure.service;

import com.finsecure.dto.*;
import com.finsecure.entity.*;
import com.finsecure.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final LoanRepository loanRepository;
    private final KycDocumentRepository kycDocumentRepository;
    private final NotificationService notificationService;
    private final TransactionService transactionService;
    private final CardService cardService;
    private final EmailService emailService;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public CustomerProfileResponse getProfile(String email) {
        Customer customer = customerRepository.findByUserEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        return mapCustomerToProfile(customer);
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(String email) {
        Customer customer = customerRepository.findByUserEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        List<Account> accounts = accountRepository.findByCustomerIdAndStatus(customer.getId(), Account.AccountStatus.ACTIVE);
        BigDecimal totalBalance = accounts.stream()
            .map(Account::getBalance)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<AccountResponse> accountResponses = accounts.stream()
            .map(this::mapAccountToResponse).collect(Collectors.toList());

        // Recent transactions for all accounts
        List<TransactionResponse> recentTxns = accounts.stream()
            .flatMap(acc -> transactionRepository.findRecentByAccountId(acc.getId(), PageRequest.of(0, 5)).stream())
            .map(this::mapTransactionToResponse)
            .limit(10)
            .collect(Collectors.toList());

        int activeLoans = (int) loanRepository.findByCustomerId(customer.getId()).stream()
            .filter(l -> l.getStatus() == Loan.LoanStatus.ACTIVE || l.getStatus() == Loan.LoanStatus.DISBURSED)
            .count();

        long unreadNotifications = notificationService.getUnreadCount(customer.getUser().getId());

        return DashboardResponse.builder()
            .profile(mapCustomerToProfile(customer))
            .totalBalance(totalBalance)
            .totalAccounts(accounts.size())
            .activeLoans(activeLoans)
            .activeCards(0) // cards fetched separately
            .unreadNotifications(unreadNotifications)
            .accounts(accountResponses)
            .recentTransactions(recentTxns)
            .build();
    }

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request, String email) {
        Customer customer = customerRepository.findByUserEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        String accountNumber = generateAccountNumber();

        Account account = Account.builder()
            .accountNumber(accountNumber)
            .customer(customer)
            .accountType(request.getAccountType())
            .balance(BigDecimal.ZERO)
            .minimumBalance(BigDecimal.valueOf(500))
            .currency("INR")
            .status(Account.AccountStatus.ACTIVE)
            .ifscCode(request.getIfscCode() != null ? request.getIfscCode() : "FINS0001234")
            .branchName(request.getBranchName() != null ? request.getBranchName() : "Main Branch")
            .build();

        account = accountRepository.save(account);

        notificationService.createNotification(customer.getUser().getId(),
            Notification.NotificationType.ACCOUNT,
            "Account Opened",
            "Your " + request.getAccountType().name() + " account " + accountNumber + " has been created.",
            accountNumber, "ACCOUNT");

        return mapAccountToResponse(account);
    }

    @Transactional
    public LoanResponse applyForLoan(LoanApplicationRequest request, String email) {
        Customer customer = customerRepository.findByUserEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        if (customer.getKycStatus() != Customer.KycStatus.APPROVED) {
            throw new IllegalStateException("KYC must be approved to apply for a loan");
        }

        BigDecimal interestRate = getLoanInterestRate(request.getLoanType());
        BigDecimal emi = calculateEmi(request.getPrincipalAmount(), interestRate, request.getTenureMonths());
        BigDecimal totalInterest = emi.multiply(BigDecimal.valueOf(request.getTenureMonths()))
            .subtract(request.getPrincipalAmount());

        String loanNumber = generateLoanNumber();

        Loan loan = Loan.builder()
            .loanNumber(loanNumber)
            .customer(customer)
            .loanType(request.getLoanType())
            .principalAmount(request.getPrincipalAmount())
            .interestRate(interestRate)
            .tenureMonths(request.getTenureMonths())
            .emiAmount(emi)
            .outstandingAmount(request.getPrincipalAmount())
            .totalInterest(totalInterest)
            .purpose(request.getPurpose())
            .status(Loan.LoanStatus.APPLIED)
            .build();

        loan = loanRepository.save(loan);

        notificationService.sendLoanNotification(customer.getUser().getId(), loanNumber, "APPLIED");
        emailService.sendLoanStatusEmail(customer.getUser().getEmail(), customer.getFirstName(), loanNumber, "APPLIED");

        return mapLoanToResponse(loan);
    }

    @Transactional
    public KycDocumentResponse uploadKycDocument(KycDocumentUploadRequest request, String email) {
        Customer customer = customerRepository.findByUserEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        KycDocument document = KycDocument.builder()
            .customer(customer)
            .documentType(request.getDocumentType())
            .documentNumber(request.getDocumentNumber())
            .filePath(request.getFilePath())
            .fileName(request.getFileName())
            .mimeType(request.getMimeType())
            .status(KycDocument.DocumentStatus.UPLOADED)
            .build();

        document = kycDocumentRepository.save(document);

        if (customer.getKycStatus() == Customer.KycStatus.PENDING) {
            customer.setKycStatus(Customer.KycStatus.SUBMITTED);
            customerRepository.save(customer);
        }

        return mapKycToResponse(document);
    }

    @Transactional(readOnly = true)
    public List<KycDocumentResponse> getKycDocuments(String email) {
        Customer customer = customerRepository.findByUserEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        return kycDocumentRepository.findByCustomerId(customer.getId())
            .stream().map(this::mapKycToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LoanResponse> getLoans(String email) {
        Customer customer = customerRepository.findByUserEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        return loanRepository.findByCustomerId(customer.getId())
            .stream().map(this::mapLoanToResponse).collect(Collectors.toList());
    }

    private BigDecimal getLoanInterestRate(Loan.LoanType loanType) {
        return switch (loanType) {
            case HOME -> BigDecimal.valueOf(8.5);
            case CAR -> BigDecimal.valueOf(9.5);
            case PERSONAL -> BigDecimal.valueOf(12.5);
            case EDUCATION -> BigDecimal.valueOf(7.5);
            case BUSINESS -> BigDecimal.valueOf(11.0);
            case GOLD -> BigDecimal.valueOf(10.0);
        };
    }

    private BigDecimal calculateEmi(BigDecimal principal, BigDecimal annualRate, int months) {
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);
        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        BigDecimal pow = onePlusR.pow(months, MathContext.DECIMAL128);
        BigDecimal numerator = principal.multiply(monthlyRate).multiply(pow);
        BigDecimal denominator = pow.subtract(BigDecimal.ONE);
        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    private String generateAccountNumber() {
        return "FINS" + System.currentTimeMillis();
    }

    private String generateLoanNumber() {
        return "LN" + System.currentTimeMillis();
    }

    private CustomerProfileResponse mapCustomerToProfile(Customer customer) {
        return CustomerProfileResponse.builder()
            .id(customer.getId())
            .userId(customer.getUser().getId())
            .email(customer.getUser().getEmail())
            .username(customer.getUser().getUsername())
            .firstName(customer.getFirstName())
            .lastName(customer.getLastName())
            .phone(customer.getPhone())
            .dateOfBirth(customer.getDateOfBirth())
            .panNumber(customer.getPanNumber())
            .aadharNumber(customer.getAadharNumber())
            .address(customer.getAddress())
            .city(customer.getCity())
            .state(customer.getState())
            .pinCode(customer.getPinCode())
            .kycStatus(customer.getKycStatus())
            .emailVerified(customer.getUser().getEmailVerified())
            .createdAt(customer.getCreatedAt())
            .build();
    }

    private AccountResponse mapAccountToResponse(Account account) {
        return AccountResponse.builder()
            .id(account.getId())
            .accountNumber(account.getAccountNumber())
            .accountType(account.getAccountType())
            .balance(account.getBalance())
            .minimumBalance(account.getMinimumBalance())
            .currency(account.getCurrency())
            .status(account.getStatus())
            .ifscCode(account.getIfscCode())
            .branchName(account.getBranchName())
            .createdAt(account.getCreatedAt())
            .build();
    }

    private TransactionResponse mapTransactionToResponse(Transaction txn) {
        return TransactionResponse.builder()
            .id(txn.getId())
            .referenceNumber(txn.getReferenceNumber())
            .accountNumber(txn.getAccount().getAccountNumber())
            .type(txn.getType())
            .mode(txn.getMode())
            .amount(txn.getAmount())
            .balanceAfter(txn.getBalanceAfter())
            .description(txn.getDescription())
            .targetAccountNumber(txn.getTargetAccountNumber())
            .status(txn.getStatus())
            .createdAt(txn.getCreatedAt())
            .build();
    }

    private LoanResponse mapLoanToResponse(Loan loan) {
        return LoanResponse.builder()
            .id(loan.getId())
            .loanNumber(loan.getLoanNumber())
            .loanType(loan.getLoanType())
            .principalAmount(loan.getPrincipalAmount())
            .interestRate(loan.getInterestRate())
            .tenureMonths(loan.getTenureMonths())
            .emiAmount(loan.getEmiAmount())
            .outstandingAmount(loan.getOutstandingAmount())
            .totalInterest(loan.getTotalInterest())
            .status(loan.getStatus())
            .disbursementDate(loan.getDisbursementDate())
            .nextEmiDate(loan.getNextEmiDate())
            .purpose(loan.getPurpose())
            .rejectionReason(loan.getRejectionReason())
            .createdAt(loan.getCreatedAt())
            .build();
    }

    private KycDocumentResponse mapKycToResponse(KycDocument doc) {
        return KycDocumentResponse.builder()
            .id(doc.getId())
            .customerId(doc.getCustomer().getId())
            .customerName(doc.getCustomer().getFirstName() + " " + doc.getCustomer().getLastName())
            .documentType(doc.getDocumentType())
            .documentNumber(doc.getDocumentNumber())
            .status(doc.getStatus())
            .rejectionReason(doc.getRejectionReason())
            .verifiedAt(doc.getVerifiedAt())
            .createdAt(doc.getCreatedAt())
            .build();
    }
}
