package com.nasc.application.data.core.enums;

import com.nasc.application.utils.UIUtils;

public enum StudentSection {
    SECTION_A,
    SECTION_B,
    SECTION_C,
    SECTION_D,
    SECTION_E,
    SECTION_F,
    SECTION_G,
    SECTION_H;

    public String getDisplayName() {
        String enumName = name();
        int underscoreIndex = enumName.indexOf('_');

        if (underscoreIndex > 0) {
            String substring = enumName.substring(0, underscoreIndex);
            String capitalize = UIUtils.toCapitalize(substring);
            char nextChar = enumName.charAt(underscoreIndex + 1);
            String string = Character.toString(nextChar).toUpperCase();

            return capitalize + " " + string;
        } else {
            return enumName;
        }
    }
}
