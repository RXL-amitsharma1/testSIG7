$(function () {

    //Put the etl status. It will periodically poll the etl status.
    //The polling interval will be cleared when the etl status is not running.
    var etlStatusTimer = setInterval(function () {
        if (typeof window.etlStatus == "undefined" || window.etlStatus == 'RUNNING') {
            poll_etl_status();
        } else {
            clearInterval(etlStatusTimer);
        }
    }, refreshInterval);

});

function poll_etl_status() {
    $.ajax({
        type: "GET",
        url: etlStatusUrl,
        dataType: 'json'
    })
        .done(function (result) {
            var status = result.status
            window.etlStatus = status;
            var html = '';
            switch (status) {
                case 'SUCCESS':
                    html = '<span class="label label-success">' + status + '</span>';
                    break;
                case 'RUNNING':
                    html = '<span class="label label-primary">' + status + '</span>';
                    break;
                case 'FAILED':
                    html = '<span class="label label-danger">' + status + '</span>';
                    break;
            }
            $('.etlStatus').html(html);
    });
}