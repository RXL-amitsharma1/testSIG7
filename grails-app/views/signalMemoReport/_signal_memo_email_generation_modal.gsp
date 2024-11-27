<div id="emailGenerationModal" class="modal fade" role="dialog">
    <div class="modal-dialog modal-lg">

        <!-- Modal content-->
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <label class="modal-title"><g:message code="app.label.signal.memo.email"/></label>
            </div>

                <div class="modal-body">
                    <div class="row">
                        <div class="col-sm-12">
                            <div class="row m-t-10">
                                <div class="col-md-12 required">
                                    <label for="sentTo" class="control-label lbl-elipsis"><g:message
                                            code="email.generation.label.to"/></label>
                                    <g:initializeEmailForSignalMemo assignedToId="assignedToId"   id="sentTo" isLabel="false" isTags="true"  class ="" bean="${signalMemo}"/>
                                </div>
                            </div>


                            <div class="row m-t-10 mailSubject">
                                <div class="subject col-md-12 required">
                                    <label for="subject" class="control-label lbl-elipsis"><g:message
                                            code="email.generation.label.subject"/></label>
                                    <g:textField id="subject" value=""  maxlength="255"
                                                 class="form-control fm-text-area p-t-0 email-subject" name="email-subject"/>
                                </div>
                            </div><br>

                            <div class="modalFUQMsg col-md-12 required message">
                                <label for="emailContentMessage" class="control-label"><g:message
                                        code="email.generation.label.message" default="Email Content"/></label>
                                <textarea id="emailContentMessage" name="emailContentMessage" cols="100" rows="15"
                                          class="form-control hide email-body" name="email-body" disabled></textarea>
                            </div>

                        </div>
                    </div>
                </div>

                <div class="modal-footer">
                    <button type="button" class="btn pv-btn-dark-grey waves-effect" id="emailGenResetBtn"><g:message
                            code="email.generation.label.reset"/></button>
                    <button data-dismiss="modal" class="btn pv-btn-grey waves-effect btn-default" id="cancelButton"><g:message
                            code="email.generation.label.cancel"/></button>
                    <button type="button" class="btn btn-primary" id="saveEmail"><g:message
                            code="default.button.save.label"/></button>
                    <g:hiddenField name="memoId" id="signalMemoId" value=""/>

                </div>

        </div>
    </div>
</div>


