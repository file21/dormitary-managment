package edu.aau.dorm.ui;

import edu.aau.dorm.model.User;
import edu.aau.dorm.service.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Handles login UI.
 */
public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    private final AuthService authService = new AuthService();

    @FXML
    private void onLogin() {
        messageLabel.setText("");
        try {
            User user = authService.login(usernameField.getText(), passwordField.getText());
            SessionContext.setCurrentUser(user);

            Stage stage = (Stage) usernameField.getScene().getWindow();
            new SceneRouter(stage).goTo("dashboard.fxml", "Dorm Management - Dashboard");
        } catch (Exception ex) {
            messageLabel.setText(ex.getMessage());
        }
    }
}
