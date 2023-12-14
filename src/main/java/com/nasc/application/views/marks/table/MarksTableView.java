package com.nasc.application.views.marks.table;

import com.nasc.application.data.model.AcademicYearEntity;
import com.nasc.application.data.model.DepartmentEntity;
import com.nasc.application.data.model.MarksEntity;
import com.nasc.application.data.model.User;
import com.nasc.application.data.model.enums.ExamType;
import com.nasc.application.data.model.enums.Role;
import com.nasc.application.data.model.enums.Semester;
import com.nasc.application.data.repository.SubjectRepository;
import com.nasc.application.services.AcademicYearService;
import com.nasc.application.services.DepartmentService;
import com.nasc.application.services.MarksService;
import com.nasc.application.services.UserService;
import com.nasc.application.views.MainLayout;
import com.nasc.application.views.marks.entry.Item;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Route(value = "marks-table", layout = MainLayout.class)
@PageTitle("Marks Table")
@RolesAllowed({"HOD", "ADMIN"})
@Slf4j
public class MarksTableView extends VerticalLayout {

    private final MarksService marksService;
    private final UserService userService;
    private final Grid<Item> marksGrid;
    private final ComboBox<Semester> semesterFilter;
    private final ComboBox<ExamType> examTypeFilter;
    private final ComboBox<AcademicYearEntity> academicYearFilter;
    private final ComboBox<DepartmentEntity> departmentFilter;
    private final Button searchButton;
    private final SubjectRepository subjectRepository;
    private ExamType selectedExamType;

    @Autowired
    public MarksTableView(MarksService marksService,
                          UserService userService,
                          AcademicYearService academicYearService,
                          DepartmentService departmentService,
                          SubjectRepository subjectRepository
    ) {
        this.marksService = marksService;
        this.userService = userService;
        this.subjectRepository = subjectRepository;

        // Populate drop down values
        List<AcademicYearEntity> academicYears = academicYearService.findAll();
        List<DepartmentEntity> departments = departmentService.findAll();
        Semester[] semesters = Semester.values();
        ExamType[] examTypes = ExamType.values();

        // Create filter components
        semesterFilter = new ComboBox<>("Filter by Semester");
        semesterFilter.setItems(semesters);
        semesterFilter.setItemLabelGenerator(Semester::getDisplayName);

        examTypeFilter = new ComboBox<>("Filter by Exam Type");
        examTypeFilter.setItems(examTypes);
        examTypeFilter.setItemLabelGenerator(ExamType::getDisplayName);

        academicYearFilter = new ComboBox<>("Filter by Academic Year");
        academicYearFilter.setItems(academicYears);
        academicYearFilter.setItemLabelGenerator(item -> item.getStartYear() + " - " + item.getEndYear());

        departmentFilter = new ComboBox<>("Filter by Department");
        departmentFilter.setItems(departments);
        departmentFilter.setItemLabelGenerator(item -> item.getName() + " - " + item.getShortName());

        searchButton = new Button("Search", event -> updateGrid());
        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // Create a grid for displaying marks
        marksGrid = new Grid<>();
        marksGrid.setSizeFull();
        marksGrid.setHeightFull();

        // Add the grid and filter components to the layout
        add(createFilterLayout(), marksGrid);

        setSizeFull();
    }

    private HorizontalLayout createFilterLayout() {
        HorizontalLayout filterLayout = new HorizontalLayout();
        filterLayout.setAlignItems(Alignment.BASELINE); // To align everything on the same line
        filterLayout.add(departmentFilter, academicYearFilter, examTypeFilter, semesterFilter, searchButton);
        return filterLayout;
    }

    private void updateGrid() {
        // Fetch students based on filters
        List<User> students = userService.findStudentsByDepartmentAndRoleAndAcademicYear(
                departmentFilter.getValue(), Role.STUDENT, academicYearFilter.getValue()
        );

        List<String> distinctSubjectNames = subjectRepository.findDistinctSubjectNamesByCriteria(
                semesterFilter.getValue(),
                departmentFilter.getValue()
        );

        // Set the selected exam type
        selectedExamType = examTypeFilter.getValue();

        // Create a map to store student marks for each subject
        Map<User, Map<String, Double>> studentMarksMap = fetchStudentMarksMap(students);

        // Set the grid columns dynamically based on subjects
        marksGrid.removeAllColumns();
        marksGrid.addColumn(Item::getUsername).setHeader("Student Name");
        marksGrid.addColumn(Item::getRegisterNumber).setHeader("Register Number");

        // Add columns for each subject
        for (String subjectName : distinctSubjectNames) {
            marksGrid.addColumn(item -> item.getMarksForSubject(subjectName, distinctSubjectNames)).setHeader(subjectName);
        }

        // Set the filtered marks data in the grid
        List<Item> items = fetchGridData(students, distinctSubjectNames, studentMarksMap);

        marksGrid.setItems(items);
    }

    private Map<User, Map<String, Double>> fetchStudentMarksMap(List<User> students) {
        return marksService.getAllMarksByStudents(students).stream()
                .filter(mark -> !mark.isAbsent() && mark.getExam().getExamType() == selectedExamType) // Filter by exam type
                .collect(Collectors.groupingBy(
                        MarksEntity::getStudent,
                        Collectors.toMap(
                                mark -> mark.getSubject().getSubjectName(),
                                MarksEntity::getMarksObtained
                        )
                ));
    }

    private List<Item> fetchGridData(List<User> students,
                                     List<String> distinctSubjectNames,
                                     Map<User, Map<String, Double>> studentMarksMap) {
        List<Item> gridData = new ArrayList<>();

        for (User user : students) {
            Map<String, Double> subjectMarks = studentMarksMap.getOrDefault(user, Collections.emptyMap());

            List<String> rowData = new ArrayList<>();
            rowData.add(user.getUsername());
            rowData.add(user.getRegisterNumber());

            for (String subjectName : distinctSubjectNames) {
                if (subjectMarks.containsKey(subjectName)) {
                    Double marksObtained = subjectMarks.get(subjectName);
                    if (marksObtained != null) {
                        rowData.add(marksObtained.toString());
                    } else {
                        rowData.add("Absent");
                    }
                } else {
                    rowData.add("N/A"); // Empty cell for subjects without marks
                }
            }

            gridData.add(new Item(user.getUsername(), user.getRegisterNumber(), rowData));
        }

        return gridData;
    }

}
