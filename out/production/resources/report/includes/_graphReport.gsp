<%@ page import="grails.util.Holders; com.rxlogix.util.DateUtil; com.rxlogix.enums.DateRangeEnum; com.rxlogix.util.ViewHelper" %>

<g:if test="${!Holders.config.pv.plugin.dictionary.enabled}">
    <asset:stylesheet src="dictionaries.css"/>
</g:if>
<script>
    var DATE_DISPLAY = "MM/DD/YYYY";
    $(document).ready(function () {
        $('input[type="radio"]').click(function () {
            var inputValue = $(this).attr("value");
            var targetBox = $("." + inputValue);
            $(".select").not(targetBox).hide();
            $(targetBox).show();
        });

        $(".dateRangeEnumClass").change(function () {
            var valueChanged = $(this).val();
            if (valueChanged === 'CUSTOM') {
                $('.dateRangeSelector').show();
            } else if (valueChanged === 'CUMULATIVE') {
                $('.dateRangeSelector').hide();
            }
        })
    });
</script>
<div class="rxmain-container-content">
<g:select id="selectedDatasource" name="selectedDatasource"
          from="${datasources?.entrySet()}"
          optionKey="key" optionValue="value"
          class="form-control selectedDatasourceSignal" style="display: none"/>
</div>
<div id="reportFilter" class="row">
    <div class="col-md-2">
        <div class="panel panel-default">
            <div class="panel-body">
                <label>Data Source</label>
                <g:select name="dataSource" from="${datasources?.entrySet()}"
                          optionKey="key"
                          optionValue="value"
                          value="${preSelectedData['selectedDataSource']?.toLowerCase()}"
                          class="form-control selectedDatasource" required="required"/>
            </div>
        </div>
    </div>

    <div class="col-md-4">
        <div class="panel panel-default">
            <div class="panel-body">
                <div class="row">
                    <div class="col-md-12 select product">
                        <label>
                            <g:message code="app.label.productSelection"/>
                            <span class="required-indicator">*</span>
                        </label>

                        <div class="wrapper">
                            <div id="showProductSelection" class="showDictionarySelection">
                                <g:if test="${preSelectedData['productName']}">
                                    <div style="padding: 5px">${preSelectedData['productName']}</div>
                                </g:if>
                            </div>

                            <div class="iconSearch">
                                <i class="fa fa-search productRadio" data-toggle="modal"
                                   data-target="#productModal" tabindex="0"></i>
                            </div>
                        </div>
                        <g:hiddenField name="productSelection" value="${preSelectedData['productSelection']}"
                                       class="productSelection"/>
                        <g:hiddenField name="isMultiIngredient" id="isMultiIngredient" value="false"/>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="col-md-6">
        <div class="panel panel-default">
            <div class="panel-body">
                <div class="row">
                    <div class="col-md-9">
                        <div class="row">
                            <div class="col-md-12">
                                <label>Report Type</label>
                                <g:select id="reportType" name="reportType" from="${reportTypes}"
                                          optionValue="value"
                                          class="form-control"/>
                                <br/>
                                <input placeholder="Enter the name of memo report" name="memoReportName"
                                       class="form-control hide" id="memo-report-name" style="width: 514px;"/>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-md-12">
                                <div class="form-group">
                                    <label>
                                        Report Name
                                        <span class="required-indicator">*</span>
                                    </label>
                                    <input type="text" class="form-control" id="reportName"
                                           placeholder="Report Name">
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="col-md-3">
                        <div class="col-xs-12">
                            <label><g:message code="app.label.DateRange"/></label>
                            <g:select id="dateRangeEnum" name="dateRangeEnum"
                                      from="${ViewHelper.getEudraDateRange()}"
                                      optionValue="display"
                                      optionKey="name" value="${DateRangeEnum.CUSTOM}"
                                      class="form-control dateRangeEnumClass"/>
                        </div>

                        <div class="col-xs-12">

                            <div class="fuelux datePickerParentDiv">
                                <div class="text datepicker fromDateChanged datePickerFromDiv dateRangeSelector"
                                     id="datePickerFromDiv">
                                    <g:message code="app.dateFilter.from"/>
                                    <div class="input-group">
                                        <g:hiddenField value=""
                                                       name="dateRangeStartAbsolute"
                                                       id="dateRangeStartAbsolute"
                                                       class="form-control"/>
                                        <input placeholder="${message(code: 'scheduler.startDate')}"
                                               name="dateRangeStart"
                                               class="form-control" id="dateRangeStart" type="text"
                                        />
                                        <g:render template="/includes/widgets/datePickerTemplate"/>
                                    </div>
                                </div>

                                <div class="toolbarInline datepicker toDateChanged datePickerToDiv dateRangeSelector"
                                     id="datePickerToDiv">
                                    <g:message code="app.dateFilter.to"/>
                                    <div class="input-group">
                                        <g:hiddenField value=""
                                                       name="dateRangeEndAbsolute" id="dateRangeEndAbsolute"
                                                       class="form-control"/>
                                        <input placeholder="${message(code: 'select.end.date')}"
                                               name="dateRangeEnd"
                                               class="form-control" id="dateRangeEnd" type="text"/>
                                        <g:render template="/includes/widgets/datePickerTemplate"/>
                                    </div>
                                </div>

                            </div>
                        </div>
                        <br>
                    </div>
                </div>
            </div>
        </div>

        <div class="row">

            <div class="row pull-right" style="margin-right:5px ">
                <span id="report-generating" style=" font-size:20px"
                      class="fa fa-spinner fa-spin hide"></span>
                <span class="button">
                    <button type="button" role="button"
                            class="generate-graph-report dropdown-toggle btn btn-primary waves-effect"
                            aria-haspopup="true" aria-expanded="true">Generate</button>
                </span>

            </div>
        </div>
    </div>


