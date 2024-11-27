
<%@ page import="com.rxlogix.config.PVSState" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title><g:message code="PVSState.show" default="Show Workflow State" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><g:link class="list btn btn-primary" action="index">
                <g:message code="PVSState.list" default="Workflow State List" /></g:link></span>
            <span class="menuButton"><g:link class="create btn btn-primary" action="create">
                <g:message code="PVSState.new" default="New Workflow State" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="PVSState.show" default="Show Workflow State" /></h1>
            <g:if test="${flash.message}">
            <div class="message"><g:message code="${flash.message}" args="${flash.args}" default="${flash.defaultMessage}" /></div>
            </g:if>
            <g:form>
                <g:hiddenField name="id" value="${PVSStateInstance?.id}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name"><g:message code="PVSState.id" default="Id" />:</td>
                                
                                <td valign="top" class="value">${fieldValue(bean: PVSStateInstance, field: "id")}</td>
                                
                            </tr>
                            
                            <tr class="prop">
                                <td valign="top" class="name"><g:message code="PVSState.description" default="Description" />:</td>
                                
                                <td valign="top" class="value">${fieldValue(bean: PVSStateInstance, field: "description")}</td>
                                
                            </tr>
                            
                            <tr class="prop">
                                <td valign="top" class="name"><g:message code="PVSState.display" default="Display" />:</td>
                                
                                <td valign="top" class="value"><g:formatBoolean boolean="${PVSStateInstance?.display}" /></td>
                                
                            </tr>
                            
                            <tr class="prop">
                                <td valign="top" class="name"><g:message code="PVSState.displayName" default="Display Name" />:</td>
                                
                                <td valign="top" class="value">${fieldValue(bean: PVSStateInstance, field: "displayName")}</td>
                                
                            </tr>
                            
                            <tr class="prop">
                                <td valign="top" class="name"><g:message code="PVSState.finalState" default="Final State" />:</td>
                                
                                <td valign="top" class="value"><g:formatBoolean boolean="${PVSStateInstance?.finalState}" /></td>
                                
                            </tr>
                            
                            <tr class="prop">
                                <td valign="top" class="name"><g:message code="PVSState.value" default="Value" />:</td>
                                
                                <td valign="top" class="value">${fieldValue(bean: PVSStateInstance, field: "value")}</td>
                                
                            </tr>
                            
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><g:actionSubmit class="edit" action="edit" value="${message(code: 'edit', 'default': 'Edit')}" /></span>
                    <span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'delete', 'default': 'Delete')}" onclick="return confirm('${message(code: 'delete.confirm', 'default': 'Are you sure?')}');" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
