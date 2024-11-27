<%@ page import="com.rxlogix.Constants" %>
<g:javascript>
        var actionItemUrl = "${createLink(controller: "action", action: "listByCurrentUser")}";
        var statusValue = "${Constants.Commons.ALL}";
        var changeAssignedToUrl= "${createLink(controller: "topic", action: 'changeAssignedTo',id: topic.id)}";
        var changeGroupUrl = "${createLink(controller: "topic", action: 'changeGroup')}"
        var appType = "Topic"
        var alertId = "${topic.id}"
</g:javascript>
<asset:javascript src="app/pvs/alertComments/alertComments.js"/>
<asset:javascript src="app/pvs/meeting/meeting.js"/>
<asset:javascript src="app/pvs/alerts_review/alert_review.js"/>
<asset:javascript src="app/pvs/validated_signal/actionManagement.js"/>
<asset:javascript src="app/pvs/date-time/bootstrap-datetimepicker.js"/>
<asset:stylesheet src="app/pvs/bootstrap-datetimepicker.css" />

<script>

    var applicationName = "Topic";

    $(document).ready(function() {

        $("#groups").select2({
            placeholder: "Select groups",
            allowClear: true,
            width: "100%"
        });

        $("#assignedToUser").click(function() {
            $("#assignedToUserDiv").removeClass("hide");
            $("#assignedToGroupDiv").addClass("hide");
        });

        $("#assignedToGroup").click(function() {
            $("#assignedToUserDiv").addClass("hide");
            $("#assignedToGroupDiv").removeClass("hide");
        });

        var assignmentType = $("#assignmentType").val();

        if (assignmentType == "USER") {
            $("#assignedToUserDiv").removeClass("hide");
            $("#assignedToGroupDiv").addClass("hide");
        } else {
            $("#assignedToUserDiv").addClass("hide");
            $("#assignedToGroupDiv").removeClass("hide");
        }

        var partnerTable = null;

        var init_table = function () {

            var topicIdPartner = $("#topicIdPartner").val();

            if (typeof partnerTable != "undefined" && partnerTable != null) {
                partnerTable.destroy()
            }

            //Data table for the document modal window.
            partnerTable = $("#partnerTable").DataTable({
                "language": {
                    "url": "../assets/i18n/dataTables_" + userLocale + ".json"
                },
                fnInitComplete: function () {
                    $('[data-toggle="tooltip"]').tooltip()
                },
                "ajax": {
                    "url": 'partnersList?topicId='+topicIdPartner,
                    "dataSrc": ""
                },
                searching: true,
                "bLengthChange": true,
                "iDisplayLength": 5,
                "aaSorting": [[1,"desc"]],
                "aLengthMenu": [[10, 20, 50, -1], [10, 20, 50, "All"]],
                "aoColumns": [
                    {
                        "mData": "",
                        "mRender": function (data, type, row) {
                            return'<input type="checkbox" class="editor-active copy-select">';
                        },
                    },
                    {
                        "mData" : "productName",
                        "className" : "dt-center"
                    },
                    {
                        "mData" : "partnerName",
                        "className" : "dt-center"
                    },
                    {
                        "mData" : "partnerContact",
                        "className" : "dt-center"
                    },
                    {
                        "mData" : "partnerEmailContact",
                        "className" : "dt-center"
                    },
                    {
                        "mData" : "contactingEntity",
                        "className" : "dt-center"
                    }
                ]

            });
        }

        $(".sharedWithPartners").click(function() {
            var partnerModal = $("#partnerModal");
            partnerModal.modal("show");
            init_table();
        })

        var assignmentType = $("#assignmentType").val();

        if (assignmentType == "USER") {
            $("#assignedToUserDiv").removeClass("hide");
            $("#assignedToGroupDiv").addClass("hide");
        } else {
            $("#assignedToUserDiv").addClass("hide");
            $("#assignedToGroupDiv").removeClass("hide");
        }
    })
</script>

<style>
    a {
        color: #428bca;
    }
</style>

