<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.BusinessConfiguration.title"/></title>
    <asset:javascript src="app/pvs/businessConfiguration/businessConfigurationListing.js"/>
    <asset:javascript src="app/pvs/dataTablesActionButtons.js"/>
    <asset:javascript src="app/pvs/bootbox.min.js"/>
    <asset:javascript src="jquery-ui/jquery-ui.min.js"/>
    <g:javascript>
        var listQueriesUrl = "${createLink(controller: 'businessConfiguration', action: 'list')}";
        var businessConfigurationEditUrl = "${createLink(controller: 'businessConfiguration', action: 'edit')}";
        var runUrl = "${createLink(controller: 'configuration', action: 'create')}";
        var businessConfigurationCreateUrl = "${createLink(controller: 'businessConfiguration', action: 'create')}";
        var fetchRulesUrl = "${createLink(controller: 'businessConfiguration', action: 'fetchRules')}";
        var addRuleUrl = "${createLink(controller: 'businessConfiguration', action: 'createRule')}";
        var deleteUrl = "${createLink(controller: 'businessConfiguration', action: 'delete')}";
        var editRuleUrl = "${createLink(controller: 'businessConfiguration', action: 'editRule')}";
        var deleteRuleUrl = "${createLink(controller: 'businessConfiguration', action: 'deleteRule')}";
        var cloneRuleUrl = "${createLink(controller: 'businessConfiguration', action: 'cloneRule')}";
        var toggleEnableRuleUrl = "${createLink(controller: 'businessConfiguration', action: 'toggleEnableRule')}";
        var toggleEnableBCUrl = "${createLink(controller: 'businessConfiguration', action: 'toggleEnableBusinessConfiguration')}";
        var isAdmin = ${grails.plugin.springsecurity.SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_CONFIGURATION_CRUD")};

    </g:javascript>

    <style type="text/css">
    td.details-control:after {
        font-family: "Glyphicons Halflings";
        content: "\2b";
        cursor: pointer;
    }

    tr.shown td.details-control:after {
        font-family: "Glyphicons Halflings";
        content: "\2212";
    }
    </style>
</head>

<body>
<rx:container title="Business Rule List" options="${true}" bean="${error}">

    <g:render template="/includes/layout/flashErrorsDivs"/>
    <div class="messageContainer"></div>

    <div>
        <table id="rxTableQueries" class="row-border hover" width="100%">
            <thead>
            <tr>
                <th></th>
                <th><g:message code="app.label.business.configuration.ruleName"/></th>
                <th><g:message code="app.label.description"/></th>
                <th><g:message code="app.label.product"/></th>
                <th><g:message code="app.label.lastUpdatedBy"/></th>
                <th><g:message code="app.label.dateModified"/></th>
                <th><g:message code="app.label.status"/></th>
                <sec:ifAnyGranted roles="ROLE_CONFIGURATION_CRUD">
                    <th style="width: 30px;"><g:message code="app.label.action"/></th>
                </sec:ifAnyGranted>
            </tr>
            </thead>
        </table>
    </div>
</rx:container>
<g:render template="/includes/modals/rule_order_modal"/>
</body>