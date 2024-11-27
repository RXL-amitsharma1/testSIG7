
<%@ page import="com.rxlogix.Constants; com.rxlogix.enums.ReportFormat; grails.util.Holders" %>

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
            <g:if test="${callingScreen == Constants.Commons.TRIGGERED_ALERTS}">
                <th data-idx="1" data-field="name">
                    <div class="th-label" data-field="name"><g:message
                            code="app.label.quantitative.details.column.name"/></div>
                </th>
            </g:if>
            <g:if test="${Holders.config.alert.flags.enable}">
                <th data-idx="2" data-field="flags">
                    <i class="glyphicon glyphicon-tag m-r-5"
                       style="color: black"></i>

                    <div class="th-label" data-field="flags"></div>
                </th>
            </g:if>
            <g:if test="${isPriorityEnabled}">
                <th data-idx="4" data-field="priority">
                    <div class="th-label" data-field="priority"><g:message
                            code="app.label.quantitative.details.column.priority"/></div>
                </th>
            </g:if>

            <th data-idx="5" data-field="actions">
                <div class="th-label" data-field="actions"><g:message
                        code="app.label.quantitative.details.column.actions"/></div>
            </th>
            <g:if test="${callingScreen == Constants.Commons.DASHBOARD}">
                <th data-idx="1" data-field="name">
                    <div class="th-label" data-field="name"><g:message
                            code="app.label.quantitative.details.column.name"/></div>
                </th>
            </g:if>
            <th data-idx="7" data-field="productName">
                <div class="th-label" data-field="productName"><g:message
                        code="app.label.quantitative.details.column.productName"/></div>
            </th>
            <g:if test="${!groupBySmq}">
                <th data-idx="8" data-field="soc">
                    <div class="th-label" data-field="soc"><g:message
                            code="app.label.quantitative.details.column.soc"/></div>
                </th>
            </g:if>
            <g:if test="${groupBySmq}">
                <th data-idx="9" data-field="pt" class="col-min-200">
                    <div class="th-label" data-field="pt"><g:message
                            code="app.label.quantitative.details.column.smq.eventGroup"/></div>
                </th>
            </g:if>
            <g:else>
                <th data-idx="10" data-field="pt" class="col-min-200">
                    <div class="th-label" data-field="pt"><g:message
                            code="app.label.quantitative.details.column.pt"/></div>
                </th>
            </g:else>
            <th data-idx="6" data-field="alertTags">
                <div class="th-label" data-field="alertTags"><g:message
                        code="app.label.quantitative.details.column.alertTags"/></div>
            </th>
            <g:if test="${!groupBySmq}">
                <th data-idx="12" data-field="listed">
                    <div class="th-label" data-field="listed"><g:message
                            code="app.label.quantitative.details.column.listed"/></div>
                </th>
            </g:if>

            <th data-idx="37" data-field="newCount">
                <div class="th-label dateRange" data-field="newCount">
                    <div class="stacked-cell-center-top">
                        <g:message code="app.label.agg.alert.new.count"/>
                    </div>

                    <div class="stacked-cell-center-bottom">
                        <g:message code="app.label.agg.alert.cum.count"/>
                    </div>
                </div>
            </th>


            <th data-idx="14" data-field="newSeriousCount">
                <div class="th-label dateRange" data-field="newSeriousCount">
                    <div class="stacked-cell-center-top">
                        <g:message code="app.label.agg.alert.new.ser"/>
                    </div>

                    <div class="stacked-cell-center-bottom">
                        <g:message code="app.label.agg.alert.cumm.ser"/>
                    </div>
                </div>
            </th>

            <g:if test="${isFaersEnabled}">
                <th data-field="newCountFaers">
                    <div class="th-label dateRange" data-field="newCountFaers">
                        <div class="stacked-cell-center-top">
                            <g:message code="app.label.agg.alert.new.count.faers"/>
                        </div>

                        <div class="stacked-cell-center-bottom">
                            <g:message code="app.label.agg.alert.cum.count.faers"/>
                        </div>
                    </div>
                </th>

                <th data-field="newSeriousCountFaers">
                    <div class="th-label dateRange" data-field="newSeriousCountFaers">
                        <div class="stacked-cell-center-top">
                            <g:message code="app.label.agg.alert.new.ser.faers"/>
                        </div>

                        <div class="stacked-cell-center-bottom">
                            <g:message code="app.label.agg.alert.cumm.ser.faers"/>
                        </div>
                    </div>
                </th>
            </g:if>

            <g:if test="${groupBySmq}">
                <g:if test="${showEbgm}">
                    <th data-idx="21" data-field="eb05">
                        <div class="th-label dateRange" data-field="eb05">
                            <div class="stacked-cell-center-top">
                                <g:message code="app.label.pe.history.eb05"/>
                            </div>

                            <div class="stacked-cell-center-top">
                                <g:message code="app.label.pe.history.eb95"/>
                            </div>
                        </div>
                    </th>
                </g:if>

                <g:if test="${showEbgmFaers}">
                    <th data-idx="21" data-field="eb05Faers">
                        <div class="th-label dateRange" data-field="eb05Faers">
                            <div class="stacked-cell-center-top">
                                <g:message code="app.label.pe.history.eb05.faers"/>
                            </div>

                            <div class="stacked-cell-center-top">
                                <g:message code="app.label.pe.history.eb95.faers"/>
                            </div>
                        </div>
                    </th>
                </g:if>
            </g:if>

            <th data-field="newCountVaers">
                <div class="th-label dateRange" data-field="newCountVaers">
                    <div class="stacked-cell-center-top">
                        <g:message code="app.label.agg.alert.new.count.vaers"/>
                    </div>

                    <div class="stacked-cell-center-bottom">
                        <g:message code="app.label.agg.alert.cum.count.vaers"/>
                    </div>
                </div>
            </th>

            <th data-field="newSeriousCountVaers">
                <div class="th-label dateRange" data-field="newSeriousCountVaers">
                    <div class="stacked-cell-center-top">
                        <g:message code="app.label.agg.alert.new.ser.vaers"/>
                    </div>

                    <div class="stacked-cell-center-bottom">
                        <g:message code="app.label.agg.alert.cumm.ser.vaers"/>
                    </div>
                </div>
            </th>

            <g:if test="${groupBySmq && !isVigibaseEnabled}">
                <th data-idx="21" data-field="eb05Vaers">
                    <div class="th-label dateRange" data-field="eb05Vaers">
                        <div class="stacked-cell-center-top">
                            <g:message code="app.label.pe.history.eb05.vaers"/>
                        </div>

                        <div class="stacked-cell-center-top">
                            <g:message code="app.label.pe.history.eb95.vaers"/>
                        </div>
                    </div>
                </th>
            </g:if>

            <th data-field="newCountVigibase">
                <div class="th-label dateRange" data-field="newCountVigibase">
                    <div class="stacked-cell-center-top">
                        <g:message code="app.label.agg.alert.new.count.vigibase"/>
                    </div>

                    <div class="stacked-cell-center-bottom">
                        <g:message code="app.label.agg.alert.cum.count.vigibase"/>
                    </div>
                </div>
            </th>

            <th data-field="newSeriousCountVigibase">
                <div class="th-label dateRange" data-field="newSeriousCountVigibase">
                    <div class="stacked-cell-center-top">
                        <g:message code="app.label.agg.alert.new.ser.vigibase"/>
                    </div>

                    <div class="stacked-cell-center-bottom">
                        <g:message code="app.label.agg.alert.cumm.ser.vigibase"/>
                    </div>
                </div>
            </th>

            <g:if test="${groupBySmq && isVigibaseEnabled}">
                <th data-idx="21" data-field="eb05Vigibase">
                    <div class="th-label dateRange" data-field="eb05Vigibase">
                        <div class="stacked-cell-center-top">
                            <g:message code="app.label.pe.history.eb05.vigibase"/>
                        </div>

                        <div class="stacked-cell-center-top">
                            <g:message code="app.label.pe.history.eb95.vigibase"/>
                        </div>
                    </div>
                </th>
            </g:if>

            <g:if test="${!groupBySmq && isEvdasEnabled}">
                <th data-field="newEvEvdas">
                    <div class="th-label dateRange" data-field="newEvEvdas">
                        <div class="stacked-cell-center-top">
                            <g:message code="app.label.evdas.details.column.newEvEvdas"/>
                        </div>

                        <div class="stacked-cell-center-bottom">
                            <g:message code="app.label.evdas.details.column.totEvEvdas"/>
                        </div>
                    </div>
                </th>

                <th data-field="dmeImeEvdas">
                    <div class="th-label" data-field="dmeImeEvdas">
                        <g:message code="app.label.imedmeEvdas"/>
                    </div>
                </th>
            </g:if>

        <g:if test="${!groupBySmq}">
            <g:if test="${showEbgm}">
                <th data-idx="21" data-field="eb05">
                    <div class="th-label dateRange" data-field="eb05">
                        <div class="stacked-cell-center-top">
                            <g:message code="app.label.pe.history.eb05"/>
                        </div>

                        <div class="stacked-cell-center-top">
                            <g:message code="app.label.pe.history.eb95"/>
                        </div>
                    </div>
                </th>
            </g:if>

            <g:if test="${!groupBySmq && showEbgmFaers}">
                <th data-idx="21" data-field="eb05Faers">
                    <div class="th-label dateRange" data-field="eb05Faers">
                        <div class="stacked-cell-center-top">
                            <g:message code="app.label.pe.history.eb05.faers"/>
                        </div>

                        <div class="stacked-cell-center-top">
                            <g:message code="app.label.pe.history.eb95.faers"/>
                        </div>
                    </div>
                </th>
            </g:if>
        </g:if>

            <g:if test="${!groupBySmq && isVaersEnabled}">
                <th data-idx="21" data-field="eb05Vaers">
                    <div class="th-label dateRange" data-field="eb05Vaers">
                        <div class="stacked-cell-center-top">
                            <g:message code="app.label.pe.history.eb05.vaers"/>
                        </div>

                        <div class="stacked-cell-center-top">
                            <g:message code="app.label.pe.history.eb95.vaers"/>
                        </div>
                    </div>
                </th>
            </g:if>

            <g:if test="${!groupBySmq && isVigibaseEnabled}">
                <th data-idx="21" data-field="eb05Vigibase">
                    <div class="th-label dateRange" data-field="eb05Vigibase">
                        <div class="stacked-cell-center-top">
                            <g:message code="app.label.pe.history.eb05.vigibase"/>
                        </div>

                        <div class="stacked-cell-center-top">
                            <g:message code="app.label.pe.history.eb95.vigibase"/>
                        </div>
                    </div>
                </th>
            </g:if>

            <g:if test="${!groupBySmq && isEvdasEnabled}">
                <th data-field="sdrEvdas">
                    <div class="th-label dateRange" data-field="sdrEvdas">
                        <div class="stacked-cell-center-top"><g:message
                                code="app.label.evdas.details.column.sdrEvdas"/></div>
                    </div>
                </th>

                <th data-field="rorValueEvdas">
                    <div class="th-label dateRange" data-field="rorValueEvdas">
                        <div class="stacked-cell-center-top">
                            <g:message
                                    code="app.label.evdas.details.column.rorValueEvdas"/>
                        </div>
                    </div>
                </th>
            </g:if>

            <th align="center" data-idx="24" data-field="disposition">
                <div class="th-label" data-field="disposition"><g:message
                        code="app.label.current.disposition"/></div>
            </th>
            <th align="center" data-idx="36" data-field="currentDisposition">
                <div class="th-label" data-field="currentDisposition"><g:message
                        code="app.label.disposition.to"/></div>
            </th>
            <th align="center" data-idx="23" data-field="signalsAndTopics">
                <div class="th-label" data-field="signalsAndTopics"><g:message
                        code="app.label.quantitative.details.column.signalsAndTopics"/></div>
            </th>
            <th data-idx="25" data-field="assignedTo">
                <div class="th-label" data-field="assignedTo"><g:message
                        code="app.label.assigned.to"/></div>
            </th>
            <th data-idx="26" data-field="dueDate">
                <div class="th-label" data-field="dueDate"><g:message
                        code="app.label.quantitative.details.column.dueDate"/></div>
            </th>

            <th data-idx="11" data-field="impEvents">
                <div class="th-label"><g:message code="app.label.quantitative.details.column.impEvents"/></div>
            </th>

            <g:if test="${groupBySmq}">
                <th data-idx="12" data-field="listed">
                    <div class="th-label" data-field="listed"><g:message
                            code="app.label.quantitative.details.column.listed"/></div>
                </th>
            </g:if>

            <th data-idx="13" data-field="newSponCount">
                <div class="th-label dateRange" data-field="newSponCount">
                    <div class="stacked-cell-center-top">
                        <g:message code="app.label.agg.alert.new.spont"/>
                    </div>

                    <div class="stacked-cell-center-bottom">
                        <g:message code="app.label.agg.alert.cumm.spont"/>
                    </div>
                </div>
            </th>

            <th data-idx="15" data-field="newFatalCount">
                <div class="th-label dateRange" data-field="newFatalCount">
                    <div class="stacked-cell-center-top">
                        <g:message code="app.label.agg.alert.new.fatal"/>
                    </div>

                    <div class="stacked-cell-center-bottom">
                        <g:message code="app.label.agg.alert.cumm.fatal"/>
                    </div>
                </div>
            </th>
            <th data-idx="16" data-field="newStudyCount">
                <div class="th-label dateRange" data-field="newStudyCount">
                    <div class="stacked-cell-center-top">
                        <g:message code="app.label.agg.alert.new.study"/>
                    </div>

                    <div class="stacked-cell-center-bottom">
                        <g:message code="app.label.agg.alert.cumm.study"/>
                    </div>
                </div>
            </th>
            <th data-idx="17" data-field="freqPriority">
                <div class="th-label" data-field="freqPriority"><g:message
                        code="app.label.quantitative.details.column.freqPriority"/></div>
            </th>
            <th data-idx="18" data-field="trendType">
                <div class="th-label" data-field="trendType"><g:message
                        code="app.label.quantitative.details.column.trendType"/></div>
            </th>

            <g:if test="${showPrr}">
                <th data-idx="19" data-field="prrValue">
                    <div class="th-label dateRange" data-field="prrValue">
                        <g:message code="app.label.pe.history.prr"/>
                    </div>
                </th>
            </g:if>
            <g:if test="${showRor}">
                <th data-idx="20" data-field="rorValue">
                    <div class="th-label dateRange" data-field="rorValue">
                        <g:if test="${isRor}" >
                            <g:message code="app.label.pe.history.ror"/>
                        </g:if>
                        <g:else>
                            <g:message code="app.label.pe.history.iror"/>
                        </g:else>
                    </div>
                </th>
            </g:if>
            <g:if test="${showPrr && showRor}">
                <th align="center" data-idx="40" data-field="chiSquare">
                    <div class="th-label col-min-120" data-field="chiSquare"><g:message
                            code="app.label.quantitative.details.column.chiSquare"/></div>
                </th>
            </g:if>
            <g:if test="${!groupBySmq}">
                <th data-idx="27" data-field="positiveRechallenge">
                    <div class="th-label" data-field="positiveRechallenge"><g:message
                            code="app.label.quantitative.details.column.positiveRechallenge"/></div>
                </th>
                <th data-idx="28" data-field="positiveDechallenge">
                    <div class="th-label" data-field="positiveDechallenge"><g:message
                            code="app.label.quantitative.details.column.positiveDechallenge"/></div>
                </th>
            </g:if>

            <g:if test="${showPrr}">
                <th data-idx="29" data-field="prrLCI">
                    <div class="th-label dateRange" data-field="prrLCI">
                        <div class="stacked-cell-center-top"><g:message code="app.label.pe.prr05"/></div>

                        <div class="stacked-cell-center-top"><g:message code="app.label.pe.prr95"/></div>
                    </div>
                </th>
            </g:if>
            <g:if test="${showRor}">
                <th data-idx="30" data-field="rorLCI">
                    <div class="th-label dateRange" data-field="rorLCI">
                        <g:if test="${isRor}" >
                            <div class="stacked-cell-center-top"><g:message code="app.label.pe.ror05"/></div>

                            <div class="stacked-cell-center-top"><g:message code="app.label.pe.ror95"/></div>
                        </g:if>
                        <g:else>
                            <div class="stacked-cell-center-top"><g:message code="app.label.pe.iror05"/></div>

                            <div class="stacked-cell-center-top"><g:message code="app.label.pe.iror95"/></div>
                        </g:else>
                    </div>
                </th>
            </g:if>
            <g:if test="${showEbgm}">
                <th data-idx="31" data-field="ebgm">
                    <div class="th-label dateRange" data-field="ebgm">
                        <g:message code="app.label.pe.history.ebgm"/>
                    </div>
                </th>
            </g:if>

            <g:if test="${!groupBySmq}">
                <th data-idx="349" data-field="related">
                    <div class="th-label" data-field="related"><g:message
                            code="app.label.quantitative.details.column.related"/></div>
                </th>
                <th data-idx="35" data-field="pregenency">
                    <div class="th-label" data-field="pregenency"><g:message
                            code="app.label.quantitative.details.column.pregnancy"/></div>
                </th>
            </g:if>

            <th data-idx="38" data-field="newPediatricCount">
                <div class="th-label dateRange" data-field="newPediatricCount">
                    <div class="stacked-cell-center-top">
                        <g:message code="app.label.agg.alert.pdrtc.count"/>
                    </div>

                    <div class="stacked-cell-center-bottom">
                        <g:message code="app.label.agg.alert.cumpdrtc.count"/>
                    </div>
                </div>
            </th>


            <th data-idx="39" data-field="newInteractingCount">
                <div class="th-label dateRange" data-field="newInteractingCount">
                    <div class="stacked-cell-center-top">
                        <g:message code="app.label.agg.alert.intrctng.count"/>
                    </div>

                    <div class="stacked-cell-center-bottom">
                        <g:message code="app.label.agg.alert.cumintrctng.count"/>
                    </div>
                </div>
            </th>

            <th data-idx="350" data-field="newGeriatricCount">
                <div class="th-label dateRange" data-field="newGeriatricCount">
                    <div class="stacked-cell-center-top">
                        <g:message code="app.label.agg.alert.geria.count"/>
                    </div>
                    <div class="stacked-cell-center-bottom">
                        <g:message code="app.label.agg.alert.cumgeria.count"/>
                    </div>
                </div>
            </th>
            <th data-idx="351" data-field="newNonSerious">
                <div class="th-label dateRange" data-field="newNonSerious">
                    <div class="stacked-cell-center-top">
                        <g:message code="app.label.agg.alert.nonserious.count"/>
                    </div>
                    <div class="stacked-cell-center-bottom">
                        <g:message code="app.label.agg.alert.cumnonserious.count"/>
                    </div>
                </div>
            </th>
            <th data-field="justification">
                <div class="th-label" data-field="justification"><g:message
                        code="app.label.qualitative.details.column.justification"/></div>
            </th>
            <th data-field="dispPerformedBy">
                <div class="th-label" data-field="dispPerformedBy"><g:message
                        code="app.label.qualitative.details.column.dispPerformedBy"/></div>
            </th>
            <th data-field="dispLastChange">
                <div class="th-label" data-field="dispLastChange"><g:message
                        code="app.label.qualitative.details.column.dispLastChange"/></div>
            </th>
            <th data-field="comment">
                <div class="th-label" data-field="comment"><g:message
                        code="app.label.qualitative.details.column.comments"/></div>
            </th>

            <g:if test="${isFaersEnabled}">
                <th data-field="newSponCountFaers">
                    <div class="th-label dateRange" data-field="newSponCountFaers">
                        <div class="stacked-cell-center-top">
                            <g:message code="app.label.agg.alert.new.spont.faers"/>
                        </div>

                        <div class="stacked-cell-center-bottom">
                            <g:message code="app.label.agg.alert.cumm.spont.faers"/>
                        </div>
                    </div>
                </th>

                <th data-field="newStudyCountFaers">
                    <div class="th-label dateRange" data-field="newStudyCountFaers">
                        <div class="stacked-cell-center-top">
                            <g:message code="app.label.agg.alert.new.study.faers"/>
                        </div>

                        <div class="stacked-cell-center-bottom">
                            <g:message code="app.label.agg.alert.cumm.study.faers"/>
                        </div>
                    </div>
                </th>

                <th data-field="freqPriorityFaers">
                    <div class="th-label" data-field="freqPriority"><g:message
                            code="app.label.quantitative.details.column.freqPriority.faers"/></div>
                </th>

                <g:if test="${!groupBySmq}">
                    <th data-field="positiveRechallengeFaers">
                        <div class="th-label" data-field="positiveRechallengeFaers"><g:message
                                code="app.label.quantitative.details.column.positiveRechallenge.faers"/></div>
                    </th>
                    <th data-field="positiveDechallengeFaers">
                        <div class="th-label" data-field="positiveDechallengeFaers"><g:message
                                code="app.label.quantitative.details.column.positiveDechallenge.faers"/></div>
                    </th>
                </g:if>


                <g:if test="${showPrrFaers}">
                    <th data-field="prrValueFaers">
                        <div class="th-label dateRange" data-field="prrValueFaers">
                            <div>
                                <g:message code="app.label.pe.history.prr.faers"/>
                            </div>
                        </div>
                    </th>
                    <th data-field="prrLCIFaers">
                        <div class="th-label dateRange" data-field="prrLCIFaers">
                            <div><g:message code="app.label.pe.prr05.faers"/></div>

                            <div><g:message code="app.label.pe.prr95.faers"/></div>
                        </div>
                    </th>
                </g:if>
                <g:if test="${showRorFaers}">
                    <th data-field="rorValueFaers">
                        <div class="th-label dateRange" data-field="rorValueFaers">
                            <g:message code="app.label.pe.history.ror.faers"/>
                        </div>
                    </th>
                    <th data-field="rorLCIFaers">
                        <div class="th-label dateRange" data-field="rorLCIFaers">
                            <div class="stacked-cell-center-top"><g:message code="app.label.pe.ror05.faers"/></div>

                            <div class="stacked-cell-center-top"><g:message code="app.label.pe.ror95.faers"/></div>
                        </div>
                    </th>
                </g:if>
                <th data-field="newPediatricCountFaers">
                    <div class="th-label dateRange" data-field="newPediatricCountFaers">
                        <div class="stacked-cell-center-top">
                            <g:message code="app.label.agg.alert.pdrtc.count.faers"/>
                        </div>

                        <div class="stacked-cell-center-bottom">
                            <g:message code="app.label.agg.alert.cumpdrtc.count.faers"/>
                        </div>
                    </div>
                </th>

                <th data-field="newInteractingCountFaers">
                    <div class="th-label dateRange" data-field="newInteractingCountFaers">
                        <div class="stacked-cell-center-top">
                            <g:message code="app.label.agg.alert.intrctng.count.faers"/>
                        </div>

                        <div class="stacked-cell-center-bottom">
                            <g:message code="app.label.agg.alert.cumintrctng.count.faers"/>
                        </div>
                    </div>
                </th>

                <th data-field="newFatalCountFaers">
                    <div class="th-label dateRange" data-field="newFatalCountFaers">
                        <div class="stacked-cell-center-top">
                            <g:message code="app.label.agg.alert.new.fatal.faers"/>
                        </div>

                        <div class="stacked-cell-center-bottom">
                            <g:message code="app.label.agg.alert.cumm.fatal.faers"/>
                        </div>
                    </div>
                </th>


                <g:if test="${showEbgmFaers}">
                    <th data-idx="31" data-field="ebgmFaers">
                        <div class="th-label dateRange" data-field="ebgmFaers">
                            <g:message code="app.label.pe.history.ebgm.faers"/>
                        </div>
                    </th>
                </g:if>

                <g:if test="${showPrrFaers && showRorFaers}">
                    <th align="center" data-field="chiSquareFaers">
                        <div class="th-label" data-field="chiSquareFaers">
                            <g:message code="app.label.quantitative.details.column.chiSquare.faers"/>
                        </div>
                    </th>
                </g:if>

                <g:if test="${!groupBySmq}">


                    <th data-field="relatedFaers">
                        <div class="th-label" data-field="relatedFaers"><g:message
                                code="app.label.quantitative.details.column.related.faers"/></div>
                    </th>
                    <th data-field="pregenencyFaers">
                        <div class="th-label" data-field="pregenencyFaers"><g:message
                                code="app.label.quantitative.details.column.pregnancy.faers"/></div>
                    </th>
                </g:if>


                <th data-field="trendTypeFaers">
                    <div class="th-label" data-field="trendTypeFaers">
                        <g:message code="app.label.quantitative.details.column.trendType.faers"/>
                    </div>
                </th>

                <th data-field="newGeriatricCountFaers">
                    <div class="th-label dateRange" data-field="newGeriatricCountFaers">
                        <div class="stacked-cell-center-top">
                            <g:message code="app.label.agg.alert.geria.count.faers"/>
                        </div>
                        <div class="stacked-cell-center-bottom">
                            <g:message code="app.label.agg.alert.cumgeria.count.faers"/>
                        </div>
                    </div>
                </th>
                <th data-field="newNonSeriousFaers">
                    <div class="th-label dateRange" data-field="newNonSeriousFaers">
                        <div class="stacked-cell-center-top">
                            <g:message code="app.label.agg.alert.nonserious.count.faers"/>
                        </div>
                        <div class="stacked-cell-center-bottom">
                            <g:message code="app.label.agg.alert.cumnonserious.count.faers"/>
                        </div>
                    </div>
                </th>
            </g:if>

            <g:if test="${groupBySmq && isEvdasEnabled}">
                <th data-field="newEvEvdas">
                    <div class="th-label dateRange" data-field="newEvEvdas">
                        <div class="stacked-cell-center-top">
                            <g:message code="app.label.evdas.details.column.newEvEvdas"/>
                        </div>

                        <div class="stacked-cell-center-bottom">
                            <g:message code="app.label.evdas.details.column.totEvEvdas"/>
                        </div>
                    </div>
                </th>

                <th data-field="dmeImeEvdas">
                    <div class="th-label" data-field="dmeImeEvdas">
                        <g:message code="app.label.imedmeEvdas"/>
                    </div>
                </th>

                <th data-field="sdrEvdas">
                    <div class="th-label dateRange" data-field="sdrEvdas">
                        <div class="stacked-cell-center-top"><g:message
                                code="app.label.evdas.details.column.sdrEvdas"/></div>
                    </div>
                </th>

                <th data-field="rorValueEvdas">
                    <div class="th-label dateRange" data-field="rorValueEvdas">
                        <div class="stacked-cell-center-top">
                            <g:message
                                    code="app.label.evdas.details.column.rorValueEvdas"/>
                        </div>
                    </div>
                </th>
            </g:if>

            <g:if test="${isEvdasEnabled}">
                <th data-field="hlgtEvdas"><div class="th-label" data-field="hlgtEvdas"><g:message
                        code="app.label.evdas.details.column.hlgtEvdas"/></div></th>
                <th data-field="hltEvdas"><div class="th-label" data-field="hltEvdas"><g:message
                        code="app.label.evdas.details.column.hltEvdas"/></div></th>
                <th data-field="smqNarrowEvdas"><div class="th-label" data-field="smqNarrowEvdas"><g:message
                        code="app.label.evdas.details.column.smqNarrowEvdas"/></div></th>
                <th data-field="newEeaEvdas">
                    <div class="th-label dateRange" data-field="newEeaEvdas">
                        <div class="stacked-cell-center-top">
                            <g:message code="app.label.evdas.details.column.newEeaEvdas"/>
                        </div>

                        <div class="stacked-cell-center-bottom">
                            <g:message code="app.label.evdas.details.column.totEeaEvdas"/>
                        </div>
                    </div>
                </th>
                <th data-field="newHcpEvdas">
                    <div class="th-label dateRange" data-field="newHcpEvdas">
                        <div class="stacked-cell-center-top">
                            <g:message code="app.label.evdas.details.column.newHcpEvdas"/>
                        </div>

                        <div class="stacked-cell-center-bottom">
                            <g:message code="app.label.evdas.details.column.totHcpEvdas"/>
                        </div>
                    </div>
                </th>
                <th data-field="newSeriousEvdas">
                    <div class="th-label dateRange" data-field="newSeriousEvdas">
                        <div class="stacked-cell-center-top">
                            <g:message code="app.label.evdas.details.column.newSeriousEvdas"/>
                        </div>

                        <div class="stacked-cell-center-bottom">
                            <g:message code="app.label.evdas.details.column.totSeriousEvdas"/>
                        </div>
                    </div>
                </th>
                <th data-field="newMedErrEvdas">
                    <div class="th-label dateRange" data-field="newMedErrEvdas">
                        <div class="stacked-cell-center-top">
                            <g:message code="app.label.evdas.details.column.newMedErrEvdas"/>
                        </div>

                        <div class="stacked-cell-center-bottom">
                            <g:message code="app.label.evdas.details.column.totMedErrEvdas"/>
                        </div>
                    </div>
                </th>
                <th data-field="newObsEvdas">
                    <div class="th-label dateRange" data-field="newObsEvdas">
                        <div class="stacked-cell-center-top">
                            <g:message code="app.label.evdas.details.column.newObsEvdas"/>
                        </div>

                        <div class="stacked-cell-center-bottom">
                            <g:message code="app.label.evdas.details.column.totObsEvdas"/>
                        </div>
                    </div>
                </th>
                <th data-field="newFatalEvdas">
                    <div class="th-label dateRange" data-field="newFatalEvdas">
                        <div class="stacked-cell-center-top">
                            <g:message code="app.label.evdas.details.column.newFatalEvdas"/>
                        </div>

                        <div class="stacked-cell-center-bottom">
                            <g:message code="app.label.evdas.details.column.totFatalEvdas"/>
                        </div>
                    </div>
                </th>
                <th data-field="newRcEvdas">
                    <div class="th-label dateRange" data-field="newRcEvdas">
                        <div class="stacked-cell-center-top">
                            <g:message code="app.label.evdas.details.column.newRcEvdas"/>
                        </div>

                        <div class="stacked-cell-center-bottom">
                            <g:message code="app.label.evdas.details.column.totRcEvdas"/>
                        </div>
                    </div>
                </th>
                <th data-field="newLitEvdas">
                    <div class="th-label dateRange" data-field="newLitEvdas">
                        <div class="stacked-cell-center-top">
                            <g:message code="app.label.evdas.details.column.newLitEvdas"/>
                        </div>

                        <div class="stacked-cell-center-bottom">
                            <g:message code="app.label.evdas.details.column.totLitEvdas"/>
                        </div>
                    </div>
                </th>
                <th data-field="newPaedEvdas">
                    <div class="th-label dateRange" data-field="newPaedEvdas">
                        <div class="stacked-cell-center-top">
                            <g:message code="app.label.evdas.details.column.newPaedEvdas"/>
                        </div>

                        <div class="stacked-cell-center-bottom">
                            <g:message code="app.label.evdas.details.column.totPaedEvdas"/>
                        </div>
                    </div>
                </th>
                <th data-field="ratioRorPaedVsOthersEvdas">
                    <div class="th-label dateRange" data-field="ratioRorPaedVsOthersEvdas">
                        <g:message code="app.label.evdas.details.column.ratioRorPaedVsOthersEvdas"/>
                    </div>
                </th>
                <th data-field="newGeriaEvdas">
                    <div class="th-label dateRange" data-field="newGeriaEvdas">
                        <div class="stacked-cell-center-top">
                            <g:message code="app.label.evdas.details.column.newGeriaEvdas"/>
                        </div>

                        <div class="stacked-cell-center-bottom">
                            <g:message code="app.label.evdas.details.column.totGeriaEvdas"/>
                        </div>
                    </div>
                </th>
                <th data-field="ratioRorGeriatrVsOthersEvdas">
                    <div class="th-label dateRange" data-field="ratioRorGeriatrVsOthersEvdas">
                        <g:message code="app.label.evdas.details.column.ratioRorGeriatrVsOthersEvdas"/>
                    </div>
                </th>
                <th data-field="sdrGeratrEvdas">
                    <div class="th-label dateRange" data-field="sdrGeratrEvdas">
                        <g:message code="app.label.evdas.details.column.sdrGeratrEvdas"/>
                    </div>
                </th>
                <th data-field="newSpontEvdas">
                    <div class="th-label dateRange" data-field="newSpontEvdas">
                        <div class="stacked-cell-center-top">
                            <g:message code="app.label.evdas.details.column.newSpontEvdas"/>
                        </div>

                        <div class="stacked-cell-center-bottom">
                            <g:message code="app.label.evdas.details.column.totSpontEvdas"/>
                        </div>
                    </div>
                </th>
                <th data-field="totSpontEuropeEvdas">
                    <div class="th-label dateRange" data-field="totSpontEuropeEvdas">
                        <g:message code="app.label.evdas.details.column.totSpontEuropeEvdas"/>
                    </div>
                </th>
                <th data-field="totSpontNAmericaEvdas">
                    <div class="th-label dateRange" data-field="totSpontNAmericaEvdas">
                        <g:message code="app.label.evdas.details.column.totSpontNAmericaEvdas"/>
                    </div>
                </th>
                <th data-field="totSpontJapanEvdas">
                    <div class="th-label dateRange" data-field="totSpontJapanEvdas">
                        <g:message code="app.label.evdas.details.column.totSpontJapanEvdas"/>
                    </div>
                </th>
                <th data-field="totSpontAsiaEvdas">
                    <div class="th-label dateRange" data-field="totSpontAsiaEvdas">
                        <g:message code="app.label.evdas.details.column.totSpontAsiaEvdas"/>
                    </div>
                </th>
                <th data-field="totSpontRestEvdas">
                    <div class="th-label dateRange" data-field="totSpontRestEvdas">
                        <g:message code="app.label.evdas.details.column.totSpontRestEvdas"/>
                    </div>
                </th>
                <th data-field="sdrPaedEvdas">
                    <div class="th-label dateRange" data-field="sdrPaedEvdas">
                        <g:message code="app.label.evdas.details.column.sdrPaedEvdas"/>
                    </div>
                </th>
                <th data-field="europeRorEvdas">
                    <div class="th-label dateRange" data-field="europeRorEvdas">
                        <g:message code="app.label.evdas.details.column.europeRorEvdas"/>
                    </div>
                </th>
                <th data-field="northAmericaRorEvdas">
                    <div class="th-label dateRange" data-field="northAmericaRorEvdas">
                        <g:message code="app.label.evdas.details.column.northAmericaRorEvdas"/>
                    </div>
                </th>
                <th data-field="japanRorEvdas">
                    <div class="th-label dateRange" data-field="japanRorEvdas">
                        <g:message code="app.label.evdas.details.column.japanRorEvdas"/>
                    </div>
                </th>
                <th data-field="asiaRorEvdas">
                    <div class="th-label dateRange" data-field="asiaRorEvdas">
                        <g:message code="app.label.evdas.details.column.asiaRorEvdas"/>
                    </div>
                </th>
                <th data-field="restRorEvdas">
                    <div class="th-label dateRange" data-field="restRorEvdas">
                        <g:message code="app.label.evdas.details.column.restRorEvdas"/>
                    </div>
                </th>
                <th data-field="changesEvdas">
                    <div class="th-label dateRange" data-field="changesEvdas">
                        <g:message code="app.label.evdas.details.column.changesEvdas"/>
                    </div>
                </th>
            </g:if>

            <th data-field="newFatalCountVaers">
                <div class="th-label dateRange" data-field="newFatalCountVaers">
                    <div class="stacked-cell-center-top">
                        <g:message code="app.label.agg.alert.new.fatal.vaers"/>
                    </div>

                    <div class="stacked-cell-center-bottom">
                        <g:message code="app.label.agg.alert.cumm.fatal.vaers"/>
                    </div>
                </div>
            </th>


            <th data-field="newGeriatricCountVaers">
                <div class="th-label dateRange" data-field="newGeriatricCountVaers">
                    <div class="stacked-cell-center-top">
                        <g:message code="app.label.agg.alert.geria.count.vaers"/>
                    </div>
                    <div class="stacked-cell-center-bottom">
                        <g:message code="app.label.agg.alert.cumgeria.count.vaers"/>
                    </div>
                </div>
            </th>

            <th data-field="newPediatricCountVaers">
                <div class="th-label dateRange" data-field="newPediatricCountVaers">
                    <div class="stacked-cell-center-top">
                        <g:message code="app.label.agg.alert.pdrtc.count.vaers"/>
                    </div>

                    <div class="stacked-cell-center-bottom">
                        <g:message code="app.label.agg.alert.cumpdrtc.count.vaers"/>
                    </div>
                </div>
            </th>

        <g:if test="${showPrrVaers}">
            <th data-field="prrValueVaers">
                <div class="th-label dateRange" data-field="prrValueVaers">
                    <div>
                        <g:message code="app.label.pe.history.prr.vaers"/>
                    </div>
                </div>
            </th>
        </g:if>

        <g:if test="${showRorVaers}">
            <th data-field="rorValueVaers">
                <div class="th-label dateRange" data-field="rorValueVaers">
                    <g:message code="app.label.pe.history.ror.vaers"/>
                </div>
            </th>
        </g:if>

        <g:if test="${showEbgmVaers}">
            <th data-idx="31" data-field="ebgmVaers">
                <div class="th-label dateRange" data-field="ebgmVaers">
                    <g:message code="app.label.pe.history.ebgm.vaers"/>
                </div>
            </th>
        </g:if>

        <g:if test="${showPrrVaers}">
            <th data-field="prrLCIVaers">
                <div class="th-label dateRange" data-field="prrLCIVaers">
                    <div><g:message code="app.label.pe.prr05.vaers"/></div>

                    <div><g:message code="app.label.pe.prr95.vaers"/></div>
                </div>
            </th>
        </g:if>

        <g:if test="${showRorVaers}">
            <th data-field="rorLCIVaers">
                <div class="th-label dateRange" data-field="rorLCIVaers">
                    <div class="stacked-cell-center-top"><g:message code="app.label.pe.ror05.vaers"/></div>

                    <div class="stacked-cell-center-top"><g:message code="app.label.pe.ror95.vaers"/></div>
                </div>
            </th>
        </g:if>

        <g:if test="${showPrrVaers && showRorVaers}">
            <th align="center" data-field="chiSquareVaers">
                <div class="th-label col-min-120" data-field="chiSquareVaers">
                    <g:message code="app.label.quantitative.details.column.chiSquare.vaers"/>
                </div>
            </th>
        </g:if>

            <th data-field="newFatalCountVigibase">
                <div class="th-label dateRange" data-field="newFatalCountVigibase">
                    <div class="stacked-cell-center-top">
                        <g:message code="app.label.agg.alert.new.fatal.vigibase"/>
                    </div>

                    <div class="stacked-cell-center-bottom">
                        <g:message code="app.label.agg.alert.cumm.fatal.vigibase"/>
                    </div>
                </div>
            </th>


            <th data-field="newGeriatricCountVigibase">
                <div class="th-label dateRange" data-field="newGeriatricCountVigibase">
                    <div class="stacked-cell-center-top">
                        <g:message code="app.label.agg.alert.geria.count.vigibase"/>
                    </div>
                    <div class="stacked-cell-center-bottom">
                        <g:message code="app.label.agg.alert.cumgeria.count.vigibase"/>
                    </div>
                </div>
            </th>

            <th data-field="newPediatricCountVigibase">
                <div class="th-label dateRange" data-field="newPediatricCountVigibase">
                    <div class="stacked-cell-center-top">
                        <g:message code="app.label.agg.alert.pdrtc.count.vigibase"/>
                    </div>

                    <div class="stacked-cell-center-bottom">
                        <g:message code="app.label.agg.alert.cumpdrtc.count.vigibase"/>
                    </div>
                </div>
            </th>

            <g:if test="${showPrrVigibase}">
                <th data-field="prrValueVigibase">
                    <div class="th-label dateRange" data-field="prrValueVigibase">
                        <div>
                            <g:message code="app.label.pe.history.prr.vigibase"/>
                        </div>
                    </div>
                </th>
            </g:if>

            <g:if test="${showRorVigibase}">
                <th data-field="rorValueVaers">
                    <div class="th-label dateRange" data-field="rorValueVigibase">
                        <g:message code="app.label.pe.history.ror.vigibase"/>
                    </div>
                </th>
            </g:if>

            <g:if test="${showEbgmVigibase}">
                <th data-idx="31" data-field="ebgmVigibase">
                    <div class="th-label dateRange" data-field="ebgmVigibase">
                        <g:message code="app.label.pe.history.ebgm.vigibase"/>
                    </div>
                </th>
            </g:if>

            <g:if test="${showPrrVigibase}">
                <th data-field="prrLCIVigibase">
                    <div class="th-label dateRange col-min-100" data-field="prrLCIVigibase">
                        <div><g:message code="app.label.pe.prr05.vigibase"/></div>

                        <div><g:message code="app.label.pe.prr95.vigibase"/></div>
                    </div>
                </th>
            </g:if>

            <g:if test="${showRorVigibase}">
                <th data-field="rorLCIVaers">
                    <div class="th-label dateRange" data-field="rorLCIVigibase">
                        <div class="stacked-cell-center-top"><g:message code="app.label.pe.ror05.vigibase"/></div>

                        <div class="stacked-cell-center-top"><g:message code="app.label.pe.ror95.vigibase"/></div>
                    </div>
                </th>
            </g:if>

            <g:if test="${showPrrVigibase && showRorVigibase}">
                <th align="center" data-field="chiSquareVigibase">
                    <div class="th-label col-min-120" data-field="chiSquareVigibase">
                        <g:message code="app.label.quantitative.details.column.chiSquare.vigibase"/>
                    </div>
                </th>
            </g:if>

            <g:each in="${subGroupsColumnList}" var="category">
                <th data-field="ebgm${category}">
                    <div class="th-label dateRange" data-field="ebgm${category}">
                        <div class="stacked-cell-center-top">
                            EBGM(${category[0].toUpperCase() + category.substring(1).replaceAll('_', ' ')})
                        </div>
                    </div>
                </th>
                <th data-field="eb05${category}">
                    <div class="th-label dateRange" data-field="eb05${category}">
                        <div class="stacked-cell-center-top">
                            EB05(${category[0].toUpperCase() + category.substring(1).replaceAll('_', ' ')})
                        </div>
                    </div>
                </th>
                <th data-field="eb95${category}">
                    <div class="th-label dateRange" data-field="eb95${category}">
                        <div class="stacked-cell-center-top">
                            EB95(${category[0].toUpperCase() + category.substring(1).replaceAll('_', ' ')})
                        </div>
                    </div>
                </th>
            </g:each>

            <g:if test="${isFaersEnabled}">
                <g:each in="${faersSubGroupsColumnList}" var="category">
                    <th data-field="ebgm${category}Faers">
                        <div class="th-label dateRange" data-field="ebgm${category}Faers">
                            <div class="stacked-cell-center-top">
                                EBGM(${category[0].toUpperCase() + category.substring(1).replaceAll('_', ' ')}) (F)
                            </div>
                        </div>
                    </th>
                    <th data-field="eb05${category}Faers">
                        <div class="th-label dateRange" data-field="eb05${category}Faers">
                            <div class="stacked-cell-center-top">
                                EB05(${category[0].toUpperCase() + category.substring(1).replaceAll('_', ' ')}) (F)
                            </div>
                        </div>
                    </th>
                    <th data-field="eb95${category}Faers">
                        <div class="th-label dateRange" data-field="eb95${category}Faers">
                            <div class="stacked-cell-center-top">
                                EB95(${category[0].toUpperCase() + category.substring(1).replaceAll('_', ' ')}) (F)
                            </div>
                        </div>
                    </th>

                </g:each>
            </g:if>
            <g:render template="/includes/widgets/prevColumns"
                      model="[callingScreen: callingScreen, listDateRange: listDateRange, prevColCount: prevColCount, prevColumns: prevColumns, showPrrVaers: showPrrVaers, showRorVaers: showRorVaers, isVaersEnabled: isVaersEnabled,
                              showEbgmVaers: showEbgmVaers, showPrr      : showPrr, showRor: showRor, showEbgm: showEbgm, prevFaersDate: prevFaersDate, prevVaersDate: prevVaersDate, prevEvdasDate: prevEvdasDate]"/>
            <g:render template="/includes/widgets/prevColumns1"
                      model="[callingScreen: callingScreen, listDateRange: listDateRange, prevColCount: prevColCount,
                              showPrrVigibase: showPrrVigibase, showRorVigibase: showRorVigibase, isVigibaseEnabled: isVigibaseEnabled, showEbgmVigibase: showEbgmVigibase, prevVigibaseDate: prevVigibaseDate]"/>
        </tr>
        </thead>
    </table>
</div>