<div class="modal fade" id="addCaseModal" tabindex="-1" role="dialog" aria-hidden="true">
    <div class="vertical-alignment-helper">

        <!-- Modal Dialog starts -->
        <div class="modal-dialog vertical-align-center">

            <div class="modal-content">
                <div class="modal-header dropdown">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                    <label class="modal-title"><g:message code="app.label.add.case"/></label>
                </div>

                <div class="modal-body case-list-modal-body">
                    <div class="alert alert-danger hide">
                        <span class="errorMessageSpan"></span>
                    </div>

                    <g:uploadForm name="addNewCase">
                        <div class="row">
                            <div class="col-md-12">
                                <label for="caseNumber"><g:message code="app.label.case.number"/><span
                                        class="required-indicator">*</span></label>
                                <g:hiddenField name="executedConfigId" value="${executedConfigId}"/>
                                <g:textField id="caseNumber" class="form-control"
                                             name="caseNumber"/>
                            </div>
                        </div><br>

                        <div class="checkbox checkbox-primary">
                            <g:checkBox name="importCasesExcel"/>
                            <label for="importCasesExcel">
                                <g:message code="app.label.import.cases.excel"/>
                            </label>
                        </div>

                        <div id="importCasesSection" hidden="hidden">
                            <div class="row">
                                <div class="input-group col-xs-10">
                                    <input type="text" class="form-control" readonly>
                                    <label class="input-group-btn">
                                        <span class="btn btn-primary">
                                            Choose File&hellip; <input type="file" id="file_input"
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

                            <div id="noDataInExcel" hidden="hidden" style="color: #ff0000"></div>

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

                        <div class="row">
                            <div class="col-md-4">
                                <label for="justificationListPriority">Justification Template</label>
                                <g:select id="justificationListPriority" name="justificationListPriority"
                                          from="${justification}" optionKey="id"
                                          optionValue="name"
                                          noSelection="${['': message(code: 'select.one')]}" class="form-control"/>
                              </div>
                        </div>
                        <br/>

                        <div class="row">
                            <div class="col-md-12 form-group">
                                <label for="justification">Justification<span class="required-indicator">*</span>
                                </label>
                                <g:textArea id="justification" class="form-control col-sm-4" style="height: 150px;"
                                            name="justification"/>
                            </div>
                        </div>
                        <div class="isProcessing" style="display: none; align-items: center;
                        position: absolute;
                        top: 50%;
                        left: 50%;
                        transform: translate(-50%, -50%)"><asset:image src="spinner.gif" width="30"/></div>
                    </g:uploadForm>
                </div>

                <div class="modal-footer">
                    <div class="buttons creationButtons">
                        <input id="addCaseButton" type="button" class="btn btn-primary add-case-to-list"
                               value="${message(code: "default.button.add.label")}">
                        <button type="button" class="btn btn-default close-add-case" data-dismiss="modal"><g:message
                                code="default.button.close.label"/></button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

