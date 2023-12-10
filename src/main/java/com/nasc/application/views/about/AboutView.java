package com.nasc.application.views.about;

import com.nasc.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;

@PageTitle("About")
@Route(value = "about", layout = MainLayout.class)
@AnonymousAllowed
public class AboutView extends VerticalLayout {

    public AboutView() {
        setSpacing(false);

        Image img = new Image("images/NASC_LOGO.jpg", "NASC LOGO");
        img.setWidth("200px");
        add(img);

        H2 header = new H2("\"Automate the Future\"");
        header.addClassNames(Margin.Top.XLARGE, Margin.Bottom.MEDIUM);
        add(header);
        add(new Paragraph("NASC PORTAL WAS DESIGNED AND DEVELOPED BY DEPARTMENT OF COMPUTER SCIENCE & COMPUTER APPLICATION"));

        Button installButton = new Button("Download / Install NASC PWA");
        installButton.addClickListener(e -> {
            getUI().ifPresent(ui -> ui.getPage().executeJs("window.location.href = 'http://localhost:8080';"));
        });
        add(installButton);

        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        getStyle().set("text-align", "center");
    }

}