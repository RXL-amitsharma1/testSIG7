<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'role.label')}"/>
    <title><g:message code="role.label"/></title>
</head>

<body>

<g:set var="column1Width" value="4"/>
<g:set var="column2Width" value="8"/>

<rx:container title="${message(code: "app.label.roleManagement")}">

    <g:link action="index"><< Roles List</g:link>

    <g:render template="/includes/layout/flashErrorsDivs" bean="${roleInstance}" var="theInstance"/>

    <div class="row">
        <div class="col-md-12">

            <h3 class="sectionHeader">Role Details</h3>

            <div class="col-md-12">

            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message code="role.authority.label"/></label></div>

                <div class="col-md-${column2Width}"><g:message code="app.role.${roleInstance.authority}" default="${roleInstance.authority}"/></div>
            </div>

            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message code="role.description.label"/></label></div>

                <div class="col-md-${column2Width}">${roleInstance.description}</div>
            </div>
        </div>

        </div>
    </div>

</rx:container>

</body>
</html>
