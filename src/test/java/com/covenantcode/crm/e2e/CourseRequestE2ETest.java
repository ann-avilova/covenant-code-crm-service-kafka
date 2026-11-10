package com.covenantcode.crm.e2e;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class CourseRequestE2ETest extends BaseE2ETest {

    @DisplayName("Админ запрашивает курс которого не существует ->404")
    @Test
    void requestIdCourse_returns404() {
        //Given:
        String adminToken = adminToken();
        var courseId = "99999";

        //When:
        api.as(adminToken)
                .get("/courses/{id}", courseId)
                //Then:
                .then()
                .spec(ERROR_RESPONSE)
                .statusCode(404)
                .body("type", equalTo("resource-not-found"));
    }

    @DisplayName("Админ получает существующий курс по id -> 200 ")
    @Test
    void getIdAndAnswerCourse_returns200() {
        //Given:
        String adminToken = adminToken();
        var courseId = api.createCourse(adminToken, "Java");
        //When:
        var response = api.as(adminToken)
                .get("/courses/{id}", courseId);

        //Then:
        response.then()
                .statusCode(200)
                .body("title", equalTo("Java"))
                .body("durationInWeeks", equalTo(12))
                .body("price", equalTo(50000.0f))
                .body("status", equalTo("ACTIVE"));

        var actualId = response.jsonPath().getLong("id");
        assertEquals(courseId , actualId);
    }
}
