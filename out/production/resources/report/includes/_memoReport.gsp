<%@ page import="com.rxlogix.util.DateUtil; com.rxlogix.enums.DateRangeEnum; com.rxlogix.util.ViewHelper; com.rxlogix.pvdictionary.config.PVDictionaryConfig" %>

<div class="panel-group">
    <div class="rxmain-container">
        <div class="rxmain-container-inner">
            <div class="rxmain-container-row rxmain-container-header">
                <label class="rxmain-container-header-label">
                    Download report
                </label>
            </div>

            <div class="rxmain-container-content">
                <div class="row">
                    <div class="col-md-12">
                        <g:form action="download" method="GET">
                            <div class="row">
                                <div class="col-md-3">
                                    <label>Report Name<span class="required-indicator">*</span></label>
                                    <input placeholder="Name" name="reportName"
                                           class="form-control input-sm clear-field" id="reportName" required/>
                                </div>

                                <div class="col-md-3">
                                    <label>Product Name<span class="required-indicator">*</span></label>
                                    <g:select name="productName" from="${productNames}" value="${productName}"
                                              class="form-control"
                                              noSelection="['': '-Choose your product-']" required="required"/>
                                </div>


                                <div class="col-md-2">

                                    <div class="col-xs-12">
                                        <label><g:message code="app.label.DateRange"/></label>
                                        <g:select id="dateRangeEnum" name="dateRangeEnum" from="${ViewHelper.getEudraDateRange()}"
                                                  optionValue="display"
                                                  optionKey="name" value="${DateRangeEnum.CUMULATIVE}" class="form-control dateRangeEnumClass"/>
                                    </div>

                                    <div class="col-xs-12">
                                        <g:textField class="top-buffer form-control relativeDateRangeValue" name="relativeDateRangeValue"
                                                     id="relativeDateRangeValue" placeholder="${message(code: 'enter.x.here')}"
                                                     style="display: none; width:50%;margin-top:8px"
                                                     value=""/>
                                        <div class="notValidNumberErrorMessage" hidden="hidden" style="color: #ff0000">Enter Valid Number</div>

                                        <div class="fuelux datePickerParentDiv">
                                            <div class="text datepicker fromDateChanged" id="datePickerFromDiv" style="display:none">
                                                <g:message code="app.dateFilter.from"/>
                                                <div class="input-group">
                                                    <g:hiddenField value=""
                                                                   name="dateRangeStartAbsolute" id="dateRangeStartAbsolute" class="form-control"/>
                                                    <input placeholder="${message(code: 'scheduler.startDate')}" name="dateRangeStart"
                                                           class="form-control" id="dateRangeStart" type="text"/>
                                                    <g:render template="/includes/widgets/datePickerTemplate"/>
                                                </div>
                                            </div>

                                            <div class="toolbarInline datepicker toDateChanged" id="datePickerToDiv" style="display:none">
                                                <g:message code="app.dateFilter.to"/>
                                                <div class="input-group">
                                                    <g:hiddenField value=""
                                                                   name="dateRangeEndAbsolute" id="dateRangeEndAbsolute" class="form-control"/>
                                                    <input placeholder="${message(code: 'select.end.date')}" name="dateRangeEnd"
                                                           class="form-control" id="dateRangeEnd" type="text"/>
                                                    <g:render template="/includes/widgets/datePickerTemplate"/>
                                                </div>
                                            </div>

                                        </div>
                                    </div>
                                </div>

                            </div>

                            <div class="row" style="margin-top: 10px">
                                <div class="col-md-2 pull-right">
                                    <g:submitButton name="download" value="Download Report"
                                                    class="form-control btn btn-primary"/>
                                </div>
                            </div>
                        </g:form>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="rxmain-container" style="margin-top: 20px">
        <div class="rxmain-container-inner">
            <div class="rxmain-container-row rxmain-container-header">
                <label class="rxmain-container-header-label">
                    Report History
                </label>
            </div>

            <div class="rxmain-container-content">
                <div class="row">
                    <table id="reportsHistory" class="row-border hover evdas-alert-table" width="100%">
                        <thead>
                        <tr>
                            <th>Report Name</th>
                            <th>Product Name</th>
                            <th>Report Date Range</th>
                            <th>Generated By</th>
                            <th>Generated On</th>
                            <th></th>
                        </tr>
                        </thead>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>
<asset:javascript src="app/pvs/configuration/dateRangeEvdas.js"/>
<g:javascript>
  var ingredientLevel = ${PVDictionaryConfig.ingredientColumnIndex};
</g:javascript>