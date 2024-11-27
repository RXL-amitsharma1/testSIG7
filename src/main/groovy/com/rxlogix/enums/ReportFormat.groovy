package com.rxlogix.enums

enum ReportFormat {

    HTML("html"),
    PDF("pdf"),
    XLSX("xlsx"),
    DOCX("docx")


    final String key

    ReportFormat(String key){
        this.key = key
    }

    public getI18nKey() {
        return "app.reportFormat.${this.name()}"
    }


}