package com.rxlogix.dto

class TestCaseDTO {
    private String alertType;
    private String assignedTo;
    private String owner;
    private String products;
    private String priority;
    private String dateRangeType;
    private String dateRange;
    private String xForDateRange;
    private String startDate;
    private String endDate;
    private String versionAsOfDate;
    private String shareWith;
    private String scheduler;
    private String dataSource;
    private Boolean isAdhoc;
    private Boolean isExcludeFollowUp;
    private Boolean isDataMiningSMQ;
    private Boolean isExcludeNonValidCases;
    private Boolean isIncludeMissingCases;
    private Boolean isApplyAlertStopList;
    private Boolean isIncludeMedicallyConfirmedCases;
    private String drugType;
    private String evaluateCaseDateOn;
    private String limitCaseSeries;

    Boolean getIsAdhoc() {
        return isAdhoc
    }
    void setIsAdhoc(Boolean isAdhoc) {
        this.isAdhoc = isAdhoc
    }

    void setAlertType(String alertType) {
        this.alertType = alertType
    }

    void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo
    }

    void setOwner(String owner) {
        this.owner = owner
    }

    void setProducts(String products) {
        this.products = products
    }

    void setPriority(String priority) {
        this.priority = priority
    }

    void setDateRangeType(String dateRangeType) {
        this.dateRangeType = dateRangeType
    }

    void setxForDateRange(String xForDateRange) {
        this.xForDateRange = xForDateRange
    }

    void setStartDate(String startDate) {
        this.startDate = startDate
    }

    void setEndDate(String endDate) {
        this.endDate = endDate
    }

    void setVersionAsOfDate(String versionAsOfDate) {
        this.versionAsOfDate = versionAsOfDate
    }

    void setShareWith(String shareWith) {
        this.shareWith = shareWith
    }

    void setScheduler(String scheduler) {
        this.scheduler = scheduler
    }

    void setIsExcludeFollowUp(Boolean isExcludeFollowUp) {
        this.isExcludeFollowUp = isExcludeFollowUp
    }

    void setIsDataMiningSMQ(Boolean isDataMiningSMQ) {
        this.isDataMiningSMQ = isDataMiningSMQ
    }

    void setIsExcludeNonValidCases(Boolean isExcludeNonValidCases) {
        this.isExcludeNonValidCases = isExcludeNonValidCases
    }

    void setIsIncludeMissingCases(Boolean isIncludeMissingCases) {
        this.isIncludeMissingCases = isIncludeMissingCases
    }

    void setIsApplyAlertStopList(Boolean isApplyAlertStopList) {
        this.isApplyAlertStopList = isApplyAlertStopList
    }

    void setIsIncludeMedicallyConfirmedCases(Boolean isIncludeMedicallyConfirmedCases) {
        this.isIncludeMedicallyConfirmedCases = isIncludeMedicallyConfirmedCases
    }

    void setDateRange(String dateRange) {
        this.dateRange = dateRange
    }

    void setDrugType(String drugType) {
        this.drugType = drugType
    }

    void setEvaluateCaseDateOn(String evaluateCaseDateOn) {
        this.evaluateCaseDateOn = evaluateCaseDateOn
    }

    void setLimitCaseSeries(String limitCaseSeries) {
        this.limitCaseSeries = limitCaseSeries
    }

    String getLimitCaseSeries() {
        return limitCaseSeries
    }

    String getAlertType() {
        return alertType
    }

    String getAssignedTo() {
        return assignedTo
    }

    String getOwner() {
        return owner
    }

    String getProducts() {
        return products
    }

    String getPriority() {
        return priority
    }

    String getDateRangeType() {
        return dateRangeType
    }

    String getxForDateRange() {
        return xForDateRange
    }

    String getStartDate() {
        return startDate
    }

    String getEndDate() {
        return endDate
    }

    String getVersionAsOfDate() {
        return versionAsOfDate
    }

    String getShareWith() {
        return shareWith
    }

    String getScheduler() {
        return scheduler
    }
    void setDataSource(String dataSource) {
        this.dataSource = dataSource
    }

    String getDataSource() {
        return dataSource
    }

    Boolean getIsExcludeFollowUp() {
        return isExcludeFollowUp
    }

    Boolean getIsDataMiningSMQ() {
        return isDataMiningSMQ
    }

    Boolean getIsExcludeNonValidCases() {
        return isExcludeNonValidCases
    }

    Boolean getIsIncludeMissingCases() {
        return isIncludeMissingCases
    }

    Boolean getIsApplyAlertStopList() {
        return isApplyAlertStopList
    }

    Boolean getIsIncludeMedicallyConfirmedCases() {
        return isIncludeMedicallyConfirmedCases
    }

    String getDateRange() {
        return dateRange
    }

    String getDrugType() {
        return drugType
    }

    String getEvaluateCaseDateOn() {
        return evaluateCaseDateOn
    }
}
