package com.covenantcode.crm.dto.lead;

import com.covenantcode.crm.entity.enums.LeadStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeadStatusUpdateRequest {

    @NotNull
    private LeadStatus status;

}
