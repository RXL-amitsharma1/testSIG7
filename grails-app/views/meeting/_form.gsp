<%@ page import="com.rxlogix.user.Group; com.rxlogix.config.ActionType; com.rxlogix.config.ActionConfiguration; com.rxlogix.user.User;" %>
<g:hiddenField name="id" value="${meetingInstance?.id}"/>
<g:hiddenField name="version" value="${meetingInstance?.version}"/>
<g:hiddenField name="alertId" value="${alertId}" class="alertId"/>
<g:hiddenField name="findMeetingTimesUrl" value="${createLink(controller: 'meeting', action: 'findMeetingTimes')}"/>
<style>
.input-group-addon {
    border-radius: 26px;
    border: 1px solid #eeeeee;
}

.selectTimeSlot {
    float: right;
}

.meeting-slot-container {
    color: #8a6d3b;
    background-color: #fcf8e3;
    border-color: #faebcc;
    border-radius: 5px;
}

.modal-footer {
    background: #ffffff;
}
</style>
<script>

    try {
        $('.timepicker1').timepicker({
            showMeridian: false
        });

        $("#due-date-picker-create").datepicker({allowPastDates: true});
        $("#due-date-picker-edit").datepicker({allowPastDates: true});


        $("#due-date-picker-${text}").datepicker('setDate', new Date());

        $("#isRecurringMeeting").on("click", function () {
            var meetingDate = $("#meetingDate").val();
            var meetingTime = $("#meetingTime").val();
            var prefTimezone = $("#timezoneFromServer").val();
            var momentDate;
            if (meetingDate) {
                momentDate = moment(meetingDate + ' ' + meetingTime).format("YYYY-MM-DDTHH:mm:ss.sTZD");
            } else {
                momentDate = (new Date()).toISOString();
            }
            var timeZoneData = null;
            if (typeof prefTimezone != "undefined" && prefTimezone != null) {
                timeZoneData = prefTimezone.split(",");
            }

            if (timeZoneData) {
                var name = timeZoneData[0].split(":")[1].trim();
                var offset = timeZoneData[1].substring(8).trim();
                $('#myScheduler').scheduler('value', {
                    startDateTime: momentDate,
                    timeZone: {
                        name: name,
                        offset: offset
                    }
                })
            }
        });
    } catch (err) {

    }


</script>

