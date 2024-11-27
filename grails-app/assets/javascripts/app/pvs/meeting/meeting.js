var signal = signal || {};

var meetingDataTable;
var schedulerDataJson;

signal.meeting_utils = (function () {


    var current_create_bt
    var table
    var set_meeting_create_modal = function () {
        $('#createMeetingModal').on('shown.bs.modal', function (e) {
            $(this).find('.clear-field').val('');
            $('#meetingAttendees-create').val(" ");
            $('#createMeetingModal').find("#attachments").val("");
            clear_errors();
            processScheduler('create');
        });
        $('#createMeetingModal').on('hidden.bs.modal', function (e) {
            unProcessScheduler('create');
            $('.meetingSuggestionContainer').collapse('hide');
        });

        $('#editMeetingModal').on('shown.bs.modal', function (e) {
            clear_errors();
            processScheduler('edit', schedulerDataJson);
        });
        $('#editMeetingModal').on('hidden.bs.modal', function (e) {
            unProcessScheduler('edit');
            $('.meetingSuggestionContainer').collapse('hide');
        });

        $('.meeting-create').on('click', function (evt) {
            $(".meetingTitle").attr("disabled", false);
            var srcEle = $(evt.target)
            current_create_bt = srcEle
            var alertId = alertId
            if (typeof (srcEle.attr('alert-id')) !== 'undefined') {
                alertId = srcEle.attr('alert-id')
            }

            var createMeetingModal = $('#createMeetingModal')
            createMeetingModal.find('.id-element').attr('data-id', alertId);

            //Set the alertId in the hidden field of the modal.
            if (typeof alertId != "undefined" && alertId != '') {
                createMeetingModal.find(".alertId").val(alertId);
            }

            //createMeetingModal.find('#due-date-picker').datepicker()

            clear_errors();
            $('.isRecurringMeeting').prop("checked", false);
            $('.isRecurringMeeting').trigger("change");
            createMeetingModal.modal({});

            showTagWidget("#meetingAttendees-create");

            //Event bind when the save/create button is clicked in the action event modal.
            $("#create-meeting-btn").on('click', function (event) {
                if (!event.handled) {
                    $("#create-meeting-btn").prop('disabled', true);
                    var icsFile = $(this).data('icsFile');
                    var alertId = $(event.target).attr('data-id');
                    event.handled = true;
                    var appType = $("#appType").val();
                    var meetingModal = $("#createMeetingModal #tempForm");
                    $.ajax({
                        type: "POST",
                        url: '/signal/meeting/save?downloadICSFile=' + icsFile,
                        data: getMeetingDetails(meetingModal, 'create'),
                        async: true,
                        processData: false,
                        contentType: false,
                        dataType: 'json',
                        cache: false,
                        beforeSend: function () {
                            document.body.style.cursor = 'wait';
                            $("#createMeetingModal").css({
                                "pointer-events": "none",
                                "cursor": "not-allowed"
                            });
                        },
                    })
                        .always(function () {
                            document.body.style.cursor = 'default';
                            $("#createMeetingModal").css("pointer-events", "");
                            $("#createMeetingModal").css("cursor", "");
                        })
                        .done(function (payload) {
                            $("#create-meeting-btn").prop('disabled', false);
                            if (payload.status) {
                                $.Notification.notify('success', 'top right', "Success", "Meeting created.", {autoHideDelay: 5000});
                                downloadICSFile(icsFile, payload.data);
                                createMeetingModal.find('.form-control').val("");
                                $('#meeting-table').DataTable().ajax.reload();
                                createMeetingModal.modal('hide');
                                //Refresh the activity table
                                signal.activities_utils.reload_activity_table();
                                signal.actions_utils.reload_action_table();
                                signal.meeting_utils.reload_meeting_table();

                            } else {
                                clear_errors();
                                var errorMsg;
                                if (payload.code == 400)
                                    errorMsg = showErrorMessageInMeetingModal("Entered Email address is not valid");
                               else
                                    errorMsg = showErrorMessageInMeetingModal("Please provide information for all the mandatory fields which are marked with an asterisk (*)");
                                $('#createMeetingModal .modal-body').prepend(errorMsg)
                            }
                        })
                        .fail(function (data) {
                            clear_errors()
                            var errorMsg = showErrorMessageInMeetingModal(data.responseText);
                            $('#createMeetingModal .modal-body').prepend(errorMsg)
                    })
                }
            })

        })
    }

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

    var clear_errors = function () {
        $('.modal .modal-body .alert').remove();
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
            span = span + "<a class='attachment-name m-t-15' target='_blank' href='" + link + "' download='" + data.meetingAttachments[i].name +
                "'> <i class='fa fa-file m-r-10' aria-hidden='true'> </i>" + data.meetingAttachments[i].name + "</a></div>"
        }
        modal.find(".attachments .attachment-body").html(span)

        modal.find('.id-element').attr('data-id', alertId);
        modal.find('.meeting-id-element').attr('data-is-edit-series', isEditSeries);
        if (isEditSeries) {
            modal.find('.meeting-id-element').attr('data-master-id', data.masterId);
            $('#cancelMeetingLink').hide();
            $('#cancelMeetingSeriesLink').show();
            modal.find('.recurrence-checkbox-container').show();
            $('#edit-meeting-title-container').text('Edit Series');
            $('.isRecurringMeeting').prop("checked",true);
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
        modal.find("#meetingDate").val(moment(data.meetingDate,"DD-MMM-YYYY h:mm A").format('MM/DD/YYYY'));
        modal.find("#meetingTime").val(moment(data.meetingDate,"DD-MMM-YYYY h:mm A").format('HH:mm'));
        modal.find('#due-date-picker').datepicker({allowPastDates: true});
        $("#meetingAttendees-" + text).find("option[isGuest='true']").remove();
        modal.find("#meetingAgenda").val(data.meetingAgenda);
        if (data.guestAttendees) {
            $.each(data.guestAttendees, function (index, value) {
                modal.find("#meetingAttendees-"+text).append("<option value='" + value + "' isGuest='true'>" + value + "</option>");
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

    var init_meeting_table = function (table_id) {
        var dataUrl = "/signal/meeting/list?alertId=" + alertId + "&appType=" + appType;
        table = $(table_id).DataTable({
            "language": {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json",
                search: 'Search'
            },
            search: {
                smart: false
            },
            "oLanguage": {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json",
                "oPaginate": {
                    "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                    "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                    "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                    "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
                },
                "sLengthMenu": "Show _MENU_"
            },
            drawCallback: function () {
                var appType = $("#appType").val();
                colEllipsis();
                webUiPopInitMeeting();
                var filteredCount = $('#meeting-table').DataTable().rows({search:'applied'}).count();
                pageDictionary($('#meeting-table_wrapper'), filteredCount);
                showTotalPage($('#meeting-table_wrapper'), filteredCount);

                $('.edit-meeting-series-link').on('click', function (evt) {
                    var editMeetingSeriesModal;
                    var masterMeetingId = $(this).attr('data-master-id');
                    var url = "/signal/meeting/getByMasterId?masterId=" + masterMeetingId;
                    $.ajax({
                        url: url,
                        async: false,
                        cache: false
                    }).done(function (data) {
                        schedulerDataJson = JSON.parse(data.schedularJson);
                        editMeetingSeriesModal = $('#editMeetingModal');
                        fillModalData(data, editMeetingSeriesModal, true);
                        editMeetingSeriesModal.modal({});
                    });
                    $('.update-meeting,#edit-iCalander').off().on('click', function (evt) {
                        $('.update-meeting,#edit-iCalander').prop('disabled', true);
                        var icsFile = $(this).data('icsFile');
                        var updateUrl = "/signal/meeting/updateMeetingSeries?masterId=" + masterMeetingId + "&appType=" + appType + "&downloadICSFile=" + icsFile;
                        var editMeetingModalForm = $("#editMeetingModal #tempForm");
                        $.ajax({
                            url: updateUrl,
                            type: "POST",
                            data: getMeetingDetails(editMeetingModalForm, 'edit'),
                            async: false,
                            processData: false,
                            contentType: false,
                            dataType: 'json',
                            cache: false
                        }).done(function (payload) {
                            $('.update-meeting,#edit-iCalander').prop('disabled', false);
                            if (payload.status) {
                                downloadICSFile(icsFile, payload.data);
                                editMeetingModalForm.find('.form-control').val("");
                                editMeetingModalForm.find("#attachments").val("");
                                editMeetingSeriesModal.modal("hide");
                                signal.activities_utils.reload_activity_table();
                                signal.actions_utils.reload_action_table();
                                signal.meeting_utils.reload_meeting_table();
                            } else {
                                clear_errors();
                                var errorMsg;
                                if (payload.code == 400)
                                    errorMsg = showErrorMessageInMeetingModal("Entered Email address is not valid");
                                else
                                    errorMsg = showErrorMessageInMeetingModal("Please provide information for all the mandatory fields which are marked with an asterisk (*)");
                                editMeetingSeriesModal.find('.modal-body').prepend(errorMsg);
                            }
                        })
                    })

                });

                $('.add-meeting-minutes-link').on('click', function (evt) {
                    var mmModal = $('#meetingMinutesModal');
                    var url = "/signal/meeting/getById?meetingId=" + $(this).attr('data-id');
                    var meetingId = $(this).attr('data-id');
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
                            $.Notification.notify('success', 'top right', "Success", "Meeting updated.", {autoHideDelay: 5000});
                            mmModal.find('.form-control').val("");
                            mmModal.find("#attachments").val("");
                            mmModal.modal("hide");
                            signal.activities_utils.reload_activity_table();
                            signal.actions_utils.reload_action_table();
                            table.ajax.reload();
                        })
                    })
                });

                $('.edit-meeting-link').on('click', function (evt) {
                    var editMeetingModal;
                    var url = "/signal/meeting/getById?meetingId=" + $(this).attr('data-id');
                    var meetingId = $(this).attr('data-id');
                    $.ajax({
                        url: url,
                        async: false,
                        cache: false
                    }).done(function (data) {
                        editMeetingModal = $('#editMeetingModal');
                        fillModalData(data, editMeetingModal, false);
                        editMeetingModal.modal({})
                    }).fail(function (data) {
                        clear_errors();
                        var errorMsg = showErrorMessageInMeetingModal(data.responseText);
                        $('#editMeetingModal .modal-body').prepend(errorMsg)
                    });

                    $('.update-meeting,#edit-iCalander').off().on('click', function (evt) {
                        var icsFile = $(this).data('icsFile');
                        var updateUrl = "/signal/meeting/updateMeeting?meetingId=" + meetingId + "&downloadICSFile=" + icsFile;
                        var editMeetingModalForm = $("#editMeetingModal #tempForm");
                        $.ajax({
                            url: updateUrl,
                            type: "POST",
                            data: getMeetingDetails(editMeetingModalForm, 'edit'),
                            async: false,
                            processData: false,
                            contentType: false,
                            dataType: 'json',
                            cache: false
                        }).done(function (payload) {
                            $.Notification.notify('success', 'top right', "Success", "Meeting updated.", {autoHideDelay: 5000});
                            if(payload.status){
                                downloadICSFile(icsFile, payload.data);
                                editMeetingModalForm.find('.form-control').val("");
                                editMeetingModalForm.find("#attachments").val("");
                                editMeetingModal.modal("hide");
                                signal.activities_utils.reload_activity_table();
                                signal.actions_utils.reload_action_table();
                                signal.meeting_utils.reload_meeting_table();
                            }else {
                                clear_errors();
                                var errorMsg;
                                if (payload.code == 400)
                                    errorMsg = showErrorMessageInMeetingModal("Entered Email address is not valid");
                                else
                                    errorMsg = showErrorMessageInMeetingModal("Please provide information for all the mandatory fields which are marked with an asterisk (*)");
                                editMeetingModal.find('.modal-body').prepend(errorMsg);
                            }
                        })
                    });
                    $('.cancelMeeting').off().on('click', function () {
                        var appType = $('#appType').val();
                        var alertId = $('#alertId').val();
                        var meetindData = {id: meetingId, alertId: alertId, appType: appType};
                        cancelMeeting(meetindData);
                    });

                })
            },
            "initComplete": function (oSettings, json) {
                $('.meeting-create').attr('data-id', alertId)
                signal.meeting_utils.set_meeting_create_modal();
                $('#due-date-picker').datepicker({allowPastDates: true})
                $('#meeting-table_wrapper .dt-search').attr("id", "meeting-table_filter");
            },
            "ajax": {
                "url": dataUrl,
                cache: false,
                dataSrc: ""
            },
            searching: true,
            buttons: [
                {
                    text: 'New Meeting',
                    className: 'btn-primary meeting-create hide',
                    action: function (e, dt, node, config) {
                    }
                }
            ],
            aaSorting: [[6, "desc"]],
            "bLengthChange": true,
            "iDisplayLength": 10,
            "aLengthMenu": [[10, 20, 50], [10, 20, 50]],
            "aoColumns": [
                {
                    "mData": "meetingTitle",
                    mRender: function (data, type, row) {
                        var date = moment(row.meetingDate, 'DD-MMM-YYYY').format('DD-MMM-YYYY');
                        var title = escapeAllHTML(row.meetingTitle);
                        if (row.isRecurrenceMeeting) {
                            title += "(" + date + ")";
                        }
                        if (row.meetingStatus == "CANCELLED") {
                            title += "(" + row.meetingStatus + ")";
                        }
                        var actionButton = '<div class="btn-group dropdown dataTableHideCellContent" align="left"> \
                            <a class="dropdown-toggle" data-toggle="dropdown"> \
                                ' + title + '<span class="sr-only">Toggle Dropdown</span> \
                            </a> \
                            <ul class="dropdown-menu" role="menu" style="min-width: 80px !important; font-size: 12px;">';
                        if (row.isMeetingEditable) {
                            actionButton += '<li role="presentation"><a class="edit-meeting-link" role="menuitem"  data-id="' + row.id + '" href="#" >' + 'Edit Meeting' + '</a></li>';
                            if (row.isRecurrenceMeeting) {
                                actionButton += '<li role="presentation"><a class="edit-meeting-series-link" role="menuitem"  data-master-id="' + row.masterId + '" href="#" >' + 'Edit Series' + '</a></li>';
                            }
                        }
                        actionButton += '<li role="presentation"><a class="add-meeting-minutes-link" role="menuitem" data-id="' + row.id + '" href="#" >' + 'Add Minutes' + '</a></li>';
                        actionButton += '</ul></div>';
                        return actionButton;
                    }

                },
                {
                    "mData": "meetingDate",
                    mRender: function (data, type, row) {
                        return moment(data,"DD-MMM-YYYY h:mm A").format("DD-MMM-YYYY h:mm:ss A")
                    }
                },
                {
                    "mData": "meetingMinutes",
                    'className':'col-min-150 cell-break col-max-150',
                    "mRender" : function(data, type, row) {
                        return addEllipsis(row.meetingMinutes);

                    }

                },
                {
                    "mData": "meetingOwner"
                }, {
                    "mData": "meetingAgenda",
                    'className':'col-min-150 col-max-150 cell-break ',
                    "mRender" : function(data, type, row) {
                        return addEllipsisForMeeting(row.meetingAgenda);

                    }
                },
                {
                    "mData": "modifiedBy"
                },
                {
                    "mData": "lastModified"
                },
                {
                    "mData": "",
                    mRender: function (data, type, row) {
                        if (row.actionStatus == 3) {
                            return '<img src="' + "/signal/assets/icons/high_priority.png" + '"/>';
                        } else if (row.actionStatus == 2) {
                            return '<img src="' + "/signal/assets/icons/medium_priority.png" + '"/>';
                        } else if (row.actionStatus == 1) {
                            return '<img src="' + "/signal/assets/icons/low_priority.png" + '"/>';
                        } else {
                            return '';
                        }
                    }
                }

            ],
            scrollX:true,
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }]
        });
        table.button().disable();
        meetingDataTable = table;
        return table
    }

    var reload_meeting_table = function () {
        meetingDataTable.ajax.reload();
    };

    return {
        set_meeting_create_modal: set_meeting_create_modal,
        init_meeting_table: init_meeting_table,
        reload_meeting_table: reload_meeting_table
    }
})();

