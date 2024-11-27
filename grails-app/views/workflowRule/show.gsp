
<%@ page import="com.rxlogix.config.WorkflowRule" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title><g:message code="workflowRule.show" default="Show WorkflowRule" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><g:link class="list btn btn-primary" action="list"><g:message code="workflowRule.list" default="WorkflowRule List" /></g:link></span>
            <span class="menuButton"><g:link class="create btn btn-primary" action="create"><g:message code="workflowRule.new" default="New WorkflowRule" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="workflowRule.show" default="Show WorkflowRule" /></h1>
            <g:if test="${flash.message}">
            <div class="message"><g:message code="${flash.message}" args="${flash.args}" default="${flash.defaultMessage}" /></div>
            </g:if>
            <g:form>
                <g:hiddenField name="id" value="${workflowRuleInstance?.id}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name"><g:message code="workflowRule.id" default="Id" />:</td>
                                
                                <td valign="top" class="value">${workflowRuleInstance.id}</td>
                                
                            </tr>
                            
                            <tr class="prop">
                                <td valign="top" class="name"><g:message code="workflowRule.targetState" default="Target State" />:</td>
                                
                                <td valign="top" class="value">${workflowRuleInstance.targetState}</td>
                                
                            </tr>
                            
                            <tr class="prop">
                                <td valign="top" class="name"><g:message code="workflowRule.description" default="Description" />:</td>
                                
                                <td valign="top" class="value">${workflowRuleInstance.description}</td>
                                
                            </tr>
                            
                            <tr class="prop">
                                <td valign="top" class="name"><g:message code="workflowRule.incomingDisposition" default="Income State" />:</td>
                                
                                <td valign="top" class="value"><g:link controller="PVSState" action="show" id="${workflowRuleInstance?.incomeState?.id}">${workflowRuleInstance?.incomeState?.encodeAsHTML()}</g:link></td>
                                
                            </tr>
                            
                            <tr class="prop">
                                <td valign="top" class="name"><g:message code="workflowRule.name" default="Name" />:</td>
                                
                                <td valign="top" class="value">${workflowRuleInstance.name}</td>
                                
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
