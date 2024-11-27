package com.rxlogix.config

public enum AlertProgressExecutionType {
    DATASOURCE("DATASOURCE"),
    EBGM("EBGM"),
    ROR("ROR"),
    PRR("PRR"),
    DSS("DSS"),
    ABCD("ABCD"),
    BR("BR"),
    PERSIST("PERSIST"),
    ARCHIEVE("ARCHIEVE")

    private final String val

    AlertProgressExecutionType(String val) {
        this.val = val
    }

    String value() { return val }

    static AlertProgressExecutionType getAlertProgressExecutionType(String value){
        values().find {it.toString() == value}
    }

    static List<AlertProgressExecutionType> getAlertProgressExeuctionTypeList(){
        return [DATASOURCE, EBGM, ROR, PRR, BR, PERSIST, ARCHIEVE]
    }

    String getKey(){
        name()
    }
}
