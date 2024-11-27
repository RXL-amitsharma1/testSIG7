//= require app/pvs/common/rx_common.js
//= require app/pvs/caseDrillDown/caseDrillDown
//= require app/pvs/alertComments/alertComments
//= require app/pvs/productEventHistory/productEventHistoryTable
//= require app/pvs/alertComments/alertComments
//= require app/pvs/common/rx_alert_utils
//= require app/pvs/alerts_review/alert_review
//= require app/pvs/caseDrillDown/evdasCaseDrillDown.js
//= require app/pvs/caseHistory/caseHistoryJustification.js

var tableAggReview;
var tableSingleReview;
var tableAdhocReview;
var tableLiteratureReview;
var fullCaseList;
var singleAlertMap = {0:"alertName", 1:"priority", 2:"caseNumber",3:"productName",4:"masterPrefTermAll",5:"disposition"};

var evdasDrillDownOptions = function (value, substance, pt, id, flag_var, isStartDate, alertId, url, className,isArchived) {
    if (value === -1 || value === '-') {
        return '<a tabindex="0">' + '-' + '</a>'
    }
    if (value == 0 || value=="0") {
        return '<span class="blue-1">' + value + '</span>'
    }
    var actionButton;
    var isArchived = isArchived;
    var caseDrillDownUrl = encodeURI(fetchDrillDownDataUrl + "?substance=" + substance + "&id=" + id + "&pt=" + pt + "&flagVar=" + flag_var + "&isStartDate=" + isStartDate + "&alertId=" + alertId + "&numberOfCount=" + value);
    if (url && url != '') {
        actionButton = '<div style="display: block" class="btn-group dropdown">' +
            '<a class="dropdown-toggle" data-toggle="dropdown" href="#"><span class="' + className + '">' + value + '</span></a>' +
            '<ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;">' +
            '<li role="presentation"><a href="' + caseDrillDownUrl + '" class="evdas-case-drill-down-link">Case List</a></li>' +
            '<li role="presentation"><a href="' + url + '" target="_blank">Evdas Link</a></li>' +
            '</ul>' +
            '</div>';
    } else {
        actionButton = '<a href="' + caseDrillDownUrl + '" class="evdas-case-drill-down-link" data-archive="' + isArchived + '"><span class="' + className + '">' + value + '</span></a>'
    }
    return actionButton
};

var drill_down_options = function (keyId,value,type,typeFlag,className,row,isStartDate,flag_var) {
    if(row.dataSource == 'EVDAS'){
        return evdasDrillDownOptions(value, row.productName, row.preferredTerm, row.execConfigId, flag_var,isStartDate,row.id,'',className,row.isArchived);
    }else {
        if(row.dataSourceValue == undefined || row.dataSourceValue == "undefined") {
            row.dataSourceValue = row.dataSource
        }
        if(row.dataSource === "JADER"){
            // Report Type Flag is not changed for Jader
            keyId = typeFlag
        }
        if (null != row.dataSourceValue) {
            if ((row.dataSourceValue).toUpperCase() === "PVA") {
                typeFlag = keyId;
            } else if (((row.dataSourceValue).split(",").length > 0) ? ((row.dataSourceValue).split(",")[0].toUpperCase() === "PVA") : false) {
                typeFlag = keyId;
            }
        }
        return cnt_drill_down(value,type,typeFlag,row.execConfigId, row.id, row.productId, row.ptCode,className, row.dataSourceValue, row.productName, row.preferredTerm,row.isArchived);
    }
};

function nullableNumberValue(value) {
    return (value === null || value === 'null') ? '-' : value;
}

var cnt_drill_down = function (value, type, typeFlag, executedConfigId, alertId, productId, ptCode, className,dataSource,productName,preferredTerm,isArchived) {
    if (value === -1 || value === '-') {
        return '<a tabindex="0">' + '-' + '</a>'
    }
    if (value == 0 || value=="0") {
        return '<span class="blue-1">' + value + '</span>'
    }
    var listConfigUrl = "/signal/aggregateCaseAlert/caseDrillDown?id=" + alertId + '&typeFlag=' + typeFlag + '&type=' +
        type + "&executedConfigId=" + executedConfigId + "&productId=" + productId + "&ptCode=" + ptCode
    var singleCaseDetailsUrl = '/signal/singleCaseAlert/caseSeriesDetails?aggExecutionId=' + executedConfigId + '&aggAlertId=' + alertId + '&aggCountType=' + className + '&productId=' + productId + '&ptCode=' + ptCode + '&type=' + type + "&typeFlag=" + typeFlag + "&isArchived=" + isArchived;
    var seriesData = "id:" + alertId + ",typeFlag:" + typeFlag + ",type:" + type + ",executedConfigId:" +
        executedConfigId + ",productId:" + productId + ",ptCode:" + ptCode;

    if (typeof pvrIntegrate != "undefined" && JSON.parse(pvrIntegrate)) {
        var actionButton = '<div style="display: block" class="btn-group dropdown"> \
                    <a class="dropdown-toggle ' + className + '" data-toggle="dropdown" href="#">' + value + '</a> \
                            <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                                <li role="presentation"><a href=' + singleCaseDetailsUrl + ' target="_blank" data-url=' + listConfigUrl + ' class="">' + 'Case Series' + '</a></li> \
                                </ul> \
                        </div>';
        return actionButton;
    } else {
        return '<a href="#" data-url=' + listConfigUrl + ' class="case-drill-down-link ' + className + '">' + value + '</a>'
    }
};

