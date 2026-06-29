package com.mv.event_ledger_domain.repository;

import com.mv.event_ledger_domain.entity.Event;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findByEventId(String eventId);

    boolean existsByEventId(String eventId);

    List<Event> findByAccountIdOrderByEventTimestampAsc(String accountId);
}
