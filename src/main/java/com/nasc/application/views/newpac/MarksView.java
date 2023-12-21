package com.nasc.application.views.newpac;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.nasc.application.data.model.AcademicYearEntity;
import com.nasc.application.data.model.DepartmentEntity;
import com.nasc.application.data.model.SubjectEntity;
import com.nasc.application.data.model.User;
import com.nasc.application.data.model.dto.StudentMarksDTO;
import com.nasc.application.data.model.dto.StudentSubjectInfo;
import com.nasc.application.data.model.enums.ExamType;
import com.nasc.application.data.model.enums.Role;
import com.nasc.application.data.model.enums.Semester;
import com.nasc.application.services.*;
import com.nasc.application.utils.NotificationUtils;
import com.nasc.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.ComboBoxVariant;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.gridpro.GridPro;
import com.vaadin.flow.component.gridpro.GridProVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;
import software.xdev.vaadin.grid_exporter.GridExporter;
import software.xdev.vaadin.grid_exporter.column.ColumnConfigurationBuilder;
import software.xdev.vaadin.grid_exporter.jasper.format.PdfFormat;
import software.xdev.vaadin.grid_exporter.jasper.format.XlsxFormat;

import java.util.*;
import java.util.function.Consumer;

@Route(value = "marks", layout = MainLayout.class)
@RolesAllowed({"HOD", "ADMIN", "PROFESSOR"})
@PageTitle("Marks")
@CssImport(
        themeFor = "vaadin-grid",
        value = "./recipe/gridcell/grid-cell.css"
)
@Slf4j
public class MarksView extends VerticalLayout {

    //  TODO Give Correct Meaning full names for all the key for column
    //  TODO AVOID UNWANTED SPACE FOR COLUMN

    // Service
    private final MarksService marksService;
    private final DepartmentService departmentService;
    private final AcademicYearService academicYearService;
    Button menuButton = new Button("Show/Hide Columns");
    private final SubjectService subjectService;
    private final UserService userService;
    // Grid
    private final GridPro<StudentMarksDTO> marksGrid = new GridPro<>(StudentMarksDTO.class);
    private GridListDataView<StudentMarksDTO> dataProvider;
    // Combobox
    private final ComboBox<ExamType> examTypeComboBox = new ComboBox<>("Select Exam Type");
    private final ComboBox<Semester> semesterComboBox = new ComboBox<>("Select Semester");
    private final ComboBox<DepartmentEntity> departmentComboBox = new ComboBox<>("Select Department");
    private final ComboBox<AcademicYearEntity> academicYearComboBox = new ComboBox<>("Select Academic Year");

    // UTILS
    private final ColumnToggleContextMenu contextMenu = new ColumnToggleContextMenu(menuButton);
    private final ColumnConfigurationBuilder columnConfigurationBuilder = new ColumnConfigurationBuilder();
    private final HeaderRow headerRow;
    private GridExporter<StudentMarksDTO> gridExporter;

