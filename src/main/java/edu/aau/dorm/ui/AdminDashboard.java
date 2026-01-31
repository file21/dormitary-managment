package edu.aau.dorm.ui;

import edu.aau.dorm.model.ApplicationStatus;
import edu.aau.dorm.model.DormApplication;
import edu.aau.dorm.model.Student;
import edu.aau.dorm.model.User;
import edu.aau.dorm.service.DormService;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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

public class AdminDashboard {
    private final DormService service;
    private final User admin;
    private final BorderPane root;
    private final TableView<DormApplication> applicationTable;
    private final ListView<String> announcementList;
    private final ListView<String> messageList;

    public AdminDashboard(DormService service, User admin) {
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
            service.updateApplication(selected, statusBox.getValue(), noteField.getText().trim());
            refresh();
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
            service.assignBuilding(selected.getStudent(), buildingField.getText().trim());
            refresh();
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
            service.addAnnouncement(titleField.getText().trim(), bodyArea.getText().trim(), admin.getDisplayName());
            titleField.clear();
            bodyArea.clear();
            refresh();
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
            service.sendMessage(admin.getUsername(), recipientBox.getValue(), messageArea.getText().trim());
            messageArea.clear();
            refresh();
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
            service.findStudentByStudentId(id)
                    .map(student -> formatStudent(student))
                    .ifPresentOrElse(resultLabel::setText, () -> resultLabel.setText("Student not found"));
        });

        Button exportButton = new Button("Export to CSV");
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
            writer.write("Name,Student ID,City,Building\n");
            for (Student student : service.getStudents()) {
                writer.write(String.format("%s,%s,%s,%s\n",
                        student.getDisplayName(),
                        student.getStudentId(),
                        student.getCity(),
                        safeValue(student.getAssignedBuilding())));
            }
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
