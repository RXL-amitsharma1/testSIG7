<%@ page import="com.rxlogix.util.ViewHelper; com.rxlogix.Constants; com.rxlogix.config.Disposition; com.rxlogix.enums.DictionaryTypeEnum; com.rxlogix.enums.ReportFormat" %>
<g:set var="grailsApplication" bean="grailsApplication"/>
<g:javascript>
        var actionItemUrl = "${createLink(controller: "action", action: "listByCurrentUser")}";
        var statusValue = "${Constants.Commons.ALL}";
        var changeAssignedToUrl = "${createLink(controller: "validatedSignal", action: 'changeAssignedTo')}";
        var changeGroupUrl = "${createLink(controller: "validatedSignal", action: 'changeGroup')}";
        var signalHistoryUrl = "${createLink(controller: "validatedSignal", action: 'fetchSignalHistory', params: [id: signal.id])}";
        var meetingDetailsUrl = "${createLink(controller: "validatedSignal", action: "exportMeetingDetailReport")}";
        var getAttachmentUrl = "${createLink(controller: "validatedSignal", action: "fetchAttachments", params: [alertId: signal.id])}";
        var deleteAttachmentUrl = "${createLink(controller: "validatedSignal", action: "deleteAttachment", params: [alertId: signal.id])}";
        var getUploadUrl = "${createLink(controller: "validatedSignal", action: "upload", params: [id: signal.id])}";
        var addReferenceUrl = "${createLink(controller: "validatedSignal", action: "addReference", params: [id: signal.id])}";
        var appType = "Signal Management";
        var alertId = "${signal.id}";
        var linkedSignalDetailUrl="${createLink(controller: 'validatedSignal', action: 'details')}";
        var workflowStates = JSON.parse("${workflowStatesSignal}");
</g:javascript>
<asset:javascript src="app/pvs/activity/activities.js"/>
<asset:javascript src="app/pvs/meeting/meeting.js"/>
<asset:javascript src="app/pvs/alertComments/alertComments.js"/>
<asset:javascript src="app/pvs/alerts_review/alert_review.js"/>
<asset:javascript src="app/pvs/validated_signal/actionManagement.js"/>
<asset:javascript src="app/pvs/signalHistory/signalHistoryTable.js"/>
<asset:javascript src="app/pvs/date-time/bootstrap-datetimepicker.js"/>
<asset:stylesheet src="app/pvs/bootstrap-datetimepicker.css"/>

