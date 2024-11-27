<%@ page import="com.rxlogix.enums.ReportFormat" %>
<g:render template="/includes/layout/flashErrorsDivs" bean="${alert}" var="theInstance"/>
    <div class="panel-heading pv-sec-heading">
        <div class="row">
            <div class="col-md-4">
            </div>

            <div class="col-md-8 grid_margin">
                <div class="pos-rel pull-right">
                <!------------------=================--------------pinned icon code started-------------================----------------------->
                <span style="padding-top:20px">
                    <!------------==================-------Field selection code start------==========------------------->
                    <a href="javascript:void(0)" class="grid-pin collapse theme-color" id="ic-configureAdhocFields" data-fieldconfigurationbarid="adhocFields" data-pagetype="adhoc_alert" title="${message(code: 'app.label.choosefields')}">
                        <i class="mdi mdi-settings-outline font-24"></i>
                    </a>
                    <!------------==================---------------Field selection code end----------==========------------------->
                    <!------------==================--------------export to code start---------------==========------------------->
                    <span class="grid-pin collapse theme-color dropdown" id="exportTypes">
                        <span  tabindex="0" class="dropdown-toggle saveViewPanel grid-menu-tooltip dropbtn" data-title="Save View" data-toggle="dropdown" accesskey="s" title="${message(code: 'app.label.exportTo')}">
                            <i class="mdi mdi-export font-24"></i>
                            <span class="caret hidden"></span>
                        </span>
                            <ul class="dropdown-menu save-list dropdown-content dropdown-menu-right ddm-padding">
                                <span class="dropdown">
                                <strong class="font-12 title-spacing">Export</strong>
                                <li><g:link controller="adHocAlert" action="exportReport" class="m-r-30"
                                            params="${[outputFormat: ReportFormat.DOCX,id : id]}">
                                    <img src="/signal/assets/word-icon.png" class="m-r-10" height="16" width="16" /><g:message code="save.as.word" />
                                </g:link>
                                </li>
                                <li><g:link controller="adHocAlert" action="exportReport" class="m-r-30"
                                            params="${[outputFormat: ReportFormat.XLSX,id : id]}">
                                    <img src="/signal/assets/excel.gif" class="m-r-10" height="16" width="16" /><g:message code="save.as.excel" />
                                </g:link></li>
                                <li><g:link controller="adHocAlert" action="exportReport" class="m-r-30"
                                            params="${[outputFormat: ReportFormat.PDF,id : id]}">
                                    <img src="/signal/assets/pdf-icon.jpg" class="m-r-10" height="16" width="16" /><g:message code="save.as.pdf" />
                                </g:link></li>
                                </span>
                            </ul>
                    </span>

                <!------------==================-------Disposition code start------==========------------------->
                        <span class="grid-pin collapse theme-color dropdown" id="dispositionTypes2">
                            <span  tabindex="0" class="dropdown-toggle grid-menu-tooltip dropbtn" data-toggle="dropdown" accesskey="s" title="${message(code: 'app.label.disposition')}">
                                <i class="mdi mdi-checkbox-marked-outline font-24"></i>
                            </span>
                            <ul class="dropdown-menu dropdown-content dropdown-menu-right export-type-list disposition-ico disposition-li" id="dispositionTypes"></ul>
                        </span>
                <!------------==================-------Disposition code end------==========-------------------->
                <!------------==================-------Filter code start------==========------------------->
                    <a href="javascript:void(0)" class="grid-pin collapse theme-color" id="ic-toggle-column-filters" data-fieldconfigurationbarid="quantitativeFields" data-pagetype="quantitative_alert" title="${message(code: 'app.label.filter')}">
                        <i class="mdi mdi-filter-outline font-24"></i>
                    </a>
                    <!------------==================-------Filter code end--------==========------------------->
      <!------------------=================--------------pinned icon code closed-------------================----------------------->
      <!-----------------==================--------------export to code end------------------================----------------------->

                <div class="ico-menu pull-right">
                    <!------------------------------------------------------------list menu code start----------------------------------------------------------------------------->
                    <span class="dropdown grid-icon" id="reportIconMenu">
                        <span class="dropdown-toggle" data-toggle="dropdown" style="float: left;">
                            <i class="mdi mdi-format-list-bulleted mr-10 font-24 pull-right mr-10" style="margin-right:5px"></i></span>
                        <ul class="dropdown-menu ul-ddm">
                            <!------------------export to menu code start--------------------------->
                            <li class="li-pin-width">
                                <a class="test field-config-bar-toggle text-left-prop ul-ddm-hide" tabindex="0" href="#" id="configureAdhocFields" data-fieldconfigurationbarid="adhocFields" data-pagetype="adhoc_alert" class="pull-right field-config-bar-toggle"
                                   data-backdrop="true" data-container="columnList-container" title="Choose Fields" accesskey="c">
                                    <i class="mdi mdi-settings-outline"></i>
                                    <span tabindex="0">
                                        Field Selection
                                    </span>
                                </a>
                                <a href="javascript:void(0)" class="text-right-prop" >
                                <span class="pin-unpin-rotate pull-right mdi mdi-pin" data-id="#ic-configureAdhocFields" title="Pin to top"  data-toggle="collapse"  data-title="Field selection"></span>
                                </a>
                            </li>

                            <li class="dropdown-submenu li-pin-width">
                                <a class="test text-left-prop ul-ddm-hide" href="#">
                                    <i class="mdi mdi-export"></i> <span tabindex="0" class="dropdown-toggle exportPanel grid-menu-tooltip" data-toggle="dropdown"  accesskey="x">
                                    Export To
                                </span></a>
                                <a href="javascript:void(0)" class="text-right-prop">
                                    <span class="pin-unpin-rotate pull-right mdi mdi-pin"  data-toggle="collapse" title="Pin to top" data-title="Export To" data-id="#exportTypes"></span>
                                </a>
                                <ul class="dropdown-menu export-type-list ul-ddm-child" id="exportTypes">
                                    <strong class="font-12">Export</strong>
                                    <li><g:link controller="adHocAlert" action="exportReport" class="m-r-30"
                                                params="${[outputFormat: ReportFormat.DOCX,id : id]}">
                                        <img src="/signal/assets/word-icon.png" class="m-r-10" height="16" width="16" /><g:message code="save.as.word" />
                                    </g:link>
                                    </li>
                                    <li><g:link controller="adHocAlert" action="exportReport" class="m-r-30"
                                                params="${[outputFormat: ReportFormat.XLSX,id : id]}">
                                        <img src="/signal/assets/excel.gif" class="m-r-10" height="16" width="16" /><g:message code="save.as.excel" />
                                    </g:link></li>
                                    <li><g:link controller="adHocAlert" action="exportReport" class="m-r-30 export-report"
                                                params="${[outputFormat: ReportFormat.PDF,id : id]}">
                                        <img src="/signal/assets/pdf-icon.jpg" class="m-r-10" height="16" width="16" /><g:message code="save.as.pdf" />
                                    </g:link></li>
                                </ul>

                            </li>
                            <!-----------------export to menu code end------------------------------->

                            <li class="dropdown-submenu li-pin-width">
                                <a class="test text-left-prop"  href="#">
                                    <i class="mdi mdi-checkbox-marked-outline"></i>
                                    <span class="dropdown-toggle grid-menu-tooltip" data-toggle="dropdown" tabindex="0" role="button" accesskey="x"  aria-expanded="true">
                                        <g:message code="app.label.disposition" />
                                    </span>
                                </a>
                                <a href="javascript:void(0)" class="text-right-prop">
                                <span class="pin-unpin-rotate pull-right mdi mdi-pin"  data-toggle="collapse" title="Pin to top" data-id="#dispositionTypes2" data-title="Disposition"></span>
                                </a>
                                <ul class="dropdown-menu export-type-list disposition-ico" id="dispositionTypes"></ul>
                            </li>

                            <li class="li-pin-width">
                                <a class="test text-left-prop ul-ddm-hide" id="toggle-column-filters" href="#">
                                    <i class="mdi mdi-filter-outline"></i>
                                    <span data-title="Filters" class="test" tabindex="0" role="button" accesskey="y">
                                        Filters</span>
                                </a>
                                <a href="javascript:void(0)" class="text-right-prop">
                                    <span class="pin-unpin-rotate pull-right mdi mdi-pin" data-toggle="collapse" title="Pin to top" data-id="#ic-toggle-column-filters" data-title="Filters"></span>
                                </a>
                            </li>
                        </ul>
                    </span>
