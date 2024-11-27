<%@ page import="com.rxlogix.util.DateUtil; com.rxlogix.enums.DateRangeEnum; com.rxlogix.util.ViewHelper" %>
<div class="row m-b-10">
    <div class="col-xs-4">
        <label><g:message code="app.label.DateRange"/></label>

        <div id="dateRangeEnumProductFreqFalse">
            <g:select
                    name="dateRangeEnum"
                    from="${ViewHelper.getDateRangeLiterature()}"
                    optionValue="display"
                    optionKey="name"
                    value="${configurationInstance?.dateRangeInformation?.dateRangeEnum ?: DateRangeEnum.LAST_WEEK}"
                    class="form-control dateRangeEnumClass"/>
        </div>
        <input name="dateRangeEnum" id="dateRangeEnum" type="text" hidden="hidden"
               value="${configurationInstance?.dateRangeInformation?.dateRangeEnum}"/>

    </div>

    <div class="col-xs-8">
        <g:textField class="top-buffer form-control relativeDateRangeValue"
                     name="relativeDateRangeValue"
                     placeholder="${message(code: 'enter.x.here')}"
                     value="${configurationInstance?.dateRangeInformation?.relativeDateRangeValue ?: 1}"
                     style="width: 100%; margin-top:23px; display: none;"/>
        <div class="notValidNumberErrorMessage" hidden="hidden" style="color: #ff0000">Enter Valid Number</div>

        <input name="startDateAbsoluteCustomFreq" id="startDateAbsoluteCustomFreq" type="text" hidden="hidden"
               value="${startDateAbsoluteCustomFreq}"/>
        <input name="endDateAbsoluteCustomFreq" id="endDateAbsoluteCustomFreq" type="text" hidden="hidden"
               value="${endDateAbsoluteCustomFreq}"/>

        <div class=" row fuelux datePickerParentDiv">
            <div class="datepickerForTemplateQuery text datepicker fromDateChanged"
                 id="datePickerFromDiv" style="display:none">

                <div class="col-xs-6">
                    <g:hiddenField name="dateRangeStartAbsolute"
                                   value="${dateMap?.startDate}"
                                   placeholder="${message(code: 'select.start.date')}"
                                   class="form-control"/>
                    <div id="dateRangeStartProductFreqTrue">
                        <label><g:message code="app.dateFilter.from"/></label>
                        <select class="form-control" name="dateRangeStart" id="dateRangeStartProductFreq"
                                autocomplete="off">
                            <option value="null">--Select One--</option>
                        </select>
                    </div>

                    <div id="dateRangeStartProductFreqFalse">
                        <label class="box-100"><g:message code="app.dateFilter.from"/></label>

                        <div class="input-group">
                            <input placeholder="${message(code: 'scheduler.startDate')}" name="dateRangeStart"
                                   class="form-control" id="dateRangeStart" type="text"
                                   value="${configurationInstance?.dateRangeInformation?.dateRangeStartAbsolute}"/>
                            <g:render template="/includes/widgets/datePickerTemplate"/>
                        </div>
                    </div>
                </div>
            </div>

            <div class="datepickerForTemplateQuery text datepicker toDateChanged"
                 id="datePickerToDiv" style="display:none">

                <div class="col-xs-6">
                    <g:hiddenField name="dateRangeEndAbsolute"
                                   value="${dateMap?.endDate}"
                                   class="form-control"/>
                    <div id="dateRangeEndProductFreqTrue">
                        <label><g:message code="app.dateFilter.to"/></label>
                        <select class="form-control" name="dateRangeEnd" id="dateRangeEndProductFreq"
                                autocomplete="off">
                            <option value="null">--Select One--</option>
                        </select>
                    </div>

                    <div id="dateRangeEndProductFreqFalse">
                        <label><g:message code="app.dateFilter.to"/></label>

                        <div class="input-group">
                            <input placeholder="${message(code: 'select.end.date')}" name="dateRangeEnd"
                                   class="form-control" id="dateRangeEnd" type="text"
                                   value="${configurationInstance?.dateRangeInformation?.dateRangeEndAbsolute}"/>
                            <g:render template="/includes/widgets/datePickerTemplate"/>
                        </div>
                    </div>
                </div>
            </div>

        </div>
    </div>
</div>