<div class="row">
    <div class="col-sm-6">
        <div class="rxmain-container ">
            <div class="rxmain-container-inner">
                <div class="rxmain-container-row rxmain-container-header">
                    <label class="rxmain-container-header-label">
                        <span>Workflow Management</span>&nbsp;<i class="fa fa-info-circle showWorkflowHistory"
                                                                 aria-hidden="true"></i>
                    </label>
                </div>
                <div class="rxmain-container-content">
                    <div class="row">
                        <div class="col-xs-6">
                            <label>Topic Name:</label>&nbsp;<span>${topic.name}</span>
                        </div>

                    </div>

                    <div class="row">
                        <div class="col-xs-6">
                            <label>Workflow State:</label>&nbsp;
                            <span class="changeWorkflow workflowBox">
                                <a data-id='${topic.id}' class="edit-state change-workflow-state" data-info="row" href="#" data-apptype="Topic" data-field="workflowState"  data-signal-id=${topic.id} data-workflow=${topic.workflowState.displayName}>${topic.workflowState.displayName}</a>
                            </span>
                        </div>
                        <div class="col-xs-6">
                            <label>Disposition:</label>&nbsp;<span class="disposition">${topic.disposition.displayName}</span>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-xs-6">
                            <div id="reviewDateDiv1"><label>Current State Due Date: </label>&nbsp;<div style="display:inline-block" id="reviewDate1"><g:formatDate type="date" style="MEDIUM" date="${topic.endDate}"/></div></div>
                            <span></span>
                        </div>
                        <div class="col-xs-6">
                            <label>Priority:</label>&nbsp;
                            <span class="changePriority">
                                 <a href="#" data-field="priority" data-id='${topic.id}' data-info="row" class="change-priority">${topic.priority.displayName}</a>
                            </span>
                        </div>
                    </div>

                </div>
            </div>
        </div>

    </div>

    <div class="col-sm-6">
        <div class="rxmain-container ">
            <div class="rxmain-container-inner">
                <div class="rxmain-container-row rxmain-container-header">
                    <label class="rxmain-container-header-label">
                        Sharing and Assignments
                    </label>
                </div>
                <div class="rxmain-container-content">

                    <div class="row" style="height:75px">
                        <div class="col-xs-6">
                            <div id="assignmentDiv" style="margin-left: -6px;">
                                <label>&nbsp;
                                <g:radio id="assignedToUser" name="assignement"
                                         value="USER" checked="${topic?.assignmentType == 'USER' ? true: false}"/>User Assignment</label>
                                <label>&nbsp;
                            </div>
                            <br/>
                            <div id="assignedToUserDiv">
                                <a href='#' class='edit-user change-assignment'>
                                    <span data-field='assignedTo' data-id='${topic.id}' data-current-user-id='${topic.assignedTo?.id}'
                                          data-info='row' data-current-user='${topic.assignedTo?.fullName}'>${topic.assignedTo?.fullName}</span>
                                </a>
                            </div>
                            <div id="assignedToGroupDiv" class="col-xs-6 hide">
                                <a href='#' class='edit-user-assignment-group'>
                                    <span data-field='assignedTo' data-id='${topic.id}'
                                          data-current-group='${(topic.sharedGroups?.collect {it.name}).join(',')}'>
                                        ${(topic.sharedGroups?.collect {it.name}).join(',') ?: 'Add Groups'}
                                    </span>
                                </a>
                            </div>

                            <input type="hidden" id="assignmentType" value="${topic.assignmentType}" />

                        </div>

                    </div>
                </div>
            </div>
            <input type="hidden" id="alertId" value="${topic?.id}"/>
            <input type="hidden" id="assignedToName" value="${topic?.assignedTo?.fullName}"/>
        </div>

    </div>

    <div class="col-sm-12">
        <div class="rxmain-container ">
            <div class="rxmain-container-inner">
                <div class="rxmain-container-row rxmain-container-header">
                    <label class="rxmain-container-header-label">
                        ${message(code: 'app.label.attachments')}
                    </label>
                </div>
                <div class="rxmain-container-content" >
                    <g:render template="/includes/widgets/attachment_panel_signal_management" model="[alertInst: topic, source: 'detail', controller:'topic']"/>
                    <g:render template="/includes/modals/attachments_modals_dialog_signal_management" model="[alertInst: topic, source: 'detail', controller:'topic']"/>
            </div>
        </div>
    </div>

</div>

    <g:render template="/includes/widgets/actions/action_types"/>
    <div class="col-sm-12">
        <div class="rxmain-container ">
            <div class="rxmain-container-inner">
                <div class="rxmain-container-row rxmain-container-header">
                    <label class="rxmain-container-header-label">
                        ${message(code: 'app.label.actions')}
                    </label>
                </div>

                <div class="rxmain-container-content">
                    <g:render template="/includes/widgets/action_list_panel"
                              model="[alertInst: topic, appType: 'Topic']"/>
                </div>
            </div>
        </div>
    </div>
</div>


<g:render template="/includes/modals/actionCreateModal" model="[appType: 'Topic',alertId :topic.id,isTopicFlow:true, actionConfigList: actionConfigList, isArchived: false]" />
<g:render template="/includes/modals/action_edit_validated_signal" model="[appType: 'Topic',alertId :topic.id]"/>
<g:render template="/includes/widgets/meeting_minutes" model="[alertInst: topic,appType: 'Topic']" />
<g:render template="/includes/modals/meetingCreateModal" model="[appType: 'Topic',alertId :topic.id, meetingInstance : topic.meetings]" />
<g:render template="/includes/modals/meetingEditModal" model="[appType: 'Topic',alertId :topic.id, meetingInstance : topic.meetings]" />
<g:render template="/includes/modals/meetingMinutesModal" model="[appType: 'Topic',alertId :topic.id, meetingInstance : topic.meetings]" />