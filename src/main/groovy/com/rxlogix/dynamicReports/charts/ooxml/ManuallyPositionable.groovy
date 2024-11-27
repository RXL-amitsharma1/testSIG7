package com.rxlogix.dynamicReports.charts.ooxml;

/**
 * Abstraction of chart element that can be positioned with manual
 * layout.
 */
public interface ManuallyPositionable {

	/**
	 * Returns manual layout for the chart element.
	 * @return manual layout for the chart element.
	 */
	public ManualLayout getManualLayout();
}
