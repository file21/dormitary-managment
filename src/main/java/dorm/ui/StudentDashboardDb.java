package dorm.ui;

import dorm.model.ApplicationStatus;
import dorm.model.DormApplication;
import dorm.model.SponsorshipType;
import dorm.model.Student;
import dorm.service.DatabaseDormService;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Student dashboard UI with two-phase application system.
 * Phase One: Initial application for all students
 * Phase Two: Payment slip (4500 birr) for self-sponsored students only
 */
public class StudentDashboardDb {
    private final DatabaseDormService service;
    private final Student student;
    private final Stage stage;
    private final BorderPane root;
    private final ListView<String> announcementList;
    private final ListView<String> messageList;
    private final Label statusLabel;
    private final Label phaseOneStatusLabel;
    private final Label phaseTwoStatusLabel;

    public StudentDashboardDb(DatabaseDormService service, Student student, Stage stage) {
        this.service = service;
        this.student = student;
        this.stage = stage;
        this.root = new BorderPane();
        this.announcementList = new ListView<>();
        this.messageList = new ListView<>();
        this.statusLabel = new Label();
        this.phaseOneStatusLabel = new Label();
        this.phaseTwoStatusLabel = new Label();
        build();
        refresh();
    }

    public Parent getRoot() {
        return root;
    }

    private void build() {
        // Header with logout button
        Label headerLabel = new Label("Student Dashboard - " + student.getDisplayName());
        headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(event -> logout());
        
        HBox header = new HBox(20, headerLabel, logoutButton);
        header.setPadding(new Insets(10));
        root.setTop(header);

        TabPane tabs = new TabPane();
        tabs.getTabs().add(createAnnouncementTab());
        tabs.getTabs().add(createPhaseOneTab());
        tabs.getTabs().add(createPhaseTwoTab());
        tabs.getTabs().add(createMessagesTab());
        tabs.getTabs().add(createProfileTab());

        root.setCenter(tabs);
    }

    private Tab createAnnouncementTab() {
        Tab tab = new Tab("Announcements");
        tab.setClosable(false);
        tab.setContent(announcementList);
        return tab;
    }

    private Tab createPhaseOneTab() {
        Tab tab = new Tab("Application - Phase One");
        tab.setClosable(false);

        GridPane form = new GridPane();
        form.setPadding(new Insets(15));
        form.setHgap(10);
        form.setVgap(10);

        ComboBox<SponsorshipType> sponsorshipBox = new ComboBox<>(
            FXCollections.observableArrayList(SponsorshipType.values())
        );
        sponsorshipBox.setPromptText("Select Sponsorship Type");
        
        TextField disabilityField = new TextField();
        disabilityField.setPromptText("Leave blank if none");
        
        TextField documentField = new TextField();
        documentField.setEditable(false);
        documentField.setPromptText("Upload ID/Student card image");
        Button chooseDocButton = new Button("Choose Document");

        Button submitButton = new Button("Submit Phase One Application");
        Button deleteButton = new Button("Delete Pending Application");

        form.addRow(0, new Label("Sponsorship Type *"), sponsorshipBox);
        form.addRow(1, new Label("Disability Info"), disabilityField);
        form.addRow(2, new Label("Document (ID/Card)"), new HBox(10, documentField, chooseDocButton));
        form.addRow(3, new Label("Status"), phaseOneStatusLabel);
        form.addRow(4, submitButton, deleteButton);

        // Pre-fill if student already has data
        if (student.getSponsorshipType() != null) {
            sponsorshipBox.setValue(student.getSponsorshipType());
        }
        if (student.getDisabilityInfo() != null) {
            disabilityField.setText(student.getDisabilityInfo());
        }

        chooseDocButton.setOnAction(event -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Document Image");
            chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            File file = chooser.showOpenDialog(root.getScene().getWindow());
            if (file != null) {
                documentField.setText(file.getAbsolutePath());
            }
        });

        submitButton.setOnAction(event -> {
            if (sponsorshipBox.getValue() == null) {
                showAlert("Missing Data", "Please select sponsorship type.");
                return;
            }
            
            try {
                service.submitPhaseOneApplication(
                    student, 
                    sponsorshipBox.getValue(), 
                    disabilityField.getText().trim(),
                    documentField.getText()
                );
                refresh();
                showAlert("Application Submitted", "Your Phase One application has been submitted for review.");
            } catch (Exception e) {
                showAlert("Error", "Failed to submit application: " + e.getMessage());
            }
        });

        deleteButton.setOnAction(event -> {
            try {
                service.deleteApplication(student);
                refresh();
                showAlert("Application Deleted", "Pending application deleted.");
            } catch (Exception e) {
                showAlert("Error", "Failed to delete application: " + e.getMessage());
            }
        });

        VBox info = new VBox(10,
            new Label("Phase One Application"),
            new Label("Submit your initial dormitory application."),
            new Label("Government students: After approval, you will be assigned a building."),
            new Label("Self-sponsored students: After approval, proceed to Phase Two for payment.")
        );
        info.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10;");
        
