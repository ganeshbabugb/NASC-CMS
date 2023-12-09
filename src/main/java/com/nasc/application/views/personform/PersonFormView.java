package com.nasc.application.views.personform;

import com.nasc.application.data.model.PersonalDetails;
import com.nasc.application.data.model.User;
import com.nasc.application.services.UserService;
import com.nasc.application.utils.NotificationUtils;
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
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
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
    private final TextField firstName = new TextField();
    private final TextField lastName = new TextField();
    private final DatePicker birthday = new DatePicker();
    private final TextField phoneNumber = new TextField();
    private final EmailField email = new EmailField();
    private final ComboBox<String> gender = new ComboBox<>();
    private final HorizontalLayout buttonLayout = new HorizontalLayout();
    private final Button saveButton = new Button();
    private final BeanValidationBinder<PersonalDetails> binder = new BeanValidationBinder<>(PersonalDetails.class);

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
        firstName.setLabel("First Name");
        lastName.setLabel("Last Name");
        birthday.setLabel("Birthday");
        phoneNumber.setLabel("Phone Number");
        email.setLabel("Email");

        buttonLayout.addClassName(Gap.MEDIUM);
        buttonLayout.setWidth("100%");
        buttonLayout.getStyle().set("flex-grow", "1");

        saveButton.setText("Save");
        saveButton.setWidth("min-content");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        mainLayout.add(title, formLayout, buttonLayout);
        formLayout.add(firstName,
                lastName,
                birthday,
                phoneNumber,
                email,
                gender);
        gender.setLabel("Gender");
        gender.setItems("Male", "Female", "Other");

        saveButton.addClickListener(e -> {
            if (isPersonalDetailsAlreadySaved()) {
                updatePersonalDetails();
            } else {
                savePersonalDetails();
            }
        });

        binder.addStatusChangeListener(e -> saveButton.setEnabled(binder.isValid()));

        buttonLayout.add(saveButton);

        getContent().add(mainLayout);
    }

    private void initFormWithExistingDetails() {
        User currentUser = userService.getCurrentUser();
        binder.bindInstanceFields(this);
        PersonalDetails existingPersonalDetails = currentUser.getPersonalDetails();
        if (existingPersonalDetails != null) {
            populateForm(existingPersonalDetails);
            saveButton.setText("Update");
        }
    }

    private void populateForm(PersonalDetails existingPersonalDetails) {
        firstName.setValue(existingPersonalDetails.getFirstName());
        lastName.setValue(existingPersonalDetails.getLastName());
        birthday.setValue(existingPersonalDetails.getBirthday());
        phoneNumber.setValue(existingPersonalDetails.getPhoneNumber());
        email.setValue(existingPersonalDetails.getEmail());
        gender.setValue(existingPersonalDetails.getGender());
    }

    private boolean isPersonalDetailsAlreadySaved() {
        return userService.getCurrentUser().getPersonalDetails() != null;
    }

    private void updatePersonalDetails() {
        User currentUser = userService.getCurrentUser();
        PersonalDetails existingPersonalDetails = currentUser.getPersonalDetails();
        existingPersonalDetails.setFirstName(firstName.getValue());
        existingPersonalDetails.setLastName(lastName.getValue());
        existingPersonalDetails.setBirthday(birthday.getValue());
        existingPersonalDetails.setPhoneNumber(phoneNumber.getValue());
        existingPersonalDetails.setEmail(email.getValue());
        existingPersonalDetails.setGender(gender.getValue());
        userService.saveUserWithPersonalDetails(currentUser, existingPersonalDetails);
        NotificationUtils.createSubmitSuccess("Personal Form Updated Successfully.");
    }

    private void savePersonalDetails() {
        User currentUser = userService.getCurrentUser();
        PersonalDetails personalDetailsFromForm = createPersonalDetailsFromForm();
        personalDetailsFromForm.setUser(currentUser);
        currentUser.setPersonalDetails(personalDetailsFromForm);
        userService.saveUserWithPersonalDetails(currentUser, personalDetailsFromForm);
        NotificationUtils.createSubmitSuccess("Personal Form Saved Successfully.");
    }

    private PersonalDetails createPersonalDetailsFromForm() {
        PersonalDetails personalDetails = new PersonalDetails();
        personalDetails.setFirstName(firstName.getValue());
        personalDetails.setLastName(lastName.getValue());
        personalDetails.setBirthday(birthday.getValue());
        personalDetails.setPhoneNumber(phoneNumber.getValue());
        personalDetails.setEmail(email.getValue());
        personalDetails.setGender(gender.getValue());
        return personalDetails;
    }
}
