<asset:javascript src="app/pvs/signalStrategy/strategyAlerts.js"/>
<g:javascript>
    var strategyAlertConfigurationUrl = "${createLink(controller: "signalStrategy", action: 'getAlertConfigurationData')}"
</g:javascript>

<script>

    $(document).ready(function () {
        var url = strategyAlertConfigurationUrl+"?id="+$("#strategyId").val();
        signal.strategyAlerts.init_strategy_alert_table(url+"&alertType="+"Single Case Alert", "#attachedAggAlertTable", "Single Case Alert");
        signal.strategyAlerts.init_strategy_alert_table(url+"&alertType="+"Aggregate Case Alert", "#attachedSingleAlertTable", "Aggregate Case Alert");
        signal.strategyAlerts.init_strategy_alert_table(url+"&alertType="+"Ad-Hoc Alert", "#attachedAdhocAlertTable", "Ad-Hoc Alert");
    });

</script>

<div class="inner-table">
    <div class="panel-group">
        <div class="rxmain-container panel panel-default">
            <div class="rxmain-container-row rxmain-container-header panel-heading">
               <h4 class="panel-title">
                   <a data-toggle="collapse" href="#linkedQuantitiativeAlert" class="" aria-expanded="true">
                       <g:message code="app.label.linked.quantitative.alert" />
                   </a>
               </h4>
            </div>
            <div class="rxmain-container-content panel-body panel-collapse collapse in" id="linkedQuantitiativeAlert" aria-expanded="true">
                <table id="attachedAggAlertTable" class="row-border hover table">
                    <thead>
                    <tr>
                        <th><g:message code="app.label.name" /></th>
                        <th><g:message code="app.label.description" /></th>
                        <th><g:message code="app.label.product" /></th>
                        <th><g:message code="app.label.event" /></th>
                        <th><g:message code="app.label.dateCreated" /></th>
                        <th><g:message code="app.label.priority" /></th>
                    </tr>
                    </thead>
                </table>
            </div>
        </div>

        <div class="rxmain-container panel panel-default">
            <div class="rxmain-container-row rxmain-container-header panel-heading">
                <h4 class="panel-title">
                    <a data-toggle="collapse" href="#linkedQualitativeAlert" class="" aria-expanded="true">
                        <g:message code="app.label.linked.quantitative.alert" />
                    </a>
                </h4>
            </div>
            <div class="rxmain-container-content panel-body panel-collapse collapse in" id="linkedQualitativeAlert" aria-expanded="true">
                <table id="attachedSingleAlertTable" class="row-border hover table">
                    <thead>
                    <tr>
                        <th><g:message code="app.label.name" /></th>
                        <th><g:message code="app.label.description" /></th>
                        <th><g:message code="app.label.product" /></th>
                        <th><g:message code="app.label.event" /></th>
                        <th><g:message code="app.label.dateCreated" /></th>
                        <th><g:message code="app.label.priority" /></th>
                    </tr>
                    </thead>
                </table>
            </div>
        </div>


        <div class="rxmain-container panel panel-default">
            <div class="rxmain-container-row rxmain-container-header panel-heading">
                <h4 class="panel-title">
                    <a data-toggle="collapse" href="#linkedAdhocAlert" class="" aria-expanded="true">
                        <g:message code="app.label.linked.adhoc.alert" />
                    </a>
                </h4>
            </div>
            <div class="rxmain-container-content panel-body panel-collapse collapse in" id="linkedAdhocAlert" aria-expanded="true">
                <table id="attachedAdhocAlertTable" class="row-border hover table">
                    <thead>
                    <tr>
                        <th><g:message code="app.label.name" /></th>
                        <th><g:message code="app.label.description" /></th>
                        <th><g:message code="app.label.product" /></th>
                        <th><g:message code="app.label.event" /></th>
                        <th><g:message code="app.label.formulation" /></th>
                        <th><g:message code="app.label.indication" /></th>
                        <th><g:message code="app.label.report.type" /></th>
                        <th><g:message code="app.label.number.of.icsr" /></th>
                        <th><g:message code="app.label.initial.data.source" /></th>
                        <th><g:message code="app.label.dateCreated" /></th>
                        <th><g:message code="app.label.priority" /></th>
                    </tr>
                    </thead>
                </table>
            </div>
        </div>

    </div>
</div>



<input type="hidden" id="strategyId" value="${id}" />