package dorm.ui;

import dorm.model.*;
import dorm.service.DatabaseDormService;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminDashboardDb {
    private final DatabaseDormService service;
    private final User admin;
    private final Stage stage;
    private final BorderPane root;
    private final TableView<DormApplication> applicationTable;
    private final ListView<String> announcementList;
    private final ListView<String> messageList;
    private final Map<String, SimpleBooleanProperty> selectionMap = new HashMap<>();

    public AdminDashboardDb(DatabaseDormService service, User admin, Stage stage) {
        this.service = service;
        this.admin = admin;
        this.stage = stage;
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
        Label headerLabel = new Label("Admin: " + admin.getDisplayName());
        headerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
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

        // Checkbox column
        TableColumn<DormApplication, Boolean> selectCol = new TableColumn<>("Select");
        selectCol.setCellValueFactory(cell -> {
            String id = cell.getValue().getId();
            selectionMap.putIfAbsent(id, new SimpleBooleanProperty(false));
            return selectionMap.get(id);
        });
        selectCol.setCellFactory(col -> new CheckBoxTableCell<>());
        selectCol.setEditable(true);
        selectCol.setPrefWidth(60);

        TableColumn<DormApplication, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
            cell.getValue().getStudent().getDisplayName()));
        nameCol.setPrefWidth(120);

        TableColumn<DormApplication, String> idCol = new TableColumn<>("Student ID");
        idCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
            cell.getValue().getStudent().getStudentId()));
        idCol.setPrefWidth(100);

        TableColumn<DormApplication, String> sponsorCol = new TableColumn<>("Sponsorship");
        sponsorCol.setCellValueFactory(cell -> {
            SponsorshipType type = cell.getValue().getStudent().getSponsorshipType();
            return new javafx.beans.property.SimpleStringProperty(type != null ? type.name() : "-");
        });
        sponsorCol.setPrefWidth(100);

        TableColumn<DormApplication, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
            cell.getValue().getStatus().name()));
        statusCol.setPrefWidth(130);

        TableColumn<DormApplication, String> buildingCol = new TableColumn<>("Building");
        buildingCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
            cell.getValue().getStudent().getAssignedBuilding()));
        buildingCol.setPrefWidth(100);

        applicationTable.getColumns().addAll(selectCol, nameCol, idCol, sponsorCol, statusCol, buildingCol);
        applicationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        applicationTable.setEditable(true);

        // Select all checkbox
        CheckBox selectAllBox = new CheckBox("Select All");
        selectAllBox.setOnAction(event -> {
            boolean selected = selectAllBox.isSelected();
            for (DormApplication app : applicationTable.getItems()) {
                selectionMap.putIfAbsent(app.getId(), new SimpleBooleanProperty(false));
                selectionMap.get(app.getId()).set(selected);
            }
            applicationTable.refresh();
        });

        // Bulk action buttons
        Button approveBtn = new Button("Approve Selected");
        Button declineBtn = new Button("Decline Selected");
        Button resubmitBtn = new Button("Request Resubmit");
        Button exportBtn = new Button("Export Selected to CSV");
        
        TextField buildingField = new TextField();
        buildingField.setPromptText("Building name");
        buildingField.setPrefWidth(100);
        Button assignBtn = new Button("Assign Building");

        approveBtn.setOnAction(event -> {
            List<DormApplication> selected = getSelectedApplications();
            if (selected.isEmpty()) {
                showAlert("Select applications first");
                return;
            }
            for (DormApplication app : selected) {
                ApplicationStatus status = app.getStatus();
                if (status == ApplicationStatus.PHASE_ONE_PENDING) {
                    service.approvePhaseOne(app, "");
                } else if (status == ApplicationStatus.PHASE_TWO_PENDING) {
                    service.approvePhaseTwoApplication(app, "");
                }
            }
            refresh();
            showAlert("Approved " + selected.size() + " applications");
        });

        declineBtn.setOnAction(event -> {
            List<DormApplication> selected = getSelectedApplications();
            if (selected.isEmpty()) {
                showAlert("Select applications first");
                return;
            }
            for (DormApplication app : selected) {
                ApplicationStatus status = app.getStatus();
                if (status == ApplicationStatus.PHASE_ONE_PENDING) {
                    service.declinePhaseOne(app, "");
                } else if (status == ApplicationStatus.PHASE_TWO_PENDING) {
                    service.declinePhaseTwoApplication(app, "");
                }
            }
            refresh();
            showAlert("Declined " + selected.size() + " applications");
        });

        resubmitBtn.setOnAction(event -> {
            List<DormApplication> selected = getSelectedApplications();
            if (selected.isEmpty()) {
                showAlert("Select applications first");
                return;
            }
            for (DormApplication app : selected) {
                if (app.getStatus() == ApplicationStatus.PHASE_ONE_PENDING) {
                    service.requestResubmit(app, "Please resubmit");
                }
            }
            refresh();
            showAlert("Requested resubmit for selected applications");
        });

        assignBtn.setOnAction(event -> {
            List<DormApplication> selected = getSelectedApplications();
            String building = buildingField.getText().trim();
            if (selected.isEmpty() || building.isEmpty()) {
                showAlert("Select applications and enter building name");
                return;
            }
            int count = 0;
            for (DormApplication app : selected) {
                if (service.isReadyForAssignment(app)) {
                    service.assignBuilding(app.getStudent(), building);
                    count++;
                }
            }
            refresh();
            showAlert("Assigned " + count + " students to " + building);
        });

        exportBtn.setOnAction(event -> {
            List<DormApplication> selected = getSelectedApplications();
            if (selected.isEmpty()) {
                showAlert("Select applications first");
                return;
            }
            exportToCsv(selected);
        });

        HBox actionRow1 = new HBox(10, selectAllBox, approveBtn, declineBtn, resubmitBtn);
        actionRow1.setPadding(new Insets(5));
        
        HBox actionRow2 = new HBox(10, buildingField, assignBtn, exportBtn);
        actionRow2.setPadding(new Insets(5));

        VBox wrapper = new VBox(10, applicationTable, actionRow1, actionRow2);
        wrapper.setPadding(new Insets(10));
        tab.setContent(wrapper);
        return tab;
    }

    private List<DormApplication> getSelectedApplications() {
        List<DormApplication> selected = new ArrayList<>();
        for (DormApplication app : applicationTable.getItems()) {
            SimpleBooleanProperty prop = selectionMap.get(app.getId());
            if (prop != null && prop.get()) {
                selected.add(app);
            }
        }
        return selected;
    }

    private void exportToCsv(List<DormApplication> applications) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = chooser.showSaveDialog(root.getScene().getWindow());
        if (file == null) return;
        
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("Name,Student ID,Gender,Sponsorship,Residency,City,Subcity,Woreda,Status,Building,Transaction ID\n");
            for (DormApplication app : applications) {
                Student s = app.getStudent();
                writer.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                        s.getDisplayName(),
                        s.getStudentId(),
                        s.getGender() != null ? s.getGender().name() : "-",
                        s.getSponsorshipType() != null ? s.getSponsorshipType().name() : "-",
                        s.getResidency() != null ? s.getResidency().name() : "-",
                        safe(s.getCity()),
                        safe(s.getSubcity()),
                        safe(s.getWoreda()),
                        app.getStatus().name(),
                        safe(s.getAssignedBuilding()),
                        safe(s.getTransactionId())));
            }
            showAlert("Exported to " + file.getName());
        } catch (IOException e) {
            showAlert("Export failed: " + e.getMessage());
        }
    }

    private Tab createAnnouncementsTab() {
        Tab tab = new Tab("Announcements");
        tab.setClosable(false);

        TextField titleField = new TextField();
        TextArea bodyArea = new TextArea();
        bodyArea.setPrefRowCount(3);
        Button postButton = new Button("Post");

        postButton.setOnAction(event -> {
            if (titleField.getText().isBlank() || bodyArea.getText().isBlank()) {
                showAlert("Title and body required");
                return;
            }
            try {
                service.addAnnouncement(titleField.getText().trim(), bodyArea.getText().trim(), admin.getDisplayName());
                titleField.clear();
                bodyArea.clear();
                refresh();
            } catch (Exception e) {
                showAlert("Failed: " + e.getMessage());
            }
        });

        VBox form = new VBox(10, titleField, bodyArea, postButton);
        form.setPadding(new Insets(10));

        VBox wrapper = new VBox(10, form, announcementList);
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
        Button sendButton = new Button("Send");

        sendButton.setOnAction(event -> {
            if (recipientBox.getValue() == null || messageArea.getText().isBlank()) {
                showAlert("Select student and enter message");
                return;
            }
            try {
                service.sendMessage(admin.getUsername(), recipientBox.getValue(), messageArea.getText().trim());
                messageArea.clear();
                refresh();
            } catch (Exception e) {
                showAlert("Failed: " + e.getMessage());
            }
        });

        VBox form = new VBox(10, recipientBox, messageArea, sendButton);
        form.setPadding(new Insets(10));

        VBox wrapper = new VBox(10, form, messageList);
        wrapper.setPadding(new Insets(10));
        tab.setContent(wrapper);
        return tab;
    }

    private Tab createSearchTab() {
        Tab tab = new Tab("Search");
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
                showAlert("Enter student ID");
                return;
            }
            service.findStudentByStudentId(id)
                    .map(this::formatStudent)
                    .ifPresentOrElse(resultLabel::setText, () -> resultLabel.setText("Not found"));
        });

        grid.addRow(0, new Label("Student ID"), studentIdField, searchButton);
        grid.addRow(1, new Label("Result"), resultLabel);

        tab.setContent(grid);
        return tab;
    }

    private String formatStudent(Student s) {
        String status = service.getApplicationForStudent(s)
                .map(app -> app.getStatus().name())
                .orElse("No application");
        return String.format("Name: %s | ID: %s | Gender: %s | Sponsorship: %s | Status: %s | Building: %s",
                s.getDisplayName(), s.getStudentId(),
                s.getGender() != null ? s.getGender().name() : "-",
                s.getSponsorshipType() != null ? s.getSponsorshipType().name() : "-",
                status, safe(s.getAssignedBuilding()));
    }

    private void refresh() {
        applicationTable.setItems(FXCollections.observableArrayList(service.getApplications()));
        announcementList.setItems(FXCollections.observableArrayList(
                service.getAnnouncements().stream()
                        .map(a -> a.getTitle() + " - " + a.getBody())
                        .collect(Collectors.toList())
        ));
        messageList.setItems(FXCollections.observableArrayList(
                service.getMessagesForUser(admin.getUsername()).stream()
                        .map(m -> m.getSentAt() + " | " + m.getFromUser() + ": " + m.getContent())
                        .collect(Collectors.toList())
        ));
    }

    private void logout() {
        LoginViewDb loginView = new LoginViewDb(service, stage);
        Scene scene = new Scene(loginView.getRoot(), 900, 600);
        stage.setScene(scene);
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
