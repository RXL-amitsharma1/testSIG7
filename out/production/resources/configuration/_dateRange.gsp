<%@ page import="com.rxlogix.util.DateUtil; com.rxlogix.enums.DateRangeEnum; com.rxlogix.util.ViewHelper" %>
<div class="row dateRange">
    <div class="col-xs-12">
        <label><g:message code="app.label.DateRange"/></label>
        <div class="row">
            <div class="col-xs-12">
                <g:select name="templateQueries[${i}].dateRangeInformationForTemplateQuery.dateRangeEnum"
                          from="${ViewHelper.getDateRangeReportSection()}"
                          optionValue="display"
                          optionKey="name"
                          value="${templateQueryInstance?.dateRangeInformationForTemplateQuery?.dateRangeEnum}"
                          noSelection="['' :'--Select One--']"
                          class="form-control dateRangeEnumClass"/>
            </div>
        </div>

    </div>
    <div class="col-xs-12">
        <g:textField class="top-buffer form-control relativeDateRangeValue" name="templateQueries[${i}].dateRangeInformationForTemplateQuery.relativeDateRangeValue"
                     placeholder="${message(code: 'enter.x.here')}"
                     style="display: none; width: 50%;"
                     value="${templateQueryInstance?.dateRangeInformationForTemplateQuery?.relativeDateRangeValue ?: 1}"/>
        <div class="notValidNumberErrorMessage" hidden="hidden" style="color: #ff0000"> <g:message code="app.query.value.invalid.number" /> </div>

        <div class="fuelux datePickerParentDiv">
            <div class="datepicker" id="templateQueries[${i}].datePickerFromDiv" style="display:none">
                <div style="margin-top: 10px;">
                    <g:message code="app.dateFilter.from"/>
                </div>
                <div class="input-group">
                    <g:textField name="templateQueries[${i}].dateRangeInformationForTemplateQuery.dateRangeStartAbsolute"
                                 value="${renderShortFormattedDate(date: templateQueryInstance?.dateRangeInformationForTemplateQuery?.dateRangeStartAbsolute)}"
                                 placeholder="${message(code: 'app.label.startDate')}"
                                 class="form-control"/>
                    <g:render template="/includes/widgets/datePickerTemplate"/>
                </div>
            </div>

            <div class="datepicker" id="templateQueries[${i}].datePickerToDiv" style="display:none">
                <div style="margin-top: 10px;">
                    <g:message code="app.dateFilter.to" />
                </div>
                <div class="input-group">
                    <g:textField name="templateQueries[${i}].dateRangeInformationForTemplateQuery.dateRangeEndAbsolute"
                                 value="${renderShortFormattedDate(date: templateQueryInstance?.dateRangeInformationForTemplateQuery?.dateRangeEndAbsolute)}"
                                 placeholder="${message(code: 'app.label.endDate')}"
                                 class="form-control"/>
                    <g:render template="/includes/widgets/datePickerTemplate"/>
                </div>
            </div>

        </div>
    </div>
</div>


