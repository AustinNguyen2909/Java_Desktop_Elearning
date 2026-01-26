-- E-Learning Platform Seed Data
-- This file provides sample data for testing the three critical features:
-- 1. Course Creation with Hero Images (thumbnail_path)
-- 2. Video Upload per Lesson (video_path)
-- 3. Video-Based Progress Tracking (progress_percent, is_completed)

USE elearning_db;

-- Default password for all seeded users (BCrypt encrypted)
-- admin: admin123 (hash: $2a$12$Q4.X/aF4MtZmhRaZXQs5nOWlCpLq/eQ1OIwEOxa3VqXoUy4Uj4uVS)
-- instructor: instructor123 (hash: $2a$12$Fz/Cxu1LfkT3lXF34j7dBup1s7i4EpHiD3F1wEkStw3LT0.nZWDFG)
-- student: student123 (hash: $2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW)

-- Insert users
INSERT INTO users (username, password_hash, role, email, phone, full_name, status) VALUES
('admin', '$2a$12$Q4.X/aF4MtZmhRaZXQs5nOWlCpLq/eQ1OIwEOxa3VqXoUy4Uj4uVS', 'ADMIN', 'admin@elearning.com', '1000000001', 'System Administrator', 'ACTIVE'),
('instructor1', '$2a$12$Fz/Cxu1LfkT3lXF34j7dBup1s7i4EpHiD3F1wEkStw3LT0.nZWDFG', 'INSTRUCTOR', 'instructor1@elearning.com', '1000000002', 'John Instructor', 'ACTIVE'),
('instructor2', '$2a$12$Fz/Cxu1LfkT3lXF34j7dBup1s7i4EpHiD3F1wEkStw3LT0.nZWDFG', 'INSTRUCTOR', 'instructor2@elearning.com', '1000000003', 'Sarah Teacher', 'ACTIVE'),
('student1', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student1@elearning.com', '1000000004', 'Jane Student', 'ACTIVE'),
('student2', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student2@elearning.com', '1000000005', 'Bob Learner', 'ACTIVE'),
('student3', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student3@elearning.com', NULL, 'Alice Brown', 'PENDING');

-- Insert courses
-- NOTE: thumbnail_path values are examples. In actual implementation, these would be absolute paths
-- Example format: /path/to/project/thumbnails/course_1/course_1_1674567890.jpg
-- FEATURE 1: Hero Images (thumbnail_path)
INSERT INTO courses (instructor_id, title, description, thumbnail_path, status, category, difficulty_level, estimated_hours, is_published, approved_by, approved_at) VALUES
(2, 'Java Foundations', 'Master the fundamentals of Java programming including OOP principles, data structures, and best practices.', 'thumbnails/course_1/course_1_1674567890.jpg', 'APPROVED', 'Programming', 'BEGINNER', 40, TRUE, 1, NOW()),
(2, 'Advanced Java Development', 'Deep dive into advanced Java concepts including concurrency, design patterns, and enterprise development.', 'thumbnails/course_2/course_2_1674567891.jpg', 'APPROVED', 'Programming', 'ADVANCED', 60, TRUE, 1, NOW()),
(3, 'SQL for Beginners', 'Learn MySQL basics, queries, and database design principles.', 'thumbnails/course_3/course_3_1674567892.png', 'APPROVED', 'Database', 'BEGINNER', 25, TRUE, 1, NOW()),
(3, 'Web Development with Spring Boot', 'Build modern web applications using Spring Boot framework.', 'thumbnails/course_4/course_4_1674567893.jpg', 'PENDING', 'Web Development', 'INTERMEDIATE', 50, FALSE, NULL, NULL);

-- Insert lessons for Java Foundations (course_id: 1)
-- NOTE: video_path values follow the implementation pattern: videos/course_X/lesson_Y_timestamp.ext
-- In actual implementation, these would be absolute paths generated during upload
-- FEATURE 2: Video Files (video_path)
INSERT INTO lessons (course_id, title, video_path, content_text, duration_minutes, order_index, is_preview) VALUES
(1, 'Welcome to Java', 'videos/course_1/lesson_1_1674567890.mp4', 'Introduction to Java programming language and its ecosystem. Learn about Java history, features, and why it is one of the most popular programming languages.', 15, 1, TRUE),
(1, 'Setting Up Your Development Environment', 'videos/course_1/lesson_2_1674567891.mp4', 'Learn how to install JDK and set up IntelliJ IDEA. We will configure your development environment step by step.', 20, 2, TRUE),
(1, 'Your First Java Program', 'videos/course_1/lesson_3_1674567892.mp4', 'Write and run your first Hello World program in Java. Understand the basic structure of a Java application.', 25, 3, FALSE),
(1, 'Variables and Data Types', 'videos/course_1/lesson_4_1674567893.mp4', 'Understanding primitive data types and variables in Java. Learn about int, double, boolean, char, and String.', 30, 4, FALSE),
(1, 'Classes and Objects', 'videos/course_1/lesson_5_1674567894.mp4', 'Learn object-oriented programming with classes and objects. Create your first custom class and instantiate objects.', 35, 5, FALSE);

-- Insert lessons for SQL for Beginners (course_id: 3)
INSERT INTO lessons (course_id, title, video_path, content_text, duration_minutes, order_index, is_preview) VALUES
(3, 'Introduction to Databases', 'videos/course_3/lesson_6_1674567895.mp4', 'Understanding relational databases and SQL. Learn about tables, rows, columns, and relationships.', 18, 1, TRUE),
(3, 'SELECT Statements', 'videos/course_3/lesson_7_1674567896.mp4', 'Learn how to query data using SELECT statements. Master the foundation of SQL queries.', 25, 2, FALSE),
(3, 'Filtering Data with WHERE', 'videos/course_3/lesson_8_1674567897.mp4', 'Master data filtering techniques using WHERE clause. Learn comparison operators and logical conditions.', 22, 3, FALSE);

-- Insert enrollments
-- FEATURE 3: Progress Tracking
-- progress_percent is calculated as: (completed_lessons / total_lessons) × 100
-- Student 1 (user_id: 4):
--   - Java Foundations: 3/5 lessons completed = 60.00%
--   - SQL for Beginners: 1/3 lessons completed = 33.33%
-- Student 2 (user_id: 5):
--   - Java Foundations: 1/5 lessons completed = 20.00%
-- Student 3 (user_id: 6):
--   - SQL for Beginners: 0/3 lessons completed = 0.00%
INSERT INTO enrollments (user_id, course_id, progress_percent, last_accessed_at) VALUES
(4, 1, 60.00, NOW()),
(4, 3, 33.33, NOW()),
(5, 1, 20.00, NOW()),
(6, 3, 0.00, NULL);

-- Insert lesson progress
-- FEATURE 3: Video Completion Tracking
-- is_completed = TRUE means student has watched and marked the video as complete
-- This data is used to calculate the progress_percent in enrollments table
INSERT INTO lesson_progress (user_id, lesson_id, is_completed, completed_at, last_opened_at) VALUES
-- Student 1 (Jane Student) progress in Java Foundations
-- Completed: Lessons 1, 2, 3 (3/5 = 60%)
(4, 1, TRUE, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),  -- Welcome to Java ✓
(4, 2, TRUE, DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)),  -- Setting Up Environment ✓
(4, 3, TRUE, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),  -- First Java Program ✓
(4, 4, FALSE, NULL, DATE_SUB(NOW(), INTERVAL 1 DAY)),                            -- Variables (watching now)
-- Student 1 progress in SQL for Beginners
-- Completed: Lesson 6 (1/3 = 33.33%)
(4, 6, TRUE, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),  -- Introduction to Databases ✓
(4, 7, FALSE, NULL, NOW()),                                                       -- SELECT Statements (watching now)
-- Student 2 (Bob Learner) progress in Java Foundations
-- Completed: Lesson 1 (1/5 = 20%)
(5, 1, TRUE, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),  -- Welcome to Java ✓
(5, 2, FALSE, NULL, NOW());                                                       -- Setting Up Environment (watching now)

-- Insert comments
INSERT INTO comments (user_id, lesson_id, parent_id, content) VALUES
(4, 1, NULL, 'Great introduction! Very clear explanation.'),
(5, 1, NULL, 'Is Java pass-by-value or pass-by-reference?'),
(2, 1, 2, 'Good question! Java is strictly pass-by-value. Object references are passed by value.'),
(4, 3, NULL, 'The Hello World example helped me understand the basic structure.'),
(4, 6, NULL, 'Looking forward to learning more SQL queries!');

-- Insert reviews
INSERT INTO reviews (user_id, course_id, rating, comment) VALUES
(4, 1, 5, 'Excellent course! The instructor explains complex concepts in a very understandable way.'),
(5, 1, 4, 'Good course, but I wish there were more practical exercises.'),
(4, 3, 5, 'Perfect for beginners. The pace is just right and examples are very practical.');

COMMIT;

-- =====================================================
-- VERIFICATION QUERIES FOR THE THREE CRITICAL FEATURES
-- =====================================================

-- FEATURE 1: Verify courses with hero images (thumbnail_path)
-- SELECT id, title, thumbnail_path, status, is_published FROM courses;

-- FEATURE 2: Verify lessons with video files (video_path)
-- SELECT l.id, l.title, l.video_path, l.order_index, c.title as course_title
-- FROM lessons l JOIN courses c ON l.course_id = c.id
-- ORDER BY c.id, l.order_index;

-- FEATURE 3: Verify progress tracking
-- View enrollment progress for all students:
-- SELECT
--     u.full_name as student,
--     c.title as course,
--     e.progress_percent,
--     e.enrolled_at,
--     e.last_accessed_at,
--     e.completed_at
-- FROM enrollments e
-- JOIN users u ON e.user_id = u.id
-- JOIN courses c ON e.course_id = c.id
-- ORDER BY u.id, c.id;

-- View detailed lesson completion for a specific student (e.g., Jane Student, user_id: 4):
-- SELECT
--     c.title as course,
--     l.title as lesson,
--     l.order_index,
--     lp.is_completed,
--     lp.completed_at,
--     lp.last_opened_at
-- FROM lesson_progress lp
-- JOIN lessons l ON lp.lesson_id = l.id
-- JOIN courses c ON l.course_id = c.id
-- WHERE lp.user_id = 4
-- ORDER BY c.id, l.order_index;

-- Calculate progress manually to verify formula:
-- For Jane Student (user_id: 4) in Java Foundations (course_id: 1):
-- SELECT
--     (SELECT COUNT(*) FROM lesson_progress lp
--      JOIN lessons l ON lp.lesson_id = l.id
--      WHERE lp.user_id = 4 AND l.course_id = 1 AND lp.is_completed = TRUE) as completed,
--     (SELECT COUNT(*) FROM lessons WHERE course_id = 1) as total,
--     ROUND(
--         (SELECT COUNT(*) FROM lesson_progress lp
--          JOIN lessons l ON lp.lesson_id = l.id
--          WHERE lp.user_id = 4 AND l.course_id = 1 AND lp.is_completed = TRUE) * 100.0 /
--         (SELECT COUNT(*) FROM lessons WHERE course_id = 1)
--     , 2) as calculated_progress;
-- Expected result: 3 completed, 5 total, 60.00% progress
