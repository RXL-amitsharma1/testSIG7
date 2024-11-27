<%@ page import="com.rxlogix.Constants; com.rxlogix.enums.ReportFormat" %>
<%@ page import="com.rxlogix.Constants; com.rxlogix.enums.ReportFormat; grails.util.Holders" %>

<style>
table.dataTable thead > tr > th {
    padding-left: 8px;
    padding-right: 8px;
}
div.dataTables_wrapper {
    margin: 0 auto;
}
input[type=checkbox] {
    margin: 8px 0 0!important;
}

</style>
<script>
    $(document).ready(function () {
        $('.search-box').show();
    });
</script>

<script>
    $(document).ready(function () {
        $("#statistical-comparison, #ic-statistical-comparison").click(function () {
            location.href = statComparisonUrl;
            })
    });
</script>

<script>
    $('.bookmarks-list-opener').hover(function () {
        $('.dropdown-toggle', this).trigger('click');
    });
</script>

<div class="row">
  <div class="panel-heading pv-sec-heading">
    <div class="row">
        <div class="col-md-4"></div>
<div class="col-md-8 grid_margin">
        <div class="pos-rel pull-right filter-box-agg">
            <g:if test="${!isArchived}">
                <div class="pos-ab1 form-inline filter-box">
                    <div class="form-group  mtp-25">
                        <label id="filter-label pull-left">Filter</label>
                        <select id="advanced-filter" class="form-control advanced-filter-dropdown"></select>
                        <a href="#" class="edit-filter pv-ic ${buttonClass == "hidden"?"invisible":""}" title="Edit Filter" tabindex="0" id="editAdvancedFilter"><i
                                class="mdi mdi-pencil font-24 "></i></a>
                        <a href="#" class="add-filter pv-ic ${buttonClass}" title="Add Filter" tabindex="0" id="addAdvancedFilter"><i
                                class="mdi  mdi-plus font-24 "></i></a>
                    </div>
                </div>
            </g:if>
        <!------------------=================--------------pinned icon code started-------------================----------------------->
            <span style="padding-top:20px">
                <!------------==================-------Field selection code start------==========------------------->
                <a href="javascript:void(0)" class="grid-pin collapse theme-color" id="ic-configureEvdasFields" data-fieldconfigurationbarid="evdasFields" data-pagetype="evdas_alert" title="${message(code: 'app.label.choosefields')}">
                    <i class="mdi mdi-settings-outline font-24"></i>
                </a>
        <!------------------==================------------Field selection code end---------------================---------------------->

        <!------------------==================------------------export to code start-------------================---------------------->
                <span class="grid-pin collapse theme-color dropdown" id="exportTypes">
                    <span  tabindex="0" class="dropdown-toggle saveViewPanel grid-menu-tooltip dropbtn" data-title="Save View" data-toggle="dropdown" accesskey="s" title="${message(code: 'app.label.exportTo')}">
                        <i class="mdi mdi-export font-24"></i>
                        <span class="caret hidden"></span>
                    </span>
                    <ul class="dropdown-menu save-list dropdown-content dropdown-menu-right ddm-padding">
                        <span class="dropdown">
                            <strong class="font-12 title-spacing">Export</strong>
                            <g:if test="${isLatest && appType == Constants.AlertConfigType.SINGLE_CASE_ALERT}">
                                <li><g:link controller="${domainName}" action="exportReport" class="m-r-30"
                                            params="${[outputFormat: ReportFormat.XLSX,id : id, alertName : domainName,
                                                       callingScreen: callingScreen, length: '-1', start: '0', adhocRun: false,cumulativeExport: true]}">
                                    <img src="/signal/assets/excel.gif" class="m-r-10 export-report" height="16" width="16" /><g:message code="cumulative.export" />
                                </g:link></li>
                            </g:if>
                            <li><g:link controller="${domainName}" action="exportReport" class="m-r-30 export-report"
                                        params="${[outputFormat: ReportFormat.DOCX,id : id, alertName : domainName,
                                                   callingScreen: callingScreen, length: '-1', start: '0', adhocRun: false]}">
                                <img src="/signal/assets/word-icon.png" class="m-r-10 export-report" height="16" width="16" /><g:message code="save.as.word" />
                            </g:link>
                            </li>
                            <li><g:link controller="${domainName}" action="exportReport" class="m-r-30 export-report"
                                        params="${[outputFormat: ReportFormat.XLSX,id : id, alertName : domainName,
                                                   callingScreen: callingScreen, length: '-1', start: '0', adhocRun: false]}">
                                <img src="/signal/assets/excel.gif" class="m-r-10 export-report" height="16" width="16" /><g:message code="save.as.excel" />
                            </g:link></li>
                            <li><g:link controller="${domainName}" action="exportReport" class="m-r-30 export-report"
                                        params="${[outputFormat: ReportFormat.PDF,id : id, alertName : domainName,
                                                   callingScreen: callingScreen, length: '-1', start: '0', adhocRun: false]}">
                                <img src="/signal/assets/pdf-icon.jpg" class="m-r-10 export-report" height="16" width="16" /><g:message code="save.as.pdf" />
                            </g:link></li>
                            <g:set var="userService" bean="userService"/>
                            <g:if test="${callingScreen != Constants.Commons.DASHBOARD && callingScreen != Constants.Commons.TRIGGERED_ALERTS}">
                                <STRONG class="font-12 title-spacing">Detection Summary</STRONG>
                                <li>
                                    <a target="_blank"
                                       href="exportSignalSummaryReport?outputFormat=DOCX&id=${executedConfigId}&cumulative=${cumulative}"><img
                                            src="/signal/assets/word-icon.png" class="m-r-10 export-report" height="16" width="16"/><g:message
                                            code="save.as.word"/></a>
                                </li>
                                <li>
                                    <a target="_blank"
                                       href="exportSignalSummaryReport?outputFormat=XLSX&id=${executedConfigId}&cumulative=${cumulative}"><img
                                            src="/signal/assets/excel.gif" class="m-r-10 export-report" height="16" width="16"/><g:message
                                            code="save.as.excel"/></a>
                                </li>
                                <li>
                                    <a target="_blank"
                                       href="exportSignalSummaryReport?outputFormat=PDF&id=${executedConfigId}&cumulative=${cumulative}"><img
                                            src="/signal/assets/pdf-icon.jpg" class="m-r-10 export-report" height="16" width="16"/><g:message
                                            code="save.as.pdf"/></a>
                                </li>
                            </g:if>
                        </span>
                    </ul>
                </span>
        <!-------------------==================--------------export to code end---------------==========------------------->

            <!------------==================-------Save view code start------==========------------------->
                <g:if test="${!isArchived && (callingScreen == Constants.Commons.REVIEW || callingScreen == Constants.Commons.DASHBOARD)}">
                    <span class="grid-pin collapse theme-color dropdown ${buttonClass}" id="saveViewTypes2">
                        <span  tabindex="0" class="dropdown-toggle saveViewPanel grid-menu-tooltip dropbtn" data-toggle="dropdown" accesskey="s" title="${message(code: 'app.label.saveview')}">
                            <i class="mdi mdi-star-outline font-24"></i>
                            <span class="caret hidden"></span>
                        </span>

                        <ul class="dropdown-menu save-list dropdown-content dropdown-menu-right ddm-padding">
                            <li><a href="#" tabindex="0" class="updateView ps5"><span><g:message code="default.button.save.label"/></span></a></li>
                            <li><a href="#" tabindex="0" class="saveView ps5"><span><g:message code="app.label.saveAs"/></span></a></li>
                            <li><a href="#" tabindex="0"  class="editView ps5"><span><g:message code="default.button.edit.label"/></span></a></li>
                        </ul>
                    </span>
                </g:if>
            <!------------==================----------Save view code end----------==========------------------->
        <!---------------==================-----Disposition code start---------------------------==========-------------->
                <g:if test="${callingScreen == Constants.Commons.REVIEW || callingScreen == Constants.Commons.DASHBOARD}">
                    <span class="grid-pin collapse theme-color dropdown" id="dispositionTypes2">
                        <span  tabindex="0" class="dropdown-toggle grid-menu-tooltip dropbtn" data-toggle="dropdown" accesskey="s" title="${message(code: 'app.label.disposition')}">
                            <i class="mdi mdi-checkbox-marked-outline font-24"></i>
                        </span>
                        <ul class="dropdown-menu dropdown-content dropdown-menu-right export-type-list disposition-ico disposition-li" id="dispositionTypes"></ul>
                    </span>
                </g:if>
        <!------------------==================-------Disposition code end------------------------================---------------------->
        <!------------------==================------------Filter code start----------------------================---------------------->
                <a href="javascript:void(0)" class="grid-pin collapse theme-color" id="ic-toggle-column-filters" data-fieldconfigurationbarid="quantitativeFields" data-pagetype="quantitative_alert" title="${message(code: 'app.label.filter')}">
                    <i class="mdi mdi-filter-outline font-24"></i>
                </a>
        <!------------------==================------------------Filter code end------------------================---------------------->
            <!------------------==================-------Alert level disposition code start----------================---------------------->
                <g:if test="${alertDispositionList && callingScreen != Constants.Commons.DASHBOARD}">
                    <span class="grid-pin collapse theme-color dropdown ${buttonClass}" id="ic-alert-level-disposition">
                        <span tabindex="0" class="dropdown-toggle grid-menu-tooltip" data-toggle="dropdown" accesskey="l" title="${message(code: 'alert.level.disposition.label')}">
                            <i class="mdi mdi-alpha-d-box-outline font-24"></i>
                        </span>
                        <ul class="dropdown-menu dropdown-content col-min-150 dropdown-menu-right ul-ddm-child alert-disp-dmm">
                            <g:render template="/includes/popover/bulkDispositionSelect" model="[alertDispositionList: alertDispositionList]"/>
                        </ul>
                    </span>
                </g:if>
            <!---------------===============--------Alert level disposition code closed--------------==========-------------->


            </span>


        <!------------------=================--------------pinned icon code closed--------------================----------------------->

        <div class="ico-menu pull-right">
            <!------------------------------------------------------------list menu code start----------------------------------------------------------------------------->
            <span class="dropdown grid-icon" id="reportIconMenu">
                <span class="dropdown-toggle" data-toggle="dropdown" style="float: left;">
                    <i class="mdi mdi-format-list-bulleted mr-10 font-24 pull-right mr-10" style="margin-right:5px"></i></span>
                <ul class="dropdown-menu ul-ddm">
                    <!------------------export to menu code start--------------------------->

                    <li class="li-pin-width">
                        <a class="test field-config-bar-toggle text-left-prop ul-ddm-hide" href="#" id="configureEvdasFields" data-fieldconfigurationbarid="evdasFields"
                           data-pagetype="evdas_alert" data-backdrop="true" data-container="columnList-container" title="" accesskey="c"
                           data-original-title="Choose Fields">
                            <i class="mdi mdi-settings-outline"></i>
                            <span tabindex="0">
                                Field Selection
                            </span>
                        </a>
                        <a href="javascript:void(0)" class="text-right-prop">
                        <span class="pin-unpin-rotate pull-right mdi mdi-pin" data-id="#ic-configureEvdasFields" title="Pin to top"  data-toggle="collapse"  data-title="Field selection"></span>
                        </a>
                </li>


                    <li class="li-pin-width dropdown-submenu">
                        <a class="test text-left-prop" href="#">
                            <i class="mdi mdi-export"></i> <span tabindex="0" class="dropdown-toggle exportPanel grid-menu-tooltip" data-toggle="dropdown"  accesskey="x">
                            Export To
                        </span></a>
                        <a href="javascript:void(0)" class="text-right-prop">
                            <span class="pin-unpin-rotate pull-right mdi mdi-pin" data-toggle="collapse" title="Pin to top" data-title="Export To" data-id="#exportTypes"></span>
                        </a>
                        <ul class="dropdown-menu export-type-list ul-ddm-child" id="exportTypes">
                            <strong class="font-12">Export</strong>
                            <g:if test="${isLatest && appType == Constants.AlertConfigType.SINGLE_CASE_ALERT}">
                                <li><g:link controller="${domainName}" action="exportReport" class="m-r-30 export-report"
                                            params="${[outputFormat: ReportFormat.XLSX,id : id, alertName : domainName,
                                                       callingScreen: callingScreen, length: '-1', start: '0', adhocRun: false,cumulativeExport: true]}">
                                    <img src="/signal/assets/excel.gif" class="m-r-10 export-report" height="16" width="16" /><g:message code="cumulative.export" />
                                </g:link></li>
                            </g:if>
                            <li><g:link controller="${domainName}" action="exportReport" class="m-r-30 export-report"
                                        params="${[outputFormat: ReportFormat.DOCX,id : id, alertName : domainName,
                                                   callingScreen: callingScreen, length: '-1', start: '0', adhocRun: false]}">
                                <img src="/signal/assets/word-icon.png" class="m-r-10 export-report" height="16" width="16" /><g:message code="save.as.word" />
                            </g:link>
                            </li>
                            <li><g:link controller="${domainName}" action="exportReport" class="m-r-30 export-report"
                                        params="${[outputFormat: ReportFormat.XLSX,id : id, alertName : domainName,
                                                   callingScreen: callingScreen, length: '-1', start: '0', adhocRun: false]}">
                                <img src="/signal/assets/excel.gif" class="m-r-10 export-report" height="16" width="16" /><g:message code="save.as.excel" />
                            </g:link></li>
                            <li><g:link controller="${domainName}" action="exportReport" class="m-r-30 export-report"
                                        params="${[outputFormat: ReportFormat.PDF,id : id, alertName : domainName,
                                                   callingScreen: callingScreen, length: '-1', start: '0', adhocRun: false]}">
                                <img src="/signal/assets/pdf-icon.jpg" class="m-r-10 export-report" height="16" width="16" /><g:message code="save.as.pdf" />
                            </g:link></li>
                            <g:set var="userService" bean="userService"/>
                            <g:if test="${callingScreen != Constants.Commons.DASHBOARD && callingScreen != Constants.Commons.TRIGGERED_ALERTS}">
                                <STRONG class="font-12">Detection Summary</STRONG>
                                <li>
                                    <a target="_blank"
                                       href="exportSignalSummaryReport?outputFormat=DOCX&id=${executedConfigId}&cumulative=${cumulative}"><img
                                            src="/signal/assets/word-icon.png" class="m-r-10 export-report" height="16" width="16"/><g:message
                                            code="save.as.word"/></a>
                                </li>
                                <li>
                                    <a target="_blank"
                                       href="exportSignalSummaryReport?outputFormat=XLSX&id=${executedConfigId}&cumulative=${cumulative}"><img
                                            src="/signal/assets/excel.gif" class="m-r-10 export-report" height="16" width="16"/><g:message
                                            code="save.as.excel"/></a>
                                </li>
                                <li>
                                    <a target="_blank"
                                       href="exportSignalSummaryReport?outputFormat=PDF&id=${executedConfigId}&cumulative=${cumulative}"><img
                                            src="/signal/assets/pdf-icon.jpg" class="m-r-10 export-report" height="16" width="16"/><g:message
                                            code="save.as.pdf"/></a>
                                </li>
                            </g:if>
                        </ul>
                    </li>
                <!-----------------export to menu code end------------------------------->

                    <g:if test="${!isArchived && (callingScreen == Constants.Commons.REVIEW || callingScreen == Constants.Commons.DASHBOARD)}">
                        <li class="li-pin-width dropdown-submenu ${buttonClass}">
                            <a class="test text-left-prop" href="#">
                                <i class="mdi mdi-star-outline"></i>
                                <span  tabindex="0" class="dropdown-toggle saveViewPanel grid-menu-tooltip" data-toggle="dropdown" accesskey="s">
                                    Save View
                                </span>
                            </a>
                            <a href="javascript:void(0)" class="text-right-prop">
                                <span class="pin-unpin-rotate pull-right mdi mdi-pin" data-toggle="collapse" title="Pin to top" data-id="#saveViewTypes2" data-title="Save View"></span>
                            </a>
                            <ul class="dropdown-menu save-list col-min-150 ul-ddm-child" id="saveViewTypes">
                                <li><a href="#" tabindex="0" class="updateView ps5"><span><g:message code="default.button.save.label"/></span></a></li>
                                <li><a href="#" tabindex="0" class="saveView ps5"><span><g:message code="app.label.saveAs"/></span></a></li>
                                <li><a href="#" tabindex="0"  class="editView ps5"><span><g:message code="default.button.edit.label"/></span></a></li>
                            </ul>
                        </li>
                    </g:if>


                    <g:if test="${callingScreen == Constants.Commons.REVIEW || callingScreen == Constants.Commons.DASHBOARD}">
                        <li class="li-pin-width dropdown-submenu">
                            <a class="test text-left-prop"  href="#">
                                <i class="mdi mdi-checkbox-marked-outline"></i>
                                <span class="dropdown-toggle" data-target="#bulkDispositionPopover" data-toggle="modal-popover" tabindex="0" role="button" accesskey="1"  aria-expanded="true"  title="${message(code :'alert.level.disposition.label')}">
                                    <g:message code="app.label.disposition" />
                                </span>
                            </a>
                            <a href="javascript:void(0)" class="text-right-prop">
                                <span class="pin-unpin-rotate pull-right mdi mdi-pin" title="Pin to top"  data-toggle="collapse" data-id="#dispositionTypes2" data-title="Disposition"></span>
                            </a>
                            <ul class="dropdown-menu export-type-list disposition-ico" id="dispositionTypes"></ul>
                        </li>
                    </g:if>

                    <g:if test="${!isArchived}">
                        <li class="li-pin-width">
                            <a class="test text-left-prop ul-ddm-hide" id="toggle-column-filters" href="#">
                                <i class="mdi mdi-filter-outline"></i>
                                <span data-title="Filters" class="test" tabindex="0" role="button" accesskey="y">
                                    Filters</span>
                            </a>
                            <a href="javascript:void(0)" class="text-right-prop">
                                <span class="pin-unpin-rotate pull-right mdi mdi-pin" title="Pin to top" data-id="#ic-toggle-column-filters" data-toggle="collapse" data-title="Filters"></span>
                            </a>
                        </li>
                    </g:if>


                    <g:if test="${alertDispositionList && callingScreen != Constants.Commons.DASHBOARD}">
                        <li class="li-pin-width dropdown-submenu ${buttonClass}">
                            <a tabindex="0" class="m-r-10 grid-menu-tooltip">
                                <i class="mdi mdi-alpha-d-box-outline" title="${message(code :'alert.level.disposition.label')}"></i>
                                <span data-target="#bulkDispositionPopover" role="button"
                                      data-toggle="modal-popover" data-placement="left" accesskey="l">
                                    Alert level disposition</span>
                            </a>
                            <a href="javascript:void(0)" class="text-right-prop">
                                <span class="pin-unpin-rotate pull-right mdi mdi-pin"  data-toggle="collapse" data-id="#ic-alert-level-disposition" title="Pin to top"  data-title="Alert level Disposition"></span>
                            </a>
                            <ul class="dropdown-menu col-min-150 alert-disp-dmm">
                                <g:render template="/includes/popover/bulkDispositionSelect" model="[alertDispositionList: alertDispositionList]"/>
                            </ul>
                        </li>
                    </g:if>


                </ul>
            </span>

        <!-------------------------------------list menu ended---------------------------------------------------------------------------------------------->
            <g:if test="${callingScreen == Constants.Commons.TRIGGERED_ALERTS}">
                <div class="pull-right m-r-10 form-inline">
                    <label>Frequency</label>
                    <select id="frequencyNames" class="form-control">
                        <g:each in="${freqNames}" var="i">
                            <option value="${i}">${i}</option>
                        </g:each>
                    </select>
                </div>
            </g:if>
        </div>


           </div>
        </div>
    </div>
