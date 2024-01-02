package com.nasc.application.views.valuevalut;

import com.nasc.application.data.components.*;
import com.nasc.application.views.MainLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.UIScope;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.stereotype.Component;

@PageTitle("Value Vault")
@Route(value = "value-vault", layout = MainLayout.class)
@RolesAllowed("EDITOR")
@UIScope
@Component
public class ValueVaultView extends Div {

    public ValueVaultView(
            CreateStateCrud createStateCrud,
            CreateDistrictCrud createDistrictCrud,
            CreateCountryCrud createCountryCrud,
            CreateBloodGroupCrud createBloodGroupCrud,
            CreateDepartmentCrud createDepartmentCrud,
            CreateAcademicYearCrud createAcademicYearCrud
    ) {
        TabSheet tabSheet = new TabSheet();
        tabSheet.add("State", createStateCrud);
        tabSheet.add("District", createDistrictCrud);
        tabSheet.add("Country", createCountryCrud);
        tabSheet.add("Blood Group", createBloodGroupCrud);
        tabSheet.add("Department", createDepartmentCrud);
        tabSheet.add("Academic Year", createAcademicYearCrud);
        tabSheet.setSizeFull();
        setSizeFull();
        add(tabSheet);
    }
}
