package com.nasc.application.data.core.enums;

import com.nasc.application.utils.UIUtils;

public enum Semester {
    SEMESTER_1,
    SEMESTER_2,
    SEMESTER_3,
    SEMESTER_4,
    SEMESTER_5,
    SEMESTER_6;

    public String getDisplayName() {
        String capitalize = UIUtils.toCapitalize(name());
        return capitalize.replace("_", " ");
    }

}