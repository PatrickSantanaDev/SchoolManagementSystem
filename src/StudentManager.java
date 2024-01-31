import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StudentManager {

	// Adds a new student or updates an existing one and enrolls them in the
	// specified class.
	public void addStudent(Connection connection, String activeClassId, String username, String studentId,
			String lastName, String firstName) {
		if (activeClassId == null) {
			System.out.println("No active class selected.");
			return;
		}

		try {
			String checkStudentSql = "SELECT StudentID, LastName, FirstName FROM Students WHERE Username = ?";
			try (PreparedStatement checkStmt = connection.prepareStatement(checkStudentSql)) {
				checkStmt.setString(1, username);
				try (ResultSet resultSet = checkStmt.executeQuery()) {
					if (resultSet.next()) {
						String existingLastName = resultSet.getString("LastName");
						String existingFirstName = resultSet.getString("FirstName");

						if (!lastName.equals(existingLastName) || !firstName.equals(existingFirstName)) {
							updateStudentName(connection, username, lastName, firstName);
							System.out.println("Warning: Name for existing student " + username + " has been updated.");
						}

						enrollStudentInClass(connection, activeClassId, resultSet.getString("StudentID"));
					} else {
						insertNewStudent(connection, username, studentId, lastName, firstName);
						enrollStudentInClass(connection, activeClassId, studentId);
					}
				}
			}
		} catch (SQLException e) {
			System.out.println("Error processing student: " + e.getMessage());
		}
	}

	// Enroll an existing student in the current class
	public void addStudent(Connection connection, String activeClassId, String username) {
		if (activeClassId == null) {
			System.out.println("No active class selected.");
			return;
		}

		try {
			String checkStudentSql = "SELECT StudentID FROM Students WHERE Username = ?";
			try (PreparedStatement preparedStatement = connection.prepareStatement(checkStudentSql)) {
				preparedStatement.setString(1, username);
				try (ResultSet resultSet = preparedStatement.executeQuery()) {
					if (resultSet.next()) {
						enrollStudentInClass(connection, activeClassId, resultSet.getString("StudentID"));
					} else {
						System.out.println("Student with username " + username + " does not exist.");
					}
				}
			}
		} catch (SQLException e) {
			System.out.println("Error enrolling student: duplicate entry");
			System.out.println(e.getMessage());
		}
	}

	// Show all students in the current class, optionally filtered by a search
	// string
	public void showStudents(Connection connection, String activeClassId, String searchString) {
		if (activeClassId == null) {
			System.out.println("No active class selected.");
			return;
		}

		try {
			String sql = "SELECT s.StudentID, s.Username, s.LastName, s.FirstName "
					+ "FROM Students s JOIN Enrollments e ON s.StudentID = e.StudentID " + "WHERE e.ClassID = ?";
			if (searchString != null && !searchString.trim().isEmpty()) {
				sql += " AND (s.Username LIKE ? OR s.LastName LIKE ? OR s.FirstName LIKE ?)";
			}

			try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
				preparedStatement.setString(1, activeClassId);

				if (searchString != null && !searchString.trim().isEmpty()) {
					searchString = "%" + searchString + "%";
					preparedStatement.setString(2, searchString);
					preparedStatement.setString(3, searchString);
					preparedStatement.setString(4, searchString);
				}

				try (ResultSet resultSet = preparedStatement.executeQuery()) {
					if (!resultSet.isBeforeFirst()) {
						System.out.println("No students found.");
						return;
					}

					while (resultSet.next()) {
						String studentId = resultSet.getString("StudentID");
						String username = resultSet.getString("Username");
						String lastName = resultSet.getString("LastName");
						String firstName = resultSet.getString("FirstName");

						System.out.println("Student ID: " + studentId + ", Username: " + username + ", Name: "
								+ firstName + " " + lastName);
					}
				}
			}
		} catch (SQLException e) {
			System.out.println("Error listing students: " + e.getMessage());
		}
	}

	// Updates the name of an existing student.
	private void updateStudentName(Connection connection, String username, String lastName, String firstName)
			throws SQLException {
		String updateSql = "UPDATE Students SET LastName = ?, FirstName = ? WHERE Username = ?";
		try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
			updateStmt.setString(1, lastName);
			updateStmt.setString(2, firstName);
			updateStmt.setString(3, username);
			updateStmt.executeUpdate();
		}
	}

	// Inserts a new student into the database.
	private void insertNewStudent(Connection connection, String username, String studentId, String lastName,
			String firstName) throws SQLException {
		String insertSql = "INSERT INTO Students (Username, StudentID, LastName, FirstName) VALUES (?, ?, ?, ?)";
		try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
			insertStmt.setString(1, username);
			insertStmt.setString(2, studentId);
			insertStmt.setString(3, lastName);
			insertStmt.setString(4, firstName);
			insertStmt.executeUpdate();
			System.out.println("Added new student " + username + ".");
		}
	}

	// Enrolls a student in a class if they are not already enrolled.
	private void enrollStudentInClass(Connection connection, String classId, String studentId) throws SQLException {
		String checkEnrollmentSql = "SELECT * FROM Enrollments WHERE ClassID = ? AND StudentID = ?";
		try (PreparedStatement checkStmt = connection.prepareStatement(checkEnrollmentSql)) {
			checkStmt.setString(1, classId);
			checkStmt.setString(2, studentId);
			try (ResultSet resultSet = checkStmt.executeQuery()) {
				if (!resultSet.next()) {
					String enrollSql = "INSERT INTO Enrollments (ClassID, StudentID) VALUES (?, ?)";
					try (PreparedStatement enrollStmt = connection.prepareStatement(enrollSql)) {
						enrollStmt.setString(1, classId);
						enrollStmt.setString(2, studentId);
						enrollStmt.executeUpdate();
						System.out.println("Enrolled student ID " + studentId + " in class ID " + classId + ".");
					}
				} else {
					System.out.println("Student ID " + studentId + " is already enrolled in class ID " + classId + ".");
				}
			}
		}
	}

}
