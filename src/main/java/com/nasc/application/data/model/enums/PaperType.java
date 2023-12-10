package com.nasc.application.data.model.enums;

import com.vaadin.flow.shared.util.SharedUtil;

import java.util.Locale;

public enum PaperType {
    CORE,
    ELECTIVE,
    SKILL;

    public String getDisplayName() {
        return SharedUtil.capitalize(name().toLowerCase(Locale.ENGLISH));
    }
}
