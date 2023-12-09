package com.nasc.application.views.markentryview;

import com.nasc.application.data.components.Divider;
import com.nasc.application.data.model.*;
import com.nasc.application.data.model.enums.ExamType;
import com.nasc.application.data.model.enums.Role;
import com.nasc.application.data.model.enums.Semester;
import com.nasc.application.security.AuthenticatedUser;
import com.nasc.application.services.*;
import com.nasc.application.utils.NotificationUtils;
import com.nasc.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Route(value = "subject-view", layout = MainLayout.class)
@PageTitle("Subject View")
@RolesAllowed({"HOD", "ADMIN", "PROFESSOR"})
public class MarkEntryView extends Div {

    private final VerticalLayout primaryLayout;
    private final VerticalLayout secondaryLayout;
    private final SplitLayout splitLayout;
    // service
    private final ExamService examService;
    private final DepartmentService departmentService;
    private final SubjectService subjectService;
    private final MarksService marksService;
    private final UserService userService;
    private final AcademicYearService academicYearService;
    // Global declaration for current user
    private final User currentUser;
    private ComboBox<DepartmentEntity> departmentComboBox;
    private ComboBox<Semester> semesterComboBox;
    private ComboBox<SubjectEntity> subjectComboBox;
    private TextField subjectShortFormTextField;
    private TextField majorTextField;
    private TextField typeOfPaperTextField;
    private TextField subjectCodeTextField;
    private ComboBox<AcademicYearEntity> academicYearComboBox;
    //Exam Fields
    private DatePicker examDateDatePicker;
    private ComboBox<ExamType> examTypeComboBox;
    private IntegerField minMarksIntegerField;
    private IntegerField maxMarksIntegerField;
    private NumberField portionCoveredNumberField;
    private IntegerField examDurationIntegerField;
    private DatePicker examCorrectionDatePicker;
    private MultiSelectComboBox<User> professorMultiSelectComboBox;
    private Button createExamButton;
    //Student and Exam ComboBoxes
    private ComboBox<ExamEntity> examComboBox;
    //Layouts
    private FormLayout markFormLayout;
    private FormLayout examFormLayout;
    private FormLayout formLayout;

    @Autowired
    public MarkEntryView(DepartmentService departmentService,
                         SubjectService subjectService,
                         MarksService marksService,
                         UserService userService,
                         ExamService examService,
                         AuthenticatedUser authenticatedUser,
                         AcademicYearService academicYearService
    ) {
        this.departmentService = departmentService;
        this.subjectService = subjectService;
        this.examService = examService;
        this.marksService = marksService;
        this.userService = userService;
        this.academicYearService = academicYearService;

        primaryLayout = new VerticalLayout();
        secondaryLayout = new VerticalLayout();

        primaryLayout.setMaxHeight("90vh");
        secondaryLayout.setMaxHeight("90vh");
        splitLayout = new SplitLayout(primaryLayout, secondaryLayout);

        currentUser = authenticatedUser.get().get();

        initMarkForm();

        initExamComboBoxes();
        primaryLayout.add(new Divider());

        initExamForm();


        departmentComboBox.addValueChangeListener(event -> {
            updateSubjectOptions();
        });

        semesterComboBox.addValueChangeListener(event -> {
            updateSubjectOptions();
        });

        examComboBox.addValueChangeListener(event -> {
            // Fetch the selected exam
            ExamEntity selectedExam = event.getValue();

            // Update the student input fields based on the selected exam
            updateStudentFieldsForExam(selectedExam);
        });

        semesterComboBox.addValueChangeListener(event -> {
            updateSubjectOptions();
        });

        subjectComboBox.addValueChangeListener(event -> {

            // No need to call updateSubjectOptions again, as it is automatically called when department or semester changes
            DepartmentEntity selectedDepartment = departmentComboBox.getValue();
            Semester selectedSemester = semesterComboBox.getValue();

            // Fetch exams based on the selected subject, department, and semester
            updateExamOptions(selectedDepartment, selectedSemester);
        });
    }

