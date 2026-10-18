package com.covenantcode.crm.dto.group;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserShortResponse {

    private Long id;
    private String firstName;
    private String lastName;
}
