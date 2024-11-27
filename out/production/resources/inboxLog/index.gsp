<%@ page import="com.rxlogix.config.WorkflowRule" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <title>Inbox</title>
    <g:javascript>
        var SIGNAL_CREATION = 'Signal Creation';
        var inboxListUrl = "${createLink(controller: 'inboxLog', action: 'list')}";
        var markAsReadUrl = "${createLink(controller: 'inboxLog',action: 'markAsRead')}";
        var markAsUnreadUrl = "${createLink(controller: 'inboxLog',action: 'markAsUnread')}";
        var deleteUrl = "${createLink(controller: 'inboxLog',action: 'deleteInboxLog')}";
        var reportRedirectURL = "${createLink(controller: 'report', action: 'criteria')}";
        var caseSeriesURL =  "${createLink(controller: 'singleCaseAlert', action: 'caseSeriesDetails')}";
        var detailUrls = {};
        detailUrls["sca_reportRedirectURL"] =  "${createLink(controller: 'singleCaseAlert', action: 'details')}";
        detailUrls["sca_adhoc_reportRedirectURL"] =  "${createLink(controller: 'singleOnDemandAlert', action: 'adhocDetails')}";
        detailUrls["aga_reportRedirectURL"] = "${createLink(controller: 'aggregateCaseAlert', action: 'details')}";
        detailUrls["aga_adhoc_reportRedirectURL"] = "${createLink(controller: 'aggregateOnDemandAlert', action: 'adhocDetails')}";
        detailUrls["evdas_reportRedirectURL"] = "${createLink(controller: 'evdasAlert', action: 'details')}";
        detailUrls["evdas_adhoc_reportRedirectURL"] = "${createLink(controller: 'evdasOnDemandAlert', action: 'adhocDetails')}";
        detailUrls["error_url"]  = "${createLink(controller: 'configuration', action: 'executionStatus')}";
        detailUrls["LITERATURE_reportRedirectURL"] = "${createLink(controller: 'literatureAlert', action: 'details')}";
        detailUrls["SIGNAL_CREATION"] = "${createLink(controller: 'validatedSignal', action: 'details')}";
        </g:javascript>
    <asset:javascript src="app/pvs/inbox_log/inbox_log.js"/>
</head>

<body>
<g:render template="/includes/layout/flashErrorsDivs" bean="${workflowRuleInstanceTotal}" var="theInstance"/>
<div class="row">
    <div class="col-sm-12">
        <div class="page-title-box">
            <div class="fixed-page-head">
                <div class="page-head-lt">
                    <h5>Inbox</h5>
                </div>
            </div>
        </div>
    </div>
</div>

<div class="inbox-container">
    <div class="navbar navbar-fixed-left">
        <ul class="nav nav-pills nav-stacked">

            <li role="presentation" class="${activeFilter=="today"?'active':'a-link'}"><a href="/signal/inboxLog/index" accesskey="o">Today</a></li>
            <li role="presentation" class="${activeFilter=="lastWeek"?'active':'a-link'}"><a href="/signal/inboxLog/index?dueIn=lastWeek" accesskey="w">Last Week</a></li>
            <li role="presentation" class="${activeFilter=="lastMonth"?'active':'a-link'}"><a href="/signal/inboxLog/index?dueIn=lastMonth" accesskey="m">Last Month</a></li>
            <li role="presentation" class="${activeFilter=="all"?'active':'a-link'}"><a href="/signal/inboxLog/index?dueIn=all" accesskey="l">All</a></li>
        </ul>
    </div>

    <div class="right-pane">
        <div class="something">
                <div class="notificationPanel alert alert-header">
                    <div class="row">
                        <div class="col-md-2"><label class="font-16">Notification Type</label></div>
                        <div class="col-md-7"><label class="font-16">Notification Subject</label></div>
                        <div class="col-md-2 text-right"><label class="font-16">Timestamp</label></div>
                    </div>
                </div>
            <g:if test="${inboxList.size() > 0}">

                <g:each in="${inboxList}">
                    <div class="${it.isRead ? 'alert alert-inbox grey' : 'alert alert-inbox'} notificationPanel"
                         data-type="${it.type}" data-content="${it.content}" data-id="${it.id}"
                         data-execId="${it.execConfigId}" data-detailUrl="${it.detailUrl}">
                        <div class="row">

                            <div class="col-md-2">
                                <p class="${!it.isRead ? 'font-bold':''} m-b-0 boldText">${it.type}</p>
                            </div>
                            <div class="col-md-7">
                                <p class="${!it.isRead ? 'font-bold':''} m-b-0 boldText cell-break">${it.subject}</p>
                            </div>
                            <div class="col-md-2 boldText text-right">
                                <p class="${!it.isRead ? 'font-bold':''}  m-b-0 boldText"><g:render template="/includes/widgets/dateDisplayWithTimezone"
                                                                                                    model="[date: it.createdOn]"/></p>
                            </div>
                            <div class="col-md-1">
                                <div class="pull-right">
                                    <div style="display: block;" class="btn-group dropdown dataTableHideCellContent">
                                        <a class="dropdown-toggle" data-toggle="dropdown" tabindex="0" aria-expanded="true">
                                            <span style="cursor: pointer;font-size: 125%;" class="glyphicon glyphicon-option-vertical"></span>
                                            <span class="sr-only">Toggle Dropdown</span>
                                        </a>
                                        <ul class="dropdown-menu menu-cosy" style="left:auto;right:-5px; top: calc(100% + 5px);" role="menu">
                                            <li role="presentation">
                                                <a class="mark-read-icon" data-id="${it.id}" tabindex="0">
                                                    <i class="fa fa-check-circle m-r-10"></i>Mark As Read
                                                </a>
                                            </li>

                                            <li role="presentation">
                                                <a class=" mark-unread-icon" data-id="${it.id}" tabindex="0">
                                                    <i class="fa fa-times-circle m-r-10"></i>Mark As Unread
                                                </a>
                                            </li>

                                            <li role="presentation">
                                                <a class="delete-icon" data-id="${it.id}" tabindex="0">
                                                    <i class="fa fa-trash m-r-10"></i>Delete
                                                </a>
                                            </li>

                                        </ul>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                </g:each>
            </g:if>
            <g:else>
                <div class="no-data">
                    <h3>No Notification</h3>
                </div>
            </g:else>
        </div>
    </div>
</div>
<g:render template="/includes/modals/notification_email_modal"/>

</body>
</html>
