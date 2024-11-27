package com.rxlogix.enums

enum StrategyTypeEnum {

    RMP('RMP'),
    REMS('REMP'),
    ACTIVE_SURVEILLANCE('Active Surveillance'),
    OTHERS('Others')

    final String val

    StrategyTypeEnum(String val){
        this.val = val
    }

    String value() { return val }

    public getI18nKey() {
        return "app.strategy.type.${this.name()}"
    }
}