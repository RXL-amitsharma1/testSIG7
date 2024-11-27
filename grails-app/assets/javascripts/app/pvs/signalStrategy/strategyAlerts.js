var signal = signal || {}

signal.strategyAlerts = (function() {

    var init_strategy_alert_table = function(url, tableId, appType) {


        var table = $(tableId).DataTable({

            language: {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json"
            },
            "ajax": {
                "url": url,
                "dataSrc": ""
            },
            "initComplete": function() {

            },
            "bLengthChange": true,
            "iDisplayLength": 50,
            "aLengthMenu": [[50, 100, 200, 500], [50, 100, 200, 500]],
            "aoColumns": strategy_alert_column_data(appType),
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text()
            }]
        })

        return table
    }

    var strategy_alert_column_data = function(appType) {
        var aoColumns = [
            { "mData": 'name' },
            { "mData": 'description' },
            { "mData": "product" },
            { "mData": "event" }
        ]
        if(appType == 'Ad-Hoc Alert') {
            aoColumns.push({ "mData" : "numberOfICSRs" });
            aoColumns.push({ "mData" : "initialDataSource" });
        }
        aoColumns.push({ "mData" : "dateCreated" });
        aoColumns.push({ "mData" : "priority" });
        return aoColumns
    }


    return {
        init_strategy_alert_table: init_strategy_alert_table
    }

})();