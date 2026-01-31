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
    private Student student;  // Not final - can be refreshed
    private final Stage stage;
    private final BorderPane root;
    private final ListView<Announcement> announcementListView;
    private final ListView<String> messageList;
    private TabPane tabs;
    private Label statusLabel;

    public StudentDashboardDb(DatabaseDormService service, Student student, Stage stage) {
        this.service = service;
        this.student = student;
        this.stage = stage;
        this.root = new BorderPane();
        this.announcementListView = new ListView<>();
        this.messageList = new ListView<>();
        build();
        refresh();
    }

    public Parent getRoot() {
        return root;
    }

    private void build() {
        Label headerLabel = new Label("Welcome, " + student.getDisplayName() + " (ID: " + student.getStudentId() + ")");
        headerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(event -> refreshAll());
        
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(event -> logout());
        
        HBox header = new HBox(20, headerLabel, refreshButton, logoutButton);
        header.setPadding(new Insets(10));
        root.setTop(header);

        tabs = new TabPane();
        tabs.getTabs().add(createProfileTab());
        tabs.getTabs().add(createPhaseOneTab());
        tabs.getTabs().add(createPhaseTwoTab());
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
        grid.addRow(3, new Label("College:"), new Label(student.getCollege() != null ? student.getCollege().getAcronym() : "-"));
        grid.addRow(4, new Label("Sponsorship:"), new Label(student.getSponsorshipType() != null ? student.getSponsorshipType().name() : "-"));
        grid.addRow(5, new Label("Building:"), new Label(student.getAssignedBuilding()));
        
        statusLabel = new Label();
        updateStatusLabel();
        grid.addRow(6, new Label("Status:"), statusLabel);

        tab.setContent(grid);
        return tab;
    }

    private void updateStatusLabel() {
        String status = service.getApplicationForStudent(student)
                .map(app -> app.getStatus().name())
                .orElse("Not Applied");
        statusLabel.setText(status);
    }

    private Tab createPhaseOneTab() {
        Tab tab = new Tab("Phase 1");
        tab.setClosable(false);

        GridPane form = new GridPane();
        form.setPadding(new Insets(20));
        form.setHgap(10);
        form.setVgap(10);

        ComboBox<SponsorshipType> sponsorshipBox = new ComboBox<>(FXCollections.observableArrayList(SponsorshipType.values()));
        ComboBox<Residency> residencyBox = new ComboBox<>(FXCollections.observableArrayList(Residency.values()));
        TextField cityField = new TextField();
        TextField subcityField = new TextField();
        
        // Woreda - positive integer only
        TextField woredaField = new TextField();
        woredaField.setPromptText("Enter number");
        
        TextField disabilityField = new TextField();
        Button submitButton = new Button("Submit Phase 1");
        Label phaseStatusLabel = new Label();

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
        form.addRow(4, new Label("Woreda (number)"), woredaField);
        form.addRow(5, new Label("Disability (optional)"), disabilityField);
        form.add(submitButton, 1, 6);
        form.add(phaseStatusLabel, 1, 7);

        Optional<DormApplication> existingApp = service.getApplicationForStudent(student);
        if (existingApp.isPresent()) {
            ApplicationStatus appStatus = existingApp.get().getStatus();
            phaseStatusLabel.setText("Status: " + appStatus.name());
            
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
                phaseStatusLabel.setText("Status: " + appStatus.name() + " | Note: " + note);
            }
        }

        submitButton.setOnAction(event -> {
            if (sponsorshipBox.getValue() == null || residencyBox.getValue() == null ||
                cityField.getText().isBlank() || subcityField.getText().isBlank() || 
                woredaField.getText().isBlank()) {
                showAlert("All fields except disability are required");
                return;
            }
            
            // Validate woreda is positive integer
            String woredaText = woredaField.getText().trim();
            try {
                int woredaNum = Integer.parseInt(woredaText);
                if (woredaNum <= 0) {
                    showAlert("Woreda must be a positive number");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert("Woreda must be a valid positive number");
                return;
            }
            
            try {
                service.submitPhaseOneApplication(
                    student,
                    sponsorshipBox.getValue(),
                    residencyBox.getValue(),
                    cityField.getText().trim(),
                    subcityField.getText().trim(),
                    woredaText,
                    disabilityField.getText().trim()
                );
                phaseStatusLabel.setText("Status: PHASE_ONE_PENDING");
                showAlert("Phase 1 submitted");
                refreshAll();
            } catch (Exception e) {
                showAlert("Failed: " + e.getMessage());
            }
        });

        tab.setContent(form);
        return tab;
    }

    private Tab createPhaseTwoTab() {
        Tab tab = new Tab("Phase 2");
        tab.setClosable(false);

        GridPane form = new GridPane();
        form.setPadding(new Insets(20));
        form.setHgap(10);
        form.setVgap(10);

        TextField emergencyNameField = new TextField();
        TextField emergencyPhoneField = new TextField();
        TextField transactionIdField = new TextField();
        Button submitButton = new Button("Submit Phase 2");
        Label phaseStatusLabel = new Label();

        if (student.getEmergencyContactName() != null) emergencyNameField.setText(student.getEmergencyContactName());
        if (student.getEmergencyContactPhone() != null) emergencyPhoneField.setText(student.getEmergencyContactPhone());
        if (student.getTransactionId() != null) transactionIdField.setText(student.getTransactionId());

        form.addRow(0, new Label("Emergency Contact Name"), emergencyNameField);
        form.addRow(1, new Label("Emergency Contact Phone"), emergencyPhoneField);
        
        Label transactionLabel = new Label("Transaction ID (self-sponsored)");
        form.addRow(2, transactionLabel, transactionIdField);
        
        form.add(submitButton, 1, 3);
        form.add(phaseStatusLabel, 1, 4);

        boolean canFillPhaseTwo = service.canFillPhaseTwo(student);
        
        if (!canFillPhaseTwo) {
            phaseStatusLabel.setText("Complete Phase 1 first and wait for approval");
            emergencyNameField.setDisable(true);
            emergencyPhoneField.setDisable(true);
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
                    phaseStatusLabel.setText("Status: " + appStatus.name());
                    submitButton.setDisable(true);
                    emergencyNameField.setDisable(true);
                    emergencyPhoneField.setDisable(true);
                    transactionIdField.setDisable(true);
                }
            }
        }

        submitButton.setOnAction(event -> {
            if (emergencyNameField.getText().isBlank() || emergencyPhoneField.getText().isBlank()) {
                showAlert("Emergency contact name and phone are required");
                return;
            }
            
            if (student.getSponsorshipType() == SponsorshipType.SELF_SPONSORED && 
                transactionIdField.getText().isBlank()) {
                showAlert("Transaction ID is required for self-sponsored students");
                return;
            }
            
            try {
                service.submitPhaseTwoApplication(
                    student,
                    emergencyNameField.getText().trim(),
                    emergencyPhoneField.getText().trim(),
                    transactionIdField.getText().trim()
                );
                phaseStatusLabel.setText("Status: PHASE_TWO_PENDING");
                showAlert("Phase 2 submitted");
                refreshAll();
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

        announcementListView.setCellFactory(listView -> new ListCell<Announcement>() {
            @Override
            protected void updateItem(Announcement item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox box = new VBox(5);
                    Label titleLabel = new Label(item.getTitle());
                    titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                    
                    Label bodyLabel = new Label(item.getBody());
                    bodyLabel.setWrapText(true);
                    bodyLabel.setMaxWidth(600);
                    
                    Label dateLabel = new Label(item.getCreatedAt().toString() + " by " + item.getCreatedBy());
                    dateLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: gray;");
                    
                    box.getChildren().addAll(titleLabel, bodyLabel, dateLabel);
                    box.setPadding(new Insets(5));
                    setGraphic(box);
                }
            }
        });

        VBox wrapper = new VBox(10, announcementListView);
        wrapper.setPadding(new Insets(10));
        tab.setContent(wrapper);
        return tab;
    }

    private Tab createMessagesTab() {
        Tab tab = new Tab("Messages");
        tab.setClosable(false);

        ComboBox<String> adminBox = new ComboBox<>();
        adminBox.setPromptText("Select Admin");
        adminBox.setItems(FXCollections.observableArrayList(
            service.getUsersByRole(Role.ADMIN).stream()
                .map(User::getUsername)
                .collect(Collectors.toList())
        ));
        adminBox.getItems().addAll(
            service.getUsersByRole(Role.OWNER).stream()
                .map(User::getUsername)
                .collect(Collectors.toList())
        );

        TextArea messageArea = new TextArea();
        messageArea.setPrefRowCount(3);
        messageArea.setPromptText("Type your message here");
        Button sendButton = new Button("Send");

        sendButton.setOnAction(event -> {
            if (adminBox.getValue() == null || messageArea.getText().isBlank()) {
                showAlert("Select admin and enter message");
                return;
            }
            try {
                service.sendMessage(student.getUsername(), adminBox.getValue(), messageArea.getText().trim());
                messageArea.clear();
                refresh();
                showAlert("Message sent");
            } catch (Exception e) {
                showAlert("Failed: " + e.getMessage());
            }
        });

        VBox form = new VBox(10, adminBox, messageArea, sendButton);
        form.setPadding(new Insets(10));

        VBox wrapper = new VBox(10, form, new Label("Received Messages:"), messageList);
        wrapper.setPadding(new Insets(10));
        tab.setContent(wrapper);
        return tab;
    }

    private void refresh() {
        try {
            announcementListView.setItems(FXCollections.observableArrayList(service.getAnnouncements()));
            messageList.setItems(FXCollections.observableArrayList(
                    service.getMessagesForUser(student.getUsername()).stream()
                            .map(m -> m.getSentAt() + " | " + m.getFromUser() + ": " + m.getContent())
                            .collect(Collectors.toList())
            ));
        } catch (Exception e) {
            showAlert("Refresh failed: " + e.getMessage());
        }
    }

    /**
     * Full refresh - reloads student data and rebuilds UI
     */
    private void refreshAll() {
        try {
            // Reload student from database
            Optional<Student> reloaded = service.findStudentByStudentId(student.getStudentId());
            if (reloaded.isPresent()) {
                this.student = reloaded.get();
            }
            
            // Update status label
            if (statusLabel != null) {
                updateStatusLabel();
            }
            
            // Refresh lists
            refresh();
            
            // Update phase two tab accessibility
            boolean canFillPhaseTwo = service.canFillPhaseTwo(student);
            if (tabs.getTabs().size() > 2) {
                tabs.getTabs().get(2).setDisable(!canFillPhaseTwo);
            }
            
            showAlert("Data refreshed");
        } catch (Exception e) {
            showAlert("Refresh failed: " + e.getMessage());
        }
    }

    private void logout() {
        LoginViewDb loginView = new LoginViewDb(service, stage);
        Scene scene = new Scene(loginView.getRoot(), 1200, 700);
        stage.setScene(scene);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
