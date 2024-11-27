<%@ page import="com.rxlogix.enums.ReportFormat;grails.util.Holders;" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.controlpanel.title"/></title>
    <asset:stylesheet src="toggle-button.css"/>
    <asset:stylesheet src="apiControlPanel.css"/>
    <asset:javascript src="apiControlPanel.js"/>
    <style>
        select.form-control + .select2 {
             width: 95% !important;
        }
        #dispositionEndOfReviewContainer .rxmain-container-header {
            background: #d5d5d5 !important;
        }
    </style>
</head>
<body>
<script>
    var signalChartsUrl = '<g:createLink controller="apiControlPanel" action="enableSignalChartsCheck"/>';
    var url = '<g:createLink controller="apiControlPanel" action="toggleEtlPvrConnectivityCheck"/>';
    var pgUpdateUrl = '<g:createLink controller="apiControlPanel" action="enablePGUpdateCheck"/>';
    var updateDisplayDueInCheckUrl = '<g:createLink controller="apiControlPanel" action="updateDisplayDueIn"/>';
    var enableEndOfMilestoneCheckUrl = '<g:createLink controller="apiControlPanel" action="updateEndOfMilestone"/>';
    var updateSelectedEndpointsUrl = '<g:createLink controller="apiControlPanel" action="updateSelectedEndpoints"/>';
    var updateDateClosedBasedOnDispositionUrl = '<g:createLink controller="apiControlPanel" action="updateDateClosedBasedOnDisposition"/>';
    var getValuesforDropdownUrl = '<g:createLink controller="apiControlPanel" action="getValuesforWorkflowDropdown"/>';
    var updateDateClosedBasedOnWorkflowUrl = '<g:createLink controller="apiControlPanel" action="updateDateClosedBasedOnWorkflow"/>'
    var updateDispositionEndPointsUrl = '<g:createLink controller="apiControlPanel" action="updateDispositionEndPoints"/>';
    var dispositionEndPointTableUrl = '<g:createLink controller="apiControlPanel" action="dispositionEndPointTable"/>';
    var deleteByEndPointUrl = '<g:createLink controller="apiControlPanel" action="deleteByEndPoint"/>';
    var importConfigUrl = '<g:createLink controller="adHocAlertRest" action="readData"/>';
    var updateExportAlways = '<g:createLink controller="apiControlPanel" action="updateExportAlways"/>';
    var updatePromptUser = '<g:createLink controller="apiControlPanel" action="updatePromptUser"/>';
    var VALIDATED = {
                     lastETLBatchLotDownloadLink: "${createLink(controller: 'apiUploadStatusReport', action: 'exportLastETLBatchLots')}"
                };
    var isSignalWorkflowEnabled = ${isSignalWorkflowEnabled};
    var isEndOfMilestone = ${isEndOfMilestone};
    var selectedEndPoints = "${selectedEndPoints}";
    var dateClosedDisposition = "${dateClosedDisposition}";
    var dateClosedWorkflowDisposition = "${dateClosedWorkflowDisposition}";
    var dateClosedWorkflow = "${dateClosedWorkflow}";
    var isDisposition = ${isDisposition}
</script>

<div id="accordion-pvs-analysis">

