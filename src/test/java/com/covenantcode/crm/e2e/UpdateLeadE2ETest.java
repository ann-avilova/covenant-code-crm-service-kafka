package com.covenantcode.crm.e2e;

import java.util.HashMap;
import java.util.Map;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;

public class UpdateLeadE2ETest extends BaseE2ETest {

    private Faker faker;

    @BeforeEach
    public void setUp() {
        this.faker = new Faker();
    }

    @Disabled("BUG #7: PUT /leads/{id} с устаревшей формой (открыта до чужого сохранения) " +
            "затирает изменения другого пользователя: возвращается 200 вместо 409")
    @DisplayName("PUT /leads/{id} с формой, открытой до чужого сохранения -> 409, изменения первого пользователя сохранились")
    @Test
    void updateLead_outdatedForm_returns409() {
        //Given:
        String email = faker.internet().emailAddress();
        String firstName = faker.name().firstName();

        String adminToken = adminToken();
        var manager = api.registerManager(email, "123456789");
        var managerId = manager.id();
        var leadId = api.createLead(adminToken, firstName, null);

        Map<String, Object> formA = new HashMap<>();
        formA.put("firstName", firstName);
        formA.put("phone", "+79991113344");

        Map<String, Object> formB = new HashMap<>();
        formB.put("firstName", firstName);
        formB.put("phone", "+79991112233");
        formB.put("assignedManagerId", managerId);

        //When:
        api.as(adminToken)
                .body(formA)
                .put("/leads/{id}", leadId)

                //Then:
                .then()
                .statusCode(200);

        //When:
        api.as(adminToken)
                .body(formB)
                .put("/leads/{id}", leadId)
                //Then:

                .then()
                .statusCode(409);

        api.as(adminToken)
                .get("/leads/{id}", leadId)
                .then()
                .statusCode(200)
                .body("phone", equalTo("+79991113344"));
    }
}
