package com.nasc.application.views.forms.adderss;

import com.nasc.application.data.core.AddressDetails;
import com.nasc.application.data.core.User;
import com.nasc.application.services.CountryService;
import com.nasc.application.services.StateService;
import com.nasc.application.services.UserService;
import com.nasc.application.utils.NotificationUtils;
import com.nasc.application.views.MainLayout;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;

@PageTitle("Address Form")
@Route(value = "address-form", layout = MainLayout.class)
@RolesAllowed({"STUDENT", "PROFESSOR", "HOD"})
public class AddressFormView extends Composite<VerticalLayout> {
    private final VerticalLayout layoutColumn2 = new VerticalLayout();
    private final FormLayout formLayout2Col = new FormLayout();
    private final HorizontalLayout layoutRow = new HorizontalLayout();
    private final TextArea address = new TextArea();
    private final TextField pinCode = new TextField();
    private final TextField city = new TextField();
    private final Select<String> state = new Select<>();
    private final Select<String> country = new Select<>();
    private final UserService userService;
    private final BeanValidationBinder<AddressDetails> binder = new BeanValidationBinder<>(AddressDetails.class);
    Button saveButtonPrimary = new Button();

    public AddressFormView(CountryService countryService, StateService stateService, UserService userService) {
        this.userService = userService;
        binder.bindInstanceFields(this);
        initLayout(countryService, stateService);
        initFormWithExistingDetails();
    }

    private void initLayout(CountryService countryService, StateService stateService) {

        List<String> allCountries = countryService.getAllCountries();
        List<String> allStates = stateService.getAllStates();

        H3 h3 = new H3("Address Form");

        address.setLabel("Address");
        address.setRequiredIndicatorVisible(true);

        pinCode.setLabel("Pin Code");
        pinCode.setRequiredIndicatorVisible(true);

        city.setLabel("City");
        city.setRequiredIndicatorVisible(true);

        // Set up styles and layout properties
        getContent().setWidthFull();
        getContent().getStyle().set("flex-grow", "1");
        getContent().setJustifyContentMode(JustifyContentMode.START);
        getContent().setAlignItems(Alignment.CENTER);
        layoutColumn2.setWidthFull();
        layoutColumn2.setMaxWidth("800px");
        layoutColumn2.setSpacing(true);

        // Set up components
        h3.getStyle().set("margin-bottom", "20px");
        address.setWidth("100%");
        formLayout2Col.setWidth("100%");
        pinCode.setWidth("100%");

        country.setLabel("Country");
        country.setItems(allCountries);
        country.setPlaceholder("Select Country");
        country.setRequiredIndicatorVisible(true);
        country.getStyle().setWidth("100%");

        state.setLabel("State");
        state.setPlaceholder("Select State");
        state.setItems(allStates);
        state.setRequiredIndicatorVisible(true);
        state.getStyle().setWidth("100%");

        layoutRow.setSpacing(true);

        // Customize button styles
        saveButtonPrimary.setText("Save");
        saveButtonPrimary.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        binder.addStatusChangeListener(e -> saveButtonPrimary.setEnabled(binder.isValid()));

        saveButtonPrimary.addClickListener(
                event -> {
                    try {
                        binder.writeBean(createAddressDetailsFromForm());
                        if (isAddressDetailsAlreadySaved()) {
                            updateAddressDetails();
                        } else {
                            saveAddressDetails();
                        }
                    } catch (ValidationException e) {
                        Notification.show(e.getMessage());
                    }
                });

        // Build the layout hierarchy
        getContent().add(layoutColumn2);
        layoutColumn2.add(h3, address, formLayout2Col, layoutRow);
        formLayout2Col.add(pinCode, city, country, state);
        layoutRow.add(saveButtonPrimary);
    }

    private void initFormWithExistingDetails() {
        User currentUser = userService.getCurrentUser();
        AddressDetails existingAddressDetails = currentUser.getAddressDetails();

        if (existingAddressDetails != null) {
            populateForm(existingAddressDetails);
            saveButtonPrimary.setText("Update");
        }
    }

    private void populateForm(AddressDetails existingAddress) {
        address.setValue(existingAddress.getAddress());
        pinCode.setValue(existingAddress.getPinCode());
        city.setValue(existingAddress.getCity());
        country.setValue(existingAddress.getCountry());
        state.setValue(existingAddress.getState());
    }

    private boolean isAddressDetailsAlreadySaved() {
        return userService.getCurrentUser().getBankDetails() != null;
    }

    private void updateAddressDetails() {
        User currentUser = userService.getCurrentUser();
        AddressDetails existingAddressDetails = currentUser.getAddressDetails();
        existingAddressDetails.setAddress(address.getValue());
        existingAddressDetails.setPinCode(pinCode.getValue());
        existingAddressDetails.setCity(city.getValue());
        existingAddressDetails.setState(state.getValue());
        existingAddressDetails.setCountry(country.getValue());
        userService.saveUserWithAddressDetails(currentUser, existingAddressDetails);
        NotificationUtils.createSubmitSuccess("Address details updated successfully");
    }

    private void saveAddressDetails() {
        User currentUser = userService.getCurrentUser();
        AddressDetails addressDetailsFromForm = createAddressDetailsFromForm();
        addressDetailsFromForm.setUser(currentUser);
        currentUser.setAddressDetails(addressDetailsFromForm);
        userService.saveUserWithAddressDetails(currentUser, addressDetailsFromForm);
        NotificationUtils.createSubmitSuccess("Address details saved successfully");
    }

    private AddressDetails createAddressDetailsFromForm() {
        AddressDetails addressDetails = new AddressDetails();
        addressDetails.setAddress(address.getValue());
        addressDetails.setPinCode(pinCode.getValue());
        addressDetails.setCity(city.getValue());
        addressDetails.setState(state.getValue());
        addressDetails.setCountry(country.getValue());
        return addressDetails;
    }

}