    private void initMarkForm() {

        // Populate department and semester dropdowns
        List<DepartmentEntity> departments = departmentService.findAll();
        List<Semester> semesters = List.of(Semester.values());
        List<AcademicYearEntity> academicYears = academicYearService.findAll();

        //Creating forms
        markFormLayout = new FormLayout();

        departmentComboBox = new ComboBox<>("Select Department", departments);
        semesterComboBox = new ComboBox<>("Select Semester", semesters);

        // Setup subject dropdown and subject code text-field
        subjectComboBox = new ComboBox<>("Select Subject");

        academicYearComboBox = new ComboBox<>("Academic Year");
        academicYearComboBox.setItems(academicYears);
        academicYearComboBox.setItemLabelGenerator(item -> item.getStartYear() + "-" + item.getEndYear());

        subjectComboBox.setPlaceholder("Select a subject");

        subjectShortFormTextField = new TextField("Subject Short Form");
        subjectShortFormTextField.setReadOnly(true);

        majorTextField = new TextField("Major");
        majorTextField.setReadOnly(true);

        subjectCodeTextField = new TextField("Subject Code");
        subjectCodeTextField.setReadOnly(true);

        typeOfPaperTextField = new TextField("Type Of Paper");
        typeOfPaperTextField.setReadOnly(true);

        // Add a listener to handle subject selection
        subjectComboBox.addValueChangeListener(event -> handleSubjectSelection());

        markFormLayout.add(departmentComboBox,
                semesterComboBox,
                subjectComboBox,
                subjectShortFormTextField,
                majorTextField,
                typeOfPaperTextField,
                subjectCodeTextField,
                academicYearComboBox
        );

        primaryLayout.add(markFormLayout);

        add(splitLayout);
    }

    private void initExamForm() {

        //Creating exam form
        examFormLayout = new FormLayout();

        // TODO : ADD HOD ROLES TO THE DROP DOWN!.
        List<User> professors = userService.findUsersByRole(Role.PROFESSOR);

        examDateDatePicker = new DatePicker("Exam Date");
        examTypeComboBox = new ComboBox<>("Exam Type", ExamType.values());
        minMarksIntegerField = new IntegerField("Minimum Marks");
        maxMarksIntegerField = new IntegerField("Maximum Marks");
        portionCoveredNumberField = new NumberField("Portion Covered");
        examDurationIntegerField = new IntegerField("Exam Duration (minutes)");
        examCorrectionDatePicker = new DatePicker("Exam Correction Date");

        createExamButton = new Button("Create Exam", event -> createExam());
        createExamButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        professorMultiSelectComboBox = new MultiSelectComboBox<>("Subject Staffs");
        professorMultiSelectComboBox.setItems(professors);
        professorMultiSelectComboBox.setPlaceholder("Select persons");
        professorMultiSelectComboBox.setItemLabelGenerator(item -> item.getUsername() + " [" + item.getRegisterNumber() + "]");

        TextArea selectedStaffTextArea = new TextArea("Selected Staff");
        selectedStaffTextArea.setReadOnly(true);

        professorMultiSelectComboBox.addValueChangeListener(e -> {
            String selectedCountriesText = e.getValue().stream()
                    .map(User::getUsername).collect(Collectors.joining(", "));

            selectedStaffTextArea.setValue(selectedCountriesText);
        });

        examFormLayout.add(
                examDateDatePicker,
                examTypeComboBox,
                minMarksIntegerField,
                maxMarksIntegerField,
                portionCoveredNumberField,
                examDurationIntegerField,
                examCorrectionDatePicker,
                professorMultiSelectComboBox,
                selectedStaffTextArea,
                createExamButton
        );

        primaryLayout.add(examFormLayout);
    }

    private void createExam() {
        ExamEntity newExam = createNewExam(); // Implement this method to create a new exam
        examComboBox.setItems(examService.getAllExams()); // Refresh the examComboBox
        examComboBox.setValue(newExam); // Set the newly created exam as the selected exam
        handleExamSelection(); // Trigger the handleExamSelection logic
    }

    private ExamEntity createNewExam() {
        // Implement the logic to create a new exam
        ExamEntity exam = new ExamEntity();
        exam.setDepartment(departmentComboBox.getValue());
        exam.setSemester(semesterComboBox.getValue());
        exam.setExamType(examTypeComboBox.getValue());
        exam.setExamDate(examDateDatePicker.getValue());
        exam.setSubject(subjectComboBox.getValue());
        exam.setMinMarks(minMarksIntegerField.getMin());
        exam.setMaxMarks(maxMarksIntegerField.getMax());
        exam.setPortionCovered(portionCoveredNumberField.getValue());
        exam.setExamDuration(examDurationIntegerField.getValue());
        exam.setExamCorrectionDate(examCorrectionDatePicker.getValue());

        Set<User> selectedProfessors = professorMultiSelectComboBox.getSelectedItems();
        if (!selectedProfessors.isEmpty()) {
            // Add both the current user and selected professors to the responsibleUsers set
            Set<User> responsibleUsers = new HashSet<>(selectedProfessors);
            responsibleUsers.add(currentUser);

            exam.setResponsibleUsers(responsibleUsers);
        } else {
            // If no professors are selected, only add the current user
            exam.setResponsibleUsers(Set.of(currentUser));
        }

        examService.saveExam(exam); // Save the new exam
        return exam;
    }

