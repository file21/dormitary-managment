package dorm.ui;

import dorm.model.ApplicationStatus;
import dorm.model.DormApplication;
import dorm.model.Student;
import dorm.service.DatabaseDormService;
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
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.stream.Collectors;

/**
 * Student dashboard UI.
 * Demonstrates clear separation between UI and business logic.
 */
public class StudentDashboardDb {
    private final DatabaseDormService service;
    private final Student student;
    private final BorderPane root;
    private final ListView<String> announcementList;
    private final ListView<String> messageList;
    private final Label statusLabel;

    public StudentDashboardDb(DatabaseDormService service, Student student) {
        this.service = service;
        this.student = student;
        this.root = new BorderPane();
        this.announcementList = new ListView<>();
        this.messageList = new ListView<>();
        this.statusLabel = new Label();
        build();
        refresh();
    }

    public Parent getRoot() {
        return root;
    }

    private void build() {
        Label header = new Label("Student Dashboard - " + student.getDisplayName());
        header.setPadding(new Insets(10));
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        root.setTop(header);

        TabPane tabs = new TabPane();
        tabs.getTabs().add(createAnnouncementTab());
        tabs.getTabs().add(createApplicationTab());
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

    private Tab createApplicationTab() {
        Tab tab = new Tab("Application");
        tab.setClosable(false);

        GridPane form = new GridPane();
        form.setPadding(new Insets(15));
        form.setHgap(10);
        form.setVgap(10);

        TextField sponsorshipField = new TextField();
        TextField disabilityField = new TextField();
        TextField documentField = new TextField();
        documentField.setEditable(false);
        Button chooseDocButton = new Button("Choose Document");
        TextField paymentSlipField = new TextField();
        paymentSlipField.setEditable(false);
        Button chooseSlipButton = new Button("Choose Payment Slip");

        Button submitButton = new Button("Submit Application");
        Button deleteButton = new Button("Delete Unreviewed Application");

        form.addRow(0, new Label("Sponsorship Type"), sponsorshipField);
        form.addRow(1, new Label("Disability Info"), disabilityField);
        form.addRow(2, new Label("Document"), new HBox(10, documentField, chooseDocButton));
        form.addRow(3, new Label("Payment Slip (self-sponsored)"), new HBox(10, paymentSlipField, chooseSlipButton));
        form.addRow(4, new Label("Status"), statusLabel);
        form.addRow(5, submitButton, deleteButton);

        chooseDocButton.setOnAction(event -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Document Image");
            File file = chooser.showOpenDialog(root.getScene().getWindow());
            if (file != null) {
                documentField.setText(file.getAbsolutePath());
            }
        });

        chooseSlipButton.setOnAction(event -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Payment Slip Image");
            File file = chooser.showOpenDialog(root.getScene().getWindow());
            if (file != null) {
                paymentSlipField.setText(file.getAbsolutePath());
            }
        });

        submitButton.setOnAction(event -> {
            if (sponsorshipField.getText().isBlank()) {
                showAlert("Missing Data", "Sponsorship type is required.");
                return;
            }
            
            try {
                DormApplication application = service.getApplicationForStudent(student).orElseGet(() ->
                        service.submitApplication(student, sponsorshipField.getText().trim(), disabilityField.getText().trim()));

                if (!documentField.getText().isBlank()) {
                    student.addDocumentPath(documentField.getText());
                }
                if (!paymentSlipField.getText().isBlank()) {
                    student.setPaymentSlipPath(paymentSlipField.getText());
                }
                if (application.getStatus() == ApplicationStatus.RESUBMIT) {
                    service.updateApplication(application, ApplicationStatus.NOT_SEEN, null);
                }
                refresh();
                showAlert("Application Submitted", "Your application has been submitted.");
            } catch (Exception e) {
                showAlert("Error", "Failed to submit application: " + e.getMessage());
            }
        });

        deleteButton.setOnAction(event -> {
            try {
                service.deleteApplication(student);
                refresh();
                showAlert("Application Deleted", "Unreviewed application deleted.");
            } catch (Exception e) {
                showAlert("Error", "Failed to delete application: " + e.getMessage());
            }
        });

        VBox wrapper = new VBox(10, form);
        wrapper.setPadding(new Insets(10));
        tab.setContent(wrapper);
        return tab;
    }

    private Tab createMessagesTab() {
        Tab tab = new Tab("Messages");
        tab.setClosable(false);

        ComboBox<String> recipientBox = new ComboBox<>();
        recipientBox.setItems(FXCollections.observableArrayList(
                service.getUsersByRole(dorm.model.Role.ADMIN).stream().map(user -> user.getUsername()).collect(Collectors.toList())
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
                new Label("Student ID: " + student.getStudentId()),
                new Label("City: " + student.getCity()),
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

            String statusText = service.getApplicationForStudent(student)
                    .map(application -> application.getStatus().name() + statusNote(application))
                    .orElse("No application submitted");
            statusLabel.setText(statusText);
        } catch (Exception e) {
            showAlert("Error", "Failed to refresh data: " + e.getMessage());
        }
    }

    private String statusNote(DormApplication application) {
        if (application.getAdminNote() == null || application.getAdminNote().isBlank()) {
            return "";
        }
        return " (Note: " + application.getAdminNote() + ")";
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
