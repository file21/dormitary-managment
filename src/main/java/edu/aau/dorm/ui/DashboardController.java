package edu.aau.dorm.ui;

import edu.aau.dorm.model.ApplicationStatus;
import edu.aau.dorm.model.DormApplication;
import edu.aau.dorm.model.SponsorshipType;
import edu.aau.dorm.model.User;
import edu.aau.dorm.service.ApplicationService;
import edu.aau.dorm.util.Validation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

import java.time.Instant;
import java.util.List;

/**
 * Main dashboard with application CRUD.
 */
public class DashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label roleLabel;

    @FXML
    private Label messageLabel;

    @FXML
    private TableView<DormApplication> applicationTable;

    @FXML
    private TableColumn<DormApplication, Long> idColumn;

    @FXML
    private TableColumn<DormApplication, Long> studentIdColumn;

    @FXML
    private TableColumn<DormApplication, String> windowColumn;

    @FXML
    private TableColumn<DormApplication, String> sponsorshipColumn;

    @FXML
    private TableColumn<DormApplication, String> statusColumn;

    @FXML
    private TableColumn<DormApplication, Integer> scoreColumn;

    @FXML
    private TextField studentUserIdField;

    @FXML
    private TextField windowCodeField;

    @FXML
    private ComboBox<SponsorshipType> sponsorshipCombo;

    @FXML
    private ComboBox<ApplicationStatus> statusCombo;

    @FXML
    private TextField departmentField;

    @FXML
    private TextField campusField;

    @FXML
    private TextField distanceField;

    @FXML
    private TextField scoreField;

    @FXML
    private CheckBox disabilityCheck;

    private final ApplicationService applicationService = new ApplicationService();
    private User currentUser;

    @FXML
    private void initialize() {
        currentUser = SessionContext.getCurrentUser();
        welcomeLabel.setText("Welcome, " + currentUser.username());
        roleLabel.setText("Role: " + currentUser.role().name());

        sponsorshipCombo.setItems(FXCollections.observableArrayList(SponsorshipType.values()));
        statusCombo.setItems(FXCollections.observableArrayList(ApplicationStatus.values()));

        idColumn.setCellValueFactory(cell -> new SimpleLongProperty(cell.getValue().id()).asObject());
        studentIdColumn.setCellValueFactory(cell -> new SimpleLongProperty(cell.getValue().studentUserId()).asObject());
        windowColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().windowCode()));
        sponsorshipColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().sponsorshipType().name()));
        statusColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().status().name()));
        scoreColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().score()).asObject());

        applicationTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
            if (selected != null) {
                populateForm(selected);
            }
        });

        if (currentUser.role() == User.Role.STUDENT) {
            studentUserIdField.setText(String.valueOf(currentUser.id()));
            studentUserIdField.setDisable(true);
            statusCombo.setItems(FXCollections.observableArrayList(
                    ApplicationStatus.DRAFT,
                    ApplicationStatus.SUBMITTED
            ));
        }

        refreshTable();
    }

    @FXML
    private void onCreate() {
        messageLabel.setText("");
        try {
            DormApplication created = applicationService.create(buildApplication(0));
            refreshTable();
            selectById(created.id());
            messageLabel.setText("Application created.");
        } catch (Exception ex) {
            messageLabel.setText(ex.getMessage());
        }
    }

    @FXML
    private void onUpdate() {
        messageLabel.setText("");
        DormApplication selected = applicationTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Select an application to update.");
            return;
        }
        try {
            ensureOwnership(selected);
            DormApplication updated = buildApplication(selected.id(), selected.submittedAt());
            applicationService.update(updated);
            refreshTable();
            selectById(selected.id());
            messageLabel.setText("Application updated.");
        } catch (Exception ex) {
            messageLabel.setText(ex.getMessage());
        }
    }

    @FXML
    private void onDelete() {
        messageLabel.setText("");
        DormApplication selected = applicationTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Select an application to delete.");
            return;
        }
        try {
            ensureOwnership(selected);
            applicationService.delete(selected.id());
            refreshTable();
            clearForm();
            messageLabel.setText("Application deleted.");
        } catch (Exception ex) {
            messageLabel.setText(ex.getMessage());
        }
    }

    @FXML
    private void onClear() {
        clearForm();
        messageLabel.setText("");
    }

    private void refreshTable() {
        List<DormApplication> applications = applicationService.listAll();
        if (currentUser.role() == User.Role.STUDENT) {
            applications = applications.stream()
                    .filter(app -> app.studentUserId() == currentUser.id())
                    .toList();
        }
        ObservableList<DormApplication> items = FXCollections.observableArrayList(applications);
        applicationTable.setItems(items);
    }

    private DormApplication buildApplication(long id) {
        return buildApplication(id, null);
    }

    private DormApplication buildApplication(long id, Instant existingSubmittedAt) {
        long studentUserId = parseLong(studentUserIdField.getText(), "Student user ID is required.");
        String windowCode = windowCodeField.getText() == null ? null : windowCodeField.getText().trim();
        SponsorshipType sponsorship = sponsorshipCombo.getValue();
        ApplicationStatus status = statusCombo.getValue();
        String department = emptyToNull(departmentField.getText());
        String campus = emptyToNull(campusField.getText());
        Double distance = parseOptionalDouble(distanceField.getText());
        int score = parseOptionalInt(scoreField.getText());

        Validation.require(status != null, "Status is required.");

        Instant submittedAt = existingSubmittedAt;
        if (submittedAt == null && status == ApplicationStatus.SUBMITTED) {
            submittedAt = Instant.now();
        }

        return new DormApplication(
                id,
                studentUserId,
                windowCode,
                sponsorship,
                disabilityCheck.isSelected(),
                department,
                campus,
                distance,
                status,
                score,
                submittedAt,
                Instant.now()
        );
    }

    private void populateForm(DormApplication application) {
        studentUserIdField.setText(String.valueOf(application.studentUserId()));
        windowCodeField.setText(application.windowCode());
        sponsorshipCombo.setValue(application.sponsorshipType());
        statusCombo.setValue(application.status());
        departmentField.setText(safeText(application.department()));
        campusField.setText(safeText(application.campusPreference()));
        distanceField.setText(application.distanceKm() == null ? "" : String.valueOf(application.distanceKm()));
        scoreField.setText(String.valueOf(application.score()));
        disabilityCheck.setSelected(application.disability());
    }

    private void clearForm() {
        if (currentUser.role() == User.Role.STUDENT) {
            studentUserIdField.setText(String.valueOf(currentUser.id()));
        } else {
            studentUserIdField.clear();
        }
        windowCodeField.clear();
        sponsorshipCombo.getSelectionModel().clearSelection();
        statusCombo.getSelectionModel().clearSelection();
        departmentField.clear();
        campusField.clear();
        distanceField.clear();
        scoreField.clear();
        disabilityCheck.setSelected(false);
    }

    private void ensureOwnership(DormApplication application) {
        if (currentUser.role() == User.Role.STUDENT && application.studentUserId() != currentUser.id()) {
            throw new IllegalStateException("Students can only manage their own applications.");
        }
    }

    private void selectById(long id) {
        applicationTable.getItems().stream()
                .filter(app -> app.id() == id)
                .findFirst()
                .ifPresent(app -> applicationTable.getSelectionModel().select(app));
    }

    private long parseLong(String value, String errorMessage) {
        Validation.require(value != null && !value.isBlank(), errorMessage);
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private int parseOptionalInt(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Score must be a whole number.");
        }
    }

    private Double parseOptionalDouble(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Distance must be a number.");
        }
    }

    private String emptyToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }
}
