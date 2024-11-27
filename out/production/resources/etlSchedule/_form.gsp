<%@ page import="java.text.SimpleDateFormat; com.rxlogix.util.DateUtil; grails.util.Environment" %>
<head>
    <g:set var="userService" bean="userService"/>
    <asset:javascript src="fuelux/fuelux.js"/>
    <asset:stylesheet src="fuelux.css"/>
    <asset:javascript src="app/pvs/etlScheduler.js"/>
</head>

<div class="row">
    <div class="col-md-6">
        <label class="control-label" >
            <g:message code="etlMaster.scheduleName.label"/> : ${etlScheduleInstance?.scheduleName}
        </label>
    </div>
</div>
<div class="horizontalRuleFull"></div>
<div class="row">

    <div class="col-xs-3 fuelux">
        <g:render template="/includes/schedulerTemplate"/>
        <g:hiddenField id="isDisabled" name="isDisabled"  value="${etlScheduleInstance?.isDisabled}"/>
        <g:hiddenField id="startDateTime" name="startDateTime" value="${etlScheduleInstance?.startDateTime}"/>
        <g:hiddenField id="repeatInterval" name="repeatInterval" value="${etlScheduleInstance?.repeatInterval}" />
        <g:hiddenField id="timezoneFromServer" name="timezone" value="${com.rxlogix.util.DateUtil.getTimezone(userService.getUser())}"/>
    </div>
</div>

