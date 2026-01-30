package edu.aau.dorm.ui.controller;

import edu.aau.dorm.model.ApplicationStatus;
import edu.aau.dorm.model.DormApplication;
import edu.aau.dorm.service.StudentHousingService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.Objects;

/**
 * StudentDashboardController: UI Controller for Student Housing Dashboard
 * 
 * Responsibilities (SRP):
 * - Display available dormitory housing options
 * - Show occupancy and bed availability
 * - Allow students to create/manage housing applications
 * - Display application status and timeline
 * - Handle application submission
 * 
 * Architecture:
 * - MVVM: Controller delegates business logic to StudentHousingService
 * - Separation of Concerns: UI only, no business logic
 * - Observable data for real-time updates
 * 
 * OOP Principles:
 * - Encapsulation: Private fields with controlled access
 * - Abstraction: Service handles complexity
 * - Single Responsibility: UI concerns only
 */
public class StudentDashboardController {

    // ========================================================================
    // UI Components - Available Housing Tab
    // ========================================================================

    @FXML private Tab availableHousingTab;
    @FXML private TableView<HousingOption> housingTable;
    @FXML private TableColumn<HousingOption, String> blockNameColumn;
    @FXML private TableColumn<HousingOption, String> campusColumn;
    @FXML private TableColumn<HousingOption, String> totalBedsColumn;
    @FXML private TableColumn<HousingOption, String> availableBedsColumn;
    @FXML private TableColumn<HousingOption, String> occupancyColumn;

    @FXML private ComboBox<String> campusFilterCombo;
    @FXML private ComboBox<String> genderFilterCombo;
    @FXML private Slider occupancySlider;
    @FXML private Button filterButton;
    @FXML private Button refreshHousingButton;

    @FXML private VBox housingDetailsPanel;
    @FXML private Label selectedBlockLabel;
    @FXML private Label selectedCampusLabel;
    @FXML private Label selectedCapacityLabel;
    @FXML private Label selectedOccupancyLabel;

    // ========================================================================
    // UI Components - Application Tab
    // ========================================================================

    @FXML private Tab applicationTab;
    @FXML private Label applicationStatusLabel;
    @FXML private Label applicationIdLabel;
    @FXML private Label submissionStatusLabel;

    // Application Form
    @FXML private ComboBox<String> campusPreferenceCombo;
    @FXML private ComboBox<String> sponsorshipCombo;
    @FXML private CheckBox disabilityCheckBox;
    @FXML private Spinner<Double> distanceSpinner;
    @FXML private TextArea notesArea;

    @FXML private Button createApplicationButton;
    @FXML private Button updateApplicationButton;
    @FXML private Button submitApplicationButton;
    @FXML private Label formStatusLabel;

    // ========================================================================
    // UI Components - Status & Timeline Tab
    // ========================================================================

    @FXML private Tab statusTab;
    @FXML private VBox timelineContainer;
    @FXML private Label currentStatusLabel;
    @FXML private Label estimatedTimeLabel;
    @FXML private TextArea statusDetailsArea;
    @FXML private Button refreshStatusButton;

    // ========================================================================
    // UI Components - Check-in Information Tab
    // ========================================================================

    @FXML private Tab checkInTab;
    @FXML private Label checkInStatusLabel;
    @FXML private Label roomNumberLabel;
    @FXML private Label blockAssignmentLabel;
    @FXML private Label checkInDateLabel;
    @FXML private TextArea checkInInstructionsArea;

    // Status/Feedback
    @FXML private Label generalStatusLabel;
    @FXML private ProgressIndicator loadingIndicator;

    // ========================================================================
    // Service Layer
    // ========================================================================

    private final StudentHousingService housingService;

    // Observable data
    private final ObservableList<HousingOption> housingData = FXCollections.observableArrayList();

    // Current context
    private long studentUserId = 1; // TODO: Get from session
    private String currentWindowCode = "2024-MAIN"; // TODO: Get from session
    private DormApplication currentApplication;
    private HousingOption selectedHousingOption;

    // ========================================================================
    // Inner Class: HousingOption Display Model
    // ========================================================================

    /**
     * Display model for housing options
     * Encapsulates housing information for UI display
     */
    public static class HousingOption {
        private final long blockId;
        private final String blockName;
        private final String campus;
        private final int totalBeds;
        private final int availableBeds;
        private final String gender;

        public HousingOption(long blockId, String blockName, String campus, int totalBeds, int availableBeds, String gender) {
            this.blockId = blockId;
            this.blockName = blockName;
            this.campus = campus;
            this.totalBeds = totalBeds;
            this.availableBeds = availableBeds;
            this.gender = gender;
        }

