<%@ page import="com.rxlogix.enums.ReportFormat; com.rxlogix.Constants" %>
<g:set var="grailsApplication" bean="grailsApplication"/>
<g:set var="userGroupService" bean="userGroupService"/>
<g:set var="userService" bean="userService"/>
<g:set var="hideMenu" value="${userGroupService.fetchUserListForGroup( userService.getUser(), com.rxlogix.enums.GroupType.WORKFLOW_GROUP )?.size() > 0?false:true}" />
<html>
<head>
    <meta name="layout" content="main"/>
    <meta http-equiv='cache-control' content='no-cache'>
    <meta http-equiv='expires' content='0'>
    <meta http-equiv='pragma' content='no-cache'>
    <title><g:message code="app.label.dashboard"/></title>

    <g:javascript>
            var signalListUrl = "${createLink(controller: 'dashboard', action: 'signalList')}";
            var preCheckListUrl = "${createLink(controller: 'dashboard', action: 'preCheckList')}";
            var eventsUrl = "${createLink(controller: "calendar", action: "events")}";
            var topicListUrl = "${createLink(controller: 'dashboard', action: 'topicList')}";
            var alertUrl =  "${createLink(controller: 'dashboard', action: 'alertList')}";
            var actionListUrl = "${createLink(controller: 'dashboard', action: 'actionList')}";
            var aggDataUrl = "${createLink(controller: 'aggregateCaseAlert', action: 'details')}";
            var aggAdhocDataUrl = "${createLink(controller: 'aggregateOnDemandAlert', action: 'adhocDetails')}";
            var singleDataUrl = "${createLink(controller: 'singleCaseAlert', action: 'details')}";
            var singleAdhocDataUrl = "${createLink(controller: 'singleOnDemandAlert', action: 'adhocDetails')}";
            var evdasDataUrl = "${createLink(controller: 'evdasAlert', action: 'details')}";
            var evdasAdhocDataUrl = "${createLink(controller: 'evdasOnDemandAlert', action: 'adhocDetails')}";
            var litDataUrl = "${createLink(controller: 'literatureAlert', action: 'details')}";
            var deleteTriggeredAlert = "${createLink(controller: 'dashboard', action: 'deleteTriggeredAlert')}";
            var dashboardWidgetConfigJSON = "${dashboardWidgetConfigJSON}";
            var showWidget = "${showWidget}";
            var referenceTypes = "${referenceTypes}";
            var createdBy="${createdBy}";
            var userViewAccessMap = {
            "${Constants.AlertConfigType.SINGLE_CASE_ALERT}": ${userViewAccessMap.get(Constants.AlertConfigType.SINGLE_CASE_ALERT)},
            "${Constants.AlertConfigType.SIGNAL_MANAGEMENT}": ${userViewAccessMap.get(Constants.AlertConfigType.SIGNAL_MANAGEMENT)},
            "${Constants.AlertConfigType.LITERATURE_SEARCH_ALERT}": ${userViewAccessMap.get(Constants.AlertConfigType.LITERATURE_SEARCH_ALERT)},
            "${Constants.AlertConfigType.EVDAS_ALERT}": ${userViewAccessMap.get(Constants.AlertConfigType.EVDAS_ALERT)},
            "${Constants.AlertConfigType.AGGREGATE_CASE_ALERT}": ${userViewAccessMap.get(Constants.AlertConfigType.AGGREGATE_CASE_ALERT)},
            "${Constants.AlertConfigType.AD_HOC_ALERT}": ${userViewAccessMap.get(Constants.AlertConfigType.AD_HOC_ALERT)}
        }
    </g:javascript>
    <asset:javascript src="purify/purify.min.js" />
    <g:render template="/includes/widgets/actions/action_types"/>
    <asset:javascript src="app/pvs/actions/actions.js"/>
    <asset:javascript src="spring-websocket" />
    <asset:javascript src="app/pvs/dashboard/dashboardWidget.js"/>
    <asset:javascript src="app/pvs/dashboard/dashboard.js"/>
    <asset:javascript src="vendorUi/highcharts/modules/no-data-to-display.js"/>
    <asset:javascript src="vendorUi/fullcalendar.min.js"/>
    <asset:javascript src="vendorUi/gridstack/gridstack.min.js"/>
    <asset:javascript src="vendorUi/gridstack/gridstack.jQueryUI.min.js"/>
    <asset:stylesheet src="vendorUi/gridstack.min.css"/>
    <asset:stylesheet src="workflowModal.css"/>
    <asset:stylesheet src="dashboard.css"/>
    <asset:javascript src="app/pvs/date-time/bootstrap-datetimepicker.js"/>
    <asset:stylesheet src="app/pvs/bootstrap-datetimepicker.css"/>

    <asset:stylesheet src="vendorUi/fullcalendar.min.css"/>
    <asset:stylesheet src="vendorUi/fullcalendar.print.css" media="print"/>
    <asset:javascript src="app/bootstrap-modal-popover/bootstrap-modal-popover.js"/>