    public MarksView(MarksService marksService,
                     DepartmentService departmentService,
                     AcademicYearService academicYearService,
                     SubjectService subjectService,
                     UserService userService
    ) {
        this.marksService = marksService;
        this.departmentService = departmentService;
        this.academicYearService = academicYearService;
        this.subjectService = subjectService;
        this.userService = userService;

        configureComboBoxes();

        marksGrid.removeAllColumns();
        marksGrid.setSizeFull();
        marksGrid.setEditOnClick(true);
        marksGrid.setSingleCellEdit(true);
        marksGrid.addThemeVariants(GridProVariant.LUMO_COMPACT);


        // marksGrid.setEnterNextRow(true);
        headerRow = marksGrid.appendHeaderRow();

        // Button
        Button searchButton = new Button("Search/Refresh");
        searchButton.addClickListener(e -> updateGridData());
        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        menuButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout FilterLayout = new HorizontalLayout();

        FilterLayout.setAlignItems(Alignment.BASELINE);
        FilterLayout.add(departmentComboBox, academicYearComboBox, semesterComboBox, examTypeComboBox, searchButton);


        HorizontalLayout horizontalLayout = new HorizontalLayout(menuButton,
                new Button("Export",
                        FontAwesome.Solid.FILE_EXPORT.create(),
                        e -> {
                            String fileName = departmentComboBox.getValue().getShortName()
                                    + "_"
                                    + academicYearComboBox.getValue().getStartYear() + "-" + academicYearComboBox.getValue().getEndYear()
                                    + "_"
                                    + semesterComboBox.getValue().getDisplayName()
                                    + "_"
                                    + examTypeComboBox.getValue().getDisplayName() + "_Marks";

                            XlsxFormat xlsxFormat = new XlsxFormat();
                            PdfFormat pdfFormat = new PdfFormat();

                            List<Grid.Column<StudentMarksDTO>> columns = marksGrid.getColumns();
                            columns.forEach(columnConfigurationBuilder::build);

                            gridExporter = GridExporter
                                    .newWithDefaults(marksGrid)
                                    .withFileName(fileName)
                                    .withAvailableFormats(xlsxFormat, pdfFormat)
                                    .withPreSelectedFormat(xlsxFormat)
                                    .withColumnConfigurationBuilder(columnConfigurationBuilder)
                            ;
                            gridExporter.open();
                        }
                ));
        horizontalLayout.setWidthFull();
        horizontalLayout.setJustifyContentMode(JustifyContentMode.END);
        horizontalLayout.setAlignItems(Alignment.CENTER);

        add(new H3("Marks View"), FilterLayout, horizontalLayout, marksGrid);

        setSizeFull();
    }

    private void configureComboBoxes() {
        List<AcademicYearEntity> academicYears = academicYearService.findAll();
        List<DepartmentEntity> departments = departmentService.findAll();
        Semester[] semesters = Semester.values();
        ExamType[] examTypes = ExamType.values();

        semesterComboBox.setItems(semesters);
        semesterComboBox.setItemLabelGenerator(Semester::getDisplayName);

        examTypeComboBox.setItems(examTypes);
        examTypeComboBox.setItemLabelGenerator(ExamType::getDisplayName);

        academicYearComboBox.setItems(academicYears);
        academicYearComboBox.setItemLabelGenerator(item -> item.getStartYear() + " - " + item.getEndYear());

        departmentComboBox.setItems(departments);
        departmentComboBox.setItemLabelGenerator(item -> item.getName() + " - " + item.getShortName());
    }

    private static Component createFilterHeader(Consumer<String> filterChangeConsumer) {
        TextField textField = new TextField();
        textField.setValueChangeMode(ValueChangeMode.EAGER);
        textField.setClearButtonVisible(true);
        textField.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        textField.setWidthFull();
        textField.getStyle().set("max-width", "100%");

        // CASE IN SENSITIVE
        textField.addValueChangeListener(e -> filterChangeConsumer.accept(e.getValue().toLowerCase()));

        VerticalLayout layout = new VerticalLayout(textField);
        layout.getThemeList().clear();
        layout.getThemeList().add("spacing-xs");
        return layout;
    }

    private void updateTotalPassFailCounts(List<StudentMarksDTO> allStudentMarks, PassFailCounts counts) {
        counts.totalPass = 0;
        counts.totalFail = 0;
        counts.totalPresent = 0;
        counts.totalAbsent = 0;
        counts.subjectPassCounts.clear();
        counts.subjectFailCounts.clear();
        counts.subjectPresentCounts.clear();
        counts.subjectAbsentCounts.clear();

        for (StudentMarksDTO studentMarksDTO : allStudentMarks) {
            updateTotalPassFailCounts(studentMarksDTO, counts);
        }
    }

    private void updateTotalPassFailCounts(StudentMarksDTO studentMarksDTO, PassFailCounts counts) {
        for (Map.Entry<SubjectEntity, StudentSubjectInfo> entry : studentMarksDTO.getSubjectInfoMap().entrySet()) {
            SubjectEntity subject = entry.getKey();
            StudentSubjectInfo subjectInfo = entry.getValue();

            if (subjectInfo.getMarks() != null && subjectInfo.getPassMarks() != null) {
                boolean passed = subjectInfo.getMarks() >= subjectInfo.getPassMarks();
                if (passed) {
                    counts.totalPass++;
                    counts.subjectPassCounts.put(subject, counts.subjectPassCounts.getOrDefault(subject, 0) + 1);
                } else {
                    counts.totalFail++;
                    counts.subjectFailCounts.put(subject, counts.subjectFailCounts.getOrDefault(subject, 0) + 1);
                }
            }

            // PRE & ABS
            if (subjectInfo.getAbsent() != null) {
                if (subjectInfo.getAbsent()) {
                    counts.totalAbsent++;
                    counts.subjectAbsentCounts.put(subject, counts.subjectAbsentCounts.getOrDefault(subject, 0) + 1);
                } else {
                    counts.totalPresent++;
                    counts.subjectPresentCounts.put(subject, counts.subjectPresentCounts.getOrDefault(subject, 0) + 1);
                }
            }
        }
    }