function showErrorMessageInMeetingModal(text) {
    var errorMsg = "<div class='alert alert-danger'>";
    if (text) {
        errorMsg += text;
    } else {
        errorMsg += 'Please fill  the required Fields';
    }
    errorMsg += "</div>";
    return errorMsg;
}

$(document).on('click', '.closeMeetingModal', function (e) {
    bootbox.confirm({
        message: "There are unsaved changes for this meeting. Do you still want to close it?",
        buttons: {
            confirm: {
                label: 'Yes',
                className: 'btn-primary'
            },
            cancel: {
                label: 'No',
                className: 'btn-default'
            }
        },
        callback: function (result) {
            bootbox.hideAll();
            if (result) {
                $('#createMeetingModal').modal('hide');
            }
        }
    });
});

function clearModal($modal) {
    $modal.find("input[type=text]").val('');
    $modal.find("textarea").val('');
}

function disableFormFields($form) {
    $form.find('select').attr('disabled', true);
    $form.find('textarea').attr('disabled', true);
    $form.find('.searchMeetingTimes').addClass('disabled');
    $(".meetingTitle").attr("disabled", "disabled");
    $(".meetingDate").attr("disabled", "disabled");
    $(".meetingTime").attr("disabled", "disabled");
    $(".meetingTime").css({"background-color":"#eeeeee"});
}

