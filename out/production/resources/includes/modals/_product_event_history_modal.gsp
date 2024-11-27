    <div class="modal fade modal-xlg" data-backdrop="static" id="productEventHistoryModal" tabindex="-1" role="dialog" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <label class="modal-title"><g:message code="app.label.peHistory.modal.title"/>  : <span id="productName"></span>-<span
                        id="eventName"></span>
                </label>
                <input type="hidden" id="configId"/>
                <button type="button" class=" pull-right close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span></button>
                <g:render template="/includes/widgets/export_panel" model="[controller:'productEventHistory',
                                                                            action:'exportReport',
                                                                            extraParams: [alertName: 'productEventHistory']]"/>
            </div>
            <div class="modal-body">
                <div class="box">
                    <label><g:message code="app.label.current.alert.history"/></label>
                    <div id="current-alert-history-container" class="list m-t-15" >
                        <table class="table" id="currentAlertHistoryModalTable" style="width: 100%">
                            <thead>
                            <tr>
                                <th><g:message code="app.label.alert.name"/></th>
                                <th><g:message code="app.label.review.period.days"/></th>
                                <th><g:message code="app.label.disposition" /></th>
                                <th><g:message code="app.label.justification"/></th>
                                <th><g:message code="app.label.priority"/></th>
                                <th><g:message code="app.label.tag.column"/></th>
                                <th><g:message code="app.label.subTag.column"/></th>
                                <th><g:message code="app.label.performed.by"/></th>
                                <th><g:message code="app.label.date"/></th>
                            </tr>
                            <thead>
                            <tbody id="currentAlertHistoryModalTableBody" class="tableModalBody"></tbody>
                        </table>
                    </div>
                </div>
                <div class="box m-t-15">
                    <label><g:message code="app.label.other.alerts.history"/></label>
                    <div id="other-alerts-history-container" class="list m-t-15" >
                        <table class="table" id="otherAlertsHistoryModalTable" style="width: 100%">
                            <thead>
                            <tr>
                                <th><g:message code="app.label.alert.name"/></th>
                                <th><g:message code="app.label.review.period.days"/></th>
                                <th><g:message code="app.label.disposition" /></th>
                                <th><g:message code="app.label.justification"/></th>
                                <th><g:message code="app.label.priority"/></th>
                                <th><g:message code="app.label.tag.column"/></th>
                                <th><g:message code="app.label.subTag.column"/></th>
                                <th><g:message code="app.label.performed.by"/></th>
                                <th><g:message code="app.label.date"/></th>
                            </tr>
                            <thead>
                            <tbody id="otherAlertsHistoryModalTableBody" class="tableModalBody"></tbody>
                        </table>
                    </div>
                </div>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-default product-event-history-modal-close" data-dismiss="modal">
                    <g:message code="default.button.close.label"/>
                </button>
            </div>
        </div>
    </div>
</div>
