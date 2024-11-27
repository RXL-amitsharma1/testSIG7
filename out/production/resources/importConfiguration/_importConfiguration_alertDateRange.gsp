<%@ page import="com.rxlogix.util.DateUtil; com.rxlogix.enums.DateRangeEnum; com.rxlogix.util.ViewHelper" %>
<div class="row m-b-10 alertDateRange" id="alertDateRange">
    <div class="row" style="margin:10px" >
        <div class="col-xs-12">
            <label><g:message code="app.label.DateRange"/></label>
            <g:select
                    name="alertDateRangeInformation.dateRangeEnum"
                    from="${ViewHelper.getDateRange()}"
                    optionValue="display"
                    optionKey="name"
                    value=""
                    class="form-control dateRangeEnumClass"/>

            <input name="dateRangeEnum" data-alertId="null" id="dateRangeEnum" type="text" hidden="hidden"
                   value=""/>

        </div>
    </div>
   <div class="row" style="margin:10px" >
           <g:textField class="top-buffer form-control relativeDateRangeValue"
                        name="alertDateRangeInformation.relativeDateRangeValue"
                        placeholder="${message(code: 'enter.x.here')}"
                        style="display: none; width: 100%; margin-top:23px;"
                        value="1"/>


           <div class=" row fuelux datePickerParentDiv">
               <div class="datepickerForTemplateQuery text datepicker fromDateChanged"
                    id="datePickerFromDiv" style="display:none">

                   <div class=" col-xs-6">
                       <g:hiddenField
                               name="alertDateRangeInformation.dateRangeStartAbsolute"
                               value="${configurationInstance?.alertDateRangeInformation?.dateRangeStartAbsolute?DateUtil.StringFromDate(configurationInstance?.alertDateRangeInformation?.dateRangeStartAbsolute,"dd-MMM-yyyy","UTC"):''}"
                               placeholder="${message(code: 'select.start.date')}"
                               class="form-control"/>

                       <label class="box-100"><g:message code="app.dateFilter.from"/></label>

                       <div class="input-group">
                           <input placeholder="${message(code: 'scheduler.startDate')}" name="dateRangeStart"
                                  class="form-control" id="dateRangeStart" type="text" />
                           <g:render template="/includes/widgets/datePickerTemplate"/>
                       </div>
                   </div>
               </div>

               <div class="datepickerForTemplateQuery text datepicker toDateChanged"
                    id="datePickerToDiv" style="display:none">

                   <div class="col-xs-6">
                       <g:hiddenField
                               name="alertDateRangeInformation.dateRangeEndAbsolute"
                               value=""
                               class="form-control"/>

                       <label><g:message code="app.dateFilter.to"/></label>

                       <div class="input-group">
                           <input placeholder="${message(code: 'select.end.date')}" name="dateRangeEnd"
                                  class="form-control" id="dateRangeEnd" type="text" />
                           <g:render template="/includes/widgets/datePickerTemplate"/>
                       </div>
                   </div>
               </div>
           </div>

   </div>
    <div class="row" style="margin:10px">
        <label><g:message code="app.label.EvaluateCaseDateOn"/></label>
        <div id="evaluateDateAsDiv">
            <g:select name="evaluateDateAsNonSubmission"
                      from="${ViewHelper.getEvaluateCaseDateI18n()}"
                      optionValue="display" optionKey="name"
                      value=""
                      class="form-control evaluateDateAs"/>
        </div>
        <input name="evaluateDateAs" id="evaluateDateAs" type="text" hidden="hidden"
               value=""/>

        <div style="margin-top: 10px">
            <div class="fuelux">
                <div class="datepicker toolbarInline" id="asOfVersionDatePicker" hidden="hidden">
                    <div class="input-group">
                        <g:hiddenField name="asOfVersionDateValue"  value=""/>
                        <input placeholder="Select Date"
                               class="form-control"
                               name="asOfVersionDate" type="text" />
                        <g:render id="asOfVersion" template="/includes/widgets/datePickerTemplate"/>
                    </div>
                </div>
            </div>
        </div>
    </div>


</div>