    private void updateGridData() {

        // Clearing all the list that where in previous one!
        contextMenu.removeAll();

        ExamType selectedExamType = examTypeComboBox.getValue();
        Semester selectedSemester = semesterComboBox.getValue();
        DepartmentEntity selectedDepartment = departmentComboBox.getValue();
        AcademicYearEntity selectedAcademicYear = academicYearComboBox.getValue();

        if (selectedExamType == null || selectedSemester == null || selectedDepartment == null || selectedAcademicYear == null) {
            NotificationUtils.showErrorNotification("Please select values for all the filters!");
            return;
        }

        // Set up grid data for all students
        List<User> allStudents = userService.findStudentsByDepartmentAndRoleAndAcademicYear(
                selectedDepartment,
                Role.STUDENT,
                selectedAcademicYear
        );
        List<StudentMarksDTO> allStudentMarks = new ArrayList<>();

        for (User student : allStudents) {
            // Fetch marks for each student
            List<StudentMarksDTO> studentMarksList = marksService.getStudentMarksByFilters(
                    selectedExamType,
                    selectedSemester,
                    student
            );

            // Add the marks to the list
            allStudentMarks.addAll(studentMarksList);
        }

        PassFailCounts passFailCounts = new PassFailCounts();
        updateTotalPassFailCounts(allStudentMarks, passFailCounts);

        // Set up columns dynamically based on subject names
        List<SubjectEntity> subjects = subjectEntities(selectedDepartment, selectedSemester);

        marksGrid.removeAllColumns();

        Grid.Column<StudentMarksDTO> studentName = marksGrid.addColumn(studentMarksDTO -> studentMarksDTO.getStudent().getUsername())
                .setHeader("Student Name")
                .setSortable(true)
                .setKey("studentName")
                .setFooter("TOTAL STUDENT :" + allStudents.size());


        headerRow.getCell(studentName).setComponent(createFilterHeader(name ->
                dataProvider.setFilter(studentMarksDTO -> studentMarksDTO.getStudent().getUsername().toLowerCase().contains(name)))); // CASE INSENSITIVE

        Grid.Column<StudentMarksDTO> registerNumber = marksGrid.addColumn(studentMarksDTO -> studentMarksDTO.getStudent().getRegisterNumber())
                .setHeader("Register Number")
                .setSortable(true)
                .setKey("registerNumber")
                .setFooter("");

        headerRow.getCell(registerNumber).setComponent(createFilterHeader(regNumber ->
                dataProvider.setFilter(studentMarksDTO -> studentMarksDTO.getStudent().getRegisterNumber().toLowerCase().contains(regNumber)))); // CASE INSENSITIVE

        contextMenu.addColumnToggleItem("STUDENT NAME", studentName);
        contextMenu.addColumnToggleItem("REGISTER NUMBER", registerNumber);

        setupColumns(subjects);

        // FIXED ISSUE BY ADDING HASHCODE AND EQUALS METHOD IN SUBJECT ENTITY
        for (SubjectEntity subject : subjects) {

            Grid.Column<StudentMarksDTO> columnByKey = marksGrid.getColumnByKey(subject.getSubjectName());
            if (columnByKey != null) {
                Integer pass = passFailCounts.subjectPassCounts.getOrDefault(subject, 0);
                Integer fail = passFailCounts.subjectFailCounts.getOrDefault(subject, 0);
                int totalStudents = pass + fail;
                int average = totalStudents > 0 ? (pass * 100) / totalStudents : 0;
                columnByKey.setFooter(String.format("P: %d F: %d AVG: %d%%", pass, fail, average));
            }

            Grid.Column<StudentMarksDTO> attendanceStatus = marksGrid.getColumnByKey(subject.getSubjectName() + " ATT");
            if (attendanceStatus != null) {
                Integer present = passFailCounts.subjectPresentCounts.getOrDefault(subject, 0);
                Integer absent = passFailCounts.subjectAbsentCounts.getOrDefault(subject, 0);
                int totalStudents = present + absent;
                int average = totalStudents > 0 ? (present * 100) / totalStudents : 0;
                attendanceStatus.setFooter(String.format("PRE: %d ABS: %d AVG: %d%%", present, absent, average));
            }
        }

        // Calculate the total pass and fail counts for each student
        for (StudentMarksDTO studentMarksDTO : allStudentMarks) {
            int totalPass = 0;
            int totalFail = 0;
            int totalAbsent = 0;
            int totalPresent = 0;

            for (Map.Entry<SubjectEntity, StudentSubjectInfo> entry : studentMarksDTO.getSubjectInfoMap().entrySet()) {
                StudentSubjectInfo subjectInfo = entry.getValue();

                if (subjectInfo.getMarks() != null && subjectInfo.getPassMarks() != null) {
                    boolean passed = subjectInfo.getMarks() >= subjectInfo.getPassMarks();
                    if (passed) {
                        totalPass++;
                    } else {
                        totalFail++;
                    }
                }

                if (subjectInfo.getAbsent() != null) {
                    if (subjectInfo.getAbsent()) {
                        totalAbsent++;
                    } else {
                        totalPresent++;
                    }
                }
            }

            // Set the total pass/fail counts for the student
            studentMarksDTO.setTotalPass(totalPass);
            studentMarksDTO.setTotalFail(totalFail);
            studentMarksDTO.setTotalAbsent(totalAbsent);
            studentMarksDTO.setTotalPresent(totalPresent);
        }

        Grid.Column<StudentMarksDTO> totalPassFailColumn = marksGrid.addColumn(studentMarksDTO ->
                        studentMarksDTO.getTotalPass() + "/" + studentMarksDTO.getTotalFail())
                .setHeader("Total Pass/Fail")
                .setWidth("150px")
                .setSortable(true)
                .setKey("totalPassFail")
                .setFooter("");

        Grid.Column<StudentMarksDTO> totalPresentAbsentColumn = marksGrid.addColumn(studentMarksDTO ->
                        studentMarksDTO.getTotalPresent() + "/" + studentMarksDTO.getTotalAbsent())
                .setHeader("Total Present/Absent")
                .setWidth("150px")
                .setSortable(true)
                .setKey("totalPresentAbsent")
                .setFooter("");

        contextMenu.addColumnToggleItem("TOTAL PASS/FAIL", totalPassFailColumn);
        contextMenu.addColumnToggleItem("TOTAL PRESENT/ABSENT", totalPresentAbsentColumn);

        dataProvider = marksGrid.setItems(allStudentMarks);
    }

