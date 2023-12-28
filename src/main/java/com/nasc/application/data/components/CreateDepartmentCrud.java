package com.nasc.application.data.components;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.nasc.application.data.core.DepartmentEntity;
import com.nasc.application.services.DepartmentService;
import com.nasc.application.services.dataprovider.GenericDataProvider;
import com.nasc.application.utils.NotificationUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.crud.BinderCrudEditor;
import com.vaadin.flow.component.crud.Crud;
import com.vaadin.flow.component.crud.CrudEditor;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.xdev.vaadin.grid_exporter.GridExporter;
import software.xdev.vaadin.grid_exporter.column.ColumnConfigurationBuilder;


@Component
@UIScope
public class CreateDepartmentCrud extends VerticalLayout {

    private final String EDIT_COLUMN = "vaadin-crud-edit-column";
    private final DepartmentService service;
    private final Crud<DepartmentEntity> crud;

    @Autowired
    public CreateDepartmentCrud(DepartmentService service) {
        this.service = service;
        crud = new Crud<>(DepartmentEntity.class, createEditor());
        createGrid();
        setupDataProvider();
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        Button button = new Button(
                "Export",
                FontAwesome.Solid.FILE_EXPORT.create(),
                e -> {
                    String fileName = "Department";
                    GridExporter.newWithDefaults(crud.getGrid())
                            //Removing Edit Column For Export
                            .withColumnFilter(stateEntityColumn -> !stateEntityColumn.getKey().equals(EDIT_COLUMN))
                            .withFileName(fileName)
                            .withColumnConfigurationBuilder(new ColumnConfigurationBuilder())
                            .open();
                });
        horizontalLayout.setWidthFull();
        horizontalLayout.setJustifyContentMode(JustifyContentMode.END);
        horizontalLayout.setAlignItems(Alignment.CENTER);
        horizontalLayout.add(button);
        add(horizontalLayout, crud);
    }

    private CrudEditor<DepartmentEntity> createEditor() {
        TextField departmentNameTextFiled = new TextField("Department Name");
        TextField shortNameTextField = new TextField("Short Name");

        FormLayout form = new FormLayout(departmentNameTextFiled, shortNameTextField);
        form.setMaxWidth("480px");
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("30em", 2));

        Binder<DepartmentEntity> binder = new Binder<>(DepartmentEntity.class);
        binder.forField(departmentNameTextFiled).asRequired().bind(DepartmentEntity::getName, DepartmentEntity::setName);
        binder.forField(shortNameTextField).asRequired().bind(DepartmentEntity::getShortName, DepartmentEntity::setShortName);

        return new BinderCrudEditor<>(binder, form);
    }

    private void createGrid() {
        Grid<DepartmentEntity> grid = crud.getGrid();

        grid.removeColumnByKey("id");
        grid.removeColumnByKey("subjects");

        grid.getColumnByKey(EDIT_COLUMN).setHeader("Edit");
        grid.getColumnByKey(EDIT_COLUMN).setWidth("100px");
        grid.getColumnByKey(EDIT_COLUMN).setResizable(false);
    }

    private void setupDataProvider() {
        GenericDataProvider<DepartmentEntity> genericDataProvider =
                new GenericDataProvider<>(DepartmentEntity.class, service);
        crud.setDataProvider(genericDataProvider);
        crud.addDeleteListener(deleteEvent -> {
            genericDataProvider.delete(deleteEvent.getItem());
            NotificationUtils.showSuccessNotification("Department Deleted Successfully");
        });
        crud.addSaveListener(saveEvent -> {
            genericDataProvider.persist(saveEvent.getItem());
            NotificationUtils.showSuccessNotification("Department Saved Successfully");
        });
    }
}