package dorm.ui;

import dorm.model.*;
import dorm.service.DatabaseDormService;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

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
    private final BorderPane root;
    private final TableView<DormApplication> applicationTable;
    private final ListView<String> announcementList;
    private final ListView<String> messageList;
    private final ListView<String> staffList;

    public OwnerDashboardDb(DatabaseDormService service, User owner) {
        this.service = service;
        this.owner = owner;
        this.root = new BorderPane();
        this.applicationTable = new TableView<>();
        this.announcementList = new ListView<>();
        this.messageList = new ListView<>();
        this.staffList = new ListView<>();
        build();
        refresh();
    }

    public Parent getRoot() {
        return root;
    }

    private void build() {
        Label header = new Label("Owner Dashboard - " + owner.getDisplayName());
        header.setPadding(new Insets(10));
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
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

        TableColumn<DormApplication, String> studentCol = new TableColumn<>("Student");
        studentCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getStudent().getDisplayName()));

        TableColumn<DormApplication, String> idCol = new TableColumn<>("Student ID");
        idCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getStudent().getStudentId()));

        TableColumn<DormApplication, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getStatus().name()));

        applicationTable.getColumns().addAll(studentCol, idCol, statusCol);
        applicationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        ComboBox<ApplicationStatus> statusBox = new ComboBox<>(FXCollections.observableArrayList(ApplicationStatus.values()));
        TextField noteField = new TextField();
        noteField.setPromptText("Note for student");
        TextField buildingField = new TextField();
        buildingField.setPromptText("Assign building (for approved students)");

        Button updateButton = new Button("Update Status");
        Button assignButton = new Button("Assign Building");

        updateButton.setOnAction(event -> {
            DormApplication selected = applicationTable.getSelectionModel().getSelectedItem();
            if (selected == null || statusBox.getValue() == null) {
                showAlert("Select Application", "Choose an application and status.");
                return;
            }
            try {
                service.updateApplication(selected, statusBox.getValue(), noteField.getText().trim());
                refresh();
            } catch (Exception e) {
                showAlert("Error", "Failed to update: " + e.getMessage());
            }
        });

        assignButton.setOnAction(event -> {
            DormApplication selected = applicationTable.getSelectionModel().getSelectedItem();
            if (selected == null || buildingField.getText().isBlank()) {
                showAlert("Missing Data", "Select an application and enter a building.");
                return;
            }
            if (selected.getStatus() != ApplicationStatus.APPROVED && selected.getStatus() != ApplicationStatus.ASSIGNED) {
                showAlert("Not Approved", "Only approved applications can be assigned.");
                return;
            }
            try {
                service.assignBuilding(selected.getStudent(), buildingField.getText().trim());
                refresh();
            } catch (Exception e) {
                showAlert("Error", "Failed to assign: " + e.getMessage());
            }
        });

        HBox actions = new HBox(10, statusBox, noteField, updateButton, buildingField, assignButton);
        actions.setPadding(new Insets(10));

        VBox wrapper = new VBox(10, applicationTable, actions);
        wrapper.setPadding(new Insets(10));
        tab.setContent(wrapper);
        return tab;
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

        Button exportButton = new Button("Export to CSV");
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
        TextField buildingField = new TextField();

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
                refresh();
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
            writer.write("Name,Student ID,City,Building\n");
            for (Student student : service.getStudents()) {
                writer.write(String.format("%s,%s,%s,%s\n",
                        student.getDisplayName(),
                        student.getStudentId(),
                        student.getCity(),
                        safeValue(student.getAssignedBuilding())));
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
        return String.format("%s | %s | %s | Status: %s | Building: %s",
                student.getDisplayName(), student.getStudentId(), student.getCity(), status, safeValue(student.getAssignedBuilding()));
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
