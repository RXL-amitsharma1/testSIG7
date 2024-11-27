//ToDo remove this
//= require app/pvs/common/rx_common
//= require app/pvs/dataTablesActionButtons.js

//ToDo check usage and remove this fie.
var signal = signal || {};
var checkedIdList = [];
var checkedRowList = [];
var table;

signal.topic_signal_utils = (function () {

    var init_single_case_alert_table = function (id) {
        table = $('#rxTableSingleReview').DataTable({
            "language": {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json",
                "oPaginate": {
                    "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                    "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                    "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                    "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
                },
            },
            drawCallback: function () {

                $('.sca-workflow').on('click', function () {
                    var parent_row = $(event.target).closest('tr');
                    var productName = parent_row.find('span[data-field="removeSignal"]').attr("data-id");
                    findSignals("Single Case Alert", productName, parent_row);
                });
                //The case history modal.
                signal.alertReview.openCaseHistoryModal();

                //The case comment modal.
                signal.alertReview.openAlertCommentModal("Single Case Alert", "Single Case Alert", "Single Case Alert", [], []);

            },
            "ajax": {
                "url": VALIDATED.scaListUrl + "?id=" + id,
                "dataSrc": ""

            },
            "aoColumns": [
                {"mData": "alertName"},
                {"mData": "priority"},
                {
                    "mData": "caseNumber",
                    "mRender": function (data, type, row) {
                        return '<a target="_blank" href="' + (caseDetailUrl + '?' +
                            signal.utils.composeParams({
                                caseNumber: row.caseNumber,
                                version: row.caseVersion,
                                followUpNumber: row.followUpNumber
                            })) + '">' +
                            '<input data-field ="productFamily" data-id="' + row.productFamily + '" type="hidden" value="' + row.productFamily + '" />' +
                            '<input data-field ="primaryEvent" data-id="' + row.primaryEvent + '" type="hidden" value="' + row.primaryEvent + '" />' +
                            "<span data-field ='caseNumber' data-id='" + row.caseNumber + "'>" +
                            (row.caseNumber) + "</span><span data-field ='caseVersion' data-id='" + row.caseVersion + "'>" +
                            "</span>(<span data-field ='followUpNumber' data-id='" + row.followUpNumber + "'>" +
                            (row.followUpNumber) + "</span>)" + '</a>'
                    }
                },
                {
                    "mData": "productName",
                    "mRender": function (data, type, row) {
                        return "<span data-field ='productName' data-id='" + row.productName + "'>" + (row.productName) + "</span>";
                    }
                },
                {"mData": "masterPrefTermAll"},
                {
                    "mData": "",
                    "mRender": function (data, type, row) {
                        return '<div class="stacked-cell-center-top">' + calculateSeriousness(row.assessSeriousness) + "/" + calculateListedness(row.assessListedness) + '</div>';
                    }
                },
                {
                    "mData": "signalsAndTopics",
                    "mRender": function (data, type, row) {
                        return signal.alerts_utils.getSignalNameList(row.signalsAndTopics);
                    }
                },
                {"mData": "disposition"},
                {
                    "mData": "",
                    "mRender": function (data, type, row) {
                        return '<span style="cursor: pointer" class="case-history-icon glyphicon glyphicon-list-alt"></span>';
                    },
                    className: 'dt-center'
                },
                {
                    "mData": "",
                    className: 'dt-center',
                    "mRender": function (data, type, row) {
                        return '<span style="cursor: pointer;font-size: 125%;" class="text-left count-info-comment comment-icon comment-icon glyphicon glyphicon-comment"></span>';
                    }
                },
                {
                    "mData": "",
                    className: 'dt-center',
                    "mRender": function (data, type, row) {
                        return '<span style="cursor: pointer;font-size: 125%;" data-field="removeSignal" data-id="' + row.id + '" class="sca-workflow glyphicon glyphicon-link"></span>' +
                            '<span style="display: none;" data-apptype="Single Case Alert" data-field="workflowState" data-signal-id=' + $("#signalId").val() + ' data-workflow="Validation" data-validation-state="false" data-id="' + row.id + '" data-info="row" class="text-left change-workflow-state dissociate-icon glyphicon glyphicon-remove"></span>' +
                            '<span style="display: none;" data-field ="masterPrefTermAll" data-id="' + row.masterPrefTermAll + '"></span>' +
                            '<span style="display: none;" class="alert-check-box" data-id="' + row.id + '"></span> ' +
                            '<span style="display: none;" data-field="assignedTo" data-current-user-id="' + row.assignedTo.id + '"></span>' +
                            '<input class="execConfigId" id="execConfigId" type="hidden" value="' + row.execConfigId + '" />';
                    }
                }
            ],
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }]
        });
    };

    var init_aggregate_alert_table = function (id) {
        var labelConfigJson = JSON.parse($("#labelConfigJson").val());
        var labelConfigCopyJson = JSON.parse($("#labelConfigCopyJson").val());
        var labelConfigKeyId = $("#labelConfigKeyId").val()
        labelConfigKeyId = JSON.parse(labelConfigKeyId)


        var addColumns = [];
        addColumns.push.apply(addColumns, labelConfigCopyJson.name ? [{"mData": "alertName"}] : []);
        addColumns.push.apply(addColumns, labelConfigCopyJson.productName ? [{
            "mData": "productName",
            "mRender": function (data, type, row) {
                var elem = "<a href='javascript:void(0);'>" + row.productName + "</a>";
                return "<span data-field ='productName' data-id='" + row.productName + "'>" + elem + "</span>"
            }
        }] : []);
        addColumns.push.apply(addColumns, labelConfigCopyJson.soc ? [{"mData": "soc"}] : []);
        addColumns.push.apply(addColumns, [{
            "mData": "preferredTerm",
            "mRender": function (data, type, row) {
                return "<span data-field ='eventName' data-id='" + row.preferredTerm + "'>" + (row.preferredTerm) + "</span>"
            }
        }]);
        addColumns.push.apply(addColumns, labelConfigCopyJson.newSponCount ? [{
            "mData": "",
            "mRender": function (data, type, row) {
                return signal.utils.stacked(
                    cnt_drill_down(labelConfigKeyId['newSponCount'],row.newSponCount, 'NEW', 'SPONT_FLAG', row.execConfigId, row.id, row.productId, row.ptCode),
                    cnt_drill_down(labelConfigKeyId['newSponCount'],row.cumSponCount, 'CUMM', 'SPONT_FLAG', row.execConfigId, row.id, row.productId, row.ptCode))
            },
            className: 'dt-center'
        }] : []);
        addColumns.push.apply(addColumns, labelConfigCopyJson.newSeriousCount ? [{
            "mData": "",
            "mRender": function (data, type, row) {
                return signal.utils.stacked(
                    cnt_drill_down(labelConfigKeyId['newSeriousCount'],row.newSeriousCount, 'NEW', 'SERIOUS_FLAG', row.execConfigId, row.id, row.productId, row.ptCode),
                    cnt_drill_down(labelConfigKeyId['newSeriousCount'],row.cumSeriousCount, 'CUMM', 'SERIOUS_FLAG', row.execConfigId, row.id, row.productId, row.ptCode))
            },
            className: 'dt-center'
        }] : []);
        addColumns.push.apply(addColumns, labelConfigCopyJson.prrValue ? [{"mData": "prrValue"}] : []);
        addColumns.push.apply(addColumns, labelConfigCopyJson.rorValue ? [{"mData": "rorValue"}] : []);
        addColumns.push.apply(addColumns, labelConfigCopyJson.ebgm ? [{"mData": "ebgm"}] : []);
        addColumns.push.apply(addColumns, labelConfigCopyJson.eb05 ? [{
            "mData": "",
            "mRender": function (data, type, row) {
                return signal.utils.stacked(
                    row.eb05,
                    row.eb95)
            },
            className: 'dt-center',
        }] : []);
        addColumns.push.apply(addColumns, [{
            "mData": "trend",
            className: 'dt-center',
            "mRender": function (data, type, row) {
                if (row.trend == "HIGH") {
                    return '<span style="color:red">(+)</span>'
                } else {
                    return '<span style="color:green">(-)</span>'
                }
            }
        }]);
        addColumns.push.apply(addColumns, [{"mData": "dataSource"}]);
        addColumns.push.apply(addColumns, labelConfigCopyJson.signalsAndTopics ? [{
            "mData": "signalsAndTopics",
            "mRender": function (data, type, row) {
                return signal.alerts_utils.getSignalNameList(row.signalsAndTopics);
            }
        }] : []);
        addColumns.push.apply(addColumns, labelConfigCopyJson.disposition ? [{"mData": "disposition"}] : []);
        addColumns.push.apply(addColumns, [{
            "mData": "",
            "mRender": function (data, type, row) {
                return '<span style="cursor: pointer" class="product-event-history-icon glyphicon glyphicon-list-alt"></span>';
            },
            className: 'dt-center'
        }]);
        addColumns.push.apply(addColumns, [{
            "mData": "",
            className: 'dt-center padding0',
            "mRender": function (data, type, row) {
                return '<span style="cursor: pointer;font-size: 125%;" class="text-left count-info-comment comment-icon comment-icon glyphicon glyphicon-comment"></span>';
            }
        }]);
        addColumns.push.apply(addColumns, [{
            "mData": "",
            className: 'dt-center',
            "mRender": function (data, type, row) {
                return '<span style="cursor: pointer;font-size: 125%;" data-field="removeSignal" data-id="' + row.id + '" class="agg-workflow glyphicon glyphicon-link"></span>' +
                    '<span style="display: none;" data-apptype="Aggregate Case Alert" data-field="workflowState" data-signal-id=' + $("#signalId").val() + ' data-workflow="Validation" data-validation-state="false" data-id="' + row.id + '" data-info="row" class="text-left change-workflow-state dissociate-icon glyphicon glyphicon-remove"></span>' +
                    '<span style="display: none;" data-field ="masterPrefTermAll" data-id="' + row.masterPrefTermAll + '"></span>' +
                    '<span style="display: none;" class="alert-check-box" data-id="' + row.id + '"></span> ' +
                    '<span style="display: none;" data-field="assignedTo" data-current-user-id="' + row.assignedTo.id + '"></span>' +
                    '<input class="execConfigId" id="execConfigId" type="hidden" value="' + row.execConfigId + '" />';
            }
        }]);


        $('#rxTableAggregateReview').DataTable({
            "language": {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json"
            },
            "ajax": {
                "url": VALIDATED.acaListUrl + "?id=" + id,
                "dataSrc": ""
            },
            drawCallback: function () {

                signal.caseDrillDown.bind_drill_down_table(caseDetailUrl);

                $('.product-event-history-icon').on('click', function (event) {
                    event.preventDefault();
                    var parent_row = $(event.target).closest('tr');
                    var productName = parent_row.find('span[data-field="productName"]').attr("data-id");
                    var eventName = parent_row.find('span[data-field="eventName"]').attr("data-id");
                    var productEventHistoryModal = $('#productEventHistoryModal');
                    productEventHistoryModal.modal('show');
                    productEventHistoryModal.find("#productName").html(productName);
                    productEventHistoryModal.find("#eventName").html(eventName);
                    signal.productEventHistoryTable.init_product_event_history_table(productEventHistoryUrl, productName, eventName);
                });
                $('.agg-workflow').on('click', function () {
                    var parent_row = $(event.target).closest('tr');
                    var productName = parent_row.find('span[data-field="removeSignal"]').attr("data-id");
                    findSignals("Aggregate Case Alert", productName, parent_row);
                });

                //The case history modal.
                signal.alertReview.openAlertCommentModal("Aggregate Case Alert");

                $(".downloadMemoReport").on('click', function (event) {
                    showPopup($(event.target).parent().data('id'));
                });

            },
            "bAutoWidth": false,
            "aoColumns": addColumns,
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }]
        });
    };

    var init_ad_hoc_alert_table = function (id) {
        $('#rxTableAdHocReview').DataTable({
            "language": {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json"
            },
            "ajax": {
                "url": VALIDATED.adHocListUrl + "?id=" + id,
                "dataSrc": ""
            },
            drawCallback: function () {
                $('.adhoc-workflow').on('click', function () {
                    var parent_row = $(event.target).closest('tr');
                    var productName = parent_row.find('span[data-field="removeSignal"]').attr("data-id");
                    findSignals("Ad-Hoc Alert", productName, parent_row);
                });
            },
            "aoColumns": [
                {"mData": "signalsAndTopics"},
                {"mData": "name"},
                {"mData": "productSelection"},
                {"mData": "eventSelection"},
                {"mData": "detectedBy"},
                {"mData": "initDataSrc"},
                {"mData": "disposition"},
                {
                    "mData": "",
                    className: 'dt-center',
                    "mRender": function (data, type, row) {
                        return '<span style="cursor: pointer;font-size: 125%;" data-field="removeSignal" data-id="' + row.id + '" class="adhoc-workflow glyphicon glyphicon-link"></span>' +
                            '<span style="display: none;" data-apptype="Ad-Hoc Alert" data-field="workflowState" data-signal-id=' + $("#signalId").val() + ' data-workflow="Validation" data-validation-state="false" data-id="' + row.id + '" data-info="row" class="text-left change-workflow-state dissociate-icon glyphicon glyphicon-remove"></span>' +
                            '<span style="display: none;" data-field ="masterPrefTermAll" data-id="' + row.masterPrefTermAll + '"></span>' +
                            '<span style="display: none;" class="alert-check-box" data-id="' + row.id + '"></span> ' +
                            '<span style="display: none;" data-field="assignedTo" data-current-user-id="' + row.assignedTo.id + '"></span>' +
                            '<input class="execConfigId" id="execConfigId" type="hidden" value="' + row.execConfigId + '" />';
                    }
                }
            ],
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }]
        });
    };

    return {
        init_single_case_alert_table: init_single_case_alert_table,
        init_aggregagte_alert_table: init_aggregate_alert_table,
        init_ad_hoc_alert_table: init_ad_hoc_alert_table
    }
})();

