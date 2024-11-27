<div class="modal fade modal-xlg" id="literatureHistoryModal" data-backdrop="static" tabindex="-1" role="dialog" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <label class="modal-title"><g:message code="app.label.literature.history.label"/> <span
                        id="articleID"></span>
                </label>
                <input type="hidden" id="articleId"/>
                <input type="hidden" id="alertConfigId"/>
                <button type="button" class="close pull-right" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span></button>
                <g:render template="/includes/widgets/export_panel" model="[controller : 'literatureHistory',
                                                                            action     : 'exportReport',
                                                                            extraParams: [alertName: 'literatureHistory']]"/>
            </div>

            <div class="modal-body">

                <div id="literature-history-container" class="list">
                    <label class="modal-title m-b-15"><g:message code="literature.currentArticle.history"/></label>
                    <br/>
                    <table class="table literatureHistoryModalTable" id="literatureHistoryModalTable"
                           style="width: 100%">
                        <thead>
                        <tr>
                            <th><g:message code="app.label.alert.name"/></th>
                            <th><g:message code="app.label.disposition"/></th>
                            <th><g:message code="app.label.justification"/></th>
                            <th><g:message code="app.label.priority"/></th>
                            <th><g:message code="app.label.tag.column"/></th>
                            <th><g:message code="app.label.subTag.column"/></th>
                            <th><g:message code="app.label.performed.by"/></th>
                            <th><g:message code="app.label.date"/></th>
                        </tr>
                        <thead>
                        <tbody id="literatureHistoryModalTableBody" class="tableModalBody"></tbody>
                    </table>
                </div>
                <br/>

                <div id="literature-history-container_suspect" class="list">
                    <label class="modal-title m-b-15"><g:message
                            code="literature.currentArticle.history.suspect"/></label>
                    <br/>
                    <table class="table" id="literatureHistoryModalTableSuspect" style="width: 100%">
                        <thead>
                        <tr>
                            <th><g:message code="app.label.alert.name"/></th>
                            <th><g:message code="app.label.disposition"/></th>
                            <th><g:message code="app.label.justification"/></th>
                            <th><g:message code="app.label.priority"/></th>
                            <th><g:message code="app.label.tag.column"/></th>
                            <th><g:message code="app.label.subTag.column"/></th>
                            <th><g:message code="app.label.performed.by"/></th>
                            <th><g:message code="app.label.date"/></th>
                        </tr>
                        <thead>
                        <tbody id="literatureHistoryModalTableBody2" class="tableModalBody"></tbody>
                    </table>
                </div>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-default literature-history-modal-close" data-dismiss="modal">
                    <g:message code="default.button.close.label"/>
                </button>
            </div>
        </div>
    </div>
</div>