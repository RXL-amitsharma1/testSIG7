//= require app/pvs/common/rx_handlebar_ext.js
//= require app/pvs/actions/actionList.js
$(function () {

    var calendarId = '#calendar';

    //Initiate the calendar.
    init_calendar(calendarId);
    focusFirst();


    $('#actionItemModal').find('.datepicker').datepicker({
        allowPastDates: true,
        momentConfig: {
            format: DEFAULT_DATE_DISPLAY_FORMAT
        }
    });




    $('#action-edit-modal').on('hidden.bs.modal', function (e) {
        clear_edit_errors();
        $(this)
            .find("input[type=text],textarea,select")
            .val('')
            .end()
    });


});

var getActionDetails = function(modalId) {
    return {
        "config": modalId.find("#config").val(),
        "type": modalId.find("#type").val(),
        "assignedToValue": modalId.find("#assignedTo").val(),
        "dueDate": modalId.find("#dueDate").val(),
        "completedDate": modalId.find("#completedDate").val(),
        "details": modalId.find("#details").val(),
        "comments": modalId.find("#comments").val(),
        "alertId": modalId.find("#alertId").val(),
        "appType": modalId.find("#appType").val(),
        "exeConfigId": modalId.find("#exeConfigId").val(),
        "meetingId": modalId.find("#meetingElement").val(),
        "actionStatus": modalId.find("#actionStatus").val(),
        "actionId": modalId.find("#actionId").val()
    }
};


var editAction = function (evt) {
    var url = "/signal/action/getById?id=" + evt.id;
    $.ajax({
        url: url,
        async: false,
        cache: false
    }).done(function (data) {
        data.action_types = action_select_values.types;
        data.action_configs = action_select_values.configs;
        data.all_status = action_select_values.allStatus;
        data.alertId = undefined;
        data.actionStatus = {name: data.actionStatus, value: "New"};

        var html_content = signal.utils.render('action_editor_6.1_6_v1', data);
        $('#action-editor-container').html(html_content);

        $('#action-editor-container #due-date-picker').datepicker({
            date: $("#due-date-picker").val() ? new Date($("#due-date-picker").val()) : null,
            allowPastDates: true,
            momentConfig: {
                culture: userLocale,
                format: DEFAULT_DATE_DISPLAY_FORMAT
            }
        });

        var dueDate = moment(data.dueDate, "DD-MMM-YYYY").format(DEFAULT_DATE_FORMAT);
        $('#action-editor-container').find('#due-date-picker').find('#dueDate').val(dueDate);

        var completedDate = data.completedDate?moment(data.completedDate, "DD-MMM-YYYY").format(DEFAULT_DATE_FORMAT):'';

        $('#action-editor-container #completion-date-picker').datepicker({
            date: data.completedDate ? new Date($("#completion-date-picker").val()) : '',
            allowPastDates: true,
            restricted: [{from: TOMORROW , to: Infinity}],
            momentConfig: {
                culture: userLocale,
                format: DEFAULT_DATE_DISPLAY_FORMAT
            }
        }).on('inputParsingFailed.fu.datepicker',function (e) {
            $('#completedDate').val('');
        });

        $('#action-editor-container').find('#completion-date-picker').find('#completedDate').val(completedDate);

        $('#action-editor-container').find("#actionStatus").on('change', function () {
            if ($(this).val() === 'Closed') {
                var completedDate = moment.utc(new Date()).tz(serverTimeZone).format("DD-MMM-YYYY");
                $('#action-editor-container').find('#completion-date-picker').find('#completedDate').val(completedDate);
            }
        });

        $('#action-editor-container').find('#completion-date-picker').find('#completedDate').on('focusout', function(){
            $(this).val(newSetDefaultDisplayDateFormat( $(this).val()));
            if($(this).val()=='Invalid date'){
                $(this).val('')
            }
        });

        $('#action-editor-container').find('#due-date-picker').find('#dueDate').on('focusout', function(){
            $(this).val(newSetDefaultDisplayDateFormat( $(this).val()));
            if($(this).val()=='Invalid date'){
                $(this).val('')
            }
        });

        var editActionModal = $('#action-edit-modal');

        if (typeof alertId != "undefined") {
            editActionModal.find('.id-element').attr('data-id', evt.id);
        }
        editActionModal.find('#appType').val(data.alertType);
        editActionModal.modal({});
        $('#action-edit-modal #cancel-bt').off().on('click', handle_action_editor_cancel);
        $('#action-edit-modal #update-bt').off().on('click', handle_action_editor_update);
    })
};

