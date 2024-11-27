<%@ page import="grails.util.Holders; grails.plugin.springsecurity.SpringSecurityUtils; com.rxlogix.enums.TemplateTypeEnum; com.rxlogix.user.User" %>
<g:set var="userService" bean="userService"/>

    <g:set var="userGroupService" bean="userGroupService"/>
<g:set var="grailsApplication" bean="grailsApplication"/>

    <g:set var="hideMenu" value="${session.hideMenu == undefined ? (userGroupService.fetchUserListForGroup( userService.getUser(), com.rxlogix.enums.GroupType.WORKFLOW_GROUP )?.size() > 0?false:true) : session.hideMenu}" />
<sec:ifLoggedIn>
    <asset:javascript src="spring-websocket" />
    <asset:javascript src="app/pvs/pushNotification.js"/>
    <g:render template="/sessionTimeout/sessiontimeout"/>
</sec:ifLoggedIn>
<g:javascript>
function clearAndSetDefaultValueInSessionStorage() {
    sessionStorage.removeItem("signalDashboard");
    sessionStorage.removeItem("datatable_audit");
    sessionStorage.setItem("alertDashboard","");
}
    function logoutRxSession() {
        var request = new XMLHttpRequest();
        request.open('GET', "${Holders.config.spotfire.logoutUrl}", true);
        request.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
        request.send();
        clearFormInputsChangeFlag($(document));
        clearAndSetDefaultValueInSessionStorage();
        $.fn.idleTimeout().logout();
}
function helpUrl(){
    if(helpUrlLink === ''){
    $.Notification.notify('warning', 'top right', "Warning", "It appears that a Help URL has not been configured at the moment.", {autoHideDelay: 5000});
    }
    else{
    $('#helpUrl').attr('target', '_blank');
    $('#helpUrl').attr('href', helpUrlLink);
    }
}
    var SIGNAL_CREATION= 'Signal Creation';
    var helpUrlLink = '${Holders.config.helpUrl}';
    var dataAnalysisRiskURL = "${createLink(controller: 'dataAnalysis', action: 'view')}";
    var notificationURL = "${createLink(controller: 'inboxLog', action: 'forUser', params: [id:userService.getUser()?.id])}";
    var notificationDeleteURL = "${createLink(controller: 'inboxLog', action: 'deleteNotificationById')}";
    var notificationMarkAsReadURL = "${createLink(controller: 'inboxLog', action: 'markAsRead')}";
    var notificationDeleteByUserURL = "${createLink(controller: 'inboxLog', action: 'deleteNotificationsForUserId')}";
    var notificationMarkAsReadByUserURL = "${createLink(controller: 'inboxLog', action: 'markAsReadNotificationsForUserId')}";
    var notificationDashboardURL = "${createLink(controller: 'inboxLog', action: 'index')}";
    var notificationChannel ="${user?.notificationChannel}";
    var mappingEnabled='${Holders.config.disposition.signal.outcome.mapping.enabled}'
    var defaultValidatedDate='${Holders.config.signal.defaultValidatedDate}'
    var notificationWSURL = "${createLink(uri: "") + user?.notificationChannel}";
    var reportRedirectURL = "${createLink(controller: 'report', action: 'criteria')}";
    var filterDeleteUrl = "${createLink(controller: 'advancedFilter', action: 'delete')}";
    var mappingEnabled='${Holders.config.disposition.signal.outcome.mapping.enabled}'
    var caseSeriesURL =  "${createLink(controller: 'singleCaseAlert', action: 'caseSeriesDetails')}";
    var detailUrls = {};
    detailUrls["sca_reportRedirectURL"] =  "${createLink(controller: 'singleCaseAlert', action: 'details')}";
    detailUrls["sca_adhoc_reportRedirectURL"] =  "${createLink(controller: 'singleOnDemandAlert', action: 'adhocDetails')}";
    detailUrls["aga_reportRedirectURL"] = "${createLink(controller: 'aggregateCaseAlert', action: 'details')}";
    detailUrls["aga_adhoc_reportRedirectURL"] = "${createLink(controller: 'aggregateOnDemandAlert', action: 'adhocDetails')}";
    detailUrls["evdas_reportRedirectURL"] = "${createLink(controller: 'evdasAlert', action: 'details')}";
    detailUrls["evdas_adhoc_reportRedirectURL"] = "${createLink(controller: 'evdasOnDemandAlert', action: 'adhocDetails')}";
    detailUrls["LITERATURE_reportRedirectURL"] = "${createLink(controller: 'literatureAlert', action: 'details')}";
    detailUrls["error_url"]  = "${createLink(controller: 'configuration', action: 'executionStatus')}";
    detailUrls["validatedSignalRedirectURL"] = "${createLink(controller: 'validatedSignal', action: 'details')}"
    detailUrls["SIGNAL_CREATION"] = "${createLink(controller: 'validatedSignal', action: 'details')}";
    var socketURL = "${createLink(uri: '/stomp')}";

    $(document).ready(function () {
        var isAdmin = ${grails.plugin.springsecurity.SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_CONFIGURATION_VIEW")};
        if(!isAdmin) {
           $('.mega-dropdown-menu').width("100px");
        }

    });

    $(document).on('click','.temp-redirect',function() {
        window.location.href = notificationDashboardURL;
    });
      $(document).on('mouseover','.riskURL',function() {
       var  riskSummeryReportURL = "${grailsApplication.config.spotfire.riskSummaryReport.url}";
       var url=dataAnalysisRiskURL+ "?fileName="+window.encodeURIComponent(riskSummeryReportURL);
       $(this).attr('href',url)
    });
    $(document).on('click','.riskURL',function() {
        var  riskSummeryReportURL = "${grailsApplication.config.spotfire.riskSummaryReport.url}";
        window.location.href = dataAnalysisRiskURL+ "?fileName="+window.encodeURIComponent(riskSummeryReportURL);
    });
    var apiControlPanelURL = "${createLink(controller: 'apiControlPanel', action: 'index')}";
    $(document).on('click','apiControlPannel-redirect',function() {
        window.location.href = apiControlPanelURL;
    });
</g:javascript>
<div class="topbar" style="z-index: 1000 !important;">
    <!-- LOGO -->
    <div class="pull-left">
        <button class="button-menu-mobile open-left waves-effect" accesskey="b" data-toggle="tooltip" title="Main Menu">
            <g:if test="${hideMenu==false}"><i class="md md-menu"></i></g:if>
        </button>
        <span class="clearfix"></span>
    </div>
    <div class="left side-menu">
        <div class="sidebar-inner slimscrollleft">
            <div id="sidebar-menu">

                <g:if test="${hideMenu==false}">
<ul>

                 <sec:ifAnyGranted
        roles="ROLE_AD_HOC_CRUD, ROLE_SINGLE_CASE_CONFIGURATION, ROLE_SINGLE_CASE_REVIEWER, ROLE_SINGLE_CASE_VIEWER,
        ROLE_AGGREGATE_CASE_CONFIGURATION, ROLE_AGGREGATE_CASE_REVIEWER, ROLE_AGGREGATE_CASE_VIEWER,
        ROLE_LITERATURE_CASE_CONFIGURATION, ROLE_LITERATURE_CASE_REVIEWER, ROLE_LITERATURE_CASE_VIEWER,
        ROLE_EVDAS_CASE_CONFIGURATION, ROLE_EVDAS_CASE_REVIEWER, ROLE_EVDAS_CASE_VIEWER, ROLE_VIEW_ALL, ROLE_FAERS_CONFIGURATION, ROLE_VAERS_CONFIGURATION, ROLE_VIGIBASE_CONFIGURATION,ROLE_JADER_CONFIGURATION
        ">
                        <li class="has_sub">
                            <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                                <i class="md md-report-problem"></i><span><g:message
        code="app.label.alert"/></span> <span class="menu-arrow"></span>
                            </a>
                            <ul class="list-unstyled">
                                <sec:ifAnyGranted roles="ROLE_SINGLE_CASE_CONFIGURATION, ROLE_SINGLE_CASE_REVIEWER, ROLE_SINGLE_CASE_VIEWER, ROLE_VIEW_ALL">
                                    <li><g:link controller="singleCaseAlert" action="review"><g:message
        code="app.single.case.review"/></g:link></li>
                                </sec:ifAnyGranted>
                                <sec:ifAnyGranted roles="ROLE_AGGREGATE_CASE_CONFIGURATION, ROLE_AGGREGATE_CASE_REVIEWER, ROLE_AGGREGATE_CASE_VIEWER, ROLE_VIEW_ALL, ROLE_FAERS_CONFIGURATION, ROLE_VAERS_CONFIGURATION, ROLE_VIGIBASE_CONFIGURATION,ROLE_JADER_CONFIGURATION">
                                    <li><g:link controller="aggregateCaseAlert" action="review"><g:message
        code="app.aggregated.case.review"/></g:link></li>
                                </sec:ifAnyGranted>

                                <g:if test="${grailsApplication.config.signal.evdas.enabled}">
                                    <sec:ifAnyGranted roles="ROLE_EVDAS_CASE_CONFIGURATION, ROLE_EVDAS_CASE_REVIEWER, ROLE_EVDAS_CASE_VIEWER, ROLE_VIEW_ALL">
                                        <li><g:link controller="evdasAlert" action="review"><g:message
        code="app.evdas.review"/></g:link></li>
                                    </sec:ifAnyGranted>
                                </g:if>
                                <sec:ifAnyGranted roles="ROLE_AD_HOC_CRUD, ROLE_VIEW_ALL">
                                    <li><g:link controller="adHocAlert" action="index"><g:message
        code="ad.hoc.review"/></g:link></li>
                                </sec:ifAnyGranted>
                                <sec:ifAnyGranted roles="ROLE_LITERATURE_CASE_CONFIGURATION, ROLE_LITERATURE_CASE_REVIEWER, ROLE_LITERATURE_CASE_VIEWER, ROLE_VIEW_ALL">
                                    <li>
                                        <g:link controller="literatureAlert" action="review">
                                            <g:message code="app.new.literature.review" />
                                        </g:link>
                                    </li>
                                </sec:ifAnyGranted>
                            </ul>
                        </li>
                    </sec:ifAnyGranted>

                      <sec:ifAnyGranted roles="ROLE_SIGNAL_MANAGEMENT_CONFIGURATION, ROLE_SIGNAL_MANAGEMENT_REVIEWER, ROLE_SIGNAL_MANAGEMENT_VIEWER, ROLE_VIEW_ALL">
                        <li class="has_sub">
                            <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                                <i class="md md-call-merge"></i><span><g:message
        code="app.menu.signal"/><span class="menu-arrow"></span></span>
                            </a>
                            <ul class="list-unstyled">
                                <li>
                                    <g:link controller="validatedSignal" action="index"
        class="waves-effect waves-primary">
                                        <g:message code="app.signal.summary"/>
                                    </g:link>
                                </li>
                                <g:if test="${grailsApplication.config.signalManagement.productSummary.enabled}">
                                    <li>
                                        <g:link controller="productSummary" action="index">
                                            <g:message code="app.product.summary"/>
                                        </g:link>
                                    </li>
                                </g:if>
                                <sec:ifAnyGranted roles="ROLE_DATA_ANALYSIS, ROLE_OPERATIONAL_METRICS, ROLE_PRODUCTIVITY_AND_COMPLIANCE">
                                <g:if test="${grailsApplication.config.signal.spotfire.enabled && grailsApplication.config.spotfire.riskSummaryReport.url}">
                                    <li>
                                        <a href="javascript:void(0);" class="riskURL">
                                       <g:message code="app.spotfire.label.riskSummary"/></a>
                                    </li>
                                </g:if>
                                </sec:ifAnyGranted>
                            </ul>
                        </li>
                    </sec:ifAnyGranted>

                        <sec:ifAnyGranted roles="ROLE_DATA_ANALYSIS, ROLE_OPERATIONAL_METRICS, ROLE_REPORTING, ROLE_PRODUCTIVITY_AND_COMPLIANCE">
                            <li class="has_sub">
                                <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                                    <i class="md md-trending-up"></i><span><g:message
        code="app.label.analysis"/></span> <span class="menu-arrow"></span>
                                </a>
                                <ul class="list-unstyled">
                                    <sec:ifAnyGranted roles="ROLE_REPORTING">
                                        <li>
                                            <g:link controller="report" >
                                                <g:message code="app.label.report.label" default="Reporting"/>
                                            </g:link>
                                        </li>
                                    </sec:ifAnyGranted>
                                    <g:if test="${grailsApplication.config.signal.spotfire.enabled}">
                                    <sec:ifAnyGranted roles="ROLE_DATA_ANALYSIS, ROLE_OPERATIONAL_METRICS, ROLE_REPORTING, ROLE_PRODUCTIVITY_AND_COMPLIANCE">
                                    <sec:ifAnyGranted roles="ROLE_DATA_ANALYSIS">
                                        <li>
                                            <g:link controller="dataAnalysis" action="index">
                                                <g:message code="app.viewSpotfireFiles.menu"/>
                                            </g:link>
                                        </li>
                                        <li class="divider"></li>
                                        <li><a href="${grailsApplication.config.pvreports.web.url}/reports/caseSeries/create" target="_blank"><g:message
        code="app.newSpotfireFile.menu"/></a></li>
                                    </sec:ifAnyGranted>
                                    <li class="divider"></li>
                                    <sec:ifAnyGranted roles="ROLE_OPERATIONAL_METRICS">
                                        <g:if test="${grailsApplication.config.spotfire.operationalReport.url}">
                                            <li><a href="${createLink(controller: 'dataAnalysis', action: 'view')}?fileName=${grailsApplication.config.spotfire.operationalReport.url}">
                                                <g:message code="app.spotfire.label.operational.report"/></a></li>
                                        </g:if>
                                    </sec:ifAnyGranted>
                                    <sec:ifAnyGranted roles="ROLE_PRODUCTIVITY_AND_COMPLIANCE">
                                        <g:if test="${grailsApplication.config.spotfire.productivityAndComplianceReport.url}">
                                            <li><a href="${createLink(controller: 'dataAnalysis', action: 'view')}?fileName=${grailsApplication.config.spotfire.productivityAndComplianceReport.url}">
                                                <g:message code="app.spotfire.productivity.compliance.report"/></a></li>
                                        </g:if>
                                    </sec:ifAnyGranted>
                                    </sec:ifAnyGranted>
                                    </g:if>
                                </ul>
                            </li>
                        </sec:ifAnyGranted>

                    <sec:ifAnyGranted
        roles="ROLE_SINGLE_CASE_CONFIGURATION, ROLE_SINGLE_CASE_REVIEWER, ROLE_SINGLE_CASE_VIEWER,
         ROLE_AGGREGATE_CASE_CONFIGURATION, ROLE_AGGREGATE_CASE_REVIEWER, ROLE_AGGREGATE_CASE_VIEWER,
         ROLE_EVDAS_CASE_CONFIGURATION, ROLE_EVDAS_CASE_REVIEWER, ROLE_EVDAS_CASE_VIEWER, ROLE_VIEW_ALL, ROLE_FAERS_CONFIGURATION, ROLE_VAERS_CONFIGURATION, ROLE_VIGIBASE_CONFIGURATION,ROLE_JADER_CONFIGURATION
         ">
                        <li class="has_sub">
                            <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                                <i class="md md-grid-on"></i>
                                <span><g:message code="app.label.adhoc"/></span> <span class="menu-arrow"></span>
                            </a>
                            <ul class="list-unstyled">
                                <sec:ifAnyGranted roles="ROLE_SINGLE_CASE_CONFIGURATION, ROLE_SINGLE_CASE_REVIEWER, ROLE_SINGLE_CASE_VIEWER, ROLE_VIEW_ALL">
                                    <li><g:link controller="singleOnDemandAlert" action="adhocReview"><g:message
        code="app.single.case.review"/></g:link></li>
                                </sec:ifAnyGranted>
                                <sec:ifAnyGranted roles="ROLE_AGGREGATE_CASE_CONFIGURATION, ROLE_AGGREGATE_CASE_REVIEWER, ROLE_AGGREGATE_CASE_VIEWER, ROLE_VIEW_ALL, ROLE_FAERS_CONFIGURATION, ROLE_VAERS_CONFIGURATION, ROLE_VIGIBASE_CONFIGURATION,ROLE_JADER_CONFIGURATION">
                                    <li><g:link controller="aggregateOnDemandAlert" action="adhocReview"><g:message
        code="app.aggregated.case.review"/></g:link></li>
                                </sec:ifAnyGranted>
                                <g:if test="${grailsApplication.config.signal.evdas.enabled}">
                                    <sec:ifAnyGranted roles="ROLE_EVDAS_CASE_CONFIGURATION, ROLE_EVDAS_CASE_REVIEWER, ROLE_EVDAS_CASE_VIEWER, ROLE_VIEW_ALL">
                                        <li>
                                            <g:link controller="evdasOnDemandAlert" action="adhocReview">
                                                <g:message code="app.evdas.review"/>
                                            </g:link>
                                        </li>
                                    </sec:ifAnyGranted>
                                </g:if>
                            </ul>
                        </li>
                    </sec:ifAnyGranted>

                    <sec:ifAnyGranted
        roles="ROLE_AD_HOC_CRUD, ROLE_SINGLE_CASE_CONFIGURATION, ROLE_AGGREGATE_CASE_CONFIGURATION,
                                        ROLE_EVDAS_CASE_CONFIGURATION, ROLE_LITERATURE_CASE_CONFIGURATION, ROLE_FAERS_CONFIGURATION, ROLE_VAERS_CONFIGURATION, ROLE_VIGIBASE_CONFIGURATION,ROLE_JADER_CONFIGURATION">
                        <li class="has_sub">
                            <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                                <i class="md md-settings-applications"></i><span><g:message
        code="app.label.alert.setup"/></span> <span class="menu-arrow"></span>
                            </a>
                            <ul class="list-unstyled">

                            <!-- Single Case Creation menu -->
                                <sec:ifAnyGranted roles="ROLE_SINGLE_CASE_CONFIGURATION">
                                    <sec:ifAnyGranted roles="ROLE_SINGLE_CASE_CONFIGURATION, ROLE_AGGREGATE_CASE_CONFIGURATION,
                                    ROLE_EVDAS_CASE_CONFIGURATION, ROLE_LITERATURE_CASE_CONFIGURATION, ROLE_FAERS_CONFIGURATION, ROLE_VAERS_CONFIGURATION, ROLE_VIGIBASE_CONFIGURATION,ROLE_JADER_CONFIGURATION">
                                        <li class="divider"></li>
                                    </sec:ifAnyGranted>
                                    <li><g:link controller="singleCaseAlert" action="create">
                                        <g:message code="app.new.individual.case.configuration"/></g:link></li>
                                </sec:ifAnyGranted>
                            <!-- Aggregate Creation menu -->
                                <sec:ifAnyGranted roles="ROLE_AGGREGATE_CASE_CONFIGURATION, ROLE_FAERS_CONFIGURATION, ROLE_VAERS_CONFIGURATION, ROLE_VIGIBASE_CONFIGURATION,ROLE_JADER_CONFIGURATION">
                                    <li><g:link controller="aggregateCaseAlert" action="create">
                                        <g:message code="app.new.aggregate.case.alert"/></g:link></li>
                                </sec:ifAnyGranted>
                            <!-- Evdas Creation menu -->
                                <g:if test="${grailsApplication.config.signal.evdas.enabled}">
                                    <sec:ifAnyGranted roles="ROLE_EVDAS_CASE_CONFIGURATION">

                                        <li><g:link controller="evdasAlert" action="create">
                                            <g:message code="app.label.evdas.configuration"/></g:link>
                                        </li>
                                    </sec:ifAnyGranted>
                                </g:if>
                            <!-- Adhoc Creation menu -->
                                <sec:ifAnyGranted roles="ROLE_AD_HOC_CRUD">
                                    <li><g:link controller="adHocAlert" action="create">
                                        <g:message code="app.label.adhoc.review"/></g:link></li>
                                </sec:ifAnyGranted>

                            <!-- Literature Alert Creation menu -->
                                    <sec:ifAnyGranted roles="ROLE_LITERATURE_CASE_CONFIGURATION">
                                        <li>
                                            <g:link controller="literatureAlert" action="create">
                                                <g:message code="app.new.literature.search.alert" />
                                            </g:link>
                                        </li>
                                    </sec:ifAnyGranted>

                            <!-- View alert menu option -->
                                <sec:ifAnyGranted
        roles="ROLE_SINGLE_CASE_CONFIGURATION, ROLE_AGGREGATE_CASE_CONFIGURATION, ROLE_EVDAS_CASE_CONFIGURATION,
                                        ROLE_LITERATURE_CASE_CONFIGURATION, ROLE_FAERS_CONFIGURATION, ROLE_VAERS_CONFIGURATION, ROLE_VIGIBASE_CONFIGURATION,ROLE_JADER_CONFIGURATION">
                                    <li><g:link controller="configuration" action="index">
                                        <g:message code="app.view.alerts"/></g:link></li>
                                </sec:ifAnyGranted>

                            <!-- View execution status menu option -->
                                <sec:ifAnyGranted
        roles="ROLE_SINGLE_CASE_CONFIGURATION, ROLE_AGGREGATE_CASE_CONFIGURATION,
                                        ROLE_EVDAS_CASE_CONFIGURATION, ROLE_LITERATURE_CASE_CONFIGURATION, ROLE_FAERS_CONFIGURATION, ROLE_VAERS_CONFIGURATION, ROLE_VIGIBASE_CONFIGURATION,ROLE_JADER_CONFIGURATION">
                                    <li><g:link controller="configuration" action="executionStatus">
                                        <g:message code="app.viewExecutionStatus.menu"/></g:link></li>
                                </sec:ifAnyGranted>

                            <!-- Import Configuration -->
                                <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_CONFIGURE_TEMPLATE_ALERT">
                                    <li><g:link controller="importConfiguration" action="importScreen">
                                        <g:message code="app.import.configuration"/></g:link></li>
                                </sec:ifAnyGranted>
                            </ul>
                        </li>

                    </sec:ifAnyGranted>


                    <sec:ifAnyGranted roles="ROLE_QUERY_CRUD">
                        <li class="has_sub">
                            <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                                <i class="md md-filter-list"></i>
                                <span><g:message code="app.label.queries"/></span> <span class="menu-arrow"></span>
                            </a>
                            <ul class="list-unstyled">
                                <sec:ifAllGranted roles="ROLE_DEV">
                                    <li>
                                        <g:link url="${grailsApplication.config.pvreports.query.load.uri}" target="_blank">
                                            <g:message code="app.loadQuery.menu"/>
                                        </g:link>
                                    </li>
                                </sec:ifAllGranted>
                                <sec:ifAnyGranted roles="ROLE_QUERY_CRUD">
                                    <li>
                                        <g:link url="${grailsApplication.config.pvreports.query.list.uri}" target="_blank">
                                            <g:message code="app.viewQueries.menu"/>
                                        </g:link>
                                    </li>
                                </sec:ifAnyGranted>
                                <sec:ifAnyGranted roles="ROLE_QUERY_CRUD">
                                    <li>
                                        <g:link url="${grailsApplication.config.pvreports.query.create.uri}" target="_blank">
                                            <g:message code="app.NewQuery.menu"/>
                                        </g:link>
                                    </li>
                                </sec:ifAnyGranted>
                            </ul>
                        </li>
                    </sec:ifAnyGranted>

                    <li class="has_sub">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                            <i class="md md-event"></i><span><g:message code="app.signal.event.title"/><span class="menu-arrow"></span></span>
                        </a>
                        <ul class="list-unstyled">
                            <li>
                                <g:link controller="action" action="index" class="waves-effect waves-primary">
                                    <g:message code="app.label.action.items"/>
                                </g:link>
                            </li>
                            <li>
                                <g:link controller="calendar" action="index" class="waves-effect waves-primary">
                                    <g:message code="app.calendar"/>
                                </g:link>
                            </li>
                        </ul>
                    </li>
                </ul>
                </g:if>

            </div>

            <div class="clearfix"></div>
        </div>
    </div>
    <div class="topbar-left">
        <div class="pull-left logo">
            <g:link controller="dashboard" action="index"><asset:image src="pv-signal-logo.png" class="pvLogo"/></g:link>
        </div>
    </div>
    <!-- Navbar -->
    <div class="navbar navbar-default" role="navigation">
        <div class="container">
            <div class="">
                <ul class="nav navbar-nav hidden-xs pull-left">
                    <li><a href="${grailsApplication.config.pvreports.web.url}/reports" target="_blank" class="waves-effect"><g:message code="app.config.reports.label"/></a></li>
                </ul>

                <sec:ifAnyGranted roles="ROLE_ADMIN, ROLE_DEV">
                    <ul class="nav navbar-nav hidden-xs pull-left">
                        <g:if test="${grailsApplication.config.pvadmin.web.url}">
                            <g:if test = "${grailsApplication.config.is.pvcm.env}">
                                <li><a href="${grailsApplication.config.pvadmin.web.url}" target="_blank"
                                       class="waves-effect"><g:message code="app.config.pvadmin.label"/></a></li>
                            </g:if>
                            <g:else>
                                <li><g:link uri="${grailsApplication.config.pvadmin.web.url}/login?token=${URLEncoder.encode(com.rxlogix.RxCodec.encode(session.id), 'UTF-8')}&app=PVS" class="waves-effect" target="_blank"><g:message code="app.config.pvadmin.label"/></g:link></li>
                            </g:else>
                        </g:if>
                    </ul>
                </sec:ifAnyGranted>
                <ul class="nav navbar-nav navbar-right pull-right">
                    <sec:ifLoggedIn>
                        <li class="hidden-xs m-t-10">
                            <h5 class="text-white login-user-fullname member-login"><g:message code="app.label.topnav.welcome" /> ${getCurrentUserFullName()}!</h5>
                            <small class="text-white last-login-date-time-box f12"><b><g:message code="user.lastLogin.label"/></b> :  <g:renderUserLastLoginDate/></small>
                        </li>
                        <li id="menuNotification" class="hidden-xs dropdown mega-dropdown">

                            <a href="#" data-target="#" class="dropdown-toggle waves-effect waves-light pv-head-noti-icon" data-toggle="dropdown" aria-expanded="true" accesskey="i">
                                 <i class="glyphicon glyphicon-inbox" style="font-size: 24px; margin-top:19px;padding-top: 1px"></i>
                            </a>
                            <ul id="notificationContainer" class="dropdown-menu dropdown-menu-lg notification-menu">
                                <li id="notificationHeader" class="notifi-title"></li>
                                <li id="notificationRows" class="ajax"></li>
                                <div class="text-right m-t-10">
                                    <g:link controller="inboxLog" action="index" class="btn btn-primary temp-redirect dpv-head-noti-icon box-inline" tabindex="0" accesskey="m" title="Open Inbox">Inbox</g:link>
                                    <a href="javascript:void(0);" id="clearNotifications" class="btn btn-default box-inline" userId="${userService.getUser()?.id}" tabindex="0" title="Clear All" accesskey="e"><g:message code="app.notification.clearAll"/></a>
                                </div>
                            </ul>

                        </li>

                        <li class="hidden-xs dropdown mega-dropdown">
                            <a href="#" data-target="#" class="dropdown-toggle waves-effect waves-light"
                               data-toggle="dropdown" aria-expanded="true" accesskey="t">
                                <i class="md md-settings"></i></a>
                            <ul class="dropdown-menu mega-dropdown-menu" role="menu">
                                <sec:ifAnyGranted roles="ROLE_ADMIN, ROLE_CONFIGURATION_CRUD,ROLE_CONFIGURATION_VIEW">
                                    <li class="col-sm-4">
                                        <ul>
                                            <li class="dropdown-header"><i class="fa fa-user-circle fa-fw"></i> User Management</li>
                                            <li id="userManagement"><g:link controller="user" action="index"><g:message code="app.label.userManagement"/></g:link></li>
                                            <li id="roleManagement"><g:link controller="role" action="index"><g:message code="app.label.roleManagement"/></g:link></li>
                                            <li id="groupManagement"><g:link controller="group" action="index"><g:message code="app.label.groupManagement" /></g:link></li>
                                </sec:ifAnyGranted>
                                            <li id="menuPreference"><g:link controller="preference" action="index"><g:message code="app.label.userPreferences" /></g:link></li>
                                            <li id="menuProductAssignment"><g:link controller="productAssignment" action="index" params="[isProductView:true]"><g:message code="app.label.productAssignment" /></g:link></li>
                                <sec:ifAnyGranted roles="ROLE_ADMIN, ROLE_CONFIGURATION_VIEW">
                                            <li class="dropdown-header"><i class="fa fa-sliders fa-fw"></i> Workflow Management</li>
                                            <li id="menuWorkflowRule"><g:link controller="workflowRule" action="index"><g:message code="app.label.workflow.rule" /></g:link></li>
                                            <li id="menuDisposition"><g:link controller="disposition" action="list"><g:message code="app.label.disposition" /></g:link></li>
                                            <li id="menuSignalWorkflow"><g:link controller="signalWorkflow" action="signalWorkflowRule"><g:message code="app.label.signal.workflow" /></g:link> </li>
                                            <li id="menuPriority"><g:link controller="priority" action="list"><g:message code="app.label.priority" /></g:link> </li>
                                            <li id="menuPriority"><g:link controller="justification" action="index"><g:message code="app.label.justification" /></g:link> </li>
                                        </ul>
                                    </li>
                                    <li class="col-sm-4">
                                        <ul>
                                            <li class="dropdown-header"><i class="fa fa-tasks fa-fw"></i> Business Configuration</li>
                                            <li id="menuBusinessConfig">
                                                <g:link controller="businessConfiguration" action="index">
                                                    <g:message code="app.label.business.configuration.title"/>
                                                </g:link>
                                            </li>
                                             <g:if test="${Holders.config.alertStopList}">
                                            <li id="alertStopList">
                                                <g:link controller="alertStopList" action="index">
                                                    <g:message code="app.label.alert.stop.list"/>
                                                </g:link>
                                            </li>
                                            </g:if>
                                            <li id="emergingSafetyIssues">
                                                <g:link controller="emergingIssue" action="index">
                                                    <g:message code="app.label.important.events"/>
                                                </g:link>
                                            </li>
                                            <li class="dropdown-header"><i class="fa fa-file-text-o fa-fw"></i> Action Template</li>
%{--                                                            %{-- Removed story/PVS-57996- part for drug classification --}%
                                            <li id="menuCommentTemplate">
                                                <g:link controller="commentTemplate" action="index">
                                                    <g:message code="app.label.comment.template" default="Comment Template"/>
                                                </g:link>
                                            </li>
                                            <li id="menuActionType">
                                                <g:link controller="actionType" action="list">
                                                    <g:message code="app.label.action.types" default="Action Type"/>
                                                </g:link>
                                            </li>
                                            <li id="menuActionConfig">
                                                <g:link controller="actionConfiguration" action="list">
                                                    <g:message code="app.label.action.configuration"/>
                                                </g:link>
                                            </li>
                                            <li id="productIngredientsMapping" class="hide">
                                                <g:link controller="productIngredientsMapping" action="index">
                                                    <g:message code="app.label.product.ingredients.mapping"/>
                                                </g:link>
                                            </li>
                                            <sec:ifAnyGranted roles="ROLE_ADMIN">
                                                <li class="dropdown-header" id="apiControlPannel-redirect" style="cursor: pointer;" onclick="window.location=apiControlPanelURL">
                                                    <i class="fa fa-dashboard fa-fw"></i>
                                                    Control Panel
                                                </li>
                                            </sec:ifAnyGranted>
                                        </ul>
                                    </li>
                                    <li class="col-sm-4">
                                        <ul>
                                            <li class="dropdown-header"><i class="fa fa-gear fa-fw"></i> System Configuration</li>
                                            %{--<li id="etlScheduler">
                                                    <g:link controller="etlSchedule" action="index">
                                                            <g:message code="app.label.etlScheduler"/>
                                                    </g:link>
                                            </li>--}%
                                    <sec:ifAnyGranted
                                            roles="ROLE_ADMIN">
                                        <li id="auditLog">
                                            <g:link controller="auditLogEvent" action="index">
                                                <g:message code="auditLog.label"/>
                                            </g:link>
                                        </li>
                                    </sec:ifAnyGranted>
                                    <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_CONFIGURATION_VIEW,ROLE_CONFIGURATION_CRUD">
                                                <g:if test="${grailsApplication.config.signal.evdas.enabled}">
                                                    <li id="evdasDataUpload">
                                                        <g:link controller="evdasData" action="index"><g:message code="app.label.evdas.data.upload"/></g:link>
                                                    </li>
                                                </g:if>
                                            </sec:ifAnyGranted>
                                </sec:ifAnyGranted>

                                            <sec:ifAnyGranted roles="ROLE_CONFIGURATION_CRUD">
                                            <g:if test="${Holders.config.dmsConfiguration}">
                                                <li id="dmsConfigurationLink">
                                                    <g:link controller="controlPanel" action="index"><g:message code="app.label.dms.configuration"/></g:link>
                                                </li>
                                            </g:if>
                                            </sec:ifAnyGranted>

                                            <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_CONFIGURATION_VIEW,ROLE_CONFIGURATION_CRUD">
                                                <li id="substanceFrequencyViewer">
                                                    <g:link controller="SubstanceFrequency" action="index"><g:message code="app.label.substance.frequency.viewer"/></g:link>
                                                </li>
                                            </sec:ifAnyGranted>

                %{-- Removed story/PVS-57996- part for drug classification --}%

                                            <g:if test="${grailsApplication.config.outlook.enabled}">
                                                <li><a href="${createLink(controller: 'outlook',action: 'login')}"><g:message code="app.label.outlook"/></a></li>
                                            </g:if>
                                            <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_CONFIGURATION_VIEW,ROLE_CONFIGURATION_CRUD">
                                                    <li id="mailNotifiaction">
                                                        <g:link controller="emailNotification" action="edit">Email Configuration</g:link>
                                                    </li>
                                            </sec:ifAnyGranted>
                                            <sec:ifAnyGranted roles="ROLE_ADMIN, ROLE_CONFIGURATION_CRUD">
                                                    <li id="notificationManagement">
                                                        <g:link controller="signalMemoReport" action="index">Signal Memo Configuration</g:link>
                                                    </li>
                                            </sec:ifAnyGranted>
                                            <sec:ifAnyGranted roles="ROLE_DEV">
                                                <li class="dropdown-header"><i class="fa fa-wrench fa-fw"></i> <g:message code="app.label.system.management"/></li>
                                                <li><a href="${createLink(uri: "/monitoring")}"><g:message code="app.label.monitoring"/></a></li>
                                                <li><a href="${createLink(uri: "/quartz")}"><g:message code="app.label.job.monitoring"/></a></li>
                                            </sec:ifAnyGranted>

                                            <a id="helpUrl" href="javascript:void(0);" onclick="helpUrl();">
                                                <div tabindex="0" class="help" title="${message(code: "app.label.help")}">
                                                  <i class="fa fa-question fa-fw"></i>
                                                      <g:message code="app.label.help"/>
                                                </div>
                                             </a>

                                            <a id="MI-logout" href="javascript:void(0);" onclick="logoutRxSession();" title="${message(code: "app.label.logout")}" class="logout">
                                                <div tabindex="0" class="logout">
                                                    <i class="fa fa-power-off fa-fw"></i>
                                                    <g:message code="app.label.logout"/>
                                                </div>
                                            </a>
                                        </ul>
                                    </li>
                            </ul>
                        </li>
                    </sec:ifLoggedIn>
                </ul>
            </div>
        </div>
    </div>
</div>
<style>
    .mega-dropdown-menu{
        width:650px;
        padding: 5px 0px;
        box-shadow: darkcyan;
        border-bottom : 4px solid #ccc;
        border-radius: 2px;
    }
    .mega-dropdown-menu > li > ul {
        padding: 0px;
        margin: 0;
    }

    .mega-dropdown-menu > li > ul > li {
        list-style: none;
        padding-left: 8px;
        border-bottom: 1px dotted #eee;
    }
    .mega-dropdown-menu > li > ul > li:last-child {
        list-style: none;
        padding-left: 8px;
        border-bottom: 0px dotted #eee;
    }
    .mega-dropdown-menu > li > ul > li > a,
    .mega-dropdown-menu > li > ul > li > span {
        display: block;
        padding: 7px 10px;
        clear: both;
        font-weight: 500;
        line-height: 1.428571429;
        color: #656565;
        white-space: normal;
        font-size: 13px;
    }
    .mega-dropdown-menu > li ul > li > a:hover,
    .mega-dropdown-menu > li ul > li > a:focus{
        text-decoration: none;
        color: #444;
        background-color: #eee;
    }
    .mega-dropdown-menu .dropdown-header{
        color: #099ddd;
        font-size: 13.5px;
        font-weight: bold;
    }
    .mega-dropdown-menu .logout, .help{
        color: #428bca;
        font-size: 15px;
        font-weight: 600;
        text-decoration: none;
    }
    #apiControlPannel-redirect:hover {
        background-color: #eee;
    }
</style>
