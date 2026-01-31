package dorm.ui;

import dorm.model.ApplicationStatus;
import dorm.model.DormApplication;
import dorm.model.Student;
import dorm.model.User;
import dorm.service.DatabaseDormService;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
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
 * Admin dashboard UI.
 * Demonstrates I/O operations (file export) and separation of concerns.
 */
public class AdminDashboardDb {
    private final DatabaseDormService service;
    private final User admin;
    private final BorderPane root;
    private final TableView<DormApplication> applicationTable;
    private final ListView<String> announcementList;
    private final ListView<String> messageList;

    public AdminDashboardDb(DatabaseDormService service, User admin) {
        this.service = service;
        this.admin = admin;
        this.root = new BorderPane();
        this.applicationTable = new TableView<>();
        this.announcementList = new ListView<>();
        this.messageList = new ListView<>();
        build();
        refresh();
    }

    public Parent getRoot() {
        return root;
    }

    private void build() {
        Label header = new Label("Admin Dashboard - " + admin.getDisplayName());
        header.setPadding(new Insets(10));
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
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

    /**
     * Demonstrates File I/O - exports student data to CSV file
     */
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
                    service.getMessagesForUser(admin.getUsername()).stream()
                            .map(message -> message.getSentAt() + " | " + message.getFromUser() + ": " + message.getContent())
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
