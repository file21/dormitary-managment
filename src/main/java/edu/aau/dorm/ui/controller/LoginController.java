package edu.aau.dorm.ui.controller;

import edu.aau.dorm.service.AuthService;
import edu.aau.dorm.util.Validation;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Minimal login controller (wire to DB users).
 */
public final class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    private final AuthService authService = new AuthService();

    @FXML
    private void onLogin() {
        try {
            Validation.requireNotBlank(usernameField.getText(), "Username is required.");
            Validation.requireNotBlank(passwordField.getText(), "Password is required.");
            authService.login(usernameField.getText(), passwordField.getText());
            statusLabel.setText("Login OK (wire routing by role).");
        } catch (Exception e) {
            statusLabel.setText(e.getMessage());
        }
    }
}
