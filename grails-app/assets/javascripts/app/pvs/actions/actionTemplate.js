$(function () {
    $('.select2').select2();
    var actionTemplateTable;

    var actionTemplateLabels = {
        actionConfig: 'Action',
        actionType: 'Action Type',
        assignedTo: 'Assigned To',
        dueIn: 'Due In(Days)',
        details: 'Action Details',
        comments: 'Comments'
    };

    $(document).on('click', "#addAction", function (event) {
        var actionTemplateModal = $(".action-template-modal");
        actionTemplateModal.modal("show");
        $("#assignedTo").find("option[isGuest='true']").remove();
        populateAssignedTo('#assignedTo')
        actionTemplateModal.find('#due-date-picker').datepicker();
        actionTemplateModal.find('#completion-date-picker').datepicker({restricted: [{from: TOMORROW , to: Infinity}]}).on('inputParsingFailed.fu.datepicker',function (e) {
            $('#completedDate').val('');
        });
        actionTemplateModal.find("#save-action-template").text("Save");
        actionTemplateModal.find("#save-action-template").off().on('click', function (evt) {
            addOrEditActionTemplate(saveActionTemplateUrl, actionTemplateModal, "");
        });
    });

    var initActionTemplateTable = function () {

        var columns = create_action_table_columns();
        actionTemplateTable = $('#actionTemplateListTable').DataTable({

            responsive: true,
            language: {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json"
            },
            "ajax": {
                "url": actionTemplateUrl,
                "dataSrc": ""
            },
            "initComplete": function () {
                if (isAdmin) {
                    var buttonHtml = '<button type="button" class="btn btn-primary" id="addAction" data-toggle="modal">Add Action Template</button>&nbsp;&nbsp;&nbsp;';
                    var $divToolbar = $('#actionTemplateListTable_filter');
                    $divToolbar.prepend(buttonHtml);
                }
                $(document).on("click", '.deleteActionTemplate', function (event) {
                    event.preventDefault();
                    var url = $(this).attr('href');
                    bootbox.confirm({
                        message: $.i18n._('deleteThis'),
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
                            if (result) {
                                $.ajax({
                                    type: "GET",
                                    url: url,
                                    dataType: 'json'
                                })
                                    .done(function (data) {
                                        if (data.status) {
                                            actionTemplateTable.ajax.reload()
                                            showSuccessMsg(data.message)
                                        } else {
                                            showErrorMsg(data.message);
                                        }
                                });
                            }
                        }
                    });
                });
                $(document).on('click', '.editActionTemplate', function (event) {
                        event.preventDefault();
                        var url = $(this).attr('href');
                        $.ajax({
                            type: "GET",
                            url: url,
                            dataType: 'json'
                        }).done(editJustificationSuccessCallback);
                });

                // added to fix bug/PVS-56036
                $(document).on('contextmenu', '.editActionTemplate', function (event) {
                        event.preventDefault();
                        var url = $(this).attr('href');
                        $.ajax({
                             type: "GET",
                             url: url,
                             dataType: 'json',
                        }).done(editJustificationSuccessCallback);
                });
                addGridShortcuts('#actionTemplateListTable');
            },
            "oLanguage": {
                "oPaginate": {
                    "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                    "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                    "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                    "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
                },
            },
            "aaSorting": [[3, "desc"]],
            "bLengthChange": true,
            "iDisplayLength": 10,
            "aLengthMenu": [[10, 50, 100, 200, -1], [10, 50, 100, 200, "All"]],
            columnDefs: [{
                "targets": '_all',
                "render":$.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }],
            "scrollY":"calc(100vh - 256px)",
            "aoColumns": columns
        });
        actionButton("#actionTemplateListTable");
    };

    var create_action_table_columns = function () {
        var aoColumns = [
            {
                "mData": "name",
                "className": "cell-break word-break pvi-col-md"
            },
            {
                "mData": 'description',
                "className": "cell-break word-break textPre pvi-col-md"
            },
            {
                "mData": "",
                "mRender": function (data, type, row) {
                    var actionPropertiesObj = JSON.parse(row.actionProperties);
                    if(actionPropertiesObj['assignedTo'] == "null")
                        actionPropertiesObj['assignedTo'] = row.guestAttendeeEmail
                    var display = "";
                    for (var key in actionPropertiesObj) {
                        if (key.toLowerCase().indexOf("id") < 0)
                            display = display + "<span><b>" + actionTemplateLabels[key] + "</b></span>&nbsp<span>:&nbsp" + escapeHTML(actionPropertiesObj[key]) + "</span></br>"
                    }
                    return display;
                },
                "className": "cell-break word-break pvi-col-lg"
            },

            {
                "mData": 'lastUpdated',
                "className": "cell-break word-break pvi-col-md"
            },
            {
                "mData": 'modifiedBy',
                "className": "cell-break word-break pvi-col-md"
            }
        ];

        if (isAdmin) {
            aoColumns.push.apply(aoColumns, [{
                "mRender": function (data, type, row) {
                    var deleteUrl = deleteActionTemplateUrl + "/" + row.id;
                    var editActionUrl = editActionTemplateUrl + "/" + row.id;
                    data = row;
                    var actionButton = '<div class="hidden-btn btn-group dropdown dataTableHideCellContent" align="center"> \
                            <a class="btn btn-success btn-xs editActionTemplate" href="' + editActionUrl + '" >' + "Edit" + '</a> \
                            <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                                <span class="caret"></span> \
                                <span class="sr-only">Toggle Dropdown</span> \
                            </button> \
                            <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                                <li role="presentation"><a class="deleteActionTemplate" data-name="' + row.name + '"  data-version="' + row.version + '" data-id="' + row.id + '" ' +
                        'role="menuitem" href="' + deleteUrl + '" >' + 'Delete' + '</a></li>\
                             </ul></div>';
                    return actionButton;
                }

            }]);
        }
        return aoColumns;
    };
    initActionTemplateTable();

    var editJustificationSuccessCallback = function (data) {
        if (data.status) {
            var actionTemplateModal = $(".action-template-modal");
            populateFormData(data.data, actionTemplateModal);
        }
    };

    var addOrEditActionTemplate = function (url, actionTemplateModal, actionTemplateId) {
        $.ajax({
            type: "POST",
            url: url,
            data: {
                "id": actionTemplateId,
                "name": actionTemplateModal.find("#name").val(), // PVS-58913:No need to escape name and description for special character
                "description": actionTemplateModal.find("#description").val(),
                "actionConfig": escapeHTML(actionTemplateModal.find("#config").val()),
                "actionType": escapeHTML(actionTemplateModal.find("#type").val()),
                "assignedTo": actionTemplateModal.find("#assignedTo").val(),
                "dueIn": actionTemplateModal.find("#dueIn").val(),
                "details": escapeHTML(actionTemplateModal.find("#details").val().replaceAll("\n", "\\n")),
                "comments": escapeHTML(actionTemplateModal.find("#comments").val().replaceAll("\n", "\\n"))
            }
        })
            .done(function (data) {
                if (data.status) {
                    actionTemplateModal.modal("hide");
                    actionTemplateTable.ajax.reload();
                    showSuccessMsg(data.message)
                } else {
                    var $alert = $(".msgContainer").find('.alert');
                    $alert.find('.message').text(data.message);
                    $(".msgContainer").show();

                    $alert.alert();
                    $alert.fadeTo(5000, 500).slideUp(500, function () {
                        $alert.slideUp(500);
                    });
                }
        })
    };

    function populateFormData(data, actionTemplateModal) {
        actionTemplateModal.modal("show");
        $("#assignedTo").find("option[isGuest='true']").remove();
        populateAssignedTo('#assignedTo');
        actionTemplateModal.find("#name").val(unescapeHTML(data.name));
        actionTemplateModal.find("#description").val(unescapeHTML(data.description));
        var actionPropertiesObj = JSON.parse(data.actionProperties);
        if (actionPropertiesObj['assignedTo'] == "null") {
            actionPropertiesObj['assignedTo'] = data.guestAttendeeEmail
            actionPropertiesObj['assignedToId'] = data.guestAttendeeEmail
        }
        if (data.guestAttendeeEmail)
            actionTemplateModal.find("#assignedTo").append("<option value='" + data.guestAttendeeEmail + "' isGuest='true'>" + data.guestAttendeeEmail + "</option>");
        actionTemplateModal.find("#config").val(unescapeHTML(actionPropertiesObj["actionConfigId"]));
        actionTemplateModal.find("#type").val(unescapeHTML(actionPropertiesObj["actionTypeId"]));
        actionTemplateModal.find('#assignedTo').val(actionPropertiesObj['assignedToId']).trigger('change');
        actionTemplateModal.find("#dueIn").val(actionPropertiesObj["dueIn"]);
        actionTemplateModal.find("#details").val(unescapeHTML(actionPropertiesObj["details"]));
        actionTemplateModal.find("#comments").val(unescapeHTML(actionPropertiesObj["comments"]));
        actionTemplateModal.find("#save-action-template").text("Update")
        actionTemplateModal.find("#save-action-template").off().on('click', function (evt) {
            addOrEditActionTemplate(updateActionTemplateUrl, actionTemplateModal, data.id)
        });
    }

    $('.action-template-modal').on('hidden.bs.modal', function () {
        $('.actionType').find('#name, #description, #type, #config, #dueIn, #assignedTo, #details, #comments').val('')
    });

    function getErrorMessageHtml(msg) {
        var alertHtml = '<div class="alert alert-danger alert-dismissible" role="alert"> ' +
            '<button type="button" class="close" data-dismiss="alert"> ' +
            '<span aria-hidden="true">&times;</span> ' +
            '<span class="sr-only"><g:message code="default.button.close.label" /></span> ' +
            '</button> ' + msg;
        '</div>';
        return alertHtml;
    }

    function getSuccessMessageHtml(msg) {
        var alertHtml = '<div class="alert alert-success alert-dismissible" role="alert"> ' +
            '<button type="button" class="close" data-dismiss="alert"> ' +
            '<span aria-hidden="true">&times;</span> ' +
            '<span class="sr-only"><g:message code="default.button.close.label" /></span> ' +
            '</button> ' + msg;
        '</div>';
        return alertHtml;
    }

    function showErrorMsg(msg) {
        removeExistingMessageHolder();
        var alertHtml = getErrorMessageHtml(msg);
        $('body .messageContainer').prepend(alertHtml);
        $('body').scrollTop(0);
    }

    function showSuccessMsg(msg) {
        removeExistingMessageHolder();
        var alertHtml = getSuccessMessageHtml(msg);
        $('body .messageContainer').prepend(alertHtml);
        $('body').scrollTop(0);
    }

    function removeExistingMessageHolder() {
        $('.messageContainer').html("");
    }
});

function populateAssignedTo(id) {
    var $tag = $(id);
    $tag.select2({
        tags: true,
        multiple: false,
        placeholder: 'Select Assigned To',
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
