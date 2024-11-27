<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'etlSchedule.label')}"/>
    <title><g:message code="app.etlSchedule.edit.title"/></title>
    <asset:javascript src="fuelux/fuelux.js"/>
    <asset:stylesheet src="fuelux.css"/>
</head>

<body>

<rx:container title="${message(code: "app.label.etlScheduler")}">

    <g:link controller="etlSchedule" action="index" class="btn btn-default"><i class="fa fa-long-arrow-left"></i> <g:message code="app.label.etlStatus"/></g:link>

    <h3 class="page-header"><g:message code="default.edit.label" args="[entityName]"/></h3>

    <g:render template="/includes/layout/flashErrorsDivs" bean="${etlScheduleInstance}" var="theInstance"/>

    <g:form method="put" action="update" class="form-horizontal">
        <g:hiddenField name="id" value="${etlScheduleInstance?.id}"/>
        <g:hiddenField name="version" value="${etlScheduleInstance?.version}"/>

        <g:render template="form" model="[etlScheduleInstance: etlScheduleInstance]"/>

        <div class="buttonBar m-t-15">

            <g:if test="${etlScheduleInstance.isDisabled}">
                <g:link class="btn btn-primary" action="enable">
                    <g:message code="etlSchedule.enable.label"/>
                </g:link>
            </g:if>
            <g:else>
                <button name="edit" class="btn btn-primary">
                    %{--<span class="glyphicon glyphicon-ok icon-white"></span>--}%
                    ${message(code: 'default.button.update.label')}
                </button>
                <g:link class="btn btn-primary" action="disable">
                    <g:message code="etlSchedule.disable.label"/>
                </g:link>
            </g:else>
        </div>
    </g:form>

</rx:container>

</body>
</html>


