<div id="quantitative-score-container" class="list">
    <table class="table caseHistoryModalTable" id="peAnalysisTable" style="width: 100%">
        <thead>
        <tr>
            <th>Data Source</th>
            <th>Product</th>
            <th>Event</th>
            <th></th>
            <th>
                <div class="stacked-cell-center-top">New Count</div>
                <div class="stacked-cell-center-bottom">Total Count</div>
            </th>
            <th>
                <div class="stacked-cell-center-top">New Fatal</div>
                <div class="stacked-cell-center-bottom">Total Fatal</div>
            </th>
            <th>
                SDR
            </th>
            <th>
                Trend
            </th>
            <th>
                <div class="stacked-cell-center-top">Listed</div>
                <div class="stacked-cell-center-bottom">Serious</div>
            </th>
            <th>Priority</th>
            <th>
                <div class="stacked-cell-center-top">PRR Trend</div>
                <div class="stacked-cell-center-bottom">EBGM Trend</div>
            </th>
            <th></th>
        </tr>
        <thead>
        <tbody id="peAnalysisTableBody" class="tableModalBody"></tbody>
    </table>
    <input type="hidden" value="${signalId}" id="signalId" />
    <input type="hidden" value="${isTopic}" id="isTopic"/>
</div>

<g:render template="/includes/widgets/show_evdas_charts_modal" />
<asset:javascript src="app/pvs/validated_signal/pe_analysis.js" />