</div>
<br/>
<br/>

<div class="pv-tab hide graphReport">
    <input type="hidden" id="reportHistoryId" value=""/>
    <!-- Nav tabs -->
    <ul id="detail-tabs" class="validation-tab m-b-5 p-0" role="tablist">
        <li role="presentation" class="active">
            <a href="#individualCases" aria-controls="details" role="tab"
               data-toggle="tab">Number of Individual Cases</a>
        </li>
        <li role="presentation">
            <a href="#individualCasesByReactionGroup" aria-controls="details" role="tab"
               data-toggle="tab">Number of Individual Cases By Reaction Group</a>
        </li>
        <li role="presentation">
            <a href="#reactionGroup" aria-controls="details" role="tab"
               data-toggle="tab">Number of Cases by Reaction Group</a>
        </li>
        <li role="presentation">
            <a href="#reaction" aria-controls="details" role="tab" data-toggle="tab">Number of Cases by Reaction</a>
        </li>
    </ul>

    <!-- Tab panes -->
    <div class="tab-content">
        <div role="tabpanel" class="tab-pane active" id="individualCases">
            <g:render template="includes/individualCases" model="[]"/>
        </div>

        <div role="tabpanel" class="tab-pane fade" id="individualCasesByReactionGroup">
            <g:render template="includes/individualCasesByReactionGroup" model="[socList: socList]"/>
        </div>

        <div role="tabpanel" class="tab-pane fade" id="reactionGroup">
            <g:render template="includes/reactionGroup" model="[socList: socList]"/>
        </div>

        <div role="tabpanel" class="tab-pane fade" id="reaction">
            <g:render template="includes/reaction" model="[ptList: ptList, socList: socList]"/>
        </div>

    </div>
</div>

<div class="zoom" style="display: none;" id="saveICSRs">
    <a class="zoom-fab zoom-btn-large"><asset:image src="save-icon.png" class="pdf-icon" height="24" width="24"/></a>
</div>

<asset:javascript src="app/pvs/common/rx_common.js"/>
<g:if test="${!Holders.config.pv.plugin.dictionary.enabled}">
    <asset:javascript src="app/pvs/alert_utils/alert_product_utils.js"/>
</g:if>
<g:else>
    <asset:javascript src="app/pvs/alert_utils/dictionary-utils.js"/>
    <asset:javascript src="app/pvs/alert_utils/multi_datasource_dictionary.js"/>
</g:else>
<asset:javascript src="app/pvs/report/graphReport.js"/>
<asset:javascript src="app/pvs/report/graph.js"/>
<asset:javascript src="app/pvs/report/report_history.js"/>
<asset:javascript src="fab/fab.js"/>
<g:if test="${Holders.config.pv.plugin.dictionary.enabled}">
    <g:render template="/plugin/dictionary/dictionaryModals" plugin="pv-dictionary"
              model="[filtersMapList: Holders.config.product.dictionary.filtersMapList, viewsMapList: grails.util.Holders.config.product.dictionary.viewsMapList,isPVCM: isPVCM]"/>
    <input type="hidden" value="true" id="disableOldDictionaryInit">
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
        changeDataSource("pva");
    </script>
</g:if>
<g:else>
    <g:render template="/includes/modals/product_selection_modal"/>
</g:else>
<g:javascript>
var reportsData
    <g:if test="${historicData}">
    reportsData = JSON.parse("${historicData}");
</g:if>
</g:javascript>
<script>
    $("#productGroups").select2();
    $(document).ready(function () {
        signal.graph.bindReportScreenEvents();
        signal.reportHistory.init_report_history_table(reportHistoryUrl);
        if (reportsData) {
            signal.graph.showGraph(reportsData);
        }
        setInterval(signal.reportHistory.refresh_report_history_table, 30000);
    });
    $(document).ready(function() {
        $("#selectedDatasource").select2({
            multiple: true
        });
        $('#selectedDatasource').next(".select2-container").hide();
        $("#dataSource").on('change', function () {
            $('#selectedDatasource').val($(this).val());
            $('#dataSourcesProductDict').val($(this).val()).trigger('change.select2');
            if($(this).val() == 'eudra') {
                $("#dataSourcesProductDict").val("eudra").trigger('change.select2');
                disableDictionaryValues(true, false, true, true, true)
            }
        });

    });
</script>