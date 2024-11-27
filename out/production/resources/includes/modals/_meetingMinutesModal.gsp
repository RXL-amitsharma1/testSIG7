<div class="modal fade" id="meetingMinutesModal" tabindex="-1" role="dialog" aria-labelledby="productDictionaryLabel"
     aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title" id="createModalLabel">Add Minutes</h4>
            </div>

            <div class="modal-body clearfix">
                <g:if test="${flash.message}">
                    <div class="message"><g:message code="${flash.message}" args="${flash.args}"
                                                    default="${flash.defaultMessage}"/></div>
                </g:if>
                <g:hasErrors bean="${meetingInstance}">
                    <div class="errors">
                        <g:renderErrors bean="${meetingInstance}" as="list"/>
                    </div>
                </g:hasErrors>
                <g:form name="meetingModalForm" controller="meeting" action="save" method="post">
                    <g:hiddenField name="create-url" value="${createLink(controller: 'meeting', action: 'save')}"/>
                    <g:hiddenField name="appType" id="appType" value="${appType}"/>
                    <g:hiddenField name="alertId" id="alertId" value="${alertId}"/>
                    <g:render template="/meeting/form"
                              model="[meetingInstance: meetingInstance, alertId: alertId, text: 'meeting-minutes', isMeetingMinutesFlow: true, userList: userList]"/>
                </g:form>
            </div>

            <div class="modal-footer">
                <div class="buttons ">
                    <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
                    <button type="button" class="btn btn-primary id-element update-meeting ${buttonClass}" data-id="">Update</button>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
    $('#meetingMinutesModal').on('show.bs.modal', function () {
        ($("#meetingTime").css("background-color","#eee"));
    })
    $('#meetingMinutesModal').on('hide.bs.modal', function () {
        ($("#meetingTime").css("background-color","white"));
    })

    $("#due-date-picker-meeting-minutes").datepicker({allowPastDates: true});
</script>
