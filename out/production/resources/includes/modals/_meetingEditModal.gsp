<div class="modal fade" id="editMeetingModal" tabindex="-1" role="dialog" aria-labelledby="productDictionaryLabel"
     aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title" id="editModalLabel"><span id="edit-meeting-title-container">Edit Meeting</span></h4>
            </div>
            <div class="modal-body clearfix">
                <g:if test="${flash.message}">
                    <div class="message"><g:message code="${flash.message}" args="${flash.args}" default="${flash.defaultMessage}" /></div>
                </g:if>
                <g:hasErrors bean="${meetingInstance}">
                    <div class="errors">
                        <g:renderErrors bean="${meetingInstance}" as="list" />
                    </div>
                </g:hasErrors>
                <g:form name="tempForm" controller="meeting" action="save" method="post" enctype="multipart/form-data">
                    <g:hiddenField name="create-url" value="${createLink(controller: 'meeting', action: 'update')}"/>
                    <g:hiddenField name="appType" id="appType" value="${appType}" />
                    <g:render template="/meeting/form" model="[meetingInstance: meetingInstance, alertId: alertId, edit:false,text:'edit']" />
                </g:form>
            </div>

            <div class="modal-footer">
                <div class="buttons ">
                        <button type="button" class="btn btn-primary id-element update-meeting ${buttonClass}" data-ics-file="false" data-id="">Update</button>
                    <button type="button" class="btn btn-default cancelMeeting meeting-id-element id-element ${buttonClass}" data-id="">Cancel Meeting</button>
                        <g:link controller="meeting" action="cancelMeetingSeries" elementId="cancelMeetingSeriesLink"
                                class="btn btn-default meeting-id-element cancelMeetingLink">Cancel Meeting Series</g:link>
                    <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
                </div>
            </div>
        </div>
    </div>
</div>