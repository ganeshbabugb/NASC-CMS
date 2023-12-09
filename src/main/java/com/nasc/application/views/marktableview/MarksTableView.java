package com.nasc.application.views.marktableview;

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
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Route(value = "marks-table", layout = MainLayout.class)
@PageTitle("Marks Table")
@RolesAllowed({"HOD", "ADMIN"})
public class MarksTableView extends VerticalLayout {

    private final MarksService marksService;
    private final UserService userService;
    private final Grid<MarksEntity> marksGrid;
    private final ComboBox<Semester> semesterFilter;
    private final ComboBox<ExamType> examTypeFilter;
    private final ComboBox<AcademicYearEntity> academicYearFilter;
    private final ComboBox<DepartmentEntity> departmentFilter;
    private final Button searchButton;
    private final SubjectRepository subjectRepository;

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
        Semester[] semester = Semester.values();
        ExamType[] examType = ExamType.values();

        // Create filter components
        semesterFilter = new ComboBox<>("Filter by Semester");
        semesterFilter.setItems(semester);

        examTypeFilter = new ComboBox<>("Filter by Exam Type");
        examTypeFilter.setItems(examType);
        examTypeFilter.addValueChangeListener(event -> updateSubjectFilter());

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

        // Add the grid and filter components to the layout
        add(createFilterLayout(), marksGrid);
    }

    private HorizontalLayout createFilterLayout() {
        HorizontalLayout filterLayout = new HorizontalLayout();
        filterLayout.setAlignItems(Alignment.BASELINE); // To align everything in a same line
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

        // Create a map to store student marks for each subject
        Map<User, Map<String, Double>> studentMarksMap = fetchStudentMarksMap(students);

        // Set the grid columns dynamically based on subjects
        marksGrid.removeAllColumns();
        marksGrid.addColumn(marksEntity -> marksEntity.getStudent().getUsername()).setHeader("Student Name");
        marksGrid.addColumn(marksEntity -> marksEntity.getStudent().getRegisterNumber()).setHeader("Register Number");

        // Add columns for each subject
        for (String subjectName : distinctSubjectNames) {
            marksGrid.addColumn(mark -> getMarksForSubject(mark, studentMarksMap, subjectName))
                    .setHeader(subjectName);
        }

        // Set the filtered marks data in the grid
        marksGrid.setItems(fetchFilteredMarks());
    }

    private Map<User, Map<String, Double>> fetchStudentMarksMap(List<User> students) {
        return marksService.getAllMarksByStudents(students).stream()
                .collect(Collectors.groupingBy(
                        MarksEntity::getStudent,
                        Collectors.toMap(
                                mark -> mark.getSubject().getSubjectName(),
                                MarksEntity::getMarksObtained
                        )
                ));
    }

    private List<MarksEntity> fetchFilteredMarks() {

        // Assuming 'students' is a class member
        List<User> selectedStudents = userService.findStudentsByDepartmentAndRoleAndAcademicYear(
                departmentFilter.getValue(), Role.STUDENT, academicYearFilter.getValue()
        );

        return marksService.getAllMarksByStudents(selectedStudents);
    }

    private Double getMarksForSubject(MarksEntity mark, Map<User, Map<String, Double>> studentMarksMap, String subjectName) {
        Map<String, Double> subjectMarks = studentMarksMap.get(mark.getStudent());
        if (subjectMarks != null && subjectMarks.containsKey(subjectName)) {
            return subjectMarks.get(subjectName);
        } else {
            return 0.0;
        }
    }

    private void updateSubjectFilter() {
        updateGrid(); // Update the grid based on the selected semester and exam type
    }
}
