package edu.aau.dorm.ui;

import edu.aau.dorm.model.Role;
import edu.aau.dorm.model.Student;
import edu.aau.dorm.model.User;
import edu.aau.dorm.service.DormService;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Optional;

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
            Optional<User> user = service.authenticate(username, password);
            if (user.isEmpty()) {
                showAlert("Login Failed", "Invalid username or password.");
                return;
            }
            switchToDashboard(user.get());
        });

        VBox wrapper = new VBox(10, new Label("Dormitory Management System"), form);
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
        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        Button registerButton = new Button("Create Account");

        form.addRow(0, new Label("Full Name"), fullNameField);
        form.addRow(1, new Label("Student ID"), studentIdField);
        form.addRow(2, new Label("City"), cityField);
        form.addRow(3, new Label("Username"), usernameField);
        form.addRow(4, new Label("Password"), passwordField);
        form.add(registerButton, 1, 5);

        registerButton.setOnAction(event -> {
            if (fullNameField.getText().isBlank() || studentIdField.getText().isBlank() || cityField.getText().isBlank()
                    || usernameField.getText().isBlank() || passwordField.getText().isBlank()) {
                showAlert("Missing Data", "Please fill in all registration fields.");
                return;
            }
            Student student = service.registerStudent(
                    usernameField.getText().trim(),
                    passwordField.getText().trim(),
                    fullNameField.getText().trim(),
                    studentIdField.getText().trim(),
                    cityField.getText().trim()
            );
            showAlert("Account Created", "Student account created. You can now log in.");
            switchToDashboard(student);
        });

        VBox wrapper = new VBox(10, new Label("Create Student Account"), form);
        wrapper.setPadding(new Insets(20));
        tab.setContent(wrapper);
        return tab;
    }

    private void switchToDashboard(User user) {
        Scene scene;
        if (user.getRole() == Role.STUDENT) {
            scene = new Scene(new StudentDashboard(service, (Student) user).getRoot(), 1100, 700);
        } else if (user.getRole() == Role.ADMIN) {
            scene = new Scene(new AdminDashboard(service, user).getRoot(), 1100, 700);
        } else if (user.getRole() == Role.PROCTOR) {
            scene = new Scene(new ProctorDashboard(service, user).getRoot(), 1100, 700);
        } else {
            scene = new Scene(new OwnerDashboard(service, user).getRoot(), 1100, 700);
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