var handle_action_editor_cancel = function (evt) {
    evt.preventDefault();
    $('#action-edit-modal').modal('hide')
};

var handle_action_editor_update = function (evt) {
    evt.preventDefault()
    if (!evt.handled) {
        evt.handled = true

        var json = $('#action-editor-container form#action-editor-form').serialize()  + "&appType=" + $("#appType").val()
        $.ajax({
            url: '/signal/action/updateAction',
            method: 'POST',
            data: json,
            async: false,
            cache: false
        }).done(function (response) {
                if (response.status == false) {
                    clear_edit_errors();
                    var errorMsg = '<div class="alert alert-danger alert-dismissible" role="alert"> ' +
                        '<button type="button" class="close" data-dismiss="alert"> ' +
                        '<span aria-hidden="true">&times;</span> ' +
                        '<span class="sr-only"><g:message code="default.button.close.label" /></span> ' +
                        '</button> ' ;
                    for (var index = 0; index < response.data.length; index++) {
                        var errorMessageObj = response.data[index];
                        errorMsg += errorMessageObj;
                        errorMsg += '</br>';
                    }
                    errorMsg += "</div>"
                    $('#action-edit-modal .modal-body').prepend(errorMsg)
                } else {
                    var action_list_table = $('#action-table').DataTable()
                    action_list_table.ajax.reload()
                    checkedRowList = []
                    checkedActionIdList = []
                    $('#select-all').prop('checked', false)
                    $(".btn-buld-update").prop('disabled', true);

                    var containerId = "#" + signal.utils.localStorageUtil.getJSON("dashboardWidgetsConfig")['pvWidgetChart-6'].content.id;
                    $(containerId).fullCalendar('destroy');
                    init_calendar(containerId);

                    $('#action-edit-modal').modal('hide')
                }
        })
    }
}

var editMeeting = function (evt) {
    var mmModal = $('#meetingMinutesModal');
    var url = "/signal/meeting/getById?meetingId=" + evt.id;
    var meetingId = evt.id;
    var $form = $('#meetingModalForm');
    $.ajax({
        url: url,
        async: false,
        cache: false,
        dataType: 'json'
    }).done(function (data) {
        var $meetingMinutes = $('.meetingMinutes');
        fillModalDataWithText(data,mmModal,false,'meeting-minutes');
        fillModalData(data, mmModal, false);
        disableFormFields($form);
        $("#attachments").attr("disabled",false);
        $meetingMinutes.removeAttr('disabled');
        $meetingMinutes.val(data.meetingMinutes);
        mmModal.modal({});
    });
    $('.update-meeting').off().on('click', function (evt) {
        var appType = $('#meetingModalForm #appType').val();
        var alertId = $('#meetingModalForm #alertId').val();
        var updateUrl = "/signal/meeting/saveMeetingMinutes?meetingId=" + meetingId;
        $.ajax({
            url: updateUrl,
            type: "POST",
            data: getMeetingDetails(mmModal, 'meetingMinutes'),
            async: false,
            processData: false,
            contentType: false,
            dataType: 'json',
            cache: false
        }).done(function (data) {
            mmModal.find('.form-control').val("");
            mmModal.find("#attachments").val("");
            mmModal.modal("hide");
            signal.activities_utils.reload_activity_table();
            signal.actions_utils.reload_action_table();
            table.ajax.reload();
        })
    })
};

