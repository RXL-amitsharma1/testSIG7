<html>
<head>
    <meta http-equiv="Content-Type" content="text/html" charset="UTF-8"/>
    <meta name="layout" content="main"/>
    <title><g:message code="action.configuration.show" default="Show ActionConfiguration"/></title>
</head>

<body>
<rx:container title="Show ActionConfiguration">
    <div class="nav bord-bt">
    <span class="menuButton"><g:link class="list btn btn-primary" action="list"><g:message
            code="action.configuration.list" default="ActionConfiguration List"/></g:link></span>
    <sec:ifAnyGranted roles="ROLE_CONFIGURATION_CRUD">
        <span class="menuButton"><g:link class="create btn btn-primary" action="create"><g:message
            code="action.configuration.new" default="New ActionConfiguration"/></g:link></span>
    </sec:ifAnyGranted>
    </div>
    <g:render template="/includes/layout/flashErrorsDivs" bean="${signal}" var="theInstance"/>
    <div class="body">
        <g:if test="${flash.message}">
            <div class="message"><g:message code="${flash.message}" args="${flash.args}"
                                            default="${flash.defaultMessage}"/></div>
        </g:if>

        <div class="row">

            <div class="col-md-10">
                <g:form>
                    <g:hiddenField name="id" value="${actionConfigurationInstance?.id}"/>
                    <div class="form-group">
                        <label><g:message code="action.configuration.name" default="Name"/>:</label>
                        <span>${fieldValue(bean: actionConfigurationInstance, field: "value")}</span>
                    </div>

                    <div class="form-group">
                        <label><g:message code="action.configuration.display.name" default="Display Name"/>:</label>
                        <span>${fieldValue(bean: actionConfigurationInstance, field: "displayName")}</span>
                    </div>

                    <div class="form-group">
                        <label><g:message code="action.configuration.description" default="Description"/>:</label>
                        <span style="white-space: pre-wrap !important">${fieldValue(bean: actionConfigurationInstance, field: "description")}</span>
                    </div>

                    <sec:ifAnyGranted roles="ROLE_CONFIGURATION_CRUD">
                        <div class="form-group">
                        <g:link class="edit btn btn-primary" controller="actionConfiguration"
                                params="[id: actionConfigurationInstance.id]"
                                action="edit">${message(code: 'default.button.edit.label', 'default': 'Edit')}</g:link>
                        <g:actionSubmit class="delete btn pv-btn-grey hide" action="delete"
                                        value="${message(code: 'default.button.delete.label', 'default': 'Delete')}"
                                        onclick="return confirm('${message(code: 'default.button.delete.confirm.message', 'default': 'Are you sure?')}');"/>
                    </div>
                    </sec:ifAnyGranted>
                </g:form>
            </div>

            <div class="col-md-1"></div>
        </div>
    </div>
</rx:container>



</body>
</html>