<%@ page import="com.rxlogix.Constants" %>

<head>
    <meta name="layout" content="main"/>
    <title>Action List</title>
    <g:javascript>
        var actionItemUrl = "${createLink(controller: "action", action: "listByCurrentUser")}";
        var statusValue = "${Constants.Commons.ALL}";
        var pubMedUrl = "${literatureArticleUrl}";
        var buttonClass = "${buttonClass}"
        var eventDetailsUrl = "${createLink(controller: "eventInfo", action: "eventDetail")}";
        var caseDetailUrl = "${createLink(controller: "caseInfo", action: 'caseDetail')}";
        var validatedSignalUrl = "${createLink(controller: "validatedSignal", action: 'details')}";
        var adhocAlertUrl = "${createLink(controller: "adHocAlert", action: 'alertDetail')}";
        var userViewAccessMap = {
            "${Constants.AlertConfigType.SINGLE_CASE_ALERT}": ${userViewAccessMap.get(Constants.AlertConfigType.SINGLE_CASE_ALERT)},
            "${Constants.AlertConfigType.SIGNAL_MANAGEMENT}": ${userViewAccessMap.get(Constants.AlertConfigType.SIGNAL_MANAGEMENT)},
            "${Constants.AlertConfigType.LITERATURE_SEARCH_ALERT}": ${userViewAccessMap.get(Constants.AlertConfigType.LITERATURE_SEARCH_ALERT)},
            "${Constants.AlertConfigType.EVDAS_ALERT}": ${userViewAccessMap.get(Constants.AlertConfigType.EVDAS_ALERT)},
            "${Constants.AlertConfigType.AGGREGATE_CASE_ALERT}": ${userViewAccessMap.get(Constants.AlertConfigType.AGGREGATE_CASE_ALERT)},
            "${Constants.AlertConfigType.AD_HOC_ALERT}": ${userViewAccessMap.get(Constants.AlertConfigType.AD_HOC_ALERT)},
        }
    </g:javascript>
    <asset:javascript src="moment/moment.js" />
    <asset:javascript src="moment/momentlocales.js" />
    <asset:javascript src="purify/purify.min.js" />
    <asset:javascript src="moment/momentTimezones.js" />
    <asset:javascript src="app/pvs/actions/actions.js"/>
    <asset:javascript src="app/pvs/actions/actionList.js"/>
    <asset:javascript src="fuelux/fuelux.js"/>
    <asset:stylesheet src="fuelux.css"/>
    <asset:stylesheet src="app/pvs/updatedFixedColumn.css"/>
    <script>
        $(document).ready(function () {
            signal.action.init_action_table(actionItemUrl);

            $('#dueDate').change(function(){
                $(this).val(newSetDefaultDisplayDateFormat( $(this).val()));
                if($(this).val()=='Invalid date'){
                    $(this).val('');
                }
            });

            $('#completedDate').change(function(){
                $(this).val(newSetDefaultDisplayDateFormat( $(this).val()));
                if($(this).val()=='Invalid date'){
                    $(this).val('');
                }
            });
        })
    </script>
    <g:render template="/includes/widgets/actions/action_types"/>
    <g:render template="/includes/modals/actionCreateModal"
              model="[actionConfigList: actionConfigList]"/>


</head>

<body>

    <div class="panel panel-default rxmain-container rxmain-container-top">
        <div class="rxmain-container-row rxmain-container-header panel-heading pv-sec-heading">
            <div class="row">
                <div class="col-md-11">
                    <label class="rxmain-container-header-label m-t-5">Action List</label>
                </div>
                <div class="col-md-1 ico-menu">
                            <sec:ifAnyGranted roles="ROLE_ADMIN, ROLE_DEV,ROLE_CONFIGURATION_CRUD,ROLE_SINGLE_CASE_CONFIGURATION,ROLE_AGGREGATE_CASE_CONFIGURATION, ROLE_LITERATURE_CASE_CONFIGURATION,ROLE_SIGNAL_MANAGEMENT_CONFIGURATION,ROLE_SIGNAL_MANAGEMENT_REVIEWER,ROLE_AD_HOC_CRUD, ROLE_SINGLE_CASE_CONFIGURATION,ROLE_SINGLE_CASE_REVIEWER,ROLE_EVDAS_CASE_CONFIGURATION, ROLE_VAERS_CONFIGURATION,ROLE_FAERS_CONFIGURATION,  ROLE_VIGIBASE_CONFIGURATION,ROLE_JADER_CONFIGURATION">
                                 <span class="pull-right p-r-10 inline-icon">
                                                      <a class="grid-pin action-create font-24" tabindex="0" title="Create Action Item" href="#" style="display: inline;"><i class="mdi mdi-plus-box font-24 "></i> </a>
                                                    </span>
                            </sec:ifAnyGranted>
                </div>
            </div>
        </div>
        <div class="rxmain-container-content">
        <table id="action-table" style="width: 100%">
            <thead>
            <tr>
                <th class="pvi-col-xs">Id</th>
                <th><g:message code="app.label.action.types"/></th>
                <th><g:message code="app.label.action" default="Action"/></th>
                <th><g:message code="app.action.list.action.details.label" default="Details"/></th>
                <th><g:message code="app.label.alert.name"/></th>
                <th>Alert Type</th>
                <th><g:message code="app.label.action.entity"/></th>
                <th><g:message code="app.label.due.date" default="Due Date"/></th>
                <th><g:message code="app.action.list.status.label" default="Status"/></th>
                <th><g:message code="app.label.comments"/></th>
                <th><g:message code="app.action.list.completion.label" default="Completion Date"/></th>
            </tr>
            </thead>
        </table>
        <g:render template="/includes/modals/action_edit_modal"/>
        <g:render template="/includes/modals/bulk_action_edit_modal"/>
    </div>

</body>

