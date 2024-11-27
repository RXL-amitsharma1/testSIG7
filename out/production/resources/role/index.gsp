<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'role.label')}"/>
    <title><g:message code="roles.label"/></title>
</head>

<body>

<rx:container title="${message(code: "app.label.roleManagement")}">

    <g:render template="/includes/layout/flashErrorsDivs" bean="${roleInstance}" var="theInstance"/>

    <div class="table-responsive curvedBox">
        <table class="table table-striped table-curved table-hover">
            <thead>
            <tr>
                <g:sortableColumn property="authority" title="${message(code: 'role.authority.label')}"/>
                <g:sortableColumn property="description" title="${message(code: 'role.description.label')}"/>
            </tr>
            </thead>
            <tbody>
            <g:if test="${roleInstanceTotal > 0}">
                <g:each in="${roleInstanceList.findAll { !("ROLE_DEV".equalsIgnoreCase(it.authority)) }}" status="i" var="roleInstance">
                    <tr>
                        <td><g:link action="show" id="${roleInstance.id}"><g:message code="app.role.${roleInstance.authority}" default="${roleInstance.authority}"/></g:link></td>
                        <td>${roleInstance.description}</td>
                    </tr>
                </g:each>
            </g:if>
            <g:else>
                <tr>
                    <td colspan="2">None</td>
                </tr>
            </g:else>
            </tbody>
        </table>
    </div>

    <g:render template="/includes/widgets/pagination" bean="${roleInstanceTotal}" var="theInstanceTotal"/>

</rx:container>

</body>
</html>
