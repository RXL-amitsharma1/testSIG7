<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Import Test Excel File</title>
    <meta name="layout" content="main">
    <link type="text/css" href="//gyrocode.github.io/jquery-datatables-checkboxes/1.2.12/css/dataTables.checkboxes.css" rel="stylesheet" />
    <script type="text/javascript" src="//gyrocode.github.io/jquery-datatables-checkboxes/1.2.12/js/dataTables.checkboxes.min.js"></script>
    <asset:javascript src="testSignal/index.js"/>
    <asset:javascript src="app/pvs/bootbox.min.js"/>
    <g:javascript>
    var testCaseUrl ="${createLink(controller: "testSignalRest", action: 'testCasesData')}";
    var selectedRowData=[];
    var uploadFileUrl = "${createLink(controller: "testSignalRest", action: 'uploadFile')}";
    var fileExists = ${fileExists};
    </g:javascript>

</head>

<body>

<div class="panel panel-default rxmain-container rxmain-container-top" id ="advacedOptionSection">
    <div class="rxmain-container-row rxmain-container-header panel-heading">
        <h4 class="rxmain-container-header-label">
            Import Test Case File
        </h4>
    </div>
    <div id="pvsAdvancedOptions" class="panel-collapse rxmain-container-content rxmain-container-show collapse in" aria-expanded="true">
        <div class="row">
            <div class="col-xs-5">

                <form action="uploadFile" enctype="multipart/form-data" id="importTestFileUploadForm" method="post">
                    <div class="form-group">
                        <div class="file-uploader" data-provides="fileupload">
                            <input type="file" name="file"
                                   data-mandatory="false" data-mandatoryset="0" class="file" accept=".xlsx,.xls" >

                            <div class="input-group">
                                <input type="text" class="form-control " placeholder="Attach a file"
                                       id="configuration-file-name" name="configuration-file-name" value=""
                                       title="">

                                <span class="input-group-btn ">
                                    <button class="browse btn btn-primary btn-file-pa-upload " type="button"><i
                                            class="glyphicon glyphicon-search" id="upload-attachment"></i>
                                    </button>
                                </span>

                            </div>
                        </div>
                    </div>

                    <div class="modal-footer">
                        <button type="button" class="btn btn-default cancelUploadModalButton" data-dismiss="modal">
                            Cancel
                        </button>
                        <input type="submit" class="btn primaryButton btn-primary upload" value="Upload"
                               id="submit-button">
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<div class="panel panel-default rxmain-container rxmain-container-top" id ="testCases">
    <div class="rxmain-container-row rxmain-container-header panel-heading">
        <h4 class="rxmain-container-header-label">
            Test Cases
        </h4>
    </div>
    <div id="testCasesTable" class="panel-collapse rxmain-container-content rxmain-container-show collapse in" aria-expanded="true">
        <g:form action="executeTestAlerts" method="post">
            <table id="excelDataTable" class="row-border hover" width="100%">
                <thead>
                <tr>
                    <th><input name="select_all" value="1" id="excelDataTable-select-all" type="checkbox" /></th>
                    <th>Alert Type</th>
                    <th>Owner</th>
                    <th>Data Source</th>
                    <th>Products</th>
                    <th>Is Adhoc</th>
                    <th>Exclude Follow-Up</th>
                    <th>Data Mining based on SMQ/Event group</th>
                    <th>Exclude Non-valid Cases</th>
                    <th>Include Cases Missed in the Previous Reporting Period</th>
                    <th>Apply Alert Stop List</th>
                    <th>Include Medically Confirmed Cases Only</th>
                    <th>Date Range Type</th>
                    <th>Date Range</th>
                    <th>X For Date Range</th>
                    <th>Start Date</th>
                    <th>End Date</th>
                    <th>Evaluate Case Date On</th>
                    <th>Version As Of Date</th>
                    <th>Drug Type</th>
                    <th>Priority</th>
                    <th>Assigned To</th>
                    <th>Share With</th>
                    <th>Limit to Case Series</th>
                </tr>
                </thead>
            </table>
            <input type="submit" class="btn primaryButton btn-primary" value="Run Selected Cases"
                   id="send-selected-cases">
        </g:form>
    </div>
</div>




</body>
</html>