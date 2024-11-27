<%@ page import="com.rxlogix.enums.ReportFormat; grails.plugin.springsecurity.SpringSecurityUtils" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.signal.summary"/></title>
    <asset:javascript src="app/pvs/dataTablesActionButtons.js"/>
    <asset:javascript src="yadcf/jquery.dataTables.yadcf.js"/>
    <asset:javascript src="app/pvs/bootbox.min.js"/>
    <asset:javascript src="app/pvs/validated_signal/validated_signal.js"/>
    <asset:javascript src="vendorUi/datatables/datatable.colsReorder.min.js"/>
    <asset:javascript src="app/pvs/alerts_review/fieldConfigurationManagement.js"/>


    <g:javascript>
            var VALIDATED = {
                 signalListUrl : "${createLink(controller: 'validatedSignal', action: 'list')}",
                 searchStrategyProducts : "${createLink(controller: 'validatedSignal', action: 'searchStrategyProducts')}",
                 saveSignalUrl : "${createLink(controller: 'validatedSignal', action: 'save')}",
                 exportReportUrl : "${createLink(controller: 'validatedSignal', action: 'exportReport')}",
                 deleteSignalUrl: "${createLink(controller: 'validatedSignal', action: 'deleteValidatedSignals')}"
            };
            var callingScreen = "${callingScreen}";
            var viewId = "${viewId}";
            var iconSeq = "${iconSeq}";
            var applicationName;
             var selectedFilter=false;

            var topicSaveUrl = "${createLink(controller: 'topic', action: 'save')}";
            var topicListUrl = "${createLink(controller: 'topic', action: 'list')}";
            var topicEditUrl = "${createLink(controller: 'topic', action: 'edit')}";
            var topicExportUrl = "${createLink(controller: 'topic', action: 'exportReport')}";
            var gridColumnsViewUrl = "${createLink(controller: 'viewInstance', action: 'viewColumnInfo', params: ['viewInstance.id': viewId])}";
            var gridColumnsViewUpdateUrl = "${createLink(controller: 'viewInstance', action: 'updateViewColumnInfo', params: ['viewInstance.id': viewId])}";
            var pinUnpinUrl = "${createLink(controller: 'viewInstance', action: 'savePinnedIcon')}"
            var isAdmin = ${SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN")};
            var isSignalManagement = ${SpringSecurityUtils.ifAnyGranted("ROLE_SIGNAL_MANAGEMENT_CONFIGURATION")};
    </g:javascript>
    <asset:stylesheet src="yadcf/jquery.dataTables.yadcf.css"/>
    <asset:stylesheet src="colReorder.dataTables.min.css"/>
    <asset:stylesheet src="app/pvs/updatedFixedColumn.css"/>
    <style type="text/css">
    .table-responsive {
        border: 0 solid #FFFFFF !important;
        overflow-x: inherit !important;
        box-shadow: 0 0 0 #aaa !important;
    }
    .dropdown-menu>li>a:focus, .dropdown-menu>li>a:hover {
        color: #262626;
        text-decoration: none;
        background-color: transparent!important;
    }
    .text-right-prop:focus, input:focus, textarea:focus, select:focus, button:focus, i:focus, .btn:focus, li:focus, span:focus, .form-control:focus {
        outline: 0!important;
        box-shadow: none!important;
    }
    .text-left-prop:focus, input:focus, textarea:focus, select:focus, button:focus, i:focus, .btn:focus, li:focus, span:focus, .form-control:focus {
        outline: 0!important;
        box-shadow: none!important;
    }
    .rxmain-container-header {
        padding-bottom: 1px; !important;
    }
    </style>
</head>
<body>
<div id="accordion-pvs-analysis">

<g:render template="/includes/layout/flashErrorsDivs" bean="${configurationInstance}" var="theInstance"/>
<div class="messageContainer"></div>

<div class="panel panel-default rxmain-container rxmain-container-top m-b-0">
    <div class="rxmain-container-row rxmain-container-header panel-heading pv-sec-heading">
        <div class="row">
            <div class="col-md-4">
                <label class="rxmain-container-header-label m-t-3">${message(code: "app.label.signals")}</label>
            </div>

            <div class="col-md-8 ico-menu">
                <span class="dropdown grid-icon pull-right" id="reportIconMenu">
                <span class="dropdown-toggle" data-toggle="dropdown" style="float: left;">
                        <i class="mdi mdi-format-list-bulleted mr-10 font-24 pull-right mr-10" style="margin-right:5px"></i>
                    </span>
                    <ul class="dropdown-menu ul-ddm">

                        <li class="li-pin-width">
                            <a class="test text-left-prop" id="toggle-column-filters" title="Filters" href="#">
                                <i class="mdi mdi-filter-outline"></i>
                                <span data-title="Filters" class="test" tabindex="0" role="button" accesskey="y">
                                    Filters</span>
                            </a>
                            <a href="javascript:void(0)" class="text-right-prop" data-toggle="collapse" data-id="#ic-toggle-column-filters"><span class="pin-unpin-rotate pull-right mdi mdi-pin" title="Pin to top"  data-toggle="collapse" data-id="#ic-toggle-column-filters" data-title="Filters"></span></a>
                        </li>

                        <li class="li-pin-width">
                            <a href="#" id="configureValidatedSignalFields" data-fieldconfigurationbarid="validatedSignalFields"
                               data-pagetype="signal_management" class="text-left-prop test field-config-bar-toggle" data-backdrop="true" data-container="columnList-container" title="Field Selection"
                               data-title="${message(code: 'app.label.choosefields')}" accesskey="c"
                               data-original-title="Choose Fields">
                                <i class="mdi mdi-settings-outline"></i>
                                <span tabindex="0" data-backdrop="true">
                                    Field Selection
                                </span></a>
                            <a href="javascript:void(0)" class="text-right-prop" ><span class="pin-unpin-rotate pull-right mdi mdi-pin" title="Pin to top" data-title="Field Selection" data-toggle="collapse" data-id="#ic-configureValidatedSignalFields"></span></a>
                        </li>

                        <li class="s-tracker li-pin-width">
                            <span class="export_icon_signal">
                                <a  href="#" data-format="${ReportFormat.XLSX}" data-title="${message(code: 'app.label.signaltrackerexport')}" id="exportTypes" title="Signal Tracker Export" class="text-left-prop" style="width: auto">
                                    <i class="mdi mdi-export"></i>
                                    <span>Signal Tracker Export</span></a>
                                <span class="pin-unpin-rotate pull-right mdi mdi-pin" style="width:5%" href="javascript:void(0)" data-toggle="collapse" data-id="#ic-exportTypes" title="Pin to top" data-title="${message(code: 'app.label.signaltrackerexport')}"></span>
                            </span>
                        </li>
                        <sec:ifAnyGranted roles="ROLE_SIGNAL_MANAGEMENT_CONFIGURATION">
                        <li class="li-pin-width">
                            <a href="create" class="text-left-prop" tabindex="0" id="create-signal" title="${message(code: 'app.label.createsignal')}">
                                <i class="mdi mdi-plus-box"></i> <span>Create Signal</span></a>
                            <a href="javascript:void(0)" class="text-right-prop" ><span class="pin-unpin-rotate pull-right mdi mdi-pin"  title="Pin to top" data-title="${message(code: 'app.label.createsignal')}" data-toggle="collapse" data-id="#ic-create-signal"></span>
                            </a>
                        </li>
                        </sec:ifAnyGranted>
                    </ul>
                </span>
                <span class="pull-right p-r-10 inline-icon">
                    <a href="javascript:void(0)" class="grid-pin collapse theme-color" id="ic-toggle-column-filters" data-fieldconfigurationbarid="validatedSignalFields" data-pagetype="signal_management" title="${message(code: 'app.label.filter')}">
                                <i class="mdi mdi-filter-outline font-24"></i>
                   <a href="javascript:void(0)" class="grid-pin collapse theme-color" id="ic-configureValidatedSignalFields" data-fieldconfigurationbarid="validatedSignalFields" data-pagetype="signal_management" title="${message(code: 'app.label.choosefields')}">
                               <i class="mdi mdi-settings-outline font-24"></i>
                             </a>
                    <a  href="#" data-format="${ReportFormat.XLSX}" data-title="${message(code: 'app.label.signaltrackerexport')}" title="Signal Tracker Export" class="grid-pin  gp3 theme-color collapse font-24" id="ic-exportTypes"><i class="mdi mdi-export font-24"></i> </a>
                    <sec:ifAnyGranted roles="ROLE_SIGNAL_MANAGEMENT_CONFIGURATION">
                    <a class="grid-pin  gp4 theme-color collapse font-24" id="ic-create-signal" href="create" data-fieldconfigurationbarid="validatedSignalFields" data-pagetype="signal_management" title="${message(code: 'app.label.createsignal')}"><i class="mdi mdi-plus-box font-24 "></i> </a>
                    </sec:ifAnyGranted>
                </span>
                <span id="search-control" class="pull-right dropdown-toggle" style="display: flex; flex-direction: row;margin-right: 10px;">
                    <span id="dropdownUsers" class="col-xl-2 pull-right dropdown-toggle" style="width: 30rem;max-width: 30rem; display: flex; flex-direction: row;">
                        <label for="alertsFilter" class="pull-right" style="white-space: nowrap; margin-right: 5px;margin-top: 2px">Select Users</label>
                        <g:initializeUsersAndGroupsElement shareWithId="" isWorkflowEnabled="true" alertType="signalFilter" isFromSignal="true" callingScreen="${callingScreen}" />
                    </span>
                    <span id="custom-search-label" class="pull-right" style="margin-left: 25px;margin-right: 5px;margin-top: 2px">
                        <label for="custom-search">Search</label>
                    </span>
                    <input id="custom-search" class="pull-right dropdown-toggle form-control" style="width: 200px; margin-left: 3px; height: 22px!important;">
                </span>
                <!-----------------------------------list view code end-------------------------------->

                <div class="pull-right dropdown-menu menu-large" aria-labelledby="dropdownMenu1">
                    <div class="rxmain-container-dropdown">
                        <div>
                            <table id="tableColumns" class="table no-border">
                                <thead><tr><th>${message(code: 'app.label.name')}</th><th>${message(code: 'app.label.show')}</th>
                                </tr></thead>
                            </table>
                        </div>
                    </div>
                </div>

            </div>
        </div>
    </div>

    <div id="signalDiv" class="panel-collapse rxmain-container-content rxmain-container-show collapse in"
         aria-expanded="true">
        <div id="validatedSignalTableContainer">
            <table id="validatedSignalsTable" width="100%">
                <thead>
                <tr id="alertsDetailsTableRow">
                    <th data-idx="0" data-field="checkbox">
                        <input id="select-all" type="checkbox"/>
                    </th>
                    <th data-idx="1" data-field="name">
                        <g:message code="app.label.signal.name"/>
                    </th>
                    <th data-idx="2" data-field="productName">
                        <g:message code="app.label.signal.product.name"/>
                    </th>
                    <th data-idx="3" data-field="eventName">
                        <g:message code="app.label.signal.event.name"/>
                    </th>
                    <th data-idx="4" data-field="noOfPec">
                        <g:message code="app.label.pec.count"/>
                    </th>
                    <th data-idx="5" data-field="noOfCases">
                        <g:message code="app.label.case.count"/>
                    </th>
                    <th data-idx="6" data-field="monitoringStatus">
                        <g:message code="app.label.disposition"/>
                    </th>
                    <th data-idx="7" data-field="topicCategory">
                        <g:message code="signal.configuration.topicCategory.label"/>
                    </th>
                    <th data-idx="8" data-field="assignedTo">
                        <g:message code="app.label.assigned.to"/>
                    </th>
                    <th data-idx="9" data-field="priority">
                        <g:message code="app.label.priority"/>
                    </th>
                    <th data-idx="10" data-field="actions">
                        <g:message code="app.label.actions"/>
                    </th>
                    <th data-idx="11" data-field="detectedDate">
                        <g:message code="app.label.detectedDate"/>
                    </th>
                    <th data-idx="12" data-field="status">
                        <g:message code="app.label.status"/>
                    </th>
                    <th data-idx="13" data-field="dateClosed">
                        <g:message code="app.label.date.closed"/>
                    </th>
                    <th data-idx="14" data-field="dueIn">
                        <g:message code="app.label.dueIn"/>
                    </th>
                    <th data-idx="15" data-field="signalSource">
                        <g:message code="app.label.signalSource"/>
                    </th>
                    <th data-idx="16" data-field="actionTaken">
                        <g:message code="app.label.actionTaken"/>
                    </th>
                    <th data-idx="17" data-field="signalOutcome">
                        <g:message code="app.label.signalOutcome"/>
                    </th>
                    <th data-idx="18" data-field="signaId">
                        <g:message code="app.label.signalId"/>
                    </th>
                    <sec:ifAnyGranted roles="ROLE_SIGNAL_MANAGEMENT_CONFIGURATION">
                        <th data-idx="19" data-field="action">
                            <g:message code="app.label.action"/>
                        </th>
                    </sec:ifAnyGranted>
                </tr>
                </thead>
            </table>
        </div>
    </div>
</div>
</div>
<g:hiddenField id="filterVals" value="${session.getAttribute("signalFilter")}" name="filterVals" />
<g:hiddenField id="filterValsForDashboard" value="${session.getAttribute("signalFilterFromDashboard")}" name="filterValsForDashboard" />
    <!-- The signal modal goes here. -->
<g:render template="/includes/modals/downloadReportDatePickerModal"/>
<g:render template="/template/fieldConfiguration" model="[fieldConfigurationBarId: 'validatedSignalFields']"/>
</div>
</body>
