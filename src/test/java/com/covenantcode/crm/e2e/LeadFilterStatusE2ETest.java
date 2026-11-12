package com.covenantcode.crm.e2e;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.empty;

public class LeadFilterStatusE2ETest extends BaseE2ETest {

    @DisplayName("Админ делает запрос GET /leads?status=REJECTED при отсутствии лидов -> 200 + пустой список")
    @Test
    void getLeadsByStatusWithoutLeads_returnsEmptyList () {
        //Given:
        String adminToken = adminToken();

        //When:
        api.as(adminToken)
                .get("/leads?status=REJECTED")
        //Then:
                .then()
                .statusCode(200)
                .body("content", empty());
    }
}