var create_aggregate_rule_table_columns = function (labelConfigCopy,hyperlinkConfiguration) {
    var aoColumns = [
        {"mData": "alertName", className: 'col-min-150'},
        {
            "mData": "productName",
            className: 'col-min-150 col-max-150',
            "mRender": function (data, type, row) {
                var result = "<input type='hidden' value='" + row.productId + "' class='row-product-id'/>";
                if (row.isSpecialPE) {
                    result += "<span style='color:red'  data-field ='productName' data-id='" + row.productName + "'>" + row.productName + "</span>";
                } else {
                    result += "<span data-field ='productName' data-id='" + row.productName + "'>" + row.productName + "</span>";
                }
                return result;
            }
        },
        {
            "mData": "soc",
            "mRender": function (data, type, row) {
                if (row.soc !== 'undefined' && row.soc !== "SMQ") {
                    return '<td>' + row.soc + '</td>';
                } else {
                    return '<td>' + $.i18n._('blankString') + '</td>';
                }
            },
            className: 'col-min-50 col-max-50'
        },
        {
            "mData": "preferredTerm",
            "mRender": function (data, type, row) {
                return "<span data-field='eventName' data-id='" + row.preferredTerm + "'>" + row.preferredTerm + "</span>";
            },
            className: 'col-min-100 col-max-100'
        },
        {
            "mData": "newCount1",
            "mRender": function (data, type, row) {
                return signal.utils.stacked(
                    drill_down_options(labelConfigKeyId['newCount'], nullableNumberValue(row.newCount1), 'NEW', 'CUMM_FLAG', 'NEW_COUNT', row, true, 0),
                    drill_down_options(labelConfigKeyId['newCount'], nullableNumberValue(row.cumCount1), 'CUMM', 'CUMM_FLAG', 'CUMM_COUNT', row, false, 0)
                );
            },
            className: 'col-min-100 col-max-100'
        }
    ];

    // Add conditional columns based on labelConfigCopy
    if (labelConfigCopy.newSeriousCount) {
        aoColumns.push({
            "mData": "newSeriousCount",
            "mRender": function (data, type, row) {
                if (hyperlinkConfiguration.newSeriousCount) {
                    return signal.utils.stacked(
                        drill_down_options(labelConfigKeyId['newSeriousCount'], nullableNumberValue(row.newSeriousCount), 'NEW', 'SERIOUS_FLAG', 'NEW_SER', row, true, 3),
                        drill_down_options(labelConfigKeyId['newSeriousCount'], nullableNumberValue(row.cumSeriousCount), 'CUMM', 'SERIOUS_FLAG', 'CUMM_SER', row, false, 3)
                    );
                } else {
                    return signal.utils.stacked(
                        nullableNumberValue(row.newSeriousCount),
                        nullableNumberValue(row.cumSeriousCount)
                    );
                }
            },
            className: 'col-min-100 col-max-100'
        });
    }

    if (labelConfigCopy.prrValue) {
        aoColumns.push({
            "mData": "prrValue",
            "mRender": function (data, type, row) {
                return '<a tabindex="0">' + nullableNumberValue(row.prrValue) + '</a>';
            },
            className: 'col-min-100 col-max-100'
        });
    }

    if (labelConfigCopy.rorValue) {
        aoColumns.push({
            "mData": "rorValue",
            "mRender": function (data, type, row) {
                return '<a tabindex="0">' + nullableNumberValue(row.rorValue) + '</a>';
            },
            className: 'col-min-100 col-max-100'
        });
    }

    if (labelConfigCopy.ebgm) {
        aoColumns.push({
            "mData": "ebgm",
            "mRender": function (data, type, row) {
                return '<a tabindex="0" title="">' + nullableNumberValue(data) + '</a>';
            },
            className: 'col-min-100 col-max-100'
        });
    }

    if (labelConfigCopy.eb05) {
        aoColumns.push({
            "mData": "eb05",
            "mRender": function (data, type, row) {
                return signal.utils.stacked(
                    '<a tabindex="0">' + nullableNumberValue(row.eb05) + '</a>',
                    '<a tabindex="0">' + nullableNumberValue(row.eb95) + '</a>'
                );
            },
            className: 'col-min-100 col-max-100'
        });
    }

    aoColumns.push({
        "mData": "dataSource",
        className: 'col-min-100 col-max-100',
        "mRender": function (data, type, row) {
            return "<span data-field='dataSource' data-id='" + row.dataSource + "'>" + row.dataSource + "</span>";
        }
    });

    if (labelConfigCopy.disposition) {
        aoColumns.push({
            "mData": "disposition"
        });
    }

    aoColumns.push({
        "mData": "",
        "mRender": function (data, type, row) {
            return '<span style="cursor: pointer" class="product-event-history-icon glyphicon glyphicon-list-alt"></span>';
        },
        className: 'col-min-50 col-max-50'
    });

    aoColumns.push({
        "mData": "",
        "bSortable": false,
        className: 'col-min-50 col-max-50',
        "mRender": function (data, type, row) {
            if (row.dataSource === "EVDAS") {
                return '<span style="cursor: pointer;font-size: 125%;" data-info="row" data-signal=true data-name="EVDAS Alert" class="text-left count-info-comment comment-icon glyphicon glyphicon-comment"></span>';
            } else {
                return '<span style="cursor: pointer;font-size: 125%;" data-info="row" data-signal=true data-name="Aggregate Case Alert" class="text-left count-info-comment comment-icon glyphicon glyphicon-comment"></span>';
            }
        }
    });

    aoColumns.push({
        "mData": "",
        "bSortable": false,
        className: 'col-min-50 col-max-50',
        "mRender": function (data, type, row) {
            var appType = (row.dataSource === 'EVDAS') ? "EVDAS Alert" : "Aggregate Case Alert";
            return '<span style="cursor: pointer;font-size: 125%;" data-field="removeSignal" data-id="' + row.id + '" class="changeDisassociation glyphicon glyphicon-link"></span>' +
                '<span style="display: none;" data-field ="configId" data-id="' + row.alertConfigId + '"></span>' +
                '<span style="display: none;" class="alert-check-box" data-id="' + row.id + '"></span> ' +
                '<span style="display: none;" data-field ="isArchived" data-id="' + row.isArchived + '"></span>' +
                '<span style="display: none;" class="selectJustificationForDisassociation" data-field="workflowState" data-apptype="' + appType + '"></span>' +
                '<input class="execConfigId" id="execConfigId" type="hidden" value="' + row.execConfigId + '" />';
        }
    });
    return aoColumns.filter(Boolean);
};

