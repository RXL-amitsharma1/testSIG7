<%@ page import="com.rxlogix.enums.ReportFormat" %>

<asset:javascript src="app/pvs/activity/activities.js"/>
<div class="rxmain-container panel-group">
    <div class="panel panel-default rxmain-container rxmain-container-top">
        <div class="rxmain-container-row rxmain-container-header-label panel-heading pv-sec-heading">
            <div class="row">
                <div class="col-md-7">
                    <a data-toggle="collapse" data-parent="#accordion-pvs-analysis" href="#signalDiv"
                       aria-expanded="true" style="color:inherit;">Activities</a>
                </div>

                <div class="col-md-5 ico-menu">

                    <span class="pull-right">
                        <span class="dropdown-toggle exportPanel" data-toggle="dropdown" tabindex="0" accesskey="x"
                              title="Export to">
                            <i class="mdi mdi-export ic-sm"></i>
                            <span class="caret hidden"></span>
                        </span>
                        <ul class="dropdown-menu export-type-list" id="exportTypes">
                            <strong class="font-12">Export</strong>
                            <li><g:link controller="activity" action="exportSignalActivitiesReport" class="m-r-30"
                                        params="${[outputFormat: ReportFormat.DOCX, signalId: signal.id]}">
                                <img src="/signal/assets/word-icon.png" class="m-r-10" height="16" width="16"/>
                                <g:message code="save.as.word"/>
                            </g:link>
                            </li>
                            <li><g:link controller="activity" action="exportSignalActivitiesReport" class="m-r-30"
                                        params="${[outputFormat: ReportFormat.XLSX, signalId: signal.id]}">
                                <img src="/signal/assets/excel.gif" class="m-r-10" height="16" width="16"/>
                                <g:message code="save.as.excel"/>
                            </g:link>
                            </li>
                            <li><g:link controller="activity" action="exportSignalActivitiesReport" class="m-r-30"
                                        params="${[outputFormat: ReportFormat.PDF, signalId: signal.id]}">
                                <img src="/signal/assets/pdf-icon.jpg" class="m-r-10" height="16" width="16"/>
                                <g:message code="save.as.pdf"/>
                            </g:link>
                            </li>
                        </ul>
                    </span>

                </div>
            </div>
        </div>

        <div id="signalDiv" class="panel-collapse rxmain-container-content rxmain-container-show collapse in"
             aria-expanded="true">
            <div id="validatedSignalTableContainer" class="pv-scrollable-dt">
                <table id="signalActivityTable" class="row-border hover" width="100%">
                    <thead>
                    <tr>
                        <th class=""><g:message code="app.label.activity.type"/></th>
                        <th width="50%"><g:message code="app.label.description"/></th>
                        <th><g:message code="app.label.performed.by"/></th>
                        <th><g:message code="app.label.timestamp"/></th>
                    </tr>
                    </thead>
                </table>
            </div>
        </div>
    </div>
</div>