$(document).on('change', ".isRecurringMeeting", function () {
    if ($(this).is(":checked")) {
        $('.schedular-container').collapse('show');
        $(".meetingDate").attr("disabled", "disabled");
        $(".meetingTime").attr("disabled", "disabled");
    } else {
        $('.schedular-container').collapse('hide');
        $(".meetingDate").removeAttr("disabled");
        $(".meetingTime").removeAttr("disabled");
    }
});

function intializeSchedular() {
    $('.myScheduler').scheduler('value', {
        startDateTime: new Date().toISOString(),
        timeZone: {
            // offset: '+05:30',
            name: "Asia/Kolkata"
        },
        recurrencePattern: 'BYDAY=MO'
    });
}

$(document).on('click', '.searchMeetingTimes', function () {
    $(this).find('.time-slot-container').html('');
    $('.meetingSuggestionContainer').collapse('toggle');
});

$(document).on('click', '.findMeetingTimes', function () {
    $(this).find('.time-slot-container').html('');
    var text = $(this).data('text');
    var $modal;
    if (text == 'create') {
        $modal = $('#createMeetingModal');
    } else {
        $modal = $('#editMeetingModal');
    }
    var myData = {
        fmtStartDate: $('#fmtStartDate-' + text).val(),
        fmtEndDate: $('#fmtEndDate-' + text).val(),
        duration: $('#duration').val()
    };
    var attendeesArray = $modal.find("#meetingAttendees-"+text).val();
    if (attendeesArray) {
        myData.meetingAttendees = attendeesArray.toString()
    }
    var $msgContainer = $modal.find('.modal-body').find('#msg-container');
    $.ajax({
        type: "POST",
        url: $("#findMeetingTimesUrl").val(),
        data: $.param(myData),
        dataType: 'json',
    })
        .done(function (payload) {
            if (payload.status) {
                $msgContainer.hide();
                showTimeSuggestions(payload.data);
                $('.meetingSuggestionContainer').collapse('show');
            } else {
                var errorMsg = showErrorMessageInMeetingModal(payload.message);
                $msgContainer.html(errorMsg);
                $msgContainer.show();
                $('.time-slot-container').html('');
            }
        });
});

