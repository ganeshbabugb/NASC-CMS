package com.nasc.application.data.core.enums;

import com.nasc.application.utils.UIUtils;

public enum MajorOfPaper {
    PART_1,
    PART_2,
    PART_3,
    PART_4;

    public String getDisplayName() {
        String capitalize = UIUtils.toCapitalize(name());
        return capitalize.replace("_", " ");
    }
}
