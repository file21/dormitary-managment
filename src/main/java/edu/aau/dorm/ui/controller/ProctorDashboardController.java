package edu.aau.dorm.ui.controller;

import edu.aau.dorm.model.ApplicationStatus;
import edu.aau.dorm.model.DormApplication;
import edu.aau.dorm.service.ProctorManagementService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * ProctorDashboardController: UI Controller for Proctor Dashboard
 * 
 * Responsibilities (SRP):
 * - Display assigned dormitory/block information
 * - Manage student check-in process
 * - Handle student withdrawals
 * - Record document verification
 * - Update dormitory capacity
 * - Monitor occupancy in real-time
 * 
 * Architecture:
 * - MVVM Pattern: Controller delegates business logic to ProctorManagementService
 * - UI Components separated by functional tabs (check-in, withdrawal, capacity)
 * - Observable data for real-time updates
 * 
 * OOP Principles:
 * - Encapsulation: Private fields with controlled access
 * - Abstraction: Service layer handles complexity
 * - Single Responsibility: UI concerns only, no business logic
 */
public class ProctorDashboardController {

    // ========================================================================
    // UI Components - Check-in Tab
    // ========================================================================

    @FXML private Tab checkInTab;
    @FXML private TableView<DormApplication> acceptedStudentsTable;
    @FXML private TableColumn<DormApplication, String> studentNameColumn;
    @FXML private TableColumn<DormApplication, String> departmentColumn;
    @FXML private TableColumn<DormApplication, String> statusColumn;

    @FXML private VBox studentDetailsPanel;
    @FXML private Label studentNameLabel;
    @FXML private Label aauIdLabel;
    @FXML private Label departmentLabel;
    @FXML private Label genderLabel;
    @FXML private TextArea studentNotesArea;

    @FXML private ComboBox<String> bedSelectionCombo;
    @FXML private TextField roomNumberField;
    @FXML private Button verifyDocumentsButton;
    @FXML private Button checkInButton;
    @FXML private Label checkInStatusLabel;

    // ========================================================================
    // UI Components - Withdrawal Tab
    // ========================================================================

    @FXML private Tab withdrawalTab;
    @FXML private TableView<DormApplication> checkedInStudentsTable;
    @FXML private TableColumn<DormApplication, String> checkedInStudentColumn;
    @FXML private TableColumn<DormApplication, String> roomColumn;
    @FXML private TableColumn<DormApplication, String> checkInDateColumn;

    @FXML private TextArea withdrawalReasonArea;
    @FXML private Button withdrawButton;
    @FXML private Label withdrawalStatusLabel;

    // ========================================================================
    // UI Components - Capacity Management Tab
    // ========================================================================

    @FXML private Tab capacityTab;
    @FXML private Label blockNameLabel;
    @FXML private Label currentCapacityLabel;
    @FXML private Label availableBedsLabel;
    @FXML private Label occupancyPercentageLabel;

    @FXML private Spinner<Integer> totalBedsSpinner;
    @FXML private Spinner<Integer> maleBedsSpinner;
    @FXML private Spinner<Integer> femaleBedsSpinner;
    @FXML private Button updateCapacityButton;
    @FXML private Label capacityStatusLabel;

    // ========================================================================
    // UI Components - Occupancy Report Tab
    // ========================================================================

    @FXML private Tab reportTab;
    @FXML private Label totalAllocatedLabel;
    @FXML private Label totalCheckedInLabel;
    @FXML private Label totalWithdrawLabel;
    @FXML private TableView<String> occupancyDetailsTable;
    @FXML private TableColumn<String, String> reportColumn;
    @FXML private Button refreshReportButton;

    // Status/Feedback
    @FXML private Label generalStatusLabel;
    @FXML private ProgressIndicator loadingIndicator;

    // ========================================================================
    // Service Layer
    // ========================================================================

    private final ProctorManagementService proctorService;

    // Observable data for UI
    private final ObservableList<DormApplication> acceptedStudentsData = FXCollections.observableArrayList();
    private final ObservableList<DormApplication> checkedInStudentsData = FXCollections.observableArrayList();
    private final ObservableList<String> reportData = FXCollections.observableArrayList();

