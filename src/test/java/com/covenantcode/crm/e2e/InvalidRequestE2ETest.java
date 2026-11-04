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
}