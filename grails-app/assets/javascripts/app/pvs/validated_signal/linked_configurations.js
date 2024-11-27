$(function () {
    $(document).on("click",".data-analysis-role-check",function (e) {
        if(typeof hasDataAnalysisRole !== "undefined" && !hasDataAnalysisRole){
            e.preventDefault();
            $.Notification.notify('warning', 'top right', "Warning", "You don't have access to perform this action", {autoHideDelay: 5000});
        }
    })
    var genAlertNameLink = function (value, id, type, isOnDemand) {
        return function () {
            var url = null;
            if (type == ALERT_CONFIG_TYPE.QUALITATIVE_ALERT) {
                url = isOnDemand ? singleCaseOnDemandDetailsUrl : singleCaseDetailsUrl
            } else if (type == ALERT_CONFIG_TYPE.QUANTITATIVE_ALERT) {
                url = isOnDemand ? aggregateCaseOnDemandDetailsUrl : aggregateCaseDetailsUrl
            } else {
                url = isOnDemand ? evdasOnDemandDetailsUrl : evdasDetailsUrl
            }
            return '<a href="' + (url + '?' + 'callingScreen=review&' +
                signal.utils.composeParams({configId: id})) + '">' + escapeHTML(value) + '</a>'
        }
    };
    var linkedConfigurationTable = $('#linkedConfigurationTable').DataTable({
        "language": {
            "url": "../assets/i18n/dataTables_" + userLocale + ".json"
        },
        "ajax": {
            "url": linkedConfigurationUrl,
            "type": "POST",
            "dataSrc": ""
        },
        "aaSorting": [],
        "bLengthChange": true,
        "bProcessing": true,
        "oLanguage": {
            "oPaginate": {
                "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
            },
            "sLengthMenu":"Show _MENU_",
            "sInfo":"of _TOTAL_ entries",
            "sInfoFiltered": "",
        },
        "aLengthMenu": [[50, 100, 200, 500], [50, 100, 200, 500]],
        "iTotalDisplayRecords": "50",
        drawCallback: function (settings) {
            colEllipsis();
            webUiPopInit();
            var rowsDataAR = $('#linkedConfigurationTable').DataTable().rows().data();
            pageDictionary($('#linkedConfigurationTable_wrapper'),rowsDataAR.length);
            showTotalPage($('#linkedConfigurationTable_wrapper'),rowsDataAR.length);
        },

        "aoColumns": [
            {
                "mData": "name",
                "mRender": function (data, type, row) {
                    if (row.isEnabled) {
                        return genAlertNameLink(row.name, row.id, row.type, row.isOndemand)
                    }
                    return row.name
                }
            },
            {
                "mData": "type"
            },
            {
                "mData": "version"
            },
            {
                "mData": "dateRange"
            },
            {
                "mRender": function (data, type, row) {
                    var criteria = '';
                    if (row.type != 'EVDAS') {
                        criteria = '<b>Product :</b>' + " " + row.productName;
                    }else{
                        criteria = '<b>Substance :</b>' + " " + row.productName;
                    }
                    if (row.events) {
                        criteria += '<br/><b>Events :</b>' + " " + row.events;
                    }
                    if (row.queryName) {
                        criteria += '<br/><b>Query Name :</b>' + " " + row.queryName;
                        if (row.type != 'EVDAS') {
                            var queryParameters = row.queryParameters;
                            $.each(queryParameters, function (index, element) {
                                criteria += '<br/>' + element
                            });
                        }
                    }

                    return criteria;
                }
            },
            {
                "mData": "lastExecuted"
            },
            {
                "mRender": function (data, type, row) {
                    var type = row.type
                    if (pvrIntegrate == "true" && spotfireIntegrate == "true" && type == ALERT_CONFIG_TYPE.QUALITATIVE_ALERT) {
                        var actionButton = '<div class="hidden-btn btn-group dataTableHideCellContent" align="center"> \
                            <a class="btn btn-success btn-xs data-analysis-role-check" data-name="' + row.name + '"  data-version="' + row.version + '" data-id="' + row.id + '" ' +
                            'href="/signal/dataAnalysis/index" >' + 'View Analysis' + '</a>\
                            </div>';
                        return actionButton;
                    }
                    return ""
                }
            }

        ],
        "responsive": true,
        scrollX: true,
        columnDefs: [{
            "targets": '_all',
            "render": $.fn.dataTable.render.text(),
            orderSequence: ['desc', 'asc']
        }]
    });
    actionButton('#linkedConfigurationTable');

});