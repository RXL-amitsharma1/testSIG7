<div class="modal fade" data-backdrop="static" id="action-edit-modal" role="dialog"
     aria-hidden="true">
    <div class="modal-dialog modal-lg actionClass">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id=""><g:message code="app.action.edit.label"/></h4>
            </div>

            <div class="modal-body clearfix">
                <div id="action-editor-container">
                </div>
            </div>
            <input type="hidden" id="actionMeetingId" value="${meetingId}"
                   class="action-meeting-id"/>
            <div class="modal-footer">
                <div class="buttons ">
                    <button class="button  btn btn-primary ${buttonClass}" id="update-bt">Update</button>
                    <button class="button btn btn-default " id="cancel-bt">Cancel</button>
                </div>
            </div>
        </div>
    </div>
</div>