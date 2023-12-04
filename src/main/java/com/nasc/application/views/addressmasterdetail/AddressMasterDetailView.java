package com.nasc.application.views.addressmasterdetail;

import com.nasc.application.data.model.AddressDetails;
import com.nasc.application.services.CountryService;
import com.nasc.application.services.SampleAddressService;
import com.nasc.application.services.StateService;
import com.nasc.application.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.*;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.Comparator;
import java.util.Optional;

@PageTitle("Address Master Detail")
@Route(value = "student-master-detail/:sampleAddressID?/:action?(edit)", layout = MainLayout.class)
@RolesAllowed("HOD")
@Slf4j
public class AddressMasterDetailView extends Div implements BeforeEnterObserver, BeforeLeaveObserver {

    private final String SAMPLEADDRESS_ID = "sampleAddressID";
    private final String SAMPLEADDRESS_EDIT_ROUTE_TEMPLATE = "student-master-detail/%s/edit";
    private final Grid<AddressDetails> grid = new Grid<>(AddressDetails.class, false);
    private final BeanValidationBinder<AddressDetails> binder;
    private final CountryService countryService;
    private final StateService stateService;
    private TextField username;
    private TextField city;
    private TextField registerNumber;
    private TextField address;
    private final Button cancel = new Button("Cancel");
    private final Button save = new Button("Save");
    private TextField pinCode;
    private Select<String> state;
    private final SampleAddressService sampleAddressService;
    private Select<String> country;
    private AddressDetails addressDetails;

    public AddressMasterDetailView(SampleAddressService sampleAddressService,
                                   CountryService countryService,
                                   StateService stateService) {
        this.sampleAddressService = sampleAddressService;
        this.countryService = countryService;
        this.stateService = stateService;
        addClassNames("address-master-detail-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn(AddressDetails -> AddressDetails.getUser().getUsername())
                .setHeader("User Name")
                .setAutoWidth(true)
                .setComparator(Comparator.comparing(addressDetails -> addressDetails.getUser().getUsername()));

        grid.addColumn(AddressDetails -> AddressDetails.getUser().getRegisterNumber())
                .setHeader("Register Number")
                .setAutoWidth(true)
                .setComparator(Comparator.comparing(addressDetails1 -> addressDetails1.getUser().getRegisterNumber()));


        grid.addColumn("address").setAutoWidth(true);
        grid.addColumn("pinCode").setAutoWidth(true);
        grid.addColumn("city").setAutoWidth(true);
        grid.addColumn("state").setAutoWidth(true);
        grid.addColumn("country").setAutoWidth(true);
        grid.setItems(query -> sampleAddressService.list(
                        PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(SAMPLEADDRESS_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(AddressMasterDetailView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(AddressDetails.class);

        // Bind fields. This is where you'd define e.g. validation rules
        binder.bindInstanceFields(this);

        binder.forField(state)
                .bind(AddressDetails::getState, AddressDetails::setState);

        binder.forField(country)
                .bind(AddressDetails::getCountry, AddressDetails::setCountry);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                binder.writeBean(this.addressDetails);
                sampleAddressService.update(this.addressDetails);

                UI.getCurrent().navigate(AddressMasterDetailView.class);

                refreshGrid();
                Notification.show("Data updated");
                clearForm();
                log.info("Data updated");
            } catch (ObjectOptimisticLockingFailureException exception) {
                Notification n = Notification.show(
                        "Error updating the data. Somebody else has updated the record while you were making changes.");
                n.setPosition(Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (ValidationException validationException) {
                Notification.show("Failed to update the data. Check again that all values are valid");
            }
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> sampleAddressId = event.getRouteParameters().get(SAMPLEADDRESS_ID).map(Long::parseLong);
        if (sampleAddressId.isPresent()) {
            Optional<AddressDetails> sampleAddressFromBackend = sampleAddressService.get(sampleAddressId.get());
            if (sampleAddressFromBackend.isPresent()) {
                populateForm(sampleAddressFromBackend.get());
            } else {
                Notification.show(
                        String.format("The requested sampleAddress was not found, ID = %s", sampleAddressId.get()),
                        3000, Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(AddressMasterDetailView.class);
            }
        }
    }

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        username = new TextField("User Name");
        registerNumber = new TextField("Register Number");
        address = new TextField("address");
        pinCode = new TextField("Postal Code");
        city = new TextField("City");

        state = new Select<>();
        state.setLabel("State");
        state.setItems(stateService.getAllStates());
        state.setPlaceholder("Select State");
        state.setRequiredIndicatorVisible(true);

        country = new Select<>();
        country.setLabel("Country");
        country.setItems(countryService.getAllCountries());
        country.setPlaceholder("Select Country");
        country.setRequiredIndicatorVisible(true);

        username.setEnabled(false);
        registerNumber.setEnabled(false);

        formLayout.add(username, registerNumber, address, pinCode, city, state, country);

        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("button-layout");
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save, cancel);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        splitLayout.addToPrimary(wrapper);
        wrapper.add(grid);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(AddressDetails value) {
        this.addressDetails = value;

        // Bind the rest of the fields
        binder.readBean(this.addressDetails);

        // Set values for disabled fields
        if (value != null && value.getUser() != null) {
            username.setValue(value.getUser().getUsername());
            registerNumber.setValue(value.getUser().getRegisterNumber());
        } else {
            // Clear values if no addressDetails provided
            username.clear();
            registerNumber.clear();
        }
    }

    @Override
    public void beforeLeave(BeforeLeaveEvent event) {
        // only prevent if certain condition is met. In my example I prevent navigation if binder has changes.
        if (binder.hasChanges()) {

            // prevents navigation
            BeforeLeaveEvent.ContinueNavigationAction action = event.postpone();

            // after you prevented the navigation, you are still able to proceed with the navigation, by using action.proceed();
            // it is good practice IMO to give the user the choice to navigate away anyway, if they wish so.
            ConfirmDialog dialog = new ConfirmDialog(
                    "Unsaved Changes",
                    "You have unsaved changes. Are you sure you want to leave this anyway?",
                    "Yes", confirmEvent -> {
                // do the navigation anyway
                action.proceed();
            },
                    "No", cancelEvent -> {
                // navigation was already prevented with event.postpone() so nothing has to be done here
            }
            );
            dialog.open();
        }
    }
}