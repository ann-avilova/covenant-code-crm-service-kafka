package com.covenantcode.crm.e2e;

import java.util.Map;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.security.authorization.AuthenticatedAuthorizationManager.anonymous;

@DisplayName("Блокировка и разблокировка пользователей")
public class UserEnabledE2ETest extends BaseE2ETest {

    private Faker faker;

    @BeforeEach
    void setUp() {
        this.faker = new Faker();
    }

    @Test
    @DisplayName("Повторная блокировка заблокированного -> 200, остаётся заблокирован")
    void blockedUser_adminBlock_returns200() {
        // Given:
        String email = faker.internet().emailAddress();
        String password = faker.internet().password(8, 16);

        var manager = api.registerManager(email, password);
        String adminToken = adminToken();
        api.setUserEnabled(adminToken, manager.id(), false);
        //When:
        api.as(adminToken)
                .body(Map.of("enabled", false))
                .patch("/users/{id}/enabled", manager.id())
                //Then:
                .then()
                .statusCode(200);
        api.as(adminToken)
                .get("/users/{id}", manager.id())
                .then()
                .statusCode(200)
                .body("enabled", equalTo(false));
    }

    @Disabled("BUG №5: Старый токен продолжает работать после блокировки")
    @Test
    @DisplayName("Состояние: Разблокирован -> Заблокирован (Отзыв прав)")
    void blockedUser_admin_userUnAuthorized401() {
        // Given:
        String email = faker.internet().emailAddress();
        String password = faker.internet().password(8, 16);

        var manager = api.registerManager(email, password);
        String managerToken = api.login(email, password);
        String adminToken = adminToken();
        // When:
        api.as(adminToken)
                .body(Map.of("enabled", false))
                .patch("/users/{id}/enabled", manager.id())
                // Then:
                .then()
                .statusCode(200);
        api.as(managerToken)
                .get("/users/{id}", manager.id())
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("Состояние: Разблокирован -> Заблокирован (проверка через вход)")
    void blockedUser_admin_unAuthorizedUser401(){
        //Given:
        String email = faker.internet().emailAddress();
        String password = faker.internet().password(8, 16);
        var manager = api.registerManager(email, password);
        String adminToken = adminToken();
        //When:
        api.as(adminToken)
                .body(Map.of("enabled", false))
                .patch("/users/{id}/enabled", manager.id())
                .then()
                .statusCode(200);
        //Then:
        api.anonymous()
                .body(Map.of("email", email, "password", password))
                .post("/auth/login")
                .then()
                .spec(ERROR_RESPONSE)
                .statusCode(401);
    }

    @Test
    @DisplayName("Состояние: Заблокирован -> Разблокирован (Активация)")
    void unlockingUser_admin_returns200() {
        // Given:
        String email = faker.internet().emailAddress();
        String password = faker.internet().password(8, 16);

        var manager = api.registerManager(email, password);
        String adminToken = adminToken();
        api.setUserEnabled(adminToken, manager.id(), false);
        // When:
        api.as(adminToken)
                .body(Map.of("enabled", true))
                .patch("/users/{id}/enabled", manager.id())
                // Then:
                .then()
                .statusCode(200)
                .body("enabled", equalTo(true));
        api.as(adminToken)
                .get("/users/{id}", manager.id())
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("Состояние: Разблокирован -> Разблокирован (Повторная активация)")
    void userActive_admin_returns200() {
        // Given:
        String email = faker.internet().emailAddress();
        String password = faker.internet().password(8, 16);

        String adminToken = adminToken();
        var manager = api.registerManager(email, password);
        api.setUserEnabled(adminToken, manager.id(), true);
        // When:
        api.as(adminToken)
                .body(Map.of("enabled", true))
                .patch("/users/{id}/enabled", manager.id())
                // Then:
                .then()
                .statusCode(200)
                .body("enabled", equalTo(true));
        api.as(adminToken)
                .get("/users/{id}", manager.id())
                .then()
                .statusCode(200);
    }
}
