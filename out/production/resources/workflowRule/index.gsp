<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils; com.rxlogix.config.WorkflowRule" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <title><g:message code="workflowRule.list"/></title>
    <g:javascript>
        var workflowListUrl = "${createLink(controller: 'workflowRule', action: 'list')}";
        var createUrl = "${createLink(controller: 'workflowRule', action: 'create')}";
        var isAdmin = ${SpringSecurityUtils.ifAnyGranted("ROLE_CONFIGURATION_CRUD")};
    </g:javascript>
    <asset:javascript src="app/pvs/wokflowRule/workflow_rule.js"/>
     <style>
            table.dataTable td {
                word-break: break-word !important;
            }
        </style>
</head>

<body>
<rx:container title="Workflow Rule List">
    <g:render template="/includes/layout/flashErrorsDivs" bean="${workflowRuleInstanceTotal}" var="theInstance"/>
    <div class="row">
        <div class="col-lg-12">
            <table id="worflowRuleTable" class="row-border hover simple-alert-table" width="100%">
                <thead>
                <tr>
                    <th><g:message code="workflowRule.name"/></th>
                    <th><g:message code="workflowRule.description"/></th>
                    <th><g:message code="workflowRule.incomingDisposition"/></th>
                    <th><g:message code="workflowRule.targetDisposition"/></th>
                    <th>Workflow Groups</th>
                    <th>Allowed Groups</th>
                    <th><g:message code="workflowRule.display"/></th>
                </tr>
                </thead>
            </table>
        </div>
    </div>
</rx:container>
</body>
</html>
