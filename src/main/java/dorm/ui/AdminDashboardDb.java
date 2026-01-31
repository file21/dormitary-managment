package dorm.ui;

import dorm.model.ApplicationStatus;
import dorm.model.Building;
import dorm.model.DormApplication;
import dorm.model.SponsorshipType;
import dorm.model.Student;
import dorm.model.User;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.stream.Collectors;

/**
 * Admin dashboard UI with application review, document viewing, and building assignment.
 */
public class AdminDashboardDb {
    private final DatabaseDormService service;
    private final User admin;
    private final Stage stage;
    private final BorderPane root;
    private final TableView<DormApplication> applicationTable;
    private final ListView<String> announcementList;
    private final ListView<String> messageList;
    private final TextArea detailsArea;
    private final Label buildingCapacityLabel;

    public AdminDashboardDb(DatabaseDormService service, User admin, Stage stage) {
        this.service = service;
        this.admin = admin;
        this.stage = stage;
        this.root = new BorderPane();
        this.applicationTable = new TableView<>();
        this.announcementList = new ListView<>();
        this.messageList = new ListView<>();
        this.detailsArea = new TextArea();
        this.detailsArea.setEditable(false);
        this.detailsArea.setPrefRowCount(8);
        this.buildingCapacityLabel = new Label();
        build();
        refresh();
    }

    public Parent getRoot() {
        return root;
    }

    private void build() {
        // Header with logout button
        Label headerLabel = new Label("Admin Dashboard - " + admin.getDisplayName());
        headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(event -> logout());
        
        HBox header = new HBox(20, headerLabel, logoutButton);
        header.setPadding(new Insets(10));
        root.setTop(header);

        TabPane tabs = new TabPane();
        tabs.getTabs().add(createApplicationsTab());
        tabs.getTabs().add(createAnnouncementsTab());
        tabs.getTabs().add(createMessagesTab());
        tabs.getTabs().add(createSearchTab());

        root.setCenter(tabs);
    }

