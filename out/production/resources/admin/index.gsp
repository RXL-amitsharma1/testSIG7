<%--
  Created by IntelliJ IDEA.
  User: isha-sharma
  Date: 06/05/21
  Time: 1:27 PM
--%>

<html>
<head>
    <meta name="layout" content="main"/>
    <title>Control Panel</title>
</head>

<body>

<rx:container title="${message(code: "controlPanel.label")}">

    <g:render template="/includes/layout/flashErrorsDivs"/>

    <h3 class="page-header">Add New Users</h3>
    <g:form controller="Admin" action="addUsers" enctype="multipart/form-data">
        <div>
            <label for="excelFile">Upload File</label>
            <br>
            <input id="excelFile" type="file" name="excelFile" >
        </div>

        <div>
            <br>
            <g:submitButton class='btn btn-primary' name="uploadbutton" value="Upload"/>
            <g:link controller="Admin" action="downloadUserTemplate">Click here to download sample users sheet</g:link>
        </div><br>
        <g:link controller="admin" action="downloadExistingUsers">Click here to download list of existing users</g:link>
        <br>
    </g:form>
    <hr>

%{--    <h3 class="page-header">Add New Dispositions</h3>--}%
%{--    <g:form controller="admin" action="addDispositions" enctype="multipart/form-data">--}%
%{--        <div>--}%
%{--            <label for="excel_File">Upload File</label>--}%
%{--            <br>--}%
%{--            <input id="excel_File" type="file" name="excel_File" required>--}%
%{--        </div>--}%

%{--        <div>--}%
%{--            <br>--}%
%{--            <g:submitButton class='btn btn-primary' name="uploadbutton" value="Upload"/>--}%
%{--            <g:link controller="admin"--}%
%{--                    action="downloadDispositionTemplate">Click here to download sample disposition sheet</g:link>--}%
%{--        </div><br>--}%
%{--        <h4>Download Existing Dispositons</h4>--}%
%{--        <g:link controller="admin"--}%
%{--                action="downloadExistingDispositions">Click here to download Existing Dispositions</g:link>--}%
%{--    </g:form>--}%
%{--    <hr>--}%

%{--    <h3 class="page-header">Add New Disposition Workflow Rules</h3>--}%
%{--    <g:form controller="admin" action="addDispositionRules" enctype="multipart/form-data">--}%
%{--        <div>--}%
%{--            <label for="excel">Upload File</label>--}%
%{--            <br>--}%
%{--            <input id="excel" type="file" name="excel" required>--}%
%{--        </div>--}%

%{--        <div>--}%
%{--            <br>--}%
%{--            <g:submitButton class='btn btn-primary' name="uploadbutton" value="Upload"/>--}%
%{--            <g:link controller="admin"--}%
%{--                    action="downloadDispositionWorkflowRulesTemplate">Click here to download sample disposition workflow rules sheet</g:link>--}%
%{--        </div><br>--}%
%{--        <h4>Download Existing Dispositon Rules</h4>--}%
%{--        <g:link controller="admin"--}%
%{--                action="downloadExistingWorkFlowRules">Click here to download Existing Disposition Workflow Rules</g:link>--}%
%{--    </g:form>--}%


</rx:container>
</body>
</html>