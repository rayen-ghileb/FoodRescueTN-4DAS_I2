package foodrescuetn4das_i2.matching;

import foodrescuetn4das_i2.matching.entity.*;
import foodrescuetn4das_i2.matching.repository.PickupStatusHistoryRepository;
import foodrescuetn4das_i2.matching.service.MatchingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MatchingApplicationTests {
    @Autowired MatchingService service;
    @Autowired PickupStatusHistoryRepository historyRepo;

    @Test void contextLoads() { }

    @Test void fullLocalLifecycleCreatesHistory() {
        DonationMatch match = service.createMatch(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("15"));
        assertEquals("PENDING", match.getStatus());
        Pickup pickup = service.createPickup(match.getId(), LocalDateTime.now().plusDays(1), "test-user");
        assertEquals("SCHEDULED", pickup.getStatus());
        DriverAssignment assignment = service.assignDriver(pickup.getId(), UUID.randomUUID(), "test-user");
        assertEquals("ASSIGNED", assignment.getStatus());
        service.updatePickupStatus(pickup.getId(), "IN_TRANSIT", "test-user");
        Pickup delivered = service.updatePickupStatus(pickup.getId(), "DELIVERED", "test-user");
        assertNotNull(delivered.getDeliveredAt());
        assertEquals("FULFILLED", service.getMatch(match.getId()).getStatus());
        assertEquals(4, service.statusHistory(pickup.getId()).size());
    }

    @Test void rejectsSkipFromScheduledStraightToDelivered() {
        DonationMatch match = service.createMatch(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("10"));
        Pickup pickup = service.createPickup(match.getId(), LocalDateTime.now().plusDays(1), "test");
        assertThrows(ResponseStatusException.class,
                () -> service.updatePickupStatus(pickup.getId(), "DELIVERED", "test"));
    }

    @Test void allowsReassignmentAfterCancellation() {
        DonationMatch match = service.createMatch(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("4"));
        Pickup pickup = service.createPickup(match.getId(), LocalDateTime.now().plusDays(1), "test");
        DriverAssignment first = service.assignDriver(pickup.getId(), UUID.randomUUID(), "test");
        assertThrows(ResponseStatusException.class,
                () -> service.assignDriver(pickup.getId(), UUID.randomUUID(), "test"));
        service.cancelAssignment(first.getId(), "test");
        DriverAssignment second = service.assignDriver(pickup.getId(), UUID.randomUUID(), "test");
        assertEquals("ASSIGNED", second.getStatus());
    }
}
