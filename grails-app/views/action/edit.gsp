<%@ page import="com.rxlogix.config.Action" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title><g:message code="app.action.edit.label" default="Edit Action" /></title>
        <asset:javascript src="fuelux/fuelux.js" />
        <asset:stylesheet src="fuelux.css" />
        <asset:javascript src="app/pvs/actions/action_edit.js" />
    </head>
    <rx:container title="Update Action">
        <div class="nav">
        </div>
        <div class="body">
            <g:if test="${flash.message}">
            <div class="message"><g:message code="${flash.message}" args="${flash.args}" default="${flash.defaultMessage}" /></div>
            </g:if>
            <g:hasErrors bean="${actionInstance}">
            <div class="errors">
                <g:renderErrors bean="${actionInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form controller="action" method="post" action="update" name="action-editor-form" >
                <g:render template="form" model="[actionInstance: actionInstance, edit: true, createdDate: createdDate]" />
            </g:form>
        </div>
    </rx:container>
</html>
