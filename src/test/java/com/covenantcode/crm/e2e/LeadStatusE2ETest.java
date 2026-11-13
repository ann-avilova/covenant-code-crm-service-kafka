package com.covenantcode.crm.e2e;

import java.util.Map;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;

public class LeadStatusE2ETest extends BaseE2ETest {

    private Faker faker;

    @BeforeEach
    public void setUp() {
        this.faker = new Faker();
    }

    @DisplayName("При изменении статуса на CONVERTED_TO_STUDENT ошибка 409, статус лида остается NEW")
    @Test
    void statusLeadNoChange_after409() {
        //Given:

        String emailRandom = faker.internet().emailAddress();
        var manager = api.registerManager(emailRandom, "123456789");
        var tokenManager = manager.token();
        var idLead = api.createLead(tokenManager, "Anna", null);
        //When:
        api.as(tokenManager)
                .body(Map.of("status", "CONVERTED_TO_STUDENT"))
                .patch("/leads/{id}/status", idLead)
                //Then:
                .then()
                .statusCode(409);
        api.as(tokenManager)
                .get("/leads/{id}", idLead)
                .then()
                .statusCode(200)
                .body("status", equalTo("NEW"));

    }

    @DisplayName("При выполнении запроса PATCH /leads/{id}/status -> 200, статус изменился на IN_PROGRESS; повторный GET /leads/{id} подтверждает, что статус сохранился в базе")
    @Test
    void statusChangedToInProgress_isSaved() {
        //Given:
        String email = faker.internet().emailAddress();
        var manager = api.registerManager(email, "123456789");
        var managerToken = manager.token();
        var leadId = api.createLead(managerToken, "Anna", null);

        //When:
        api.as(managerToken)
                .body(Map.of("status", "IN_PROGRESS"))
                .patch("/leads/{id}/status", leadId)
                //Then:
                .then()
                .statusCode(200)
                .body("status", equalTo("IN_PROGRESS"));
        api.as(managerToken)
                .get("/leads/{id}", leadId)
                .then()
                .statusCode(200)
                .body("status", equalTo("IN_PROGRESS"));

    }
}

