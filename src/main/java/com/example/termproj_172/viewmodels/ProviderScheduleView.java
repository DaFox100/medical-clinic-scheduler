package com.example.termproj_172.viewmodels;

import java.util.List;

public class ProviderScheduleView {

    private final String providerName;
    private final List<ProviderScheduleRowView> rows;

    public ProviderScheduleView(String providerName, List<ProviderScheduleRowView> rows) {
        this.providerName = providerName;
        this.rows = rows;
    }

    public String getProviderName() {
        return providerName;
    }

    public List<ProviderScheduleRowView> getRows() {
        return rows;
    }
}
