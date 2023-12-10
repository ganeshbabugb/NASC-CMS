package com.nasc.application.data.model.enums;

import com.vaadin.flow.shared.util.SharedUtil;

import java.util.Locale;

public enum Semester {
    SEMESTER_1,
    SEMESTER_2,
    SEMESTER_3,
    SEMESTER_4,
    SEMESTER_5,
    SEMESTER_6;

    public String getDisplayName() {
        String capitalize = SharedUtil.capitalize(name().toLowerCase(Locale.ENGLISH));
        return capitalize.replace("_", " ");
    }

}