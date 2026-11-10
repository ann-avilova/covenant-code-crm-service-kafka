package com.covenantcode.crm.e2e;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;

public class CourseRequestE2ETest extends BaseE2ETest{

    @DisplayName("Админ запрашивает курс которого не существует ->404")
    @Test
    void requestIdCourse_returns404(){
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
}