    private void setupColumns(List<SubjectEntity> subjects) {
        for (SubjectEntity subject : subjects) {
            NumberField markField = new NumberField();
            markField.setValue(0.0); // Default value for the fields
            markField.setWidthFull();

            Grid.Column<StudentMarksDTO> marksColumn = marksGrid.addEditColumn(studentMarksDTO ->
                    {
                        StudentSubjectInfo subjectInfo = studentMarksDTO.getSubjectInfoMap()
                                .entrySet()
                                .stream()
                                .filter(entry -> entry.getKey().getSubjectName().equals(subject.getSubjectName()))
                                .findFirst()
                                .map(Map.Entry::getValue)
                                .orElse(new StudentSubjectInfo());

                        return subjectInfo.getMarks() != null ? subjectInfo.getMarks() : 0.0; // FIX THIS LATER

                    })
                    .custom(markField, (studentMarksDTO, newValue) -> {
                        StudentSubjectInfo subjectInfo = studentMarksDTO.getSubjectInfoMap()
                                .computeIfAbsent(subject, s -> new StudentSubjectInfo());

                        // Validate the new mark against the maximum allowed mark
                        if (subjectInfo.getMaxMarks() != null) /* To avoid NullPointerException on maxMark */ {
                            if (newValue != null && newValue >= 0 && newValue <= subjectInfo.getMaxMarks()) {
                                subjectInfo.setMarks(newValue);
                                marksService.updateStudentMarks(studentMarksDTO);
                                dataProvider.refreshItem(studentMarksDTO);
                            } else {
                                NotificationUtils.showErrorNotification("Invalid mark value");
                            }
                        }
                    })
                    .setHeader(subject.getSubjectName())
                    .setWidth("100px")
                    .setSortable(true)
                    .setClassNameGenerator(studentMarksDTO -> {
                        StudentSubjectInfo subjectInfo = studentMarksDTO.getSubjectInfoMap()
                                .entrySet()
                                .stream()
                                .filter(entry -> entry.getKey().getSubjectName().equals(subject.getSubjectName()))
                                .findFirst()
                                .map(Map.Entry::getValue)
                                .orElse(new StudentSubjectInfo()); // Create a default if not found

                        // Check for null values before comparing marks and pass marks
                        Double marks = subjectInfo.getMarks();
                        Double passMarks = subjectInfo.getPassMarks();

                        // Add your logic to determine pass or fail based on marks and pass marks
                        if (marks != null && passMarks != null) {
                            boolean passed = marks >= passMarks;
                            // Return the appropriate CSS class based on pass/fail
                            return passed ? "pass-color" : "fail-color";
                        } else {
                            return null;
                        }
                    })
                    .setKey(subject.getSubjectName());

            headerRow.getCell(marksColumn).setComponent(createPassFailFilter(passOrFail -> {

                // Remove any existing filters before applying new ones
                dataProvider.removeFilters();

                if ("Pass".equalsIgnoreCase(passOrFail)) {
                    // Show only rows where the student passed for the selected subject
                    dataProvider.addFilter(studentMarksDTO -> {
                        StudentSubjectInfo subjectInfo = studentMarksDTO.getSubjectInfoMap().get(subject);
                        return subjectInfo != null && subjectInfo.getMarks() != null && subjectInfo.getMarks() >= subjectInfo.getPassMarks();
                    });
                } else if ("Fail".equalsIgnoreCase(passOrFail)) {
                    // Show only rows where the student failed for the selected subject
                    dataProvider.addFilter(studentMarksDTO -> {
                        StudentSubjectInfo subjectInfo = studentMarksDTO.getSubjectInfoMap().get(subject);
                        return subjectInfo != null && (subjectInfo.getMarks() == null || subjectInfo.getMarks() < subjectInfo.getPassMarks());
                    });
                }
            }));


            Grid.Column<StudentMarksDTO> attendanceStatus = marksGrid.addColumn(new ComponentRenderer<>(
                            studentMarksDTO -> {
                                StudentSubjectInfo subjectInfo = studentMarksDTO.getSubjectInfoMap()
                                        .entrySet()
                                        .stream()
                                        .filter(entry -> entry.getKey().getSubjectName().equals(subject.getSubjectName()))
                                        .findFirst()
                                        .map(Map.Entry::getValue).orElse(null);

                                // Create a span with a badge
                                Boolean isAbsent = subjectInfo != null ? subjectInfo.getAbsent() : null;
                                Span span = new Span();
                                if (isAbsent != null) {
                                    span.setText(Boolean.TRUE.equals(isAbsent) ? "Absent" : "Present");
                                    span.getElement().getThemeList().add(isAbsent ? "badge error" : "badge success");
                                } else {
                                    span.setText("N/A");
                                    span.getElement().getThemeList().add("badge contrast");
                                }
                                return span;
                            })
                    ).setHeader(subject.getSubjectShortForm() + " Attendance")
                    .setWidth("100px")
                    .setSortable(true)
                    .setComparator((studentMarksDTO1, studentMarksDTO2) -> {
                        boolean isAbsent1 = isAbsentForCurrentCell(studentMarksDTO1, subject);
                        boolean isAbsent2 = isAbsentForCurrentCell(studentMarksDTO2, subject);
                        return Boolean.compare(isAbsent1, isAbsent2); // Compare based on the "Absent" status
                    }).setKey(subject.getSubjectName() + " ATT");

            marksGrid.addCellEditStartedListener(event -> {
                StudentMarksDTO studentMarksDTO = event.getItem();

                // Check if the subject has marks information for the current student
                boolean hasMarks = studentMarksDTO.getSubjectInfoMap().containsKey(subject);

                // Check if the student is marked as absent for the given subject
                boolean isAbsent = hasMarks && isAbsentForCurrentCell(studentMarksDTO, subject);

                markField.setReadOnly(isAbsent || !hasMarks);
            });

            headerRow.getCell(attendanceStatus).setComponent(createPresentAbsentFilter(presentAbsent -> {
                // Remove any existing filters before applying new ones
                dataProvider.removeFilters();

                if ("Present".equalsIgnoreCase(presentAbsent)) {
                    // Show only rows where the student is present for the selected subject
                    dataProvider.addFilter(studentMarksDTO -> {
                        StudentSubjectInfo subjectInfo = studentMarksDTO.getSubjectInfoMap().get(subject);
                        return subjectInfo != null && subjectInfo.getAbsent() != null && !subjectInfo.getAbsent();
                    });
                } else if ("Absent".equalsIgnoreCase(presentAbsent)) {
                    // Show only rows where the student is absent for the selected subject
                    dataProvider.addFilter(studentMarksDTO -> {
                        StudentSubjectInfo subjectInfo = studentMarksDTO.getSubjectInfoMap().get(subject);
                        return subjectInfo != null && subjectInfo.getAbsent() != null && subjectInfo.getAbsent();
                    });
                }
            }));

            contextMenu.addColumnToggleItem(subject.getSubjectShortForm(), marksColumn);
            contextMenu.addColumnToggleItem(subject.getSubjectShortForm() + " ATTENDANCE", attendanceStatus);
        }
    }

