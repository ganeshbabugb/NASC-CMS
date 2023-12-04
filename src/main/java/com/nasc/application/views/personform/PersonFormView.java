package com.nasc.application.views.personform;

import com.nasc.application.data.model.PersonalDetails;
import com.nasc.application.data.model.User;
import com.nasc.application.services.UserService;
import com.nasc.application.views.MainLayout;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Personal Information Form")
@Route(value = "personal-information-form", layout = MainLayout.class)
@RolesAllowed({"HOD", "PROFESSOR", "STUDENT"})
@Uses(Icon.class)
public class PersonFormView extends Composite<VerticalLayout> {

    private final UserService userService;
    private final VerticalLayout mainLayout = new VerticalLayout();
    private final H3 title = new H3();
    private final FormLayout formLayout = new FormLayout();
    private final TextField firstNameField = new TextField();
    private final TextField lastNameField = new TextField();
    private final DatePicker birthdayPicker = new DatePicker();
    private final TextField phoneNumberField = new TextField();
    private final EmailField emailField = new EmailField();
    private final ComboBox<String> genderComboBox = new ComboBox<>();
    private final HorizontalLayout buttonLayout = new HorizontalLayout();
    private final Button saveButton = new Button();
    private final Button cancelButton = new Button();

    public PersonFormView(UserService userService) {
        this.userService = userService;
        configureLayout();
        initFormWithExistingDetails(); // Initialize the form with existing personal details
    }

    private void configureLayout() {
        mainLayout.setWidth("100%");
        mainLayout.getStyle().set("flex-grow", "1");
        mainLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        mainLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        title.setText("Personal Information");
        title.setWidth("100%");

        formLayout.setWidth("100%");
        firstNameField.setLabel("First Name");
        lastNameField.setLabel("Last Name");
        birthdayPicker.setLabel("Birthday");
        phoneNumberField.setLabel("Phone Number");
        emailField.setLabel("Email");

        buttonLayout.addClassName(Gap.MEDIUM);
        buttonLayout.setWidth("100%");
        buttonLayout.getStyle().set("flex-grow", "1");

        saveButton.setText("Save");
        saveButton.setWidth("min-content");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        cancelButton.setText("Cancel");
        cancelButton.setWidth("min-content");

        mainLayout.add(title, formLayout, buttonLayout);
        formLayout.add(firstNameField,
                lastNameField,
                birthdayPicker,
                phoneNumberField,
                emailField,
                genderComboBox);
        genderComboBox.setLabel("Gender");
        genderComboBox.setItems("Male", "Female", "Other");

        saveButton.addClickListener(e -> {
            if (isPersonalDetailsAlreadySaved()) {
                updatePersonalDetails();
            } else {
                savePersonalDetails();
            }
        });


        buttonLayout.add(saveButton, cancelButton);

        getContent().add(mainLayout);
    }

    private void initFormWithExistingDetails() {
        // TODO: Add logic to initialize the form with existing personal details if available
        User currentUser = userService.getCurrentUser();
        PersonalDetails existingPersonalDetails = currentUser.getPersonalDetails();
        if (existingPersonalDetails != null) {
            populateForm(existingPersonalDetails);
            saveButton.setText("Update");
        }
    }

    private void populateForm(PersonalDetails existingPersonalDetails) {
        // TODO: Populate the form fields with the existing personal details
        firstNameField.setValue(existingPersonalDetails.getFirstName());
        lastNameField.setValue(existingPersonalDetails.getLastName());
        birthdayPicker.setValue(existingPersonalDetails.getBirthday());
        phoneNumberField.setValue(existingPersonalDetails.getPhoneNumber());
        emailField.setValue(existingPersonalDetails.getEmail());
        genderComboBox.setValue(existingPersonalDetails.getGender());
    }

    private boolean isPersonalDetailsAlreadySaved() {
        // TODO: Implement logic to check if personal details are already saved for the current user
        return userService.getCurrentUser().getPersonalDetails() != null;
    }

    private void updatePersonalDetails() {
        // TODO: Implement logic to update the personal details for the current user
        User currentUser = userService.getCurrentUser();
        PersonalDetails existingPersonalDetails = currentUser.getPersonalDetails();
        existingPersonalDetails.setFirstName(firstNameField.getValue());
        existingPersonalDetails.setLastName(lastNameField.getValue());
        existingPersonalDetails.setBirthday(birthdayPicker.getValue());
        existingPersonalDetails.setPhoneNumber(phoneNumberField.getValue());
        existingPersonalDetails.setEmail(emailField.getValue());
        existingPersonalDetails.setGender(genderComboBox.getValue());
        userService.saveUserWithPersonalDetails(currentUser, existingPersonalDetails);
        Notification.show("Personal Form Updated Successfully.");
    }

    private void savePersonalDetails() {
        // TODO: Implement logic to save the personal details for the current user
        User currentUser = userService.getCurrentUser();
        PersonalDetails personalDetailsFromForm = createPersonalDetailsFromForm();
        personalDetailsFromForm.setUser(currentUser);
        currentUser.setPersonalDetails(personalDetailsFromForm);
        userService.saveUserWithPersonalDetails(currentUser, personalDetailsFromForm);
        Notification.show("Personal Form Saved Successfully.");
    }

    private PersonalDetails createPersonalDetailsFromForm() {
        // TODO: Implement logic to create PersonalDetails object from the form fields
        PersonalDetails personalDetails = new PersonalDetails();
        personalDetails.setFirstName(firstNameField.getValue());
        personalDetails.setLastName(lastNameField.getValue());
        personalDetails.setBirthday(birthdayPicker.getValue());
        personalDetails.setPhoneNumber(phoneNumberField.getValue());
        personalDetails.setEmail(emailField.getValue());
        personalDetails.setGender(genderComboBox.getValue());
        return personalDetails;
//        return null;
    }
}
