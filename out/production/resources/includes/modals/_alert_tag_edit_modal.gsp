<div class="modal fade" id="alert-tag-edit-modal" tabindex="-1" role="dialog" aria-hidden="true">
    <div class="modal-dialog modal-sm">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title"><g:message code="app.label.edit.tag"></g:message></h4>
            </div>

            <div class="modal-body">
                <div class="row">
                    <div class="alert alert-danger" style="display:none;"></div>
                </div>
                <div class="row">
                    <div class="col-md-12 form-group">
                        <label><span class="required-indicator">*</span></label>
                        <label><g:message code="app.label.edit.tag.name"></g:message><span class="required-indicator">*</span></label>
                        <input type="text" class="form-control" id="alertTagName"/>
                    </div>
                </div>
                <input type="hidden" id="alertTagId"/>
            </div>
            <div class="modal-footer">
                <div class="buttons">
                    <button type="button" class="btn btn-primary" id="editAlertTag" style="display: none">
                        Update
                    </button>
                    <button type="button" class="btn btn-primary" id="editSystemTag" style="display: none">
                        Update
                    </button>
                    <button type="button" class="btn btn-default" data-dismiss="modal">
                        Close
                    </button>

                </div>
            </div>
        </div>
    </div>
</div>


<div class="modal fade" id="tag-delete-modal" tabindex="-1" role="dialog" aria-hidden="true">
    <div class="modal-dialog modal-sm">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title">Delete Tag</h4>
            </div>

            <div class="modal-body">
                <div class="row">
                    <div class="col-md-12">
                        <span class="error-message"></span>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <div class="buttons">
                    <button type="button" class="btn btn-primary" data-dismiss="modal">
                        OK
                    </button>
                </div>
            </div>
        </div>
    </div>
</div>
<script>
    $(document).ready(function () {
        $('#editAlertTag').on('click', function () {
            var request = new Object();
            request['name'] = $("#alertTagName").val();
            request['id'] = $("#alertTagId").val();
            $.ajax({
                url : editUrl,
                type: "POST",
                data: request,
                dataType: "json",
                success : function (data) {
                    if(data.success){
                        location.reload();
                    }else{
                        $('#alert-tag-edit-modal .alert-danger').html(data.errorMessage);
                        $('#alert-tag-edit-modal .alert-danger').show()
                    }
                }
            })
        });
        $('#editSystemTag').on('click', function () {
            var request = new Object();
            request['name'] = $("#alertTagName").val();
            request['id'] = $("#alertTagId").val();
            $.ajax({
                url : sysTagEditUrl,
                type: "POST",
                data: request,
                dataType: "json",
                success : function (data) {
                    if(data.success){
                        location.reload();
                    }else{
                        $('#alert-tag-edit-modal .alert-danger').html(data.errorMessage);
                        $('#alert-tag-edit-modal .alert-danger').show()
                    }
                }
            })
        });
        $('#alert-tag-edit-modal').on('hidden.bs.modal', function () {
            $('#editAlertTag, #editSystemTag').hide();
            $('#alert-tag-edit-modal .alert-danger').hide()
        })
    })
</script>