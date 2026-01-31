package edu.aau.dorm.ui;

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
import javafx.scene.control.TextArea;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.stream.Collectors;

public class ProctorDashboard {
    private final DormService service;
    private final User proctor;
    private final BorderPane root;
    private final TableView<Student> studentsTable;
    private final ListView<String> messageList;

    public ProctorDashboard(DormService service, User proctor) {
        this.service = service;
        this.proctor = proctor;
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

        Label header = new Label("Proctor Dashboard - " + proctor.getDisplayName() + " (" + buildingName + ")");
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
        nameCol.setCellValueFactory(new PropertyValueFactory<>("displayName"));

        TableColumn<Student, String> idCol = new TableColumn<>("Student ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("studentId"));

        TableColumn<Student, String> entryCol = new TableColumn<>("Entry Date");
        entryCol.setCellValueFactory(new PropertyValueFactory<>("entryDate"));

        TableColumn<Student, String> withdrawalCol = new TableColumn<>("Withdrawal Date");
        withdrawalCol.setCellValueFactory(new PropertyValueFactory<>("withdrawalDate"));

        studentsTable.getColumns().addAll(nameCol, idCol, entryCol, withdrawalCol);
        studentsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button registerEntryButton = new Button("Register Entry");
        Button registerWithdrawalButton = new Button("Register Withdrawal");

        registerEntryButton.setOnAction(event -> {
            Student selected = studentsTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Select Student", "Choose a student to register.");
                return;
            }
            service.registerEntry(selected);
            refresh();
        });

        registerWithdrawalButton.setOnAction(event -> {
            Student selected = studentsTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Select Student", "Choose a student to withdraw.");
                return;
            }
            service.registerWithdrawal(selected);
            refresh();
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
        TextArea messageArea = new TextArea();
        messageArea.setPrefRowCount(3);
        Button sendButton = new Button("Send Message");

        sendButton.setOnAction(event -> {
            if (recipientBox.getValue() == null || messageArea.getText().isBlank()) {
                showAlert("Missing Data", "Select a student and enter a message.");
                return;
            }
            service.sendMessage(proctor.getUsername(), recipientBox.getValue(), messageArea.getText().trim());
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

    private void refresh() {
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
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
