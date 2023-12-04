package com.nasc.application.views.createstudents;

import com.nasc.application.data.model.AcademicYearEntity;
import com.nasc.application.data.model.DepartmentEntity;
import com.nasc.application.data.model.Role;
import com.nasc.application.data.model.User;
import com.nasc.application.services.AcademicYearService;
import com.nasc.application.services.DepartmentService;
import com.nasc.application.services.UserService;
import com.nasc.application.views.MainLayout;
import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.exceptions.CsvException;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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
        upload.addSucceededListener(event -> handleFileUpload(event.getMIMEType(), buffer.getInputStream()));

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

        createUserButton = new Button("Create Users", event -> createUserButtonClicked(buffer.getInputStream()));
        createUserButton.setEnabled(verifyCheckbox.getValue());

        createRoleComboBox();
        createDepartmentComboBox();
        createAcademicYearComboBox();

        academicYearComboBox.setEnabled(false);
        HorizontalLayout horizontalLayout = new HorizontalLayout(roleComboBox, departmentComboBox, academicYearComboBox);

        // Horizontal layout for checkbox and button
        HorizontalLayout checkboxButtonLayout = new HorizontalLayout(verifyCheckbox, createUserButton);
        checkboxButtonLayout.setWidthFull();
        checkboxButtonLayout.setJustifyContentMode(JustifyContentMode.END);
        checkboxButtonLayout.setAlignItems(Alignment.CENTER);

        // Initialize the state of the "Create Users" button based on the initial value of the verifyCheckbox
        createUserButton.setEnabled(verifyCheckbox.getValue());

        add(upload, horizontalLayout, checkboxButtonLayout, userGrid);
    }


    private void handleFileUpload(String mimeType, InputStream stream) {
        if ("text/csv".equals(mimeType)) {
            try (Reader reader = new InputStreamReader(stream)) {
                // Parse CSV file and process data
                List<User> users = readCSV(reader);

                // Use DataProvider to set items in the grid
                userGrid.setDataProvider(DataProvider.fromStream(users.stream()));

                Notification.show("File uploaded successfully", 3000, Notification.Position.TOP_CENTER);
            } catch (Exception e) {
                Notification.show("Error processing the CSV file", 3000, Notification.Position.TOP_CENTER);
            }
        } else {
            Notification.show("Please select a valid CSV file", 3000, Notification.Position.TOP_CENTER);
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

            if (selectedRole == null) {
                Notification.show("Please select a role", 3000, Notification.Position.BOTTOM_END);
                return;
            }

            if (selectedDepartmentEntity == null) {
                Notification.show("Please select a department", 3000, Notification.Position.BOTTOM_END);
                return;
            }

            if (selectedRole == Role.STUDENT) {
                if (selectedAcademicYearEntity == null) {
                    Notification.show("Please select a academic year", 3000, Notification.Position.BOTTOM_END);
                    return;
                }
            }

            users.forEach(user -> {
                user.setRoles(Set.of(selectedRole));
                user.setPassword(passwordEncoder.encode(user.getPassword()));
                user.setDepartment(departmentComboBox.getValue());

                // Set academic year only for students
                if (selectedRole == Role.STUDENT) {
                    user.setAcademicYear(selectedAcademicYearEntity);
                }

            });

            userService.saveAll(users);
            Notification.show("Users created successfully", 3000, Notification.Position.TOP_CENTER);
        } catch (IOException | CsvException e) {
            throw new RuntimeException(e);
        }
    }

    private void createDepartmentComboBox() {
        departmentComboBox = new ComboBox<>("Select Department");
        departmentComboBox.setItemLabelGenerator(DepartmentEntity::getName);
        departmentComboBox.setItems(departmentService.findAll());
        departmentComboBox.setRequired(true);
        departmentComboBox.setRequiredIndicatorVisible(true);
    }

    private void createRoleComboBox() {
        roleComboBox = new ComboBox<>("Select Role");

        // Filter out excluded roles (EDITOR and ADMIN)
        roleComboBox.setItems(Arrays.stream(Role.values())
                .filter(role -> !Arrays.asList(Role.EDITOR, Role.ADMIN).contains(role))
                .collect(Collectors.toList()));

        roleComboBox.setRequired(true);
        roleComboBox.addValueChangeListener(event -> {
            Role selectedRole = event.getValue();
            academicYearComboBox.setEnabled(selectedRole == Role.STUDENT);
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
        academicYearComboBox.setRequired(true);
        academicYearComboBox.setRequiredIndicatorVisible(true);
        downloadSampleCsv();
    }

    private void downloadSampleCsv() {
        String sampleCsvContent = "username,registerNumber,email,password\nganesh,2122k1466,ganesh@mail.com,password123";
        Button button = new Button("Download Sample CSV");
        FileDownloadWrapper buttonWrapper = new FileDownloadWrapper(
                new StreamResource("sample.csv", () -> new ByteArrayInputStream(sampleCsvContent.getBytes())));
        buttonWrapper.wrapComponent(button);
        add(buttonWrapper);
    }

}