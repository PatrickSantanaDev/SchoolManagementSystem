import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CategoryManager {

	// Shows all categories and their respective weights for a specified class.
	public void showCategories(Connection connection, String activeClassId) {
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try {
			String sql = "SELECT CategoryName, Weight FROM Categories WHERE ClassID = ? ORDER BY CategoryName";
			preparedStatement = connection.prepareStatement(sql);

			preparedStatement.setString(1, activeClassId);

			resultSet = preparedStatement.executeQuery();

			if (!resultSet.isBeforeFirst()) {
				System.out.println("No categories found for class ID: " + activeClassId);
				return;
			}

			System.out.println("Categories and Weights for class ID: " + activeClassId);
			while (resultSet.next()) {
				String categoryName = resultSet.getString("CategoryName");
				double weight = resultSet.getDouble("Weight");

				System.out.println("Category: " + categoryName + ", Weight: " + weight);
			}
		} catch (SQLException sqlException) {
			System.out.println("Failed to list categories");
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
		}
	}

	// Adds a new category to a specified class with a given name and weight.
	public void addCategory(Connection connection, String name, double weight, String classId) {
		PreparedStatement preparedStatement = null;

		try {
			String sql = "INSERT INTO Categories (ClassID, CategoryName, Weight) VALUES (?, ?, ?)";
			preparedStatement = connection.prepareStatement(sql);

			preparedStatement.setString(1, classId);
			preparedStatement.setString(2, name);
			preparedStatement.setDouble(3, weight);

			int rowsAffected = preparedStatement.executeUpdate();
			if (rowsAffected > 0) {
				System.out.println("Category '" + name + "' with weight " + weight + " added successfully.");
			} else {
				System.out.println("No new category was added.");
			}
		} catch (SQLException sqlException) {
			System.out.println("Failed to add category");
			System.out.println(sqlException.getMessage());
		} finally {
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
