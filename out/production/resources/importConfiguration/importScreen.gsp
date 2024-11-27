<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.rxlogix.util.ViewHelper;com.rxlogix.util.DateUtil;com.rxlogix.Constants;grails.util.Holders; com.rxlogix.pvdictionary.config.PVDictionaryConfig" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.import.configuration"/></title>
    <asset:javascript src="app/pvs/userOptionPinning.js"/>
</head>
<body>
<rx:container title="${message(code: "app.import.configuration")}">
    <g:set var="userService" bean="userService"/>
    <g:render template="/includes/layout/flashErrorsDivs"/>
    <g:render template="/includes/widgets/import_config_widget_tab"/>
    <asset:javascript src="app/pvs/validated_signal/assessment_dictionary.js"/>
    <asset:javascript src="app/pvs/bootbox.min.js"/>

    <g:javascript>
    var isProductAssignment = true;
    var hasConfigurationEditorRole = "true";
    var fetchImportConfigListURL="${createLink(controller: 'importConfiguration', action: 'fetchAlertList')}";
    var selectTemplatesUrl="${createLink(controller: 'importConfiguration', action: 'fetchAlertTemplateByType')}";
    var createAlertFromTemplate="${createLink(controller: 'importConfiguration', action: 'createAlertFromTemplate')}";
    var editAlertNameUrl ="${createLink(controller: 'importConfiguration', action:'editAlertName')}";
    var updateDateRangeUrl="${createLink(controller: 'importConfiguration', action:'updateDateRangeForAlert')}";


    var sca_edit_url= "${createLink(controller: 'singleCaseAlert', action: 'edit')}";
    var sca_view_url= "${createLink(controller: 'singleCaseAlert', action: 'view')}";
    var sca_copy_url= "${createLink(controller: 'singleCaseAlert', action: 'copy')}";

    var aga_edit_url= "${createLink(controller: 'aggregateCaseAlert', action: 'edit')}";
    var aga_view_url= "${createLink(controller: 'aggregateCaseAlert', action: 'view')}";
    var aga_copy_url= "${createLink(controller: 'aggregateCaseAlert', action: 'copy')}";

    var runOnce_url="${createLink(controller: 'configuration', action: 'runOnce')}";
    var updateScheduleDateJSON_URL="${createLink(controller: 'importConfiguration', action: 'updateScheduleDateJSON')}";
    var unschedule_alert_url="${createLink(controller: 'importConfiguration', action: 'unScheduleAlert')}";
    var exportListToExcelURL="${createLink(controller:'importConfiguration',action:'exportToExcel')}"
    var searchUserGroupListUrl = "${createLink(controller: 'user', action: 'searchUserGroupList')}";
    var changeAssignedToGroup_Url="${createLink(controller: 'importConfiguration', action: 'changeAssignedToGroup')}"
    var changeShareWith_Url="${createLink(controller: 'importConfiguration', action: 'updateShareWithForConf')}"
    var dataSheetList= "${createLink(controller: 'dataSheet', action: 'dataSheets')}";
    var changeDatasheets_Url="${createLink(controller: 'importConfiguration', action: 'updateDatasheetsConfig')}"
    var fetchAssignmentForProductsUrl= "${createLink(controller: 'productAssignment', action: 'fetchAssignmentForProducts')}";
    var importConfigurationLogs_Url="${createLink(controller: 'importConfiguration', action: 'fetchImportConfigurationLog')}";
    var sharedWithListUrl = "${createLink(controller: 'user', action: 'searchUserGroupList')}";
    var sharedWithValuesUrl = "${createLink(controller: 'util', action: 'sharedWithValues')}";
    var sharedWithUrl = "${createLink(controller: 'user', action: 'searchShareWithUserGroupList')}";
    var uploadFileUrl= "${createLink(controller: 'importConfiguration', action: 'uploadFile')}";
    var delete_url="${createLink(controller: 'importConfiguration', action: 'deleteAlertConfig')}";
    var editProductSelect_url="${createLink(controller: 'importConfiguration', action: 'updateProdSelection')}";
        var dataSourcesColorMapValues = '{"faers": "rgb(112, 193, 179)", "eudra": "rgb(157, 129, 137)", "vaers": "rgb(92, 184, 92)", "vigibase": "rgb(255, 87, 51)"}';

            var options = { spinnerPath:"${assetPath(src: 'select2-spinner.gif')}" };
        options.product = {
            levelNames: "${PVDictionaryConfig.ProductConfig.views.collect { message(code: it.code) }.join(",")}",
            dicColumnCount: ${com.rxlogix.pvdictionary.config.PVDictionaryConfig.ProductConfig.columns.size()},
            selectUrl: "${createLink(controller: 'productDictionary', action: 'getSelectedItem')}",
            preLevelParentsUrl: "${createLink(controller: 'productDictionary', action: 'getPreLevelProductParents')}",
            searchUrl: "${createLink(controller: 'productDictionary', action: 'searchViews')}"
        };
        intializeDictionariesAssessment(options);
    </g:javascript>

    <g:if test="${!Holders.config.pv.plugin.dictionary.enabled}">
        <asset:javascript src="app/pvs/alert_utils/alert_product_utils.js"/>
        <asset:stylesheet src="dictionaries.css"/>
    </g:if>
    <g:else>
        <asset:javascript src="app/pvs/alert_utils/dictionary-utils.js"/>
    </g:else>

    <asset:stylesheet src="copyPasteModal.css"/>
    <asset:stylesheet src="configuration.css"/>
    <asset:javascript src="app/pvs/datahseet.js"/>
    <asset:javascript src="yadcf/jquery.datatables.yadcf.js"/>
    <asset:javascript src="app/pvs/configuration/deliveryOption.js"/>
    <asset:javascript src="app/pvs/dataTablesActionButtons.js"/>
    <asset:javascript src="fuelux/fuelux.js"/>
    <asset:javascript src="app/pvs/scheduler.js"/>

    <asset:javascript src="app/pvs/importConfiguration.js"/>
    <asset:javascript src="app/pvs/importConfigInlineEdit.js"/>


    <g:if test="${!Holders.config.pv.plugin.dictionary.enabled}">
        <asset:javascript src="app/pvs/alert_utils/alert_product_utils.js"/>
        <asset:stylesheet src="dictionaries.css"/>
    </g:if>
    <g:else>
        <asset:javascript src="app/pvs/alert_utils/dictionary-utils.js"/>
    </g:else>
    <style>

    table{
        margin: 0 auto;
        width: 100%;
        clear: both;
        border-collapse: collapse;
        table-layout: fixed;
    word-wrap:break-word;
    }
    .cancelButton{
        margin-right: 10px;
        margin-left: 5px;
    }
    </style>


    <div class="row">

        <div class="col-sm-3">
            <b><g:message code="app.label.substance.frequency.alertType"/></b>
            <select id="alertType" class="form-control">
                <option value="Single Case Alert" default="true">Individual Case Review</option>
                <option value="Aggregate Case Alert">Aggregate Review</option>
            </select>
        </div>
        <div class="col-sm-3">
            <b><g:message code="app.menu.setUpFromTemplate"/></b>
            <select id="templatesAlert" class="form-control"></select>
        </div>

        <div class="col-sm-2">
            <div class="row" style="padding-top: 15px;">
                <div class="col-sm-7"><button id="createFromSetupTemplate" style="margin-top: -13px;"
                                              class="btn btn-primary createFromSetupTemplate form"><g:message
                            code="default.button.ok.label"/></button>
                    <span class="glyphicon glyphicon-info-sign modal-link"
                          style="cursor:pointer;font-size: 20px;margin-left: 0px;margin-top:5px;top:-1px;"
                          data-toggle="modal"
                          data-target="#ExcelImportHelpModal"></span>
                </div>

            </div>
        </div>
        <div class="col-sm-4">

        </div>


    </div>
    <br><br>

