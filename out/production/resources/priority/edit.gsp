<%@ page import="com.rxlogix.config.Priority" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <title><g:message code="priority.edit" default="Edit Priority"/></title>
    <g:javascript>
            var dispositionListUrl = "${createLink(controller: 'disposition', action: 'fetchDispositionsList')}";
            var isAdmin = ${grails.plugin.springsecurity.SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_CONFIGURATION_CRUD")};
    </g:javascript>
    <asset:javascript src="app/pvs/configuration/priority.js"/>
</head>
<body>
<rx:container title="Edit Priority">
    <g:if test="${flash.error}">
        <div class="alert alert-danger alert-dismissible" role="alert" style="word-break: break-all">
            <button type="button" class="close" data-dismiss="alert">
                <span  onclick="this.parentNode.parentNode.remove(); return false;">x</span>
                <span class="sr-only"><g:message code="default.button.close.label" /></span>
            </button>
            <g:if test="${flash.error}">
                <g:if test="${flash.error.contains('<linkQuery>')}">
                    ${flash.error.substring(0, flash.error.indexOf('<linkQuery>'))}
                    <a href="${flash.error.substring(flash.error.indexOf('<linkQuery>') + 11)}"><g:message code="see.details" /></a>
                </g:if>
                <g:else>
                    ${raw(flash.error)}
                </g:else>
            </g:if>
        </div>
    </g:if>
    <div class="nav bord-bt">
        <span class="menuButton"><g:link class="list btn btn-primary" action="list"><g:message code="priority.list"
                                                                                               default="Priority List"/></g:link></span>
        <sec:ifAnyGranted roles="ROLE_CONFIGURATION_CRUD">
            <span class="menuButton"><g:link class="create btn btn-primary" action="create"><g:message
                    code="priority.new" default="New Priority"/></g:link></span>
            <span class="menuButton create btn btn-primary" id="reviewPeriodButton"><g:message code="priority.review.period" default="Review Period"/></span>
        </sec:ifAnyGranted>
    </div>

    <div class="body">
        <g:form method="post" name="priorityForm">
            <g:render template="form" model="[priorityInstance: priorityInstance, edit: true , dispositionConfigs : dispositionConfigs]"/>
        </g:form>
    </div>
</rx:container>
</body>
</html>
