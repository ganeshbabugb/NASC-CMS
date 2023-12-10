package com.nasc.application.data.model.enums;

import com.vaadin.flow.shared.util.SharedUtil;

import java.util.Locale;

public enum Role {
    ADMIN, STUDENT, PROFESSOR, HOD, EDITOR;

    public String getDisplayName() {
        return SharedUtil.capitalize(name().toLowerCase(Locale.ENGLISH));
    }
}
