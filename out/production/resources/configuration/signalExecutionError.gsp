<%@ page import="com.rxlogix.enums.FrequencyEnum; com.rxlogix.util.DateUtil;" %>
<!doctype html>
<html>
<head>
    <title><g:message code="app.viewErrorDetails" /></title>
    <meta name="layout" content="main">
    <asset:stylesheet src="errors.css"/>
    <g:javascript>
       var milestoneTableMap = "${milestonesCompleted}";
       var progressTrackerMap = "${progressTrackerMap}";
       var highestExecutionLevel = "${highestExecutionLevel}";
    </g:javascript>
    <asset:javascript src="app/pvs/configuration/milestoneTable.js"/>


</head>

<body>
<rx:container title="${message(code: "app.ExecutionStatus.error")}">
    <g:set var="userService" bean="userService"/>
    <div class="row">
        <div class="col-lg-12">
            <div class="row">
                <div class="col-lg-4">
                    <label><g:message code="app.label.alert.name" /></label>
                    <div>
                        ${exStatus?.name}
                    </div>

                </div>
                <div class="col-lg-4">
                    <label><g:message code="app.label.runDate" /></label>
                    <div>
                        ${com.rxlogix.util.DateUtil.fromDateToStringWithTimezone(exStatus?.nextRunDate,"dd-MMM-yyyy HH:mm:ss",userService.getCurrentUserPreference().timeZone)}
                    </div>

                </div>
                <div class="col-lg-4">
                    <label><g:message code="app.label.frequency"/></label>
                    <div>
                        ${(exStatus?.frequency).value()}
                    </div>

                </div>
                <g:if test="${exStatus?.timeStampJSON != null}">
                    <div class="col-lg-3">
                        <label><g:message code="app.label.CompletedMilestones"/></label>

                        <div id="completedMileStones">
                        </div>
                    </div>
                </g:if>

            </div>

            <sec:ifAnyGranted roles="ROLE_ADMIN">
                <div class="row">
                    <div class="col-lg-12">
                        <label><g:message code="app.label.stackTrace"/></label>
                        <div>
                            <div>
                                <g:textArea name="text" class="error" value="${exStatus?.stackTrace}" readonly="true"/>
                            </div>
                        </div>
                    </div>
                </div>
            </sec:ifAnyGranted>
        </div>
    </div>
</rx:container>
</body>
</html>