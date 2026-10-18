package com.covenantcode.crm.dto.teacher;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherCreateRequest {

    @NotBlank(message = "Имя обязательно для заполнения")
    @Size(max = 100, message = "Имя не должно превышать 100 символов")
    private String firstName;

    @NotBlank(message = "Фамилия обязательно для заполнения")
    @Size(max = 100, message = "Фамилия не должно превышать 100 символов")
    private String lastName;

    @NotBlank(message = "Поле email обязательно для заполнения")
    @Email(message = "Некорректный формат email")
    @Size(max = 255, message = "Email не должен превышать 255 символов")
    private String email;

    @NotBlank(message = "Поле пароль обязательно для заполнения")
    @Size(min = 8, message = "Пароль не должен быть меньше 8 символов")
    private String password;

    @Size(max = 20, message = "Не больше 20 символов")
    private String phone;
}
