package com.nasc.application.data.components;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.nasc.application.data.model.StateEntity;
import com.nasc.application.services.StateService;
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
public class CreateStateCrud extends VerticalLayout {

    private final String EDIT_COLUMN = "vaadin-crud-edit-column";
    private final StateService stateService;
    private final Crud<StateEntity> crud;

    @Autowired
    public CreateStateCrud(StateService stateService) {
        this.stateService = stateService;
        crud = new Crud<>(StateEntity.class, createEditor());
        createGrid();
        setupDataProvider();
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.add(
                new Button(
                        "Export",
                        FontAwesome.Solid.FILE_EXPORT.create(),
                        e -> GridExporter.newWithDefaults(crud.getGrid())

                                //Removing Edit Column For Export
                                .withColumnFilter(stateEntityColumn -> !stateEntityColumn.getKey().equals(EDIT_COLUMN))
                                .open())
        );
        horizontalLayout.setWidthFull();
        horizontalLayout.setJustifyContentMode(JustifyContentMode.END);
        horizontalLayout.setAlignItems(Alignment.CENTER);
        add(horizontalLayout, crud);
    }

    private CrudEditor<StateEntity> createEditor() {
        TextField field = new TextField("State name");

        FormLayout form = new FormLayout(field);
        form.setMaxWidth("480px");
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("30em", 2));

        Binder<StateEntity> binder = new Binder<>(StateEntity.class);
        binder.forField(field).asRequired().bind(StateEntity::getName, StateEntity::setName);

        return new BinderCrudEditor<>(binder, form);
    }

    private void createGrid() {
        Grid<StateEntity> grid = crud.getGrid();

        grid.removeColumnByKey("id");
        grid.getColumnByKey(EDIT_COLUMN).setHeader("Edit");
        grid.getColumnByKey(EDIT_COLUMN).setWidth("100px");
        grid.getColumnByKey(EDIT_COLUMN).setResizable(false);
    }

    private void setupDataProvider() {
        GenericDataProvider<StateEntity> genericDataProvider =
                new GenericDataProvider<>(StateEntity.class, stateService);
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

    private void confirmDelete(StateEntity item) {
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