var getMeetingDetails = function (modalId, text) {
    var meetingMinutes = "";
    var data = new FormData();
    if (text==='meetingMinutes') {
        meetingMinutes = modalId.find("#meetingMinutes").val();
        data.append('meetingMinutes', meetingMinutes);
        data.append('meetingOwner', modalId.find("#meetingOwner").val());
        var attendeesArray = modalId.find("#meetingAttendees-meeting-minutes").val();
        if (attendeesArray) {
            data.append('meetingAttendees', attendeesArray.toString());
        }
    }
    if(text !== 'meetingMinutes'){
        var attendeesArray = modalId.find("#meetingAttendees-" + text).val();
        if (attendeesArray) {
            data.append('meetingAttendees', attendeesArray.toString());
        }
        data.append('scheduleDateJSON', JSON.stringify($('#myScheduler').scheduler('value')));
        data.append('isRecurringMeeting', modalId.find("#isRecurringMeeting").is(":checked"));
        data.append('duration', modalId.find("#duration").val());
        data.append('meetingAgenda', modalId.find("#meetingAgenda").val());
        data.append('meetingOwner', modalId.find("#meetingOwner").val());
        var dateTime = modalId.find("#meetingDate").val() + " " + modalId.find("#meetingTime").val();
        data.append('meetingDate', dateTime);
        data.append('meetingTitle', modalId.find("#meetingTitle").val());
    }
    data.append('type', modalId.find("#type").val());
    data.append('appType', modalId.find("#appType").val());
    data.append('alertId', modalId.find("#alertId").val());
    data.append('attachmentSize', modalId.find("#attachments").prop('files').length);
    for (var i = 0; i < modalId.find("#attachments").prop('files').length; i++) {
        data.append('attachments' + i, modalId.find("#attachments").prop('files')[i]);
    }
    return data
}

var fillModalDataWithText =  function(data,modal,isEditSeries,text){
    var meetingAttendees = data.attendees;
    var attendees = "";
    clearModal(modal);
    var attendeesList = [];
    for (var i = 0; i < meetingAttendees.length; i++) {
        if (i == 0) {
            attendees = meetingAttendees[i].id
            attendeesList.push(meetingAttendees[i].id);
        } else {
            attendees = attendees + "," + meetingAttendees[i].id;
            attendeesList.push(meetingAttendees[i].id);
        }
    }
    var span = ""
    for (var i = 0; i < data.meetingAttachments.length; i++) {

        var link = "/signal/meeting/viewAttachments?attachmentId=" + data.meetingAttachments[i].id + "&meetingId=" + data.id
        span = span + "<div class='attachment-name'><a target='_blank' href='" + link + "' download='" + data.meetingAttachments[i].name +
            "' <i class='fa fa-file' aria-hidden='true' style='font-size: medium'> </i>" + data.meetingAttachments[i].name + "</a></div>"
    }
    modal.find(".attachments .attachment-body").html(span)
    var alertId = $('#meetingModalForm #alertId').val();
    modal.find('.id-element').attr('data-id', alertId);
    modal.find('.meeting-id-element').attr('data-is-edit-series', isEditSeries);
    if (isEditSeries) {
        modal.find('.meeting-id-element').attr('data-master-id', data.masterId);
        $('#cancelMeetingLink').hide();
        $('#cancelMeetingSeriesLink').show();
        modal.find('.recurrence-checkbox-container').show();
        $('#edit-meeting-title-container').text('Edit Series');
        $('.isRecurringMeeting').attr("checked","checked");
        $(".meetingDate").attr("disabled", "disabled");
        $(".meetingTime").attr("disabled", "disabled");
        $('.schedular-container').collapse('show');
    } else {
        modal.find('.meeting-id-element').attr('data-meeting-id', data.id);
        $('#cancelMeetingLink').show();
        $('#cancelMeetingSeriesLink').hide();
        modal.find('.recurrence-checkbox-container').hide();
        $('#edit-meeting-title-container').text('Edit Meeting');
        $('.isRecurringMeeting').prop("checked", false);
        $(".meetingDate").removeAttr("disabled");
        $(".meetingTime").removeAttr("disabled");
        $('.schedular-container').collapse('hide');
    }
    modal.find("#meetingTitle").val(data.meetingTitle);
    modal.find("#meetingOwner").val(data.ownerId);
    modal.find("#meetingDate").val(moment(data.meetingDate).format('MM/DD/YYYY'));
    modal.find("#meetingTime").val(moment(data.meetingDate).format('HH:mm'));
    modal.find('#due-date-picker').datepicker();
    modal.find("#meetingAgenda").val(data.meetingAgenda);
    if (data.guestAttendees) {
        $.each(data.guestAttendees, function (index, value) {
            modal.find("#meetingAttendees-"+text).append("<option value='" + value + "'>" + value + "</option>");
            attendeesList.push(value);
        });
    }
    modal.find("#meetingAttendees-"+text).val("");
    modal.find('#meetingAttendees-'+text).find('option.guestAttendee').remove();
    modal.find("#meetingAttendees-"+text).val(attendeesList);
    showTagWidget("#meetingAttendees-"+text);
    modal.find('#duration').val(data.duration);
};

