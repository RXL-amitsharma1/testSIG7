<div class="modal fade" id="showConfigNameModal" tabindex="-1" role="dialog" aria-hidden="true">
    <div class="modal-dialog modal-md">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span></button>
                <label class="modal-title">Save Case Series</label>
            </div>

            <div class="modal-body">
                <div class="row">
                    <div class="alert alert-success alert-dismissible" id="alertMessage" role="alert" hidden>
                        <button type="button" class="close" data-dismiss="alert">
                            <span aria-hidden="true">&times;</span>
                            <span class="sr-only"><g:message code="default.button.close.label" /></span>
                        </button>
                        <g:message code="app.label.save.case.series" />
                    </div>
                    <div class="alert alert-danger alert-dismissible" id="errorMessage" role="alert" hidden>
                        <button type="button" class="close" data-dismiss="alert">
                            <span aria-hidden="true">&times;</span>
                            <span class="sr-only"><g:message code="default.button.close.label" /></span>
                        </button>
                        <g:message code="app.label.save.case.series.failed" />
                    </div>
                </div>
                <div class="row">
                    <div class="col-md-6">
                        <div class="form-group">
                            <label for="configName">Case Series Name</label>
                            <input type="text" class="form-control" id="configName">
                        </div>
                    </div>
                </div>
            </div>

            <div class="modal-footer">
                <div class="buttons ">
                    <button class="button btn btn-primary" id="save-cases-btn">Save</button>
                    <button class="button btn btn-default" id="cancel-btn">Cancel</button>
                </div>
            </div>
        </div>
    </div>
</div>
<script>
    $("#cancel-btn").on('click', function () {
        $("#showConfigNameModal").modal('hide')
    })
</script>