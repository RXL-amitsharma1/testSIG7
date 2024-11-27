<div class="row form-group">
    <div class="col-lg-4">
        <label>
            <g:message code="app.label.meeting.fmtStartDate" default="Start Date"/>
            <span class="required-indicator">*</span>
        </label>
        <div class="fuelux">
            <div class="datepicker form-group" data-initialize="datepicker" id="fmt-start-date-picker-${text}">
                <div class="input-group">
                    <input name="fmtStartDate"
                           class="form-control input-sm" id="fmtStartDate-${text}" type="text"
                           data-date=""
                           value=""/>
                    <g:render template="/includes/widgets/datePickerTemplate"/>
                </div>
            </div>
        </div>
    </div>

    <div class="col-lg-4">
        <label>
            <g:message code="app.label.meeting.fmtEndDate" default="End Date"/>
            <span class="required-indicator">*</span>
        </label>
        <div class="fuelux">
            <div class="datepicker form-group" data-initialize="datepicker" id="fmt-start-date-picker-${text}">
                <div class="input-group">
                    <input name="fmtEndDate"
                           class="form-control input-sm" id="fmtEndDate-${text}" type="text"
                           data-date=""
                           value=""/>
                    <g:render template="/includes/widgets/datePickerTemplate"/>
                </div>
            </div>
        </div>
    </div>

    <div class="col-lg-4">
        <a class="findMeetingTimes btn btn-primary user-action" data-text="${text}" style="float:right;">Search</a>
    </div>

</div>

<div class="time-slot-container">
</div>