$(document).on('click', '.selectTimeSlot', function () {
    $('.meetingDate').val($(this).data('startDate'));
    $('.meetingTime').val($(this).data('startTime'));
    $('.meetingSuggestionContainer').collapse('hide');
});

function showTimeSuggestions(data) {
    var suggestionList;
    if (data) {
        suggestionList = data.meetingTimeSuggestions;
    }
    var generatedHtml = "";
    var startDate;
    var startTime;
    if (suggestionList) {
        $.each(suggestionList, function (index, value) {
            startDate = moment(value.timeSlot.startDateTime).format('MM/DD/YYYY');
            startTime = moment(value.timeSlot.startDateTime).format('HH:mm:ss');
            generatedHtml += '<div class="panel panel-warning meeting-slot-container">' +
                '<div class="panel-body">' +
                '<div class="meeting-suggestion row">' +
                // '<div class="col-md-3">'
                // + value.attendees[0].email+
                // '</div>' +
                '<div class="col-md-4">'
                + moment(value.timeSlot.startDateTime).format('DD-MMM-YYYY') + ' '
                + startTime +
                '</div>' +
                '<div class="col-md-4">'
                + moment(value.timeSlot.endDateTime).format('DD-MMM-YYYY') + ' '
                + moment(value.timeSlot.endDateTime).format('HH:mm:ss') +
                '</div>' +
                '<div class="col-md-4">' +
                '<a class="btn btn-primary selectTimeSlot" data-start-date="' + startDate + '" data-start-time="' + startTime + '">Select</a></div>' +
                '</div></div></div>';
        });
    } else {
        generatedHtml += '<div class="panel panel-warning meeting-slot-container">' +
            '<div class="panel-body">No Time Slot found for given date.</div></div>'
    }
    $('.time-slot-container').html(generatedHtml);
}

