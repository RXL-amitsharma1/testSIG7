package com.rxlogix.dynamicReports;

import java.awt.Color;

import ar.com.fdvs.dj.domain.Style;
import ar.com.fdvs.dj.domain.builders.ColumnBuilder;
import ar.com.fdvs.dj.domain.constants.Border;
import ar.com.fdvs.dj.domain.constants.Font;
import ar.com.fdvs.dj.domain.constants.HorizontalAlign;
import ar.com.fdvs.dj.domain.constants.Transparency;
import ar.com.fdvs.dj.domain.constants.VerticalAlign;
import ar.com.fdvs.dj.domain.entities.columns.AbstractColumn;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

public abstract class ReportBase {

    protected AbstractColumn modelCol;
    protected AbstractColumn mfgCol;
    protected AbstractColumn qtyCol;
    protected AbstractColumn yearCol;

    public ReportBase() {
        modelCol = ColumnBuilder.getNew()
                .setColumnProperty("model", String.class.getName())
                .setTitle("Model").setWidth(70).build();
        mfgCol = ColumnBuilder.getNew()
                .setColumnProperty("manufacturer", String.class.getName())
                .setTitle("Manufacturer").setWidth(70).build();
        qtyCol = ColumnBuilder.getNew()
                .setColumnProperty("quantity", Integer.class.getName())
                .setTitle("Quantity").setWidth(70).build();
        yearCol = ColumnBuilder.getNew()
                .setColumnProperty("year", String.class.getName())
                .setTitle("Year").setWidth(70).build();
    }

    public Style getHeaderStyle() {
        Style headerStyle = new Style();
        headerStyle.setFont(Font.VERDANA_MEDIUM_BOLD);
        headerStyle.setBorderBottom(Border.PEN_2_POINT());
        headerStyle.setHorizontalAlign(HorizontalAlign.CENTER);
        headerStyle.setVerticalAlign(VerticalAlign.MIDDLE);
        headerStyle.setBackgroundColor(Color.DARK_GRAY);
        headerStyle.setTextColor(Color.WHITE);
        headerStyle.setTransparency(Transparency.OPAQUE);
        return headerStyle;
    }

    protected JRDataSource getDataSource() {
//        JRDataSource dataSource = new JRBeanCollectionDataSource();
        JRDataSource dataSource = null;
        return dataSource;

    }

}