<g:javascript>
     var labelsMap = '${groovy.json.JsonOutput.toJson(nameToLabelMap)}';
</g:javascript>
<div class="rxmain-container-inner panel panel-default m-b-5" id="adhocObservation">
    <div class="rxmain-container-row rxmain-container-header panel-heading pv-sec-heading">
        <label class="rxmain-container-header-label">
            <a data-toggle="collapse" href="#accordion-pvs-adhocreview">
                <g:message code="ad.hoc.validatedSignal.review.label" />
            </a>

        </label>
        <span class="pv-head-config configureFields">
            <a href="javascript:void(0);" class="ic-sm action-search-btn" title="" data-original-title="Search">
                <i class="md md-search" aria-hidden="true"></i>
            </a>
        </span>
    </div>

    <div class="panel-collapse rxmain-container-content rxmain-container-show collapse in pv-scrollable-dt" id="accordion-pvs-adhocreview">
        <table id="rxTableAdHocReview" class="table table-striped pv-list-table row-border hover" width="100%">
            <thead>
                <tr>
                    <g:if test="${nameToLabelMap?.containsKey('name')}">
                        <th>${nameToLabelMap.get('name')}</th>
                    </g:if>
                    <g:if test="${nameToLabelMap?.containsKey('productSelection')}">
                        <th>${nameToLabelMap.get('productSelection')}</th>
                    </g:if>
                    <g:if test="${nameToLabelMap?.containsKey('eventSelection')}">
                        <th>${nameToLabelMap.get('eventSelection')}</th>
                    </g:if>
                    <g:if test="${nameToLabelMap?.containsKey('detectedBy')}">
                        <th>${nameToLabelMap.get('detectedBy')}</th>
                    </g:if>
                    <g:if test="${nameToLabelMap?.containsKey('initialDataSource')}">
                        <th>${nameToLabelMap.get('initialDataSource')}</th>
                    </g:if>
                    <g:if test="${nameToLabelMap?.containsKey('disposition')}">
                        <th>${nameToLabelMap.get('disposition')}</th>
                    </g:if>
                    <th></th>
                </tr>
            </thead>
        </table>
    </div>
</div>
