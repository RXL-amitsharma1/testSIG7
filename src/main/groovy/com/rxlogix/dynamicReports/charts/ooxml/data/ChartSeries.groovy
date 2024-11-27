package com.rxlogix.dynamicReports.charts.ooxml.data

import com.rxlogix.dynamicReports.charts.ooxml.enums.TitleType
import org.apache.poi.ss.util.CellReference;

/**
 * Basic settings for all chart series.
 */
public interface ChartSeries {

    /**
     * Sets the title of the series as a string literal.
     *
     * @param title
     */
    void setTitle(String title);

    /**
     * Sets the title of the series as a cell reference.
     *
     * @param titleReference
     */
    void setTitle(CellReference titleReference);

    /**
     * @return title as string literal.
     */
    String getTitleString();

    /**
     * @return title as cell reference.
     */
    CellReference getTitleCellReference();

    /**
     * @return title type.
     */
    TitleType getTitleType();
}
