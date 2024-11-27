<div class="modal fade" id="percentModal" tabindex="-1" role="dialog" aria-labelledby="percentValueLabel"
     aria-hidden="true">
    <div class="modal-dialog modal-sm">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title" id="percentValueLabel">
                    <g:message code="app.label.business.configuration.percentValue.Label"/>
                </h4>

            </div>

            <div class="modal-body">
                <div class="messageContainerForDialog"></div>
                <div>
                    <span style="float:left; font-weight: bold"><g:message code="app.label.business.configuration.enterValue.Label"/></span>
                    <g:field name="percentValue" class="form-control" value="" placeholder="Value"
                                 maxlength="3" style="width:40%; position: relative; float:left; margin-left:12px;margin-top:-5px" type="number" onkeyup="numberValidate()" />
                    <span style="float:right; position: relative; left: -25px">%</span>
                </div>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-default savePercentValue" accesskey="o"><g:message
                        code="default.button.ok.label"/></button>
                <button type="button" class="btn btn-default" accesskey="c" data-dismiss="modal"><g:message
                        code="default.button.cancel.label"/></button>
            </div>
        </div>
    </div>
</div>