var fillModalData = function (data, modal, isEditSeries) {
    fillModalDataWithText(data,modal,isEditSeries,'edit')
};

function disableFormFields($form) {
    $form.find('input').attr('disabled', true);
    $form.find('select').attr('disabled', true);
    $form.find('textarea').attr('disabled', true);
    $form.find('.searchMeetingTimes').addClass('disabled');
    $(".meetingDate").attr("disabled", "disabled");
    $(".meetingTime").attr("disabled", "disabled");
};

function clearModal($modal) {
    $modal.find("input[type=text]").val('');
    $modal.find("textarea").val('');
};

function showTagWidget(id) {
    var $tag = $(id);
    $tag.select2({

        placeholder: "Select Attendees",
        allowClear: true,
        width: "100%",
        createTag: function (params) {
            var term = $.trim(params.term);
            if (term != "") {
                return {
                    id: term,
                    text: term
                }
            }
            return null
        }
    });
};
var init_calendar = function (calendarId) {

    $(calendarId).fullCalendar({
        header: {
            left: 'prev,next today',
            center: 'title',
            right: 'month,agendaWeek,agendaDay'
        },
        firstDay: 1,
        eventLimit: true,   // allow "more" link when there are too many events

        dayClick: function (date, jsEvent, view) {
            date = moment(date).format(DEFAULT_DATE_DISPLAY_FORMAT);
            $("#dueDateHidden").val(date);
        },

        eventClick: function (calEvent, jsEvent, view) {
            switch (calEvent.eventType){
                case "AGGREGATE_CASE_ALERT":
                    window.open(
                        '/signal/aggregateCaseAlert/view?id='+calEvent.id,
                        '_blank'
                    );
                    break;
                case "SINGLE_CASE_ALERT":
                    window.open(
                        '/signal/singleCaseAlert/view?id='+calEvent.id,
                        '_blank'
                    );
                    break;
                case "ACTION_ITEM":
                    editAction(calEvent);
                    break;
                case "MEETING":
                    editMeeting(calEvent);
                    break;
            }
        },

        eventMouseover: function (calEvent, jsEvent, view) {

            // change the border style
            $(this).css({
                "border-color": "#ba3939",
                "border-width": "1px",
                "border-style": "dashed"
            });

        },

        eventMouseout: function (calEvent, jsEvent, view) {

            // change the border style
            $(this).css({
                "border-color": "#cccccc",
                "border-width": "1px",
                "border-style": "solid"
            });

        },

        //Calendar api automatically appends the start and end date of the rendered event.
        //When user clicks on the next or previous or today then these events are re-fetched.
        events: function(start, end, timezone, callback) {
            jQuery.ajax({
                url: eventsUrl,
                type: 'POST',
                dataType: 'json',
                data: {
                    start: start.format(DEFAULT_DATE_DISPLAY_FORMAT),
                    end: end.format(DEFAULT_DATE_DISPLAY_FORMAT)
                }
            })
                .done(function(result) {
                    var events = [];
                    if(!!result){
                        $.map( result, function( r ) {
                            events.push({
                                id: r.id,
                                title: r.title,
                                start: r.eventType === "MEETING" ? moment.utc(r.startDate).tz(calenderUserTimeZone) : moment.utc(r.startDate).tz(userDefaultTimeZone),
                                end: r.endDate ? r.eventType === "MEETING" ? moment.utc(r.endDate).tz(calenderUserTimeZone): moment.utc(r.endDate).tz(userDefaultTimeZone)  : undefined,
                                url: getEventUrl(r.eventType, r.id),
                                allDay: r.eventType === "MEETING" ? r.allDay : true,
                                color: r.color,
                                textColor: r.textColor,
                                eventType: r.eventType
                            });
                        });
                    }
                    callback(events);
            });
        },

        loading: function (isLoading, view) {
            if (isLoading) {
                $('.alert-info').removeClass('hide');
            } else {
                $('.alert-info').addClass('hide');
            }
        }
    });

//Add button to reload the calender
    //reloadCalendar($('.fc-month-button').parent(), calendarId);
};

