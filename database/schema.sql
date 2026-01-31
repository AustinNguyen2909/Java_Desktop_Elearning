-- E-Learning Platform Database Schema
-- MySQL version
--
-- CRITICAL FEATURES IMPLEMENTED:
-- 1. Course Creation with Hero Images (thumbnail_path)
-- 2. Video Upload per Lesson (video_path)
-- 3. Video-Based Progress Tracking (progress_percent, is_completed)
--
-- File Storage Strategy:
-- - Hero Images: Stored in thumbnails/course_X/ directory
-- - Videos: Stored in videos/course_X/ directory
-- - Only file paths are stored in database, not the files themselves
--
-- Progress Tracking Formula:
-- progress_percent = (completed_lessons / total_lessons) × 100

-- Create database
CREATE DATABASE IF NOT EXISTS elearning_db;
USE elearning_db;

-- Drop tables if they exist (for clean setup)
DROP TABLE IF EXISTS review_likes;
DROP TABLE IF EXISTS review_comments;
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS user_login_logs;
DROP TABLE IF EXISTS user_activity;
DROP TABLE IF EXISTS certificates;
DROP TABLE IF EXISTS course_review_likes;
DROP TABLE IF EXISTS course_review_comments;
DROP TABLE IF EXISTS lesson_views;
DROP TABLE IF EXISTS lesson_likes;
DROP TABLE IF EXISTS lesson_progress;
DROP TABLE IF EXISTS course_reviews;
DROP TABLE IF EXISTS lesson_comments;
DROP TABLE IF EXISTS enrollments;
DROP TABLE IF EXISTS lessons;
DROP TABLE IF EXISTS courses;
DROP TABLE IF EXISTS users;

-- Users table
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'INSTRUCTOR', 'USER') NOT NULL DEFAULT 'USER',
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(40) NULL UNIQUE,
    full_name VARCHAR(100),
    avatar_path VARCHAR(255),
    status ENUM('PENDING', 'ACTIVE', 'SUSPENDED') NOT NULL DEFAULT 'PENDING',
    date_of_birth DATE NULL,
    school VARCHAR(150),
    job_title VARCHAR(150),
    experience TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Courses table