        // Getters
        public long getBlockId() { return blockId; }
        public String getBlockName() { return blockName; }
        public String getCampus() { return campus; }
        public int getTotalBeds() { return totalBeds; }
        public int getAvailableBeds() { return availableBeds; }
        public String getGender() { return gender; }
        public double getOccupancyPercentage() {
            return totalBeds > 0 ? ((totalBeds - availableBeds) * 100.0) / totalBeds : 0;
        }
    }

    // ========================================================================
    // Constructors
    // ========================================================================

    /**
     * Default constructor: Creates StudentHousingService with default DAOs
     */
    public StudentDashboardController() {
        this(new StudentHousingService());
    }

    /**
     * Constructor with dependency injection
     * 
     * @param housingService StudentHousingService (cannot be null)
     * @throws NullPointerException if housingService is null
     */
    public StudentDashboardController(StudentHousingService housingService) {
        this.housingService = Objects.requireNonNull(housingService, "housingService cannot be null");
    }

    // ========================================================================
    // FXML Initialization
    // ========================================================================

    /**
     * Called by FXMLLoader after FXML injection
     */
    @FXML
    public void initialize() {
        try {
            initializeAvailableHousingTab();
            initializeApplicationTab();
            initializeStatusTab();
            initializeCheckInTab();
            loadAvailableHousing();
            loadCurrentApplication();
            updateStatus("Student dashboard initialized");
        } catch (Exception e) {
            updateStatus("Error initializing dashboard: " + e.getMessage());
            showError("Initialization Error", e.getMessage());
        }
    }

    // ========================================================================
    // Available Housing Tab
    // ========================================================================

