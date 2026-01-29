-- E-Learning Platform Clear Data Script
-- This script removes all data from tables while preserving the schema
-- Execute this before running seed.sql to ensure a clean data state

USE elearning_db;

-- Disable foreign key checks temporarily for faster deletion
SET FOREIGN_KEY_CHECKS = 0;

-- Clear all tables in reverse order of dependencies
-- Start with child tables first, then parent tables

-- Activity and analytics tables
TRUNCATE TABLE user_activity;

-- Certificate tables
TRUNCATE TABLE certificates;

-- Course review related tables
TRUNCATE TABLE course_review_likes;
TRUNCATE TABLE course_review_comments;
TRUNCATE TABLE course_reviews;

-- Lesson related tables
TRUNCATE TABLE lesson_views;
TRUNCATE TABLE lesson_likes;
TRUNCATE TABLE lesson_comments;
TRUNCATE TABLE lesson_progress;

-- Enrollment table
TRUNCATE TABLE enrollments;

-- Lessons table
TRUNCATE TABLE lessons;

-- Courses table
TRUNCATE TABLE courses;

-- Users table
TRUNCATE TABLE users;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- Reset auto-increment counters to 1
ALTER TABLE users AUTO_INCREMENT = 1;
ALTER TABLE courses AUTO_INCREMENT = 1;
ALTER TABLE lessons AUTO_INCREMENT = 1;
ALTER TABLE enrollments AUTO_INCREMENT = 1;
ALTER TABLE lesson_progress AUTO_INCREMENT = 1;
ALTER TABLE lesson_comments AUTO_INCREMENT = 1;
ALTER TABLE lesson_likes AUTO_INCREMENT = 1;
ALTER TABLE lesson_views AUTO_INCREMENT = 1;
ALTER TABLE course_reviews AUTO_INCREMENT = 1;
ALTER TABLE course_review_comments AUTO_INCREMENT = 1;
ALTER TABLE course_review_likes AUTO_INCREMENT = 1;
ALTER TABLE certificates AUTO_INCREMENT = 1;
ALTER TABLE user_activity AUTO_INCREMENT = 1;

COMMIT;

-- Verification: Check that all tables are empty
SELECT
    'users' AS table_name, COUNT(*) AS row_count FROM users
UNION ALL SELECT 'courses', COUNT(*) FROM courses
UNION ALL SELECT 'lessons', COUNT(*) FROM lessons
UNION ALL SELECT 'enrollments', COUNT(*) FROM enrollments
UNION ALL SELECT 'lesson_progress', COUNT(*) FROM lesson_progress
UNION ALL SELECT 'lesson_comments', COUNT(*) FROM lesson_comments
UNION ALL SELECT 'lesson_likes', COUNT(*) FROM lesson_likes
UNION ALL SELECT 'lesson_views', COUNT(*) FROM lesson_views
UNION ALL SELECT 'course_reviews', COUNT(*) FROM course_reviews
UNION ALL SELECT 'course_review_comments', COUNT(*) FROM course_review_comments
UNION ALL SELECT 'course_review_likes', COUNT(*) FROM course_review_likes
UNION ALL SELECT 'certificates', COUNT(*) FROM certificates
UNION ALL SELECT 'user_activity', COUNT(*) FROM user_activity;

-- Expected output: All tables should have 0 rows
