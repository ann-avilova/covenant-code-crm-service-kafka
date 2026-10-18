package com.covenantcode.crm.e2e;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.notNullValue;

@DisplayName("E2E: аутентификация и JWT")
public class AuthE2ETest extends BaseE2ETest {
    @Test
    @DisplayName("Админ логинется и с полученым токеном получает доступ к защищенному эндпойнту")
    void login_validCredentials_tokenGrantAccess() {
        // Given: admin create in BaseE2ETest.seedAdmin()

        //When: Really login whiting /auth/login
        String token = adminToken();

        // Then: token пропускает на  endpoint, доступный только ADMIN
        api.as(token)
                .get("/users")
                .then()
                .statusCode(200)
                .body("content", notNullValue());
    }

    @Test
    @DisplayName("Запрос без токена к защищенному эндпойнту -> 401")
    void request_withoutToken_returns401() {
        //Given: Токена нет

        //When: анонимный запрос к эндпойнту только для ADMIN
        api.anonymous()
                .get("/users")
                //Then: Сервер не пускает
                .then()
                .statusCode(401);
    }
}
