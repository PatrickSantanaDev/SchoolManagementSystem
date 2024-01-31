import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ClassManager {
	private String activeClassId;

	// Constructor to initialize the class manager.
	public ClassManager() {
		this.activeClassId = null;
	}

	// Returns the ID of the currently active class.
	public String getActiveClassId() {
		return activeClassId;
	}

	// Creates a new class in the database with specified details.
	public void createNewClass(Connection connection, String courseNumber, String term, int section,
			String description) {
		PreparedStatement preparedStatement = null;

		try {
			connection = Database.getDatabaseConnection();
			String sql = "INSERT INTO Classes (CourseNumber, Term, SectionNumber, Description) VALUES (?, ?, ?, ?)";
			preparedStatement = connection.prepareStatement(sql);

			preparedStatement.setString(1, courseNumber);
			preparedStatement.setString(2, term);
			preparedStatement.setInt(3, section);
			preparedStatement.setString(4, description);

			int rowsAffected = preparedStatement.executeUpdate();
			if (rowsAffected > 0) {
				System.out.println("Class created successfully.");
			} else {
				System.out.println("No new class was created.");
			}
		} catch (SQLException sqlException) {
			System.out.println("Failed to create new class");
			System.out.println(sqlException.getMessage());
		} finally {
			try {
				if (preparedStatement != null)
					preparedStatement.close();
				if (connection != null)
					connection.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
	}

	// Lists all classes along with the number of students enrolled in each.
	public void listClasses(Connection connection) {
		Statement statement = null;
		ResultSet resultSet = null;

		try {
			connection = Database.getDatabaseConnection();
			String sql = "SELECT c.CourseNumber, c.Term, c.SectionNumber, c.Description, COUNT(e.StudentID) AS StudentCount "
					+ "FROM Classes c LEFT JOIN Enrollments e ON c.ClassID = e.ClassID "
					+ "GROUP BY c.CourseNumber, c.Term, c.SectionNumber, c.Description";

			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql);

			while (resultSet.next()) {
				String courseNumber = resultSet.getString("CourseNumber");
				String term = resultSet.getString("Term");
				int sectionNumber = resultSet.getInt("SectionNumber");
				String description = resultSet.getString("Description");
				int studentCount = resultSet.getInt("StudentCount");

				System.out.println("Course Number: " + courseNumber + ", Term: " + term + ", Section: " + sectionNumber
						+ ", Description: " + description + ", Students Enrolled: " + studentCount);
			}
		} catch (SQLException sqlException) {
			System.out.println("Failed to list classes");
			System.out.println(sqlException.getMessage());
		} finally {
			try {
				if (resultSet != null)
					resultSet.close();
			} catch (SQLException se) {
			}
			try {
				if (statement != null)
					statement.close();
			} catch (SQLException se) {
			}
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
	}

	// Selects a class as the active class based on provided details.
	public void selectClass(Connection connection, String courseNumber, String term, Integer section) {
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try {
			String sql;
			if (term == null && section == null) {
				// Case 1: Select based on courseNumber only
				sql = "SELECT ClassID, COUNT(*) OVER() as Count FROM Classes WHERE CourseNumber = ? ORDER BY Term DESC LIMIT 1";
			} else if (section == null) {
				// Case 2: Select based on courseNumber and term
				sql = "SELECT ClassID, COUNT(*) OVER() as Count FROM Classes WHERE CourseNumber = ? AND Term = ? GROUP BY ClassID";
			} else {
				// Case 3: Select based on courseNumber, term, and section
				sql = "SELECT ClassID FROM Classes WHERE CourseNumber = ? AND Term = ? AND SectionNumber = ?";
			}

			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, courseNumber);

			if (term != null) {
				preparedStatement.setString(2, term);
				if (section != null) {
					preparedStatement.setInt(3, section);
				}
			}

			resultSet = preparedStatement.executeQuery();

			if (resultSet.next()) {
				if (section == null && resultSet.getInt("Count") > 1) {
					System.out.println("Multiple sections found. Please specify the section.");
				} else {
					String classId = resultSet.getString("ClassID");
					this.activeClassId = classId;
					System.out.println("Class " + courseNumber + " is now active.");
				}
			} else {
				System.out.println("No such class found.");
			}
		} catch (SQLException sqlException) {
			System.out.println("Failed to select class");
			System.out.println(sqlException.getMessage());
		} finally {
		}
	}

	// Displays details of the currently active class.
	public void showClass(Connection connection) {
		if (activeClassId == null) {
			System.out.println("No active class selected.");
			return;
		}

		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try {
			connection = Database.getDatabaseConnection();
			String sql = "SELECT CourseNumber, Term, SectionNumber, Description FROM Classes WHERE ClassID = ?";
			preparedStatement = connection.prepareStatement(sql);

			preparedStatement.setString(1, activeClassId);

			resultSet = preparedStatement.executeQuery();

			if (resultSet.next()) {
				String courseNumber = resultSet.getString("CourseNumber");
				String term = resultSet.getString("Term");
				int sectionNumber = resultSet.getInt("SectionNumber");
				String description = resultSet.getString("Description");

				System.out.println("Active Class Details:");
				System.out.println("Course Number: " + courseNumber + ", Term: " + term + ", Section: " + sectionNumber
						+ ", Description: " + description);
			} else {
				System.out.println("Active class not found in the database.");
			}
		} catch (SQLException sqlException) {
			System.out.println("Failed to show class details");
			System.out.println(sqlException.getMessage());
		} finally {
			try {
				if (resultSet != null)
					resultSet.close();
			} catch (SQLException se) {
			}
			try {
				if (preparedStatement != null)
					preparedStatement.close();
			} catch (SQLException se) {
			}
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
	}

}
