
<%@ page import="com.rxlogix.config.Disposition" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title><g:message code="disposition.show" default="Show Disposition" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><g:link class="list btn btn-primary" action="list"><g:message code="disposition.list" default="Disposition List" /></g:link></span>
            <span class="menuButton"><g:link class="create btn btn-primary" action="create"><g:message code="disposition.new" default="New Disposition" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="disposition.show" default="Show Disposition" /></h1>
            <g:if test="${flash.message}">
            <div class="message"><g:message code="${flash.message}" args="${flash.args}" default="${flash.defaultMessage}" /></div>
            </g:if>
            <g:form>
                <g:hiddenField name="id" value="${dispositionInstance?.id}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name"><g:message code="disposition.id" default="Id" />:</td>
                                
                                <td valign="top" class="value">${dispositionInstance.id}</td>
                                
                            </tr>
                            
                            <tr class="prop">
                                <td valign="top" class="name"><g:message code="disposition.display" default="Display" />:</td>
                                
                                <td valign="top" class="value"><g:formatBoolean boolean="${dispositionInstance?.display}" /></td>
                                
                            </tr>
                            
                            <tr class="prop">
                                <td valign="top" class="name"><g:message code="disposition.validatedConfirmed" default="Validated Confirmed" />:</td>
                                
                                <td valign="top" class="value"><g:formatBoolean boolean="${dispositionInstance?.validatedConfirmed}" /></td>
                                
                            </tr>
                            
                            <tr class="prop">
                                <td valign="top" class="name"><g:message code="disposition.value" default="Value" />:</td>
                                
                                <td valign="top" class="value">${dispositionInstance.value}</td>
                                
                            </tr>
                            
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><g:actionSubmit class="edit btn btn-primary" action="edit" value="${message(code: 'edit', 'default': 'Edit')}" /></span>
                    <span class="button"><g:actionSubmit class="delete btn btn-default" action="delete" value="${message(code: 'delete', 'default': 'Delete')}" onclick="return confirm('${message(code: 'delete.confirm', 'default': 'Are you sure?')}');" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