    private Tab createApplicationsTab() {
        Tab tab = new Tab("Applications");
        tab.setClosable(false);

        // Table columns
        TableColumn<DormApplication, String> studentCol = new TableColumn<>("Student");
        studentCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
            cell.getValue().getStudent().getDisplayName()));
        studentCol.setPrefWidth(150);

        TableColumn<DormApplication, String> idCol = new TableColumn<>("Student ID");
        idCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
            cell.getValue().getStudent().getStudentId()));
        idCol.setPrefWidth(100);

        TableColumn<DormApplication, String> sponsorCol = new TableColumn<>("Sponsorship");
        sponsorCol.setCellValueFactory(cell -> {
            SponsorshipType type = cell.getValue().getStudent().getSponsorshipType();
            return new javafx.beans.property.SimpleStringProperty(type != null ? type.name() : "-");
        });
        sponsorCol.setPrefWidth(120);

        TableColumn<DormApplication, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
            cell.getValue().getStatus().name()));
        statusCol.setPrefWidth(150);

        applicationTable.getColumns().addAll(studentCol, idCol, sponsorCol, statusCol);
        applicationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Show details when selected
        applicationTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                showApplicationDetails(newVal);
            }
        });

        // Action buttons
        TextField noteField = new TextField();
        noteField.setPromptText("Note for student");
        noteField.setPrefWidth(200);

        Button approveBtn = new Button("Approve");
        Button declineBtn = new Button("Decline");
        Button resubmitBtn = new Button("Request Resubmit");
        Button viewDocBtn = new Button("View Document");
        Button viewPaymentBtn = new Button("View Payment Slip");

        // Building selection with capacity display
        ComboBox<String> buildingBox = new ComboBox<>();
        buildingBox.setPromptText("Select Building");
        buildingBox.setPrefWidth(150);
        
        // Populate building dropdown
        buildingBox.setItems(FXCollections.observableArrayList(
            service.getBuildings().stream()
                .map(b -> b.getName() + " (" + service.getBuildingRemainingCapacity(b.getName()) + " available)")
                .collect(Collectors.toList())
        ));
        
        buildingBox.setOnAction(event -> {
            String selected = buildingBox.getValue();
            if (selected != null) {
                String buildingName = selected.split(" \\(")[0];
                int remaining = service.getBuildingRemainingCapacity(buildingName);
                int occupancy = service.getBuildingOccupancy(buildingName);
                Building building = service.findBuildingByName(buildingName).orElse(null);
                if (building != null) {
                    buildingCapacityLabel.setText(String.format("Capacity: %d/%d", occupancy, building.getMaxCapacity()));
                }
            }
        });
        
        Button assignBtn = new Button("Assign Building");

        approveBtn.setOnAction(event -> {
            DormApplication selected = applicationTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Select Application", "Please select an application first.");
                return;
            }
            try {
                ApplicationStatus status = selected.getStatus();
                if (status == ApplicationStatus.PHASE_ONE_PENDING) {
                    service.approvePhaseOne(selected, noteField.getText().trim());
                } else if (status == ApplicationStatus.PHASE_TWO_PENDING) {
                    service.approvePhaseTwoApplication(selected, noteField.getText().trim());
                } else {
                    showAlert("Cannot Approve", "This application is not in a pending state.");
                    return;
                }
                refresh();
                showAlert("Approved", "Application approved successfully.");
            } catch (Exception e) {
                showAlert("Error", "Failed to approve: " + e.getMessage());
            }
        });

        declineBtn.setOnAction(event -> {
            DormApplication selected = applicationTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Select Application", "Please select an application first.");
                return;
            }
            try {
                ApplicationStatus status = selected.getStatus();
                if (status == ApplicationStatus.PHASE_ONE_PENDING) {
                    service.declinePhaseOne(selected, noteField.getText().trim());
                } else if (status == ApplicationStatus.PHASE_TWO_PENDING) {
                    service.declinePhaseTwoApplication(selected, noteField.getText().trim());
                } else {
                    showAlert("Cannot Decline", "This application is not in a pending state.");
                    return;
                }
                refresh();
                showAlert("Declined", "Application declined.");
            } catch (Exception e) {
                showAlert("Error", "Failed to decline: " + e.getMessage());
            }
        });

        resubmitBtn.setOnAction(event -> {
            DormApplication selected = applicationTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Select Application", "Please select an application first.");
                return;
            }
            if (noteField.getText().isBlank()) {
                showAlert("Note Required", "Please provide a note explaining what needs to be corrected.");
                return;
            }
            try {
                service.requestResubmit(selected, noteField.getText().trim());
                refresh();
                showAlert("Resubmit Requested", "Student has been asked to resubmit.");
            } catch (Exception e) {
                showAlert("Error", "Failed: " + e.getMessage());
            }
        });

        viewDocBtn.setOnAction(event -> {
            DormApplication selected = applicationTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Select Application", "Please select an application first.");
                return;
            }
            Student student = selected.getStudent();
            if (student.getDocumentPaths().isEmpty()) {
                showAlert("No Document", "No document has been uploaded by this student.");
                return;
            }
            openFile(student.getDocumentPaths().get(0));
        });

        viewPaymentBtn.setOnAction(event -> {
            DormApplication selected = applicationTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Select Application", "Please select an application first.");
                return;
            }
            Student student = selected.getStudent();
            if (student.getPaymentSlipPath() == null || student.getPaymentSlipPath().isBlank()) {
                showAlert("No Payment Slip", "No payment slip has been uploaded by this student.");
                return;
            }
            openFile(student.getPaymentSlipPath());
        });

        assignBtn.setOnAction(event -> {
            DormApplication selected = applicationTable.getSelectionModel().getSelectedItem();
            if (selected == null || buildingBox.getValue() == null) {
                showAlert("Missing Data", "Select an application and a building.");
                return;
            }
            if (!service.isReadyForAssignment(selected)) {
                showAlert("Not Ready", "This student is not ready for building assignment. " +
                    "Government students need Phase One approval. Self-sponsored need Phase Two approval.");
                return;
            }
            
            String buildingName = buildingBox.getValue().split(" \\(")[0];
            
            if (!service.hasBuildingCapacity(buildingName)) {
                showAlert("No Capacity", "This building is full. Please select another building.");
                return;
            }
            
            try {
                service.assignBuilding(selected.getStudent(), buildingName);
                refresh();
                showAlert("Assigned", "Student assigned to " + buildingName);
            } catch (Exception e) {
                showAlert("Error", "Failed to assign: " + e.getMessage());
            }
        });

        // Layout
        HBox actionRow1 = new HBox(10, new Label("Note:"), noteField, approveBtn, declineBtn, resubmitBtn);
        actionRow1.setPadding(new Insets(5));
        
        HBox actionRow2 = new HBox(10, viewDocBtn, viewPaymentBtn, buildingBox, buildingCapacityLabel, assignBtn);
        actionRow2.setPadding(new Insets(5));

        VBox actions = new VBox(5, actionRow1, actionRow2);
        
        VBox tableSection = new VBox(10, applicationTable, actions);
        
        VBox detailsSection = new VBox(5, new Label("Selected Application Details:"), detailsArea);
        detailsSection.setPrefWidth(300);
        detailsSection.setPadding(new Insets(10));

        HBox mainContent = new HBox(10, tableSection, detailsSection);
        mainContent.setPadding(new Insets(10));
        
        tab.setContent(mainContent);
        return tab;
    }

    private void showApplicationDetails(DormApplication app) {
        Student s = app.getStudent();
        StringBuilder sb = new StringBuilder();
        sb.append("Name: ").append(s.getDisplayName()).append("\n");
        sb.append("Student ID: ").append(s.getStudentId()).append("\n");
        sb.append("City: ").append(s.getCity()).append("\n");
        sb.append("Gender: ").append(s.getGender() != null ? s.getGender().name() : "-").append("\n");
        sb.append("Sponsorship: ").append(s.getSponsorshipType() != null ? s.getSponsorshipType().name() : "-").append("\n");
        sb.append("Disability: ").append(s.getDisabilityInfo() != null ? s.getDisabilityInfo() : "None").append("\n");
        sb.append("Status: ").append(app.getStatus().name()).append("\n");
        sb.append("Admin Note: ").append(app.getAdminNote() != null ? app.getAdminNote() : "-").append("\n");
        sb.append("\nDocuments: ").append(s.getDocumentPaths().isEmpty() ? "None" : String.join(", ", s.getDocumentPaths())).append("\n");
        sb.append("Payment Slip: ").append(s.getPaymentSlipPath() != null ? s.getPaymentSlipPath() : "None").append("\n");
        sb.append("Assigned Building: ").append(s.getAssignedBuilding() != null ? s.getAssignedBuilding() : "-");
        
        detailsArea.setText(sb.toString());
    }

    private void openFile(String path) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                showAlert("File Not Found", "The file does not exist: " + path);
                return;
            }
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            } else {
                showAlert("Cannot Open", "Desktop is not supported. File path: " + path);
            }
        } catch (IOException e) {
            showAlert("Error", "Could not open file: " + e.getMessage());
        }
    }

    private Tab createAnnouncementsTab() {
        Tab tab = new Tab("Announcements");
        tab.setClosable(false);

        TextField titleField = new TextField();
        TextArea bodyArea = new TextArea();
        bodyArea.setPrefRowCount(3);
        Button postButton = new Button("Post Announcement");

        postButton.setOnAction(event -> {
            if (titleField.getText().isBlank() || bodyArea.getText().isBlank()) {
                showAlert("Missing Data", "Title and body are required.");
                return;
            }
            try {
                service.addAnnouncement(titleField.getText().trim(), bodyArea.getText().trim(), admin.getDisplayName());
                titleField.clear();
                bodyArea.clear();
                refresh();
            } catch (Exception e) {
                showAlert("Error", "Failed to post: " + e.getMessage());
            }
        });

        VBox form = new VBox(10, new Label("New Announcement"), titleField, bodyArea, postButton);
        form.setPadding(new Insets(10));

        VBox wrapper = new VBox(10, form, new Label("Recent Announcements"), announcementList);
        wrapper.setPadding(new Insets(10));
        tab.setContent(wrapper);
        return tab;
    }

    private Tab createMessagesTab() {
        Tab tab = new Tab("Messages");
        tab.setClosable(false);

        ComboBox<String> recipientBox = new ComboBox<>();
        recipientBox.setItems(FXCollections.observableArrayList(
                service.getStudents().stream().map(Student::getUsername).collect(Collectors.toList())
        ));
        recipientBox.setPromptText("Select Student");
        
        TextArea messageArea = new TextArea();
        messageArea.setPrefRowCount(3);
        Button sendButton = new Button("Send Message");

        sendButton.setOnAction(event -> {
            if (recipientBox.getValue() == null || messageArea.getText().isBlank()) {
                showAlert("Missing Data", "Select a student and enter a message.");
                return;
            }
            try {
                service.sendMessage(admin.getUsername(), recipientBox.getValue(), messageArea.getText().trim());
                messageArea.clear();
                refresh();
            } catch (Exception e) {
                showAlert("Error", "Failed to send: " + e.getMessage());
            }
        });

        VBox form = new VBox(10, new Label("Send Message"), recipientBox, messageArea, sendButton);
        form.setPadding(new Insets(10));

        VBox wrapper = new VBox(10, form, new Label("Message History"), messageList);
        wrapper.setPadding(new Insets(10));
        tab.setContent(wrapper);
        return tab;
    }

    private Tab createSearchTab() {
        Tab tab = new Tab("Search & Export");
        tab.setClosable(false);

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(10);
        grid.setVgap(10);

        TextField studentIdField = new TextField();
        Button searchButton = new Button("Search");
        Label resultLabel = new Label();
        resultLabel.setWrapText(true);

        searchButton.setOnAction(event -> {
            String id = studentIdField.getText().trim();
            if (id.isBlank()) {
                showAlert("Missing Data", "Enter a student ID.");
                return;
            }
            try {
                service.findStudentByStudentId(id)
                        .map(student -> formatStudent(student))
                        .ifPresentOrElse(resultLabel::setText, () -> resultLabel.setText("Student not found"));
            } catch (Exception e) {
                showAlert("Error", "Search failed: " + e.getMessage());
            }
        });

        Button exportButton = new Button("Export All Students to CSV");
        exportButton.setOnAction(event -> exportToCsv());

        grid.addRow(0, new Label("Student ID"), studentIdField, searchButton);
        grid.addRow(1, new Label("Result"), resultLabel);
        grid.addRow(2, exportButton);

        tab.setContent(grid);
        return tab;
    }

    private void exportToCsv() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Student List");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = chooser.showSaveDialog(root.getScene().getWindow());
        if (file == null) {
            return;
        }
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("Name,Student ID,City,Gender,Sponsorship,Building,Status\n");
            for (Student student : service.getStudents()) {
                String status = service.getApplicationForStudent(student)
                        .map(app -> app.getStatus().name())
                        .orElse("No Application");
                writer.write(String.format("%s,%s,%s,%s,%s,%s,%s\n",
                        student.getDisplayName(),
                        student.getStudentId(),
                        student.getCity(),
                        student.getGender() != null ? student.getGender().name() : "-",
                        student.getSponsorshipType() != null ? student.getSponsorshipType().name() : "-",
                        safeValue(student.getAssignedBuilding()),
                        status));
            }
            showAlert("Export Successful", "Student list exported to " + file.getName());
        } catch (IOException e) {
            showAlert("Export Failed", "Could not write file: " + e.getMessage());
        }
    }

    private String formatStudent(Student student) {
        String status = service.getApplicationForStudent(student)
                .map(app -> app.getStatus().name())
                .orElse("No application");
        return String.format("Name: %s\nID: %s\nCity: %s\nGender: %s\nSponsorship: %s\nStatus: %s\nBuilding: %s",
                student.getDisplayName(), 
                student.getStudentId(), 
                student.getCity(),
                student.getGender() != null ? student.getGender().name() : "-",
                student.getSponsorshipType() != null ? student.getSponsorshipType().name() : "-",
                status, 
                safeValue(student.getAssignedBuilding()));
    }

    private void refresh() {
        try {
            applicationTable.setItems(FXCollections.observableArrayList(service.getApplications()));
            announcementList.setItems(FXCollections.observableArrayList(
                    service.getAnnouncements().stream()
                            .map(announcement -> announcement.getTitle() + " - " + announcement.getBody())
                            .collect(Collectors.toList())
            ));
            messageList.setItems(FXCollections.observableArrayList(
                    service.getMessagesForUser(admin.getUsername()).stream()
                            .map(message -> message.getSentAt() + " | " + message.getFromUser() + ": " + message.getContent())
                            .collect(Collectors.toList())
            ));
        } catch (Exception e) {
            showAlert("Error", "Failed to refresh: " + e.getMessage());
        }
    }

    private void logout() {
        LoginViewDb loginView = new LoginViewDb(service, stage);
        Scene scene = new Scene(loginView.getRoot(), 900, 600);
        stage.setScene(scene);
    }

    private String safeValue(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
