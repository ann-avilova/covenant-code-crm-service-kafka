package com.covenantcode.crm.e2e;

import com.covenantcode.crm.dto.auth.LoginRequest;
import com.covenantcode.crm.e2e.support.CrmApi;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Map;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@DisplayName("E2E: аутентификация и JWT")
public class AuthE2ETest extends BaseE2ETest {

    private Faker faker;

    @BeforeEach
    void setUp(){
        faker = new Faker();
    }


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
        // Given: Токена нет

        // When: анонимный запрос к эндпойнту только для ADMIN
        api.anonymous()
                .get("/users")
                // Then: Сервер не пускает
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("Токен не настоящий -> 401")
    void request_fakeToken_returns401() {
        // Given: строка "garbage" — это вообще не JWT, она отваливается на первом же шаге разбора
        // When: Запрос с ненастоящим токеном к эндпойнту только для ADMIN
        api.as("garbage")
                .get("/users")
                // Then: сервер не пускает
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("Токен правильного формата, но подписан чужим ключом -> 401")
    void request_withTokenSignedByOtherKey_returns401() {
        // Опаснее мусорной строки подделка: токен правильного формата, в котором злоумышленник
        // сам вписал email админа и подписал своим ключом. Тест проверяет, что сервер
        // действительно сверяет подпись, а не просто читает email из токена.

        // Given: JWT от имени админа, подписанный НЕ серверным ключом
        SecretKey attackerKey = Keys.hmacShaKeyFor(
                "attacker-secret-key-at-least-32-bytes-long!".getBytes(StandardCharsets.UTF_8));
        String forged = Jwts.builder()
                .subject(ADMIN_EMAIL)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(attackerKey)
                .compact();

        // When: запрос с поддельным токеном к эндпойнту только для ADMIN
        api.as(forged)
                .get("/users")
                // Then: подпись не сходится — сервер не пускает
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("Логин с неверны паролем -> 401")
    void request_wrongPassword_returns401() {

        //Создаем существующий DTO из папки main.dto.auth
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(ADMIN_EMAIL);
        loginRequest.setPassword("wrong-password");

        // Given: админ существует (BaseE2ETest.seedAdmin()), но пароль неверный

        //When: попытка логина с неверным паролем
        api.anonymous()
                .body(loginRequest)
                .post("/auth/login")
                //Then: 401
                .then()
                .statusCode(401)
                .body("detail", equalTo("Неверный email или пароль"))
                .body("token", nullValue());
    }

    @Disabled("BUG: в классе JwtService метод isTokenValid проверяет только email и срок токена, но не проверяет, заблокирован ли пользователь")
    @Test
    @DisplayName("Получение списка лидов повторно после блокировки менеджера -> 401")
    void createdManagerAndGetLeads200_blockedManager_GetLeads401() {
        // Given: Создаем пользователя, и получаем список по его токену
        String randomEmail = faker.internet().emailAddress();
        String password = "123456789";
        var manager = api.registerManager(randomEmail, password);
        api.as(manager.token())
                .get("/leads")
                .then()
                .statusCode(200);

        // When: Админ блокирует пользователя
        api.setUserEnabled(adminToken(), manager.id(), false);

        // Then: Пользователь после блокировки пытается получить список лидов
        api.as(manager.token())
                .get("/leads")
                .then()
                .statusCode(401);
    }
}
