package dorm.ui;

import dorm.model.Building;
import dorm.model.Gender;
import dorm.model.Role;
import dorm.model.Student;
import dorm.model.User;
import dorm.service.DatabaseDormService;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Login view for the dormitory management system.
 * Handles authentication for: Admin/Owner (users), Students, and Buildings (proctors)
 */
public class LoginViewDb {
    private final DatabaseDormService service;
    private final Stage stage;
    private final TabPane root;

    public LoginViewDb(DatabaseDormService service, Stage stage) {
        this.service = service;
        this.stage = stage;
        this.root = new TabPane();
        this.root.getTabs().add(createLoginTab());
        this.root.getTabs().add(createRegisterTab());
    }

    public Parent getRoot() {
        return root;
    }

    private Tab createLoginTab() {
        Tab tab = new Tab("Login");
        tab.setClosable(false);

        GridPane form = new GridPane();
        form.setPadding(new Insets(20));
        form.setHgap(10);
        form.setVgap(10);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username or Building Name");
        PasswordField passwordField = new PasswordField();
        Button loginButton = new Button("Login");

        form.addRow(0, new Label("Username"), usernameField);
        form.addRow(1, new Label("Password"), passwordField);
        form.add(loginButton, 1, 2);

        loginButton.setOnAction(event -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            
            if (username.isEmpty() || password.isEmpty()) {
                showAlert("Validation Error", "Username and password are required.");
                return;
            }
            
            Optional<Object> authResult = service.authenticate(username, password);
            if (authResult.isEmpty()) {
                showAlert("Login Failed", "Invalid username or password.");
                return;
            }
            
            Object authenticated = authResult.get();
            switchToDashboard(authenticated);
        });

        VBox info = new VBox(5,
            new Label("Dormitory Management System"),
            new Label(""),
            new Label("Login Types:"),
            new Label("• Admin/Owner: Use your username"),
            new Label("• Student: Use your username"),
            new Label("• Building (Proctor): Use building name (e.g., B501)")
        );
        info.setStyle("-fx-font-size: 12px;");
        
        VBox wrapper = new VBox(10, info, form);
        wrapper.setPadding(new Insets(20));
        tab.setContent(wrapper);
        return tab;
    }

    private Tab createRegisterTab() {
        Tab tab = new Tab("Student Registration");
        tab.setClosable(false);

        GridPane form = new GridPane();
        form.setPadding(new Insets(20));
        form.setHgap(10);
        form.setVgap(10);

        TextField fullNameField = new TextField();
        TextField studentIdField = new TextField();
        TextField cityField = new TextField();
        ComboBox<Gender> genderBox = new ComboBox<>(FXCollections.observableArrayList(Gender.values()));
        genderBox.setPromptText("Select Gender");
        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        Button registerButton = new Button("Create Account");

        form.addRow(0, new Label("Full Name"), fullNameField);
        form.addRow(1, new Label("Student ID"), studentIdField);
        form.addRow(2, new Label("City"), cityField);
        form.addRow(3, new Label("Gender"), genderBox);
        form.addRow(4, new Label("Username"), usernameField);
        form.addRow(5, new Label("Password"), passwordField);
        form.add(registerButton, 1, 6);

        registerButton.setOnAction(event -> {
            if (fullNameField.getText().isBlank() || studentIdField.getText().isBlank() || 
                cityField.getText().isBlank() || genderBox.getValue() == null ||
                usernameField.getText().isBlank() || passwordField.getText().isBlank()) {
                showAlert("Missing Data", "Please fill in all registration fields.");
                return;
            }
            
            try {
                Student student = service.registerStudent(
                        usernameField.getText().trim(),
                        passwordField.getText().trim(),
                        fullNameField.getText().trim(),
                        studentIdField.getText().trim(),
                        cityField.getText().trim(),
                        genderBox.getValue()
                );
                showAlert("Account Created", "Student account created. You can now log in.");
                switchToDashboard(student);
            } catch (Exception e) {
                showAlert("Registration Failed", "Could not create account: " + e.getMessage());
            }
        });

        VBox wrapper = new VBox(10, new Label("Create Student Account"), form);
        wrapper.setPadding(new Insets(20));
        tab.setContent(wrapper);
        return tab;
    }

    private void switchToDashboard(Object authenticated) {
        Scene scene;
        
        if (authenticated instanceof Student) {
            Student student = (Student) authenticated;
            scene = new Scene(new StudentDashboardDb(service, student, stage).getRoot(), 1100, 700);
        } else if (authenticated instanceof Building) {
            Building building = (Building) authenticated;
            scene = new Scene(new ProctorDashboardDb(service, building, stage).getRoot(), 1100, 700);
        } else if (authenticated instanceof User) {
            User user = (User) authenticated;
            if (user.getRole() == Role.ADMIN) {
                scene = new Scene(new AdminDashboardDb(service, user, stage).getRoot(), 1100, 700);
            } else if (user.getRole() == Role.OWNER) {
                scene = new Scene(new OwnerDashboardDb(service, user, stage).getRoot(), 1100, 700);
            } else {
                showAlert("Error", "Unknown user role.");
                return;
            }
        } else {
            showAlert("Error", "Unknown account type.");
            return;
        }
        
        stage.setScene(scene);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
