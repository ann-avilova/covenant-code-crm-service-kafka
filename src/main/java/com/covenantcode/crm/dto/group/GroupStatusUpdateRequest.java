package com.covenantcode.crm.dto.group;

import com.covenantcode.crm.entity.enums.GroupStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupStatusUpdateRequest {

    @NotNull(message = "Статус группы обязателен для заполнения")
    private GroupStatus status;
}
