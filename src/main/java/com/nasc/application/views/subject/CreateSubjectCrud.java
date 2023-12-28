package com.nasc.application.views.subject;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.nasc.application.data.core.SubjectEntity;
import com.nasc.application.data.core.enums.MajorOfPaper;
import com.nasc.application.data.core.enums.PaperType;
import com.nasc.application.data.core.enums.Semester;
import com.nasc.application.services.SubjectService;
import com.nasc.application.services.dataprovider.GenericDataProvider;
import com.nasc.application.utils.NotificationUtils;
import com.nasc.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.crud.BinderCrudEditor;
import com.vaadin.flow.component.crud.Crud;
import com.vaadin.flow.component.crud.CrudEditor;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.UIScope;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.xdev.vaadin.grid_exporter.GridExporter;
import software.xdev.vaadin.grid_exporter.column.ColumnConfigurationBuilder;

@Component
@UIScope
@Route(value = "create-subject", layout = MainLayout.class)
@RolesAllowed("HOD")
@PageTitle("Create Subject")
public class CreateSubjectCrud extends VerticalLayout {
    public static final String EDIT_COLUMN = "vaadin-crud-edit-column";
    private final SubjectService service;
    private final Crud<SubjectEntity> crud;

    @Autowired
    public CreateSubjectCrud(SubjectService service) {
        this.service = service;
        crud = new Crud<>(SubjectEntity.class, createEditor());
        createGrid();
        setupDataProvider();
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        Button exportButton = new Button(
                "Export",
                FontAwesome.Solid.FILE_EXPORT.create(),
                e -> {
                    int size = crud.getGrid().getDataProvider().size(new Query<>());
                    if (size > 0) {
                        String fileName = "Department Subjects";
                        GridExporter.newWithDefaults(crud.getGrid())
                                //Removing Edit Column For Export
                                .withColumnFilter(stateEntityColumn -> !stateEntityColumn.getKey().equals(EDIT_COLUMN))
                                .withFileName(fileName)
                                .withColumnConfigurationBuilder(new ColumnConfigurationBuilder())
                                .open();
                    }
                }
        );
        exportButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        horizontalLayout.add(exportButton);
        horizontalLayout.setWidthFull();
        horizontalLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        horizontalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        add(horizontalLayout, crud);
    }

    private CrudEditor<SubjectEntity> createEditor() {
        MajorOfPaper[] majorOfPaperEnum = MajorOfPaper.values();
        PaperType[] paperTypeEnum = PaperType.values();
        Semester[] semesterEnum = Semester.values();

        TextField subjectNameField = new TextField("Subject Name");
        TextField subjectShortNameField = new TextField("Subject Short Name");
        TextField subjectCodeField = new TextField("Subject Code");

        ComboBox<PaperType> typeOfPaperComboBox = new ComboBox<>("Type of Paper");
        typeOfPaperComboBox.setItems(paperTypeEnum);
        typeOfPaperComboBox.setItemLabelGenerator(PaperType::getDisplayName);

        ComboBox<MajorOfPaper> majorOfPaperComboBox = new ComboBox<>("Major of Paper");
        majorOfPaperComboBox.setItems(majorOfPaperEnum);
        majorOfPaperComboBox.setItemLabelGenerator(MajorOfPaper::getDisplayName);

        ComboBox<Semester> semesterComboBox = new ComboBox<>("Semester");
        semesterComboBox.setItems(semesterEnum);
        semesterComboBox.setItemLabelGenerator(Semester::getDisplayName);

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
