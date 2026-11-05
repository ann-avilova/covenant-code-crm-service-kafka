package com.covenantcode.crm.e2e;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Неверные запросы")
public class InvalidRequestE2ETest extends BaseE2ETest {

    @Disabled("BUG #3: При выполнении запроса GET /courses/abc (id не число) возвращается 500 вместо 400")
    @DisplayName("В запрос передан неверный id (abc) -> 400")
    @Test
    void invalidIdRequest_returns400() {
        //Given:
        String adminToken = adminToken();
        String invalidId = "abc";

        //When:
        api.as(adminToken)
                .get("/courses/{id}", invalidId)

                //Then:
                .then()
                .statusCode(400);
    }

    @Disabled("BUG #2: При выполнении запроса GET /leads?status=FOO (несуществующий статус) возвращается 500 вместо 400")
    @DisplayName("В запрос GET /leads передан неверный status(FOO) -> 400")
    @Test
    void invalidStatusRequestLeads_returns400() {
        //Given:
        String adminToken = adminToken();
        String invalidStatus = "FOO";

        //When:
        api.as(adminToken)
                .queryParam("status", invalidStatus)
                .get("/leads")

                //Then:
                .then()
                .statusCode(400);
    }

    @Disabled("BUG #4: При выполнении запроса GET /unknown/path (несуществующий адрес) возвращается 500 вместо 404")
    @DisplayName("Запрос GET /unknown/path (несуществующий адрес) -> 404")
    @Test
    void nonexistentUrlInRequest_returns404() {
        //Given:
        String adminToken = adminToken();

        //When:
        api.as(adminToken)
                .get("/unknown/path")

                //Then:
                .then()
                .statusCode(404);
    }
}