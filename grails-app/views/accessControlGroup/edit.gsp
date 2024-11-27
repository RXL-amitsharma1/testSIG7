<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'user.label')}"/>
    <title><g:message code="default.edit.label" args="[entityName]"/></title>
</head>

<body>

<rx:container title="${message(code: "app.label.accessControlGroup")}">

    <h1 class="page-header"><g:message code="default.edit.label" args="[entityName]"/></h1>

    <g:render template="/includes/layout/flashErrorsDivs" bean="${acgInstance}" var="theInstance"/>

    <g:form method="put" action="update" class="form-horizontal">
        <g:hiddenField name="id" value="${acgInstance?.id}"/>
        <g:hiddenField name="version" value="${acgInstance?.version}"/>

        <g:render template="form" model="[acgInstance: acgInstance]"/>

        <div class="buttonBar">
            <button name="edit" class="btn btn-primary">
                %{--<span class="glyphicon glyphicon-ok icon-white"></span>--}%
                ${message(code: 'default.button.save.label')}
            </button>
            <g:link class="cancelLink" action="index">Cancel</g:link>
        </div>
    </g:form>

</rx:container>

</body>
</html>
