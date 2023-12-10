package com.nasc.application.data.model.enums;

import com.vaadin.flow.shared.util.SharedUtil;

import java.util.Locale;

public enum ExamType {
    INTERNAL_1,
    INTERNAL_2,
    MODEL,
    SEMESTER;

    public String getDisplayName() {
        String capitalize = SharedUtil.capitalize(name().toLowerCase(Locale.ENGLISH));
        return capitalize.contains("_") ? capitalize.replace("_", " ") : capitalize;

    }
}
