<div class="rxmain-container">

    <div class="rxmain-container-inner">

        <div class="rxmain-container-row rxmain-container-header">
            <label class="rxmain-container-header-label">
                <g:message code="app.aggregated.case.review" />
            </label>
        </div>

        <div class="rxmain-container-content">

            <table id="rxTableAggregateReview" class="dataTable row-border hover table-hd-less-gap" width="100%">
                <thead>
                <tr>
                    <th>Alert Name</th>
                    <th>Product Name</th>
                    <th >SOC</th>
                    <th>Event PT</th>
                    <th>
                        <div class="stacked-cell-center-top"><g:message code="app.label.agg.alert.new.spont" /></div>
                        <div class="stacked-cell-center-bottom"><g:message code="app.label.agg.alert.cumm.spont" /></div>
                    </th>
                    <th>
                        <div class="stacked-cell-center-top"><g:message code="app.label.agg.alert.new.ser" /></div>
                        <div class="stacked-cell-center-bottom"><g:message code="app.label.agg.alert.cumm.ser" /></div>
                    </th>
                    <th style="width: 5%"><g:message code="app.label.pe.history.prr" /></th>
                    <th style="width: 5%"><g:message code="app.label.pe.history.ror" /></th>
                    <th style="width: 5%"><g:message code="app.label.pe.history.ebgm" /></th>
                    <th style="width: 5%">
                        <div class="stacked-cell-center-top"><g:message code="app.label.pe.history.eb05" /></div>
                        <div class="stacked-cell-center-top"><g:message code="app.label.pe.history.eb95" /></div>
                    </th>
                    <th>Trend</th>
                    <th style="width: 5%">Data Source</th>
                    <th>Signal Names</th>
                    <th>Disposition</th>
                    <th>History</th>
                    <th></th>
                </tr>
                </thead>
            </table>
        </div>
    </div>
</div>
<g:render template="/includes/modals/case_drill_down" />
<g:render template="/includes/modals/product_event_history_modal" />
<g:render template="/includes/modals/alert_comment_modal" />