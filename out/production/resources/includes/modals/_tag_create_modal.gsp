<div class="modal fade" id="tag-create-modal" tabindex="-1" role="dialog" aria-hidden="true">
    <div class="modal-dialog modal-sm">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title"><g:message code="app.label.create.tag"></g:message></h4>
            </div>

            <div class="modal-body">
                <div class="row">
                    <div class="alert alert-danger" style="display:none;"></div>
                </div>
                <div class="row">
                    <div class="col-md-12 form-group">
                        <label><g:message code="app.label.tag.name"></g:message><span class="required-indicator">*</span></label>
                        <input type="text" class="form-control" id="tagName"/>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <div class="buttons">
                    <button type="button" class="btn btn-primary" id="saveAlertTag" style="display: none">
                        Create
                    </button>
                    <button type="button" class="btn btn-primary" id="saveSystemTag" style="display: none">
                        Create
                    </button>
                    <button type="button" class="btn btn-default" data-dismiss="modal">
                        Close
                    </button>

                </div>
            </div>
        </div>
    </div>
</div>
<script>
    $(document).ready(function () {
        $('#saveAlertTag').on('click', function () {
            var request = new Object();
            request['name'] = $("#tagName").val();
            $.ajax({
                url : saveUrl,
                type: "POST",
                data: request,
                dataType: "json",
                success : function (data) {
                    if(data.success){
                        location.reload();
                    }else{
                        $('#tag-create-modal .alert-danger').html(data.errorMessage);
                        $('#tag-create-modal .alert-danger').show()
                    }
                }
            })
        });
        $('#saveSystemTag').on('click', function () {
            var request = new Object();
            request['name'] = $("#tagName").val();
            $.ajax({
                url : sysTagSaveUrl,
                type: "POST",
                data: request,
                dataType: "json",
                success : function (data) {
                    if(data.success){
                        location.reload();
                    }else{
                        $('#tag-create-modal .alert-danger').html(data.errorMessage);
                        $('#tag-create-modal .alert-danger').show()
                    }
                }
            })
        });
        $('#tag-create-modal').on('hidden.bs.modal', function () {
            $('#saveAlertTag, #saveSystemTag').hide();
            $('#tag-create-modal .alert-danger').hide()
        })
    })
</script>