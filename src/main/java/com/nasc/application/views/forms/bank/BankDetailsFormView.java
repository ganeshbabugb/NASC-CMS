package com.nasc.application.views.forms.bank;

import com.nasc.application.data.core.BankDetails;
import com.nasc.application.data.core.User;
import com.nasc.application.services.UserService;
import com.nasc.application.utils.NotificationUtils;
import com.nasc.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

@PageTitle("Bank Details Form")
@Route(value = "bank-details-form", layout = MainLayout.class)
@RolesAllowed({"HOD", "PROFESSOR", "STUDENT"})
public class BankDetailsFormView extends Div {
    private final TextField accountHolderName = new TextField("Account Holder Name");
    private final TextField bankName = new TextField("Bank Name");
    private final TextField accountNumber = new TextField("Account Number");
    private final TextField ifscCode = new TextField("IFSC Code");
    private final TextField branchName = new TextField("Branch Name");
    private final TextField branchAddress = new TextField("Branch Address");
    private final TextField panNumber = new TextField("Permanent Account Number");
    private final Button saveButton = new Button("Save");
    private final UserService userService;
    private final BeanValidationBinder<BankDetails> binder = new BeanValidationBinder<>(BankDetails.class);

    @Autowired
    public BankDetailsFormView(UserService userService) {
        this.userService = userService;
        addClassName("bank-details-form-view");
        add(createTitle(), createFormLayout(), createButtonLayout());

        binder.bindInstanceFields(this);

        saveButton.addClickListener(e -> {
            if (isBankDetailsAlreadySaved()) {
                updateBankDetails();
            } else {
                saveBankDetails();
            }
        });

        initFormWithExistingDetails();
        binder.addStatusChangeListener(e -> saveButton.setEnabled(binder.isValid()));

    }

    private Component createTitle() {
        return new H3("Bank Account Details");
    }

    private Component createFormLayout() {
        return new FormLayout(
                accountHolderName, bankName, accountNumber,
                ifscCode, branchName, branchAddress, panNumber
        );
    }

    private Component createButtonLayout() {
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton);
        buttonLayout.addClassName("button-layout");
        return buttonLayout;
    }

    private void initFormWithExistingDetails() {
        User currentUser = userService.getCurrentUser();
        BankDetails existingBankDetails = currentUser.getBankDetails();

        if (existingBankDetails != null) {
            populateForm(existingBankDetails);
            saveButton.setText("Update");
        }
    }

    private void populateForm(BankDetails bankDetails) {
        accountHolderName.setValue(bankDetails.getAccountHolderName());
        bankName.setValue(bankDetails.getBankName());
        accountNumber.setValue(bankDetails.getAccountNumber());
        ifscCode.setValue(bankDetails.getIfscCode());
        branchName.setValue(bankDetails.getBranchName());
        branchAddress.setValue(bankDetails.getBranchAddress());
        panNumber.setValue(bankDetails.getPanNumber());
    }

    private boolean isBankDetailsAlreadySaved() {
        return userService.getCurrentUser().getBankDetails() != null;
    }

    private void saveBankDetails() {
        User currentUser = userService.getCurrentUser();
        BankDetails newBankDetails = createBankDetailsFromForm();

        // Associate bank details with the current user
        newBankDetails.setUser(currentUser);

        // Set the new bank details to the current user
        currentUser.setBankDetails(newBankDetails);

        // Save the user with bank details
        userService.saveUserWithBankDetails(currentUser, newBankDetails);
        NotificationUtils.createSubmitSuccess("Bank details saved successfully");
    }

    private void updateBankDetails() {
        User currentUser = userService.getCurrentUser();
        BankDetails existingBankDetails = currentUser.getBankDetails();

        // Print values before update
        System.out.println("Before Update: " + existingBankDetails);

        // Update existing bank details
        existingBankDetails.setAccountHolderName(accountHolderName.getValue());
        existingBankDetails.setBankName(bankName.getValue());
        existingBankDetails.setAccountNumber(accountNumber.getValue());
        existingBankDetails.setIfscCode(ifscCode.getValue());
        existingBankDetails.setBranchName(branchName.getValue());
        existingBankDetails.setBranchAddress(branchAddress.getValue());
        existingBankDetails.setPanNumber(panNumber.getValue());

        // Save the user with updated bank details
        userService.saveUserWithBankDetails(currentUser, existingBankDetails);

        NotificationUtils.createSubmitSuccess("Bank details updated successfully");
    }

    private BankDetails createBankDetailsFromForm() {
        BankDetails bankDetails = new BankDetails();
        bankDetails.setAccountHolderName(accountHolderName.getValue());
        bankDetails.setBankName(bankName.getValue());
        bankDetails.setAccountNumber(accountNumber.getValue());
        bankDetails.setIfscCode(ifscCode.getValue());
        bankDetails.setBranchName(branchName.getValue());
        bankDetails.setBranchAddress(branchAddress.getValue());
        bankDetails.setPanNumber(panNumber.getValue());
        return bankDetails;
    }
}