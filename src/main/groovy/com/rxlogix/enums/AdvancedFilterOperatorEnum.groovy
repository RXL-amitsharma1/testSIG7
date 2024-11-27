package com.rxlogix.enums

public enum AdvancedFilterOperatorEnum {
    EQUALS('between'), NOT_EQUAL('NOT_EQUAL'), LESS_THAN('lt'), LESS_THAN_OR_EQUAL('le'), GREATER_THAN('gt'), GREATER_THAN_OR_EQUAL('ge'),
    //The following are for String operations, and their values are not used
            CONTAINS('CONTAINS'), DOES_NOT_CONTAIN('DOES_NOT_CONTAIN'), START_WITH('START_WITH'), DOES_NOT_START('DOES_NOT_START'), ENDS_WITH('ENDS_WITH'), DOES_NOT_END('DOES_NOT_END'),
    YESTERDAY('lastXDays'), LAST_WEEK('lastXWeeks'), LAST_MONTH('lastXMonths'), LAST_YEAR('lastXYears'),
    LAST_X_DAYS('lastXDays'), LAST_X_WEEKS('lastXWeeks'), LAST_X_MONTHS('lastXMonths'), LAST_X_YEARS('lastXYears'),
    IS_EMPTY('isNull'), IS_NOT_EMPTY('isNotNull')

    private final String val

    AdvancedFilterOperatorEnum(String val) {
        this.val = val
    }

    String value() { return val }

    public static AdvancedFilterOperatorEnum[] getNumericOperators() {
        return [EQUALS, LESS_THAN, LESS_THAN_OR_EQUAL, GREATER_THAN, GREATER_THAN_OR_EQUAL]
    }

    public static AdvancedFilterOperatorEnum[] getStringOperators() {
        return [EQUALS, NOT_EQUAL, DOES_NOT_CONTAIN, CONTAINS, START_WITH, DOES_NOT_START, ENDS_WITH, DOES_NOT_END, IS_EMPTY, IS_NOT_EMPTY]
    }

    public static AdvancedFilterOperatorEnum[] getDateOperators() {
        return (getNumericOperators() + [YESTERDAY, LAST_WEEK, LAST_MONTH, LAST_YEAR, LAST_X_DAYS, LAST_X_WEEKS, LAST_X_MONTHS, LAST_X_YEARS])
    }

    public static AdvancedFilterOperatorEnum[] getValuelessOperators() {
        return [YESTERDAY, LAST_WEEK, LAST_MONTH, LAST_YEAR, IS_EMPTY, IS_NOT_EMPTY]
    }

    public static AdvancedFilterOperatorEnum[] getNumericValueDateOperators() {
        return ([LAST_X_DAYS, LAST_X_WEEKS, LAST_X_MONTHS, LAST_X_YEARS])
    }
    public static AdvancedFilterOperatorEnum[] getEmptyOperators() {
        return ([ IS_EMPTY, IS_NOT_EMPTY])
    }

    public getI18nKey() {
        return "app.queryOperator.${this.name()}"
    }
}