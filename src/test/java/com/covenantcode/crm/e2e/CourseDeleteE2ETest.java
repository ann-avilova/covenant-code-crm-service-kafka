package com.covenantcode.crm.e2e;

import java.util.List;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


public class CourseDeleteE2ETest extends BaseE2ETest {

    private Faker faker;

    @BeforeEach
    void setUp() {
        this.faker = new Faker();
    }

    @DisplayName("Админ удаляет курс без группы -> 204, get ->404")
    @Test
    void adminDeleteCourseWithoutGroup_returns204() {
        //Given:
        String adminToken = adminToken();
        var courseId = api.createCourse(adminToken, "Java");
        //When:
        api.as(adminToken)
                .delete("/courses/{id}", courseId)
                //Then:
                .then()
                .statusCode(204);
        api.as(adminToken)
                .get("/courses/{id}", courseId)
                .then()
                .statusCode(404);

    }

    @DisplayName("Админ удаляет не существующий курс -> 404")
    @Test
    void adminDeleteNonExistentCourse_returns404() {
        //Given:
        String adminToken = adminToken();
        var courseId = 99999;
        //When:
        api.as(adminToken)
                .delete("/courses/{id}", courseId)
                //Then:
                .then()
                .statusCode(404);
    }

    @DisplayName("Токен не действителен -> 401")
    @Test
    void tokenAdminNotValid_returns401() {
        //Given:
        String adminToken = adminToken();
        var courseId = api.createCourse(adminToken, "Java");
        String invalidToken = "12345ckhkjgjhsjdf";

        //When:
        api.as(invalidToken)
                .delete("/courses/{id}", courseId)
        //Then:
                .then()
                .statusCode(401);
        api.as(adminToken)
                .get("/courses/{id}", courseId)
                .then()
                .statusCode(200);
    }

    @DisplayName("Менеджер пытается удалить курс -> 403")
    @Test
    void deleteCourseManager_returns403() {
        //Given:
        String emailRandom = faker.internet().emailAddress();
        var manager = api.registerManager(emailRandom, "123456789");
        String managerToken = manager.token();
        String adminToken = adminToken();
        var courseId = api.createCourse(adminToken, "Java");
        //When:
        api.as(managerToken)
                .delete("/courses/{id}", courseId)
                //Then:
                .then()
                .statusCode(403);
        api.as(adminToken)
                .get("/courses/{id}", courseId)
                .then()
                .statusCode(200);
    }

    @DisplayName("Попытка удаления курса при наличии активной группы -> 409, get -> 200")
    @Test
    void adminDeleteCourseWithActiveGroup_returns409() {
        //Given:
        String adminToken = adminToken();
        String emailRandom = faker.internet().emailAddress();

        var courseId = api.createCourse(adminToken, "Java");
        var teacherId = api.createTeacher(adminToken, emailRandom, "123456789");
        var groupId = api.createGroup(adminToken, "Group_1", courseId, teacherId, List.of());

        api.changeGroupStatus(adminToken, groupId, "ACTIVE");
        //When:
        api.as(adminToken)
                .delete("/courses/{id}", courseId)
                //Then:
                .then()
                .statusCode(409);
        api.as(adminToken)
                .get("/courses/{id}", courseId)
                .then()
                .statusCode(200);

    }


    @Disabled("BUG #6 Удаление курса с завершённой группой возвращает 500 вместо 409")
    @DisplayName("После попытки удаления курса с завершенной группой со статусом COMPLETED ->409, проверка что курс не удалился -> 200")
    @Test
    void courseDelete_withCompletedGroup_return409() {
        //Given:
        String emailRandom = faker.internet().emailAddress();
        String adminToken = adminToken();

        var courseId = api.createCourse(adminToken, "Java");
        var teacherId = api.createTeacher(adminToken, emailRandom, "123456789");
        var groupId = api.createGroup(adminToken, "Group_1", courseId, teacherId, List.of());

        api.changeGroupStatus(adminToken, groupId, "ACTIVE");
        api.changeGroupStatus(adminToken, groupId, "COMPLETED");

        //When:
        api.as(adminToken)
                .delete("/courses/{id}", courseId)
                //Then:
                .then()
                .statusCode(409);
        api.as(adminToken)
                .get("/courses/{id}", courseId)
                .then()
                .statusCode(200);
    }

}
