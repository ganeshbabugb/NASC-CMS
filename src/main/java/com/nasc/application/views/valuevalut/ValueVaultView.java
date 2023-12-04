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
        tabSheet.add("State", new Div(createStateCrud));
        tabSheet.add("District", new Div(createDistrictCrud));
        tabSheet.add("Country", new Div(createCountryCrud));
        tabSheet.add("Blood Group", new Div(createBloodGroupCrud));
        tabSheet.add("Department", new Div(createDepartmentCrud));
        tabSheet.add("Academic Year", new Div(createAcademicYearCrud));
        add(tabSheet);
    }

}