</rx:container>
<rx:container title="${message(code: "app.import.configuration.list")}" >
    <table id="rxTableImportConfigurationList" class="row-border hover" width="100%">
        <thead>
        <tr>
            <th></th>
            <th><g:message code="import.configuraton.label.alertName"/></th>
            <th><g:message code="import.configuration.label.configuration.master"/></th>
            <th><g:message code="import.configuration.label.configuration.template"/></th>
            <th><g:message code="import.configuration.label.products"/></th>
            <th><g:message code="import.configuration.label.dataSheets"/></th>
            <th><g:message code="app.label.DateRangeType"/></th>
            <th><g:message code="app.label.assigned.to"/></th>
            <th><g:message code="app.label.share.with"/></th>
            <th><g:message code="app.label.scheduler"/></th>
            <th><g:message code="next.run.date"/></th>
            <th><g:message code="import.configuration.last.modified"/></th>
            <th><g:message code="app.label.action"/></th>
        </tr>
        </thead>
    </table>
</rx:container>



<!-- Modal for importExportHelp -->
<div class="modal fade ExcelImportHelpModal" id="ExcelImportHelpModal" tabindex="-1" role="dialog" aria-labelledby="Import From Excel Help">
    <div class="modal-dialog modal-lg " role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <span><b><g:message code="app.import.configuration.help.title"/></b></span>
            </div>
            <div class="modal-body container-fluid">
                <div> <g:message code="app.import.configuration.help.text1" /></div>
                <div> <g:message code="app.import.configuration.help.text2" /></div>
                <div> <g:message code="app.import.configuration.datasheet.help.text1" /></div>
                <div> <g:message code="app.import.configuration.help.text3" /></div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default cancel" data-dismiss="modal"><g:message code="default.button.close.label"/></button>
                </div>
            </div>
        </div>
    </div>
