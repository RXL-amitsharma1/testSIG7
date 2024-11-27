<%@ page import="com.rxlogix.enums.ReportFormat" %>
    <g:render template="/includes/layout/flashErrorsDivs" bean="${executedConfiguration}" var="theInstance"/>
    <div class="row">
        <div class="panel-heading pv-sec-heading m-b-10">
            <div class="row">
                <div class="col-md-7">
                    <span class="panel-title">${name}</span>
                </div>
                <div class="col-md-5">
                    <span class="pull-right m-l-1" style="font-size: 16px; margin-right:15px; cursor: pointer">
                        <span class="dropdown-toggle exportPanel" data-toggle="dropdown">
                            %{--<i data-title="Export Report" class="glyphicon glyphicon-save-file grid-menu-tooltip"></i>--}%
                            <img src="/signal/assets/excel.gif" class="" height="20" width="20" data-title="Export" data-toggle="tooltip" data-placement="bottom"/>
                            <span class="caret"></span></button>

                        </span>
                        <ul class="dropdown-menu export-type-list" id="exportTypes">
                            <li><g:link controller="activity" action="exportActivitiesReport" class="m-r-30"
                                        params="${[outputFormat: ReportFormat.DOCX, alertId:alertId, appType:type, callingScreen: callingScreen]}">
                                <img src="/signal/assets/word-icon.png" class="m-r-10" height="16" width="16" /></i><g:message code="save.as.word" />
                            </g:link>
                            </li>
                            <li><g:link controller="activity" action="exportActivitiesReport" class="m-r-30"
                                        params="${[outputFormat: ReportFormat.XLSX, alertId:alertId, appType:type, callingScreen: callingScreen]}">
                                <img src="/signal/assets/excel.gif" class="m-r-10" height="16" width="16" /><g:message code="save.as.excel" />
                            </g:link></li>
                            <li><g:link controller="activity" action="exportActivitiesReport" class="m-r-30"
                                        params="${[outputFormat: ReportFormat.PDF, alertId:alertId, appType:type, callingScreen: callingScreen]}">
                                <img src="/signal/assets/pdf-icon.jpg" class="m-r-10" height="16" width="16" /><g:message code="save.as.pdf" />
                            </g:link></li>
                        </ul>
                    </span>
                </div>
            </div>
        </div>
    </div>
        <div id="alertIdHolder" data-alert-id="${alertId}" data-alert-type="${type}"></div>
        <table id="activitiesTable" class="dataTable" width="100%">
            <thead>
            <tr>
                <th><g:message code="app.reportField.masterCaseNum" /></th>
                <th><g:message code="app.label.activity.type" /></th>
                <th><g:message code="app.label.suspect.product" /></th>
                <th><g:message code="app.queryLevel.EVENT" /></th>
                <th><g:message code="app.label.description" /></th>
                <th><g:message code="app.label.current.assignment" /></th>
                <th><g:message code="app.label.performed.by" /></th>
                <th><g:message code="app.label.timestamp" /></th>
            </tr>
            </thead>
        </table>