function bindActionItemCRUD(actionItemId) {
    $('#actionItemModal').find('#deleteActionItem').removeClass('hide');
    actionItem.actionItemModal.view_action_item(actionItemId);

    //Click event bind to the delete button.
    $('.action-item-delete').on('click', function () {
        actionItem.actionItemModal.delete_action_item(actionItemId, true,PR_Calendar);
    });

    //Click event bind to the edit button.
    $('.edit-action-item').on('click', function () {
        actionItem.actionItemModal.edit_action_item(actionItemId, true, null, null);
    });

    //Click event bind to the update button.
    $('.update-action-item').on('click', function () {
        actionItem.actionItemModal.update_action_item(true, $('#actionItemModal'), PR_Calendar);
    });

}

function reloadCalendar(toolbar, calendarName) {
    var reloader = '<span title="Refresh Calender" class="glyphicon calendar-reloader-btn glyphicon-refresh"></span>';
    reloader = $(reloader);
    toolbar.prepend(reloader);
    if (calendarName != undefined) {
        $('.calendar-reloader-btn').on('click', function () {
            $('.calendar-reloader-btn').addClass('glyphicon-refresh-animate');
            $(calendarName).fullCalendar('refetchEvents');
        });
    }
}


function getEventUrl(eventType, id) {
    if (eventType == EVENTS_TYPE_CONSTANTS.REPORT_REQUEST) {
        return reportRequestShowURL + "/" + id;
    }
    if (eventType == EVENTS_TYPE_CONSTANTS.EXECUTED_PERIODIC_REPORT) {
        return executedPeriodicReportShowURL + "/" + id;
    }
    if (eventType == EVENTS_TYPE_CONSTANTS.EXECUTED_ADHOC_REPORT) {
        return executedAdhocReportShowURL + "/" + id;
    }
    if (eventType == EVENTS_TYPE_CONSTANTS.SCHEDULED_ADHOC_REPORT) {
        return adhocReportShowURL + "/" + id;
    }
    if (eventType == EVENTS_TYPE_CONSTANTS.SCHEDULED_PERIODIC_REPORT) {
        return periodicReportShowURL + "/" + id;
    }
    return undefined;
}

var clear_errors = function() {
    $('#createActionModal .modal-body .alert').remove()
};

var clear_edit_errors = function () {
    $('#action-edit-modal .modal-body .alert').remove()
};
var clear_edit_signal = function () {
    $('#editActionModal .modal-body .alert').remove()
};

var EVENTS_TYPE_CONSTANTS = {
    EXECUTED_ADHOC_REPORT: "EXECUTED_ADHOC_REPORT",
    SCHEDULED_ADHOC_REPORT: "SCHEDULED_ADHOC_REPORT",
    EXECUTED_PERIODIC_REPORT: "EXECUTED_PERIODIC_REPORT",
    SCHEDULED_PERIODIC_REPORT: "SCHEDULED_PERIODIC_REPORT",
    REPORT_REQUEST: "REPORT_REQUEST",
    ACTION_ITEM: "ACTION_ITEM"
};
