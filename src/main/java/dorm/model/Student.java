package dorm.model;

public class Student extends User {
    private final String studentId;
    private Gender gender;
    
    // Phase One - Address info
    private Residency residency;
    private String city;
    private String subcity;
    private String woreda;
    private SponsorshipType sponsorshipType;
    private String disabilityInfo;
    
    // Phase Two - Emergency contact & payment
    private String motherName;
    private String motherPhone;
    private Residency motherResidency;
    private String emergencyContact;
    private String transactionId;
    
    // Assignment
    private String assignedBuilding;

    public Student(String id, String username, String password, String displayName, String studentId, Gender gender) {
        super(id, username, password, Role.STUDENT, displayName);
        this.studentId = studentId;
        this.gender = gender;
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

    public String getMotherName() {
        return motherName;
    }

    public void setMotherName(String motherName) {
        this.motherName = motherName;
    }

    public String getMotherPhone() {
        return motherPhone;
    }

    public void setMotherPhone(String motherPhone) {
        this.motherPhone = motherPhone;
    }

    public Residency getMotherResidency() {
        return motherResidency;
    }

    public void setMotherResidency(Residency motherResidency) {
        this.motherResidency = motherResidency;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
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
