package dorm.model;

public class Student extends User {
    private final String studentId;
    private Gender gender;
    private College college;
    
    // Phase One - Address info
    private Residency residency;
    private String city;
    private String subcity;
    private String woreda;
    private SponsorshipType sponsorshipType;
    private String disabilityInfo;
    
    // Phase Two - Emergency contact & payment
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String transactionId;  // for self-sponsored only
    
    // Assignment
    private String assignedBuilding;

    public Student(String id, String username, String password, String displayName, String studentId, Gender gender, College college) {
        super(id, username, password, Role.STUDENT, displayName);
        this.studentId = studentId;
        this.gender = gender;
        this.college = college;
        this.assignedBuilding = "unassigned";
    }

    public String getStudentId() {
        return studentId;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public College getCollege() {
        return college;
    }

    public void setCollege(College college) {
        this.college = college;
    }

    public Residency getResidency() {
        return residency;
    }

    public void setResidency(Residency residency) {
        this.residency = residency;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getSubcity() {
        return subcity;
    }

    public void setSubcity(String subcity) {
        this.subcity = subcity;
    }

    public String getWoreda() {
        return woreda;
    }

    public void setWoreda(String woreda) {
        this.woreda = woreda;
    }

    public SponsorshipType getSponsorshipType() {
        return sponsorshipType;
    }

    public void setSponsorshipType(SponsorshipType sponsorshipType) {
        this.sponsorshipType = sponsorshipType;
    }

    public String getDisabilityInfo() {
        return disabilityInfo;
    }

    public void setDisabilityInfo(String disabilityInfo) {
        this.disabilityInfo = disabilityInfo;
    }

    public String getEmergencyContactName() {
        return emergencyContactName;
    }

    public void setEmergencyContactName(String emergencyContactName) {
        this.emergencyContactName = emergencyContactName;
    }

    public String getEmergencyContactPhone() {
        return emergencyContactPhone;
    }

    public void setEmergencyContactPhone(String emergencyContactPhone) {
        this.emergencyContactPhone = emergencyContactPhone;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getAssignedBuilding() {
        return assignedBuilding;
    }

    public void setAssignedBuilding(String assignedBuilding) {
        this.assignedBuilding = assignedBuilding;
    }
}