</div>

<div id="alertNameEdit" class="popupBox" style="position: absolute; display: none; width: 25%;background: #e5e5e5; box-shadow: 10px 10px 60px #555">
    <div class="row" style="margin:10px">
        <label><g:message code="import.configuraton.label.alertName"/></label>
        <input class="form-control newVal" >
    </div>

    <div class="row" style="margin-top: 10px; width: 100%; text-align: right; margin-bottom: 10px;">
        <button class="btn btn-primary saveButton"><g:message code="default.button.save.label"/></button>
        <button class="btn btn-default cancelButton"><g:message code="default.button.cancel.label"/></button>
    </div>
</div>

<div id="dateRangeEdit" class="popupBox" style="position: absolute; display: none;width:40%; background: #e5e5e5;box-shadow: 10px 10px 60px #555">
    <g:render template="/importConfiguration/importConfiguration_alertDateRange"/>

    <div style="margin-top: 10px; width: 100%; text-align: right; margin-bottom: 10px;">
        <button class="btn btn-primary saveButtonDateRange"><g:message code="default.button.save.label"/></button>
        <button class="btn btn-default cancelButton"><g:message code="default.button.cancel.label"/></button>
    </div>
</div>

<div id="assignedToEdit" class="popupBox" style="position: absolute; display: none;width:20%; background: #e5e5e5;box-shadow: 10px 10px 60px #555">
    <div class="row" style="margin:10px;">
        <label><g:message code="app.label.assigned.to"/></label>
        <div class="assignedToContainer"><select class="assignedToSelect form-control select2"></select><i class="mdi mdi-spin mdi-loading assignToProcessing" style="display: none;right:-10px"></i></div>

    </div>

    <div style="margin-top: 10px; width: 100%; text-align: right; margin-bottom: 10px;">
        <button class="btn btn-primary saveButtonAssignedTo"><g:message code="default.button.save.label"/></button>
        <button class="btn btn-default cancelButton"><g:message code="default.button.cancel.label"/></button>
    </div>
</div>

<div id="shareWithEdit" class="popupBox" style="position: absolute; display: none;width:20%; background: #e5e5e5;box-shadow: 10px 10px 60px #555">


    <div class="row" style="margin:10px;">
        <label><g:message code="app.label.share.with"/></label>
        <div class="shareWithContainer"><select class="shareWithSelect form-control select2"></select><i class="mdi mdi-spin mdi-loading shareWithProcessing" style="display: none;right:-10px"></i></div>

    </div>

    <div style="margin-top: 10px; width: 100%; text-align: right; margin-bottom: 10px;">
        <button class="btn btn-primary saveButtonShareWith"><g:message code="default.button.save.label"/></button>
        <button class="btn btn-default cancelButton"><g:message code="default.button.cancel.label"/></button>
    </div>
