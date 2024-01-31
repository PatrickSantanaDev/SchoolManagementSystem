import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//Driver class
public class SchoolManagementSystem {

	private String activeClassId; // keep track of currently active class
	private ClassManager classManager;
	private CategoryManager categoryManager;
	private AssignmentManager assignmentManager;
	private StudentManager studentManager;
	private GradesManager gradesManager;

	// Constructor
	public SchoolManagementSystem() {
		this.activeClassId = null;
		this.classManager = new ClassManager();
		this.categoryManager = new CategoryManager();
		this.assignmentManager = new AssignmentManager();
		this.studentManager = new StudentManager();
		this.gradesManager = new GradesManager();
	}

	// Retrieves the class name based on the given class ID.
	private String getClassName(String classId) {
		if (classId == null) {
			return "No active class";
		}

		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		String className = "";

		try {
			connection = Database.getDatabaseConnection();
			String sql = "SELECT Description FROM Classes WHERE ClassID = ?";
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, classId);

			resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				className = resultSet.getString("Description");
			}
		} catch (SQLException e) {
			System.out.println("Error fetching class name: " + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				if (resultSet != null)
					resultSet.close();
				if (preparedStatement != null)
					preparedStatement.close();
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return className;
	}

	// Parses a command line input into separate arguments.
	public static List<String> parseArguments(String command) {
		List<String> commandArguments = new ArrayList<String>();
		Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(command);
		while (m.find())
			commandArguments.add(m.group(1).replace("\"", ""));
		return commandArguments;
	}

	// Runs the main loop of the program to process commands.
	public void run() {
		Scanner scan = new Scanner(System.in);
		String command;

		System.out.println("Welcome to the School Management System");
		System.out.println("--------------------------------------------------------------------------------");

		while (true) {
			System.out.print("Command: ");
			command = scan.nextLine();
			List<String> commandArguments = parseArguments(command);

			if (commandArguments.isEmpty()) {
				continue;
			}

			command = commandArguments.get(0).toLowerCase();
			commandArguments.remove(0);

			try {
				switch (command) {
				case "new-class":
					if (commandArguments.size() >= 4) {
						try {
							String courseNumber = commandArguments.get(0);
							String term = commandArguments.get(1);
							int section = Integer.parseInt(commandArguments.get(2));
							String description = commandArguments.get(3);

							classManager.createNewClass(Database.getDatabaseConnection(), courseNumber, term, section,
									description);
						} catch (NumberFormatException e) {
							System.out.println("Invalid section number. Please enter a valid integer.");
						}
					} else {
						System.out.println("Incorrect number of arguments for new-class command.");
						System.out.println("Usage: new-class <courseNumber> <term> <section> <description>");
					}
					break;
				case "list-classes":
					classManager.listClasses(Database.getDatabaseConnection());
					break;
				case "select-class":
					if (commandArguments.size() >= 1) {
						try {
							String courseNumber = commandArguments.get(0);
							String term = commandArguments.size() > 1 ? commandArguments.get(1) : null;
							Integer section = null;

							if (commandArguments.size() > 2) {
								section = Integer.parseInt(commandArguments.get(2));
							}

							classManager.selectClass(Database.getDatabaseConnection(), courseNumber, term, section);
							// Update activeClassId in SchoolManagementSystem
							this.activeClassId = classManager.getActiveClassId();
						} catch (NumberFormatException e) {
							System.out.println("Invalid section number. Please enter a valid integer.");
						}
					} else {
						System.out.println("Insufficient arguments for select-class command.");
						System.out.println("Usage: select-class <courseNumber> [<term> [<section>]]");
					}
					break;
				case "show-class":
					classManager.showClass(Database.getDatabaseConnection());
					break;
				case "show-categories":
					categoryManager.showCategories(Database.getDatabaseConnection(), this.activeClassId);
					break;
				case "add-category":
					if (commandArguments.size() == 2) {
						try {
							String name = commandArguments.get(0);
							double weight = Double.parseDouble(commandArguments.get(1));

							if (this.activeClassId != null) {
								// Use the class member directly
								this.categoryManager.addCategory(Database.getDatabaseConnection(), name, weight,
										this.activeClassId);
							} else {
								System.out.println("No active class selected. Please select a class first.");
							}
						} catch (NumberFormatException e) {
							System.out.println("Invalid weight format. Please enter a valid number for the weight.");
						}
					} else {
						System.out.println("Incorrect number of arguments for add-category command.");
						System.out.println("Usage: add-category <name> <weight>");
					}
					break;
				case "show-assignment":
					assignmentManager.showAssignments(Database.getDatabaseConnection(), this.activeClassId);
					break;
				case "add-assignment":
					if (commandArguments.size() >= 4) {
						try {
							String assignmentName = commandArguments.get(0);
							String category = commandArguments.get(1);
							String description = commandArguments.get(2);
							int points = Integer.parseInt(commandArguments.get(3));
							String className = getClassName(this.activeClassId); // Fetch the class name based on
																					// activeClassId

							// Call addAssignment with the correct parameter order
							assignmentManager.addAssignment(Database.getDatabaseConnection(), this.activeClassId,
									className, assignmentName, category, description, points);
						} catch (NumberFormatException e) {
							System.out
									.println("Invalid format for points. Please enter a valid integer for the points.");
						}
					} else {
						System.out.println("Incorrect number of arguments for add-assignment command.");
						System.out.println("Usage: add-assignment <assignmentName> <category> <description> <points>");
					}
					break;
				case "add-student":
					if (commandArguments.size() == 4) {
						String username = commandArguments.get(0);
						String studentId = commandArguments.get(1);
						String lastName = commandArguments.get(2);
						String firstName = commandArguments.get(3);

						studentManager.addStudent(Database.getDatabaseConnection(), this.activeClassId, username,
								studentId, lastName, firstName);
					} else if (commandArguments.size() == 1) {
						String username = commandArguments.get(0);

						studentManager.addStudent(Database.getDatabaseConnection(), this.activeClassId, username);
					} else {
						System.out.println("Incorrect number of arguments for add-student command.");
						System.out.println("Usage: add-student <username> <studentId> <lastName> <firstName>");
						System.out.println("Or: add-student <username> to enroll an existing student");
					}
					break;
				case "show-students":
					String searchString = commandArguments.isEmpty() ? null : String.join(" ", commandArguments);
					studentManager.showStudents(Database.getDatabaseConnection(), this.activeClassId, searchString);
					break;
				case "grade":
					if (commandArguments.size() == 3) {
						String assignmentName = commandArguments.get(0);
						String username = commandArguments.get(1);
						String gradeStr = commandArguments.get(2);

						try {
							double grade = Double.parseDouble(gradeStr);
							gradesManager.gradeAssignment(Database.getDatabaseConnection(), this.activeClassId,
									assignmentName, username, grade);
						} catch (NumberFormatException e) {
							System.out.println("Invalid format for grade. Please enter a valid number for the grade.");
						}
					} else {
						System.out.println("Incorrect number of arguments for grade command.");
						System.out.println("Usage: grade <assignmentName> <username> <grade>");
					}
					break;
				case "student-grades":
					if (commandArguments.size() == 1) {
						String username = commandArguments.get(0);
						gradesManager.studentGrades(Database.getDatabaseConnection(), this.activeClassId, username);
					} else {
						System.out.println("Incorrect number of arguments for student-grades command.");
						System.out.println("Usage: student-grades <username>");
					}
					break;

				case "gradebook":
					gradesManager.gradebook(Database.getDatabaseConnection(), this.activeClassId);
					break;
				case "test":
					if (!commandArguments.isEmpty() && "connection".equals(commandArguments.get(0))) {
						Database.testConnection();
					}
					break;
				case "exit":
				case "quit":
					System.out.println("Exiting the School Management System.");
					scan.close();
					return;
				case "help":
					displayHelp();
					break;
				default:
					System.out.println("Unknown command. Enter 'help' for list of commands");
					break;
				}
			} catch (Exception e) {
				System.out.println("An error occurred: " + e.getMessage());
			}

			System.out.println("--------------------------------------------------------------------------------");
		}
	}

	// The main entry point for the application.
	public static void main(String[] args) {
		SchoolManagementSystem sms = new SchoolManagementSystem();
		sms.run(); // Run the instance method
	}

	// Displays help information for all available commands.
	private static void displayHelp() {
		System.out.println("--------------------------------------" + " Help " + "--------------------------------------");

		// Class Management
		System.out.println("Class Management Commands:");
		System.out.println("new-class <courseNumber> <term> <section> <description> - Create a new class.");
		System.out.println("list-classes - List all classes with the number of students in each.");
		System.out.println(
				"select-class <courseNumber> [<term> [<section>]] - Activate a class. Selects the only section of the course in the most recent term if there's only one such section.");
		System.out.println("show-class - Show details of the currently active class.");

		// Category and Assignment Management
		System.out.println("\nCategory and Assignment Management Commands:");
		System.out.println("show-categories - List the categories with their weights.");
		System.out.println("add-category <Name> <weight> - Add a new category.");
		System.out.println("show-assignment - List the assignments with their point values, grouped by category.");
		System.out.println("add-assignment <assignmentName> <Category> <Description> <points> - Add a new assignment.");

		// Student Management
		System.out.println("\nStudent Management Commands:");
		System.out.println(
				"add-student <username> <studentId> <Last> <First> - Adds a student and enrolls them in the current class. Updates the name if it does not match the stored name.");
		System.out.println("add-student <username> - Enrolls an already-existing student in the current class.");
		System.out.println("show-students - Show all students in the current class.");
		System.out.println("show-students <string> - Show all students with 'string' in their name or username.");

		// Grade Assignment
		System.out.println("\nGrade Assignment Commands:");
		System.out.println(
				"grade <assignmentName> <username> <grade> - Assign the grade 'grade' for the student 'username' for 'assignmentName'. Replace if a grade already exists.");

		// Grade Reporting
		System.out.println("\nGrade Reporting Commands:");
		System.out.println(
				"student-grades <username> - Show a student's current grade: all assignments grouped by category, with subtotals and the overall grade.");
		System.out.println(
				"gradebook - Show the current class's gradebook: students (username, student ID, and name), along with their total grades in the class.");

		// General Commands
		System.out.println("\nGeneral Commands:");
		System.out.println("help - Lists help information.");
		System.out.println("quit - Exits the program.");

		System.out.println("--------------------------------------------------------------------------------");
	}

}
