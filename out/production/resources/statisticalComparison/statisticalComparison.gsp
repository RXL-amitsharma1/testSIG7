<%@ page contentType="text/html;charset=UTF-8" import="grails.converters.JSON; com.rxlogix.enums.ReportFormat; com.rxlogix.config.PVSState; com.rxlogix.Constants ; grails.util.Holders" %>
<head>
    <meta name="layout" content="main"/>
    <title>Alert Details</title>
    <asset:javascript src="app/pvs/alerts_review/alert_review.js"/>
    <asset:javascript src="app/pvs/alerts_review/statistical_comparison.js"/>
    <asset:javascript src="app/pvs/disposition/disposition-change.js"/>
    <asset:javascript src="app/bootstrap-modal-popover/bootstrap-modal-popover.js"/>
    <asset:javascript src="app/pvs/bootbox.min.js"/>
    <g:javascript>
        var callingScreen = "${callingScreen}";
        var executedConfigId = "${executedConfigId}";
        var isArchived = "${isArchived}";
        var appName = "${appName}";
        var dataUrl = "${createLink(controller: 'statisticalComparison', action: 'fetchStatsComparisonData', params: [id: executedConfigId, appName: appName])}";
        var dispositionIncomingOutgoingMap = JSON.parse('${dispositionIncomingOutgoingMap}');
        var forceJustification = ${forceJustification};
        var hasReviewerAccess = ${hasReviewerAccess};
        var hasSignalCreationAccessAccess = ${hasSignalCreationAccessAccess};
        var hasSignalViewAccessAccess = ${hasSignalViewAccessAccess};

        var changeDispositionUrl = "";
        <g:if test="${appName == Constants.AlertConfigType.AGGREGATE_CASE_ALERT}">
            changeDispositionUrl = "${createLink(controller: 'aggregateCaseAlert', action: 'changeDisposition')}";
        </g:if>
        <g:if test="${appName == Constants.AlertConfigType.EVDAS_ALERT}">
            changeDispositionUrl = "${createLink(controller: 'evdasAlert', action: 'changeDisposition')}";  
        </g:if>
        var availableSignalNameList = JSON.parse('${availableSignals.collect{it.name} as JSON}');
    </g:javascript>

<style>
@media screen and (max-width: 1600px) {
    .modal-xlg .modal-dialog{
        width: 85% !important;
    }
}
@media screen and (min-width: 1601px) {
    .modal-xlg .modal-dialog{
        width: 80% !important;
    }
}
</style>
</head>

<body>

    <div>
        <div class="row">
            <div class="col-sm-12">
                <div class="page-title-box">
                    <div class="fixed-page-head">
                        <div class="page-head-lt">
                            <h4 class="m-t-1-p">${configName}</h4>
                        </div>
                        <div class="page-head-rt">
                            <a class="btn btn-default pull-right m-t-5"
                            href="/signal/statisticalComparison/routeToReviewScreen?callingScreen=review&configId=${executedConfigId}&appName=${appName}">
                                <i class="fa fa-long-arrow-left" aria-hidden="true"></i> Back
                            </a>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

<rx:container title="Statistical Comparison">

    <table id="statsTable" class="auto-scale row-border hover" style="width:100%">
        <thead style="width:100%">
        <tr>
            <th>Product Name</th>
            <th>SOC</th>
            <th>PT</th>
            <th>
                <div class="stacked-cell-center-top"><g:message code="app.label.agg.alert.new.spont"/></div>

                <div class="stacked-cell-center-bottom"><g:message code="app.label.agg.alert.cumm.spont"/></div>

                <div class="stacked-cell-center-bottom">(${(Holders.config.signal.dataSource.safety.name)})</div>
            </th>
            <th>
                <div class="stacked-cell-center-top"><g:message code="app.label.agg.alert.new.spont"/></div>

                <div class="stacked-cell-center-bottom"><g:message code="app.label.agg.alert.cumm.spont"/></div>

                <div class="stacked-cell-center-bottom">(FAERS)</div>
            </th>
            <th>
                <div class="stacked-cell-center-top">New EV</div>

                <div class="stacked-cell-center-top">Total EV</div>

                <div class="stacked-cell-center-bottom">(EVDAS)</div>
            </th>
            <th>
                <div class="stacked-cell-center-top"><g:message code="app.label.pe.history.prr"/></div>

                <div class="stacked-cell-center-top"><g:message code="app.label.pe.history.ror"/></div>

                <div class="stacked-cell-center-bottom">(${(Holders.config.signal.dataSource.safety.name)})</div>
            </th>
            <th>
                <div class="stacked-cell-center-top"><g:message code="app.label.pe.history.prr"/></div>

                <div class="stacked-cell-center-top"><g:message code="app.label.pe.history.ror"/></div>

                <div class="stacked-cell-center-bottom">(FAERS)</div>
            </th>
            <th>


                <div class="stacked-cell-center-top"><g:message code="app.label.pe.history.ror"/></div>

                <div class="stacked-cell-center-bottom">(EVDAS)</div>
            </th>
            <th>
                <div class="stacked-cell-center-top"><g:message code="app.business.config.type.EB05"/></div>

                <div class="stacked-cell-center-bottom">(${Holders.config.signal.dataSource.safety.name})</div>
            </th>
            <th>
                <div class="stacked-cell-center-top">EB05</div>

                <div class="stacked-cell-center-bottom">(FAERS)</div>
            </th>
            <th>
                <g:message code="app.label.disposition"/>
            </th>
            <th></th>

        </tr>
        </thead>
    </table>

</rx:container>
<g:hiddenField id='isArchived' name='isArchived' value="${isArchived}"/>
<g:render template="/includes/widgets/show_evdas_charts_modal" />
<g:render template="/includes/popover/dispositionJustificationSelect"/>
<g:render template="/includes/popover/dispositionSignalSelect" model="[availableSignals: availableSignals, forceJustification: forceJustification]"/>
</body>
</html>