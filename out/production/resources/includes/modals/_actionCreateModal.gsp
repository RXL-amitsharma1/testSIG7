<div class="modal fade" data-backdrop="static" id="createActionModal" role="dialog" aria-labelledby="productDictionaryLabel"
     aria-hidden="true">
    <div class="modal-dialog modal-lg actionClass">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
                <label class="modal-title" id="createActionLabel"><g:message code="app.action.create.label"/></label>
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
                    <g:hiddenField name="create-url" value="${createLink(controller: 'action', action: 'save')}"/>
                    <g:hiddenField name="appType" id="appType" value="${appType}" />
                    <g:hiddenField name="exeConfigId" id="exeConfigId" value="${id}" />
                    <g:hiddenField name="isArchived" id="isArchived" value="${isArchived}" />
                    <g:render template="/action/form" model="[actionInstance: actionInstance, alertId: alertId,isTopicFlow:isTopicFlow,text:'create',
                                                              actionConfigList: actionConfigList,actionTypeList:actionTypeList]" />
                </g:form>
            </div>

            <div class="modal-footer">
                <div class="buttons ">
                    <g:if test="${edit}">
                        <button type="button" class="btn btn-primary id-element" data-id="">Update</button>
                        <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
                    </g:if>
                    <g:else>
                        <button type="button" id="create-action-btn" class="btn btn-primary id-element">Create</button>
                        <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
                    </g:else>
                </div>
            </div>
        </div>
    </div>
</div>