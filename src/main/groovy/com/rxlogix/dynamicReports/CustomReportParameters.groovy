package com.rxlogix.dynamicReports

import java.awt.Image

import static net.sf.dynamicreports.report.builder.DynamicReports.exp

/**
 * Created by gologuzov on 06.03.17.
 */
enum CustomReportParameters {
    HEADER_TEXT(String.class),
    PRINT_HEADER_TEXT(Boolean.class, true),

    HEADER_IMAGE(Image.class),
    PRINT_HEADER_IMAGE(Boolean.class, true),

    REPORT_TITLE(String.class),
    PRINT_REPORT_TITLE(Boolean.class, true),

    RUN_DATE(String.class),
    PRINT_RUN_DATE(Boolean.class, true),

    EXTRA_DETAILS(String.class),
    PRINT_EXTRA_DETAILS(Boolean.class, true),

    IS_CRITERIA_SHEET_OR_APPENDIX(Boolean.class, false),


    PAGE_FOOTER_TEXT(String.class),
    PRINT_PAGE_FOOTER_TEXT(Boolean.class, false),

    PRINT_PAGE_FOOTER_PAGE_NUM(Boolean.class, false),

    PAGE_FOOTER_IMAGE(Image.class),
    PRINT_PAGE_FOOTER_IMAGE(Boolean.class, false),

    // parameters from the params map
    showCompanyLogo(Boolean.class, true),
    advancedOptions(String.class),
    outputFormat(String.class);

    private static final String CUSTOM_PREFIX = "PV_"

    String jasperName
    Class jasperType
    Object defaultValue

    private CustomReportParameters(Class jasperType, Object defaultValue = null) {
        this.jasperName = CUSTOM_PREFIX + name()
        this.jasperType = jasperType
        this.defaultValue = defaultValue
    }

    def getJasperValue() {
        return exp.jasperSyntax("\$P{${jasperName}}")
    }
}