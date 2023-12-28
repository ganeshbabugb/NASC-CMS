package com.nasc.application.data.core.enums;

import com.nasc.application.utils.UIUtils;

public enum PaperType {
    CORE,
    ELECTIVE,
    SKILL;

    public String getDisplayName() {
        return UIUtils.toCapitalize(name());
    }
}