$(document).on('click', '.cancelMeetingLink', function (e) {
    e.preventDefault();
    var isEditSeries = $(this).data('isEditSeries');
    var url;
    if (isEditSeries) {
        url = $(this).attr('href') + "?masterId=" + $(this).data('master-id');
    } else {
        url = $(this).attr('href') + "&id=" + $(this).data('meeting-id');
    }
    $.ajax({
        type: "POST",
        url: url,
        data: {},
        async:false,
        dataType: 'json',
    })
        .done(function (data) {
            $("#editMeetingModal").modal('hide');
            signal.activities_utils.reload_activity_table();
            signal.actions_utils.reload_action_table();
            meetingDataTable.ajax.reload();
        });
});

function processScheduler(text, json) {
    var $schedularContainer = $('#schedularContainer');
    var html = $schedularContainer.html();
    $('.schedular-container-' + text).html(html);
    $schedularContainer.html('');
    initializeScheduler(json);
}

function unProcessScheduler(text) {
    var $schedulerClassContainer = $('.schedular-container-' + text);
    var html = $schedulerClassContainer.html();
    $('#schedularContainer').html(html);
    $schedulerClassContainer.html('');
}

function initializeScheduler(value) {
    $('#myScheduler').scheduler();
    if ((typeof value === 'undefined' || value === null)) {
        setToday();
    } else {
        $('#myScheduler').scheduler('value', {
            startDateTime: value.startDateTime,
            timeZone: {
                // name: value.timeZone.name,
                offset: value.timeZone.offset
            },
            recurrencePattern: value.recurrencePattern
        })
        var newInfo = $('#myScheduler').scheduler('value');
        $('#configSelectedTimeZone').val(newInfo.timeZone.name);
        for (var i = 0; i < 24; i++) {
            var min = 0;
            for (var j = 0; j < 2; j++) {
                $("#myScheduler #timeSelect ul").append('<li><a href="#">' + calculateAllIntervals(i, min) + '</a></li>');
                min = 30
            }
        }
    }
}

