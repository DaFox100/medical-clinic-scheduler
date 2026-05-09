package com.example.termproj_172.viewmodels;

import java.util.List;

public class HomeScheduleView {

    private final List<String> dateHeaders;
    private final List<DepartmentScheduleView> departments;

    public HomeScheduleView(List<String> dateHeaders, List<DepartmentScheduleView> departments) {
        this.dateHeaders = dateHeaders;
        this.departments = departments;
    }

    public List<String> getDateHeaders() {
        return dateHeaders;
    }

    public List<DepartmentScheduleView> getDepartments() {
        return departments;
    }
}
