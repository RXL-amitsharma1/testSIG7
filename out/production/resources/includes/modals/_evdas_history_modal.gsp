<div class="modal fade" data-backdrop="static" id="evdasHistoryModal" tabindex="-1" role="dialog" aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content" >
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span></button>
                <label class="modal-title">EVDAS History : <span id="productName"></span>-<span id="eventName"></span>
                </label>
            </button>
            </div>
            <div class="modal-body">

                <div class="row">
                    <div id="evdas-history-container" class="row m-b-15" >
                        <table class="table" id="evdasHistoryModalTable" style="width: 100%">
                            <thead>
                            <tr>
                                <th><g:message code="app.label.disposition" /></th>
                                <th><g:message code="app.label.priority"/></th>
                                <th><g:message code="app.label.assigned.to"/></th>
                                <th><g:message code="app.label.case.history.time.stamp"/></th>
                                <th><g:message code="app.label.case.history.performaed.by"/></th>
                                <th><g:message code="app.label.case.history.justification"/></th>
                            </tr>
                            <thead>
                            <tbody id="evdasHistoryModalTableBody" class="tableModalBody"></tbody>
                        </table>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default evdas-history-modal-close" data-dismiss="modal">
                    <g:message code="default.button.close.label"/>
                </button>
            </div>
        </div>
    </div>
</div>
