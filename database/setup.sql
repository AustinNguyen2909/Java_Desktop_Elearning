-- E-Learning Platform Database Schema
-- MySQL version

-- Create database
CREATE DATABASE IF NOT EXISTS elearning_db;
USE elearning_db;

-- Drop tables if they exist (for clean setup)
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS enrollments;
DROP TABLE IF EXISTS lessons;
DROP TABLE IF EXISTS courses;
DROP TABLE IF EXISTS users;

-- Users table
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'TEACHER', 'USER') NOT NULL DEFAULT 'USER',
    email VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(100),
    avatar_path VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Courses table
CREATE TABLE courses (
    id INT PRIMARY KEY AUTO_INCREMENT,
    teacher_id INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    thumbnail_path VARCHAR(255),
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    rejection_reason TEXT,
    category VARCHAR(50),
    difficulty_level ENUM('BEGINNER', 'INTERMEDIATE', 'ADVANCED'),
    estimated_hours INT,
    is_published BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    approved_at TIMESTAMP NULL,
    approved_by INT NULL,
    
    FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL,
    
    INDEX idx_teacher (teacher_id),
    INDEX idx_status (status),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Lessons table
CREATE TABLE lessons (
    id INT PRIMARY KEY AUTO_INCREMENT,
    course_id INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    video_path VARCHAR(500),
    content_text TEXT,
    duration_minutes INT,
    order_index INT NOT NULL DEFAULT 0,
    is_preview BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    
    INDEX idx_course (course_id),
    INDEX idx_order (course_id, order_index)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Enrollments table
CREATE TABLE enrollments (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    course_id INT NOT NULL,
    enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    progress_percent DECIMAL(5,2) DEFAULT 0.00,
    last_accessed_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    
    UNIQUE KEY uk_user_course (user_id, course_id),
    INDEX idx_user (user_id),
    INDEX idx_course (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Comments table
CREATE TABLE comments (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    lesson_id INT NOT NULL,
    parent_id INT NULL,
    content TEXT NOT NULL,
    is_edited BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES comments(id) ON DELETE CASCADE,
    
    INDEX idx_lesson (lesson_id),
    INDEX idx_user (user_id),
    INDEX idx_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Reviews table
CREATE TABLE reviews (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    course_id INT NOT NULL,
    rating TINYINT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    is_edited BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    
    UNIQUE KEY uk_user_course (user_id, course_id),
    INDEX idx_course (course_id),
    INDEX idx_rating (rating)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Insert default admin user
-- Password: admin123
INSERT INTO users (username, password_hash, role, email, full_name, is_active) VALUES
('admin', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYKBXHXxeYy', 'ADMIN', 'admin@elearning.com', 'System Administrator', TRUE);

-- Insert sample teacher
-- Password: teacher123
INSERT INTO users (username, password_hash, role, email, full_name, is_active) VALUES
('teacher', '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'TEACHER', 'teacher@elearning.com', 'John Teacher', TRUE);

-- Insert sample student
-- Password: student123  
INSERT INTO users (username, password_hash, role, email, full_name, is_active) VALUES
('student', '$2a$12$hZsmvadZLfIfzxe4ZUkIveFCbVU4rTDdVLVTrJvLON8GK3TnN8dMG', 'USER', 'student@elearning.com', 'Jane Student', TRUE);

COMMIT;
