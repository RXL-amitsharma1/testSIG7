package com.rxlogix.config

public enum AlertType {
    SINGLE_CASE_ALERT("Single Case Alert"),
    AGGREGATE_CASE_ALERT("Aggregate Case Alert"),
    EVDAS_ALERT("EVDAS Alert"),
    LITERATURE_SEARCH_ALERT("Literature Search Alert")
    private final String val

    AlertType(String val) {
        this.val = val
    }

    String value() { return val }


    static List<AlertType> getAlertTypeList(){
        return [SINGLE_CASE_ALERT, AGGREGATE_CASE_ALERT , EVDAS_ALERT, LITERATURE_SEARCH_ALERT]
    }

    public getI18nValueForExecutionStatusDropDown(){
        return "app.alert.type.dropdown.${this.name()}"
    }

    String getKey(){
        name()
    }

    public getI18nKey() {
        return "app.executionStatus.${this.name()}"
    }
}
