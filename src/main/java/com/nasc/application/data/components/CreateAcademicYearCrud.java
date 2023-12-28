package com.nasc.application.data.components;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.nasc.application.data.core.AcademicYearEntity;
import com.nasc.application.services.AcademicYearService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.xdev.vaadin.grid_exporter.GridExporter;
import software.xdev.vaadin.grid_exporter.column.ColumnConfigurationBuilder;

import java.util.List;

@Component
@UIScope
public class CreateAcademicYearCrud extends VerticalLayout {

    private final String EDIT_COLUMN = "vaadin-crud-edit-column";
    private final AcademicYearService service;
    private final Crud<AcademicYearEntity> crud;

    @Autowired
    public CreateAcademicYearCrud(AcademicYearService service) {
        this.service = service;
        crud = new Crud<>(AcademicYearEntity.class, createEditor());
        createGrid();
        setupDataProvider();
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        Button exportButton = new Button(
                "Export",
                FontAwesome.Solid.FILE_EXPORT.create(),
                e -> {
                    // Check if there are any items in the data source
                    int size = crud.getDataProvider().size(new Query<>());
                    if (size > 0) {
                        String fileName = "AcademicYear";
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
        horizontalLayout.setJustifyContentMode(JustifyContentMode.END);
        horizontalLayout.setAlignItems(Alignment.CENTER);
        add(horizontalLayout, crud);
    }

    private CrudEditor<AcademicYearEntity> createEditor() {
        TextField startYearField = new TextField("Start Year");
        TextField endYearField = new TextField("End Year");

        FormLayout form = new FormLayout(startYearField, endYearField);
        form.setMaxWidth("480px");
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("30em", 2));

        Binder<AcademicYearEntity> binder = new Binder<>(AcademicYearEntity.class);
        binder.forField(startYearField).asRequired().bind(AcademicYearEntity::getStartYear, AcademicYearEntity::setStartYear);
        binder.forField(endYearField).asRequired().bind(AcademicYearEntity::getEndYear, AcademicYearEntity::setEndYear);

        return new BinderCrudEditor<>(binder, form);
    }

    private void createGrid() {
        Grid<AcademicYearEntity> grid = crud.getGrid();

        grid.removeColumnByKey("id");

        Grid.Column<AcademicYearEntity> startYear = grid.getColumnByKey("startYear");
        Grid.Column<AcademicYearEntity> endYear = grid.getColumnByKey("endYear");
        Grid.Column<AcademicYearEntity> columnByKey = grid.getColumnByKey(EDIT_COLUMN);

        List<Grid.Column<AcademicYearEntity>> columns = List.of(startYear, endYear, columnByKey);

        grid.getColumnByKey(EDIT_COLUMN).setHeader("Edit");
        grid.getColumnByKey(EDIT_COLUMN).setWidth("100px");
        grid.getColumnByKey(EDIT_COLUMN).setResizable(false);

        grid.setColumnOrder(columns);
    }

    private void setupDataProvider() {
        GenericDataProvider<AcademicYearEntity> genericDataProvider =
                new GenericDataProvider<>(AcademicYearEntity.class, service);
        crud.setDataProvider(genericDataProvider);
        crud.addDeleteListener(deleteEvent -> {
            genericDataProvider.delete(deleteEvent.getItem());
            NotificationUtils.showSuccessNotification("Academic year deleted successfully");
        });
        crud.addSaveListener(saveEvent -> {
            genericDataProvider.persist(saveEvent.getItem());
            NotificationUtils.showSuccessNotification("Academic year saved successfully");
        });
    }
}