        VBox wrapper = new VBox(10, info, form);
        wrapper.setPadding(new Insets(10));
        tab.setContent(wrapper);
        return tab;
    }

    private Tab createPhaseTwoTab() {
        Tab tab = new Tab("Application - Phase Two");
        tab.setClosable(false);

        GridPane form = new GridPane();
        form.setPadding(new Insets(15));
        form.setHgap(10);
        form.setVgap(10);

        TextField paymentSlipField = new TextField();
        paymentSlipField.setEditable(false);
        paymentSlipField.setPromptText("Upload payment slip (4500 Birr)");
        Button chooseSlipButton = new Button("Choose Payment Slip");
        Button submitButton = new Button("Submit Phase Two Application");

        form.addRow(0, new Label("Payment Slip *"), new HBox(10, paymentSlipField, chooseSlipButton));
        form.addRow(1, new Label("Status"), phaseTwoStatusLabel);
        form.addRow(2, submitButton);

        chooseSlipButton.setOnAction(event -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Payment Slip Image");
            chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            File file = chooser.showOpenDialog(root.getScene().getWindow());
            if (file != null) {
                paymentSlipField.setText(file.getAbsolutePath());
            }
        });

        submitButton.setOnAction(event -> {
            if (!service.canFillPhaseTwo(student)) {
                showAlert("Not Available", "Phase Two is only for self-sponsored students with approved Phase One.");
                return;
            }
            
            if (paymentSlipField.getText().isBlank()) {
                showAlert("Missing Data", "Please upload your payment slip.");
                return;
            }
            
            try {
                service.submitPhaseTwoApplication(student, paymentSlipField.getText());
                refresh();
                showAlert("Payment Submitted", "Your payment slip has been submitted for verification.");
            } catch (Exception e) {
                showAlert("Error", "Failed to submit: " + e.getMessage());
            }
        });

        VBox info = new VBox(10,
            new Label("Phase Two Application (Self-Sponsored Students Only)"),
            new Label("After Phase One approval, submit your payment slip of 4500 Birr."),
            new Label("This section is locked for government-sponsored students.")
        );
        info.setStyle("-fx-background-color: #fff3cd; -fx-padding: 10;");
        
        VBox wrapper = new VBox(10, info, form);
        wrapper.setPadding(new Insets(10));
        tab.setContent(wrapper);
        return tab;
    }

    private Tab createMessagesTab() {
        Tab tab = new Tab("Messages");
        tab.setClosable(false);

        ComboBox<String> recipientBox = new ComboBox<>();
        recipientBox.setItems(FXCollections.observableArrayList(
                service.getUsersByRole(dorm.model.Role.ADMIN).stream()
                    .map(user -> user.getUsername()).collect(Collectors.toList())
        ));
        recipientBox.setPromptText("Select Admin");

        TextArea messageArea = new TextArea();
        messageArea.setPrefRowCount(3);
        Button sendButton = new Button("Send Message");

        sendButton.setOnAction(event -> {
            if (recipientBox.getValue() == null || messageArea.getText().isBlank()) {
                showAlert("Missing Data", "Select a recipient and enter a message.");
                return;
            }
            try {
                service.sendMessage(student.getUsername(), recipientBox.getValue(), messageArea.getText().trim());
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

    private Tab createProfileTab() {
        Tab tab = new Tab("Profile");
        tab.setClosable(false);

        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        box.getChildren().addAll(
                new Label("Name: " + student.getDisplayName()),
                new Label("Student ID: " + student.getStudentId()),
                new Label("City: " + student.getCity()),
                new Label("Gender: " + (student.getGender() != null ? student.getGender().name() : "-")),
                new Label("Sponsorship: " + (student.getSponsorshipType() != null ? student.getSponsorshipType().name() : "-")),
                new Label("Assigned Building: " + valueOrDash(student.getAssignedBuilding())),
                new Label("Entry Date: " + valueOrDash(student.getEntryDate())),
                new Label("Withdrawal Date: " + valueOrDash(student.getWithdrawalDate()))
        );
        tab.setContent(box);
        return tab;
    }

    private void refresh() {
        try {
            announcementList.setItems(FXCollections.observableArrayList(
                    service.getAnnouncements().stream()
                            .map(announcement -> announcement.getTitle() + " - " + announcement.getBody())
                            .collect(Collectors.toList())
            ));

            messageList.setItems(FXCollections.observableArrayList(
                    service.getMessagesForUser(student.getUsername()).stream()
                            .map(message -> message.getSentAt() + " | " + message.getFromUser() + ": " + message.getContent())
                            .collect(Collectors.toList())
            ));

            Optional<DormApplication> appOpt = service.getApplicationForStudent(student);
            if (appOpt.isPresent()) {
                DormApplication app = appOpt.get();
                String note = app.getAdminNote() != null ? " (Note: " + app.getAdminNote() + ")" : "";
                phaseOneStatusLabel.setText(app.getStatus().name() + note);
                
                // Update phase two status
                if (student.getSponsorshipType() == SponsorshipType.GOVERNMENT) {
                    phaseTwoStatusLabel.setText("Not applicable - Government sponsored");
                } else if (service.canFillPhaseTwo(student)) {
                    phaseTwoStatusLabel.setText("Ready to submit payment slip");
                } else if (app.getStatus() == ApplicationStatus.PHASE_TWO_PENDING) {
                    phaseTwoStatusLabel.setText("Payment slip under review");
                } else if (app.getStatus() == ApplicationStatus.PHASE_TWO_APPROVED) {
                    phaseTwoStatusLabel.setText("Payment verified - awaiting building assignment");
                } else if (app.getStatus() == ApplicationStatus.PHASE_TWO_DECLINED) {
                    phaseTwoStatusLabel.setText("Payment rejected" + note);
                } else {
                    phaseTwoStatusLabel.setText("Complete Phase One first");
                }
            } else {
                phaseOneStatusLabel.setText("No application submitted");
                phaseTwoStatusLabel.setText("Complete Phase One first");
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to refresh data: " + e.getMessage());
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

    private String valueOrDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
