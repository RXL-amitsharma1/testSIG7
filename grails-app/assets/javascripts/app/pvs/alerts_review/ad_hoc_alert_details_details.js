//= require app/pvs/common/rx_common.js
//= require app/pvs/common/rx_handlebar_ext.js
//= require app/pvs/common/rx_alert_utils.js
//= require app/pvs/common/rx_list_utils.js
//= require app/pvs/activity/activities.js
//= require app/pvs/actions/actions.js

var applicationName = "Ad-Hoc Alert";
var appType = "Ad-Hoc Alert";

$(function() {

    $('.attachment-file').on('change', function () {
        // Enable the upload button when a file is selected
        $("#disableBtn").prop('disabled', false);
    });
    $('#myModal').on('shown.bs.modal', function () {
        $("#disableBtn").prop('disabled', true);
        $("#attachmentForm input[name='attachments']").val('');
        $("#attachmentForm input[name='description']").val('');
        $("#attachmentForm input[name='attachments']").on('change', function () {
            if (this.files.length > 0) {
                $("#disableBtn").prop('disabled', false);
            } else {
                $("#disableBtn").prop('disabled', true);
            }
        });
    });

    $('#deleteAttachment a[href]').on('click', function () {
        $(this).addClass('disabled-link');
    });
    var activity_table;
    var action_table;

    var init = function() {
        activity_table = signal.activities_utils.init_activities_table('#activity_list',
            '/signal/activity/activitiesByAlert?id=' + alertId+"&appType="+applicationName, applicationName);
        action_table = signal.actions_utils.init_action_table($('#action-table'), appType);
        $('.rxmain-container-show').css({'overflow': 'auto'});
    };
    init();
    var reviewDelay = $("#reviewDelay").val();
    if(reviewDelay == "false"){
        $("#reasonBox").prop("disabled", true);
        $(".add-reason").prop("disabled", true);
    }
    var reasonForDelay = $("#reasonForDelay").val();
    if(reasonForDelay){
        $("#reasonBox").val(reasonForDelay)
    }
    $(".comment-icon").on("click", function () {
        var adhocAlertId = alertId;
        $.ajax({
            type: "POST",
            data: {'adhocAlert.id': adhocAlertId},
            url: fetchCommentUrl,
        })
            .done(function (result) {
                $("#commentsModal #commentNotes").val(encodeToHTML(result.comment));
            });
        $("#commentsModal #adhocAlertId").val(adhocAlertId);
        $("#commentsModal").modal('show');
    })
});

function saveReason() {
    $.ajax({
        type: "POST",
        data: {'adhocAlert.id': alertId, 'reason': $("#reasonBox").val()},
        url: saveReasonUrl
    })
        .done(function (result) {
            if (result.success) {}
        });
}

var saveCommentNotes =  function(){
    $.ajax({
        type: "POST",
        data: {'adhocAlert.id': $("#adhocAlertId").val(), 'comment': $("#commentNotes").val()},
        url: saveCommentUrl,
    })
        .done(function (result) {
            if (result.success) {}
        });
};