    private void updateSubjectOptions() {
        DepartmentEntity selectedDepartment = departmentComboBox.getValue();
        Semester selectedSemester = semesterComboBox.getValue();

        if (selectedDepartment != null && selectedSemester != null) {
            // Fetch subjects based on the selected department and semester
            List<SubjectEntity> subjects = subjectService.getSubjectsByDepartmentAndSemester(
                    selectedDepartment,
                    selectedSemester
            );

            // Update the subject dropdown options
            subjectComboBox.setItems(subjects);
            subjectComboBox.setItemLabelGenerator(
                    subject -> subject.getSubjectName() + " - " + subject.getSubjectShortForm()
            );
        } else {

            // Clear subject dropdown if either department or semester is not selected
            subjectComboBox.setItems();
        }
    }

    private void handleSubjectSelection() {
        SubjectEntity selectedSubject = subjectComboBox.getValue();

        if (selectedSubject != null) {
            subjectShortFormTextField.setValue(selectedSubject.getSubjectShortForm());
            subjectCodeTextField.setValue(selectedSubject.getSubjectCode());

            // Added toSting because value ENUM
            majorTextField.setValue(selectedSubject.getMajorOfPaper().toString());
            typeOfPaperTextField.setValue(selectedSubject.getTypeOfPaper().toString());
        } else {
            subjectShortFormTextField.clear();
            majorTextField.clear();
            typeOfPaperTextField.clear();
        }
    }

    private void createNewMarks(User student,
                                SubjectEntity subject,
                                ExamEntity exam,
                                Double marksObtained,
                                boolean isAbsent) {
        // Check if the obtained marks are valid
        if (!isAbsent) {
            if (isValidMark(marksObtained, exam)) {
                // Create a new MarksEntity
                MarksEntity marksEntity = new MarksEntity();
                marksEntity.setStudent(student);
                marksEntity.setSubject(subject);
                marksEntity.setExam(exam);
                marksEntity.setMarksObtained(marksObtained);

                // Save the marks
                marksService.saveMarks(marksEntity);

                NotificationUtils.showSuccessNotification("Marks saved successfully");
            } else {
                NotificationUtils.showErrorNotification("Please enter valid marks within the specified range");
            }
        } else {
            MarksEntity marksEntity = new MarksEntity();
            marksEntity.setStudent(student);
            marksEntity.setSubject(subject);
            marksEntity.setExam(exam);
            marksEntity.setAbsent(true);
            marksService.saveMarks(marksEntity);
        }
    }

    private void updateMarks(User student,
                             SubjectEntity subject,
                             ExamEntity exam,
                             Double marksObtained,
                             boolean isAbsent) {

        if (!isAbsent) {
            MarksEntity existingMarks = marksService.getMarksByStudentAndSubjectAndExam(student, subject, exam);

            // Update the marks and save
            existingMarks.setMarksObtained(marksObtained);
            marksService.saveMarks(existingMarks);

            // Display a success notification
            NotificationUtils.showSuccessNotification("Marks saved successfully");
        }

    }

    private void initExamComboBoxes() {
        formLayout = new FormLayout();
        examComboBox = new ComboBox<>("Select Exam");
        examComboBox.setItemLabelGenerator(item ->
                "DEPT: " + item.getDepartment().getShortName()
                        + ", SUB: " + item.getSubject().getSubjectShortForm()
                        + ", EXAM TYPE: " + item.getExamType()
                        + ", EXAM DATE: " + item.getExamDate()
        );

        // Add a listener to handle student and exam selection
        examComboBox.addValueChangeListener(event -> handleExamSelection());
        formLayout.add(examComboBox);
        primaryLayout.add(formLayout);
    }

    private void handleExamSelection() {
        ExamEntity selectedExam = examComboBox.getValue();

        if (selectedExam != null) {
            // You can add logic here based on the selected exam
            String message = "Selected exam: " + selectedExam.getExamType();
            NotificationUtils.showInfoNotification(message);
        } else {
            // Handle the case where no exam is selected
            NotificationUtils.showErrorNotification("Please select an exam");
        }
    }

