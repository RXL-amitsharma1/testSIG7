//= require app/pvs/common/rx_common.js
//= require app/pvs/common/rx_handlebar_ext.js
//= require app/pvs/common/rx_alert_utils.js
//= require app/pvs/common/rx_list_utils.js
//= require app/pvs/actions/actions.js
//= require app/pvs/meeting/meeting.js

var applicationName = APPLICATION_NAME.SIGNAL_MANAGEMENT;
var applicationLabel = APPLICATION_LABEL.SIGNAL_MANAGEMENT;
var appType = ALERT_CONFIG_TYPE.SIGNAL_MANAGEMENT;
var statusHistoryList = [];
var map={};

$(function () {
    var action_table;
    var meeting_table;

    var init = function () {
        action_table = signal.actions_utils.init_action_table($('#action-table'), appType);
        meeting_table = signal.meeting_utils.init_meeting_table($('#meeting-table'));
        setCreatedDate();
    };

    $(document).on('click', '#signal-history-table .btn-add-row', function (evt) {
        if (!$(this).hasClass('disabled')) {
            var addedStatus = [];
            $('.status').each(function (){
                if(!$(this).prop('disabled') && $(this).is(":visible")){
                    addedStatus.push($(this).val())
                }
            });
            var $signalHistoryRow = $(this).closest('.signalHistoryRow');
            $signalHistoryRow.find('.btn-add-row').remove();
            var $tr = $('.tr_clone');
            var $clone = $tr.clone();
            $clone.removeClass('hidden');
            if ($signalHistoryRow.hasClass('odd')) {
                $clone.addClass('even')
            } else {
                $clone.addClass('odd')
            }
            $clone.find("select[name*='signalStatus'] > option").each(function() {
                if($.inArray($(this).text(), addedStatus ) != -1){
                    $(this).remove();
                }
            });
            $clone.find('.historyDatePicker').datepicker({
                date: null,
                allowPastDates: true,
                momentConfig: {
                    culture: userLocale,
                    tz: userTimeZone,
                    format: DEFAULT_DATE_DISPLAY_FORMAT
                },
                restricted: [{from: TOMORROW , to: Infinity}]
            });
            $tr.after($clone);
            $(".date-created").on("focusout", function(){
                $(this).val(newSetDefaultDisplayDateFormat( $(this).val()));
                if($(this).val()=='Invalid date'){
                    $(this).val('')
                }
            });
        }
    });

    $(document).on('click', '#signal-history-table .save-history', function (evt) {
        //enabling disabled values before ajax call in order to get the values of that field
        var currentObject= $(this);
        $('select[name=signalStatus]').find('option').attr("disabled", false);
        $(this).attr("disabled", true);
        var $tr = $(this).closest('.signalHistoryRow');
        var signalStatus = $tr.find('.status').val();
        var createdDate = $tr.find('.date-created').val();
        var statusComment = $tr.find('.comment').val();
        var signalHistoryId = $tr.find('.signalHistoryId').val();
        var fromSignalStatus = $tr.find('.workflow-from-state').val();
        var today = moment.tz(userTimeZone).format("DD-MMM-YYYY");
        var givenDate = new Date(createdDate);
        var todayDate = new Date(today);
         $(currentObject).parent().parent().append('<span id="report-generating" style=" font-size:20px"  class="fa fa-spinner fa-spin"></span>');
        if (!signalStatus || !createdDate) {
            $.Notification.notify('warning', 'top right', "Warning", $.i18n._('signalHistory.saveWarning'), {autoHideDelay: 5000});
        } else if (givenDate > todayDate) {
            $.Notification.notify('warning', 'top right', "Warning", "User cannot create and save any entry with future date range.", {autoHideDelay: 5000});
        } else if(map.signalHistoryId!=signalStatus + createdDate + statusComment){
           map.signalHistoryId=signalStatus + createdDate + statusComment;
            $.ajax({
                url: saveSignalStatusHistory,
                type: "POST",
                data: {
                    'createdDate': createdDate,
                    'statusComment': statusComment,
                    'signalStatus': signalStatus,
                    'signalHistoryId': signalHistoryId,
                    'signalId': $("#signalIdPartner").val(),
                    'fromSignalStatus' : fromSignalStatus,
                    'enableSignalWorkflow' : enableSignalWorkflow,
                    'isWorkflowUpdated': true
                },
            }).done(function (response) {
                if (response.status) {
                    $('#signalHistory').html(response.data);
                    setCreatedDate();
                    if (response.value == null || response.value == 'null') {
                        $('#dueInHeader').html('-');
                        $(".editButtonEvent").hide();
                    } else {
                        $('#dueInHeader').html(response.value + " Days");
                        $("#dueDatePicker").hide();
                        $(".editButtonEvent").show();
                    }
                    if (response.notificationDisplayFlag)
                        $.Notification.notify('success', 'top right', "Success", $.i18n._('signalHistory.success'), {autoHideDelay: 5000});
                } else {
                    if (null != response.data) {
                        $tr.find('.date-created').val(response.data);
                        $.Notification.notify('error', 'top right', "Error", "Validation date is already available.", {autoHideDelay: 5000});
                    } else {
                        $.Notification.notify('error', 'top right', "Error", response.message, {autoHideDelay: 5000});
                    }
                }
                disableOptions(disabledValues);
                disableDueDate()
                if (JSON.parse(validationDateSynchingEnabled) && signalStatus == defaultValidatedDate) {
                    $("#detectedDate").val(createdDate)
                }
                $('#signalActivityTable').DataTable().ajax.reload();
            })
        }
        $(currentObject).parent().parent().find("#report-generating").remove()
        $(this).attr("disabled", false);
        disableOptions(disabledValues);
        });

    $(document).on('change', '.file-uploader .file', function () {
        var currentElement = $(this);
        var inputBox = currentElement.parent('.file-uploader').find('.form-control');
        if (!_.isEmpty(currentElement.val())) {
            inputBox.val(currentElement.val().replace(/C:\\fakepath\\/i, ''));//todo remove replace method
        }
        inputBox.trigger('change');
    });

    init();
});

function setCreatedDate() {

    $('.date-created').on("focusout", function(){
        $(this).val(newSetDefaultDisplayDateFormat( $(this).val()));
        if($(this).val()=='Invalid date'){
            $(this).val('')
        }
    });

    $('.historyDatePicker').each(function () {
        var selectedDate = $(this).find('.date-created').val();
        $(this).datepicker({
            date: selectedDate ? selectedDate : null,
            allowPastDates: true,
            momentConfig: {
                culture: userLocale,
                tz: userTimeZone,
                format: DEFAULT_DATE_DISPLAY_FORMAT
            },
            restricted: [{from: TOMORROW , to: Infinity}]
        })
    });

}
