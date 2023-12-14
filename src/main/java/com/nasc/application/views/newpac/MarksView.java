package com.nasc.application.views.newpac;

import com.nasc.application.data.model.AcademicYearEntity;
import com.nasc.application.data.model.DepartmentEntity;
import com.nasc.application.data.model.SubjectEntity;
import com.nasc.application.data.model.User;
import com.nasc.application.data.model.dto.StudentMarksDTO;
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
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.gridpro.GridPro;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Route(value = "marks", layout = MainLayout.class)
@PermitAll
@PageTitle("Marks")
public class MarksView extends VerticalLayout {

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
    // Button
    private final Button searchButton = new Button("Search/Refresh");

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

        searchButton.addClickListener(e -> updateGridData());
        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        menuButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout FilterLayout = new HorizontalLayout();

        FilterLayout.setAlignItems(Alignment.BASELINE);
        FilterLayout.add(departmentComboBox, academicYearComboBox, semesterComboBox, examTypeComboBox, searchButton);

        HorizontalLayout horizontalLayout = new HorizontalLayout(menuButton);
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

    private void updateGridData() {
        ExamType selectedExamType = examTypeComboBox.getValue();
        Semester selectedSemester = semesterComboBox.getValue();
        DepartmentEntity selectedDepartment = departmentComboBox.getValue();
        AcademicYearEntity selectedAcademicYear = academicYearComboBox.getValue();

        if (selectedExamType == null || selectedSemester == null || selectedDepartment == null || selectedAcademicYear == null) {
            NotificationUtils.showErrorNotification("Please select values for all the filters!");
            return;
        }

        // Set up columns dynamically based on subject names
        List<SubjectEntity> subjects = subjectEntities(selectedDepartment, selectedSemester);

        marksGrid.removeAllColumns();

        marksGrid.addColumn(studentMarksDTO -> studentMarksDTO.getStudent().getUsername())
                .setHeader("Student Name")
                .setSortable(true);

        marksGrid.addColumn(studentMarksDTO -> studentMarksDTO.getStudent().getRegisterNumber())
                .setHeader("Register Number")
                .setSortable(true);

        ColumnToggleContextMenu contextMenu = new ColumnToggleContextMenu(menuButton);

        for (SubjectEntity subject : subjects) {
            Grid.Column<StudentMarksDTO> column = marksGrid.addEditColumn(studentMarksDTO ->
                            studentMarksDTO.getSubjectMarksMap()
                                    .entrySet()
                                    .stream()
                                    .filter(entry -> entry.getKey().getSubjectName().equals(subject.getSubjectName()))
                                    .findFirst()
                                    .map(Map.Entry::getValue)
                                    .orElse(0.0))
                    .custom(new NumberField(), (studentMarksDTO, newValue) -> {
                        studentMarksDTO.getSubjectMarksMap().put(subject, newValue);
                        marksService.updateStudentMarks(studentMarksDTO); // save changes to database
                        dataProvider.refreshAll(); //refresh grid
                    })
                    .setHeader(subject.getSubjectName())
                    .setSortable(true);
            contextMenu.addColumnToggleItem(subject.getSubjectShortForm(), column);
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
        dataProvider = marksGrid.setItems(allStudentMarks);
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
            MenuItem menuItem = this.addItem(label, e -> {
                column.setVisible(e.getSource().isChecked());
            });
            menuItem.setCheckable(true);
            menuItem.setChecked(column.isVisible());
            menuItem.setKeepOpen(true);
        }
    }
}
