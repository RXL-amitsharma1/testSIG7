package com.rxlogix.enums

enum SignalChartsEnum {

    SOURCE(1),
    AGE(2),
    COUNTRY(3),
    GENDER(4),
    OUTCOME(5),
    SERIOUSNESS(6),
    ORGAN(7)

    Integer value

    SignalChartsEnum(value) {this.value = value}
}