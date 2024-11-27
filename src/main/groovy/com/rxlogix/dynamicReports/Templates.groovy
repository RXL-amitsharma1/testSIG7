package com.rxlogix.dynamicReports

import com.rxlogix.enums.ReportThemeEnum
import grails.util.Holders
import net.sf.dynamicreports.report.base.expression.AbstractValueFormatter
import net.sf.dynamicreports.report.builder.ReportTemplateBuilder
import net.sf.dynamicreports.report.builder.component.ComponentBuilder
import net.sf.dynamicreports.report.builder.datatype.BigDecimalType
import net.sf.dynamicreports.report.builder.style.FontBuilder
import net.sf.dynamicreports.report.builder.style.PenBuilder
import net.sf.dynamicreports.report.builder.style.StyleBuilder
import net.sf.dynamicreports.report.builder.tableofcontents.TableOfContentsCustomizerBuilder
import net.sf.dynamicreports.report.constant.*
import net.sf.dynamicreports.report.definition.ReportParameters
import org.springframework.context.i18n.LocaleContextHolder

import java.awt.*

import static net.sf.dynamicreports.report.builder.DynamicReports.*

public class Templates {
    public static final StyleBuilder rootStyle
    public static final StyleBuilder printableRootStyle
    public static final StyleBuilder boldStyle
    public static final StyleBuilder printableBoldStyle
    public static final StyleBuilder italicStyle
    public static final StyleBuilder boldCenteredStyle
    public static final StyleBuilder bold14CenteredStyle
    public static final StyleBuilder criteriaSectionTitleStyle
    public static final StyleBuilder criteriaNameStyle
    public static final StyleBuilder criteriaValueStyle
    public static final StyleBuilder columnStyle
    public static final StyleBuilder columnStyleBold
    public static final StyleBuilder columnTitleStyle
    public static final StyleBuilder columnTitleStyleMemo
    public static final StyleBuilder columnStyleHTML
    public static final StyleBuilder columnTitleStyleHTML
    public static final StyleBuilder horizontalListColumnTitleStyle
    public static final StyleBuilder subtotalStyle
    public static final StyleBuilder criteriaNoteStyle

    public static final StyleBuilder pageHeaderStyleHTML
    public static final StyleBuilder pageHeader_HeaderStyle
    public static final StyleBuilder pageHeader_HeaderStyle_left
    public static final StyleBuilder pageHeader_TitleStyle
    public static final StyleBuilder pageHeader_DateRangeStyle
    public static final StyleBuilder pageFooterStyle
    public static final StyleBuilder groupStyle
    public static final StyleBuilder groupStyleHTML
    public static final StyleBuilder groupTitleStyle
    public static final StyleBuilder groupTitleStyleHTML
    public static final StyleBuilder groupHeaderStyle
    public static final StyleBuilder groupFooterStyle
    public static final StyleBuilder emptyPaddingStyle
    public static final StyleBuilder columnTitleStyleComments
    public static final StyleBuilder subReportPageHeaderStyle
    public static final StyleBuilder columnBorderLine
    public static final ReportTemplateBuilder reportCaseTemplate

    public static final StyleBuilder colspanStyle
    public static final StyleBuilder columnHeaderStyle
    public static subTotalBackgroundColor

    public static final ComponentBuilder<?, ?> pageNumberingComponent
    public static final ComponentBuilder<?, ?> pageNumberingInMidComponent

    public static final PenBuilder horizontalLine
    public static final ReportTemplateBuilder reportTemplate
    public static final CurrencyType currencyType
    public static final TableOfContentsCustomizerBuilder tableOfContentsCustomizer
    public static final ComponentBuilder<?, ?> pageNumberingCaseForm


    // Custom colors
    public static final grey = new Color(220, 220, 220)
    public static final darkGrey = new Color(180, 180, 180)
    public static final dark_blue = new Color(91,163,197) //(22,124,173)
    public static final light_blue = new Color(212,233,239)
    public static final orange = new Color(254, 207, 127)
    public static final subTotalOrange = new Color(242, 235, 220)
    public static final light_black = new Color(51, 51, 51)
    public static final blue = new Color(0, 113, 165)
    public static final white = new Color(255, 255, 255)

    private static Map<Locale, FontBuilder> defaultFonts = [:]

