DROP DATABASE IF EXISTS CS_410_Final_Project;
CREATE DATABASE IF NOT EXISTS CS_410_Final_Project;
USE CS_410_Final_Project;

CREATE TABLE Classes (
    ClassID INT AUTO_INCREMENT PRIMARY KEY,
    CourseNumber VARCHAR(10) NOT NULL,
    Term VARCHAR(6) NOT NULL,
    SectionNumber INT NOT NULL,
    Description TEXT,
    InstructorName VARCHAR(100),
    UNIQUE (CourseNumber, Term, SectionNumber) -- course, term, section combination should be unique
);

CREATE TABLE Categories (
    CategoryID INT AUTO_INCREMENT PRIMARY KEY,
    ClassID INT NOT NULL,
    CategoryName VARCHAR(50) NOT NULL,
    Weight DECIMAL(5, 2) NOT NULL CHECK (Weight > 0), -- should be positive
    FOREIGN KEY (ClassID) REFERENCES Classes(ClassID)
);

CREATE TABLE Assignments (
    AssignmentID INT AUTO_INCREMENT PRIMARY KEY,
    ClassID INT NOT NULL,
    CategoryID INT NOT NULL,
    AssignmentName VARCHAR(100) NOT NULL,
    Description TEXT,
    PointValue DECIMAL(5, 2) NOT NULL CHECK (PointValue > 0), -- should be positive
    FOREIGN KEY (ClassID) REFERENCES Classes(ClassID),
    FOREIGN KEY (CategoryID) REFERENCES Categories(CategoryID),
    UNIQUE (ClassID, AssignmentName) -- No two assignments in the same class can have the same name
);

CREATE TABLE Students (
    StudentID INT PRIMARY KEY,
    Username VARCHAR(50) NOT NULL UNIQUE,
    LastName VARCHAR(100),
    FirstName VARCHAR(100)
);

CREATE TABLE Enrollments (
    EnrollmentID INT AUTO_INCREMENT PRIMARY KEY,
    StudentID INT NOT NULL,
    ClassID INT NOT NULL,
    FOREIGN KEY (StudentID) REFERENCES Students(StudentID),
    FOREIGN KEY (ClassID) REFERENCES Classes(ClassID),
    UNIQUE (StudentID, ClassID) -- A student can only be enrolled once in each class
);

CREATE TABLE Grades (
    GradeID INT AUTO_INCREMENT PRIMARY KEY,
    StudentID INT NOT NULL,
    AssignmentID INT NOT NULL,
    Grade DECIMAL(5, 2),
    FOREIGN KEY (StudentID) REFERENCES Students(StudentID),
    FOREIGN KEY (AssignmentID) REFERENCES Assignments(AssignmentID),
    UNIQUE (StudentID, AssignmentID) -- A student can only have one grade per assignment
);

CREATE INDEX idx_course ON Classes (CourseNumber, Term);
CREATE INDEX idx_category ON Categories (ClassID);
CREATE INDEX idx_assignment ON Assignments (ClassID, CategoryID);
CREATE INDEX idx_enrollment ON Enrollments (StudentID, ClassID);
CREATE INDEX idx_grade ON Grades (StudentID, AssignmentID);
