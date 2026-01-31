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
import java.util.*;
import java.util.stream.Collectors;

public class AdminDashboardDb {
    private final DatabaseDormService service;
    private final User admin;
    private final Stage stage;
    private final BorderPane root;
    private final TableView<DormApplication> applicationTable;
    private final ListView<Announcement> announcementListView;
    private final ListView<String> messageList;
    private final Map<String, SimpleBooleanProperty> selectionMap = new HashMap<>();

    public AdminDashboardDb(DatabaseDormService service, User admin, Stage stage) {
        this.service = service;
        this.admin = admin;
        this.stage = stage;
        this.root = new BorderPane();
        this.applicationTable = new TableView<>();
        this.announcementListView = new ListView<>();
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
        
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(event -> refresh());
        
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(event -> logout());
        
        HBox header = new HBox(20, headerLabel, refreshButton, logoutButton);
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

        TableColumn<DormApplication, Boolean> selectCol = new TableColumn<>("Select");
        selectCol.setCellValueFactory(cell -> {
            String id = cell.getValue().getId();
            selectionMap.putIfAbsent(id, new SimpleBooleanProperty(false));
            return selectionMap.get(id);
        });
        selectCol.setCellFactory(col -> new CheckBoxTableCell<>());
        selectCol.setEditable(true);
        selectCol.setPrefWidth(50);

        TableColumn<DormApplication, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
            cell.getValue().getStudent().getDisplayName()));
        nameCol.setPrefWidth(100);

        TableColumn<DormApplication, String> idCol = new TableColumn<>("Student ID");
        idCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
            cell.getValue().getStudent().getStudentId()));

        TableColumn<DormApplication, String> genderCol = new TableColumn<>("Gender");
        genderCol.setCellValueFactory(cell -> {
            Gender g = cell.getValue().getStudent().getGender();
            return new javafx.beans.property.SimpleStringProperty(g != null ? g.name() : "-");
        });

        TableColumn<DormApplication, String> collegeCol = new TableColumn<>("College");
        collegeCol.setCellValueFactory(cell -> {
            College c = cell.getValue().getStudent().getCollege();
            return new javafx.beans.property.SimpleStringProperty(c != null ? c.getAcronym() : "-");
        });

        TableColumn<DormApplication, String> residencyCol = new TableColumn<>("Residency");
        residencyCol.setCellValueFactory(cell -> {
            Residency r = cell.getValue().getStudent().getResidency();
            return new javafx.beans.property.SimpleStringProperty(r != null ? r.name() : "-");
        });

        TableColumn<DormApplication, String> subcityCol = new TableColumn<>("Subcity");
        subcityCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
            safe(cell.getValue().getStudent().getSubcity())));

        TableColumn<DormApplication, String> woredaCol = new TableColumn<>("Woreda");
        woredaCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
            safe(cell.getValue().getStudent().getWoreda())));

        TableColumn<DormApplication, String> transactionCol = new TableColumn<>("Transaction ID");
        transactionCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
            safe(cell.getValue().getStudent().getTransactionId())));

        TableColumn<DormApplication, String> sponsorCol = new TableColumn<>("Sponsorship");
        sponsorCol.setCellValueFactory(cell -> {
            SponsorshipType type = cell.getValue().getStudent().getSponsorshipType();
            return new javafx.beans.property.SimpleStringProperty(type != null ? type.name() : "-");
        });

        TableColumn<DormApplication, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
            cell.getValue().getStatus().name()));

        TableColumn<DormApplication, String> submittedCol = new TableColumn<>("Submitted");
        submittedCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
            safe(cell.getValue().getSubmittedDate())));

        TableColumn<DormApplication, String> buildingCol = new TableColumn<>("Building");
        buildingCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
            cell.getValue().getStudent().getAssignedBuilding()));

        applicationTable.getColumns().addAll(selectCol, nameCol, idCol, genderCol, collegeCol, residencyCol, subcityCol, woredaCol, sponsorCol, statusCol, transactionCol, submittedCol, buildingCol);
        applicationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        applicationTable.setEditable(true);

        CheckBox selectAllBox = new CheckBox("Select All");
        selectAllBox.setOnAction(event -> {
            boolean selected = selectAllBox.isSelected();
            for (DormApplication app : applicationTable.getItems()) {
                selectionMap.putIfAbsent(app.getId(), new SimpleBooleanProperty(false));
                selectionMap.get(app.getId()).set(selected);
            }
            applicationTable.refresh();
        });

        // Status change buttons - work on any status (flexible)
        Button approveBtn = new Button("Approve");
        Button declineBtn = new Button("Decline");
        Button resubmitBtn = new Button("Resubmit");
        Button exportBtn = new Button("Export CSV");
        
        TextField buildingField = new TextField();
        buildingField.setPromptText("Building");
        buildingField.setPrefWidth(80);
        Button assignBtn = new Button("Assign");

        approveBtn.setOnAction(event -> {
            List<DormApplication> selected = getSelectedApplications();
            if (selected.isEmpty()) {
                showAlert("Select applications first");
                return;
            }
            for (DormApplication app : selected) {
                ApplicationStatus status = app.getStatus();
                // Determine which phase to approve based on current status
                if (status == ApplicationStatus.PHASE_ONE_PENDING || 
                    status == ApplicationStatus.PHASE_ONE_DECLINED ||
                    status == ApplicationStatus.PHASE_ONE_RESUBMIT) {
                    service.approvePhaseOne(app, "");
                } else if (status == ApplicationStatus.PHASE_TWO_PENDING ||
                           status == ApplicationStatus.PHASE_TWO_DECLINED) {
                    service.approvePhaseTwoApplication(app, "");
                } else if (status == ApplicationStatus.PHASE_ONE_APPROVED) {
                    // Already approved phase one, skip
                } else if (status == ApplicationStatus.PHASE_TWO_APPROVED ||
                           status == ApplicationStatus.ASSIGNED) {
                    // Already done
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
                if (status == ApplicationStatus.PHASE_ONE_PENDING ||
                    status == ApplicationStatus.PHASE_ONE_APPROVED ||
                    status == ApplicationStatus.PHASE_ONE_RESUBMIT) {
                    service.declinePhaseOne(app, "");
                } else if (status == ApplicationStatus.PHASE_TWO_PENDING ||
                           status == ApplicationStatus.PHASE_TWO_APPROVED) {
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
                ApplicationStatus status = app.getStatus();
                if (status == ApplicationStatus.PHASE_ONE_PENDING ||
                    status == ApplicationStatus.PHASE_ONE_DECLINED ||
                    status == ApplicationStatus.PHASE_ONE_APPROVED) {
                    service.requestResubmit(app, "");
                }
            }
            refresh();
        });

        assignBtn.setOnAction(event -> {
            List<DormApplication> selected = getSelectedApplications();
            String building = buildingField.getText().trim();
            if (selected.isEmpty() || building.isEmpty()) {
                showAlert("Select applications and enter building");
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
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File file = chooser.showSaveDialog(root.getScene().getWindow());
        if (file == null) return;
        
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("Name,Student ID,Gender,Sponsorship,Residency,City,Subcity,Woreda,Status,Submitted,Last Response,Building,Transaction ID\n");
            for (DormApplication app : applications) {
                Student s = app.getStudent();
                writer.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                        s.getDisplayName(),
                        s.getStudentId(),
                        s.getGender() != null ? s.getGender().name() : "-",
                        s.getSponsorshipType() != null ? s.getSponsorshipType().name() : "-",
                        s.getResidency() != null ? s.getResidency().name() : "-",
                        safe(s.getCity()),
                        safe(s.getSubcity()),
                        safe(s.getWoreda()),
                        app.getStatus().name(),
                        safe(app.getSubmittedDate()),
                        safe(app.getLatestResponse()),
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

        // Custom cell factory for multi-line display
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
                    bodyLabel.setMaxWidth(500);
                    
                    Label dateLabel = new Label(item.getCreatedAt().toString() + " by " + item.getCreatedBy());
                    dateLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: gray;");
                    
                    box.getChildren().addAll(titleLabel, bodyLabel, dateLabel);
                    box.setPadding(new Insets(5));
                    setGraphic(box);
                }
            }
        });
        announcementListView.setPrefHeight(300);

        // Form for new/edit announcement
        TextField titleField = new TextField();
        titleField.setPromptText("Title");
        
        TextArea bodyArea = new TextArea();
        bodyArea.setPrefRowCount(4);
        bodyArea.setPromptText("Content (supports multiple lines)");
        bodyArea.setWrapText(true);
        
        Button postButton = new Button("Post");
        Button editButton = new Button("Edit Selected");
        Button deleteButton = new Button("Delete Selected");

        final Announcement[] editingAnnouncement = {null};

        postButton.setOnAction(event -> {
            if (titleField.getText().isBlank() || bodyArea.getText().isBlank()) {
                showAlert("Title and content required");
                return;
            }
            
            if (editingAnnouncement[0] != null) {
                // Saving edit
                editingAnnouncement[0].setTitle(titleField.getText().trim());
                editingAnnouncement[0].setBody(bodyArea.getText().trim());
                service.updateAnnouncement(editingAnnouncement[0]);
                editingAnnouncement[0] = null;
                postButton.setText("Post");
            } else {
                // New post
                service.addAnnouncement(titleField.getText().trim(), bodyArea.getText().trim(), admin.getDisplayName());
            }
            
            titleField.clear();
            bodyArea.clear();
            refresh();
        });

        editButton.setOnAction(event -> {
            Announcement selected = announcementListView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Select an announcement first");
                return;
            }
            editingAnnouncement[0] = selected;
            titleField.setText(selected.getTitle());
            bodyArea.setText(selected.getBody());
            postButton.setText("Save");
        });

        deleteButton.setOnAction(event -> {
            Announcement selected = announcementListView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Select an announcement first");
                return;
            }
            service.deleteAnnouncement(selected);
            refresh();
        });

        HBox buttons = new HBox(10, postButton, editButton, deleteButton);
        VBox form = new VBox(10, titleField, bodyArea, buttons);
        form.setPadding(new Insets(10));

        VBox wrapper = new VBox(10, announcementListView, form);
        wrapper.setPadding(new Insets(10));
        tab.setContent(wrapper);
        return tab;
    }

    private Tab createMessagesTab() {
        Tab tab = new Tab("Messages");
        tab.setClosable(false);

        TextField studentIdField = new TextField();
        studentIdField.setPromptText("Student ID");
        
        TextArea messageArea = new TextArea();
        messageArea.setPrefRowCount(3);
        Button sendButton = new Button("Send");

        sendButton.setOnAction(event -> {
            String studentId = studentIdField.getText().trim();
            if (studentId.isEmpty() || messageArea.getText().isBlank()) {
                showAlert("Enter student ID and message");
                return;
            }
            
            Optional<Student> student = service.findStudentByStudentId(studentId);
            if (student.isEmpty()) {
                showAlert("Student not found");
                return;
            }
            
            service.sendMessage(admin.getUsername(), student.get().getUsername(), messageArea.getText().trim());
            messageArea.clear();
            refresh();
        });

        VBox form = new VBox(10, studentIdField, messageArea, sendButton);
        form.setPadding(new Insets(10));

        VBox wrapper = new VBox(10, form, messageList);
        wrapper.setPadding(new Insets(10));
        tab.setContent(wrapper);
        return tab;
    }

    private Tab createSearchTab() {
        Tab tab = new Tab("Search");
        tab.setClosable(false);

        GridPane searchGrid = new GridPane();
        searchGrid.setPadding(new Insets(10));
        searchGrid.setHgap(10);
        searchGrid.setVgap(10);

        TextField studentIdField = new TextField();
        Button searchButton = new Button("Search");

        searchGrid.addRow(0, new Label("Student ID"), studentIdField, searchButton);

        GridPane editGrid = new GridPane();
        editGrid.setPadding(new Insets(10));
        editGrid.setHgap(10);
        editGrid.setVgap(10);

        TextField nameField = new TextField();
        nameField.setDisable(true);
        ComboBox<Gender> genderBox = new ComboBox<>(FXCollections.observableArrayList(Gender.values()));
        genderBox.setDisable(true);
        Label collegeLabel = new Label();
        ComboBox<Residency> residencyBox = new ComboBox<>(FXCollections.observableArrayList(Residency.values()));
        residencyBox.setDisable(true);
        TextField cityField = new TextField();
        cityField.setDisable(true);
        TextField subcityField = new TextField();
        subcityField.setDisable(true);
        TextField woredaField = new TextField();
        woredaField.setDisable(true);
        TextField buildingField = new TextField();
        buildingField.setDisable(true);
        Label statusLabel = new Label();
        Label responseHistoryLabel = new Label();
        responseHistoryLabel.setWrapText(true);
        Button saveButton = new Button("Save Changes");
        saveButton.setDisable(true);

        editGrid.addRow(0, new Label("Name"), nameField);
        editGrid.addRow(1, new Label("Gender"), genderBox);
        editGrid.addRow(2, new Label("College"), collegeLabel);
        editGrid.addRow(3, new Label("Residency"), residencyBox);
        editGrid.addRow(4, new Label("City"), cityField);
        editGrid.addRow(5, new Label("Subcity"), subcityField);
        editGrid.addRow(6, new Label("Woreda"), woredaField);
        editGrid.addRow(7, new Label("Building"), buildingField);
        editGrid.addRow(8, new Label("Status"), statusLabel);
        editGrid.addRow(9, new Label("Response History"), responseHistoryLabel);
        editGrid.add(saveButton, 1, 10);

        final Student[] foundStudent = {null};

        searchButton.setOnAction(event -> {
            String id = studentIdField.getText().trim();
            if (id.isBlank()) {
                showAlert("Enter student ID");
                return;
            }
            
            Optional<Student> result = service.findStudentByStudentId(id);
            if (result.isEmpty()) {
                showAlert("Student not found");
                foundStudent[0] = null;
                nameField.clear();
                genderBox.setValue(null);
                residencyBox.setValue(null);
                cityField.clear();
                subcityField.clear();
                woredaField.clear();
                buildingField.clear();
                statusLabel.setText("");
                saveButton.setDisable(true);
                return;
            }
            
            Student s = result.get();
            foundStudent[0] = s;
            
            nameField.setText(s.getDisplayName());
            nameField.setDisable(false);
            genderBox.setValue(s.getGender());
            genderBox.setDisable(false);
            collegeLabel.setText(s.getCollege() != null ? s.getCollege().getFullName() : "-");
            residencyBox.setValue(s.getResidency());
            residencyBox.setDisable(false);
            cityField.setText(safe(s.getCity()));
            cityField.setDisable(false);
            subcityField.setText(safe(s.getSubcity()));
            subcityField.setDisable(false);
            woredaField.setText(safe(s.getWoreda()));
            woredaField.setDisable(false);
            buildingField.setText(s.getAssignedBuilding());
            buildingField.setDisable(false);
            
            Optional<DormApplication> app = service.getApplicationForStudent(s);
            if (app.isPresent()) {
                statusLabel.setText(app.get().getStatus().name());
                String history = app.get().getResponseHistory();
                responseHistoryLabel.setText(history != null && !history.isBlank() ? history.replace(";", "\n") : "-");
            } else {
                statusLabel.setText("No application");
                responseHistoryLabel.setText("-");
            }
            saveButton.setDisable(false);
        });

        saveButton.setOnAction(event -> {
            if (foundStudent[0] == null) return;
            
            Student s = foundStudent[0];
            s.setGender(genderBox.getValue());
            s.setResidency(residencyBox.getValue());
            s.setCity(cityField.getText().trim());
            s.setSubcity(subcityField.getText().trim());
            s.setWoreda(woredaField.getText().trim());
            s.setAssignedBuilding(buildingField.getText().trim());
            
            service.updateStudent(s);
            showAlert("Student updated");
            refresh();
        });

        VBox wrapper = new VBox(10, searchGrid, new Separator(), editGrid);
        wrapper.setPadding(new Insets(10));
        tab.setContent(wrapper);
        return tab;
    }

    private void refresh() {
        applicationTable.setItems(FXCollections.observableArrayList(service.getApplications()));
        announcementListView.setItems(FXCollections.observableArrayList(service.getAnnouncements()));
        messageList.setItems(FXCollections.observableArrayList(
                service.getMessagesForUser(admin.getUsername()).stream()
                        .map(m -> m.getSentAt() + " | " + m.getFromUser() + ": " + m.getContent())
                        .collect(Collectors.toList())
        ));
    }

    private void logout() {
        LoginViewDb loginView = new LoginViewDb(service, stage);
        Scene scene = new Scene(loginView.getRoot(), 1200, 700);
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
