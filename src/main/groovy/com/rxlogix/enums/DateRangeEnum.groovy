package com.rxlogix.enums

import com.rxlogix.config.DateRangeValue

public enum DateRangeEnum {

    YESTERDAY('lastXDays', DateRangeValue.RELATIVE),
    LAST_WEEK('lastXWeeks', DateRangeValue.RELATIVE),
    LAST_MONTH('lastXMonths',DateRangeValue.RELATIVE),
    LAST_YEAR('lastXYears',DateRangeValue.RELATIVE),
    PR_DATE_RANGE('PRDateRange',null),
    PR_DATE_RANGE_SAFETYDB('PRDateRangeSafetyDB',null),
    PR_DATE_RANGE_FAERS('PRDateRangeFaers',null),
    PR_DATE_RANGE_VIGIBASE('PRDateRangeVigibase',null),
    PR_DATE_RANGE_VAERS('PRDateRangeVaers',null),
    LAST_X_DAYS('lastXDays', DateRangeValue.RELATIVE),
    LAST_X_WEEKS('lastXWeeks' ,DateRangeValue.RELATIVE),
    LAST_X_MONTHS('lastXMonths',DateRangeValue.RELATIVE),
    LAST_X_YEARS('lastXYears',DateRangeValue.RELATIVE),
    CUMULATIVE('Cumulative',DateRangeValue.CUMULATIVE),
    CUMULATIVE_SAFETYDB('CumulativeSafetyDB',DateRangeValue.CUMULATIVE),
    CUMULATIVE_FAERS('CumulativeFaers',DateRangeValue.CUMULATIVE),
    CUMULATIVE_VIGIBASE('CumulativeVigibase',DateRangeValue.CUMULATIVE),
    CUMULATIVE_VAERS('CumulativeVaers',DateRangeValue.CUMULATIVE),
    CUSTOM('Custom',DateRangeValue.CUSTOM)

    final String val
    final DateRangeValue dateRangeType

    DateRangeEnum(String val,DateRangeValue dateRangeType) {
        this.val = val
        this.dateRangeType = dateRangeType
    }

    String value() { return val }


    public static DateRangeEnum[] getDateOperators() {
        return [YESTERDAY, LAST_WEEK, LAST_MONTH, LAST_YEAR, LAST_X_DAYS, LAST_X_WEEKS, LAST_X_MONTHS, LAST_X_YEARS,CUMULATIVE,CUSTOM]
    }

    public static DateRangeEnum[] getDateRange() {
        return [YESTERDAY, LAST_WEEK, LAST_MONTH, LAST_YEAR, LAST_X_DAYS, LAST_X_WEEKS, LAST_X_MONTHS, LAST_X_YEARS,CUMULATIVE,CUSTOM]
    }

    public static DateRangeEnum[] getDateOperatorsLiterature() {
        return [YESTERDAY, LAST_WEEK, LAST_MONTH, LAST_YEAR, LAST_X_DAYS, LAST_X_WEEKS, LAST_X_MONTHS, LAST_X_YEARS, CUSTOM]
    }

    public static DateRangeEnum[] getRelativeDateOperatorsWithX() {
        return [LAST_X_DAYS, LAST_X_WEEKS, LAST_X_MONTHS, LAST_X_YEARS]
    }

    public static DateRangeEnum[] getEudraDateOperators() {
        return [CUSTOM, CUMULATIVE]
    }

    public static DateRangeEnum[] getQuantitativeDateOperators() {
        return [CUSTOM]
    }

    public static List<DateRangeEnum> getPeriodicReportTemplateDateRangeOptions() {
        return [PR_DATE_RANGE, CUMULATIVE]
    }

    public static List<DateRangeEnum> getNewPeriodicReportTemplateDateRangeOptions() {
        //return [PR_DATE_RANGE_SAFETYDB, CUMULATIVE_SAFETYDB, PR_DATE_RANGE_FAERS, CUMULATIVE_FAERS, PR_DATE_RANGE_VAERS, CUMULATIVE_VAERS, PR_DATE_RANGE_VIGIBASE, CUMULATIVE_VIGIBASE]// Commented Vigibase spotfire drop down value due to descope of vigibase spotfire.
        return [PR_DATE_RANGE_SAFETYDB, CUMULATIVE_SAFETYDB, PR_DATE_RANGE_FAERS, CUMULATIVE_FAERS, PR_DATE_RANGE_VAERS, CUMULATIVE_VAERS]
    }

    public static List<DateRangeEnum> getNewPeriodicReportTemplateDateRangeOptionsForPVA() {
        return [PR_DATE_RANGE_SAFETYDB, CUMULATIVE_SAFETYDB]
    }

    public getI18nKey() {
        return "app.queryOperator.${this.name()}"
    }

}
