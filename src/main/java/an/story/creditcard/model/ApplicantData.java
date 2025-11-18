package an.story.creditcard.model;

import java.time.LocalDate;

/**
 * Model class representing applicant data for credit card application
 */
public class ApplicantData {
    private String applicationId;
    private String applicantName;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String ssn;
    private Address address;
    private Double annualIncome;
    
    public ApplicantData() {
    }
    
    public ApplicantData(String applicationId, String applicantName, String email, 
                        String phoneNumber, LocalDate dateOfBirth, String ssn, 
                        Address address, Double annualIncome) {
        this.applicationId = applicationId;
        this.applicantName = applicantName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
        this.ssn = ssn;
        this.address = address;
        this.annualIncome = annualIncome;
    }
    
    // Getters and setters
    public String getApplicationId() {
        return applicationId;
    }
    
    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }
    
    public String getApplicantName() {
        return applicantName;
    }
    
    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
    
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    
    public String getSsn() {
        return ssn;
    }
    
    public void setSsn(String ssn) {
        this.ssn = ssn;
    }
    
    public Address getAddress() {
        return address;
    }
    
    public void setAddress(Address address) {
        this.address = address;
    }
    
    public Double getAnnualIncome() {
        return annualIncome;
    }
    
    public void setAnnualIncome(Double annualIncome) {
        this.annualIncome = annualIncome;
    }
}

