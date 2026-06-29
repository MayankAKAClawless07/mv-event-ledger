package com.mv.event_ledger_domain.repository;

import com.mv.event_ledger_domain.entity.AccountTransaction;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountTransactionRepository extends JpaRepository<AccountTransaction, Long> {

    Optional<AccountTransaction> findByEventId(String eventId);

    boolean existsByEventId(String eventId);

    List<AccountTransaction> findByAccountIdOrderByEventTimestampAsc(String accountId);

    List<AccountTransaction> findByAccountIdOrderByEventTimestampDesc(String accountId, Pageable pageable);
}
