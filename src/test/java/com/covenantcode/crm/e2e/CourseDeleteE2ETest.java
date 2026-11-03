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

    @Disabled("BUG #6 Удаление курса с завершённой группой возвращает 500 вместо 409")
    @DisplayName("После попытки удаления курса с завершенной группой со статусом COMPLETED ->409, проверка что курс не удалился -> 200")
    @Test
    void courseDelete_withCompletedGroup_return409() {
        //Given:
        String emailRandom = faker.internet().emailAddress();
        String adminToken = adminToken();

        var courseId = api.createCourse(adminToken, "New course");
        var teacherId = api.createTeacher(adminToken, emailRandom, "123456789");
        var groupId = api.createGroup(adminToken, "Developer", courseId, teacherId, List.of());

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
