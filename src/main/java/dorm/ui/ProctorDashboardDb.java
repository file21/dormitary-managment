package dorm.ui;

import dorm.model.Building;
import dorm.model.Student;
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
 * Proctor dashboard UI - proctors login as buildings.
 * Shows students assigned to this building and manages entry/withdrawal.
 */
public class ProctorDashboardDb {
    private final DatabaseDormService service;
    private final Building building;
    private final Stage stage;
    private final BorderPane root;
    private final TableView<Student> studentsTable;
    private final ListView<String> messageList;
    private final Label occupancyLabel;

    public ProctorDashboardDb(DatabaseDormService service, Building building, Stage stage) {
        this.service = service;
        this.building = building;
        this.stage = stage;
        this.root = new BorderPane();
        this.studentsTable = new TableView<>();
        this.messageList = new ListView<>();
        this.occupancyLabel = new Label();
        build();
        refresh();
    }

    public Parent getRoot() {
        return root;
    }

    private void build() {
        // Header with building info and logout button
        Label headerLabel = new Label("Building: " + building.getName() + " (Proctor Dashboard)");
        headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(event -> logout());
        
        HBox header = new HBox(20, headerLabel, occupancyLabel, logoutButton);
        header.setPadding(new Insets(10));
        root.setTop(header);

        TabPane tabs = new TabPane();
        tabs.getTabs().add(createBuildingTab());
        tabs.getTabs().add(createMessagesTab());
        root.setCenter(tabs);
    }

    private Tab createBuildingTab() {
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

        TableColumn<Student, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cell -> {
            Student s = cell.getValue();
            String status;
            if (s.getEntryDate() != null && !s.getEntryDate().isBlank()) {
                if (s.getWithdrawalDate() != null && !s.getWithdrawalDate().isBlank()) {
                    status = "Withdrawn";
                } else {
                    status = "In Dorm";
                }
            } else {
                status = "Assigned (Not Entered)";
            }
            return new javafx.beans.property.SimpleStringProperty(status);
        });

        studentsTable.getColumns().addAll(nameCol, idCol, genderCol, entryCol, withdrawalCol, statusCol);
        studentsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button registerEntryButton = new Button("Register Entry");
        Button registerWithdrawalButton = new Button("Register Withdrawal");

        registerEntryButton.setOnAction(event -> {
            Student selected = studentsTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Select Student", "Choose a student to register entry.");
                return;
            }
            if (selected.getEntryDate() != null && !selected.getEntryDate().isBlank() &&
                (selected.getWithdrawalDate() == null || selected.getWithdrawalDate().isBlank())) {
                showAlert("Already Entered", "This student has already entered the dorm.");
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
            if (selected.getEntryDate() == null || selected.getEntryDate().isBlank()) {
                showAlert("Not Entered", "This student has not entered the dorm yet.");
                return;
            }
            if (selected.getWithdrawalDate() != null && !selected.getWithdrawalDate().isBlank()) {
                showAlert("Already Withdrawn", "This student has already withdrawn.");
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
                service.getStudentsByBuilding(building.getName()).stream()
                    .map(Student::getUsername).collect(Collectors.toList())
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
                service.sendMessage(building.getName(), recipientBox.getValue(), messageArea.getText().trim());
                messageArea.clear();
                refresh();
            } catch (Exception e) {
                showAlert("Error", "Failed to send message: " + e.getMessage());
            }
        });

        VBox form = new VBox(10, new Label("Send Message to Student"), recipientBox, messageArea, sendButton);
        form.setPadding(new Insets(10));

        VBox wrapper = new VBox(10, form, new Label("Message History"), messageList);
        wrapper.setPadding(new Insets(10));
        tab.setContent(wrapper);
        return tab;
    }

    private void refresh() {
        try {
            studentsTable.setItems(FXCollections.observableArrayList(
                service.getStudentsByBuilding(building.getName())
            ));

            messageList.setItems(FXCollections.observableArrayList(
                    service.getMessagesForUser(building.getName()).stream()
                            .map(message -> message.getSentAt() + " | " + message.getFromUser() + ": " + message.getContent())
                            .collect(Collectors.toList())
            ));
            
            // Update occupancy label
            int occupancy = service.getBuildingOccupancy(building.getName());
            int remaining = service.getBuildingRemainingCapacity(building.getName());
            occupancyLabel.setText(String.format("Capacity: %d/%d (Remaining: %d)", 
                occupancy, building.getMaxCapacity(), remaining));
            occupancyLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
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