    // Filters
    private ComboBox<String> createFilterComponent(List<String> items) {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setItems(items);
        comboBox.setPlaceholder("Filter");
        comboBox.setClearButtonVisible(true);
        comboBox.setWidth("100%");
        comboBox.addThemeVariants(ComboBoxVariant.LUMO_SMALL);
        return comboBox;
    }

    private ComboBox<String> createPresentAbsentFilter(Consumer<String> filterChangeConsumer) {
        ComboBox<String> filter = createFilterComponent(Arrays.asList("Present", "Absent"));
        filter.addValueChangeListener(e -> filterChangeConsumer.accept(e.getValue()));
        return filter;
    }

    private ComboBox<String> createPassFailFilter(Consumer<String> filterChangeConsumer) {
        ComboBox<String> filter = createFilterComponent(Arrays.asList("Pass", "Fail"));
        filter.addValueChangeListener(e -> filterChangeConsumer.accept(e.getValue()));
        return filter;
    }

    private boolean isAbsentForCurrentCell(StudentMarksDTO studentMarksDTO, SubjectEntity subject) {
        return studentMarksDTO.getSubjectInfoMap()
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().getSubjectName().equals(subject.getSubjectName()))
                .findFirst()
                .map(Map.Entry::getValue)
                .map(StudentSubjectInfo::getAbsent)
                .orElse(false); // Default to false if not found
    }

    // CONTEXT CLASS
    private static class PassFailCounts {
        int totalPass;
        int totalFail;
        int totalPresent;
        int totalAbsent;

        // Map to store counts for each subject
        Map<SubjectEntity, Integer> subjectPassCounts = new HashMap<>();
        Map<SubjectEntity, Integer> subjectFailCounts = new HashMap<>();
        Map<SubjectEntity, Integer> subjectPresentCounts = new HashMap<>();
        Map<SubjectEntity, Integer> subjectAbsentCounts = new HashMap<>();
    }

    private List<SubjectEntity> subjectEntities(DepartmentEntity department, Semester semester) {
        return subjectService.getSubjectsByDepartmentAndSemester(department, semester);
    }

    // Utility
    private static class ColumnToggleContextMenu extends ContextMenu {
        public ColumnToggleContextMenu(Component target) {
            super(target);
            setOpenOnClick(true);
        }

        void addColumnToggleItem(String label, Grid.Column<StudentMarksDTO> column) {
            MenuItem menuItem = this.addItem(label, e -> column.setVisible(e.getSource().isChecked()));
            menuItem.setCheckable(true);
            menuItem.setChecked(column.isVisible());
            menuItem.setKeepOpen(true);
        }
    }
}
