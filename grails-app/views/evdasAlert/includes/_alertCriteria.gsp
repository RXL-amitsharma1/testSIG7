<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils; grails.util.Holders; com.rxlogix.util.DateUtil; com.rxlogix.enums.DateRangeEnum; com.rxlogix.util.ViewHelper" %>
<div class="rxmain-container ">
    <g:set var="userService" bean="userService"/>
    <g:if test="${action == "copy"}">
        <g:hiddenField name="owner" id="owner" value="${userService.getUser().id}"/>
    </g:if>
    <g:else>
        <g:hiddenField name="owner" id="owner" value="${configurationInstance?.owner?.id ?: userService.getUser().id}"/>
    </g:else>
    <div class="rxmain-container-inner">
        <div class="rxmain-container-row rxmain-container-header">
            <label class="rxmain-container-header-label">
                <g:message code="app.label.alert.criteria"/>
            </label>
        </div>

        <div class="rxmain-container-content">

            <div class="row">
                <div class="col-md-12">
                    <div class="row">

                        %{-- Product Selection. --}%
                        <div class="col-md-5">
                            <label><g:message code="app.label.substanceSelection" default="Substance"/><span
                                    class="required-indicator">*</span></label>

                            <div class="wrapper">
                                <div id="showProductSelection" class="showDictionarySelection"></div>

                                <div class="iconSearch">
                                    <a data-toggle="modal" data-target="#productModal" tabindex="0" role="button" title="Select Substance" accesskey="p"><i class="fa fa-search"></i></a>
                                </div>
                            </div>

                            <g:hiddenField name="productSelection" value="${configurationInstance?.productSelection}"/>
                            <g:hiddenField name="productGroupSelection" id="productGroupSelection" value="${configurationInstance.productGroupSelection}"/>
                    </div>

                        %{-- Event Selection. --}%
                        <div class="col-md-5">
                            <label><g:message code="app.label.eventSelection"/></label>

                            <div class="wrapper">
                                <div id="showEventSelection" class="showDictionarySelection"></div>

                                <div class="iconSearch">
                                    <a  id="searchEvents" data-toggle="modal" data-target="#eventModal" tabindex="0" role="button" title="Select Event" accesskey="}"><i class="fa fa-search"></i></a>
                                </div>
                            </div>
                            <g:textField name="eventSelection" value="${configurationInstance?.eventSelection}"
                                         hidden="hidden"/>

                            <g:hiddenField name="eventGroupSelection" id="eventGroupSelection" value="${configurationInstance.eventGroupSelection}"/>
                    </div>

                        <div class="col-md-2" style="padding-left: 30px;">
                            <label>Options</label>

                            <div class="checkbox checkbox-primary">
                                <g:checkBox id="adhocRun"
                                            name="adhocRun"
                                            value="${configurationInstance?.adhocRun}"
                                            checked="${configurationInstance?.adhocRun}"
                                            disabled="${(action.equals("edit") && configurationInstance.adhocRun)}"/>
                                <label for="adhocRun">
                                    <g:message code="app.label.configuration.on.demand.run" />
                                </label>
                            </div>

                            <div>
                                <label id="substanceFrequency"
                                       style="display: none; margin-left: 25px; font-style: italic;"></label>
                                <g:hiddenField name="frequency" vlaue="${configurationInstance.frequency}" id="frequency"/>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <br/>

            <div class="row">
                <div class="col-md-3">
                    <label><g:message code="app.label.chooseAQuery"/></label>
                    <div style="padding-bottom: 5px;">
                        <g:select name="query" from="${[]}"
                                  class="form-control"/>
                    </div>
                    <g:hiddenField name="queryName" value="${configurationInstance?.queryName}"/>
                    <g:hiddenField name="evdasQueryId" value="${configurationInstance?.query}"/>
                </div>

                <div class="col-md-3">
                    <div class="row">
                        <div class="col-xs-12">
                            <label><g:message code="app.label.DateRange"/></label>
                            <g:select id="dateRangeEnum"
                                      name="dateRangeEnum" from="${ViewHelper.getEudraDateRange()}"
                                      optionValue="display"
                                      optionKey="name"
                                      value="${configurationInstance.dateRangeInformation?.dateRangeEnum ?: DateRangeEnum.CUMULATIVE}"
                                      class="form-control dateRangeEnumClass"/>
                        </div>
                    </div>

                    <div class="row">
                        <div id="dateRangeSelector" class="col-xs-12" style="display: none">
                            <div class="row">
                                <input name="startDateAbsoluteCustomFreq" id="startDateAbsoluteCustomFreq" type="text" hidden="hidden" value="${startDateAbsoluteCustomFreq}"/>
                                <div class="col-md-6">
                                    <g:message code="app.dateFilter.from"/>
                                    <select class="form-control" name="dateRangeStartAbsolute" id="fromDate"
                                            autocomplete="off">
                                        <option value="null">--Select One--</option>
                                    </select>
                                </div>

                                <input name="endDateAbsoluteCustomFreq" id="endDateAbsoluteCustomFreq" type="text" hidden="hidden" value="${endDateAbsoluteCustomFreq}"/>
                                <div class="col-md-6">
                                    <g:message code="app.dateFilter.to"/>
                                    <select class="form-control" name="dateRangeEndAbsolute" id="toDate"
                                            autocomplete="off">
                                        <option value="null">--Select One--</option>
                                    </select>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-md-3 dataSheetContainer">
                    <h5>
                        <g:checkBox id="selectedDatasheet" name="selectedDatasheet" value="${configurationInstance?.isDatasheetChecked}"
                                    checked="${configurationInstance?.isDatasheetChecked}"/>
                        <label for="selectedDatasheet">
                            <g:message code="app.label.datasheet.aggregate" default="Datasheet(s) Selection"/>
                        </label>
                        <span class="glyphicon glyphicon-info-sign themecolor modal-link"
                              data-toggle="modal"
                              data-target="#dataSheetOptionsHelpModal">
                        </span>
                    </h5>
                </div>
            </div>
            <div class="row datasheet-options">
                <div class="col-md-3 dataSheetContainer">
                        <g:select id="dataSheet" name="dataSheet" from="${[]}"
                                  value="${configurationInstance?.selectedDataSheet}"
                                  data-value="${configurationInstance?.selectedDataSheet}"
                                  class="form-control"/>
                </div>
                <div class="col-md-3 dataSheetContainer">
                            <g:checkBox id="allSheets" name="allSheets"
                                        checked="${(configurationInstance?.datasheetType == com.rxlogix.Constants.DatasheetOptions.ALL_SHEET) ?: false}"
                                        value="${(configurationInstance?.datasheetType == com.rxlogix.Constants.DatasheetOptions.ALL_SHEET) ? com.rxlogix.Constants.DatasheetOptions.ALL_SHEET : com.rxlogix.Constants.DatasheetOptions.CORE_SHEET}"/>

                            <label for="allSheets">
                                <g:message code="app.datasheet.showAllSheets"/>
                            </label>
                </div>
            </div>

                <div class="modal fade dataSheetOptionsHelpModal" id="dataSheetOptionsHelpModal" tabindex="-1" role="dialog" aria-labelledby="Datasheet Selection help">
                    <div class="modal-dialog modal-lg " role="document">
                        <div class="modal-content">
                            <div class="modal-header">
                                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                                <span><b><g:message code="app.datasheet.help.title.text"/></b></span>
                            </div>
                            <div class="modal-body container-fluid border">
                                <div class="">
                                    <ul class="dataSheetAdvancedModal table-bordered">
                                        <li><div> <g:message code="app.datasheet.help.modal.text2" /></div><br></li>
                                        <li><div> <g:message code="app.datasheet.help.modal.text3" /></div><br></li>
                                        <li><div> <g:message code="app.datasheet.help.modal.text4" /></div><br></li>
                                        <li><div> <g:message code="app.datasheet.help.modal.text5" /></div><br></li>
                                        <li><div> <g:message code="app.datasheet.help.modal.text6" /></div><br></li>
                                        <li><div> <g:message code="app.datasheet.help.modal.text7" /></div><br></li>
                                        <li><div> <g:message code="app.datasheet.help.modal.text8" /></div><br></li>
                                        <li><div> <g:message code="app.datasheet.help.modal.text9" /></div><br></li>
                                        <li><div> <g:message code="app.datasheet.help.modal.text10" /></div><br></li>
                                        <li><div> <g:message code="app.datasheet.help.modal.text11" /></div><br></li>
                                        <li><div> <g:message code="app.datasheet.help.modal.text12" /></div><br></li>
                                        <li><div> <g:message code="app.datasheet.help.modal.text13" /></div><br></li>
                                        <li><div> <g:message code="app.datasheet.help.modal.text14" /></div><br></li>
                                        <li><div> <g:message code="app.datasheet.help.modal.text15" /></div><br></li>
                                    </ul>
                                </div>


                            </div>
                            <div class="modal-footer">
                                <button type="button" class="btn btn-default cancel" data-dismiss="modal"><g:message code="default.button.ok.label"/></button>
                            </div>
                        </div>
                    </div>
                </div>
            <g:if test="${actionName == 'edit'}">
                <div class="margin m-t-10">
                    <label class="text-grey"><i>Original Date Range: ${firstExecutionDate}</i></label>
                </div>

                <div>
                    <label class="text-grey"><i>Last Execution Date Range: ${lastExecutionDate}</i></label>
                </div>
            </g:if>

        </div>
</div>
</div>
<input type="hidden" id="selectedDatasheets" value="ALL_SHEET"/>

<g:if test="${Holders.config.pv.plugin.dictionary.enabled}">
    <g:render template="/configuration/copyPasteModal" />
    <input type="hidden" id="editable" value="true">
    <g:render template="/plugin/dictionary/dictionaryModals" plugin="pv-dictionary"
              model="[filtersMapList: Holders.config.product.dictionary.filtersMapList, viewsMapList: Holders.config.product.dictionary.viewsMapList]"/>
</g:if>
<g:else>
    <g:render template="/includes/modals/event_selection_modal"/>
    <g:render template="/includes/modals/product_selection_modal" />
</g:else>