package com.nasc.application.data.components;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.nasc.application.data.core.BloodGroupEntity;
import com.nasc.application.services.BloodGroupService;
import com.nasc.application.services.dataprovider.GenericDataProvider;
import com.nasc.application.utils.NotificationUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.crud.BinderCrudEditor;
import com.vaadin.flow.component.crud.Crud;
import com.vaadin.flow.component.crud.CrudEditor;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.spring.annotation.UIScope;
import com.vaadin.flow.theme.lumo.LumoIcon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.xdev.vaadin.grid_exporter.GridExporter;
import software.xdev.vaadin.grid_exporter.column.ColumnConfigurationBuilder;

@Component
@UIScope
public class CreateBloodGroupCrud extends VerticalLayout {
    private final String EDIT_COLUMN = "vaadin-crud-edit-column";
    private final BloodGroupService service;
    private final Crud<BloodGroupEntity> crud;

    @Autowired
    public CreateBloodGroupCrud(BloodGroupService service) {
        this.service = service;
        crud = new Crud<>(BloodGroupEntity.class, createEditor());
        createGrid();
        setupDataProvider();
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        Button exportButton = new Button(
                "Export",
                FontAwesome.Solid.FILE_EXPORT.create(),
                e -> {
                    int size = crud.getDataProvider().size(new Query<>());
                    if (size > 0) {
                        String fileName = "Blood Group";
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
        horizontalLayout.setWidthFull();
        horizontalLayout.setJustifyContentMode(JustifyContentMode.END);
        horizontalLayout.setAlignItems(Alignment.CENTER);
        horizontalLayout.add(exportButton);

        Button newItemBtn = new Button("Create New Blood Group", LumoIcon.PLUS.create());
        newItemBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        crud.setNewButton(newItemBtn);

        setAlignItems(Alignment.STRETCH);
        expand(crud);
        setSizeFull();

        add(horizontalLayout, crud);
    }

    private CrudEditor<BloodGroupEntity> createEditor() {
        TextField field = new TextField("Blood Group");

        FormLayout form = new FormLayout(field);
        form.setMaxWidth("480px");
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("30em", 2));

        Binder<BloodGroupEntity> binder = new Binder<>(BloodGroupEntity.class);
        binder.forField(field).asRequired().bind(BloodGroupEntity::getName, BloodGroupEntity::setName);

        return new BinderCrudEditor<>(binder, form);
    }

    private void createGrid() {
        Grid<BloodGroupEntity> grid = crud.getGrid();

        grid.removeColumnByKey("id");
        grid.getColumnByKey(EDIT_COLUMN).setHeader("Edit");
        grid.getColumnByKey(EDIT_COLUMN).setWidth("100px");
        grid.getColumnByKey(EDIT_COLUMN).setResizable(false);
    }

    private void setupDataProvider() {
        GenericDataProvider<BloodGroupEntity> genericDataProvider =
                new GenericDataProvider<>(BloodGroupEntity.class, service);
        crud.setDataProvider(genericDataProvider);
        crud.addDeleteListener(deleteEvent -> {
            genericDataProvider.delete(deleteEvent.getItem());
            NotificationUtils.showSuccessNotification("Blood Group Deleted Successfully");
        });
        crud.addSaveListener(saveEvent -> {
            genericDataProvider.persist(saveEvent.getItem());
            NotificationUtils.showSuccessNotification("Blood Group Saved Successfully");
        });
    }

}