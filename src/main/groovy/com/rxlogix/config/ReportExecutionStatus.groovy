package com.rxlogix.config

public enum ReportExecutionStatus {
    SCHEDULED("Scheduled"),
    GENERATING("Generating"),
    DELIVERING("Delivering"),
    COMPLETED("Completed"),
    ERROR("Error"),
    WARN("Warn")

    private final String val

    ReportExecutionStatus(String val) {
        this.val = val
    }

    String value() { return val }

    static ReportExecutionStatus getReportExecutionStatus(String value){
        values().find {it.toString() == value}
    }

    static List<ReportExecutionStatus> getExeuctionStatusList(){
        return [GENERATING, SCHEDULED , COMPLETED, ERROR]
    }

    public getI18nValueForExecutionStatusDropDown(){
        return "app.executionStatus.dropdown.${this.name()}"
    }

    String getKey(){
        name()
    }

    public getI18nKey() {
        return "app.executionStatus.${this.name()}"
    }
}
