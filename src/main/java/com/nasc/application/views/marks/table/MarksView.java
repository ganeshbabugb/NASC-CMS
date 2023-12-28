package com.nasc.application.views.marks.table;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.nasc.application.data.core.AcademicYearEntity;
import com.nasc.application.data.core.DepartmentEntity;
import com.nasc.application.data.core.SubjectEntity;
import com.nasc.application.data.core.User;
import com.nasc.application.data.core.dto.StudentMarksDTO;
import com.nasc.application.data.core.dto.StudentSubjectInfo;
import com.nasc.application.data.core.enums.ExamType;
import com.nasc.application.data.core.enums.Role;
import com.nasc.application.data.core.enums.Semester;
import com.nasc.application.data.core.enums.StudentSection;
import com.nasc.application.services.*;
import com.nasc.application.utils.NotificationUtils;
import com.nasc.application.utils.UIUtils;
import com.nasc.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.export.SVGGenerator;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.ComboBoxVariant;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.gridpro.GridPro;
import com.vaadin.flow.component.gridpro.GridProVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;
import org.vaadin.olli.FileDownloadWrapper;
import software.xdev.vaadin.grid_exporter.GridExporter;
import software.xdev.vaadin.grid_exporter.column.ColumnConfigurationBuilder;
import software.xdev.vaadin.grid_exporter.jasper.format.HtmlFormat;
import software.xdev.vaadin.grid_exporter.jasper.format.PdfFormat;
import software.xdev.vaadin.grid_exporter.jasper.format.XlsxFormat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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

    // Service
    private final MarksService marksService;
    private final DepartmentService departmentService;
    private final AcademicYearService academicYearService;
    private final Button menuButton = new Button("Show/Hide Columns");
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
    private final ComboBox<StudentSection> studentSectionComboBox = new ComboBox<>("Select Student Section");

    // UTILS
    private final ColumnToggleContextMenu contextMenu = new ColumnToggleContextMenu(menuButton);
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
        marksGrid.setMultiSort(true);
        marksGrid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT, GridVariant.LUMO_COLUMN_BORDERS);
        marksGrid.addThemeVariants(GridProVariant.LUMO_ROW_STRIPES);

        headerRow = marksGrid.appendHeaderRow();

        // Button
        Button searchButton = new Button("Search/Refresh");
        searchButton.addClickListener(e -> updateGridData());
        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

        menuButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        HorizontalLayout FilterLayout = new HorizontalLayout();

        FilterLayout.setAlignItems(Alignment.BASELINE);
        FilterLayout.add(departmentComboBox, academicYearComboBox, semesterComboBox, examTypeComboBox, studentSectionComboBox, searchButton);

        HorizontalLayout horizontalLayout = new HorizontalLayout(menuButton,
                new Button("Export",
                        FontAwesome.Solid.FILE_EXPORT.create(),
                        e -> {
                            ExamType selectedExamType = examTypeComboBox.getValue();
                            Semester selectedSemester = semesterComboBox.getValue();
                            DepartmentEntity selectedDepartment = departmentComboBox.getValue();
                            AcademicYearEntity selectedAcademicYear = academicYearComboBox.getValue();
                            StudentSection studentSection = studentSectionComboBox.getValue();

                            // Check Grid and Filter Before Export
                            if (!isFilterInValid() && dataProvider != null) {
                                String fileName = selectedDepartment.getShortName()
                                        + "_"
                                        + selectedAcademicYear.getStartYear() + "-" + selectedAcademicYear.getEndYear()
                                        + "_"
                                        + studentSection.getDisplayName()
                                        + "_"
                                        + selectedSemester.getDisplayName()
                                        + "_"
                                        + selectedExamType.getDisplayName() + "_Marks";

                                XlsxFormat xlsxFormat = new XlsxFormat();
                                PdfFormat pdfFormat = new PdfFormat();
                                HtmlFormat htmlFormat = new HtmlFormat();

                                gridExporter = GridExporter
                                        .newWithDefaults(marksGrid)
                                        .withFileName(fileName)
                                        // Ignoring chart column
                                        .withColumnFilter(studentMarksDTOColumn ->
                                                studentMarksDTOColumn.isVisible() &&
                                                        !studentMarksDTOColumn.getKey().equals("Chart"))
                                        .withAvailableFormats(xlsxFormat, pdfFormat, htmlFormat)
                                        .withPreSelectedFormat(xlsxFormat)
                                        .withColumnConfigurationBuilder(new ColumnConfigurationBuilder());

                                gridExporter.open();
                            } else {
                                NotificationUtils.showErrorNotification("Please select values for all the filters!");
                            }
                        }
                ));
        horizontalLayout.setWidthFull();
        horizontalLayout.setJustifyContentMode(JustifyContentMode.END);
        horizontalLayout.setAlignItems(Alignment.CENTER);

        add(new H3("Marks View"), FilterLayout, horizontalLayout, marksGrid);

        setSizeFull();
    }

    private boolean isFilterInValid() {
        return examTypeComboBox.getValue() == null ||
                semesterComboBox.getValue() == null ||
                departmentComboBox.getValue() == null ||
                academicYearComboBox.getValue() == null ||
                studentSectionComboBox.getValue() == null;
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
        return textField;
    }

    private void configureComboBoxes() {
        List<AcademicYearEntity> academicYears = academicYearService.findAll();
        List<DepartmentEntity> departments = departmentService.findAll();
        Semester[] semesters = Semester.values();
        ExamType[] examTypes = ExamType.values();
        StudentSection[] studentSections = StudentSection.values();

        semesterComboBox.setItems(semesters);
        semesterComboBox.setItemLabelGenerator(Semester::getDisplayName);
        semesterComboBox.addThemeVariants(ComboBoxVariant.LUMO_SMALL);
        semesterComboBox.setRequiredIndicatorVisible(true);

        examTypeComboBox.setItems(examTypes);
        examTypeComboBox.setItemLabelGenerator(ExamType::getDisplayName);
        examTypeComboBox.addThemeVariants(ComboBoxVariant.LUMO_SMALL);
        examTypeComboBox.setRequiredIndicatorVisible(true);

        academicYearComboBox.setItems(academicYears);
        academicYearComboBox.setItemLabelGenerator(item -> item.getStartYear() + " - " + item.getEndYear());
        academicYearComboBox.addThemeVariants(ComboBoxVariant.LUMO_SMALL);
        academicYearComboBox.setRequiredIndicatorVisible(true);

        departmentComboBox.setItems(departments);
        departmentComboBox.setItemLabelGenerator(item -> item.getName() + " - " + item.getShortName());
        departmentComboBox.addThemeVariants(ComboBoxVariant.LUMO_SMALL);
        departmentComboBox.setRequiredIndicatorVisible(true);

        studentSectionComboBox.setItems(studentSections);
        studentSectionComboBox.setItemLabelGenerator(StudentSection::getDisplayName);
        studentSectionComboBox.addThemeVariants(ComboBoxVariant.LUMO_SMALL);
        studentSectionComboBox.setRequiredIndicatorVisible(true);
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
        StudentSection studentSection = studentSectionComboBox.getValue();

        if (isAllFiltersNotNull(
                selectedExamType,
                selectedSemester,
                selectedDepartment,
                selectedAcademicYear,
                studentSection
        )) {
            NotificationUtils.showErrorNotification("Please select values for all the filters!");
            return;
        }

        // Set up grid data for all students
        List<User> allStudents = userService.findStudentsByDepartmentAndRoleAndAcademicYearAndSection(
                selectedDepartment,
                Role.STUDENT,
                selectedAcademicYear,
                studentSection
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

        String studentFooterString = "TOTAL STUDENT: " + allStudents.size();
        String studentFooterOuterHtml = String.format("<b>%s</b>", studentFooterString);
        Grid.Column<StudentMarksDTO> studentName = marksGrid.addColumn(studentMarksDTO -> studentMarksDTO.getStudent().getUsername())
                .setHeader("Student Name")
                .setSortable(true)
                .setKey("Student Name")
                .setAutoWidth(true)
                .setFrozen(true)
                .setFooter(new Html(studentFooterOuterHtml));

        headerRow.getCell(studentName).setComponent(createFilterHeader(name ->
                dataProvider.setFilter(studentMarksDTO -> studentMarksDTO.getStudent().getUsername().toLowerCase().contains(name)))); // CASE INSENSITIVE

        Grid.Column<StudentMarksDTO> registerNumber = marksGrid.addColumn(studentMarksDTO -> studentMarksDTO.getStudent().getRegisterNumber())
                .setHeader("Register Number")
                .setAutoWidth(true)
                .setSortable(true)
                .setFrozen(true)
                .setKey("Register Number");

        headerRow.getCell(registerNumber).setComponent(createFilterHeader(regNumber ->
                dataProvider.setFilter(studentMarksDTO -> studentMarksDTO.getStudent().getRegisterNumber().toLowerCase().contains(regNumber)))); // CASE INSENSITIVE

        contextMenu.addColumnToggleItem(studentName);
        contextMenu.addColumnToggleItem(registerNumber);

        setupColumns(subjects);

        for (SubjectEntity subject : subjects) {
            Grid.Column<StudentMarksDTO> columnByKey = marksGrid.getColumnByKey(UIUtils.toCapitalize(subject.getSubjectName()));
            if (columnByKey != null) {
                Integer pass = passFailCounts.subjectPassCounts.getOrDefault(subject, 0);
                Integer totalStudents = passFailCounts.subjectPresentCounts.getOrDefault(subject, 0);
                int average = totalStudents > 0 ? (pass * 100) / totalStudents : 0;
                int fail = totalStudents - pass;

                HorizontalLayout layout = new HorizontalLayout();
                layout.setAlignItems(Alignment.BASELINE);
                layout.setJustifyContentMode(JustifyContentMode.CENTER);
                layout.setMargin(false);
                layout.setPadding(false);

                FontAwesome.Solid.Icon icon = FontAwesome.Solid.CHART_PIE.create();
                Button chartButton = new Button(icon);

                // Adding chart
                chartButton.addClickListener(e -> openPieChartDialog(pass, fail, subject));
                chartButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
                String labelText = String.format("P: %d F: %d AVG: %d%%", pass, fail, average);
                String outerHtml = String.format("<b>%s</b>", labelText);
                layout.add(new Html(outerHtml), chartButton);
                columnByKey.setFooter(layout);
            }

            Grid.Column<StudentMarksDTO> attendanceStatus = marksGrid.getColumnByKey(UIUtils.toCapitalize(subject.getSubjectName() + " Attendance"));
            if (attendanceStatus != null) {
                Integer present = passFailCounts.subjectPresentCounts.getOrDefault(subject, 0);
                Integer absent = passFailCounts.subjectAbsentCounts.getOrDefault(subject, 0);
                int totalStudents = present + absent;
                int average = totalStudents > 0 ? (present * 100) / totalStudents : 0;
                String labelText = String.format("PRE: %d ABS: %d AVG: %d%%", present, absent, average);
                String outerHtml = String.format("<b>%s</b>", labelText);
                attendanceStatus.setFooter(new Html(outerHtml));
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
                .setHeader("PASS/FAIL")
                .setSortable(true)
                .setTextAlign(ColumnTextAlign.CENTER)
                .setAutoWidth(true)
                .setKey("Pass/Fail");

        Grid.Column<StudentMarksDTO> totalPresentAbsentColumn = marksGrid.addColumn(studentMarksDTO ->
                        studentMarksDTO.getTotalPresent() + "/" + studentMarksDTO.getTotalAbsent())
                .setHeader("PRE/ABS")
                .setSortable(true)
                .setTextAlign(ColumnTextAlign.CENTER)
                .setAutoWidth(true)
                .setKey("Pre/Abs");


        contextMenu.addColumnToggleItem(totalPassFailColumn);
        contextMenu.addColumnToggleItem(totalPresentAbsentColumn);

        // Chart Column
        Grid.Column<StudentMarksDTO> chartButtonColumn = marksGrid.addComponentColumn(studentMarksDTO -> {
                    FontAwesome.Solid.Icon icon = FontAwesome.Solid.CHART_COLUMN.create();
                    Button chartButton = new Button(icon);
                    chartButton.addClickListener(e -> openChartDialog(studentMarksDTO));
                    chartButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
                    return chartButton;
                })
                .setHeader("Chart")
                .setFlexGrow(0) // TO AVOID IT TAKES EXTRA SPACE
                .setFrozenToEnd(true)
                .setSortable(false)
                .setTextAlign(ColumnTextAlign.CENTER)
                .setKey("Chart");

        contextMenu.addColumnToggleItem(chartButtonColumn);

        dataProvider = marksGrid.setItems(allStudentMarks);
    }

    private boolean isAllFiltersNotNull(ExamType selectedExamType,
                                        Semester selectedSemester,
                                        DepartmentEntity selectedDepartment,
                                        AcademicYearEntity selectedAcademicYear,
                                        StudentSection studentSection
    ) {

        if (selectedExamType == null) {
            examTypeComboBox.setErrorMessage("Exam Type is required");
            examTypeComboBox.setInvalid(true);
        }
        if (selectedSemester == null) {
            semesterComboBox.setErrorMessage("Semester is required");
            semesterComboBox.setInvalid(true);
        }
        if (selectedDepartment == null) {
            departmentComboBox.setErrorMessage("Department is required");
            departmentComboBox.setInvalid(true);
        }
        if (selectedAcademicYear == null) {
            academicYearComboBox.setErrorMessage("Academic Year is required");
            academicYearComboBox.setInvalid(true);
        }
        if (studentSection == null) {
            studentSectionComboBox.setErrorMessage("Student Section is required");
            studentSectionComboBox.setInvalid(true);
        }
        return selectedExamType == null
                || selectedSemester == null
                || selectedDepartment == null
                || selectedAcademicYear == null
                || studentSection == null;
    }

    private Chart createPieBarChart(Integer pass, int fail, SubjectEntity subject) {

        Chart chart = new Chart(ChartType.PIE);
        Configuration conf = chart.getConfiguration();
        String title = subject.getDepartment().getName();
        String subTittle = subject.getSubjectName() + " [" + subject.getSubjectCode() + "]";
        conf.setTitle(title);
        conf.setSubTitle(subTittle);
        Tooltip tooltip = new Tooltip();
        tooltip.setValueDecimals(1);
        conf.setTooltip(tooltip);

        PlotOptionsPie plotOptions = new PlotOptionsPie();
        plotOptions.setAllowPointSelect(true);
        plotOptions.setCursor(Cursor.POINTER);
        plotOptions.setShowInLegend(true);
        conf.setPlotOptions(plotOptions);

        DataSeries series = new DataSeries();
        DataSeriesItem chrome = new DataSeriesItem("Pass", pass);
        chrome.setSliced(true);
        chrome.setSelected(true);
        series.add(chrome);
        series.add(new DataSeriesItem("Fail", fail));
        conf.setSeries(series);
        chart.setVisibilityTogglingDisabled(true);
        return chart;
    }

    private void setupColumns(List<SubjectEntity> subjects) {
        for (SubjectEntity subject : subjects) {
            NumberField markField = new NumberField();
            markField.setValue(0.0); // Default value for the fields
            markField.setWidthFull();

            String subjectCapitalize = UIUtils.toCapitalize(subject.getSubjectName());
            Grid.Column<StudentMarksDTO> marksColumn = marksGrid.addEditColumn(studentMarksDTO ->
                    {
                        StudentSubjectInfo subjectInfo = studentMarksDTO.getSubjectInfoMap()
                                .entrySet()
                                .stream()
                                .filter(entry -> entry.getKey().getSubjectName().equals(subject.getSubjectName()))
                                .findFirst()
                                .map(Map.Entry::getValue)
                                .orElse(new StudentSubjectInfo());

                        return subjectInfo.getMarks() != null ? subjectInfo.getMarks() : 0.0;

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
                    .setHeader(subjectCapitalize)
                    .setFlexGrow(0)
                    .setAutoWidth(true)
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
                        Boolean absent = subjectInfo.getAbsent();

                        // Add your logic to determine pass, fail, or absent based on marks, pass marks, and absent status
                        if (absent != null && absent) {
                            // Student is absent
                            return "absent-color";
                        } else if (marks != null && passMarks != null) {
                            // Check for pass or fail based on marks and pass marks
                            boolean passed = marks >= passMarks;
                            // Return the appropriate CSS class based on pass/fail
                            return passed ? "pass-color" : "fail-color";
                        } else {
                            return "na-color";
                        }
                    })
                    .setTextAlign(ColumnTextAlign.CENTER)
                    .setKey(subjectCapitalize);

            ComboBox<String> passFailFilter = createPassFailFilter(passOrFail -> {
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
            });

            headerRow.getCell(marksColumn).setComponent(passFailFilter);

            String subjectShortForm = UIUtils.toCapitalize(subject.getSubjectShortForm());
            Grid.Column<StudentMarksDTO> attendanceStatus = marksGrid.addColumn(new TextRenderer<>(
                            studentMarksDTO -> {
                                StudentSubjectInfo subjectInfo = studentMarksDTO.getSubjectInfoMap()
                                        .entrySet()
                                        .stream()
                                        .filter(entry -> entry.getKey().getSubjectName().equals(subject.getSubjectName()))
                                        .findFirst()
                                        .map(Map.Entry::getValue).orElse(null);

                                // Create a span with a badge
                                Boolean isAbsent = subjectInfo != null ? subjectInfo.getAbsent() : null;
                                Text text = new Text("");
                                if (isAbsent != null) {
                                    text.setText(Boolean.TRUE.equals(isAbsent) ? "Absent" : "Present");
                                } else {
                                    text.setText("N/A");
                                }
                                return text.getText();
                            })
                    )
                    .setClassNameGenerator(studentMarksDTO -> {
                        StudentSubjectInfo subjectInfo = studentMarksDTO.getSubjectInfoMap()
                                .entrySet()
                                .stream()
                                .filter(entry -> entry.getKey().getSubjectName().equals(subject.getSubjectName()))
                                .findFirst()
                                .map(Map.Entry::getValue).orElse(null);

                        Boolean isAbsent = subjectInfo != null ? subjectInfo.getAbsent() : null;
                        if (isAbsent != null)
                            return isAbsent ? "absent-color" : "present-color"; // Attendance
                        return "na-color";
                    })
                    .setHeader(subjectShortForm)
                    .setSortable(true)
                    .setFlexGrow(0)
                    .setAutoWidth(true)
                    .setTextAlign(ColumnTextAlign.CENTER)
                    .setComparator((studentMarksDTO1, studentMarksDTO2) -> {
                        boolean isAbsent1 = isAbsentForCurrentCell(studentMarksDTO1, subject);
                        boolean isAbsent2 = isAbsentForCurrentCell(studentMarksDTO2, subject);
                        return Boolean.compare(isAbsent1, isAbsent2); // Compare based on the "Absent" status
                    })
                    .setKey(subjectCapitalize + " Attendance");

            marksGrid.addCellEditStartedListener(event -> {
                StudentMarksDTO studentMarksDTO = event.getItem();

                // Check if the subject has marks information for the current student
                boolean hasMarks = studentMarksDTO.getSubjectInfoMap().containsKey(subject);

                // Check if the student is marked as absent for the given subject
                boolean isAbsent = hasMarks && isAbsentForCurrentCell(studentMarksDTO, subject);

                markField.setReadOnly(isAbsent || !hasMarks);
            });

            ComboBox<String> presentAbsentFilter = createPresentAbsentFilter(presentAbsent -> {
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
            });
            headerRow.getCell(attendanceStatus).setComponent(presentAbsentFilter);

            contextMenu.addColumnToggleItem(marksColumn);
            contextMenu.addColumnToggleItem(attendanceStatus);
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
        filter.setItemLabelGenerator(s -> s.substring(0, 3));
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

        void addColumnToggleItem(Grid.Column<StudentMarksDTO> column) {
            MenuItem menuItem = this.addItem(column.getKey(), e -> column.setVisible(e.getSource().isChecked()));
            menuItem.setCheckable(true);
            menuItem.setChecked(column.isVisible());
            menuItem.setKeepOpen(true);
        }
    }

    // Method to open the chart dialog
    private void openChartDialog(StudentMarksDTO studentMarksDTO) {
        Chart chart = createStackedBarChart(studentMarksDTO);
        Button downloadButton = new Button("Download");
        downloadButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        downloadButton.getStyle().set("margin-left", "auto"); // To set the button on left corner

        // Wrapper
        User student = studentMarksDTO.getStudent();
        String fileName = student.getUsername() + " [" + student.getRegisterNumber() + "].svg";
        createChartDialog(chart, downloadButton, fileName);
    }

    private void openPieChartDialog(Integer pass, int fail, SubjectEntity subject) {
        Chart chart = createPieBarChart(pass, fail, subject);
        Button downloadButton = new Button("Download");
        downloadButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        downloadButton.getStyle().set("margin-left", "auto"); // To set the button on left corner

        // Wrapper
        String fileName = subject.getSubjectName() + "[" + subject.getSubjectCode() + "]" + ".svg";
        createChartDialog(chart, downloadButton, fileName);
    }

    private void createChartDialog(Chart chart, Button downloadButton, String fileName) {
        FileDownloadWrapper buttonWrapper = new FileDownloadWrapper(
                new StreamResource(fileName, () -> {
                    Configuration configuration = chart.getConfiguration();
                    String svg = "";
                    try (SVGGenerator generator = new SVGGenerator()) {
                        svg = generator.generate(configuration);
                    } catch (IOException | InterruptedException ex) {
                        log.error(ex.getMessage());
                    }
                    return new ByteArrayInputStream(svg.getBytes());
                }));
        buttonWrapper.wrapComponent(downloadButton);

        Dialog dialog = new Dialog();
        dialog.setWidth("50%");
        dialog.add(chart);
        dialog.open();
        dialog.getFooter().add(buttonWrapper);
    }

    // Method to create a stacked bar chart for a student
    private Chart createStackedBarChart(StudentMarksDTO studentMarksDTO) {
        Chart chart = new Chart(ChartType.COLUMN);
        Configuration configuration = chart.getConfiguration();
        configuration.setTitle("Student Marks Column Chart");

        User student = studentMarksDTO.getStudent();
        String title = student.getUsername() + " [" + student.getRegisterNumber() + "]";
        configuration.setSubTitle(title);

        XAxis xAxis = new XAxis();
        xAxis.setTitle("Subjects");

        YAxis yAxis = new YAxis();
        yAxis.setTitle("Marks");

        configuration.addxAxis(xAxis);
        configuration.addyAxis(yAxis);

        // Retrieve the student's marks for each subject
        Map<SubjectEntity, StudentSubjectInfo> subjectInfoMap = studentMarksDTO.getSubjectInfoMap();

        for (Map.Entry<SubjectEntity, StudentSubjectInfo> entry : subjectInfoMap.entrySet()) {
            SubjectEntity subject = entry.getKey();
            StudentSubjectInfo subjectInfo = entry.getValue();

            ListSeries series = new ListSeries(subject.getSubjectName());
            if (subjectInfo.getMarks() != null) {
                series.addData(subjectInfo.getMarks());
            }
            configuration.addSeries(series);
        }

        PlotOptionsBar plotOptions = new PlotOptionsBar();
        plotOptions.setStacking(Stacking.NORMAL);
        configuration.setPlotOptions(plotOptions);

        return chart;
    }
}
