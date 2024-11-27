<%@ page import="com.rxlogix.config.SafetyGroup" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'safety.group.label')}"/>
    <title><g:message code="safety.groups.label"/></title>
</head>

<body>
<rx:container title="${message(code: "product.safety.lead.groups")}">

    <g:render template="/includes/layout/flashErrorsDivs" bean="${safetyGroupInstance}" var="theInstance"/>

    <sec:ifAnyGranted roles="ROLE_CONFIGURATION_CRUD">
        <div class="navScaffold">
            <g:link class="btn btn-primary" action="create">
                <span class="glyphicon glyphicon-plus icon-white"></span>
                <g:message code="default.new.label" args="[entityName]"/>
            </g:link>
        </div>
    </sec:ifAnyGranted>

    <div class="m-t-10">
        <table class="dataTable notused">
            <thead class="sectionsHeader">
            <tr>
                <g:sortableColumn property="name" title="${message(code: 'group.name.label')}"/>
                <th class="sorting_disabled"><g:message code="group.allowed.products"/></th>
            </tr>
            </thead>
            <tbody>
            <g:if test="${safetyGroupInstanceTotal > 0}">
                <g:each in="${safetyGroupInstanceList}" status="i" var="safetyGroupInstance">
                    <tr>
                        <td><g:link action="show"
                                    id="${safetyGroupInstance.id}">${safetyGroupInstance.name}</g:link></td>
                        <td>${fieldValue(bean: safetyGroupInstance, field: 'allowedProductDisplayName')}</td>
                    </tr>
                </g:each>
            </g:if>
            <g:else>
                <tr>
                    <td colspan="3"><g:message code="app.label.none"/></td>
                </tr>
            </g:else>
            </tbody>
        </table>
    </div>
    <g:render template="/includes/widgets/pagination" bean="${safetyGroupInstanceTotal}" var="theInstanceTotal"/>
</rx:container>
</body>
</html>
