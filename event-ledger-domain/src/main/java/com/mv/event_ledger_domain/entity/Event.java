package com.mv.event_ledger_domain.entity;

import com.mv.event_ledger_domain.enums.EventStatus;
import com.mv.event_ledger_domain.enums.Type;
import com.mv.event_ledger_domain.util.RandomIdGenerator;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "events",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_events_event_id", columnNames = "event_id")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, length = 80, updatable = false)
    private String eventId;

    @Column(name = "account_id", nullable = false, length = 80)
    private String accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 20)
    private Type type;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "event_timestamp", nullable = false)
    private Instant eventTimestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EventStatus status;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "event_metadata",
            joinColumns = @JoinColumn(name = "event_pk", referencedColumnName = "id")
    )
    @MapKeyColumn(name = "metadata_key", length = 120)
    @Column(name = "metadata_value", length = 1000)
    @Builder.Default
    private Map<String, String> metadata = new LinkedHashMap<>();

    @PrePersist
    void prePersist() {
        if (eventId == null || eventId.isBlank()) {
            eventId = RandomIdGenerator.randomEventId();
        }
        if (accountId == null || accountId.isBlank()) {
            accountId = RandomIdGenerator.randomAccountId();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (status == null) {
            status = EventStatus.RECEIVED;
        }
    }
}
