package com.rxlogix.dynamicReports.charts.ooxml.data

import com.rxlogix.dynamicReports.charts.ooxml.ChartColorTheme
import com.rxlogix.dynamicReports.charts.ooxml.ChartDataSource
import com.rxlogix.dynamicReports.charts.ooxml.enums.TitleType
import grails.gorm.dirty.checking.DirtyCheck
import org.apache.poi.ss.util.CellReference;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTSerTx;

/**
 * Base of all Chart Series
 */
@DirtyCheck
public abstract class AbstractChartSeries implements ChartSeries {
    private int id
    private int order
    private ChartDataSource<?> categories
    private ChartDataSource<? extends Number> values
    private ChartColorTheme colorTheme
    private String titleValue;
    private CellReference titleRef;
    private TitleType titleType;

    public AbstractChartSeries (int id, int order,
                     ChartDataSource<?> categories,
                     ChartDataSource<? extends Number> values,
                     ChartColorTheme colorTheme) {
        this.id = id
        this.order = order
        this.categories = categories
        this.values = values
        this.colorTheme = colorTheme
    }

    public ChartDataSource<?> getCategoryAxisData() {
        return categories
    }

    public ChartDataSource<? extends Number> getValues() {
        return values
    }

    public void setTitle(CellReference titleReference) {
        titleType = TitleType.CELL_REFERENCE;
        titleRef = titleReference;
    }

    public void setTitle(String title) {
        titleType = TitleType.STRING;
        titleValue = title;
    }

    public CellReference getTitleCellReference() {
        if (TitleType.CELL_REFERENCE.equals(titleType)) {
            return titleRef;
        }
        throw new IllegalStateException("Title type is not CellReference.");
    }

    public String getTitleString() {
        if (TitleType.STRING.equals(titleType)) {
            return titleValue;
        }
        throw new IllegalStateException("Title type is not String.");
    }

    public TitleType getTitleType() {
        return titleType;
    }

    protected boolean isTitleSet() {
        return titleType != null;
    }

    protected CTSerTx getCTSerTx() {
        CTSerTx tx = CTSerTx.Factory.newInstance();
        switch (titleType) {
            case TitleType.CELL_REFERENCE:
                tx.addNewStrRef().setF(titleRef.formatAsString());
                return tx;
            case TitleType.STRING:
                tx.setV(titleValue);
                return tx;
            default:
                throw new IllegalStateException("Unkown title type: " + titleType);
        }
    }

    int getId() {
        return id
    }

    int getOrder() {
        return order
    }

    ChartDataSource<?> getCategories() {
        return categories
    }

    ChartColorTheme getColorTheme() {
        return colorTheme
    }
}
