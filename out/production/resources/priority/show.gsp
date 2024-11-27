
<%@ page import="com.rxlogix.config.Priority" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title><g:message code="priority.show" default="Show Priority" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><g:link class="list btn  btn-primary" action="list"><g:message code="priority.list" default="Priority List" /></g:link></span>
            <span class="menuButton"><g:link class="create btn  btn-primary" action="create"><g:message code="priority.new" default="New Priority" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="priority.show" default="Show Priority" /></h1>
            <g:if test="${flash.message}">
            <div class="message"><g:message code="${flash.message}" args="${flash.args}" default="${flash.defaultMessage}" /></div>
            </g:if>
            <g:form>
                <g:hiddenField name="id" value="${priorityInstance?.id}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name"><g:message code="priority.id" default="Id" />:</td>
                                
                                <td valign="top" class="value">${priorityInstance.id}</td>
                                
                            </tr>
                            
                            <tr class="prop">
                                <td valign="top" class="name"><g:message code="priority.description" default="Description" />:</td>
                                
                                <td valign="top" class="value">${priorityInstance.description}</td>
                                
                            </tr>

                            <tr class="prop">
                                <td valign="top" class="name"><g:message code="priority.displayName" default="Display Name" />:</td>
                                
                                <td valign="top" class="value">${priorityInstance.displayName}</td>
                                
                            </tr>

                            <tr class="prop">
                                <td valign="top" class="name"><g:message code="priority.value" default="Value" />:</td>
                                
                                <td valign="top" class="value">${priorityInstance.value}</td>
                                
                            </tr>
                            
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><g:actionSubmit class="edit btn btn-primary" action="edit" value="${message(code: 'edit', 'default': 'Edit')}" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
