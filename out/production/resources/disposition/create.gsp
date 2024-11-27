
<%@ page import="com.rxlogix.config.Disposition" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title><g:message code="default.create.label" args="['Disposition']" default="Create Disposition" /></title>
    </head>
    <rx:container title="Create Disposition">
        <g:render template="/includes/layout/flashErrorsDivs" bean="${dispositionInstance}" var="theInstance"/>
        <div class="nav bord-bt">
            <span class="menuButton"><g:link class="btn btn-primary" action="list"><g:message code="default.list.label" args="['Disposition']" default="Disposition List" /></g:link></span>
        </div>
        <div class="body">
            <g:form action="save" method="post" >
                <g:render template="form" bean="${dispositionInstance}" />
            </g:form>
        </div>
    </rx:container>
</html>
