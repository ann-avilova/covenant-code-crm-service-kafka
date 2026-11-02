package com.covenantcode.crm.e2e;

import java.util.Map;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.nullValue;

public class UserResponseE2ETest extends BaseE2ETest {

    private Faker faker;

    @BeforeEach
    void setUp() {
        this.faker = new Faker();
    }

    @DisplayName("Пароля нет в ответе админу")
    @Test
    void returnResponseAdmin_noPassword() {
        //Given:
        String emailRandom = faker.internet().emailAddress();
        String adminToken = adminToken();
        var manager = api.registerManager(emailRandom, "123456789");

        //When:
        api.as(adminToken)
                .get("/users/{id}", manager.id())

        //Tnen:
                .then()
                .statusCode(200)
                .body("password", nullValue());

    }

    @DisplayName("Пароля нет в ответе админу при включении/отключении пользователя")
    @Test
    void afterEnabled_returnResponseAdmin_noPassword() {
        //Given:
        String emailRandom = faker.internet().emailAddress();
        String adminToken = adminToken();
        var manager = api.registerManager(emailRandom, "123456789");

        //When:
        api.as(adminToken)
                .body(Map.of("enabled", false))
                .patch("/users/{id}/enabled", manager.id())
        //Then:
                .then()
                .statusCode(200)
                .body("password", nullValue());
    }
}
