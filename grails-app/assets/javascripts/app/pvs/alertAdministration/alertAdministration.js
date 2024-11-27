//= require app/pvs/common/rx_common.js
//= require app/pvs/common/rx_list_utils.js
//= require app/pvs/common/rx_handlebar_ext.js
//= require app/pvs/schedular.js

const SUCCESS = 'SUCCESS';
const PRE_REQUISITE_FAIL = 'PRE_REQUISITE_FAIL';
var selectedCasesInfo = [];
var prev_page = [];
var splitText = "Individual Case Alert";

$(function () {
    $(document).on('click', '#checkBoxForAlertAdministration', function (event) {
        var isChecked = $(this).is(":checked");
        if(isChecked) {
            alertIdSet.add($(this).attr("data-id"));
            var data = {};
            var currentIndex = $(this).closest('tr').index();
            var rowObject = table.rows(currentIndex).data()[0];
            data['id'] = rowObject.id;
            data['name'] = rowObject.name;
            selectedCasesInfo.push(data)
        }else{
            selectedCasesInfo.splice($.inArray($(this).attr("data-id"), alertIdSet), 1);
            alertIdSet.delete($(this).attr("data-id"));
        }
    });

    $(".viewAlertRadio").attr("autocomplete", "off");
    var alertType
    var alertRunType
    var substanceName
    var autoAdjustEnumToStringMap = {
        'ALERT_PER_SKIPPED_EXECUTION': 'Auto-Adjust Date and Execute alert for every skipped execution',
        'SINGLE_ALERT_FOR_ALL_SKIPPED_EXECUTION': 'Auto-Adjust Date and execute a single alert for all skipped execution',
        'MANUAL_AUTO_ADJUSTMENT':  'Disable Alert Execution and Enable based on Manual Intervention'
    };
    var build_url_for_alert_administration = function (data) {
        var returnObj = new Object();
        returnObj.run_display = $.i18n._('run');
        returnObj.delete_latest_display = $.i18n._('Delete Latest Version');
        returnObj.delete_all_display = $.i18n._('Delete All Version');
        returnObj.disable_display = $.i18n._('Disable Alerts(s)');
        returnObj.enable_display = $.i18n._('Enable Alert(s)');
        returnObj.update_and_execute_display = $.i18n._('Update and Execute Alert(s)');
        returnObj.obj_id = data.id;
        returnObj.obj_name = data.name;
        returnObj.type = data.type;
        returnObj.evdasProduct = data.productSelection
        returnObj.edit_url = CONFIGURATION.editUrl + "/" + data.id;
        returnObj.copy_url = CONFIGURATION.copyUrl + '/' + data.id;
        returnObj.isEnabled = (data.status != "In Progress" && data.status != "Deletion In Progress" && ((alertRunType == "Scheduled") || data.noOfExecution>0));
        returnObj.showEdit = true;
        returnObj.isManuallyPaused = data.isManuallyPaused;
        returnObj.isScheduled = alertRunType == "Scheduled" ? true : false

        switch (data.type) {
            case 'Individual Case Configuration':
                returnObj.run_display = $.i18n._('run');
                returnObj.edit_url = CONFIGURATION.sca_edit_url + "/" + data.id;
                returnObj.copy_url = CONFIGURATION.sca_copy_url + '/' + data.id;
                returnObj.isAdhocRun = data.isAdhocRun;
                break;
            case 'Aggregate Configuration':
                returnObj.run_display = $.i18n._('run');
                returnObj.edit_url = CONFIGURATION.aga_edit_url + "/" + data.id;
                returnObj.copy_url = CONFIGURATION.aga_copy_url + '/' + data.id;
                returnObj.isAdhocRun = data.isAdhocRun;
                returnObj.masterConfigId = data.masterConfigId;
                returnObj.showEdit = (data.masterConfigId == null && data.unscheduled == false);
                returnObj.isRun = data.unscheduled == false && data.isRunnable == true
                break;
            case 'EVDAS Configuration':
                returnObj.run_display = $.i18n._('run');
                returnObj.edit_url = CONFIGURATION.evdas_edit_url + "/" + data.id;
                returnObj.copy_url = CONFIGURATION.evdas_copy_url + '/' + data.id;
                returnObj.delete_url = CONFIGURATION.evdas_delete_url + '/' + data.id;
                returnObj.isEvdas = true;
                returnObj.isAdhocRun = data.isAdhocRun;
                break;
            case 'Ad-Hoc Alert':
                returnObj.run_display = $.i18n._('run');
                returnObj.edit_url = CONFIGURATION.adha_edit_url + "/" + data.id;
                returnObj.copy_url = CONFIGURATION.adha_copy_url + '/' + data.id;
                returnObj.isAdhocRun = false;
                break;
            case 'Literature Configuration':
                returnObj.run_display = $.i18n._('run');
                returnObj.edit_url = CONFIGURATION.literature_edit_url + "/" + data.id;
                returnObj.copy_url = CONFIGURATION.literature_copy_url + '/' + data.id;
                returnObj.delete_url = CONFIGURATION.literature_delete_url + '/' + data.id;
                returnObj.isLiterature = true;
                returnObj.isAdhocRun = false;
            default:
                break
        }

        return returnObj;
    };

    $("input[name='relatedResults']:first").prop('checked', true);
    alertType = 'Single Case Alert'
    alertRunType = $('input[name="alertRunType"]:checked').val()  !== undefined ? $('input[name="alertRunType"]:checked').val() : 'Scheduled';
    $(".alertTypeSelected").on('click', function () {
        removeExistingMessageHolder();
        alertType =$(this).attr('value');
        if(!alertType){
            alertType = 'Single Case Alert';
        }
        if(alertType === "Single Case Alert"){
            splitText = "Individual Case Alert";
        }else if(alertType === "Aggregate Case Alert"){
            splitText = "Aggregate Alert";
        }else if(alertType === "Literature Search Alert"){
            splitText = "Literature Alert";
        }else {
            splitText = alertType;
        }
        prev_page = []
        alertIdSet.clear();
        selectedCasesInfo = []
        $(".select-all-alert-config").prop('checked', false);
        $('#rxTableConfiguration').DataTable().ajax.url(CONFIGURATION.listUrl + "?alertType=" + alertType +  "&alertRunType=" + alertRunType).load()
    });


    $(document).on('click', '.updateAndExecute', function (event) {
        var updateAndExecuteModal = $(".update_and_execute_modal");
        substanceName = $(this).attr('data-productname')
        if(alertRunType==='Scheduled'){
            $("#update-and-run-alert").text("Unschedule and Run");
        }else{
            $("#update-and-run-alert").text("Update and Run");
        }
        // Hide evaluated date as input option for Evdas and Literature
        if(alertType==='EVDAS Alert'){
            $('#evaluatedDateAsDiv').hide();
            $('.configDateRangeDiv').hide();
            $('.evdasDateRangeDiv').show();
        }else if(alertType==='Literature Search Alert'){
            $('#evaluatedDateAsDiv').hide();
            $('.configDateRangeDiv').show();
            $('.evdasDateRangeDiv').hide();
        }else{
            $('#evaluatedDateAsDiv').show();
            $('.configDateRangeDiv').show();
            $('.evdasDateRangeDiv').hide();
        }
        updateAndExecuteModal.modal("show");
        updateAndExecuteModal.find('.modal-body').css('height', '270px');
        $('#configId').attr('value',$(this).attr('data-instanceid'));
        $('#myScheduler').hide();
        $("#runNow").prop('checked', true);
        $("#futureSchedule").prop('checked', false);
        populateUpdateAndExecuteInfoDiv()
    });

    $(document).on('click', '#singleCaseAutoAdjustmentEnabled', function (event) {
        // hide auto adjustment options for single case if this is disabled
        if (($("#pvrCheck").prop('checked') || $("#versionAsOf").prop('checked') || $("#etlFailure").prop('checked') || $("#etlInProgress").prop('checked'))) {
            if (!$("#singleCaseAutoAdjustmentEnabled").prop('checked')) {
                $("#icrAutoAdjustmentRule").hide();
                $("#icrAutoAdjustmentInfo").hide();
            } else {
                $("#icrAutoAdjustmentRule").show();
                $("#icrAutoAdjustmentInfo").show();
            }
        }else{
            $("#singleCaseAutoAdjustmentEnabled").prop('checked', false)
            $("#save-alert-pre-check-values").prop('disabled', true);
        }
    });

    $(document).on('click', '#aggregateAutoAdjustmentEnabled', function (event) {
        // hide auto adjustment options for aggregate if this is disabled
        if (($("#pvrCheck").prop('checked') || $("#versionAsOf").prop('checked') || $("#etlFailure").prop('checked') || $("#etlInProgress").prop('checked'))) {
            if (!$("#aggregateAutoAdjustmentEnabled").prop('checked')) {
                $("#aggregateAutoAdjustmentRule").hide();
                $("#aggAutoAdjustmentInfo").hide();
            } else {
                $("#aggregateAutoAdjustmentRule").show();
                $("#aggAutoAdjustmentInfo").show();
            }
        } else {
            $("#aggregateAutoAdjustmentEnabled").prop('checked', false)
            $("#save-alert-pre-check-values").prop('disabled', true);
        }
    });

    $(".viewAlertRadio").on('change', function () {
        removeExistingMessageHolder();
        alertRunType = $('input[name="alertRunType"]:checked').val();
        prev_page = []
        alertIdSet.clear();
        selectedCasesInfo = []
        $(".select-all-alert-config").prop('checked', false);
        $('#rxTableConfiguration').DataTable().ajax.url(CONFIGURATION.listUrl + "?alertType=" + alertType + "&alertRunType=" + alertRunType).load()
    });
    $(document).on('click', '#rxTableConfiguration_paginate', function () {
        if ($('.select-all-alert-config').is(":checked") && !prev_page.includes($('li.active').text().split(splitText)[1].trim())) {
            prev_page.push($('li.active').text().split(splitText)[1].trim());
        }
        if ((!$('.select-all-alert-config').is(":checked") && prev_page.includes($('li.active').text().split(splitText)[1].trim()))) {
            var position = prev_page.indexOf($('li.active').text().split(splitText)[1].trim());
            prev_page.splice(position, 1);
        }
    });

    var table = $('#rxTableConfiguration').DataTable({
        "sPaginationType": "full_numbers",
        "language": {
            "url": "../assets/i18n/dataTables_" + userLocale + ".json"
        },
        drawCallback: function (settings) {
            var current_page = $('li.active').text().split(splitText)[1]?.trim();
            if (typeof prev_page != 'undefined' && $.inArray(current_page, prev_page) == -1) {
                $(".select-all-alert-config").prop('checked', false);
            } else {
                $(".select-all-alert-config").prop('checked', true);
            }
            colEllipsis();
            webUiPopInit();
            var rowsDataAR = $('#rxTableConfiguration').DataTable().rows().data();
            if(settings.json !== undefined) {
                pageDictionary($('#rxTableConfiguration_wrapper'), settings.json.recordsFiltered);
                showTotalPage($('#rxTableConfiguration_wrapper'), settings.json.recordsFiltered);
            }else {
                pageDictionary($('#rxTableConfiguration_wrapper'), rowsDataAR.length);
                showTotalPage($('#rxTableConfiguration_wrapper'), rowsDataAR.length);
            }
        },

        initComplete: function () {
            init_filter(table);
            $('#rxTableQueries tbody tr').each(function () {
                $(this).find('td:eq(5)').attr('nowrap', 'nowrap');
            });
            var theDataTable = $('#rxTableConfiguration').DataTable();

            $("#toggle-column-filters").on('click', function () {
                var ele = $('.yadcf-filter-wrapper');
                var inputEle = $('.yadcf-filter');
                if (ele.is(':visible')) {
                    ele.hide();
                } else {
                    ele.show();
                    inputEle.first().focus();
                }
            });

            $("#toggle-column-filters").mouseleave(function(){
                $(this).tooltip("hide");
            });
            $('.yadcf-filter-wrapper').hide();
            $('#rxTableConfiguration').DataTable().draw();

        },
        processing: true,
        serverSide: true,
        "ajax": {
            "url": CONFIGURATION.listUrl + "?alertType=" + alertType + "&alertRunType=" + alertRunType,
            "dataSrc": "aaData"
        },
        "aaSorting": [[9, "desc"]],
        "bLengthChange": true,
        "iDisplayLength": 50,
        "bAutoWidth": false,
        "aLengthMenu": [[50, 100, 200, 500], [50, 100, 200, 500]],
        "aoColumns": [
            {
                "mData": "selected",
                "mRender": function (data, type, row) {
                    var disable = ((!(alertRunType === "Scheduled") && row.noOfExecution==0) || row.status == "In Progress" || row.status == "Deletion In Progress") ? ' disabled="disabled"' : ''
                    if (typeof row.id !== "undefined" && alertIdSet.has(JSON.stringify(row.id))) {
                        return '<input type="checkbox" id="checkBoxForAlertAdministration" class="alert-check-box editor-active copy-select" data-id=' + row.id + disable + ' checked />'
                    }else {
                        return '<input type="checkbox" id="checkBoxForAlertAdministration" class="alert-check-box editor-active copy-select" data-id=' + row.id + disable + ' />';
                    }
                },
                "className": "",
                "sortable": false,
                "visible": true
            },
            {
                "mData": "name",
                "mRender": function (data, type, row) {
                    return addEllipsisForDescriptionText(encodeToHTML(row.name))
                },
                "className": 'col-min-150 col-max-150 cell-break'
            },
            {
                "mData": "productSelection",
                "mRender": function (data, type, row) {
                    return row.productSelection
                },
                "className": 'col-min-200  col-max-200 cell-break'
            },
            {
                "mData": "dataSource",
                "mRender": function (data, type, row) {
                    return row.dataSource
                },
                "className": 'col-min-200  col-max-200 cell-break'
            },
            {
                "mData": "description",
                "mRender": function (data, type, row) {
                    if (row.description) {
                        return "<span >" + addEllipsisForDescriptionText(encodeToHTML(row.description))+ "</span>"
                    } else {
                        return "-"
                    }
                },
                "className": 'col-min-200  col-max-200 cell-break'
            },
            {
                "mData": "noOfExecution",
                "className": 'col-min-100 col-max-100'
            },
            {
                "mData": "dateRange",
                "aTargets": ["dateRange"],
                "sClass": "col-min-150 col-max-150"
            },
            {
                "mData": "dateCreated",
                "aTargets": ["dateCreated"],
                "sClass": "col-min-150 col-max-150",
                "mRender": function (data, type, full) {
                    var dateCreated = new Date(data);
                    return moment(dateCreated).tz(userTimeZone).format('lll');
                }
            },
            {
                "mData": "dateModified",
                "aTargets": ["dateModified"],
                "sClass": "col-min-150 col-max-150",
                "mRender": function (data, type, row) {
                    var dateModified = new Date(data);
                    return moment(dateModified).tz(userTimeZone).format('lll');
                }
            },
            {
                "mData": "lastExecution",
                "className": 'col-min-150 col-max-150',
                "mRender": function (data, type, row) {
                    if (data) {
                        var lastExecution = new Date(data);
                        return moment(lastExecution).tz(userTimeZone).format('lll');
                    } else {
                        return "-"
                    }
                }
            },
            {
                "mData": "nextExecutionDateRange",
                "aTargets": ["nextExecutionDateRange"],
                "mRender": function (data, type, row) {
                    if (data && alertRunType == "Scheduled") {
                        return data;
                    } else {
                        return "-"
                    }
                },
                "sClass": "col-min-200 col-max-200"
            },
            {
                "mData": "nextRunDate",
                "className": 'col-min-200 col-max-200',
                "mRender": function (data, type, row) {
                    if (data && alertRunType == "Scheduled") {
                        var nextRunDate = new Date(data);
                        return moment(nextRunDate).tz(userTimeZone).format('lll');
                    } else {
                        return "-"
                    }
                }
            },
            {   "mData": "createdBy",
                "className": 'col-min-100 col-max-100'},
            {
                "mData": "status",
                "mRender": function (data, type, row) {
                    if (row.status === "In Progress") {
                        return addEllipsis(row.status) + "<i id ='alertInProgressInfo' class='glyphicon glyphicon-info-sign themecolor add-cursor' style='top: -18px; right: -80px; cursor: pointer'></i>"
                    } else if (row.status === "Deletion In Progress") {
                        return addEllipsis(row.status) + "<i id='deletionInProgressInfo' class='glyphicon glyphicon-info-sign themecolor add-cursor ' style='top: -18px; right: -138px; cursor: pointer'></i>"
                    } else {
                        return addEllipsis(row.status)
                    }
                },
                "className": 'col-min-150 col-max-150 cell-break'
            },
            {
                "mData": null,
                "sortable": false,
                "aTargets": ["id"],
                "mRender": function (data, type, row) {
                    var actionButtonContent = signal.utils.render('alert_administration_action_button_61', build_url_for_alert_administration(row, alertRunType));
                    return actionButtonContent
                },
                "sClass": "col-min-75 col-max-75"
            }
        ],
        "oLanguage": {
            "sEmptyTable": "No data available in table",
            "oPaginate": {
                "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
            },
            "sLengthMenu": "Show _MENU_"
        },
        layout: {
            topStart: null,
            topEnd: 'search',
            bottomStart: ['pageLength', 'info', {paging: {type: "full_numbers"}}],
            bottomEnd: null,
        },
        customProcessing: true,
        columnDefs: [{
            "targets": '_all',
            "render": $.fn.dataTable.render.text(),
            orderSequence: ['desc', 'asc']
        }],
        scrollX: true,
        scrollY: '50vh'
    });

    var init_filter = function (data_table) {
        yadcf.init(data_table, [
            {column_number: 1, filter_type: 'text', filter_reset_button_text: false},
            {column_number: 2, filter_type: "text", filter_reset_button_text: false},
            {column_number: 3, filter_type: "text", filter_reset_button_text: false},
            {column_number: 4, filter_type: "text", filter_reset_button_text: false},
            {column_number: 5, filter_type: "text", filter_reset_button_text: false},
            {column_number: 6, filter_type: "text", filter_reset_button_text: false},
            {column_number: 7, filter_type: "text", filter_reset_button_text: false},
            {column_number: 8, filter_type: "text", filter_reset_button_text: false},
            {column_number: 9, filter_type: "text", filter_reset_button_text: false},
            {column_number: 10, filter_type: "text", filter_reset_button_text: false},
            {column_number: 11, filter_type: "text", filter_reset_button_text: false},
            {column_number: 12, filter_type: "text", filter_reset_button_text: false},
            {column_number: 13, filter_type: "text", filter_reset_button_text: false},
        ]);
        $(".yadcf-filter").removeAttr('placeholder');
    };

    actionButton('#rxTableConfiguration');
    loadTableOption('#rxTableConfiguration');

    $( window ).on('unload', function() {
        $("input[name='relatedResults']:first").prop('checked', true);
    });

    $(document).on('click', '#toggle-alert-pre-check', function (event) {
        event.preventDefault();
        var url = "/signal/alertAdministration/editAlertPreCheck";
        $.ajax({
            type: "GET",
            url: url,
            dataType: 'json',
        }).done(populateAlertPreExValues);
    });

    var populateAlertPreExValues = function (response){
        if(response.status){
            var alertPreCheckModal = $(".alert-pre-execution-check-modal");
            alertPreCheckModal.modal("show");
            var data = response.data
            $("#pvrCheck").prop('checked', data.isPvrCheckEnabled);
            $("#versionAsOf").prop('checked', data.isVersionAsOfCheckEnabled);
            $("#etlFailure").prop('checked', data.isEtlFailureCheckEnabled);
            $("#etlInProgress").prop('checked', data.isEtlInProgressCheckEnabled);

            $("#pvrCheck").attr('data-value', data.isPvrCheckEnabled);
            $("#versionAsOf").attr('data-value', data.isVersionAsOfCheckEnabled);
            $("#etlFailure").attr('data-value', data.isEtlFailureCheckEnabled);
            $("#etlInProgress").attr('data-value', data.isEtlInProgressCheckEnabled);

            for (let i=0;i<data.autoAdjustmentRuleData.length; i++){
                var rules = data.autoAdjustmentRuleData[i];
                if(rules.alertType==="Single Case Alert"){
                    $("#icrAutoAdjustmentRule").val(rules.adjustmentTypeEnum.name);
                    $("#singleCaseAutoAdjustmentEnabled").prop('checked', rules.isEnabled);
                    $("#singleCaseAutoAdjustmentEnabled").attr('data-value', rules.isEnabled);
                    if(rules.isEnabled) {
                        $("#icrAutoAdjustmentRule").show();
                    }else{
                        $("#icrAutoAdjustmentRule").hide();
                        $("#icrAutoAdjustmentInfo").hide();
                    }
                }else{
                    $("#aggregateAutoAdjustmentRule").val(rules.adjustmentTypeEnum.name);
                    $("#aggregateAutoAdjustmentEnabled").prop('checked', rules.isEnabled);
                    $("#aggregateAutoAdjustmentEnabled").attr('data-value', rules.isEnabled);
                    if(rules.isEnabled) {
                        $("#aggregateAutoAdjustmentRule").show();
                    }else{
                        $("#aggregateAutoAdjustmentRule").hide();
                        $("#aggAutoAdjustmentInfo").hide();
                    }
                }
            }
            $("#save-alert-pre-check-values").prop('disabled', true);
        }
    }

    $('input[type="checkbox"]').off('click').on('click', function () {
        var hasStateChanged = isStateOfAlertPreChecksChanged()
        if (hasStateChanged) {
            $("#save-alert-pre-check-values").prop('disabled', false);
        } else {
            $("#save-alert-pre-check-values").prop('disabled', true);
        }
    });
    $('#aggregateAutoAdjustmentRule, #icrAutoAdjustmentRule').off().on('change', function () {
        var hasStateChanged = isStateOfAlertPreChecksChanged()
        if (hasStateChanged) {
            $("#save-alert-pre-check-values").prop('disabled', false);
        } else {
            $("#save-alert-pre-check-values").prop('disabled', true);
        }
    });

    function isStateOfAlertPreChecksChanged() {
        let hasStateChanged = false
        let oldPvrCheckValue = $("#pvrCheck").attr('data-value') === "true";
        let oldVersionAsOfValue = $("#versionAsOf").attr('data-value') === "true";
        let oldEtlFailureValue = $("#etlFailure").attr('data-value') === "true";
        let oldEtlInProgressValue = $("#etlInProgress").attr('data-value') === "true";
        let oldSingleCaseAutoAdjustmentValue = $("#singleCaseAutoAdjustmentEnabled").attr('data-value') === "true";
        let oldAggregateAutoAdjustmentValue = $("#aggregateAutoAdjustmentEnabled").attr('data-value') === "true";

        let newPvrCheckValue = $("#pvrCheck").prop('checked');
        let newVersionAsOfValue = $("#versionAsOf").prop('checked');
        let newEtlFailureValue = $("#etlFailure").prop('checked');
        let newEtlInProgressValue = $("#etlInProgress").prop('checked');
        let newSingleCaseAutoAdjustmentValue = $("#singleCaseAutoAdjustmentEnabled").prop('checked');
        let newAggregateAutoAdjustmentValue = $("#aggregateAutoAdjustmentEnabled").prop('checked');

        if (oldPvrCheckValue !== newPvrCheckValue) {
            hasStateChanged = true
        }
        if (oldVersionAsOfValue !== newVersionAsOfValue) {
            hasStateChanged = true
        }
        if (oldEtlFailureValue !== newEtlFailureValue) {
            hasStateChanged = true
        }
        if (oldEtlInProgressValue !== newEtlInProgressValue) {
            hasStateChanged = true
        }
        if (oldSingleCaseAutoAdjustmentValue !== newSingleCaseAutoAdjustmentValue) {
            hasStateChanged = true
        }
        if (oldAggregateAutoAdjustmentValue !== newAggregateAutoAdjustmentValue) {
            hasStateChanged = true
        }
        return hasStateChanged
    }
    $('#futureSchedule').off('click').on('click', function() {
        if($("#futureSchedule").prop('checked')){
            $('#myScheduler').show();
        }else {
            $('#myScheduler').hide();
        }
    });


    $(document).on('click', '.pre-check-option', function (event) {
        if (!($("#pvrCheck").prop('checked') || $("#versionAsOf").prop('checked') || $("#etlFailure").prop('checked') || $("#etlInProgress").prop('checked'))) {
            $("#singleCaseAutoAdjustmentEnabled").prop('checked', false);
            $("#icrAutoAdjustmentRule").hide();
            $("#icrAutoAdjustmentInfo").hide();
            $("#aggregateAutoAdjustmentEnabled").prop('checked', false);
            $("#aggregateAutoAdjustmentRule").hide();
            $("#aggAutoAdjustmentInfo").hide();
        }
    });



    $(document).on('click', '.run-button', function (event) {

        event.preventDefault();
        var href = $(event.target).attr('href');
        var masterConfigId = $(event.target).attr('master-config-id')

        if(masterConfigId != "undefined" && masterConfigId != null && masterConfigId != "null" && masterConfigId != "") {
            bootbox.confirm({
                title: ' ',
                message: "Executing this alert will execute all child alerts in the current master configuration.",
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
                        window.location.href = href + "&masterConfigId=" + masterConfigId
                    } else {
                        event.preventDefault();
                    }
                }
            });
        } else {
            window.location.href = href;
        }
    });

    $(document).on("click", "#alertPreCheckInfo", function () {
        var $alertPreCheckInfoDiv = $("#alertPreCheckInfoDiv");
        var display = $alertPreCheckInfoDiv.css('display')
        $('.popupBox').hide();
        var $this = $(this);
        var url = "/signal/alertAdministration/editAlertPreCheck";
        $.ajax({
            type: "GET",
            url: url,
            dataType: 'json',
        }).done(function(resp){
            populatePreCheckAndAdjustInfo(resp)
            if(display==='none'){
                showEditDiv($this,$alertPreCheckInfoDiv);
            }
        });
    })
    var populatePreCheckAndAdjustInfo = function (response){
        if(response.status){
            var data = response.data
            if(data.isPvrCheckEnabled){
                $("#disableOnPvrIssue").text("Yes");
            }else{
                $("#disableOnPvrIssue").text("No");
            }
            if(data.isVersionAsOfCheckEnabled || data.isEtlFailureCheckEnabled || data.isEtlInProgressCheckEnabled){
                $("#disableOnEtlIssue").text("Yes");
            }else{
                $("#disableOnEtlIssue").text("No");
            }
            let isSCAAutoAdjustmentRuleEnabled = false
            let isAggAutoAdjustmentRuleEnabled = false
            for (let i = 0; i < data.autoAdjustmentRuleData.length; i++) {
                var rules = data.autoAdjustmentRuleData[i];
                if (rules.alertType === "Single Case Alert") {
                    // rules.adjustmentTypeEnum.name
                    $("#autoAdjustTypeIcr").text(autoAdjustEnumToStringMap[rules.adjustmentTypeEnum.name]);
                    isSCAAutoAdjustmentRuleEnabled = rules.isEnabled
                } else if (rules.alertType === "Aggregate Case Alert") {
                    $("#autoAdjustTypeAgg").text(autoAdjustEnumToStringMap[rules.adjustmentTypeEnum.name]);
                    isAggAutoAdjustmentRuleEnabled = rules.isEnabled
                }
            }
            if (data.isPvrCheckEnabled || data.isVersionAsOfCheckEnabled || data.isEtlFailureCheckEnabled || data.isEtlInProgressCheckEnabled) {
                $("#preChecksStatusDiv").text("Enabled")
                if (isSCAAutoAdjustmentRuleEnabled && isAggAutoAdjustmentRuleEnabled) {
                    $("#autoAdjustRuleInfoDiv").css("display", "block");
                    $("#scaAutoAdjustRuleInfo").css("display", "block");
                    $("#aggAutoAdjustRuleInfo").css("display", "block");
                    $("#alertPreCheckInfoDiv").height(266);
                } else if (isSCAAutoAdjustmentRuleEnabled) {
                    $("#autoAdjustRuleInfoDiv").css("display", "block");
                    $("#scaAutoAdjustRuleInfo").css("display", "block");
                    $("#aggAutoAdjustRuleInfo").css("display", "none");
                    $("#alertPreCheckInfoDiv").height(190);
                } else if (isAggAutoAdjustmentRuleEnabled) {
                    $("#autoAdjustRuleInfoDiv").css("display", "block");
                    $("#scaAutoAdjustRuleInfo").css("display", "none");
                    $("#aggAutoAdjustRuleInfo").css("display", "block");
                    $("#alertPreCheckInfoDiv").height(190);
                } else {
                    $("#autoAdjustRuleInfoDiv").css("display", "none");
                    $("#alertPreCheckInfoDiv").height(80);
                }
            }else{
                $("#autoAdjustRuleInfoDiv").css("display", "none");
                $("#alertPreCheckInfoDiv").height(80);
            }
        }
    }

    $(document).on("click", "#etlStatusInfo", function () {
        var $etlInfoDiv = $("#etlInfoDiv");
        var display = $etlInfoDiv.css('display')
        $('.popupBox').hide();
        var $this = $(this);
        var url = "/signal/etlSchedule/etlInfo";
        if (display === 'none') {
            showEditDivEtl($this, $etlInfoDiv);
        }
        $.ajax({
            type: "GET", url: url, dataType: 'json'
        }).done(function (response) {
            populateEtlStatusInfo(response)
            if (display === 'none') {
                showEditDivEtl($this, $etlInfoDiv);
            }
        });
    })

    var populateEtlStatusInfo = function (response) {
        var etlRunStatus = response.result.status;
        var lastRunDate = response.result.lastRun;
        var isEnabled = response.result.enabled
        var repeatContent = " ";
        if(response.result.repeat.length){
        var i=0;
        for ( ; i < response.result.repeat.length-1; i++) {
            repeatContent += response.result.repeat[i].label + ': ' + response.result.repeat[i].value + '; ';
        }
        repeatContent += response.result.repeat[i].label + ': ' + response.result.repeat[i].value ;
        }
        $("#etlRunStatus").text(etlRunStatus);
        $("#lastSuccessfulEtl").text(lastRunDate);
        $("#etlEnabled").text(isEnabled);
        $("#etlScheduler").text(repeatContent);
    }

    function showEditDiv(parent, div) {
        var position = parent.offset();
        div.css("left", position.left -380);
        div.css("top", position.top+16);
        div.show();
    }

    function showEditDivEtl(parent, div) {
        var position = parent.offset();
        div.css("left", position.left-295);
        div.css("top", position.top+16);
        div.show();
    }

    $(document).on('mouseup', function (e) {
        var container = $("#alertPreCheckInfoDiv");
        var alertPreCheckInfoDiv = $("#alertPreCheckInfo");
        // if the target of the click isn't the container nor a descendant of the container
        if (!container.is(e.target) && container.has(e.target).length === 0 && !alertPreCheckInfoDiv.is(e.target)) {
            container.hide();
        }
        container = $("#etlInfoDiv");
        var etlStatusInfo = $("#etlStatusInfo");
        // if the target of the click isn't the container nor a descendant of the container
        if (!container.is(e.target) && container.has(e.target).length === 0 && !etlStatusInfo.is(e.target)) {
            container.hide();
        }
        container = $("#pvrCheckInfoDiv");
        var pvrCheckInfo = $("#pvrCheckInfo");
        // if the target of the click isn't the container nor a descendant of the container
        if (!container.is(e.target) && container.has(e.target).length === 0 && !pvrCheckInfo.is(e.target)) {
            container.hide();
        }
        container = $("#etlVersionAsOfCheckInfoDiv");
        var etlVersionAsOfCheckInfo = $("#etlVersionAsOfCheckInfo");
        // if the target of the click isn't the container nor a descendant of the container
        if (!container.is(e.target) && container.has(e.target).length === 0 && !etlVersionAsOfCheckInfo.is(e.target)) {
            container.hide();
        }
        container = $("#etlLatestFailureInfoDiv");
        var etlLatestFailureInfo = $("#etlLatestFailureInfo");
        // if the target of the click isn't the container nor a descendant of the container
        if (!container.is(e.target) && container.has(e.target).length === 0 && !etlLatestFailureInfo.is(e.target)) {
            container.hide();
        }
        container = $("#etlLatestInProgressInfoDiv");
        var etlLatestInProgressInfo = $("#etlLatestInProgressInfo");
        // if the target of the click isn't the container nor a descendant of the container
        if (!container.is(e.target) && container.has(e.target).length === 0 && !etlLatestInProgressInfo.is(e.target)) {
            container.hide();
        }

        container = $("#alertInProgressInfoDiv");
        var alertInProgressInfo = $("#alertInProgressInfo");
        // if the target of the click isn't the container nor a descendant of the container
        if (!container.is(e.target) && container.has(e.target).length === 0 && !alertInProgressInfo.is(e.target)) {
            container.hide();
        }

        container = $("#deletionInProgressInfoDiv");
        var deletionInProgressInfo = $("#deletionInProgressInfo");
        // if the target of the click isn't the container nor a descendant of the container
        if (!container.is(e.target) && container.has(e.target).length === 0 && !deletionInProgressInfo.is(e.target)) {
            container.hide();
        }

        container = $("#icrAutoAdjustmentInfoDiv");
        var icrAutoAdjustmentInfo = $("#icrAutoAdjustmentInfo");
        // if the target of the click isn't the container nor a descendant of the container
        if (!container.is(e.target) && container.has(e.target).length === 0 && !icrAutoAdjustmentInfo.is(e.target)) {
            container.hide();
        }

        container = $("#aggAutoAdjustmentInfoDiv");
        var aggAutoAdjustmentInfo = $("#aggAutoAdjustmentInfo");
        // if the target of the click isn't the container nor a descendant of the container
        if (!container.is(e.target) && container.has(e.target).length === 0 && !aggAutoAdjustmentInfo.is(e.target)) {
            container.hide();
        }
    });


    $(document).on("click", "#myScheduler,#futureSchedule", function () {

        var schedularHeight = ($("#futureSchedule").prop('checked'))?$("#myScheduler").css('height'):'0px';
        var schedularHeightInt = parseInt(schedularHeight.slice(0, -2))
        if(schedularHeightInt===0){
            schedularHeightInt=270;
        }else{
            schedularHeightInt += 300;
        }
        schedularHeightInt = schedularHeightInt+"px"
        $(".update_and_execute_modal").find('.modal-body').css('height', schedularHeightInt);
    })

    $(document).on('click', 'input.select-all-alert-config', function () {
            $(".copy-select").not(':disabled').prop('checked', this.checked);
            var checkboxSelector = '.copy-select';
            $.each($(checkboxSelector), function () {
                if ($(this).is(':checked')) {
                    var currentIndex = $(this).closest('tr').index();
                    var rowObject = table.rows(currentIndex).data()[0];
                    alertIdSet.add($(this).attr("data-id"));
                    var data = {};
                    data['id'] = rowObject.id;
                    data['name'] = rowObject.name;
                    selectedCasesInfo.push(data);
                } else if (!$(this).is(':checked') && alertIdSet.has($(this).attr("data-id"))) {
                    selectedCasesInfo.splice($.inArray($(this).attr("data-id"), alertIdSet), 1);
                    alertIdSet.delete($(this).attr("data-id"));
                }
            });
    });

    $(document).on('click', '.alert-check-box', function () {
        if(!this.checked){
            $('input.select-all-alert-config').prop('checked', false);
        }
    });



    //@TODO reset other options on modal close
    $("#update_and_run_form").on('submit', function (event) {
        var list = Array.from(alertIdSet);
        var $this = $(this);
        var alertNameList = getSelectedAlertsNameList()
        var alertNamesHtml = getAlertNameHtmlText($this, alertNameList)
        $('#configIdList').val(list);
        $("#toDate").attr("disabled", false);
        // if multiple checkbox is selected then call different method, also consider for evdas and literature
        const date = new Date();
        let day = date.getDate();
        let month = date.getMonth() + 1;
        let year = date.getFullYear();
        let currentDate = `${day}-${month}-${year}`;
        if ($('#futureSchedule').is(":checked") && $('#MyStartDate').val() < currentDate) {
            event.preventDefault();
            $.Notification.notify('error', 'top right', "Error", "Start Date cannot be past Date", {autoHideDelay: 5000});
        } else {
            if (list.length > 1) {
                if (alertType === "EVDAS Alert") {
                    $('#update_and_run_form').attr('action', '../evdasAlert/updateAndExecuteEvdasAlertBulk');
                } else if (alertType === "Literature Search Alert") {
                    $('#update_and_run_form').attr('action', '../literatureAlert/updateAndExecuteLitAlertBulk');
                } else {
                    $('#update_and_run_form').attr('action', '../singleCaseAlert/updateAndExecuteAlertBulk');
                }
            } else {
                if (alertType === "EVDAS Alert") {
                    $('#update_and_run_form').attr('action', '../evdasAlert/updateAndExecuteEvdasAlert');
                } else if (alertType === "Literature Search Alert") {
                    $('#update_and_run_form').attr('action', '../literatureAlert/updateAndExecuteLitAlert');
                } else {
                    $('#update_and_run_form').attr('action', '../singleCaseAlert/updateAndExecuteAlert');
                }
            }
            try {
                return checkNumberFields();
            } catch (e) {
                return true;
            }
        }
    });


    $(document).on("click", "#pvrCheckInfo", function () {
        var $infoDiv = $("#pvrCheckInfoDiv");
        var display = $infoDiv.css('display');
        //hide other 3
        $('.infoBox').hide();
        var $this = $(this);
        if(display==='none'){
            showEditDivInfo($(this),$infoDiv);
        }
    })

    $(document).on("click", "#etlVersionAsOfCheckInfo", function () {
        var $infoDiv = $("#etlVersionAsOfCheckInfoDiv");
        var display = $infoDiv.css('display');
        //hide other 3
        $('.infoBox').hide();
        var $this = $(this);
        if(display==='none'){
            showEditDivInfo($(this),$infoDiv);
        }
    })

    $(document).on("click", "#etlLatestFailureInfo", function () {
        var $infoDiv = $("#etlLatestFailureInfoDiv");
        var display = $infoDiv.css('display');
        //hide other 3
        $('.infoBox').hide();
        var $this = $(this);
        if(display==='none'){
            showEditDivInfo($(this),$infoDiv);
        }
    })

    $(document).on("click", "#etlLatestInProgressInfo", function () {
        var $infoDiv = $("#etlLatestInProgressInfoDiv");
        var display = $infoDiv.css('display');
        //hide other 3
        $('.infoBox').hide();
        var $this = $(this);
        if(display==='none'){
            showEditDivInfo($(this),$infoDiv);
            var position = $(this).offset();
            $infoDiv.css("left", position.left - 155);
        }
    })


    function showEditDivInfo(parent, div) {
        var position = parent.offset();
        div.css("left", position.left - 55);
        div.css("top", position.top + 21);
        div.show();
    }


    $(document).on('change', '.dateRangeEnumClassEvdas', function () {
        var elementId = (this.id);
        var index = elementId.replace(/[^0-9\.]+/g, "");

        dateRangeChangedActionEvdas(document, parseInt(index))
    });

    function dateRangeChangedActionEvdas(currentDocument, index) {

        var valueChanged = currentDocument.getElementById('dateRangeEnumEvdas').value;

        if (valueChanged === DATE_RANGE_ENUM.CUSTOM) {
            initializeDatePickersForEditEvdas(currentDocument, index);
            $('#evdasDateRangeSelector').show();
            populateFrequencyDatesForEvdas();
        } else if (valueChanged === DATE_RANGE_ENUM.CUMULATIVE) {
            $('#evdasDateRangeSelector').hide();
        }
    };

    function populateFrequencyDatesForEvdas(){
        var data = {
            'substanceName': substanceName,
            'isAdhocRun': false
        };
        $.ajax({
            type: "POST",
            url: substanceFrequencyPropertiesUrl,
            data: data,
        })
            .done(function (result) {
                if (result.probableStartDate && result.probableEndDate) {
                    var fromDateHtml = '<option value="null">--Select One--</option>';
                    result.probableStartDate.forEach(function (data) {
                        fromDateHtml += '<option value="' + data + '">' + data + '</option>'
                    });
                    var startDateAbsoluteCustomFreq = $("#startDateAbsoluteCustomFreq").val();
                    $("#fromDate").html(fromDateHtml);
                    $("#fromDate").val(startDateAbsoluteCustomFreq);
                    var toDateHtml = '<option value="null">--Select One--</option>';
                    result.probableEndDate.forEach(function (data) {
                        toDateHtml += '<option value="' + data + '">' + data + '</option>'
                    });
                    var endDateAbsoluteCustomFreq = $("#endDateAbsoluteCustomFreq").val();
                    $("#toDate").html(toDateHtml);
                    $("#toDate").val(endDateAbsoluteCustomFreq)
                }
            });
    }

    function initializeDatePickersForEditEvdas(currentDocument, index) {
        var from = null;
        var to = null;

        if ($('#dateRangeStartAbsolute').val()) {
            from = $('#dateRangeStartAbsolute').val();
        }
        if ($('#dateRangeStartAbsolute').val()) {
            to = $('#dateRangeStartAbsolute').val();
        }

        $('#datePickerFromDiv').datepicker({
            allowPastDates: true,
            date: from,
            formatDate: function (date) {
                return setDefaultDisplayDateFormat(date);
            }
        }).on('changed.fu.datepicker', function (evt, date) {
            from = date;
            // when input is changed directly
            updateFields();
        }).on('click', function (evt) {
            from = $('#datePickerFromDiv').datepicker('getDate');
            updateFields();
        });

        $('#datePickerToDiv').datepicker({
            allowPastDates: true,
            date: to,
            formatDate: function (date) {
                return setDefaultDisplayDateFormat(date);
            }
        }).on('changed.fu.datepicker', function (evt, date) {
            to = date;
            updateFields();
        }).on('click', function () {
            to = $('#datePickerToDiv').datepicker('getDate');
            updateFields();
        });

        var updateFields = function () {
            $('#dateRangeStartAbsolute').val(renderDateWithTimeZone(from));
            $('#dateRangeEndAbsolute').val(renderDateWithTimeZone(to));
        };

        function renderDateWithTimeZone(date) {
            var parseDate = moment(date).tz(userTimeZone).format(DATE_FMT_TZ);
            return parseDate;
        }

        updateFields();
    }

    populateEtlStatusDetails();

    function populateEtlStatusDetails() {
        var url = "/signal/etlSchedule/etlInfo";
        $.ajax({
            type: "GET", url: url, dataType: 'json'
        }).done(populateEtlStatusInfo)
    }

    $(document).on('click', "#alertInProgressInfo", function (event) {
        var $infoDiv = $("#alertInProgressInfoDiv");
        var display = $infoDiv.css('display');
        var $this = $(this);
        if (display === 'none') {
            showStatusDivInfo($(this), $infoDiv);
        }
    })

    $(document).on('click', "#deletionInProgressInfo", function (event) {
        var $infoDiv = $("#deletionInProgressInfoDiv");
        var display = $infoDiv.css('display');
        var $this = $(this);
        if (display === 'none') {
            showStatusDivInfo($(this), $infoDiv);
        }
    })

    $(document).on('click', "#icrAutoAdjustmentInfo", function (event) {
        var adjustmentRule = $("#icrAutoAdjustmentRule").val()
        var infoDivHtml = getAutoAdjustmentInfoDivText(adjustmentRule)
        $("#icrAutoAdjustmentInfoDivValue").html(infoDivHtml)
        var $infoDiv = $("#icrAutoAdjustmentInfoDiv");
        var display = $infoDiv.css('display');
        if (display === 'none') {
            showAutoAdjustmentInfo(adjustmentRule, $infoDiv)
        }
    })

    $(document).on('click', "#aggAutoAdjustmentInfo", function (event) {
        var adjustmentRule = $("#aggregateAutoAdjustmentRule").val()
        var infoDivHtml = getAutoAdjustmentInfoDivText(adjustmentRule)
        $("#aggAutoAdjustmentInfoDivValue").html(infoDivHtml)
        var $infoDiv = $("#aggAutoAdjustmentInfoDiv");
        var display = $infoDiv.css('display');
        if (display === 'none') {
            showAutoAdjustmentInfo(adjustmentRule, $infoDiv)
        }
    })

    function showAutoAdjustmentInfo(adjustmentRule, $infoDiv) {
        if (adjustmentRule == "ALERT_PER_SKIPPED_EXECUTION") {
            $infoDiv.css("left", 863);
            $infoDiv.css("top", 320);
            $infoDiv.css("width", 325);
            $infoDiv.css("height", 150);
        } else if (adjustmentRule == "SINGLE_ALERT_FOR_ALL_SKIPPED_EXECUTION") {
            $infoDiv.css("left", 760);
            $infoDiv.css("top", 272);
            $infoDiv.css("width", 430);
            $infoDiv.css("height", 225);
        } else if (adjustmentRule == "MANUAL_AUTO_ADJUSTMENT") {
            $infoDiv.css("left", 900);
            $infoDiv.css("top", 360);
            $infoDiv.css("width", 285);
            $infoDiv.css("height", 90);
        }
        $infoDiv.show();
    }

    function getAutoAdjustmentInfoDivText(adjustmentRule) {
        var infoDivHtml = ""
        if (adjustmentRule == "ALERT_PER_SKIPPED_EXECUTION") {
            infoDivHtml = ` <p style="display: inline;">All the skipped execution of the alert will be auto executed, if the alert is configured with “Evaluate Case Date On” as “Latest Version” then “Evaluate Case Date On” will be converted to “As of Version” with the below logic:
            <br> <i>(Date of the skipped Execution -1)</i> will be used as the As of Date.</p>`
        } else if (adjustmentRule == "SINGLE_ALERT_FOR_ALL_SKIPPED_EXECUTION") {
            infoDivHtml = `<p style="display: inline;">Single Alert will be auto executed for all the skipped execution, the date range of the alert will be updated based on the below logic:
            <br> <b>Start Date:</b> Start date for the first expected alert execution impacted due to ETL issue.
            <br> <b>End Date:</b>  End date for the latest expected alert execution.
            <br> <b>Evaluate Case Date On:</b> If the alert is configured with “Evaluate Case Date On” as “As Of Version” then “As Of Version” date for the latest expected alert execution will be used in the alert configuration OR if the alert is configured with “Evaluate Case Date On” as “Latest Version” then “Latest Version“ will be used in the alert configuration.</p>`
        } else if (adjustmentRule == "MANUAL_AUTO_ADJUSTMENT") {
            infoDivHtml = `<p style="display: inline;">Alert will not auto-execute in case if there are any skipped execution, the user has to manually update the date range for all the impacted alert(s).</p>`
        }
        return infoDivHtml
    }

    function showStatusDivInfo(parent, div) {
        var position = parent.offset();
        div.css("left", position.left - 150);
        div.css("top", position.top + 40);
        div.show();
    }

    $(document).on('click', '#save-alert-pre-check-values', function (event) {
        event.preventDefault();
        bootbox.confirm({
            title: ' ',
            message: "The updated configuration will be applicable only for those alerts which are planned to be executed post this configuration update. Do you want to proceed?",
            buttons: {
                confirm: {
                    label: 'Update', className: 'btn-primary'
                }, cancel: {
                    label: 'Cancel', className: 'btn-default'
                }
            }, callback: function (result) {
                if (result) {
                    if(!$("#save-alert-pre-check-values").prop('disabled')){
                        var data = {};
                        data.pvrCheck = $("#pvrCheck").prop('checked');
                        data.versionAsOf = $("#versionAsOf").prop('checked');
                        data.etlFailure = $("#etlFailure").prop('checked');
                        data.etlInProgress = $("#etlInProgress").prop('checked');
                        data['Single Case Alert'] = $("#icrAutoAdjustmentRule").val();
                        data['scaAutoRuleEnabled'] = $("#singleCaseAutoAdjustmentEnabled").prop('checked');
                        data['Aggregate Case Alert'] = $("#aggregateAutoAdjustmentRule").val();
                        data['aggAutoRuleEnabled'] = $("#aggregateAutoAdjustmentEnabled").prop('checked');
                        var url = "/signal/alertAdministration/updateAlertPreCheck";
                        $.ajax({
                            type: "GET",
                            url: url,
                            data: data,
                            dataType: 'json',
                        })
                            .done(function (response) {
                                var isEnabledValue = (response.data.isPvrCheckEnabled || response.data.isVersionAsOfCheckEnabled || response.data.isEtlInProgressCheckEnabled || response.data.isEtlFailureCheckEnabled) ? "Enabled" : "Disabled";
                                $("#preChecksStatusDiv").text(isEnabledValue)
                                var alertPreCheckModal = $(".alert-pre-execution-check-modal");
                                alertPreCheckModal.modal("hide");
                            });
                    }
                } else {
                    event.preventDefault();
                }
            }
        });

    });

    $(document).on('click', '.disable-alert-button', function (event) {
        event.preventDefault();
        var list = Array.from(alertIdSet);
        var $this = $(this);
        var alertNameList = getSelectedAlertsNameList()
        var alertNamesHtml = getAlertNameHtmlText($this, alertNameList)
        var configId = $(this).attr('data-instanceid');
        var configIdList = list.length > 0 ? list.join() : configId
        bootbox.confirm({
            title: 'Disable Alert', message: "Are you sure you want to disable the Alert(s)?<p style='font-weight: bolder;'>"+alertNamesHtml+"Justification</p><textarea id='disableAlertJustification' cols='60' rows='6' name='disableAlertJustification' style='height: 60px;" + "width: 539px;border-radius: 5px;'></textarea>",
            buttons: {
                confirm: {
                    label: 'Disable Alert(s)', className: 'btn-primary'
                }, cancel: {
                    label: 'Cancel', className: 'btn-default'
                }
            }, callback: function (result) {
                if (result) {
                    removeExistingMessageHolder();
                    var data = {};
                    data.alertType = alertType
                    data.configIdList = configIdList
                    data.justification = $('#disableAlertJustification').val()
                    var url = CONFIGURATION.configuration_disable_url;
                    $.ajax({
                        type: "POST", url: url, data: data, dataType: 'json',
                    })
                        .done(function (response) {
                            if (response.status) {
                                $.Notification.notify('success', 'top right', "Success", response.message, {autoHideDelay: 5000});
                                prev_page = []
                                alertIdSet.clear();
                                selectedCasesInfo = []
                                $(".select-all-alert-config").prop('checked', false);
                                table.ajax.reload()
                            } else {
                                if (response.code == 1) {
                                    showErrorMsg(response.message)
                                } else if (response.code == 2) {
                                    $.Notification.notify('error', 'top right', "Error", response.message, {autoHideDelay: 5000});
                                }
                            }
                        });
                } else {
                    event.preventDefault();
                }
            }
        });
        addCountBoxToInputField(8000,$('#disableAlertJustification'));

    });

    $(document).on('click', '.enable-alert-button', function (event) {
        event.preventDefault();
        var list = Array.from(alertIdSet);
        var $this = $(this);
        var alertNameList = getSelectedAlertsNameList()
        var alertNamesHtml = getAlertNameHtmlText($this, alertNameList)
        var configId = $(this).attr('data-instanceid');
        var configIdList = list.length > 0 ? list.join() : configId;
        var data = {};
        data.alertType = alertType
        data.configIdList = configIdList

        bootbox.confirm({
            title: 'Enable Alert',
            message: "Are you sure you want to enable the Alert(s)?<p style='font-weight: bolder;'>" + alertNamesHtml + "Justification</p><textarea id='enableAlertJustification' cols='60' rows='6' name='enableAlertJustification' style='height: 60px;" + "width: 539px;border-radius: 5px;'></textarea>",
            buttons: {
                confirm: {
                    label: 'Enable Alert(s)', className: 'btn-primary'
                }, cancel: {
                    label: 'Cancel', className: 'btn-default'
                }
            },
            callback: function (result) {
                if (result) {
                    removeExistingMessageHolder();
                    var data = {};
                    data.alertType = alertType
                    data.configIdList = configIdList
                    data.justification = $('#enableAlertJustification').val()
                    var url = CONFIGURATION.configuration_enable_url;
                    $.ajax({
                        type: "POST", url: url, data: data, dataType: 'json',
                    })
                        .done(function (response) {
                            if (response.status) {
                                $.Notification.notify('success', 'top right', "Success", response.message, {autoHideDelay: 5000});
                                prev_page = []
                                alertIdSet.clear();
                                selectedCasesInfo = []
                                $(".select-all-alert-config").prop('checked', false);
                                table.ajax.reload()
                            } else {
                                if (response.code == 1) {
                                    showErrorMsg(response.message)
                                } else if (response.code == 2) {
                                    $.Notification.notify('error', 'top right', "Error", response.message, {autoHideDelay: 5000});
                                }
                            }
                        });
                } else {
                    event.preventDefault();
                }
            }
        });
        addCountBoxToInputField(8000,$('#enableAlertJustification'));
    });

    function getSelectedAlertsNameList() {
        var alertNameList = []
        for(var i = 0; i< selectedCasesInfo.length;i++){
            alertNameList.push(selectedCasesInfo[i]["name"]);
        }
        return alertNameList
    }

    function getAlertNameHtmlText(parent,alertNameList) {
        var alertName = parent.attr('alert-name');
        var alertNamesHtml = ""
        alertNameList.forEach((alertName) => {
            alertNamesHtml += alertName + "<br>"
        });
        alertNamesHtml = alertNameList.length > 0 ? alertNamesHtml : alertName
        alertNamesHtml += "<br></br>"
        return alertNamesHtml
    }

    $(document).on('click', '.delete-latest-alert-button, .delete-all-alert-button', function (event) {
        event.preventDefault();
        var list = Array.from(alertIdSet);
        var deleteChildAlertSiblings = false

        var $this = $(this);
        var alertNameList = getSelectedAlertsNameList()
        var alertNamesHtml = getAlertNameHtmlText($this, alertNameList)
        var configId = $(this).attr('data-instanceid');
        var configIdList = list.length > 0 ? list.join() : configId
        var deleteLatest = $(this).attr('deleteLatest')

        var requestData = {};
        requestData.configIdList = configIdList
        requestData.deleteLatest = deleteLatest
        requestData.alertType = alertType

        if (alertType == "Aggregate Case Alert") {
            var data = {};
            data.configIdList = configIdList
            var childConfigPreCheckUrl = "/signal/alertAdministration/fetchDataForChildAlertDeletion"
            $.ajax({
                type: "GET", url: childConfigPreCheckUrl, data: data, dataType: 'json',
            })
                .done(function (response) {
                    if (response.status) {
                        var masterChildData=response.data["MASTER_CHILD_DATA"]
                        if (Object.keys(masterChildData).length > 0) {
                            var childConfigs = response.data["CHILD_CONFIGS"]
                            var childConfigIds = Object.keys(childConfigs)
                            var childConfigNames = Object.values(childConfigs)

                            var siblingConfigs=response.data["SIBLING_CONFIGS"]
                            var siblingConfigIds=Object.keys(siblingConfigs).join()
                            var siblingConfigNames=Object.values(siblingConfigs).join()

                            var masterConfigIds = Object.keys(response.data["MASTER_CHILD_DATA"]).join()
                            var messaage = "Do you also want to delete all the child alerts generated with :" + "<b>" + childConfigNames + " ?<b><br>"
                            openMasterChildPreCheckPopUp(messaage, masterConfigIds,childConfigIds,masterChildData,siblingConfigIds)
                        } else {
                            openDeletePopUp()
                        }
                    } else {
                        $.Notification.notify('error', 'top right', "Error", response.message, {autoHideDelay: 5000});
                    }
                });
        } else {
            openDeletePopUp()
        }

        function openDeletePopUp(childNames) {
            var label = alertRunType == "Scheduled" ? "Unschedule & Delete" : "Delete"
            if (typeof childNames == "undefined" || childNames == "") {
                childNames = alertNamesHtml
            }
            else {
                childNames += '<br>' + alertNamesHtml
            }
            bootbox.confirm({
                title: 'Delete Alert',
                message: "Are you sure you want to delete the Alert(s)?" + "<p style='font-weight: bolder;'>" + childNames + "<br><br>Justification<p><textarea id='deleteAlertJustification' cols='60' rows='6' name='deleteAlertJustification' class ='deleteAlertJustification' style='height: 60px;" + "width: 539px;border-radius: 5px;'></textarea>",
                buttons: {
                    confirm: {
                        label: '<span class="glyphicon glyphicon-trash icon-white"></span> '+label, className: 'btn btn-danger', id: 'deleteButton'
                    }, cancel: {
                        label: 'Cancel', className: 'btn-default'
                    }
                },
                callback: function (result) {
                    if (result) {
                        requestData.justification = $('#deleteAlertJustification').val()
                        var url = CONFIGURATION.delete_alert_url
                        $.ajax({
                            type: "POST", url: url, data: requestData, dataType: 'json',
                        })
                            .done(function (response) {
                                removeExistingMessageHolder();
                                if (response.status) {
                                    if (response.data[SUCCESS]) {
                                        var message = `${response.message} <br> ` + response.data[SUCCESS].toString()
                                        $.Notification.notify('success', 'top right', "Success", message, {autoHideDelay: 5000});
                                    }
                                    if (response.data[PRE_REQUISITE_FAIL]) {
                                        var discardedAlerts = response.data[PRE_REQUISITE_FAIL]
                                        for (let i = 0; i < discardedAlerts.length; i++) {
                                            let deleteLatest = discardedAlerts[i]["deleteLatest"]
                                            let alertId = discardedAlerts[i]["id"]
                                            let alertName = discardedAlerts[i]["alertName"]
                                            let linkedSignalsCount = discardedAlerts[i]["linkedSignalsCount"]
                                            var message = `Cannot delete: ${alertName} is used in ${linkedSignalsCount} Signal(s). <a href ="/signal/alertAdministration/linkedSignals?configId=${alertId}&alertType=${alertType}&deleteLatest=${deleteLatest}" target="_blank" class="linked-signals-info-icon2" data-instanceid="${alertId}">See Details</a>`
                                            showErrorMsg(message)
                                        }
                                    }
                                    prev_page = []
                                    alertIdSet.clear();
                                    selectedCasesInfo = []
                                    $(".select-all-alert-config").prop('checked', false);
                                    table.ajax.reload()
                                } else {
                                    if (response.code == 1) {
                                        showErrorMsg(response.message)
                                    } else if (response.code == 2) {
                                        $.Notification.notify('error', 'top right', "Error", response.message, {autoHideDelay: 5000});
                                    }
                                }
                            });
                    }
                }
            });
            addCountBoxToInputField(8000,$('.deleteAlertJustification'));
        }

        function openMasterChildPreCheckPopUp(message, masterConfigIds, childConfigIds, masterChildData,siblingConfigIds) {
            bootbox.confirm({
                title: 'Child Alert Deletion', message: message, buttons: {
                    cancel: {
                        label: 'No', className: 'btn-default'
                    }, confirm: {
                        label: 'Yes', className: 'btn-default'
                    }
                }, callback: function (result) {
                    if (result) {

                        requestData.configIdList = siblingConfigIds
                        requestData.deleteChildAlertSiblings = true
                        requestData.masterIdList = masterConfigIds
                    } else {
                        requestData.configIdList = configIdList
                        requestData.deleteChildAlertSiblings = false
                    }
                    var siblingConfigNames = Object.values(masterChildData)
                    var childNames = ""
                    siblingConfigNames.forEach(function (data) {
                        $.grep(data["siblingConfigNames"], function(value) {
                            return $.inArray(value, data["configNames"]) < 0;
                        }).toString().split(",").forEach(function (value) {
                                childNames = (value ) ? (childNames + " <br>" + value) : "";
                        })
                    });
                    openDeletePopUp(childNames);
                }
            });
        }
    });


    function getErrorMessageHtml(msg) {
        var alertHtml = '<div class="alert alert-danger alert-dismissible" role="alert"> ' + '<button type="button" class="close" data-dismiss="alert"> ' + '<span aria-hidden="true">&times;</span> ' + '<span class="sr-only"><g:message code="default.button.close.label" /></span> ' + '</button> ' + msg;
        '</div>';
        return alertHtml;
    }

    function getSuccessMessageHtml(msg) {
        var alertHtml = '<div class="alert alert-success alert-dismissible" role="alert"> ' + '<button type="button" class="close" data-dismiss="alert"> ' + '<span aria-hidden="true">&times;</span> ' + '<span class="sr-only"><g:message code="default.button.close.label" /></span> ' + '</button> ' + msg;
        '</div>';
        return alertHtml;
    }

    function showErrorMsg(msg) {
        var alertHtml = getErrorMessageHtml(msg);
        $('body .messageContainer').prepend(alertHtml);
        $('body').scrollTop(0);
    }

    function showSuccessMsg(msg) {
        var alertHtml = getSuccessMessageHtml(msg);
        $('body .messageContainer').prepend(alertHtml);
        $('body').scrollTop(0);
    }

    function removeExistingMessageHolder() {
        $('.messageContainer').html("");
    }

    $(document).on('click', '#runNow, #futureSchedule', function (event) {
        // update the text of updateAndExecute InfoDiv
        populateUpdateAndExecuteInfoDiv()
    });

    function populateUpdateAndExecuteInfoDiv() {

        $("#update-and-run-alert").prop('disabled', false);

        if ($("#runNow").prop('checked') && $("#futureSchedule").prop('checked')) {
            $("#updateAndExecuteInfoDiv").text("On selecting \"Run Now\" with \"Future Schedule\", the system will perform the first execution on the current date/time and the next execution will be performed on the scheduled date/time")
        } else if ($("#runNow").prop('checked')) {
            $("#updateAndExecuteInfoDiv").text("On selecting \"Run Now\" the system will start executing the alert on the current date/time")
        } else if ($("#futureSchedule").prop('checked')) {
            $("#updateAndExecuteInfoDiv").text("On selecting \"Future Schedule\", the system will perform the first execution on the scheduled date/time")
        } else {
            $("#updateAndExecuteInfoDiv").text("")
            $("#update-and-run-alert").prop('disabled', true);
        }

    }
    $('.datepicker-wheels-month ul li').children().on('click', function (e) {
        var $this = $(this);
        $this.closest('ul').find( '.selected' ).removeClass( 'selected' );
        $( e.currentTarget ).parent().addClass( 'selected' );
    });

});

