package com.example.termproj_172.viewmodels;

import java.util.List;

public class ProviderScheduleRowView {

    private final String timeLabel;
    private final List<ScheduleCellView> cells;

    public ProviderScheduleRowView(String timeLabel, List<ScheduleCellView> cells) {
        this.timeLabel = timeLabel;
        this.cells = cells;
    }

    public String getTimeLabel() {
        return timeLabel;
    }

    public List<ScheduleCellView> getCells() {
        return cells;
    }
}