    private void saveMarksForStudent(User selectedStudent,
                                     NumberField marksObtainedTextField,
                                     Checkbox absentCheckbox) {
        SubjectEntity selectedSubject = subjectComboBox.getValue();
        ExamEntity selectedExam = examComboBox.getValue();
        Double marksObtained = marksObtainedTextField.getValue();
        boolean isAbsent = absentCheckbox.getValue();

        if (selectedStudent != null && selectedSubject != null && selectedExam != null) {
            if (!isAbsent) {
                try {
                    if (marksService.existsByStudentAndSubjectAndExam(selectedStudent,
                            selectedSubject, selectedExam)) {
                        // The mark already exists, switch to update mode
                        updateMarks(selectedStudent, selectedSubject, selectedExam, marksObtained, isAbsent);
                    } else {
                        // Create a new MarksEntity
                        createNewMarks(selectedStudent, selectedSubject, selectedExam, marksObtained, isAbsent);
                    }
                } catch (NumberFormatException e) {
                    NotificationUtils.showErrorNotification("Please enter a valid number for Marks Obtained");
                }
            } else {
                createNewMarks(selectedStudent, selectedSubject, selectedExam, null, isAbsent);
            }
        } else {
            NotificationUtils.showErrorNotification("Please select a student, subject, and exam before saving marks");
        }
    }

    private boolean isValidMark(Double obtainedMark, ExamEntity exam) {
        Integer maxMark = exam.getMaxMarks();

        // Check if obtainedMark is within the valid range
        return obtainedMark != null && maxMark != null && obtainedMark <= maxMark.doubleValue();
    }

    private void updateExamOptions(DepartmentEntity department, Semester semester) {
        SubjectEntity selectedSubject = subjectComboBox.getValue();

        if (selectedSubject != null) {
            // Fetch exams based on the selected subject, department, and semester
            List<ExamEntity> exams = examService.getExamsByCriteria(
                    currentUser,
                    department,
                    semester
            );

            // Update the examComboBox options
            examComboBox.setItems(exams);
        } else {
            // Clear examComboBox if subject is not selected
            examComboBox.setItems();
        }
    }

    private void updateStudentFieldsForExam(ExamEntity exam) {
        // Clear the existing components in the secondary layout
        secondaryLayout.removeAll();

        // Fetch the students for the selected department, role, and academic year
        List<User> students = userService.findStudentsByDepartmentAndRoleAndAcademicYear(
                departmentComboBox.getValue(),
                Role.STUDENT,
                academicYearComboBox.getValue()
        );

        // Create a title for the secondary layout
        H3 examTittle = new H3("Exam: " + exam.getExamType() + " - " + exam.getSemester());
        String subjectDetails = "Subject: " + exam.getSubject().getSubjectName()
                + " - " + exam.getSubject().getSubjectShortForm()
                + " - " + exam.getSubject().getSubjectCode()
                + " - " + exam.getSubject().getMajorOfPaper()
                + " - " + exam.getSubject().getTypeOfPaper();
        H4 subjectTittle = new H4(subjectDetails);
        secondaryLayout.add(examTittle, subjectTittle);

        // Update the student input fields based on the selected exam
        students.forEach(student -> {
            String username = student.getUsername();
            String registerNumber = student.getRegisterNumber();
            NumberField studentTextField = new NumberField(username + " [" + registerNumber + "]");
            studentTextField.setPlaceholder("Enter marks");

            Checkbox absentCheckbox = new Checkbox("Absent");
            absentCheckbox.addValueChangeListener(event -> {
                studentTextField.setEnabled(!event.getValue()); // Disable Student field if the user is absent.
            });

            Button saveButton = new Button("Save", event -> saveMarksForStudent(student, studentTextField, absentCheckbox));
            saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

            // Fetch existing marks for the selected exam and student
            MarksEntity existingMarksEntity = marksService.getMarksByStudentAndSubjectAndExam(
                    student,
                    subjectComboBox.getValue(),
                    exam
            );

            if (existingMarksEntity != null) {

                // Set the initial value of the NumberField to existing marks
                studentTextField.setValue(existingMarksEntity.getMarksObtained());
                saveButton.setText("Update");
            }

            // Create a "Save" button for each student
            FormLayout studentLayout = new FormLayout(studentTextField, saveButton);

            secondaryLayout.add(studentLayout);
        });
    }
}