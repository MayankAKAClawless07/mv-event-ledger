package com.mv.event_gateway_api.controller;

import com.mv.event_gateway_api.service.EventApplicationService;
import com.mv.event_gateway_api.service.EventApplicationService.SubmitEventResult;
import com.mv.event_ledger_domain.model.Event;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/events")
public class EventController {

    private static final Logger log = LoggerFactory.getLogger(EventController.class);

    private final EventApplicationService eventService;

    public EventController(EventApplicationService eventService) {
        this.eventService = eventService;
    }

    @Operation(summary = "Submit a transaction event")
    @PostMapping
    public ResponseEntity<Event> submitEvent(@Valid @RequestBody Event request) {
        log.info("Submitting event eventId={} accountId={}", request.getEventId(), request.getAccountId());
        SubmitEventResult result = eventService.submit(request);
        HttpStatus status = result.duplicate() ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(result.event());
    }

    @Operation(summary = "Retrieve one event by event ID")
    @GetMapping("/{id}")
    public Event getEvent(@PathVariable String id) {
        return eventService.getEvent(id);
    }

    @Operation(summary = "List events for an account ordered by event timestamp")
    @GetMapping
    public List<Event> getEventsForAccount(@RequestParam("account") String accountId) {
        return eventService.getEventsForAccount(accountId);
    }
}
