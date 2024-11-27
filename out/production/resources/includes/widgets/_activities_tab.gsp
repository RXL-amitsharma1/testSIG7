<%@ page import="com.rxlogix.enums.ReportFormat" %>

<div class="row">
    <div class="panel-heading pv-sec-heading m-b-10">
        <div class="row">
            <div class="col-md-7">
                <g:if test="${!isCaseSeries}">
                    <span class="panel-title cell-break">${name}</span>
                </g:if>
                <g:else>
                    <span class="panel-title"><g:message code="app.label.details.case.series"/></span>
                </g:else>
            </div>
            <div class="col-md-5 ico-menu">
                <span class="pull-right pos-rel">
                    <span tabindex="0" class="dropdown-toggle exportPanel" data-toggle="dropdown" data-title="Export to" data-placement="bottom">
                        <i class="mdi mdi-export font-24 blue-1 lh-1"></i>
                        <span class="caret hidden"></span></button>

                    </span>
                    <ul class="dropdown-menu export-type-list" id="exportTypes">
                        <strong class="font-12">Export</strong>
                        <li><g:link controller="activity" action="exportActivitiesReport" class="m-r-30 "
                                    params="${[outputFormat: ReportFormat.DOCX, alertId:alertId, appType:type, callingScreen: callingScreen]}">
                            <img src="/signal/assets/word-icon.png" class="m-r-10" height="16" width="16" /><g:message code="save.as.word" />
                        </g:link>
                        </li>
                        <li><g:link controller="activity" action="exportActivitiesReport" class="m-r-30 "
                                    params="${[outputFormat: ReportFormat.XLSX, alertId:alertId, appType:type, callingScreen: callingScreen]}">
                            <img src="/signal/assets/excel.gif" class="m-r-10" height="16" width="16" /><g:message code="save.as.excel" />
                        </g:link></li>
                        <li><g:link controller="activity" action="exportActivitiesReport" class="m-r-30 "
                                    params="${[outputFormat: ReportFormat.PDF, alertId:alertId, appType:type, callingScreen: callingScreen]}">
                            <img src="/signal/assets/pdf-icon.jpg" class="m-r-10" height="16" width="16" /><g:message code="save.as.pdf" />
                        </g:link></li>
                    </ul>
                </span>
            </div>
        </div>
    </div>
    <div class="row">
        <div class="col-md-12 h-475">
            <table id="activitiesTable" class="row-border hover no-shadow" width="100%">
                <thead>
                <tr>
                    <g:if test="${type == 'Single Case Alert'}">
                        <th><g:message code="app.reportField.masterCaseNum" /></th>
                    </g:if>
                    <th class=""><g:message code="app.label.activity.type" /></th>
                    <th><g:message code="app.label.suspect.product" /></th>
                    <th><g:message code="app.queryLevel.EVENT" /></th>
                    <th><g:message code="app.label.description" /></th>
                    <th><g:message code="app.label.current.assignment" /></th>
                    <th><g:message code="app.label.performed.by" /></th>
                    <th><g:message code="app.label.timestamp" /></th>
                </tr>
                </thead>
            </table>
        </div>
    </div>
</div>
