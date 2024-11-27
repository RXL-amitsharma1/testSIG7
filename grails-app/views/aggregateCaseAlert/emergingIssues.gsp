<%@ page contentType="text/html;charset=UTF-8" import="com.rxlogix.enums.ReportFormat; com.rxlogix.config.PVSState" %>
<head>
    <meta name="layout" content="main"/>
    <title>Alert Details</title>
    <asset:javascript src="app/pvs/productEventHistory/productEventHistoryTable.js"/>
    <asset:javascript src="app/pvs/alerts_review/agg_case_alert_details.js"/>
    <asset:javascript src="app/pvs/dataTablesActionButtons.js"/>
    <g:javascript>
        var callingScreen = "${callingScreen}";
        var groupBySmq = "${groupBySmq}";
        var executedConfigId = "${executedConfigId}";
        var dataUrl = "${createLink(controller: 'aggregateCaseAlert', action: 'listEmergingIssues', params: [id: executedConfigId , callingScreen:callingScreen])}";
        var pvrIntegrate = "${grailsApplication.config.pvreports.url? true : false}";
        var productEventHistoryUrl = "${createLink(controller: "productEventHistory", action: 'listProductEventHistory')}";
        var template_list_url = "${createLink(controller: 'template', action: 'index')}";

        var caseDetailUrl = "${createLink(controller: "caseInfo", action: 'caseDetail')}";
        var getWorkflowUrl = "${createLink(controller: "workflow", action: 'getWorkflowState')}";
        var getPriorityUrl = "${createLink(controller: "workflow", action: 'getPriority')}";
        var changePriorityUrl = "${createLink(controller: "aggregateCaseAlert", action: 'changePriority')}";
        var changeAssignedToUrl = "${createLink(controller: "aggregateCaseAlert", action: 'changeAssignedTo')}";
        var topicUrl = "${createLink(controller: "topic", action: 'addAlertToTopic', params: [id : ""])}";
        var searchStrategyProducts = "${createLink(controller: 'validatedSignal', action: 'searchStrategyProducts')}";
    </g:javascript>
    <g:render template="/includes/widgets/actions/action_types" />
</head>

<body>
<input type="hidden" id="businessRules" value="${aggregateRules}"/>
<div class="pv-tab">
    <!-- Nav tabs -->
    <ul id="detail-tabs" class="nav nav-tabs rxmain-container-header-label" role="tablist">
        <li role="presentation" class="active" >
            <a href="#details"  aria-controls="details" role="tab" data-toggle="tab">Alert Details</a>
        </li>
    </ul>
    <g:render template="/includes/layout/flashErrorsDivs" bean="${executedConfigId}" var="theInstance"/>

    <!-- Tab panes -->
    <div class="tab-content">
        <div role="tabpanel" class="tab-pane active" id="details">
            <g:render template="includes/details_tab"  model="[id:executedConfigId, name: name]"/>
        </div>
    </div>
</div>
<g:render template="/includes/modals/actionCreateModal" model="[id:executedConfigId, appType: 'Aggregate Case Alert']" />
<g:render template="/includes/modals/action_list_modal" />
<g:render template="/includes/modals/product_event_history_modal" />
<g:render template="/includes/modals/alert_comment_modal" />
<g:hiddenField id='showPrr' name='showPrr' value="${showPrr}"/>
<g:hiddenField id='showRor' name='showRor' value="${showRor}"/>
<g:hiddenField id='showEbgm' name='showEbgm' value="${showEbgm}"/>
<input type="hidden" value="${signals}" id="signals" />

</body>
</html>