</div>


    <div class="row">
        <div class="views-list col-md-12 bookmarkstrip bookmark-10 bookmark-pos">
        </div>
        <div class="col-md-12">
            <table id="alertsDetailsTable" class="auto-scale row-border no-shadow hover" width="100%">
                <thead>
                <tr id="alertsDetailsTableRow" class="evdas-header-row">
                    <th data-idx="0" data-field="selected"><input id="select-all" type="checkbox"/>

                        <div class="th-label"></div></th>
                    <th data-idx="3" data-field="dropdown"><div class="th-label" style="width: 20px"></div></th>
                    <g:if test="${callingScreen == Constants.Commons.TRIGGERED_ALERTS}">
                        <th data-idx="1" data-field="name"><div class="th-label" data-field="name"><g:message code="app.label.evdas.details.column.name"/></div></th>
                    </g:if>

                    <g:if test="${isPriorityEnabled}">
                        <th data-idx="4" data-field="priority"><div class="th-label" style="width: 20px" data-field="priority"><g:message code="app.label.evdas.details.column.priority"/></div></th>
                    </g:if>
                    <th data-idx="5" data-field="actions"><div class="th-label" style="width: 20px" data-field="actions"><g:message code="app.label.evdas.details.column.actions"/></div></th>
                    <g:if test="${callingScreen == Constants.Commons.DASHBOARD}">
                        <th data-idx="1" data-field="name"><div class="th-label" data-field="name"><g:message code="app.label.evdas.details.column.name"/></div></th>
                    </g:if>
                    <th data-idx="6" data-field="substance"><div class="th-label" data-field="substance"><g:message code="app.label.evdas.details.column.substance"/></div></th>
                    <th data-idx="7" data-field="soc"><div class="th-label" data-field="soc"><g:message code="app.label.evdas.details.column.soc"/></div></th>
                    <th data-idx="11" data-field="pt"><div class="th-label" data-field="pt"><g:message code="app.label.evdas.details.column.pt"/></div></th>
                    <th data-idx="8" data-field="hlgt"><div class="th-label" data-field="hlgt"><g:message code="app.label.evdas.details.column.hlgt"/></div></th>
                    <th data-idx="9" data-field="hlt"><div class="th-label" data-field="hlt"><g:message code="app.label.evdas.details.column.hlt"/></div></th>
                    <th data-idx="10" data-field="smqNarrow"><div class="th-label" data-field="smqNarrow"><g:message code="app.label.evdas.details.column.smqNarrow"/></div></th>
                    <th data-idx="12" data-field="impEvents"><div class="th-label"><g:message code="app.label.evdas.details.column.impEvents"/></div></th>
                    <th data-idx="48" data-field="listed">
                        <div class="th-label" data-field="listed"><g:message code="app.label.listed"/></div>
                    </th>
                    <th data-idx="47" data-field="dmeIme">
                        <div class="th-label" data-field="dmeIme"><g:message code="app.label.imedme"/></div>
                    </th>
                    <th data-idx="13" data-field="newEv">
                        <div class="th-label dateRange" data-field="newEv">
                            <div class="stacked-cell-center-top"><g:message code="app.label.evdas.details.column.newEv"/></div>
                            <div class="stacked-cell-center-top"><g:message code="app.label.evdas.details.column.totEv"/></div>
                        </div>
                    </th>
                    <th data-idx="14" data-field="newEea">
                        <div class="th-label dateRange" data-field="newEea">
                            <div class="stacked-cell-center-top"><g:message code="app.label.evdas.details.column.newEea"/></div>
                            <div class="stacked-cell-center-top"><g:message code="app.label.evdas.details.column.totEea"/></div>
                        </div>
                    </th>
                    <th data-idx="15" data-field="newHcp">
                        <div class="th-label dateRange" data-field="newHcp">
                            <div class="stacked-cell-center-top"><g:message code="app.label.evdas.details.column.newHcp"/></div>
                            <div class="stacked-cell-center-top"><g:message code="app.label.evdas.details.column.totHcp"/></div>
                        </div>
                    </th>
                    <th data-idx="16" data-field="newSerious">
                        <div class="th-label dateRange" data-field="newSerious">
                            <div class="stacked-cell-center-top"><g:message code="app.label.evdas.details.column.newSerious"/></div>
                            <div class="stacked-cell-center-top"><g:message code="app.label.evdas.details.column.totSerious"/></div>
                        </div>
                    </th>
                    <th data-idx="17" data-field="newMedErr">
                        <div class="th-label dateRange" data-field="newMedErr">
                            <div class="stacked-cell-center-top"><g:message code="app.label.evdas.details.column.newMedErr"/></div>
                            <div class="stacked-cell-center-top"><g:message code="app.label.evdas.details.column.totMedErr"/></div>
                        </div>
                    </th>
                    <th data-idx="18" data-field="newObs">
                        <div class="th-label dateRange" data-field="newObs">
                            <div class="stacked-cell-center-top"><g:message code="app.label.evdas.details.column.newObs"/></div>
                            <div class="stacked-cell-center-top"><g:message code="app.label.evdas.details.column.totObs"/></div>
                        </div>
                    </th>
                    <th data-idx="19" data-field="newFatal">
                        <div class="th-label dateRange" data-field="newFatal">
                            <div class="stacked-cell-center-top"><g:message code="app.label.evdas.details.column.newFatal"/></div>
                            <div class="stacked-cell-center-top"><g:message code="app.label.evdas.details.column.totFatal"/></div>
                        </div>
                    </th>
                    <th data-idx="20" data-field="newRc">
                        <div class="th-label dateRange" data-field="newRc">
                            <div class="stacked-cell-center-top"><g:message code="app.label.evdas.details.column.newRc"/></div>
                            <div class="stacked-cell-center-top"><g:message code="app.label.evdas.details.column.totRc"/></div>
                        </div>
                    </th>
                    <th data-idx="21" data-field="newLit">
                        <div class="th-label dateRange" data-field="newLit">
                            <div class="stacked-cell-center-top"><g:message code="app.label.evdas.details.column.newLit"/></div>
                            <div class="stacked-cell-center-top"><g:message code="app.label.evdas.details.column.totLit"/></div>
                        </div>
                    </th>
                    <th data-idx="22" data-field="newPaed">
                        <div class="th-label dateRange" data-field="newPaed">
                            <div class="stacked-cell-center-top"><g:message code="app.label.evdas.details.column.newPaed"/></div>
                            <div class="stacked-cell-center-top"><g:message code="app.label.evdas.details.column.totPaed"/></div>
                        </div>
                    </th>
                    <th data-idx="23" data-field="ratioRorPaedVsOthers">
                        <div class="th-label dateRange" data-field="ratioRorPaedVsOthers">
                            <div class="stacked-cell-center-top"><g:message code="app.label.evdas.details.column.ratioRorPaedVsOthers"/></div>
                        </div>
                    </th>
                    <th data-idx="25" data-field="newGeria">
                        <div class="th-label dateRange" data-field="newGeria">
                            <div class="stacked-cell-center-top"><g:message code="app.label.evdas.details.column.newGeria"/></div>

                            <div class="stacked-cell-center-top"><g:message code="app.label.evdas.details.column.totGeria"/></div>
                        </div>
                    </th>
                    <th data-idx="26" data-field="ratioRorGeriatrVsOthers">
                        <div class="th-label dateRange" data-field="ratioRorGeriatrVsOthers">
                            <div class="stacked-cell-center-top"><g:message code="app.label.evdas.details.column.ratioRorGeriatrVsOthers"/></div>
                        </div>
                    </th>
                    <th data-idx="27" data-field="sdrGeratr">
                        <div class="th-label dateRange" data-field="sdrGeratr">
                            <div class="stacked-cell-center-top"><g:message code="app.label.evdas.details.column.sdrGeratr"/></div>
                        </div>
                    </th>
                    <th data-idx="28" data-field="newSpont">
                        <div class="th-label dateRange" data-field="newSpont">
                            <div class="stacked-cell-center-top"><g:message code="app.label.evdas.details.column.newSpont"/></div>

                            <div class="stacked-cell-center-top"><g:message code="app.label.evdas.details.column.totSpont"/></div>
                        </div>
                    </th>
                    <th data-idx="29" data-field="totSpontEurope">
                        <div class="th-label dateRange" data-field="totSpontEurope">
                            <div class="stacked-cell-center-top"><g:message code="app.label.evdas.details.column.totSpontEurope"/></div>
                        </div>
                    </th>
                    <th data-idx="30" data-field="totSpontNAmerica">
                        <div class="th-label dateRange" data-field="totSpontNAmerica">
                            <div class="stacked-cell-center-top"><g:message code="app.label.evdas.details.column.totSpontNAmerica"/></div>
                        </div>
                    </th>
                    <th data-idx="31" data-field="totSpontJapan">
                        <div class="th-label dateRange" data-field="totSpontJapan">
                            <div class="stacked-cell-center-top"><g:message code="app.label.evdas.details.column.totSpontJapan"/></div>
                        </div>
                    </th>
                    <th  data-idx="32" data-field="totSpontAsia">
                        <div class="th-label dateRange" data-field="totSpontAsia">
                            <div class="stacked-cell-center-top"><g:message code="app.label.evdas.details.column.totSpontAsia"/></div>
                        </div>
                    </th>
                    <th data-idx="33" data-field="totSpontRest">
                        <div class="th-label dateRange" data-field="totSpontRest">
                            <div class="stacked-cell-center-top"><g:message code="app.label.evdas.details.column.totSpontRest"/></div>
                        </div>
                    </th>
                    <th data-idx="34" data-field="rorValue">
                        <div class="th-label dateRange" data-field="rorValue">
                            <div class="stacked-cell-center-top"><g:message code="app.label.evdas.details.column.rorValue"/></div>
                        </div>
                    </th>
                    <th data-idx="35" data-field="sdr">
                        <div class="th-label dateRange" data-field="sdr">
                            <div class="stacked-cell-center-top"><g:message code="app.label.evdas.details.column.sdr"/></div>
                        </div>
                    </th>
                    <th align="center" data-idx="44" data-field="disposition">
                        <div class="th-label" data-field="disposition">
                            <g:message code="app.label.current.disposition"/></div></th>
                    <th align="center" data-idx="43" data-field="signalsAndTopics">
                        <div class="th-label" data-field="signalsAndTopics">
                            <g:message code="app.label.evdas.details.column.signalsAndTopics"/>
                        </div>
                    </th>
                    <th align="center" data-idx="48" data-field="currentDisposition">
                        <div class="th-label" data-field="currentDisposition">
                            <g:message code="app.label.disposition.to"/></div>
                    </th>
                    <th data-idx="45" data-field="assignedTo">
                        <div class="th-label" data-field="assignedTo">
                            <g:message code="app.label.assigned.to"/>
                        </div></th>
                    <th data-idx="24" data-field="sdrPaed">
                        <div class="th-label dateRange" data-field="sdrPaed">
                            <div class="stacked-cell-center-top"><g:message code="app.label.evdas.details.column.sdrPaed"/></div>
                        </div>
                    </th>
                    <th data-idx="36" data-field="europeRor">
                        <div class="th-label dateRange" data-field="europeRor">
                            <div class="stacked-cell-center-top"><g:message code="app.label.evdas.details.column.europeRor"/></div>
                        </div>
                    </th>
                    <th data-idx="37" data-field="northAmericaRor">
                        <div class="th-label dateRange" data-field="northAmericaRor">
                            <div class="stacked-cell-center-top"><g:message code="app.label.evdas.details.column.northAmericaRor"/></div>
                        </div>
                    </th>
                    <th data-idx="38" data-field="japanRor">
                        <div class="th-label dateRange" data-field="japanRor">
                            <div class="stacked-cell-center-top"><g:message code="app.label.evdas.details.column.japanRor"/></div>
                        </div>
                    </th>
                    <th data-idx="39" data-field="asiaRor">
                        <div class="th-label dateRange" data-field="asiaRor">
                            <div class="stacked-cell-center-top"><g:message code="app.label.evdas.details.column.asiaRor"/></div>
                        </div>
                    </th>
                    <th data-idx="40" data-field="restRor">
                        <div class="th-label dateRange" data-field="restRor">
                            <div class="stacked-cell-center-top"><g:message code="app.label.evdas.details.column.restRor"/></div>
                        </div>
                    </th>
                    <th data-idx="41" data-field="changes">
                        <div class="th-label dateRange" data-field="changes">
                            <div class="stacked-cell-center-top"><g:message code="app.label.evdas.details.column.changes"/></div>
                        </div>
                    </th>
                    <th data-idx="46" data-field="dueDate">
                        <div class="th-label" data-field="dueDate"><g:message code="app.label.evdas.details.column.dueDate"/></div>
                    </th>
                    <th data-idx="56" data-field="justification">
                        <div class="th-label" data-field="justification"><g:message
                                code="app.label.qualitative.details.column.justification"/></div>
                    </th>
                    <th data-idx="56" data-field="dispPerformedBy">
                        <div class="th-label" data-field="dispPerformedBy"><g:message
                                code="app.label.qualitative.details.column.dispPerformedBy"/></div>
                    </th>
                    <th data-idx="56" data-field="dispLastChange">
                        <div class="th-label" data-field="dispLastChange"><g:message
                                code="app.label.qualitative.details.column.dispLastChange"/></div>
                    </th>
                    <th data-idx="56" data-field="comment">
                        <div class="th-label" data-field="comment"><g:message
                                code="app.label.qualitative.details.column.comments"/></div>
                    </th>
                </tr>
                </thead>
            </table>
        </div>

        %{--<div class="col-md-12 flags">
            <div class="box-inline m-r-25"><span class="glyphicon glyphicon-tag m-r-5"
                                                 style="color: green"></span>Auto Flagged</div>

            <div class="box-inline m-r-25"><span class="glyphicon glyphicon-tag m-r-5"
                                                 style="color: purple"></span>Auto Flagged & Previously Reviewed
            </div>

            <div class="box-inline m-r-25"><span class="glyphicon glyphicon-tag m-r-5"
                                                 style="color: orange"></span>New</div>

            <div class="box-inline m-r-25"><span class="glyphicon glyphicon-tag m-r-5"
                                                 style="color: darkblue"></span>Previously Reviewed</div>
        </div>--}%
    </div>
</div>
<g:render template="/includes/modals/evdas_case_drill_down" model="[id : id, alertType: Constants.AlertConfigType.EVDAS_ALERT]"/>