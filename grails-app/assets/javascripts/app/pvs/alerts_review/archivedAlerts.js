var signal = signal || {};

signal.archived_utils = (function () {
    var archived_table;
    var sortingCount = 1;

    var init_archived_table = function (table, url, appType, alertDetailUrl) {
        if (!$.fn.dataTable.isDataTable(table)) {
            archived_table = $(table).DataTable({

                "oLanguage": {
                    "url": "../assets/i18n/dataTables_" + userLocale + ".json",
                    "oPaginate": {
                        "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                        "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                        "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                        "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
                    },
                    "sLengthMenu": "Show _MENU_",
                    "sZeroRecords": "No data available in table", "sEmptyTable": "No data available in table"
                },
                drawCallback: function (settings){
                    colArchivedEllipsis();
                    webUiPopInit();
                    pageDictionary($(table+'_wrapper'),settings.json.recordsFiltered);
                    showTotalPage($(table+'_wrapper'),settings.json.recordsFiltered);
                    initPSGrid($(table+'_wrapper'));
                },
                "ajax": {
                    "url": url,
                    "method": "POST",
                    "dataSrc": "aaData",
                    "error":function() {
                        $.Notification.notify('warning', 'top right', "Warning", "Sorry we could not proceed with the request. Kindly contact the administrator.", {hideDueIn: 7000});
                        $('.grid-loading').hide()
                    }
                },
                "aaSorting": [[sortingCount, "desc"]],
                "bLengthChange": true,
                "iDisplayLength": 10,
                "aLengthMenu": [[10, 20, 50, -1], [10, 20, 50, "All"]],
                "bProcessing": true,
                layout: {
                    topStart: null,
                    topEnd: null,
                    bottomStart: ['pageLength', 'info', {paging: {type: "full_numbers"}}],
                    bottomEnd: null,
                },
                customProcessing: true,
                processing: true,
                serverSide: true,
                "bAutoWidth": false,
                "aoColumns": create_archived_column_data(appType, alertDetailUrl),
                "scrollX": true,
                "scrollY":"calc(100vh - 296px)",
                "autoWidth": true,
                columnDefs: [{
                    "targets": '_all',
                    "render": $.fn.dataTable.render.text(),
                    orderSequence: ['desc', 'asc']
                }]
            });
            return archived_table;
        }
    };

    var genAlertNameLink = function (value, id, alertDetailUrl, version ) {
        return function () {
            return '<a href="' + (alertDetailUrl + '?' + 'callingScreen=review&archived=true&' +
                signal.utils.composeParams({configId: id, version: version})) + '" + target="_blank">' + value + '</a>'
        }
    };

    var create_archived_column_data = function (appType, alertDetailUrl) {

        var aoColumns;
        if(appType == ALERT_CONFIG_TYPE.LITERATURE_SEARCH_ALERT) {
            aoColumns = [
                {
                    "mData": "alertName",
                    "mRender": function (data, type, row) {
                        return genAlertNameLink(escapeHTML(row.alertName), row.exConfigId, alertDetailUrl, row.version)
                    },
                    'className': 'col-min-150'
                },
                {
                    "mData": "version",
                    "mRender": function (data, type, full) {
                        return data
                    },
                    'className': 'col-min-100'
                }, {
                    "mData": "dateRange",
                    "mRender": function (data, type, full) {
                        return data
                    },
                    'className': 'col-min-150'
                },    {
                    "mData": "selectedDatasource",
                    "mRender": function (data, type, row) {
                        return data
                    },
                    "className": 'col-min-150 cell-break'
                }, {
                    "mData": "lastModified",
                    "mRender": function (data, type, full) {
                        return data
                    },
                    'className': 'col-min-150'
                }
            ];
        } else {
            aoColumns = [
                {
                    "mData": "alertName",
                    "mRender": function (data, type, row) {
                        return genAlertNameLink(escapeHTML(row.alertName), row.exConfigId, alertDetailUrl, row.version)
                    },
                    'className': 'col-min-150'
                },
                {
                    "mData": "version",
                    "mRender": function (data, type, full) {
                        return data
                    },
                    'className': 'col-min-100'
                },
                {
                    "mData": "product",
                    "mRender": function (data, type, row) {
                        var colElement = '<div class="col-container"><div class="col-height">';
                        colElement += row.product;
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + row.product + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    },
                    "sortable": false,
                    "className": 'col-min-150 cell-break',
                },{
                    "mData": "description",
                    "mRender": function (data, type, full) {
                        return addEllipsis(data)
                    },
                    'className': 'col-min-150'
                },  {
                    "mData": "caseCount",
                    "mRender": function (data, type, full) {
                        return data
                    },
                    'className': 'col-min-100'
                }, {
                    "mData": "reviewedCases",
                    "mRender": function (data, type, full) {
                        return data
                    },
                    'className': 'col-min-100'
                },
                {
                    "mData": "dateRange",
                    "mRender": function (data, type, full) {
                        return data
                    },
                    "sortable": false,
                    'className': 'col-min-150'
                },
                {
                    "mData": "lastModified",
                    "mRender": function (data, type, full) {
                        return data
                    },
                    'className': 'col-min-150'
                }
            ];
        }
        return aoColumns
    };

    var reload_archived_table = function () {
        if (typeof archived_table != "undefined" && archived_table != null) {
            archived_table.ajax.reload();
        } else {
            console.log("Unable to reload the archived table. Please refresh the page.");
        }
    };

    return {
        init_archived_table: init_archived_table,
        reload_archived_table: reload_archived_table
    }
})();