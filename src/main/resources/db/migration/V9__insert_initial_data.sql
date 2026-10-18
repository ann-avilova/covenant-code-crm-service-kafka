-- Роли
INSERT INTO roles (name) VALUES
                             ('ADMIN'),
                             ('MANAGER'),
                             ('TEACHER'),
                             ('STUDENT');

-- Системный администратор по умолчанию
-- Логин:  admin@covenantcode.ru
-- Пароль: Admin1234!  (BCrypt, cost=10)
INSERT INTO users (first_name, last_name, email, password, role_id, enabled)
VALUES (
           'Admin',
           'System',
           'admin@covenantcode.ru',
           '$2a$10$fo1QXaLNusWym4oXjkmgLuuXqvph5SB7AHmNoPPEwB4xnIycY65w6',
           (SELECT id FROM roles WHERE name = 'ADMIN'),
           TRUE
       );