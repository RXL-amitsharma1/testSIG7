<%@ page import="com.rxlogix.reportTemplate.PercentageOptionEnum; com.rxlogix.util.ViewHelper" %>

<div class="row measureOptions" style="padding-top: 20px;" hidden="hidden" id="colMeas${colMeasIndex}-meas${measIndex}">
    <div class="col-xs-12" style="padding-left: 30px; padding-right: 30px;">
        <div class="row measureOptionsBorder">
            <i class="fa fa-times add-cursor closeMeasureOptions" style="float: right;"></i>
            <g:hiddenField class="measureType" name="colMeas${colMeasIndex}-meas${measIndex}-type" value="${measure?.type}" />
            <div class="row">
                <div class="col-xs-2">
                    <label><g:message code="app.label.name" /></label>
                    <input class="form-control inputMeasureName" name="colMeas${colMeasIndex}-meas${measIndex}-name" value="${measure?.name}">
                </div>
                <div class="col-xs-3">
                    <label><g:message code="app.label.countType" /></label>
                    <g:select class="form-control" name="colMeas${colMeasIndex}-meas${measIndex}-dateRangeCount"
                              from="${ViewHelper.getDataTabulationCounts()}" value="${measure?.dateRangeCount}"
                              optionKey="name" optionValue="display"/>
                </div>
                <div class="col-xs-2">
                    <div class="fuelux customPeriodDatePickers" style="padding: 5px;" hidden="hidden">
                        <g:hiddenField class="form-control customPeriodFrom" hidden="hidden" type="text"
                               name="colMeas${colMeasIndex}-meas${measIndex}-customPeriodFrom"
                               value="${measure?.getCustomPeriodFromWithTZ()}"/>

                        <div class="datepicker" id="colMeas${colMeasIndex}-meas${measIndex}-datePickerFrom">
                            <g:message code="app.dateFilter.from"/>
                            <div class="input-group">
                                <input placeholder="${message(code: 'select.start.date')}" class="form-control" type="text"/>
                                <g:render template="/includes/widgets/datePickerTemplate"/>
                            </div>
                        </div>

                        <g:hiddenField class="form-control customPeriodTo" hidden="hidden" type="text"
                               name="colMeas${colMeasIndex}-meas${measIndex}-customPeriodTo"
                               value="${measure?.getCustomPeriodToWithTZ()}" />

                        <div class="datepicker" id="colMeas${colMeasIndex}-meas${measIndex}-datePickerTo">
                            <g:message code="app.dateFilter.to"/>
                            <div class="input-group">
                                <input placeholder="Select End Date" class="form-control" type="text"/>
                                <g:render template="/includes/widgets/datePickerTemplate"/>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="col-xs-3 percentageOption">
                    <g:radioGroup name="colMeas${colMeasIndex}-meas${measIndex}-percentageOption" value="${measure?.percentageOption?:PercentageOptionEnum.NO_PERCENTAGE}"
                                  values="${ViewHelper.getDataTabulationPercentageOptions()}"
                                  labels="${ViewHelper.getDataTabulationPercentageOptions()}">
                        <label class="no-bold add-cursor">${it.radio} <g:message code="app.percentageOptionEnum.${it.label}" /></label>
                    </g:radioGroup>
                </div>
                <div class="col-xs-2">
                    <div>
                        <label class="no-bold add-cursor">
                            <g:checkBox name="colMeas${colMeasIndex}-meas${measIndex}-showTotal" value="${measure?.showTotal}"/> <g:message code="show.total"/>
                        </label>
                    </div>

                    <a class="add-cursor showCustomExpression_measure"><g:message code="app.template.customExpression" /></a>
                </div>
            </div>

            <div class="row customExpressionArea" hidden="hidden">
                <div class="col-xs-12">
                    <textarea class="form-control customExpressionValue_measure" name="colMeas${colMeasIndex}-meas${measIndex}-customExpression">
                        ${measure?.customExpression}
                    </textarea>
                </div>
            </div>
        </div>
    </div>
</div>