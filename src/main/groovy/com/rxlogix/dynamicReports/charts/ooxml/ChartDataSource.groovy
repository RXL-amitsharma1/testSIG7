package com.rxlogix.dynamicReports.charts.ooxml;

/**
 * Represents data model of the charts.
 *
 * @param <T> type of points the data source contents
 */
public interface ChartDataSource<T> {

    /**
     * Return number of points contained by data source.
     *
     * @return number of points contained by data source
     */
    int getPointCount();

    /**
     * Returns point value at specified index.
     *
     * @param index index to value from
     * @return point value at specified index.
     * @throws {@code IndexOutOfBoundsException} if index
     *                parameter not in range {@code 0 <= index <= pointCount}
     */
    T getPointAt(int index);

    /**
     * Returns {@code true} if charts data source is valid cell range.
     *
     * @return {@code true} if charts data source is valid cell range
     */
    boolean isReference();

    /**
     * Returns {@code true} if data source points should be treated as numbers.
     *
     * @return {@code true} if data source points should be treated as numbers
     */
    boolean isNumeric();

    /**
     * Returns true of false if datasource has formula representation. It is only applicable
     * for data source that is valid cell range.
     *
     * @return true of false if datasource has formula representation
     * @throws {@code UnsupportedOperationException} if the data source is not a
     *                reference.
     */
    boolean hasFormulaString();

    /**
     * Returns formula representation of the data source. It is only applicable
     * for data source that is valid cell range.
     *
     * @return formula representation of the data source
     * @throws {@code UnsupportedOperationException} if the data source is not a
     *                reference.
     */
    String getFormulaString();

    /**
     * Returns {@code true} if data source points have multi levels.
     *
     * @return {@code true} if data source points have multi levels
     */
    boolean isMultiLevel();
}
