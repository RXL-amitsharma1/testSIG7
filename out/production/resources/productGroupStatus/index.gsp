<%@ page import="com.rxlogix.enums.ReportFormat" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.product.group.status.logs"/></title>
    <asset:javascript src="app/pvs/dataTablesActionButtons.js"/>
    <asset:javascript src="yadcf/jquery.dataTables.yadcf.js"/>
    <asset:javascript src="app/pvs/product_groups/status_report.js"/>
    <asset:javascript src="vendorUi/datatables/datatable.colsReorder.min.js"/>
    <asset:javascript src="app/pvs/alerts_review/fieldConfigurationManagement.js"/>

    <asset:stylesheet src="yadcf/jquery.dataTables.yadcf.css"/>
    <asset:stylesheet src="colReorder.dataTables.min.css"/>
    <asset:stylesheet src="app/pvs/updatedFixedColumn.css"/>

    <g:javascript>
            var VALIDATED = {
                 signalListUrl : "${createLink(controller: 'productGroupStatus', action: 'list')}",
                 allProductGroupsStatusDownloadLink: "${createLink(controller: 'productGroupStatus', action: 'exportProductGroupsStatus')}",
                 allProductGroupsDataDownloadLink: "${createLink(controller: 'productGroupStatus', action: 'exportProductGroupDatas')}",
            };
            var callingScreen = "${callingScreen}";
            var iconSeq = "${iconSeq}";

    </g:javascript>
    <style>
    #statusReportTable_length {
        padding-top: 8px;
    }
    #validatedSignalTableContainer {
        height: auto;
    }
    #validatedSignalTableContainer {
        height: auto;
    }
    </style>
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
    .apiexport {
        margin-left: 7px;
        font-size: 14px;
        cursor: pointer;
        color: #000000;
        background-color: #ffffff00;
        border-radius: 4px;
        padding: 0px 4px;
    }
    .apiexport:hover {
        margin-left: 7px;
        font-size: 14px;
        cursor: pointer;
        color: #ffffff;
        background-color: #00000078;
        border-radius: 4px;
        padding: 0px 4px;
    }
    .runETLScheduleNowSpan {
        background-color: #85EB49;
        color: #0000008a;
        border-radius: 20px;
        padding: 4px 15px;
        font-weight: bold;
        font-size: 15px;
        cursor: pointer;
    }
    .runETLScheduleNowSpan:hover {
        color: #85EB49;
        background-color: #0000008a;
    }


    </style>
</head>
<body>
<div id="accordion-pvs-analysis">

<g:render template="/includes/layout/flashErrorsDivs" bean="${configurationInstance}" var="theInstance"/>

<div class="panel panel-default rxmain-container rxmain-container-top m-b-0">
    <div class="rxmain-container-row rxmain-container-header panel-heading pv-sec-heading">
        <div class="row" >
            <div class="col-md-4">
                <label class="rxmain-container-header-label m-t-5">${message(code: "app.product.group.status.label.list")}</label>
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
                                <p class="test" style="display: inline;" tabindex="0" role="button" accesskey="y">
                                    Filters</p>
                            </a>
                        </li>
                        <li class="s-tracker li-pin-width" title="<g:message code="export.to.excel"/>">
                            <span class="export_icon_signal">
                                <a id="downloadCompleteBatchHref" href="#" class="text-left-prop" style="width: auto; white-space: nowrap;" data-ol-has-click-handler="">
                                    <i class="mdi mdi-export"></i>
                                    <p style="display: inline">Export To Excel</p></a>
                            </span>
                        </li>

                    </ul>
                </span>
                <span class="pull-right p-r-10 inline-icon">
                    <span class="grid-pin mdi mdi-filter-outline collapse theme-color font-24" id="ic-toggle-column-filters"></span>
                    <a href="#" id="ic-configureValidatedSignalFields" data-fieldconfigurationbarid="validatedSignalFields" data-pagetype="signal_management" class="grid-pin collapse theme-color field-config-bar-toggle" data-backdrop="true" data-container="columnList-container" data-title="Choose fields" accesskey="c" data-original-title="Choose Fields">
                        <i class="mdi mdi-settings-outline font-24"></i>
                    </a>
                    <a  href="#" data-format="${ReportFormat.XLSX}" data-title="${message(code: 'app.uploadStatus.label.export')}" title="Signal Tracker Export" class="grid-pin  gp3 theme-color collapse font-24" id="ic-exportTypes"><i class="mdi mdi-export font-24"></i> </a>
                    <sec:ifAnyGranted roles="ROLE_SIGNAL_MANAGEMENT_CONFIGURATION">
                        <a class="grid-pin  gp4 theme-color collapse font-24" id="ic-create-signal" href="create"><i class="mdi mdi-plus-box font-24 "></i> </a>
                    </sec:ifAnyGranted>
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
    <div id="signalDiv" class="panel-collapse rxmain-container-content rxmain-container-show collapse in" aria-expanded="true" style="padding-top: 10px;">
        <div id="validatedSignalTableContainer">
            <table id="statusReportTable" width="100%">
                <thead>
                <tr id="alertsDetailsTableRow">
                    <th data-idx="0" data-field="checkbox">
                        <input id="select-all" type="checkbox"/>
                    </th>
                    <th data-idx="1" data-field="batchId">
                        <g:message code="app.uploadStatus.column.header.batchName"/>
                    </th>
                    <th data-idx="2" data-field="validRecordCount">
                        <g:message code="app.uploadStatus.column.header.processedCount"/>
                    </th>
                    <th data-idx="3" data-field="uploadedDate">
                        <g:message code="app.product.group.column.header.ProcessedDate"/>
                    </th>
                    <th data-idx="4" data-field="addedBy">
                        <g:message code="app.uploadStatus.column.header.addedBy"/>
                    </th>
                    <th data-idx="5" data-field="apiStatus">
                        <g:message code="app.uploadStatus.column.header.apiStatus"/>
                    </th>
                </tr>
                </thead>
            </table>
        </div>
    </div>
</div>
</div>

<!-- The signal modal goes here. -->
<g:render template="/includes/modals/downloadReportDatePickerModal"/>
<g:render template="/template/fieldConfiguration" model="[fieldConfigurationBarId: 'validatedSignalFields']"/>
</div>
</body>
