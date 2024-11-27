<style>
.popover { pointer-events: none; }
</style>
<div class="modal fade" id="eventGroupModalAssessment" data-backdrop="static" tabindex="-1" role="dialog"
     aria-labelledby="eventGroupLabelAssessment" aria-hidden="true">
    <div class="modal-dialog modal-lg event-group-modal-dialog" style="max-width: 800px" role="document">
        <div class="modal-content">
            <div class="modal-header"><button type="button" class="close closeEventGroupModalAssessment" aria-label="Close"><span
                    aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="eventGroupLabelAssessment"><g:message
                        code="app.label.eventGroup.create"/></h4>
            </div>

            <div class="modal-body container-fluid">
                <div class="row">
                    <div class="col-md-5">
                        <div class="form-group">
                            <label><g:message code="app.label.eventGroup.name"/><span class="required-indicator">*</span></label>
                            <input type="text" id="eventGroupNameAssessment" class="eventGroupNameAssessment form-control"/>
                        </div>

                        <div class="form-group">
                            <label> <g:message code="app.shareWith"/><span class="required-indicator">*</span></label>
                            <g:select id="eventGroupShareWithAssessment"
                                      name="eventGroupShareWithAssessment"
                                      from=""
                                      optionKey="userName"
                                      optionValue="fullNameAndUserName"
                                      value=""
                                      class="form-control" multiple="true"/>
                        </div>
                    </div>
                    <div class="col-md-7">
                        <div class="form-group">
                            <div class="form-group description-wrapper">
                                <label> <g:message code="app.description"/></label>
                                <g:textArea rows="5" cols="3" name="eventGroupDescription" id="eventGroupDescriptionAssessment" class="eventGroupDescriptionAssesment form-control" maxlength="4000" style="height: 110px"/>
                            </div>
                        </div>
                        <g:hiddenField name="eventGroupId" id="eventGroupIdAssessment" value=""/>
                    </div>
                </div>
            </div>

            <div class="modal-footer">
                <button type="button" id="deleteEventGroupAssessment" class="btn btn-primary">
                    <g:message code="default.button.delete.label"/>
                </button>
                <button type="button" class="btn btn-primary" id="saveEventGroupAssessment" accesskey="*"><g:message
                        code="default.button.save.label"/></button>
                <button type="button" class="btn btn-default closeEventGroupModalAssessment"><g:message
                        code="default.button.close.label"/></button>
            </div>
        </div>
    </div>
</div>
<g:render template="/includes/widgets/deleteEventGroupModalAssessment"/>