package com.covenantcode.crm.kafka;

import java.time.Instant;
import java.util.UUID;

public record UserStatusChangedEvent(
        UUID eventId,
        Long userId,
        String email,
        boolean enabled,
        Instant changedAt) {
}
