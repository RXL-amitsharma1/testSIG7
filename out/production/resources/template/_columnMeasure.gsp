<%@ page import="com.rxlogix.enums.TemplateTypeEnum; com.rxlogix.util.ViewHelper" %>
<g:set var="templateService" bean="templateService"/>

<div class="row columnMeasureSet" sequence="${index}" id="colMeas${index}_template" style="border: dashed 1px #D4D4D4; border-radius: 5px; padding: 10px; width: 270px; margin-right: 30px; float: left;">
    <i class="fa fa-times add-cursor removeColumnMeasure" style="float: right;"></i>
    <div class="col-xs-12" style="margin-top: 0px;">
        <div class="row">
            <div class="col-xs-12">
                <label><g:message code="app.label.columns" /></label>
            </div>
        </div>
        <div class="row columnsContainer rowsAndColumnsContainer rowsAndColumnsContainerBorder">
            <div class="col-xs-12 no-padding columnScroll">
                <div hidden="hidden"><g:render template="toAddColumn"></g:render></div>
                <div id="columnsContainer${index}" class="containerToBeSelected columnsContainer"></div>
            </div>
        </div>
        <g:hiddenField name="columns${index}" value="${templateService.getJSONStringRF(columnMeasure?.columnList)}"/>

        <div class="row" style="margin-top: 5px; margin-bottom: 3px;">
            <div class="col-xs-4">
                <label><g:message code="app.label.measures" /></label>
            </div>
            <div class="col-xs-8" style="margin-top: -5px;">
                <g:select class="form-control selectMeasure" noSelection="['':message(code:'select.measure')]" name="selectMeasure${index}"
                          from="${ViewHelper.getDataTabulationMeasures()}" optionKey="name" optionValue="display"/>
            </div>
        </div>
        <div class="row measuresContainerBorder">
            <div class="col-xs-12 no-padding columnScroll">
                <div class="measuresContainer"></div>
            </div>
        </div>
        <g:hiddenField class="validMeasureIndex" name="colMeas${index}-validMeasureIndex" />
        <div class="row">
            <div class="col-xs-12">
                <label class="no-bold add-cursor">
                    <g:checkBox class="showTotalIntervalCases" name="showTotalIntervalCases"
                                value="${columnMeasure?.showTotalIntervalCases}" />
                    <g:message code="show.total.interval.cases" />
                </label>
                <label class="no-bold add-cursor">
                    <g:checkBox class="showTotalCumulativeCases" name="showTotalCumulativeCases"
                                value="${columnMeasure?.showTotalCumulativeCases}" />
                    <g:message code="show.total.cumulative.cases"/>
                </label>
            </div>
        </div>
    </div>
</div>