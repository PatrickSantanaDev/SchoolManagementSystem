import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GradesManager {

	// Assigns a grade to a student for a specific assignment in the active class.
	public void gradeAssignment(Connection connection, String activeClassId, String assignmentName, String username,
			double grade) throws SQLException {
		String validateSql = "SELECT a.AssignmentID, a.PointValue FROM Assignments a WHERE a.AssignmentName = ? AND a.ClassID = ?";
		int assignmentId = -1;
		double maxPoints = 0;

		try (PreparedStatement validateStmt = connection.prepareStatement(validateSql)) {
			validateStmt.setString(1, assignmentName);
			validateStmt.setString(2, activeClassId);

			try (ResultSet resultSet = validateStmt.executeQuery()) {
				if (resultSet.next()) {
					assignmentId = resultSet.getInt("AssignmentID");
					maxPoints = resultSet.getDouble("PointValue");
				} else {
					System.out.println("Assignment not found in the active class.");
					return;
				}
			}
		}

		if (grade > maxPoints) {
			System.out.println("Warning: Grade exceeds the maximum points for the assignment. Max points: " + maxPoints
					+ ". Grade not assigned.");
			return;
		}

		String gradeSql = "INSERT INTO Grades (StudentID, AssignmentID, Grade) "
				+ "SELECT s.StudentID, ?, ? FROM Students s WHERE s.Username = ? "
				+ "ON DUPLICATE KEY UPDATE Grade = ?";

		try (PreparedStatement gradeStmt = connection.prepareStatement(gradeSql)) {
			gradeStmt.setInt(1, assignmentId);
			gradeStmt.setDouble(2, grade);
			gradeStmt.setString(3, username);
			gradeStmt.setDouble(4, grade);

			int rowsAffected = gradeStmt.executeUpdate();
			if (rowsAffected > 0) {
				System.out.println("Grade assigned successfully.");
			} else {
				System.out.println("Failed to assign grade.");
			}
		}
	}

	// Generates a report of grades for a student in the active class, displaying
	// grades by categories.
	public void studentGrades(Connection connection, String activeClassId, String username) throws SQLException {
		String sql = "WITH CategoryTotals AS (" + "    SELECT a.CategoryID, SUM(a.PointValue) AS TotalPoints "
				+ "    FROM Assignments a " + "    WHERE a.ClassID = ? " + "    GROUP BY a.CategoryID"
				+ "), StudentGrades AS ("
				+ "    SELECT a.CategoryID, COALESCE(SUM(g.Grade), 0) AS EarnedPoints, COUNT(g.Grade) > 0 AS HasGrade "
				+ "    FROM Assignments a "
				+ "    LEFT JOIN Grades g ON a.AssignmentID = g.AssignmentID AND g.StudentID = (SELECT StudentID FROM Students WHERE Username = ?) "
				+ "    WHERE a.ClassID = ? " + "    GROUP BY a.CategoryID" + ") "
				+ "SELECT c.CategoryName, sg.EarnedPoints, ct.TotalPoints, sg.HasGrade, "
				+ "       c.Weight / (SELECT SUM(Weight) FROM Categories WHERE ClassID = ?) * 100 AS AdjustedWeight "
				+ "FROM Categories c " + "JOIN CategoryTotals ct ON c.CategoryID = ct.CategoryID "
				+ "LEFT JOIN StudentGrades sg ON c.CategoryID = sg.CategoryID " + "WHERE c.ClassID = ?";

		try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setString(1, activeClassId);
			preparedStatement.setString(2, username);
			preparedStatement.setString(3, activeClassId);
			preparedStatement.setString(4, activeClassId);
			preparedStatement.setString(5, activeClassId);

			double totalFinalGrade = 0.0;
			double attemptedFinalGrade = 0.0;
			double totalAdjustedWeight = 0.0;

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					String categoryName = resultSet.getString("CategoryName");
					double earnedPoints = resultSet.getDouble("EarnedPoints");
					double totalPoints = resultSet.getDouble("TotalPoints");
					boolean hasGrade = resultSet.getBoolean("HasGrade");
					double adjustedWeight = resultSet.getDouble("AdjustedWeight");
					double categoryGradePercentage = hasGrade ? (earnedPoints / totalPoints) * 100 : 0.0;

					totalFinalGrade += categoryGradePercentage * adjustedWeight / 100;
					if (hasGrade) {
						attemptedFinalGrade += categoryGradePercentage * adjustedWeight / 100;
						totalAdjustedWeight += adjustedWeight;
					}

					System.out.println("Category: " + categoryName + ", Earned Points: " + earnedPoints
							+ ", Total Points: " + totalPoints + ", Adjusted Weight: " + adjustedWeight
							+ ", Category Grade: " + String.format("%.2f%%", categoryGradePercentage));
				}

				attemptedFinalGrade = totalAdjustedWeight > 0 ? (attemptedFinalGrade / totalAdjustedWeight) * 100 : 0;

				System.out.println("Total Final Grade: " + String.format("%.2f%%", totalFinalGrade));
				System.out.println("Attempted Final Grade: " + String.format("%.2f%%", attemptedFinalGrade));
			}
		}
	}

	// Creates a gradebook report for the active class, showing total and attempted
	// grades for each student.
	public void gradebook(Connection connection, String activeClassId) throws SQLException {
		String sql = "WITH TotalWeights AS ("
				+ "    SELECT ClassID, SUM(Weight) AS TotalWeight FROM Categories GROUP BY ClassID"
				+ "), StudentGrades AS (" + "    SELECT e.StudentID, c.CategoryID, "
				+ "           COALESCE(SUM(g.Grade), 0) AS EarnedPoints, "
				+ "           SUM(a.PointValue) AS TotalPoints, " + "           COUNT(g.Grade) > 0 AS HasGrade, "
				+ "           c.Weight " + "    FROM Enrollments e "
				+ "    JOIN Assignments a ON e.ClassID = a.ClassID "
				+ "    JOIN Categories c ON a.CategoryID = c.CategoryID "
				+ "    LEFT JOIN Grades g ON a.AssignmentID = g.AssignmentID AND e.StudentID = g.StudentID "
				+ "    WHERE e.ClassID = ? " + "    GROUP BY e.StudentID, c.CategoryID, c.Weight" + ") "
				+ "SELECT s.Username, s.StudentID, s.LastName, s.FirstName, "
				+ "       SUM(sg.EarnedPoints / sg.TotalPoints * sg.Weight) / SUM(sg.Weight) * 100 AS TotalGrade, "
				+ "       SUM(CASE WHEN sg.HasGrade THEN sg.EarnedPoints / sg.TotalPoints * sg.Weight ELSE 0 END) / SUM(CASE WHEN sg.HasGrade THEN sg.Weight ELSE 0 END) * 100 AS AttemptedGrade "
				+ "FROM StudentGrades sg " + "JOIN Students s ON sg.StudentID = s.StudentID "
				+ "GROUP BY s.Username, s.StudentID, s.LastName, s.FirstName " + "ORDER BY s.LastName, s.FirstName";

		try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setString(1, activeClassId);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (!resultSet.isBeforeFirst()) {
					System.out.println("No gradebook entries found for the class.");
					return;
				}

				System.out.println("Gradebook for ClassID: " + activeClassId);
				while (resultSet.next()) {
					String username = resultSet.getString("Username");
					String studentId = resultSet.getString("StudentID");
					String lastName = resultSet.getString("LastName");
					String firstName = resultSet.getString("FirstName");
					double totalGrade = resultSet.getDouble("TotalGrade");
					double attemptedGrade = resultSet.getDouble("AttemptedGrade");

					System.out.println("Username: " + username + ", StudentID: " + studentId + ", Name: " + firstName
							+ " " + lastName + ", Total Grade: " + String.format("%.2f%%", totalGrade)
							+ ", Attempted Grade: " + String.format("%.2f%%", attemptedGrade));
				}
			}
		}
	}

}
