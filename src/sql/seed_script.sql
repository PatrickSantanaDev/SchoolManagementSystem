/*
  Run this SQL file to delete existing data and populate the database with
  sample data.
*/
USE CS_410_Final_Project;

-- Disable foreign key checks to allow truncating tables
SET FOREIGN_KEY_CHECKS = 0;

-- Truncate tables to clear existing data
TRUNCATE TABLE Grades;
TRUNCATE TABLE Enrollments;
TRUNCATE TABLE Assignments;
TRUNCATE TABLE Categories;
TRUNCATE TABLE Students;
TRUNCATE TABLE Classes;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- Insert data into Classes
INSERT INTO Classes (CourseNumber, Term, SectionNumber, Description, InstructorName) VALUES
('CS410', 'Sp20', 1, 'Databases', 'Dr. Smith'),
('CS452', 'Sp20', 1, 'Advanced Databases', 'Dr. Johnson'),
('CS101', 'Sp20', 1, 'Intro to Computer Science', 'Dr. Allen'),
('CS303', 'Fa20', 2, 'Algorithms', 'Dr. Barnes');

-- Insert data into Categories for each class
INSERT INTO Categories (ClassID, CategoryName, Weight) VALUES
(1, 'Homework', 30),
(1, 'Project', 20),
(1, 'Exam', 50),
(2, 'Homework', 40),
(2, 'Exam', 60),
(3, 'Homework', 50),
(3, 'Project', 50),
(4, 'Homework', 30),
(4, 'Lab', 30),
(4, 'Exam', 40);

-- Insert data into Assignments
INSERT INTO Assignments (ClassID, CategoryID, AssignmentName, Description, PointValue) VALUES
(1, 1, 'Homework 1', 'Introduction to SQL', 100),
(1, 2, 'Project 1', 'Database Design', 200),
(1, 3, 'Midterm Exam', 'Midterm Examination', 150),
(2, 4, 'Homework 1', 'NoSQL Databases', 100),
(2, 5, 'Final Exam', 'Final Examination', 150),
(3, 6, 'Homework 1', 'Basic Programming', 100),
(3, 7, 'Final Project', 'Small Application Development', 200),
(4, 8, 'Homework 1', 'Sorting Algorithms', 100),
(4, 9, 'Lab 1', 'Search Algorithms Lab', 50),
(4, 10, 'Final Exam', 'Comprehensive Final', 200);

-- Insert data into Students
INSERT INTO Students (StudentID, Username, LastName, FirstName) VALUES
(111111, 'jdoe', 'Doe', 'John'),
(222222, 'msmith', 'Smith', 'Mary'),
(333333, 'tjones', 'Jones', 'Tom'),
(444444, 'lgarcia', 'Garcia', 'Luis'),
(555555, 'ewilliams', 'Williams', 'Emma');

-- Insert data into Enrollments
INSERT INTO Enrollments (StudentID, ClassID) VALUES
(111111, 1),
(222222, 1),
(333333, 2),
(444444, 3),
(555555, 4),
(111111, 2),
(222222, 3),
(333333, 4),
(444444, 1),
(555555, 2);

-- Insert data into Grades
INSERT INTO Grades (StudentID, AssignmentID, Grade) VALUES
(111111, 1, 95),
(222222, 1, 88),
(333333, 2, 92),
(444444, 3, 85),
(555555, 4, 90),
(111111, 2, 78),
(222222, 2, 80),
(333333, 3, 75),
(444444, 3, 88),
(555555, 4, 93),
(111111, 5, 82),
(222222, 5, 89),
(333333, 6, 87),
(444444, 7, 91),
(555555, 8, 94),
(111111, 9, 86),
(222222, 10, 85);

