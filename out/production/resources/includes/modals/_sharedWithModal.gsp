<div class="modal fade" id="sharedWithModal"  data-backdrop="static" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="myModalLabel"><g:message code="share.with.users" /></h4>
            </div>

            <div class="modal-body">
                <div class="row">
                    <div class="col-sm-6">
                        <label style="padding-left: 5px;"><g:message code="group.label"/>:</label>
                        <div id="sharedWithGroupList" style="padding-left: 5px;word-break:break-all"></div>
                    </div>
                    <div class="col-sm-6">
                        <label style="padding-left: 5px;"><g:message code="user.label"/>:</label>
                        <div id="sharedWithUserList" style="padding-left: 5px;"></div>
                    </div>
                </div>
                <div>
                    <g:initializeShareWithElement/>
                </div>
            </div>
            <div class="modal-footer">
                <g:actionSubmit class="btn btn-primary shareBtn" action="editShare" value="${message(code: 'default.button.update.label')}" />
                <button type="button" class="btn btn-default" data-dismiss="modal">
                    <g:message code="default.button.cancel.label" />
                </button>
            </div>
        </div>
    </div>
</div>