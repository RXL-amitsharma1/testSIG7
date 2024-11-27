var signal = signal || {};
signal.productEventHistoryTable = (function () {

    var currentAlertHistoryModalTable = null;
    var productEventHistoryEbgmModalTable = null;
    var otherAlertsHistoryModalTable = null;

    var init_current_alert_history_table = function (url, productName, eventName, configId, executedConfigId) {
        var regularExpression = new RegExp( findPlusInString, 'gi');
        var regularExpressionHash = new RegExp(findHashInString, 'gi');
        var dataUrl = url + "?" + signal.utils.composeParams({
            productName: encodeURIComponent(productName).replace(regularExpression, '%2b').replace(regularExpressionHash, '%23'),
            eventName: encodeURIComponent(eventName),
            configId: configId,
            executedConfigId: executedConfigId
        });

        if (typeof currentAlertHistoryModalTable != "undefined" && currentAlertHistoryModalTable != null) {
            currentAlertHistoryModalTable.destroy()
        }

        //Data table for the document modal window.
        currentAlertHistoryModalTable = $("#currentAlertHistoryModalTable").DataTable({

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
            "oLanguage": {
                 "oPaginate": {
                    "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                    "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                    "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                    "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
                },
                "sLengthMenu":"Show _MENU_",
            },
            drawCallback: function (settings) {
                tagEllipsis($("#currentAlertHistoryModalTable"));
                colEllipsis();
                webUiPopInit();
            },
            "rowCallback": function(row, data, index){
                if(typeof hasReviewerAccess !== "undefined" && !hasReviewerAccess) {
                    $(row).find(".btn-edit-history").addClass("hidden");
                }
            },
            searching: true,
            "bLengthChange": true,
            "sortable": false,
            "aaSorting": [],
            "aLengthMenu": [[10, 20, 50, -1], [10, 20, 50, "All"]],
            "aoColumns": [
                {
                    "mData": "alertName",
                    'className': 'dt-center'
                },
                {
                    "mData": "reviewPeriod",
                    'sWidth': "100px",
                    'className': 'dt-center'
                },
                {
                    "mData": "disposition",
                    'className': 'dt-center'
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
                },
                {
                    "mData": "priority",
                    "aTargets": ["priority"],
                    "mRender": function (data, type, row) {
                        return '<a href="#" class="font-24" title="' + row.priority.value + '"><i class="' + row.priority.iconClass  +'"></i></a>'
                    },
                    'className': 'dt-center',
                    "bVisible": checkIfPriorityEnabled(),
                },
                {
                    "mData": "alertTags",
                    "mRender": function (data, type, row) {
                        var tagsElement = '<div class="tag-container">';
                        if (row.alertTags) {
                            var alertTags = JSON.parse(row.alertTags);
                            var alertName = ''
                            for (var i = 0; i < alertTags.length; i++) {
                                if(alertTags[i].type === 'GLOBAL_TAG' && (alertName !== alertTags[i].name)){
                                    alertName = alertTags[i].name
                                    tagsElement += '<a tabindex="0" class="badge badge-info details-tags" style="word-break: break-all">' + escapeAllHTML(alertTags[i].name) +'&nbsp;</i></a>'
                                } else if (alertTags[i].type === 'CASE_SERIES_TAG' && alertName !== alertTags[i].name){
                                    alertName = alertTags[i].name
                                    tagsElement += '<a href="#" class="badge badge-info details-tags" style="word-break: break-all">' + escapeAllHTML(alertTags[i].name + ' (A)') + '</a>';
                                } else if (alertTags[i].type === 'PRIVATE_TAG_ALERT' && alertName !== alertTags[i].name) {
                                    alertName = alertTags[i].name
                                    tagsElement += '<a tabindex="0" class="badge badge-info details-tags" style="word-break: break-all">' + escapeAllHTML(alertTags[i].name + ' (P)' + ' (A)') + '&nbsp;</i></a>'
                                } else if(!(alertTags[i].name === alertName)){
                                    alertName = alertTags[i].name
                                    tagsElement += '<a tabindex="0" class="badge badge-info details-tags" style="word-break: break-all">' + escapeAllHTML(alertTags[i].name + ' (P)') + '&nbsp;</i></a>'
                                }
                            }
                        }
                        return tagsElement
                    },
                    "className": 'dt-center'
                },
                {
                    "mData": "alertSubTags",
                    "mRender": function (data, type, row) {
                        var tagsElement = '<div class="tag-container">';
                        if (row.alertSubTags) {
                            var alertSubTags = JSON.parse(row.alertSubTags);
                            for (var i = 0; i < alertSubTags.length; i++) {
                                tagsElement += '<a tabindex="0" class="badge badge-info details-tags bg-grey-3">' + escapeHTML(alertSubTags[i].name + ' (S)') + '&nbsp;'  + '</i></a>'
                            }
                        }
                        return tagsElement
                    },
                    "className": 'dt-center'
                },
                {
                    "mData": "updatedBy",
                    "className": "dt-center"
                },
                {
                    "mData": "timestamp",
                    "className": "dt-center"
                }
            ],
            scrollX: true,
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }]
        });
        return currentAlertHistoryModalTable
    };

    var init_other_alerts_history_table = function (url, productName, eventName, configId) {
        var regularExpression = new RegExp( findPlusInString, 'gi');
        var regularExpressionHash = new RegExp( findHashInString, 'gi');
        var dataUrl = url + "?" + signal.utils.composeParams({
            productName: encodeURIComponent(productName).replace(regularExpression, '%2b').replace(regularExpressionHash, '%23'),
            eventName: encodeURIComponent(eventName),
            configId: configId
        });

        if (typeof otherAlertsHistoryModalTable != "undefined" && otherAlertsHistoryModalTable != null) {
            otherAlertsHistoryModalTable.destroy()
        }

        //Data table for the document modal window.
        otherAlertsHistoryModalTable = $("#otherAlertsHistoryModalTable").DataTable({

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
            "oLanguage": {
                 "oPaginate": {
                    "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                    "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                    "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                    "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
                },
                "sLengthMenu":"Show _MENU_",
            },
            drawCallback: function (settings) {
                tagEllipsis($("#otherAlertsHistoryModalTable"));
                colEllipsis();
                webUiPopInit();
            },
            "rowCallback": function(row, data, index){
                if(typeof hasReviewerAccess !== "undefined" && !hasReviewerAccess) {
                    $(row).find(".btn-edit-history").addClass("hidden");
                }
            },
            searching: true,
            "bLengthChange": true,
            "sortable": false,
            "aaSorting": [],
            "aLengthMenu": [[10, 20, 50, -1], [10, 20, 50, "All"]],
            "aoColumns": [
                {
                    "mData": "alertName",
                    "mRender": function (data, type, row) {
                        var result = '<a href="javascript:void(0)" class="view-alert-no-access" target="_blank">' + escapeHTML(data) + '</a>';
                        var url = detailsUrl + '?callingScreen=review&configId=' + row.executedAlertConfigId
                        if (row.isArchivedAlert == true) {
                            url += '&archived=' + (row.isArchivedAlert)
                        }
                        if (row.isAccessibleToCurrentUser) {
                            result = '<a href=" ' + url + ' " target="_blank">' + escapeHTML(data) + '</a>';
                        }
                        return result;
                    },
                    'className': 'dt-center'
                },
                {
                    "mData": "reviewPeriod",
                    'sWidth': "100px",
                    'className': 'dt-center'
                },
                {
                    "mData": "disposition",
                    'className': 'dt-center'
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
                },
                {
                    "mData": "priority",
                    "aTargets": ["priority"],
                    "mRender": function (data, type, row) {
                        return '<a href="#" class="font-24" title="' + row.priority.value + '"><i class="' + row.priority.iconClass  +'"></i></a>'
                    },
                    'className': 'dt-center',
                    "bVisible": checkIfPriorityEnabled(),
                },
                {
                    "mData": "alertTags",
                    "mRender": function (data, type, row) {
                        var tagsElement = '<div class="tag-container">';
                        if (row.alertTags) {
                            var alertTags = JSON.parse(row.alertTags);
                            var alertName = ''
                            for (var i = 0; i < alertTags.length; i++) {
                                if(alertTags[i].type === 'GLOBAL_TAG' && (alertName !== alertTags[i].name)){
                                    alertName = alertTags[i].name
                                    tagsElement += '<a tabindex="0" class="badge badge-info details-tags" style="word-break: break-all">' + escapeAllHTML(alertTags[i].name) +'&nbsp;</i></a>'
                                } else if (alertTags[i].type === 'CASE_SERIES_TAG' && alertName !== alertTags[i].name){
                                    alertName = alertTags[i].name
                                    tagsElement += '<a href="#" class="badge badge-info details-tags" style="word-break: break-all">' + escapeAllHTML(alertTags[i].name + ' (A)') + '</a>';
                                } else if (alertTags[i].type === 'PRIVATE_TAG_ALERT' && alertName !== alertTags[i].name) {
                                    alertName = alertTags[i].name
                                    tagsElement += '<a tabindex="0" class="badge badge-info details-tags" style="word-break: break-all">' + escapeAllHTML(alertTags[i].name + ' (P)' + ' (A)') + '&nbsp;</i></a>'
                                } else if(!(alertTags[i].name === alertName)){
                                    alertName = alertTags[i].name
                                    tagsElement += '<a tabindex="0" class="badge badge-info details-tags" style="word-break: break-all">' + escapeAllHTML(alertTags[i].name + ' (P)') + '&nbsp;</i></a>'
                                }
                            }
                        }
                        return tagsElement
                    },
                    "className": 'dt-center'
                },
                {
                    "mData": "alertSubTags",
                    "mRender": function (data, type, row) {
                        var tagsElement = '<div class="tag-container">';
                        if (row.alertSubTags) {
                            var alertSubTags = JSON.parse(row.alertSubTags);
                            for (var i = 0; i < alertSubTags.length; i++) {
                                tagsElement += '<a tabindex="0" class="badge badge-info details-tags bg-grey-3">' + escapeHTML(alertSubTags[i].name + ' (S)') + '&nbsp;'  + '</i></a>'
                            }
                        }
                        return tagsElement
                    },
                    "className": 'dt-center'
                },
                {
                    "mData": "updatedBy",
                    'className': 'dt-center'
                },
                {
                    "mData": "timestamp",
                    "className": "dt-center"
                }
            ],
            scrollX: true,
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }]
        });
        return otherAlertsHistoryModalTable
    };

    var clear_product_event_history_table = function () {
        currentAlertHistoryModalTable.destroy();
        productEventHistoryEbgmModalTable.destroy()
    };

    return {
        init_current_alert_history_table: init_current_alert_history_table,
        init_other_alerts_history_table: init_other_alerts_history_table,
        clear_product_event_history_table: clear_product_event_history_table
    }
})();
