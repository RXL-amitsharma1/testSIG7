package com.rxlogix.enums

public enum DrugTypeEnum {

    SUSPECT('SUSPECT'),
    SUSPECT_AND_CONCOMITANT('SUSPECT_AND_CONCOMITANT'),
    VACCINE('VACCINE')
    /* lilydemo changes (commented code below) */
    //COMBINATION('COMBINATION'),
    //DEVICES('DEVICES')

    private final String val

    DrugTypeEnum(String val) {
        this.val = val
    }

    String value() { return val }

    public getI18nKey() {
        return "app.dateRangeType.${this.name()}"
    }

}
