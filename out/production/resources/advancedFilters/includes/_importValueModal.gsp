<!-- Modal for import values -->
<g:javascript>
var validValues="${validValues.join(';')}"
</g:javascript>
<div class="modal fade importValueModal" id="importValueModal${qevId}"  data-backdrop="static" data-backdrop="static" tabindex="-1" role="dialog"
     aria-labelledby="Paste/Paste Dialog">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" onclick='closeAllImportValueModal();' aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="myModalLabel"><g:message code="paste.or.import.values"/>:</h4>
            </div>

            <div class="modal-body container-fluid" style="max-height: 400px; overflow-x: hidden; overflow-y: scroll;">

                <div class="row">
                    <span><g:message code="app.label.values.successfully.imported"/> ${validValues.size()}</span>
                </div>

                <div class="row">
                    <span><g:message code="app.label.warnings"/></span><a id="showWarnings" class="add-cursor"> ${invalidValues.size() + duplicateValues.size()}</a>
                </div>

                <div class="row invalidValuesContainer" hidden="hidden">
                    <label><g:message code="app.label.invalid.values"/></label>

                    <div class="invalidValues">
                        <g:each in="${invalidValues}" var="invalid">
                            <div class="invalidValue">${invalid}</div>
                        </g:each>
                    </div>
                </div>
                <div class="row invalidValuesContainer" hidden="hidden">
                    <label><g:message code="app.label.duplicate.values"/></label>

                    <div class="duplicateValues">
                        <g:each in="${duplicateValues}" var="invalid">
                            <div class="duplicateValue">${invalid}</div>
                        </g:each>
                    </div>
                </div>

            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-default cancel"
                        onclick='closeAllImportValueModal();'><g:message code="default.button.cancel.label"/></button>
                <button type="button" class="btn btn-success confirm-import"
                        onclick='closeAllImportValueModal();'><g:message code="default.button.confirm.label"/></button>
            </div>
        </div>
    </div>
</div>