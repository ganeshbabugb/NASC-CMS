package com.nasc.application.views.marks.entry;

import java.util.List;

// Define a simple Item class to represent a row in the grid
public class Item {
    private final String username;
    private final String registerNumber;
    private final List<String> rowData;

    public Item(String username, String registerNumber, List<String> rowData) {
        this.username = username;
        this.registerNumber = registerNumber;
        this.rowData = rowData;
    }

    public String getUsername() {
        return username;
    }

    public String getRegisterNumber() {
        return registerNumber;
    }

    public List<String> getRowData() {
        return rowData;
    }

    public String getMarksForSubject(String subjectName, List<String> distinctSubjectNames) {
        int index = distinctSubjectNames.indexOf(subjectName);
        return index >= 0 && index < rowData.size() ? rowData.get(index + 2) : "N/A"; // Skip first two columns (username and register number)
    }
}