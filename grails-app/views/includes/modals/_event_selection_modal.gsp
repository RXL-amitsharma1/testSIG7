<div class="modal fade" id="eventModal" tabindex="-1" role="dialog" aria-labelledby="eventDictionaryLabel"
     aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="eventDictionaryLabel"><g:message code="app.reportField.eventDictId"/>
                    <span id="fetchingEvents" style=" font-size:20px; display: none;" class="fa fa-spinner fa-spin"></span>
                </h4>
            </div>

            <div class="modal-body">
                <g:render template="/includes/widgets/eventDictionaryTemplate" model="[sMQList: sMQList]"/>
            </div>

            <div class="modal-footer">
                %{--<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>--}%
                <button type="button" class="btn btn-default clearEventValues" accesskey="l"><g:message
                        code="default.button.clear.label"/></button>
                <button type="button" class="btn btn-primary addEventValues" accesskey="a"><g:message
                        code="default.button.add.label"/></button>
                <button type="button" class="btn btn-primary addAllEventValues" accesskey="^"><g:message
                        code="app.label.add.all"/></button>
                <button type="button" class="btn btn-default addAllEvents" data-dismiss="modal" accesskey="q"><g:message
                        code="default.button.close.label"/></button>
            </div>
        </div>
    </div>
</div>
