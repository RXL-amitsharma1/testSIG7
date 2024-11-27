<div class="modal fade" id="relatedCaseSeriesModal" class="relatedCaseSeriesModal" tabindex="-1" role="dialog" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span></button>
                <label class="modal-title">Linked Case Series for <span id="caseNumber">${caseNumber}</span></label>
                </button>
            </div>

            <div class="modal-body">
                <table id="relatedCaseSeriesTable" class="auto-scale row-border no-shadow hover" width="100%">
                    <thead>
                    <tr>
                        <th><g:message code="caseDetails.caseSeries.name"/></th>
                        <th><g:message code="caseDetails.product"/></th>
                        <th><g:message code="caseDetails.description"/></th>
                        <th><g:message code="caseDetails.criteria"/></th>
                        <th><g:message code="caseDetails.last.executed"/></th>
                    </tr>
                    <thead>
                </table>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">
                    <g:message code="default.button.close.label"/>
                </button>
            </div>
        </div>
    </div>
</div>
