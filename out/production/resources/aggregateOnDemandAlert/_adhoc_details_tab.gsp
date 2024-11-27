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
</style>

<div class="row">
    <g:render template="/includes/widgets/agg_demand_widget_tab"
              model="[id : id, name: name,isJader: isJader, reportUrl: reportUrl, analysisFileUrl: analysisFileUrl, callingScreen: callingScreen, isVaersEnabled: isVaersEnabled,
                      isVigibaseEnabled: isVigibaseEnabled, freqNames: freqNames, dateRange: dateRange, viewsList: viewsList, viewId: viewId]"/>

    <!-- Division of alert review data table -->
    <div class="col-md-12">
     <input type="hidden" value="${miningVariable}" id="miningVariable"/>
        <table id="alertsDetailsTable" class="auto-scale row-border no-shadow hover">
            <thead>
            <tr id="alertsDetailsTableRow">
                <th data-idx="0" data-field="checkbox">
                    <input id="select-all" class="alert-select-all" type="checkbox"/>
                </th>
                <g:each in="${adhocColumnListNew}" status="i" var="columnInfoMap">
                    <g:if test="${columnInfoMap.name == "productName"}">
                        <th data-idx="${i+5}" data-field="${columnInfoMap.name}">
                            <div class="th-label" data-field="${columnInfoMap.name}">${isBatchAlert ? miningVariable :columnInfoMap?.display.split("#OR")[0]}</div>
                        </th>
                    </g:if>
                    <g:else>
                    <g:if test="${columnInfoMap.type == 'text' || columnInfoMap.type == 'count' || columnInfoMap.type == 'score'}">
                        <th data-idx="${i+5}" data-field="${columnInfoMap.name}">
                            <g:if test="${groupBySmq && columnInfoMap?.display.split("#OR").size() > 1}">
                                <div class="th-label" data-field="${columnInfoMap.name}">${columnInfoMap?.display.split("#OR")[1]}</div>
                            </g:if>
                            <g:else>
                                <div class="th-label" data-field="${columnInfoMap.name}">${columnInfoMap?.display.split("#OR")[0]}</div>
                            </g:else>
                        </th>

                    </g:if>
                    <g:if test="${columnInfoMap.type == 'countStacked'}">
                        <th data-idx="${i+5}" data-field="${columnInfoMap.name}">
                            <div class="th-label" data-field="${columnInfoMap.name}">
                                <div class="stacked-cell-center-top">
                                    ${columnInfoMap?.display?.split('/')[0]}
                                </div>
                                <div class="stacked-cell-center-bottom">
                                    ${columnInfoMap?.display?.split('/')[1]}
                                </div>

                            </div>
                        </th>
                    </g:if>
                    </g:else>
                </g:each>

                <g:if test="${isPvaEnabled && !isDataMining}">
                    <g:each in="${subGroupsColumnList}" var="category">
                        <g:if test = "${('ebgm'+category) in subGroupColumnInfo.keySet()}">
                        <th data-field="ebgm${category}">
                            <div class="th-label" data-field="ebgm${category}">
                                <div>
                                    ${subGroupColumnInfo.get("ebgm"+category)}
                                </div>
                            </div>
                        </th>
                        </g:if>
                        <g:if test = "${('eb05'+category) in subGroupColumnInfo.keySet()}">
                        <th data-field="eb05${category}">
                            <div class="th-label" data-field="eb05${category}">
                                <div>
                                    ${subGroupColumnInfo.get("eb05"+category)}
                                </div>
                            </div>
                        </th>
                        </g:if>
                        <g:if test = "${('eb95'+category) in subGroupColumnInfo.keySet()}">
                        <th data-field="eb95${category}">
                            <div class="th-label" data-field="eb95${category}">
                                <div>
                                    ${subGroupColumnInfo.get("eb95"+category)}
                                </div>
                            </div>
                        </th>
                        </g:if>
                    </g:each>
                    <g:each in="${prrRorSubGroupMap}" var="index, value">
                        <th data-field="${value}">
                            <div class="th-label" data-field="${value}">
                                ${subGroupColumnInfo.get(value)}
                            </div>
                        </th>
                    </g:each>
                    <g:each in="${relativeSubGroupMap}" var="index, value">
                        <th data-field="${value}">
                            <div class="th-label" data-field="${value}">
                                ${subGroupColumnInfo.get(value)}
                            </div>
                        </th>
                    </g:each>
                </g:if>

                <g:if test="${isFaersEnabled}">
                    <g:each in="${faersSubGroupsColumnList}" var="category">
                        <g:if test = "${('ebgm'+category) in subGroupColumnInfo.keySet()}">
                        <th data-field="ebgm${category}">
                            <div class="th-label" data-field="ebgm${category}">
                                <div>
                                    ${subGroupColumnInfo.get("ebgm"+category)}
                                </div>
                            </div>
                        </th>
                        </g:if>
                        <g:if test = "${('eb05'+category) in subGroupColumnInfo.keySet()}">
                        <th data-field="eb05${category}">
                            <div class="th-label" data-field="eb05${category}">
                                <div>
                                    ${subGroupColumnInfo.get("eb05"+category)}
                                </div>
                            </div>
                        </th>
                        </g:if>
                        <g:if test = "${('eb95'+category) in subGroupColumnInfo.keySet()}">
                        <th data-field="eb95${category}">
                            <div class="th-label" data-field="eb95${category}">
                                <div>
                                    ${subGroupColumnInfo.get("eb95"+category)}
                                </div>
                            </div>
                        </th>
                        </g:if>
                    </g:each>
                </g:if>
            </tr>
            </thead>
        </table>
    </div>
    <g:render template="/includes/modals/case_drill_down"/>

</div>


