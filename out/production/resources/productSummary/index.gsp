<%@ page import="grails.util.Holders; grails.converters.JSON; com.rxlogix.enums.ReportFormat" %>
<%@ page import="com.rxlogix.util.ViewHelper" %>
<head>
    <meta name="layout" content="main"/>
    <title>Product Summary</title>
    <asset:javascript src="fuelux/fuelux.js"/>
    <asset:javascript src="app/pvs/common/rx_common.js"/>
    <asset:javascript src="vendorUi/popover/popover.min.js"/>
    <asset:javascript src="app/pvs/alert_utils/common_alert_utils.js"/>
    <g:if test="${!Holders.config.pv.plugin.dictionary.enabled}">
        <asset:javascript src="app/pvs/alert_utils/alert_product_utils.js"/>
    </g:if>
    <g:else>
        <asset:javascript src="app/pvs/alert_utils/dictionary-utils.js"/>
        <asset:javascript src="app/pvs/alert_utils/multi_datasource_dictionary.js"/>
    </g:else>
    <asset:javascript src="app/pvs/productSummary/dateRange.js"/>
    <asset:javascript src="app/pvs/productSummary/productSummary.js"/>
    <asset:javascript src="app/pvs/alertComments/alertComments.js"/>
    <asset:javascript src="app/pvs/actions/actions.js"/>
    <asset:javascript src="app/pvs/common/rx_common.js"/>
    <asset:javascript src="app/pvs/common/rx_list_utils.js"/>
    <asset:javascript src="app/pvs/common/rx_handlebar_ext.js"/>
    <asset:javascript src="app/pvs/dataTablesActionButtons.js"/>
    <asset:stylesheet src="fuelux.css"/>
    <asset:stylesheet src="vendorUi/popover/popover.min.css"/>
    <asset:stylesheet src="configuration.css"/>
    <g:if test="${!Holders.config.pv.plugin.dictionary.enabled}">
        <asset:stylesheet src="dictionaries.css"/>
    </g:if>
    <asset:javascript src="app/pvs/configuration/copyPasteValues.js"/>
    <asset:javascript src="app/pvs/configuration/dictionaryMultiSearch.js"/>
    <asset:stylesheet src="copyPasteModal.css"/>

    <asset:javascript src="yadcf/jquery.dataTables.yadcf.js"/>
    <g:javascript>
        var searchGenericsUrl = "${createLink(controller: 'configurationRest', action: 'searchGenerics')}";
        var substanceFrequencyPropertiesUrl = "${createLink(controller: 'productSummary', action: 'fetchSubstanceFrequency')}";
        var fetchAssessmentNotesUrl = "${createLink(controller: 'validatedSignal', action: 'fetchAssessmentNotes')}";
        var saveAssessmentNotesUrl = "${createLink(controller: 'validatedSignal', action: 'saveAssessmentNotes')}";
        var saveRequestByUrl = "${createLink(controller: 'productSummary', action: 'requestByForAlert')}";
        var listProductSummaryUrl = "${createLink(controller: 'productSummary', action: 'listProductSummaryResult')}";
        var exportReportUrl = "${createLink(controller: 'productSummary',action:'exportReport')}";

    </g:javascript>
</head>

<body>
<g:render template="/includes/layout/flashErrorsDivs" bean="${productSummary}" var="theInstance"/>



<g:form name="product-summary-form" url="#">

    <input type="hidden" id="productSummaryObject" value="${productSummary}"/>
    <g:hiddenField name="editAggregate" id="editAggregate" value="false"/>
    <g:hiddenField name="previousStartDate" id="previousStartDate" value="${previousStartDate}"/>
    <g:hiddenField name="previousEndDate" id="previousEndDate" value="${previousEndDate}"/>

    <div class="rxmain-container ">
        <div class="rxmain-container-inner">

            <div class="rxmain-container-row rxmain-container-header">
                <label class="rxmain-container-header-label">
                    Product Summary
                </label>
            </div>

            <div class="rxmain-container-content">
                <div class="row">
                    <div class="col-md-4">
                        <div class="form-group">
                            <label>
                                Data Source
                                <span class="required-indicator">*</span>
                            </label>
                            <g:select id="selectedDatasource" name="selectedDatasource"
                                      from="${dataSourceMap.entrySet()}"
                                      optionKey="key" optionValue="value"
                                      class="form-control selectedDatasource"
                                      value="${selectedDataSource}"/>
                            <g:hiddenField name="dispositionValue" id="dispositionValue"
                                           value="${selectedDispositions}"/>
                        </div>
                        <div class="form-group">
                            <label>
                                Disposition
                            </label>
                            <g:select name="disposition" id="disposition" class="form-control"
                                      from="${disposition}"
                                      multiple="true"/>
                        </div>

                    </div>

                    <div class="col-md-4">
                        <label>
                            Product
                            <span class="required-indicator">*</span>
                        </label>

                        <div class="wrapper">
                            <div id="showProductSelection" class="showDictionarySelection"></div>

                            <div class="iconSearch">
                                <a tabindex="0" role="button" class="fa fa-search productRadio" data-toggle="modal" data-target="#productModal" accesskey="p"></a>
                            </div>
                            <g:hiddenField name="productSelection" class="productSelection"
                                           value="${productSelection}"/>
                            <g:hiddenField name="isMultiIngredient" class="multiIngredient"
                                           value="${isMultiIngredient}"/>
                        </div>
                    </div>
                    <g:hiddenField id="frequency" name="frequency"/>
                    <div class="col-md-4">

                        <div class="row">

                            <div id="dateRangeSelector">
                                <div class="col-md-6 form-group">
                                    <label>
                                        Date Range(From)
                                        <span class="required-indicator">*</span>
                                    </label>
                                    <select class="form-control" name="startDate" id="fromDate"
                                            autocomplete="off">
                                        <option value="null">--Select One--</option>
                                    </select>
                                </div>


                                <div class="col-md-6 form-group">
                                    <label>
                                        Date Range(To)
                                        <span class="required-indicator">*</span>
                                    </label>
                                    <select class="form-control" name="endDate" id="toDate"
                                            autocomplete="off">
                                        <option value="null">--Select One--</option>
                                    </select>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="col-md-4 m-t-30 text-right ">
                        <button id="search" class="btn btn-primary" disabled="disabled"><g:message
                                code="default.button.search.label"/></button>
                        <button id="cancel-drug-classification" class="btn btn-default"><g:message
                                code="default.button.cancel.label"/></button>
                    </div>

                </div>
            </div>
        </div>
    </div>

