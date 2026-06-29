package com.mv.event_ledger_domain.entity;

import com.mv.event_ledger_domain.enums.Type;
import com.mv.event_ledger_domain.util.RandomIdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "account_transactions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_account_transactions_event_id", columnNames = "event_id")
        },
        indexes = {
                @Index(name = "idx_account_transactions_account_time", columnList = "account_id,event_timestamp")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, length = 80, updatable = false)
    private String eventId;

    @Column(name = "account_id", nullable = false, length = 80)
    private String accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private Type type;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "event_timestamp", nullable = false)
    private Instant eventTimestamp;

    @Column(name = "applied_at", nullable = false, updatable = false)
    private Instant appliedAt;

    @PrePersist
    void prePersist() {
        if (eventId == null || eventId.isBlank()) {
            eventId = RandomIdGenerator.randomEventId();
        }
        if (accountId == null || accountId.isBlank()) {
            accountId = RandomIdGenerator.randomAccountId();
        }
        if (appliedAt == null) {
            appliedAt = Instant.now();
        }
    }
}
