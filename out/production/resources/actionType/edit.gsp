<%@ page import="com.rxlogix.config.ActionType" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <title><g:message code="action.type.edit" default="Edit ActionType"/></title>
</head>

<body>
<rx:container title="Edit ActionType">
    <div class="nav">
        <span class="menuButton"><g:link class="list btn btn-primary" action="list"><g:message code="action.type.list" default="ActionType List"/></g:link></span>
        <span class="menuButton"><g:link class="create btn btn-primary" action="create"><g:message code="action.type.new" default="New ActionType"/></g:link></span>
    </div>
    <div class="row">
        <div class="col-md-6 col-xs-12">
            <g:if test="${flash.error}">
                <div class="alert alert-danger alert-dismissible" role="alert" style="word-break: break-all">
                    <button type="button" class="close" data-dismiss="alert">
                        <span  onclick="this.parentNode.parentNode.remove(); return false;">x</span>
                        <span class="sr-only"><g:message code="default.button.close.label" /></span>
                    </button>
                    <g:if test="${flash.error}">
                        <g:if test="${flash.error.contains( '<linkQuery>' )}">
                            ${flash.error.substring( 0, flash.error.indexOf( '<linkQuery>' ) )}
                            <a href="${flash.error.substring( flash.error.indexOf( '<linkQuery>' ) + 11 )}"><g:message
                                    code="see.details"/></a>
                        </g:if>
                        <g:else>
                            <g:if test="${flash.error instanceof List}">
                                <g:each in="${flash.error}" var="field">
                                    ${raw(field)}<br>
                                </g:each>
                            </g:if>
                            <g:else>
                                ${raw(flash.error)}<br>
                            </g:else>
                        </g:else>
                    </g:if>
                </div>
            </g:if>

            <g:form method="POST">
                <g:hiddenField name="id" value="${actionTypeInstance?.id}"/>
                <g:hiddenField name="version" value="${actionTypeInstance?.version}"/>

                <div class="form-group">
                    <label for="value"><g:message code="action.type.name" default="Name"/>:<span class="required-indicator">*</span></label>
                    <g:textField name="value" class="form-control" value="${actionTypeInstance?.value}" />
                </div>
                <div class="form-group">
                    <label for="displayName"><g:message code="action.type.display.name"
                                                        default="Display Name"/>:<span class="required-indicator">*</span></label>
                    <g:textField name="displayName" class="form-control" value="${actionTypeInstance?.displayName}" />
                </div>
                <div class="form-group">
                    <label for="description"><g:message code="action.type.description"
                                                        default="Description"/>:</label>
                    <g:textField name="description" class="form-control" value="${actionTypeInstance?.description}"/>
                </div>

                <div class="buttons">
                    <span class="button"><g:actionSubmit class="save btn btn-primary" action="update" value="${message(code: 'default.button.update.label', 'default': 'Update')}"/></span>
                    <span class="button"><g:actionSubmit class="delete btn pv-btn-dark-grey" action="delete" value="${message(code: 'default.button.delete.label', 'default': 'Delete')}"
                                                         onclick="return confirm('${message(code: 'default.button.delete.confirm.message', 'default': 'Are you sure?')}');"/></span>
                </div>
            </g:form>
        </div>
    </div>
</rx:container>
</body>
</html>
