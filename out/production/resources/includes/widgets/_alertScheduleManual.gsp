<%@ page import="com.rxlogix.util.DateUtil; com.rxlogix.util.ViewHelper" %>

<div class="panel panel-default rxmain-container rxmain-container-top" style="-webkit-box-shadow: none; border-color: white">
     <div class="row">
            <div class="col-xs-4">
                <div class="col-xs-3 fuelux row" id="schedule">
                    %{--The Markup code--}%
                    <g:render template="/includes/schedulerTemplate"/>
                    <g:hiddenField name="isEnabled" id="isEnabled" value="${configurationInstance?.isEnabled}"/>
                    <g:hiddenField name="schedulerTime"
                                   value="${com.rxlogix.util.RelativeDateConverter.getCurrentTimeWRTTimeZone(userService.getUser())}"/>
                    <g:hiddenField name="scheduleDateJSON"
                                   value="${configurationInstance?.scheduleDateJSON ?: null}"/>
                    <input type="hidden" name="configSelectedTimeZone" id="configSelectedTimeZone"
                           value="${ViewHelper.getUserTimeZoneForConfig(configurationInstance,userService.getUser())}"/>
                    <input type="hidden" id="timezoneFromServer" name="timezone"
                           value="${DateUtil.getTimezone(userService.getUser())}"/>
                </div>
            </div>
    </div>
</div>

