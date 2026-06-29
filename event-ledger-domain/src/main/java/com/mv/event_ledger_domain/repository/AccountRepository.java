package com.mv.event_ledger_domain.repository;

import com.mv.event_ledger_domain.entity.Account;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountId(String accountId);

    boolean existsByAccountId(String accountId);
}
