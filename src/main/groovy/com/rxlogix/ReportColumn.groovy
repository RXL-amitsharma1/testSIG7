package com.rxlogix

public enum ReportColumn {

    SOC("SOC"),
    PREFERRED_TERM("Preferred Term"),
    LOWER_LEVEL_TERM("Lower Level Term"),
    PRODUCT("Product"),
    STUDY_ID("Study ID"),
    SOURCE("Source"),
    SERIOUSNESS("Seriousness"),
    LISTEDNESS("Listedness"),
    CAUSALITY("Causality"),
    HCP("HCP"),
    BATCH_LOT("Batch/Lot#"),
    SER("Ser"),
    LIST("List"),
    REL("Rel"),
    COUNTRY_OF_INCIDENT("Country of Incident"),
    CASE_SERIOUSNESS("Case Seriousness"),
    CASE_LISTEDNESS("Case Listedness"),
    CASE_CAUSALITY("Case Causality"),
    THERAPEUTIC_AREA("Therapeutic Area"),
    INGREDIENT("Ingredient"),
    GENDER("Gender"),
    AGE_GROUP("Age Group"),
    ETHNICITY("Ethnicity")

    String name

    ReportColumn(String name) {
        this.name = name
    }

}