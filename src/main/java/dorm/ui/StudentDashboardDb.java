package dorm.ui;

import dorm.model.*;
import dorm.service.DatabaseDormService;
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
import java.util.stream.Collectors;

public class StudentDashboardDb {
    private final DatabaseDormService service;
    private final Student student;
    private final Stage stage;
    private final BorderPane root;
    private final ListView<String> announcementList;
    private final ListView<String> messageList;
    private Tab phaseTwoTab;

    public StudentDashboardDb(DatabaseDormService service, Student student, Stage stage) {
        this.service = service;
        this.student = student;
        this.stage = stage;
        this.root = new BorderPane();
        this.announcementList = new ListView<>();
        this.messageList = new ListView<>();
        build();
        refresh();
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
        tabs.getTabs().add(createPhaseOneTab());
        phaseTwoTab = createPhaseTwoTab();
        tabs.getTabs().add(phaseTwoTab);
        tabs.getTabs().add(createAnnouncementsTab());
        tabs.getTabs().add(createMessagesTab());
        
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
        grid.addRow(3, new Label("Sponsorship:"), new Label(student.getSponsorshipType() != null ? student.getSponsorshipType().name() : "-"));
        grid.addRow(4, new Label("Building:"), new Label(student.getAssignedBuilding()));
        
        // Application status
        String status = service.getApplicationForStudent(student)
                .map(app -> app.getStatus().name())
                .orElse("Not Applied");
        grid.addRow(5, new Label("Status:"), new Label(status));

        tab.setContent(grid);
        return tab;
    }

    private Tab createPhaseOneTab() {
        Tab tab = new Tab("Application - Phase 1");
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
        TextField disabilityField = new TextField();
        Button submitButton = new Button("Submit Phase 1");
        Label statusLabel = new Label();

        // Pre-fill if already submitted
        if (student.getSponsorshipType() != null) sponsorshipBox.setValue(student.getSponsorshipType());
        if (student.getResidency() != null) residencyBox.setValue(student.getResidency());
        if (student.getCity() != null) cityField.setText(student.getCity());
        if (student.getSubcity() != null) subcityField.setText(student.getSubcity());
        if (student.getWoreda() != null) woredaField.setText(student.getWoreda());
        if (student.getDisabilityInfo() != null) disabilityField.setText(student.getDisabilityInfo());

        form.addRow(0, new Label("Sponsorship Type"), sponsorshipBox);
        form.addRow(1, new Label("Residency"), residencyBox);
        form.addRow(2, new Label("City"), cityField);
        form.addRow(3, new Label("Subcity"), subcityField);
        form.addRow(4, new Label("Woreda"), woredaField);
        form.addRow(5, new Label("Disability (if any)"), disabilityField);
        form.add(submitButton, 1, 6);
        form.add(statusLabel, 1, 7);

        // Check current status
        Optional<DormApplication> existingApp = service.getApplicationForStudent(student);
        if (existingApp.isPresent()) {
            ApplicationStatus appStatus = existingApp.get().getStatus();
            statusLabel.setText("Status: " + appStatus.name());
            
            if (appStatus != ApplicationStatus.PHASE_ONE_PENDING && 
                appStatus != ApplicationStatus.PHASE_ONE_RESUBMIT) {
                submitButton.setDisable(true);
                sponsorshipBox.setDisable(true);
                residencyBox.setDisable(true);
                cityField.setDisable(true);
                subcityField.setDisable(true);
                woredaField.setDisable(true);
                disabilityField.setDisable(true);
            }
            
            String note = existingApp.get().getAdminNote();
            if (note != null && !note.isBlank()) {
                statusLabel.setText("Status: " + appStatus.name() + " | Note: " + note);
            }
        }

        submitButton.setOnAction(event -> {
            if (sponsorshipBox.getValue() == null || residencyBox.getValue() == null ||
                cityField.getText().isBlank() || subcityField.getText().isBlank() || 
                woredaField.getText().isBlank()) {
                showAlert("All fields except disability are required");
                return;
            }
            
            try {
                service.submitPhaseOneApplication(
                    student,
                    sponsorshipBox.getValue(),
                    residencyBox.getValue(),
                    cityField.getText().trim(),
                    subcityField.getText().trim(),
                    woredaField.getText().trim(),
                    disabilityField.getText().trim()
                );
                statusLabel.setText("Status: PHASE_ONE_PENDING");
                showAlert("Phase 1 submitted successfully");
                refresh();
            } catch (Exception e) {
                showAlert("Failed: " + e.getMessage());
            }
        });

        tab.setContent(form);
        return tab;
    }

