package edu.aau.dorm.ui.controller;

import edu.aau.dorm.model.ApplicationStatus;
import edu.aau.dorm.model.DormApplication;
import edu.aau.dorm.service.AdminService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * AdminDashboardController: UI Controller for Admin Dashboard
 * 
 * Responsibilities (SRP):
 * - Handle user interactions on admin dashboard
 * - Display applications requiring review
 * - Update application status (approve/reject)
 * - Assign dormitories to students
 * - Send publications/news to students
 * 
 * Architecture:
 * - MVVM Pattern: Controller handles UI, delegates business logic to Service
 * - Separation of Concerns: No business logic in controller (SRP)
 * - Dependency Injection: AdminService injected for testability
 * 
 * OOP Principles Applied:
 * - Encapsulation: Private fields with accessor methods
 * - Abstraction: Service layer abstracts business logic
 * - Single Responsibility: Only UI concerns
 */
public class AdminDashboardController {

    // ========================================================================
    // UI Components (FXML Injected)
    // ========================================================================

    // Application Review Tab
    @FXML private Tab applicationsTab;
    @FXML private TableView<DormApplication> applicationsTable;
    @FXML private TableColumn<DormApplication, String> studentNameColumn;
    @FXML private TableColumn<DormApplication, String> statusColumn;
    @FXML private TableColumn<DormApplication, String> submittedDateColumn;
    @FXML private TableColumn<DormApplication, String> scoreColumn;
    @FXML private Button reviewButton;
    @FXML private Button refreshApplicationsButton;
    @FXML private ComboBox<ApplicationStatus> statusFilterCombo;

    // Application Details Panel
    @FXML private VBox applicationDetailsPanel;
    @FXML private Label applicationIdLabel;
    @FXML private Label studentNameLabel;
    @FXML private Label departmentLabel;
    @FXML private Label sponsorshipLabel;
    @FXML private TextArea applicantNotesArea;
    @FXML private ComboBox<ApplicationStatus> decisionCombo;
    @FXML private TextArea reviewCommentArea;
    @FXML private Button approveButton;
    @FXML private Button rejectButton;
    @FXML private Button requestRevisionButton;

    // Dormitory Assignment Tab
    @FXML private Tab assignmentTab;
    @FXML private TableView<DormApplication> approvedStudentsTable;
    @FXML private TableColumn<DormApplication, String> approvedStudentColumn;
    @FXML private TableColumn<DormApplication, String> campusPreferenceColumn;
    @FXML private ComboBox<String> dormitoryCombo;
    @FXML private ComboBox<String> bedCombo;
    @FXML private TextField roomNumberField;
    @FXML private Button assignButton;
    @FXML private Label assignmentStatusLabel;

    // News Publication Tab
    @FXML private Tab newsTab;
    @FXML private TextField publicationTitleField;
    @FXML private TextArea publicationMessageArea;
    @FXML private Button publishButton;
    @FXML private Label publicationStatusLabel;
    @FXML private TableView<String> publicationHistoryTable;
    @FXML private TableColumn<String, String> historyColumn;

    // Status/Feedback
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator loadingIndicator;

    // ========================================================================
    // Service Layer (Business Logic)
    // ========================================================================

    private final AdminService adminService;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // Observable data for tables
    private final ObservableList<DormApplication> applicationsData = FXCollections.observableArrayList();
    private final ObservableList<DormApplication> approvedStudentsData = FXCollections.observableArrayList();
    private DormApplication selectedApplication;

    // ========================================================================
    // Constructors
    // ========================================================================

    /**
     * Default constructor: Creates AdminService with default DAO implementations
     * Called by FXMLLoader
     */
    public AdminDashboardController() {
        this(new AdminService());
    }

    /**
     * Dependency injection constructor: Allows custom AdminService
     * Used for testing with mock services
     * 
     * @param adminService AdminService implementation (cannot be null)
     * @throws NullPointerException if adminService is null
     */
    public AdminDashboardController(AdminService adminService) {
        this.adminService = Objects.requireNonNull(adminService, "adminService cannot be null");
    }

    // ========================================================================
    // FXML Initialization
    // ========================================================================

    /**
     * Called by FXMLLoader after FXML components are injected
     * Initializes UI components, event handlers, and loads initial data
     */
    @FXML
    public void initialize() {
        try {
            initializeApplicationsTab();
            initializeAssignmentTab();
            initializeNewsTab();
            loadPendingApplications();
            updateStatus("Dashboard loaded successfully");
        } catch (Exception e) {
            updateStatus("Error initializing dashboard: " + e.getMessage());
            showError("Initialization Error", e.getMessage());
        }
    }

    // ========================================================================
    // Applications Tab Initialization & Handlers
    // ========================================================================

