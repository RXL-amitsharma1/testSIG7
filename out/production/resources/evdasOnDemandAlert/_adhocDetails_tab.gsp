<%@ page import="com.rxlogix.Constants; com.rxlogix.enums.ReportFormat" %>

<style>
table.dataTable thead > tr > th {
    padding-left: 8px;
    padding-right: 8px;
}

div.dataTables_wrapper {
    margin: 0 auto;
}

input[type=checkbox] {
    margin: 8px 0 0 !important;
}
</style>

<script>
    $(document).ready(function () {
        $('.search-box').show();
        $('.bookmarks-list-opener').hover(function () {
            $('.dropdown-toggle', this).trigger('click');
        });
    });
</script>

<div class="row">
    <div class="panel-heading pv-sec-heading">
        <div class="row">
            <div class="col-md-4"></div>

            <div class="col-md-8 grid_margin">
                <div class="pos-rel pull-right filter-box-agg">
                    <div class="pos-ab1 form-inline filter-box">
                        <div class="form-group  mtp-25">
                            <label id="filter-label pull-left">Filter</label>
                            <select id="advanced-filter" class="form-control advanced-filter-dropdown"></select>
                            <a href="#" class="edit-filter pv-ic ${buttonClass == "hidden"?"invisible":""}" title="Edit Filter" tabindex="0"
                               id="editAdvancedFilter"><i
                                    class="mdi mdi-pencil font-24 "></i></a>
                            <a href="#" class="add-filter pv-ic ${buttonClass}" title="Add Filter" tabindex="0"
                               id="addAdvancedFilter"><i
                                    class="mdi  mdi-plus font-24 "></i></a>
                        </div>
                    </div>
                    <!------------------=================--------------pinned icon code started-------------================----------------------->
                    <span style="padding-top:20px">
                        <!------------==================-------Field selection code start------==========------------------->
                        <a href="javascript:void(0)" class="grid-pin collapse theme-color"
                           id="ic-configureEvdasOnDemandFields" data-fieldconfigurationbarid="evdasOnDemandFields"
                           data-pagetype="evdas_on_demand_alert" title="${message(code: 'app.label.choosefields')}">
                            <i class="mdi mdi-settings-outline font-24"></i>
                        </a>
                        <!------------------==================------------Field selection code end---------------================---------------------->

                        <!------------------==================------------------export to code start-------------================---------------------->
                        <span class="grid-pin collapse theme-color dropdown" id="exportTypes">
                            <span tabindex="0" class="dropdown-toggle saveViewPanel grid-menu-tooltip dropbtn"
                                  data-toggle="dropdown" accesskey="s" title="${message(code: 'app.label.exportTo')}">
                                <i class="mdi mdi-export font-24"></i>
                                <span class="caret hidden"></span>
                            </span>
                            <ul class="dropdown-menu save-list dropdown-content dropdown-menu-right ddm-padding">
                                <span class="dropdown">
                                    <strong class="font-12 title-spacing">Export</strong>
                                    <li><g:link controller="evdasOnDemandAlert" action="exportReport" class="m-r-30"
                                                params="${[outputFormat : ReportFormat.DOCX, id: id, alertName: "evdasOnDemandAlert",
                                                           callingScreen: callingScreen, length: '-1', start: '0', adhocRun: false]}">
                                        <img src="/signal/assets/word-icon.png" class="m-r-10" height="16"
                                             width="16"/><g:message code="save.as.word"/>
                                    </g:link>
                                    </li>
                                    <li><g:link controller="evdasOnDemandAlert" action="exportReport" class="m-r-30"
                                                params="${[outputFormat : ReportFormat.XLSX, id: id, alertName: "evdasOnDemandAlert",
                                                           callingScreen: callingScreen, length: '-1', start: '0', adhocRun: false]}">
                                        <img src="/signal/assets/excel.gif" class="m-r-10" height="16"
                                             width="16"/><g:message code="save.as.excel"/>
                                    </g:link></li>
                                    <li><g:link controller="evdasOnDemandAlert" action="exportReport" class="m-r-30"
                                                params="${[outputFormat : ReportFormat.PDF, id: id, alertName: "evdasOnDemandAlert",
                                                           callingScreen: callingScreen, length: '-1', start: '0', adhocRun: false]}">
                                        <img src="/signal/assets/pdf-icon.jpg" class="m-r-10" height="16"
                                             width="16"/><g:message code="save.as.pdf"/>
                                    </g:link></li>
                                    <g:set var="userService" bean="userService"/>
                                </span>
                            </ul>
                        </span>
                    <!-------------------==================--------------export to code end---------------==========------------------->

                    <!------------==================-------Save view code start------==========------------------->
                        <g:if test="${callingScreen == Constants.Commons.REVIEW}">
                            <span class="grid-pin collapse theme-color dropdown ${buttonClass}" id="saveViewTypes2">
                                <span tabindex="0" class="dropdown-toggle saveViewPanel grid-menu-tooltip dropbtn"
                                       data-toggle="dropdown" accesskey="s" title="${message(code: 'app.label.saveview')}" >
                                    <i class="mdi mdi-star-outline font-24"></i>
                                    <span class="caret hidden"></span>
                                </span>

                                <ul class="dropdown-menu save-list dropdown-content dropdown-menu-right ddm-padding">
                                    <li><a href="#" tabindex="0" class="updateView ps5"><span><g:message
                                            code="default.button.save.label"/></span></a></li>
                                    <li><a href="#" tabindex="0" class="saveView ps5"><span><g:message
                                            code="app.label.saveAs"/></span></a></li>
                                    <li><a href="#" tabindex="0" class="editView ps5"><span><g:message
                                            code="default.button.edit.label"/></span></a></li>
                                </ul>
                            </span>
                        </g:if>
                    <!------------==================----------Save view code end----------==========------------------->
                    <!------------------==================------------Filter code start----------------------================---------------------->
                        <a href="javascript:void(0)" class="grid-pin collapse theme-color" id="ic-toggle-column-filters"
                           data-fieldconfigurationbarid="evdasOnDemandFields" data-pagetype="evdas_on_demand_alert" title="${message(code: 'app.label.filter')}">
                            <i class="mdi mdi-filter-outline font-24"></i>
                        </a>
                        <!------------------==================------------------Filter code end------------------================---------------------->

                    </span>


                    <!------------------=================--------------pinned icon code closed--------------================----------------------->

                    <div class="ico-menu pull-right">
                        <!------------------------------------------------------------list menu code start----------------------------------------------------------------------------->
                        <span class="dropdown grid-icon" id="reportIconMenu">
                            <span class="dropdown-toggle" data-toggle="dropdown" style="float: left;">
                                <i class="mdi mdi-format-list-bulleted mr-10 font-24 pull-right mr-10"
                                   style="margin-right:5px"></i></span>
                            <ul class="dropdown-menu ul-ddm">
                                <!------------------export to menu code start--------------------------->

                                <li class="li-pin-width">
                                    <a class="test field-config-bar-toggle text-left-prop ul-ddm-hide" href="#"
                                       id="configureEvdasOnDemandFields"
                                       data-fieldconfigurationbarid="evdasOnDemandFields"
                                       data-pagetype="evdas_alert" data-backdrop="true"
                                       data-container="columnList-container" title="" accesskey="c"
                                       data-original-title="Choose Fields">
                                        <i class="mdi mdi-settings-outline"></i>
                                        <span tabindex="0">
                                            Field Selection
                                        </span>
                                    </a>
                                    <a href="javascript:void(0)" class="text-right-prop">
                                        <span class="pin-unpin-rotate pull-right mdi mdi-pin"
                                              data-id="#ic-configureEvdasOnDemandFields" title="Pin to top"
                                              data-toggle="collapse" data-title="Field selection"></span>
                                    </a>
                                </li>


                                <li class="li-pin-width dropdown-submenu">
                                    <a class="test text-left-prop" href="#">
                                        <i class="mdi mdi-export"></i> <span tabindex="0"
                                                                             class="dropdown-toggle exportPanel grid-menu-tooltip"
                                                                             data-toggle="dropdown" accesskey="x">
                                        Export To
                                    </span></a>
                                    <a href="javascript:void(0)" class="text-right-prop">
                                        <span class="pin-unpin-rotate pull-right mdi mdi-pin" data-toggle="collapse"
                                              title="Pin to top" data-title="Export To" data-id="#exportTypes"></span>
                                    </a>
                                    <ul class="dropdown-menu export-type-list ul-ddm-child" id="exportTypes">
                                        <strong class="font-12">Export</strong>
                                        <li><g:link controller="evdasOnDemandAlert" action="exportReport" class="m-r-30"
                                                    params="${[outputFormat : ReportFormat.DOCX, id: id, alertName: "evdasOnDemandAlert",
                                                               callingScreen: callingScreen, length: '-1', start: '0', adhocRun: false]}">
                                            <img src="/signal/assets/word-icon.png" class="m-r-10" height="16"
                                                 width="16"/><g:message code="save.as.word"/>
                                        </g:link>
                                        </li>
                                        <li><g:link controller="evdasOnDemandAlert" action="exportReport" class="m-r-30"
                                                    params="${[outputFormat : ReportFormat.XLSX, id: id, alertName: "evdasOnDemandAlert",
                                                               callingScreen: callingScreen, length: '-1', start: '0', adhocRun: false]}">
                                            <img src="/signal/assets/excel.gif" class="m-r-10" height="16"
                                                 width="16"/><g:message code="save.as.excel"/>
                                        </g:link></li>
                                        <li><g:link controller="evdasOnDemandAlert" action="exportReport" class="m-r-30"
                                                    params="${[outputFormat : ReportFormat.PDF, id: id, alertName: "evdasOnDemandAlert",
                                                               callingScreen: callingScreen, length: '-1', start: '0', adhocRun: false]}">
                                            <img src="/signal/assets/pdf-icon.jpg" class="m-r-10" height="16"
                                                 width="16"/><g:message code="save.as.pdf"/>
                                        </g:link></li>
                                    </ul>
                                </li>
                            <!-----------------export to menu code end------------------------------->

                                <g:if test="${callingScreen == Constants.Commons.REVIEW}">
                                    <li class="li-pin-width dropdown-submenu ${buttonClass}">
                                        <a class="test text-left-prop" href="#">
                                            <i class="mdi mdi-star-outline"></i>
                                            <span tabindex="0" class="dropdown-toggle saveViewPanel grid-menu-tooltip"
                                                  data-toggle="dropdown" accesskey="s">
                                                Save View
                                            </span>
                                        </a>
                                        <a href="javascript:void(0)" class="text-right-prop">
                                            <span class="pin-unpin-rotate pull-right mdi mdi-pin" data-toggle="collapse"
                                                  title="Pin to top" data-id="#saveViewTypes2"
                                                  data-title="Save View"></span>
                                        </a>
                                        <ul class="dropdown-menu save-list col-min-150 ul-ddm-child" id="saveViewTypes">
                                            <li><a href="#" tabindex="0" class="updateView ps5"><span><g:message
                                                    code="default.button.save.label"/></span></a></li>
                                            <li><a href="#" tabindex="0" class="saveView ps5"><span><g:message
                                                    code="app.label.saveAs"/></span></a></li>
                                            <li><a href="#" tabindex="0" class="editView ps5"><span><g:message
                                                    code="default.button.edit.label"/></span></a></li>
                                        </ul>
                                    </li>
                                </g:if>

                                <li class="li-pin-width">
                                    <a class="test text-left-prop ul-ddm-hide" id="toggle-column-filters" href="#">
                                        <i class="mdi mdi-filter-outline"></i>
                                        <span data-title="Filters" class="test" tabindex="0" role="button"
                                              accesskey="y">
                                            Filters</span>
                                    </a>
                                    <a href="javascript:void(0)" class="text-right-prop">
                                        <span class="pin-unpin-rotate pull-right mdi mdi-pin" title="Pin to top"
                                              data-id="#ic-toggle-column-filters" data-toggle="collapse"
                                              data-title="Filters"></span>
                                    </a>
                                </li>

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
        <div class="views-list col-md-12 bookmarkstrip">
        </div>

        <div class="col-md-12">
            <table id="alertsDetailsTable" class="auto-scale row-border no-shadow hover" width="100%">
                <thead>
                <tr id="alertsDetailsTableRow">
                    <th data-idx="6" data-field="substance">
                        <div class="th-label" data-field="substance">
                            <div class="th-label">
                                <g:message code="app.label.evdas.details.column.substance"/>
                            </div>
                        </div>
                    </th>
                    <th data-idx="7" data-field="soc"><div class="th-label" data-field="soc"><g:message
                            code="app.label.evdas.details.column.soc"/></div></th>
                    <th data-idx="11" data-field="pt"><div class="th-label" data-field="pt"><g:message
                            code="app.label.evdas.details.column.pt"/></div></th>
                    <th data-idx="8" data-field="hlgt"><div class="th-label" data-field="hlgt"><g:message
                            code="app.label.evdas.details.column.hlgt"/></div></th>
                    <th data-idx="9" data-field="hlt"><div class="th-label" data-field="hlt"><g:message
                            code="app.label.evdas.details.column.hlt"/></div></th>
                    <th data-idx="10" data-field="smqNarrow"><div class="th-label" data-field="smqNarrow"><g:message
                            code="app.label.evdas.details.column.smqNarrow"/></div></th>
                    <th data-idx="12" data-field="impEvents"><div class="th-label"><g:message
                            code="app.label.evdas.details.column.impEvents"/></div></th>
                    <th data-idx="48" data-field="listed">
                        <div class="th-label" data-field="listed"><g:message code="app.label.listed"/></div>
                    </th>
                    <th data-idx="47" data-field="dmeIme">
                        <div class="th-label" data-field="dmeIme"><g:message code="app.label.imedme"/></div>
                    </th>
                    <th data-idx="13" data-field="newEv">
                        <div class="th-label dateRange" data-field="newEv">
                            <div class="stacked-cell-center-top"><g:message
                                    code="app.label.evdas.details.column.newEv"/></div>

                            <div class="stacked-cell-center-top"><g:message
                                    code="app.label.evdas.details.column.totEv"/></div>
                        </div>
                    </th>
                    <th data-idx="14" data-field="newEea">
                        <div class="th-label dateRange" data-field="newEea">
                            <div class="stacked-cell-center-top"><g:message
                                    code="app.label.evdas.details.column.newEea"/></div>

                            <div class="stacked-cell-center-top"><g:message
                                    code="app.label.evdas.details.column.totEea"/></div>
                        </div>
                    </th>
                    <th data-idx="15" data-field="newHcp">
                        <div class="th-label dateRange" data-field="newHcp">
                            <div class="stacked-cell-center-top"><g:message
                                    code="app.label.evdas.details.column.newHcp"/></div>

                            <div class="stacked-cell-center-top"><g:message
                                    code="app.label.evdas.details.column.totHcp"/></div>
                        </div>
                    </th>
                    <th data-idx="16" data-field="newSerious">
                        <div class="th-label dateRange" data-field="newSerious">
                            <div class="stacked-cell-center-top"><g:message
                                    code="app.label.evdas.details.column.newSerious"/></div>

                            <div class="stacked-cell-center-top"><g:message
                                    code="app.label.evdas.details.column.totSerious"/></div>
                        </div>
                    </th>
                    <th data-idx="17" data-field="newMedErr">
                        <div class="th-label dateRange" data-field="newMedErr">
                            <div class="stacked-cell-center-top"><g:message
                                    code="app.label.evdas.details.column.newMedErr"/></div>

                            <div class="stacked-cell-center-top"><g:message
                                    code="app.label.evdas.details.column.totMedErr"/></div>
                        </div>
                    </th>
                    <th data-idx="18" data-field="newObs">
                        <div class="th-label dateRange" data-field="newObs">
                            <div class="stacked-cell-center-top"><g:message
                                    code="app.label.evdas.details.column.newObs"/></div>

                            <div class="stacked-cell-center-top"><g:message
                                    code="app.label.evdas.details.column.totObs"/></div>
                        </div>
                    </th>
                    <th data-idx="19" data-field="newFatal">
                        <div class="th-label dateRange" data-field="newFatal">
                            <div class="stacked-cell-center-top"><g:message
                                    code="app.label.evdas.details.column.newFatal"/></div>

                            <div class="stacked-cell-center-top"><g:message
                                    code="app.label.evdas.details.column.totFatal"/></div>
                        </div>
                    </th>
                    <th data-idx="20" data-field="newRc">
                        <div class="th-label dateRange" data-field="newRc">
                            <div class="stacked-cell-center-top"><g:message
                                    code="app.label.evdas.details.column.newRc"/></div>

                            <div class="stacked-cell-center-top"><g:message
                                    code="app.label.evdas.details.column.totRc"/></div>
                        </div>
                    </th>
                    <th data-idx="21" data-field="newLit">
                        <div class="th-label dateRange" data-field="newLit">
                            <div class="stacked-cell-center-top"><g:message
                                    code="app.label.evdas.details.column.newLit"/></div>

                            <div class="stacked-cell-center-top"><g:message
                                    code="app.label.evdas.details.column.totLit"/></div>
                        </div>
                    </th>
                    <th data-idx="22" data-field="newPaed">
                        <div class="th-label dateRange" data-field="newPaed">
                            <div class="stacked-cell-center-top"><g:message
                                    code="app.label.evdas.details.column.newPaed"/></div>

                            <div class="stacked-cell-center-top"><g:message
                                    code="app.label.evdas.details.column.totPaed"/></div>
                        </div>
                    </th>
                    <th data-idx="23" data-field="ratioRorPaedVsOthers">
                        <div class="th-label dateRange" data-field="ratioRorPaedVsOthers">
                            <div class="stacked-cell-center-top"><g:message
                                    code="app.label.evdas.details.column.ratioRorPaedVsOthers"/></div>
                        </div>
                    </th>
                    <th data-idx="25" data-field="newGeria">
                        <div class="th-label dateRange" data-field="newGeria">
                            <div class="stacked-cell-center-top"><g:message
                                    code="app.label.evdas.details.column.newGeria"/></div>

                            <div class="stacked-cell-center-top"><g:message
                                    code="app.label.evdas.details.column.totGeria"/></div>
                        </div>
                    </th>
                    <th data-idx="26" data-field="ratioRorGeriatrVsOthers">
                        <div class="th-label dateRange" data-field="ratioRorGeriatrVsOthers">
                            <div class="stacked-cell-center-top"><g:message
                                    code="app.label.evdas.details.column.ratioRorGeriatrVsOthers"/></div>
                        </div>
                    </th>
                    <th data-idx="27" data-field="sdrGeratr">
                        <div class="th-label dateRange" data-field="sdrGeratr">
                            <div class="stacked-cell-center-top"><g:message
                                    code="app.label.evdas.details.column.sdrGeratr"/></div>
                        </div>
                    </th>
                    <th data-idx="28" data-field="newSpont">
                        <div class="th-label dateRange" data-field="newSpont">
                            <div class="stacked-cell-center-top"><g:message
                                    code="app.label.evdas.details.column.newSpont"/></div>

                            <div class="stacked-cell-center-top"><g:message
                                    code="app.label.evdas.details.column.totSpont"/></div>
                        </div>
                    </th>
                    <th data-idx="29" data-field="totSpontEurope">
                        <div class="th-label dateRange" data-field="totSpontEurope">
                            <div class="stacked-cell-center-top"><g:message
                                    code="app.label.evdas.details.column.totSpontEurope"/></div>
                        </div>
                    </th>
                    <th data-idx="30" data-field="totSpontNAmerica">
                        <div class="th-label dateRange" data-field="totSpontNAmerica">
                            <div class="stacked-cell-center-top"><g:message
                                    code="app.label.evdas.details.column.totSpontNAmerica"/></div>
                        </div>
                    </th>
                    <th data-idx="31" data-field="totSpontJapan">
                        <div class="th-label dateRange" data-field="totSpontJapan">
                            <div class="stacked-cell-center-top"><g:message
                                    code="app.label.evdas.details.column.totSpontJapan"/></div>
                        </div>
                    </th>
                    <th data-idx="32" data-field="totSpontAsia">
                        <div class="th-label dateRange" data-field="totSpontAsia">
                            <div class="stacked-cell-center-top"><g:message
                                    code="app.label.evdas.details.column.totSpontAsia"/></div>
                        </div>
                    </th>
                    <th data-idx="33" data-field="totSpontRest">
                        <div class="th-label dateRange" data-field="totSpontRest">
                            <div class="stacked-cell-center-top"><g:message
                                    code="app.label.evdas.details.column.totSpontRest"/></div>
                        </div>
                    </th>
                    <th data-idx="34" data-field="rorValue">
                        <div class="th-label dateRange" data-field="rorValue">
                            <div class="stacked-cell-center-top"><g:message
                                    code="app.label.evdas.details.column.rorValue"/></div>
                        </div>
                    </th>
                    <th data-idx="35" data-field="sdr">
                        <div class="th-label dateRange" data-field="sdr">
                            <div class="stacked-cell-center-top"><g:message
                                    code="app.label.evdas.details.column.sdr"/></div>
                        </div>
                    </th>
                    <th data-idx="24" data-field="sdrPaed">
                        <div class="th-label dateRange" data-field="sdrPaed">
                            <div class="stacked-cell-center-top"><g:message
                                    code="app.label.evdas.details.column.sdrPaed"/></div>
                        </div>
                    </th>
                    <th data-idx="36" data-field="europeRor">
                        <div class="th-label dateRange" data-field="europeRor">
                            <div class="stacked-cell-center-top"><g:message
                                    code="app.label.evdas.details.column.europeRor"/></div>
                        </div>
                    </th>
                    <th data-idx="37" data-field="northAmericaRor">
                        <div class="th-label dateRange" data-field="northAmericaRor">
                            <div class="stacked-cell-center-top"><g:message
                                    code="app.label.evdas.details.column.northAmericaRor"/></div>
                        </div>
                    </th>
                    <th data-idx="38" data-field="japanRor">
                        <div class="th-label dateRange" data-field="japanRor">
                            <div class="stacked-cell-center-top"><g:message
                                    code="app.label.evdas.details.column.japanRor"/></div>
                        </div>
                    </th>
                    <th data-idx="39" data-field="asiaRor">
                        <div class="th-label dateRange" data-field="asiaRor">
                            <div class="stacked-cell-center-top"><g:message
                                    code="app.label.evdas.details.column.asiaRor"/></div>
                        </div>
                    </th>
                    <th data-idx="40" data-field="restRor">
                        <div class="th-label dateRange" data-field="restRor">
                            <div class="stacked-cell-center-top"><g:message
                                    code="app.label.evdas.details.column.restRor"/></div>
                        </div>
                    </th>
                    <th data-idx="41" data-field="changes">
                        <div class="th-label dateRange" data-field="changes">
                            <div class="stacked-cell-center-top"><g:message
                                    code="app.label.evdas.details.column.changes"/></div>
                        </div>
                    </th>
                </tr>
                </thead>
            </table>
        </div>
    </div>
</div>

<g:render template="/includes/modals/evdas_case_drill_down" model="[id : id, alertType: Constants.AlertConfigType.EVDAS_ALERT_DEMAND]"/>