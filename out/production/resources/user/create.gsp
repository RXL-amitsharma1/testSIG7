<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <asset:javascript src="app/pvs/departments.js"/>
    <g:set var="entityName" value="${message(code: 'user.label')}"/>
    <title><g:message code="default.create.label" args="[entityName]"/></title>
    <style>
    ul.multiselect-container{
            width:auto;
        }
    </style>
    <g:if test="${groupsJson}">
        <g:javascript>
            var selectedGroups = JSON.parse("${groupsJson}");
        </g:javascript>
    </g:if>
    <g:if test="${safetyGroupsJson}">
        <g:javascript>
            var selectedSafetyGroups = JSON.parse("${safetyGroupsJson}");
        </g:javascript>
    </g:if>
</head>

<body>

<rx:container title="${message(code: "app.label.userManagement")}">

    <h1 class="page-header"><g:message code="default.create.label" args="[entityName]"/></h1>

    <g:render template="/includes/layout/flashErrorsDivs" bean="${userInstance}" var="theInstance"/>

    <g:form method="post" action="save" class="form-horizontal form-elements-left">
        <g:render template="form" model="[userInstance: userInstance]"/>

        <div class="buttonBar">
            <button name="edit" class="btn btn-primary">
                %{--<span class="glyphicon glyphicon-ok icon-white"></span>--}%
                ${message(code: 'default.button.save.label')}
            </button>
            <g:link class="cancelLink btn btn-default" action="index"><g:message code="default.button.cancel.label"/></g:link>
        </div>
    </g:form>

</rx:container>

</body>
</html>
