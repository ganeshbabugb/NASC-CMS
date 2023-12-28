package com.nasc.application.views.auth.create;

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
import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.exceptions.CsvException;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.SucceededEvent;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.vaadin.olli.FileDownloadWrapper;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@PageTitle("Create User Account")
@Route(value = "create-user", layout = MainLayout.class)
@RolesAllowed({"EDITOR", "ADMIN"})
@JsModule("./recipe/copytoclipboard/copytoclipboard.js")
@Slf4j
public class CreateUsers extends VerticalLayout {

    private final DepartmentService departmentService;
    private final AcademicYearService academicYearService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final Upload upload;
    private final Grid<User> userGrid;
    private final Checkbox verifyCheckbox;
    private final Button createUserButton;
    private ComboBox<Role> roleComboBox;
    private ComboBox<DepartmentEntity> departmentComboBox;
    private ComboBox<AcademicYearEntity> academicYearComboBox;
    private ComboBox<StudentSection> studentSectionComboBox;

    public CreateUsers(UserService userService,
                       PasswordEncoder passwordEncoder,
                       DepartmentService departmentService,
                       AcademicYearService academicYearService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.departmentService = departmentService;
        this.academicYearService = academicYearService;

        // Title for the view
        add(new H3("Create Users"));

        // Helper text for upload
        add(new H3("Upload CSV with user details"));
        add(new H3("Columns: username, registerNumber, email, password"));

        MemoryBuffer buffer = new MemoryBuffer();
        upload = new Upload(buffer);
        upload.setAcceptedFileTypes("text/csv");
        upload.addSucceededListener(event -> handleFileUpload(event, buffer.getInputStream()));

        userGrid = new Grid<>(User.class);
        userGrid.removeAllColumns();

        //This theme has a compact interface
        userGrid.addThemeVariants(GridVariant.LUMO_COMPACT);

        userGrid.addColumn(User::getUsername).setHeader("Username").setSortable(true).setComparator(User::getUsername);
        userGrid.addColumn(User::getRegisterNumber).setHeader("Register Number").setSortable(true).setComparator(User::getRegisterNumber);
        userGrid.addColumn(User::getEmail).setHeader("Email").setSortable(true).setComparator(User::getEmail);
        userGrid.addColumn(User::getPassword).setHeader("Password").setSortable(true).setComparator(User::getPassword);

        verifyCheckbox = new Checkbox("Verified");
        verifyCheckbox.addValueChangeListener(this::verifyCheckboxChanged);

        createUserButton = new Button("Create Users", event -> {
            try {
                if (buffer.getInputStream() == null || buffer.getInputStream().available() == 0) {
                    NotificationUtils.showInfoNotification("Please upload a CSV file before processing.");
                    return;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            createUserButtonClicked(buffer.getInputStream());
        });
        createUserButton.setEnabled(verifyCheckbox.getValue());

        createRoleComboBox();
        createDepartmentComboBox();
        createAcademicYearComboBox();
        createStudentSectionComboBox();
        downloadSampleCsv();

        academicYearComboBox.setVisible(false);
        studentSectionComboBox.setVisible(false);
        HorizontalLayout horizontalLayout = new HorizontalLayout(roleComboBox, departmentComboBox, academicYearComboBox, studentSectionComboBox);

        // Horizontal layout for checkbox and button
        HorizontalLayout checkboxButtonLayout = new HorizontalLayout(verifyCheckbox, createUserButton);
        checkboxButtonLayout.setWidthFull();
        checkboxButtonLayout.setJustifyContentMode(JustifyContentMode.END);
        checkboxButtonLayout.setAlignItems(Alignment.CENTER);

        // Initialize the state of the "Create Users" button based on the initial value of the verifyCheckbox
        createUserButton.setEnabled(verifyCheckbox.getValue());

        add(upload, horizontalLayout, checkboxButtonLayout, userGrid);
    }

    private void handleFileUpload(SucceededEvent event, InputStream stream) {
        String mimeType = event.getMIMEType();
        if ("text/csv".equals(mimeType)) {
            try (Reader reader = new InputStreamReader(stream)) {
                // Parse CSV file and process data
                List<User> users = readCSV(reader);

                // Use DataProvider to set items in the grid
                userGrid.setDataProvider(DataProvider.fromStream(users.stream()));
                NotificationUtils.createUploadSuccess("File uploaded successfully", event.getFileName());
            } catch (Exception e) {
                NotificationUtils.showErrorNotification("Error processing the CSV file");
            }
        } else {
            NotificationUtils.showErrorNotification("Please select a valid CSV file");
        }
    }

    private List<User> readCSV(Reader reader) throws IOException, CsvException {
        // Use OpenCSV to parse the CSV file
        try (CSVReader csvReader = new CSVReader(reader)) {
            HeaderColumnNameMappingStrategy<User> strategy = new HeaderColumnNameMappingStrategy<>();
            strategy.setType(User.class);
            return new CsvToBeanBuilder<User>(csvReader)
                    .withMappingStrategy(strategy)
                    .build()
                    .parse();
        }
    }

    private void verifyCheckboxChanged(HasValue.ValueChangeEvent<Boolean> event) {
        boolean isVerified = verifyCheckbox.getValue();
        createUserButton.setEnabled(isVerified);
    }

    private void createUserButtonClicked(InputStream inputStream) {

        try (Reader reader = new InputStreamReader(inputStream)) {
            List<User> users = readCSV(reader);

            Role selectedRole = roleComboBox.getValue();
            DepartmentEntity selectedDepartmentEntity = departmentComboBox.getValue();
            AcademicYearEntity selectedAcademicYearEntity = academicYearComboBox.getValue();
            StudentSection studentSection = studentSectionComboBox.getValue();

            // Clear previous error messages
            clearErrorMessages();

            // Validate the selected values
            boolean isValid = true;

            if (selectedRole == null) {
                setComboBoxError(roleComboBox, "Please select a role");
                isValid = false;
            }

            if (selectedDepartmentEntity == null) {
                setComboBoxError(departmentComboBox, "Please select a department");
                isValid = false;
            }

            if (selectedRole == Role.STUDENT) {
                if (selectedAcademicYearEntity == null) {
                    setComboBoxError(academicYearComboBox, "Please select an academic year");
                    isValid = false;
                }

                if (studentSection == null) {
                    setComboBoxError(studentSectionComboBox, "Please select an student section");
                    isValid = false;
                }
            }

            if (!isValid) {
                // Show a common error message or handle the invalid state as needed
                NotificationUtils.showInfoNotification("Please correct the highlighted fields");
                return;
            }

            // Check for duplicate register numbers
            List<String> existingRegisterNumbers = userService.findExistingRegisterNumbers(users.stream()
                    .map(User::getRegisterNumber)
                    .collect(Collectors.toList()));

            List<String> duplicateRegisterNumbers = existingRegisterNumbers.stream()
                    .filter(registerNumber -> users.stream().anyMatch(user -> user.getRegisterNumber().equals(registerNumber)))
                    .toList();

            if (!duplicateRegisterNumbers.isEmpty()) {
                // Display a dialog with information about duplicate register numbers
                showDuplicateDialog(duplicateRegisterNumbers);
                return;
            }

            users.forEach(user -> {
                user.setRoles(Set.of(selectedRole));
                user.setPassword(passwordEncoder.encode(user.getPassword()));
                user.setDepartment(departmentComboBox.getValue());

                // Set academic year only for students
                if (selectedRole == Role.STUDENT) {
                    user.setAcademicYear(selectedAcademicYearEntity);
                    user.setStudentSection(studentSection);
                }

            });

            userService.saveAll(users);
            NotificationUtils.showSuccessNotification("Users created successfully");
        } catch (IOException | CsvException e) {
            throw new RuntimeException(e);
        }
    }

    private void createDepartmentComboBox() {
        departmentComboBox = new ComboBox<>("Select Department");
        departmentComboBox.setItemLabelGenerator(DepartmentEntity::getName);
        departmentComboBox.setItems(departmentService.findAll());
        departmentComboBox.setRequired(true);
    }

    private void createRoleComboBox() {
        roleComboBox = new ComboBox<>("Select Role");

        // Filter out excluded roles (EDITOR and ADMIN)
        roleComboBox.setItems(Arrays.stream(Role.values())
                .filter(role -> !Arrays.asList(Role.EDITOR, Role.ADMIN).contains(role))
                .collect(Collectors.toList()));
        roleComboBox.setItemLabelGenerator(Role::getDisplayName);
        roleComboBox.setRequired(true);
        roleComboBox.addValueChangeListener(event -> {
            Role selectedRole = event.getValue();
            if (selectedRole == Role.STUDENT) {
                // If student role is selected, enable the academic year ComboBox
                academicYearComboBox.setVisible(true);
                academicYearComboBox.setRequired(true);

                studentSectionComboBox.setVisible(true);
                studentSectionComboBox.setRequired(true);
            } else {
                // If any other role is selected, disable the academic year ComboBox and clear its value
                academicYearComboBox.setVisible(false);
                academicYearComboBox.setRequired(false);
                academicYearComboBox.clear();

                studentSectionComboBox.setVisible(false);
                studentSectionComboBox.setRequired(false);
                studentSectionComboBox.clear();
            }
        });
    }

    //Utility
    private String generateAcademicYearLabel(AcademicYearEntity academicYear) {
        return academicYear.getStartYear() + " - " + academicYear.getEndYear();
    }

    private void createAcademicYearComboBox() {
        academicYearComboBox = new ComboBox<>("Select Academic Year");
        academicYearComboBox.setItemLabelGenerator(this::generateAcademicYearLabel);
        academicYearComboBox.setItems(academicYearService.findAll());
    }

    private void createStudentSectionComboBox() {
        StudentSection[] values = StudentSection.values();
        studentSectionComboBox = new ComboBox<>("Select Student Section");
        studentSectionComboBox.setItems(values);
        studentSectionComboBox.setItemLabelGenerator(StudentSection::getDisplayName);
    }

    private void downloadSampleCsv() {
        String sampleCsvContent = "username,registerNumber,email,password\nganesh,2122k1466,ganesh@mail.com,password123";
        Button button = new Button("Download Sample CSV");
        FileDownloadWrapper buttonWrapper = new FileDownloadWrapper(
                new StreamResource("sample.csv", () -> new ByteArrayInputStream(sampleCsvContent.getBytes())));
        buttonWrapper.wrapComponent(button);
        add(buttonWrapper);
    }

    private void showDuplicateDialog(List<String> duplicateRegisterNumbers) {
        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);

        Button copyBtn = new Button("Copy", FontAwesome.Regular.CLIPBOARD.create());
        copyBtn.addClickListener(event ->
                {
                    UI.getCurrent().getPage().executeJs("window.copyToClipboard($0)",
                            duplicateRegisterNumbers
                                    .stream()
                                    .collect(Collectors.joining("\n", "", "\n")));
                    NotificationUtils.showInfoNotification("Register numbers copied to clipboard");
                }
        );

        dialog.getFooter().add(copyBtn);

        Button cancelBtn = new Button("Close", event -> dialog.close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(cancelBtn);

        dialog.setHeaderTitle("Duplicate Account's Detected");

        Grid<String> grid = new Grid<>();
        grid.setItems(duplicateRegisterNumbers);

        grid.addColumn(String::valueOf).setHeader("Register Number");

        dialog.add(grid);
        dialog.open();
    }

    // Helpers
    private void clearErrorMessages() {
        departmentComboBox.setErrorMessage(null);
        roleComboBox.setErrorMessage(null);
        academicYearComboBox.setErrorMessage(null);
        studentSectionComboBox.setErrorMessage(null);
    }

    private void setComboBoxError(ComboBox<?> comboBox, String errorMessage) {
        comboBox.setInvalid(true);
        comboBox.setErrorMessage(errorMessage);
    }
}
