package com.rxlogix.enums
import grails.util.Holders


enum DataSourceEnum {

    PVA(Holders.getConfig().signal.dataSource.safety.name),
    EUDRA('EVDAS'),
    FAERS('FAERS'),
    VAERS('VAERS'),
    VIGIBASE('VigiBase'),
    JADER('JADER')

    final String val

    DataSourceEnum(String val){
        this.val = val
    }

    String value() { return val }

    public getI18nKey() {
        return "app.label.datasource.${this.name()}"
    }

}