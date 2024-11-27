<head>
    <meta name="layout" content="main"/>
    <title>Signal Strategy</title>
    <g:javascript>
        var strategyListUrl = "${createLink(controller: "signalStrategy", action: 'list')}"
        var createStrategyUrl = "${createLink(controller: "signalStrategy", action: 'create')}"
        var editStrategyUrl = "${createLink(controller: "signalStrategy", action: 'edit')}"
    </g:javascript>
    <asset:javascript src="app/pvs/signalStrategy/signalStrategy.js"/>
    <script>
        $(document).ready(function() {
            signal.strategy.init_strategy_table(strategyListUrl, createStrategyUrl);
        })
    </script>
</head>

<body>
<rx:container title="Signal Strategy">

    <table id="strategyTable" class="row-border hover" width="100%">
        <thead>
        <tr>
            <th><g:message code="app.label.signal.strategy.name" /></th>
            <th><g:message code="app.label.signal.strategy.product" /></th>
            <th><g:message code="app.label.signal.strategy.type" /></th>
            <th><g:message code="app.label.signal.strategy.medical.concept" />
            <th><g:message code="app.label.signal.strategy.start.date" /></th>
            <th><g:message code="app.label.description" /></th>

        </tr>
        </thead>
    </table>

</rx:container>

</body>
