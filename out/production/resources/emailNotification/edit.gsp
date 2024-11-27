<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <title><g:message code="app.email.notification.page.title"/></title>
</head>

<body>

<rx:container title="${message(code: "app.email.notification.title")}">
    <h1 class="page-header"><g:message code="app.email.notification.title"/></h1>
    <g:if test="${flash.message}">
        <div class="alert alert-success alert-dismissible" role="alert" id="alert-success">
            <button type="button" class="close successButton">
                <span onclick="this.parentNode.parentNode.remove();
                return false;">x</span>
                <span class="sr-only"><g:message code="default.button.close.label"/></span>
            </button>
            <div class="success-message">
                ${flash.message}
            </div>
        </div>
    </g:if>
    <g:form method="put" action="update" class="form-horizontal form-elements-left">

        <g:render template="form"/>

        <sec:ifAnyGranted roles="ROLE_CONFIGURATION_CRUD">
            <div class="buttonBar">
                <button name="edit" class="btn btn-primary">
                    ${message(code: 'default.button.save.label')}
                </button>
                <g:link class="cancelLink btn btn-default" action="edit"><g:message
                        code="default.button.cancel.label"/></g:link>
            </div>
        </sec:ifAnyGranted>
    </g:form>

</rx:container>

</body>
</html>
