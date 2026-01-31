package dorm.ui;

import dorm.model.Gender;
import dorm.model.Role;
import dorm.model.Student;
import dorm.model.User;
import dorm.service.DormService;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * In-memory login view (demo only).
 */
public class LoginView {
    private final DormService service;
    private final Stage stage;
    private final TabPane root;

    public LoginView(DormService service, Stage stage) {
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
        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        Button registerButton = new Button("Create Account");

        form.addRow(0, new Label("Full Name"), fullNameField);
        form.addRow(1, new Label("Student ID"), studentIdField);
        form.addRow(2, new Label("Gender"), genderBox);
        form.addRow(3, new Label("Username"), usernameField);
        form.addRow(4, new Label("Password"), passwordField);
        form.add(registerButton, 1, 5);

        registerButton.setOnAction(event -> {
            if (fullNameField.getText().isBlank() || studentIdField.getText().isBlank() || 
                genderBox.getValue() == null || usernameField.getText().isBlank() || 
                passwordField.getText().isBlank()) {
                showAlert("All fields required");
                return;
            }
            
            try {
                Student student = service.registerStudent(
                        usernameField.getText().trim(),
                        passwordField.getText().trim(),
                        fullNameField.getText().trim(),
                        studentIdField.getText().trim(),
                        genderBox.getValue()
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
            scene = new Scene(new StudentDashboard(service, student, stage).getRoot(), 1000, 650);
        } else if (authenticated instanceof User) {
            User user = (User) authenticated;
            if (user.getRole() == Role.ADMIN) {
                scene = new Scene(new AdminDashboard(service, user, stage).getRoot(), 1100, 700);
            } else if (user.getRole() == Role.OWNER) {
                scene = new Scene(new OwnerDashboard(service, user, stage).getRoot(), 1100, 700);
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
