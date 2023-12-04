package com.nasc.application.views.activeusers;

import com.flowingcode.vaadin.addons.badgelist.Badge;
import com.flowingcode.vaadin.addons.badgelist.BadgeList;
import com.nasc.application.services.UserService;
import com.nasc.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "get-online-users", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class ActiveUsersView extends Div {
    public ActiveUsersView(UserService userService) {
        add(createActiveUserGrid(userService));
    }

    private Component createActiveUserGrid(UserService userService) {
        // Header
        HorizontalLayout header = createHeader("Active Users", "Online");

        // Grid
        Grid<UserDetails> grid = new Grid<>();
        grid.addThemeVariants(GridVariant.MATERIAL_COLUMN_DIVIDERS);
        grid.setAllRowsVisible(true);

        // Columns
        grid.addColumn(UserDetails::getUsername)
                .setHeader("User Name")
                .setAutoWidth(true)
                .setSortable(true);

/*        grid.addColumn(userDetails -> userDetails.getAuthorities().stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(", ")))
                .setHeader("User Roles")
                .setAutoWidth(true)
                .setSortable(true);*/

        grid.addComponentColumn(userDetails -> {
            List<String> roleNames = userDetails.getAuthorities().stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
            return createBadgeList(roleNames);
        }).setHeader("User Roles").setAutoWidth(true).setSortable(true);


        // Set items
        grid.setItems(userService.getOnlineUsers());

        // Add it all together
        VerticalLayout activeUsersLayout = new VerticalLayout(header, grid);
        activeUsersLayout.addClassName(LumoUtility.Padding.LARGE);
        activeUsersLayout.setPadding(false);
        activeUsersLayout.setSpacing(false);
        activeUsersLayout.getElement().getThemeList().add("spacing-l");
        return activeUsersLayout;
    }


    private HorizontalLayout createHeader(String title, String subtitle) {
        H2 h2 = new H2(title);
        h2.addClassNames(LumoUtility.FontSize.XLARGE, LumoUtility.Margin.NONE);

        Span span = new Span(subtitle);
        span.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);

        VerticalLayout column = new VerticalLayout(h2, span);
        column.setPadding(false);
        column.setSpacing(false);

        HorizontalLayout header = new HorizontalLayout(column);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setSpacing(false);
        header.setWidthFull();
        return header;
    }

    private BadgeList createBadgeList(List<String> roles) {
        List<Badge> badges = new ArrayList<>();
        roles.forEach(role -> badges.add(new Badge(role)));
        return new BadgeList(badges);
    }
}
