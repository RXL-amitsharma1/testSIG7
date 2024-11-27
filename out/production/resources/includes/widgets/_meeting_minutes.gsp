<%@ page import="com.rxlogix.enums.ReportFormat" %>
<style>
.dataTableHideCellContent {
    display: block;
}
.dropdown-menu>li>button {
    display: block;
    clear: both;
    font-weight: 400;
    line-height: 1.42857143;
    color: #333;
    white-space: nowrap;
}

</style>
<asset:javascript src="app/pvs/scheduler.js"/>
<asset:javascript src="app/pvs/bootbox.min.js"/>
<asset:stylesheet src="bootstrap-timepicker/bootstrap-timepicker.min.css"/>
<asset:javascript src="bootstrap-timepicker/bootstrap-timepicker.min.js"/>
<div class="rxmain-container panel">
    <div class="rxmain-container-inner">
        <div class="rxmain-container-row rxmain-container-header">
            <div class="row">
                <label class="rxmain-container-header-label">
                    ${message(code: 'app.rxlogix.signal.alert.detail.meeting.list.label')}
                </label>
                <span class="pv-head-config configureFields">
                    <a href="javascript:void(0);" class="ic-sm meeting-search-btn" title="Search">
                        <i class="md md-search" aria-hidden="true"></i>
                    </a>
                    <a href="#" class="pull-right ic-sm meeting-create ${buttonClass}" title="Add Meeting">
                        <i class="md md-add" aria-hidden="true"></i>
                    </a>

                    <a class="dropdown-toggle exportPanel pull-right ic-sm" data-toggle="dropdown" tabindex="0" accesskey="x" title="Export to">
                        <i class="mdi mdi-export ic-sm"></i>
                        <span class="caret hidden"></span>
                    </a>
                    <ul class="dropdown-menu export-type-list" id="exportTypes">
                        <strong class="font-12">Export</strong>
                        <li><g:link controller="validatedSignal" action="exportMeetingDetailReport" class="m-r-30" style="text-transform: none"
                                    params="${[outputFormat: ReportFormat.DOCX, signalId: alertInst.id, appType: appType, callingScreen: 'review']}">
                            <img src="/signal/assets/word-icon.png" class="m-r-10" height="16"
                                 width="16"/><g:message code="save.as.word"/>
                        </g:link>
                        </li>
                        <li><g:link controller="validatedSignal" action="exportMeetingDetailReport" class="m-r-30" style="text-transform: none"
                                    params="${[outputFormat: ReportFormat.XLSX, signalId: alertInst.id, appType: appType, callingScreen: 'review']}">
                            <img src="/signal/assets/excel.gif" class="m-r-10" height="16" width="16"/><g:message
                                    code="save.as.excel"/>
                        </g:link></li>
                        <li><g:link controller="validatedSignal" action="exportMeetingDetailReport" class="m-r-30" style="text-transform: none"
                                    params="${[outputFormat: ReportFormat.PDF, signalId: alertInst.id, appType: appType, callingScreen: 'review']}">
                            <img src="/signal/assets/pdf-icon.jpg" class="m-r-10" height="16" width="16"/><g:message
                                    code="save.as.pdf"/>
                        </g:link></li>
                    </ul>

                </span>
            </div>
        </div>

        <div class="rxmain-container-content row">
            <div class="body">
                <div id="action-list-conainter" class="list pv-max-scrollable-table dropdown-outside">
                    <table id="meeting-table" class="row-border hover dataTable no-footer"
                           style="width: 100%">
                        <thead>
                        <tr>
                            <th>Meeting Title</th>
                            <th>Meeting Date</th>
                            <th>Meeting Minutes</th>
                            <th>Owner</th>
                            <th>Agenda</th>
                            <th>Last Modified By</th>
                            <th>Last Modified</th>
                            <th>Action Status</th>
                        </tr>
                        </thead>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

<div id="schedularContainer" style="display: none;">
    <g:render template="/meeting/schedulerTemplate"/>
</div>