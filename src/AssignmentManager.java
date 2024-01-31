import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AssignmentManager {

	// Displays all assignments for the given class, grouped by category.
	public void showAssignments(Connection connection, String activeClassId) {
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try {
			String sql = "SELECT c.CategoryName, a.AssignmentName, a.Description, a.PointValue " + "FROM Assignments a "
					+ "INNER JOIN Categories c ON a.CategoryID = c.CategoryID " + "WHERE a.ClassID = ? "
					+ "ORDER BY c.CategoryName, a.AssignmentName";
			preparedStatement = connection.prepareStatement(sql);

			preparedStatement.setString(1, activeClassId);

			resultSet = preparedStatement.executeQuery();

			String currentCategory = "";
			while (resultSet.next()) {
				String categoryName = resultSet.getString("CategoryName");
				if (!currentCategory.equals(categoryName)) {
					System.out.println("Category: " + categoryName);
					currentCategory = categoryName;
				}
				String assignmentName = resultSet.getString("AssignmentName");
				String description = resultSet.getString("Description");
				double pointValue = resultSet.getDouble("PointValue");

				System.out.println("\tAssignment: " + assignmentName + ", Description: " + description + ", Points: "
						+ pointValue);
			}
		} catch (SQLException sqlException) {
			System.out.println("Failed to list assignments");
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

	// Adds a new assignment to a specified category in the class.
	public void addAssignment(Connection connection, String classId, String className, String assignmentName,
			String category, String description, int points) throws SQLException {
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try {
			String fetchCategorySql = "SELECT CategoryID FROM Categories WHERE CategoryName = ? AND ClassID = ?";
			preparedStatement = connection.prepareStatement(fetchCategorySql);
			preparedStatement.setString(1, category);
			preparedStatement.setString(2, classId);

			resultSet = preparedStatement.executeQuery();
			if (!resultSet.next()) {
				System.out.println("Category '" + category + "' not found in class " + className + ".");
				return;
			}
			int categoryId = resultSet.getInt("CategoryID");

			String insertSql = "INSERT INTO Assignments (ClassID, CategoryID, AssignmentName, Description, PointValue) VALUES (?, ?, ?, ?, ?)";
			preparedStatement = connection.prepareStatement(insertSql);

			preparedStatement.setString(1, classId);
			preparedStatement.setInt(2, categoryId);
			preparedStatement.setString(3, assignmentName);
			preparedStatement.setString(4, description);
			preparedStatement.setInt(5, points);

			int rowsAffected = preparedStatement.executeUpdate();
			if (rowsAffected > 0) {
				System.out.println("Assignment '" + assignmentName + "' added successfully to category '" + category
						+ "' in class " + className + ".");
			} else {
				System.out.println("No new assignment was added.");
			}
		} catch (SQLException sqlException) {
			System.out.println("Failed to add assignment");
			sqlException.printStackTrace();
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (preparedStatement != null) {
				preparedStatement.close();
			}
		}
	}

}
