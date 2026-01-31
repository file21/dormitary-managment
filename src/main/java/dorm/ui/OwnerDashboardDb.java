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
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.stream.Collectors;

/**
 * Owner dashboard UI - has all admin features plus staff management.
 * Demonstrates inheritance of functionality and role hierarchy.
 */
public class OwnerDashboardDb {
    private final DatabaseDormService service;
    private final User owner;
    private final Stage stage;
    private final BorderPane root;
    private final TableView<DormApplication> applicationTable;
    private final ListView<String> announcementList;
    private final ListView<String> messageList;
    private final ListView<String> staffList;
    private final TextArea detailsArea;

    public OwnerDashboardDb(DatabaseDormService service, User owner, Stage stage) {
        this.service = service;
        this.owner = owner;
        this.stage = stage;
        this.root = new BorderPane();
        this.applicationTable = new TableView<>();
        this.announcementList = new ListView<>();
        this.messageList = new ListView<>();
        this.staffList = new ListView<>();
        this.detailsArea = new TextArea();
        this.detailsArea.setEditable(false);
        this.detailsArea.setPrefRowCount(8);
        build();
        refresh();
    }

    public Parent getRoot() {
        return root;
    }

    private void build() {
        // Header with logout button
        Label headerLabel = new Label("Owner Dashboard - " + owner.getDisplayName());
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
        tabs.getTabs().add(createStaffTab());

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

        TextField buildingField = new TextField();
        buildingField.setPromptText("Building name");
        buildingField.setPrefWidth(120);
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
            if (selected == null || buildingField.getText().isBlank()) {
                showAlert("Missing Data", "Select an application and enter a building name.");
                return;
            }
            if (!service.isReadyForAssignment(selected)) {
                showAlert("Not Ready", "This student is not ready for building assignment. " +
                    "Government students need Phase One approval. Self-sponsored need Phase Two approval.");
                return;
            }
            try {
                service.assignBuilding(selected.getStudent(), buildingField.getText().trim());
                refresh();
                showAlert("Assigned", "Building assigned successfully.");
            } catch (Exception e) {
                showAlert("Error", "Failed to assign: " + e.getMessage());
            }
        });

        // Layout
        HBox actionRow1 = new HBox(10, new Label("Note:"), noteField, approveBtn, declineBtn, resubmitBtn);
        actionRow1.setPadding(new Insets(5));
        
        HBox actionRow2 = new HBox(10, viewDocBtn, viewPaymentBtn, new Label("Building:"), buildingField, assignBtn);
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
                service.addAnnouncement(titleField.getText().trim(), bodyArea.getText().trim(), owner.getDisplayName());
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
                service.sendMessage(owner.getUsername(), recipientBox.getValue(), messageArea.getText().trim());
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

    private Tab createStaffTab() {
        Tab tab = new Tab("Staff Management");
        tab.setClosable(false);

        TextField usernameField = new TextField();
        TextField nameField = new TextField();
        PasswordField passwordField = new PasswordField();
        ComboBox<Role> roleBox = new ComboBox<>(FXCollections.observableArrayList(Role.ADMIN, Role.PROCTOR));
        roleBox.setPromptText("Select Role");
        TextField buildingField = new TextField();
        buildingField.setPromptText("Building (for proctors)");

        Button addButton = new Button("Add Staff");
        Button removeButton = new Button("Remove Selected");

        addButton.setOnAction(event -> {
            if (usernameField.getText().isBlank() || nameField.getText().isBlank() || 
                passwordField.getText().isBlank() || roleBox.getValue() == null) {
                showAlert("Missing Data", "Fill all staff details.");
                return;
            }
            try {
                User staff = new User(java.util.UUID.randomUUID().toString(), 
                    usernameField.getText().trim(), 
                    passwordField.getText().trim(), 
                    roleBox.getValue(), 
                    nameField.getText().trim());
                service.addUser(staff);
                if (roleBox.getValue() == Role.PROCTOR) {
                    service.assignBuildingToProctor(staff, buildingField.getText().isBlank() ? "Unassigned" : buildingField.getText().trim());
                }
                usernameField.clear();
                nameField.clear();
                passwordField.clear();
                buildingField.clear();
                refresh();
                showAlert("Staff Added", "New staff member added successfully.");
            } catch (Exception e) {
                showAlert("Error", "Failed to add staff: " + e.getMessage());
            }
        });

        removeButton.setOnAction(event -> {
            String selected = staffList.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Select Staff", "Choose a staff member to remove.");
                return;
            }
            try {
                service.getUsers().stream()
                        .filter(user -> (user.getRole() == Role.ADMIN || user.getRole() == Role.PROCTOR) && 
                                       selected.startsWith(user.getUsername()))
                        .findFirst()
                        .ifPresent(service::removeUser);
                refresh();
                showAlert("Staff Removed", "Staff member removed.");
            } catch (Exception e) {
                showAlert("Error", "Failed to remove staff: " + e.getMessage());
            }
        });

        GridPane form = new GridPane();
        form.setPadding(new Insets(10));
        form.setHgap(10);
        form.setVgap(10);
        form.addRow(0, new Label("Username"), usernameField);
        form.addRow(1, new Label("Full Name"), nameField);
        form.addRow(2, new Label("Password"), passwordField);
        form.addRow(3, new Label("Role"), roleBox);
        form.addRow(4, new Label("Building (proctors)"), buildingField);
        form.addRow(5, addButton, removeButton);

        VBox wrapper = new VBox(10, form, new Label("Current Staff"), staffList);
        wrapper.setPadding(new Insets(10));
        tab.setContent(wrapper);
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
                    service.getMessagesForUser(owner.getUsername()).stream()
                            .map(message -> message.getSentAt() + " | " + message.getFromUser() + ": " + message.getContent())
                            .collect(Collectors.toList())
            ));
            staffList.setItems(FXCollections.observableArrayList(
                    service.getUsers().stream()
                            .filter(user -> user.getRole() == Role.ADMIN || user.getRole() == Role.PROCTOR)
                            .map(user -> user.getUsername() + " (" + user.getRole().name() + ")")
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