$(function () {
    var buttonClassVar = "";
    if(typeof buttonClass !=="undefined" && buttonClass){
        buttonClassVar = buttonClass;
    }

    var checkedIdList = [];
    var checkedRowList = [];
    signal.evdasCaseDrillDown.bind_evdas_drill_down_table(evdasCaseDetailUrl);
    signal.alertReview.openAlertCommentModal("Aggregate Case Alert", "Aggregate Case Alert", "Aggregate Case Alert", checkedIdList, checkedRowList);
    var labelConfigKeyId = $("#labelConfigKeyId").val()
    var hyperlinkConfiguration = $("#hyperlinkConfiguration").val()
    labelConfigKeyId = JSON.parse(labelConfigKeyId)
    hyperlinkConfiguration = JSON.parse(hyperlinkConfiguration)
    var labelConfigCopy = $("#labelConfigCopy").val()
    labelConfigCopy = parseCustomData(labelConfigCopy)
    let columns = create_aggregate_rule_table_columns(labelConfigCopy,hyperlinkConfiguration);
    var labels = JSON.parse(labelsMap);
    let adhocColumns = create_adhoc_alert_table_columns(labels, isVisible());
    tableAggReview = $('#rxTableAggregateReview').DataTable({
        serverSide: true,
        layout: {
            topStart: null,
            topEnd: null,
            bottomStart: ["pageLength", "info", {paging: {type: "full_numbers"}}],
            bottomEnd: null
        },
        customProcessing: true,
        "oLanguage": {
            "url": "../assets/i18n/dataTables_" + userLocale + ".json",
            "sEmptyTable": "No data available in table",
            "oPaginate": {
                "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
            },
            "sLengthMenu": "Show _MENU_"
        },
        "ajax": {
            "url": VALIDATED.acaListUrl + "?id=" + $("#signalId").val(),
            "dataSrc": "aaData"
        },
        drawCallback: function (settings) {
            var rowsDataAR = $('#rxTableAggregateReview').DataTable().rows().data();
            if (settings.json != undefined) {
                pageDictionary($('#rxTableAggregateReview_wrapper'), settings.json.recordsFiltered);
                showTotalPage($('#rxTableAggregateReview_wrapper'), settings.json.recordsFiltered);
            } else {
                pageDictionary($('#rxTableAggregateReview_wrapper'),rowsDataAR.length);
                showTotalPage($('#rxTableAggregateReview_wrapper'),rowsDataAR.length);
            }
            closeInfoPopover();
            showInfoPopover();

            signal.caseDrillDown.bind_drill_down_table(caseDetailUrl);

            $('.product-event-history-icon').on('click', function (event) {
                event.preventDefault();
                var parent_row = $(event.target).closest('tr');
                var productName = parent_row.find('span[data-field="productName"]').attr("data-id");
                var eventName = parent_row.find('span[data-field="eventName"]').attr("data-id");
                var configId = parent_row.find('span[data-field="configId"]').attr("data-id");
                var executedConfigId = parent_row.find('input[id="execConfigId"]').val();
                if (parent_row.find('span[data-field="dataSource"]').attr("data-id") == "EVDAS") {
                    var evdasHistoryModal = $('#evdasHistoryModal');
                    evdasHistoryModal.modal('show');
                    evdasHistoryModal.find("#productName").html(productName);
                    evdasHistoryModal.find("#eventName").html(eventName);
                } else {
                    var productEventHistoryModal = $('#productEventHistoryModal');
                    productEventHistoryModal.modal('show');
                    productEventHistoryModal.find("#productName").html(productName);
                    productEventHistoryModal.find("#eventName").html(eventName);
                    productEventHistoryModal.find("#configId").html(configId);
                }
                if(typeof aggUpdateJustificationUrl != 'undefined')
                    updateJustificationUrl = aggUpdateJustificationUrl;
                if (parent_row.find('span[data-field="dataSource"]').attr("data-id") == "EVDAS") {
                    signal.evdasHistoryTable.init_evdas_history_table(evdasHistoryUrl, productName, eventName, configId, executedConfigId);
                } else {
                    signal.productEventHistoryTable.init_current_alert_history_table(productEventHistoryUrl, productName, eventName, configId, executedConfigId);
                    signal.productEventHistoryTable.init_other_alerts_history_table(productEventHistoryUrl, productName, eventName, configId);
                }

                //Product Event History Modal
                $(".exportAlertHistories").on('click', function (e) {
                    var productEventHistoryModal = $('#productEventHistoryModal');
                    var productName = productEventHistoryModal.find("#productName").html();
                    var eventName = productEventHistoryModal.find("#eventName").html();
                    var configId = productEventHistoryModal.find("#configId").text();
                    var selectedCase = e.currentTarget.href + "&productName=" + productName + "&eventName=" + eventName + "&configId=" + configId;
                    window.location.href = selectedCase;
                    return false
                });

            });

            $('.agg-workflow').on('click', function () {
                var parent_row = $(event.target).closest('tr');
                var productName = parent_row.find('span[data-field="removeSignal"]').attr("data-id");
                var appType = parent_row.find('span[data-field="workflowState"]').attr("data-apptype");
                findSignals(appType, productName, parent_row);
            });

            $(".downloadMemoReport").on('click', function (event) {
                showPopup($(event.target).parent().data('id'));
            });

        },
        "bAutoWidth": false,
        "aoColumns": columns,
        scrollX:true,
        columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }]
    });

    function parseCustomData(dataString) {
        let trimmedString = dataString.slice(1, -1);
        let keyValuePairs = trimmedString.split(', ');
        let result = {};
        keyValuePairs.forEach(pair => {
            // Split by equals sign
            let [key, value] = pair.split('=');
            key = key.trim();
            value = value.trim();
            value = value === 'true' ? true : (value === 'false' ? false : value);
            result[key] = value;
        });
        return result;
    }

    function isVisible() {
        var isVisible = true;
        if(typeof hasReviewerAccess !== "undefined" && !hasReviewerAccess) {
            isVisible = false;
        }
        return isVisible
    }

    tableSingleReview = $('#rxTableSingleReview').DataTable({
        layout: {
            topStart: null,
            topEnd: null,
            bottomStart: ["pageLength", "info", {paging: {type: "full_numbers"}}],
            bottomEnd: null
        },
        customProcessing: true,
        "oLanguage": {
            "url": "../assets/i18n/dataTables_" + userLocale + ".json",
            "oPaginate": {
                "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
            },
            "sLengthMenu": "Show _MENU_"
        },
        drawCallback: function (settings) {
            $('.sca-workflow').on('click', function () {
                var parent_row = $(event.target).closest('tr');
                var productName = parent_row.find('span[data-field="removeSignal"]').attr("data-id");
                findSignals("Single Case Alert", productName, parent_row);
            });

            var filteredCount = $('#rxTableSingleReview').DataTable().rows({search:'applied'}).count();
            pageDictionary($('#rxTableSingleReview_wrapper'), filteredCount);
            showTotalPage($('#rxTableSingleReview_wrapper'), filteredCount);

            var result = this.api().rows().data().toArray();
            if(result && result.length>0 && $('#rxTableSingleReview').DataTable().order()) {
                var columnIndex = $('#rxTableSingleReview').DataTable().order()[0][0];
                var orderColumn = $('#rxTableSingleReview').DataTable().order()[0][1];
                fullCaseList = [];
                result.sort(function (a,b) {
                    if(orderColumn == 'asc'){
                        return a[singleAlertMap[columnIndex]].localeCompare(b[singleAlertMap[columnIndex]]);
                    } else{
                        return b[singleAlertMap[columnIndex]].localeCompare(a[singleAlertMap[columnIndex]]);
                    }
                });
                for (var i = 0; i < result.length; i++) {
                    fullCaseList.push({caseNumber: result[i].caseNumber, caseVersion: result[i].caseVersion, alertId: result[i].id, followUpNumber: result[i].followUpNumber})
                }
            }
            //The case history modal.
            signal.alertReview.openCaseHistoryModal();
            $(".exportAlertHistories").on('click', function (e) {
                var caseHistoryModal = $('#caseHistoryModal');
                var caseVal = caseHistoryModal.find("#actualCaseNumber").val();
                var productFamily = caseHistoryModal.find("#productFamily").html();
                var alertConfigId = caseHistoryModal.find("#alertConfigId").val();
                var caseVersion = caseHistoryModal.find("#caseVersion").val();
                var sorted =  $('#caseHistoryModalTable').DataTable().order()[0][0];
                var order = $('#caseHistoryModalTable').DataTable().order()[0][1];
                var sorted2 = $("#caseHistoryModalTableSuspect").DataTable().order()[0][0];
                var order2 = $("#caseHistoryModalTableSuspect").DataTable().order()[0][1];
                var selectedCase = e.currentTarget.href + "&caseNumber=" + caseVal + "&productFamily=" + encodeURIComponent(productFamily) + "&alertConfigId=" + alertConfigId + "&caseVersion=" + caseVersion +  "&sorted=" + sorted + "&order=" + order + "&sorted2=" + sorted2 + "&order2=" + order2;
                window.location.href = selectedCase;
                return false
            });

        },
        "ajax": { // Individual case series.
            "url": VALIDATED.scaListUrl + "?id=" + $("#signalId").val(),
            "dataSrc": ""
        },
        "bAutoWidth": false,
        "aoColumns": [
            {
                "mData": "alertName",
                "mRender": function (data,type,row) {
                    if(row.isStandalone) {
                        return "-"
                    } else {
                        return '<span>' + escapeHTML(data) + '</span>'
                    }
                },
                className: 'col-min-150 col-max-250 word-break cell-break pvi-col-md'
            },
            {
                "mData": "priority",
                "aTargets": ["priority"],
                "mRender": function (data, type, row) {
                    return '<a class="font-24" title="' + row.priority.value + '"><i class="' + row.priority.iconClass  +'"></i></a>'
                },
                'className': 'dt-center',
                "bVisible": checkIfPriorityEnabled(),
            },
            {
                "mData": "caseNumber",
                className: 'col-min-150 col-max-200 pvi-col-md',
                "mRender": function (data, type, row) {
                    var tooltipMsg = "Version:" + row.caseVersion;
                    var caseNumberWithTooltip = '<span data-toggle="tooltip" data-placement="bottom" title="' + tooltipMsg + '">' +
                        '<a target="_blank" class="caseDetailUrl" style="cursor: pointer">' +
                        '<input data-field ="productFamily" data-id="' + row.productFamily + '" type="hidden" value="' + row.productFamily + '" />' +
                        "<span data-field ='caseNumber' data-id='" + row.actualCaseNumber + "'>" +
                        (row.caseNumber) + "</span><span data-field ='caseVersion' data-id='" + row.caseVersion + "'>" +
                        "</span>(<span data-field ='followUpNumber' data-id='" + row.followUpNumber + "'>" +
                        ((row.followUpNumber < 1) ? 0 : row.followUpNumber) + "</span>)" +
                        "<span data-field ='isArchived' data-id='" + row.isArchived + "'></span>"+
                        "<span data-field ='isStandalone' data-id='" + row.isStandalone + "'></span>"+
                        "<span data-field='alertId' data-id='" + row.id + "'></span>"
                        +'</a></span>';

                    return caseNumberWithTooltip;
                }
            },
            {
                "mData": "productName",
                className: 'col-min-150 col-max-200 pvi-col-md',
                "mRender": function (data, type, row) {
                    return "<span data-field ='productName' data-id='" + row.actualProductName + "'>" + (row.productName) + "</span>";
                }
            },
            {"mData": "masterPrefTermAll",
                className: 'col-min-100 col-max-250 pvi-col-md col-height word-break',
            },
            {"mData": "disposition",
                className: 'col-min-150 col-max-200 pvi-col-md',
                "mRender": function (data, type, row) {
                    if(row.isStandalone) {
                        return "-"
                    }
                    return data;
                }
            },
            {
                "mData": "",
                className: 'col-min-20 pvi-col-xxs',
                "mRender": function (data, type, row) {
                    if(row.isStandalone) {
                        return '<span disabled disabled="disabled" style="pointer-events: none; cursor: not-allowed;" data-signal=true class="case-history-icon glyphicon glyphicon-list-alt text-grey">' +
                            '</span>';
                    }
                    return '<span style="cursor: pointer" data-signal=true class="case-history-icon glyphicon glyphicon-list-alt">' +
                        '<input class="alertConfigId" id="alertConfigId" type="hidden" value="' + row.alertConfigId + '" />'+
                    '</span>';
                },
                className: 'col-min-20 pvi-col-xxs'
            },
            {
                "mData": "",
                "sortable": false,
                className: 'col-min-20 pvi-col-xxs',
                "mRender": function (data, type, row) {
                    return '<span style="cursor: pointer;font-size: 125%;" data-info="row" data-signal=true data-name ="Single Case Alert" class="text-left count-info-comment comment-icon comment-icon glyphicon glyphicon-comment"></span>';
                }
            },
            {
                "mData": "",
                "sortable": false,
                className: 'col-min-20 dt-center pvi-col-xxs' ,
                "mRender": function (data, type, row) {
                    var appType="Single Case Alert"
                    return '<span style="cursor: pointer;font-size: 125%;" data-field="removeSignal" data-id="' + row.id + '" class="changeDisassociation glyphicon glyphicon-link"></span>' +
                        '<span style="display: none;" data-field ="masterPrefTermAll" data-id="' + row.masterPrefTermAll + '"></span>' +
                        '<span style="display: none;" class="alert-check-box" data-id="' + row.id + '"></span> ' +
                        '<span style="display: none;" data-field ="isArchived" data-id="' + row.isArchived + '"></span>' +
                        '<span style="display: none;" class="a selectJustificationForDisassociation" data-field="workflowState" data-apptype="' + appType + '" ></span>'+
                        '<input class="execConfigId" id="execConfigId" type="hidden" value="' + row.execConfigId + '" />';
                },
                "visible": isVisible()
            }
        ],
        scrollX:true,
        columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }]
    });

    $('#rxTableSingleReview').on('search.dt', function (e, settings) {
        var searchTerm = settings.oPreviousSearch.sSearch;
        var modifiedSearchTerm = modifySearchTerm(searchTerm);
        if (modifiedSearchTerm != searchTerm) {
            tableSingleReview.search(modifiedSearchTerm).draw();
        }
    });

    function modifySearchTerm(searchTerm) {
        if (typeof searchTerm != "undefined" &&  !searchTerm.includes('""')) {
            searchTerm = searchTerm.replace(/"/g, '""');
        }
        return searchTerm;
    }

    tableAdhocReview = $('#rxTableAdHocReview').DataTable( {
        layout: {
            topStart: null,
            topEnd: null,
            bottomStart: ["pageLength", "info", {paging: {type: "full_numbers"}}],
            bottomEnd: null
        },
        "oLanguage": {
            "url": "../assets/i18n/dataTables_" + userLocale + ".json",
            "oPaginate": {
                "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
            },
            "sLengthMenu": "Show _MENU_"
        },
        "ajax": {
            "url": VALIDATED.adHocListUrl + "?id=" + $("#signalId").val(),
            "dataSrc": ""
        },
        drawCallback: function (settings) {
            var filteredCount = $('#rxTableAdHocReview').DataTable().rows({search:'applied'}).count();
            pageDictionary($('#rxTableAdHocReview_wrapper'), filteredCount);
            showTotalPage($('#rxTableAdHocReview_wrapper'), filteredCount);
            $('.adhoc-workflow').on('click', function () {
                var parent_row = $(event.target).closest('tr');
                var productName = parent_row.find('span[data-field="removeSignal"]').attr("data-id");
                findSignals("Ad-Hoc Alert", productName, parent_row);
            });
        },
        "aoColumns": adhocColumns,
        scrollX: true,
        columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }],
        initComplete: function() {
            if(this.api().data().length == 0){
                $('#adhocObservation').hide();
            }
            showInfoPopover();
        }
    });

    $('#rxTableAdHocReview').on('search.dt', function (e, settings) {
        var searchTerm = settings.oPreviousSearch.sSearch;
        var modifiedSearchTerm = modifySearchTerm(searchTerm);
        if (modifiedSearchTerm != searchTerm) {
            tableAdhocReview.search(modifiedSearchTerm).draw();
        }
    });

    tableLiteratureReview = $('#rxTableLiteratureReview').DataTable({
        layout: {
            topStart: null,
            topEnd: null,
            bottomStart: ["pageLength", "info", {paging: {type: "full_numbers"}}],
            bottomEnd: null
        },
        "oLanguage": {
            "url": "../assets/i18n/dataTables_" + userLocale + ".json",
            "oPaginate": {
                "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
            },
            "sLengthMenu": "Show _MENU_"
        },
        drawCallback: function (settings) {
            var rowsDataLR = $('#rxTableLiteratureReview').DataTable().rows().data();
            if (settings.json != undefined) {
                pageDictionary($('#rxTableLiteratureReview_wrapper'), settings.json.recordsFiltered);
                showTotalPage($('#rxTableLiteratureReview_wrapper'), settings.json.recordsFiltered);
            } else {
                pageDictionary($('#rxTableLiteratureReview_wrapper'), rowsDataLR.length);
                showTotalPage($('#rxTableLiteratureReview_wrapper'), rowsDataLR.length);
            }
            tagEllipsis($("#rxTableLiteratureReview_wrapper"));
            colEllipsis();
            //There is no workflow, also comment modal is changed
            // TODO: Add new comment modal code here
            $(".more-option").webuiPopover({
                html: true,
                trigger: 'hover',
                content: function () {
                    return $(this).attr('more-data')
                }
            });
            scrollOff();
        },
        "ajax": {
            "url": VALIDATED.literatureAlertListUrl + "?id=" + $("#signalId").val(),
            "dataSrc": "aaData"
        },
        "aoColumns": [
            {
                "mData": "alertName",
                "mRender": function (data) {
                    return '<span>' + escapeHTML(data) + '</span>'
                },
                className: 'col-min-150'
            },
            {
                "mData": "priority",
                "aTargets": ["priority"],
                "mRender": function (data, type, row) {
                    return '<a class="font-24" title="' + row.priority.value + '"><i class="' + row.priority.iconClass  +'"></i></a>'
                },
                className: 'col-min-75',
                "bVisible": checkIfPriorityEnabled(),
            },
            {
                "mData": "title",
                "mRender": function (data, type, row) {

                    var strLength = row.title.split(" ").join("").length;
                    var moreOption = strLength > 45 ? '<a class="pull-right" type="link"><i class="fa fa-ellipsis-h more-option"  more-data="' + row.title + '"> </i> </a>' : "";
                    return '<span>' +  generateArticleLink(row.articleId, row.title.substring(0, 45)) + moreOption + '</span>';
                },
                className: 'col-min-250'
            },
            {
                "mData": "authors",
                "mRender": function (data, type, row) {

                        var strLength = row.authors.split(" ").join("").length;
                        var moreOption = strLength > 45 ? '<a class="pull-right" type="link"><i class="fa fa-ellipsis-h more-option"  more-data="' + row.authors + '"> </i> </a>' : "";
                        return '<span>' + row.authors.substring(0, 45) + moreOption + '</span>';
                },
                className: 'col-min-250'
            },
            {
                "mData": "publicationDate",
                className: 'col-min-100'
            },
            {
                "mData": "disposition",
                className: 'col-min-100'
            },
            {
                "mData": "",
                "sortable": false,
                className: 'col-min-25 col-max-25',
                "mRender": function (data, type, row) {
                    return '<span style="cursor: pointer;font-size: 125%;" data-info="row" data-signal=true data-name ="Literature Search Alert" class="text-left count-info-comment comment-icon comment-icon glyphicon glyphicon-comment"></span>';
                }
            },
            {
                "mData": "",
                "sortable": false,
                className: 'col-min-25',
                "mRender": function (data, type, row) {
                    var appType="Literature Search Alert";
                    return '<span style="cursor: pointer;font-size: 125%;" data-field="removeSignal" data-id="' + row.id + '" class="changeDisassociation glyphicon glyphicon-link"></span>' +
                        '<span style="display: none;" data-field ="masterPrefTermAll" data-id="' + row.masterPrefTermAll + '"></span>' +
                        '<span style="display: none;" class="alert-check-box" data-id="' + row.id + '"></span> ' +
                        '<span style="display: none;" data-field="assignedTo" data-current-user-id="' + row.assignedTo + '"></span>' +
                        '<span style="display: none;" data-field ="isArchived" data-id="' + row.isArchived + '"></span>' +
                        '<span style="display: none;" class="a selectJustificationForDisassociation" data-field="workflowState" data-apptype="' + appType + '" ></span>'+
                        '<input class="execConfigId" id="execConfigId" type="hidden" value="' + row.execConfigId + '" />'
                },
                "visible": isVisible()
            }
        ],
        scrollX: true,
        "bServerSide": true
    });

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

    var generateArticleLink = function (articleId, title) {
        return '<a target="_blank" href="' + pubMedUrl + articleId + '">' + title + '</a>';
    };

    $(document).on('click', ".generate-case-series", function () {
        var metaInfo = $(this).attr("data-url");
        var isArchived = $('#isArchived').val();
        var parent_row = $(event.target).closest('tr');
        var execConfigId = $(this).attr("data-execConfigId");
        var productName = $(this).attr("data-productName");
        var eventName = $(this).attr("data-preferredTerm")
        var caseSeriesModal = $("#case-series-modal");
        caseSeriesModal.modal("show");
        caseSeriesModal.find("#case-series-name").val(productName + "_" + eventName + "_" + execConfigId);
        caseSeriesModal.find(".save-case-series").on('click', function () {
            var seriesName = caseSeriesModal.find("#case-series-name").val();
            $.ajax({
                url: generateCaseSeriesUrl + "?metaInfo=" + metaInfo + "&seriesName=" + seriesName +  "&isArchived=" + isArchived,
            })
                .done(function (result) {
                    caseSeriesModal.modal("hide");
                })
                .fail(function (err) {
                    caseSeriesModal.modal("hide");
                })
        })
    });
    $('#exportTypesEvdas a[href]').on('click', function (e) {
        var clickedURL = e.currentTarget.href;
        var updatedExportUrl = clickedURL;
        var sortCol, sortDir,isArchived;
        if($('#evdas-case-drill-down-table').DataTable().order()){
            sortCol =$('#evdas-case-drill-down-table').DataTable().order()[0][0];
            sortDir=$('#evdas-case-drill-down-table').DataTable().order()[0][1];
        }
        isArchived = $('#evdas-case-drill-down-modal').attr('data-archive')
        updatedExportUrl = clickedURL + '&filterText=' + $('#evdas-case-drill-down-table_filter.dt-input').val() + '&sortedCol=' + sortCol + '&sortedDir=' + sortDir + '&isArchived=' + isArchived + '&callingScreen=' + applicationName;
        window.location.href = updatedExportUrl;
        return false;
    });
    $(document).on("click",".caseDetailUrl",function(){
        var scaAlertId = $(this).find("span[data-field=alertId]").attr("data-id");
        var caseVersion = $(this).find("span[data-field=caseVersion]").attr("data-id");
        var caseNumber = $(this).find("span[data-field=caseNumber]").attr("data-id");
        var followUpNumber = $(this).find("span[data-field=followUpNumber]").attr("data-id");
        var isArchivedAlert = $(this).find("span[data-field=isArchived]").attr("data-id");
        var isStandalone = $(this).find("span[data-field=isStandalone]").attr("data-id");

        signal.utils.postUrl(caseDetailUrl, {
            caseNumber: caseNumber,
            version: caseVersion,
            followUpNumber: followUpNumber,
            alertId: scaAlertId,
            isFaers: $("#isFaers").val(),
            fullCaseList: JSON.stringify(fullCaseList),
            isSingleAlertScreen: true,
            isVersion: true,
            isCaseSeries: false,
            oldFollowUp: followUpNumber,
            oldVersion: caseVersion,
            isArchived: isArchivedAlert,
            isStandalone: isStandalone
        }, true);
    });

    $(".action-search-btn").on("click",function () {
        $(this).closest(".panel").find(".dt-container").find(".dt-search").toggle();
    });

    $("#revertDisposition").on('click', function(){
        $("#undoableJustificationPopover").show();
        addCountBoxToInputField(8000, $("#undoableJustificationPopover").find('textarea'));

    });


    $(document).on('click', '#addCaseSignal', function () {
        init_add_case_modal()
    });

    $(document).on('click', '#addCaseButton', function () {
        add_case_to_signal()
    });
});

