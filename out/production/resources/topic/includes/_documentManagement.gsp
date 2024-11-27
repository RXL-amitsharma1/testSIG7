<%@ page import="com.rxlogix.enums.ReportFormat; com.rxlogix.Constants" %>
<script>
    var signalId = "${topic.id}"
    $(document).ready(function() {
        $("#generate-report").click(function() {
            $("#generating-report").removeClass("hide");
        });
    })
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

<asset:javascript src="app/pvs/documentManagement/document.js"/>

<rx:container title="Signal Reports">
    <div>&nbsp;</div>
    <div id="generated-reports" class="row">
        <table class="table">
            <thead>
                <th>Report Name</th>
                <th>Download</th>
            </thead>
            <tbody>
            <tr>
                <td><span>PBRER Signal Summary Report</span></td>
                <td>
                    <g:link controller="topic" action="generateSignalReports" style="margin-right: 20px" class="export-icon-link"
                            params="${[outputFormat: ReportFormat.PDF, topicId : topic.id, reportType: Constants.SignalReportTypes.PEBER]}">
                        <asset:image src="pdf-icon.jpg" class="pdf-icon" height="16" width="16"/>
                    </g:link>

                    <g:link controller="topic" action="generateSignalReports" style="margin-right: 20px" class="export-icon-link"
                            params="${[outputFormat: ReportFormat.XLSX, topicId : topic.id, reportType: Constants.SignalReportTypes.PEBER]}">
                        <asset:image src="excel.gif" class="excel-icon" height="16" width="16"/>
                    </g:link>

                    <g:link controller="topic" action="generateSignalReports" style="margin-right: 20px" class="export-icon-link"
                            params="${[outputFormat: ReportFormat.DOCX, topicId : topic.id, reportType: Constants.SignalReportTypes.PEBER]}">
                        <asset:image src="word-icon.png" class="word-icon" height="16" width="16"/>
                    </g:link>
                </td>
            </tr>

            <tr>
                <td><span>Signal Assessment Report </span></td>
                <td>
                    <g:link controller="topic" action="generateSignalAssessmentReport" style="margin-right: 20px" class="export-icon-link"
                            params="${[outputFormat: ReportFormat.PDF, topicId : topic.id, reportType: Constants.SignalReportTypes.PEBER]}">
                        <asset:image src="pdf-icon.jpg" class="pdf-icon" height="16" width="16"/>
                    </g:link>

                    <g:link controller="topic" action="generateSignalAssessmentReport" style="margin-right: 20px" class="export-icon-link"
                            params="${[outputFormat: ReportFormat.XLSX, topicId : topic.id, reportType: Constants.SignalReportTypes.PEBER]}">
                        <asset:image src="excel.gif" class="excel-icon" height="16" width="16"/>
                    </g:link>

                    <g:link controller="topic" action="generateSignalAssessmentReport" style="margin-right: 20px" class="export-icon-link"
                            params="${[outputFormat: ReportFormat.DOCX, topicId : topic.id, reportType: Constants.SignalReportTypes.PEBER]}">
                        <asset:image src="word-icon.png" class="word-icon" height="16" width="16"/>
                    </g:link>
                </td>
            </tr>

            <tr>
                <td><span>Signal Summary Report </span></td>
                <td>
                    <g:link controller="topic" action="generateSignalSummaryReport" style="margin-right: 20px" class="export-icon-link"
                            params="${[outputFormat: ReportFormat.PDF, topicId : topic.id, reportType: Constants.SignalReportTypes.PEBER]}">
                        <asset:image src="pdf-icon.jpg" class="pdf-icon" height="16" width="16"/>
                    </g:link>

                    <g:link controller="topic" action="generateSignalSummaryReport" style="margin-right: 20px" class="export-icon-link"
                            params="${[outputFormat: ReportFormat.XLSX, topicId : topic.id, reportType: Constants.SignalReportTypes.PEBER]}">
                        <asset:image src="excel.gif" class="excel-icon" height="16" width="16"/>
                    </g:link>

                    <g:link controller="topic" action="generateSignalSummaryReport" style="margin-right: 20px" class="export-icon-link"
                            params="${[outputFormat: ReportFormat.DOCX, topicId : topic.id, reportType: Constants.SignalReportTypes.PEBER]}">
                        <asset:image src="word-icon.png" class="word-icon" height="16" width="16"/>
                    </g:link>
                </td>
            </tr>

            </tbody>
        </table>
    </div>

</rx:container>



<rx:container title="${message(code: 'app.label.document.management')}">
    <g:render template="/includes/widgets/documentManagement" model="[alertInst: alertInst]" ></g:render>
    <g:render template="/includes/modals/documentModal" model="[singleDocumentObj: alertInst,
                                                                productNames:productNames, documentTypes:documentTypes,]" ></g:render>
</rx:container>

<div id="loading">
    <asset:image id="loading-image" src="rx-loader.gif" alt="Loading..." height="32px" width="32px" />
</div>
