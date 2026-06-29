package com.mv.event_gateway_api.service;

import com.mv.event_gateway_api.client.AccountServiceClient;
import com.mv.event_ledger_domain.entity.Event;
import com.mv.event_ledger_domain.enums.EventStatus;
import com.mv.event_ledger_domain.model.AccountTransaction;
import com.mv.event_ledger_domain.repository.EventRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventApplicationService {

    private final EventRepository eventRepository;
    private final AccountServiceClient accountServiceClient;
    private final AccountValidationService accountValidationService;

    public EventApplicationService(EventRepository eventRepository,
                                   AccountServiceClient accountServiceClient,
                                   AccountValidationService accountValidationService) {
        this.eventRepository = eventRepository;
        this.accountServiceClient = accountServiceClient;
        this.accountValidationService = accountValidationService;
    }

    @Transactional
    public SubmitEventResult submit(com.mv.event_ledger_domain.model.Event request) {
        return eventRepository.findByEventId(request.getEventId())
                .map(event -> new SubmitEventResult(toModel(event), true))
                .orElseGet(() -> createAndApply(request));
    }

    @Transactional(readOnly = true)
    public com.mv.event_ledger_domain.model.Event getEvent(String eventId) {
        return eventRepository.findByEventId(eventId)
                .map(this::toModel)
                .orElseThrow(() -> new EventNotFoundException("Event not found: " + eventId));
    }

    @Transactional(readOnly = true)
    public List<com.mv.event_ledger_domain.model.Event> getEventsForAccount(String accountId) {
        return eventRepository.findByAccountIdOrderByEventTimestampAsc(accountId)
                .stream()
                .map(this::toModel)
                .toList();
    }

    private SubmitEventResult createAndApply(com.mv.event_ledger_domain.model.Event request) {
        accountValidationService.validateAccountId(request.getAccountId());
        AccountTransaction transaction = AccountTransaction.builder()
                .eventId(request.getEventId())
                .accountId(request.getAccountId())
                .type(request.getType())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .eventTimestamp(request.getEventTimestamp())
                .build();
        accountServiceClient.applyTransaction(request.getAccountId(), transaction);

        Event saved = eventRepository.save(Event.builder()
                .eventId(request.getEventId())
                .accountId(request.getAccountId())
                .type(request.getType())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .eventTimestamp(request.getEventTimestamp())
                .metadata(request.getMetadata())
                .status(EventStatus.APPLIED)
                .build());
        return new SubmitEventResult(toModel(saved), false);
    }

    private com.mv.event_ledger_domain.model.Event toModel(Event event) {
        return com.mv.event_ledger_domain.model.Event.builder()
                .eventId(event.getEventId())
                .accountId(event.getAccountId())
                .type(event.getType())
                .amount(event.getAmount())
                .currency(event.getCurrency())
                .eventTimestamp(event.getEventTimestamp())
                .metadata(event.getMetadata())
                .status(event.getStatus())
                .failureReason(event.getFailureReason())
                .build();
    }

    public record SubmitEventResult(com.mv.event_ledger_domain.model.Event event, boolean duplicate) {
    }
}
