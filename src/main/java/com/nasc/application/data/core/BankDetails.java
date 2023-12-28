package com.nasc.application.data.core;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name = "bank_details")
public class BankDetails extends AbstractEntity {

    @NotBlank(message = "Account holder name cannot be blank")
    private String accountHolderName;

    @NotBlank(message = "Bank name cannot be blank")
    private String bankName;

    @NotBlank(message = "Account number cannot be blank")
    @Pattern(regexp = "[0-9]+", message = "Account number must contain only digits")
    private String accountNumber;

    @NotBlank(message = "IFSC code cannot be blank")
    private String ifscCode;

    @NotBlank(message = "Branch name cannot be blank")
    private String branchName;

    @NotBlank(message = "Branch address cannot be blank")
    private String branchAddress;

    //    @NotBlank(message = "PAN number cannot be blank")
    @Pattern(regexp = "[A-Z]{5}[0-9]{4}[A-Z]", message = "Invalid PAN number format")
    private String panNumber;

    @OneToOne(mappedBy = "bankDetails")
    private User user;

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getIfscCode() {
        return ifscCode;
    }

    public void setIfscCode(String ifscCode) {
        this.ifscCode = ifscCode;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getBranchAddress() {
        return branchAddress;
    }

    public void setBranchAddress(String branchAddress) {
        this.branchAddress = branchAddress;
    }

    public String getPanNumber() {
        return panNumber;
    }

    public void setPanNumber(String panNumber) {
        this.panNumber = panNumber;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}