-- FEATURE 1: Hero Image Upload
-- thumbnail_path stores absolute path to hero image (e.g., /path/to/project/thumbnails/course_1/course_1_1674567890.jpg)
CREATE TABLE courses (
    id INT PRIMARY KEY AUTO_INCREMENT,
    instructor_id INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    thumbnail_path VARCHAR(255),              -- Hero image path (locally stored)
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    rejection_reason TEXT,
    category VARCHAR(50),
    difficulty_level ENUM('BEGINNER', 'INTERMEDIATE', 'ADVANCED'),
    estimated_hours INT,
    is_published BOOLEAN DEFAULT FALSE,       -- Students can only see published courses
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    approved_at TIMESTAMP NULL,
    approved_by INT NULL,

    FOREIGN KEY (instructor_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL,

    INDEX idx_instructor (instructor_id),
    INDEX idx_status (status),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Lessons table
-- FEATURE 2: Video Upload per Lesson
-- video_path stores absolute path to video file (e.g., /path/to/project/videos/course_1/lesson_1_1674567890.mp4)
-- Supported formats: MP4, AVI, MOV, MKV, FLV, WMV (max 500 MB)
CREATE TABLE lessons (
    id INT PRIMARY KEY AUTO_INCREMENT,
    course_id INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    video_path VARCHAR(500),                  -- Video file path (locally stored)
    content_text TEXT,                        -- Lesson description
    duration_minutes INT,
    order_index INT NOT NULL DEFAULT 0,       -- Display order in course
    is_preview BOOLEAN DEFAULT FALSE,         -- Allow non-enrolled users to preview
    like_count INT DEFAULT 0,                 -- Cached likes for quick display
    comment_count INT DEFAULT 0,              -- Cached comments for quick display
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,

    INDEX idx_course (course_id),
    INDEX idx_order (course_id, order_index)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Enrollments table
-- FEATURE 3: Video-Based Progress Tracking (Course Level)
-- progress_percent is calculated as: (completed_lessons / total_lessons) × 100
-- Automatically updated when student marks a lesson as complete
CREATE TABLE enrollments (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    course_id INT NOT NULL,
    enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    progress_percent DECIMAL(5,2) DEFAULT 0.00,    -- Progress: 0.00 to 100.00
    last_accessed_at TIMESTAMP NULL,                -- Last time student accessed course
    completed_at TIMESTAMP NULL,                    -- Set when progress reaches 100%

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,

    UNIQUE KEY uk_user_course (user_id, course_id),
    INDEX idx_user (user_id),
    INDEX idx_course (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Lesson Progress table
-- FEATURE 3: Video-Based Progress Tracking (Lesson Level)
-- Tracks which videos each student has watched and completed
-- Used to calculate course progress_percent in enrollments table
CREATE TABLE lesson_progress (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    lesson_id INT NOT NULL,
    is_completed BOOLEAN DEFAULT FALSE,         -- Video marked as complete by student
    completed_at TIMESTAMP NULL,                -- When student marked video complete
    last_opened_at TIMESTAMP NULL,              -- Last time student opened this video

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE,

    UNIQUE KEY uk_user_lesson (user_id, lesson_id),
    INDEX idx_user (user_id),
    INDEX idx_lesson (lesson_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Comments table
CREATE TABLE lesson_comments (
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
    FOREIGN KEY (parent_id) REFERENCES lesson_comments(id) ON DELETE CASCADE,

    INDEX idx_lesson (lesson_id),
    INDEX idx_user (user_id),
    INDEX idx_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Lesson likes
CREATE TABLE lesson_likes (
    id INT PRIMARY KEY AUTO_INCREMENT,
    lesson_id INT NOT NULL,
    user_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE KEY uk_lesson_user (lesson_id, user_id),
    INDEX idx_lesson (lesson_id),
    INDEX idx_user (user_id),

    FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Lesson views (for analytics)
CREATE TABLE lesson_views (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    lesson_id INT NOT NULL,
    user_id INT NOT NULL,
    viewed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_lesson (lesson_id),
    INDEX idx_user (user_id),
    INDEX idx_viewed (viewed_at),

    FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Reviews table
CREATE TABLE course_reviews (
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

-- Review comments
CREATE TABLE course_review_comments (
    id INT PRIMARY KEY AUTO_INCREMENT,
    review_id INT NOT NULL,
    user_id INT NOT NULL,
    parent_id INT NULL,
    content TEXT NOT NULL,
    is_edited BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (review_id) REFERENCES course_reviews(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES course_review_comments(id) ON DELETE CASCADE,

    INDEX idx_review (review_id),
    INDEX idx_user (user_id),
    INDEX idx_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Review likes
CREATE TABLE course_review_likes (
    id INT PRIMARY KEY AUTO_INCREMENT,
    review_id INT NOT NULL,
    user_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE KEY uk_review_user (review_id, user_id),
    INDEX idx_review (review_id),
    INDEX idx_user (user_id),

    FOREIGN KEY (review_id) REFERENCES course_reviews(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Certificates
CREATE TABLE certificates (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    course_id INT NOT NULL,
    certificate_code VARCHAR(64) NOT NULL UNIQUE,
    issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    file_path VARCHAR(255) NULL,

    UNIQUE KEY uk_user_course (user_id, course_id),
    INDEX idx_user (user_id),
    INDEX idx_course (course_id),

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- User activity log
CREATE TABLE user_activity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    course_id INT NULL,
    lesson_id INT NULL,
    action_type ENUM(
        'LESSON_VIEW', 'LESSON_LIKE', 'LESSON_COMMENT',
        'REVIEW_CREATE', 'REVIEW_COMMENT', 'REVIEW_LIKE',
        'COURSE_ENROLL', 'COURSE_COMPLETE', 'CERT_ISSUED'
    ) NOT NULL,
    metadata JSON NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_user (user_id),
    INDEX idx_course (course_id),
    INDEX idx_lesson (lesson_id),
    INDEX idx_action (action_type),
    INDEX idx_created (created_at),

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE SET NULL,
    FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- User login logs (for calendar check-ins)
CREATE TABLE user_login_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    login_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_login_user (user_id),
    INDEX idx_login_at (login_at),

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
