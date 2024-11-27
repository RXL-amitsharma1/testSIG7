<%@ page import="com.rxlogix.pvdictionary.config.PVDictionaryConfig" %>
<div class="modal fade" id="eventModalAssessment" data-backdrop="static" tabindex="-1" role="dialog"
     aria-labelledby="eventDictionaryLabel"
     aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="eventDictionaryLabelAssessment"><g:message
                        code="app.reportField.eventDictId.label"/></h4>
            </div>

            <div class="modal-body">
                <g:render template="includes/assessmentEventDictionaryTemplate"
                          model="[column_names: PVDictionaryConfig.EventConfig.columns]"/>
            </div>

            <div class="modal-footer">
                <div class="errorMessage" hidden="hidden">Cannot add to different levels.</div>

                <div class="loading" style="display:none"><asset:image src="select2-spinner.gif" height="16"
                                                                       width="16"/></div>
                %{--<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>--}%
                <button type="button" class="btn btn-default clearEventValuesAssessment" accesskey="*"><g:message
                        code="default.button.clear.label"/></button>
                <button type="button" class="btn btn-primary addEventValuesAssessment" accesskey="%"><g:message
                        code="default.button.add.label"/></button>
                <button type="button" class="btn btn-primary addAllEventValuesAssessment" accesskey="^"><g:message
                        code="app.label.add.all"/></button>
                <button type="button" class="btn btn-primary createEventGroupAssessment" disabled accesskey="*"><g:message
                        code="app.label.new.event.group"/></button>
                <button type="button" class="btn btn-primary updateEventGroupAssessment" disabled accesskey="&"><g:message
                        code="app.label.update.event.group"/></button>
                <button type="button" class="btn btn-default addAllEventsAssessment" data-dismiss="modal" accesskey="&"><g:message
                        code="default.button.close.label"/></button>
            </div>
        </div>
    </div>
</div>
<g:render template="/includes/widgets/eventGroupModalAssessment"/>
