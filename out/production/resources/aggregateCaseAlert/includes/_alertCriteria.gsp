<%@ page import="grails.util.Holders; com.rxlogix.util.DateUtil; grails.plugin.springsecurity.SpringSecurityUtils; com.rxlogix.util.ViewHelper" %>
<div class="row">

    <div class="col-md-12">
        <div class="row">
            <g:render template="/includes/widgets/product_study_generic-selection" bean="${configurationInstance}"
                      model="[theInstance: configurationInstance, showHideGenericVal: 'hide', showHideStudyVal : 'hide']"/>


            <div class="col-md-3">
                 <label><g:message code="app.label.eventSelection"/><span class="required-indicator"/><span class="required-indicator"/></label>
                <label class="checkbox-inline no-bold add-margin-bottom hidden " style="margin-bottom: 5px;">
                    <g:checkBox name="limitPrimaryPath" value="${configurationInstance?.limitPrimaryPath}"
                            checked="${configurationInstance?.limitPrimaryPath}"/>
                    <g:message code="app.label.eventSelection.limit.primary.path"/>
                </label>

                <div class="wrapper">
                    <div id="showEventSelection" class="showDictionarySelection"></div>

                    <div class="iconSearch">
                        <a id="searchEvents" data-toggle="modal" data-target="#eventModal" tabindex="0" role="button" data-toggle="tooltip" title="Search Event" accesskey="}"><i class="fa fa-search"></i></a>
                    </div>
                </div>
                <g:textField name="eventSelection" value="${configurationInstance?.eventSelection}" hidden="hidden"/>
            </div>

            <div class="col-md-3">
                <label><g:message code="app.label.DateRangeType"/><span class="required-indicator">*</span></label>
                <g:select name="dateRangeType" id="dateRangeType"
                          from="${ViewHelper.getDateRangeTypeI18n()}"
                          optionValue="display" optionKey="name"
                          value="${configurationInstance?.dateRangeType}"
                          class="form-control"/>


                <div class="m-t-5">
                    <label><g:message code="app.label.EvaluateCaseDateOn"/></label>
                    <div id="evaluateDateAsDiv">
                        <g:select name="evaluateDateAsNonSubmission"
                                  from="${ViewHelper.getEvaluateCaseDateI18n()}"
                                  optionValue="display" optionKey="name"
                                  value="${configurationInstance?.evaluateDateAs}"
                                  class="form-control evaluateDateAs"/>
                    </div>
                    <input name="evaluateDateAs" id="evaluateDateAs" type="text" hidden="hidden"
                           value="${configurationInstance?.evaluateDateAs}"/>

                    <div style="margin-top: 10px">
                        <div class="fuelux">
                            <div class="datepicker toolbarInline" id="asOfVersionDatePicker" hidden="hidden">
                                <div class="input-group">
                                    <g:hiddenField name="asOfVersionDateValue"  value="${configurationInstance?.asOfVersionDate ?: null}"/>
                                    <input placeholder="${message(code: "select.version")}"
                                           class="form-control" id="asOfVersionDateId"
                                           name="asOfVersionDate" type="text"/>
                                    <g:render id="asOfVersion" template="/includes/widgets/datePickerTemplate"/>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-md-3">
                <!-- Modal -->
                <div class="modal fade" id="myModal" role="dialog">
                    <div class="modal-dialog" style="width:930px">

                        <!-- Modal content-->
                        <div class="modal-content">
                            <div class="modal-header">
                                <button type="button" class="close" data-dismiss="modal">&times;</button>
                                <h4 class="modal-title">Option help</h4>
                            </div>
                            <div class="modal-body">
                                <table class="table table-bordered">
                                    <tbody>
                                    <tr>
                                        <td><g:message code="reportCriteria.exclude.follow.up"/></td>
                                        <td><g:message code="reportCriteria.exclude.follow.up.description"/></td>
                                    </tr>
                                    <tr>
                                        <td>Exclude Non-valid Cases</td>
                                        <td>Checking this checkbox will exclude cases in the Alert output that are classified as Nonvalid Cases. This is applicable for Safety DB only.</td>
                                    </tr>
                                    <tr>
                                        <td>Include Cases Missed in the Previous Reporting Period</td>
                                        <td>Checking this check box will fetch the cases that are missed in previous reporting periods due to locked versions. This is applicable for Safety DB only.</td>
                                    </tr>
                                    <tr>
                                        <td>Adhoc Run</td>
                                        <td>Adhoc Run enables the user to execute an ad hoc Aggregate alert that does not retain any previous states of the alert. This option is not applicable when multiple data sources are configured in alert configuration.</td>
                                    </tr>
                                    <tr>
                                        <td>Data Mining Based on SMQ/Event group</td>
                                        <td>Data mining will be performed based on SMQ or Event group. The Aggregate alert output will be grouped based on Product of interest and SMQ combination or Product of interest and Event group combination. This option is not applicable when EVDAS data source is configured along with other data sources in alert configuration.</td>
                                    </tr>
                                    </tbody>
                                </table>
                            </div>
                            <div class="modal-footer">
                                <button type="button" class="btn btn-default" data-dismiss="modal">Ok</button>
                            </div>
                        </div>

                    </div>
                </div>
                <!-- Modal code end-->

                <label><g:message code="app.label.inclusionOptions"/></label>  <a href="javascript:void(0)" class="glyphicon glyphicon-info-sign themecolor" data-toggle="modal" data-target="#myModal"></a>

                <div class="checkbox checkbox-primary">
                    <g:checkBox id="excludeFollowUp"
                                name="excludeFollowUp"
                                value="${configurationInstance?.excludeFollowUp}"
                                checked="${configurationInstance?.excludeFollowUp}"/>
                    <label for="excludeFollowUp">
                        <g:message code="reportCriteria.exclude.follow.up"/>
                    </label>
                </div>
                <g:if test="${Holders.config.alertStopList}">
                <div class="checkbox hide checkbox-primary">
                    <g:checkBox id="applyAlertStopList"
                                name="applyAlertStopList"
                                value="${configurationInstance?.applyAlertStopList}"
                                checked="${configurationInstance?.applyAlertStopList}"/>
                    <label for="applyAlertStopList">
                        <g:message code="reportCriteria.exclude.non.valid.cases"/>
                    </label>
                </div>
                </g:if>

            <div class="checkbox checkbox-primary">
                <g:checkBox id="groupBySmq"
                            name="groupBySmq"
                            value="${configurationInstance?.groupBySmq}"
                            checked="${configurationInstance?.groupBySmq}"/>
                <label for="groupBySmq">
                    <g:message code="reportCriteria.groupBySmq"/>
                </label>
            </div>

            <div class="checkbox checkbox-primary">
                    <g:checkBox id="adhocRun"
                                name="adhocRun"
                                value="${configurationInstance?.adhocRun}"
                                checked="${configurationInstance?.adhocRun}"
                                disabled="${(configurationInstance.id && configurationInstance.adhocRun)}"/>

                <label for="adhocRun"><g:message code="app.label.configuration.on.demand.run" /></label>
                </div>

                <div class="checkbox checkbox-primary">
                    <g:checkBox id="excludeNonValidCases"
                                name="excludeNonValidCases"
                                value="${configurationInstance?.excludeNonValidCases}"
                                checked="${configurationInstance?.excludeNonValidCases}"/>
                    <label for="excludeNonValidCases">
                        <g:message code="app.label.configuration.exclude.invalid.cases"/>
                    </label>
                </div>

                <g:if test="${Holders.config.configurations.include.locked.versions.enabled}">
                <div class="checkbox checkbox-primary includeLockedClass">
                    <g:checkBox id="includeLockedVersion"
                                name="includeLockedVersion"
                                value="${configurationInstance?.includeLockedVersion}"
                                checked="${configurationInstance?.includeLockedVersion}"/>
                    <label for="includeLockedVersion">
                        <g:message code="reportCriteria.include.locked.versions.only"/>
                    </label>

                </div>
                </g:if>

                <div class="checkbox checkbox-primary">
                    <g:checkBox id="missedCases"
                                name="missedCases"
                                value="${configurationInstance?.missedCases}"
                                checked="${configurationInstance?.missedCases}"/>
                    <label for="missedCases">
                        <g:message code="app.label.configuration.missed.cases"/>
                    </label>

                </div>
            </div>
        </div>

        <div class="row">

            <div class="col-md-3">
                <label><g:message code="app.label.choseQuery.aggregate"/></label> <a href="javascript:void(0)" class="glyphicon glyphicon-info-sign themecolor open-background-query"  data-toggle="tooltip" title="Background Query help"></a>
                <!-- Modal -->
                <div class="modal fade" id="myModal2" role="dialog">
                    <div class="modal-dialog" style="width:930px">

                        <!-- Modal content-->
                        <div class="modal-content">
                            <div class="modal-header">
                                <button type="button" class="close" data-dismiss="modal">&times;</button>
                                <h4 class="modal-title">Background Query help</h4>
                            </div>
                            <div class="modal-body">
                                <table class="table table-bordered">
                                    <tbody>
                                    <tr>
                                        <td>Background Query</td>
                                        <td>The system will limit the data mining background based on the query configured in the ‘Background Query’ option in alert criteria for Aggregate alerts for both Safety DB and FAERS.
                                        <br/><br/>
                                        If no Query is selected, the system will restrict the background to the spontaneous cases by default. This will be default configuration and applicable for safety DB only. In this case study counts are calculated on entire data set.
                                        <br/><br/>
                                        For Safety DB, if the query is selected in background query, then this will override the default background configuration.
                                        <br/><br/>
                                        When a background query is selected for Safety DB, the system will use the query criteria selected in the background query and limit the background on the entire data set. New count and Cum count will be displayed based on back ground query. New study and cum study counts displayed will be on entire data set irrespective of query selection.
                                        <br/><br/>
                                        When both SafetyDB and FAERS are configured, then back ground query is applicable only for Safety DB.
                                        <br/><br/>Selection of event level variable for background is not recommended.
                                        </td>
                                    </tr>
                                    </tbody>
                                </table>
                            </div>
                            <div class="modal-footer">
                                <button type="button" class="btn btn-default" data-dismiss="modal">Ok</button>
                            </div>
                        </div>

                    </div>
                </div>
                <!-- Modal code end-->

                <div class="row queryContainer m-l-0">
                    <g:if test="${editMode}">
                        <div>
                            <i class="fa fa-refresh fa-spin loading"></i>
                        </div>
                    </g:if>

                    <div class="doneLoading " style="padding-bottom: 5px;">
                        <g:select name="alertQuery" from="${[]}" class="form-control alertQuery"/>
                    </div>
                    <g:hiddenField name="alertQueryName" value="${configurationInstance?.alertQueryName}" id="alertQueryName"/>
                    <g:hiddenField name="alertQueryId" value="${configurationInstance?.alertQueryId}" id="alertQueryId"/>
                </div>
            </div>

            <div class="col-md-1 bgQueryIcon">
                <a target="_blank" class="glyphicon pull-left glyphicon-info-sign viewBgQuery"  data-toggle="tooltip" data-original-title="View Query" ></a>
            </div>
            <div class="col-md-5" style="padding-left: 10px !important;">
                <g:render template="/singleCaseAlert/alertDateRange"
                          model="[configurationInstance: configurationInstance]"/>
            </div>

            <div class="col-md-3" style="float: right;">
                <label for="drugType">
                    <g:message code="productType.signal.productType"/>
                    <span class="required-indicator">*</span>
                </label>

                <div>
                    <select id="drugType" name="drugType"
                              data-value="${configurationInstance?.drugType}"
                              class="form-control"></select>
                </div>
            </div>