    private DormApplication selectedAcceptedStudent;
    private DormApplication selectedCheckedInStudent;

    // Proctor context
    private long proctorUserId = 1; // TODO: Get from session
    private long assignedBlockId = 1; // TODO: Get from session/database

    // ========================================================================
    // Constructors
    // ========================================================================

    /**
     * Default constructor: Creates ProctorManagementService with default DAOs
     * Called by FXMLLoader
     */
    public ProctorDashboardController() {
        this(new ProctorManagementService());
    }

    /**
     * Constructor with dependency injection
     * Allows for testing with mock services
     * 
     * @param proctorService ProctorManagementService (cannot be null)
     * @throws NullPointerException if proctorService is null
     */
    public ProctorDashboardController(ProctorManagementService proctorService) {
        this.proctorService = Objects.requireNonNull(proctorService, "proctorService cannot be null");
    }

    // ========================================================================
    // FXML Initialization
    // ========================================================================

    /**
     * Called by FXMLLoader after FXML injection
     * Initializes UI components, event handlers, and loads initial data
     */
    @FXML
    public void initialize() {
        try {
            initializeCheckInTab();
            initializeWithdrawalTab();
            initializeCapacityTab();
            initializeReportTab();
            loadAcceptedStudents();
            loadCheckedInStudents();
            loadCapacityInfo();
            updateStatus("Proctor dashboard initialized");
        } catch (Exception e) {
            updateStatus("Error initializing dashboard: " + e.getMessage());
            showError("Initialization Error", e.getMessage());
        }
    }

    // ========================================================================
    // Check-in Tab
    // ========================================================================