</g:form>

<div class="rxmain-container m-t-5">
    <div class="rxmain-container-inner">
        <div class="rxmain-container-row rxmain-container-header ico-menu">
            <label class="rxmain-container-header-label">
                Product Summary List
            </label>
            <span class="pull-right">
                <span class="dropdown-toggle exportPanel " data-toggle="dropdown" accesskey="x" tabindex="0" title="Export to"><i class="mdi mdi-export blue-1 font-24 "></i>
                    <span class="caret hidden"></span>

                </span>
                <ul class="dropdown-menu export-type-list" id="exportTypesTopic">
                    <strong class="font-12">Export</strong>
                    <li class="export_icon"><a  href="#" data-format="${ReportFormat.DOCX}">
                        <img src="/signal/assets/word-icon.png" class="m-r-10" height="16" width="16" /><g:message code="save.as.word" />
                    </a>
                    </li>
                    <li class="export_icon"><a href="#" data-format="${ReportFormat.XLSX}">
                        <img src="/signal/assets/excel.gif" class="m-r-10" height="16" width="16" /><g:message code="save.as.excel" />
                    </a></li>
                    <li class="export_icon"><a  href="#" data-format="${ReportFormat.PDF}">
                        <img src="/signal/assets/pdf-icon.jpg" class="m-r-10" height="16" width="16" /><g:message code="save.as.pdf" />
                    </a></li>
                </ul>
            </span>
            <span id="toggle-column-filters" data-title="Filters"
                  class="pull-right grid-menu-tooltip  m-r-10" accesskey="y" tabindex="0"><i class="mdi mdi-filter-outline blue-1 font-24"></i>
            </span>
        </div>

        <div class="rxmain-container-content">
            <div class="row">
                <table id="productSummaryTable">
                    <thead>
                    <tr>
                        <th>Event</th>
                        <th>Disposition</th>
                        <th>Alert/Signal Name</th>
                        <th>Data Source</th>
                        <th>
                            <div style="border-bottom: 2px solid #000;display: inline-block;padding-bottom: 3px;margin-bottom: 3px">Spon</div>
                            <div>N/P/C</div>
                        </th>
                        <th>
                            <div style="border-bottom: 2px solid #000;display: inline-block;padding-bottom: 3px;margin-bottom: 3px">EB05</div>

                            <div>Curr/Prev</div>
                        </th>
                        <th>
                            <div style="border-bottom: 2px solid #000;display: inline-block;padding-bottom: 3px;margin-bottom: 3px">EB95</div>

                            <div>Curr/Prev</div>
                        </th>
                        <th>Requested By/Sources</th>
                        <th>Comments</th>
                        <th>Assessment Comments</th>
                    </tr>
                    </thead>
                </table>
            </div>
        </div>
    </div>
</div>
<g:if test="${grails.util.Holders.config.pv.plugin.dictionary.enabled}">
    <input type="hidden" id="editable" value="true">
    <g:render template="/plugin/dictionary/dictionaryModals" plugin="pv-dictionary"
              model="[filtersMapList: Holders.config.product.dictionary.filtersMapList, viewsMapList:Holders.config.product.dictionary.viewsMapList]"/>
</g:if>
<g:else>
    <g:render template="/includes/modals/product_selection_modal"/>
</g:else>
<g:render template="/includes/modals/alert_comment_modal" />
<g:render template="/configuration/copyPasteModal" />
<g:render template="/includes/modals/alert_request_by_modal" />
<g:render template="/includes/modals/addAssessmentNotesModal"/>
<g:if test="${Holders.config.pv.plugin.dictionary.enabled}">
    <script>
        var pvaUrls = {
            selectUrl: options.product.selectUrl,
            preLevelParentsUrl: options.product.preLevelParentsUrl,
            searchUrl: options.product.searchUrl
        };
        var otherUrls = {
            selectUrl: "${createLink(controller: 'pvsProductDictionary', action: 'getSelectedProduct')}",
            preLevelParentsUrl: "${createLink(controller: 'pvsProductDictionary', action: 'getPreLevelProductParents')}",
            searchUrl: "${createLink(controller: 'pvsProductDictionary', action: 'searchProducts')}"
        };
        changeDataSource("${businessConfiguration?.dataSource?:'pva'}");
    </script>
</g:if>
</body>