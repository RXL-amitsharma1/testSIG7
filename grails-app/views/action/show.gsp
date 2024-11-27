
<%@ page import="com.rxlogix.config.Action" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title><g:message code="action.show" default="Show Action" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><g:link class="create" action="create"><g:message code="action.new" default="New Action" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="action.show" default="Show Action" /></h1>
            <g:if test="${flash.message}">
            <div class="message"><g:message code="${flash.message}" args="${flash.args}" default="${flash.defaultMessage}" /></div>
            </g:if>
            <g:form>
                <g:hiddenField name="id" value="${actionInstance?.id}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name"><g:message code="action.id" default="Id" />:</td>
                                
                                <td valign="top" class="value">${fieldValue(bean: actionInstance, field: "id")}</td>
                                
                            </tr>
                            
                            <tr class="prop">
                                <td valign="top" class="name"><g:message code="action.details" default="Details" />:</td>
                                
                                <td valign="top" class="value">${fieldValue(bean: actionInstance, field: "details")}</td>
                                
                            </tr>
                            
                            <tr class="prop">
                                <td valign="top" class="name"><g:message code="action.createdDate" default="Created Date" />:</td>
                                
                                <td valign="top" class="value"><g:formatDate date="${actionInstance?.createdDate}" /></td>
                                
                            </tr>
                            
                            <tr class="prop">
                                <td valign="top" class="name"><g:message code="action.dueDate" default="Due Date" />:</td>
                                
                                <td valign="top" class="value"><g:formatDate date="${actionInstance?.dueDate}" /></td>
                                
                            </tr>
                            
                            <tr class="prop">
                                <td valign="top" class="name"><g:message code="action.completedDate" default="Completed Date" />:</td>
                                
                                <td valign="top" class="value"><g:formatDate date="${actionInstance?.completedDate}" /></td>
                                
                            </tr>
                            
                            <tr class="prop">
                                <td valign="top" class="name"><g:message code="action.assignedTo" default="Assigned To" />:</td>
                                
                                <td valign="top" class="value"><g:link controller="user" action="show" id="${actionInstance?.assignedTo?.id}">${actionInstance?.assignedTo?.encodeAsHTML()}</g:link></td>
                                
                            </tr>
                            
                            <tr class="prop">
                                <td valign="top" class="name"><g:message code="action.owner" default="Owner" />:</td>
                                
                                <td valign="top" class="value"><g:link controller="user" action="show" id="${actionInstance?.owner?.id}">${actionInstance?.owner?.encodeAsHTML()}</g:link></td>
                                
                            </tr>
                            
                            <tr class="prop">
                                <td valign="top" class="name"><g:message code="action.comments" default="Comments" />:</td>
                                
                                <td  valign="top" style="text-align: left;" class="value">
                                    <ul>
                                    <g:each in="${actionInstance?.comments}" var="commentInstance">
                                        <li><g:link controller="comment" action="show" id="${commentInstance.id}">${commentInstance.encodeAsHTML()}</g:link></li>
                                    </g:each>
                                    </ul>
                                </td>
                                
                            </tr>
                            
                            <tr class="prop">
                                <td valign="top" class="name"><g:message code="action.alert" default="Alert" />:</td>
                                
                                <td valign="top" class="value"><g:link controller="alert" action="show" id="${actionInstance?.alert?.id}">${actionInstance?.alert?.encodeAsHTML()}</g:link></td>
                                
                            </tr>
                            
                            <tr class="prop">
                                <td valign="top" class="name"><g:message code="action.type" default="Type" />:</td>
                                
                                <td valign="top" class="value"><g:link controller="actionType" action="show" id="${actionInstance?.type?.id}">${actionInstance?.type?.encodeAsHTML()}</g:link></td>
                                
                            </tr>
                            
                            <tr class="prop">
                                <td valign="top" class="name"><g:message code="action.config" default="Config" />:</td>
                                
                                <td valign="top" class="value"><g:link controller="actionConfiguration" action="show" id="${actionInstance?.config?.id}">${actionInstance?.config?.encodeAsHTML()}</g:link></td>
                                
                            </tr>
                            
                            <tr class="prop">
                                <td valign="top" class="name"><g:message code="action.actionStatus" default="Action Status" />:</td>
                                
                                <td valign="top" class="value">${actionInstance?.actionStatus?.encodeAsHTML()}</td>
                                
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
