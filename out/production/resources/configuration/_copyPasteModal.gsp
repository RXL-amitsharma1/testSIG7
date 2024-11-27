<div class="modal fade copyAndPasteModal" id="copyAndPasteDicModal" tabindex="-1" data-backdrop="static" role="dialog" aria-labelledby="Copy/Paste Dialog">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" onclick='closeAllCopyPasteModals();' aria-label="Close">
                    <span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="myModalLabel"><g:message code="paste.values" />:</h4>
            </div>
            <div class="modal-body container-fluid">
                <div class="row">
                    <label><g:message code="app.delimiters" />:</label>
                </div>
                <div class="row" id="delimiter-options">
                    <div class="icon-col" title="No delimiters">
                        <label class="no-bold add-cursor">
                            <input type="radio" name="delimiter" value="none" checked="checked"/>
                            <g:message code="app.label.none"/>
                        </label>
                    </div>
                    <div class="icon-col" title="comma">
                        <label class="no-bold add-cursor">
                            <input type="radio" name="delimiter" value=","/>
                            <g:message code="app.label.comma"/>
                        </label>
                    </div>
                    <div class="icon-col" title="semi-colon">
                        <label class="no-bold add-cursor">
                            <input type="radio" name="delimiter" value=";"/>
                            <g:message code="app.label.semi.colon"/>
                        </label>
                    </div>
                    <div class="icon-col" title="space">
                        <label class="no-bold add-cursor">
                            <input type="radio" name="delimiter" value=" "/>
                            <g:message code="app.label.space"/>
                        </label>
                    </div>
                    <div class="icon-col" title="new-line">
                        <label class="no-bold add-cursor">
                            <input type="radio" name="delimiter" value="\n"/>
                            <g:message code="app.label.new.line"/>
                        </label>
                    </div>
                    <div class="icon-col" title="Others">
                        <label class="no-bold add-cursor">
                            <input type="radio" name="delimiter" value="others"/>
                            <g:message code="app.others"/>
                        </label>
                    </div>
                    <div class="icon-col">
                        <input type="text" class="c_n_p_other_delimiter" >
                    </div>
                </div>
                <div class="row content-row">
                    <textarea class="copyPasteContent"></textarea>
                </div>
                <div class="row">
                </div>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-default cancel" onclick='closeAllCopyPasteModals();'><g:message
                        code="default.button.cancel.label"/></button>
                <button type="button" class="btn btn-success confirm-paste-dic-values"
                        onclick='closeAllCopyPasteModals();'><g:message code="default.button.confirm.label"/></button>
            </div>
        </div>
    </div>
</div>
