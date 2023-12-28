package com.nasc.application.data.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasc.application.data.core.enums.Role;
import com.nasc.application.data.core.enums.StudentSection;
import com.opencsv.bean.CsvBindByName;
import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table(name = "application_user")
public class User extends AbstractEntity {

    @CsvBindByName
    private String username;
    @CsvBindByName
    @Column(unique = true)
    private String registerNumber;
    @CsvBindByName
    private String email;
    @JsonIgnore
    @CsvBindByName
    private String password;
    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<Role> roles;
    @Enumerated(EnumType.STRING)
    private StudentSection studentSection;

    /*
    @Lob
    @Column(length = 1000000)
    private byte[] profilePicture;
    */

    private Boolean personalDetailsCompleted;
    private Boolean addressDetailsCompleted;
    private Boolean bankDetailsCompleted;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private DepartmentEntity department;

    @ManyToOne
    @JoinColumn(name = "academic_year_id")
    private AcademicYearEntity academicYear;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "bank_details_id")
    private BankDetails bankDetails;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "address_details_id")
    private AddressDetails addressDetails;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "personal_details_id")
    private PersonalDetails personalDetails;

    public User() {
        this.personalDetailsCompleted = Boolean.FALSE;
        this.addressDetailsCompleted = Boolean.FALSE;
        this.bankDetailsCompleted = Boolean.FALSE;
    }

    public AcademicYearEntity getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(AcademicYearEntity academicYear) {
        this.academicYear = academicYear;
    }

    public DepartmentEntity getDepartment() {
        return department;
    }

    public void setDepartment(DepartmentEntity department) {
        this.department = department;
    }

    public StudentSection getStudentSection() {
        return studentSection;
    }

    public void setStudentSection(StudentSection studentSection) {
        this.studentSection = studentSection;
    }

    public PersonalDetails getPersonalDetails() {
        return personalDetails;
    }

    public void setPersonalDetails(PersonalDetails personalDetails) {
        this.personalDetails = personalDetails;
    }


    public Boolean getPersonalDetailsCompleted() {
        return personalDetailsCompleted;
    }

    public void setPersonalDetailsCompleted(Boolean personalDetailsCompleted) {
        this.personalDetailsCompleted = personalDetailsCompleted;
    }

    public Boolean getAddressDetailsCompleted() {
        return addressDetailsCompleted;
    }

    public void setAddressDetailsCompleted(Boolean addressDetailsCompleted) {
        this.addressDetailsCompleted = addressDetailsCompleted;
    }

    public Boolean getBankDetailsCompleted() {
        return bankDetailsCompleted;
    }

    public void setBankDetailsCompleted(Boolean bankDetailsCompleted) {
        this.bankDetailsCompleted = bankDetailsCompleted;
    }

    public AddressDetails getAddressDetails() {
        return addressDetails;
    }

    public void setAddressDetails(AddressDetails addressDetails) {
        this.addressDetails = addressDetails;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRegisterNumber() {
        return registerNumber;
    }

    public void setRegisterNumber(String registerNumber) {
        this.registerNumber = registerNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    /*
        public byte[] getProfilePicture() {
            return profilePicture;
        }
        public void setProfilePicture(byte[] profilePicture) {
            this.profilePicture = profilePicture;
        }
    */
    public BankDetails getBankDetails() {
        return bankDetails;
    }

    public void setBankDetails(BankDetails bankDetails) {
        this.bankDetails = bankDetails;
    }

    //This is ued to indicate user completed all three forms are not.
    public boolean isFormsCompleted() {
        return personalDetailsCompleted && addressDetailsCompleted && bankDetailsCompleted;
    }

}
