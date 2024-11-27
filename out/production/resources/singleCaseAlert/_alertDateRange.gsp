<%@ page import="com.rxlogix.util.DateUtil; com.rxlogix.enums.DateRangeEnum; com.rxlogix.util.ViewHelper" %>
<div class="row m-b-10 alertDateRange" id="alertDateRange">
    <div class="col-xs-6">
        <label><g:message code="app.label.DateRange"/></label>
            <g:select
                    name="alertDateRangeInformation.dateRangeEnum"
                    from="${ViewHelper.getDateRange()}"
                    optionValue="display"
                    optionKey="name"
                    value="${configurationInstance?.alertDateRangeInformation?.dateRangeEnum ?: DateRangeEnum.CUMULATIVE}"
                    class="form-control dateRangeEnumClass"/>

        <input name="dateRangeEnum" id="dateRangeEnum" type="text" hidden="hidden"
               value="${configurationInstance?.alertDateRangeInformation?.dateRangeEnum ?: DateRangeEnum.CUMULATIVE}"/>

    </div>

    <div class="col-xs-6">
        <g:textField class="top-buffer form-control relativeDateRangeValue"
                     name="alertDateRangeInformation.relativeDateRangeValue"
                     placeholder="${message(code: 'enter.x.here')}"
                     style="display: none; width: 50%; margin-top:23px;"
                     value="${configurationInstance?.alertDateRangeInformation?.relativeDateRangeValue ?: 1}"/>
        <div class="notValidNumberErrorMessage" hidden="hidden" style="color: #ff0000">Enter Valid Number</div>


        <div class=" row fuelux datePickerParentDiv">
            <div class="datepickerForTemplateQuery text datepicker fromDateChanged"
                 id="datePickerFromDiv" style="display:none">

                <div class=" col-xs-6 datepicker">
                    <g:hiddenField
                            name="alertDateRangeInformation.dateRangeStartAbsolute"
                            value="${configurationInstance?.alertDateRangeInformation?.dateRangeStartAbsolute?DateUtil.StringFromDate(configurationInstance?.alertDateRangeInformation?.dateRangeStartAbsolute,"dd-MMM-yyyy","UTC"):''}"
                            placeholder="${message(code: 'select.start.date')}"
                            class="form-control"/>

                        <label class="box-100"><g:message code="app.dateFilter.from"/></label>

                        <div class="input-group">
                            <input placeholder="${message(code: 'scheduler.startDate')}" name="dateRangeStart"
                                   class="form-control" id="dateRangeStart" type="text"/>
                            <g:render template="/includes/widgets/datePickerTemplate"/>
                        </div>
                </div>
            </div>

            <div class="datepickerForTemplateQuery text datepicker toDateChanged"
                 id="datePickerToDiv" style="display:none">

                <div class="col-xs-6">
                    <g:hiddenField
                            name="alertDateRangeInformation.dateRangeEndAbsolute"
                            value="${configurationInstance?.alertDateRangeInformation?.dateRangeEndAbsolute?DateUtil.StringFromDate(configurationInstance?.alertDateRangeInformation?.dateRangeEndAbsolute,"dd-MMM-yyyy","UTC"):''}"
                            class="form-control"/>

                        <label><g:message code="app.dateFilter.to"/></label>

                        <div class="input-group">
                            <input placeholder="${message(code: 'select.end.date')}" name="dateRangeEnd"
                                   class="form-control" id="dateRangeEnd" type="text"/>
                            <g:render template="/includes/widgets/datePickerTemplate"/>
                        </div>
                </div>
            </div>
        </div>
    </div>

    <div  class="col-xs-12" id="setFaersDateRange"></div>
    <div  class="col-xs-12" id="setVaersDateRange"></div>
    <div  class="col-xs-12" id="setVigibaseDateRange"></div>
    <div  class="col-xs-12" id="setEvdasDateRange"></div>
    <div  class="col-xs-12" id="faersLatestQuarter"></div>
    <div  class="col-xs-12" id="vigibaseLatestQuarter"></div>
    <div  class="col-xs-12" id="jaderLatestQuarter"></div>
</div>