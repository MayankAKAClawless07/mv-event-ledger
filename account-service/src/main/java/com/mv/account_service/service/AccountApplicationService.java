package com.mv.account_service.service;

import com.mv.event_ledger_domain.entity.Account;
import com.mv.event_ledger_domain.entity.AccountTransaction;
import com.mv.event_ledger_domain.enums.Type;
import com.mv.event_ledger_domain.repository.AccountRepository;
import com.mv.event_ledger_domain.repository.AccountTransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountApplicationService {

    private final AccountRepository accountRepository;
    private final AccountTransactionRepository transactionRepository;

    public AccountApplicationService(AccountRepository accountRepository,
                                     AccountTransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public com.mv.event_ledger_domain.model.Account createAccount(com.mv.event_ledger_domain.model.Account request) {
        Account account = accountRepository.findByAccountId(request.getAccountId())
                .orElseGet(() -> Account.builder()
                        .accountId(request.getAccountId())
                        .balance(request.getBalance() == null ? BigDecimal.ZERO : request.getBalance())
                        .currency(request.getCurrency())
                        .build());
        return toModel(accountRepository.save(account), List.of());
    }

    @Transactional
    public com.mv.event_ledger_domain.model.AccountTransaction applyTransaction(
            String accountId,
            com.mv.event_ledger_domain.model.AccountTransaction request) {
        return transactionRepository.findByEventId(request.getEventId())
                .map(this::toModel)
                .orElseGet(() -> applyNewTransaction(accountId, request));
    }

    @Transactional(readOnly = true)
    public com.mv.event_ledger_domain.model.AccountBalance getBalance(String accountId) {
        Account account = accountRepository.findByAccountId(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found: " + accountId));
        return com.mv.event_ledger_domain.model.AccountBalance.builder()
                .accountId(account.getAccountId())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .build();
    }

    @Transactional(readOnly = true)
    public com.mv.event_ledger_domain.model.Account getAccount(String accountId) {
        Account account = accountRepository.findByAccountId(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found: " + accountId));
        List<com.mv.event_ledger_domain.model.AccountTransaction> recent = transactionRepository
                .findByAccountIdOrderByEventTimestampDesc(accountId, PageRequest.of(0, 10))
                .stream()
                .map(this::toModel)
                .toList();
        return com.mv.event_ledger_domain.model.Account.builder()
                .accountId(account.getAccountId())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .recentTransactions(recent)
                .build();
    }

    private com.mv.event_ledger_domain.model.AccountTransaction applyNewTransaction(
            String accountId,
            com.mv.event_ledger_domain.model.AccountTransaction request) {
        Account account = accountRepository.findByAccountId(accountId)
                .orElseGet(() -> Account.builder()
                        .accountId(accountId)
                        .currency(request.getCurrency())
                        .balance(BigDecimal.ZERO)
                        .build());
        BigDecimal signedAmount = request.getType() == Type.CREDIT
                ? request.getAmount()
                : request.getAmount().negate();
        account.setBalance(account.getBalance().add(signedAmount));
        account.setCurrency(request.getCurrency());
        accountRepository.save(account);

        AccountTransaction transaction = AccountTransaction.builder()
                .eventId(request.getEventId())
                .accountId(accountId)
                .type(request.getType())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .eventTimestamp(request.getEventTimestamp())
                .build();
        return toModel(transactionRepository.save(transaction));
    }

    private com.mv.event_ledger_domain.model.AccountTransaction toModel(AccountTransaction transaction) {
        return com.mv.event_ledger_domain.model.AccountTransaction.builder()
                .eventId(transaction.getEventId())
                .accountId(transaction.getAccountId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .eventTimestamp(transaction.getEventTimestamp())
                .build();
    }

    private com.mv.event_ledger_domain.model.Account toModel(
            Account account,
            List<com.mv.event_ledger_domain.model.AccountTransaction> recentTransactions) {
        return com.mv.event_ledger_domain.model.Account.builder()
                .accountId(account.getAccountId())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .recentTransactions(recentTransactions)
                .build();
    }
}
