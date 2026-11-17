package foodrescuetn4das_i2.matching.service;

import foodrescuetn4das_i2.matching.entity.OutboxEvent;
import foodrescuetn4das_i2.matching.repository.OutboxEventRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class DomainEventRecorder {
    private final OutboxEventRepository repository;
    public DomainEventRecorder(OutboxEventRepository repository) { this.repository = repository; }

    public void record(String eventType, UUID aggregateId, String payload) {
        OutboxEvent event = new OutboxEvent();
        event.setEventType(eventType);
        event.setAggregateId(aggregateId);
        event.setPayload(payload);
        event.setCreatedAt(LocalDateTime.now());
        event.setPublished(false);
        repository.save(event);
    }
}