    private Tab createPhaseTwoTab() {
        Tab tab = new Tab("Application - Phase 2");
        tab.setClosable(false);

        GridPane form = new GridPane();
        form.setPadding(new Insets(20));
        form.setHgap(10);
        form.setVgap(10);

        TextField motherNameField = new TextField();
        TextField motherPhoneField = new TextField();
        ComboBox<Residency> motherResidencyBox = new ComboBox<>(FXCollections.observableArrayList(Residency.values()));
        TextField emergencyContactField = new TextField();
        TextField transactionIdField = new TextField();
        Button submitButton = new Button("Submit Phase 2");
        Label statusLabel = new Label();

        // Pre-fill if already submitted
        if (student.getMotherName() != null) motherNameField.setText(student.getMotherName());
        if (student.getMotherPhone() != null) motherPhoneField.setText(student.getMotherPhone());
        if (student.getMotherResidency() != null) motherResidencyBox.setValue(student.getMotherResidency());
        if (student.getEmergencyContact() != null) emergencyContactField.setText(student.getEmergencyContact());
        if (student.getTransactionId() != null) transactionIdField.setText(student.getTransactionId());

        form.addRow(0, new Label("Mother's Name"), motherNameField);
        form.addRow(1, new Label("Mother's Phone"), motherPhoneField);
        form.addRow(2, new Label("Mother's Residency"), motherResidencyBox);
        form.addRow(3, new Label("Emergency Contact"), emergencyContactField);
        form.addRow(4, new Label("Transaction ID"), transactionIdField);
        form.add(submitButton, 1, 5);
        form.add(statusLabel, 1, 6);

        // Check if Phase Two is accessible
        boolean canFillPhaseTwo = service.canFillPhaseTwo(student);
        
        if (!canFillPhaseTwo) {
            statusLabel.setText("Complete Phase 1 first and wait for approval");
            motherNameField.setDisable(true);
            motherPhoneField.setDisable(true);
            motherResidencyBox.setDisable(true);
            emergencyContactField.setDisable(true);
            transactionIdField.setDisable(true);
            submitButton.setDisable(true);
            tab.setDisable(true);
        } else {
            Optional<DormApplication> existingApp = service.getApplicationForStudent(student);
            if (existingApp.isPresent()) {
                ApplicationStatus appStatus = existingApp.get().getStatus();
                if (appStatus == ApplicationStatus.PHASE_TWO_PENDING ||
                    appStatus == ApplicationStatus.PHASE_TWO_APPROVED ||
                    appStatus == ApplicationStatus.PHASE_TWO_DECLINED ||
                    appStatus == ApplicationStatus.ASSIGNED) {
                    statusLabel.setText("Status: " + appStatus.name());
                    if (appStatus != ApplicationStatus.PHASE_ONE_APPROVED) {
                        submitButton.setDisable(true);
                        motherNameField.setDisable(true);
                        motherPhoneField.setDisable(true);
                        motherResidencyBox.setDisable(true);
                        emergencyContactField.setDisable(true);
                        transactionIdField.setDisable(true);
                    }
                }
            }
        }

        submitButton.setOnAction(event -> {
            if (motherNameField.getText().isBlank() || motherPhoneField.getText().isBlank() ||
                motherResidencyBox.getValue() == null || emergencyContactField.getText().isBlank() ||
                transactionIdField.getText().isBlank()) {
                showAlert("All fields are required");
                return;
            }
            
            try {
                service.submitPhaseTwoApplication(
                    student,
                    motherNameField.getText().trim(),
                    motherPhoneField.getText().trim(),
                    motherResidencyBox.getValue(),
                    emergencyContactField.getText().trim(),
                    transactionIdField.getText().trim()
                );
                statusLabel.setText("Status: PHASE_TWO_PENDING");
                showAlert("Phase 2 submitted successfully");
                refresh();
            } catch (Exception e) {
                showAlert("Failed: " + e.getMessage());
            }
        });

        tab.setContent(form);
        return tab;
    }

    private Tab createAnnouncementsTab() {
        Tab tab = new Tab("Announcements");
        tab.setClosable(false);

        VBox wrapper = new VBox(10, announcementList);
        wrapper.setPadding(new Insets(10));
        tab.setContent(wrapper);
        return tab;
    }

    private Tab createMessagesTab() {
        Tab tab = new Tab("Messages");
        tab.setClosable(false);

        TextArea messageArea = new TextArea();
        messageArea.setPrefRowCount(3);
        Button sendButton = new Button("Send to Admin");

        sendButton.setOnAction(event -> {
            if (messageArea.getText().isBlank()) {
                showAlert("Enter a message");
                return;
            }
            try {
                service.sendMessage(student.getUsername(), "admin", messageArea.getText().trim());
                messageArea.clear();
                refresh();
            } catch (Exception e) {
                showAlert("Failed: " + e.getMessage());
            }
        });

        VBox form = new VBox(10, messageArea, sendButton);
        form.setPadding(new Insets(10));

        VBox wrapper = new VBox(10, form, messageList);
        wrapper.setPadding(new Insets(10));
        tab.setContent(wrapper);
        return tab;
    }

    private void refresh() {
        try {
            announcementList.setItems(FXCollections.observableArrayList(
                    service.getAnnouncements().stream()
                            .map(a -> a.getTitle() + ": " + a.getBody())
                            .collect(Collectors.toList())
            ));
            messageList.setItems(FXCollections.observableArrayList(
                    service.getMessagesForUser(student.getUsername()).stream()
                            .map(m -> m.getSentAt() + " | " + m.getFromUser() + ": " + m.getContent())
                            .collect(Collectors.toList())
            ));
            
            // Update Phase Two tab accessibility
            boolean canFillPhaseTwo = service.canFillPhaseTwo(student);
            phaseTwoTab.setDisable(!canFillPhaseTwo);
        } catch (Exception e) {
            showAlert("Refresh failed: " + e.getMessage());
        }
    }

    private void logout() {
        LoginViewDb loginView = new LoginViewDb(service, stage);
        Scene scene = new Scene(loginView.getRoot(), 900, 600);
        stage.setScene(scene);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
