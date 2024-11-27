<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'etlSchedule.label')}"/>
    <title><g:message code="app.etlSchedule.create.title"/></title>
    <asset:javascript src="fuelux/fuelux.js"/>
    <asset:stylesheet src="fuelux.css"/>
    <asset:javascript src="app/pvs/etlScheduler.js"/>

</head>

<body>

<rx:container title="${message(code: "app.label.etlScheduler")}">

    <h1 class="page-header"><g:message code="default.create.label" args="[entityName]"/></h1>

    <g:render template="/includes/layout/flashErrorsDivs" bean="${etlScheduleInstance}" var="theInstance"/>

    <g:form method="post" action="save" class="form-horizontal">
        <g:render template="form" model="[etlScheduleInstance: etlScheduleInstance]"/>
        <div class="buttonBar">
            <button name="edit" class="btn btn-primary">
                %{--<span class="glyphicon glyphicon-ok icon-white"></span>--}%
                ${message(code: 'default.button.save.label')}
            </button>
            <g:link class="btn btn-default" action="index"><g:message code="default.button.cancel.label"/></g:link>
        </div>
    </g:form>
</rx:container>
</body>
</html>




