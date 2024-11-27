var etlResult = etlResult || {}

etlResult.etlResultList = (function () {

    //Etl result table.
    var etl_result_table;

    //The function for initializing the etl result data tables.
    var init_etl_result_table = function (url) {

        //Initialize the datatable
        etl_result_table = $("#etlScheduleResult").DataTable({


            "language": {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json"
            },

            "ajax": {
                "url": url,
                "dataSrc": ""
            },
            //"sDom" : "t",
            "bFilter": false,
            "aaSorting": [[0, "desc"]],
            "iDisplayLength": 10,
            "aoColumns": [
                {
                    "mData": "stageKey"
                },
                {
                    "mData": "startTime",
                    "mRender": function (data, type, full) {
                        return (data != undefined) ? moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT):'';
                    }
                },
                {
                    "mData": "finishTime",
                    "mRender": function (data, type, full) {
                        return (data != undefined) ? moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT):'';
                    }
                },
                {
                    "mData": "passStatus"
                }
            ]
        });

        return etl_result_table;
    }

    var refresh_etl_refresh_table = function () {
        etl_result_table.ajax.reload();
    }

    return {
        init_etl_result_table: init_etl_result_table,
        refresh_etl_refresh_table: refresh_etl_refresh_table
    }

})()