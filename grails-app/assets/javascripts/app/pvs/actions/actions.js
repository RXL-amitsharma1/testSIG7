var signal = signal || {};

signal.actions_utils = (function() {
    var action_list_table;
    var current_create_bt;
    var adhoc_validated_table;

    var set_action_create_modal = function () {
        $('#createActionModal').on('hidden.bs.modal', function (e) {
            $(this)
                .find('#config').val('').end()
                .find('#type').val('').end()
                .find('#meetingElement').val('').end()
                .find('.assignedToValue').val('').trigger('change').end()
                .find('.assignedToValue').empty().end()
                .find("input[type=text],textarea").val('')
                .end();

            $(this).find('select[name="actionStatus"]').val("New");

            $('#createActionModal .select2-selection__rendered').hover(function () {
                $(this).prop('title', '');
            });

        });

        $('#dueDate').on("focusout", function(){
            $(this).val(newSetDefaultDisplayDateFormat($(this).val()));
            if($(this).val()=='Invalid date'){
                $(this).val('')
            }
        });
        $('#completedDate').on("focusout", function(){
            $(this).val(newSetDefaultDisplayDateFormat( $(this).val()));
            if($(this).val()=='Invalid date'){
                $(this).val('')
            }
        });
        $('#action-edit-modal').on('hidden.bs.modal', function (e) {
            $('#activity_list').DataTable().ajax.reload();
            clear_edit_errors();
            $(this)
                .find("input[type=text],textarea,select")
                .val('')
                .end()
        });

        $('#editActionModal').on('hidden.bs.modal', function (e) {
            clear_edit_signal();
            $(this)
                .find("input[type=text],textarea,select")
                .val('')
                .end()
        });

        $(document).on('click', '.action-create', function (evt) {
            var srcEle = $(evt.target);
            current_create_bt = srcEle;
            var alertId = alertId;
            if (typeof (srcEle.attr('alert-id')) !== 'undefined') {
                alertId = srcEle.attr('alert-id')
            }
            if (typeof alertId == 'undefined') {
                alertId = srcEle.parent().attr('alert-id')
            }
            var createActionModal = $('#createActionModal');
            var createActionButton = $("#create-action-btn");
            createActionModal.find('.id-element').attr('data-id', alertId);

            //Set the alertId in the hidden field of the modal.
            if (typeof alertId != "undefined" && alertId != '') {
                createActionModal.find(".alertId").val(alertId);
            }
            createActionModal.find('#due-date-picker').datepicker();
            createActionModal.find('#completion-date-picker').datepicker({restricted: [{from: TOMORROW , to: Infinity}]}).on('inputParsingFailed.fu.datepicker',function (e) {
                $('#completedDate').val('');
            });
            createActionButton.attr("disabled", false);
            clear_errors();
            $('.action-type-list').removeClass('hidden');
            $('.meeting-list').addClass('hidden');
            createActionModal.modal({});
            createActionModal.find('.action-type-list  select').val('').trigger('change');
            createActionModal.find('.action-value').val('').trigger('change');

            //Event bind when the save/create button is click
            // ked in the action event modal.
            createActionButton.off('click').on('click', function (event) {
                createActionButton.attr("disabled", true);
                var alertId = $(event.target).attr('data-id');
                var actionModalId = $("#createActionModal #tempForm");

                $.ajax({
                    type: "POST",
                    url: '/signal/action/save',
                    data: getActionDetails(actionModalId),
                    async: false,
                    cache: false
                }).done(function (response) {
                    if (response.status == false) {
                        clear_errors();
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
                        errorMsg += "</div>";
                        $('#createActionModal .modal-body').prepend(errorMsg);
                        createActionButton.attr("disabled", false);
                    } else {
                        $.Notification.notify('success', 'top right', "Success", "Action created.", {autoHideDelay: 5000});
                        $('#activity_list').DataTable().ajax.reload();
                        createActionModal.modal('hide');
                        current_create_bt.parentsUntil("td").find('.default-value').html(response.actionCount);

                        if (typeof applicationLabel != 'undefined' && applicationLabel === "Case Detail") {
                            $('#action-table').DataTable().ajax.reload();
                        } else {
                            if (typeof alertId != "undefined" && alertId != '') {
                                if (typeof detail_table != "undefined") {
                                    detail_table.ajax.reload();
                                }
                            } else {
                                $('#action-table').DataTable().ajax.reload();
                                $('#meeting-table').DataTable().ajax.reload();
                                $('#signalActivityTable').DataTable().ajax.reload();
                            }
                        }

                        //Refresh the activity table
                        if (typeof applicationLabel != 'undefined' && applicationLabel !== APPLICATION_LABEL.CASE_DETAIL && applicationLabel !== APPLICATION_LABEL.EVENT_DETAIL) {
                            signal.activities_utils.reload_activity_table();
                            if (applicationLabel == APPLICATION_LABEL.SIGNAL_MANAGEMENT) {
                                signal.meeting_utils.reload_meeting_table();
                            }
                        }
                    }
                }).fail(function (data) {
                    clear_errors();
                    var errorMsg = '<div class="alert alert-danger alert-dismissible" role="alert"> ' +
                        '<button type="button" class="close" data-dismiss="alert"> ' +
                        '<span aria-hidden="true">&times;</span> ' +
                        '<span class="sr-only"><g:message code="default.button.close.label" /></span> ' +
                        '</button> ' ;
                    errorMsg += "Error occured while saving Action.";
                    errorMsg += "</div>";
                    $('#createActionModal .modal-body').prepend(errorMsg);
                    createActionButton.attr("disabled", false);
                })
            })
        });

        $(document).on("click", ".action-search-btn", function (e) {
            e.preventDefault();
            $("#action-table_filter").toggle();
        });

        $(document).on("click", ".meeting-search-btn", function (e) {
            e.preventDefault();
            $("#meeting-table_filter").toggle();
        });

        $(document).on("click", ".activity-search-btn", function (e) {
            e.preventDefault();
            $("#signalActivityTable_filter").toggle();
        });
    };

    var getActionDetails = function(modalId) {
        return {
            "config": modalId.find("#config").val() || modalId.find(".action-value.select2").val(),
            "type": modalId.find("#type").val() || modalId.find(".typeId.select2").val(),
            "assignedToValue": modalId.find("#assignedTo").val() || modalId.find(".assignedToValue").val(),
            "dueDate": modalId.find("#dueDate").val(),
            "completedDate": modalId.find("#completedDate").val(),
            "details": modalId.find("#details").val(),
            "comments": modalId.find("#comments").val(),
            "alertId": modalId.find("#alertId").val(),
            "appType": modalId.find("#appType").val(),
            "exeConfigId": modalId.find("#exeConfigId").val(),
            "meetingId": modalId.find("#meetingElement").val(),
            "actionStatus": modalId.find("#actionStatus").val(),
            "actionId": modalId.find("#actionId").val(),
            "isArchived": isArchived,
            "datasource": modalId.find("#datasource").val()
        }
    };

    var clear_errors = function() {
        $('#createActionModal .modal-body .alert').remove()
    };

    var clear_edit_errors = function () {
        $('#action-edit-modal .modal-body .alert').remove()
    };
    var clear_edit_signal = function () {
        $('#editActionModal .modal-body .alert').remove()
    };

//Single and Aggregate Action list data
    var set_action_list_modal = function(comp) {
        $(document).on('click', '.list-action', function(evt) {
            evt.preventDefault();
            comp.modal('toggle', $(this));
        });
        comp.on('shown.bs.modal', function(evt) {
            var srcEle = evt.relatedTarget;
            var alertId = $(srcEle).attr('data-id');
            var appType = $("#createActionModal #tempForm").find("#appType").val();
            var actionUrl = "/signal/action/listByAlert?alertId=" + alertId +"&appType=" + appType+ "&isArchived=" + isArchived;
            if (appType === ALERT_CONFIG_TYPE.LITERATURE_SEARCH_ALERT) {
                actionUrl += "&datasource=" + selectedDatasource;
            }
            action_list_table = $('#action-table').DataTable({
                destroy : true,
                "language": {
                    "url": "../assets/i18n/dataTables_" + userLocale + ".json",
                },
                drawCallback: function () {
                    $('.edit-action-link').on('click', function(evt) {
                        var url = "/signal/action/getById?id=" + $(this).attr('data-id');
                        $.ajax({
                            url: url,
                            async: false,
                            cache: false
                        }).done(function(data) {
                            data.action_types = action_select_values.types;
                            data.action_configs = action_select_values.configs;
                            data.all_status = action_select_values.allStatus;
                            data.alertId = alertId;
                            data.actionStatus = {name: data.actionStatus, value: "New"};
                            if(applicationLabel!=undefined && applicationLabel!="Signal Management"){
                                if(data.action_configs!=undefined){
                                    var newArray=[];
                                    var j=0;
                                    for (var i=0;i<data.action_configs.length;i++){
                                        if(data.action_configs[i].value!="Meeting"){
                                            newArray[j]=data.action_configs[i];
                                            j++;
                                        }
                                    }
                                    data.action_configs=newArray;
                                }
                            }
                            var html_content = signal.utils.render('action_editor_6.1_6_v1', data);
                            $('#action-editor-container').html(html_content);

                            $('#action-editor-container #due-date-picker').datepicker({
                                date: data.dueDate,
                                allowPastDates: true,
                                momentConfig: {
                                    culture: userLocale,
                                    tz: userTimeZone,
                                    format: DEFAULT_DATE_DISPLAY_FORMAT
                                }
                            }).on("keydown", preventEnterKeySubmit);

                            $('#action-editor-container #completion-date-picker').datepicker({
                                date: data.completedDate,
                                allowPastDates: true,
                                restricted: [{from: TOMORROW , to: Infinity}],
                                momentConfig: {
                                    culture: userLocale,
                                    tz: userTimeZone,
                                    format: DEFAULT_DATE_DISPLAY_FORMAT
                                }
                            }).on("keydown", preventEnterKeySubmit).on('inputParsingFailed.fu.datepicker',function (e) {
                                $('#completedDate').val('');
                            });

                            $('#action-editor-container #actionStatus').on("change", function () {
                                if ($(this).val() === 'Closed') {
                                    var completedDate = moment.utc(new Date()).tz(serverTimeZone).format("DD-MMM-YYYY");
                                    $('#action-editor-container #completion-date-picker').find('#completedDate').val(completedDate);
                                }
                            });


                            var dueDate = moment(data.dueDate, "DD-MMM-YYYY").format(DEFAULT_DATE_FORMAT);
                            $('#action-editor-container').find('#due-date-picker').find('#dueDate').val(dueDate);
                            if(data.completedDate) {
                                var completedDate = moment(data.completedDate, "DD-MMM-YYYY").format(DEFAULT_DATE_FORMAT);
                                $('#action-editor-container').find('#completion-date-picker').find('#completedDate').val(completedDate);
                            } else {
                                $('#action-editor-container').find('#completion-date-picker').find('#completedDate').val('');
                            }
                            $('#action-editor-container').find('#due-date-picker').find('#dueDate').on("focusout", function(){
                                $(this).val(newSetDefaultDisplayDateFormat($(this).val()));
                                if($(this).val()=='Invalid date'){
                                    $(this).val('')
                                }
                            });
                            $('#action-editor-container').find('#completion-date-picker').find('#completedDate').on("focusout", function(){
                                $(this).val(newSetDefaultDisplayDateFormat($(this).val()));
                                if($(this).val()=='Invalid date'){
                                    $(this).val('')
                                }
                            });
                            $('#action-editor-container').find('#appType').val(appType);
                            var action_editor = $('#action-editor-container');
                            var editActionModal = $('#action-edit-modal');
                            editActionModal.find('.id-element').attr('data-id', alertId);
                            editActionModal.find('.assignedToValue').val(null).trigger('change');
                            $('#action-modal').modal('hide');
                            editActionModal.modal({});

                            $('#action-edit-modal #cancel-bt').on("click", handle_action_editor_cancel);
                            $('#action-edit-modal #update-bt').on("click", handle_action_editor_update)
                        })
                    })
                },
                initComplete: function(oSettings, json) {
                    $('.action-create').attr('data-id', alertId);
                    signal.actions_utils.set_action_create_modal();
                    $('#due-date-picker').datepicker()
                    $('#completion-date-picker').datepicker({restricted: [{from: TOMORROW , to: Infinity}]}).on('inputParsingFailed.fu.datepicker',function (e) {
                        $('#completedDate').val('');
                    });
                },
                "ajax": {
                    "url": actionUrl,
                    cache:false,
                    "dataSrc": ""
                },
                layout: {
                    topStart: ['pageLength', 'search'],
                    topEnd: null
                },
                customProcessing: true,
                "oLanguage": {
                    "oPaginate": {
                        "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                        "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                        "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                        "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
                    },
                },
                "searching": true,
                "bProcessing": true,
                "bLengthChange": true,
                "iDisplayLength": 5,
                "aLengthMenu": [[5, 10, 20, -1], [5, 10, 20, "All"]],
                "aoColumns": [
                    {
                        "mData": "id",
                        "className":"col-min-50",
                        "mRender": function (data, type, row) {
                            return "<a href='#' class='edit-action-link' data-id='" +
                                row.id + "'>" + row.id + "<a>"
                        }
                    },
                    {
                        "mData": "type",
                        "className":"col-min-50",
                        "mRender": function (data,type,row){
                            return encodeToHTML(data)
                        }
                    },
                    {
                        "mData": "config",
                        "className":"col-min-50 cell-break",
                        "mRender": function (data,type,row){
                            return encodeToHTML(data)
                        }
                    },
                    {
                        "mData": "details",
                        "className":"col-min-100 col-max-130 cell-break",
                        "mRender": function (data,type,row){
                            return encodeToHTML(data)
                        }
                    },
                    {
                        "mData": "dueDate",
                        "className":"col-min-100"
                    },
                    {
                        "mData": "assignedTo",
                        "className":"col-min-100 cell-break",
                        "mRender": function (data,type,row){
                            return encodeToHTML(data)
                        }
                    },
                    {
                        "mData" : "actionStatus",
                        "className":"col-min-100 cell-break",
                        "mRender": function (data,type,row){
                            return encodeToHTML(data)
                        }
                    },
                    {
                        "mData": "completedDate",
                        "className":"col-min-120",
                        mRender: function(data, type,row){
                            return row.completedDate
                        }
                    }
                ],
                scrollX: true,
                columnDefs: [{
                    "targets": '_all',
                    "render": $.fn.dataTable.render.text(),
                    orderSequence: ['desc', 'asc']
                }]
            })
        });

        comp.on('hidden.bs.modal', function(evt) {
          comp.find("#action-table").empty();
         });
        return action_list_table
    };
    var setEditModalValues = function (editActionModal, data) {
        var actionType = data.typeObj.id;
        var meetingId = data.meetingId;
        var option = new Option(data.assignedToObj.fullName, data.assignedToUser, true, true);
        editActionModal.find("#assignedTo").append(option).trigger('change');
        if(!meetingId) {
            editActionModal.find("#type").val(actionType).trigger('change');
        } else {
            editActionModal.find("#meetingElement").val(meetingId);
            editActionModal.find("#meetingValue").val(meetingId);
        }
        editActionModal.find(".status-icon").removeClass('hidden');
        editActionModal.find("#config").val(data.configObj.id).trigger('change');
        editActionModal.find("#actionId").val(data.id);
        var dueDate = moment(data.dueDate, DEFAULT_DATE_FORMAT).format(DEFAULT_DATE_FORMAT);
        editActionModal.find("#due-date-picker").datepicker({
            date: $("#due-date-picker").val() ? new Date($("#due-date-picker").val()) : null,
            allowPastDates: true,
            momentConfig: {
                culture: userLocale,
                format: DEFAULT_DATE_DISPLAY_FORMAT
            }
        }).on("keypress", preventEnterKeySubmit);
        editActionModal.find("#dueDate").val(dueDate);
        editActionModal.find("#comments").val(data.comments);
        editActionModal.find("#details").val(data.details);
        editActionModal.find("#actionStatus").val(data.actionStatus);
        editActionModal.find('.action-value').trigger('change');
        editActionModal.find("#prodName").text(data.productName);
        editActionModal.find("#eventName").text(data.eventName);
        editActionModal.find("#createdBy").html("<br/><span>Created by "+data.owner.fullName+" on "+data.createdDate+"</span>");

        var completionDate = data.completedDate ? moment(data.completedDate, DEFAULT_DATE_FORMAT).format(DEFAULT_DATE_FORMAT):'';
        editActionModal.find("#completion-date-picker").datepicker({
            date: $("#completion-date-picker").val() ? new Date($("#completion-date-picker").val()) : null,
            allowPastDates: true,
            restricted: [{from: TOMORROW , to: Infinity}],
            momentConfig: {
                culture: userLocale,
                format: DEFAULT_DATE_DISPLAY_FORMAT
            }
        }).on("keypress", preventEnterKeySubmit).on('inputParsingFailed.fu.datepicker',function (e) {
            $('#completedDate').val('');
        });
        editActionModal.find("#completedDate").val(completionDate);

        editActionModal.find('#completedDate').on("focusout", function(){
            $(this).val(newSetDefaultDisplayDateFormat( $(this).val()));
            if($(this).val()=='Invalid date'){
                $(this).val('')
            }
        });

        editActionModal.find('#dueDate').on("focusout", function(){
            $(this).val(newSetDefaultDisplayDateFormat( $(this).val()));
            if($(this).val()=='Invalid date'){
                $(this).val('')
            }
        });

        editActionModal.find("#actionStatus").on("change", function () {
            if ($(this).val() === 'Closed') {
                var completedDate = moment.utc(new Date()).tz(serverTimeZone).format("DD-MMM-YYYY");
                editActionModal.find('#completedDate').val(completedDate);
            }
        });
    };
    var handle_action_editor_cancel = function(evt) {
        evt.preventDefault();
        var action_list_table = $('#action-table').DataTable();
        action_list_table.ajax.reload();
        $('#action-edit-modal').modal('hide')
    };

    var handle_action_editor_update = function(evt) {
        evt.preventDefault();
        if (!evt.handled) {
            evt.handled = true;
            let appType = $("#appType").val();
            var json = $('#action-editor-container form#action-editor-form').serialize() + "&appType=" + appType + "&exeConfigId=" + $("#exeConfigId").val() + "&isArchived=" + isArchived;

            if (appType === ALERT_CONFIG_TYPE.LITERATURE_SEARCH_ALERT) {
                json += "&datasource=" + selectedDatasource;
            }

            $.ajax({
                url: '/signal/action/updateAction',
                method: 'POST',
                data: json,
                async: false,
                cache: false,
            })
                .done(function(response) {
                    if (response.status == false) {
                        clear_edit_errors();
                        var errorMsg = '<div class="alert alert-danger alert-dismissible" role="alert"> ' +
                            '<button type="button" class="close" data-dismiss="alert"> ' +
                            '<span aria-hidden="true">&times;</span> ' +
                            '<span class="sr-only"><g:message code="default.button.close.label" /></span> ' +
                            '</button> ' ;
                        for(var index = 0; index < response.data.length; index++) {
                            var errorMessageObj = response.data[index];
                            errorMsg += errorMessageObj;
                            errorMsg += '</br>';
                        }
                        errorMsg += "</div>";
                        $('#action-edit-modal .modal-body').prepend(errorMsg)
                    } else {
                        var action_list_table = $('#action-table').DataTable()
                        action_list_table.ajax.reload()
                        //Refresh the activity table
                        if(typeof applicationLabel != 'undefined' && applicationLabel !== APPLICATION_LABEL.CASE_DETAIL && applicationLabel !== APPLICATION_LABEL.EVENT_DETAIL) {
                            signal.activities_utils.reload_activity_table();
                        }
                        $('#action-edit-modal').modal('hide')
                    }
            })
        }
    }
    //Ad-hoc alert action list data
    var init_action_table = function (table_id, appType) {
        var dataUrl = "/signal/action/listByAlert?alertId=" + alertId + "&appType=" + appType + "&isArchived=" + isArchived;
        if (appType === "Literature Search Alert") {
            dataUrl += "&datasource=" + selectedDatasource;
        }
        var buttonClassVar = "";
        if(typeof hasReviewerAccess !== "undefined" && !hasReviewerAccess){
            buttonClassVar ="hidden";
        }
        adhoc_validated_table = $(table_id).DataTable({
            //

            layout: {
                bottomStart: [
                    "pageLength",
                    "info",
                    {
                        paging: {
                            type: "full_numbers",
                        },
                    },
                ],
                bottomEnd: null,
            },
            customProcessing: true,
            language: { search: 'Search'},
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
                $('.edit-action-link').on('click', function(evt) {
                    var url = "/signal/action/getById?id=" + $(this).attr('data-id')
                    $.ajax({
                        url: url,
                        async: false,
                        cache: false
                    }).done(function(data) {
                        if(appType == "Signal Management" || appType == "Topic") {
                            var editActionModal = $('#editActionModal');
                            setEditModalValues(editActionModal, data);
                            editActionModal.modal({});
                            editActionModal.find('.update-action').off().on('click', function () {
                                var actionModalId = $("#editActionModal #tempForm");
                                var url;
                                if ($('.meeting-decider').val() == 'true') {
                                    url = '/signal/action/updateMeetingAction';
                                } else {
                                    url = '/signal/action/updateAction';
                                }

                                $.ajax({
                                    url: url,
                                    method: 'POST',
                                    data: getActionDetails(actionModalId),
                                    async: false,
                                    cache: false
                                })
                                    .done(function (response) {
                                        if (response.status == false) {
                                            clear_edit_signal();
                                            var errorMsg = '<div class="alert alert-danger alert-dismissible" role="alert"> ' +
                                                '<button type="button" class="close" data-dismiss="alert"> ' +
                                                '<span aria-hidden="true">&times;</span> ' +
                                                '<span class="sr-only"><g:message code="default.button.close.label" /></span> ' +
                                                '</button> ' ;
                                            for(var index = 0; index < response.data.length; index++) {
                                                var errorMessageObj = response.data[index];
                                                errorMsg += errorMessageObj;
                                                errorMsg += '</br>';
                                            }
                                            errorMsg += "</div>";
                                            $('#editActionModal .modal-body').prepend(errorMsg)
                                        } else {
                                            $.Notification.notify('success', 'top right', "Success", "Action updated.", {autoHideDelay: 5000});
                                            $('#action-table').DataTable().ajax.reload();
                                            $('#meeting-table').DataTable().ajax.reload();

                                            //Refresh the activity table
                                            if(typeof applicationLabel != 'undefined' && applicationLabel !== APPLICATION_LABEL.CASE_DETAIL && applicationLabel !== APPLICATION_LABEL.EVENT_DETAIL) {
                                                signal.activities_utils.reload_activity_table();
                                            }
                                            editActionModal.modal('hide');
                                        }
                                    })
                            });
                        } else {
                            data.action_types = action_select_values.types;
                            data.action_configs = action_select_values.configs;
                            data.all_status = action_select_values.allStatus;
                            data.alertId = alertId;
                            data.actionStatus = {name: data.actionStatus, value: "New"};
                            var html_content = signal.utils.render('action_editor_6.1_6_v1', data);
                            var action_editor = $('#action-editor-container');
                            action_editor.html(html_content);
                            $('#action-editor-container #due-date-picker').datepicker({
                                date: data.dueDate,
                                allowPastDates: true,
                                momentConfig: {
                                    culture: userLocale,
                                    tz: userTimeZone,
                                    format: DEFAULT_DATE_DISPLAY_FORMAT
                                }
                            }).keydown(preventEnterKeySubmit);
                            action_editor.find('#due-date-picker').find('#dueDate').val(data.dueDate);
                            action_editor.find('#due-date-picker').datepicker();
                            $('#action-editor-container #completion-date-picker').datepicker({
                                date: data.completedDate ?data.completedDate :'',
                                allowPastDates: true,
                                restricted: [{from: TOMORROW , to: Infinity}],
                                momentConfig: {
                                    culture: userLocale,
                                    tz: userTimeZone,
                                    format: DEFAULT_DATE_DISPLAY_FORMAT
                                }
                            }).on("keydown", preventEnterKeySubmit).on('inputParsingFailed.fu.datepicker',function (e) {
                                $('#completedDate').val('');
                            });
                            action_editor.find('#completion-date-picker').find('#completedDate').val(data.completedDate?data.completedDate :'');

                            $('#action-editor-container').find("#actionStatus").on("change", function () {
                                if ($(this).val() === 'Closed') {
                                    var completedDate = moment.utc(new Date()).tz(serverTimeZone).format("DD-MMM-YYYY");
                                    $('#action-editor-container').find('#completion-date-picker').find('#completedDate').val(completedDate);
                                }
                            });

                            action_editor.find('#completion-date-picker').find('#completedDate').val(data.completedDate?data.completedDate :'');
                            var editActionModal = $('#action-edit-modal');
                            editActionModal.find('.id-element').attr('data-id', alertId);
                            editActionModal.modal({});
                            $('#action-editor-container').find('#appType').val(appType);
                            $('#action-edit-modal #cancel-bt').on('click', handle_action_editor_cancel);
                            $('#action-edit-modal #update-bt').on('click', handle_action_editor_update)
                        }

                    })
                })
                var filteredCount = $('#action-table').DataTable().rows({search:'applied'}).count();
                pageDictionary($('#action-table_wrapper'), filteredCount);
                showTotalPage($('#action-table_wrapper'), filteredCount);
                colEllipsis();
                webUiPopInitAction();
            },
            initComplete: function(oSettings, json) {
                $('.action-create').attr('data-id', alertId)
                signal.actions_utils.set_action_create_modal()
                $('#due-date-picker').datepicker()
                $('#action-table_wrapper .dt-search').attr("id", "action-table_filter");

            },
            "ajax": {
                "url": dataUrl,
                cache: false,
                dataSrc: ""
            },
            searching: true,
            layout: {
                topStart: ['buttons', 'search'],
                topEnd: null,
                bottomStart: ["pageLength", "info", {paging: {type: "full_numbers"}}],
                bottomEnd: null,
            },
            customProcessing: true,
            buttons: [
                {
                    text: 'New Action',
                    className: 'btn-primary action-create '+ buttonClassVar,
                    action: function ( e, dt, node, config ) {
                    }
                }
            ],
            aaSorting: [[0, "desc"]],
            "bLengthChange": true,
            "iDisplayLength": 10,
            "aLengthMenu": [[10, 20, 50], [10, 20, 50]],
            "aoColumns": [
                {
                    "mData": "id",
                    mRender: function(data, type, row) {
                        return "<a href='#' class='edit-action-link' data-id='" +
                            row.id + "'>" + row.id + "<a>"
                    },
                    "className": ""
                },
                {
                    "mData": "type",
                    "className": "",
                    "mRender": function (data, type, row) {
                        return "<span class='word-wrap-break-word'>" + escapeAllHTML(row.type) + "</span>";
                    }
                },
                {
                    "mData": "config",
                    "className": "col-min-200 col-max-200",
                    "mRender": function (data, type, row) {
                        return "<span class='word-wrap-break-word'>" + escapeAllHTML(row.config) + "</span>";
                    }
                },
                {
                    "mData": "details",
                    "className": "col-min-200 col-max-200 cell-break",
                    "mRender": function (data, type, row) {
                        var colElement = '<div class="col-container"><div class="col-height textPre">';
                        colElement += escapeAllHTML(row.details);
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + escapeAllHTML(row.details) + '" style="display:inline-block;" target-data="webuiPopover0" data-original-title="View All"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement;
                    }
                },
                {
                    "mData": "dueDate",
                    'className': 'col-min-150 col-max-150',
                    mRender: function(data, type, row) {
                        if (!(row.actionStatus === 'Closed' && row.actionStatus === 'Deleted') && row.passDue) {
                            return "<div class='closed'>" + row.dueDate + "</div>"
                        } else {
                            return "<div>" + row.dueDate + "</div>"
                        }
                    }
                },
                {
                    "mData": "assignedTo",
                    mRender: function(data, type, row) {
                        return escapeHTML(row.assignedTo)
                    },
                    "className": "col-min-150 col-max-150"
                },
                {
                    "mData": "",
                    mRender: function(data, type, row) {
                        return row.actionStatus
                    },
                    "className": ""
                },
                {
                    "mData": "completedDate",
                    mRender: function(data, type,row){
                        return row.completedDate
                    },
                    "className": "col-min-150 col-max-150"
                }
            ],
            scrollX : true,
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }]
        });
        return adhoc_validated_table
    };

    var build_action_render = function(data) {
        var action_drop_down_html = signal.utils.render("action_drop_down_5.1", data);
        return action_drop_down_html
    };
    var reload_action_table = function () {
        if (typeof adhoc_validated_table != "undefined" && adhoc_validated_table != null) {
            adhoc_validated_table.ajax.reload();
        } else {
            console.log("Unable to reload the activity table. Please refresh the page.")
        }
    };

    return {
        set_action_list_modal: set_action_list_modal,
        set_action_create_modal: set_action_create_modal,
        build_action_render: build_action_render,
        init_action_table: init_action_table,
        reload_action_table : reload_action_table,
        handle_action_editor_cancel: handle_action_editor_cancel,
        handle_action_editor_update: handle_action_editor_update,
        clear_edit_errors: clear_edit_errors
    }
})();
