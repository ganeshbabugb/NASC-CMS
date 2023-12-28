package com.nasc.application.utils;

import com.vaadin.flow.shared.util.SharedUtil;

import java.time.format.DateTimeFormatter;

public class UIUtils {
    public static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public static String toCapitalize(String string) {
        return SharedUtil.capitalize(string.toLowerCase());
    }
}
