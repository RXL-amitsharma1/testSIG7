<div class="modal fade" id="createMeetingModal"data-backdrop="static" tabindex="-1" role="dialog" aria-labelledby="productDictionaryLabel"
     aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title" id="createModalLabel"><g:message code="app.label.create.meeting"/></h4>
            </div>

            <div class="modal-body clearfix">
                <div id="msg-container" style="display:none;"></div>
                <g:if test="${flash.message}">
                    <div class="message"><g:message code="${flash.message}" args="${flash.args}" default="${flash.defaultMessage}" /></div>
                </g:if>
                <g:hasErrors bean="${meetingInstance}">
                    <div class="errors">
                        <g:renderErrors bean="${meetingInstance}" as="list" />
                    </div>
                </g:hasErrors>
                <g:form name="tempForm" controller="meeting" action="save" method="post" >
                    <g:hiddenField name="create-url" value="${createLink(controller: 'meeting', action: 'save')}"/>
                    <g:hiddenField name="appType" id="appType" value="${appType}" />
                    <g:render template="/meeting/form" model="[meetingInstance: meetingInstance, alertId: alertId,text:'create', userList: userList]" />
                </g:form>

                </div>

            <div class="modal-footer">
                <div class="buttons ">
                    <g:if test="${edit}">
                        <button type="button" class="btn btn-primary id-element" data-id=""><g:message code="default.button.update.label"/></button>
                        <button type="button" class="btn btn-default" data-dismiss="modal"><g:message code="default.button.close.label"/></button>
                    </g:if>
                    <g:else>
                        <button type="button" id="create-meeting-btn" data-ics-file="false" class="btn btn-primary id-element"><g:message code="default.button.create.label"/></button>
                        <button type="button" class="btn btn-default closeMeetingModal" data-dissmiss="modal"><g:message code="default.button.close.label"/></button>
                    </g:else>
                </div>
            </div>
        </div>
    </div>
</div>