%{-- Removed story/PVS-57996- part for drug classification --}%
        </div>

        <div class="row">
            <div class="alertQueryWrapper col-md-9 p-lr-0">
                <div class="queryExpressionValues">
                    <g:if test="${configurationInstance?.alertQueryValueLists?.size() > 0}">
                        <g:each var="qvl" in="${configurationInstance.alertQueryValueLists}"  status="i">
                            <g:each var="qev" in="${qvl.parameterValues}" status="j">
                                <g:if test="${qev?.hasProperty('reportField')}">
                                    <g:render template='/query/toAddContainerQEV' model="['qev': qev,'type': 'qev', 'i':i, 'j':j]"/>
                                </g:if>
                                <g:else>
                                    <g:render template='/query/customSQLValue' model="['qev': qev,'type': 'qev', 'i':i, 'j':j]"/>
                                </g:else>
                            </g:each>
                        </g:each>
                    </g:if>
                </div>
              <g:hiddenField class="validQueries" name="validQueries" value="${configurationInstance ? configurationInstance.getQueriesIdsAsString() :''}" />
            </div>
        </div>
        <input type="hidden" name="foregroundSearch" id="foregroundSearch" value="${configurationInstance.foregroundSearch}" class="m-l-5">
        <input type="hidden" name="foregroundSearchAttr" id="foregroundSearchAttr" value="${configurationInstance.foregroundSearchAttr}">
        <input type="hidden" id="alertType" value="${configurationInstance.type}">
        <div class="row foregroundQueryCheckbox" style="margin-left: 15px; margin-top: 20px;">
            <div class="checkbox checkbox-primary col-md-6">
                <input type="hidden" name="_addForegroundQuery" autocomplete="off"><input type="checkbox"
                                                                                          name="foregroundQuery"
                                                                                          id="foregroundQuery"
                                                                                          autocomplete="off">
                <label for="foregroundQuery" class="foregroundQueryCheckbox">Add Foreground Query</label>
            </div>
        </div>

        <div class="row forgroundQuery" style="display: none; margin-top: 20px;">
            <div class="col-md-3">
                <label><g:message code="app.label.foreground.queryName"/></label> <a
                    href="javascript:void(0)"
                    class="glyphicon glyphicon-info-sign themecolor grid-menu-tooltip open-foreground-query"
                    data-toggle="tooltip" title="Foreground Query help"
                    data-target="#myModal3"></a>

                <!-- Modal -->
                <div class="modal fade" id="myModal3" role="dialog">
                    <div class="modal-dialog" style="width:930px">
                        <!-- Modal content-->
                        <div class="modal-content">
                            <div class="modal-header">
                                <button type="button" class="close" data-dismiss="modal">&times;</button>
                                <h4 class="modal-title">Foreground Query help</h4>
                            </div>

                            <div class="modal-body">
                                <table class="table table-bordered">
                                    <tbody>
                                    <tr>
                                        <td>Foreground Query</td>
                                        <td>Based on the selection of foreground query, the system will restrict the cases for selected products on which data mining is performed. Remaining cases for same products/other products shall be considered as other product cases.
                                            <br/><br/>
                                            Foreground query is applicable only for Safety DB.
                                            <br/><br/>
                                            When the Product dictionary is configured with search attributes , a check box "Consider selected filters as the foreground for data mining run" will be available.
                                            Based on the search attributes, the product will be filtered and available for selection in product dictionary.
                                            <br/><br/>
                                            When the search attributes are selected in product dictionary and "Consider selected filters as the foreground for data mining run" is checked then such variables will be considered as foreground for the alert.
                                            <br/><br/>
                                            If no search attributes are selected in product dictionary or no query is selected in foreground query or no value is selected in the foreground  parameterized query, then system will consider as no foreground is configured.
                                            <br/><br/>
                                            Selection of event level variable for foreground is not recommended.
                                        </td>
                                    </tr>
                                    </tbody>
                                </table>
                            </div>

                            <div class="modal-footer">
                                <button type="button" class="btn btn-default" data-dismiss="modal">Ok</button>
                            </div>
                        </div>

                    </div>
                </div>
                <!-- Modal code end-->

                <div class="row queryContainer1">
                    <g:if test="${editMode}">
                        <div>
                            <i class="fa fa-refresh fa-spin loading"></i>
                        </div>
                    </g:if>

                    <div class="doneLoading m-l-5" style="padding-bottom: 5px;">
                        <g:select name="alertForegroundQuery" from="${[]}" class="form-control alertForegroundQuery"/>
                    </div>
                    <g:hiddenField name="alertForegroundQueryName"
                                   value="${configurationInstance?.alertForegroundQueryName}"/>
                    <g:hiddenField name="alertForegroundQueryId"
                                   value="${configurationInstance?.alertForegroundQueryId}"/>
                </div>
            </div>
            <div class="col-md-1 fgQueryIcon">
                <a target="_blank" class="glyphicon pull-left glyphicon-info-sign viewFgQuery"
                   style="float: right;display: none; cursor:pointer;" data-toggle="tooltip"
                   data-original-title="View Query"></a>
            </div>
        </div>

        <div class="row">
            <div class="forgroundQuery" style="display: none">
                <div class="alertQueryWrapper1 col-md-9">
                    <div class="queryExpressionValues1" style="margin-left: -5px">
                        <g:if test="${configurationInstance?.alertForegroundQueryValueLists?.size() > 0}">
                            <g:each var="qvl" in="${configurationInstance.alertForegroundQueryValueLists}" status="i">
                                <g:each var="fev" in="${qvl.parameterValues}" status="j">
                                    <g:if test="${fev?.hasProperty('reportField')}">
                                        <g:render template='/query/toAddContainerQEV'
                                                  model="['type': 'fev', 'fev': fev, 'i': i, 'j': j]"/>
                                    </g:if>
                                    <g:else>
                                        <g:render template='/query/customSQLValue'
                                                  model="['type': 'fev', 'fev': fev, 'i': i, 'j': j]"/>
                                    </g:else>
                                </g:each>
                            </g:each>
                        </g:if>
                    </div>
                    <g:hiddenField class="foregroundValidQueries" name="foregroundValidQueries"
                                   value="${configurationInstance ? configurationInstance.getForegroundQueriesIdsAsString() : ''}"/>
                </div>
            </div>
        </div>


        <div class="row dataSheetContainer m-l-0">
            <div class="checkbox checkbox-primary col-md-6" style="margin-left: 15px !important">
                <g:checkBox id="selectedDatasheet" name="selectedDatasheet"
                            value="${configurationInstance?.isDatasheetChecked}"
                            checked="${configurationInstance?.isDatasheetChecked}"/>
                <label for="selectedDatasheet">
                    <g:message code="app.label.datasheet.aggregate" default="Datasheet(s) Selection"/>
                </label>
                <a class="glyphicon glyphicon-info-sign themecolor modal-link"
                      data-toggle="modal"
                      data-target="#dataSheetOptionsHelpModal" style="cursor:pointer;" data-toggle="tooltip"
                   data-original-title="Datasheet(s) Selection">
                </a>
            </div>
        </div>
        <div class="row dataSheetContainer">
            <div class="datasheet-options col-md-3">
                <g:select id="dataSheet" name="dataSheet" from="${[]}"
                          value="${configurationInstance?.selectedDataSheet}"
                          data-value="${configurationInstance?.selectedDataSheet}"
                          class="form-control"/>
            </div>
            <div class="datasheet-options col-md-3">
                <g:checkBox id="allSheets" name="allSheets"
                            checked="${(configurationInstance?.datasheetType == com.rxlogix.Constants.DatasheetOptions.ALL_SHEET) ?: false}"
                            value="${(configurationInstance?.datasheetType == com.rxlogix.Constants.DatasheetOptions.ALL_SHEET) ? com.rxlogix.Constants.DatasheetOptions.ALL_SHEET : com.rxlogix.Constants.DatasheetOptions.CORE_SHEET}"/>

                    <label for="allSheets">
                        <g:message code="app.datasheet.showAllSheets"/>
                    </label>
            </div>
        </div>
    </div>
