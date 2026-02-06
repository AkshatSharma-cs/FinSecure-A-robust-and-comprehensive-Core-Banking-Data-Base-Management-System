package com.finsecure.service;

import static com.finsecure.dto.DTOs.*;

import com.finsecure.entity.Account;
import com.finsecure.entity.Customer;
import com.finsecure.entity.Transaction;
import com.finsecure.repository.AccountRepository;
import com.finsecure.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public TransactionService(
            AccountRepository accountRepository,
            TransactionRepository transactionRepository
    ) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Transaction processTransaction(TransactionRequest request, Customer customer) {

        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // Ownership check
        if (!account.getCustomer().getCustomerId().equals(customer.getCustomerId())) {
            throw new RuntimeException("Account does not belong to customer");
        }

        // Status check
        if (account.getStatus() != Account.Status.ACTIVE) {
            throw new RuntimeException("Account is not active");
        }

        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setTransactionType(
                Transaction.TransactionType.valueOf(request.getTransactionType())
        );
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setReferenceNumber(generateReferenceNumber());
        transaction.setOtpVerified(true);

        BigDecimal newBalance = account.getBalance();

        switch (transaction.getTransactionType()) {

            case DEPOSIT -> {
                newBalance = newBalance.add(request.getAmount());
            }

            case WITHDRAWAL -> {
                if (newBalance.compareTo(request.getAmount()) < 0) {
                    throw new RuntimeException("Insufficient balance");
                }
                newBalance = newBalance.subtract(request.getAmount());
            }

            case TRANSFER_OUT -> {
                if (newBalance.compareTo(request.getAmount()) < 0) {
                    throw new RuntimeException("Insufficient balance");
                }

                Account counterparty = accountRepository
                        .findByAccountNumber(request.getCounterpartyAccount())
                        .orElseThrow(() -> new RuntimeException("Counterparty account not found"));

                // Debit sender
                newBalance = newBalance.subtract(request.getAmount());
                account.setBalance(newBalance);
                accountRepository.save(account);

                // Credit receiver
                counterparty.setBalance(
                        counterparty.getBalance().add(request.getAmount())
                );
                accountRepository.save(counterparty);

                // Counterparty transaction
                Transaction transferIn = new Transaction();
                transferIn.setAccount(counterparty);
                transferIn.setTransactionType(Transaction.TransactionType.TRANSFER_IN);
                transferIn.setAmount(request.getAmount());
                transferIn.setCounterpartyAccount(account.getAccountNumber());
                transferIn.setDescription("Transfer from " + account.getAccountNumber());
                transferIn.setReferenceNumber(generateReferenceNumber());
                transferIn.setBalanceAfter(counterparty.getBalance());
                transferIn.setStatus(Transaction.Status.SUCCESS);
                transferIn.setOtpVerified(true);

                transactionRepository.save(transferIn);
            }

            default -> throw new RuntimeException("Unsupported transaction type");
        }

        account.setBalance(newBalance);
        accountRepository.save(account);

        transaction.setBalanceAfter(newBalance);
        transaction.setStatus(Transaction.Status.SUCCESS);

        return transactionRepository.save(transaction);
    }

    private String generateReferenceNumber() {
        return "TXN-" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 16)
                .toUpperCase();
    }
}