<div class="col-lg-12">
    <div class="row form-group">
        <div class="col-lg-4">
            <label>
                <g:message code="app.label.meeting.title" default="Meeting Title"/>
                <span class="required-indicator">*</span>
            </label>
            <input type="text" name="meetingTitle" id="meetingTitle" class="form-control clear-field meetingTitle" maxlength="255">
        </div>

        <div class="col-lg-4">
            <label>
                <g:message code="app.label.meeting.owner" default="Meeting Owner"/>
                <span class="required-indicator">*</span>
            </label>
            <g:select class="form-control clear-field" name="meetingOwner" id="meetingOwner"
                      from="${userList.sort({it?.fullname?.toUpperCase()})}"
                      optionKey="id" optionValue="fullName"
                      value="${fieldValue(bean: meetingInstance, field: 'meetingOwner')}"/>
        </div>

        <div class="col-lg-4">
            <label>
                <g:message code="app.label.meeting.attendees" default="Meeting Attendees"/>
            </label>
            <g:select name="meetingAttendees" id="meetingAttendees-${text}"
                      from="${userList.sort({it?.fullname?.toUpperCase()})}" multiple="true"
                      optionKey="id" optionValue="fullName"
                      value="" class="form-control select2-active clear-field meetingAttendees"/>
        </div>

    </div>

    <div class="row form-group">

        <div class="col-lg-4">
            <label>
                <g:message code="app.label.meeting.date" default="Date"/>
                <span class="required-indicator">*</span>
            </label>

            <div class="fuelux">
                <div class="datepicker form-group" data-initialize="datepicker" id="due-date-picker-${text}">
                    <div class="input-group">
                        <input placeholder="Meeting Date" name="dueDate"
                               class="form-control input-sm meetingDate clear-field" id="meetingDate" type="text"
                               data-date="${meetingInstance?.meetingDate}"
                               value=""/>
                        <g:render template="/includes/widgets/datePickerTemplate"/>
                    </div>
                </div>
            </div>
        </div>

        <div class="col-lg-4">
            <label>
                <g:message code="app.label.meeting.time" default="Start Time"/>
                <span class="required-indicator">*</span>
            </label>

            <div class="input-group bootstrap-timepicker timepicker">
                <input placeholder="MeetingTime" id="meetingTime" readonly
                       class="timepicker1 meetingTime form-control clear-field" type="text">
                <span class="input-group-addon"><i class="glyphicon glyphicon-time"></i></span>

            </div>
        </div>

        <div class="col-lg-4">
            <label>
                <g:message code="app.label.meeting.duration" default="Duration"/>
                <span class="required-indicator">*</span>
            </label>
            <select name="duration" id="duration" class="form-control clear-field">
                <option value="${5}">5 min</option>
                <option value="${10}">10 min</option>
                <option value="${15}">15 min</option>
                <option value="${30}">30 min</option>
                <option value="${45}">45 min</option>
                <option value="${60}">1 hour</option>
                <option value="${90}">1.5 hour</option>
                <option value="${2 * 60}">2 hour</option>
                <option value="${3 * 60}">3 hour</option>
                <option value="${4 * 60}">4 hour</option>
                <option value="${5 * 60}">5 hour</option>
                <option value="${6 * 60}">6 hour</option>
                <option value="${7 * 60}">7 hour</option>
                <option value="${8 * 60}">8 hour</option>
                <option value="${9 * 60}">9 hour</option>
                <option value="${10 * 60}">10 hour</option>
                <option value="${11 * 60}">11 hour</option>
                <option value="${12 * 60}">12 hour</option>
                <option value="${13 * 60}">13 hour</option>
                <option value="${14 * 60}">14 hour</option>
                <option value="${15 * 60}">15 hour</option>
                <option value="${16 * 60}">16 hour</option>
                <option value="${17 * 60}">17 hour</option>
                <option value="${18 * 60}">18 hour</option>
                <option value="${19 * 60}">19 hour</option>
                <option value="${20 * 60}">20 hour</option>
                <option value="${21 * 60}">21 hour</option>
                <option value="${22 * 60}">22 hour</option>
                <option value="${23 * 60}">23 hour</option>
                <option value="${24 * 60}">24 hour</option>
            </select>
        </div>
    </div>

    <div class="row recurrence-checkbox-container">
        <div class="col-md-6">
            <label><input type="checkbox" id="isRecurringMeeting" class="isRecurringMeeting" name="isRecurringMeeting"
                          value="true"/><span> Recurrence</span>
            </label>
        </div>
        <g:if test="${grailsApplication.config.outlook.enabled}">
            <div class="col-md-6">
                <a class="searchMeetingTimes btn btn-primary user-action" style="float:right;">Check Availability</a>
            </div>
        </g:if>
    </div>

    <div class="meetingSuggestionContainer collapse" style="margin: 20px">
        <g:render template="/meeting/findMeetingTimeTemplate" model="[text: text]"/>
    </div>

    <div class="collapse schedular-container schedular-container-${text}">
    </div>

    <div class="row">
        <g:if test="${isMeetingMinutesFlow}">
            <div class="col-lg-12 form-group meetingMinutes">
                <label>
                    <g:message code="meeting.minutes" default="Meeting Minutes"/>
                </label>
                <g:textArea style="height: 150px" class="col-lg-4 form-control meetingMinutes" name="meetingMinutes"
                            id="meetingMinutes"
                            value=""/>
            </div>
        </g:if>
    </div>

    <div class="row">

        <div class="col-lg-12 form-group">
            <label>
                <g:message code="meeting.agenda" default="Agenda"/>
            </label>
            <g:textArea style="height: 150px" class="col-lg-4 form-control clear-field" name="meetingAgenda"
                        id="meetingAgenda"/>
        </div>

        <div class="row">
            <div class="col-lg-12 form-group">
                <label for="attachments">
                    <g:message code="attachments.meeting" default="Add Attachments"/>
                </label>

                <div class="rxmain-container attachment">
                    <div class="rxmain-container-inner">

                        <div class="">
                            <g:render template="/includes/widgets/attachment_panel_meeting"
                                      model="[alertInst: meetingInstance, source: 'detail']"/>
                        </div>
                    </div>
                </div>
            </div>
        </div>

    </div>

</div>
