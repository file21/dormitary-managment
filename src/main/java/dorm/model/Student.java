package dorm.model;

import java.util.ArrayList;
import java.util.List;

public class Student extends User {
    private final String studentId;
    private String city;
    private Gender gender;
    private SponsorshipType sponsorshipType;
    private String disabilityInfo;
    private final List<String> documentPaths;
    private String paymentSlipPath;
    private String assignedBuilding;
    private String entryDate;
    private String withdrawalDate;

    public Student(String id, String username, String password, String displayName, String studentId, String city, Gender gender) {
        super(id, username, password, Role.STUDENT, displayName);
        this.studentId = studentId;
        this.city = city;
        this.gender = gender;
        this.documentPaths = new ArrayList<>();
    }

    public String getStudentId() {
        return studentId;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
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

    public List<String> getDocumentPaths() {
        return documentPaths;
    }

    public void addDocumentPath(String path) {
        if (path != null && !path.isBlank()) {
            documentPaths.add(path);
        }
    }
    
    public void setDocumentPaths(List<String> paths) {
        documentPaths.clear();
        if (paths != null) {
            documentPaths.addAll(paths);
        }
    }

    public String getPaymentSlipPath() {
        return paymentSlipPath;
    }

    public void setPaymentSlipPath(String paymentSlipPath) {
        this.paymentSlipPath = paymentSlipPath;
    }

    public String getAssignedBuilding() {
        return assignedBuilding;
    }

    public void setAssignedBuilding(String assignedBuilding) {
        this.assignedBuilding = assignedBuilding;
    }

    public String getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(String entryDate) {
        this.entryDate = entryDate;
    }

    public String getWithdrawalDate() {
        return withdrawalDate;
    }

    public void setWithdrawalDate(String withdrawalDate) {
        this.withdrawalDate = withdrawalDate;
    }
}
