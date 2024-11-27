<%--
  Created by IntelliJ IDEA.
  User: rxlogix
  Date: 14/09/22
  Time: 3:50 PM
--%>
<!DOCTYPE html>
<html>
<%@ page import="grails.util.Holders; com.rxlogix.util.ViewHelper; com.rxlogix.util.DateUtil; com.rxlogix.config.Tag; com.rxlogix.pvdictionary.config.PVDictionaryConfig" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.title.alert.administration"/></title>
    <style>

    .dataTableHideCellContent {
        display: none !important;
    }
    </style>
    <g:javascript>
        $("#productSelection").val(null);
        var editMessage = "${message(code: "app.onlyAdminCreateNewTags.message")}";

        var isAutoAssignedTo = null;
        var isAutoSharedWith = null;
        var isMultipleDatasource = null;
        var sharedWithUrl = "${createLink(controller: 'user', action: 'searchShareWithUserGroupList')}";
        var fetchAssignmentForProductsUrl = "${createLink(controller: 'productAssignment', action: 'fetchAssignmentForProducts')}";

        var getSelectedGenericUrl = "${createLink(controller: 'configurationRest', action: 'getSelectedGenericNames')}";
        var searchGenericsUrl = "${createLink(controller: 'configurationRest', action: 'searchGenerics')}";

        var templateSearchUrl = "${createLink(controller: 'reportTemplateRest', action: 'getTemplateList')}";

        var stringOperatorsUrl =  "${createLink(controller: 'query', action: 'getStringOperators')}";
        var numOperatorsUrl =  "${createLink(controller: 'query', action: 'getNumOperators')}";
        var booleanOperatorsUrl =  "${createLink(controller: 'query', action: 'getBooleanOperators')}";
        var dateOperatorsUrl =  "${createLink(controller: 'query', action: 'getDateOperators')}";
        var valuelessOperatorsUrl = "${createLink(controller: 'query', action: 'getValuelessOperators')}";
        var keywordsUrl =  "${createLink(controller: 'query', action:'getAllKeywords')}";
        var fieldsValueUrl = "${createLink(controller: 'query', action: 'getFieldsValue')}";
        var allFieldsUrl = "${createLink(controller: 'query', action: 'getAllFields')}";
        var possibleValuesUrl = "${createLink(controller: 'query', action: 'possibleValues')}";

        var blankValuesForQueryUrl = "${createLink(controller: 'query', action: 'queryExpressionValuesForQuery')}";
        var customSQLValuesForQueryUrl = "${createLink(controller: 'query', action: 'customSQLValuesForQuery')}";
        var customSQLValuesForTemplateUrl = "${createLink(controller: 'template', action: 'customSQLValuesForTemplate')}";

        var queryViewUrl = "${createLink(controller: 'query', action: 'view')}";
        var blankValuesForQuerySetUrl = "${createLink(controller: 'query', action: 'queryExpressionValuesForQuerySet')}";
        var substanceFrequencyPropertiesUrl = "${createLink(controller: 'aggregateCaseAlert',action: 'getSubstanceFrequency')}";
        var queryList = "${createLink(controller: 'query',action: 'queryList')}";
        var dataSheetList = "${createLink(controller: 'dataSheet',action: 'dataSheets')}";
        var fetchFreqNameUrl = "${createLink(controller:"aggregateCaseAlert", action: "fetchFreqName")}";
        var fetchAllowedUsersUrl = "${createLink(controller: 'configurationRest', action: 'fetchAllowedUsers')}";
        var fetchDrugClassificationUrl = "${createLink(controller: 'configurationRest', action: 'fetchDrugClassification')}";
        var selectAutoUrl = "${createLink(controller: 'query', action: 'ajaxReportFieldSearch')}";
        var templateList = "${createLink(controller: 'template',action: 'templateList')}";
        var cioms1Id = "${cioms1Id}";
        var templateViewUrl = "${createLink(controller: 'template', action: 'view')}";
        var queryViewUrl = "${createLink(controller: 'query', action: 'view')}";
        var templateIdNameListUrl = "${createLink(controller: 'template', action: 'templateIdNameList')}";
        var queryIdNameListUrl = "${createLink(controller: 'query', action: 'queryIdNameList')}";
        var faersDisabledColumnsIndexesUrl = "${createLink(controller: 'aggregateCaseAlert', action: 'fetchFaersDisabledColumnsIndexes')}";
        var reportFieldsForQueryUrl = "${createLink(controller: 'query', action: 'reportFieldsForQueryValue')}";
        var selectNonCacheUrl = "${createLink(controller: 'query', action: 'possiblePaginatedValues')}";
        var validateValue = "${createLink(controller: 'query', action: 'validateValue')}";
        var importExcel = "${createLink(controller: 'query', action: 'importExcel')}";
        var getDmvData = "${createLink(controller: 'query', action: 'getDmvData')}";
        var productBasedSecurity = ${Holders.config.pvsignal.product.based.security};
        var substanceFrequencyPropertiesUrl = "${createLink(controller: 'evdasAlert', action: 'fetchSubstanceFrequencyProperties')}";

        var LABELS = {
            labelShowAdvancedOptions : "${message(code:'add.header.title.and.footer')}",
            labelHideAdvancedOptions : "${message(code:'hide.header.title.and.footer')}"
        };

        var appLabelProductGroup = "Quantitative Alert";
        var userIdList = null;
        var configurationMiningVariable =null;
        var editAlert = null;
        var hasNormalAlertExecutionAccess =null;
        var enabledDataSourceList= null;
        var dataSheets = null;
        var alertIdSet = new Set();
    </g:javascript>
    <asset:javascript src="app/pvs/alertAdministration/alertAdministration.js"/>
    <asset:javascript src="app/pvs/dataTablesActionButtons.js"/>
    <asset:javascript src="yadcf/jquery.dataTables.yadcf.js"/>
    <asset:stylesheet src="jquery-ui/jquery-ui.css"/>
    <asset:stylesheet src="yadcf/jquery.dataTables.yadcf.css"/>
    <asset:stylesheet src="yadcf/jquery.dataTables.yadcf.css"/>
    <asset:javascript src="app/pvs/bootbox.min.js"/>
    <asset:javascript src="app/pvs/alert_utils/common_alert_utils.js"/>
    <asset:javascript src="app/pvs/alert_utils/alert_product_assignment.js"/>
    <asset:javascript src="app/pvs/common/rx_common.js"/>
    <asset:javascript src="app/pvs/configuration/templateQueries.js"/>
    <asset:javascript src="app/pvs/scheduler.js"/>
    <asset:javascript src="app/pvs/configuration/configurationCommon.js" asset-defer="defer"/>
    <asset:javascript src="app/pvs/configuration/dateRange.js"/>
    <asset:javascript src="app/pvs/configuration/dateRangeEvdas.js"/>
    <asset:javascript src="app/pvs/configuration/blankParameters.js"/>
    <asset:javascript src="app/pvs/alert_utils/alert_query_utils.js"/>
    <asset:javascript src="app/pvs/disableAutocomplete.js"/>
    <asset:javascript src="app/pvs/bootbox.min.js"/>
    <asset:javascript src="app/pvs/configuration/copyPasteValues.js"/>
    <asset:javascript src="app/pvs/configuration/dictionaryMultiSearch.js"/>
    <asset:stylesheet src="toggle-button.css"/>
    <g:javascript>
        var CONFIGURATION = {
                listUrl: "${createLink(controller: 'alertAdministration', action: 'list')}",
                deleteUrl: "${createLink(controller: 'configuration', action: 'delete')}",
                editUrl: "${createLink(controller: 'configuration', action: 'edit')}",
                viewUrl: "${createLink(controller: 'configuration', action: 'view')}",
                copyUrl: "${createLink(controller: 'configuration', action: 'copy')}",
                runUrl:"${createLink(controller: 'configuration', action: 'runOnce')}",
                sca_list_url: "${createLink(controller: 'singleCaseAlert', action: 'index')}",
                sca_edit_url: "${createLink(controller: 'singleCaseAlert', action: 'edit')}",
                sca_view_url: "${createLink(controller: 'singleCaseAlert', action: 'view')}",
                sca_copy_url: "${createLink(controller: 'singleCaseAlert', action: 'copy')}",
                aga_list_url: "${createLink(controller: 'aggregateCaseAlert', action: 'list')}",
                aga_edit_url: "${createLink(controller: 'aggregateCaseAlert', action: 'edit')}",
                aga_view_url: "${createLink(controller: 'aggregateCaseAlert', action: 'view')}",
                aga_copy_url: "${createLink(controller: 'aggregateCaseAlert', action: 'copy')}",
                adha_list_url: "${createLink(controller: 'adHocAlert', action: 'list')}",
                adha_edit_url: "${createLink(controller: 'adHocAlert', action: 'edit')}",
                adha_view_url: "${createLink(controller: 'adHocAlert', action: 'view')}",
                adha_copy_url: "${createLink(controller: 'adHocAlert', action: 'copy')}",
                evdas_run_url: "${createLink(controller: 'evdasAlert', action: 'runOnce')}",
                evdas_edit_url: "${createLink(controller: 'evdasAlert', action: 'edit')}",
                evdas_view_url: "${createLink(controller: 'evdasAlert', action: 'view')}",
                evdas_copy_url: "${createLink(controller: 'evdasAlert', action: 'copy')}",
                evdas_delete_url: "${createLink(controller: 'evdasAlert', action: 'delete')}",
                literature_edit_url: "${createLink(controller: 'literatureAlert', action: 'edit')}",
                literature_view_url: "${createLink(controller: 'literatureAlert', action: 'view')}",
                literature_copy_url: "${createLink(controller: 'literatureAlert', action: 'copy')}",
                literature_delete_url: "${createLink(controller: 'literatureAlert', action: 'delete')}",
                literature_run_url: "${createLink(controller: 'literatureAlert', action: 'runOnce')}",
                configuration_enable_url: "${createLink(controller: 'alertAdministration', action: 'enableConfigurations')}",
                configuration_disable_url: "${createLink(controller: 'alertAdministration', action: 'disableConfigurations')}",
                delete_alert_url: "${createLink(controller: 'alertAdministration', action: 'deleteAlerts')}"
        }
        var ingredientLevel = ${PVDictionaryConfig.ingredientColumnIndex};
    </g:javascript>

