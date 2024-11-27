package com.rxlogix.enums


public enum ReportFieldSelectionTypeEnum {

    /*
        This enum allows us to pass in the type of ReportField selection we're performing, using a single method
        vs. having 4 essentially-duplicate methods to achieve the same thing.  That duplicate code is a point of
        brittleness and bugs. This could be further refactored later, but right now, those 4 duplicate
        methods needs to be centralized. Three methods in TemplateService and one in QueryService
        will now be converted to a single getReportFields() method in ReportFieldService.  Moving it
        to ReportFieldService also places it where it belongs in terms of responsibility
        (i.e. the operation in question is not a Query or Template operation, but a ReportField operation). -morett

     */

    CLL("Case Line Listing"),
    DT_ROW("Data Tabulation Row"),
    DT_COLUMN("Data Tabulation Column"),
    QUERY("Record Modified")


    final String value

    ReportFieldSelectionTypeEnum(String value){
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
        for (ReportFieldSelectionTypeEnum theEnum : values()){
            String name = theEnum.name();
            if (theValue == name) {
                return (theEnum.value)
            }
        }
        return ""
    }

}
