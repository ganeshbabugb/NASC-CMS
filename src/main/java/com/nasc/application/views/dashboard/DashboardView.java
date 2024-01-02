package com.nasc.application.views.dashboard;

import com.nasc.application.services.UserService;
import com.nasc.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.board.Board;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.theme.lumo.LumoUtility.*;
import jakarta.annotation.security.PermitAll;

@PageTitle("Dashboard")
@Route(value = "dashboard", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@PermitAll
public class DashboardView extends Main {

    public DashboardView(UserService userService) {
        addClassName("dashboard-view");
        Board board = new Board();
        board.addRow(
                createHighlight("Total Users", String.valueOf(userService.count())),
                createHighlight("Active Users", String.valueOf(userService.getOnlineUsers().size())));
        add(board);
    }

    private Component createHighlight(String title, String value) {
        H2 h2 = new H2(title);
        h2.addClassNames(FontWeight.NORMAL, Margin.NONE, TextColor.SECONDARY, FontSize.SMALL);
        Span span = new Span(value);
        span.addClassNames(FontWeight.SEMIBOLD, FontSize.XXXLARGE);
        VerticalLayout layout = new VerticalLayout(h2, span);
        layout.addClassName(Padding.LARGE);
        layout.setPadding(false);
        layout.setSpacing(false);
        return layout;
    }
}
