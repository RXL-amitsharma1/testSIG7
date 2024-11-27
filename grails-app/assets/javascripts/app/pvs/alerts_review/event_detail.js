//= require app/pvs/common/rx_common.js
//= require app/pvs/common/rx_alert_utils.js
//= require app/pvs/alerts_review/alert_review.js
//= require app/pvs/alertComments/alertComments.js
//= require app/pvs/actions/actions.js
//= require app/pvs/validated_signal/signal_charts.js

var applicationLabel = APPLICATION_LABEL.EVENT_DETAIL;
var applicationName = APPLICATION_NAME.EVENT_DETAIL;

$(function () {
    $(function () {
        $('#eventDetailTree').on('click', '.jstree-anchor', function (e) {
            $('#eventDetailTree').jstree(true).toggle_node(e.target);
        }).jstree({
            'core': {
                'data': window.node,
                'dblclick_toggle': false
            }
        });

        $('#eventDetailTree').on("dblclick.jstree", function (event) {
            var node = $(event.target).closest("li");
            var $targetid = node[0].id;
            $targetid = $targetid.length && $targetid || $('[id=' + this.hash.slice(1) + ']');
            $targetid = "#"+$targetid+"_container";
            $($targetid).collapse('hide');
        });

        $('#eventDetailTree').on('ready.jstree', function () {
            $("#eventDetailTree .jstree-anchor").each(function () {
                var val = $(this).attr('id').replace("anchor", "container");
                $(this).attr('href', '#' + val)
            });
            var scrollDuring = 1000;
            var scrollBegin = 163;
            $('a.jstree-anchor').on('click', function () {
                $('#case_detail_container').collapse('show');
                if (location.pathname.replace(/^\//, '') === this.pathname.replace(/^\//, '') && location.hostname === this.hostname) {
                    var $targetid = $(this.hash);
                    $targetid = $targetid.length && $targetid || $('[id=' + this.hash.slice(1) + ']');
                    $($targetid.selector).collapse('show');
                    if ($targetid.length) {
                        var targetOffset = $targetid.offset().top - scrollBegin;
                        $('html,body').animate({scrollTop: targetOffset}, scrollDuring);
                        $("a").removeClass("active");
                        $(this).addClass("active");
                        return false;
                    }
                }
            })
        });

        $('#eventDetailTree').on("changed.jstree", function (e, data) {
            window.selectedId = data.selected;
            var containerText = getContainerText(data.node.text);
            $('#' + containerText).collapse('toggle');
        });

        $('.collapse').collapse('toggle');

        function getContainerText(text) {
            return text.toLowerCase().replace(/ /g, "_") + "_container";
        }
    });
    addCountBoxToInputField(8000,$('#attachmentDescription'));
    $("#attachmentForm input[type=file]").on("change", function () {
        var imgVal = $(this).val();
        var imgSize;
        if (imgVal != '') {
            imgSize = $(this)[0].files[0].size;
        }
        if (imgVal != '' && imgSize < 20000000) {
            $('#fileSizeMessage').hide()
            $("[name=_action_upload]").attr('disabled', false)
        } else if (imgVal != '') {
            $('#fileSizeMessage').show();
            $("[name=_action_upload]").attr('disabled', true);
        } else {
            $("[name=_action_upload]").attr('disabled', true);
        }
    });


    $("form#attachmentForm").off('submit').on('submit', function () {
        var formData = new FormData(this);
        $("form#attachmentForm input[type=submit]").attr('disabled', true)
        $.ajax({
            url: getUploadUrl,
            type: "POST",
            data: formData,
            async: true,
            cache: false,
            contentType: false,
            processData: false
        })
            .done(function () {
                $.Notification.notify('success', 'top right', "Success", "Attachment added successfully.", {autoHideDelay: 5000});
                $('#attachmentCaseDetailModal').modal('hide');
                $('#attachment-table').DataTable().ajax.reload();
            })
            .fail(function () {
                $("form#attachmentForm input[type=submit]").attr('disabled', false);
                $.Notification.notify('error', 'top right', "Error", "Sorry, This File Format Not Accepted", {autoHideDelay: 5000});
            });
        return false;
    });


    $('#attachmentCaseDetailModal').on('hidden.bs.modal', function (e) {
        $(this)
            .find("input[type=file],input[type=text]")
            .val('')
            .end()
            .find("[name=_action_upload]")
            .attr('disabled', true)
            .end();
        $("#fileSizeMessage").css({display:"none"});
    });

    $('#attachment-table').DataTable({
        destroy: true,
        searching: false,

        responsive: false,
        language: {
            "url": "../assets/i18n/dataTables_" + userLocale + ".json"
        },
        "ajax": {
            "url": getAttachmentUrl,
            "dataSrc": ""
        },
        drawCallback: function () {
            $('.remove-attachment').on('click', function (e) {
                var attachmentRow = $(e.target).closest('tr');
                var attachmentId = attachmentRow.find('span[data-field="removeAttachment"]').attr("data-attachmentId");
                var removeUrl = deleteAttachmentUrl + '&attachmentId=' + attachmentId + '&isArchived=' + isArchived;
                $.ajax({
                    type: "POST",
                    url: removeUrl,
                    beforeSend: function () {
                        $('.remove-attachment').css('pointer-events', 'none'); // Disables the span element
                        $('.remove-attachment').css('opacity', '0.5');
                    }
                })
                    .done(function (result) {
                        if(result.success) {
                            $.Notification.notify('success', 'top right', "Success", "Attachment deleted successfully.", {autoHideDelay: 5000});
                            $('#attachment-table').DataTable().ajax.reload();
                            setTimeout(function () {
                                $('.remove-attachment').css('pointer-events', 'auto'); // Enables the span element
                                $('.remove-attachment').css('opacity', '1');
                            }, 10000);
                            }
                        })
                    .fail(function () {
                        $.Notification.notify('error', 'top right', "Error", $.i18n._('attachmentDeleteFailed'), {autoHideDelay: 5000});
                        setTimeout(function () {
                            $('.remove-attachment').css('pointer-events', 'auto'); // Enables the span element
                            $('.remove-attachment').css('opacity', '1');
                        }, 10000);
                    })
            })
        },
        "aoColumns": [
            {
                "mData": "name",
                "mRender": function (data, type, row) {
                    var downloadUrl = attachmentDownloadUrl + "?id=" + row.id + '&isArchived=' + isArchived;
                    return '<a href="'+downloadUrl+'">' + row.name + '</a>'
                }
            }, {
                "mData": "description"
            }, {
                "mData": "timeStamp",
                "mRender": function (data, type, row) {
                    if(row.timeStamp=="null" || row.timeStamp==null){
                        return '' ;
                    } else {
                        return moment.utc(row.timeStamp).format('DD-MMM-YYYY hh:mm:ss A');
                    }
                }
            }, {
                "mData": "modifiedBy"
            }, {
                "mData": "id",
                "sortable": false,
                "mRender": function (data, type, row) {
                    return '<span tabindex="0" title="Remove Attachment" class="glyphicon glyphicon-remove remove-attachment" style="cursor: pointer" data-field="removeAttachment" data-attachmentId=' + row.id + '></span>'
                },
                "visible": isVisible()
            }
        ],
        "bLengthChange": false,
        columnDefs: [{
            "targets": '_all',
            "render": $.fn.dataTable.render.text(),
            orderSequence: ['desc', 'asc']
        }]
    });

    function isVisible() {
        var isVisible = true;
        if(typeof hasReviewerAccess !== "undefined" && !hasReviewerAccess) {
            isVisible = false;
        }
        return isVisible
    }

    var checkedIdList = [];
    var checkedRowList = [];

    var eventCommentJson = {
        "alertType": alertType,
        "productName": $(".comment").find("#productName").html(),
        "eventName": $(".comment").find("#eventName").html(),
        "configId": $(".comment").find("#configId").html(),
        "ptCode": $(".comment").find("#ptCode").html(),
        "productId": $(".comment").find("#productId").html(),
        "executedConfigId": $(".comment").find("#executedConfigId").html()
    };
    signal.alertComments.populate_comments($(".comment"), eventCommentJson);
    signal.alertComments.save_comment($(".comment"));
    signal.actions_utils.init_action_table($('#action-table'), alertType);
    signal.alertReview.enableMenuTooltips();
    signal.alertReview.disableTooltips();
    bindAssignedToSelect();
    initEventHistorySection();
    $('#assignedTo').append(option).trigger('change');
});

var bindAssignedToSelect = function() {
    $('#assignedTo').on("select2:opening", function (e) {
        if(typeof hasReviewerAccess !== "undefined" && !hasReviewerAccess){
            e.preventDefault();
            $.Notification.notify('warning', 'top right', "Warning", "You don't have access to perform this action", {autoHideDelay: 5000});
        }
    });
    $('#assignedTo').on("select2:selecting", function(e) {
        var value = e.params.args.data.id;
        $.ajax({
            url: changeAssignedToUrl,
            data:{
                assignedToValue: value,
                isArchived: $('#isArchived').val(),
                selectedId: JSON.stringify([parseInt($('#alertId').val())])
            }
        }).done(function (payload) {
            if(payload.status){
                $.Notification.notify('success','top right', "Success", payload.message, {autoHideDelay: 5000});
            }else{
                $.Notification.notify('error','top right', "Error", payload.message, {autoHideDelay: 5000});
            }
        });
    });
};

var initEventHistorySection = function () {

    var productName = $('#productNameE').val();
    var eventName = $('#eventNameE').val();
    var configId = $('#configIdE').val();
    var executedConfigId = $('#executedConfigIdE').val();
    var callingApp = $("#appNameE").val();
    var alertId = $("#alertId").val();

    if(callingApp == ALERT_CONFIG_TYPE.AGGREGATE_CASE_ALERT){
        signal.productEventHistoryTable.init_current_alert_history_table(productEventHistoryUrl, productName, eventName, configId, executedConfigId);
        signal.productEventHistoryTable.init_other_alerts_history_table(productEventHistoryUrl, productName, eventName, configId);
    }else if(callingApp == ALERT_CONFIG_TYPE.EVDAS_ALERT){
        signal.evdasHistoryTable.init_evdas_history_table(evdasHistoryUrl, productName, eventName, configId, alertId);
    }
};

function getProductJson() {
    var productName = $('#productName').val();
    var level = 3;
    return generateProductJson(productName, level);
}

function generateProductJson(productName, level) {
    var signalProductValues = {"1": [], "2": [], "3": [], "4": [], "5": []};
    signalProductValues[level].push({name: productName});
    return signalProductValues;
}
