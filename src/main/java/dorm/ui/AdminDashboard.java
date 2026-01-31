package dorm.ui;

import dorm.model.User;
import dorm.service.DormService;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * In-memory admin dashboard (demo only).
 */
public class AdminDashboard {
    private final DormService service;
    private final User admin;
    private final Stage stage;
    private final BorderPane root;

    public AdminDashboard(DormService service, User admin, Stage stage) {
        this.service = service;
        this.admin = admin;
        this.stage = stage;
        this.root = new BorderPane();
        build();
    }

    public Parent getRoot() {
        return root;
    }

    private void build() {
        Label headerLabel = new Label("Admin: " + admin.getDisplayName());
        headerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(event -> showAlert("Please restart to log out"));
        
        HBox header = new HBox(20, headerLabel, logoutButton);
        header.setPadding(new Insets(10));
        root.setTop(header);

        VBox content = new VBox(10, new Label("Admin Dashboard - Demo Mode"));
        content.setPadding(new Insets(20));
        root.setCenter(content);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
