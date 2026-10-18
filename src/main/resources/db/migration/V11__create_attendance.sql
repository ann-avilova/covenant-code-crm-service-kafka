CREATE TABLE attendance
(
    id BIGSERIAL PRIMARY KEY,
    lesson_id  BIGINT  NOT NULL REFERENCES lessons (id) ON DELETE CASCADE,
    student_id BIGINT  NOT NULL REFERENCES students (id) ON DELETE CASCADE,
    present    BOOLEAN NOT NULL DEFAULT TRUE,
    note       VARCHAR(255),
    marked_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (lesson_id, student_id)
);