    /**
     * Initialize Available Housing Tab
     */
    private void initializeAvailableHousingTab() {
        // Configure table columns
        blockNameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getBlockName())
        );
        campusColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getCampus())
        );
        totalBedsColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getTotalBeds() + "")
        );
        availableBedsColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getAvailableBeds() + "")
        );
        occupancyColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.format("%.1f%%", cellData.getValue().getOccupancyPercentage()))
        );

        housingTable.setItems(housingData);

        // Table selection handler
        housingTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                displayHousingDetails(newVal);
            }
        });

        // Filter handlers
        campusFilterCombo.setItems(FXCollections.observableArrayList("All", "Main Campus", "North Campus", "South Campus"));
        campusFilterCombo.setValue("All");

        genderFilterCombo.setItems(FXCollections.observableArrayList("All", "Male", "Female"));
        genderFilterCombo.setValue("All");

        occupancySlider.setMin(0);
        occupancySlider.setMax(100);
        occupancySlider.setValue(100);

        filterButton.setOnAction(e -> filterHousing());
        refreshHousingButton.setOnAction(e -> loadAvailableHousing());
    }

    /**
     * Load available housing options
     */
    private void loadAvailableHousing() {
        try {
            setLoading(true);
            // TODO: Implement actual housing loading from service
            // For now: Demo data
            housingData.setAll(
                    new HousingOption(1, "Block A (Male)", "Main Campus", 50, 15, "MALE"),
                    new HousingOption(2, "Block B (Female)", "Main Campus", 40, 8, "FEMALE"),
                    new HousingOption(3, "Block C (Male)", "North Campus", 60, 22, "MALE"),
                    new HousingOption(4, "Block D (Female)", "South Campus", 45, 12, "FEMALE")
            );
            updateStatus("Loaded " + housingData.size() + " available housing options");
        } catch (Exception e) {
            updateStatus("Error loading housing: " + e.getMessage());
            showError("Load Error", e.getMessage());
        } finally {
            setLoading(false);
        }
    }

    /**
     * Display housing option details
     * 
     * @param option Housing option selected
     */
    private void displayHousingDetails(HousingOption option) {
        selectedHousingOption = option;
        selectedBlockLabel.setText("Block: " + option.getBlockName());
        selectedCampusLabel.setText("Campus: " + option.getCampus());
        selectedCapacityLabel.setText("Total Beds: " + option.getTotalBeds() + " | Available: " + option.getAvailableBeds());
        selectedOccupancyLabel.setText(String.format("Occupancy: %.1f%%", option.getOccupancyPercentage()));

        // Set campus preference when housing selected
        campusPreferenceCombo.setValue(option.getCampus());
    }

    /**
     * Filter housing based on criteria
     */
    private void filterHousing() {
        String campusFilter = campusFilterCombo.getValue();
        String genderFilter = genderFilterCombo.getValue();
        double maxOccupancy = occupancySlider.getValue();

        housingData.filtered(option -> {
            boolean campusMatch = campusFilter.equals("All") || option.getCampus().equals(campusFilter);
            boolean genderMatch = genderFilter.equals("All") || option.getGender().equals(genderFilter);
            boolean occupancyMatch = option.getOccupancyPercentage() <= maxOccupancy;
            return campusMatch && genderMatch && occupancyMatch;
        });

        updateStatus("Filtered " + housingData.size() + " housing options");
    }

    // ========================================================================
    // Application Tab
    // ========================================================================

    /**
     * Initialize Application Tab
     */
    private void initializeApplicationTab() {
        // Configure combos
        campusPreferenceCombo.setItems(FXCollections.observableArrayList(
                "Main Campus", "North Campus", "South Campus"
        ));
        sponsorshipCombo.setItems(FXCollections.observableArrayList("GOV", "SELF"));
        sponsorshipCombo.setValue("GOV");

        // Configure spinner
        distanceSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 1000, 10, 1));

        // Button handlers
        createApplicationButton.setOnAction(e -> onCreateApplication());
        updateApplicationButton.setOnAction(e -> onUpdateApplication());
        submitApplicationButton.setOnAction(e -> onSubmitApplication());
    }

    /**
     * Load current application or indicate need to create
     */
    private void loadCurrentApplication() {
        try {
            currentApplication = housingService.getStudentApplication(studentUserId, currentWindowCode);
            
            if (currentApplication == null) {
                applicationStatusLabel.setText("No active application for this window");
                createApplicationButton.setDisable(false);
                updateApplicationButton.setDisable(true);
                submitApplicationButton.setDisable(true);
            } else {
                applicationIdLabel.setText("Application ID: " + currentApplication.id());
                displayApplicationStatus();
                populateFormFromApplication();
                
                createApplicationButton.setDisable(true);
                updateApplicationButton.setDisable(currentApplication.status() != ApplicationStatus.DRAFT && 
                                                    currentApplication.status() != ApplicationStatus.NEEDS_EDIT);
                submitApplicationButton.setDisable(currentApplication.status() != ApplicationStatus.DRAFT && 
                                                   currentApplication.status() != ApplicationStatus.NEEDS_EDIT);
            }
        } catch (Exception e) {
            updateStatus("Error loading application: " + e.getMessage());
        }
    }

    /**
     * Display current application status
     */
    private void displayApplicationStatus() {
        if (currentApplication != null) {
            applicationStatusLabel.setText("Status: " + currentApplication.status());
            submissionStatusLabel.setText(housingService.getStatusMessage(currentApplication.id()));
        }
    }

    /**
     * Populate form from current application
     */
    private void populateFormFromApplication() {
        if (currentApplication != null) {
            campusPreferenceCombo.setValue(currentApplication.campusPreference());
            sponsorshipCombo.setValue(currentApplication.sponsorshipType().toString());
            disabilityCheckBox.setSelected(currentApplication.disability());
            distanceSpinner.getValueFactory().setValue(currentApplication.distanceKm());
            notesArea.setText(currentApplication.notes() != null ? currentApplication.notes() : "");
        }
    }

    /**
     * Handler: Create Application button clicked
     */
    @FXML
    private void onCreateApplication() {
        String campus = campusPreferenceCombo.getValue();
        String sponsorship = sponsorshipCombo.getValue();
        boolean disability = disabilityCheckBox.isSelected();
        double distance = distanceSpinner.getValue();

        if (campus == null || sponsorship == null) {
            showWarning("Missing Fields", "Please select campus and sponsorship type");
            return;
        }

        try {
            setLoading(true);
            long appId = housingService.createApplication(
                    studentUserId,
                    currentWindowCode,
                    sponsorship,
                    campus,
                    disability,
                    distance
            );

            formStatusLabel.setText("Application created successfully (ID: " + appId + ")");
            loadCurrentApplication();
            showInformation("Success", "Application created. You can now fill in details and submit.");
        } catch (Exception e) {
            formStatusLabel.setText("Creation failed: " + e.getMessage());
            showError("Creation Failed", e.getMessage());
        } finally {
            setLoading(false);
        }
    }

    /**
     * Handler: Update Application button clicked
     */
    @FXML
    private void onUpdateApplication() {
        if (currentApplication == null) {
            showWarning("No Application", "Please create an application first");
            return;
        }

        try {
            housingService.updateApplication(
                    currentApplication.id(),
                    campusPreferenceCombo.getValue(),
                    distanceSpinner.getValue(),
                    notesArea.getText()
            );

            formStatusLabel.setText("Application updated successfully");
            loadCurrentApplication();
            showInformation("Success", "Application updated");
        } catch (Exception e) {
            formStatusLabel.setText("Update failed: " + e.getMessage());
            showError("Update Failed", e.getMessage());
        }
    }

    /**
     * Handler: Submit Application button clicked
     */
    @FXML
    private void onSubmitApplication() {
        if (currentApplication == null) {
            showWarning("No Application", "Please create an application first");
            return;
        }

        // Confirm submission
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Submission");
        confirmAlert.setContentText("Submit your application for review? You cannot edit after submission.");
        if (confirmAlert.showAndWait().get() != ButtonType.OK) {
            return;
        }

        try {
            setLoading(true);
            housingService.submitApplication(currentApplication.id(), studentUserId, null);
            formStatusLabel.setText("Application submitted successfully");
            loadCurrentApplication();
            showInformation("Success", "Application submitted for review");
        } catch (Exception e) {
            formStatusLabel.setText("Submission failed: " + e.getMessage());
            showError("Submission Failed", e.getMessage());
        } finally {
            setLoading(false);
        }
    }

    // ========================================================================
    // Status Tab
    // ========================================================================

    /**
     * Initialize Status & Timeline Tab
     */
    private void initializeStatusTab() {
        refreshStatusButton.setOnAction(e -> updateStatusDisplay());
    }

    /**
     * Update status display with timeline
     */
    private void updateStatusDisplay() {
        if (currentApplication == null) {
            currentStatusLabel.setText("No application to display");
            return;
        }

        currentStatusLabel.setText("Current Status: " + currentApplication.status());
        estimatedTimeLabel.setText("Estimated Processing: " + 
                                  housingService.getEstimatedProcessingDays(currentWindowCode) + " days");
        statusDetailsArea.setText(housingService.getStatusMessage(currentApplication.id()));

        // Display timeline (TODO: Create visual timeline)
        timelineContainer.getChildren().clear();
        addTimelineStep("Application Created", true);
        addTimelineStep("Submitted for Review", currentApplication.status().ordinal() >= ApplicationStatus.SUBMITTED.ordinal());
        addTimelineStep("Under Review", currentApplication.status().ordinal() >= ApplicationStatus.UNDER_REVIEW.ordinal());
        addTimelineStep("Approved", currentApplication.status().ordinal() >= ApplicationStatus.ACCEPTED.ordinal());
        addTimelineStep("Checked In", currentApplication.status().ordinal() >= ApplicationStatus.CHECKED_IN.ordinal());
    }

    /**
     * Add timeline step to display
     * 
     * @param stepName Name of timeline step
     * @param completed Whether step is completed
     */
    private void addTimelineStep(String stepName, boolean completed) {
        Label step = new Label((completed ? "✓ " : "○ ") + stepName);
        step.setStyle(completed ? "-fx-text-fill: green; -fx-font-weight: bold;" : "-fx-text-fill: gray;");
        timelineContainer.getChildren().add(step);
    }

    // ========================================================================
    // Check-in Tab
    // ========================================================================

    /**
     * Initialize Check-in Tab
     */
    private void initializeCheckInTab() {
        loadCheckInInfo();
    }

    /**
     * Load check-in information if student is allocated
     */
    private void loadCheckInInfo() {
        if (currentApplication != null && housingService.isCheckedIn(currentApplication.id())) {
            checkInStatusLabel.setText("Status: Checked In");
            roomNumberLabel.setText("Room: N/A"); // TODO: Get from allocation
            blockAssignmentLabel.setText("Block: N/A"); // TODO: Get from allocation
            checkInDateLabel.setText("Check-in Date: N/A"); // TODO: Get from allocation
            checkInInstructionsArea.setText("Welcome to your dormitory! Please follow the house rules and contact your block proctor for any issues.");
        } else {
            checkInStatusLabel.setText("Not yet checked in");
            checkInInstructionsArea.setText("Waiting for allocation and check-in instructions from administration.");
        }
    }

    // ========================================================================
    // UI Utility Methods
    // ========================================================================

    /**
     * Update status label
     * 
     * @param message Status message
     */
    private void updateStatus(String message) {
        if (generalStatusLabel != null) {
            generalStatusLabel.setText(message);
        }
        System.out.println("[Student] " + message);
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
