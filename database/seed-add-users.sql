
-- Additional users for registration trend chart (90 users spread over 6 months)
-- Password for all: student123 / instructor123 (same hash as above)

USE elearning_db;

-- Month 1: 6 months ago (8 users: 2 instructors, 6 students)
INSERT INTO users (username, password_hash, role, email, phone, full_name, status, created_at) VALUES
('instructor5', '$2a$12$Fz/Cxu1LfkT3lXF34j7dBup1s7i4EpHiD3F1wEkStw3LT0.nZWDFG', 'INSTRUCTOR', 'instructor5@elearning.com', '1000000010', 'Michael Chen', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 6 MONTH)),
('instructor6', '$2a$12$Fz/Cxu1LfkT3lXF34j7dBup1s7i4EpHiD3F1wEkStw3LT0.nZWDFG', 'INSTRUCTOR', 'instructor6@elearning.com', '1000000011', 'Sophie Martinez', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 6 MONTH)),
('student6', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student6@elearning.com', '1000000012', 'Oliver Wilson', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 6 MONTH)),
('student7', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student7@elearning.com', NULL, 'Emma Davis', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 6 MONTH)),
('student8', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student8@elearning.com', '1000000013', 'Noah Anderson', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 6 MONTH)),
('student9', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student9@elearning.com', NULL, 'Ava Taylor', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 6 MONTH)),
('student10', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student10@elearning.com', '1000000014', 'Ethan Thomas', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 6 MONTH)),
('student11', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student11@elearning.com', NULL, 'Isabella Jackson', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 6 MONTH));

-- Month 2: 5 months ago (12 users: 4 instructors, 8 students)
INSERT INTO users (username, password_hash, role, email, phone, full_name, status, created_at) VALUES
('instructor7', '$2a$12$Fz/Cxu1LfkT3lXF34j7dBup1s7i4EpHiD3F1wEkStw3LT0.nZWDFG', 'INSTRUCTOR', 'instructor7@elearning.com', '1000000015', 'James White', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 5 MONTH)),
('instructor8', '$2a$12$Fz/Cxu1LfkT3lXF34j7dBup1s7i4EpHiD3F1wEkStw3LT0.nZWDFG', 'INSTRUCTOR', 'instructor8@elearning.com', NULL, 'Charlotte Harris', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 5 MONTH)),
('instructor9', '$2a$12$Fz/Cxu1LfkT3lXF34j7dBup1s7i4EpHiD3F1wEkStw3LT0.nZWDFG', 'INSTRUCTOR', 'instructor9@elearning.com', '1000000016', 'William Clark', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 5 MONTH)),
('instructor10', '$2a$12$Fz/Cxu1LfkT3lXF34j7dBup1s7i4EpHiD3F1wEkStw3LT0.nZWDFG', 'INSTRUCTOR', 'instructor10@elearning.com', NULL, 'Amelia Lewis', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 5 MONTH)),
('student12', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student12@elearning.com', '1000000017', 'Benjamin Lee', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 5 MONTH)),
('student13', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student13@elearning.com', NULL, 'Harper Walker', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 5 MONTH)),
('student14', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student14@elearning.com', '1000000018', 'Lucas Hall', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 5 MONTH)),
('student15', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student15@elearning.com', NULL, 'Sophia Allen', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 5 MONTH)),
('student16', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student16@elearning.com', '1000000019', 'Mason Young', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 5 MONTH)),
('student17', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student17@elearning.com', NULL, 'Evelyn King', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 5 MONTH)),
('student18', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student18@elearning.com', '1000000020', 'Elijah Wright', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 5 MONTH)),
('student19', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student19@elearning.com', NULL, 'Abigail Scott', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 5 MONTH));

-- Month 3: 4 months ago (15 users: 5 instructors, 10 students)
INSERT INTO users (username, password_hash, role, email, phone, full_name, status, created_at) VALUES
('instructor11', '$2a$12$Fz/Cxu1LfkT3lXF34j7dBup1s7i4EpHiD3F1wEkStw3LT0.nZWDFG', 'INSTRUCTOR', 'instructor11@elearning.com', '1000000021', 'Logan Torres', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 4 MONTH)),
('instructor12', '$2a$12$Fz/Cxu1LfkT3lXF34j7dBup1s7i4EpHiD3F1wEkStw3LT0.nZWDFG', 'INSTRUCTOR', 'instructor12@elearning.com', NULL, 'Ella Nguyen', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 4 MONTH)),
('instructor13', '$2a$12$Fz/Cxu1LfkT3lXF34j7dBup1s7i4EpHiD3F1wEkStw3LT0.nZWDFG', 'INSTRUCTOR', 'instructor13@elearning.com', '1000000022', 'Alexander Hill', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 4 MONTH)),
('instructor14', '$2a$12$Fz/Cxu1LfkT3lXF34j7dBup1s7i4EpHiD3F1wEkStw3LT0.nZWDFG', 'INSTRUCTOR', 'instructor14@elearning.com', NULL, 'Scarlett Flores', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 4 MONTH)),
('instructor15', '$2a$12$Fz/Cxu1LfkT3lXF34j7dBup1s7i4EpHiD3F1wEkStw3LT0.nZWDFG', 'INSTRUCTOR', 'instructor15@elearning.com', '1000000023', 'Henry Green', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 4 MONTH)),
('student20', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student20@elearning.com', NULL, 'Aria Adams', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 4 MONTH)),
('student21', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student21@elearning.com', '1000000024', 'Sebastian Baker', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 4 MONTH)),
('student22', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student22@elearning.com', NULL, 'Luna Gonzalez', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 4 MONTH)),
('student23', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student23@elearning.com', '1000000025', 'Jackson Nelson', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 4 MONTH)),
('student24', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student24@elearning.com', NULL, 'Chloe Carter', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 4 MONTH)),
('student25', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student25@elearning.com', '1000000026', 'Aiden Mitchell', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 4 MONTH)),
('student26', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student26@elearning.com', NULL, 'Mila Perez', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 4 MONTH)),
('student27', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student27@elearning.com', '1000000027', 'Matthew Roberts', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 4 MONTH)),
('student28', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student28@elearning.com', NULL, 'Grace Turner', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 4 MONTH)),
('student29', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student29@elearning.com', '1000000028', 'Daniel Phillips', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 4 MONTH));

-- Month 4: 3 months ago (18 users: 5 instructors, 13 students)
INSERT INTO users (username, password_hash, role, email, phone, full_name, status, created_at) VALUES
('instructor16', '$2a$12$Fz/Cxu1LfkT3lXF34j7dBup1s7i4EpHiD3F1wEkStw3LT0.nZWDFG', 'INSTRUCTOR', 'instructor16@elearning.com', '1000000029', 'Victoria Campbell', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 3 MONTH)),
('instructor17', '$2a$12$Fz/Cxu1LfkT3lXF34j7dBup1s7i4EpHiD3F1wEkStw3LT0.nZWDFG', 'INSTRUCTOR', 'instructor17@elearning.com', NULL, 'Samuel Parker', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 3 MONTH)),
('instructor18', '$2a$12$Fz/Cxu1LfkT3lXF34j7dBup1s7i4EpHiD3F1wEkStw3LT0.nZWDFG', 'INSTRUCTOR', 'instructor18@elearning.com', '1000000030', 'Penelope Evans', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 3 MONTH)),
('instructor19', '$2a$12$Fz/Cxu1LfkT3lXF34j7dBup1s7i4EpHiD3F1wEkStw3LT0.nZWDFG', 'INSTRUCTOR', 'instructor19@elearning.com', NULL, 'Joseph Edwards', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 3 MONTH)),
('instructor20', '$2a$12$Fz/Cxu1LfkT3lXF34j7dBup1s7i4EpHiD3F1wEkStw3LT0.nZWDFG', 'INSTRUCTOR', 'instructor20@elearning.com', '1000000031', 'Layla Collins', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 3 MONTH)),
('student30', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student30@elearning.com', NULL, 'Carter Stewart', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 3 MONTH)),
('student31', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student31@elearning.com', '1000000032', 'Zoey Sanchez', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 3 MONTH)),
('student32', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student32@elearning.com', NULL, 'Wyatt Morris', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 3 MONTH)),
('student33', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student33@elearning.com', '1000000033', 'Nora Rogers', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 3 MONTH)),
('student34', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student34@elearning.com', NULL, 'Owen Reed', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 3 MONTH)),
('student35', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student35@elearning.com', '1000000034', 'Riley Cook', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 3 MONTH)),
('student36', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student36@elearning.com', NULL, 'Jack Morgan', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 3 MONTH)),
('student37', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student37@elearning.com', '1000000035', 'Lily Bell', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 3 MONTH)),
('student38', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student38@elearning.com', NULL, 'Grayson Murphy', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 3 MONTH)),
('student39', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student39@elearning.com', '1000000036', 'Eleanor Bailey', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 3 MONTH)),
('student40', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student40@elearning.com', NULL, 'Leo Rivera', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 3 MONTH)),
('student41', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student41@elearning.com', '1000000037', 'Hannah Cooper', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 3 MONTH)),
('student42', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student42@elearning.com', NULL, 'Nathan Richardson', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 3 MONTH));

-- Month 5: 2 months ago (20 users: 6 instructors, 14 students)
INSERT INTO users (username, password_hash, role, email, phone, full_name, status, created_at) VALUES
('instructor21', '$2a$12$Fz/Cxu1LfkT3lXF34j7dBup1s7i4EpHiD3F1wEkStw3LT0.nZWDFG', 'INSTRUCTOR', 'instructor21@elearning.com', '1000000038', 'Dylan Cox', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 2 MONTH)),
('instructor22', '$2a$12$Fz/Cxu1LfkT3lXF34j7dBup1s7i4EpHiD3F1wEkStw3LT0.nZWDFG', 'INSTRUCTOR', 'instructor22@elearning.com', NULL, 'Zoe Howard', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 2 MONTH)),
('instructor23', '$2a$12$Fz/Cxu1LfkT3lXF34j7dBup1s7i4EpHiD3F1wEkStw3LT0.nZWDFG', 'INSTRUCTOR', 'instructor23@elearning.com', '1000000039', 'Isaac Ward', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 2 MONTH)),
('instructor24', '$2a$12$Fz/Cxu1LfkT3lXF34j7dBup1s7i4EpHiD3F1wEkStw3LT0.nZWDFG', 'INSTRUCTOR', 'instructor24@elearning.com', NULL, 'Addison Torres', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 2 MONTH)),
('instructor25', '$2a$12$Fz/Cxu1LfkT3lXF34j7dBup1s7i4EpHiD3F1wEkStw3LT0.nZWDFG', 'INSTRUCTOR', 'instructor25@elearning.com', '1000000040', 'Gabriel Peterson', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 2 MONTH)),
('instructor26', '$2a$12$Fz/Cxu1LfkT3lXF34j7dBup1s7i4EpHiD3F1wEkStw3LT0.nZWDFG', 'INSTRUCTOR', 'instructor26@elearning.com', NULL, 'Lillian Gray', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 2 MONTH)),
('student43', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student43@elearning.com', '1000000041', 'Lincoln Ramirez', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 2 MONTH)),
('student44', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student44@elearning.com', NULL, 'Hazel James', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 2 MONTH)),
('student45', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student45@elearning.com', '1000000042', 'Hudson Watson', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 2 MONTH)),
('student46', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student46@elearning.com', NULL, 'Violet Brooks', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 2 MONTH)),
('student47', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student47@elearning.com', '1000000043', 'Levi Kelly', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 2 MONTH)),
('student48', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student48@elearning.com', NULL, 'Aurora Sanders', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 2 MONTH)),
('student49', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student49@elearning.com', '1000000044', 'Christian Price', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 2 MONTH)),
('student50', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student50@elearning.com', NULL, 'Savannah Bennett', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 2 MONTH)),
('student51', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student51@elearning.com', '1000000045', 'Isaiah Wood', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 2 MONTH)),
('student52', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student52@elearning.com', NULL, 'Brooklyn Barnes', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 2 MONTH)),
('student53', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student53@elearning.com', '1000000046', 'Thomas Ross', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 2 MONTH)),
('student54', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student54@elearning.com', NULL, 'Bella Henderson', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 2 MONTH)),
('student55', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student55@elearning.com', '1000000047', 'Jaxon Coleman', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 2 MONTH)),
('student56', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student56@elearning.com', NULL, 'Claire Jenkins', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 2 MONTH));

-- Month 6: 1 month ago (17 users: 5 instructors, 12 students)
INSERT INTO users (username, password_hash, role, email, phone, full_name, status, created_at) VALUES
('instructor27', '$2a$12$Fz/Cxu1LfkT3lXF34j7dBup1s7i4EpHiD3F1wEkStw3LT0.nZWDFG', 'INSTRUCTOR', 'instructor27@elearning.com', '1000000048', 'Josiah Perry', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 1 MONTH)),
('instructor28', '$2a$12$Fz/Cxu1LfkT3lXF34j7dBup1s7i4EpHiD3F1wEkStw3LT0.nZWDFG', 'INSTRUCTOR', 'instructor28@elearning.com', NULL, 'Maya Powell', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 1 MONTH)),
('instructor29', '$2a$12$Fz/Cxu1LfkT3lXF34j7dBup1s7i4EpHiD3F1wEkStw3LT0.nZWDFG', 'INSTRUCTOR', 'instructor29@elearning.com', '1000000049', 'Caleb Long', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 1 MONTH)),
('instructor30', '$2a$12$Fz/Cxu1LfkT3lXF34j7dBup1s7i4EpHiD3F1wEkStw3LT0.nZWDFG', 'INSTRUCTOR', 'instructor30@elearning.com', NULL, 'Natalie Patterson', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 1 MONTH)),
('instructor31', '$2a$12$Fz/Cxu1LfkT3lXF34j7dBup1s7i4EpHiD3F1wEkStw3LT0.nZWDFG', 'INSTRUCTOR', 'instructor31@elearning.com', '1000000050', 'Ryan Hughes', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 1 MONTH)),
('student57', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student57@elearning.com', NULL, 'Skylar Flores', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 1 MONTH)),
('student58', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student58@elearning.com', '1000000051', 'Jeremiah Washington', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 1 MONTH)),
('student59', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student59@elearning.com', NULL, 'Lucy Butler', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 1 MONTH)),
('student60', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student60@elearning.com', '1000000052', 'Easton Simmons', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 1 MONTH)),
('student61', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student61@elearning.com', NULL, 'Anna Foster', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 1 MONTH)),
('student62', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student62@elearning.com', '1000000053', 'Connor Gonzales', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 1 MONTH)),
('student63', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student63@elearning.com', NULL, 'Caroline Bryant', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 1 MONTH)),
('student64', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student64@elearning.com', '1000000054', 'Asher Alexander', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 1 MONTH)),
('student65', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student65@elearning.com', NULL, 'Stella Russell', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 1 MONTH)),
('student66', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student66@elearning.com', '1000000055', 'Cameron Griffin', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 1 MONTH)),
('student67', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student67@elearning.com', NULL, 'Kennedy Diaz', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 1 MONTH)),
('student68', '$2a$12$kbVTJlaxqzxk1ppPjhfxa.88oVSfcAzDZJLU96YSwvYpof.hHltIW', 'USER', 'student68@elearning.com', '1000000056', 'Adrian Hayes', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 1 MONTH));