    /**
     * Initialize Applications Review Tab
     * Sets up table columns, filters, and event handlers
     */
    private void initializeApplicationsTab() {
        // Configure table columns
        studentNameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().studentUserId() + "")
        );
        statusColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().status().toString())
        );
        submittedDateColumn.setCellValueFactory(cellData -> {
            var submitted = cellData.getValue().submittedAt();
            return new SimpleStringProperty(submitted != null ? submitted.toString() : "N/A");
        });
        scoreColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().score() + "")
        );

        // Set table data
        applicationsTable.setItems(applicationsData);

        // Configure status filter dropdown
        statusFilterCombo.setItems(FXCollections.observableArrayList(ApplicationStatus.values()));
        statusFilterCombo.setOnAction(e -> filterApplicationsByStatus());

        // Table row selection handler
        applicationsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                displayApplicationDetails(newVal);
            }
        });

        // Button handlers
        reviewButton.setOnAction(e -> onReviewClick());
        refreshApplicationsButton.setOnAction(e -> loadPendingApplications());
        approveButton.setOnAction(e -> onApproveClick());
        rejectButton.setOnAction(e -> onRejectClick());
        requestRevisionButton.setOnAction(e -> onRequestRevisionClick());
    }

    /**
     * Display application details in right panel
     * 
     * @param application Application to display
     */
    private void displayApplicationDetails(DormApplication application) {
        selectedApplication = application;
        applicationIdLabel.setText("ID: " + application.id());
        studentNameLabel.setText("Student: " + application.studentUserId());
        departmentLabel.setText("Department: " + (application.department() != null ? application.department() : "N/A"));
        sponsorshipLabel.setText("Sponsorship: " + application.sponsorshipType());
        applicantNotesArea.setText(application.notes() != null ? application.notes() : "");
        decisionCombo.setValue(application.status());
    }

    /**
     * Filter applications by selected status
     */
    private void filterApplicationsByStatus() {
        ApplicationStatus selectedStatus = statusFilterCombo.getValue();
        if (selectedStatus == null) {
            loadPendingApplications();
            return;
        }
        
        try {
            List<DormApplication> filtered = adminService.getApplicationsByStatus(selectedStatus);
            applicationsData.setAll(filtered);
            updateStatus("Showing " + filtered.size() + " " + selectedStatus + " applications");
        } catch (Exception e) {
            updateStatus("Error filtering applications: " + e.getMessage());
            showError("Filter Error", e.getMessage());
        }
    }

    /**
     * Load pending applications from service
     */
    private void loadPendingApplications() {
        try {
            setLoading(true);
            List<DormApplication> pending = adminService.getPendingApplications();
            applicationsData.setAll(pending);
            updateStatus("Loaded " + pending.size() + " pending applications");
        } catch (Exception e) {
            updateStatus("Error loading applications: " + e.getMessage());
            showError("Load Error", e.getMessage());
        } finally {
            setLoading(false);
        }
    }

    /**
     * Handler: Review button clicked
     */
    @FXML
    private void onReviewClick() {
        DormApplication selected = applicationsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No Application Selected", "Please select an application to review");
            return;
        }
        displayApplicationDetails(selected);
    }

    /**
     * Handler: Approve button clicked
     * Updates application status to ACCEPTED
     */
    @FXML
    private void onApproveClick() {
        if (selectedApplication == null) {
            showWarning("No Application Selected", "Please select an application first");
            return;
        }

        String comment = reviewCommentArea.getText().trim();
        if (comment.isEmpty()) {
            comment = "Application approved by admin";
        }

        try {
            adminService.reviewApplication(
                    selectedApplication.id(),
                    1, // TODO: Get actual admin user ID from session
                    ApplicationStatus.ACCEPTED,
                    comment
            );
            updateStatus("Application approved successfully");
            loadPendingApplications();
            clearApplicationDetails();
        } catch (Exception e) {
            updateStatus("Error approving application: " + e.getMessage());
            showError("Approval Failed", e.getMessage());
        }
    }

    /**
     * Handler: Reject button clicked
     * Updates application status to REJECTED
     */
    @FXML
    private void onRejectClick() {
        if (selectedApplication == null) {
            showWarning("No Application Selected", "Please select an application first");
            return;
        }

        String reason = reviewCommentArea.getText().trim();
        if (reason.isEmpty()) {
            showWarning("Missing Comment", "Please provide a reason for rejection");
            return;
        }

        try {
            adminService.reviewApplication(
                    selectedApplication.id(),
                    1, // TODO: Get actual admin user ID from session
                    ApplicationStatus.REJECTED,
                    reason
            );
            updateStatus("Application rejected successfully");
            loadPendingApplications();
            clearApplicationDetails();
        } catch (Exception e) {
            updateStatus("Error rejecting application: " + e.getMessage());
            showError("Rejection Failed", e.getMessage());
        }
    }

    /**
     * Handler: Request Revision button clicked
     * Updates application status to NEEDS_EDIT
     */
    @FXML
    private void onRequestRevisionClick() {
        if (selectedApplication == null) {
            showWarning("No Application Selected", "Please select an application first");
            return;
        }

        String instructions = reviewCommentArea.getText().trim();
        if (instructions.isEmpty()) {
            showWarning("Missing Instructions", "Please provide revision instructions");
            return;
        }

        try {
            adminService.reviewApplication(
                    selectedApplication.id(),
                    1, // TODO: Get actual admin user ID from session
                    ApplicationStatus.NEEDS_EDIT,
                    instructions
            );
            updateStatus("Revision request sent to student");
            loadPendingApplications();
            clearApplicationDetails();
        } catch (Exception e) {
            updateStatus("Error requesting revision: " + e.getMessage());
            showError("Request Failed", e.getMessage());
        }
    }

    /**
     * Clear application details panel
     */
    private void clearApplicationDetails() {
        selectedApplication = null;
        applicationIdLabel.setText("ID: -");
        studentNameLabel.setText("Student: -");
        departmentLabel.setText("Department: -");
        sponsorshipLabel.setText("Sponsorship: -");
        applicantNotesArea.clear();
        reviewCommentArea.clear();
    }

    // ========================================================================
    // Assignment Tab Initialization & Handlers
    // ========================================================================

    /**
     * Initialize Dormitory Assignment Tab
     * Sets up approved students table and assignment controls
     */
    private void initializeAssignmentTab() {
        approvedStudentColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty("Student " + cellData.getValue().studentUserId())
        );
        campusPreferenceColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().campusPreference() != null ? cellData.getValue().campusPreference() : "N/A")
        );

        approvedStudentsTable.setItems(approvedStudentsData);
        assignButton.setOnAction(e -> onAssignClick());

        loadApprovedStudents();
    }

    /**
     * Load approved students requiring assignment
     */
    private void loadApprovedStudents() {
        try {
            List<DormApplication> approved = adminService.getApplicationsByStatus(ApplicationStatus.ACCEPTED);
            approvedStudentsData.setAll(approved);
            updateStatus("Loaded " + approved.size() + " approved students");
        } catch (Exception e) {
            updateStatus("Error loading approved students: " + e.getMessage());
            showError("Load Error", e.getMessage());
        }
    }

    /**
     * Handler: Assign button clicked
     * Assigns selected student to bed/dormitory
     */
    @FXML
    private void onAssignClick() {
        DormApplication selected = approvedStudentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No Student Selected", "Please select a student to assign");
            return;
        }

        String dormitory = dormitoryCombo.getValue();
        String bed = bedCombo.getValue();
        String roomNumber = roomNumberField.getText().trim();

        if (dormitory == null || dormitory.isEmpty() || bed == null || bed.isEmpty() || roomNumber.isEmpty()) {
            showWarning("Incomplete Assignment", "Please fill in all assignment fields");
            return;
        }

        try {
            adminService.assignDormitory(
                    selected.id(),
                    1, // TODO: Get actual bed ID from database
                    1, // TODO: Get actual admin user ID from session
                    roomNumber
            );
            assignmentStatusLabel.setText("Assigned successfully");
            loadApprovedStudents();
            clearAssignmentFields();
        } catch (Exception e) {
            assignmentStatusLabel.setText("Assignment failed: " + e.getMessage());
            showError("Assignment Failed", e.getMessage());
        }
    }

    /**
     * Clear assignment form fields
     */
    private void clearAssignmentFields() {
        dormitoryCombo.setValue(null);
        bedCombo.setValue(null);
        roomNumberField.clear();
    }

    // ========================================================================
    // News Publication Tab Initialization & Handlers
    // ========================================================================

    /**
     * Initialize News Publication Tab
     * Sets up publication form and history table
     */
    private void initializeNewsTab() {
        historyColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()));
        publishButton.setOnAction(e -> onPublishClick());
    }

    /**
     * Handler: Publish button clicked
     * Sends news publication to all students
     */
    @FXML
    private void onPublishClick() {
        String title = publicationTitleField.getText().trim();
        String message = publicationMessageArea.getText().trim();

        if (title.isEmpty() || message.isEmpty()) {
            showWarning("Missing Fields", "Please enter both title and message");
            return;
        }

        try {
            adminService.publishNews(
                    1, // TODO: Get actual admin user ID from session
                    title,
                    message
            );
            publicationStatusLabel.setText("Publication sent successfully to all students");
            publicationTitleField.clear();
            publicationMessageArea.clear();
        } catch (Exception e) {
            publicationStatusLabel.setText("Publication failed: " + e.getMessage());
            showError("Publication Failed", e.getMessage());
        }
    }

    // ========================================================================
    // UI Utility Methods
    // ========================================================================

    /**
     * Update status label and log message
     * 
     * @param message Status message to display
     */
    private void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
        System.out.println("[Admin] " + message);
    }

    /**
     * Show error dialog to user
     * 
     * @param title Dialog title
     * @param message Error message
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show warning dialog to user
     * 
     * @param title Dialog title
     * @param message Warning message
     */
    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show/hide loading indicator
     * 
     * @param loading true to show, false to hide
     */
    private void setLoading(boolean loading) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(loading);
        }
    }
}
