<%@ page import="com.rxlogix.Constants; com.rxlogix.enums.ReportFormat; grails.util.Holders" %>

<style>
table.dataTable thead > tr > th {
    padding-left: 5px;
    padding-right: 5px;
}

.yadcf-filter {
    width: 100px;
}

div.dataTables_wrapper {
    margin: 0 auto;
}
</style>
<script>
    $(document).ready(function () {
        $('.search-box').show();
    });
</script>

<div class="row">

    <div class="panel-heading pv-sec-heading">
        <div class="row">

            <div class="col-md-4"></div>
       <div class="col-md-8 grid_margin">
            <div class="pos-rel pull-right filter-box-agg">
                <g:if test="${isArchived != true}">
                    <div class="pos-ab1 form-inline filter-box">
                        <div class="form-group mtp-25">
                            <label id="filter-label pull-left">Filter</label>
                            <select id="advanced-filter" class="form-control advanced-filter-dropdown"></select>
                            <a href="#" class="edit-filter pv-ic ${buttonClass == "hidden"?"invisible":""}" title="Edit Filter" tabindex="0" id="editAdvancedFilter"><i
                                    class="mdi mdi-pencil font-24"></i></a>
                            <a href="#" class="add-filter pv-ic ${buttonClass}" title="Add Filter" tabindex="0" id="addAdvancedFilter"><i
                                    class="mdi  mdi-plus font-24"></i></a>
                        </div>
                    </div>
                </g:if>
            <!------------------=================--------------pinned icon code started-------------================----------------------->
                <span style="padding-top:20px">
                    <!------------==================-------Field selection code start------==========------------------->
                    <a href="javascript:void(0)" class="grid-pin collapse theme-color" id="ic-configureQualitativeFields" data-fieldconfigurationbarid="qualitativeFields" data-pagetype="quantitative_alert" title="${message(code: 'app.label.choosefields')}">
                        <i class="mdi mdi-settings-outline font-24"></i>
                    </a>
                    <!------------==================---------------Field selection code end----------==========------------------->

                    <!------------==================---------------Detailed view code start----------==========------------------->
                    <a class="grid-pin collapse theme-color" href="#" id="ic-detailed-view-checkbox"
                       data-is-case-detail-view="${!isCaseDetailView}"
                       data-is-case-series-alert="${isCaseSeriesAlert}"
                       data-calling-screen="${callingScreen}" data-config-id="${id}"
                       data-product-name="${productName}" data-event-name="${eventName}"
                       data-url="${createLink(controller: 'singleCaseAlert', action: 'details')}" accesskey="v" title="${message(code: 'app.label.detailedview')}">
                        <span>
                            <i class='mdi mdi-file-document-box-outline font-24'></i>

                        </span>
                    </a>
                    <!------------==================---------------Detailed view code end----------==========------------------->

                    <!------------==================--------------export to code start---------------==========------------------->
                    <span class="grid-pin collapse theme-color dropdown" id="exportTypes">
                        <span  tabindex="0" class="dropdown-toggle saveViewPanel grid-menu-tooltip dropbtn" data-toggle="dropdown" accesskey="s" title="${message(code: 'app.label.exportTo')}">
                            <i class="mdi mdi-export font-24"></i>
                            <span class="caret hidden"></span>
                        </span>
                   %{-- <ul class="dropdown-menu export-type-list ul-ddm-child" id="exportTypes">--}%
                    <ul class="dropdown-menu save-list dropdown-content dropdown-menu-right ddm-padding">
                        <strong class="font-12 title-spacing">Export</strong>
                        <g:if test="${!isFaers && !isVaers && !isVigibase && !isJader && isLatest && appType == Constants.AlertConfigType.SINGLE_CASE_ALERT}">
                            <li><g:link elementId="cumulativeExport" controller="${domainName}" action="exportReport" class="m-r-30"
                                        params="${[outputFormat: ReportFormat.XLSX,id : id, alertName : domainName,
                                                   callingScreen: callingScreen, length: '-1', start: '0', adhocRun: false,cumulativeExport: true, isArchived: false, isVaers: isVaers, isVigibase: isVigibase]}">
                                <img src="/signal/assets/excel.gif" class="m-r-10" height="16" width="16" /><g:message code="cumulative.export" />
                            </g:link></li>
                        </g:if>
                        <li><g:link controller="${domainName}" action="exportReport" class="m-r-30"
                                    params="${[outputFormat: ReportFormat.DOCX,id : id, alertName : domainName,
                                               callingScreen: callingScreen, length: '-1', start: '0', adhocRun: false, isArchived: isArchived, soc: soc, 'isCaseSeries': isCaseSeries, eventName:eventName, isVaers: isVaers, isVigibase: isVigibase]}">
                            <img src="/signal/assets/word-icon.png" class="m-r-10" height="16" width="16" /><g:message code="save.as.word" />
                        </g:link>
                        </li>
                        <li><g:link elementId="exportExcel" controller="${domainName}" action="exportReport" class="m-r-30"
                                    params="${[outputFormat: ReportFormat.XLSX,id : id, alertName : domainName,
                                               callingScreen: callingScreen, length: '-1', start: '0', adhocRun: false, isArchived: isArchived, soc: soc, 'isCaseSeries': isCaseSeries, eventName:eventName, isVaers: isVaers, isVigibase: isVigibase]}">
                            <img src="/signal/assets/excel.gif" class="m-r-10" height="16" width="16" /><g:message code="save.as.excel" />
                        </g:link></li>
                        <li><g:link controller="${domainName}" action="exportReport" class="m-r-30"
                                    params="${[outputFormat: ReportFormat.PDF,id : id, alertName : domainName, isVaers: isVaers, isVigibase: isVigibase,
                                               callingScreen: callingScreen, length: '-1', start: '0', adhocRun: false, isArchived: isArchived, 'soc': soc, 'isCaseSeries': isCaseSeries, eventName:eventName]}">
                            <img src="/signal/assets/pdf-icon.jpg" class="m-r-10" height="16" width="16" /><g:message code="save.as.pdf" />
                        </g:link></li>
                        <g:if test="${!isVaers && !isFaers && !isJader && callingScreen == Constants.Commons.REVIEW}">
                            <li class="dropdown-submenu li-pin-width dropdown-case-pin">
                                <a class="test text-left-prop option-case">
                                    <i class="fa fa-file-text-o fa-fw text-blue" height="16" width="16" style="padding-left: 0 !important;"></i> <span
                                        tabindex="0" class="dropdown-toggle exportPanel grid-menu-tooltip"
                                        data-toggle="dropdown" accesskey="x">
                                    <g:message code="option.case.form"/>
                                </span></a>
                                <ul class="dropdown-menu export-type-list ul-ddm-child case-form-dropdown hide">
                                    <li class="generate-case-form"><g:link controller="${domainName}"
                                                                        action="exportCaseForm" class="m-r-30"
                                                                        params="${[outputFormat : ReportFormat.PDF, id: id, alertName: domainName,
                                                                                   callingScreen: callingScreen, length: '-1', start: '0', adhocRun: false, isCaseSeries: isCaseSeries, isArchived: isArchived, soc: soc, eventName: eventName]}">
                                        <g:message code="option.generate.case.form"/>
                                    </g:link>
                                    </li>
                                    <li class="view-case-form-list" style="cursor: pointer;"><a><g:message
                                            code="default.button.view.label"/></a></li>
                                </ul>
                            </li>
                        </g:if>
                        <g:set var="userService" bean="userService"/>

                        <g:if test="${!isFaers && !isVaers && !isVigibase && !isJader && callingScreen != Constants.Commons.DASHBOARD && callingScreen != Constants.Commons.TRIGGERED_ALERTS}">
                            <STRONG class="font-12 title-spacing">Detection Summary</STRONG>
                            <li>
                                <a target="_blank"
                                   href="exportSignalSummaryReport?outputFormat=DOCX&id=${executedConfigId}&cumulative=${cumulative}&isArchived=${isArchived}"><img
                                        src="/signal/assets/word-icon.png" class="m-r-10" height="16" width="16"/><g:message
                                        code="save.as.word"/></a>
                            </li>
                            <li>
                                <a target="_blank"
                                   href="exportSignalSummaryReport?outputFormat=XLSX&id=${executedConfigId}&cumulative=${cumulative}&isArchived=${isArchived}"><img
                                        src="/signal/assets/excel.gif" class="m-r-10" height="16" width="16"/><g:message
                                        code="save.as.excel"/></a>
                            </li>
                            <li>
                                <a target="_blank"
                                   href="exportSignalSummaryReport?outputFormat=PDF&id=${executedConfigId}&cumulative=${cumulative}&isArchived=${isArchived}"><img
                                        src="/signal/assets/pdf-icon.jpg" class="m-r-10" height="16" width="16"/><g:message
                                        code="save.as.pdf"/></a>
                            </li>
                        </g:if>
                    </ul>

                    </span>
                    <!--------------==================----------export to code end------------============------------------------>
                <!------------==================-------Save view code start------==========------------------->
                    <g:if test="${callingScreen == Constants.Commons.REVIEW || callingScreen == Constants.Commons.DASHBOARD}">
                        <span class="grid-pin collapse theme-color dropdown ${buttonClass}" id="saveViewTypes1">
                            <span  tabindex="0" class="dropdown-toggle saveViewPanel grid-menu-tooltip dropbtn" title="${message(code: 'app.label.saveview')}" data-toggle="dropdown" accesskey="s">
                                <i class="mdi mdi-star-outline font-24"></i>
                                <span class="caret hidden"></span>
                            </span>
                            <ul class="dropdown-menu save-list dropdown-content dropdown-menu-right ddm-padding" id="view-types-menu">
                                <li><a href="#" tabindex="0" class="updateView ps5"><span><g:message code="default.button.save.label"/></span></a></li>
                                <li><a href="#" tabindex="0" class="saveView ps5"><span><g:message code="app.label.saveAs"/></span></a></li>
                                <li><a href="#" tabindex="0"  class="editView ps5"><span><g:message code="default.button.edit.label"/></span></a></li>
                            </ul>
                        </span>
                    </g:if>
                <!------------==================----------Save view code end-------==========------------------->
                <!------------==================-------Disposition code start------==========------------------->
                    <g:if test="${!isFaers && !isVaers && !isVigibase && !isJader && (callingScreen == Constants.Commons.REVIEW || callingScreen == Constants.Commons.DASHBOARD)}">
                        <span class="grid-pin collapse theme-color dropdown" id="dispositionTypes2">
                            <span  tabindex="0" class="dropdown-toggle grid-menu-tooltip dropbtn" data-toggle="dropdown" accesskey="s" title="${message(code: 'app.label.disposition')}">
                                <i class="mdi mdi-checkbox-marked-outline font-24"></i>
                            </span>
                            <ul class="dropdown-menu dropdown-content dropdown-menu-right export-type-list disposition-ico disposition-li" id="dispositionTypes"></ul>
                        </span>
                    </g:if>
                <!------------==================-------Disposition code end------==========-------------------->
                <!------------==================-------Add case code start------==========------------------->
                    <g:if test="${(!isFaers) && (!isVaers) && (!isVigibase)  && (!isJader) && (!isCaseSeries) && (callingScreen == Constants.Commons.REVIEW) }">
                            <a href="javascript:void(0)" class="grid-pin collapse theme-color dropdown ${buttonClass}" id="ic-addCase" title="${message(code: 'app.label.add.case')}">
                                <i class="mdi mdi-plus-outline font-24"></i>
                                <span id="addCase" tabindex="0" class="grid-menu-tooltip" accesskey="a"></span>
                            </a>
                    </g:if>
                <!------------==================-------Add case code end------==========-------------------->

                <!------------==================-------Filter code start------==========-------------------->
                    <a href="javascript:void(0)" class="grid-pin collapse theme-color" id="ic-toggle-column-filters" title="${message(code: 'app.label.filter')}">
                        <i class="mdi mdi-filter-outline font-24"></i>
                    </a>
                    <g:if test="${!isVaers && !isFaers && !isVigibase && !isJader && callingScreen != Constants.Commons.DASHBOARD}">
                        <a  class="grid-pin collapse theme-color" id="ic-report" title="${message(code: 'app.label.generate.report')}" href="#"
                            data-url="${createLink(controller: 'template', action: 'index', params: [configId: id, hasReviewerAccess: hasSignalViewAccessAccess,isAggScreen: false])}">
                          <i class="mdi mdi-note-plus-outline font-24"></i>
                        </a>
                    </g:if>
                    <!------------==================-------Filter code end--------==========------------------->
                <!------------==================-------Alert level disposition code start------==========------------------->
                    <g:if test="${alertDispositionList && !isFaers && !isVaers && !isVigibase && !isJader && callingScreen != Constants.Commons.DASHBOARD}">
                        <span class="grid-pin collapse theme-color dropdown ${buttonClass}" id="ic-alert-level-disposition">
                            <span tabindex="0" class="dropdown-toggle grid-menu-tooltip" data-toggle="dropdown" accesskey="l" title="${message(code: 'alert.level.disposition.label')}">
                                <i class="mdi mdi-alpha-d-box-outline font-24"></i>
                            </span>
                            <ul class="dropdown-menu dropdown-content col-min-150 dropdown-menu-right ul-ddm-child alert-disp-dmm">
                                <g:render template="/includes/popover/bulkDispositionSelect" model="[alertDispositionList: alertDispositionList]"/>
                            </ul>
                        </span>
                    </g:if>
                <!---------------===============--------Alert level disposition code closed---------==========-------------->
                <!------------==================-------Generated report code start------==========------------------->
                    <span class="grid-pin collapse theme-color dropdown" id="ic-generated-report">
                        <g:if test="${reportUrl && !onlyShareAccess}">
                            <a tabindex="0" target="_blank" href="${reportUrl}">
                                <i data-title="Generated Report" class="mdi mdi-table grid-menu-tooltip font-24" accesskey="g"></i>
                            </a>
                        </g:if>
                    </span>
                <!------------==================-------Generated report code end------==========------------------->
                <!------------==================-------Data Analysis code started----------==================------>
                <!------------==================-------Data analysis code start------==========------------------->
                    <g:if test="${!isCaseSeries && !onlyShareAccess && callingScreen != Constants.Commons.DASHBOARD}">
                     <span class="grid-pin collapse theme-color dropdown" id="ic-data-analysis">
                        <span  tabindex="0" class="dropdown-toggle saveViewPanel grid-menu-tooltip" data-toggle="dropdown" accesskey="s" title="${message(code: 'app.label.dataAnalysis')}">
                            <i class="mdi mdi-chart-bar font-24"></i>
                        </span>
                        <ul class="dropdown-menu dropdown-content dropdown-menu-right ul-ddm-child data-analysis-button">
                            <g:each var="entry" in="${analysisStatus}">
                                <g:if test="${entry.key == 'PR_DATE_RANGE'}">
                                    <g:if test="${entry.value.status == 2}">
                                        <li><a href="${entry.value.url}" data-status=${entry.value.status}
                                        target="_blank"><span class="hyperLinkColor">Current Period Analysis</span>
                                        </a></li>
                                    </g:if>
                                    <g:else>
                                        <li class="${buttonClass}"><a href="javascript:void(0)"
                                               class="generateCurrentAnalysis" data-id=${executedConfigId} data-status=${entry.value.status}><span>${entry.value.message}</span>
                                        </a></li>
                                    </g:else>

                                </g:if>
                                <g:else>
                                    <g:if test="${entry.value.status == 2}">
                                        <li><a href="${entry.value.url}" data-status=${entry.value.status}
                                        target="_blank"><span class="hyperLinkColor">Cumulative Period Analysis</span>
                                        </a></li>
                                    </g:if>
                                    <g:else>
                                        <li class="${buttonClass}"><a href="javascript:void(0)"
                                               class="generateCumulativeAnalysis" data-id=${executedConfigId} data-status=${entry.value.status}><span>${entry.value.message}</span>
                                        </a></li>
                                    </g:else>
                                </g:else>
                            </g:each>
                            <g:if test="${analysisFileUrl}">
                                <li><a href="${analysisFileUrl?.spotfireUrl}" target="_blank">
                                    <span class="hyperLinkColor">${analysisFileUrl?.name}</span></a></li>
                            </g:if>
                        </ul>
                    </span>
                  </g:if>
                    <!------------==================-------Data analysis code end------------------==========------------------->
                <!------------=================--------Data Analysis  code end------------===================------>

                </span>
