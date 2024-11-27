<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'role.label')}"/>
    <title><g:message code="default.edit.label" args="[entityName]"/></title>
</head>

<body>

<rx:container title="${message(code: "app.label.roleManagement")}">

    <h1 class="page-header"><g:message code="default.edit.label" args="[entityName]"/></h1>

    <g:render template="/includes/layout/flashErrorsDivs" bean="${roleInstance}" var="theInstance"/>

    <g:form method="put" action="update" class="form-horizontal">
        <g:hiddenField name="id" value="${roleInstance?.id}"/>
        <g:hiddenField name="version" value="${roleInstance?.version}"/>

        <g:render template="form" model="[roleInstance: roleInstance]"/>

        <div class="buttonBar">
            <button name="edit" class="btn btn-custom">
                %{--<span class="glyphicon glyphicon-ok icon-white"></span>--}%
                ${message(code: 'default.button.save.label')}
            </button>
            <g:link class="cancelLink" action="index">Cancel</g:link>
        </div>
    </g:form>

</rx:container>

</body>
</html>
