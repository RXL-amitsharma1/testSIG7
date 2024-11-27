<asset:javascript src="app/pvs/alertComments/alertComments.js"/>
<script>
    $(document).ready(function() {
        var caseJson = {
            "alertType": $("#appType").val(),
            "validatedSignalId":$(".comment").find("#validatedSignalId").html()
        };
        signal.alertComments.populate_comments($(".comment"), caseJson, true);
        signal.alertComments.save_comment($(".comment"));
    })
</script>
<div class="rxmain-container ">
   <div class="rxmain-container-inner">
        <div class="rxmain-container-row rxmain-container-header">
            <label class="rxmain-container-header-label">
                Comments
            </label>
        </div>
        <div class="rxmain-container-content comment">
            <div id="alert-comment-container">
                <g:textArea maxlength="4000" name="commentbox" id="commentbox"
                            placeholder="Please enter your comment here." class="form-control"/>
            </div>

            <span class="createdBy"></span>

            <button type="button" class="btn btn-primary add-comments pull-right m-t-10">
                    <g:message code="default.button.add.label"/>
            </button>
            <br>
            <br>
            <input type="hidden" id="commentId"/>
            <span id="application" class="hide">${com.rxlogix.Constants.AlertConfigType.SIGNAL_MANAGEMENT}</span>
            <input id="appType" type="hidden" value='${com.rxlogix.Constants.AlertConfigType.SIGNAL_MANAGEMENT}' />
            <span id="validatedSignalId" class="hidden">${signal.id}</span>
        </div>
   </div>
</div>



