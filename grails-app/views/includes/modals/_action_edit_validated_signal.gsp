<div class="modal fade" id="editActionModal" role="dialog" aria-labelledby="productDictionaryLabel"
     aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title" id="editActionLabel"><g:message code="app.action.edit.label"/></h4>
            </div>

            <div class="modal-body clearfix">
                <g:if test="${flash.message}">
                    <div class="message"><g:message code="${flash.message}" args="${flash.args}" default="${flash.defaultMessage}" /></div>
                </g:if>
                <g:hasErrors bean="${actionInstance}">
                    <div class="errors">
                        <g:renderErrors bean="${actionInstance}" as="list" />
                    </div>
                </g:hasErrors>
                <g:form name="tempForm" controller="action" action="save" method="post" >
                    <g:hiddenField name="create-url" value="${createLink(controller: 'action', action: 'update')}"/>
                    <g:hiddenField name="appType" id="appType" value="${appType}" />
                    <g:hiddenField name="exeConfigId" id="exeConfigId" value="${id}" />
                    <g:render template="/action/form" model="[actionInstance: actionInstance, alertId: alertId,text: 'edit']" />
                </g:form>
            </div>

            <div class="modal-footer">
                <div class="buttons ">
                    <button type="button" class="btn btn-primary id-element update-action ${buttonClass}" data-id="">Update</button>
                    <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
                </div>
            </div>
        </div>
    </div>
</div>