</head>

<body>
<g:if test="${hideMenu==false}">
<div class="pv-dashboard">
    <div class="row">
        <div class="col-sm-12">
            <div class="page-title-box">

                <div class="fixed-page-head">
                    <div class="page-head-lt">
                        <h5>Dashboard</h5>
                    </div>
                    <div class="pull-right nav-right page-head-rt text-right ic-md open">

                        <span>
                            <a href="#" class="pv-ic pv-dash-inbox-icon hide" title="Show My Inbox">
                                <i class="md md-inbox"></i>
                            </a>
                        </span>
                        <span id="pvi-dash-topMinWidget">
                            <a href="#" class="btn btn-no-bg right-bar-toggle" data-backdrop="true"
                               style="display: none;">
                                <i class="md md-add"></i>
                            </a>
                        </span>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="row" id="rx-widgets">
        <div id="inbox" class="sidebarDashboard col-sm-12 pv-dashWidget" style="display:none;">
            <div class="panel panel-default chart-panel">
                <div class="panel-heading panel-title pv-sec-heading">
                    <div class="rxmain-container-header-label">
                        <label>My Inbox</label>

                        <div class="pull-right">
                            <a tabindex="0" href="#" class="close-widget-inbox" title="Hide Widget"><i class="md md-close"></i></a>
                        </div>
                    </div>
                </div>

                <div class="panel-body pv-sec-bg">
                    <sec:ifAnyGranted roles="ROLE_SINGLE_CASE_CONFIGURATION, ROLE_SINGLE_CASE_REVIEWER, ROLE_SINGLE_CASE_VIEWER, ROLE_VIEW_ALL">

                            <div id="my-single" class="${grailsApplication.config.signal.evdas.enabled ? 'col-sm-2' : 'col-sm-2 width-20per'}">
                                <div class="widget-simple-chart text-center card-box pv-inner-shadow-box">
                                    <h5 class="text-muted"><g:message code="app.single.case.review"/></h5>

                                    <h1 class="text-blue p-t-0"><a
                                            href="/signal/singleCaseAlert/details?callingScreen=dashboard"
                                            onclick="return checkCount(this)" class="text-blue"><span class="counter">0</span></a></h1>
                                </div>
                            </div>

                    </sec:ifAnyGranted>
                    <sec:ifAnyGranted roles="ROLE_AGGREGATE_CASE_CONFIGURATION, ROLE_AGGREGATE_CASE_REVIEWER, ROLE_AGGREGATE_CASE_VIEWER, ROLE_VIEW_ALL, ROLE_FAERS_CONFIGURATION, ROLE_VAERS_CONFIGURATION, ROLE_VIGIBASE_CONFIGURATION">

                            <div id="my-aggregate" class="${grailsApplication.config.signal.evdas.enabled ? 'col-sm-2' : 'col-sm-2 width-20per'}">
                                <div class="widget-simple-chart text-center card-box pv-inner-shadow-box">
                                    <h5 class="text-muted"><g:message code="app.aggregated.case.review"/></h5>
                                    <h1 class="text-red p-t-0">
                                        <a href="/signal/aggregateCaseAlert/details?callingScreen=dashboard" onclick="return checkCount(this)" class="text-red">
                                            <span class="counter">0</span>
                                        </a>
                                    </h1>
                                </div>
                            </div>

                    </sec:ifAnyGranted>
                    <sec:ifAnyGranted roles="ROLE_AD_HOC_CRUD, ROLE_VIEW_ALL">
                            <div id="my-adhoc" class="${grailsApplication.config.signal.evdas.enabled ? 'col-sm-2' : 'col-sm-2 width-20per'}">
                                <div class="widget-simple-chart text-center card-box pv-inner-shadow-box">
                                    <h5 class="text-muted">Ad hoc Review</h5>

                                    <h1 class="text-success p-t-0"><a
                                            href="/signal/adHocAlert/index?callingScreen=dashboard"
                                            onclick="return checkCount(this)" class="text-success"><span class="counter">0</span></a></h1>
                                </div>
                            </div>

                    </sec:ifAnyGranted>

                    <g:if test="${grailsApplication.config.signal.evdas.enabled}">
                        <sec:ifAnyGranted roles="ROLE_EVDAS_CASE_CONFIGURATION, ROLE_EVDAS_CASE_REVIEWER, ROLE_EVDAS_CASE_VIEWER, ROLE_VIEW_ALL">
                            <div id="evdasReview" class="${grailsApplication.config.signal.evdas.enabled ? 'col-sm-2' : 'col-sm-2 width-20per'}">
                                <div class="widget-simple-chart text-center card-box pv-inner-shadow-box">
                                    <h5 class="text-muted">EVDAS Review</h5>

                                    <h1 class="text-pink p-t-0"><a
                                            href="/signal/evdasAlert/details?callingScreen=dashboard"
                                            onclick="return checkCount(this)" class="text-pink"><span
                                                class="counter">0</span></a></h1>
                                </div>
                            </div>
                        </sec:ifAnyGranted>
                    </g:if>
                    <sec:ifAnyGranted roles="ROLE_SIGNAL_MANAGEMENT_CONFIGURATION, ROLE_SIGNAL_MANAGEMENT_REVIEWER, ROLE_SIGNAL_MANAGEMENT_VIEWER, ROLE_VIEW_ALL">

                        <div id="signals" class="${grailsApplication.config.signal.evdas.enabled ? 'col-sm-2' : 'col-sm-2 width-20per'}">
                            <div class="widget-simple-chart text-center card-box pv-inner-shadow-box">
                                <h5 class="text-muted">Signals</h5>

                                <h1 class="text-purple p-t-0"><a
                                        href="/signal/validatedSignal/index?callingScreen=dashboard"
                                        onclick="return checkCount(this)" class="text-purple"><span
                                            class="counter">0</span></a></h1>
                            </div>
                        </div>

                    </sec:ifAnyGranted>
                    <div id="actionItems" class="${grailsApplication.config.signal.evdas.enabled ? 'col-sm-2' : 'col-sm-2 width-20per'}">
                        <div class="widget-simple-chart text-center card-box pv-inner-shadow-box">
                            <h5 class="text-muted">Action Items</h5>

                            <h1 class="text-orange p-t-0"><a href="/signal/action/list" class="text-orange" onclick="return checkCount(this)"><span
                                    class="counter">0</span></a></h1>
                        </div>
                    </div>


                </div>
            </div>
        </div>
        <div id="mainDashboard" class="mainDashboard col-sm-12 grid-stack">
        </div>
    </div>
    <g:render template="/validatedSignal/includes/extendedTextarea"/>
    %{--<g:render template="/validatedSignal/includes/referencesAttachmentModal"/>--}%
    <g:render template="/dashboard/includes/referencesLinkModal"/>
</div>
</g:if>
<g:else>
    <div class="text-center pv-error-page-box">
        <h1><i class="fa fa-exclamation-triangle text-gray-silver"></i></h1>

        <p align="center"
           style="font-size: 18px; padding-right: 200px; padding-left: 200px; padding-bottom: -10px;">You are not currently assigned to any workflow group.<br>
            Please contact your administrator to get assigned to a workflow group.
        </p>
    </div>
</g:else>

<g:render template="includes/widget_config"/>
<g:render template="/includes/widgets/actions/action_types" />
<g:render template="/includes/modals/action_edit_modal" />
<g:render template="/includes/modals/actionCreateModal"
          model="[actionConfigList: actionConfigList, actionTypeList: actionTypeList]"/>
<g:render template="/includes/modals/sharedWithModal"/>
<g:render template="/includes/modals/deleteReferences"/>
<asset:javascript src="app/pvs/calendar.js"/>
<g:render template="/includes/modals/meetingMinutesModal" model="[appType: 'Signal Management', userList: userList]" />
<g:if test="${flash.outlookMessage}">
    <g:javascript>
        swal("","${flash.outlookMessage}","error");
    </g:javascript>
</g:if>
</body>
</html>