<script>
    var applicationName = "Signal Management";

    $(document).ready(function () {

        var partnerTable = null;

        var init_table = function () {

            var signalIdPartner = $("#signalIdPartner").val();

            if (typeof partnerTable != "undefined" && partnerTable != null) {
                partnerTable.destroy()
            }

            //Data table for the document modal window.
            partnerTable = $("#partnerTable").DataTable({
                "language": {
                    "url": "/assets/i18n/dataTables_" + userLocale + ".json"
                },
                fnInitComplete: function () {
                    $('[data-toggle="tooltip"]').tooltip()
                },
                "ajax": {
                    "url": 'partnersList?signalId=' + signalIdPartner,
                    "dataSrc": ""
                },
                searching: true,
                "bLengthChange": true,
                "iDisplayLength": 5,
                "aaSorting": [[1, "desc"]],
                "aLengthMenu": [[10, 20, 50, -1], [10, 20, 50, "All"]],
                "aoColumns": [
                    {
                        "mData": "",
                        "mRender": function (data, type, row) {
                            return '<input type="checkbox" class="editor-active copy-select">';
                        }
                    },
                    {
                        "mData": "productName",
                        "className": "dt-center"
                    },
                    {
                        "mData": "partnerName",
                        "className": "dt-center"
                    },
                    {
                        "mData": "partnerContact",
                        "className": "dt-center"
                    },
                    {
                        "mData": "partnerEmailContact",
                        "className": "dt-center"
                    },
                    {
                        "mData": "contactingEntity",
                        "className": "dt-center"
                    }
                ]

            });
        };

        $('.haSignalStatus').select2();

        $(".sharedWithPartners").click(function () {
            var partnerModal = $("#partnerModal");
            partnerModal.modal("show");
            init_table();
        });

        var assignmentType = $("#assignmentType").val();

        if (assignmentType == "USER") {
            $("#assignedToUserDiv").removeClass("hide");
            $("#assignedToGroupDiv").addClass("hide");
        } else {
            $("#assignedToUserDiv").addClass("hide");
            $("#assignedToGroupDiv").removeClass("hide");
        }

        $(".showWorkflowHistory").click(function () {
            var workflowHistoryModal = $("#signalHistoryModal");
            workflowHistoryModal.find('.modal-lg').css('width', '1180px');
            workflowHistoryModal.modal("show");
            signal.signalHistoryTable.init_signal_history_table(signalHistoryUrl);
        });

        if ($("#signalActionTaken").val()) {
            var sourceArray = $("#signalActionTaken").val().toString().replace("[", "").replace("]", "").split(',');
            for (var i = 0; i < sourceArray.length; i++) {
                sourceArray[i] = sourceArray[i].trim()
            }
            $("#action_name").select2().val(sourceArray).trigger('change')
        } else {
            $("#action_name").select2()
        }

        function triggerChangesOnModalOpening(extTextAreaModal) {
            extTextAreaModal.on('shown.bs.modal', function () {
                $('textarea').trigger('keyup');
                //Change button label.
                if (extTextAreaModal.find('.textAreaValue').val()) {
                    extTextAreaModal.find(".updateTextarea").html($.i18n._('labelUpdate'));
                } else {
                    extTextAreaModal.find(".updateTextarea").html($.i18n._('labelAdd'));
                }
            });
        }

        $(document).on('click', '#signal-history-table .openStatusComment,#reference-table .openStatusComment,.reference-table-foot .openStatusComment,#signal-rmms-table .openStatusComment,#signal-communication-table .openStatusComment', function (evt) {
            evt.preventDefault();
            var extTextAreaModal = $("#textarea-ext1");
            triggerChangesOnModalOpening(extTextAreaModal);
            var commentBox = $(this).prev('.comment');
            extTextAreaModal.find('.textAreaValue').val(commentBox.val());
            if(($.i18n._('statusHistoryComment') === "Status Comment")){
              addCountBoxToInputField(8000, extTextAreaModal.find('textarea'));
            }
            extTextAreaModal.find('.modal-title').text($.i18n._('statusHistoryComment'));
            if (commentBox.prop('disabled')) {
                extTextAreaModal.find('.textAreaValue').prop('disabled', true);
                extTextAreaModal.find('.updateTextarea').prop('disabled', true);
            } else {
                extTextAreaModal.find('.textAreaValue').prop('disabled', false);
                extTextAreaModal.find('.updateTextarea').prop('disabled', false);
            }
            extTextAreaModal.modal("show");
            updateTextAreaData(commentBox);
        });

        function updateTextAreaData(container) {
            var extTextAreaModal = $("#textarea-ext1");
            $('#textarea-ext1 .updateTextarea').off("click").on('click', function (evt) {
                evt.preventDefault();
                container.val(extTextAreaModal.find('textarea').val());
                extTextAreaModal.modal("hide");
            });
        }


        $("form#attachmentForm").unbind('submit').on('submit', function () {
            $(this).find('#description').val(encodeToHTML($(this).find('#description').val()));
            var formData = new FormData(this);
            $.ajax({
                url: getUploadUrl,
                type: "POST",
                data: formData,
                dataType: "json",
                async: true,
                success: function () {
                    $('#myModal').modal('hide');
                    $.Notification.notify('success', 'top right', "Success", $.i18n._('attachmentAddedSuccess'), {autoHideDelay: 10000});
                    $("#attachmentForm input[name='attachments']").val('');
                    $("#attachmentForm input[name='description']").val('');
                    $('#attachment-table').DataTable().ajax.reload();
                }, error: function (data) {
                    $('#myModal').modal('hide');
                    var response = JSON.parse(data.responseText)
                    $.Notification.notify('error', 'top right', "Error", response.message, {autoHideDelay: 10000});
                    $("#attachmentForm input[name='attachments']").val('');
                    $("#attachmentForm input[name='description']").val('');
                },
                cache: false,
                contentType: false,
                processData: false
            });
            return false;
        });

        $("form#referenceForm").unbind('submit').on('submit', function () {
            $(this).find('#description').val(encodeToHTML($(this).find('#description').val()));
            $(this).find('#referenceLink').val(encodeToHTML($(this).find('#referenceLink').val()));
            var formData = new FormData(this);
            $.ajax({
                url: addReferenceUrl,
                type: "POST",
                data: formData,
                async: false,
                success: function () {
                    $('#myReference').modal('hide');
                    $.Notification.notify('success', 'top right', "Success", $.i18n._('referenceAddedSuccess'), {autoHideDelay: 10000});
                    $('#myReference input[name="referenceLink"]').val('');
                    $("#myReference input[name='description']").val('');
                    $('#attachment-table').DataTable().ajax.reload();
                },
                cache: false,
                contentType: false,
                processData: false
            });
            return false;
        });

        $('#attachment-table').DataTable({
            destroy: true,
            searching: false,
            sPaginationType: "bootstrap",
            responsive: false,
            language: {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json"
            },
            "ajax": {
                "url": getAttachmentUrl,
                "dataSrc": ""
            },
            fnDrawCallback: function () {
                colEllipsis();
                webUiPopInit();
                $('.remove-attachment').click(function (e) {
                    var attachmentRow = $(e.target).closest('tr');
                    var attachmentId = attachmentRow.find('span[data-field="removeAttachment"]').attr("data-attachmentId");
                    var removeUrl = deleteAttachmentUrl + '&attachmentId=' + attachmentId;
                    $.ajax({
                        type: "POST",
                        url: removeUrl,
                        success: function () {
                            $('#attachment-table').DataTable().ajax.reload();
                            $.Notification.notify('success', 'top right', "Success", $.i18n._('attachmentRemovedSuccess'), {autoHideDelay: 10000});
                        },
                        error: function () {
                            $.Notification.notify('error', 'top right', "Error", $.i18n._('attachmentDeleteFailed'), {autoHideDelay: 10000});
                        }
                    })
                })
            },
            "aoColumns": [
                {
                    "mData": "link",
                    'className':'col-min-150 col-max-200',
                    "mRender": function (data, type, row) {
                        if(row.type === ATTACHMENT_TYPE_ENUM.ATTACHMENT){
                            return "<a class='word-wrap-break-word' href='/signal/attachmentable/download?id=" + row.id + "'>" + row.link + "</a>";
                        } else {
                            if(row.link.toLowerCase().startsWith("http")){
                                return "<a class='word-wrap-break-word' href='" + row.link + "'>" + row.link + "</a>";
                            }else{
                                return "<span style='white-space: pre-wrap;word-break: Normal;'>"+row.link+"</span>";
                            }
                        }
                    }
                },
                {
                    "mData": "type"
                },
                {
                    "mData": "description",
                    'className': 'col-min-150 col-max-200 cell-break',
                    "mRender": function (data, type, row) {
                        var colElement = '<div class="col-container"><div class="col-height">';
                        colElement += '<p style="white-space: pre-wrap;word-break: Normal;">'+row.description+'</p>';
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + row.description + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    }
                },
                {
                    "mData": "timeStamp"
                },
                {
                    "mData": "modifiedBy"
                },
                {
                    "mData": "id",
                    "mRender": function (data, type, row) {
                        return '<span tabindex="0" title="${message(code:"app.label.remove.attachment")}" class="glyphicon glyphicon-remove remove-attachment" style="cursor: pointer" data-field="removeAttachment" data-attachmentId=' + row.id + '></span>'
                    }
                }
            ],
            "bLengthChange": false,
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text()
            }]
        });

        $('#myReference').on('show.bs.modal', function(){
            $(this).find('.refBlank').val('');
        });

        $('.manageWorkflowStateHeader').select2();
        $('.manageWorkflowStateHeader').next(".select2-container").hide();
        var workflowStateStart="";
        $("#workflowHeader").on("click", function (event) {
            event.preventDefault();
            if(typeof hasReviewerAccess !=="undefined" && !hasReviewerAccess){
                $.Notification.notify('warning', 'top right', "Warning", "You don't have access to perform this action", {autoHideDelay: 5000});
            } else {
                workflowStateStart = $("#workflowHeader").text();
                var $selector = $(".manageWorkflowStateHeader");
                $selector.val(null).trigger("change.select2");
                $selector.empty();
                var currentState = $(this).text();
                $selector.append(new Option(currentState, currentState, false, false)).trigger('change.select2');
                $.each(workflowStates[currentState], function (key, val) {
                    $selector.append(new Option(val, val, false, false)).trigger('change.select2');
                });
                $(this).hide();
                $selector.next(".select2-container").show();
                $selector.select2('open');
            }
        });


        $('.manageWorkflowStateHeader').on("select2:close", function (e) {
            if(possibleDispositions[workflowStateStart].length !== 0 && $.inArray($('.dispositionId').attr("data-id"), possibleDispositions[workflowStateStart])  === -1) {
                $("#workflowHeader").text(workflowStateStart).show();
            } else {
                $("#workflowHeader").text($(this).val()).show();
            }
            $(this).next(".select2-container").hide();
        });

        $('.manageWorkflowStateHeader').change(function () {
            if(possibleDispositions[workflowStateStart].length !== 0 && $.inArray($('.dispositionId').attr("data-id"), possibleDispositions[workflowStateStart])  === -1) {
                $.Notification.notify('warning', 'top right', "Warning",  "The decision for the signal has not been completed yet. Please change the disposition before moving to the next signal workflow state.", {autoHideDelay: 20000});
            } else {
                $.ajax({
                    url: saveWorkflowStateUrl,
                    data: {
                        'workflowStateStart': workflowStateStart,
                        'workflowStateEnd': $(this).val(),
                        'signalId': $("#signalIdPartner").val(),
                        'isWorkflowUpdate': true
                    },
                    success: function (response) {
                         if (response.status) {
                            $.Notification.notify('success', 'top right', "Success", $.i18n._('workflowState.success'), {autoHideDelay: 20000});
                            if (response.data.dueIn!=null) {
                                $('#dueInHeader').html(response.data.dueIn + " Days");
                                $('#signal-history-table select option[value="Due Date"]:selected').parent().parent().parent().find(".date-created").val(response.data.dueDate)
                                $('#signalActivityTable').DataTable().ajax.reload();
                               if(response.value>0){
                                    $("#dueDatePicker").hide();
                                    $(".editButtonEvent").show();
                                 }
                            }else{
                                    $('#dueInHeader').html( "-");
                                    $("#dueDatePicker").hide();
                                    $(".editButtonEvent").hide();
                            }
                            $("#revertDisposition").hide()
                             var title = $("#workflowHeader").text();
                             $("#workflowHeader").attr('data-original-title',title);
                            updateSignalHistory();
                             $('#signalActivityTable').DataTable().ajax.reload();
                        } else {
                            $.Notification.notify('error', 'top right', "Error", response.message, {autoHideDelay: 20000});
                        }
                    }
                });
            }
        });

        $('.assigned').hide();
        $(document).on("click", "#assignedToAction", function (e) {
            e.preventDefault();
            if(typeof hasReviewerAccess !=="undefined" && !hasReviewerAccess){
                $.Notification.notify('warning', 'top right', "Warning", "You don't have access to perform this action", {autoHideDelay: 5000});
            } else {
                $('#assignedToAction').toggle();
                $('.assigned').toggle();
                $('#assignedToActionAndWorkflow').select2('open');
                $("#assignedToActionAndWorkflow").siblings(".select2").find(".select2-selection__clear").text("")
            }
        });

        $('#assignedToActionAndWorkflow').on("select2:close", function (e) {
            $("#assignedToAction").text($(this).select2('data')[0].text).toggle();
            $('.assigned').toggle();
        });


        $('#assignedToActionAndWorkflow').change(function () {
                $.ajax({
                    url: saveAssignedTo,
                    data: {
                        'currentUser': $(this).val(),
                        'signalId': $("#signalIdPartner").val()
                    },
                    success: function (response) {
                        if (response.status) {
                            var title = $("#assignedToAction").text();
                            $("#assignedToAction").attr('data-original-title',"")
                            $("#assignedToAction").attr('title',title);
                            $.Notification.notify('success', 'top right', "Success", $.i18n._('assignedTo.success'), {autoHideDelay: 20000});
                            signal.activities_utils.reload_activity_table("#signalActivityTable", activityUrl, applicationName);
                        } else {
                            $.Notification.notify('error', 'top right', "Error", response.message, {autoHideDelay: 20000});
                        }
                    }
                });
        });
        $(".action-create").eq(1).hide();
    });

