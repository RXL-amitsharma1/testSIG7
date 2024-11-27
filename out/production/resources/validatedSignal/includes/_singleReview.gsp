<div class="rxmain-container-inner panel panel-default m-b-5">
    <div class="rxmain-container-row rxmain-container-header panel-heading pv-sec-heading">
        <label class="rxmain-container-header-label">
            <a data-toggle="collapse" href="#accordion-pvs-singlereview">
                <g:message code="app.validatedSignal.single.case.review" />
            </a>
        </label>
        <span class="pv-head-config configureFields">
            <a href="javascript:void(0);" class="ic-sm action-search-btn" title="" data-original-title="Search">
                <i class="md md-search" aria-hidden="true"></i>
            </a>
             <sec:ifAnyGranted roles="ROLE_ADMIN, ROLE_DEV,ROLE_CONFIGURATION_CRUD,ROLE_SINGLE_CASE_CONFIGURATION,ROLE_AGGREGATE_CASE_CONFIGURATION, ROLE_LITERATURE_CASE_CONFIGURATION,ROLE_SIGNAL_MANAGEMENT_CONFIGURATION,ROLE_SIGNAL_MANAGEMENT_REVIEWER,ROLE_AD_HOC_CRUD, ROLE_SINGLE_CASE_CONFIGURATION,ROLE_SINGLE_CASE_REVIEWER,ROLE_EVDAS_CASE_CONFIGURATION, ROLE_VAERS_CONFIGURATION,ROLE_FAERS_CONFIGURATION,  ROLE_VIGIBASE_CONFIGURATION">
                 <a href="javascript:void(0)" class="pull-right ic-sm addCaseSignal" id="addCaseSignal" title="Add Case">
                        <i class="md md-add" aria-hidden="true"></i>
                  </a>
             </sec:ifAnyGranted>
        </span>
    </div>
    <div class="panel-collapse rxmain-container-content rxmain-container-show collapse in pv-scrollable-dt" id="accordion-pvs-singlereview">
        <table id="rxTableSingleReview" class="row-border hover">
            <thead>
            <tr>
                <th style="width:13%">
                    <div class="th-label">
                        Alert Name
                    </div>
                </th>
                <th style="width:5%">
                    <div class="th-label">
                        Priority
                    </div>
                </th>
                <th style="width:9%">
                    <div class="th-label">
                        <g:if test="${columnLabelForSCA.containsKey('caseNumber')}">
                            ${columnLabelForSCA.get('caseNumber')}
                        </g:if>
                        <g:else>
                            Case Number
                        </g:else>
                    </div>
                </th>
                <th style="width:10%">
                    <div class="th-label">
                        <g:if test="${columnLabelForSCA.containsKey('productName')}">
                            ${columnLabelForSCA.get('productName')}
                        </g:if>
                        <g:else>
                            Product Name
                        </g:else>
                    </div>
                </th>
                <th>
                    <div>
                        <div class="th-label">
                            <g:if test="${columnLabelForSCA.containsKey('masterPrefTermAll')}">
                                ${columnLabelForSCA.get('masterPrefTermAll')}
                            </g:if>
                            <g:else>
                                Event PT
                            </g:else>
                        </div>
                    </div>

                </th>
                <th style="width:10%">
                    <div class="th-label">
                        Disposition
                    </div>
                </th>
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
<g:render template="/includes/modals/case_history_modal" />
<g:render template="/includes/modals/alert_comment_modal" />


