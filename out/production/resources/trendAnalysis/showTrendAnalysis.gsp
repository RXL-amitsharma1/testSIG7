<%@ page import="com.rxlogix.util.ViewHelper" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.label.trend.analysis" /></title>
    <g:javascript>
        var alertType = "${type}";
        var peTableUrl = "${createLink(controller: 'trendAnalysis', action: 'alertList', params: [id: id, type: type])}";
        var fetchTrendUrl = "${createLink(controller: "trendAnalysis", action: "getTrendData", params: [id: id, type: type])}";
    </g:javascript>
    <asset:javascript src="dygraphs/dygraph.min.js"/>
    <asset:stylesheet src="dygraph.css"/>
    <asset:javascript src="app/pvs/trendAnalysis.js"/>
    <asset:javascript src="app/pvs/dataTablesActionButtons.js"/>
    <style>
    .demographyTable {
        border-collapse: collapse;
        width: 100%;
        border: 1px solid #ddd;
        text-align: left;
    }

    .demographyTableTd {
        padding: 15px;
        border: 1px solid #ddd;
        text-align: left;
    }
    </style>
</head>
<body>
<rx:container title="Trend">

    <div class="row">
        <div class="col-sm-2">
            <label for="trendFrequency">Trend Frequency</label>
            <g:select name="trendFrequency" id="trendFrequency" from="${ViewHelper.getTrendFrequencyEnum()}"
                      optionValue="display" optionKey="name" class="form-control"/>
        </div>
    </div>

    <br/>

    <div id="pe-table-container" class="list">
        <table class="table" id="peTable" style="width: 100%">
            <thead>
            <tr>
                <th>Data Source</th>
                <th>Product Name</th>
                <th>SOC</th>
                <th>Event PT</th>
                <th>Retrieval Date</th>
                <th>New Spon Count</th>
                <th></th>
            </tr>
            <thead>
            <tbody id="peTableBody" class="tableModalBody"></tbody>
        </table>
    </div>

    <br/>
    <input type="hidden" value="${id}" id="signalId" />
    <input type="hidden" value="${isFaers}" id="isFaers"/>

    <g:render template="/includes/widgets/trend" />

</rx:container>
</body>