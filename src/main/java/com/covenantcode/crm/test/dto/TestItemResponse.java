package com.covenantcode.crm.test.dto;

import java.time.Instant;

public record TestItemResponse(
        Long id,
        String name,
        String description,
        Instant createdAt,
        Instant updatedAt
) {}
