package dorm.ui;

import dorm.model.*;
import dorm.service.DormService;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * In-memory student dashboard (demo only).
 */
public class StudentDashboard {
    private final DormService service;
    private final Student student;
    private final Stage stage;
    private final BorderPane root;

    public StudentDashboard(DormService service, Student student, Stage stage) {
        this.service = service;
        this.student = student;
        this.stage = stage;
        this.root = new BorderPane();
        build();
    }

    public Parent getRoot() {
        return root;
    }

    private void build() {
        Label headerLabel = new Label("Welcome, " + student.getDisplayName());
        headerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(event -> logout());
        
        HBox header = new HBox(20, headerLabel, logoutButton);
        header.setPadding(new Insets(10));
        root.setTop(header);

        TabPane tabs = new TabPane();
        tabs.getTabs().add(createProfileTab());
        tabs.getTabs().add(createApplicationTab());
        
        root.setCenter(tabs);
    }

    private Tab createProfileTab() {
        Tab tab = new Tab("Profile");
        tab.setClosable(false);

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        grid.addRow(0, new Label("Name:"), new Label(student.getDisplayName()));
        grid.addRow(1, new Label("Student ID:"), new Label(student.getStudentId()));
        grid.addRow(2, new Label("Gender:"), new Label(student.getGender() != null ? student.getGender().name() : "-"));
        grid.addRow(3, new Label("Building:"), new Label(student.getAssignedBuilding()));

        tab.setContent(grid);
        return tab;
    }

    private Tab createApplicationTab() {
        Tab tab = new Tab("Application");
        tab.setClosable(false);

        GridPane form = new GridPane();
        form.setPadding(new Insets(20));
        form.setHgap(10);
        form.setVgap(10);

        ComboBox<SponsorshipType> sponsorshipBox = new ComboBox<>(FXCollections.observableArrayList(SponsorshipType.values()));
        ComboBox<Residency> residencyBox = new ComboBox<>(FXCollections.observableArrayList(Residency.values()));
        TextField cityField = new TextField();
        TextField subcityField = new TextField();
        TextField woredaField = new TextField();
        Button submitButton = new Button("Submit");
        Label statusLabel = new Label();

        form.addRow(0, new Label("Sponsorship Type"), sponsorshipBox);
        form.addRow(1, new Label("Residency"), residencyBox);
        form.addRow(2, new Label("City"), cityField);
        form.addRow(3, new Label("Subcity"), subcityField);
        form.addRow(4, new Label("Woreda"), woredaField);
        form.add(submitButton, 1, 5);
        form.add(statusLabel, 1, 6);

        Optional<DormApplication> existingApp = service.getApplicationForStudent(student);
        if (existingApp.isPresent()) {
            statusLabel.setText("Status: " + existingApp.get().getStatus().name());
            submitButton.setDisable(true);
        }

        submitButton.setOnAction(event -> {
            if (sponsorshipBox.getValue() == null || residencyBox.getValue() == null) {
                showAlert("All fields required");
                return;
            }
            
            student.setSponsorshipType(sponsorshipBox.getValue());
            student.setResidency(residencyBox.getValue());
            student.setCity(cityField.getText().trim());
            student.setSubcity(subcityField.getText().trim());
            student.setWoreda(woredaField.getText().trim());
            
            service.submitApplication(student);
            statusLabel.setText("Status: PHASE_ONE_PENDING");
            submitButton.setDisable(true);
            showAlert("Application submitted");
        });

        tab.setContent(form);
        return tab;
    }

    private void logout() {
        // Note: In-memory service doesn't persist, would need to recreate
        showAlert("Please restart the application to log out");
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
