package com.covenantcode.crm.e2e;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.empty;

public class LeadFilterStatusE2ETest extends BaseE2ETest {

    @DisplayName("Админ делает запрос GET /leads?status=REJECTED при отсутствии лидов с таким статусом -> 200 + пустой список")
    @Test
    void getLeadsByStatusRejected_onlyOtherStatuses_returnsEmptyList() {
        //Given:
        String adminToken = adminToken();

        var leadId1 = api.createLead(adminToken, "Mikael", null);
        var leadId2 = api.createLead(adminToken, "Anna", null);
        api.createLead(adminToken, "Helga", null);
        api.createLead(adminToken, "Mike", null);


        api.changeLeadStatus(adminToken, leadId1, "IN_PROGRESS");
        api.changeLeadStatus(adminToken, leadId2, "CONTACTED");


        //When:
        api.as(adminToken)
                .get("/leads?status=REJECTED")
        //Then:
                .then()
                .statusCode(200)
                .body("content", empty());
    }
}
