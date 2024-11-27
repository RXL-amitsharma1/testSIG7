<div class="modal fade" id="studyModal" tabindex="-1" role="dialog" aria-labelledby="studyDictionaryLabel"
     aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="studyDictionaryLabel"><g:message
                        code="app.reportField.studyDictionary"/>
                    <span id="fetchingStudy" style=" font-size:20px; display: none;" class="fa fa-spinner fa-spin"></span>
                </h4>
            </div>

            <div class="modal-body">
                <g:render template="/includes/widgets/studyDictionaryTemplate"/>
            </div>

            <div class="modal-footer">
                %{--<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>--}%
                <button type="button" class="btn btn-default clearStudyValues"><g:message
                        code="default.button.clear.label"/></button>
                <button type="button" class="btn btn-primary addStudyValues"><g:message
                        code="default.button.add.label"/></button>
                <button type="button" class="btn btn-default addAllStudies" data-dismiss="modal"><g:message
                        code="default.button.close.label"/></button>
            </div>
        </div>
    </div>
</div>