var init_add_case_modal = function () {
    addCaseModalObj = $('#addCaseModal');
    addCaseModalObj.find('input:text').val('');
    addCaseModalObj.find('#justification').val("");
    addCaseModalObj.find('#importCasesExcel').prop('checked', false);
    addCaseModalObj.find('#importCasesSection').attr('hidden', 'hidden');
    addCaseModalObj.find('#caseNumber').prop('disabled', false).siblings('label').find('span').show()
    addCaseModalObj.find('#justificationListPriority').val('');
    addCaseModalObj.find('#addCaseButton').prop('disabled', false);
    addCaseModalObj.modal('show');

    addCaseModalObj.find('#importCasesExcel').on('click', function () {
        if (addCaseModalObj.find('#importCasesExcel').is(':checked')) {
            addCaseModalObj.find('#importCasesSection').prop('hidden', false);
            addCaseModalObj.find('#versionNumber, #caseNumber').val('').attr('disabled', 'disabled').siblings('label').find('span').hide();
            $('.add-case-to-list').attr('disabled', 'disabled');
        } else {
            addCaseModalObj.find('#importCasesSection').attr('hidden', 'hidden');
            addCaseModalObj.find(':file').val('').parents('.input-group').find(':text').val('');
            addCaseModalObj.find('#versionNumber, #caseNumber').prop('disabled', false).siblings('label').find('span').show();
            $('.add-case-to-list').prop('disabled', false);
        }
    });


    $(document).on('change', '#justificationListPriority', function () {
        var value = $(this).val();
        if (value != "") {
            justificationObj = JSON.parse(justificationObj.replaceAll('\n', '\\n'));
            var text = justificationObj.filter(function (arr) {
                return arr.id == value
            })[0].text;
            addCaseModalObj.find('#justification').val(text);
        } else {
            addCaseModalObj.find('#justification').val('');
        }
    });


    addCaseModalObj.on('change', ':file', function () {
        var input = $(this);
        var numFiles = input.get(0).files ? input.get(0).files.length : 0;
        var label = input.val().replace(/\\/g, '/').replace(/.*\//, '');
        var validExts = new Array(".xlsx", ".xls");
        var fileExt = label.substring(label.lastIndexOf('.'));
        if (numFiles > 0) {
            if (validExts.indexOf(fileExt.toLowerCase()) < 0) {
                $('#fileFormatError').show();
                $('.add-case-to-list').attr('disabled', 'disabled');
            } else {
                $('#fileFormatError').hide();
                $('.add-case-to-list').prop('disabled', false);
            }
        } else {
            $('#fileFormatError').hide();
        }
        input.trigger('fileselect', [numFiles, label]);
    });

    addCaseModalObj.find(':file').on('fileselect', function (event, numFiles, label) {
        var input = $(this).parents('.input-group').find(':text');
        var log = numFiles > 0 ? label : "";

        if (input.length) {
            input.val(log);
        }
    });
};

var add_case_to_signal = function () {
    addCaseModalObj.find('#addCaseButton').attr('disabled', 'disabled');
    addCaseModalObj.find('#importCasesExcel').prop('disabled', true);
    addCaseModalObj.find('#justificationListPriority').prop('disabled', true);
    var formData = new FormData(addCaseModalObj.find('#addNewCase')[0]);
    if (addCaseModalObj.find(':file').val()) {
        formData.append("file", $('#file_input').get(0).files[0]);
    }
    var signalId = $("#signalId").val();
    if(signalId) {
        formData.append("signalId", signalId);
    }
    $.ajax({
        url: "/signal/validatedSignal/addCaseToSignal",
        type: 'POST',
        data: formData,
        mimeType: "multipart/form-data",
        contentType: false,
        cache: false,
        processData: false,
        beforeSend: function () {
            addCaseModalObj.find("div.isProcessing").show();
        },
    })
        .done(function (data) {
            if (!JSON.parse(data).success) {
                addCaseModalObj.find('#addCaseButton').prop('disabled', false);
                addCaseModalObj.find('.alert-danger').removeClass('hide');
                addCaseModalObj.find('.errorMessageSpan').html(JSON.parse(data).message);
                addCaseModalObj.find("div.isProcessing").hide();
                addCaseModalObj.find('#importCasesExcel').prop('disabled', false);
                addCaseModalObj.find('#justificationListPriority').prop('disabled', false);
                setTimeout(function () {
                    addCaseModalObj.find('.alert-danger').addClass('hide');
                }, 5000)
            } else {
                showSuccessNotification(JSON.parse(data).message);
                addCaseModalObj.find("div.isProcessing").hide();
                addCaseModalObj.find('#importCasesExcel').prop('disabled', false);
                addCaseModalObj.find('#justificationListPriority').prop('disabled', false);
                $('#rxTableSingleReview').DataTable().ajax.reload();
                addCaseModalObj.find('#addCaseButton').prop('disabled', false);
                addCaseModalObj.find('#importCasesSection').attr('hidden', 'hidden');
                addCaseModalObj.find(':file').val('').parents('.input-group').find(':text').val('');
                addCaseModalObj.find('#versionNumber, #caseNumber').prop('disabled', false).siblings('label').find('span').show();
                addCaseModalObj.modal('hide');
                reloadData("", true);
            }
        })
        .fail(function (err) {
            addCaseModalObj.find("i.isProcessing").hide();
            addCaseModalObj.find('#importCasesExcel').prop('disabled', false);
            addCaseModalObj.find('#justificationListPriority').prop('disabled', false);
        });
};

var create_adhoc_alert_table_columns = function (labels, isVisible) {
    var aoColumns = [
        {"mData": "name"}
    ];
    // Add conditional columns based on labelConfigCopy
    if (labels['productSelection']) {
        aoColumns.push({
            "mData": "productSelection",
        });
    }
    if (labels['eventSelection']) {
        aoColumns.push({
            "mData": "eventSelection"
        });
    }
    if (labels['detectedBy']) {
        aoColumns.push({
            "mData": "detectedBy"
        });
    }
    if (labels['initialDataSource']) {
        aoColumns.push({
            "mData": "initDataSrc"
        });
    }
    if (labels['disposition']) {
        aoColumns.push({
            "mData": "disposition"
        });
    }
    aoColumns.push({
        "mData": "",
        className: 'dt-center',
        "mRender": function (data, type, row) {
            var appType="Ad-Hoc Alert";
            return '<span style="cursor: pointer;font-size: 125%;" data-field="removeSignal" data-id="' + row.id + '" class="changeDisassociation glyphicon glyphicon-link"></span>' +
                '<span style="display: none;" data-field ="masterPrefTermAll" data-id="' + row.masterPrefTermAll + '"></span>' +
                '<span style="display: none;" class="alert-check-box" data-id="' + row.id + '"></span> ' +
                '<span style="display: none;" data-field="assignedTo" data-current-user-id="' + row.assignedTo.id + '"></span>' +
                '<span style="display: none;" class="selectJustificationForDisassociation" data-field="workflowState" data-apptype="' + appType + '" ></span>'+
                '<input class="execConfigId" id="execConfigId" type="hidden" value="' + row.execConfigId + '" />';
        },
        "visible": isVisible
    });
    return aoColumns.filter(Boolean);
};








