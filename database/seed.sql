USE elearning_db;

-- Default password for all seeded users
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
INSERT INTO courses (instructor_id, title, description, status, category, difficulty_level, estimated_hours, is_published, approved_by, approved_at) VALUES
(2, 'Java Foundations', 'Master the fundamentals of Java programming including OOP principles, data structures, and best practices.', 'APPROVED', 'Programming', 'BEGINNER', 40, TRUE, 1, NOW()),
(2, 'Advanced Java Development', 'Deep dive into advanced Java concepts including concurrency, design patterns, and enterprise development.', 'APPROVED', 'Programming', 'ADVANCED', 60, TRUE, 1, NOW()),
(3, 'SQL for Beginners', 'Learn MySQL basics, queries, and database design principles.', 'APPROVED', 'Database', 'BEGINNER', 25, TRUE, 1, NOW()),
(3, 'Web Development with Spring Boot', 'Build modern web applications using Spring Boot framework.', 'PENDING', 'Web Development', 'INTERMEDIATE', 50, FALSE, NULL, NULL);

-- Insert lessons for Java Foundations (course_id: 1)
INSERT INTO lessons (course_id, title, video_path, content_text, duration_minutes, order_index, is_preview) VALUES
(1, 'Welcome to Java', '/videos/java-foundations/lesson1.mp4', 'Introduction to Java programming language and its ecosystem.', 15, 1, TRUE),
(1, 'Setting Up Your Development Environment', '/videos/java-foundations/lesson2.mp4', 'Learn how to install JDK and set up IntelliJ IDEA.', 20, 2, TRUE),
(1, 'Your First Java Program', '/videos/java-foundations/lesson3.mp4', 'Write and run your first Hello World program in Java.', 25, 3, FALSE),
(1, 'Variables and Data Types', '/videos/java-foundations/lesson4.mp4', 'Understanding primitive data types and variables in Java.', 30, 4, FALSE),
(1, 'Classes and Objects', '/videos/java-foundations/lesson5.mp4', 'Learn object-oriented programming with classes and objects.', 35, 5, FALSE);

-- Insert lessons for SQL for Beginners (course_id: 3)
INSERT INTO lessons (course_id, title, video_path, content_text, duration_minutes, order_index, is_preview) VALUES
(3, 'Introduction to Databases', '/videos/sql-basics/lesson1.mp4', 'Understanding relational databases and SQL.', 18, 1, TRUE),
(3, 'SELECT Statements', '/videos/sql-basics/lesson2.mp4', 'Learn how to query data using SELECT statements.', 25, 2, FALSE),
(3, 'Filtering Data with WHERE', '/videos/sql-basics/lesson3.mp4', 'Master data filtering techniques.', 22, 3, FALSE);

-- Insert enrollments
INSERT INTO enrollments (user_id, course_id, progress_percent, last_accessed_at) VALUES
(4, 1, 60.00, NOW()),
(4, 3, 33.33, NOW()),
(5, 1, 20.00, NOW()),
(6, 3, 0.00, NULL);

-- Insert lesson progress
INSERT INTO lesson_progress (user_id, lesson_id, is_completed, completed_at, last_opened_at) VALUES
-- Student 1 progress in Java Foundations
(4, 1, TRUE, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
(4, 2, TRUE, DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)),
(4, 3, TRUE, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
(4, 4, FALSE, NULL, DATE_SUB(NOW(), INTERVAL 1 DAY)),
-- Student 1 progress in SQL
(4, 6, TRUE, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4, 7, FALSE, NULL, NOW()),
-- Student 2 progress in Java Foundations
(5, 1, TRUE, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
(5, 2, FALSE, NULL, NOW());

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
