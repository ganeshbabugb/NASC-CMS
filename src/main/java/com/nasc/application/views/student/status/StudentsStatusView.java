package com.nasc.application.views.student.status;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.nasc.application.data.core.AcademicYearEntity;
import com.nasc.application.data.core.DepartmentEntity;
import com.nasc.application.data.core.User;
import com.nasc.application.data.core.enums.Role;
import com.nasc.application.data.core.enums.StudentSection;
import com.nasc.application.services.AcademicYearService;
import com.nasc.application.services.DepartmentService;
import com.nasc.application.services.UserService;
import com.nasc.application.utils.NotificationUtils;
import com.nasc.application.views.MainLayout;
import com.opencsv.CSVWriter;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.ComboBoxVariant;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.RolesAllowed;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@PageTitle("Students Status")
@Route(value = "students-status", layout = MainLayout.class)
@RolesAllowed({"HOD", "PROFESSOR"})
public class StudentsStatusView extends VerticalLayout {
    private final UserService userService;
    private final AcademicYearService academicYearService;
    private final DepartmentService departmentService;
    private final Button menuButton = new Button("Show/Hide Columns", FontAwesome.Solid.LIST_CHECK.create());
    private final Anchor downloadLink;
    private final ColumnToggleContextMenu columnToggleContextMenu = new ColumnToggleContextMenu(menuButton);
    private Grid<User> grid;
    private GridListDataView<User> gridListDataView;
    private Grid.Column<User> userColumn;
    private Grid.Column<User> RegisterNumberColumn;
    private final Button searchButton;
    private ComboBox<AcademicYearEntity> academicYearFilter;
    private ComboBox<DepartmentEntity> departmentFilter;
    private ComboBox<StudentSection> studentSectionFilter;
    //Layouts
    private HorizontalLayout filterLayout;
    private HorizontalLayout exportAndColumnListLayout;

    public StudentsStatusView(UserService userService,
                              AcademicYearService academicYearService,
                              DepartmentService departmentService
    ) {
        this.userService = userService;
        this.departmentService = departmentService;
        this.academicYearService = academicYearService;
        addClassName("students-status-view");
        setSizeFull();
        createDepartmentFilterComponent();
        createAcademicYearFilterComponent();
        createStudentSectionComboBox();
        createExportAndColumnListLayout();
        downloadLink = createDownloadLink();

        menuButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);

        exportAndColumnListLayout.add(menuButton, downloadLink);
        createGrid();
        createFilterLayout();

        searchButton = new Button("Search");
        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        searchButton.addClickListener(buttonClickEvent -> handleAcademicYearFilterChange());

