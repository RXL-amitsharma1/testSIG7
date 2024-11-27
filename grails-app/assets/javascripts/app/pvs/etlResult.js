// This file requires the etlStatus.js to be imported first.
// This file requires the etlResultList.js to be imported first.
$(function () {

    //Initialize the etl result table.
    etlResult.etlResultList.init_etl_result_table(etlResultUrl);

    //Put the result table refresh timer. It will periodically poll the etl result.
    //The polling interval will be cleared when the etl status is not running.
    var resultTimer = setInterval(function () {
        if (typeof window.etlStatus == "undefined" || window.etlStatus == 'RUNNING') {
            etlResult.etlResultList.refresh_etl_refresh_table();
        } else {
            clearInterval(resultTimer);
        }
    }, refreshInterval);
});
