package com.example.termproj_172.viewmodels;

import java.util.List;

public class DepartmentScheduleView {

    private final String departmentName;
    private final List<ProviderScheduleView> providers;

    public DepartmentScheduleView(String departmentName, List<ProviderScheduleView> providers) {
        this.departmentName = departmentName;
        this.providers = providers;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public List<ProviderScheduleView> getProviders() {
        return providers;
    }
}