</script>

<style>
a {
    color: #428bca;
}

.icon-calendar {
    background-position: -192px -120px
}

.fg-m10 .form-group {
    margin-bottom: 10px;
}
</style>
<div class="row height-e">
    <g:render template="/validatedSignal/includes/signal_history_panel" model="[timezone: timezone]"/>


    <g:render template="/includes/widgets/actions/action_types"/>
    <div class="col-sm-12">
        <div class="rxmain-container ">
            <div class="rxmain-container-inner">
                <div class="rxmain-container-row rxmain-container-header">
                    <label class="rxmain-container-header-label">
                        ${message(code: 'app.label.actions')}
                    </label>
                    <span class="pv-head-config configureFields">
                        <a href="javascript:void(0);" class="ic-sm action-search-btn" title="Search">
                            <i class="md md-search" aria-hidden="true"></i>
                        </a>
                        <a href="#" class="pull-right ic-sm action-create ${buttonClass}" title="Add Action">
                            <i class="md md-add" aria-hidden="true"></i>
                        </a>

                    </span>

                </div>

                <div class="rxmain-container-content">
                    <g:render template="/includes/widgets/action_list_panel"
                              model="[alertInst: signal, appType: 'Signal Management']"/>
                </div>
            </div>
        </div>
    </div>
</div>

<g:render template="/includes/modals/actionCreateModal"
          model="[appType: 'Signal Management', alertId: signal.id, actionConfigList: actionConfigList, isArchived: false]"/>
<g:render template="/includes/modals/action_edit_validated_signal"
          model="[appType: 'Signal Management', alertId: signal.id, name: signal.name, productName : signal.getProductNameList().join(','),
                  eventName : signal.events ? ViewHelper.getDictionaryValues(signal.events, DictionaryTypeEnum.EVENT) : signal.getGroupNameFieldFromJson(signal.eventGroupSelection)]"/>
<g:render template="/includes/widgets/meeting_minutes" model="[alertInst: signal, appType: 'Signal Management']"/>
<g:render template="/includes/modals/meetingCreateModal"
          model="[appType: 'Signal Management', alertId: signal.id, meetingInstance: signal.meetings, userList:userList]"/>
<g:render template="/includes/modals/meetingEditModal"
          model="[appType: 'Signal Management', alertId: signal.id, meetingInstance: signal.meetings]"/>
<g:render template="/includes/modals/meetingMinutesModal"
          model="[appType: 'Signal Management', alertId: signal.id, meetingInstance: signal.meetings]"/>

<g:render template="includes/activities"/>








