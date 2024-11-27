<%--
  Created by IntelliJ IDEA.
  User: nikhil
  Date: 26/02/23
  Time: 7:08 PM
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.rxlogix.Constants;grails.util.Holders"%>
<head>
    <meta name="layout" content="main"/>
    <asset:javascript src="app/pvs/configManagement.js"/>
    <asset:stylesheet src="apiControlPanel.css"/>
    <title><g:message code="app.label.config.management"/></title>
</head>
<body>
<div id="accordion-pvs">

<g:render template="/includes/layout/flashErrorsDivs" bean="${configurationInstance}" var="theInstance"/>

<div class="panel panel-default rxmain-container rxmain-container-top m-b-0">
    <div class="rxmain-container-row rxmain-container-header panel-heading pv-sec-heading">
        <div class="row" >
            <div class="col-md-4">
                <label class="rxmain-container-header-label m-t-5">${message(code: "app.label.config.management")}</label>
            </div>
        </div>
    </div>
    %{--    <hr style="margin: 10px 20px 2px 0px;"/>--}%
    <BR>
    %{--    <div class= "margin20Top">--}%
    %{--        <div class="row" >--}%
    %{--            <div class="col-md-3">--}%
    %{--                <label>Refresh Technical Configurations: </label>--}%
    %{--            </div>--}%
    %{--            <div class="col-md-2">--}%
    %{--                <span id="refreshTechnicalConf" title="Refresh" class="glyphicon reloaderBtn glyphicon-refresh"></span>--}%
    %{--            </div>--}%
    %{--        </div>--}%
    %{--        <BR>--}%

    %{--        <div class="row" >--}%
    %{--            <div class="col-md-3">--}%
    %{--                <label>Last Technical Configuration Sync :</label>--}%
    %{--            </div>--}%
    %{--            <div id="lastEtlDate" class="col-md-2"><p id="lastRefreshTime">${Holders.config.last.configuration.refreshed}</p></div>--}%
    %{--        </div>--}%

    %{--        <div>&nbsp;<br/></div>--}%
    %{--    </div>--}%
    %{--    status--}%
    %{--    <HR>--}%
    <div id="signalDiv" class="panel-collapse rxmain-container-content rxmain-container-show collapse in" aria-expanded="true" style="padding-top: 10px;">
        <!-------------------------------------------------------IMPORT--------------------------------------------------------->
        <div class="panel panel-default">
            <div class="panel-heading">
                <h4 class="panel-title">
                    <a class="accordion-toggle collapsed" data-toggle="collapse" data-parent="#accordion"
                       href="#collapse13">Import</a>
                </h4>
            </div>

            <div id="collapse13" class="panel-collapse collapse open p-10">
                <div class="margin20Top">
                    <form id="importConfigForm" enctype="multipart/form-data" method="post">
                        <div class="container"
                             style="border: groove;margin-top: 15px;border-radius: 10px;padding-top: 5px;padding-bottom: 5px">
                            <input type="hidden" value="PVS" name="appName" id="appName">

                            <div class="row" style="margin-top: 5px">
%{--                                <div class="col-sm-4" style="margin-top: 5px">--}%
%{--                                    <input type="radio" id="techConfig" name="configType" value="Technical Configurations"--}%
%{--                                           checked> Technical Configurations--}%
%{--                                </div>--}%

                                <div class="col-sm-4" style="margin-top: 5px" hidden>
                                    <input type="radio" id="busConfig" name="configType"
                                           value="Business Configurations" checked> Business Configurations
                                </div>

                            </div>

                            <div class="row" style="margin-top: 5px">
                                <div class="col-sm-6" style="margin-top: 5px"><label>File Format</label>
                                    <select id="format" name="format" disabled
                                            style="width: 100%;cursor: not-allowed;color: grey;">
                                        <option value="Excel" selected>Excel</option>
                                    </select>
                                </div>

                                <div class="col-sm-6" style="margin-top: 5px;"><label>File</label>

                                    <div class="input-group">
                                        <input type="text" class="form-control" id="config_file_name" readonly="true">
                                        <label class="input-group-btn "><span class="btn btn-primary"><span
                                                class="glyphicon glyphicon-upload"></span>                                                        <input
                                                type="file" id="config_file_input" name="configFile"
                                                accept="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet, application/vnd.ms-excel"
                                                style="display: none;"></span>
                                        </label>
                                    </div>
                                </div>
                            </div>                                    <BR>


                            <div class="row">
                                <div class="col-sm-1">
                                    <button type="button" id="ImportConfiguration" class="btn btn-primary">
                                        <span class="glyphicon glyphicon-import"></span>
                                        Import</button>
                                </div>
                            </div></div></form></div></div>
        </div>
        <!--------------------------------//-----------------------EXPORT--------------------------------------------------------->
        <div class="panel panel-default">
            <div class="panel-heading">
                <h4 class="panel-title">
                    <a class="accordion-toggle collapsed" data-toggle="collapse" data-parent="#accordion"
                       href="#collapse15">Export</a>
                </h4>
            </div>

            <div id="collapse15" class="panel-collapse collapse open p-10">
                <div class="margin20Top">
                    <div class="margin20Top"><div class="container"
                                                  style="border: groove;margin-top: 15px;border-radius: 10px;padding-top: 5px;padding-bottom: 5px"><label>Export Configuration</label><BR><BR>

                        <div class="row">
                            <div class="col-xs-6"><label>File Format</label>
                                <select id="exportFormat" name="exportFormat" disabled
                                        style="width: 100%;cursor: not-allowed;color: grey;">
                                    <option selected>Excel</option></select>
                            </div>

                            <div class="col-xs-6"><label>Modules</label>
                                <select name="configsSelect" id="configsSelect" disabled
                                        style="cursor: not-allowed;color: grey;width: 100%">
                                    <option value="Technical Configurations">All Configurations</option>
                                </select>
                            </div>
                        </div>
                        <BR>

                        <div class="row">
                            <button type="button" id="exportTechConfig" class="btn btn-primary">
                                %{--                                    onclick="window.location.href = '/signal/adHocAlertRest/index'">--}%
                                <span class="glyphicon glyphicon-export"></span>
                                Export</button>
                        </div>
                    </div>
                    </div>
                </div>
            </div>
        </div>
        <!-------------------------------------------------------COMPARE-------------------------------------------------------->
