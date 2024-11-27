<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'accessControlGroup.label')}"/>
    <title><g:message code="accessControlGroups.label"/></title>
</head>

<body>

<rx:container title="${message(code: "app.label.accessControlGroup")}">

    <h1 class="page-header"><g:message code="accessControlGroups.label"/></h1>

    <g:render template="/includes/layout/flashErrorsDivs" bean="${acgInstance}" var="theInstance"/>

    <div class="navScaffold">
        <g:link class="btn btn-primary" action="create">
            <span class="glyphicon glyphicon-plus icon-white"></span>
            <g:message code="default.new.label" args="[entityName]"/>
        </g:link>
    </div>

    <div class="table-responsive curvedBox">
        <table class="table table-striped table-curved table-hover">
            <thead>
            <tr>
                <g:sortableColumn property="name" title="${message(code: 'accessControlGroup.name.label')}"/>
                <g:sortableColumn property="ldapGroupName" title="${message(code: 'accessControlGroup.ldapGroupName.label')}"/>
                <g:sortableColumn property="description" title="${message(code: 'accessControlGroup.description.label')}"/>
            </tr>
            </thead>
            <tbody>
            <g:if test="${acgInstanceTotal > 0}">
                <g:each in="${acgInstanceList}" status="i" var="acgInstance">
                    <tr>
                        <td><g:link action="show" id="${acgInstance.id}">${acgInstance.name}</g:link></td>
                        <td>${acgInstance.ldapGroupName}</td>
                        <td>${acgInstance.description}</td>
                    </tr>
                </g:each>
            </g:if>
            <g:else>
                <tr>
                    <td colspan="3">None</td>
                </tr>
            </g:else>
            </tbody>
        </table>
    </div>

    <g:render template="/includes/widgets/pagination" bean="${acgInstanceTotal}" var="theInstanceTotal"/>

</rx:container>

</body>
</html>