<g:render template="/includes/layout/flashErrorsDivs" bean="${configurationInstance}" var="theInstance"/>

    <div class="panel panel-default rxmain-container rxmain-container-top m-b-0">
        <div class="rxmain-container-row rxmain-container-header panel-heading pv-sec-heading">
            <div class="row">
                <div class="col-md-4">
                    <label class="rxmain-container-header-label m-t-5"><g:message code="app.controlpanel.title"/></label>
                </div>
            </div>
        </div>

    </div>
    <!-- Check Below -->
    <div class="row controlPannelContainer">
    <div class="row">
        <h3>Add New Users</h3>

        <g:form controller="Admin" action="addUsers" enctype="multipart/form-data" id = "excelFileUpload">
            <div>
                <label for="excelFile">Upload File</label>
                <br>
                <input id="excelFile" type="file" name="excelFile">
            </div>

            <div>
                <br>
                <g:submitButton class='btn btn-primary' name="uploadbutton" value="Upload" disabled="true"/>
                <a href="/signal/assets/data/template/Users.xlsx" target="_blank" title="">Click here to download sample users sheet</a>
            </div><br>
            <g:link controller="admin" action="downloadExistingUsers">Click here to download list of existing users</g:link>
            <br>
        </g:form>
    </div>
    <hr style="margin: 10px 20px 2px 0px;"/>
    <div class="row">
        <h3>API Token</h3>

        <div class="col-md-8">
            <input type="text" id="api-token-field" name="apiToken" value="${apiToken}" placeholder="<g:message code="app.controlpanel.APIToken"/>" class="form-control">
        </div>

        <div class="row">
            <a id="token-gen-bt" href="#" class="btn btn-primary "><g:message code="app.controlpanel.GenerateNewToken"/></a>
        </div>
        <div class="row">
            <span class="text-muted" style="padding-left: 10px;"><g:message code="app.controlpanel.GenerateNewTokenMessage"/></span>
        </div>
    </div>
    <hr style="margin: 10px 20px 2px 0px;"/>
    <g:if test="${Holders.config.grails.mail.oAuth.enabled}">
        <h3><g:message code="mail.auth.configuration.title.label"/></h3>

        <div class="margin20Top">
            <g:form controller="mailOAuth" action="sendTestMail">
                <div class="form-group row">
                    <label for="email" class="col-md-1 col-form-label"><g:message
                            code="app.label.email.email"/></label>

                    <div class="col-md-2">
                        <g:textField name="email" class="form-control"
                                     value="${sec.loggedInUserInfo(field: "email")?.decodeHTML()}"/>
                    </div>

                    <div class="col-md-2">
                        <g:submitButton class="btn btn-info" name="testEmail"
                                        value="${message(code: 'mail.auth.test.email.btn.label')}"/> &nbsp;
                        <g:link controller="mailOAuth" action="generate" class="btn btn-primary"><g:message
                                code="mail.auth.generate.token.btn.label"/></g:link>
                    </div>
                </div>
            </g:form>
        </div>
    </g:if>
    <hr style="margin: 10px 20px 2px 0px;"/>
    <div class="row">
        <h3><g:message code="app.controlpanel.BatchLotData"/></h3>
        <div class="text-muted" style="margin-bottom: 10px;">
            <g:message code="app.controlpanel.DisplayTheStatusOfBatchLotDataload"/>
        </div>
        <div>
            <g:link controller="apiUploadStatusReport" action="index" class="btn btn-primary ">
                <span class="glyphicon glyphicon-resize-horizontal"></span>
                <g:message code="app.controlpanel.BatchLotDatalogs"/>
            </g:link>
        </div>
    </div>
    <hr style="margin: 10px 20px 2px 0px;"/>
    <div class="row">
        <h3><g:message code="app.controlpanel.BatchLotETLStatus"/></h3>
        <div class="row" >
            <div class="col-md-3"><label><g:message code="app.controlpanel.ETLRunStatus"/></label></div>
            <div class="col-md-2">
                <g:if test="${lastEtlStatus == 'FAILED' }">
                    <span id="etlStatusBtn" class="etlStatusBtn" style="background-color:#ef5350; color:#f3f3f3;">Failed</span>
                </g:if>
                <g:if test="${lastEtlStatus == 'RUNNING' }">
                    <span id="etlStatusBtn" class="etlStatusBtn" style="background-color: #FDBB40;">Running</span>
                </g:if>
                <g:if test="${lastEtlStatus == 'COMPLETED_BUT_FAILED' }">
                    <span id="etlStatusBtn" class="etlStatusBtn">Success</span>
                </g:if>
                <g:if test="${lastEtlStatus == 'SUCCESS' }">
                    <span id="etlStatusBtn" class="etlStatusBtn">Success</span>
                </g:if>
                <span id="refreshETLStatus" title="Refresh" class="glyphicon reloaderBtn glyphicon-refresh" style="margin-top: 0px;"></span>
            </div>
        </div>

        <div class="row" >
            <div class="col-md-3"><label><g:message code="app.controlpanel.LastSuccessfulRun"/></label></div>
            <div id="lastEtlDate" class="col-md-2">${lastEtlDate}</div>
        </div>
        <div class="row">
            <div class="col-md-3"><label><label><g:message code="app.controlpanel.LastSuccessfulBatchLoad"/></label></div>
            <div id="lastEtlBatchIdsDiv" class="col-md-7">
                <span id="lastEtlBatchIds" title="${lastEtlBatchIds}">
                 <g:if test="${lastEtlBatchIds.length() > 90}">${ lastEtlBatchIds.substring(0, 90) }...</g:if>
                 <g:if test="${lastEtlBatchIds.length() <= 90}">${ lastEtlBatchIds } </g:if>
                </span>
                <a href="/signal/apiUploadStatusReport/exportLastETLBatchLots" id="exportLastETLBatchLotsHREF" class="apiexport mdi mdi-export" data-ol-has-click-handler="" data-original-title="" title="<g:message code="export.to.excel"/>">
                </a>
            </div>
        </div>

        <div class="row" style="margin-bottom: 10px;">
            <div class="col-md-5"><span class="text-red-important"><g:message code="app.controlpanel.NoteForPVDEtlTrigger"/></span></div>
        </div>
        <div>
            <g:if test="${remainingBatchLotCountForETL > 0 && pvdETLCompleted=="YES" && lastEtlStatus != 'RUNNING'}">
                <a href="#runApiEtlScheduleNow" id="runApiEtlScheduleNowHref" class="btn btn-primary" data-toggle="modal">
                    <span class="glyphicon glyphicon-resize-horizontal"></span>
                    <g:message code="run.initial.etl"/>
                </a>
            </g:if>
            <g:if test="${remainingBatchLotCountForETL == 0 && pvdETLCompleted=="YES"}">
                <a href="javascript:return false;" class="btn btn-primary" disabled="disabled">
                    <g:message code="run.initial.etl"/>
                </a>
            </g:if>
            <g:if test="${pvdETLCompleted=="NO"}">
                <a href="javascript:return false;" class="btn btn-primary" disabled="disabled" title="<g:message code="app.controlpanel.TheOptionWillBeAvailableOncePVDETLIsCompleted"/>">
                    <g:message code="run.initial.etl"/>
                </a>
            </g:if>
            <g:if test="${remainingBatchLotCountForETL > 0 && pvdETLCompleted=="YES" && lastEtlStatus == 'RUNNING'}">
                <a href="javascript:return false;" class="btn btn-primary" disabled="disabled">
                    <g:message code="run.initial.etl"/>
                </a>
            </g:if>
        </div>
        <div class="modal fade" id="runApiEtlScheduleNow" tabindex="-1" role="dialog" aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <label><g:message code="run.initial.etl"/></label>
                    </div>

                    <div class="modal-body">
                        <g:message code="app.controlpanel.ETLEillBeExecutedForRemainingRecords" args="[remainingBatchLotCountForETL]" />
                    </div>

                    <div class="modal-footer">
                        <button type="button" id="runApiEtl" class="btn btn-primary clearEventValues"><g:message code="default.button.ok.label"/></button>
                        <button type="button" class="btn btn-default" data-dismiss="modal"><g:message code="default.button.cancel.label"/></button>
                    </div>
                </div>
            </div>
        </div>
        <hr style="margin: 10px 20px 2px 0px;"/>
        <div class="row">
            <h3><g:message code="app.controlpanel.ProductGroupImport"/></h3>
            <div class="text-muted" style="margin-bottom: 10px;">
                <g:message code="app.controlpanel.DisplayTheStatusOfProductGroupImport"/>
            </div>
            <div>
                <g:link controller="productGroupStatus" action="index" class="btn btn-primary ">
                    <span class="glyphicon glyphicon-resize-horizontal"></span>
                    <g:message code="app.controlpanel.ProductGroupImportLogs"/>
                </g:link>
            </div>
        </div>
        <hr style="margin: 10px 20px 2px 0px;"/>
        <div class="row">
            <h3>Alert Administration</h3>
            <div>
                <g:link controller="alertAdministration" action="index" class="btn btn-primary">
                    <span class="glyphicon glyphicon-resize-horizontal"></span>
                    <g:message code="app.label.alert.administration" default="Alert Administration"/>
                </g:link>
            </div>
        </div>
        <hr style="margin: 10px 20px 2px 0px;"/>
        <div class="row">
            <h3>Signal Configurations</h3>
            <div>
                <div class="form-check form-switch">
                    <div class="col-md-4"><label>Enable Signal Assessment Charts</label></div>
                    <label class="switch">
                        <g:if test="${isEnableSignalCharts}">
                            <input type="checkbox" name="signalChartDisableEnable" value="${isEnableSignalCharts}" id="toggleSignalCharts" checked>
                        </g:if>
                        <g:else>
                            <input type="checkbox" name="signalChartDisableEnable" value="${isEnableSignalCharts}" id="toggleSignalCharts">
                        </g:else>
                        <span class="slider round"></span>
                    </label>
                </div>
                <div class="form-check form-switch">
                    <div class="col-md-4"><label>Display Due In</label></div>
                    <label class="switch">
                        <g:if test="${isDisplayDueIn}">
                            <input type="checkbox" name="displayDueInDisableEnable" value="${isDisplayDueIn}" id="toggleDisplayDueIn" checked>
                        </g:if>
                        <g:else>
                            <input type="checkbox" name="displayDueInDisableEnable" value="${isDisplayDueIn}" id="toggleDisplayDueIn">
                        </g:else>
                        <span class="slider round"></span>
                    </label>
                </div>
                <div style="display: flex">
                    <div class="col-md-4"><label>Due In Endpoint</label>
                        <a class="glyphicon glyphicon-info-sign themecolor modal-link"
                           data-toggle="modal"
                           data-target="#dueInEndpointHelpModal" style="cursor:pointer;" data-toggle="tooltip"
                           data-original-title="Due In Endpoint">
                        </a>
                    </div>
                    <label class="col-md-4">
                        <span id="dueInEndpointSelectSpan">
                            <g:select id="dueInEndpointSelect" name="dueInEndpoint" from="${signalStatusList}"
                                      value=""
                                      data-value="${signalStatusList}"
                                      class="form-control" />
                            <a tabindex="0" title="Save" id="saveDueInEndpoint" style="cursor: pointer; display: none"><i
                                    class="mdi mdi-check grey-2"></i></a>
                        </span>
                    </label>
                </div>
                <g:if test = "${isSignalWorkflowEnabled}">
                    <div style="display: flex">
                        <div class="col-md-4"><label>Date Closed Based On Disposition/Workflow</label>
                            <a class="glyphicon glyphicon-info-sign themecolor modal-link"
                               data-toggle="modal"
                               data-target="#dateClosedWorkflowHelpModal" style="cursor:pointer;" data-toggle="tooltip"
                               data-original-title="Date Closed Based On Disposition/Workflow">
                            </a>
                            <form>
                                <label>
                                    <input type="radio" name="option" value="signalDisposition"> Signal Disposition
                                </label>
                                <label>
                                    <input type="radio" name="option" value="signalWorkflow"> Signal Workflow
                                </label>
                            </form>
                        </div>
                        <label class="col-md-4">
                            <span id="dateClosedBasedOnDispSpanWorkflow">
                                <g:select id="dateClosedBasedOnDispWorkflow" name="dateClosedWorkflow" from="${dateClosedWorkflowList}"
                                          value=""
                                          data-value= "${dateClosedWorkflowList}"
                                          class="form-control"/>
                                <a tabindex="0" title="Save" id="saveDateClosedDispWorkflow" style="cursor: pointer; display: none"><i
                                        class="mdi mdi-check grey-2"></i></a>
                            </span>
                        </label>
                    </div>
                </g:if>
                <g:else>
                    <div style="display: flex">
                        <div class="col-md-4"><label>Date Closed Based On Disposition</label>
                            <a class="glyphicon glyphicon-info-sign themecolor modal-link"
                               data-toggle="modal"
                               data-target="#dateClosedDispHelpModal" style="cursor:pointer;" data-toggle="tooltip"
                               data-original-title="Date Closed Based On Disposition">
                            </a>
                        </div>
                        <label class="col-md-4">
                            <span id="dateClosedBasedOnDispSpan">
                                <g:select id="dateClosedBasedOnDisp" name="dateClosed" from="${reviewCompletedDispList}"
                                          value=""
                                          data-value= "${reviewCompletedDispList}"
                                          class="form-control"/>
                                <a tabindex="0" title="Save" id="saveDateClosedDisp" style="cursor: pointer; display: none"><i
                                        class="mdi mdi-check grey-2"></i></a>
                            </span>
                        </label>
                    </div>
                </g:else>
                <div class="form-check form-switch">
                    <div class="col-md-4"><label>End of Review Milestone Date Auto Population Configuration</label>
                        <a class="glyphicon glyphicon-info-sign themecolor modal-link"
                           data-toggle="modal"
                           data-target="#endOfReviewMilestone" style="cursor:pointer;" data-toggle="tooltip"
                           data-original-title="Disposition end of review milestone configuration">
                        </a></div>
                    <label class="switch">
                        <g:if test="${isEndOfMilestone}">
                            <input type="checkbox" name="endOfMilestone" value="${isEndOfMilestone}" id="toggleEndOfMilestone" checked>
                        </g:if>
                        <g:else>
                            <input type="checkbox" name="endOfMilestone" value="${isEndOfMilestone}" id="toggleEndOfMilestone" >
                        </g:else>
                        <span class="slider round"></span>
                    </label>
                </div>
                <g:render template="/includes/dispositionEndOfReviewTable"/>
            </div>
        </div>
        <hr style="margin: 10px 20px 2px 0px;"/>
        <div class="row">
            <h3>Product Group Update Configurations</h3>
            <div>
                <div class="form-check form-switch">
                    <div class="col-md-4"><label>Product Group Update Pre-check Status</label></div>
                    <label class="switch">
                        <g:if test="${pgUpdateCheck}">
                            <input type="checkbox" name="pgUpdateDisableEnable" value="${pgUpdateCheck}" id="togglePGUpdateCheck" checked>
                        </g:if>
                        <g:else>
                            <input type="checkbox" name="pgUpdateDisableEnable" value="${pgUpdateCheck}" id="togglePGUpdateCheck">
                        </g:else>
                        <span class="slider round"></span>
                    </label>
                </div>
            </div>
        </div>
        <hr style="margin: 10px 20px 2px 0px;"/>
        <div class="row">
            <h3><g:message code="app.controlpanel.productType" default="Aggregate Alert Product Type Configuration"/></h3>
            <div class="text-muted" style="margin-bottom: 10px;">
                <g:message code="app.controlpanel.productTypeAggregate" default="Configuration screen for Aggregate Alert Product Type Configuration"/>
            </div>
            <div>
                <g:link controller="productTypeConfiguration" action="index" target="_blank" class="btn btn-primary ">
                    <span class="glyphicon glyphicon-resize-horizontal"></span>
                    <g:message code="app.controlpanel.productTypeConfiguration" default="Product Type Configuration"/>
                </g:link>
            </div>
        </div>

        <div>&nbsp;<br/></div>
 </div>

    <div class="modal fade endOfReviewMilestone" id="endOfReviewMilestone" tabindex="-1" role="dialog" aria-labelledby="End of review milestone configuration help">
        <div class="modal-dialog modal-lg " role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                    <span><b><g:message code="controlPanel.endOfReview.help.title"/></b></span>
                </div>
                <div class="modal-body container-fluid border">
                    <div class=""><g:message code="controlPanel.endOfReview.help.text" /></div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default cancel" data-dismiss="modal"><g:message code="default.button.ok.label"/></button>
                </div>
            </div>
        </div>
    </div>

    <div class="modal fade dueInEndpointHelpModal" id="dueInEndpointHelpModal" tabindex="-1" role="dialog" aria-labelledby="Due In Endpoint help">
        <div class="modal-dialog modal-lg " role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                    <span><b><g:message code="controlPanel.dueInEndpoint.help.title"/></b></span>
                </div>
                <div class="modal-body container-fluid border">
                    <div class=""><g:message code="controlPanel.dueInEndpoint.help.text" /></div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default cancel" data-dismiss="modal"><g:message code="default.button.ok.label"/></button>
                </div>
            </div>
        </div>
    </div>
    <div class="modal fade dateClosedDispHelpModal" id="dateClosedDispHelpModal" tabindex="-1" role="dialog" aria-labelledby="Date Closed Based On Disposition help">
        <div class="modal-dialog modal-lg " role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                    <span><b><g:message code="controlPanel.dateClosedBasedOnDisposition.help.title"/></b></span>
                </div>
                <div class="modal-body container-fluid border">
                    <div class=""><g:message code="controlPanel.dateClosedBasedOnDisposition.help.text" /></div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default cancel" data-dismiss="modal"><g:message code="default.button.ok.label"/></button>
                </div>
            </div>
        </div>
    </div>
    <div class="modal fade dateClosedWorkflowHelpModal" id="dateClosedWorkflowHelpModal" tabindex="-1" role="dialog" aria-labelledby="Date Closed Based On Disposition/Workflow help">
        <div class="modal-dialog modal-lg " role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                    <span><b><g:message code="controlPanel.dateClosedBasedOnWorkflow.help.title"/></b></span>
                </div>
                <div class="modal-body container-fluid border">
                    <div class=""><g:message code="controlPanel.dateClosedBasedOnWorkflow.help.text" /></div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default cancel" data-dismiss="modal"><g:message code="default.button.ok.label"/></button>
                </div>
            </div>
        </div>
    </div>
    <hr style="margin: 10px 20px 2px 0px;"/>
    <div class="row">
        <h3>Case Narrative Export</h3>
        <div>
            <div class="form-check form-switch">
                <div class="col-md-4">
                    <label>Export Always <br>
                        <span class="text-muted">
                            This will enable Case Narrative export from Individual Case Alerts and Case Series Export
                        </span>
                    </label>
                </div>
                <label class="switch">
                    <g:if test="${exportAlways}">
                        <input type="checkbox" name="exportAlways" value="${exportAlways}" id="toggleExportAlways" checked>
                    </g:if>
                    <g:else>
                        <input type="checkbox" name="exportAlways" value="${exportAlways}" id="toggleExportAlways">
                    </g:else>
                    <span class="slider round"></span>
                </label>
            </div><br><br>
            <div class="form-check form-switch">
                <div class="col-md-4">
                    <label>Prompt User <br>
                        <span class="text-muted">
                            This will ask user via confirmation prompt if they want to export the Case Narrative along with
                            the other data when exporting Individual Case Alerts and Case Series.
                        </span>
                    </label>
                </div>
                <label class="switch">
                    <g:if test="${promptUser}">
                        <input type="checkbox" name="promptUser" value="${promptUser}" id="togglePromptUser" checked>
                    </g:if>
                    <g:else>
                        <input type="checkbox" name="promptUser" value="${promptUser}" id="togglePromptUser">
                    </g:else>
                    <span class="slider round"></span>
                </label>
            </div>
        </div>
    </div>
    <hr style="margin: 10px 20px 2px 0px;"/>
    <div class="row">
        <h3>Configuration Management</h3>

        <div>
            <g:link controller="configManagement" action="index" class="btn btn-primary">
                <span class="glyphicon glyphicon-resize-horizontal"></span>
                <g:message code="app.label.config.management" default="Configuration Management"/>
            </g:link>
        </div>
    </div>
    <hr style="margin: 10px 20px 2px 0px;"/>
    <div class="row">
        <h3>Business Rules Migrations</h3>
        <div class="row" >
            <div class="col-md-2"><label><g:message code="app.controlpanel.migrationStatus"/></label></div>
            <div id="migrationStatus" class="col-md-1">${rulesMigrationRequired ? "Error" : "Success"}
            <a href="/signal/businessConfiguration/exportRulesMigrationStatus/" id="exportMigrationStatus" class="apiexport mdi mdi-export" data-ol-has-click-handler="" data-original-title="" title="<g:message code="export.to.excel"/>">
            </a></div>
        </div>

        <div>
            <g:if test="${rulesMigrationRequired}">
                <a id="run-migrations" href="#" class="btn btn-primary "><g:message code="app.controlpanel.runMigrations"/></a>
            </g:if>
            <g:else>
                <a id="run-migrations" href="#" class="btn btn-primary disabled"><g:message code="app.controlpanel.runMigrations"/></a>
            </g:else>
        </div>
    </div>
    <hr style="margin: 10px 20px 2px 0px;"/>
    <!-- The signal modal goes here. -->
<g:render template="/includes/modals/downloadReportDatePickerModal"/>
<g:render template="/template/fieldConfiguration" model="[fieldConfigurationBarId: 'validatedSignalFields']"/>
</div>
</body>