%{--        <div--}%
%{--                class="panel panel-default"><div class="panel-heading"><h4 class="panel-title"><a--}%
%{--                class="accordion-toggle collapsed" data-toggle="collapse" data-parent="#accordion"--}%
%{--                href="#collapse14">Compare Configuration</a></h4></div>--}%

%{--            <div id="collapse14" class="panel-collapse collapse open p-10"><div class="margin20Top"><div--}%
%{--                    class="margin20Top">--}%
%{--                <form id="compareConfigForm"  method="POST" enctype="multipart/form-data" >--}%
%{--                    --}%%{--                <form action="http://localhost:8085/pvadmin/config/compare"--}%
%{--                    --}%%{--                                              enctype="multipart/form-data" method="post">--}%
%{--                    <div class="container" style="border: groove;margin-top: 15px;border-radius: 10px;padding-top: 5px;padding-bottom: 5px">--}%%{--                                        <label>Compare Technical configuration</label><BR>--}%%{--                                        <div--}%
%{--                            class="row"--}%
%{--                            style="margin-top: 5px">--}%%{--                                            <div class="col-sm-6" style="margin-top: 5px">--}%%{----}%%{--                                                <label>File 1(.xlsx)</label>--}%%{----}%%{--                                                <input type="file" id="configFile1" name="configFileFirst" style="width: 100%"  accept="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet">--}%%{----}%%{--                                            </div>--}%%{--                                        <div--}%
%{--                                class="col-sm-6" style="margin-top: 5px;"><label>Configuration Snapshot 1</label> <p class="fileTypeError hidden" style="color: red;font-size: x-small"> * Only Excel (.xlsx) Files are supported</p>--}%

%{--                            <div class="input-group"><input type="text" class="form-control" id="config1_file_name"--}%
%{--                                                            readonly="">                                                <label--}%
%{--                                    class="input-group-btn "><span class="btn btn-primary"><span--}%
%{--                                        class="glyphicon glyphicon-upload"></span>                                                        <input--}%
%{--                                        type="file" id="config_first_file" name="configFileFirst"--}%
%{--                                        accept="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet, application/vnd.ms-excel"--}%
%{--                                        style="display: none;"></span></label></div>--}%
%{--                        </div>--}%%{--                                            <div class="col-sm-6" style="margin-top: 5px">--}%%{----}%%{--                                                <label>File 2(.xlsx)</label>--}%%{----}%%{--                                                <input type="file" id="configFile2" name="configFileSecond" style="width: 100%"  accept="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet">--}%%{----}%%{--                                            </div>--}%%{--                                        <div--}%
%{--                                class="col-sm-6" style="margin-top: 5px;"><label>Configuration Snapshot 2</label> <p class="fileTypeError hidden" style="color: red;font-size: x-small"> * Only Excel (.xlsx) Files are supported</p>--}%

%{--                            <div class="input-group"><input type="text" class="form-control" id="config2_file_name"--}%
%{--                                                            readonly="">                                                <label--}%
%{--                                    class="input-group-btn "><span class="btn btn-primary"><span--}%
%{--                                        class="glyphicon glyphicon-upload"></span>                                                        <input--}%
%{--                                        type="file" id="config_second_file" name="configFileSecond"--}%
%{--                                        accept="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet, application/vnd.ms-excel"--}%
%{--                                        style="display: none;"></span></label></div></div></div>--}%

%{--                        <div class="row" style="margin-top: 5px"><button TYPE="button" id="compareConfigurations"--}%
%{--                                                                         class="btn btn-primary"--}%
%{--                                                                         style="margin-top: 5px"><span--}%
%{--                                    class="glyphicon"></span>                                                --}%%{--                                    <g:message code="controlPanel.exportToExcel.button"/>--}%%{--                                                Compare--}%
%{--                        </button></div></div>--}%
%{--                </form></div></div></div>--}%
%{--        </div>--}%

        <script>
            $('#config_file_input').change(function (evt, numFiles, label) {
                $("#config_file_name").val($('#config_file_input').get(0).files[0].name);
            });
            $('#config_first_file').change(function (evt, numFiles, label) {
                if ($('#config_first_file').get(0).files[0].name.includes(".xlsx")) {
                    $('.fileTypeError').addClass("hidden");
                    $("#config1_file_name").val($('#config_first_file').get(0).files[0].name);
                } else {
                    $("#config_first_file").val(null);
                    $("#config1_file_name").val('');
                    $('.fileTypeError').removeClass("hidden");
                }
            });
            $('#config_second_file').change(function (evt, numFiles, label) {
                if ($('#config_second_file').get(0).files[0].name.includes(".xlsx")) {
                    $('.fileTypeError').addClass("hidden");
                    $("#config2_file_name").val($('#config_second_file').get(0).files[0].name);
                } else {
                    $("#config_second_file").val(null);
                    $("#config2_file_name").val('');
                    $('.fileTypeError').removeClass("hidden");
                }
                $("#config2_file_name").val($('#config_second_file').get(0).files[0].name);
            });
        </script>

    </div>
</div>
</div>
</div>
</body>