<div class="modal fade " id="alertTagModal" role="dialog">
    <div class="modal-dialog modal-md modal-width-sm" role="document">

        <div class="modal-content">

            <div class="rxmain-container-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <label class="modal-title"><g:message code="app.label.alert.tag"/></label>
            </div>

            <div class="modal-body">

                <div class="row">
                    <div class="col-md-12">
                        <label for="singleAlertTags"><g:message code="app.tag.caseSeries.label" /></label>
                        <select id="singleAlertTags" class="form-control" name="singleAlertTags" multiple="multiple">
                        </select>
                    </div>
                </div>
                <br/>
                <div class="row">
                    <div class="col-md-12">
                        <label for="globalTags"><g:message code="app.tag.global.label" /></label>
                        <select id="globalTags" class="form-control" name="globalTags" multiple="multiple">
                        </select>
                    </div>
                </div>

            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-primary addTags" data-dismiss="modal"><g:message
                        code="default.button.update.label"/></button>
                <button type="button" class="btn btn-default closeGenericValues" data-dismiss="modal"><g:message
                        code="default.button.close.label"/></button>

            </div>
        </div>
    </div>
</div>