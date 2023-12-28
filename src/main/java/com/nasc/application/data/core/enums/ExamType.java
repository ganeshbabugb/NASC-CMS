package com.nasc.application.data.core.enums;

import com.nasc.application.utils.UIUtils;

public enum ExamType {
    INTERNAL_1,
    INTERNAL_2,
    MODEL,
    SEMESTER;

    public String getDisplayName() {
        String capitalize = UIUtils.toCapitalize(name());
        return capitalize.contains("_") ? capitalize.replace("_", " ") : capitalize;
    }
}
