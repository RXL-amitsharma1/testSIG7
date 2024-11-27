
<input type="hidden" name="hyperlinkConfiguration" id="hyperlinkConfiguration" value="${hyperlinkConfiguration as grails.converters.JSON}">
<div class="rxmain-container-inner panel panel-default m-b-5">
    <div class="rxmain-container-row rxmain-container-header panel-heading pv-sec-heading">
        <label class="rxmain-container-header-label">
            <a data-toggle="collapse" href="#accordion-pvs-aggreview">
                <g:message code="app.validatedSignal.aggregated.case.review"/>
            </a>
        </label>
        <span class="pv-head-config configureFields">
            <a href="javascript:void(0);" class="ic-sm action-search-btn" title="" data-original-title="Search">
                <i class="md md-search" aria-hidden="true"></i>
            </a>
        </span>
    </div>

    <div class="panel-collapse rxmain-container-content rxmain-container-show collapse in pv-scrollable-dt"
         id="accordion-pvs-aggreview">
        <table id="rxTableAggregateReview" class=" dataTable row-border hover table-hd-less-gap dropdown-outside">
            <thead>
            <tr>
                <g:if test="${labelConfigCopy.name}">
                    <th>
                        <div class="th-label">
                            ${labelConfig.name}
                        </div>
                    </th>
                </g:if>
                <g:if test="${labelConfigCopy.productName}">
                    <th>
                        <div class="th-label">
                            ${labelConfig.productName}
                        </div>
                    </th>
                </g:if>


                <g:if test="${labelConfigCopy.soc}">
                    <th>
                        <div class="th-label">
                            ${labelConfig.soc}
                        </div>
                    </th>
                </g:if>



                <th>
                    <div class="th-label">
                        Event PT
                    </div>
                </th>


                <g:if test="${labelConfigCopy.newCount}">
                    <th>
                        <div class="stacked-cell-center-top">
                            ${labelConfig.newCount.split("/")[0]}
                        </div>

                        <div class="stacked-cell-center-bottom">
                            ${labelConfig.newCount.split("/")[1]}
                        </div>
                    </th>
                </g:if>

                <g:if test="${labelConfigCopy.newSeriousCount}">
                    <th>
                        <div class="stacked-cell-center-top">
                            ${labelConfig.newSeriousCount.split("/")[0]}
                        </div>

                        <div class="stacked-cell-center-bottom">
                            ${labelConfig.newSeriousCount.split("/")[1]}
                        </div>
                    </th>

                </g:if>

                <g:if test="${labelConfigCopy.prrValue}">
                    <th style="width: 5%">
                        <div class="th-label">
                            ${labelConfig.prrValue}
                        </div>
                    </th>

                </g:if>

                <g:if test="${labelConfigCopy.rorValue}">
                    <g:if test="${isRor == true}">
                        <th style="width: 5%">
                            <div class="th-label">
                                ${labelConfig.rorValue.split("#OR")[0]}
                            </div>
                        </th>
                    </g:if>
                    <g:else>
                        <th style="width: 5%">
                            <div class="th-label">
                                ${labelConfig.rorValue.split("#OR")[1]}
                            </div>
                        </th>
                    </g:else>
                </g:if>

                <g:if test="${labelConfigCopy.ebgm}">
                    <th style="width: 5%">
                        <div class="th-label">
                            ${labelConfig.ebgm}
                        </div>
                    </th>
                </g:if>
                <g:if test="${labelConfigCopy.eb05}">
                    <th style="width: 5%">
                        <div class="stacked-cell-center-top">
                            ${labelConfig.eb05.split("/")[0]}
                        </div>

                        <div class="stacked-cell-center-top">
                            ${labelConfig.eb05.split("/")[1]}
                        </div>
                    </th>
                </g:if>
                <th style="width: 5%">
                    <div class="th-label">
                        Data Source
                    </div>
                </th>
                <g:if test="${labelConfigCopy.disposition}">
                    <th>
                        <div class="th-label">
                            ${labelConfig.disposition}
                        </div>
                    </th>
                </g:if>
                <th>
                    <div class="th-label">
                        History
                    </div>
                </th>
                <th></th>
                <th></th>
            </tr>
            </thead>
        </table>

    </div>
</div>

<g:render template="/includes/modals/case_drill_down"/>
<g:render template="/includes/modals/product_event_history_modal"/>
<g:render template="/includes/modals/evdas_history_modal"/>
<g:render template="/includes/modals/alert_comment_modal"/>