</head>

<body>
<rx:container title="${message(code: message(code: "app.ReportLibrary.label"))}" options="${true}" alertPreCheck="${true}" filters="${true}" >
    <g:render template="/includes/layout/flashErrorsDivs"/>
    <div class="messageContainer"></div>
    <div class="m-b-5" style="margin-bottom: 25px !important;">
        <div style="float: left; font-size: 14px">
            <label class="no-bold add-cursor radio-container">
                <input class="m-r-5 viewAlertRadio"
                       type="radio" name="alertRunType"
                       value="Scheduled" checked/>
                <g:message code="app.label.scheduled"/>
            </label>
            <label class="no-bold add-cursor radio-container">
                <input class="m-r-5 viewAlertRadio"
                       type="radio" name="alertRunType"
                       value="Unscheduled"/>
                <g:message code="app.label.unscheduled"/>
            </label>
        </div>

        <div  style="margin-right: 10px;float: right">
            <i class='glyphicon glyphicon-info-sign themecolor add-cursor' id="etlStatusInfo"
               style="top: 3px; float: right; cursor: pointer"></i>
            <label class="bold" style="margin: 0; float: right;font-size: 15px">
                ETL Status: <label id="etlStatusDiv"
                    style="font-weight: normal !important; margin: 0;margin-right: 5px;">${etlStatus}</label>
            </label>
        </div>

        <div style="left: 40px;float: right; margin-right: 30px;">
            <i class='glyphicon glyphicon-info-sign themecolor add-cursor' id="alertPreCheckInfo"
               style="top: 3px; float: right; cursor: pointer"></i>
            <label class="bold" style="margin: 0; float: right;font-size: 15px">
                Alert Pre-Checks: <label id ="preChecksStatusDiv"
                    style="font-weight: normal !important; margin: 0;margin-right: 5px;">${isPreChecksEnabled ? "Enabled" : "Disabled"}</label>
            </label>
        </div>
    </div>

    <table id="rxTableConfiguration" class="row-border hover" width="100%">
        <thead>
        <tr>
            <th data-idx="0" data-field="checkbox">
                <input class="select-all-alert-config" type="checkbox"/>
            </th>
            <th class="nameColumn">
                <div class="th-label">
                    <g:message code="app.label.name"/>
                </div>
            </th>
            <th>
                <div class="th-label">
                    <g:message code="app.label.product"/>
                </div>
            </th>
            <th>
                <div class="th-label">
                    <g:message code="app.label.dataSource"/>
                </div>
            </th>
            <th class="reportDescriptionColumn">
                <div class="th-label">
                    <g:message code="app.label.description"/>
                </div>
            </th>
            <th>
                <div class="th-label">
                    <g:message code="app.label.runTimes"/>
                </div>
            </th>
            <th>
                <div class="th-label">
                    <g:message code="app.label.DateRange"/>
                </div>
            </th>
            <th>
                <div class="th-label">
                    <g:message code="app.label.dateCreated"/>
                </div>
            </th>
            <th>
                <div class="th-label">
                    <g:message code="app.label.dateModified"/>
                </div>
            </th>
            <th>
                <div class="th-label">
                    <g:message code="app.label.lastExecution"/>
                </div>
            </th>
            <th>
                <div class="th-label">
                    <g:message code="app.label.next.DateRange"/>
                </div>
            </th>
            <th>
                <div class="th-label">
                    <g:message code="next.run.date"/>
                </div>
            </th>
            <th>
                <div class="th-label">
                    <g:message code="app.label.owner"/>
                </div>
            </th>
            <th class="nameColumn">
                <div class="th-label">
                    <g:message code="app.label.alert.status"/>
                </div>
            </th>
            <th>
                <div class="th-label">
                    <g:message code="app.label.action"/>
                </div>
            </th>
        </tr>
        </thead>
    </table>
    <g:form controller="${controller}" method="delete">
        <g:render template="/includes/widgets/deleteRecord"/>
    </g:form>
