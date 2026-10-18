package com.covenantcode.crm.test.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TestItemRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 255)
        String name,

        String description
) {}
