<%@ page import="com.rxlogix.util.DateUtil; com.rxlogix.util.ViewHelper" %>

<div class="panel panel-default rxmain-container rxmain-container-top">
    <div class="rxmain-container-row rxmain-container-header panel-heading">
        <h4 class="rxmain-container-header-label">
            <a data-toggle="collapse" data-parent="#accordion-pvs-analysis" href="#pvsAlertScheduler" aria-expanded="true" class="">
                <g:message code="app.label.schedule.alert"/>
            </a>
        </h4>
    </div>
    <div id="pvsAlertScheduler" class="panel-collapse rxmain-container-content rxmain-container-show collapse in" aria-expanded="true">
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
            %{--For Edit only--}%
            <div class="row">
                <g:if test="${configurationInstance?.isEnabled && configurationInstance?.nextRunDate}">
                    <div class="col-xs-8 nextScheulerInfo ${hasErrors(bean: configurationInstance, field: 'nextRunDate', 'has-error')}">
                        <label><g:message code="app.label.nextScheduledRunDate"/></label>
                        <div>
                            <g:render template="/includes/widgets/dateDisplayWithTimezone" model="[date:configurationInstance?.nextRunDate]"/>
                        </div>
                    </div>
                </g:if>
            </div>
        </div>
        <g:if test="${isLiterature}">
        <div class="row" style="padding-top:10px;">
            <span class="text-secondary" style="font-style: italic;">
                <g:message code="app.literature.data.length"/>
            </span>
        </div>
        </g:if>
    </div>
</div>



