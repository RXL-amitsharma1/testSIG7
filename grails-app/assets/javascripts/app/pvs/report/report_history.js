var signal = signal || {};

signal.reportHistory = (function () {

    var reportHistoryTable = null;

    var init_report_history_table = function (url) {
        if (typeof reportHistoryTable != "undefined" && reportHistoryTable != null) {
            refresh_report_history_table();
        } else {
            reportHistoryTable = $('#reportsHistory').DataTable({
                language: {"lengthMenu":"Show _MENU_ entries"},
                "ajax": {
                    "url": url,
                    "dataSrc": ""
                },
                "oLanguage": {
                    "oPaginate": {
                        "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                        "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                        "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                        "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
                    },
                },
                "aaSorting": [[5, "desc"]],
                "bLengthChange": true,
                "iDisplayLength": 50,
                "aLengthMenu": [[50, 100, 200, 500], [50, 100, 200, 500]],
                "drawCallback": function (oSettings) {
                    $(".downloadReport li").on('click', function () {
                        var reportId = $(this).parent().data("report-id");
                        var outputType = $(this).data("type");
                        var url = downloadReportUrl + "?id=" + reportId + "&type=" + outputType
                        window.location.href = url;
                    })
                    colEllipsis();
                    showEllipse();
                    webUiPopInit();
                },
                "aoColumns": [
                    {
                        "mData": "reportName",
                        "mRender": function (data, type, row) {
                            var colElement = '<div class="col-container"><div class="col-height word-break">';
                            colElement += row.reportName;
                            colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(row.reportName) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                            colElement += '</div></div>';
                            return colElement;
                        },
                        "className": 'dt-center col-min-150 col-max-150 cell-break'
                    },
                    {
                        "mData": "dataSource",
                        'className': 'col-min-100 col-max-100 dt-center'
                    },
                    {
                        "mData": "productName",
                        "className": 'dt-center col-min-200 col-max-200 cell-break',
                        "mRender": function (data, type, row) {
                            var productNameDisplayValue = "";
                            if (row.productGroup !== null && row.productGroup !== "null" && row.productGroup !== "") {
                                productNameDisplayValue = productNameDisplayValue + "<div><b>Product Group</b>: " + row.productGroup + "</div>";
                            } else {
                                var productNameList = []
                                if (isValidJson(JSON.stringify(row.productName))) {
                                    productNameList = JSON.parse(JSON.stringify(row.productName));
                                    for (var label in productNameList) {
                                        if (productNameList[label]) {
                                            productNameDisplayValue = productNameDisplayValue + "<div><b>" + label.toUpperCase() + " </b>: " + productNameList[label] + "</div>";
                                        }
                                    }
                                } else {
                                    productNameDisplayValue = row.productName;
                                }
                            }
                            var colElement = '<div class="col-container"><div class="col-height word-break">';
                            colElement += productNameDisplayValue;
                            colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(productNameDisplayValue) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                            colElement += '</div></div>';
                            return colElement;

                        }
                    },
                    {
                        "mData": "reportType",
                        'className': 'col-min-150 col-max-150 dt-center'
                    },
                    {
                        "mData": "summaryDateRange",
                        'className': 'col-min-150 col-max-150 dt-center'
                    },
                    {
                        "mData": "generatedOn",
                        'className': 'col-min-150 col-max-150 dt-center',
                    },
                    {
                        "width": "5%",
                        "mRender": function (data, type, row) {
                            if (row.reportType !== "ICSRs by Case Characteristics") {
                                if (row.reportGenerated) {
                                    return '<li class="dropdown downloadReport" style="list-style: none;">' +
                                        '<a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false" title="Export to"><i class="mdi mdi-export font-22 theme-color" aria-hidden="true"></i> <span class="caret hidden"></span></a>' +
                                        '<ul class="dropdown-menu dropdown-menu-right" style="min-width: 70px" data-report-id="' + row.downloadId + '">' +
                                        '<li data-type="PDF"><a href="#"><img src="/signal/assets/pdf-icon.jpg" class="pdf-icon" height="16" width="16">Save as PDF</a></li>' +
                                        '<li data-type="XLSX"><a href="#"><img src="/signal/assets/excel.gif" class="pdf-icon" height="16" width="16">Save as Excel</a></li>' +
                                        '<li data-type="DOCX"><a href="#"><img src="/signal/assets/word-icon.png" class="pdf-icon" height="16" width="16">Save as Word</a></li>' +
                                        '</ul>' +
                                        '</li>'
                                } else if (row.reportGenerated == null) {
                                    return 'Failed'
                                } else {
                                    return '<i class="fa fa-spinner fa-spin fa-lg es-generating popoverMessage" data-content="Generating" ></i>'
                                }
                            } else {
                                if (row.reportGenerated) {
                                    return '<a href="' + viewICSRReportUrl + '?reportHistoryId=' + row.downloadId + '" role="button" aria-haspopup="true" aria-expanded="false" target="_blank" title="Show report"><i class="mdi mdi-file-chart theme-color font-20"></i></a>'
                                } else if (row.reportGenerated == null) {
                                    return 'Failed'
                                } else {
                                    return '<i class="fa fa-spinner fa-spin fa-lg es-generating popoverMessage" data-content="Generating" ></i>'
                                }
                            }
                        }
                    }
                ],
                columnDefs: [{
                    "targets": '_all',
                    "render": $.fn.dataTable.render.text(),
                    orderSequence: ['desc', 'asc']
                },
                    {
                        "targets": 5,
                        "type": 'date'
                    }]
            });
        }
    };
    var isValidJson = function (jsonString) {
        if (typeof jsonString !== 'string' || jsonString.trim() === '') {
            return false;
        }
        jsonString = jsonString.trim();
        if ((jsonString.startsWith('{') && jsonString.endsWith('}')) ||
            (jsonString.startsWith('[') && jsonString.endsWith(']'))) {
            try {
                JSON.parse(jsonString);
                return true;
            } catch (e) {
                return false;
            }
        }
        return false;
    };

    var clear_report_history_table = function () {
        if (typeof reportHistoryTable != "undefined" && reportHistoryTable != null) {
            reportHistoryTable.clear().draw();
        }
    };

    var refresh_report_history_table = function () {
        if (typeof reportHistoryTable != "undefined" && reportHistoryTable != null) {
            $("#report-history-fetch").show();
            clear_report_history_table();
            reportHistoryTable.ajax.reload(function(){
                $("#report-history-fetch").hide();
            });
        }
    };

    return {
        init_report_history_table: init_report_history_table,
        clear_report_history_table: clear_report_history_table,
        refresh_report_history_table: refresh_report_history_table
    }

})();
