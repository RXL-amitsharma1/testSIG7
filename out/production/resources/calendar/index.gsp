<%@ page import="com.rxlogix.CalendarEventDTO; com.rxlogix.util.DateUtil; com.rxlogix.CalendarService" contentType="text/html;charset=UTF-8" %>
<head>
    <meta name="layout" content="main"/>
    <asset:javascript src="vendorUi/fullcalendar.js"/>
    <asset:stylesheet src="vendorUi/fullcalendar.min.css"/>
    <asset:stylesheet src="vendorUi/fullcalendar.print.css" media="print"/>

    <style type="text/css">
    .calendar {
        max-width: 100%;
    }
    </style>
    <script>
        var eventsUrl = "${createLink(controller: "calendar", action: "events")}";
        var createReportRequestUrl = "${createLink(controller: 'reportRequest', action: 'create')}";
        var saveActionItemUrl = "${createLink(controller: "actionItem", action: "save")}";
        var updateActionItemUrl = "${createLink(controller: "actionItem", action: "update")}";
        var deleteActionItemUrl = "${createLink(controller: 'actionItem', action: 'delete')}";
        var reportRequestShowURL = "${createLink(controller: 'reportRequest', action: 'show')}";
        var executedPeriodicReportShowURL = "${createLink(controller: 'periodicReport', action: 'viewExecutedConfig')}";
        var executedAdhocReportShowURL = "${createLink(controller: 'configuration', action: 'viewExecutedConfig')}";
        var adhocReportShowURL = "${createLink(controller: 'configuration', action: 'view')}";
        var periodicReportShowURL = "${createLink(controller: 'periodicReport', action: 'view')}";
        var viewActionItemUrl = "${createLink(controller: "actionItem", action: "view")}"
</script>

    <asset:javascript src="app/pvs/calendar.js"/>
    <asset:javascript src="app/pvs/actions/actions.js"/>
    <g:render template="/includes/widgets/actions/action_types" />
    <g:render template="/includes/modals/action_edit_modal" />
    <g:render template="/includes/modals/actionCreateModal"
              model="[actionConfigList: actionConfigList, actionTypeList: actionTypeList]"/>
</head>
<style>
    .calendar{
        max-width: 1200px;
    }
</style>
<body>
<div class="alert alert-danger hide">
    <a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>
    <strong>Error !</strong> <span id="errorNotification"></span>
</div>

<div class="alert alert-success hide">
    <a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>
    <strong>Success !</strong> <span id="successNotification"></span>
</div>
<rx:container title="Calendar">
    <div class="body">

        <div id="report-request-conainter col-lg-12" class="list">
            <div>
                <div class="alert alert-info hide calendar col-lg-12">
                    Loading Events.
                </div>

                <div class="calendar" id="calendar" style="overflow-x: auto">
                    <div id="legend" class="fc-view-container">
                        <p><strong style="font-size: 12px;"></strong></p>

                        <div class="legendColorBox"
                             style="background-color: ${CalendarEventDTO.ColorCodeEnum.SINGLE_CASE_ALERT_COLOR.code}">&nbsp;</div><span
                            class="legendLabel text-muted">Individual Case Review Alert</span>

                        <div class="legendColorBox"
                             style="background-color: ${CalendarEventDTO.ColorCodeEnum.AGGREGATE_CASE_ALERT_COLOR.code}">&nbsp;</div><span
                            class="legendLabel text-muted">Aggregate Review Alert</span>

                        <div class="legendColorBox"
                             style="background-color: ${CalendarEventDTO.ColorCodeEnum.ACTION_ITEM.code}">&nbsp;</div><span
                            class="legendLabel text-muted">Action Item</span>

                        <div class="legendColorBox"
                             style="background-color: ${CalendarEventDTO.ColorCodeEnum.MEETING_ITEM.code}">&nbsp;</div><span
                            class="legendLabel text-muted">Meetings</span>

                    </div>

                </div>
        </div>
    </div>
    </div>
</rx:container>
<g:render template="/includes/modals/meetingMinutesModal" model="[appType: 'Signal Management', userList: userList]" />
</body>
