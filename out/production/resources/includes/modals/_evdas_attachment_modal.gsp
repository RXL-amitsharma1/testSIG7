<%@ page import="com.rxlogix.Constants" %>
<div class="modal" id="evdasAttachmentModal" tabindex="-1" role="dialog" aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content" style="margin-left: 0;margin-right: 0">
            <div class="modal-header">
                <button type="button" class="close"  id="evdas-attachment-modal-close" aria-label="Close">
                    <span aria-hidden="true">×</span></button>
                <h4 class="modal-title " style="font-weight: bold"> Description : </h4>
            </div>
            <div class="modal-body">
                <div id="evdas-attachment-container" class="list">
                    <div class="form-group">
                        <g:textArea maxlength="8000" name="evdas-attachment-box" id="evdas-attachment-box" rows="12" placeholder="Add Description" class="form-control height-100"/>
                    </div>
                </div>

                <span class="createdBy"></span>

                <input type="hidden" id="commentId"/>
                <input type="hidden" id="caseId"/>
                <input type="hidden" id="versionNum"/>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-primary add-evdas-attachment ${buttonClass}">
                    <g:message code="default.button.add.label"/>
                </button>
                <button type="button" class="btn btn-default evdas-attachment-modal-close" data-dismiss="modal">
                    Cancel
                </button>
            </div>
        </div>
        <div class="hidden">
            <span id="executedConfigId"></span>
            <span id="configId"></span>

        </div>
    </div>
</div>

<script>
    $('.evdas-attachment-modal-close , #evdas-attachment-modal-close').click(function () {
        $('#evdasAttachmentModal').css({display: 'none'});
        $('#evdas-attachment-box').val('');
    });
</script>