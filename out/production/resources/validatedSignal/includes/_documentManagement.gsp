<%@ page import="com.rxlogix.enums.ReportFormat; com.rxlogix.Constants" %>
<script>
    var signalId = "${signal.id}";
    $(document).ready(function () {
        $("#generate-report").click(function () {
            $("#generating-report").removeClass("hide");
        });

        $('.sendToDms').click(function () {
            $('#sendToDmsModal').modal();
            $('#docTypeValue').val($(this).data('doc-type'));
            $('#productSelectionDMS').val($('#productSelectionAssessment').val());
            $('#eventSelectionDMS').val($('#eventSelectionAssessment').val());
            $('#dataSourceDMS').val($('#dataSources').val());
            $('#dateRangeDMS').val($('#dateRange').val());
            if ($(this).data('report-type')) {
                $('#reportTypeDMS').val($(this).data('report-type'));
            }
        });
    });
    $('a#documentManagement').click(function () {
        if(!$('#productSelectionAssessment').val()) {
            $.ajax({
                url: VALIDATED.assessmentFilterUrl + "?id=" + $("#signalId").val(),
                success: function (result) {
                    $('#productSelectionAssessment').val('{"1":[],"2":[],"3":' + JSON.stringify(result.productList) + ',"4":[],"5":[]}');
                    $('#eventSelectionAssessment').val('{"1":[],"2":[],"4":' + JSON.stringify(result.eventList) + ',"3":[],"5":[],"6":[]}');
                }
            });
        }
    });
</script>

<style type="text/css">
#loading {
    width: 100%;
    height: 100%;
    top: 0px;
    left: 0px;
    position: fixed;
    display: none;
    opacity: 0.7;
    background-color: #fff;
    z-index: 99;
    text-align: center;
    background-position: center;
}

#loading-image {
    position: fixed;
    top: 50%;
    left: 50%;
    z-index: 100;
}

</style>

<asset:stylesheet src="jquery-picklist.css"/>

<asset:javascript src="app/pvs/documentManagement/document.js"/>
<asset:javascript src="app/pvs/documentManagement/summaryReportPreference.js"/>
<asset:javascript src="jquery/jquery-picklist.js"/>
<g:javascript>
   var downloadSummaryReportPDFUrl = "${createLink(controller: "validatedSignal", action: 'generateSignalSummaryReport', params: [outputFormat: ReportFormat.PDF, signalId: signal.id, reportType: Constants.SignalReportTypes.SIGNAL_SUMMARY, assessmentRequired : true])}";
   var downloadSummaryReportXLSXUrl = "${createLink(controller: "validatedSignal", action: 'generateSignalSummaryReport', params: [outputFormat: ReportFormat.XLSX, signalId: signal.id, reportType: Constants.SignalReportTypes.SIGNAL_SUMMARY, assessmentRequired : true])}";
   var downloadSummaryReportDocXUrl = "${createLink(controller: "validatedSignal", action: 'generateSignalSummaryReport', params: [outputFormat: ReportFormat.DOCX, signalId: signal.id, reportType: Constants.SignalReportTypes.SIGNAL_SUMMARY, assessmentRequired : true])}";
