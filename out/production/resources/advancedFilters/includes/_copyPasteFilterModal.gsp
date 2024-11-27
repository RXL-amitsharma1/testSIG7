<style>
#delimiter-options hr{
    margin-top: 20px !important;
    margin-bottom: 20px !important;
}
</style>
<!-- Modal for copy and paste -->
<div class="modal fade copyAndPasteModal" id="copyAndPasteModal"  data-backdrop="static" tabindex="-1" role="dialog" aria-labelledby="Copy/Paste Dialog">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" onclick='closeAllCopyPasteModals();' aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="myModalLabel"><g:message code="paste.or.import.values" />:</h4>
            </div>
            <div class="modal-body container-fluid copy-paste-modal-height-fix">
                <input type="hidden" name="containerId" id="containerId" value=""/>
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
                    <textarea id="copyPasteContent"></textarea>
                </div>
                <div class="row" style="text-align: right; width: 100%;padding-top: 5px;margin-top:8px">
                    <button type="button" class="btn btn-default validate-copy-paste" ><g:message
                            code="copy.paste.modal.validate.checkbox"/></button>
                    <hr>
                </div>

                <div class="row">
                    <span><g:message code="copy.paste.modal.import.values.from.file"/></span>
                </div>

                <div id="importValueSection" class="importValueSection">
                    <div class="row">
                        <div class="input-group col-xs-10">
                            <input type="text" class="form-control" readonly>
                            <label class="input-group-btn">
                                <span class="btn btn-primary">
                                    Choose File&hellip; <input type="file" id="file_input_filter"
                                                               accept="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet, application/vnd.ms-excel"
                                                               style="display: none;">
                                </span>
                            </label>
                        </div>
                    </div>

                    <div id="fileFormatError" hidden="hidden">
                        <div class="row">
                            <div class="col-xs-12" style="color: #ff0000">
                                <g:message code="copy.paste.modal.invalid.file.format.error"/>!
                            </div>
                        </div>
                    </div>
                    <div id="noDataInExcel" hidden="hidden" class="noDataInExcel" style="color: #ff0000"></div>

                    <div class="row">
                        <div class="col-xs-11 bs-callout bs-callout-info">
                            <h5><g:message code="app.label.note"/></h5>

                            <div><g:message code="copy.paste.modal.import.values.from.file"/>:</div>

                            <div><g:message code="copy.paste.modal.values.imported.from.first.worksheet"/></div>

                            <div><g:message code="copy.paste.modal.file.have.one.column"/></div>

                            <div><g:message code="copy.paste.modal.values.in.separate.row"/></div>

                            <div><g:message code="copy.paste.modal.values.first.row.label"/></div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-default cancel" onclick='closeAllCopyPasteModals();'><g:message
                        code="default.button.cancel.label"/></button>
                <button type="button" class="btn btn-success confirm-paste"
                        onclick='closeAllCopyPasteModals();'><g:message code="default.button.confirm.label"/></button>
                <button type="button" class="btn btn-success import-values" style="display: none"><g:message
                        code="default.button.import.label"/></button>
            </div>
        </div>
    </div>
</div>
<div id="importValuesTemplateContainer">
</div>