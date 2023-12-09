package com.nasc.application.views.password;

import com.nasc.application.services.UserService;
import com.nasc.application.utils.NotificationUtils;
import com.nasc.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@PageTitle("Change Password")
@Route(value = "password-change", layout = MainLayout.class)
@PermitAll
public class PasswordChangeView extends VerticalLayout {

    private final TextField oldPassword;
    private final PasswordField newPassword;
    private final PasswordField confirmPassword;
    private final UserService userService;

    public PasswordChangeView(UserService userService) {
        oldPassword = new TextField("Old Password");
        newPassword = new PasswordField("New Password");
        confirmPassword = new PasswordField("Confirm Password");

        this.userService = userService;
        Button changeButton = new Button("Change Password");
        changeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        changeButton.addClickListener(event -> handleChangeButtonClick());

        FormLayout formLayout2Col = new FormLayout();
        formLayout2Col.add(oldPassword, newPassword, confirmPassword);
        HorizontalLayout layoutRow = new HorizontalLayout();
        layoutRow.add(changeButton);
        add(formLayout2Col, layoutRow);
    }

    private void handleChangeButtonClick() {
        try {
            String oldPasswordValue = oldPassword.getValue();
            String newPasswordValue = newPassword.getValue();
            String confirmPasswordValue = confirmPassword.getValue();
            userService.changePassword(oldPasswordValue, newPasswordValue, confirmPasswordValue);
            NotificationUtils.createSubmitSuccess("Password changed successfully");
        } catch (Exception e) {
            String message = e.getMessage();
            NotificationUtils.showErrorNotification(message);
        }
    }
}