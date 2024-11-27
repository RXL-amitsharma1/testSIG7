<div class="rxmain-container-inner panel panel-default m-b-5">
    <div class="rxmain-container-row rxmain-container-header panel-heading pv-sec-heading">
        <label class="rxmain-container-header-label">
            <a data-toggle="collapse" href="#accordion-pvs-litereview">
                <g:message code="app.validatedSignal.new.literature.review" />
            </a>
        </label>
        <span class="pv-head-config configureFields">
            <a href="javascript:void(0);" class="ic-sm action-search-btn" title="" data-original-title="Search">
                <i class="md md-search" aria-hidden="true"></i>
            </a>
        </span>
    </div>
    <div class="panel-collapse rxmain-container-content rxmain-container-show collapse in pv-scrollable-dt" id="accordion-pvs-litereview">
        <table id="rxTableLiteratureReview" class="dataTable row-border hover table-hd-less-gap no-footer" width="100%">
            <thead>
            <tr>
                <th><g:message code="app.label.literature.details.column.name"/></th>
                <th><g:message code="app.label.priority"/></th>
                <th><g:message code="app.label.literature.details.column.title"/></th>
                <th><g:message code="app.label.literature.details.column.authors"/></th>
                <th><g:message code="app.label.literature.details.column.publication.date"/></th>
                <th><g:message code="app.label.disposition"/></th>
                <th></th>
                <th></th>
            </tr>
            </thead>
        </table>

    </div>
</div>
<g:render template="/includes/modals/alert_comment_modal" />