</g:javascript>
<rx:container title="Signal Reports">
    <div>&nbsp;</div>
    <div id="generated-reports" class="row">
        <table class="table">
            <thead>
            <th class="col-md-2">Report Name</th>
            <th class="col-md-2">Download</th>
            </thead>
            <tbody>
            <tr>
                <td><span><g:message code="app.label.signal.summary.report" default="Signal Summary Report"/></span>
                </td>
                <td>
                    <a href="#" class="downloadReport pdf m-r-15 export-icon-link">
                        <asset:image src="pdf-icon.jpg" class="pdf-icon" height="16" width="16"/>
                    </a>
                    <a href="#" class="downloadReport xlsx m-r-15 export-icon-link">
                        <asset:image src="excel.gif" class="excel-icon" height="16" width="16"/>
                    </a>
                    <a href="#" class="downloadReport word m-r-15 export-icon-link">
                        <asset:image src="word-icon.png" class="word-icon" height="16" width="16"/>
                    </a>
                    <a href="#" data-toggle="modal" data-target="#signalSummaryReportPreferenceModal">
                        <asset:image src="settings-icon.png" class="settings-icon m-r-15" height="16" width="16"/>
                    </a>
                    <g:if test="${grailsApplication.config.dms.enabled}">
                        <a href="#" class="sendToDms ${buttonClass}" role="menuitem"
                           data-doc-type="${Constants.DMSDocTypes.SIGNAL_SUMMARY_REPORT}"
                           data-report-type="${Constants.SignalReportTypes.SIGNAL_SUMMARY}"
                           data-target="#sendToDmsModal"><i data-toggle="tooltip" class="fa fa-upload font-16" title="Send to DMS"> </i></a>
                    </g:if>
                </td>
            </tr>
            </tbody>
        </table>
        <table class="table">
            <thead>
            <th class="col-md-2">Other Reports</th>
            <th class="col-md-2">Download</th>
            </thead>
            <tbody>
            <tr>
                <td><span><g:message code="app.label.signal.pbrer.summary.report"/></span></td>
                <td>
                    <g:link controller="validatedSignal" action="generateSignalReports" class="export-icon-link"
                            params="${[outputFormat: ReportFormat.PDF, signalId: signal.id, reportType: Constants.SignalReportTypes.PEBER]}">
                        <asset:image src="pdf-icon.jpg" class="pdf-icon m-r-15" height="16" width="16"/>
                    </g:link>

                    <g:link controller="validatedSignal" action="generateSignalReports" class="export-icon-link"
                            params="${[outputFormat: ReportFormat.XLSX, signalId: signal.id, reportType: Constants.SignalReportTypes.PEBER]}">
                        <asset:image src="excel.gif" class="excel-icon m-r-15" height="16" width="16"/>
                    </g:link>

                    <g:link controller="validatedSignal" action="generateSignalReports" class="export-icon-link"
                            params="${[outputFormat: ReportFormat.DOCX, signalId: signal.id, reportType: Constants.SignalReportTypes.PEBER]}">
                        <asset:image src="word-icon.png" class="word-icon m-r-15" height="16" width="16"/>
                    </g:link>
                    <g:if test="${grailsApplication.config.dms.enabled}">
                        <a href="#" class="sendToDms ${buttonClass}" role="menuitem"
                           data-doc-type="${Constants.DMSDocTypes.PBRER_SIGNAL_SUMMARY_REPORT}"
                           data-target="#sendToDmsModal"><i data-toggle="tooltip" class="fa fa-upload font-16" title="Send to DMS"> </i></a>
                    </g:if>

                </td>
            </tr>

            <tr>
                <td><span><g:message code="app.label.all.signal.actions"/></span></td>
                <td>
                    <g:link controller="validatedSignal" action="exportSignalActionDetailReport" class="export-icon-link"
                            params="${[outputFormat: ReportFormat.PDF, signalId: signal.id]}">
                        <asset:image src="pdf-icon.jpg" class="pdf-icon m-r-15" height="16" width="16"/>
                    </g:link>

                    <g:link controller="validatedSignal" action="exportSignalActionDetailReport" class="export-icon-link"

                            params="${[outputFormat: ReportFormat.XLSX, signalId: signal.id]}">
                        <asset:image src="excel.gif" class="excel-icon m-r-15" height="16" width="16"/>
                    </g:link>

                    <g:link controller="validatedSignal" action="exportSignalActionDetailReport" class="export-icon-link"
                            params="${[outputFormat: ReportFormat.DOCX, signalId: signal.id]}">
                        <asset:image src="word-icon.png" class="word-icon m-r-15" height="16" width="16"/>
                    </g:link>

                    <g:if test="${grailsApplication.config.dms.enabled}">
                        <a href="#" class="sendToDms ${buttonClass}" role="menuitem"
                           data-doc-type="${Constants.DMSDocTypes.SIGNAL_ACTION_DETAIL_REPORT}"
                           data-target="#sendToDmsModal"><i data-toggle="tooltip" class="fa fa-upload font-16" title="Send to DMS"> </i></a>
                    </g:if>
                </td>
            </tr>

            </tbody>
        </table>
    </div>

</rx:container>

<rx:container title="${message(code: 'app.label.document.management')}">
    <g:render template="/includes/widgets/documentManagement" />
    <g:render template="/includes/modals/documentModal" model="[productNames : productNames, documentTypes: documentTypes]" />
</rx:container>

<g:render template="/includes/modals/signalSummaryReportPreferenceModal"/>

<g:form controller="DMSIntegration" action="sendToDms" method="post">
    <g:hiddenField name="signalId" value="${signal?.id}"/>
    <g:hiddenField name="eventSelection" value="" id="eventSelectionDMS"/>
    <g:hiddenField name="productSelection" value="" id="productSelectionDMS"/>
    <g:hiddenField name="dataSource" value="" id="dataSourceDMS"/>
    <g:hiddenField name="dateRange" value="" id="dateRangeDMS"/>
    <g:hiddenField name="reportType" value="${Constants.SignalReportTypes.PEBER}" id="reportTypeDMS"/>
    <g:hiddenField name="docTypeValue" value=""/>
    <g:render template="/dms/sendToDmsModal"/>
</g:form>

<div id="loading">
    <asset:image id="loading-image" src="rx-loader.gif" alt="Loading..." height="32px" width="32px" />
</div>

