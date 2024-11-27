package com.rxlogix.enums

public enum TemplateTypeEnum {
    CASE_LINE ("Case Line Listing"),
    DATA_TAB ("Data Tabulation"),
    CUSTOM_SQL ("Custom SQL"),
    NON_CASE ("Non Case"),
    TEMPLATE_SET("Template Set")

    final String value

    TemplateTypeEnum (String value) {
        this.value = value
    }

    //Used to get to values for dropdown lists
    String toString(){
        value
    }

    //Used to get to key for dropdown lists
    String getKey() {
        name()
    }

    //Used to match up a value from the DB against the enum
    static String getValue(String theValue) {
        for (TemplateTypeEnum theEnum : values()){
            String name = theEnum.name();
            if (theValue == name) {
                return (theEnum.value)
            }
        }
        return ""
    }

    public getI18nKey() {
        return "app.templateType.${this.name()}"
    }
}

