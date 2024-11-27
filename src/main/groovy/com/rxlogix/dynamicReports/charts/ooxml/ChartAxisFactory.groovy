package com.rxlogix.dynamicReports.charts.ooxml;

import com.rxlogix.dynamicReports.charts.ooxml.enums.AxisPosition;

/**
 * A factory for different chart axis.
 */
public interface ChartAxisFactory {

	/**
	 * @return new value axis
	 */
	ValueAxis createValueAxis(AxisPosition pos);

	/**
	 * @return new category axis.
	 */
	ChartAxis createCategoryAxis(AxisPosition pos);

}
