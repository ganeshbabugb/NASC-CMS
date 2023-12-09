package com.nasc.application.utils;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class NotificationUtils {
    // Duration
    private static final int SUCCESS_NOTIFICATION_DURATION = 2000;
    private static final int INFO_NOTIFICATION_DURATION = 3000;
    private static final int ERROR_NOTIFICATION_DURATION = 4000;
    private static final int WARNING_NOTIFICATION_DURATION = 5000;

    // Position
    private static final Notification.Position SUCCESS_NOTIFICATION_POSITION = Notification.Position.BOTTOM_END;
    private static final Notification.Position ERROR_NOTIFICATION_POSITION = Notification.Position.TOP_END;
    private static final Notification.Position INFO_NOTIFICATION_POSITION = Notification.Position.TOP_CENTER;
    private static final Notification.Position WARNING_NOTIFICATION_POSITION = Notification.Position.BOTTOM_CENTER;

    public static void showSuccessNotification(String message) {
        showNotification(message, SUCCESS_NOTIFICATION_DURATION, SUCCESS_NOTIFICATION_POSITION);
    }

    public static void showInfoNotification(String message) {
        showNotification(message, INFO_NOTIFICATION_DURATION, INFO_NOTIFICATION_POSITION);
    }

    public static void showErrorNotification(String message) {
        showNotification(message, ERROR_NOTIFICATION_DURATION, ERROR_NOTIFICATION_POSITION);
    }

    public static void showWarningNotification(String message) {
        showNotification(message, WARNING_NOTIFICATION_DURATION, WARNING_NOTIFICATION_POSITION);
    }

    public static void showNotification(String message, int duration, Notification.Position position) {
        Notification notification = new Notification(message, duration, position);
        notification.open();
    }

    public static void createSubmitSuccess(String message) {
        Notification notification = new Notification();
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);


        Icon icon = VaadinIcon.CHECK_CIRCLE.create();

        HorizontalLayout layout = new HorizontalLayout(icon, new Text(message));
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        notification.add(layout);
        notification.setDuration(SUCCESS_NOTIFICATION_DURATION);
        notification.setPosition(SUCCESS_NOTIFICATION_POSITION);
        notification.open();
    }

    public static void createReportError(String message) {
        Notification notification = new Notification();
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);

        notification.setDuration(ERROR_NOTIFICATION_DURATION);
        notification.setPosition(ERROR_NOTIFICATION_POSITION);

        Icon icon = VaadinIcon.WARNING.create();

        HorizontalLayout layout = new HorizontalLayout(icon, new Text(message));
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        notification.add(layout);
        notification.open();
    }

    public static void createUploadSuccess(String message, String fileName) {
        Notification notification = new Notification();

        Icon icon = VaadinIcon.CHECK_CIRCLE.create();
        icon.setColor("var(--lumo-success-color)");

        Div uploadSuccessful = new Div(new Text(message));
        uploadSuccessful.getStyle()
                .set("font-weight", "600")
                .setColor("var(--lumo-success-text-color)");

        Span fn = new Span(fileName);
        fn.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("font-weight", "600");

        var layout = new HorizontalLayout(icon, uploadSuccessful, new Text(" "), fn);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        notification.add(layout);

        notification.setDuration(SUCCESS_NOTIFICATION_DURATION);
        notification.setPosition(SUCCESS_NOTIFICATION_POSITION);
        notification.open();
    }
}
