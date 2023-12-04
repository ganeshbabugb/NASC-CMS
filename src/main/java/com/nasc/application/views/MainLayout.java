package com.nasc.application.views;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.nasc.application.data.model.User;
import com.nasc.application.security.AuthenticatedUser;
import com.nasc.application.views.about.AboutView;
import com.nasc.application.views.activeusers.ActiveUsersView;
import com.nasc.application.views.addressform.AddressFormView;
import com.nasc.application.views.addressmasterdetail.AddressMasterDetailView;
import com.nasc.application.views.bankdetailsform.BankDetailsFormView;
import com.nasc.application.views.createstudents.CreateUsers;
import com.nasc.application.views.dashboard.DashboardView;
import com.nasc.application.views.password.PasswordChangeView;
import com.nasc.application.views.personform.PersonFormView;
import com.nasc.application.views.professor.ProfessorStatusView;
import com.nasc.application.views.studentmasterdetails.StudentMasterDetailsView;
import com.nasc.application.views.studentsstatus.StudentsStatusView;
import com.nasc.application.views.valuevalut.ValueVaultView;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.util.Optional;

/**
 * The main view is a top-level placeholder for other views.
 */
@PreserveOnRefresh
public class MainLayout extends AppLayout {

    private H2 viewTitle;

    private final AuthenticatedUser authenticatedUser;
    private final AccessAnnotationChecker accessChecker;

    public MainLayout(AuthenticatedUser authenticatedUser, AccessAnnotationChecker accessChecker) {
        this.authenticatedUser = authenticatedUser;
        this.accessChecker = accessChecker;

        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");

        viewTitle = new H2();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        addToNavbar(true, toggle, viewTitle);
    }

    private void addDrawerContent() {
        H1 appName = new H1("NASC");
        appName.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
        Header header = new Header(appName);
        Scroller scroller = new Scroller(createNavigation());
        addToDrawer(header, scroller, createFooter());
    }

    private SideNav createNavigation() {
        SideNav nav = new SideNav();

        if (accessChecker.hasAccess(DashboardView.class)) {
            nav.addItem(new SideNavItem("Dashboard", DashboardView.class, LineAwesomeIcon.CHART_AREA_SOLID.create()));

        }
        if (accessChecker.hasAccess(PersonFormView.class)) {
            nav.addItem(new SideNavItem("Person Form", PersonFormView.class, LineAwesomeIcon.USER.create()));

        }
        if (accessChecker.hasAccess(AddressFormView.class)) {
            nav.addItem(
                    new SideNavItem("Address Form", AddressFormView.class, LineAwesomeIcon.MAP_MARKER_SOLID.create()));

        }
        if (accessChecker.hasAccess(BankDetailsFormView.class)) {
            nav.addItem(new SideNavItem("Bank Details Form", BankDetailsFormView.class,
                    LineAwesomeIcon.CREDIT_CARD.create()));

        }
        if (accessChecker.hasAccess(StudentsStatusView.class)) {
            nav.addItem(
                    new SideNavItem("Students Status", StudentsStatusView.class, LineAwesomeIcon.TH_SOLID.create()));

        }
        if (accessChecker.hasAccess(ProfessorStatusView.class)) {
            nav.addItem(
                    new SideNavItem("Professor Status", ProfessorStatusView.class, LineAwesomeIcon.TH_SOLID.create()));

        }
        if (accessChecker.hasAccess(StudentMasterDetailsView.class)) {
            nav.addItem(new SideNavItem("Student Master Details", StudentMasterDetailsView.class,
                    LineAwesomeIcon.USERS_SOLID.create()));

        }
        if (accessChecker.hasAccess(AddressMasterDetailView.class)) {
            nav.addItem(new SideNavItem("Address Master Detail", AddressMasterDetailView.class,
                    LineAwesomeIcon.ADDRESS_CARD.create()));

        }
        if (accessChecker.hasAccess(CreateUsers.class)) {
            nav.addItem(new SideNavItem("Create Users", CreateUsers.class,
                    FontAwesome.Solid.USER_PLUS.create()));
        }
        if (accessChecker.hasAccess(PasswordChangeView.class)) {
            nav.addItem(new SideNavItem("Change Password", PasswordChangeView.class,
                    FontAwesome.Solid.USER_LOCK.create()));
        }
        if (accessChecker.hasAccess(ValueVaultView.class)) {
            nav.addItem(
                    new SideNavItem("Value Vault", ValueVaultView.class, FontAwesome.Solid.DATABASE.create()));

        }
        if (accessChecker.hasAccess(ActiveUsersView.class)) {
            nav.addItem(
                    new SideNavItem("Active Users", ActiveUsersView.class, FontAwesome.Solid.DATABASE.create()));

        }
        if (accessChecker.hasAccess(AboutView.class)) {
            nav.addItem(new SideNavItem("About", AboutView.class, LineAwesomeIcon.FILE.create()));
        }

        return nav;
    }

    private Footer createFooter() {
        Footer layout = new Footer();

        Optional<User> maybeUser = authenticatedUser.get();
        if (maybeUser.isPresent()) {
            User user = maybeUser.get();

            Avatar avatar = new Avatar(user.getUsername());

/*
            // Profile icon
            StreamResource resource = new StreamResource("profile-pic",
                    () -> new ByteArrayInputStream(user.getProfilePicture()));
            avatar.setImageResource(resource);
*/

            avatar.setThemeName("xsmall");
            avatar.getElement().setAttribute("tabindex", "-1");

            MenuBar userMenu = new MenuBar();
            userMenu.setThemeName("tertiary-inline contrast");

            MenuItem userName = userMenu.addItem("");
            Div div = new Div();
            div.add(avatar);
            div.add(user.getUsername() + " [ " + user.getRegisterNumber() + " ]");
            div.add(new Icon("lumo", "dropdown"));
            div.getElement().getStyle().set("display", "flex");
            div.getElement().getStyle().set("align-items", "center");
            div.getElement().getStyle().set("gap", "var(--lumo-space-s)");
            userName.add(div);
            userName.getSubMenu().addItem("log out", e -> {
                authenticatedUser.logout();
            });

            layout.add(userMenu);
        } else {
            Anchor loginLink = new Anchor("login", "Sign in");
            layout.add(loginLink);
        }

        return layout;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }
}
