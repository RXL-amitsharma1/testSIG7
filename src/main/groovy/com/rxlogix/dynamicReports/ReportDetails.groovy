package com.rxlogix.dynamicReports

class ReportDetails {

    String title
    String subTitle
    List data

    ReportDetails(String title, String subTitle, List data) {
        this.title = title
        this.subTitle = subTitle
        this.data = data
    }
}
