-- ============================================================================
-- E-Learning Platform: Course Tests and Certificates Feature
-- ============================================================================
-- This script adds support for:
-- 1. Multiple-choice tests for courses (1 test per course)
-- 2. Test questions with 4 answer options each
-- 3. Test attempts and answer tracking
-- 4. Certificate generation for passing students
-- ============================================================================

-- Create database
CREATE DATABASE IF NOT EXISTS elearning_db;
USE elearning_db;

-- Drop tables if they exist (for clean reinstall)
DROP TABLE IF EXISTS test_answers;
DROP TABLE IF EXISTS test_attempts;
-- DROP TABLE IF EXISTS certificates;
DROP TABLE IF EXISTS answer_options;
DROP TABLE IF EXISTS test_questions;
DROP TABLE IF EXISTS course_tests;

-- ============================================================================
-- Table: course_tests
-- Purpose: Store tests for each course (1 test per course)
-- ============================================================================
CREATE TABLE course_tests (
    id INT AUTO_INCREMENT PRIMARY KEY,
    course_id INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    passing_score DECIMAL(5, 2) DEFAULT 80.00 NOT NULL,  -- Percentage (0-100)
    time_limit_minutes INT DEFAULT NULL,                 -- NULL = no time limit
    shuffle_questions BOOLEAN DEFAULT FALSE,             -- Randomize question order
    shuffle_options BOOLEAN DEFAULT FALSE,               -- Randomize answer options
    max_attempts INT DEFAULT NULL,                       -- NULL = unlimited attempts
    is_published BOOLEAN DEFAULT FALSE,                  -- Only published tests are visible
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Constraints
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    UNIQUE KEY unique_course_test (course_id),           -- One test per course
    CHECK (passing_score >= 0 AND passing_score <= 100),
    CHECK (time_limit_minutes IS NULL OR time_limit_minutes > 0),
    CHECK (max_attempts IS NULL OR max_attempts > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- Table: test_questions
-- Purpose: Store questions for each test
-- ============================================================================
CREATE TABLE test_questions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    test_id INT NOT NULL,
    question_text TEXT NOT NULL,
    order_index INT DEFAULT 0,                           -- For ordering questions
    points DECIMAL(5, 2) DEFAULT 1.00,                   -- Points for this question
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Constraints
    FOREIGN KEY (test_id) REFERENCES course_tests(id) ON DELETE CASCADE,
    INDEX idx_test_order (test_id, order_index),
    CHECK (points > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- Table: answer_options
-- Purpose: Store 4 answer choices for each question
-- ============================================================================
CREATE TABLE answer_options (
    id INT AUTO_INCREMENT PRIMARY KEY,
    question_id INT NOT NULL,
    option_text TEXT NOT NULL,
    is_correct BOOLEAN DEFAULT FALSE,                    -- Only 1 should be true per question
    option_letter CHAR(1) NOT NULL,                      -- A, B, C, or D
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Constraints
    FOREIGN KEY (question_id) REFERENCES test_questions(id) ON DELETE CASCADE,
    UNIQUE KEY unique_question_option (question_id, option_letter),
    INDEX idx_question_options (question_id),
    CHECK (option_letter IN ('A', 'B', 'C', 'D'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- Table: test_attempts
-- Purpose: Track when users take tests and their results
-- ============================================================================
CREATE TABLE test_attempts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    test_id INT NOT NULL,
    user_id INT NOT NULL,
    course_id INT NOT NULL,                              -- Denormalized for quick lookup

    -- Attempt details
    attempt_number INT DEFAULT 1,                        -- Which attempt (1st, 2nd, etc.)
    total_questions INT NOT NULL,
    total_points DECIMAL(8, 2) NOT NULL,
    earned_points DECIMAL(8, 2) DEFAULT 0,
    score_percentage DECIMAL(5, 2) DEFAULT 0,            -- Calculated: (earned/total) * 100

    -- Status and timing
    status ENUM('IN_PROGRESS', 'COMPLETED', 'ABANDONED') DEFAULT 'IN_PROGRESS',
    passed BOOLEAN DEFAULT FALSE,                        -- True if score >= passing_score
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL DEFAULT NULL,
    time_spent_seconds INT DEFAULT 0,

    -- Constraints
    FOREIGN KEY (test_id) REFERENCES course_tests(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    INDEX idx_user_test (user_id, test_id),
    INDEX idx_course_attempts (course_id, user_id),
    INDEX idx_completed (completed_at),
    CHECK (earned_points >= 0 AND earned_points <= total_points),
    CHECK (score_percentage >= 0 AND score_percentage <= 100),
    CHECK (attempt_number > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- Table: test_answers
-- Purpose: Store user's answer for each question in an attempt
-- ============================================================================
CREATE TABLE test_answers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    attempt_id INT NOT NULL,
    question_id INT NOT NULL,
    selected_option_id INT NOT NULL,                     -- Which option the user selected
    is_correct BOOLEAN DEFAULT FALSE,                    -- Was the selected option correct?
    points_earned DECIMAL(5, 2) DEFAULT 0,
    answered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    FOREIGN KEY (attempt_id) REFERENCES test_attempts(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES test_questions(id) ON DELETE CASCADE,
    FOREIGN KEY (selected_option_id) REFERENCES answer_options(id) ON DELETE CASCADE,
    UNIQUE KEY unique_attempt_question (attempt_id, question_id),  -- One answer per question per attempt
    INDEX idx_attempt_answers (attempt_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- Table: certificates
-- Purpose: Store generated certificates for students who pass tests
-- ============================================================================
-- CREATE TABLE certificates (
--     id INT AUTO_INCREMENT PRIMARY KEY,
--     user_id INT NOT NULL,
--     course_id INT NOT NULL,
--     test_id INT NULL,                                    -- For test-based certificates (NULL for legacy course completion)
--     attempt_id INT NULL,                                 -- The passing attempt (NULL for legacy)

--     -- Certificate details
--     certificate_code VARCHAR(50) NOT NULL UNIQUE,        -- Unique verification code (e.g., CERT-2024-XXXX)
--     student_name VARCHAR(200),                           -- Student's full name (nullable for legacy)
--     course_title VARCHAR(255),                           -- Course title (denormalized, nullable for legacy)
--     instructor_name VARCHAR(200),                        -- Instructor's name (denormalized)
--     score_achieved DECIMAL(5, 2),                        -- Score percentage (NULL for legacy course completion)

--     -- Legacy support for course completion certificates
--     file_path VARCHAR(500),                              -- Path to certificate image (for legacy system)

--     -- Dates
--     issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     expires_at TIMESTAMP NULL DEFAULT NULL,              -- NULL = never expires

--     -- Status
--     is_revoked BOOLEAN DEFAULT FALSE,                    -- Can be revoked by admin
--     revoked_at TIMESTAMP NULL DEFAULT NULL,
--     revoked_reason TEXT,

--     -- Constraints
--     FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
--     FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
--     FOREIGN KEY (test_id) REFERENCES course_tests(id) ON DELETE CASCADE,
--     FOREIGN KEY (attempt_id) REFERENCES test_attempts(id) ON DELETE CASCADE,
--     UNIQUE KEY unique_user_course_certificate (user_id, course_id),  -- One certificate per user per course
--     INDEX idx_certificate_code (certificate_code),
--     INDEX idx_user_certificates (user_id),
--     INDEX idx_course_certificates (course_id),
--     INDEX idx_issued_date (issued_at)
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- Sample Data for Testing
-- ============================================================================

-- Sample Test for a Course (assuming course_id 1 exists)
INSERT INTO course_tests (course_id, title, description, passing_score, time_limit_minutes, shuffle_questions, is_published)
VALUES
(1, 'Introduction to Programming - Final Exam', 'This test covers all topics from the course including variables, loops, functions, and object-oriented programming.', 80.00, 60, TRUE, TRUE);

-- Sample Questions for Test ID 1
INSERT INTO test_questions (test_id, question_text, order_index, points) VALUES
(1, 'What is a variable in programming?', 1, 1.00),
(1, 'Which of the following is a loop structure?', 2, 1.00),
(1, 'What does OOP stand for?', 3, 1.00),
(1, 'Which data type stores whole numbers?', 4, 1.00),
(1, 'What is the purpose of a function?', 5, 1.00);

-- Answer Options for Question 1 (What is a variable?)
INSERT INTO answer_options (question_id, option_text, is_correct, option_letter) VALUES
(1, 'A container for storing data values', TRUE, 'A'),
(1, 'A type of loop', FALSE, 'B'),
(1, 'A mathematical operator', FALSE, 'C'),
(1, 'A type of function', FALSE, 'D');

-- Answer Options for Question 2 (Loop structure)
INSERT INTO answer_options (question_id, option_text, is_correct, option_letter) VALUES
(2, 'if-else', FALSE, 'A'),
(2, 'for loop', TRUE, 'B'),
(2, 'variable', FALSE, 'C'),
(2, 'class', FALSE, 'D');

-- Answer Options for Question 3 (OOP meaning)
INSERT INTO answer_options (question_id, option_text, is_correct, option_letter) VALUES
(3, 'Out Of Place', FALSE, 'A'),
(3, 'Object Oriented Programming', TRUE, 'B'),
(3, 'Only One Process', FALSE, 'C'),
(3, 'Open Operating Platform', FALSE, 'D');

-- Answer Options for Question 4 (Data type for whole numbers)
INSERT INTO answer_options (question_id, option_text, is_correct, option_letter) VALUES
(4, 'String', FALSE, 'A'),
(4, 'Boolean', FALSE, 'B'),
(4, 'Integer', TRUE, 'C'),
(4, 'Float', FALSE, 'D');

-- Answer Options for Question 5 (Purpose of a function)
INSERT INTO answer_options (question_id, option_text, is_correct, option_letter) VALUES
(5, 'To store data', FALSE, 'A'),
(5, 'To create loops', FALSE, 'B'),
(5, 'To organize reusable blocks of code', TRUE, 'C'),
(5, 'To define variables', FALSE, 'D');

-- ============================================================================
-- Useful Queries for the Application
-- ============================================================================

-- Get test with all questions and options for a course
-- SELECT ct.*, tq.*, ao.*
-- FROM course_tests ct
-- LEFT JOIN test_questions tq ON ct.id = tq.test_id
-- LEFT JOIN answer_options ao ON tq.id = ao.question_id
-- WHERE ct.course_id = ? AND ct.is_published = TRUE
-- ORDER BY tq.order_index, ao.option_letter;

-- Get user's test attempts for a course
-- SELECT ta.*, ct.title, ct.passing_score
-- FROM test_attempts ta
-- JOIN course_tests ct ON ta.test_id = ct.id
-- WHERE ta.user_id = ? AND ta.course_id = ?
-- ORDER BY ta.started_at DESC;

-- Check if user has passed the test
-- SELECT ta.*, c.id as certificate_id
-- FROM test_attempts ta
-- LEFT JOIN certificates c ON ta.id = c.attempt_id
-- WHERE ta.user_id = ? AND ta.test_id = ? AND ta.passed = TRUE
-- ORDER BY ta.score_percentage DESC
-- LIMIT 1;

-- Get certificate by verification code
-- SELECT c.*, u.full_name, co.title as course_title
-- FROM certificates c
-- JOIN users u ON c.user_id = u.id
-- JOIN courses co ON c.course_id = co.id
-- WHERE c.certificate_code = ? AND c.is_revoked = FALSE;

-- Calculate test statistics for instructor
-- SELECT
--     ct.title,
--     COUNT(DISTINCT ta.user_id) as total_students_attempted,
--     COUNT(ta.id) as total_attempts,
--     AVG(ta.score_percentage) as average_score,
--     SUM(CASE WHEN ta.passed = TRUE THEN 1 ELSE 0 END) as passed_count,
--     SUM(CASE WHEN ta.passed = FALSE THEN 1 ELSE 0 END) as failed_count
-- FROM course_tests ct
-- LEFT JOIN test_attempts ta ON ct.id = ta.test_id AND ta.status = 'COMPLETED'
-- WHERE ct.course_id = ?
-- GROUP BY ct.id;

-- ============================================================================
-- Business Rules to Implement in Application Layer
-- ============================================================================
-- 1. User can only access test if enrollment.progress_percent = 100
-- 2. Respect max_attempts limit (if set)
-- 3. Generate unique certificate_code (e.g., CERT-YYYY-COURSEID-USERID-RANDOM)
-- 4. Auto-calculate score_percentage when test is completed
-- 5. Create certificate automatically when passed = TRUE
-- 6. Only show correct answers AFTER test is completed
-- 7. Track time_spent_seconds during the test
-- 8. If time_limit_minutes is set, auto-submit when time expires
-- 9. Prevent editing test if there are already attempts
-- 10. Each test must have at least 1 question with 4 options
-- 11. Exactly 1 option must be marked as correct per question
-- ============================================================================
