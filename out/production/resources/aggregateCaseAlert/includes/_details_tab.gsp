<%@ page import="com.rxlogix.Constants; com.rxlogix.enums.ReportFormat; grails.util.Holders" %>

<script>
    $(document).ready(function () {
        $("#statistical-comparison").click(function () {
            location.href = statComparisonUrl;
        });
        $('.search-box').show();
    });
    $('.bookmarks-list-opener').hover(function () {
        $('.dropdown-toggle', this).trigger('click');
    });
</script>

<style>
    @media only screen and (max-width: 1280px) and (min-width: 1125px) {
        .bookmarkstrip {
            margin-top: -16px !important;
        }
    }

    table.dataTable thead > tr > th {
        padding-left: 5px;
        padding-right: 5px;
    }

    div.dataTables_wrapper {
        margin: 0 auto;
    }

    .stacked-cell-center-top, .stacked-cell-center-bottom {
        text-align: center !important;
        white-space: nowrap;
    }
</style>

<div class="row">
    <g:render template="/includes/widgets/widget_tab"
              model="[id                           : id, name: name, reportUrl: reportUrl, isPVAEnabled: isPVAEnabled, analysisFileUrl: analysisFileUrl, callingScreen: callingScreen,
                      alertDispositionList         : alertDispositionList, freqNames: freqNames, dateRange: dateRange, viewsList: viewsList, viewId: viewId, selectedDatasource: selectedDatasource, spotFireFiles: spotFireFiles,
                      currentAnalysisStatus        : currentAnalysisStatus, cumulativeAnalysisStatus: cumulativeAnalysisStatus,
                      cumulativeAnalysisStatusFaers: cumulativeAnalysisStatusFaers, isVaersEnabled: isVaersEnabled, isFaersAvailable: isFaersAvailable, isVigibaseEnabled: isVigibaseEnabled,
                      currentAnalysisStatusFaers   : currentAnalysisStatusFaers, isVaersAvailable: isVaersAvailable]"/>
    <input type="hidden" name="labelConfigJson" id="labelConfigJson" value="${labelConfigJson}">
    <input type="hidden" name="labelConfigNew" id="labelConfigNew" value="${labelConfigNew}">
    <input type="hidden" name="labelConfig" id="labelConfig" value="${labelConfig}">
    <input type="hidden" name="labelConfigCopy" id="labelConfigCopy" value="${labelConfigCopy}">
    <input type="hidden" name="labelConfigCopyJson" id="labelConfigCopyJson" value="${labelConfigCopy as grails.converters.JSON}">
    <input type="hidden" name="labelConfigKeyId" id="labelConfigKeyId" value="${labelConfigKeyId as grails.converters.JSON}">
    <input type="hidden" name="hyperlinkConfiguration" id="hyperlinkConfiguration" value="${hyperlinkConfiguration as grails.converters.JSON}">


    <!-- Division of alert review data table -->
     <div class="col-md-12">
         <table id="alertsDetailsTable" class="auto-scale row-border no-shadow hover">
           <thead>
            <tr id="alertsDetailsTableRow">
                <th data-idx="0" data-field="selected">
                    <span class="select-all-check"><input id="select-all" type="checkbox"></span>

                    <div class="th-label"></div>
                </th>
                <th data-idx="3" data-field="dropdown">
                    <div class="th-label"></div>
                </th>
                <g:if test="${isJaderAvailable && callingScreen != Constants.Commons.DASHBOARD}">
                    <g:if test="${isPriorityEnabled}">
                        <th data-idx="4" data-field="priority">
                            <div class="th-label" data-field="priority">P</div>
                        </th>
                    </g:if>
                    <g:render template="includes/jader_details_tab"
                              model="[jaderColumnList: jaderColumnList,groupBySmq:groupBySmq,prevColCount:prevColCount,listDateRange:listDateRange]"/>
                </g:if>
                <g:else>
                    <g:if test="${callingScreen == Constants.Commons.TRIGGERED_ALERTS && labelConfigCopy.name}">
                        <th data-idx="1" data-field="name">
                            <div class="th-label" data-field="name">${labelConfig.name}</div>
                        </th>
                    </g:if>
                    <g:if test="${isPriorityEnabled}">
                        <th data-idx="4" data-field="priority">
                            <div class="th-label" data-field="priority">${labelConfig.priority}</div>
                        </th>
                    </g:if>

                    <th data-idx="5" data-field="actions">
                        <div class="th-label" data-field="actions">${labelConfig.actions}</div>
                    </th>
                    <g:if test="${callingScreen == Constants.Commons.DASHBOARD && labelConfigCopy.name}">
                        <th data-idx="1" data-field="name">
                            <div class="th-label" data-field="name">${labelConfig.name}</div>
                        </th>
                    </g:if>
                    <g:if test="${labelConfigCopy.productName}">
                        <th data-idx="7" data-field="productName">
                            <div class="th-label" data-field="productName">${labelConfig.productName}</div>
                        </th>
                    </g:if>
                    <g:if test="${!groupBySmq && labelConfigCopy.soc}">
                        <th data-idx="8" data-field="soc">
                            <div class="th-label" data-field="soc">${labelConfig.soc}</div>
                        </th>
                    </g:if>
                    <g:if test="${groupBySmq && labelConfigCopy.pt}">
                        <th data-idx="9" data-field="pt" class="col-min-200">
                            <div class="th-label" data-field="pt">${labelConfig.pt.split("#OR")[1]}</div>
                        </th>
                    </g:if>
                    <g:else>
                        <g:if test="${!groupBySmq && labelConfigCopy.pt}">
                            <th data-idx="10" data-field="pt" class="col-min-200">
                                <div class="th-label" data-field="pt">${labelConfig.pt.split("#OR")[0]}</div>
                            </th>
                        </g:if>
                    </g:else>


                    <g:if test="${labelConfigCopy.alertTags}">
                        <th data-idx="6" data-field="alertTags">
                            <div class="th-label" data-field="alertTags">${labelConfig.alertTags}</div>
                        </th>
                    </g:if>
                    <g:if test="${labelConfigCopy.impEvents}">
                        <th data-idx="11" data-field="impEvents">
                            <div class="th-label">${labelConfig.impEvents}</div>
                        </th>
                    </g:if>



                    <g:if test="${!groupBySmq && labelConfigCopy.listed}">
                        <th data-idx="12" data-field="listed">
                            <div class="th-label" data-field="listed">
                                ${labelConfig.listed}
                            </div>
                        </th>
                    </g:if>

                    <g:if test="${labelConfigCopy.newCount}">
                        <th data-idx="37" data-field="newCount">
                            <div class="th-label dateRange" data-field="newCount">
                                <div class="stacked-cell-center-top">
                                    ${labelConfig.newCount.split("/")[0]}
                                </div>

                                <div class="stacked-cell-center-bottom">
                                    ${labelConfig.newCount.split("/")[1]}
                                </div>
                            </div>
                        </th>
                    </g:if>

                    <g:if test="${isFaersEnabled && labelConfigCopy.newCountFaers}">
                        <th data-field="newCountFaers">
                            <div class="th-label dateRange" data-field="newCountFaers">
                                <div class="stacked-cell-center-top">
                                    ${labelConfig.newCountFaers.split("/")[0]}
                                </div>

                                <div class="stacked-cell-center-bottom">
                                    ${labelConfig.newCountFaers.split("/")[1]}
                                </div>
                            </div>
                        </th>
                    </g:if>

                    <g:if test="${isEvdasEnabled && labelConfigCopy.newEvEvdas}">
                        <th data-field="newEvEvdas">
                            <div class="th-label dateRange" data-field="newEvEvdas">
                                <div class="stacked-cell-center-top">
                                    ${labelConfig.newEvEvdas.split("/")[0]}
                                </div>

                                <div class="stacked-cell-center-bottom">
                                    ${labelConfig.newEvEvdas.split("/")[1]}
                                </div>
                            </div>
                        </th>
                    </g:if>
                    <g:if test="${labelConfigCopy.newSponCount}">
                        <th data-idx="13" data-field="newSponCount">
                            <div class="th-label dateRange" data-field="newSponCount">
                                <div class="stacked-cell-center-top">
                                    ${labelConfig.newSponCount.split("/")[0]}
                                </div>

                                <div class="stacked-cell-center-bottom">
                                    ${labelConfig.newSponCount.split("/")[1]}
                                </div>
                            </div>
                        </th>
                    </g:if>
                    <g:if test="${labelConfigCopy.newSeriousCount}">
                        <th data-idx="14" data-field="newSeriousCount">
                            <div class="th-label dateRange" data-field="newSeriousCount">
                                <div class="stacked-cell-center-top">
                                    ${labelConfig.newSeriousCount.split("/")[0]}
                                </div>

                                <div class="stacked-cell-center-bottom">
                                    ${labelConfig.newSeriousCount.split("/")[1]}
                                </div>
                            </div>
                        </th>
                    </g:if>
                    <g:if test="${isFaersEnabled && labelConfigCopy.newSeriousCountFaers}">
                        <th data-field="newSeriousCountFaers">
                            <div class="th-label dateRange" data-field="newSeriousCountFaers">
                                <div class="stacked-cell-center-top">
                                    ${labelConfig.newSeriousCountFaers.split("/")[0]}
                                </div>

                                <div class="stacked-cell-center-bottom">
                                    ${labelConfig.newSeriousCountFaers.split("/")[1]}
                                </div>
                            </div>
                        </th>
                    </g:if>

                    <g:if test="${isEvdasEnabled}">
                        <g:if test="${labelConfigCopy.dmeImeEvdas}">
                            <th data-field="dmeImeEvdas">
                                <div class="th-label" data-field="dmeImeEvdas">
                                    ${labelConfig.dmeImeEvdas}
                                </div>
                            </th>
                        </g:if>

                        <g:if test="${labelConfigCopy.sdrEvdas}">
                            <th data-field="sdrEvdas">
                                <div class="th-label dateRange" data-field="sdrEvdas">
                                    <div class="stacked-cell-center-top">${labelConfig.sdrEvdas}</div>
                                </div>
                            </th>
                        </g:if>
                        <g:if test="${labelConfigCopy.rorValueEvdas}">
                            <th data-field="rorValueEvdas">
                                <div class="th-label dateRange" data-field="rorValueEvdas">
                                    <div class="stacked-cell-center-top">
                                        ${labelConfig.rorValueEvdas}
                                    </div>
                                </div>
                            </th>
                        </g:if>
                    </g:if>

                    <g:if test="${labelConfigCopy.newFatalCount}">
                        <th data-idx="15" data-field="newFatalCount">
                            <div class="th-label dateRange" data-field="newFatalCount">
                                <div class="stacked-cell-center-top">
                                    ${labelConfig.newFatalCount.split("/")[0]}
                                </div>

                                <div class="stacked-cell-center-bottom">
                                    ${labelConfig.newFatalCount.split("/")[1]}
                                </div>
                            </div>
                        </th>
                    </g:if>

                    <g:if test="${labelConfigCopy.newStudyCount}">
                        <th data-idx="16" data-field="newStudyCount">
                            <div class="th-label dateRange" data-field="newStudyCount">
                                <div class="stacked-cell-center-top">
                                    ${labelConfig.newStudyCount.split("/")[0]}
                                </div>

                                <div class="stacked-cell-center-bottom">
                                    ${labelConfig.newStudyCount.split("/")[1]}
                                </div>
                            </div>
                        </th>
                    </g:if>

                    <g:if test="${labelConfigCopy.freqPriority}">
                        <th data-idx="17" data-field="freqPriority">
                            <div class="th-label" data-field="freqPriority">${labelConfig.freqPriority}</div>
                        </th>
                    </g:if>

                    <g:if test="${labelConfigCopy.trendType}">
                        <th data-idx="18" data-field="trendType">
                            <div class="th-label" data-field="trendType">${labelConfig.trendType}</div>
                        </th>
                    </g:if>
                    <g:if test="${showPrr && labelConfigCopy.prrValue}">
                        <th data-idx="19" data-field="prrValue">
                            <div class="th-label dateRange" data-field="prrValue">
                                ${labelConfig.prrValue}
                            </div>
                        </th>
                    </g:if>

                    <g:if test="${showRor && labelConfigCopy.rorValue}">
                        <th data-idx="20" data-field="rorValue">
                            <div class="th-label dateRange" data-field="rorValue">
                                <g:if test="${isRor}">
                                    ${labelConfig.rorValue.split("#OR")[0]}
                                </g:if>
                                <g:else>
                                    ${labelConfig.rorValue.split("#OR")[1]}
                                </g:else>
                            </div>
                        </th>
                    </g:if>




                    <g:if test="${showEbgm && labelConfigCopy.eb05}">
                        <th data-idx="21" data-field="eb05">
                            <div class="th-label dateRange" data-field="eb05">
                                <div class="stacked-cell-center-top">
                                    ${labelConfig.eb05.split("/")[0]}
                                </div>

                                <div class="stacked-cell-center-top">
                                    ${labelConfig.eb05.split("/")[1]}
                                </div>
                            </div>
                        </th>
                    </g:if>
                    <g:if test="${isFaersEnabled && showEbgmFaers && labelConfigCopy.eb05Faers}">
                        <th data-idx="21" data-field="eb05Faers">
                            <div class="th-label dateRange" data-field="eb05Faers">
                                <div class="stacked-cell-center-top">
                                    ${labelConfig.eb05Faers.split("/")[0]}
                                </div>

                                <div class="stacked-cell-center-top">
                                    ${labelConfig.eb05Faers.split("/")[1]}
                                </div>
                            </div>
                        </th>
                    </g:if>
                    <g:if test="${showPrr && showRor && labelConfigCopy.chiSquare}">
                        <th align="center" data-idx="40" data-field="chiSquare">
                            <div class="th-label col-min-120" data-field="chiSquare">${labelConfig.chiSquare}</div>
                        </th>
                    </g:if>

                    <g:if test="${labelConfigCopy.signalsAndTopics}">
                        <th align="center" data-idx="23" data-field="signalsAndTopics">
                            <div class="th-label" data-field="signalsAndTopics">${labelConfig.signalsAndTopics}</div>
                        </th>
                    </g:if>

                    <g:if test="${labelConfigCopy.disposition}">
                        <th align="center" data-idx="24" data-field="disposition">
                            <div class="th-label" data-field="disposition">${labelConfig.disposition}</div>
                        </th>
                    </g:if>

                    <g:if test="${labelCondition && labelConfigCopy.currentDisposition}">
                        <th align="center" data-idx="36" data-field="currentDisposition">
                            <div class="th-label"
                                 data-field="currentDisposition">${labelConfig.currentDisposition.split("#OR")[0]}</div>
                        </th>
                    </g:if>
                    <g:else>
                        <g:if test="${labelConfigCopy.currentDisposition}">
                            <th align="center" data-idx="36" data-field="currentDisposition">
                                <div class="th-label"
                                     data-field="currentDisposition">${labelConfig.currentDisposition.split("#OR")[1]}</div>
                            </th>
                        </g:if>
                    </g:else>


                    <g:if test="${showDss}">
                        <th data-idx="21" data-field="rationale">
                            <div class="th-label dateRange" data-field="rationale">
                                <g:if test="${isAutoProposed && labelConfigCopy.rationale}">
                                    <div class="stacked-cell-center-top">
                                        ${labelConfig.rationale.split("#OR")[0]}
                                    </div>

                                    <div class="stacked-cell-center-top">
                                        ${labelConfig.rationale.split("#OR")[1]}
                                    </div>
                                </g:if>
                                <g:else>
                                    <g:if test="${labelConfigCopy.rationale}">
                                        <div class="stacked-cell-center-top">
                                            ${labelConfig.rationale.split("#OR")[2]}
                                        </div></g:if>
                                </g:else>
                            </div>
                        </th>
                    </g:if>

                    <g:if test="${showDss && labelConfigCopy.pecImpNumHigh}">
                        <th data-idx="21" data-field="pecImpNumHigh">
                            <div class="th-label dateRange" data-field="pecImpNumHigh">
                                <div class="stacked-cell-center-top">
                                    ${labelConfig.pecImpNumHigh}
                                </div>
                            </div>
                        </th>
                    </g:if>
                    <g:if test="${labelConfigCopy.assignedTo}">
                        <th data-idx="25" data-field="assignedTo">
                            <div class="th-label" data-field="assignedTo">${labelConfig.assignedTo}</div>
                        </th>
                    </g:if>

                    <g:if test="${labelConfigCopy.dueDate}">
                        <th data-idx="26" data-field="dueDate">
                            <div class="th-label" data-field="dueDate">${labelConfig.dueDate}</div>
                        </th>
                    </g:if>




                    <g:if test="${!groupBySmq}">
                        <g:if test="${labelConfigCopy.positiveRechallenge}">
                            <th data-idx="27" data-field="positiveRechallenge">
                                <div class="th-label"
                                     data-field="positiveRechallenge">${labelConfig.positiveRechallenge}</div>
                            </th>
                        </g:if>
                        <g:if test="${labelConfigCopy.positiveDechallenge}">
                            <th data-idx="28" data-field="positiveDechallenge">
                                <div class="th-label"
                                     data-field="positiveDechallenge">${labelConfig.positiveDechallenge}</div>
                            </th>
                        </g:if>
                    </g:if>

                    <g:if test="${showPrr && labelConfigCopy.prrLCI}">
                        <th data-idx="29" data-field="prrLCI">
                            <div class="th-label dateRange" data-field="prrLCI">
                                <div class="stacked-cell-center-top">${labelConfig.prrLCI.split("/")[0]}</div>

                                <div class="stacked-cell-center-top">${labelConfig.prrLCI.split("/")[1]}</div>
                            </div>
                        </th>
                    </g:if>
                    <g:if test="${showRor && labelConfigCopy.rorLCI}">
                        <th data-idx="30" data-field="rorLCI">
                            <div class="th-label dateRange" data-field="rorLCI">
                                <g:if test="${isRor}">
                                    <div class="stacked-cell-center-top">${labelConfig.rorLCI.split("#OR")[0].split("/")[0]}</div>

                                    <div class="stacked-cell-center-top">${labelConfig.rorLCI.split("#OR")[0].split("/")[1]}</div>
                                </g:if>
                                <g:else>
                                    <div class="stacked-cell-center-top">${labelConfig.rorLCI.split("#OR")[1].split("/")[0]}</div>

                                    <div class="stacked-cell-center-top">${labelConfig.rorLCI.split("#OR")[1].split("/")[1]}</div>
                                </g:else>
                            </div>
                        </th>
                    </g:if>



                    <g:if test="${showEbgm && labelConfigCopy.ebgm}">
                        <th data-idx="31" data-field="ebgm">
                            <div class="th-label dateRange" data-field="ebgm">
                                ${labelConfig.ebgm}
                            </div>
                        </th>
                    </g:if>

                    <g:if test="${!groupBySmq}">
                        <g:if test="${labelConfigCopy.related}">
                            <th data-idx="349" data-field="related">
                                <div class="th-label" data-field="related">${labelConfig.related}</div>
                            </th>
                        </g:if>
                        <g:if test="${labelConfigCopy.pregenency}">
                            <th data-idx="35" data-field="pregenency">
                                <div class="th-label" data-field="pregenency">${labelConfig.pregenency}</div>
                            </th>
                        </g:if>
                    </g:if>
                    <g:if test="${labelConfigCopy.newPediatricCount}">
                        <th data-idx="38" data-field="newPediatricCount">
                            <div class="th-label dateRange" data-field="newPediatricCount">
                                <div class="stacked-cell-center-top">
                                    ${labelConfig.newPediatricCount.split("/")[0]}
                                </div>

                                <div class="stacked-cell-center-bottom">
                                    ${labelConfig.newPediatricCount.split("/")[1]}
                                </div>
                            </div>
                        </th>
                    </g:if>

                    <g:if test="${labelConfigCopy.newInteractingCount}">
                        <th data-idx="39" data-field="newInteractingCount">
                            <div class="th-label dateRange" data-field="newInteractingCount">
                                <div class="stacked-cell-center-top">
                                    ${labelConfig.newInteractingCount.split("/")[0]}
                                </div>

                                <div class="stacked-cell-center-bottom">
                                    ${labelConfig.newInteractingCount.split("/")[1]}
                                </div>
                            </div>
                        </th>
                    </g:if>
                    <g:if test="${labelConfigCopy.newGeriatricCount}">
                        <th data-idx="350" data-field="newGeriatricCount">
                            <div class="th-label dateRange" data-field="newGeriatricCount">
                                <div class="stacked-cell-center-top">
                                    ${labelConfig.newGeriatricCount.split("/")[0]}
                                </div>

                                <div class="stacked-cell-center-bottom">
                                    ${labelConfig.newGeriatricCount.split("/")[1]}
                                </div>
                            </div>
                        </th>
                    </g:if><g:if test="${labelConfigCopy.newNonSerious}">
                    <th data-idx="351" data-field="newNonSerious">
                        <div class="th-label dateRange" data-field="newNonSerious">
                            <div class="stacked-cell-center-top">
                                ${labelConfig.newNonSerious.split("/")[0]}
                            </div>

                            <div class="stacked-cell-center-bottom">
                                ${labelConfig.newNonSerious.split("/")[1]}
                            </div>
                        </div>
                    </th></g:if>
                    <g:if test="${labelConfigCopy.justification}">
                        <th data-field="justification">
                            <div class="th-label" data-field="justification">${labelConfig.justification}</div>
                        </th>
                    </g:if>
                    <g:if test="${labelConfigCopy.dispPerformedBy}">
                        <th data-field="dispPerformedBy">
                            <div class="th-label" data-field="dispPerformedBy">${labelConfig.dispPerformedBy}</div>
                        </th>
                    </g:if>
                    <g:if test="${labelConfigCopy.dispLastChange}">
                        <th data-field="dispLastChange">
                            <div class="th-label" data-field="dispLastChange">${labelConfig.dispLastChange}</div>
                        </th>
                    </g:if>
                    <g:if test="${labelConfigCopy.comment}">
                        <th data-field="comment">
                            <div class="th-label" data-field="comment">${labelConfig.comment}</div>
                        </th>
                    </g:if>




                    <g:if test="${labelConfigCopy.trendFlag}">
                        <th data-field="trendFlag">
                            <div class="th-label" data-field="trendFlag">${labelConfig.trendFlag}</div>
                        </th>
                    </g:if>
                    <g:if test="${labelConfigCopy.newProdCount}">
                        <th data-field="newProdCount">
                            <div class="th-label dateRange" data-field="newProdCount">
                                <div class="stacked-cell-center-top">
                                    ${labelConfig.newProdCount.split("/")[0]}
                                </div>

                                <div class="stacked-cell-center-bottom">
                                    ${labelConfig.newProdCount.split("/")[1]}
                                </div>
                            </div>
                        </th>
                    </g:if>
                    <g:if test="${labelConfigCopy.freqPeriod}">
                        <th data-field="freqPeriod">
                            <div class="th-label dateRange" data-field="freqPeriod">
                                <div class="stacked-cell-center-top">
                                    ${labelConfig.freqPeriod.split("/")[0]}
                                </div>

                                <div class="stacked-cell-center-bottom">
                                    ${labelConfig.freqPeriod.split("/")[1]}
                                </div>
                            </div>
                        </th>
                    </g:if>
                    <g:if test="${labelConfigCopy.reviewedFreqPeriod}">
                        <th data-field="reviewedFreqPeriod">
                            <div class="th-label dateRange" data-field="reviewedFreqPeriod">
                                <div class="stacked-cell-center-top">
                                    ${labelConfig.reviewedFreqPeriod.split("/")[0]}
                                </div>

                                <div class="stacked-cell-center-bottom">
                                    ${labelConfig.reviewedFreqPeriod.split("/")[1]}
                                </div>
                            </div>
                        </th>
                    </g:if>
                    <g:if test="${showPrr || showRor || showEbgm}">
                        <g:if test="${labelConfigCopy.aValue}">
                            <th data-field="aValue">
                                <div class="th-label dateRange" data-field="aValue">
                                    ${labelConfig.aValue}
                                </div>
                            </th>
                        </g:if>
                        <g:if test="${labelConfigCopy.bValue}">
                            <th data-field="bValue">
                                <div class="th-label dateRange" data-field="bValue">
                                    ${labelConfig.bValue}
                                </div>
                            </th>
                        </g:if>
                        <g:if test="${labelConfigCopy.cValue}">
                            <th data-field="cValue">
                                <div class="th-label dateRange" data-field="cValue">
                                    ${labelConfig.cValue}
                                </div>
                            </th>
                        </g:if>
                        <g:if test="${labelConfigCopy.dValue}">
                            <th data-field="dValue">
                                <div class="th-label dateRange" data-field="dValue">
                                    ${labelConfig.dValue}
                                </div>
                            </th>
                        </g:if>
                    </g:if>

                    <g:if test="${showEbgm}">
                        <g:if test="${labelConfigCopy.eValue}">
                            <th data-field="eValue">
                                <div class="th-label dateRange" data-field="eValue">
                                    ${labelConfig.eValue}
                                </div>
                            </th>
                        </g:if>
                        <g:if test="${labelConfigCopy.rrValue}">
                            <th data-field="rrValue">
                                <div class="th-label dateRange" data-field="rrValue">
                                    ${labelConfig.rrValue}
                                </div>
                            </th>
                        </g:if>
                    </g:if>

                    <g:if test="${isFaersEnabled && labelConfigCopy.newSponCountFaers}">
                        <th data-field="newSponCountFaers">
                            <div class="th-label dateRange" data-field="newSponCountFaers">
                                <div class="stacked-cell-center-top">
                                    ${labelConfig.newSponCountFaers.split("/")[0]}
                                </div>

                                <div class="stacked-cell-center-bottom">
                                    ${labelConfig.newSponCountFaers.split("/")[1]}
                                </div>
                            </div>
                        </th>
                    </g:if>
                    <g:if test="${labelConfigCopy.newStudyCountFaers}">

                        <th data-field="newStudyCountFaers">
                            <div class="th-label dateRange" data-field="newStudyCountFaers">
                                <div class="stacked-cell-center-top">
                                    ${labelConfig.newStudyCountFaers.split("/")[0]}
                                </div>

                                <div class="stacked-cell-center-bottom">
                                    ${labelConfig.newStudyCountFaers.split("/")[1]}
                                </div>
                            </div>
                        </th>
                    </g:if>
                    <g:if test="${labelConfigCopy.freqPriorityFaers}">
                        <th data-field="freqPriorityFaers">
                            <div class="th-label" data-field="freqPriority">${labelConfig.freqPriorityFaers}</div>
                        </th>
                    </g:if>
                    <g:if test="${!groupBySmq}">
                        <g:if test="${labelConfigCopy.positiveRechallengeFaers}">
                            <th data-field="positiveRechallengeFaers">
                                <div class="th-label"
                                     data-field="positiveRechallengeFaers">${labelConfig.positiveRechallengeFaers}</div>
                            </th>
                        </g:if>
                        <g:if test="${labelConfigCopy.positiveDechallengeFaers}">
                            <th data-field="positiveDechallengeFaers">
                                <div class="th-label"
                                     data-field="positiveDechallengeFaers">${labelConfig.positiveDechallengeFaers}</div>
                            </th>
                        </g:if>
                    </g:if>


                    <g:if test="${showPrrFaers}">
                        <g:if test="${labelConfigCopy.prrValueFaers}">
                            <th data-field="prrValueFaers">
                                <div class="th-label dateRange" data-field="prrValueFaers">
                                    <div>
                                        ${labelConfig.prrValueFaers}
                                    </div>
                                </div>
                            </th>
                        </g:if>
                        <g:if test="${labelConfigCopy.prrLCIFaers}">
                            <th data-field="prrLCIFaers">
                                <div class="th-label dateRange" data-field="prrLCIFaers">
                                    <div>${labelConfig.prrLCIFaers.split("/")[0]}</div>

                                    <div>${labelConfig.prrLCIFaers.split("/")[1]}</div>
                                </div>
                            </th>
                        </g:if>
                    </g:if>
                    <g:if test="${showRorFaers}">
                        <g:if test="${labelConfigCopy.rorValueFaers}">
                            <th data-field="rorValueFaers">
                                <div class="th-label dateRange" data-field="rorValueFaers">
                                    ${labelConfig.rorValueFaers}
                                </div>
                            </th>
                        </g:if>
                        <g:if test="${labelConfigCopy.rorLCIFaers}">
                            <th data-field="rorLCIFaers">
                                <div class="th-label dateRange" data-field="rorLCIFaers">
                                    <div class="stacked-cell-center-top">${labelConfig.rorLCIFaers.split("/")[0]}</div>

                                    <div class="stacked-cell-center-top">${labelConfig.rorLCIFaers.split("/")[1]}</div>
                                </div>
                            </th>
                        </g:if>
                    </g:if>
                    <g:if test="${labelConfigCopy.newPediatricCountFaers}">
                        <th data-field="newPediatricCountFaers">
                            <div class="th-label dateRange" data-field="newPediatricCountFaers">
                                <div class="stacked-cell-center-top">
                                    ${labelConfig.newPediatricCountFaers.split("/")[0]}
                                </div>

                                <div class="stacked-cell-center-bottom">
                                    ${labelConfig.newPediatricCountFaers.split("/")[1]}
                                </div>
                            </div>
                        </th>

                    </g:if>
                    <g:if test="${labelConfigCopy.newInteractingCountFaers}">
                        <th data-field="newInteractingCountFaers">
                            <div class="th-label dateRange" data-field="newInteractingCountFaers">
                                <div class="stacked-cell-center-top">
                                    ${labelConfig.newInteractingCountFaers.split("/")[0]}
                                </div>

                                <div class="stacked-cell-center-bottom">
                                    ${labelConfig.newInteractingCountFaers.split("/")[1]}
                                </div>
                            </div>
                        </th>
                    </g:if>
                    <g:if test="${labelConfigCopy.newFatalCountFaers}">
                        <th data-field="newFatalCountFaers">
                            <div class="th-label dateRange" data-field="newFatalCountFaers">
                                <div class="stacked-cell-center-top">
                                    ${labelConfig.newFatalCountFaers.split("/")[0]}
                                </div>

                                <div class="stacked-cell-center-bottom">
                                    ${labelConfig.newFatalCountFaers.split("/")[1]}
                                </div>
                            </div>
                        </th>
                    </g:if>

                    <g:if test="${showEbgmFaers && labelConfigCopy.ebgmFaers}">
                        <th data-idx="31" data-field="ebgmFaers">
                            <div class="th-label dateRange" data-field="ebgmFaers">
                                ${labelConfig.ebgmFaers}
                            </div>
                        </th>
                    </g:if>

                    <g:if test="${showPrrFaers && showRorFaers && labelConfigCopy.chiSquareFaers}">
                        <th align="center" data-field="chiSquareFaers">
                            <div class="th-label col-min-120" data-field="chiSquareFaers">
                                ${labelConfig.chiSquareFaers}
                            </div>
                        </th>
                    </g:if>

                    <g:if test="${!groupBySmq}">
                        <g:if test="${labelConfigCopy.relatedFaers}">
                            <th data-field="relatedFaers">
                                <div class="th-label" data-field="relatedFaers">${labelConfig.relatedFaers}</div>
                            </th>
                        </g:if>
                        <g:if test="${labelConfigCopy.pregenencyFaers}">
                            <th data-field="pregenencyFaers">
                                <div class="th-label" data-field="pregenencyFaers">${labelConfig.pregenencyFaers}</div>
                            </th></g:if>
                    </g:if>


                    <g:if test="${labelConfigCopy.trendTypeFaers}">
                        <th data-field="trendTypeFaers">
                            <div class="th-label" data-field="trendTypeFaers">
                                ${labelConfig.trendTypeFaers}
                            </div>
                        </th>
                    </g:if>
                    <g:if test="${labelConfigCopy.newGeriatricCountFaers}">
                        <th data-field="newGeriatricCountFaers">
                            <div class="th-label dateRange" data-field="newGeriatricCountFaers">
                                <div class="stacked-cell-center-top">
                                    ${labelConfig.newGeriatricCountFaers.split("/")[0]}
                                </div>

                                <div class="stacked-cell-center-bottom">
                                    ${labelConfig.newGeriatricCountFaers.split("/")[1]}
                                </div>
                            </div>
                        </th>
                    </g:if>
                    <g:if test="${labelConfigCopy.newNonSeriousFaers}">
                        <th data-field="newNonSeriousFaers">
                            <div class="th-label dateRange" data-field="newNonSeriousFaers">
                                <div class="stacked-cell-center-top">
                                    ${labelConfig.newNonSeriousFaers.split("/")[0]}
                                </div>

                                <div class="stacked-cell-center-bottom">
                                    ${labelConfig.newNonSeriousFaers.split("/")[1]}
                                </div>
                            </div>
                        </th>
                    </g:if>


                    <g:if test="${isEvdasEnabled}">
                        <g:if test="${labelConfigCopy.hlgtEvdas}"><th data-field="hlgtEvdas"><div class="th-label"
                                                                                                  data-field="hlgtEvdas">${labelConfig.hlgtEvdas}</div>
                        </th></g:if>
                        <g:if test="${labelConfigCopy.hltEvdas}"><th data-field="hltEvdas"><div class="th-label"
                                                                                                data-field="hltEvdas">${labelConfig.hltEvdas}</div>
                        </th></g:if>
                        <g:if test="${labelConfigCopy.smqNarrowEvdas}"><th data-field="smqNarrowEvdas"><div
                                class="th-label"
                                data-field="smqNarrowEvdas">${labelConfig.smqNarrowEvdas}</div>
                        </th></g:if>
                        <g:if test="${labelConfigCopy.newEeaEvdas}"><th data-field="newEeaEvdas">
                            <div class="th-label dateRange" data-field="newEeaEvdas">
                                <div class="stacked-cell-center-top">
                                    ${labelConfig.newEeaEvdas.split("/")[0]}
                                </div>

                                <div class="stacked-cell-center-bottom">
                                    ${labelConfig.newEeaEvdas.split("/")[1]}
                                </div>
                            </div>
                        </th></g:if>
                        <g:if test="${labelConfigCopy.newHcpEvdas}">
                            <th data-field="newHcpEvdas">
                                <div class="th-label dateRange" data-field="newHcpEvdas">
                                    <div class="stacked-cell-center-top">
                                        ${labelConfig.newHcpEvdas.split("/")[0]}
                                    </div>

                                    <div class="stacked-cell-center-bottom">
                                        ${labelConfig.newHcpEvdas.split("/")[1]}
                                    </div>
                                </div>
                            </th></g:if>
                        <g:if test="${labelConfigCopy.newSeriousEvdas}">
                            <th data-field="newSeriousEvdas">
                                <div class="th-label dateRange" data-field="newSeriousEvdas">
                                    <div class="stacked-cell-center-top">
                                        ${labelConfig.newSeriousEvdas.split("/")[0]}
                                    </div>

                                    <div class="stacked-cell-center-bottom">
                                        ${labelConfig.newSeriousEvdas.split("/")[1]}
                                    </div>
                                </div>
                            </th>
                        </g:if>
                        <g:if test="${labelConfigCopy.newMedErrEvdas}">
                            <th data-field="newMedErrEvdas">
                                <div class="th-label dateRange" data-field="newMedErrEvdas">
                                    <div class="stacked-cell-center-top">
                                        ${labelConfig.newMedErrEvdas.split("/")[0]}
                                    </div>

                                    <div class="stacked-cell-center-bottom">
                                        ${labelConfig.newMedErrEvdas.split("/")[1]}
                                    </div>
                                </div>
                            </th>
                        </g:if>
                        <g:if test="${labelConfigCopy.newObsEvdas}">
                            <th data-field="newObsEvdas">
                                <div class="th-label dateRange" data-field="newObsEvdas">
                                    <div class="stacked-cell-center-top">
                                        ${labelConfig.newObsEvdas.split("/")[0]}
                                    </div>

                                    <div class="stacked-cell-center-bottom">
                                        ${labelConfig.newObsEvdas.split("/")[1]}
                                    </div>
                                </div>
                            </th></g:if>
                        <g:if test="${labelConfigCopy.newFatalEvdas}">
                            <th data-field="newFatalEvdas">
                                <div class="th-label dateRange" data-field="newFatalEvdas">
                                    <div class="stacked-cell-center-top">
                                        ${labelConfig.newFatalEvdas.split("/")[0]}
                                    </div>

                                    <div class="stacked-cell-center-bottom">
                                        ${labelConfig.newFatalEvdas.split("/")[1]}
                                    </div>
                                </div>
                            </th></g:if>
                        <g:if test="${labelConfigCopy.newRcEvdas}">
                            <th data-field="newRcEvdas">
                                <div class="th-label dateRange" data-field="newRcEvdas">
                                    <div class="stacked-cell-center-top">
                                        ${labelConfig.newRcEvdas.split("/")[0]}
                                    </div>

                                    <div class="stacked-cell-center-bottom">
                                        ${labelConfig.newRcEvdas.split("/")[1]}
                                    </div>
                                </div>
                            </th>
                        </g:if>
                        <g:if test="${labelConfigCopy.newLitEvdas}">
                            <th data-field="newLitEvdas">
                                <div class="th-label dateRange" data-field="newLitEvdas">
                                    <div class="stacked-cell-center-top">
                                        ${labelConfig.newLitEvdas.split("/")[0]}
                                    </div>

                                    <div class="stacked-cell-center-bottom">
                                        ${labelConfig.newLitEvdas.split("/")[1]}
                                    </div>
                                </div>
                            </th>
                        </g:if>
                        <g:if test="${labelConfigCopy.newPaedEvdas}">
                            <th data-field="newPaedEvdas">
                                <div class="th-label dateRange" data-field="newPaedEvdas">
                                    <div class="stacked-cell-center-top">
                                        ${labelConfig.newPaedEvdas.split("/")[0]}
                                    </div>

                                    <div class="stacked-cell-center-bottom">
                                        ${labelConfig.newPaedEvdas.split("/")[1]}
                                    </div>
                                </div>
                            </th>
                        </g:if>
                        <g:if test="${labelConfigCopy.ratioRorPaedVsOthersEvdas}">
                            <th data-field="ratioRorPaedVsOthersEvdas">
                                <div class="th-label dateRange" data-field="ratioRorPaedVsOthersEvdas">
                                    ${labelConfig.ratioRorPaedVsOthersEvdas}
                                </div>
                            </th>
                        </g:if>
                        <g:if test="${labelConfigCopy.newGeriaEvdas}">
                            <th data-field="newGeriaEvdas">
                                <div class="th-label dateRange" data-field="newGeriaEvdas">
                                    <div class="stacked-cell-center-top">
                                        ${labelConfig.newGeriaEvdas.split("/")[0]}
                                    </div>

                                    <div class="stacked-cell-center-bottom">
                                        ${labelConfig.newGeriaEvdas.split("/")[1]}
                                    </div>
                                </div>
                            </th>
                        </g:if>
                        <g:if test="${labelConfigCopy.ratioRorGeriatrVsOthersEvdas}">
                            <th data-field="ratioRorGeriatrVsOthersEvdas">
                                <div class="th-label dateRange" data-field="ratioRorGeriatrVsOthersEvdas">
                                    ${labelConfig.ratioRorGeriatrVsOthersEvdas}
                                </div>
                            </th>
                        </g:if>
                        <g:if test="${labelConfigCopy.sdrGeratrEvdas}">
                            <th data-field="sdrGeratrEvdas">
                                <div class="th-label dateRange" data-field="sdrGeratrEvdas">
                                    ${labelConfig.sdrGeratrEvdas}
                                </div>
                            </th>
                        </g:if>
                        <g:if test="${labelConfigCopy.newSpontEvdas}">
                            <th data-field="newSpontEvdas">
                                <div class="th-label dateRange" data-field="newSpontEvdas">
                                    <div class="stacked-cell-center-top">
                                        ${labelConfig.newSpontEvdas.split("/")[0]}
                                    </div>

                                    <div class="stacked-cell-center-bottom">
                                        ${labelConfig.newSpontEvdas.split("/")[1]}
                                    </div>
                                </div>
                            </th>
                        </g:if>
                        <g:if test="${labelConfigCopy.totSpontEuropeEvdas}">
                            <th data-field="totSpontEuropeEvdas">
                                <div class="th-label dateRange" data-field="totSpontEuropeEvdas">
                                    ${labelConfig.totSpontEuropeEvdas}
                                </div>
                            </th>
                        </g:if>
                        <g:if test="${labelConfigCopy.totSpontNAmericaEvdas}">
                            <th data-field="totSpontNAmericaEvdas">
                                <div class="th-label dateRange" data-field="totSpontNAmericaEvdas">
                                    ${labelConfig.totSpontNAmericaEvdas}
                                </div>
                            </th>
                        </g:if>
                        <g:if test="${labelConfigCopy.totSpontJapanEvdas}">
                            <th data-field="totSpontJapanEvdas">
                                <div class="th-label dateRange" data-field="totSpontJapanEvdas">
                                    ${labelConfig.totSpontJapanEvdas}
                                </div>
                            </th>
                        </g:if>
                        <g:if test="${labelConfigCopy.totSpontAsiaEvdas}">
                            <th data-field="totSpontAsiaEvdas">
                                <div class="th-label dateRange" data-field="totSpontAsiaEvdas">
                                    ${labelConfig.totSpontAsiaEvdas}
                                </div>
                            </th>
                        </g:if>
                        <g:if test="${labelConfigCopy.totSpontRestEvdas}">
                            <th data-field="totSpontRestEvdas">
                                <div class="th-label dateRange" data-field="totSpontRestEvdas">
                                    ${labelConfig.totSpontRestEvdas}
                                </div>
                            </th>
                        </g:if>
                        <g:if test="${labelConfigCopy.newSeriousEvdas}">
                            <th data-field="sdrPaedEvdas">
                                <div class="th-label dateRange" data-field="sdrPaedEvdas">
                                    ${labelConfig.sdrPaedEvdas}
                                </div>
                            </th>
                        </g:if>
                        <g:if test="${labelConfigCopy.europeRorEvdas}">
                            <th data-field="europeRorEvdas">
                                <div class="th-label dateRange" data-field="europeRorEvdas">
                                    ${labelConfig.europeRorEvdas}
                                </div>
                            </th>
                        </g:if>
                        <g:if test="${labelConfigCopy.northAmericaRorEvdas}">
                            <th data-field="northAmericaRorEvdas">
                                <div class="th-label dateRange" data-field="northAmericaRorEvdas">
                                    ${labelConfig.northAmericaRorEvdas}
                                </div>
                            </th>
                        </g:if>
                        <g:if test="${labelConfigCopy.japanRorEvdas}">
                            <th data-field="japanRorEvdas">
                                <div class="th-label dateRange" data-field="japanRorEvdas">
                                    ${labelConfig.japanRorEvdas}
                                </div>
                            </th>
                        </g:if>
                        <g:if test="${labelConfigCopy.asiaRorEvdas}">
                            <th data-field="asiaRorEvdas">
                                <div class="th-label dateRange" data-field="asiaRorEvdas">
                                    ${labelConfig.asiaRorEvdas}
                                </div>
                            </th>
                        </g:if>
                        <g:if test="${labelConfigCopy.restRorEvdas}">
                            <th data-field="restRorEvdas">
                                <div class="th-label dateRange" data-field="restRorEvdas">
                                    ${labelConfig.restRorEvdas}
                                </div>
                            </th>
                        </g:if>
                        <g:if test="${labelConfigCopy.changesEvdas}">
                            <th data-field="changesEvdas">
                                <div class="th-label dateRange" data-field="changesEvdas">
                                    ${labelConfig.changesEvdas}
                                </div>
                            </th>
                        </g:if>
                    </g:if>

                    <g:if test="${isVaersEnabled}">
                        <g:if test="${labelConfigCopy.newCountVaers}">
                            <th data-field="newCountVaers">
                                <div class="th-label dateRange" data-field="newCountVaers">
                                    <div class="stacked-cell-center-top">
                                        ${labelConfig.newCountVaers.split("/")[0]}
                                    </div>

                                    <div class="stacked-cell-center-bottom">
                                        ${labelConfig.newCountVaers.split("/")[1]}
                                    </div>
                                </div>
                            </th>
                        </g:if>
                        <g:if test="${labelConfigCopy.newSeriousCountVaers}">
                            <th data-field="newSeriousCountVaers">
                                <div class="th-label dateRange" data-field="newSeriousCountVaers">
                                    <div class="stacked-cell-center-top">
                                        ${labelConfig.newSeriousCountVaers.split("/")[0]}
                                    </div>

                                    <div class="stacked-cell-center-bottom">
                                        ${labelConfig.newSeriousCountVaers.split("/")[1]}
                                    </div>
                                </div>
                            </th>
                        </g:if>
                        <g:if test="${showEbgmVaers && labelConfigCopy.eb05Vaers}">
                            <th data-idx="21" data-field="eb05Vaers">
                                <div class="th-label dateRange" data-field="eb05Vaers">
                                    <div class="stacked-cell-center-top">
                                        ${labelConfig.eb05Vaers.split("/")[0]}
                                    </div>

                                    <div class="stacked-cell-center-top">
                                        ${labelConfig.eb05Vaers.split("/")[1]}
                                    </div>
                                </div>
                            </th>
                        </g:if>
                        <g:if test="${labelConfigCopy.newFatalCountVaers}">
                            <th data-field="newFatalCountVaers">
                                <div class="th-label dateRange" data-field="newFatalCountVaers">
                                    <div class="stacked-cell-center-top">
                                        ${labelConfig.newFatalCountVaers.split("/")[0]}
                                    </div>

                                    <div class="stacked-cell-center-bottom">
                                        ${labelConfig.newFatalCountVaers.split("/")[1]}
                                    </div>
                                </div>
                            </th>
                        </g:if>
                        <g:if test="${labelConfigCopy.newGeriatricCountVaers}">
                            <th data-field="newGeriatricCountVaers">
                                <div class="th-label dateRange" data-field="newGeriatricCountVaers">
                                    <div class="stacked-cell-center-top">
                                        ${labelConfig.newGeriatricCountVaers.split("/")[0]}
                                    </div>

                                    <div class="stacked-cell-center-bottom">
                                        ${labelConfig.newGeriatricCountVaers.split("/")[1]}
                                    </div>
                                </div>
                            </th>
                        </g:if>
                        <g:if test="${labelConfigCopy.newPediatricCountVaers}">
                            <th data-field="newPediatricCountVaers">
                                <div class="th-label dateRange" data-field="newPediatricCountVaers">
                                    <div class="stacked-cell-center-top">
                                        ${labelConfig.newPediatricCountVaers.split("/")[0]}
                                    </div>

                                    <div class="stacked-cell-center-bottom">
                                        ${labelConfig.newPediatricCountVaers.split("/")[1]}
                                    </div>
                                </div>
                            </th></g:if>

                        <g:if test="${showPrrVaers && labelConfigCopy.prrValueVaers}">
                            <th data-field="prrValueVaers">
                                <div class="th-label dateRange" data-field="prrValueVaers">
                                    <div>
                                        ${labelConfig.prrValueVaers}
                                    </div>
                                </div>
                            </th>
                        </g:if>

                        <g:if test="${showRorVaers && labelConfigCopy.rorValueVaers}">
                            <th data-field="rorValueVaers">
                                <div class="th-label dateRange" data-field="rorValueVaers">
                                    ${labelConfig.rorValueVaers}
                                </div>
                            </th>
                        </g:if>


                        <g:if test="${showEbgmVaers && labelConfigCopy.ebgmVaers}">
                            <th data-idx="31" data-field="ebgmVaers">
                                <div class="th-label dateRange" data-field="ebgmVaers">
                                    ${labelConfig.ebgmVaers}
                                </div>
                            </th>
                        </g:if>

                        <g:if test="${showPrrVaers && labelConfigCopy.prrLCIVaers}">
                            <th data-field="prrLCIVaers">
                                <div class="th-label dateRange" data-field="prrLCIVaers">
                                    ${labelConfig.prrLCIVaers.split("/")[0]}

                                    ${labelConfig.prrLCIVaers.split("/")[1]}
                                </div>
                            </th>
                        </g:if>

                    <g:if test="${showRorVaers && labelConfigCopy.rorLCIVaers}">
                        <th data-field="rorLCIVaers">
                            <div class="th-label dateRange" data-field="rorLCIVaers">
                                <div class="stacked-cell-center-top">${labelConfig.rorLCIVaers.split("/")[0]}</div>

                                <div class="stacked-cell-center-top">${labelConfig.rorLCIVaers.split("/")[1]}</div>
                            </div>
                        </th>
                    </g:if>

                        <g:if test="${showPrrVaers && showRorVaers && labelConfigCopy.chiSquareVaers}">
                            <th align="center" data-field="chiSquareVaers">
                                <div class="th-label col-min-120" data-field="chiSquareVaers">
                                    ${labelConfig.chiSquareVaers}
                                </div>
                            </th>
                        </g:if>
                    </g:if>
                    <g:if test="${isVigibaseEnabled}">
                        <g:if test="${labelConfigCopy.newCountVigibase}">
                            <th data-field="newCountVigibase">
                                <div class="th-label dateRange" data-field="newCountVigibase">
                                    <div class="stacked-cell-center-top">
                                        ${labelConfig.newCountVigibase.split("/")[0]}
                                    </div>

                                    <div class="stacked-cell-center-bottom">
                                        ${labelConfig.newCountVigibase.split("/")[1]}
                                    </div>
                                </div>
                            </th>
                        </g:if>
                        <g:if test="${labelConfigCopy.newSeriousCountVigibase}">
                            <th data-field="newSeriousCountVigibase">
                                <div class="th-label dateRange" data-field="newSeriousCountVigibase">
                                    <div class="stacked-cell-center-top">
                                        ${labelConfig.newSeriousCountVigibase.split("/")[0]}
                                    </div>

                                    <div class="stacked-cell-center-bottom">
                                        ${labelConfig.newSeriousCountVigibase.split("/")[1]}
                                    </div>
                                </div>
                            </th>
                        </g:if>
                        <g:if test="${showEbgmVigibase && labelConfigCopy.eb05Vigibase}">
                            <th data-idx="21" data-field="eb05Vigibase">
                                <div class="th-label dateRange" data-field="eb05Vigibase">
                                    <div class="stacked-cell-center-top">
                                        ${labelConfig.eb05Vigibase.split("/")[0]}
                                    </div>

                                    <div class="stacked-cell-center-top">
                                        ${labelConfig.eb05Vigibase.split("/")[1]}
                                    </div>
                                </div>
                            </th>
                        </g:if>
                        <g:if test="${labelConfigCopy.newFatalCountVigibase}">
                            <th data-field="newFatalCountVigibase">
                                <div class="th-label dateRange" data-field="newFatalCountVigibase">
                                    <div class="stacked-cell-center-top">
                                        ${labelConfig.newFatalCountVigibase.split("/")[0]}
                                    </div>

                                    <div class="stacked-cell-center-bottom">
                                        ${labelConfig.newFatalCountVigibase.split("/")[1]}
                                    </div>
                                </div>
                            </th>
                        </g:if>
                        <g:if test="${labelConfigCopy.newGeriatricCountVigibase}">

                            <th data-field="newGeriatricCountVigibase">
                                <div class="th-label dateRange" data-field="newGeriatricCountVigibase">
                                    <div class="stacked-cell-center-top">
                                        ${labelConfig.newGeriatricCountVigibase.split("/")[0]}
                                    </div>

                                    <div class="stacked-cell-center-bottom">
                                        ${labelConfig.newGeriatricCountVigibase.split("/")[0]}
                                    </div>
                                </div>
                            </th>
                        </g:if>
                        <g:if test="${labelConfigCopy.newPediatricCountVigibase}">
                            <th data-field="newPediatricCountVigibase">
                                <div class="th-label dateRange" data-field="newPediatricCountVigibase">
                                    <div class="stacked-cell-center-top">
                                        ${labelConfig.newPediatricCountVigibase.split("/")[0]}
                                    </div>

                                    <div class="stacked-cell-center-bottom">
                                        ${labelConfig.newPediatricCountVigibase.split("/")[1]}
                                    </div>
                                </div>
                            </th>
                        </g:if>

                        <g:if test="${showPrrVigibase && labelConfigCopy.prrValueVigibase}">
                            <th data-field="prrValueVigibase">
                                <div class="th-label dateRange" data-field="prrValueVigibase">
                                    <div>
                                        ${labelConfig.prrValueVigibase}
                                    </div>
                                </div>
                            </th>
                        </g:if>

                        <g:if test="${showRorVigibase && labelConfigCopy.rorValueVigibase}">
                            <th data-field="rorValueVigibase">
                                <div class="th-label dateRange" data-field="rorValueVigibase">
                                    ${labelConfig.rorValueVigibase}
                                </div>
                            </th>
                        </g:if>

                        <g:if test="${showEbgmVigibase && labelConfigCopy.ebgmVigibase}">
                            <th data-idx="31" data-field="ebgmVigibase">
                                <div class="th-label dateRange" data-field="ebgmVigibase">
                                    ${labelConfig.ebgmVigibase}
                                </div>
                            </th>
                        </g:if>
                        <g:if test="${showPrrVigibase && labelConfigCopy.prrLCIVigibase}">
                            <th data-field="prrLCIVigibase">
                                <div class="th-label dateRange col-min-100" data-field="prrLCIVigibase">
                                    <div>${labelConfig.prrLCIVigibase.split("/")[0]}</div>

                                    <div>${labelConfig.prrLCIVigibase.split("/")[1]}</div>
                                </div>
                            </th>
                        </g:if>

                        <g:if test="${showRorVigibase && labelConfigCopy.rorLCIVigibase}">
                            <th data-field="rorLCIVigibase">
                                <div class="th-label dateRange" data-field="rorLCIVigibase">
                                    <div class="stacked-cell-center-top">${labelConfig.rorLCIVigibase.split("/")[0]}</div>

                                    <div class="stacked-cell-center-top">${labelConfig.rorLCIVigibase.split("/")[1]}</div>
                                </div>
                            </th>
                        </g:if>


                        <g:if test="${showPrrVigibase && showRorVigibase && labelConfigCopy.chiSquareVigibase}">
                            <th align="center" data-field="chiSquareVigibase">
                                <div class="th-label col-min-120" data-field="chiSquareVigibase">
                                    ${labelConfig.chiSquareVigibase}
                                </div>
                            </th>
                        </g:if>
                    </g:if>
                    <g:each var="d" in="${labelConfigNew}" status="i">
                        <g:if test="${d.display?.contains("/") && d.enabled && (!d.name.contains("exe"))}">
                            <th data-field="${d.name}">
                                <div class="th-label dateRange" data-field="${d.name}">
                                    <div class="stacked-cell-center-top">
                                        ${d.display?.split("/")[0].toString().trim()}
                                    </div>

                                    <div class="stacked-cell-center-bottom">
                                        ${d.display?.split("/")[1].toString().trim()}
                                    </div>
                                </div>
                            </th>
                        </g:if>
                        <g:if test="${!d.display?.contains("/") && d.enabled && (!d.name.contains("exe"))}">
                            <g:if test="${!groupBySmq}">
                                <th data-field="${d.name}">
                                    <div class="th-label" data-field="${d.name}">${d.display}</div>
                                </th>
                            </g:if>
                        </g:if>
                    </g:each>
                    <g:if test="${showEbgm}">
                        <g:each in="${subGroupsColumnList}" var="category">
                            <g:if test="${labelConfigCopy."ebgm${category}"}">
                                <th data-field="ebgm${category}">
                                    <div class="th-label dateRange" data-field="ebgm${category}">
                                        <div class="stacked-cell-center-top">
                                            ${labelConfig."ebgm${category}"}
                                        </div>
                                    </div>
                                </th>
                            </g:if>
                            <g:if test="${labelConfigCopy."eb05${category}"}">
                                <th data-field="eb05${category}">
                                    <div class="th-label dateRange" data-field="eb05${category}">
                                        <div class="stacked-cell-center-top">
                                            ${labelConfig."eb05${category}"}
                                        </div>
                                    </div>
                                </th>
                            </g:if>
                            <g:if test="${labelConfigCopy."eb95${category}"}">
                                <th data-field="eb95${category}">
                                    <div class="th-label dateRange" data-field="eb95${category}">
                                        <div class="stacked-cell-center-top">
                                            ${labelConfig."eb95${category}"}
                                        </div>
                                    </div>
                                </th>
                            </g:if>
                        </g:each>
                    </g:if>
                    <g:each in="${prrRorSubGroupMap}" var="index, value">
                        <g:if test="${labelConfigCopy."${value}"}">
                            <th data-field="${value}">
                                <div class="th-label dateRange" data-field="${value}">
                                    <div class="stacked-cell-center-top">
                                        ${labelConfig."${value}"}
                                    </div>
                                </div>
                            </th>
                        </g:if>
                    </g:each>
                    <g:each in="${relativeSubGroupMap}" var="index, value">
                        <g:if test="${labelConfigCopy."${value}"}">
                            <th data-field="${value}">
                                <div class="th-label dateRange" data-field="${value}">
                                    <div class="stacked-cell-center-top">
                                        ${labelConfig."${value}"}
                                    </div>
                                </div>
                            </th>
                        </g:if>
                    </g:each>
                    <g:if test="${isFaersEnabled}">
                        <g:each in="${faersSubGroupsColumnList}" var="category">
                            <g:if test="${labelConfigCopy."ebgm${category}Faers"}">
                                <th data-field="ebgm${category}Faers">
                                    <div class="th-label dateRange" data-field="ebgm${category}Faers">
                                        <div class="stacked-cell-center-top">
                                            ${labelConfig."ebgm${category}Faers"}
                                        </div>
                                    </div>
                                </th>
                            </g:if>
                            <g:if test="${labelConfigCopy."eb05${category}Faers"}">
                                <th data-field="eb05${category}Faers">
                                    <div class="th-label dateRange" data-field="eb05${category}Faers">
                                        <div class="stacked-cell-center-top">
                                            ${labelConfig."eb05${category}Faers"}
                                        </div>
                                    </div>
                                </th>
                            </g:if>
                            <g:if test="${labelConfigCopy."eb95${category}Faers"}">
                                <th data-field="eb95${category}Faers">
                                    <div class="th-label dateRange" data-field="eb95${category}Faers">
                                        <div class="stacked-cell-center-top">
                                            ${labelConfig."eb95${category}Faers"}
                                        </div>
                                    </div>
                                </th>
                            </g:if>
                        </g:each>
                    </g:if>
                    <g:render template="/includes/widgets/prevColumns"
                              model="[labelConfigKeyId: labelConfigKeyId, callingScreen: callingScreen, isPVAEnabled: isPVAEnabled, isFaersEnabled: isFaersEnabled, relativeSubGroupMap: relativeSubGroupMap, prrRorSubGroupMap: prrRorSubGroupMap, listDateRange: listDateRange, prevColCount: prevColCount, prevColumns: prevColumns, isVaersEnabled: isVaersEnabled, isVigibaseEnabled: isVigibaseEnabled,
                                      showPrr         : showPrr, showRor: showRor, showEbgm: showEbgm, prevFaersDate: prevFaersDate, prevEvdasDate: prevEvdasDate, subGroupsColumnList: subGroupsColumnList, isEvdasEnabled: isEvdasEnabled,
                                      showPrrVigibase : showPrrVigibase, showRorVigibase: showRorVigibase, showEbgmVigibase: showEbgmVigibase, isFaersEnabled: isFaersEnabled, faersSubGroupsColumnList: faersSubGroupsColumnList,
                                      isRor           : isRor, showPrrFaers: showPrrFaers, showRorFaers: showRorFaers, showEbgmFaers: showEbgmFaers, showPrrVaers: showPrrVaers, showRorVaers: showRorVaers, showEbgmVaers: showEbgmVaers, labelConfig: labelConfig, labelConfigCopy: labelConfigCopy, labelConfigNew: labelConfigNew]"/>

                    <g:render template="/includes/widgets/newColumns"
                              model="[labelConfigKeyId: labelConfigKeyId, labelConfig: labelConfig, labelConfigCopy: labelConfigCopy, labelConfigNew: labelConfigNew, listDateRange: listDateRange]"/>
                </g:else>
            </tr>
            </thead>
        </table>
    </div>
    <g:render template="/includes/modals/case_drill_down"/>
    <g:render template="/includes/modals/evdas_case_drill_down"
              model="[id: id, alertType: Constants.AlertConfigType.AGGREGATE_CASE_ALERT]"/>
    <g:render template="/includes/modals/dss_details"/>

</div>


