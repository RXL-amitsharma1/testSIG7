var signal = signal || {}

signal.signalHistoryTable = (function() {

    var signalHistoryModalTable = null;

    var init_signal_history_table = function (url) {

        if (typeof signalHistoryModalTable != "undefined" && signalHistoryModalTable != null) {
            signalHistoryModalTable.destroy()
        }

        //Data table for the document modal window.
        signalHistoryModalTable = $("#signalHistoryModalTable").DataTable({
            "language": {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json"
            },
            initComplete: function () {
                $('[data-toggle="tooltip"]').tooltip()
            },
            "ajax": {
                "url": url,
                "dataSrc": ""
            },
            searching: true,
            "bLengthChange": true,
            "sortable": false,
            "aaSorting": [],
            "aLengthMenu": [[10, 20, 50, -1], [10, 20, 50, "All"]],
            "aoColumns": [
                {
                    "mData": "disposition",
                    'className': 'dt-center pvi-col-sm'
                },
                {
                    "mData" : "priority",
                    "mRender": function (data, type, row) {
                        return row.priority
                    },
                    'className': 'dt-center pvi-col-sm'
                },
                {
                    "mData": "assignedTo",
                    'className': 'dt-center pvi-col-xs'
                },
                {
                    "mData": "timestamp",
                    'className': 'dt-center pvi-col-md'
                },
                {
                    "mData" : "updatedBy",
                    'className': "dt-center pvi-col-xs"
                },
                {
                    "mData" : "justification",
                    "className": "dt-center pvi-col-lg"
                },
            ],
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }]
        })
        return signalHistoryModalTable
    }

    var clear_signal_history_table = function() {
        signalHistoryModalTable.destroy()
    }

    return {
        init_signal_history_table: init_signal_history_table,
        clear_signal_history_table :  clear_signal_history_table
    }
})()
