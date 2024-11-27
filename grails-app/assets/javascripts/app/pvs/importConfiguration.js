X_OPERATOR_ENUMS = {
    LAST_X_DAYS: 'LAST_X_DAYS',
    LAST_X_WEEKS: 'LAST_X_WEEKS',
    LAST_X_MONTHS: 'LAST_X_MONTHS',
    LAST_X_YEARS: 'LAST_X_YEARS'
};
var table;
var importLogTable;
var currentEditingRow;
$(function () {

    $("#alertType").select2({placeholder: $.i18n._("selectOne"), allowClear:true});
    $('#createFromSetupTemplate').on('click', function () {
        var alertType = $('#alertType').val();
        var templateAlertId = $('#templatesAlert').val();
        if (templateAlertId) {
            $.ajax({
                type: "GET",
                url: createAlertFromTemplate + '?alertType=' + alertType + '&templateAlertId=' + templateAlertId
            })
                .done(function (result) {
                    if (result.status == true) {
                        $.Notification.notify('success', 'top right', "Success", "Alert Created Successfully.", {autoHideDelay: 5000});
                        table.ajax.reload();
                    } else {
                        $.Notification.notify('error', 'top right', "Error", "An Error occurred while Processing request.", {autoHideDelay: 5000});
                    }
            });
        } else {
            $.Notification.notify('warning', 'top right', "Warning", "Select a value from Setup From Template.", {autoHideDelay: 5000});
        }
    });
    $('#alertType').on('change', function () {

        $(".template-alert-class").each(function () {
            $(this).remove();
        });
        $('#importConfTypeId').val( $('#alertType').val());
        getAlertTemplates()
        table.ajax.url(fetchImportConfigListURL + '?alertType=' + $('#alertType').val()).load();
    });

    $("#importConfigurationIconMenu").mouseover(function () {
        $(".ul-ddm").show();
    });
    $("#importConfigurationIconMenu").mouseout(function() {
        $(".ul-ddm").hide();
    });

    function getAlertTemplates() {
        $.ajax({
            type: "GET",
            url: selectTemplatesUrl + '?alertType=' + $('#alertType').val()
        })
            .done(function (result) {
                var options = "<option value=''></option>\n";
                _.each(result, function (data_item) {
                    options += "<option class='template-alert-class' value='" + data_item.id + "'>" + replaceBracketsAndQuotes(data_item.name) + "</option>\n";
                });
                $("#templatesAlert").html(options);
                $("#templatesAlert").select2({placeholder: $.i18n._("selectOne"),allowClear: true});
        });
    }

    getAlertTemplates()
    table = $('#rxTableImportConfigurationList').DataTable({

        processing: true,
        serverSide: true,
        "stateSave": true,
        "stateDuration": -1,
        "language": {
            "url": "../assets/i18n/dataTables_" + userLocale + ".json",
            "searchPlaceholder": $.i18n._("Search")
        },
        "oLanguage": {
            "oPaginate": {
                "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
            },
        },
        "ajax": {
            "url": fetchImportConfigListURL + '?alertType=' + $('#alertType').val(),
            "dataSrc": "data",
            "data": {}
        },
        "aaSorting": [],
        "order": [],
        columnDefs: [
            {width: "30", targets: 0},
            {
                "targets": '_all',
                orderSequence: ['desc', 'asc']
            }
        ],
        "bLengthChange": true,
        "iDisplayLength": 25,
        "aLengthMenu": [[25, 50, 100, 200, 500], [25, 50, 100, 200, 500]],
        "aoColumns": [
            {
                "className": "dataTableColumnCenter isTemplateCell",
                "mData": "isTemplateAlert",
                "sortable": false,
                mRender: function (data, type, row) {
                    return (data ? formStatusCell("template") : (row.status ? formStatusCell("scheduled") : formStatusCell("unscheduled")))
                }
            }, {
                "className": "alertNameCell",
                "mData": "name",
                mRender: function (data, type, row) {
                    return formAlertNameCell(row.id, data ,row.isEditableAlert);
                }
            },{
                "className": "masterConfigCell",
                "mData": "masterConfiguration",
                mRender: function (data, type, row) {
                    return '<span style="word-wrap:break-word;">'+row.masterConfiguration+'<span>';
                }
            }, {
                "className": "configurationTemplateCell col-min-150 col-max-150",
                "mData": "configurationTemplate",
                "sortable": false,
                mRender: function (data, type, row) {
                    var templateName = row?.configurationTemplateName ? row?.configurationTemplateName : '';
                    return "<span class='configurationTemplate' style='word-wrap:break-word;' id='" + row?.configurationTemplate + "'>" + templateName + "</span>";

                }
            },
            {
                "className": "productCell",
                "mData": "products",
                "sortable": false,
                "mRender": function (data, type, row) {
                    var configProd = row.productSelection ? row.productSelection :''
                    var db = row.selectedDatasource
                    var configProdGroup = row.productGroupSelection ? row.productGroupSelection :''
                    return '<span class="showProductDictionary word-wrap-break-word">' + escapeAllHTML(data) + '</span>' +
                        '<div class="row hide selectProductDictionary">' +
                        '<div class="wrapper">' +
                        "<input type='hidden' name='productSelectionAssessment' id='productSelectionAssessment' value='" + escapeAllHTML(configProd) + "'/>" +
                        "<input type='hidden' name='productGroupSelectionAssessment' id='productGroupSelectionAssessment' value='" + escapeAllHTML(configProdGroup) + "'/>" +
                        "<input type='hidden' name='selectedDatasource' id='selectedDatasource' class='selectedDatasource' value='" + db + "'/>" +
                        "<input type='hidden' name='isMultiIngredientAssessment' id='isMultiIngredientAssessment' value='"+ row.isMultiIngredient +"'/>" +
                        '<div id="showProductSelectionAssessment" class="showDictionarySelection">' +
                        '<div style="padding: 1px;">' + escapeAllHTML(row?.products) + '</div>' +
                        '</div>' +
                        '<div class="iconSearch">' +
                        '<a data-toggle="modal" id="searchProductsAssessment" data-target="#productModalAssessment" tabindex="0" data-toggle="tooltip" title="Search product"  data-ismultiingredient = "'+row.isMultiIngredient+'"class="productRadio">' +
                        '<i class="fa fa-search"></i></a>' +
                        '</div>' +
                        '</div>' +
                        '</div>'
                }
            },
            {
                "className": "selectedDataSheet",
                "mData": "selectedDataSheet",
                "sortable": false,
                mRender: function (data, type, row) {
                    return "<span class='datasheets word-wrap-break-word'" + "data-sheetType='"+row.datasheetType+"'"+"data-selectedDatasource='"+row.selectedDatasource+"'"+
                        " data-id='" + row?.id + "'" + " data-selectedDataSheet='" + row?.dataSheetData + "'>" +  row?.selectedDataSheet +"</span>"
                }
            },
            {
                "className": "dateRangeCell",
                "mData": "dateRangeType",
                "sortable": false,
                 mRender: function (data, type, row) {
                    return "<span class='dateRangeType'" +
                        " data-id='" + row?.id + "'" + "data-selectedDataSource='"+row?.selectedDatasource+"'"+" data-dateRangeEnum='" + row?.alertDateRangeInformation?.dateRangeEnum?.name + "'" +
                        " data-relativeDateRangeValue='" + row.alertDateRangeInformation?.relativeDateRangeValue + "'" +
                        " data-dateRangeStartAbsolute='" + row?.alertDateRangeInformation?.dateRangeStart + "'" +
                        " data-dateRangeEndAbsolute='" + row?.alertDateRangeInformation?.dateRangeEnd + "'>" +
                        row?.dateRangeType + "</span>" +
                        "<Input type='hidden' class='EvaluateDateAsClass' data-evaluateDateAs='" + row.evaluateDateAs + "' data-asOfVersionDate='" + row.asOfVersionDate + "'/>"
                        ;
                }
            },
            {
                "className": "assignedToCell",
                "mData": "assignedTo",
                "sortable": false,
                mRender: function (data, type, row) {
                    var assignedToValueID = row?.isAutoAssignedTo?"AUTO_ASSIGN":(row?.assignedTo ? row?.assignedTo?.id : row?.assignedToGroup?.id)
                    var assignedToValue = row?.isAutoAssignedTo?"Auto Assign": (row?.assignedTo ? row?.assignedTo?.fullName : row?.assignedToGroup?.name)
                    return "<span class='assignedToClass word-wrap-break-word' data-selectedDataSource='"+row?.selectedDatasource+"' data-productSelection='"+escapeAllHTML(row?.productSelection)+"'  data-productGroupSelection='"+escapeAllHTML(row?.productGroupSelection)+"' data-assignedToID='"+assignedToValueID+"' id='" + row?.id + "'>" + assignedToValue.replaceAll('.', " ") + "</span>";
                }
            },
            {
                "className": "shareWithCell",
                "mData": "shareWith",
                "sortable": false,
                mRender: function (data, type, row) {
                    var shareWithMapData=row?.sharedWith
                    if(row?.isAutoSharedWith){
                        shareWithMapData = shareWithMapData!=""?shareWithMapData + ", Auto Assign":"Auto Assign"
                    }
                    return "<span class='shareWithClass word-wrap-break-word' data-shareWithMap='"+row?.shareWithMap+"' data-selectedDataSource='"+row?.selectedDatasource+"' data-productSelection='"+escapeAllHTML(row?.productSelection)+"'  data-productGroupSelection='"+escapeAllHTML(row?.productGroupSelection)+"' id='" + row?.id + "'>" +  shareWithMapData.replaceAll('.', " ")  + "</span>";

                }
            },
            {
                "sClass": "schedulerCell",
                "className": "scheduleDateJSON",
                "sortable": true,
                mRender: function (data, type, row) {
                    return formSchedulerCell(row.id, row.scheduleDateJSON, data)
                }
            },
            {
                "mData": "nextRunDate",
                "className": "forceLineWrapDate nextRunDateCell",
                mRender: function (data, type, row) {
                    return row?.nextRunDate
                }
            },
            {
                "mData": "lastUpdated",
                'className': 'dt-center',
                "mRender": function (data, type, row) {
                    if(row.lastUpdated=="null" || row.lastUpdated==null){
                        return '' ;
                    } else {
                        return moment.utc(row.lastUpdated).format('DD-MMM-YYYY hh:mm:ss A');
                    }
                }
            },
            {
                "mData": null,
                "className": "dataTableColumnCenter col-min-150 col-max-150",
                "sortable": false,
                "mRender": function (data, type, row) {
                        return formMenuCell(data["id"], data["status"], data["name"], data["isTemplateAlert"],data["type"],data["isEditableAlert"], data["masterConfigId"], data["unscheduled"], data["isRunnable"]);
                }
            }

        ],
        initComplete: function () {
            loadTableOption('#rxTableImportConfigurationList');
        },
        drawCallback: function () {
            console.log($('#alertType').val());
            if($('#alertType').val() == "Aggregate Case Alert") {
                $('#rxTableImportConfigurationList').DataTable().column(2).visible(true);
                $('#rxTableImportConfigurationList').DataTable().column(5).visible(true);
            } else {
                $('#rxTableImportConfigurationList').DataTable().column(2).visible(false);
                $('#rxTableImportConfigurationList').DataTable().column(5).visible(false);
            }
        }
    });
    table.search('');


    function formStatusCell(data) {
        return (data === "template" ? "<span class='fa fa-copy fa-lg' title='" + $.i18n._("Template Alert") + "'></span>" :
            (data === "scheduled" ? "<span class='fa fa-play-circle fa-lg green' style='color:green' title='" + $.i18n._("Scheduled") + "'></span>" :
                    "<span class='fa fa-stop-circle fa-lg' title='" + $.i18n._("Not Scheduled") + "'></span>"
            ));
    }

    function formAlertNameCell(id, text, editable) {
        return "<span data-editable='"+editable+"' class='alertName' style='word-wrap:break-word;' data-id='" + id + "'>" + encodeToHTML(text) + "</span>";
    }

    function formSchedulerCell(id, value, label) {
        if(label == 'null'){
            label =''
        }
        return "<span class='scheduler' style='word-break: break-all;' data-id='" + id + "' data-value='" + value + "'>" + label + "</span>";
    }

    function formMenuCell(id, isSheduled, alertName, isTemplate, type, isEditable, masterConfigId, unscheduled, isRunnable) {
        var isAggAlert = type == 'Aggregate Case Alert';
        var view_url = isAggAlert ? aga_view_url : sca_view_url;
        var edit_url = isAggAlert ? aga_edit_url : sca_edit_url;
        var copy_url = isAggAlert ? aga_copy_url : sca_copy_url;
        var conf_type = isAggAlert ? "Aggregate Configuration" : "Individual Case Configuration";
        var runOnceURLWithParameters = runOnce_url + '?id=' + id + '&type=' + encodeURIComponent(conf_type);

        var actionButton = '<div class="btn-group dropdown col-min-150" align="center">';
        if (isSheduled) {
            actionButton += '<a class="btn btn-success btn-xs unscheduleAlert"  style="font-size: 11px;" data-id="' + id + '">' + $.i18n._("Unschedule") + '</a>';
        } else {
            if(unscheduled == false && isRunnable == true)
                actionButton += '<a class="btn btn-success btn-xs run run-button" master-config-id='+masterConfigId+'  href=' + runOnceURLWithParameters + ' style="font-size: 11px;"  data-id="' + id + '" >' + $.i18n._('run') + '</a>';
            else if(isEditable == true && unscheduled == true && isRunnable == true)
                actionButton += '<a class="btn btn-success btn-xs run run-button" master-config-id='+masterConfigId+'  href=' + runOnceURLWithParameters + ' style="font-size: 11px;"  data-id="' + id + '" >' + $.i18n._('run') + '</a>';
            else
                actionButton += '<a class="btn btn-success btn-xs run disabled" master-config-id='+masterConfigId+'  href=' + runOnceURLWithParameters + ' style="font-size: 11px;"  data-id="' + id + '" >' + $.i18n._('run') + '</a>';
        }
        if(isEditable && masterConfigId !="undefined" && masterConfigId != "null" && masterConfigId != null && masterConfigId != ""){
            actionButton += '<button type="button" class="btn btn-default btn-xs dropdown-toggle" style="font-size: 11px; padding: 2px 2px !important;"  data-toggle="dropdown"> \
                            <span class="caret"></span> \
                            <span class="sr-only">Toggle Dropdown</span> \
                        </button> \
                        <ul class="dropdown-menu" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                            <li role="presentation"><a role="menuitem" href="' + view_url + '/' + id + '">' + $.i18n._('view') + '</a></li> \
                            <li role="presentation"><a role="menuitem" href="' + copy_url + '/' + id + '" class="copy" data-id="'+ id + '">' + $.i18n._('copy') + '</a></li> \
                            <li role="presentation"><a role="menuitem" class="delete"  data-id="' + id + '" data-instancename="" href="javascript:void(0)">' + $.i18n._('delete') + '</a></li> \
                        </ul> \
                    </div>';
        } else if(isEditable && unscheduled == true){
            actionButton += '<button type="button" class="btn btn-default btn-xs dropdown-toggle" style="font-size: 11px; padding: 2px 2px !important;"  data-toggle="dropdown"> \
                            <span class="caret"></span> \
                            <span class="sr-only">Toggle Dropdown</span> \
                        </button> \
                        <ul class="dropdown-menu" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                            <li role="presentation"><a role="menuitem" href="' + view_url + '/' + id + '">' + $.i18n._('view') + '</a></li> \
                            <li role="presentation"><a role="menuitem" href="' + copy_url + '/' + id + '" class="copy" data-id="'+ id + '">' + $.i18n._('copy') + '</a></li> \
                            <li role="presentation"><a role="menuitem" class="delete"  data-id="' + id + '" data-instancename="" href="javascript:void(0)">' + $.i18n._('delete') + '</a></li> \
                        </ul> \
                    </div>';
        } else if(isEditable){
            actionButton += '<button type="button" class="btn btn-default btn-xs dropdown-toggle" style="font-size: 11px; padding: 2px 2px !important;"  data-toggle="dropdown"> \
                            <span class="caret"></span> \
                            <span class="sr-only">Toggle Dropdown</span> \
                        </button> \
                        <ul class="dropdown-menu" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                            <li role="presentation"><a role="menuitem" href="' + view_url + '/' + id + '">' + $.i18n._('view') + '</a></li> \
                            <li role="presentation"><a role="menuitem"  href="' + edit_url + '/' + id + '">' + $.i18n._('edit') + '</a></li> \
                            <li role="presentation"><a role="menuitem" href="' + copy_url + '/' + id + '" class="copy" data-id="' + id + '">' + $.i18n._('copy') + '</a></li> \
                            <li role="presentation"><a role="menuitem" class="delete"  data-id="' + id + '" data-instancename="" href="javascript:void(0)">' + $.i18n._('delete') + '</a></li> \
                        </ul> \
                    </div>';
        }else{
            actionButton += '<button type="button" class="btn btn-default btn-xs dropdown-toggle" style="font-size: 11px; padding: 2px 2px !important;"  data-toggle="dropdown"> \
                            <span class="caret"></span> \
                            <span class="sr-only">Toggle Dropdown</span> \
                        </button> \
                        <ul class="dropdown-menu" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                            <li role="presentation"><a role="menuitem" href="' + view_url + '/' + id + '">' + $.i18n._('view') + '</a></li> \
                        </ul> \
                    </div>';

        }

        return actionButton;
    }

    function replaceBracketsAndQuotes(string) {
        return string != undefined ? string.replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, "&quot;") : ""
    }


    $('.exportPanel').on('click', function () {
        var alertType = $('#alertType').val();
        var sortCol
        var sortDir
        if($('#rxTableImportConfigurationList').DataTable().order()){
            sortCol =$('#rxTableImportConfigurationList').DataTable().order()[0][0];
            sortDir=$('#rxTableImportConfigurationList').DataTable().order()[0][1];
        }

        window.location.href= exportListToExcelURL + encodeURI('?alertType=' + alertType + '&sortedCol=' + sortCol+'&sortedDir='+sortDir)

    });

    $(document).on('click', '.import-log-modal', function () {

        if (typeof importLogTable != "undefined" && importLogTable != null) {
            importLogTable.destroy()
        }
        $('#importLogModal').modal({show: true});

        initImportLogTable();
    });

    var initImportLogTable = function () {

        var columns = import_log_table_columns();

        importLogTable = $('#import-log-table').DataTable({
            layout: {
                topStart: 'search',
                topEnd: null,
                bottomStart: ["pageLength", "info", {paging: {type: "full_numbers"}}],
                bottomEnd: null,
            },
            customProcessing: true,
            searching: true,

            responsive: true,
            language: {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json"
            },
            "ajax": {
                "url": importConfigurationLogs_Url,
                "dataSrc": "importLogList",
            },
            drawCallback: function (settings) {
                colEllipsis();
                webUiPopInit();
            },
            "oLanguage": {
                "oPaginate": {
                    "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                    "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                    "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                    "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
                },
                "sLengthMenu": "Show _MENU_",
                "sZeroRecords": "No data available in table", "sEmptyTable": "No data available in table"
            },
            "aaSorting": [],
            "bLengthChange": true,
            "iDisplayLength": 10,
            "aLengthMenu": [[10, 50, 100, 200, -1], [10, 50, 100, 200, "All"]],
            "pagination": true,
            "aoColumns": columns,
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }]

        });
    };

    var import_log_table_columns = function () {
        return [
            {
                "mData": "importedFileName",
                "className": "col-md-3 cell-break",
                "mRender": function (data, type, row) {
                    return '<a href="' + '/signal/importConfiguration/importedConfigurationFile' + '?' + 'logsId=' + row.id + '&' + 'fileName=' + encodeURIComponent(row.importFileName) + '&' + 'importType=' + true +'">' + addEllipsis(row.importFileName) + '</a>';
                }
            },
            {
                "mData": "generatedFileName",
                "className": "col-md-3 cell-break",
                "mRender": function (data, type, row) {
                    if (row.generatedFileName === '-') {
                        return '-';
                    }
                    else {
                        return '<a href="' + '/signal/importConfiguration/importedConfigurationFile' + '?' + 'logsId=' + row.id + '&' + 'fileName=' + encodeURIComponent(row.generatedFileName) + '">' + addEllipsis(row.generatedFileName) + '</a>';
                    }
                }
            },
            {
                "mData": "importedBy",
                "className": "col-md-2"
            },
            {
                "mData": "importedDate",
                "className": "col-md-2"
            },
            {
                "mData": "status",
                "className": "col-md-2"
            }
        ]
    };

    $('#file').on('change', function () {
        if ($(this).val()) {
            if ($(this)[0].files[0].size > maxUploadLimit) {
                $.Notification.notify('error', 'top right', "Failed", $.i18n._('fileUploadMaxSizeExceedError', maxUploadLimit/1048576), {autoHideDelay: 5000});
                $(this).val('');
                $('input:submit').attr('disabled', true);
            } else {
                $('input:submit').attr('disabled', false);
            }
        } else {
            $('input:submit').attr('disabled', true);
        }
    });

    $('#importConfigurationFileUploadForm').on('submit', function () {
        $('input:submit', this).attr('disabled', true);
        return true;
    });

    $(document).on("click", "#searchProductsAssessment", function () {

        currentEditingRow = $(this).closest("tr");
        var isMultiIngredient = $(this).data("ismultiingredient");
        $("#product_multiIngredient_assessment").prop('checked', isMultiIngredient);
        $("#isAssessmentDicitionary").val("true");

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
