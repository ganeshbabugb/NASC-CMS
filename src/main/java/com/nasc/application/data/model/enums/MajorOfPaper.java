package com.nasc.application.data.model.enums;

import com.vaadin.flow.shared.util.SharedUtil;

import java.util.Locale;

public enum MajorOfPaper {
    PART_1,
    PART_2,
    PART_3,
    PART_4;

    public String getDisplayName() {
        String capitalize = SharedUtil.capitalize(name().toLowerCase(Locale.ENGLISH));
        return capitalize.replace("_", " ");
    }
}
