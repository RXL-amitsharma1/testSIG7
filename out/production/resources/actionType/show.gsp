<%@ page import="com.rxlogix.config.ActionType" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <title><g:message code="action.type.show" default="Show ActionType"/></title>
</head>

<body>
<rx:container title="Show ActionType">
    <g:render template="/includes/layout/flashErrorsDivs" bean="${actionTypeInstance}" var="theInstance"/>
    <div class="nav ">
        <span class="menuButton"><g:link class="list btn btn-primary" action="list"><g:message code="action.type.list"
                                                                                               default="ActionType List"/></g:link></span>
        <sec:ifAnyGranted roles="ROLE_CONFIGURATION_CRUD">
            <span class="menuButton"><g:link class="create btn btn-primary" action="create"><g:message
                    code="action.type.new" default="New ActionType"/></g:link></span>
        </sec:ifAnyGranted>
    </div>

    <div class="body">
        <g:form>
            <g:hiddenField name="id" value="${actionTypeInstance?.id}"/>
            <div class="row m-t-10">
                <div class="dialog col-md-6 col-xs-12">
                    <table class="row-border hover table table-stripped">
                        <tbody>
                            <tr class="prop">
                                <th class="name"><g:message code="action.type.name" default="Name"/>:</th>
                                <td class="value">${fieldValue(bean: actionTypeInstance, field: "value")}</td>
                            </tr>
                            <tr class="prop">
                                <th class="name"><g:message code="action.type.display.name" default="Display Name"/>:</th>
                                <td class="value">${fieldValue(bean: actionTypeInstance, field: "displayName")}</td>
                            </tr>
                            <tr class="prop">
                                <th class="name"><g:message code="action.type.description" default="Description"/>:</th>
                                <td class="value" style="white-space: pre-wrap !important">${fieldValue(bean: actionTypeInstance, field: "description")}</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
            <sec:ifAnyGranted roles="ROLE_CONFIGURATION_CRUD">
                <div class="row">
                    <div class="dialog col-md-6 col-xs-12">
                        <div class="buttons m-t-10">
                            <span class="button"><g:actionSubmit class="edit btn btn-primary" action="edit"
                                                                 value="${message(code: 'default.button.edit.label', 'default': 'Edit')}"/></span>
                            <span class="button"><g:actionSubmit class="delete btn pv-btn-dark-grey" action="delete"
                                                                 value="${message(code: 'default.button.delete.label', 'default': 'Delete')}"
                                                                 onclick="return confirm('${message(code: 'default.button.delete.confirm.message', 'default': 'Are you sure?')}');"/></span>
                        </div>
                    </div>
                </div>
            </sec:ifAnyGranted>
        </g:form>
    </div>
</rx:container>
</body>
</html>
