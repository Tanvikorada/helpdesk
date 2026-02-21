CREATE DATABASE IF NOT EXISTS student_helpdesk;
USE student_helpdesk;

CREATE TABLE IF NOT EXISTS app_users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(80) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(180) NOT NULL UNIQUE,
    department VARCHAR(120) NOT NULL,
    role VARCHAR(30) NOT NULL
);

CREATE TABLE IF NOT EXISTS complaints (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(30) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL,
    student_id BIGINT NOT NULL,
    assigned_to_id BIGINT,
    escalated BIT NOT NULL DEFAULT 0,
    escalation_due_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_complaint_student FOREIGN KEY (student_id) REFERENCES app_users(id),
    CONSTRAINT fk_complaint_assignee FOREIGN KEY (assigned_to_id) REFERENCES app_users(id)
);

CREATE TABLE IF NOT EXISTS complaint_notes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    complaint_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    message TEXT NOT NULL,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_note_complaint FOREIGN KEY (complaint_id) REFERENCES complaints(id),
    CONSTRAINT fk_note_author FOREIGN KEY (author_id) REFERENCES app_users(id)
);

CREATE TABLE IF NOT EXISTS complaint_attachments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    complaint_id BIGINT NOT NULL,
    uploaded_by_id BIGINT NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    stored_file_name VARCHAR(255) NOT NULL UNIQUE,
    content_type VARCHAR(150) NOT NULL,
    size BIGINT NOT NULL,
    uploaded_at DATETIME NOT NULL,
    CONSTRAINT fk_attachment_complaint FOREIGN KEY (complaint_id) REFERENCES complaints(id),
    CONSTRAINT fk_attachment_uploader FOREIGN KEY (uploaded_by_id) REFERENCES app_users(id)
);