        filterLayout.add(departmentFilter, academicYearFilter, studentSectionFilter, searchButton);
        add(filterLayout, exportAndColumnListLayout, grid);
    }

    private void createFilterLayout() {
        filterLayout = new HorizontalLayout();
        filterLayout.setSpacing(true);
        filterLayout.setAlignItems(Alignment.BASELINE);
    }

    private static FontAwesome.Regular.Icon getThumbsUpIcon() {
        FontAwesome.Regular.Icon icon = FontAwesome.Regular.THUMBS_UP.create();
        icon.getStyle().set("padding", "var(--lumo-space-xs");
        return icon;
    }

    private static FontAwesome.Regular.Icon getThumbsDownIcon() {
        FontAwesome.Regular.Icon icon = FontAwesome.Regular.THUMBS_DOWN.create();
        icon.getStyle().set("padding", "var(--lumo-space-xs");
        return icon;
    }

    private void createExportAndColumnListLayout() {
        exportAndColumnListLayout = new HorizontalLayout();
        exportAndColumnListLayout.setWidthFull();
        exportAndColumnListLayout.setJustifyContentMode(JustifyContentMode.END);
        exportAndColumnListLayout.setAlignItems(Alignment.CENTER);
        exportAndColumnListLayout.setSpacing(true);
        exportAndColumnListLayout.getElement().getStyle().set("margin-bottom", "1em");
    }

    private Anchor createDownloadLink() {
        Anchor link = new Anchor();
        link.getElement().setAttribute("download", true);
        Button exportButton = new Button("Export to CSV", FontAwesome.Solid.FILE_EXPORT.create());
        exportButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        link.add(exportButton);
        return link;
    }

    private void createDepartmentFilterComponent() {
        List<DepartmentEntity> allDepartment = departmentService.findAll();
        departmentFilter = new ComboBox<>();
        departmentFilter.setLabel("Filter by Department");
        departmentFilter.setItems(allDepartment);
        departmentFilter.addThemeVariants(ComboBoxVariant.LUMO_SMALL);
        departmentFilter.setItemLabelGenerator(departmentEntity -> departmentEntity.getName() + " " + departmentEntity.getShortName());

    }

    private void createAcademicYearFilterComponent() {
        List<AcademicYearEntity> academicYears = academicYearService.findAll();
        academicYearFilter = new ComboBox<>();
        academicYearFilter.setLabel("Filter by Academic Year");
        academicYearFilter.setItems(academicYears);
        academicYearFilter.setItemLabelGenerator(this::generateAcademicYearLabel);
        academicYearFilter.addThemeVariants(ComboBoxVariant.LUMO_SMALL);
    }

    private void createStudentSectionComboBox() {
        StudentSection[] values = StudentSection.values();
        studentSectionFilter = new ComboBox<>("Filter By Student Section");
        studentSectionFilter.setItems(values);
        studentSectionFilter.setItemLabelGenerator(StudentSection::getDisplayName);
        studentSectionFilter.addThemeVariants(ComboBoxVariant.LUMO_SMALL);
    }

    private void createGrid() {
        createGridComponent();
        addColumnsToGrid();
        addFiltersToGrid();
    }

    private String generateAcademicYearLabel(AcademicYearEntity academicYear) {
        return academicYear.getStartYear() + " - " + academicYear.getEndYear();
    }

    private void handleAcademicYearFilterChange() {
        refreshGridData();
    }

    private static Component createFilterFilter(Consumer<String> filterChangeConsumer) {
        TextField textField = new TextField();
        textField.setValueChangeMode(ValueChangeMode.EAGER);
        textField.setClearButtonVisible(true);
        textField.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        textField.setWidthFull();
        textField.getStyle().set("max-width", "100%");
        // CASE IN SENSITIVE
        textField.addValueChangeListener(e -> filterChangeConsumer.accept(e.getValue().toLowerCase()));
        return textField;
    }

    private void addFiltersToGrid() {
        HeaderRow filterRow = grid.appendHeaderRow();

        // Username filter
        filterRow.getCell(userColumn)
                .setComponent(createFilterFilter(name -> gridListDataView.setFilter(user -> user
                        .getUsername()
                        .toLowerCase()
                        .contains(name))));

        filterRow.getCell(RegisterNumberColumn)
                .setComponent(createFilterFilter(name -> gridListDataView.setFilter(user -> user
                        .getRegisterNumber()
                        .toLowerCase()
                        .contains(name))));

        // Status filters
        addStatusFilter("Personal Details", "personalDetailsCompleted", filterRow);
        addStatusFilter("Address Details", "addressDetailsCompleted", filterRow);
        addStatusFilter("Bank Details", "bankDetailsCompleted", filterRow);

        ComboBox<String> allFormsFilter = createAllFormsFilter();
        allFormsFilter.addValueChangeListener(this::handleAllFormsFilterChange);
        filterRow.getCell(grid.getColumnByKey("allFormsCompleted")).setComponent(allFormsFilter);
    }

    private void addStatusFilter(String columnName, String propertyKey, HeaderRow filterRow) {
        ComboBox<String> statusFilter = createStatusFilter();
        statusFilter.addValueChangeListener(event -> handleStatusFilterChange(event, columnName));
        filterRow.getCell(grid.getColumnByKey(propertyKey)).setComponent(statusFilter);
    }

    private ComboBox<String> createStatusFilter() {
        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.setItems(Arrays.asList("Pending", "Success"));
        statusFilter.setPlaceholder("Filter");
        statusFilter.setClearButtonVisible(true);
        statusFilter.setWidth("100%");
        statusFilter.addThemeVariants(ComboBoxVariant.LUMO_SMALL);
        return statusFilter;
    }

    private void handleStatusFilterChange(HasValue.ValueChangeEvent<String> event, String columnName) {
        String filterValue = event.getValue();
        gridListDataView.removeFilters(); //Every Time Drop Down Change, It removes filters from grid.
        if (StringUtils.isBlank(filterValue)) {
            gridListDataView.removeFilters();
        } else {
            gridListDataView.addFilter(user -> areStatusesEqual(user, columnName, filterValue));
        }
    }

    private boolean areStatusesEqual(User user, String columnName, String filterValue) {
        switch (columnName) {
            case "Personal Details" -> {
                return Boolean.TRUE.equals(user.getPersonalDetailsCompleted()) &&
                        filterValue.equals("Success") || Boolean.FALSE.equals(user.getPersonalDetailsCompleted()) &&
                        filterValue.equals("Pending");
            }
            case "Address Details" -> {
                return Boolean.TRUE.equals(user.getAddressDetailsCompleted()) &&
                        filterValue.equals("Success") || Boolean.FALSE.equals(user.getAddressDetailsCompleted()) &&
                        filterValue.equals("Pending");
            }
            case "Bank Details" -> {
                return Boolean.TRUE.equals(user.getBankDetailsCompleted()) &&
                        filterValue.equals("Success") || Boolean.FALSE.equals(user.getBankDetailsCompleted()) &&
                        filterValue.equals("Pending");
            }
            default -> {
                return true;
            }
        }
    }

    private ComboBox<String> createAllFormsFilter() {
        ComboBox<String> allFormsFilter = new ComboBox<>();
        allFormsFilter.setItems(Arrays.asList("Complete", "Incomplete"));
        allFormsFilter.setPlaceholder("Filter");
        allFormsFilter.setClearButtonVisible(true);
        allFormsFilter.setWidth("100%");
        allFormsFilter.addThemeVariants(ComboBoxVariant.LUMO_SMALL);
        return allFormsFilter;
    }

    private void handleAllFormsFilterChange(HasValue.ValueChangeEvent<String> event) {
        String filterValue = event.getValue();
        gridListDataView.removeFilters(); // Every Time Drop Down Change, It removes filters from the grid.
        if (StringUtils.isBlank(filterValue)) {
            gridListDataView.removeFilters();
        } else {
            gridListDataView.addFilter(user -> areAllFormsComplete(user, filterValue));
        }
    }

    private boolean areAllFormsComplete(User user, String filterValue) {
        boolean allFormsComplete = user.isFormsCompleted();
        return (allFormsComplete && "Complete".equalsIgnoreCase(filterValue)) ||
                (!allFormsComplete && "Incomplete".equalsIgnoreCase(filterValue));
    }

    private void createGridComponent() {
        grid = new Grid<>();
        grid.setHeight("100%");
        grid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_COLUMN_BORDERS);
    }

    private void refreshGridData() {
        AcademicYearEntity academicYearEntity = academicYearFilter.getValue();
        StudentSection studentSection = studentSectionFilter.getValue();
        DepartmentEntity department = departmentFilter.getValue();

        if (academicYearEntity != null && studentSection != null && department != null) {
            List<User> users = getStudentsByRoleForLoggedInUserDepartment(department, academicYearEntity, studentSection);
            if (users.isEmpty()) {
                if (users.isEmpty()) {
                    NotificationUtils.showWarningNotification("No information recorded for the selected academic year");
                }
            }
            gridListDataView = grid.setItems(users);
            exportToCSV(users);
        }
    }

    private void addColumnsToGrid() {
        createUsernameColumn();
        createRegisterNumberColumn();
        createPersonalDetailsColumn();
        createAddressDetailsColumn();
        createBankDetailsColumn();
        createAllFormsCompletedColumn();
    }

    private void createUsernameColumn() {
        userColumn = grid.addColumn(User::getUsername)
                .setHeader("Username")
                .setKey("username")
                .setFrozen(true)
                .setComparator((User::getUsername));
    }

    private void createRegisterNumberColumn() {
        RegisterNumberColumn = grid.addColumn(User::getRegisterNumber)
                .setHeader("Register Number")
                .setKey("Register Number")
                .setFrozen(true)
                .setComparator((User::getRegisterNumber));
    }

    private void createPersonalDetailsColumn() {
        Grid.Column<User> personalDetailsColumn = grid.addColumn(new ComponentRenderer<>(user -> {
                    Span span = new Span(user.getPersonalDetailsCompleted() ? createIcon(VaadinIcon.CHECK) : createIcon(VaadinIcon.CLOCK),
                            new Span(user.getPersonalDetailsCompleted() ? "Success" : "Pending"));
                    span.getElement().setAttribute("theme", "badge " + (user.getPersonalDetailsCompleted() ? "success" : "pending"));
                    return span;
                }))
                .setHeader("Personal Details")
                .setKey("personalDetailsCompleted")
                .setComparator(User::getUsername);

        columnToggleContextMenu.addColumnToggleItem("Personal Details Completed", personalDetailsColumn);
    }

    private void createAddressDetailsColumn() {
        Grid.Column<User> addressDetailsColumn = grid.addColumn(new ComponentRenderer<>(user -> {
                    Span span = new Span(user.getAddressDetailsCompleted() ? createIcon(VaadinIcon.CHECK) : createIcon(VaadinIcon.CLOCK),
                            new Span(user.getAddressDetailsCompleted() ? "Success" : "Pending"));
                    span.getElement().setAttribute("theme", "badge " + (user.getAddressDetailsCompleted() ? "success" : "pending"));
                    return span;
                }))
                .setHeader("Address Details")
                .setKey("addressDetailsCompleted")
                .setComparator(User::getUsername);


        columnToggleContextMenu.addColumnToggleItem("Address Details Completed", addressDetailsColumn);
    }

    private void createBankDetailsColumn() {
        Grid.Column<User> bankDetailsColumn = grid.addColumn(new ComponentRenderer<>(user -> {
                    Span span = new Span(user.getBankDetailsCompleted() ? createIcon(VaadinIcon.CHECK) : createIcon(VaadinIcon.CLOCK),
                            new Span(user.getBankDetailsCompleted() ? "Success" : "Pending"));
                    span.getElement().setAttribute("theme", "badge " + (user.getBankDetailsCompleted() ? "success" : "pending"));
                    return span;
                }))
                .setHeader("Bank Details")
                .setKey("bankDetailsCompleted")
                .setComparator(User::getUsername);

        columnToggleContextMenu.addColumnToggleItem("Bank Details", bankDetailsColumn);
    }

    private void createAllFormsCompletedColumn() {
        Grid.Column<User> allFormsCompletedColumn = grid.addColumn(new ComponentRenderer<>(user -> {
                    boolean allFormsCompleted = user.isFormsCompleted();
                    Span span = new Span(allFormsCompleted ? getThumbsUpIcon() : getThumbsDownIcon(),
                            new Span(allFormsCompleted ? "Complete" : "Incomplete"));
                    span.getElement().setAttribute("theme", "badge " + (allFormsCompleted ? "success" : "error"));
                    return span;
                }))
                .setHeader("All Forms Completed")
                .setKey("allFormsCompleted")
                .setComparator(User::getUsername);

        columnToggleContextMenu.addColumnToggleItem("All Forms Completed", allFormsCompletedColumn);
    }

    private List<User> getStudentsByRoleForLoggedInUserDepartment(DepartmentEntity department,
                                                                  AcademicYearEntity academicYear,
                                                                  StudentSection studentSection
    ) {
        return userService.findStudentsByDepartmentAndRoleAndAcademicYearAndSection(
                department,
                Role.STUDENT,
                academicYear,
                studentSection
        );
    }

    private Icon createIcon(VaadinIcon vaadinIcon) {
        Icon icon = vaadinIcon.create();
        icon.getStyle().set("padding", "var(--lumo-space-xs");
        return icon;
    }

    private void exportToCSV(List<User> users) {

        try {
            AcademicYearEntity academicYearEntity = academicYearFilter.getValue();
            // Prepare data for export
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            try (CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(byteArrayOutputStream, StandardCharsets.UTF_8))) {

                // Write header
                String[] header = {
                        "Username", "Register Number", "Email", "Department", "Academic Year",
                        "Bank Name", "Account Holder Name", "Account Number", "IFSC Code", "Branch Name",
                        "Branch Address", "PAN Number", "First Name", "Last Name", "Phone Number",
                        "Birthday", "Gender", "Address", "Pin code", "City", "State", "Country"
                };
                csvWriter.writeNext(header);

                for (User user : users) {
                    List<String> data = new ArrayList<>();

                    // Common information
                    data.add(user.getUsername());
                    data.add(user.getRegisterNumber());
                    data.add(user.getEmail());
                    data.add(user.getDepartment().toString());
                    data.add(academicYearEntity.getStartYear() + " - " + academicYearEntity.getEndYear());

                    // Bank details (conditionally added)
                    if (Boolean.TRUE.equals(user.getBankDetailsCompleted())) {
                        data.add(user.getBankDetails().getBankName());
                        data.add(user.getBankDetails().getAccountHolderName());
                        data.add(user.getBankDetails().getAccountNumber());
                        data.add(user.getBankDetails().getIfscCode());
                        data.add(user.getBankDetails().getBranchName());
                        data.add(user.getBankDetails().getBranchAddress());
                        data.add(user.getBankDetails().getPanNumber());
                    } else {
                        // Add empty values or placeholders for bank details if not completed
                        data.addAll(Collections.nCopies(7, ""));
                    }

                    // Personal details
                    if (Boolean.TRUE.equals(user.getPersonalDetailsCompleted())) {
                        data.add(user.getPersonalDetails().getFirstName());
                        data.add(user.getPersonalDetails().getLastName());
                        data.add(user.getPersonalDetails().getPhoneNumber());
                        data.add(user.getPersonalDetails().getBirthday().toString());
                        data.add(user.getPersonalDetails().getGender());
                    } else {
                        data.addAll(Collections.nCopies(5, ""));
                    }

                    // Address details (conditionally added)
                    if (Boolean.TRUE.equals(user.getAddressDetailsCompleted())) {
                        data.add(user.getAddressDetails().getAddress());
                        data.add(user.getAddressDetails().getPinCode());
                        data.add(user.getAddressDetails().getCity());
                        data.add(user.getAddressDetails().getState());
                        data.add(user.getAddressDetails().getCountry());
                    } else {
                        // Add empty values or placeholders for address details if not completed
                        data.addAll(Collections.nCopies(5, ""));
                    }

                    // Convert the list to an array and write to CSV`
                    csvWriter.writeNext(data.toArray(new String[0]));
                }

                String fileName = "STUDENTS_"
                        + academicYearFilter.getValue().getStartYear()
                        + "_"
                        + academicYearFilter.getValue().getEndYear()
                        + ".csv";

                StreamResource resource = new StreamResource(fileName,
                        () -> new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));

                downloadLink.setHref(resource);

            }
        } catch (IOException e) {
            NotificationUtils.showErrorNotification("Error exporting data to CSV");
        }
    }

    private static class ColumnToggleContextMenu extends ContextMenu {
        public ColumnToggleContextMenu(Component target) {
            super(target);
            setOpenOnClick(true);
        }

        void addColumnToggleItem(String label, Grid.Column<User> column) {
            MenuItem menuItem = this.addItem(label, e -> column.setVisible(e.getSource().isChecked()));
            menuItem.setCheckable(true);
            menuItem.setChecked(column.isVisible());
            menuItem.setKeepOpen(true);
        }
    }
}
