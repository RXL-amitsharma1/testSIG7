var signal = signal || {};

signal.evdasHistoryTable = (function() {

    var evdasHistoryModalTable = null;

    var init_evdas_history_table = function (url, productName, eventName, alertConfigId, alertId) {
        var regularExpression = new RegExp( findPlusInString, 'gi');
        productName=productName.replace(regularExpression, '%2b')
        var dataUrl = url +"?" +signal.utils.composeParams (
            {productName: productName, eventName: eventName, alertConfigId: alertConfigId, alertId : alertId}
        );

        if (typeof evdasHistoryModalTable != "undefined" && evdasHistoryModalTable != null) {
            evdasHistoryModalTable.destroy()
        }

        //Data table for the document modal window.
        evdasHistoryModalTable = $("#evdasHistoryModalTable").DataTable({

            "language": {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json"
            },
            initComplete: function () {
                $('[data-toggle="tooltip"]').tooltip()
            },
            "ajax": {
                "url": dataUrl,
                "dataSrc": ""
            },
            searching: true,
            "bLengthChange": true,
            "sortable": false,
            "aaSorting": [],
            "aLengthMenu": [[10, 20, 50, -1], [10, 20, 50, "All"]],
            drawCallback: function (settings) {
                tagEllipsis($("#evdasHistoryModalTable"));
                colEllipsis();
                webUiPopInit();
            },
            "rowCallback": function(row, data, index){
                if(typeof hasReviewerAccess !== "undefined" && !hasReviewerAccess) {
                    $(row).find(".btn-edit-history").addClass("hidden");
                }
            },
            "aoColumns": [
                {
                    "mData": "disposition",
                    'className': 'dt-center'
                },
                {
                    "mData" : "priority",
                    "mRender": function (data, type, row) {
                        return row.priority
                    },
                    'className': 'dt-center',
                    "bVisible": checkIfPriorityEnabled(),
                },
                {
                    "mData": "assignedTo",
                    'className': 'dt-center'
                },
                {
                    "mData": "timestamp",
                    'className': 'dt-center'
                },
                {
                    "mData" : "updatedBy",
                    'className' : "dt-center"
                },
                {
                    "mData": "justification",
                    "className": "dt-center justification-cell col-min-150 col-max-200 cell-break",
                    "mRender": function (data, type, row) {
                        var colElement = '<div class="col-container"><div class="col-height textPre">';
                        colElement += encodeToHTML(row.justification);
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + escapeHTML(encodeToHTML(row.justification)) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement;
                    }
                }
            ],
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }]
        });
        return evdasHistoryModalTable
    };

    var clear_evdas_history_table = function() {
        evdasHistoryModalTable.destroy()
    };

    return {
        init_evdas_history_table: init_evdas_history_table,
        clear_evdas_history_table :  clear_evdas_history_table
    }
})();