<!-------------------------------------list menu ended---------------------------------------------------------------------------------------------->
                </div>
                </span>
            </div>
            </div>
        </div>
    </div>

    <div class="row">
        <table id="alertsDetailsTable" class="auto-scale row-border no-shadow hover" width="100%">
            <thead>
            <tr id="alertsDetailsTableRow">
                <th data-idx="0" data-field="selected"><div class="th-label"><input id="select-all" type="checkbox"/></div></th>
                <th data-idx="2" data-field="dropdown"><div class="th-label"> </div></th>
                <g:if test="${isPriorityEnabled}">
                    <th data-idx="3" data-field="priority"><div class="th-label"><g:message code="app.label.adhoc.details.column.priority"/></div></th>
                </g:if>
                <th data-idx="4" data-field="name"><div class="th-label"><g:message code="app.label.adhoc.details.column.name"/></div></th>
                <th data-idx="5" data-field="productSelection"><div class="th-label"><g:message code="app.label.adhoc.details.column.productSelection"/></div></th>
                <th data-idx="6" data-field="eventSelection"><div class="th-label"><g:message code="app.label.adhoc.details.column.eventSelection"/></div></th>
                <th data-idx="7" data-field="issueTracked"><div class="th-label"><g:message code="app.label.adhoc.details.column.issueTracked"/></div></th>
                <th data-idx="8" data-field="numOfIcsrs"><div class="th-label"><g:message code="app.label.adhoc.details.column.numOfIcsrs"/></div></th>
                <th data-idx="9" data-field="initDataSrc"><div class="th-label"><g:message code="app.label.adhoc.details.column.initDataSrc"/></div></th>
                <th data-idx="11" data-field="signalsAndTopics"><div class="th-label"><g:message code="app.label.adhoc.details.column.signalsAndTopics"/></div></th>
                <th data-idx="16" data-field="currentDisposition"><div class="th-label"><g:message code="app.label.disposition.to"/></div></th>
                <th data-idx="12" data-field="disposition"><div class="th-label"><g:message code="app.label.current.disposition"/></div></th>
                <th data-idx="13" data-field="assignedTo"><div class="th-label"><g:message code="app.label.assigned.to"/></div></th>
                <th data-idx="14" data-field="detectedDate"><div class="th-label"><g:message code="app.label.detected.date"/></div></th>
                <th data-idx="15" data-field="dueIn"><div class="th-label"><g:message code="app.label.adhoc.details.column.dueIn"/></div></th>
            </tr>
            </thead>
        </table>
    </div>
