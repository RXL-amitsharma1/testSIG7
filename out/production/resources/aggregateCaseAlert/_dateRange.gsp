<%@ page import="com.rxlogix.util.DateUtil; com.rxlogix.enums.DateRangeEnum; com.rxlogix.util.ViewHelper" %>
<div class="row dateRange">
    <div class="col-xs-12">
        <label><g:message code="app.label.DateRange"/></label>
        <div class="row">
            <div class="col-xs-12">
                <g:select
                          name="templateQueries[${i}].dateRangeInformationForTemplateQuery.dateRangeEnum"
                          from="${ViewHelper.getDateRange()}"
                          optionValue="display"
                          optionKey="name"
                          value="${templateQueryInstance?.dateRangeInformationForTemplateQuery?.dateRangeEnum?:DateRangeEnum.CUMULATIVE}"
                          class="form-control dateRangeEnumClass"/>
            </div>
        </div>

    </div>
    <div class="col-xs-12">
        <g:textField class="top-buffer form-control relativeDateRangeValue" name="templateQueries[${i}].dateRangeInformationForTemplateQuery.relativeDateRangeValue"
               placeholder="${message(code: 'enter.x.here')}"
               style="display: none; width: 50%;"
               value="${templateQueryInstance?.dateRangeInformationForTemplateQuery?.relativeDateRangeValue ?: 1}"/>
        <div class="notValidNumberErrorMessage" hidden="hidden" style="color: #ff0000"> Enter Valid Number </div>

        <div class="fuelux datePickerParentDiv">
            <div class="datepickerForTemplateQuery text datepicker" id="templateQueries[${i}].datePickerFromDiv" style="display:none">
                <g:message code="app.dateFilter.from"/>
                <div class="input-group">
                    <g:hiddenField name="templateQueries[${i}].dateRangeInformationForTemplateQuery.dateRangeStartAbsolute"
                                 value="${templateQueryInstance?.dateRangeInformationForTemplateQuery?.dateRangeStartAbsolute}"
                                 placeholder="${message(code: 'select.start.date')}"
                                 class="form-control"/>
                    <input placeholder="${message(code: 'scheduler.startDate')}" name="dateRangeStart[${i}]"
                       class="form-control" id="dateRangeStart[${i}]" type="text"/>
                    <g:render template="/includes/widgets/datePickerTemplate"/>
                </div>
            </div>

            <div class="datepickerForTemplateQuery toolbarInline datepicker" id="templateQueries[${i}].datePickerToDiv" style="display:none">
                <g:message code="app.dateFilter.to" />
                <div class="input-group">
                <g:hiddenField name="templateQueries[${i}].dateRangeInformationForTemplateQuery.dateRangeEndAbsolute"
                             value="${templateQueryInstance?.dateRangeInformationForTemplateQuery?.dateRangeEndAbsolute}"
                             class="form-control"/>
                <input placeholder="End Date" name="dateRangeEnd[${i}]"
                             class="form-control" id="dateRangeEnd[${i}]" type="text"/>
                <g:render template="/includes/widgets/datePickerTemplate" />
                </div>
            </div>

        </div>
    </div>
</div>


