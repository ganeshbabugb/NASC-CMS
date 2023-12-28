package com.nasc.application.data.core.enums;

import com.nasc.application.utils.UIUtils;

public enum Role {
    ADMIN, STUDENT, PROFESSOR, HOD, EDITOR;

    public String getDisplayName() {
        return UIUtils.toCapitalize(name());
    }
}
