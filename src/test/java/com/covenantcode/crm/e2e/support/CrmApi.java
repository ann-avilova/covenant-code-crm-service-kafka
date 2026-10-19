package com.covenantcode.crm.e2e.support;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.LogConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * "Шаги" бизнес-сценариев поверх HTTP API.
 *
 * <p>Идея: E2E-тест должен читаться как сценарий ("менеджер создал лида, конвертировал его в студента..."),
 * а не как простыня из HTTP-запросов. Поэтому типовые действия вынесены сюда.
 *
 * <p>Правило: шаг-хелпер проверяет только "счастливый" статус ответа и возвращает id/токен.
 * Негативные сценарии и проверки тела ответа пишем прямо в тесте через {@link #as(String)} / {@link #anonymous()}.
 */
public class CrmApi {

    private final RequestSpecification baseSpec;

    public CrmApi(int port) {
        this.baseSpec = new RequestSpecBuilder()
                .setBaseUri("http://localhost")
                .setPort(port)
                .setBasePath("/api/v1")
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                // при падении теста RestAssured напечатает запрос и ответ — удобно для отладки
                .setConfig(RestAssuredConfig.config()
                        .logConfig(LogConfig.logConfig()
                                .enableLoggingOfRequestAndResponseIfValidationFails()))
                .build();
    }

    /** Запрос от имени пользователя с данным JWT.*/
    public RequestSpecification as(String token) {
        return given()
                .spec(baseSpec)
                .header("Authorization", "Bearer " + token);
    }

    /** Запрос без заголовка Authorization. */
    public RequestSpecification anonymous() {
        return given()
                .spec(baseSpec);
    }

    // ---------- auth ----------

    public String login(String email, String password) {
        return anonymous()
                .body(Map.of("email", email, "password", password))
                .post("/auth/login")
                .then().statusCode(200)
                .extract().path("token");
    }

    /** Публичная регистрация. По текущей логике всегда создаёт MANAGER. Метод возвращает id и токен.*/
    public RegisteredUser registerManager(String email, String password) {
        var json = anonymous()
                .body(Map.of(
                        "firstName", "Мария",
                        "lastName", "Менеджерова",
                        "email", email,
                        "password", password,
                        "phone", "+79990000001"))
                .post("/auth/register")
                .then().statusCode(201)
                .extract().jsonPath();
        return new RegisteredUser(json.getLong("userId"), json.getString("token"));
    }

    /** Результат регистрации: id нового пользователя и его токен. */
    public record RegisteredUser(long id, String token) {
    }

    // ---------- справочники ----------

    public long createCourse(String token, String title) {
        return as(token)
                .body(Map.of("title", title, "durationInWeeks", 12, "price", 50000, "status", "ACTIVE"))
                .post("/courses")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    public long createTeacher(String adminToken, String email, String password) {
        return as(adminToken)
                .body(Map.of(
                        "firstName", "Иван",
                        "lastName", "Преподов",
                        "email", email,
                        "password", password))
                .post("/teachers")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    public long createStudent(String token, String firstName, Long userId) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("firstName", firstName);
        body.put("lastName", "Студентов");
        body.put("email", firstName.toLowerCase() + "@student.test");
        if (userId != null) {
            body.put("userId", userId);
        }
        return as(token)
                .body(body)
                .post("/students")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    // ---------- лиды ----------

    public long createLead(String token, String firstName, Long courseId) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("firstName", firstName);
        body.put("phone", "+79991112233");
        body.put("email", firstName.toLowerCase() + "@lead.test");
        body.put("source", "website");
        if (courseId != null) {
            body.put("interestedCourseId", courseId);
        }
        return as(token)
                .body(body)
                .post("/leads")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    public void changeLeadStatus(String token, long leadId, String status) {
        as(token)
                .body(Map.of("status", status))
                .patch("/leads/{id}/status", leadId)
                .then().statusCode(200);
    }

    public long convertLead(String token, long leadId, String firstName) {
        return as(token)
                .body(Map.of("firstName", firstName, "lastName", "Конвертов", "phone", "+79991112233"))
                .post("/leads/{id}/convert", leadId)
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    // ---------- группы ----------

    public long createGroup(String token, String name, long courseId, long teacherId, List<Long> studentIds) {
        return as(token)
                .body(Map.of(
                        "name", name,
                        "courseId", courseId,
                        "teacherId", teacherId,
                        "startDate", LocalDate.now().toString(),
                        "studentIds", studentIds))
                .post("/groups")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    public void changeGroupStatus(String token, long groupId, String status) {
        as(token)
                .body(Map.of("status", status))
                .patch("/groups/{id}/status", groupId)
                .then().statusCode(200);
    }

    // ---------- занятия ----------

    public Map<String, Object> lessonBody(long groupId, long teacherId, LocalDate date, LocalTime start, LocalTime end) {
        return Map.of(
                "groupId", groupId,
                "teacherId", teacherId,
                "topic", "Тема занятия",
                "lessonDate", date.toString(),
                "startTime", start.toString(),
                "endTime", end.toString());
    }

    public long createLesson(String token, long groupId, long teacherId, LocalDate date, LocalTime start, LocalTime end) {
        return as(token)
                .body(lessonBody(groupId, teacherId, date, start, end))
                .post("/lessons")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    public Map<String, Object> attendance(long studentId, boolean present) {
        return Map.of("studentId", studentId, "present", present);
    }

    // ---------- пользователи ----------

    /** Блокирует (false) или разблокирует (true) пользователя. Только для ADMIN. */
    public void setUserEnabled(String adminToken, long userId, boolean enabled) {
        as(adminToken)
                .body(Map.of("enabled", enabled))
                .patch("/users/{id}/enabled", userId)
                .then()
                .statusCode(200);
    }
}
