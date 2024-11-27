<div class="modal fade modal-xlg" data-backdrop="static" id="caseHistoryModal" tabindex="-1" role="dialog" aria-hidden="true">
    <div class="modal-dialog" >
        <div class="modal-content">
            <div class="modal-header">
                <label class="modal-title"><g:message code="app.label.case.history.label" />: <span id="caseNumber"></span>-<span id="productFamily"></span>
                </label>
                <input type="hidden" id="caseVersion" />
                <input type="hidden" id="productName" />
                <input type="hidden" id="alertConfigId" />
                <input type="hidden" id="pt" />
                <g:hiddenField name="exeConfigId" id="exeConfigId" value="${executedConfigId}" />
                <button type="button" class="close pull-right" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span></button>
                <g:render template="/includes/widgets/export_panel" model="[controller:'caseHistory',
                                                                            action:'exportReport',
                                                                            extraParams: [alertName:'caseHistory', isArchived: isArchived, exeConfigId: executedConfigId, isVaers: isVaers,isFaers:isFaers, isVigibase:isVigibase]]"/>
            </div>

            <div class="modal-body">

                <div id="case-history-container" class="list">
                    <label class="modal-title m-b-15"><g:message code="caseDetails.review.history.current"/></label>
                    <br/>
                    <table class="table caseHistoryModalTable" id="caseHistoryModalTable" style="width: 100%">
                        <thead>
                        <tr>
                            <th><g:message code="app.label.alert.name"/></th>
                            <th><g:if test="${isCaseVersion && !isFaers && !isVaers && !isVigibase}">
                                <g:message code="${"app.label.qualitative.details.column.caseNumber.version"}"/>
                            </g:if><g:else>
                                <g:message code="${isVaers ? "app.label.qualitative.details.column.caseNumber.vaers": isVigibase ? "app.label.qualitative.details.column.caseNumber.vigibase":"app.label.qualitative.details.column.caseNumber"}"/>
                            </g:else>
                            </th>
                            <th><g:message code="app.label.disposition" /></th>
                            <th><g:message code="app.label.justification"/></th>
                            <th><g:message code="app.label.priority"/></th>
                            <th><g:message code="app.label.tag.column"/></th>
                            <th><g:message code="app.label.subTag.column"/></th>
                            <th><g:message code="app.label.performed.by"/></th>
                            <th><g:message code="app.label.date"/></th>
                        </tr>
                        <thead>
                        <tbody id="caseHistoryModalTableBody" class="tableModalBody"></tbody>
                    </table>
                </div>
                <br/>
                <div id="case-history-container_suspect" class="list">
                    <label class="modal-title m-b-15"><g:message code="caseDetails.review.history.suspect"/></label>
                    <br/>
                    <table class="table" id="caseHistoryModalTableSuspect" style="width: 100%">
                        <thead>
                        <tr>
                            <th><g:message code="app.label.alert.name"/></th>
                            <th><g:if test="${isCaseVersion && !isFaers && !isVaers && !isVigibase}">
                                <g:message code="${"app.label.qualitative.details.column.caseNumber.version"}"/>
                            </g:if><g:else>
                                <g:message code="${isVaers ? "app.label.qualitative.details.column.caseNumber.vaers": isVigibase ? "app.label.qualitative.details.column.caseNumber.vigibase":"app.label.qualitative.details.column.caseNumber"}"/>
                            </g:else>
                            </th>
                            <th><g:message code="app.label.disposition" /></th>
                            <th><g:message code="app.label.justification"/></th>
                            <th><g:message code="app.label.priority"/></th>
                            <th><g:message code="app.label.tag.column"/></th>
                            <th><g:message code="app.label.subTag.column"/></th>
                            <th><g:message code="app.label.performed.by"/></th>
                            <th><g:message code="app.label.date"/></th>
                        </tr>
                        <thead>
                        <tbody id="caseHistoryModalTableBody2" class="tableModalBody"></tbody>
                    </table>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default case-history-modal-close" data-dismiss="modal">
                    <g:message code="default.button.close.label"/>
                </button>
            </div>
        </div>
    </div>
</div>