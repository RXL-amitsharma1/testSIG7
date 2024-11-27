package com.rxlogix.enums

enum MontoringStatusEnum {

    OVERDUE("Overdue"),
    SUBMITTED("Submitted"),
    ONGOING("Ongoing"),
    DUETODAY("Due Today")

    private final String val

    MontoringStatusEnum(String val) {
        this.val = val
    }

    String value() { return val }

    public getI18nKey() {
        return "app.monitoring.status.${this.name()}"
    }

}