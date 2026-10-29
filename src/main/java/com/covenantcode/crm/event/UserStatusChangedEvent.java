package com.covenantcode.crm.event;

import java.time.Instant;
import java.util.UUID;

public record UserStatusChangedEvent(
        UUID eventId,
        Long userId,
        String email,
        boolean enabled,
        Instant changedAt) {
}
