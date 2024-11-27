<%@ page import="com.rxlogix.config.Action" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title><g:message code="app.action.create.label" default="Create Action" /></title>

        <asset:javascript src="fuelux/fuelux.js"/>
        <asset:stylesheet src="fuelux.css" />
    </head>
    <rx:container title="<app.action.create.label">
        <div>
            <a href="${backUrl}">Back</a>
        </div>
        <div class="nav">
            <span class="menuButton">
                <a class="home btn btn-primary" href="${createLinkTo(dir: '')}">
                    <g:message code="default.home.label" default="Home" />
                </a>
            </span>
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
            <g:form method="post" >
                <g:render template="form" model="[actionInstance: actionInstance, alertId: alertId]" />
            </g:form>
        </div>
    </rx:container>
</html>