    static {
        defaultFonts[Locale.ENGLISH] = FontBuilder.newInstance().setFontName("Arial Unicode")
        defaultFonts[Locale.JAPANESE] = FontBuilder.newInstance().setFontName("IPAex Gothic")

        rootStyle = stl.style().setFontSize(14).setPadding(0)
        printableRootStyle = stl.style().setFontSize(9).setBottomPadding(5)

        boldStyle = stl.style(rootStyle).bold()
        printableBoldStyle = stl.style(printableRootStyle).bold()

        italicStyle = stl.style(rootStyle).italic()
        boldCenteredStyle = stl.style(boldStyle).setAlignment(HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE)
        bold14CenteredStyle = stl.style(boldCenteredStyle).setFontSize(14)

        criteriaNameStyle = stl.style(printableRootStyle).setBold(true).setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.TOP)
                .setPadding(4)
                .setLineSpacing(LineSpacing.SINGLE)
        criteriaSectionTitleStyle = stl.style(criteriaNameStyle).setBold(true)
                .setForegroundColor(light_black)
                .setFontSize(10)
        criteriaNoteStyle = stl.style(printableRootStyle).setBold(false).setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.TOP)
                .setPadding(4)
                .setLineSpacing(LineSpacing.SINGLE)
                .italic()

        columnStyle = stl.style(printableRootStyle).setVerticalAlignment(VerticalAlignment.TOP)
                .setBorder(stl.pen1Point().setLineWidth(0.5 as Float)
                        .setLineColor(grey))
                .setPadding(4)
                .setLineSpacing(LineSpacing.SINGLE)

        columnTitleStyle = stl.style(printableRootStyle).setBold(true)
                .setBorder(stl.pen1Point().setLineColor(blue))
                .setForegroundColor(white)
                .setBackgroundColor(blue)
                .setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.TOP)
                .setPadding(4)
                .setLineSpacing(LineSpacing.SINGLE)

        columnStyleBold = stl.style().setFontSize(8).setBold(true)
                .setBorder(stl.pen1Point().setLineWidth(0.5 as Float)
                        .setLineColor(grey))
                .setForegroundColor(white)
                .setBackgroundColor(blue)
                .setAlignment(HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE)

        columnTitleStyleMemo = stl.style(printableRootStyle).setBold(true)
                .setBorder(stl.pen1Point().setLineColor(blue))
                .setForegroundColor(white)
                .setBackgroundColor(blue)
                .setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.TOP)
                .setPadding(0)
                .setLineSpacing(LineSpacing.SINGLE)

        columnTitleStyleComments = stl.style(printableRootStyle).setBold(true)
                .setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.TOP)
                .setPadding(0)
                .setLineSpacing(LineSpacing.SINGLE)

        columnStyleHTML = stl.style(rootStyle)
                .setBorder(stl.pen1Point().setLineWidth(0.5 as Float)
                        .setLineColor(grey))
                .setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.TOP)

        columnTitleStyleHTML = stl.style(rootStyle).setBold(true)
                .setBorder(stl.pen1Point().setLineColor(blue))
                .setForegroundColor(white)
                .setLineSpacing(LineSpacing.SINGLE)
                .setBackgroundColor(blue)
                .setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.TOP)

        criteriaValueStyle = stl.style(printableRootStyle)
                .setName("criteriaValueStyle")
                .setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.TOP)
                .setPadding(1)
                .setLineSpacing(LineSpacing.SINGLE)
        criteriaNameStyle = stl.style(criteriaValueStyle)
                .setName("criteriaNameStyle")
                .setBold(true)
        horizontalListColumnTitleStyle = stl.style(columnTitleStyle).setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.TOP)

        columnHeaderStyle = stl.style()
                .setName("columnHeaderStyle").bold()

        groupStyle = stl.style(printableBoldStyle).setHorizontalAlignment(HorizontalAlignment.LEFT)
        groupStyleHTML = stl.style(boldStyle).setHorizontalAlignment(HorizontalAlignment.LEFT)
        groupTitleStyle = stl.style(printableBoldStyle)
        groupTitleStyleHTML = stl.style(boldStyle)
        groupHeaderStyle = stl.style().setTopPadding(5)
        groupFooterStyle = stl.style().setBottomPadding(0)
        emptyPaddingStyle = stl.style().setPadding(0)
        subtotalStyle = stl.style(boldStyle).setTopBorder(stl.pen1Point())

        pageHeaderStyleHTML = stl.style(boldStyle).setFontSize(16).setBottomPadding(5)
                .setAlignment(HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE)
        pageHeader_HeaderStyle = stl.style(boldStyle).setFontSize(10).setBottomPadding(5)
                .setAlignment(HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE)
        pageHeader_HeaderStyle_left = stl.style(boldStyle).setFontSize(10).setBottomPadding(5).setLeftPadding(0).setRightPadding(5)
                .setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.MIDDLE)
        pageHeader_TitleStyle = stl.style(boldStyle).setFontSize(10).setBottomPadding(5)
                .setAlignment(HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE)
        pageHeader_DateRangeStyle = stl.style(boldStyle).setFontSize(9).setBottomPadding(5)
                .setAlignment(HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE)

        pageFooterStyle = stl.style(printableRootStyle)

        subReportPageHeaderStyle = stl.style(pageHeader_HeaderStyle)
                .setName("subReportPageHeaderStyle")
                .setFontSize(9)
                .setHorizontalAlignment(HorizontalAlignment.LEFT)
                .setPadding(2)

        tableOfContentsCustomizer = tableOfContentsCustomizer().setHeadingStyle(0, stl.style(boldStyle))

        horizontalLine = stl.pen1Point().setLineColor(darkGrey).setLineWidth(0.5 as Float)

        colspanStyle = stl.style()
                .setHorizontalAlignment(HorizontalAlignment.LEFT)
                .setLeftPadding(60)

        pageNumberingComponent = cmp.pageXofY()                   
                .setHorizontalAlignment(HorizontalAlignment.LEFT)

        pageNumberingInMidComponent = cmp.pageXofY()
                .setHorizontalAlignment(HorizontalAlignment.CENTER)

        pageNumberingCaseForm = createPageNumberComponent()

        currencyType = new CurrencyType()

        reportTemplate=template()
        //todo:  this locale cannot be hardcoded
                .setLocale(Locale.ENGLISH)
                .setDefaultFont(defaultFont.setFontSize(11))
                .setGroupHeaderStyle(groupHeaderStyle)
                .setGroupFooterStyle(groupFooterStyle)
                .setSubtotalStyle(subtotalStyle)
                .crosstabHighlightEvenRows()
                .setTableOfContentsCustomizer(tableOfContentsCustomizer)
                .setDetailStyle(stl.style().setBorder(stl.pen1Point().setLineWidth(0.5 as Float).setLineColor(grey)))

        reportCaseTemplate = template()
                .setLocale(Locale.ENGLISH)
                .setDefaultFont(defaultFont.setFontSize(11))
                .setGroupHeaderStyle(groupHeaderStyle)
                .setGroupFooterStyle(groupFooterStyle)
                .setSubtotalStyle(subtotalStyle)
                .crosstabHighlightEvenRows()
                .setTableOfContentsCustomizer(tableOfContentsCustomizer)
    }

    private static createPageNumberComponent() {
        def currentPageNumberingStyle = stl.style(pageFooterStyle)
                .setName("currentPageNumberingStyle")
                .setAlignment(HorizontalAlignment.RIGHT, VerticalAlignment.MIDDLE)
                .setPadding(0)
        def currentPageComponent = cmp.text(exp.jasperSyntax("\"Page \" + \$V{PAGE_NUMBER}"))
                .setEvaluationTime(Evaluation.PAGE)
                .setStyle(currentPageNumberingStyle)
        def totalPagesNumberingStyle = stl.style(pageFooterStyle)
                .setName("totalPagesNumberingStyle")
                .setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.MIDDLE)
                .setPadding(0)
        def totalPagesComponent = cmp.text(exp.jasperSyntax("\" of \" + \$V{PAGE_NUMBER}"))
                .setEvaluationTime(Evaluation.REPORT)
                .setStyle(totalPagesNumberingStyle)
        return cmp.horizontalList(currentPageComponent, totalPagesComponent)
    }


    private static FontBuilder getDefaultFont() {
        return defaultFonts[LocaleContextHolder.getLocale()]?:defaultFonts[Locale.ENGLISH]
    }

    public static FontBuilder getDefaultFontStyle() {
        return getDefaultFont().setFontSize(9)
    }

    public static FontBuilder getDefaultFontStyleHTML() {
        return getDefaultFont().setFontSize(14)
    }

    public static CurrencyValueFormatter createCurrencyValueFormatter(String label) {
        return new CurrencyValueFormatter(label)
    }

    public static class CurrencyType extends BigDecimalType {
        private static final long serialVersionUID = 1L

        @Override
        public String getPattern() {
            return "\$ #,###.00"
        }
    }

    private static class CurrencyValueFormatter extends AbstractValueFormatter<String, Number> {
        private static final long serialVersionUID = 1L

        private String label

        public CurrencyValueFormatter(String label) {
            this.label = label
        }

        @Override
        public String format(Number value, ReportParameters reportParameters) {
            return label + currencyType.valueToString(value, reportParameters.getLocale())
        }
    }

    public static void applyTheme(String name) {
        ReportThemeEnum theme = ReportThemeEnum.searchByName(name)
        if (!theme) {
            applyDefaultTheme()
        } else {
            applyThemeInternal(theme)
        }
    }

    static void applyDefaultTheme() {
        applyThemeInternal(ReportThemeEnum.GRADIENT_BLUE)
    }

    private static void applyThemeInternal(ReportThemeEnum theme) {
        // Set background color
        columnTitleStyle.setBackgroundColor(theme.columnHeaderBackgroundColor)
        columnHeaderStyle.setBackgroundColor(theme.columnHeaderBackgroundColor)
        // Set foreground color
        columnTitleStyle.setForegroundColor(theme.columnHeaderForegroundColor)
        subTotalBackgroundColor = theme.subTotalBackgroundColor
    }
}