</rx:container>
<div id="alertPreCheckInfoDiv" class="popupBox"
     style="position: absolute; display: none;width:400px;z-index: 999; height: 266px; border-color: rgb(200,200,200);border-width: 1px;border-radius: 5px;border-style: solid; background:white;box-shadow: 10px 10px 60px #555;">
    <div style="margin: 10px 10px 10px 10px">
        <label for="disableOnPvrIssue" class="bold">Disable Alerts on PVR Issues</label>
        <p id="disableOnPvrIssue" style="display: inline; float: right">Yes</p>
        <hr style="margin: 5px 0 5px 0; border-color:#d1d1d1">
        <label for="disableOnEtlIssue" class="bold">Disable Alerts on ETL Issues</label>
        <p id="disableOnEtlIssue" style="display: inline; float: right">Yes</p>
        <hr style="margin: 5px 0 5px 0; border-color:#d1d1d1">

        <div id="autoAdjustRuleInfoDiv" style="display: none">
        <label  class="bold">Alert Execution Configuration for Disabled Alert</label>

            <div id="scaAutoAdjustRuleInfo" class="scaAutoAdjustRuleInfo" style="display: none">
                <div style="background-color: #f5f5f5;border-radius: 3px;padding: 5px 5px 5px 10px;margin-bottom: 5px">
                    <label style="width: 100%; font-weight: normal !important;">Individual Case Alert (scheduled)</label>
                    <i class="ion-ios7-arrow-right"></i>
                    <label id="autoAdjustTypeIcr"
                           style="font-weight: normal !important; display: inline">Auto-Adjust Date and Execute alert for every skipped execution</label>
                </div>
            </div>

            <div id="aggAutoAdjustRuleInfo" class="aggAutoAdjustRuleInfo" style="display: none">
                <div style="background-color: #f5f5f5;border-radius: 3px;padding: 5px 5px 5px 10px;">
                    <label style="width: 100%; font-weight: normal !important;">Aggregate Alert (scheduled)</label>
                    <i class="ion-ios7-arrow-right"></i>
                    <label id="autoAdjustTypeAgg"
                           style="font-weight: normal !important; display: inline">Auto-Adjust Date and execute a single alert for all skipped execution</label>
                </div>
            </div>
        </div>
    </div>