<!---------------===================--------------pinned code end------------==================----------------->

           <span class="ico-menu pull-right">
               <!------------------------------------------list menu code start------------------------------------------------------------->
               <span class="dropdown grid-icon" id="reportIconMenu">
                   <span class="dropdown-toggle" data-toggle="dropdown" style="float: left;">
                       <i class="mdi mdi-format-list-bulleted mr-10 font-24 pull-right mr-10" style="margin-right:5px"></i></span>
                   <ul class="dropdown-menu ul-ddm">
                       <!------------------export to menu code start--------------------------->
                       <li class="li-pin-width">
                           <a class="test field-config-bar-toggle text-left-prop ul-ddm-hide" href="#" id="configureQualitativeFields" data-fieldconfigurationbarid="qualitativeFields"
                              data-backdrop="true" data-container="columnList-container" accesskey="c" title="Choose Fields">
                               <i class="mdi mdi-settings-outline"></i>
                               <span tabindex="0">
                                   Field Selection
                               </span>
                           </a>
                           <a href="javascript:void(0)" class="text-right-prop" >
                               <span class="pin-unpin-rotate pull-right mdi mdi-pin" title="Pin to top" data-id="#ic-configureQualitativeFields" data-toggle="collapse"  data-title="Field selection"></span>
                           </a>
                       </li>
                       <li class="li-pin-width">
                           <a class="test field-config-bar-toggle text-left-prop ul-ddm-hide" href="#" id="detailed-view-checkbox"
                              data-is-case-detail-view="${!isCaseDetailView}"
                              data-is-case-series-alert="${isCaseSeriesAlert}"
                              data-calling-screen="${callingScreen}" data-config-id="${id}"
                              data-product-name="${productName}" data-event-name="${eventName}" data-soc="${soc}"
                              data-url="${createLink(controller: 'singleCaseAlert', action: 'details')}" accesskey="v">
                               <span>
                                   <i class='mdi mdi-file-document-box-outline'></i>
                                   Detailed View
                               </span></a>
                           <a href="javascript:void(0)" class="text-right-prop" >
                               <span class="pin-unpin-rotate pull-right mdi mdi-pin" title="Pin to top" data-id="#ic-detailed-view-checkbox" data-toggle="collapse"  data-title="Detail View"></span>
                           </a>
                       </li>
                       <!-----------------export to menu code started------------------------------->
                       <li class="dropdown-submenu li-pin-width">
                       <a class="test text-left-prop" href="#">
                           <i class="mdi mdi-export"></i> <span tabindex="0" class="dropdown-toggle exportPanel grid-menu-tooltip" data-toggle="dropdown"  accesskey="x">
                           Export To
                       </span></a>
                       <a href="javascript:void(0)" class="text-right-prop">
                           <span class="pin-unpin-rotate pull-right mdi mdi-pin" data-toggle="collapse" title="Pin to top" data-title="Export To" data-id="#exportTypes"></span>
                       </a>
                       <ul class="dropdown-menu export-type-list ul-ddm-child" id="exportTypes">
                           <strong class="font-12">Export</strong>
                           <g:if test="${!isFaers && !isVaers && !isVigibase && !isJader && isLatest && appType == Constants.AlertConfigType.SINGLE_CASE_ALERT}">
                               <li><g:link elementId="cumulativeExport" controller="${domainName}" action="exportReport" class="m-r-30 export-report"
                                           params="${[outputFormat: ReportFormat.XLSX,id : id, alertName : domainName,
                                                      callingScreen: callingScreen, length: '-1', start: '0', adhocRun: false,cumulativeExport: true, isArchived: false, aggExecutionId:aggExecutionId]}">
                                   <img src="/signal/assets/excel.gif" class="m-r-10" height="16" width="16" /><g:message code="cumulative.export" />
                               </g:link></li>
                           </g:if>
                           <li><g:link controller="${domainName}" action="exportReport" class="m-r-30 export-report"
                                       params="${[outputFormat: ReportFormat.DOCX,id : id, alertName : domainName,
                                                  callingScreen: callingScreen, length: '-1', start: '0', adhocRun: false, isCaseSeries: isCaseSeries,isArchived: isArchived, soc: soc, eventName:eventName, aggExecutionId:aggExecutionId]}">
                               <img src="/signal/assets/word-icon.png" class="m-r-10" height="16" width="16" /><g:message code="save.as.word" />
                           </g:link>
                           </li>
                           <li><g:link elementId="exportExcel" controller="${domainName}" action="exportReport" class="m-r-30 export-report"
                                       params="${[outputFormat: ReportFormat.XLSX,id : id, alertName : domainName,
                                                  callingScreen: callingScreen, length: '-1', start: '0', adhocRun: false, isCaseSeries: isCaseSeries,isArchived: isArchived, soc: soc, eventName:eventName, aggExecutionId:aggExecutionId]}">
                               <img src="/signal/assets/excel.gif" class="m-r-10" height="16" width="16" /><g:message code="save.as.excel" />
                           </g:link></li>
                           <li><g:link controller="${domainName}" action="exportReport" class="m-r-30 export-report"
                                       params="${[outputFormat: ReportFormat.PDF,id : id, alertName : domainName,
                                                  callingScreen: callingScreen, length: '-1', start: '0', adhocRun: false, isCaseSeries: isCaseSeries,isArchived: isArchived, soc: soc, eventName:eventName]}">
                               <img src="/signal/assets/pdf-icon.jpg" class="m-r-10" height="16" width="16" /><g:message code="save.as.pdf" />
                           </g:link></li>
                           <g:if test="${!isVaers && !isFaers && !isJader && callingScreen == Constants.Commons.REVIEW}">
                               <li class="dropdown-submenu li-pin-width dropdown-case">
                                   <a class="test text-left-prop option-case">
                                       <i class="fa fa-file-text-o fa-fw text-blue" height="16" width="16" style="padding-left: 0 !important;"></i> <span
                                           tabindex="0" class="dropdown-toggle exportPanel grid-menu-tooltip" style="padding-left: 0 !important;"
                                           data-toggle="dropdown" accesskey="x">
                                       <g:message code="option.case.form"/>
                                   </span></a>
                                   <ul class="dropdown-menu export-type-list ul-ddm-child hide">
                                       <li class="generate-case-form"><g:link controller="${domainName}"
                                                                           action="exportCaseForm" class="m-r-30"
                                                                           params="${[outputFormat : ReportFormat.PDF, id: id, alertName: domainName,
                                                                                      callingScreen: callingScreen, length: '-1', start: '0', adhocRun: false, isCaseSeries: isCaseSeries, isArchived: isArchived, soc: soc, eventName: eventName]}">
                                          <g:message code="option.generate.case.form"/>
                                       </g:link>
                                       </li>
                                       <li class="view-case-form-list"><a><g:message
                                               code="default.button.view.label"/></a></li>
                                   </ul>
                               </li>
                           </g:if>
                           <g:set var="userService" bean="userService"/>

                           <g:if test="${!isFaers && !isVaers && !isVigibase && !isJader && callingScreen != Constants.Commons.DASHBOARD && callingScreen != Constants.Commons.TRIGGERED_ALERTS}">
                               <STRONG class="font-12">Detection Summary</STRONG>
                               <li>
                                   <a target="_blank"
                                      href="exportSignalSummaryReport?outputFormat=DOCX&id=${executedConfigId}&cumulative=${cumulative}&isArchived=${isArchived}&isCaseSeries=${isCaseSeries}&soc=${soc}&eventName=${eventName}"><img
                                           src="/signal/assets/word-icon.png" class="m-r-10" height="16" width="16"/><g:message
                                           code="save.as.word"/></a>
                               </li>
                               <li>
                                   <a target="_blank"
                                      href="exportSignalSummaryReport?outputFormat=XLSX&id=${executedConfigId}&cumulative=${cumulative}&isArchived=${isArchived}&isCaseSeries=${isCaseSeries}&soc=${soc}&eventName=${eventName}"><img
                                           src="/signal/assets/excel.gif" class="m-r-10" height="16" width="16"/><g:message
                                           code="save.as.excel"/></a>
                               </li>
                               <li>
                                   <a target="_blank"
                                      href="exportSignalSummaryReport?outputFormat=PDF&id=${executedConfigId}&cumulative=${cumulative}&isArchived=${isArchived}&isCaseSeries=${isCaseSeries}&soc=${soc}&eventName=${eventName}"><img
                                           src="/signal/assets/pdf-icon.jpg" class="m-r-10" height="16" width="16"/><g:message
                                           code="save.as.pdf"/></a>
                               </li>
                           </g:if>
                       </ul>
                   </li>
                   <!-----------------export to menu code end------------------------------->
                       <g:if test="${!isArchived && (callingScreen == Constants.Commons.REVIEW || callingScreen == Constants.Commons.DASHBOARD)}">
                           <li class="li-pin-width dropdown-submenu ${buttonClass}">
                               <a class="text-left-prop" href="#">
                                   <i class="mdi mdi-star-outline"></i>
                                   <span  tabindex="0" class="dropdown-toggle saveViewPanel grid-menu-tooltip"  data-toggle="dropdown" accesskey="s">
                                       Save View
                                   </span>
                               </a>
                               <a href="javascript:void(0)" class="text-right-prop">
                                   <span class="pin-unpin-rotate pull-right mdi mdi-pin" data-toggle="collapse" title="Pin to top" data-id="#saveViewTypes1" data-title="Save View"></span>
                               </a>
                               <ul class="dropdown-menu save-list col-min-150 ul-ddm-child" id="saveViewTypes">
                                   <li><a href="#" tabindex="0" class="updateView ps5"><span>Save</span></a></li>
                                   <li><a href="#" tabindex="0" class="saveView ps5"><span>Save As</span></a></li>
                                   <li><a href="#" tabindex="0"  class="editView ps5"><span>Edit</span></a></li>
                               </ul>
                           </li>
                       </g:if>

                       <g:if test="${!isFaers && !isVaers && !isVigibase && !isJader}">
                           <li class="dropdown-submenu li-pin-width">
                               <a class="test text-left-prop"  href="#">
                                   <i class="mdi mdi-checkbox-marked-outline"></i> <span class="dropdown-toggle grid-menu-tooltip" data-toggle="dropdown" tabindex="0" role="button" accesskey="x"  aria-expanded="true">
                                   <g:message code="app.label.disposition" />
                               </span>
                               </a>
                               <a href="javascript:void(0)" class="text-right-prop">
                                   <span class="pin-unpin-rotate pull-right mdi mdi-pin" title="Pin to top"  data-toggle="collapse" data-id="#dispositionTypes2" data-title="Disposition"></span>
                               </a>
                               <ul class="dropdown-menu export-type-list disposition-ico" id="dispositionTypes"></ul>
                           </li>
                       </g:if>
                       <!--Added below check isArchieve to remove Add Case features from archieve screen PVS-55040 As per discussion with product team. -->
                       <g:if test="${(isArchived != true) && (!isFaers) && (!isVaers) && (!isVigibase) && !isJader && (!isCaseSeries) && (callingScreen == Constants.Commons.REVIEW) }">
                           <li class="dropdown-submenu li-pin-width ${buttonClass}">
                               <a class="test text-left-prop ul-ddm-hide"  href="#">
                                   <i class="mdi mdi-plus-outline"></i>
                                   <span id="addCase" tabindex="0"  title="Add Case" accesskey="a">
                                       Add Case
                                   </span>
                               </a>
                               <a href="javascript:void(0)" class="text-right-prop" >
                                   <span class="pin-unpin-rotate pull-right mdi mdi-pin" data-id="#ic-addCase" title="Pin to top"  data-toggle="collapse"  data-title="Add Case"></span>
                               </a>
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
                                   <span class="pin-unpin-rotate pull-right mdi mdi-pin" title="Pin to top" data-toggle="collapse" data-id="#ic-toggle-column-filters" data-title="Filters"></span>
                               </a>
                           </li>
                       </g:if>
                       <g:if test="${!isVaers && !isFaers && !isVigibase && !isJader && callingScreen != Constants.Commons.DASHBOARD}">
                           <li class="dropdown-submenu li-pin-width"  id="report">
                               <g:link tabindex="0" class="m-r-10 grid-menu-tooltip text-left-prop"
                                       controller="Template" target="_blank" action="index"
                                       params="${[configId: id, hasReviewerAccess: hasSignalViewAccessAccess, isAggScreen: isCaseSeries, type: type, typeFlag: typeFlag, aggExecutionId: aggExecutionId, version: version, productName: productName, eventName: eventName, alertId: alertId
                                       ]}"><i class="mdi mdi-note-plus-outline"></i> Report</g:link>
                               <a href="javascript:void(0)" class="text-right-prop">
                                   <span class="pin-unpin-rotate pull-right mdi mdi-pin" title="Pin to top"
                                         data-toggle="collapse" data-id="#ic-report"
                                         data-title="Report"></span>
                               </a>
                           </li>
                       </g:if>

                       <g:if test="${alertDispositionList && !isFaers && !isVaers && !isVigibase && !isJader && callingScreen != Constants.Commons.DASHBOARD && !isAggregateAdhoc}">
                           <li class="dropdown-submenu li-pin-width ${buttonClass}">
                               <a tabindex="0" class="m-r-10 grid-menu-tooltip text-left-prop">
                                   <i class="mdi mdi-alpha-d-box-outline" title="${message(code :'alert.level.disposition.label')}"></i>
                                   <span data-target="#bulkDispositionPopover" role="button"
                                         data-toggle="modal-popover" data-placement="left" accesskey="l">
                                       Alert level disposition</span>
                               </a>
                               <a href="javascript:void(0)" class="text-right-prop">
                                   <span class="pin-unpin-rotate pull-right mdi mdi-pin" title="Pin to top" data-toggle="collapse" data-id="#ic-alert-level-disposition" data-title="Alert level Disposition"></span>
                               </a>
                               <ul class="dropdown-menu col-min-150 alert-disp-dmm">
                                   <g:render template="/includes/popover/bulkDispositionSelect" model="[alertDispositionList: alertDispositionList]"/>
                               </ul>
                           </li>
                       </g:if>

                   <g:if test="${reportUrl && !onlyShareAccess}">
                       <li class="dropdown-submenu li-pin-width">
                               <a tabindex="0" class="text-left-prop m-r-10" target="_blank" href="${reportUrl}">
                                   <i data-title="Generated Report"
                                      class="mdi mdi-table grid-menu-tooltip" accesskey="g"></i>
                                   <span>Generated Report</span>
                               </a>
                               <a href="javascript:void(0)" class="text-right-prop">
                                   <span class="pin-unpin-rotate pull-right mdi mdi-pin" data-toggle="collapse" title="Pin to top" data-id="#ic-generated-report" data-title="Generated Report"></span>
                               </a>
                       </li>
                    </g:if>


                       <g:if test="${!isCaseSeries && Holders.config.signal.spotfire.enabled && !onlyShareAccess && callingScreen != Constants.Commons.DASHBOARD}">
                           <li class="dropdown-submenu li-pin-width">
                               <a class="text-left-prop m-r-10" href="#">
                                   <i class="mdi mdi-chart-bar"></i>
                                   <span  tabindex="0" class="dropdown-toggle saveViewPanel grid-menu-tooltip" data-toggle="dropdown" accesskey="s">
                                       Data Analysis
                                   </span>
                               </a>
                               <a href="javascript:void(0)" class="text-right-prop">
                                   <span class="pin-unpin-rotate pull-right mdi mdi-pin" title="Pin to top" data-toggle="collapse" data-id="#ic-data-analysis" data-title="Data Analysis"></span>
                               </a>
                               <ul class="dropdown-menu ul-ddm-child data-analysis-button">
                                   <g:each var="entry" in="${analysisStatus}">
                                       <g:if test="${entry.key == 'PR_DATE_RANGE'}">
                                           <g:if test="${entry.value.status == 2}">
                                               <li><a href="${entry.value.url}" data-status=${entry.value.status}
                                                      target="_blank"><span class = "hyperLinkColor">Current Period Analysis</span>
                                               </a></li>
                                           </g:if>
                                           <g:else>
                                               <li class="${buttonClass}"><a href="javascript:void(0)"
                                                      class="generateCurrentAnalysis" data-id=${executedConfigId} data-status=${entry.value.status}><span>${entry.value.message}</span>
                                               </a></li>
                                           </g:else>

                                       </g:if>
                                       <g:else>
                                           <g:if test="${entry.value.status == 2}">
                                               <li><a href="${entry.value.url}" data-status=${entry.value.status}
                                               target="_blank"><span class = "hyperLinkColor">Cumulative Period Analysis</span>
                                               </a></li>
                                           </g:if>
                                           <g:else>
                                               <li class="${buttonClass}"><a href="javascript:void(0)"
                                                      class="generateCumulativeAnalysis" data-id=${executedConfigId} data-status=${entry.value.status}><span>${entry.value.message}</span>
                                               </a></li>
                                           </g:else>
                                       </g:else>
                                   </g:each>
                                    <g:if test="${analysisFileUrl}">
                                        <li><a href="${analysisFileUrl?.spotfireUrl}" target="_blank">
                                            <span class="hyperLinkColor">${analysisFileUrl?.name}</span></a></li>
                                    </g:if>
                               </ul>
                           </li>
                       </g:if>



                   </ul>
               </span>
               <!-------------------------------------list menu ended------------------------------------------------------------->
           </span>

            </div>
        </div>
    </div>
  </div>