$('#myScheduler').on('changed.fu.scheduler', function (e, data) {
    var newInfo = $('#myScheduler').scheduler('value');
    $('#scheduleDateJSON').val(JSON.stringify(newInfo));
    $('#configSelectedTimeZone').val(newInfo.timeZone.name);
});

$('#myDatePicker').on('click', function () {
    var currentDate = new Date();
    var selectedDate = $("#MyStartDate").val();
    var schedulerFrom = $('#schedulerFrom').val();


    if (currentDate < new Date(selectedDate)) {
        $("#myScheduler #timeSelect ul").empty();
        for (var i = 0; i < 24; i++) {
            var min = 0;
            for (var j = 0; j < 2; j++) {
                $("#myScheduler #timeSelect ul").append('<li><a href="#">' + calculateAllIntervals(i, min) + '</a></li>');
                min = 30
            }
            $("#myStartTime").val(12 + ':' + "00" + ' ' + 'AM');
        }
    } else {
        setToday()

    }
    var newInfo = $('#myScheduler').scheduler('value');
    $('#scheduleDateJSON').val(JSON.stringify(newInfo));
    $('#configSelectedTimeZone').val(newInfo.timeZone.name);
});
if (document.getElementById("enable")) {
    var enable = $("#enable").val();
    if (enable) {
        $('#schedule *').prop('disabled', true);
    }
}