</div>
<div id="etlInfoDiv" class="popupBox"
     style="position: absolute;z-index: 999; display: none;width:310px; height: 110px; border-color: rgb(200,200,200);border-width: 1px;border-radius: 5px;border-style: solid; background:white;box-shadow: 10px 10px 60px #555;">
    <div style="margin: 10px 10px 10px 10px">
        <div>
            <label for="etlRunStatus" class="bold">ETL Run Status:</label>
            <p id="etlRunStatus" style="display: inline; text-transform: capitalize;">Success</p>
        </div>

        <div>
            <label for="lastSuccessfulEtl" class="bold">Last Successful Run:</label>
            <p id="lastSuccessfulEtl" style="display: inline;">11-May-2022 09:01:12 AM</p>
        </div>

        <div>
            <label for="etlEnabled" class="bold">Enabled:</label>
            <p id="etlEnabled" style="display: inline;">Yes</p>
        </div>

        <div>
            <label for="etlScheduler" class="bold">Scheduler:</label>
            <p id="etlScheduler" style="display: inline;">Yes</p>
        </div>
    </div>
</div>
<div id="pvrCheckInfoDiv" class="infoBox"
     style="position: absolute;z-index: 9999; display: none;width:630px; height: 70px; border-color: rgb(200,200,200);border-width: 1px;border-radius: 5px;border-style: solid; background:white;box-shadow: 10px 10px 60px #555;">
    <div style="margin: 10px 10px 10px 10px">
        <div>
            <p style="display: inline;">If PV Reports is not accessible system then will not execute the alert and will move to the scheduled/disabled state, once the PV Reports is accessible then system will auto-execute the impacted alerts. The check is applicable for scheduled and non-scheduled alerts for all data sources.</p>
        </div>
    </div>
