$(function () {

    var inboxLogTable = $('#inboxLogTable').DataTable({
        language: {
            "url": "../assets/i18n/dataTables_" + userLocale + ".json"
        },
        "ajax": {
            "url": inboxListUrl,
            "dataSrc": ""
        },

        "bLengthChange": true,
        "iDisplayLength": 50,
        "aLengthMenu": [[50, 100, 200, 500], [50, 100, 200, 500]],
        "aoColumns": [
            {
                "mData": "type"
            },
            {
                "mData": "subject"
            },
            {
                "mRender": function (data, type, row) {
                    return '<span class="signalOverflow">' + row.content + '</span>'
                }

            },
            {
                "mData": "createdOn"
            }

        ],
        columnDefs: [{
            "targets": '_all',
            "render": $.fn.dataTable.render.text(),
            orderSequence: ['desc', 'asc']
        }]
    });

    $(document).on('click', '.mark-read-icon', function (event) {
        event.stopPropagation();
        var id = $(this).data('id');
        $.ajax({
            type: "GET",
            url: markAsReadUrl,
            data: {
                id: id
            },
            dataType: 'json',
        })
            .done(function (result) {
                location.reload()
            });

    });

    $(document).on('click', '.mark-unread-icon', function (event) {
        event.stopPropagation();
        var id = $(this).data('id');
        $.ajax({
            type: "GET",
            url: markAsUnreadUrl,
            data: {
                id: id
            },
            dataType: 'json',
        })
            .done(function (result) {
                location.reload()
            });

    });

    $(document).on('click', '.delete-icon', function (event) {
        event.stopPropagation();
        var id = $(this).data('id');
        $.ajax({
            type: "GET",
            url: deleteUrl,
            data: {
                id: id
            },
            dataType: 'json',
        })
            .done(function (result) {
                location.reload()
            });

    });

    $(document).on('click', '.notificationPanel', function () {
        var notitficationPanel = $(this);
        var unbold = notitficationPanel.find('.boldText');
        var execConfigId = $(this).data('execid');
        var link = $(this).data('execid');
        var detailUrl = $(this).data('detailurl');
        var content = $(this).data('content');
        var id = $(this).data('id');
        var type = $(this).data('type');
        if(typeof content ==="undefined" || content ==null || content==""){
        content=type;
        }
        const paramsForCaseSeriesUrl = new Map();
        if (type === CASE_SERIES_DRILLDOWN){
            let params = content.replaceAll('[','').replaceAll(']','');
            let paramsList = params.split(',');
            $.each(paramsList, function (idx, val) {
                paramsForCaseSeriesUrl.set(val.split(':')[0].trim(),val.split(':')[1].trim())
            });
        }
        $.ajax({
            type: "GET",
            url: markAsReadUrl,
            data: {
                id: id
            },
            dataType: 'json',
        })
            .done(function (result) {
                // Redirect to the clicked report's criteria page if we can
                if (result.status) {
                    notitficationPanel.addClass("grey");
                    unbold.removeClass('font-bold');
                    if (link && link !== 0) {
                        if (detailUrl in detailUrls) {
                            if(type === CASE_SERIES_DRILLDOWN){
                                    window.open( caseSeriesURL+"?" +
                                    signal.utils.composeParams({
                                        aggExecutionId: paramsForCaseSeriesUrl.get('aggExecutionId'),
                                        aggAlertId: paramsForCaseSeriesUrl.get('aggAlertId'),
                                        aggCountType:paramsForCaseSeriesUrl.get('aggCountType'),
                                        productId:paramsForCaseSeriesUrl.get('productId'),
                                        ptCode:paramsForCaseSeriesUrl.get('ptCode'),
                                        type:paramsForCaseSeriesUrl.get('type'),
                                        typeFlag:paramsForCaseSeriesUrl.get('typeFlag'),
                                        isArchived:paramsForCaseSeriesUrl.get('isArchived')
                                    }));
                            }  else if (type === SIGNAL_CREATION || detailUrl === SIGNAL_CREATION || detailUrl === 'validatedSignalRedirectURL') {
                                window.open(detailUrls[detailUrl]+"?id="+link);
                            }  else{
                            window.open(detailUrls[detailUrl] + "?callingScreen=review&configId=" + link);
                            }
                        } else {
                            window.open(reportRedirectURL + "/" + link)
                        }
                    } else if (type === REPORT_GENERATED && detailUrl) {
                        window.open(detailUrl);
                    } else if (type === ANALYSIS_REPORT_GENERATED && detailUrl) {
                        window.open(detailUrl);
                    } else {
                        var contentTagModalObj = $('#contentTagModal');
                        contentTagModalObj.find("#content").html(content);
                        contentTagModalObj.modal('show');
                    }
                }
            });
    });

});