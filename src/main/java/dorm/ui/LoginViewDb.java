package dorm.ui;

import dorm.model.College;
import dorm.model.Gender;
import dorm.model.Role;
import dorm.model.Student;
import dorm.model.User;
import dorm.service.DatabaseDormService;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.Optional;

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
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(20));
        form.setHgap(10);
        form.setVgap(10);

        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        Button loginButton = new Button("Login");

        form.addRow(0, new Label("Username"), usernameField);
        form.addRow(1, new Label("Password"), passwordField);
        form.add(loginButton, 1, 2);

        loginButton.setOnAction(event -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            
            if (username.isEmpty() || password.isEmpty()) {
                showAlert("Username and password required");
                return;
            }
            
            Optional<Object> authResult = service.authenticate(username, password);
            if (authResult.isEmpty()) {
                showAlert("Invalid credentials");
                return;
            }
            
            switchToDashboard(authResult.get());
        });

        VBox wrapper = new VBox(form);
        wrapper.setAlignment(Pos.CENTER);
        tab.setContent(wrapper);
        return tab;
    }

    private Tab createRegisterTab() {
        Tab tab = new Tab("Register");
        tab.setClosable(false);

        GridPane form = new GridPane();
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(20));
        form.setHgap(10);
        form.setVgap(10);

        TextField fullNameField = new TextField();
        TextField studentIdField = new TextField();
        ComboBox<Gender> genderBox = new ComboBox<>(FXCollections.observableArrayList(Gender.values()));
        
        // College dropdown - shows full name
        ComboBox<College> collegeBox = new ComboBox<>(FXCollections.observableArrayList(College.values()));
        collegeBox.setConverter(new StringConverter<College>() {
            @Override
            public String toString(College college) {
                return college != null ? college.getFullName() : "";
            }
            @Override
            public College fromString(String string) {
                return null;
            }
        });
        
        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        Button registerButton = new Button("Create Account");

        form.addRow(0, new Label("Full Name"), fullNameField);
        form.addRow(1, new Label("Student ID"), studentIdField);
        form.addRow(2, new Label("Gender"), genderBox);
        form.addRow(3, new Label("College"), collegeBox);
        form.addRow(4, new Label("Username"), usernameField);
        form.addRow(5, new Label("Password (min 8 chars)"), passwordField);
        form.add(registerButton, 1, 6);

        registerButton.setOnAction(event -> {
            if (fullNameField.getText().isBlank() || studentIdField.getText().isBlank() || 
                genderBox.getValue() == null || collegeBox.getValue() == null ||
                usernameField.getText().isBlank() || passwordField.getText().isBlank()) {
                showAlert("All fields required");
                return;
            }
            
            // Password validation - minimum 8 characters
            if (passwordField.getText().length() < 8) {
                showAlert("Password must be at least 8 characters");
                return;
            }
            
            try {
                Student student = service.registerStudent(
                        usernameField.getText().trim(),
                        passwordField.getText().trim(),
                        fullNameField.getText().trim(),
                        studentIdField.getText().trim(),
                        genderBox.getValue(),
                        collegeBox.getValue()
                );
                switchToDashboard(student);
            } catch (Exception e) {
                showAlert("Registration failed: " + e.getMessage());
            }
        });

        VBox wrapper = new VBox(form);
        wrapper.setAlignment(Pos.CENTER);
        tab.setContent(wrapper);
        return tab;
    }

    private void switchToDashboard(Object authenticated) {
        Scene scene;
        
        if (authenticated instanceof Student) {
            Student student = (Student) authenticated;
            scene = new Scene(new StudentDashboardDb(service, student, stage).getRoot(), 1200, 700);
        } else if (authenticated instanceof User) {
            User user = (User) authenticated;
            if (user.getRole() == Role.ADMIN) {
                scene = new Scene(new AdminDashboardDb(service, user, stage).getRoot(), 1200, 700);
            } else if (user.getRole() == Role.OWNER) {
                scene = new Scene(new OwnerDashboardDb(service, user, stage).getRoot(), 1200, 700);
            } else {
                showAlert("Unknown role");
                return;
            }
        } else {
            showAlert("Unknown account type");
            return;
        }
        
        stage.setScene(scene);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
