<%@ page import=" grails.util.Holders" %>

<g:set var="dataSourceLabel" value="${Holders.config.signal.dataSource.safety.name}"/>

<div class="col-md-12">

    <table id="prevCountAndScoresTable" class="auto-scale row-border no-shadow hover">
        <thead>
        <tr id="alertsDetailsTableRow">

            <th data-field="xAxisTitle">
                <div class="th-label" data-field="xAxisTitle"><g:message
                        code="app.label.event.detail.xaxis.title.pva"/></div>
            </th>

            <th data-field="newFatal_pva">
                <div class="th-label dateRange" data-field="newFatal_pva">
                    <div class="stacked-cell-center-top">
                        <g:message code="app.label.event.detail.new.fatal.pva"/>(${dataSourceLabel})
                    </div>

                    <div class="stacked-cell-center-bottom">
                        <g:message code="app.label.event.detail.cumm.fatal.pva"/>(${dataSourceLabel})
                    </div>
                </div>
            </th>

            <th data-field="newCount_pva">
                <div class="th-label dateRange" data-field="newCount_pva">
                    <div class="stacked-cell-center-top">
                        <g:message code="app.label.event.detail.new.count.pva"/>(${dataSourceLabel})
                    </div>

                    <div class="stacked-cell-center-bottom">
                        <g:message code="app.label.event.detail.cumm.count.pva"/>(${dataSourceLabel})
                    </div>
                </div>
            </th>

            <th data-field="prr_pva">
                <div class="th-label" data-field="prr_pva"><g:message code="app.label.event.detail.prr.pva"/></div>(${dataSourceLabel})
            </th>

            <th data-field="ror_pva">
                <div class="th-label" data-field="ror_pva"><g:message code="app.label.event.detail.ror.pva"/></div>(${dataSourceLabel})
            </th>

            <th data-field="newEvpm_ev">
                <div class="th-label dateRange" data-field="newEvpm_ev">
                    <div class="stacked-cell-center-top">
                        <g:message code="app.label.event.detail.new.evpm.ev"/>
                    </div>

                    <div class="stacked-cell-center-bottom">
                        <g:message code="app.label.event.detail.total.evpm.ev"/>
                    </div>
                </div>
            </th>

            <th data-field="ime_ev">
                <div class="th-label" data-field="ime_ev"><g:message code="app.label.event.detail.imeDme.ev"/></div>
            </th>
            <th data-field="newFatal_ev">
                <div class="th-label dateRange" data-field="newFatal_ev">
                    <div class="stacked-cell-center-top">
                        <g:message code="app.label.event.detail.new.fatal.ev"/>
                    </div>

                    <div class="stacked-cell-center-bottom">
                        <g:message code="app.label.event.detail.total.fatal.ev"/>
                    </div>
                </div>
            </th>

            <th data-field="newPaed_ev">
                <div class="th-label dateRange" data-field="newPaed_ev">
                    <div class="stacked-cell-center-top">
                        <g:message code="app.label.event.detail.new.paed.ev"/>
                    </div>

                    <div class="stacked-cell-center-bottom">
                        <g:message code="app.label.event.detail.total.paed.ev"/>
                    </div>
                </div>
            </th>

            <th data-field="sdrPaed_ev">
                <div class="th-label" data-field="sdrPaed_ev">
                    <div class="stacked-cell-center-top">
                        <g:message code="app.label.event.detail.sdr.ev"/>
                    </div>

                    <div class="stacked-cell-center-bottom">
                        <g:message code="app.label.event.detail.sdr.paed.ev"/>
                    </div>
                </div>
            </th>

            <th align="center" data-field="changes_ev">
                <div class="th-label" data-field="changes_ev"><g:message
                        code="app.label.event.detail.changes.ev"/></div>
            </th>

            <th align="center" data-field="rorAll_ev">
                <div class="th-label" data-field="rorAll_ev"><g:message code="app.label.event.detail.ror.all.ev"/></div>
            </th>

            <th data-field="newCounts_faers">
                <div class="th-label dateRange" data-field="newCounts_faers">
                    <div class="stacked-cell-center-top">
                        <g:message code="app.label.event.detail.new.counts.faers"/>
                    </div>

                    <div class="stacked-cell-center-bottom">
                        <g:message code="app.label.event.detail.cumm.counts.faers"/>
                    </div>
                </div>
            </th>

            <th data-field="eb05_faers">
                <div class="th-label dateRange" data-field="eb05_faers">
                    <div class="stacked-cell-center-top">
                        <g:message code="app.label.event.detail.eb05.faers"/>
                    </div>

                    <div class="stacked-cell-center-bottom">
                        <g:message code="app.label.event.detail.eb95.faers"/>
                    </div>
                </div>
            </th>
        </tr>
        </thead>
    </table>

</div>