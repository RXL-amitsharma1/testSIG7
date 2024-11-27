<div class="" id="alertThreshold" style="margin-top: 10px;">
    <div class="col-md-4" style="padding-left:2px;">
        <div class="form-inline">
            <label><g:message code="reportConfiguration.tiggered.alert.threshold"/>&nbsp;</label>
            <input class="form-control" name="alertTriggerCases" type="text" style="text-align: center; width:40px;"
                   value="${configurationInstance?.alertTriggerCases}"/>
            <label>&nbsp;<g:message code="reportConfiguration.tiggered.cases.in.last"/>&nbsp;</label>
            <input class="form-control" name="alertTriggerDays" type="text" style="text-align: center; width:40px;"
                   value="${configurationInstance?.alertTriggerDays}"/>
            <label>&nbsp;Days</label>
        </div>
    </div>
<g:if test="${actionName == 'edit'}">
    <div class="col-md-3">
        <label class="text-grey"><i>Original Date Range: ${firstExecutionDate}</i></label>
        <label class="text-grey"><i>Last Execution Date Range: ${lastExecutionDate}</i></label>
    </div>
</g:if>

    <div class="col-md-4 hidden" >
        <div class="checkbox" style="margin: 0;padding:0;">
            <label>
                <g:checkBox id="seperateAlertForFollowUp" name="seperateAlertForFollowUp"/>
                <b>
                    <g:message code="reportConfiguration.seperate.alert.follow.up"/>
                </b>
            </label>
        </div>
    </div>
    <div class="col-md-3 hidden" >
        <div class="checkbox" style="margin: 0;padding:0;">
            <ul>
                <g:checkBox id="unlistedEvents" name="unlistedEvents"/>
                <g:message code="reportConfiguration.triggered.unlistedEvents"/>
                <ul>
                     <g:checkBox id="unknownUnlisted" name="unlistedUnknown"/>
                     <g:message code="reportConfiguration.unknown.unlisted"/>
                </ul>
            </ul>
        </div>
    </div>
</div>

