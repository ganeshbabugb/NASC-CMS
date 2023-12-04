package com.nasc.application.data.components;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.nasc.application.data.model.DepartmentEntity;
import com.nasc.application.services.DepartmentService;
import com.nasc.application.services.dataprovider.GenericDataProvider;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.crud.BinderCrudEditor;
import com.vaadin.flow.component.crud.Crud;
import com.vaadin.flow.component.crud.CrudEditor;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.xdev.vaadin.grid_exporter.GridExporter;

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
                e -> GridExporter.newWithDefaults(crud.getGrid())
                        //Removing Edit Column For Export
                        .withColumnFilter(stateEntityColumn -> !stateEntityColumn.getKey().equals(EDIT_COLUMN))
                        .open());
        horizontalLayout.setWidthFull();
        horizontalLayout.setJustifyContentMode(JustifyContentMode.END);
        horizontalLayout.setAlignItems(Alignment.CENTER);
        horizontalLayout.add(button);
        add(horizontalLayout, crud);
    }

    private CrudEditor<DepartmentEntity> createEditor() {
        TextField field = new TextField("Department Name");

        FormLayout form = new FormLayout(field);
        form.setMaxWidth("480px");
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("30em", 2));

        Binder<DepartmentEntity> binder = new Binder<>(DepartmentEntity.class);
        binder.forField(field).asRequired().bind(DepartmentEntity::getName, DepartmentEntity::setName);

        return new BinderCrudEditor<>(binder, form);
    }

    private void createGrid() {
        Grid<DepartmentEntity> grid = crud.getGrid();

        grid.removeColumnByKey("id");
        grid.getColumnByKey(EDIT_COLUMN).setHeader("Edit");
        grid.getColumnByKey(EDIT_COLUMN).setWidth("100px");
        grid.getColumnByKey(EDIT_COLUMN).setResizable(false);
    }

    private void setupDataProvider() {
        GenericDataProvider<DepartmentEntity> genericDataProvider =
                new GenericDataProvider<>(DepartmentEntity.class, service);
        crud.setDataProvider(genericDataProvider);
        crud.addDeleteListener(deleteEvent -> {
            confirmDelete(deleteEvent.getItem());
            genericDataProvider.delete(deleteEvent.getItem());
        });
        crud.addSaveListener(saveEvent -> {
            genericDataProvider.persist(saveEvent.getItem());
            showSaveNotification();
        });
    }

    private void confirmDelete(DepartmentEntity item) {
        Notification.show("Item deleted: " + item.toString(),
                5000,
                Notification.Position.BOTTOM_END);
    }

    private void showSaveNotification() {
        Notification.show("Item saved successfully",
                5000,
                Notification.Position.BOTTOM_END);
    }

}