</div>
<div id="etlVersionAsOfCheckInfoDiv" class="infoBox"
     style="position: absolute;z-index: 9999; display: none;width:510px; height: 90px; border-color: rgb(200,200,200);border-width: 1px;border-radius: 5px;border-style: solid; background:white;box-shadow: 10px 10px 60px #555;">
    <div style="margin: 10px 10px 10px 10px">
        <div>
            <p style="display: inline;">Alerts will not execute if the data is not present till data lock point i.e Version As Of Date, the system will move the Alerts to scheduled/disabled state, and once latest data is available then all impacted Alerts will auto-execute. The check is applicable for scheduled and non-scheduled alerts for Safety data source.</p>
        </div>
    </div>
</div>
<div id="etlLatestFailureInfoDiv" class="infoBox"
     style="position: absolute;z-index: 9999; display: none;width:470px; height: 90px; border-color: rgb(200,200,200);border-width: 1px;border-radius: 5px;border-style: solid; background:white;box-shadow: 10px 10px 60px #555;">
    <div style="margin: 10px 10px 10px 10px">
        <div>
            <p style="display: inline;">Alerts will not execute if the last ETL is Failed, the system will move the Alerts to scheduled/disabled state and once the ETL is successfully completed then all impacted Alerts will auto-execute. The check is applicable for scheduled and non-scheduled alerts for Safety data source.</p>
        </div>
    </div>
</div>
<div id="etlLatestInProgressInfoDiv" class="infoBox"
     style="position: absolute;z-index: 9999; display: none;width:310px; height: 145px; left: 914.03px !important; border-color: rgb(200,200,200);border-width: 1px;border-radius: 5px;border-style: solid; background:white;box-shadow: 10px 10px 60px #555;">
    <div style="margin: 10px 10px 10px 10px">
        <div>
            <p style="display: inline;">Alerts will not execute if the ETL is In Progress state, the system will move the Alerts to scheduled/disabled state and once the ETL is successfully completed then all impacted Alerts will auto-execute. The check is applicable for scheduled and non-scheduled alerts for Safety data source.</p>
        </div>
    </div>
</div>
<div id="alertInProgressInfoDiv" class="infoBox"
     style="position: absolute;z-index: 9999; display: none;width:215px; height: 55px; left: 914.03px !important; border-color: rgb(200,200,200);border-width: 1px;border-radius: 5px;border-style: solid; background:white;box-shadow: 10px 10px 60px #555;">
    <div style="margin: 10px 10px 10px 10px">
        <div>
            <p style="display: inline;">As the Alert is In Progress state, no operations are permitted.</p>
        </div>
    </div>
</div>

<div id="deletionInProgressInfoDiv" class="infoBox"
     style="position: absolute;z-index: 9999; display: none;width:285px; height: 55px; left: 914.03px !important; border-color: rgb(200,200,200);border-width: 1px;border-radius: 5px;border-style: solid; background:white;box-shadow: 10px 10px 60px #555;">
    <div style="margin: 10px 10px 10px 10px">
        <div>
            <p style="display: inline;">As the Alert is in Deletion In Progress state, no operations are permitted.</p>
        </div>
    </div>
</div>

<div id="icrAutoAdjustmentInfoDiv" class="infoBox"
     style="position: absolute;z-index: 9999; display: none;width:285px; height: 165px; left: 914.03px !important; border-color: rgb(200,200,200);border-width: 1px;border-radius: 5px;border-style: solid; background:white;box-shadow: 10px 10px 60px #555;">
    <div style="margin: 10px 10px 10px 10px">
        <div id="icrAutoAdjustmentInfoDivValue">
            <p style="display: inline;"></p>
        </div>
    </div>
</div>

<div id="aggAutoAdjustmentInfoDiv" class="infoBox"
     style="position: absolute;z-index: 9999; display: none;width:285px; height: 165px; left: 914.03px !important; border-color: rgb(200,200,200);border-width: 1px;border-radius: 5px;border-style: solid; background:white;box-shadow: 10px 10px 60px #555;">
    <div style="margin: 10px 10px 10px 10px">
        <div id="aggAutoAdjustmentInfoDivValue">
            <p style="display: inline;"></p>
        </div>
    </div>
</div>

    <g:render template="/includes/modals/alert_pre_execution_check_modal" />
    <g:render template="/includes/modals/update_and_execute_modal" />
<input type="hidden" id="selectedDatasource" class="selectedDatasource" value="pva"/>
</body>
</html>