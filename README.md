# BSU CS410 School Management System - Final Project

## Description of Implementation:
The School Management System is an application designed to help the user with various administrative tasks for a school. 
The system manages classes, students, assignments, grades, and categories, all organized and accessible via a user-friendly command-line interface. 
The system has a modular design since it is divided into different manager classes - ClassManager, CategoryManager, AssignmentManager, StudentManager, and GradesManager. 
Each of these specialized classes encapsulates specific functionalities, allowing for a clear separation of concerns and easier maintenance. 
For instance, the ClassManager handles operations related to class creation and selection, while the AssignmentManager deals with assignment-related tasks. 
Similarly, the StudentManager manages student information and enrollments, and the GradesManager focuses on grading assignments and generating grade reports.

Underlying the system is a well-structured database schema, which is necessary for storing and retrieving the data efficiently. 
The database comprises tables for classes, categories, assignments, students, enrollments, and grades, each with appropriate relationships and constraints to ensure data integrity. 
For example, the Classes table stores information about each class, while the Categories and Assignments tables hold data about grading categories and individual assignments, respectively. 
The Students and Enrollments tables manage student records and their class enrollments, and the Grades table records students' grades for various assignments. 
The application uses SQL queries to interact with the database, performing operations like adding new students, creating classes, and computing grades. 

## Class Management Commands - ClassManager.java:
- `new-class <courseNumber> <term> <section> <description>` - Create a new class.
- `list-classes` - List all classes with the number of students in each.
- `select-class <courseNumber> [<term> [<section>]]` - Activate a class. Selects the only section of the course in the most recent term if there's only one such section.
- `show-class` - Show details of the currently active class.

## Category Management Commands:
- `show-categories` - List the categories with their weights.
- `add-category <Name> <weight>` - Add a new category.

## Assignment Management Commands:
- `show-assignment` - List the assignments with their point values, grouped by category.
- `add-assignment <assignmentName> <Category> <Description> <points>` - Add a new assignment.

## Student Management Commands:
- `add-student <username> <studentId> <Last> <First>` - Adds a student and enrolls them in the current class. Updates the name if it does not match the stored name.
- `add-student <username>` - Enrolls an already-existing student in the current class.
- `show-students` - Show all students in the current class.
- `show-students <string>` - Show all students with 'string' in their name or username.

## Grade Management Commands:
- `grade <assignmentName> <username> <grade>` - Assign the grade 'grade' for the student 'username' for 'assignmentName'. Replace if a grade already exists.
- `student-grades <username>` - Show a student's current grade: all assignments grouped by category, with subtotals and the overall grade.
- `gradebook` - Show the current class's gradebook: students (username, student ID, and name), along with their total grades in the class.

## General Commands:
- `help` - Lists help information.
- `quit` - Exits the program.
