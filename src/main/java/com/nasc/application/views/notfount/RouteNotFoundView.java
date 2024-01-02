package com.nasc.application.views.notfount;

import com.nasc.application.views.dashboard.DashboardView;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("/PageNotFound")
@PageTitle("Page Not Found")
@AnonymousAllowed
public class RouteNotFoundView extends VerticalLayout implements HasErrorParameter<NotFoundException> {

    public RouteNotFoundView() {
        setClassName("not-found-container");
        Paragraph errorMessage = new Paragraph("404 - Route not found");
        RouterLink dashboardLink = new RouterLink("Dashboard", DashboardView.class);
        add(errorMessage, dashboardLink);
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        getStyle().set("text-align", "center");
    }

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<NotFoundException> parameter) {
        return 404;
    }
}