// scheduleDateJSONDefault = JSON.stringify($('#myScheduler').scheduler('value'));

function calculateNxtInterval(date, interval) {
    var hour = date.getHours() + 0;
    var minute = date.getMinutes() + interval;
    if (minute >= 60) {
        hour = hour + Math.floor(minute / 60);
        minute = minute % 60
    }
    var amPm = hour >= 12 ? "PM" : "AM";
    hour = hour % 12;
    hour = hour ? hour : 12;
    minute = (minute > 9 ? minute : "0" + minute);
    hour = hour > 9 ? hour : "0" + hour;
    var now = hour + ':' + minute + ' ' + amPm;
    return now;
}

function calculateAllIntervals(hour, minute) {
    var amPm = hour >= 12 ? "PM" : "AM";
    hour = hour % 12;
    hour = hour ? hour : 12;
    minute = (minute > 9 ? minute : "0" + minute);
    hour = hour > 9 ? hour : "0" + hour;
    var now = hour + ':' + minute + ' ' + amPm;
    return now;
}

function calculateNumberOfIntervals(hour, min, ampm) {
    hour = hour%12;
    var count = (12 - hour) * 2;
    if (ampm == "AM") {
        count = count + 48
    }
    var listCount = min < 30 ? count : count - 1;
    return listCount
}

function setToday() {

    var schedulerTime = $("#schedulerTime").val();
    if (typeof schedulerTime != "undefined") {
        schedulerTime = parseInt(schedulerTime);
    }
    var scheduledDate = new Date(); // get the date time from server
    var now = calculateNxtInterval(scheduledDate, 0);
    var hour = now.split(":")[0];
    var nowMin = now.split(":")[1].split(" ")[0];
    var nxtInterval = nowMin < 30 ? 30 - nowMin : 60 - nowMin;
    $("#time").text(now);
    var count = calculateNumberOfIntervals(hour, nowMin, now.split(":")[1].split(" ")[1]);
    for (var i = 0; i < count - 1; i++) {
        $("#myScheduler #timeSelect ul").append('<li><a href="#">' +
            calculateNxtInterval(scheduledDate, nxtInterval) + '</a></li>');
        nxtInterval += 30
    }

    var prefTimezone = $("#timezoneFromServer").val();

    var timeZoneData = null;
    if (typeof prefTimezone != "undefined" && prefTimezone != null) {
        timeZoneData = prefTimezone.split(",");
    }

    if (timeZoneData) {
        var name = timeZoneData[0].split(":")[1].trim();
        var offset = timeZoneData[1].substring(8).trim();
        $('#myScheduler').scheduler('value', {
            startDateTime: moment(scheduledDate).format("YYYY-MM-DDTHH:mm:ss.sTZD"),
            timeZone: {
                name: name,
                offset: offset
            }
        })
    }


}

function showTagWidget(id) {
    var $tag = $(id);
    $tag.select2({
        tags: true,
        placeholder: "Select Attendees",
        allowClear: true,
        width: "100%",
        createTag: function (params) {
            var term = $.trim(params.term);
            if (term != "") {
                return {
                    id: term,
                    text: term,
                }
            }

            return null
        }
    })

}

function downloadICSFile(icsFile, data) {
    if (icsFile) {
        window.open("data:text/calendar;charset=utf8," + escape(data));
    }
}

function cancelMeeting(meetingData) {
    $.ajax({
        type: "POST",
        url: '/signal/meeting/cancelMeeting',
        data: meetingData,
        async: false,
        dataType: 'json',
    })
        .done(function (data) {
            if (data.status) {
                $.Notification.notify('success', 'top right', "Success", data.message, {autoHideDelay: 5000});
                $("#editMeetingModal").modal('hide');
                signal.activities_utils.reload_activity_table();
                signal.actions_utils.reload_action_table();
                meetingDataTable.ajax.reload();
            }
        })
        .fail(function (data) {
            $.Notification.notify('error', 'top right', "Error", data.message, {autoHideDelay: 5000});
            $("#editMeetingModal").modal('hide');

        });
}
