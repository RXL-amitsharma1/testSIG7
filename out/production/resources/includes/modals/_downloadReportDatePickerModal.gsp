<div class="modal fade" id="downloadReportModal" tabindex="-1" role="dialog"
     aria-hidden="true">
    <div class="modal-dialog modal-lg">
    <div class="modal-content">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                    aria-hidden="true">&times;</span></button>
            <h4 class="modal-title" id=""><g:message code="app.action.edit.label"/></h4>
        </div>

        <g:form controller="report" action="download" method="GET">
            <div class="modal-body" style="height: 100px;">
                <div class="col-md-12">
                    <div class="row">
                        <div class="col-md-4">
                            <label>Report Name<span class="required-indicator">*</span></label>
                            <input placeholder="Name" name="reportName"
                                   class="form-control input-sm clear-field" id="reportName" required/>
                            <g:hiddenField name="productName" value=""/>
                        </div>

                        <div class="col-md-4">
                            <label><g:message code="scheduler.startDate"/><span
                                    class="required-indicator">*</span></label>

                            <div class="fuelux">
                                <div class="datepicker form-group"
                                     id="reportStartDatePicker">
                                    <div class="input-group">
                                        <input placeholder="Start Date" name="startDate"
                                               class="form-control input-sm clear-field" id="startDate"
                                               value=""/>
                                        <g:render template="/includes/widgets/datePickerTemplate"/>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="col-md-4">
                            <label><g:message code="scheduler.endDate" default="End Date"/><span
                                    class="required-indicator">*</span></label>

                            <div class="fuelux">
                                <div class="datepicker form-group"
                                     id="reportEndDatePicker">
                                    <div class="input-group">
                                        <input placeholder="End Date" name="endDate"
                                               class="form-control input-sm clear-field" id="endDate"
                                               value=""/>
                                        <g:render template="/includes/widgets/datePickerTemplate"/>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="modal-footer">
                <div class="buttons col-md-2 pull-right">
                    <g:submitButton name="download" value="Download Report"
                                    class="form-control btn btn-primary"/>
                </div>
            </div>
            </div>
        </g:form>
    </div>
</div>
<script>
    $("#reportStartDatePicker").datepicker({
        allowPastDates: true
    });
    $("#reportEndDatePicker").datepicker({
        allowPastDates: true
    });
</script>