//= require app/pvs/common/rx_common.js
//= require app/pvs/common/rx_handlebar_ext.js
//= require app/pvs/alerts_review/alert_review.js

var signal = signal || {}

signal.action = (function () {

    $(document).on("click",".check-action-access",function (e) {
        if((typeof userViewAccessMap !=="undefined" &&!userViewAccessMap[$(this).data("type")])||$(this).hasClass("no-access")){
            e.preventDefault();
            $.Notification.notify('warning', 'top right', "Warning", "You don't have access to perform this action", {autoHideDelay: 5000});
        }
    });

    var actionListTable
    var checkedActionIdList = []
    var checkedRowList = [];
    var executedIdList = [];




    function getRadios(toolbar) {
        var radios = '<div class="col-xs-12">';
        radios = radios + '<label class="no-bold add-cursor radio-container" ><input class="actionItemUserRadio" type="radio"  name="relatedResults" value="' + ACTION_ITEM_FILTER_ENUM.MY_OPEN + '" checked/>' + $.i18n._('myOpenActionItems') + '</label>'
        radios = radios + '<label class="no-bold add-cursor radio-container" ><input class="m-r-5 m-t-0 actionItemUserRadio" type="radio" value="' + ACTION_ITEM_FILTER_ENUM.MY_ALL + '" name="relatedResults"/>' +$.i18n._('myAllActionItems') +'</label>'
        radios = radios + '<label class="no-bold add-cursor radio-container" ><input class="m-r-5 m-t-0 actionItemUserRadio" type="radio" value="' + ACTION_ITEM_FILTER_ENUM.ALL + '" name="relatedResults" /> ' + $.i18n._('allActionItems') + '</label>'
        radios = $(radios);
        $(toolbar).append(radios);

    }

    var genActionEntityLink = function (row) {
        var url = '';
        switch (row.alertType) {
            case ALERT_CONFIG_TYPE.LITERATURE_SEARCH_ALERT:
                url = '<a target="_blank" href="' + pubMedUrl + row.alertId + '">' + row.entity + '</a>';
                break;
            case ALERT_CONFIG_TYPE.SINGLE_CASE_ALERT:
                if(userViewAccessMap[ALERT_CONFIG_TYPE.SINGLE_CASE_ALERT]&& row.hasAccess) {
                    url = '<a target="_blank" data-type ="' + ALERT_CONFIG_TYPE.SINGLE_CASE_ALERT + '" class="check-action-access" href="' + (caseDetailUrl + '?' +
                        signal.utils.composeParams({
                            caseNumber: row.entity,
                            version: row.caseVersion,
                            followUpNumber: row.followUpNumber,
                            isArchived: row.isArchived,
                            alertId: row.alertId,
                            isFaers: false,
                            isSingleAlertScreen: true
                        })) + '">' + row.entity + '</a>';
                } else {
                    url = '<a target="_blank" data-type ="' + ALERT_CONFIG_TYPE.SINGLE_CASE_ALERT + '" class="check-action-access no-access" href="javascript:void(0)">' + row.entity + '</a>';
                }
                break;
            case ALERT_CONFIG_TYPE.SIGNAL_MANAGEMENT:
                if(userViewAccessMap[ALERT_CONFIG_TYPE.SIGNAL_MANAGEMENT]&& row.hasAccess) {
                    url = '<a target="_blank" data-type ="' + ALERT_CONFIG_TYPE.SIGNAL_MANAGEMENT + '" class="check-action-access" href="' + validatedSignalUrl + '?id=' + row.alertId + '">' + row.entity + '</a>';
                } else {
                    url = '<a target="_blank" data-type ="' + ALERT_CONFIG_TYPE.SIGNAL_MANAGEMENT + '" class="check-action-access no-access" href="javascript:void(0)">' + row.entity + '</a>';
                }
                break;
            case ALERT_CONFIG_TYPE.ADHOC_ALERT:
                if(userViewAccessMap[ALERT_CONFIG_TYPE.ADHOC_ALERT]&& row.hasAccess) {
                    url = '<a target="_blank" class="check-action-access" href="' + adhocAlertUrl + '?id=' + row.alertId + '">' + row.entity + '</a>';
                } else {
                    url = '<a target="_blank" class="check-action-access no-access" href="javascript:void(0)">' + row.entity + '</a>';
                }
                break;
            case ALERT_CONFIG_TYPE.AGGREGATE_CASE_ALERT:
                url =  row.entity;
                break;
            case ALERT_CONFIG_TYPE.EVDAS_ALERT:
                url =  row.entity;
                break;
        }
        return url
    };


    var init_action_table = function (url) {
        actionListTable = $("#action-table").DataTable({
            language: { "lengthMenu": 'Show _MENU_'},
            layout: {
                topStart: ["pageLength", 'search'],
                topEnd: null,
                bottomStart: [
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
            drawCallback: function (settings) {
                $('.edit-action-link').on("click", function (evt) {
                    $('#action-edit-modal #update-bt').prop("disabled",false);
                    var url = "/signal/action/getById?id=" + $(this).attr('data-id');
                    $.ajax({
                        url: url,
                        async: false,
                        cache: false
                    }).done(function (data) {
                        data.action_types = action_select_values.types;
                        data.action_configs = action_select_values.configs;
                        data.all_status = action_select_values.allStatus;
                        if (typeof alertId != "undefined") {
                            data.alertId = $(this).attr('data-id');
                        }
                        data.actionStatus = {name: data.actionStatus, value: "New"};
                        if(data.action_configs!=undefined && data.alertType!="Signal Management"){
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
                        var html_content = signal.utils.render('action_editor_6.1_6_v1', data);
                        $('#action-editor-container').html(html_content);
                        if(!data.hasAccess){
                            $('#action-editor-container .check-action-access').addClass('no-access');
                        }
                        $('#action-editor-container #due-date-picker').datepicker({
                            date: data.dueDate,
                            allowPastDates: true,
                            momentConfig: {
                                culture: userLocale,
                                tz: userTimeZone,
                                format: DEFAULT_DATE_DISPLAY_FORMAT
                            }
                        });

                        $('#action-editor-container #completion-date-picker').datepicker({
                            date: data.completedDate,
                            allowPastDates: true,
                            restricted: [{from: TOMORROW , to: Infinity}],
                            momentConfig: {
                                culture: userLocale,
                                tz: userTimeZone,
                                format: DEFAULT_DATE_DISPLAY_FORMAT
                            }
                        }).on('inputParsingFailed.fu.datepicker',function (e) {
                            $('#completedDate').val('');
                        });

                       if(data.meetingId && data.meetingId != "null" && data.alertType =="Signal Management" ){
                           var option = new Option(data.config, data.configObj.id, true, true);
                           $('#action-edit-modal').find(".action-config-list .selectBox").append(option);
                       }
                        $('#action-editor-container #due-date-picker').datepicker();

                        $('#action-editor-container #completion-date-picker').datepicker({restricted: [{from: TOMORROW , to: Infinity}]}).on('inputParsingFailed.fu.datepicker',function (e) {
                            $('#completedDate').val('');
                        });

                        var dueDate = moment(data.dueDate, "DD-MMM-YYYY").format(DEFAULT_DATE_FORMAT);
                        $('#action-editor-container').find('#due-date-picker').find('#dueDate').val(dueDate);
                        var completedDate = data.completedDate? moment(data.completedDate, "DD-MMM-YYYY").format(DEFAULT_DATE_FORMAT):'';
                        $('#action-editor-container').find('#completion-date-picker').find('#completedDate').val(completedDate);



                        $('#action-editor-container').find('#due-date-picker').find('#dueDate').on("focusout", function(){
                            $(this).val(newSetDefaultDisplayDateFormat( $(this).val()));
                            if($(this).val()=='Invalid date'){
                                $(this).val('')
                            }
                        });
                        $('#action-editor-container').find('#completion-date-picker').find('#completedDate').on("focusout", function(){
                            $(this).val(newSetDefaultDisplayDateFormat( $(this).val()));
                            if($(this).val()=='Invalid date'){
                                $(this).val('')
                            }
                        });

                        $('#action-editor-container #actionStatus').on("change", function () {
                            if ($(this).val() === 'Closed') {
                                var completedDate = moment.utc(new Date()).tz(serverTimeZone).format("DD-MMM-YYYY");
                                $('#action-editor-container #completion-date-picker').find('#completedDate').val(completedDate);
                            }
                        });

                        $('#action-editor-container').find('#appType').val(data.alertType);
                        var action_editor = $('#action-editor-container');
                        var editActionModal = $('#action-edit-modal');

                        if (typeof alertId != "undefined") {
                            editActionModal.find('.id-element').attr('data-id', $(this).attr('data-id'))
                        }
                        editActionModal.modal({});
                        $('#action-edit-modal #cancel-bt').off().on("click", handle_action_editor_cancel);
                        $('#action-edit-modal #update-bt').off().on("click", handle_action_editor_update);
                    })
                });
                colEllipsis();
                webUiPopInit();
            },
            initComplete: function (oSettings, json) {
                var $divToolbar = $('<div class="toolbarDiv col-xs-6"></div>');
                var $rowDiv = $('<div class=""></div>');
                $divToolbar.append($rowDiv);
                $(".dt-search").attr("id", "action-table_filter");
                $(".dt-length").closest(".dt-layout-row").addClass('col-xs-12');
                $(".dt-length").addClass("col-xs-2");
                $("#action-table_filter").attr("class", "col-xs-4");
                $("#action-table_filter").attr("style", "display: flex; justify-content: right;");
                $($("#action-table_filter").children()[0]).attr("class", "col-xs-4  ");
                $($("#action-table_filter").children()[0]).attr("style", "font-weight:normal;width:auto;padding:0");
                $("#action-table_filter").before($divToolbar);
                getRadios($rowDiv);
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

                $('#action-edit-modal').on('hidden.bs.modal', function (e) {
                    clear_edit_errors();
                    $(this)
                        .find("input[type=text],textarea,select")
                        .val('')
                        .end()
                });

                $(".action-create").on("click", function(evt) {
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
                    createActionButton.attr("disabled", false);
                    clear_errors();
                    $('.action-type-list').removeClass('hidden');
                    $('.meeting-list').addClass('hidden');
                    createActionModal.modal({});
                    createActionModal.find('.action-type-list  select').val('').trigger('change');
                    createActionModal.find('.action-value').val('').trigger('change');

                    //Event bind when the save/create button is click
                    // ked in the action event modal.
                    createActionButton.off('click').on("click", function (event) {
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


                firstTimeLoad = false
                $('#rxTableReportsExecutionStatus tbody tr').each(function () {
                    $(this).find('td:eq(3)').attr('nowrap', 'nowrap');
                });
                $('#action-table_filter.dt-input').val("");
                $(".actionItemUserRadio").on("change", function () {
                        var filter_type = "";
                        if ($('input[name="relatedResults"]').length > 0) {
                            filter_type = $('input[name="relatedResults"]:checked').val();
                        }
                        var actionItemListUrl = actionItemUrl + "?filterType="+filter_type;
                        actionListTable.ajax.url(actionItemListUrl).load()
                });
                signal.alertReview.enableMenuTooltips();
                signal.alertReview.disableTooltips();
            },
            serverSide: true,
            "aaSorting": [[0, 'desc']], // default sorting on id
            "bLengthChange": true,
            "iDisplayLength": 50,
            "aLengthMenu": [[50, 100, 200, -1], [50, 100, 200, "All"]],
            "sPaginationType": "simple_numbers",
            "ajax": {
                "url": url,
                "dataSrc": "aaData",
                data: function (args) {
                    return {
                        "args": JSON.stringify(args)
                    };
                }

            },
            "aoColumns": [
                {
                    "mData": "id",
                    'name': "id",
                    "mRender": function (data, type, row) {
                        return "<a href='#' class='edit-action-link' data-id='" +
                            row.id + "'>" + row.id + "<a>"
                    },
                    "className": 'col-min-100 col-max-100',
                },
                {
                    "mData": "type",
                    'name': "actionType",
                    "className": 'col-min-100 col-max-100',
                    "mRender": function (data, type, row) {
                        return "<span class='word-wrap-break-word'>" + encodeToHTML(row.type) + "</span>";
                    }
                },
                {
                    "mData": "config",
                    "className": 'col-min-200 col-max-200',
                    'name': "actionConfig",
                    "mRender": function (data, type, row) {
                        return "<span class='word-wrap-break-word'>" + encodeToHTML(row.config) + "</span>";
                    }
                },
                {
                    "mData": "details",
                    "mRender": function (data, type, row) {
                        return addEllipsisForDescriptionText(encodeToHTML(row.details))
                    },
                    "className": 'col-min-200 col-max-200 cell-break',
                    'name': "details"
                },
                {
                    "mData": "alertName",
                    "mRender": function (data, type, row) {
                        return addEllipsisForDescriptionText(encodeToHTML(row.alertName))
                    },
                    "className": 'col-min-200 col-max-200 cell-break',
                    'name': "alertName"
                },
                {
                    "mData": "alertType",
                    "mRender": function (data, type, row) {
                        return convertAlertTypeName(row.alertType)
                    },
                    'className': 'col-min-150 col-max-150',
                    'name': "alertType"
                },
                {
                    "mData": "entity",
                    "sortable": false,
                    'className': 'col-min-150 col-max-150 cell-break',
                    "mRender": function (data,type,row) {
                        if(data == "" || data == "-"){
                            return "-";
                        } else {
                            return genActionEntityLink(row);
                        }

                    }
                },
                {
                    "mData": "dueDate",
                    "mRender": function (data) {
                        var runDate = new Date(data);
                        return moment(runDate).format(DEFAULT_DATE_DISPLAY_FORMAT);
                    },
                    'className': 'col-min-150 col-min-150',
                    'name': "dueDate"
                },
                {
                    "mData": "actionStatus",
                    "mRender": function (data, type, row) {
                        return "<span data-field ='actionStatus' data-value='" + row.actionStatus + "'>" + row.actionStatus + "</span>"
                    },
                    'className': "col-min-150 col-max-150",
                    'name': "actionStatus"
                },
                {
                    "mData": "comments",
                    "sortable": false,
                    "className": 'col-min-200 col-max-200 cell-break',
                    'name': "comments",
                    "mRender": function (data, type, row) {
                        return addEllipsisForDescriptionText(encodeToHTML(row.comments))
                    }
                },
                {
                    "mData": "completedDate",
                    'className': 'col-min-150 col-max-150',
                    'name': "completedDate",
                    mRender: function (data, type, row) {
                        if (row.completedDate == null || row.completedDate == "-") {
                            return "-"
                        } else {
                            return (row.completedDate);
                        }
                    }
                }
            ],
            scrollX: true,
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }]
        });

        return actionListTable
    }

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
            "actionId": modalId.find("#actionId").val()
        }
    };





    var handle_action_editor_cancel = function (evt) {
        evt.preventDefault()
        $('#action-edit-modal').modal('hide')
    }

    var handle_action_editor_update = function (evt) {
        $('#action-edit-modal #update-bt').prop("disabled",true);
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
            })
                .done(function (response) {
                    if (response.status == false) {
                        $('#action-edit-modal #update-bt').prop("disabled",false);
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
                        $('#action-edit-modal').modal('hide')
                    }
            })
        }
    }
    var clear_edit_errors = function () {
        $('#action-edit-modal .modal-body .alert').remove()
    }
    function initializeDueDate() {
        var dueDate = null;

        if (document.getElementById('dueDate') != null &&
            document.getElementById('dueDate').value != null) {
            dueDate = (document.getElementById('dueDate').value);
        }
        $('#dueDate').datepicker({
            allowPastDates: true,
            date:dueDate
        }).on('changed.fu.datepicker', function (evt, date) {
            dueDate = date;
            updateInputField();
        }).on("click", function () {
            dueDate = $("#dueDate").datepicker('getDate');
            updateInputField();
        });

        var updateInputField = function () {
            if (dueDate != 'Invalid Date' && dueDate != "") {
                $('input[name="dueDate"]').val(renderDateWithTimeZone(dueDate));
            }
        };
        updateInputField();
        $("#dueDate").on("focusout", function(){
            $(this).val(newSetDefaultDisplayDateFormat( $(this).val()));
            if($(this).val()=='Invalid date'){
                $(this).val('')
            }
            document.getElementById('dueDate').value=moment($(this).val(),['DD/MM/YYYY','DD-MMM-YYYY']).utc($(this).val()).format(DEFAULT_DATA_ANALYSIS_DATE_FORMAT)
        });
    }
    function initializeCompletedDate() {
        var completedDate = null;

        if (document.getElementById('completedDate') != null &&
            document.getElementById('completedDate').value != null) {
            completedDate = (document.getElementById('completedDate').value);
        }
        $('#completedDate').datepicker({
            allowPastDates: true,
            date:completedDate
        }).on('changed.fu.datepicker', function (evt, date) {
            completedDate = date;
            updateInputField();
        }).on("click", function () {
            completedDate = $("#completedDate").datepicker('getDate');
            updateInputField();
        });

        var updateInputField = function () {
            if (completedDate != 'Invalid Date' && completedDate != "") {
                $('input[name="completedDate"]').val(renderDateWithTimeZone(completedDate));
            }
        };
        updateInputField();
        $("#completedDate").on("focusout", function(){
            $(this).val(newSetDefaultDisplayDateFormat( $(this).val()));
            if($(this).val()=='Invalid date'){
                $(this).val('')
            }
            document.getElementById('completedDate').value=moment($(this).val(),['DD/MM/YYYY','DD-MMM-YYYY']).utc($(this).val()).format(DEFAULT_DATA_ANALYSIS_DATE_FORMAT)
        });
    }

    return {
        init_action_table: init_action_table
    }
})()
