package dorm.ui;

import dorm.model.Student;
import dorm.model.User;
import dorm.service.DatabaseDormService;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.stream.Collectors;

/**
 * Proctor dashboard UI.
 * Demonstrates role-based access control.
 */
public class ProctorDashboardDb {
    private final DatabaseDormService service;
    private final User proctor;
    private final Stage stage;
    private final BorderPane root;
    private final TableView<Student> studentsTable;
    private final ListView<String> messageList;

    public ProctorDashboardDb(DatabaseDormService service, User proctor, Stage stage) {
        this.service = service;
        this.proctor = proctor;
        this.stage = stage;
        this.root = new BorderPane();
        this.studentsTable = new TableView<>();
        this.messageList = new ListView<>();
        build();
        refresh();
    }

    public Parent getRoot() {
        return root;
    }

    private void build() {
        String buildingName = service.getBuildingAssignments().stream()
                .filter(assign -> assign.getProctor().equals(proctor))
                .map(assign -> assign.getBuildingName())
                .findFirst()
                .orElse("Unassigned");

        // Header with logout button
        Label headerLabel = new Label("Proctor Dashboard - " + proctor.getDisplayName() + " (" + buildingName + ")");
        headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(event -> logout());
        
        HBox header = new HBox(20, headerLabel, logoutButton);
        header.setPadding(new Insets(10));
        root.setTop(header);

        TabPane tabs = new TabPane();
        tabs.getTabs().add(createBuildingTab(buildingName));
        tabs.getTabs().add(createMessagesTab());
        root.setCenter(tabs);
    }

    private Tab createBuildingTab(String buildingName) {
        Tab tab = new Tab("Building Students");
        tab.setClosable(false);

        TableColumn<Student, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getDisplayName()));

        TableColumn<Student, String> idCol = new TableColumn<>("Student ID");
        idCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getStudentId()));

        TableColumn<Student, String> genderCol = new TableColumn<>("Gender");
        genderCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
            cell.getValue().getGender() != null ? cell.getValue().getGender().name() : "-"));

        TableColumn<Student, String> entryCol = new TableColumn<>("Entry Date");
        entryCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
            cell.getValue().getEntryDate() == null ? "-" : cell.getValue().getEntryDate()));

        TableColumn<Student, String> withdrawalCol = new TableColumn<>("Withdrawal Date");
        withdrawalCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
            cell.getValue().getWithdrawalDate() == null ? "-" : cell.getValue().getWithdrawalDate()));

        studentsTable.getColumns().addAll(nameCol, idCol, genderCol, entryCol, withdrawalCol);
        studentsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button registerEntryButton = new Button("Register Entry");
        Button registerWithdrawalButton = new Button("Register Withdrawal");

        registerEntryButton.setOnAction(event -> {
            Student selected = studentsTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Select Student", "Choose a student to register.");
                return;
            }
            try {
                service.registerEntry(selected);
                refresh();
                showAlert("Entry Registered", "Entry date recorded for " + selected.getDisplayName());
            } catch (Exception e) {
                showAlert("Error", "Failed to register entry: " + e.getMessage());
            }
        });

        registerWithdrawalButton.setOnAction(event -> {
            Student selected = studentsTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Select Student", "Choose a student to withdraw.");
                return;
            }
            try {
                service.registerWithdrawal(selected);
                refresh();
                showAlert("Withdrawal Registered", "Withdrawal date recorded for " + selected.getDisplayName());
            } catch (Exception e) {
                showAlert("Error", "Failed to register withdrawal: " + e.getMessage());
            }
        });

        HBox actions = new HBox(10, registerEntryButton, registerWithdrawalButton);
        actions.setPadding(new Insets(10));

        VBox wrapper = new VBox(10, studentsTable, actions);
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
                service.sendMessage(proctor.getUsername(), recipientBox.getValue(), messageArea.getText().trim());
                messageArea.clear();
                refresh();
            } catch (Exception e) {
                showAlert("Error", "Failed to send message: " + e.getMessage());
            }
        });

        VBox form = new VBox(10, new Label("Send Message"), recipientBox, messageArea, sendButton);
        form.setPadding(new Insets(10));

        VBox wrapper = new VBox(10, form, new Label("Message History"), messageList);
        wrapper.setPadding(new Insets(10));
        tab.setContent(wrapper);
        return tab;
    }

    private void refresh() {
        try {
            String buildingName = service.getBuildingAssignments().stream()
                    .filter(assign -> assign.getProctor().equals(proctor))
                    .map(assign -> assign.getBuildingName())
                    .findFirst()
                    .orElse("Unassigned");

            studentsTable.setItems(FXCollections.observableArrayList(service.getStudentsByBuilding(buildingName)));

            messageList.setItems(FXCollections.observableArrayList(
                    service.getMessagesForUser(proctor.getUsername()).stream()
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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
