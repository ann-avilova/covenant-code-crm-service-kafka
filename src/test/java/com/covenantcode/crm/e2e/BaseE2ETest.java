package com.covenantcode.crm.e2e;

import com.covenantcode.crm.BaseIntegrationTest;
import com.covenantcode.crm.e2e.support.CrmApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * База для сквозных (E2E) тестов.
 *
 * <p>Отличия от {@link BaseIntegrationTest}:
 * <ul>
 *     <li>приложение поднимается на реальном случайном порту (RANDOM_PORT), запросы идут по HTTP;</li>
 *     <li>никаких {@code @WithUserDetails}/{@code @WithMockUser} — только настоящий логин и JWT;</li>
 *     <li>никаких {@code @Transactional} на тесте — данные реально коммитятся, как в проде;</li>
 *     <li>данные создаются через публичный API, а не через репозитории.</li>
 * </ul>
 *
 * <p>Контейнер PostgreSQL и очистка БД после каждого теста наследуются от {@link BaseIntegrationTest}.
 * Очистка удаляет в том числе роли и сид-админа из V9, поэтому перед каждым тестом мы их восстанавливаем.
 */
@Tag("e2e")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseE2ETest extends BaseIntegrationTest {

    protected static final String ADMIN_EMAIL = "admin@covenantcode.ru";
    protected static final String ADMIN_PASSWORD = "Admin1234!";

    @LocalServerPort
    private int port;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    protected CrmApi api;

    @BeforeEach
    void seedReferenceDataAndClient() {
        jdbcTemplate.update("""
                INSERT INTO roles (name) VALUES ('ADMIN'), ('MANAGER'), ('TEACHER'), ('STUDENT')
                ON CONFLICT (name) DO NOTHING
                """);
        // ВРЕМЕННО: старый вызов, чтобы увидеть конфликт с админом из V9. Потом вернуть seedAdmin();
        createUserInDb("Admin", "System", ADMIN_EMAIL, ADMIN_PASSWORD, "ADMIN");
        api = new CrmApi(port);
    }

    /**
     * Админ из V9 есть в БД только до первой очистки, поэтому upsert:
     * создаём, если его нет, и приводим к известному состоянию, если он остался после миграции.
     */
    private void seedAdmin() {
        jdbcTemplate.update("""                                                                                                                                                           
              INSERT INTO users (first_name, last_name, email, password, role_id, enabled)                                                                                              
              VALUES ('Admin', 'System', ?, ?, (SELECT id FROM roles WHERE name = 'ADMIN'), TRUE)                                                                                       
              ON CONFLICT (email) DO UPDATE                                                                                                                                             
                  SET password = EXCLUDED.password,                                                                                                                                     
                      role_id  = EXCLUDED.role_id,                                                                                                                                      
                      enabled  = TRUE                                                                                                                                                   
              """,
                ADMIN_EMAIL, passwordEncoder.encode(ADMIN_PASSWORD));
    }

    /**
     * Создаёт пользователя напрямую в БД.
     * Используем ТОЛЬКО там, где у API нет способа создать такого пользователя
     * (сид-админ и учётки с ролью STUDENT — эндпоинта для них нет).
     */
    protected Long createUserInDb(String firstName, String lastName, String email, String rawPassword, String role) {
        return jdbcTemplate.queryForObject("""
                        INSERT INTO users (first_name, last_name, email, password, role_id, enabled)
                        VALUES (?, ?, ?, ?, (SELECT id FROM roles WHERE name = ?), TRUE)
                        RETURNING id
                        """,
                Long.class, firstName, lastName, email, passwordEncoder.encode(rawPassword), role);
    }

    protected String adminToken() {
        return api.login(ADMIN_EMAIL, ADMIN_PASSWORD);
    }
}

