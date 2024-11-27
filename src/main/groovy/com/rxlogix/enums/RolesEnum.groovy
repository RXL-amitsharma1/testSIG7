package com.rxlogix.enums

public enum RolesEnum {

    AD_HOC_Evaluator( "ROLE_AD_HOC_CRUD"),
    Administrator( "ROLE_ADMIN"),
    Aggregate_Alert_Configuration("ROLE_AGGREGATE_CASE_CONFIGURATION"),
    Aggregate_Alert_Reviewer("ROLE_AGGREGATE_CASE_REVIEWER"),
    Aggregate_Alert_Viewer("ROLE_AGGREGATE_CASE_VIEWER"),
    Alerts_Operational_Metrics("ROLE_PRODUCTIVITY_AND_COMPLIANCE") ,
    Category_and_Subcategory_management("ROLE_CATEGORY_SUBCATEGORY_MANAGEMENT"),
    Configuration_Editor("ROLE_CONFIGURATION_CRUD"),
    Configuration_Viewer( "ROLE_CONFIGURATION_VIEW"),
    Configure_Template_Alert("ROLE_CONFIGURE_TEMPLATE_ALERT"),
    Data_Analysis("ROLE_DATA_ANALYSIS"),
    Data_Mining_Runs("ROLE_DATA_MINING"),
    Dev_User("ROLE_DEV"),
    EVDAS_Alert_Configuration("ROLE_EVDAS_CASE_CONFIGURATION"),
    EVDAS_Alert_Reviewer("ROLE_EVDAS_CASE_REVIEWER"),
    EVDAS_Alert_Viewer("ROLE_EVDAS_CASE_VIEWER"),
    Execute_shared_alerts("ROLE_EXECUTE_SHARED_ALERTS"),
    FAERS_Alert_Configuration("ROLE_FAERS_CONFIGURATION"),
    Individual_Case_Review_Configuration("ROLE_SINGLE_CASE_CONFIGURATION"),
    Individual_Case_Reviewer("ROLE_SINGLE_CASE_REVIEWER"),
    Individual_Case_Viewer("ROLE_SINGLE_CASE_VIEWER"),
    Literature_Alert_Configuration("ROLE_LITERATURE_CASE_CONFIGURATION"),
    Literature_Alert_Reviewer("ROLE_LITERATURE_CASE_REVIEWER"),
    Literature_Alert_Viewer("ROLE_LITERATURE_CASE_VIEWER"),
    Manage_Product_Assignments("ROLE_MANAGE_PRODUCT_ASSIGNMENTS"),
    Query_Editor("ROLE_QUERY_CRUD"),
    Query_Editor_Advanced("ROLE_QUERY_ADVANCED"),
    Reporting("ROLE_REPORTING"),
    Share_with_all_users("ROLE_SHARE_ALL"),
    Share_with_user_group("ROLE_SHARE_GROUP"),
    Show_Signal_Dashboard("ROLE_SIGNAL_DASHBOARD"),
    Signal_Management_Creation("ROLE_SIGNAL_MANAGEMENT_CONFIGURATION"),
    Signal_Management_Reviewer("ROLE_SIGNAL_MANAGEMENT_REVIEWER"),
    Signal_Management_Viewer("ROLE_SIGNAL_MANAGEMENT_VIEWER"),
    Signal_Operational_Metrics("ROLE_OPERATIONAL_METRICS"),
    View_All("ROLE_VIEW_ALL"),
    Evdas_Auto_Download("ROLE_EVDAS_AUTO_DOWNLOAD"),
    Data_Mining_Advanced_Runs("ROLE_DATA_MINING_ADVANCED_RUNS")

    final String value

    RolesEnum(value) {
        this.value = value
    }

    String value() { return value }

}