var calculateSeriousness = function (serVal) {
    var calculatedSeriousness = "";
    switch (serVal) {
        case "Serious":
            calculatedSeriousness = "Y";
            break;
        case "Non-Serious":
            calculatedSeriousness = "N";
            break;
        case "Unknown":
            calculatedSeriousness = "U";
            break;
        default:
            calculatedSeriousness = "-";
    }
    return calculatedSeriousness;
};


var calculateListedness = function (listVal) {
    var calculatedListedness = "";
    switch (listVal) {
        case "Listed":
            calculatedListedness = "N";
            break;
        case "Unlisted":
            calculatedListedness = "Y";
            break;
        case "Unknown":
            calculatedListedness = "U";
            break;
        default:
            calculatedListedness = "-";
    }
    return calculatedListedness;
};

var findSignals = function (alertType, alertId, row) {
    $.ajax({
        url: "fetchSignalsList?alertId=" + alertId + "&alertType=" + alertType,
    })
        .done(function (result) {
            if (result.signalsBoolean) {
                $('#signal-dissociate-modal').modal('show');
                $('#signal-dissociate-modal #change-bt').on('click', function () {
                    row.find('.change-workflow-state').trigger('click');
                    $('#signal-dissociate-modal').modal('hide')
                });
                $('#signal-dissociate-modal #cancel-bt').on('click', function () {
                    $('#signal-dissociate-modal').modal('hide')
                })
            } else {
                row.find('.change-workflow-state').trigger('click');
            }
        })
        .fail(function (exception) {
            console.log(exception)
        });
};

var cnt_drill_down = function (keyId,value, type, typeFlag, executedConfigId, alertId, productId, ptCode) {
    var dataUrl = "/signal/aggregateCaseAlert/caseDrillDown?id=" + alertId + '&typeFlag=' + keyId + '&type=' + type + "&executedConfigId=" + executedConfigId + "&productId=" + productId + "&ptCode=" + ptCode
    if (typeof pvrIntegrate != "undefined" && JSON.parse(pvrIntegrate)) {
        var actionButton = '<div style="display: block" class="btn-group dropdown" align="center"> \
                    <a class="dropdown-toggle" data-toggle="dropdown" href="#">' + value + '</a> \
                            <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                                <li role="presentation"><a href="#" data-url=' + dataUrl + ' class="case-drill-down-link">' + 'Case List' + '</a></li> \
                                </ul> \
                        </div>';
        return actionButton;
    } else {
        return '<a href="#" data-url=' + dataUrl + ' class="case-drill-down-link">' + +value + '</a>'
    }
};