<div class="row">
    <div class="views-list col-md-12 bookmarkstrip ">
    </div>
</div>

    <div class="row">
        <div class="col-md-12">

            <table id="alertsDetailsTable" class="auto-scale row-border no-shadow hover">
                <thead>
                <tr id="alertsDetailsTableRow" class="single-header-row">
                    <th data-idx="0" data-field="checkbox">
                        <input id="select-all" class="alert-select-all" type="checkbox"/>
                    </th>
                    <th data-idx="3" data-field="dropdown">
                        <div class="th-label"></div>
                    </th>
                    <g:if test="${!isFaers && !isVaers && !isVigibase && !isJader}">
                        <g:if test="${isPriorityEnabled}">
                            <th data-idx="1" data-field="priority"><g:message
                                    code="app.label.qualitative.details.column.priority"/></th>
                        </g:if>
                        <th data-idx="2" data-field="actions">
                            <div class="th-label">
                                <g:message code="app.label.qualitative.details.column.actions"/>
                        </div>
                        </th>
                    </g:if>

                    <g:if test="${callingScreen == Constants.Commons.TRIGGERED_ALERTS || callingScreen == Constants.Commons.DASHBOARD}">
                        <th data-idx="5" data-field="name">
                            <div class="th-label pvi-col-md" data-field="name"><g:message
                                    code="app.label.qualitative.details.column.name"/></div>
                        </th>
                    </g:if>
                    <th data-idx="6" data-field="caseNumber" class="reportDescriptionColumn">
                    <div class="th-label" data-field="caseNumber">
                        <g:if test="${!isFaers && !isVaers && !isVigibase && !isJader}">
                            <g:if test="${columnLabelMap.containsKey('caseNumber')}">
                                ${columnLabelMap.get('caseNumber')}
                            </g:if>
                            <g:else>
                                <g:message code="${"app.label.qualitative.details.column.caseNumber.version"}"/>
                            </g:else>
                        </g:if><g:else>
                        <g:message code="${isFaers || isVaers || isVigibase ? "app.label.qualitative.details.column.caseNumber.faers":"app.label.qualitative.details.column.caseNumber"}"/>

                        </g:else>
                    </div>
                    </th>
                    <th data-idx="4" data-field="alertTags">
                        <div class="th-label" data-field="alertTags">
                            <g:message code="app.label.qualitative.details.column.alertTags"/>
                        </div>
                    </th>
                    <th data-idx="7" data-field="caseInitReceiptDate">
                        <div class="th-label" data-field="caseInitReceiptDate">
                            <g:if test="${columnLabelMap.containsKey('caseInitReceiptDate')}">
                                ${columnLabelMap.get('caseInitReceiptDate')}
                            </g:if>
                            <g:elseif test="${isJader}">
                                <g:message code="app.label.qualitative.details.column.initial.caseInitReceiptDate"/>
                            </g:elseif>
                            <g:else>
                                <g:message code="app.label.qualitative.details.column.caseInitReceiptDate"/>
                            </g:else>
                        </div>
                    </th>
                    <th data-idx="8" data-field="productName">
                        <div class="th-label" data-field="productName">
                            <g:if test="${columnLabelMap.containsKey('productName')}">
                                ${columnLabelMap.get('productName')}
                            </g:if>
                            <g:else>
                                <g:message code="app.label.qualitative.details.column.productName"/></div>
                            </g:else>
                        </div>
                    </th>
                    <th data-idx="9" data-field="pt">
                        <div class="th-label" data-field="pt">
                            <g:if test="${columnLabelMap.containsKey('pt')}">
                                ${columnLabelMap.get('pt')}
                            </g:if>
                            <g:else>
                                <g:message code="app.label.qualitative.details.column.pt"/>
                            </g:else>
                        </div>
                    </th>

                    <g:if test="${isVigibase}">
                        <th data-idx="33" data-field="age">
                            <div class="th-label" data-field="age">
                                <g:message code="app.label.qualitative.details.column.patientAge"/>
                            </div>
                        </th>
                        <th data-idx="27" data-field="gender">
                            <div class="th-label" data-field="gender">
                                <g:message code="app.label.qualitative.details.column.gender"/>
                            </div>
                        </th>
                        <th data-idx="23" data-field="caseReportType">
                            <div class="th-label" data-field="caseReportType">
                                <g:message code="app.label.qualitative.details.column.caseReportType"/>
                            </div>
                        </th>
                        <th data-idx="43" data-field="eventOutcome">
                            <div class="th-label" data-field="eventOutcome">
                                <g:message code="app.label.qualitative.details.column.eventOutcome"/>
                            </div>
                        </th>
                        <th data-idx="22" data-field="serious">
                            <div class="th-label" data-field="serious">
                                <g:message code="app.label.qualitative.details.column.assessSeriousness"/>
                            </div>
                        </th>
                        <th data-idx="42" data-field="indication">
                            <div class="th-label" data-field="indication">
                                <g:message code="app.label.qualitative.details.column.indication"/>
                            </div>
                        </th>
                        <th data-idx="19" data-field="suspProd">
                            <div class="th-label" data-field="suspProd">
                                <g:message code="app.label.qualitative.details.column.suspProd"/>
                            </div>
                        </th>
                        <th data-idx="61" data-field="allPt">
                            <div class="th-label" data-field="allPt">
                                <g:message code="app.label.qualitative.details.column.masterPrefTermAll"/>
                            </div>
                        </th>
                        <th data-idx="20" data-field="conComit">
                            <div class="th-label" data-field="conComit">
                                <g:message code="app.label.qualitative.details.column.conComit"/>
                            </div>
                        </th>

                        <th data-idx="51" data-field="initialFu">
                            <div class="th-label" data-field="initialFu">
                                <g:message code="app.label.qualitative.details.column.initialFu"/>
                            </div>
                        </th>
                        <th data-idx="61" data-field="comments">
                            <div class="th-label" data-field="comments">
                                <g:message code="app.label.qualitative.details.column.comments"/>
                            </div>
                        </th>
                        <th data-idx="71" data-field="region">
                            <div class="th-label" data-field="region">
                                <g:message code="app.label.qualitative.details.column.region"/>
                            </div>
                        </th>
                    </g:if>
                    <g:elseif test="${isJader}">
                        <th data-idx="11" data-field="genericName">
                            <div class="th-label" data-field="genericName">
                                <g:message code="app.label.qualitative.details.column.genericNameJader"/>
                            </div>
                        </th>
                        <th data-idx="12" data-field="allPt">
                            <div class="th-label" data-field="allPt">
                                <g:message code="app.label.qualitative.details.column.masterPrefTermAll"/>
                            </div>
                        </th>
                        <th data-idx="13" data-field="suspProd">
                            <div class="th-label" data-field="suspProd">
                                <g:message code="app.label.qualitative.details.column.suspProd"/>
                            </div>
                        </th>
                        <th data-idx="14" data-field="gender">
                            <div class="th-label" data-field="gender">
                                <g:message code="app.label.qualitative.details.column.gender"/>
                            </div>
                        </th>
                        <th data-idx="15" data-field="age">
                            <div class="th-label" data-field="age">
                                <g:message code="app.label.qualitative.details.column.patientAge"/>
                            </div>
                        </th>
                        <th data-idx="16" data-field="caseReportType">
                            <div class="th-label" data-field="caseReportType">
                                <g:message code="app.label.qualitative.details.column.caseReportType"/>
                            </div>
                        </th>
                        <th data-idx="17" data-field="indication">
                            <div class="th-label" data-field="indication">
                                <g:message code="app.label.qualitative.details.column.indication"/>
                            </div>
                        </th>
                        <th data-idx="18" data-field="eventOutcome">
                            <div class="th-label" data-field="eventOutcome">
                                <g:message code="app.label.qualitative.details.column.eventOutcome"/>
                            </div>
                        </th>
                        <th data-idx="16" data-field="conComit">
                            <div class="th-label" data-field="conComit">
                                <g:message code="app.label.qualitative.details.column.conComit"/>
                            </div>
                        </th>
                        <th data-idx="17" data-field="death">
                            <div class="th-label" data-field="death">
                                <g:message code="app.label.qualitative.details.column.death"/>
                            </div>
                        </th>
                        <th data-idx="18" data-field="doseDetails">
                            <div class="th-label" data-field="doseDetails">
                                <g:message code="app.label.qualitative.details.column.doseDetails"/>
                            </div>
                        </th>
                        <th data-idx="16" data-field="reportersHcpFlag">
                            <div class="th-label" data-field="reportersHcpFlag">
                                <g:message code="app.label.qualitative.details.column.reportersHcpFlag"/>
                            </div>
                        </th>
                        <th data-idx="17" data-field="eventOnsetDate">
                            <div class="th-label" data-field="eventOnsetDate">
                                <g:message code="app.label.qualitative.details.column.onsetDate"/>
                            </div>
                        </th>
                        <th data-idx="18" data-field="patientMedHist">
                            <div class="th-label" data-field="patientMedHist">
                                <g:message code="app.label.qualitative.details.column.patientMedHist"/>
                            </div>
                        </th>

                        <th data-idx="18" data-field="rechallenge">
                            <div class="th-label" data-field="rechallenge">
                                <g:message code="app.label.qualitative.details.column.rechallengeJader"/>
                            </div>
                        </th>
                        <th data-idx="16" data-field="reporterQualification">
                            <div class="th-label" data-field="reporterQualification">
                                <g:message code="app.label.qualitative.details.column.reporterQualification"/>
                            </div>
                        </th>
                        <th data-idx="17" data-field="riskCategory">
                            <div class="th-label" data-field="riskCategory">
                                <g:message code="app.label.qualitative.details.column.riskCategory"/>
                            </div>
                        </th>
                        <th data-idx="18" data-field="therapyDates">
                            <div class="th-label" data-field="therapyDates">
                                <g:message code="app.label.qualitative.details.column.therapyDates"/>
                            </div>
                        </th>
                    </g:elseif>
                    <g:elseif test="${isVaers}">
                        <th data-idx="11" data-field="outcome">
                            <div class="th-label" data-field="outcome">
                                <g:message code="app.label.qualitative.details.column.outcome"/>
                            </div>
                        </th>
                        <th data-idx="22" data-field="serious">
                            <div class="th-label" data-field="serious">
                                <g:message code="app.label.qualitative.details.column.assessSeriousness"/>
                            </div>
                        </th>
                        <th data-idx="31" data-field="death">
                            <div class="th-label" data-field="death">
                                <g:message code="app.label.qualitative.details.column.death"/>
                            </div>
                        </th>
                            <th data-idx="49" data-field="timeToOnset">
                                <div class="th-label" data-field="timeToOnset">
                                    <g:message code="app.label.qualitative.details.column.timeToOnset"/>
                                </div>
                            </th>
                        <th data-idx="33" data-field="patientAge">
                            <div class="th-label" data-field="patientAge">
                                <g:message code="app.label.qualitative.details.column.patientAge"/>
                            </div>
                        </th>
                        <th data-idx="27" data-field="gender">
                            <div class="th-label" data-field="gender">
                                <g:message code="app.label.qualitative.details.column.gender"/>
                            </div>
                        </th>
                        <th data-idx="48" data-field="batchLotNo">
                            <div class="th-label" data-field="batchLotNo">
                                <g:message code="app.label.qualitative.details.column.batchLotNo"/>
                            </div>
                        </th>
                        <th data-idx="61" data-field="comments">
                            <div class="th-label" data-field="comments">
                                <g:message code="app.label.qualitative.details.column.comments"/>
                            </div>
                        </th>
                    </g:elseif>
                    <g:else>
                        <g:if test="${!isFaers}">
                            <th data-idx="10" data-field="listedness">
                                <div class="th-label" data-field="listedness">
                                    <g:if test="${columnLabelMap.containsKey('listedness')}">
                                        ${columnLabelMap.get('listedness')}
                                    </g:if>
                                    <g:else>
                                        <g:message code="app.label.qualitative.details.column.assessListedness"/>
                                    </g:else>
                                </div>
                            </th>
                        </g:if>
                        <th data-idx="11" data-field="outcome">
                            <div class="th-label" data-field="outcome">
                                <g:if test="${columnLabelMap.containsKey('outcome')}">
                                    ${columnLabelMap.get('outcome')}
                                </g:if>
                                <g:else>
                                    <g:message code="app.label.qualitative.details.column.outcome"/>
                                </g:else>
                            </div>
                        </th>
                        <g:if test="${!isFaers && !isAdhocCaseSeries}">
                            <th data-idx="14" data-field="signalsAndTopics" align="center">
                                <div class="th-label" data-field="signalsAndTopics">
                                    <g:message code="app.label.qualitative.details.column.signalsAndTopics"/>
                                </div>
                            </th>
                            <th data-idx="32" data-field="currentDisposition" align="center">
                                <div class="th-label" data-field="currentDisposition">
                                    <g:message code="app.label.disposition.to"/>
                             </div>
                            </th>

                            <th data-idx="15" data-field="disposition" align="center">
                                <div class="th-label" data-field="disposition">
                                    <g:message code="app.label.current.disposition"/>
                                </div>
                            </th>
                        </g:if>

                    <g:if test="${!isFaers && !isAdhocCaseSeries}">
                        <th data-idx="16" data-field="assignedToUser">
                            <div class="th-label" data-field="assignedToUser">
                                <g:message code="app.label.assigned.to"/>
                            </div>
                        </th>
                        <th data-idx="18" data-field="dueDate">
                            <div class="th-label" data-field="dueDate">
                                <g:message code="app.label.qualitative.details.column.dueDate"/>
                            </div>
                        </th>
                    </g:if>
                    <th data-idx="19" data-field="suspProd">
                        <div class="th-label" data-field="suspProd">
                            <g:if test="${columnLabelMap.containsKey('suspProd')}">
                                ${columnLabelMap.get('suspProd')}
                            </g:if>
                            <g:else>
                                <g:message code="app.label.qualitative.details.column.suspProd"/>
                            </g:else>
                        </div>
                    </th>
                    <th data-idx="20" data-field="conComit">
                        <div class="th-label" data-field="conComit">
                            <g:if test="${columnLabelMap.containsKey('conComit')}">
                                ${columnLabelMap.get('conComit')}
                            </g:if>
                            <g:else>
                                <g:message code="app.label.qualitative.details.column.conComit"/>
                            </g:else>
                        </div>
                    </th>
                    <th data-idx="21" data-field="masterPrefTermAll">
                        <div class="th-label" data-field="masterPrefTermAll">
                            <g:if test="${columnLabelMap.containsKey('masterPrefTermAll')}">
                                ${columnLabelMap.get('masterPrefTermAll')}
                            </g:if>
                            <g:else>
                                <g:message code="app.label.qualitative.details.column.masterPrefTermSurAll"/>
                            </g:else>
                        </div>
                    </th>
                    <th data-idx="22" data-field="serious">
                        <div class="th-label" data-field="serious">
                            <g:if test="${columnLabelMap.containsKey('serious')}">
                                ${columnLabelMap.get('serious')}
                            </g:if>
                            <g:else>
                                <g:message code="${customFieldsEnabled? "app.label.qualitative.details.column.assessSeriousNonSeriousness":"app.label.qualitative.details.column.assessSeriousness"}"/>
                            </g:else>
                        </div>
                    </th>
                    <th data-idx="23" data-field="caseReportType">
                        <div class="th-label" data-field="caseReportType">
                            <g:if test="${columnLabelMap.containsKey('caseReportType')}">
                                ${columnLabelMap.get('caseReportType')}
                            </g:if>
                            <g:else>
                                <g:message code="app.label.qualitative.details.column.caseReportType"/>
                            </g:else>
                        </div>
                    </th>
                    <th data-idx="24" data-field="reportersHcpFlag">
                        <div class="th-label" data-field="reportersHcpFlag">
                            <g:if test="${columnLabelMap.containsKey('reportersHcpFlag')}">
                                ${columnLabelMap.get('reportersHcpFlag')}
                            </g:if>
                            <g:else>
                                <g:message code="app.label.qualitative.details.column.reportersHcpFlag"/>
                            </g:else>
                        </div>
                    </th>
                    <th data-idx="25" data-field="country">
                        <div class="th-label" data-field="country">
                            <g:if test="${columnLabelMap.containsKey('country')}">
                                ${columnLabelMap.get('country')}
                            </g:if>
                            <g:else>
                                <g:message code="${customFieldsEnabled? "app.label.qualitative.details.column.derivedCountry":"app.label.qualitative.details.column.country"}"/>
                            </g:else>
                        </div>
                    </th>
                    <th data-idx="26" data-field="age">
                        <div class="th-label" data-field="age">
                            <g:if test="${columnLabelMap.containsKey('age')}">
                                ${columnLabelMap.get('age')}
                            </g:if>
                            <g:else>
                                <g:message code="app.label.qualitative.details.column.age"/>
                            </g:else>
                        </div>
                    </th>
                    <th data-idx="27" data-field="gender">
                        <div class="th-label" data-field="gender">
                            <g:if test="${columnLabelMap.containsKey('gender')}">
                                ${columnLabelMap.get('gender')}
                            </g:if>
                            <g:else>
                                <g:message code="app.label.qualitative.details.column.gender"/>
                            </g:else>
                        </div>
                    </th>
                    <th data-idx="28" data-field="rechallenge">
                        <div class="th-label" data-field="rechallenge">
                            <g:if test="${columnLabelMap.containsKey('rechallenge')}">
                                ${columnLabelMap.get('rechallenge')}
                            </g:if>
                            <g:else>
                                <g:message code="app.label.qualitative.details.column.rechallenge"/>
                            </g:else>
                        </div>
                    </th>
                    <th data-idx="29" data-field="lockedDate">
                        <div class="th-label" data-field="lockedDate">
                            <g:if test="${columnLabelMap.containsKey('lockedDate')}">
                                ${columnLabelMap.get('lockedDate')}
                            </g:if>
                            <g:else>
                                <g:message code="app.label.qualitative.details.column.lockedDate"/>
                            </g:else>
                        </div>
                    </th>
                    <th data-idx="31" data-field="death">
                        <div class="th-label" data-field="death">
                            <g:if test="${columnLabelMap.containsKey('death')}">
                                ${columnLabelMap.get('death')}
                            </g:if>
                            <g:else>
                                <g:message code="app.label.qualitative.details.column.death"/>
                            </g:else>
                        </div>
                    </th>
                    <th data-idx="33" data-field="patientAge">
                        <div class="th-label" data-field="patientAge">
                            <g:if test="${columnLabelMap.containsKey('patientAge')}">
                                ${columnLabelMap.get('patientAge')}
                            </g:if>
                            <g:else>
                                <g:message code="app.label.qualitative.details.column.patientAge"/>
                            </g:else>
                        </div>
                    </th>
                    <g:if test="${customFieldsEnabled}">
                        <th data-idx="32" data-field="medErrorsPt">
                            <div class="th-label" data-field="medErrorsPt"><g:message
                                    code="app.label.qualitative.details.column.medErrorsPt"/></div>
                        </th>
                        <th data-idx="34" data-field="caseType">
                            <div class="th-label" data-field="caseType"><g:message
                                    code="app.label.qualitative.details.column.caseType"/></div>
                        </th>
                        <th data-idx="35" data-field="completenessScore">
                            <div class="th-label" data-field="completenessScore"><g:message
                                    code="app.label.qualitative.details.column.completenessScore"/></div>
                        </th>
                        <th data-idx="36" data-field="indNumber">
                            <div class="th-label" data-field="indNumber"><g:message
                                    code="app.label.qualitative.details.column.indNumber"/></div>
                        </th>
                        <th data-idx="37" data-field="appTypeAndNum">
                            <div class="th-label" data-field="appTypeAndNum"><g:message
                                    code="app.label.qualitative.details.column.appTypeAndNum"/></div>
                        </th>
                        <th data-idx="38" data-field="compoundingFlag">
                            <div class="th-label" data-field="compoundingFlag"><g:message
                                    code="app.label.qualitative.details.column.compoundingFlag"/></div>
                        </th>
                        <th data-idx="39" data-field="submitter">
                            <div class="th-label" data-field="submitter"><g:message
                                    code="app.label.qualitative.details.column.submitter"/></div>
                        </th>
                    </g:if>
                        <g:if test="${!isFaers}">
                            <th data-idx="40" data-field="malfunction">
                                <div class="th-label" data-field="malfunction">
                                    <g:if test="${columnLabelMap.containsKey('malfunction')}">
                                        ${columnLabelMap.get('malfunction')}
                                    </g:if>
                                    <g:else>
                                        <g:message code="app.label.qualitative.details.column.malfunction"/>
                                    </g:else>
                                </div>
                            </th>
                            <th data-idx="41" data-field="comboFlag">
                                <div class="th-label" data-field="comboFlag">
                                    <g:if test="${columnLabelMap.containsKey('comboFlag')}">
                                        ${columnLabelMap.get('comboFlag')}
                                    </g:if>
                                    <g:else>
                                        <g:message code="app.label.qualitative.details.column.comboFlag"/>
                                    </g:else>
                                </div>
                            </th>
                        </g:if>
                    <th data-idx="42" data-field="indication">
                        <div class="th-label" data-field="indication">
                            <g:if test="${columnLabelMap.containsKey('indication')}">
                                ${columnLabelMap.get('indication')}
                            </g:if>
                            <g:else>
                                <g:message code="app.label.qualitative.details.column.indication"/>
                            </g:else>
                        </div>
                    </th>
                    <g:if test="${!isFaers}">
                        <th data-idx="43" data-field="eventOutcome">
                            <div class="th-label" data-field="eventOutcome">
                                <g:if test="${columnLabelMap.containsKey('eventOutcome')}">
                                    ${columnLabelMap.get('eventOutcome')}
                                </g:if>
                                <g:else>
                                    <g:message code="app.label.qualitative.details.column.eventOutcome"/>
                                </g:else>
                            </div>
                        </th>
                        <th data-idx="44" data-field="causeOfDeath">
                            <div class="th-label" data-field="causeOfDeath">
                                <g:if test="${columnLabelMap.containsKey('causeOfDeath')}">
                                    ${columnLabelMap.get('causeOfDeath')}
                                </g:if>
                                <g:else>
                                    <g:message code="app.label.qualitative.details.column.causeOfDeath"/>
                                </g:else>
                            </div>
                        </th>
                    </g:if>
                    <th data-idx="45" data-field="seriousUnlistedRelated">
                        <div class="th-label" data-field="seriousUnlistedRelated">
                            <g:if test="${columnLabelMap.containsKey('seriousUnlistedRelated')}">
                                ${columnLabelMap.get('seriousUnlistedRelated')}
                            </g:if>
                            <g:else>
                                <g:message code="app.label.qualitative.details.column.seriousUnlistedRelated"/>
                            </g:else>
                        </div>
                    </th>

                    <th data-idx="48" data-field="batchLotNo">
                        <div class="th-label" data-field="batchLotNo">
                            <g:if test="${columnLabelMap.containsKey('batchLotNo')}">
                                ${columnLabelMap.get('batchLotNo')}
                            </g:if>
                            <g:else>
                                <g:message code="app.label.qualitative.details.column.batchLotNo"/>
                            </g:else>
                        </div>
                    </th>
                        <th data-idx="51" data-field="initialFu">
                            <div class="th-label" data-field="initialFu">
                                <g:if test="${columnLabelMap.containsKey('initialFu')}">
                                    ${columnLabelMap.get('initialFu')}
                                </g:if>
                                <g:else>
                                    <g:message code="app.label.qualitative.details.column.initialFu"/>
                                </g:else>
                            </div>
                        </th>
                        <g:if test="${!isFaers}">
                            <th data-idx="50" data-field="caseClassification">
                                <div class="th-label" data-field="caseClassification">
                                    <g:if test="${columnLabelMap.containsKey('caseClassification')}">
                                        ${columnLabelMap.get('caseClassification')}
                                    </g:if>
                                    <g:else>
                                        <g:message code="app.label.qualitative.details.column.caseClassification"/>
                                    </g:else>
                                </div>
                            </th>
                            <th data-idx="46" data-field="patientMedHist">
                                <div class="th-label" data-field="patientMedHist">
                                    <g:if test="${columnLabelMap.containsKey('patientMedHist')}">
                                        ${columnLabelMap.get('patientMedHist')}
                                    </g:if>
                                    <g:else>
                                        <g:message code="app.label.qualitative.details.column.patientMedHist"/>
                                    </g:else>
                                </div>
                            </th>
                            <th data-idx="47" data-field="patientHistDrugs">
                                <div class="th-label" data-field="patientHistDrugs">
                                    <g:if test="${columnLabelMap.containsKey('patientHistDrugs')}">
                                        ${columnLabelMap.get('patientHistDrugs')}
                                    </g:if>
                                    <g:else>
                                        <g:message code="app.label.qualitative.details.column.patientHistDrugs"/>
                                    </g:else>
                                </div>
                            </th>
                            <th data-idx="49" data-field="timeToOnset">
                                <div class="th-label" data-field="timeToOnset">
                                    <g:if test="${columnLabelMap.containsKey('timeToOnset')}">
                                        ${columnLabelMap.get('timeToOnset')}
                                    </g:if>
                                    <g:else>
                                        <g:message code="app.label.qualitative.details.column.timeToOnset"/>
                                    </g:else>
                                </div>
                            </th>
                            <th data-idx="52" data-field="protocolNo">
                                <div class="th-label" data-field="protocolNo">
                                    <g:if test="${columnLabelMap.containsKey('protocolNo')}">
                                        ${columnLabelMap.get('protocolNo')}
                                    </g:if>
                                    <g:else>
                                        <g:message code="app.label.qualitative.details.column.protocolNo"/>
                                    </g:else>
                                </div>
                            </th>
                        </g:if>
                    <th data-idx="53" data-field="isSusar">
                        <div class="th-label" data-field="isSusar">
                            <g:if test="${columnLabelMap.containsKey('isSusar')}">
                                ${columnLabelMap.get('isSusar')}
                            </g:if>
                            <g:else>
                                <g:message code="app.label.qualitative.details.column.isSusar"/>
                            </g:else>
                        </div>
                    </th>
                    <th data-idx="54" data-field="therapyDates">
                        <div class="th-label" data-field="therapyDates">
                            <g:if test="${columnLabelMap.containsKey('therapyDates')}">
                                ${columnLabelMap.get('therapyDates')}
                            </g:if>
                            <g:else>
                                <g:message code="app.label.qualitative.details.column.therapyDates"/>
                            </g:else>
                        </div>
                    </th>
                    <th data-idx="55" data-field="doseDetails">
                        <div class="th-label" data-field="doseDetails">
                            <g:if test="${columnLabelMap.containsKey('doseDetails')}">
                                ${columnLabelMap.get('doseDetails')}
                            </g:if>
                            <g:else>
                                <g:message code="app.label.qualitative.details.column.doseDetails"/>
                            </g:else>
                        </div>
                    </th>
                    <g:if test="${customFieldsEnabled}">
                    <th data-idx="57" data-field="preAnda">
                        <div class="th-label" data-field="preAnda">
                            <g:if test="${columnLabelMap.containsKey('preAnda')}">
                                ${columnLabelMap.get('preAnda')}
                            </g:if>
                            <g:else>
                                <g:message code="app.label.qualitative.details.column.protocolNo"/>
                            </g:else>
                        </div>
                    </th>
                    </g:if>
                    <g:if test="${!isAdhocCaseSeries && !isFaers}">
                            <th data-idx="58" data-field="justification">
                            <div class="th-label" data-field="justification">
                                <g:message code="app.label.qualitative.details.column.justification"/>
                            </div>
                        </th>
                        <th data-idx="59" data-field="dispPerformedBy">
                            <div class="th-label" data-field="dispPerformedBy">
                                <g:message code="app.label.qualitative.details.column.dispPerformedBy"/>
                            </div>
                        </th>
                    </g:if>
                        <th data-idx="61" data-field="comments">
                            <div class="th-label" data-field="comments">
                                <g:message code="app.label.qualitative.details.column.comments"/>
                            </div>
                        </th>
                        <g:if test="${!isFaers}">
                            <th data-idx="58" data-field="primSuspProd">
                                <div class="th-label" data-field="primSuspProd">
                                    <g:if test="${columnLabelMap.containsKey('primSuspProd')}">
                                        ${columnLabelMap.get('primSuspProd')}
                                    </g:if>
                                    <g:else>
                                        <g:message code="app.label.qualitative.details.column.primSuspProd"/>
                                    </g:else>
                                </div>
                            </th>
                        </g:if>
                        <g:if test="${customFieldsEnabled}">
                            <th data-idx="59" data-field="primSuspPai">
                                <div class="th-label" data-field="primSuspPai"><g:message
                                        code="app.label.qualitative.details.column.primSuspPai"/></div>
                            </th>
                            <th data-idx="60" data-field="paiAll">
                                <div class="th-label" data-field="paiAll"><g:message
                                        code="app.label.qualitative.details.column.paiAll"/></div>
                            </th>
                        </g:if>
                        <th data-idx="61" data-field="allPt">
                            <div class="th-label" data-field="allPt">
                                <g:if test="${columnLabelMap.containsKey('allPt')}">
                                    ${columnLabelMap.get('allPt')}
                                </g:if>
                                <g:else>
                                    <g:message code="app.label.qualitative.details.column.masterPrefTermAll"/>
                                </g:else>
                            </div>
                        </th>
                        <g:if test="${!isAdhocCaseSeries && !isFaers}">
                            <th data-idx="60" data-field="dispLastChange">
                                <div class="th-label" data-field="dispLastChange">
                                    <g:message code="app.label.qualitative.details.column.dispLastChange"/>
                                </div>
                            </th>
                        </g:if>

                        <g:if test="${!isFaers}">
                            <th data-idx="62" data-field="genericName">
                                <div class="th-label" data-field="genericName">
                                    <g:if test="${columnLabelMap.containsKey('genericName')}">
                                        ${columnLabelMap.get('genericName')}
                                    </g:if>
                                    <g:else>
                                        <g:message code="app.label.qualitative.details.column.genericName"/>
                                    </g:else>
                                </div>
                            </th>
                            <th data-idx="63" data-field="caseCreationDate">
                                <div class="th-label" data-field="caseCreationDate">
                                    <g:if test="${columnLabelMap.containsKey('caseCreationDate')}">
                                        ${columnLabelMap.get('caseCreationDate')}
                                    </g:if>
                                    <g:else>
                                        <g:message code="app.label.qualitative.details.column.caseCreationDate"/>
                                    </g:else>
                                </div>
                            </th>
                            <th data-idx="64" data-field="dateOfBirth">
                                <div class="th-label" data-field="dateOfBirth">
                                    <g:if test="${columnLabelMap.containsKey('dateOfBirth')}">
                                        ${columnLabelMap.get('dateOfBirth')}
                                    </g:if>
                                    <g:else>
                                        <g:message code="app.label.qualitative.details.column.dateOfBirth"/>
                                    </g:else>
                                </div>
                            </th>
                            <th data-idx="65" data-field="eventOnsetDate">
                                <div class="th-label" data-field="eventOnsetDate">
                                    <g:if test="${columnLabelMap.containsKey('eventOnsetDate')}">
                                        ${columnLabelMap.get('eventOnsetDate')}
                                    </g:if>
                                    <g:else>
                                        <g:message code="app.label.qualitative.details.column.eventOnsetDate"/>
                                    </g:else>
                                </div>
                            </th>
                            <th data-idx="66" data-field="pregnancy">
                                <div class="th-label" data-field="pregnancy">
                                    <g:if test="${columnLabelMap.containsKey('pregnancy')}">
                                        ${columnLabelMap.get('pregnancy')}
                                    </g:if>
                                    <g:else>
                                        <g:message code="app.label.qualitative.details.column.pregnancy"/>
                                    </g:else>
                                </div>
                            </th>
                            <th data-idx="67" data-field="medicallyConfirmed">
                                <div class="th-label" data-field="medicallyConfirmed">
                                    <g:if test="${columnLabelMap.containsKey('medicallyConfirmed')}">
                                        ${columnLabelMap.get('medicallyConfirmed')}
                                    </g:if>
                                    <g:else>
                                        <g:message code="app.label.qualitative.details.column.medicallyConfirmed"/>
                                    </g:else>
                                </div>
                            </th>
                            <th data-idx="68" data-field="allPTsOutcome">
                                <div class="th-label" data-field="allPTsOutcome">
                                    <g:if test="${columnLabelMap.containsKey('allPTsOutcome')}">
                                        ${columnLabelMap.get('allPTsOutcome')}
                                    </g:if>
                                    <g:else>
                                        <g:message code="app.label.qualitative.details.column.allPTsOutcome"/>
                                    </g:else>
                                </div>
                            </th>
                            <g:if test="${customFieldsEnabled}">
                                <th data-idx="69" data-field="crossReferenceInd">
                                    <div class="th-label" data-field="crossReferenceInd">
                                        <g:if test="${columnLabelMap.containsKey('crossReferenceInd')}">
                                            ${columnLabelMap.get('crossReferenceInd')}
                                        </g:if>
                                        <g:else>
                                            <g:message code="app.label.qualitative.details.column.crossReferenceInd"/>
                                        </g:else>
                                    </div>
                                </th>
                            </g:if>
                        </g:if>
                    </g:else>
    </tr>
    </thead>
    </table>

    </div>
    <form id="">
        <input type="hidden" id="filterParams"/>
        <input type="hidden" id="sideParams"/>
    </form>

</div>
</div>
