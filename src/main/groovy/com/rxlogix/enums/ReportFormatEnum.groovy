package com.rxlogix.enums

enum ReportFormatEnum {

    HTML("html", "HTML"),
    PDF("pdf", "PDF"),
    XLSX("xlsx", "Excel"),
    DOCX("docx", "Word"),
    PPTX("pptx", "PowerPoint")


    final String key
    final String displayName

    ReportFormatEnum(String key, String displayName){
        this.key = key
        this.displayName = displayName
    }

    public getI18nKey() {
        return "app.reportFormat.${this.name()}"
    }

    static List<ReportFormatEnum> getEmailShareOptions() {
        return values().findAll { it != HTML }
    }
}