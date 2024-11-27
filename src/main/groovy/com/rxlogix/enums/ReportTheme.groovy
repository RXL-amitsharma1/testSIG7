package com.rxlogix.enums

import java.awt.Color

enum ReportThemeEnum {
    GRADIENT_BLUE("gradient_blue", "#0071a5", "#ffffff", "#dcebf2"),
    SOLID_BLUE("solid_blue", "#0071a5", "#ffffff", "#dcebf2"),
    SOLID_GOLDEN_GREY("solid_golden_grey", "#eea320", "#333333", "#f2eadc"),
    SOLID_ORANGE("solid_orange", "#eea320", "#333333", "#f2eadc");

    String name
    Color columnHeaderBackgroundColor
    Color columnHeaderForegroundColor
    Color subTotalBackgroundColor

    private ReportThemeEnum(String name, String columnHeaderBackgroundColor, String columnHeaderForegroundColor, String subTotalBackgroundColor) {
        this.name = name
        this.columnHeaderBackgroundColor = Color.decode(columnHeaderBackgroundColor)
        this.columnHeaderForegroundColor = Color.decode(columnHeaderForegroundColor)
        this.subTotalBackgroundColor = Color.decode(subTotalBackgroundColor)
    }

    public static ReportThemeEnum searchByName(String name) {
        values().find { it.name == name }
    }
}