</div>
<input type="hidden" id="isIncludeLockedVersions" value="${Holders.config.configurations.include.locked.versions.enabled}"/>
<input type="hidden" id="selectedDatasheets" value="CORE_SHEET"/>
<g:if test="${actionName == 'edit'}">
    <div class="margin m-t-10">
        <label class="text-grey"><i>Original Date Range: ${firstExecutionDate}</i></label>
    </div>

    <div>
        <label class="text-grey"><i>Last Executed Date Range: ${lastExecutionDate}</i></label>
    </div>
</g:if>

<g:if test="${Holders.config.pv.plugin.dictionary.enabled}">
    <input type="hidden" id="editable" value="true">
    <g:render template="/plugin/dictionary/dictionaryModals" plugin="pv-dictionary"
              model="[filtersMapList: Holders.config.product.dictionary.filtersMapList, viewsMapList: Holders.config.product.dictionary.viewsMapList,isPVCM: isPVCM, multiIngredientValue: configurationInstance.isMultiIngredient]"/>
</g:if>
<g:else>
    <g:render template="/includes/modals/event_selection_modal" model="[sMQList: sMQList]"/>
    <g:render template="/includes/modals/product_selection_modal" />
    <g:render template="/includes/modals/study_selection_modal" />
</g:else>
<g:render template="/includes/modals/product_group_selection_modal" model="[productGroupList: productGroupList]"/>

