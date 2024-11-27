//= require app/pvs/common/rx_common.js
//= require app/pvs/common/rx_list_utils.js
//= require app/pvs/common/rx_handlebar_ext.js

$(function () {
    var alertName
    var build_url_for_config = function (data) {
        var returnObj = new Object();
        returnObj.run_display = $.i18n._('run');
        returnObj.view_display = $.i18n._('view');
        returnObj.edit_display = $.i18n._('edit');
        returnObj.copy_display = $.i18n._('copy');
        returnObj.delete_display = $.i18n._('delete');
        returnObj.obj_id = data.id;
        returnObj.type = data.type;
        returnObj.edit_url = CONFIGURATION.editUrl + "/" + data.id;
        returnObj.run_url = CONFIGURATION.runUrl + '/' + data.id;
        returnObj.view_url = CONFIGURATION.viewUrl + '/' + data.id;
        returnObj.copy_url = CONFIGURATION.copyUrl + '/' + data.id;
        returnObj.isEvdas = false;
        returnObj.isLiterature = false;
        returnObj.isEdit = data.isEdit;
        returnObj.isView = data.isView;
        returnObj.isDelete = data.isDelete;
        returnObj.isRun = data.isRun;
        returnObj.showEdit = true;
        returnObj.readOnly = data.readOnly?? true;

        switch (data.type) {
            case 'Individual Case Configuration':
                returnObj.run_display = $.i18n._('run');
                returnObj.edit_url = CONFIGURATION.sca_edit_url + "/" + data.id;
                returnObj.run_url = CONFIGURATION.runUrl + '?id=' + data.id + '&type=' + data.type;
                returnObj.view_url = CONFIGURATION.sca_view_url + '/' + data.id;
                returnObj.copy_url = CONFIGURATION.sca_copy_url + '/' + data.id;
                returnObj.isAdhocRun = data.isAdhocRun;
                break;
            case 'Aggregate Configuration':
                returnObj.run_display = $.i18n._('run');
                returnObj.edit_url = CONFIGURATION.aga_edit_url + "/" + data.id;
                returnObj.run_url = CONFIGURATION.runUrl  + '?id=' + data.id + '&type=' + data.type;
                returnObj.view_url = CONFIGURATION.aga_view_url + '/' + data.id;
                returnObj.copy_url = CONFIGURATION.aga_copy_url + '/' + data.id;
                returnObj.isAdhocRun = data.isAdhocRun;
                returnObj.masterConfigId = data.masterConfigId;
                returnObj.showEdit = (data.masterConfigId == null && data.unscheduled == false);
                returnObj.isRun = data.isMasterTemplateAlert ? false : (data.unscheduled == false && data.isRunnable == true)
                break;
            case 'EVDAS Configuration':
                returnObj.run_display = $.i18n._('run');
                returnObj.edit_url = CONFIGURATION.evdas_edit_url + "/" + data.id;
                returnObj.run_url = CONFIGURATION.evdas_run_url  + '?id=' + data.id ;
                returnObj.view_url = CONFIGURATION.evdas_view_url + '/' + data.id;
                returnObj.copy_url = CONFIGURATION.evdas_copy_url + '/' + data.id;
                returnObj.delete_url = CONFIGURATION.evdas_delete_url + '/' + data.id;
                returnObj.isEvdas = true;
                returnObj.isAdhocRun = data.isAdhocRun;
                break;
            case 'Ad-Hoc Alert':
                returnObj.run_display = $.i18n._('run');
                returnObj.edit_url = CONFIGURATION.adha_edit_url + "/" + data.id;
                returnObj.view_url = CONFIGURATION.adha_view_url + '/' + data.id;
                returnObj.copy_url = CONFIGURATION.adha_copy_url + '/' + data.id;
                returnObj.isAdhocRun = false;
                break;
            case 'Literature Configuration':
                returnObj.run_display = $.i18n._('run');
                returnObj.edit_url = CONFIGURATION.literature_edit_url + "/" + data.id;
                returnObj.run_url = CONFIGURATION.literature_run_url  + '?id=' + data.id + '&type=' + data.type;
                returnObj.view_url = CONFIGURATION.literature_view_url + '/' + data.id;
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
    alertName = $('input[name="relatedResults"]:checked').val();
    $(".viewAlertRadio").on('change', function () {
        alertName = $('input[name="relatedResults"]:checked').val();
        $('#rxTableConfiguration').DataTable().ajax.url(CONFIGURATION.listUrl+"?alertType="+alertName).load()
    });

    var table = $('#rxTableConfiguration').DataTable({
        "sPaginationType": "full_numbers",
        "language": {
            "url": "../assets/i18n/dataTables_" + userLocale + ".json"
        },
        drawCallback: function (settings) {
            colEllipsis();
            webUiPopInit();
            var rowsDataAR = $('#rxTableConfiguration').DataTable().rows().data();
            if(typeof settings.json !== 'undefined') {
                showTotalPage($('#rxTableConfiguration_wrapper'), settings.json.recordsFiltered);
            }else {
                pageDictionary($('#rxTableConfiguration_wrapper'), rowsDataAR.length);
                showTotalPage($('#rxTableConfiguration_wrapper'), rowsDataAR.length);
            };
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
            $('.yadcf-filter-wrapper').hide();
            $('#rxTableConfiguration').DataTable().draw();

        },
        processing: true,
        "ajax": {
            "url": CONFIGURATION.listUrl + "?alertType=" + alertName,
            "dataSrc": "aaData"
        },
        "serverSide":true,
        "aaSorting": [[4, "desc"]],
        "bLengthChange": true,
        "iDisplayLength": 50,
        "bAutoWidth": true,
        "aLengthMenu": [[50, 100, 200, 500], [50, 100, 200, 500]],
        "aoColumns": [
            {
                "mData": "name",
                "mRender": function (data, type, row) {
                    return '<div style="white-space: pre">'+ addEllipsisForDescriptionText((row.name)) +'</div>'
                },
                "className": 'col-min-150 col-max-150 cell-break'
            },
            {
                "mData": "description",
                "mRender": function (data, type, row) {
                    if (row.description) {
                        return "<span >" + addEllipsisForDescriptionText((row.description))+ "</span>"
                    } else {
                        return "-"
                    }
                },
                "className": 'col-min-200 col-max-200 cell-break word-break textPre',
                "type": 'string'
            },
            {
                "mData": "noOfExecution",
                "className": 'col-min-100 col-max-100',
                "type": 'string'
            },
            {
                "mData": "dateCreated",
                "aTargets": ["dateCreated"],
                "sClass": "col-min-150 col-max-150",
                "mRender": function (data, type, full) {
                    return moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT);
                },
                "type": 'string'
            }, {
                "mData": "lastUpdated",
                "aTargets": ["lastUpdated"],
                "sClass": "col-min-150 col-max-150",
                "mRender": function (data, type, full) {
                    return moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT);
                },
                "type": 'string'
            },
            {"mData": "createdBy", "className": 'col-min-100 col-max-100'},
            {
                "mData": null,
                "sortable": false,
                "aTargets": ["id"],
                "mRender": function (data, type, row) {
                    var actionButtonContent = signal.utils.render('alert_config_edit_button_5.5', build_url_for_config(row));
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
            topStart: 'search',
            topEnd: null,
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
        scrollY: '50vh',
    });

    var init_filter = function (data_table) {
        yadcf.init(data_table, [
            {column_number: 0, filter_type: 'text', filter_reset_button_text: false,filter_delay: 600,},
            {column_number: 1, filter_type: 'text', filter_reset_button_text: false, filter_delay: 600,},
            {column_number: 2, filter_type: "text", filter_reset_button_text: false, filter_delay: 600,},
            {column_number: 3, filter_type: "text", filter_reset_button_text: false, filter_delay: 600,},
            {column_number: 4, filter_type: "text", filter_reset_button_text: false, filter_delay: 600,},
            {column_number: 5, filter_type: "text", filter_reset_button_text: false, filter_delay: 600,},
        ]);
        $(".yadcf-filter").removeAttr('placeholder');
    };


    actionButton('#rxTableConfiguration');
    loadTableOption('#rxTableConfiguration');

    $( window ).on('unload', function() {
        $("input[name='relatedResults']:first").prop('checked', true);
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
            bootbox.confirm({
            title: 'Run Alert ',
            message: "Are you sure want to run this Alert?",
            buttons: {
                confirm: {
                    label: 'Run Alert',
                    className: 'btn-primary'
                },
                cancel: {
                    label: 'Cancel',
                    className: 'btn-default'
                }
            },
            callback: function (result) {
                if (result) {
                    window.location.href = href
                } else {
                    event.preventDefault();
                }
            }
        });
        }
    });

});

