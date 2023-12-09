package com.nasc.application.views.createsubjectcrud;

import com.nasc.application.data.model.SubjectEntity;
import com.nasc.application.data.model.enums.MajorOfPaper;
import com.nasc.application.data.model.enums.PaperType;
import com.nasc.application.data.model.enums.Semester;
import com.nasc.application.services.SubjectService;
import com.nasc.application.services.dataprovider.GenericDataProvider;
import com.nasc.application.utils.NotificationUtils;
import com.nasc.application.views.MainLayout;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.crud.BinderCrudEditor;
import com.vaadin.flow.component.crud.Crud;
import com.vaadin.flow.component.crud.CrudEditor;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.UIScope;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@UIScope
@Route(value = "create-subject", layout = MainLayout.class)
@RolesAllowed("HOD")
public class CreateSubjectCrud extends Div {

    private final SubjectService service;
    private final Crud<SubjectEntity> crud;

    @Autowired
    public CreateSubjectCrud(SubjectService service) {
        this.service = service;
        crud = new Crud<>(SubjectEntity.class, createEditor());
        createGrid();
        setupDataProvider();
        add(new VerticalLayout(crud));
    }

    private CrudEditor<SubjectEntity> createEditor() {
        TextField subjectNameField = new TextField("Subject Name");
        TextField subjectShortNameField = new TextField("Subject Short Name");
        TextField subjectCodeField = new TextField("Subject Code");

        ComboBox<PaperType> typeOfPaperComboBox = new ComboBox<>("Type of Paper");
        typeOfPaperComboBox.setItems(PaperType.values());

        ComboBox<MajorOfPaper> majorOfPaperComboBox = new ComboBox<>("Major of Paper");
        majorOfPaperComboBox.setItems(MajorOfPaper.values());

        ComboBox<Semester> semesterComboBox = new ComboBox<>("Semester");
        semesterComboBox.setItems(Semester.values());

        FormLayout form = new FormLayout(subjectNameField, subjectShortNameField, subjectCodeField,
                typeOfPaperComboBox, majorOfPaperComboBox, semesterComboBox);
        form.setMaxWidth("480px");
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("30em", 2));

        Binder<SubjectEntity> binder = new Binder<>(SubjectEntity.class);
        binder.forField(subjectNameField).asRequired().bind(SubjectEntity::getSubjectName, SubjectEntity::setSubjectName);
        binder.forField(subjectCodeField).bind(SubjectEntity::getSubjectCode, SubjectEntity::setSubjectCode);
        binder.forField(subjectShortNameField).asRequired().bind(SubjectEntity::getSubjectShortForm, SubjectEntity::setSubjectShortForm);
        binder.forField(typeOfPaperComboBox).bind(SubjectEntity::getTypeOfPaper, SubjectEntity::setTypeOfPaper);
        binder.forField(majorOfPaperComboBox).bind(SubjectEntity::getMajorOfPaper, SubjectEntity::setMajorOfPaper);
        binder.forField(semesterComboBox).bind(SubjectEntity::getSemester, SubjectEntity::setSemester);

        // Add bindings for other fields as needed
        return new BinderCrudEditor<>(binder, form);
    }

    private void createGrid() {
        Grid<SubjectEntity> grid = crud.getGrid();

        grid.removeColumnByKey("id");
        grid.removeColumnByKey("department");
        String EDIT_COLUMN = "vaadin-crud-edit-column";
        grid.getColumnByKey(EDIT_COLUMN).setHeader("Edit");
        grid.getColumnByKey(EDIT_COLUMN).setWidth("100px");
        grid.getColumnByKey(EDIT_COLUMN).setResizable(false);
    }

    private void setupDataProvider() {
        GenericDataProvider<SubjectEntity> genericDataProvider =
                new GenericDataProvider<>(SubjectEntity.class, service);
        crud.setDataProvider(genericDataProvider);
        crud.addDeleteListener(deleteEvent -> {
            genericDataProvider.delete(deleteEvent.getItem());
            NotificationUtils.showSuccessNotification("Subject deleted successfully");
        });
        crud.addSaveListener(saveEvent -> {
            genericDataProvider.persist(saveEvent.getItem());
            NotificationUtils.showSuccessNotification("Subject saved successfully");
        });
    }
}