    /**
     * Initialize Check-in Tab UI components
     */
    private void initializeCheckInTab() {
        // Configure table columns
        studentNameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty("Student " + cellData.getValue().studentUserId())
        );
        departmentColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().department() != null ? cellData.getValue().department() : "N/A")
        );
        statusColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().status().toString())
        );

        acceptedStudentsTable.setItems(acceptedStudentsData);

        // Table selection handler
        acceptedStudentsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                displayStudentDetails(newVal);
            }
        });

        // Button handlers
        verifyDocumentsButton.setOnAction(e -> onVerifyDocuments());
        checkInButton.setOnAction(e -> onCheckIn());
    }

    /**
     * Load students with ACCEPTED status (ready for check-in)
     */
    private void loadAcceptedStudents() {
        try {
            setLoading(true);
            List<DormApplication> accepted = proctorService.getAllocatedStudents(assignedBlockId);
            acceptedStudentsData.setAll(
                    accepted.stream()
                            .filter(app -> app.status() == ApplicationStatus.ACCEPTED)
                            .toList()
            );
            updateStatus("Loaded " + acceptedStudentsData.size() + " students ready for check-in");
        } catch (Exception e) {
            updateStatus("Error loading students: " + e.getMessage());
            showError("Load Error", e.getMessage());
        } finally {
            setLoading(false);
        }
    }

    /**
     * Display selected student details
     * 
     * @param application Student application to display
     */
    private void displayStudentDetails(DormApplication application) {
        selectedAcceptedStudent = application;
        studentNameLabel.setText("Student ID: " + application.studentUserId());
        aauIdLabel.setText("AAU ID: N/A"); // TODO: Fetch from StudentProfile
        departmentLabel.setText("Department: " + (application.department() != null ? application.department() : "N/A"));
        genderLabel.setText("Gender: N/A"); // TODO: Fetch from StudentProfile
        studentNotesArea.setText(application.notes() != null ? application.notes() : "No notes");
    }

    /**
     * Handler: Verify Documents button clicked
     * Records document verification for student
     */
    @FXML
    private void onVerifyDocuments() {
        if (selectedAcceptedStudent == null) {
            showWarning("No Student Selected", "Please select a student first");
            return;
        }

        try {
            String notes = studentNotesArea.getText();
            proctorService.verifyDocuments(
                    selectedAcceptedStudent.id(),
                    proctorUserId,
                    notes
            );
            checkInStatusLabel.setText("Documents verified for student " + selectedAcceptedStudent.studentUserId());
            showInformation("Success", "Documents verified successfully");
        } catch (Exception e) {
            checkInStatusLabel.setText("Verification failed: " + e.getMessage());
            showError("Verification Failed", e.getMessage());
        }
    }

    /**
     * Handler: Check In button clicked
     * Completes check-in process for student
     */
    @FXML
    private void onCheckIn() {
        if (selectedAcceptedStudent == null) {
            showWarning("No Student Selected", "Please select a student first");
            return;
        }

        String roomNumber = roomNumberField.getText().trim();
        if (roomNumber.isEmpty()) {
            showWarning("Missing Room Number", "Please enter room number");
            return;
        }

        try {
            // Get selected bed (TODO: implement proper bed selection)
            long bedId = 1; // Placeholder
            
            proctorService.checkInStudent(
                    selectedAcceptedStudent.id(),
                    bedId,
                    proctorUserId,
                    roomNumber,
                    Instant.now()
            );

            checkInStatusLabel.setText("Student checked in to " + roomNumber);
            loadAcceptedStudents();
            loadCheckedInStudents();
            clearCheckInFields();
            showInformation("Success", "Check-in completed successfully");
        } catch (Exception e) {
            checkInStatusLabel.setText("Check-in failed: " + e.getMessage());
            showError("Check-in Failed", e.getMessage());
        }
    }

    /**
     * Clear check-in form fields
     */
    private void clearCheckInFields() {
        selectedAcceptedStudent = null;
        studentNameLabel.setText("Student: -");
        aauIdLabel.setText("AAU ID: -");
        departmentLabel.setText("Department: -");
        genderLabel.setText("Gender: -");
        studentNotesArea.clear();
        roomNumberField.clear();
        bedSelectionCombo.setValue(null);
    }

    // ========================================================================
    // Withdrawal Tab
    // ========================================================================

    /**
     * Initialize Withdrawal Tab UI components
     */
    private void initializeWithdrawalTab() {
        // Configure table columns
        checkedInStudentColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty("Student " + cellData.getValue().studentUserId())
        );
        roomColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty("N/A") // TODO: Get from allocation
        );
        checkInDateColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty("N/A") // TODO: Get from allocation
        );

        checkedInStudentsTable.setItems(checkedInStudentsData);

        // Table selection handler
        checkedInStudentsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedCheckedInStudent = newVal;
                withdrawalReasonArea.clear();
            }
        });

        withdrawButton.setOnAction(e -> onWithdraw());
    }

    /**
     * Load students with CHECKED_IN status (can be withdrawn)
     */
    private void loadCheckedInStudents() {
        try {
            List<DormApplication> allocated = proctorService.getAllocatedStudents(assignedBlockId);
            checkedInStudentsData.setAll(
                    allocated.stream()
                            .filter(app -> app.status() == ApplicationStatus.CHECKED_IN)
                            .toList()
            );
            updateStatus("Loaded " + checkedInStudentsData.size() + " checked-in students");
        } catch (Exception e) {
            updateStatus("Error loading checked-in students: " + e.getMessage());
            showError("Load Error", e.getMessage());
        }
    }

    /**
     * Handler: Withdraw button clicked
     * Withdraws selected student from dormitory
     */
    @FXML
    private void onWithdraw() {
        if (selectedCheckedInStudent == null) {
            showWarning("No Student Selected", "Please select a student to withdraw");
            return;
        }

        String reason = withdrawalReasonArea.getText().trim();
        if (reason.isEmpty()) {
            showWarning("Missing Reason", "Please provide withdrawal reason");
            return;
        }

        try {
            proctorService.withdrawStudent(
                    selectedCheckedInStudent.id(),
                    proctorUserId,
                    reason,
                    Instant.now()
            );

            withdrawalStatusLabel.setText("Student withdrawn successfully");
            loadCheckedInStudents();
            loadCapacityInfo();
            selectedCheckedInStudent = null;
            withdrawalReasonArea.clear();
            showInformation("Success", "Withdrawal processed");
        } catch (Exception e) {
            withdrawalStatusLabel.setText("Withdrawal failed: " + e.getMessage());
            showError("Withdrawal Failed", e.getMessage());
        }
    }

    // ========================================================================
    // Capacity Management Tab
    // ========================================================================

    /**
     * Initialize Capacity Management Tab
     */
    private void initializeCapacityTab() {
        // Configure spinners
        totalBedsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1000, 50));
        maleBedsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 500, 25));
        femaleBedsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 500, 25));

        updateCapacityButton.setOnAction(e -> onUpdateCapacity());
    }

    /**
     * Load and display current capacity information
     */
    private void loadCapacityInfo() {
        try {
            blockNameLabel.setText("Block: " + assignedBlockId); // TODO: Get actual block name
            int available = proctorService.getAvailableBeds(assignedBlockId);
            availableBedsLabel.setText("Available Beds: " + available);
            
            // Calculate occupancy percentage (TODO: from database)
            int total = totalBedsSpinner.getValue();
            int occupied = total - available;
            double percentage = total > 0 ? (occupied * 100.0) / total : 0;
            occupancyPercentageLabel.setText(String.format("Occupancy: %.1f%%", percentage));
        } catch (Exception e) {
            updateStatus("Error loading capacity: " + e.getMessage());
        }
    }

    /**
     * Handler: Update Capacity button clicked
     * Updates dormitory bed capacity
     */
    @FXML
    private void onUpdateCapacity() {
        int total = totalBedsSpinner.getValue();
        int male = maleBedsSpinner.getValue();
        int female = femaleBedsSpinner.getValue();

        if (male + female != total) {
            showWarning("Invalid Capacity", "Male + Female beds must equal total beds");
            return;
        }

        try {
            proctorService.updateBlockCapacity(
                    assignedBlockId,
                    proctorUserId,
                    total,
                    male,
                    female
            );

            capacityStatusLabel.setText("Capacity updated: Total=" + total + ", Male=" + male + ", Female=" + female);
            loadCapacityInfo();
            showInformation("Success", "Capacity updated successfully");
        } catch (Exception e) {
            capacityStatusLabel.setText("Update failed: " + e.getMessage());
            showError("Update Failed", e.getMessage());
        }
    }

    // ========================================================================
    // Report Tab
    // ========================================================================

    /**
     * Initialize Report Tab
     */
    private void initializeReportTab() {
        reportColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()));
        occupancyDetailsTable.setItems(reportData);
        refreshReportButton.setOnAction(e -> loadOccupancyReport());
    }

    /**
     * Load and display occupancy report
     */
    private void loadOccupancyReport() {
        try {
            setLoading(true);
            List<DormApplication> allocated = proctorService.getAllocatedStudents(assignedBlockId);
            
            long totalAllocated = allocated.size();
            long totalCheckedIn = allocated.stream()
                    .filter(app -> app.status() == ApplicationStatus.CHECKED_IN)
                    .count();
            long totalWithdrawn = allocated.stream()
                    .filter(app -> app.status() == ApplicationStatus.WITHDREW)
                    .count();

            totalAllocatedLabel.setText("Total Allocated: " + totalAllocated);
            totalCheckedInLabel.setText("Checked In: " + totalCheckedIn);
            totalWithdrawLabel.setText("Withdrawn: " + totalWithdrawn);

            reportData.setAll(
                    "Total Allocated: " + totalAllocated,
                    "Checked In: " + totalCheckedIn,
                    "Withdrawn: " + totalWithdrawn,
                    "Active: " + (totalAllocated - totalWithdrawn)
            );
        } catch (Exception e) {
            updateStatus("Error loading report: " + e.getMessage());
            showError("Report Error", e.getMessage());
        } finally {
            setLoading(false);
        }
    }

    // ========================================================================
    // UI Utility Methods
    // ========================================================================

    /**
     * Update status label and log
     * 
     * @param message Status message
     */
    private void updateStatus(String message) {
        if (generalStatusLabel != null) {
            generalStatusLabel.setText(message);
        }
        System.out.println("[Proctor] " + message);
    }

    /**
     * Show error dialog
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
     * Show warning dialog
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
     * Show information dialog
     * 
     * @param title Dialog title
     * @param message Information message
     */
    private void showInformation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
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