</div>

<div id="dataSheetEdit" class="popupBox" style="position: absolute; display: none; background: #e5e5e5;box-shadow: 10px 10px 60px #555">
    <div class="datasheet-options row" style="margin:10px;">
        <div class="datasheet-options  datasheetSelectWidth">
            <g:select id="dataSheet" name="dataSheet" from="${[]}"
                  value=""
                  data-value=""
                  class="dataSheet form-control datasheetSelectWidth" /><i class="mdi mdi-spin mdi-loading datasheetProcessing" style="display: none"></i>
        </div>
        <div class="datasheet-options">
                <g:checkBox id="allSheets" name="allSheets"
                            checked="${(configurationInstance?.datasheetType == com.rxlogix.Constants.DatasheetOptions.ALL_SHEET) ?: false}"
                            value="${(configurationInstance?.datasheetType == com.rxlogix.Constants.DatasheetOptions.ALL_SHEET) ? com.rxlogix.Constants.DatasheetOptions.ALL_SHEET: com.rxlogix.Constants.DatasheetOptions.CORE_SHEET}"/>
             <label for="allSheets">
                    <g:message code="app.datasheet.showAllSheets"/>
                </label>

        </div>
    </div>

    <div style="margin-top: 10px; width: 100%; text-align: right; margin-bottom: 10px;">
        <button class="btn btn-primary saveButtonDatasheets"><g:message code="default.button.save.label"/></button>
        <button class="btn btn-default cancelButton"><g:message code="default.button.cancel.label"/></button>
    </div>
</div>
<div id="schedulerEditDiv" class="popupBox" style="position: absolute; display: none;width:40%; background: #e5e5e5;box-shadow: 10px 10px 60px #555;">
    <div class="fuelux" id="schedulerDiv">
        <br>
        <g:hiddenField name="isEnabled" id="isEnabled" value="true"/>

        <g:render template="/includes/schedulerTemplate"/>
        <g:hiddenField name="schedulerTime" value="${com.rxlogix.util.RelativeDateConverter.getCurrentTimeWRTTimeZone(userService.getUser())}"/>
        <g:hiddenField name="scheduleDateJSON" value=""/>
        <input type="hidden" name="configSelectedTimeZone" id="configSelectedTimeZone" value="${userService.getCurrentUserPreference().timeZone}"/>
        <input type="hidden" id="timezoneFromServer" name="timezone" value="${DateUtil.getTimezone(userService.getUser())}"/>

    </div>
    <div style="margin-top: 10px; width: 100%; text-align: right; margin-bottom: 10px;">
        <button class="btn btn-primary saveButtonScheduler"><g:message code="default.button.save.label"/></button>
        <button class="btn btn-default cancelButton"><g:message code="default.button.cancel.label"/></button>
    </div>
</div>
<g:render template="/includes/modals/importConfigurationFileModal"/>
<g:render template="/includes/modals/importLogModal"/>

<g:hiddenField name="selectedDatasource" value="pva"></g:hiddenField>
<g:hiddenField name="clickedDatasource" id="clickedDatasource" value="pva"></g:hiddenField>
<g:hiddenField name="selectedDatasheets" id="selectedDatasheets" value="CORE_SHEET"/>
<g:hiddenField name="products" id="products" />

<g:render template="/template/fieldConfiguration" model="[fieldConfigurationBarId: 'productFields']"/>
<g:if test="${Holders.config.pv.plugin.dictionary.enabled}">
    <g:render template="/configuration/copyPasteModal"/>
    <input type="hidden" id="editable" value="true">
    <g:render template="/plugin/dictionary/dictionaryModals" plugin="pv-dictionary"
              model="[filtersMapList: Holders.config.product.dictionary.filtersMapList, viewsMapList: Holders.config.product.dictionary.viewsMapList]"/>
</g:if>
<g:else>
    <g:render template="/includes/modals/product_selection_modal"/>
</g:else>
<g:hiddenField name="isAssessmentDicitionary" id="isAssessmentDicitionary" value="true"/>

<g:render template="/productAssignment/includes/assessment_product_selection_modal" model="[isPCVM: isPVCM]"/>
</body>

