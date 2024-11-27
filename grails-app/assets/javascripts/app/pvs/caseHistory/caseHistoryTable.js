var signal = signal || {};

signal.caseHistoryTable = (function() {

    var quantitativeScoreTable = null;
    var caseHistoryModalTable = null;
    var caseHistoryModalTableSuspect = null;

    var init_case_history_table = function (url, isArchived = false) {
        var caseHistoryModal = $('#caseHistoryModal');
        var dataUrl = url + "?caseNumber=" + caseHistoryModal.find("#actualCaseNumber").val()
            + "&productFamily=" + encodeURIComponent(caseHistoryModal.find("#actualProductFamily").val())
            + "&alertConfigId=" + caseHistoryModal.find("#alertConfigId").val() + "&isArchived=" + isArchived + "&exeConfigId=" + caseHistoryModal.find("#exeConfigId").val() + "&caseVisibility=" + caseHistoryModal.find("#caseNumber").html();


        if (typeof caseHistoryModalTable != "undefined" && caseHistoryModalTable != null) {
            caseHistoryModalTable.destroy()
        }

        //Data table for the document modal window.
        caseHistoryModalTable = $("#caseHistoryModalTable").DataTable({

            "language": {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json"
            },
            drawCallback: function (settings) {
                tagEllipsis($("#caseHistoryModalTable"));
                colEllipsis();
                webUiPopInitCaseHistory();
                if (settings.fnRecordsTotal() < 1) {
                    $("#caseHistoryModalTable_paginate").hide();
                    $("#caseHistoryModalTable_info").hide();
                } else {
                    $("#caseHistoryModalTable_paginate").show();
                    $("#caseHistoryModalTable_info").show();
                }
            },
            initComplete: function () {
                $('[data-toggle="tooltip"]').tooltip()
            },
            "ajax": {
                "url": dataUrl,
                "dataSrc": "",
                "error": ajaxAuthroizationError
            },
            "rowCallback": function(row, data, index){
                if(typeof hasReviewerAccess !== "undefined" && !hasReviewerAccess) {
                    $(row).find(".btn-edit-history").addClass("hidden");
                }
            },
            searching: true,
            "bLengthChange": true,
            "iDisplayLength": 10,
            "aaSorting": [[8,"desc"]],
            "aLengthMenu": [[10, 20, 50, -1], [10, 20, 50, "All"]],
            "scrollX": true,
            "aoColumns": [
                {
                    "mData": "alertName",
                    'className': 'dt-center',
                    "sWidth": "10%",
                    "mRender": function (data, type, row) {
                        if(row.alertName){
                            var p="CUMM_";
                            var indexOfCumCount=row.alertName.indexOf(p);
                            if(indexOfCumCount){
                               return row.alertName.replace(p,"CUM_");
                            }else{
                               return row.alertName;
                            }
                        } else {
                            return '';
                        }
                    }
                },
                {
                    "mData": "caseNumber",
                    'className': 'dt-center',
                    "mRender": function (data, type, row) {
                        var tooltipMsg = "Version:" + row.caseVersion;
                        var isBlinded = row.caseNumber == BLINDING_STATUS.BLINDED || row.caseNumber == BLINDING_STATUS.REDACTED;
                        if(row.caseNumber){
                            var caseNum = "<span data-toggle='tooltip' data-placement='bottom' title='" + tooltipMsg + "'>";
                            if(!isBlinded && row.followUpNumber!==$.i18n._('nullString')){
                                caseNum+= row.caseNumber + '(' + row.followUpNumber + ')' ;
                            }else{
                                caseNum+= row.caseNumber;
                            }
                            caseNum+="</span>";
                            return caseNum;
                        } else {
                            return '';
                        }
                    }
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
                        return '<i class="font-24 ' + row.priority.iconClass  +'"></i>'
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
                    "mData" : "updatedBy",
                    'className' : "dt-center"
                },
                {
                    "mData": "timestamp",
                    'className': 'dt-center'
                }
            ],
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }]
        });
        return caseHistoryModalTable
    };

    var init_case_history_table_suspect = function (url) {

        var caseHistoryModal = $('#caseHistoryModal');
        var dataUrl = url + "?caseNumber=" + caseHistoryModal.find("#actualCaseNumber").val() + "&productFamily=" + encodeURIComponent(caseHistoryModal.find("#actualProductFamily")) + "&caseVersion="+caseHistoryModal.find("#caseVersion").val() + "&alertConfigId=" + caseHistoryModal.find("#alertConfigId").val() + "&caseVisibility=" + caseHistoryModal.find("#caseNumber").html();

        if (typeof caseHistoryModalTableSuspect != "undefined" && caseHistoryModalTableSuspect != null) {
            caseHistoryModalTableSuspect.destroy()
        }

        //Data table for the document modal window.
        caseHistoryModalTableSuspect = $("#caseHistoryModalTableSuspect").DataTable({

            "language": {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json"
            },
            initComplete: function () {
                $('[data-toggle="tooltip"]').tooltip()
            },
            "ajax": {
                "url": dataUrl,
                "dataSrc": "",
                "error": ajaxAuthroizationError
            },
            drawCallback: function (settings) {
                tagEllipsis($("#caseHistoryModalTableSuspect"));
                colEllipsis();
                webUiPopInit();

                if (settings.fnRecordsTotal() < 1) {
                    $("#caseHistoryModalTableSuspect_paginate").hide();
                    $("#caseHistoryModalTableSuspect_info").hide();
                } else {
                    $("#caseHistoryModalTableSuspect_paginate").show();
                    $("#caseHistoryModalTableSuspect_info").show();
                }
            },
            "rowCallback": function(row, data, index){
                if(typeof hasReviewerAccess !== "undefined" && !hasReviewerAccess) {
                    $(row).find(".btn-edit-history").addClass("hidden");
                }
            },
            searching: true,
            "bLengthChange": true,
            "iDisplayLength": 10,
            "aaSorting": [[8,"desc"]],
            "aLengthMenu": [[10, 20, 50, -1], [10, 20, 50, "All"]],
            "scrollX": true,
            "aoColumns": [
                {
                    "mData": "alertName",
                    "mRender": function (data, type, row) {
                        var p="CUMM_";
                        var indexOfCumCount=data.indexOf(p);
                        var execConfigId = row.executedAlertConfigId;
                        if(indexOfCumCount) {
                            var result = '<a href="javascript:void(0)" class="view-alert-no-access" target="_blank">' + escapeHTML(data.replace(p, "CUM_")) + '</a>';
                            if (row.isAccessibleToCurrentUser) {
                                result = '<a href="' + singleCaseDetailsUrl + '?' + 'callingScreen=review&configId=' + execConfigId + '" target="_blank">' + escapeHTML(data.replace(p, "CUM_")) + '</a>';
                            }
                        }
                        else{
                            result= '<a href="' + singleCaseDetailsUrl + '?' + 'callingScreen=review&configId=' + execConfigId + '" target="_blank">' + escapeHTML(data) + '</a>'
                        }
                        return result;
                    },
                    'className': 'dt-center',
                    "sWidth": "10%"
                },
                {
                    "mData": "caseNumber",
                    'className': 'dt-center',
                    "mRender": function (data, type, row) {
                        var tooltipMsg = "Version:" + row.caseVersion;
                        var isBlinded = row.caseNumber == BLINDING_STATUS.BLINDED || row.caseNumber == BLINDING_STATUS.REDACTED;
                        if (row.caseNumber) {
                            var caseNum = "<span data-toggle='tooltip' data-placement='right' title='" + tooltipMsg + "'>";
                            if (!isBlinded && row.followUpNumber !== $.i18n._('nullString')) {
                                caseNum += row.caseNumber + '(' + row.followUpNumber + ')';
                            } else {
                                caseNum += row.caseNumber;
                            }
                            caseNum += "</span>";
                            return caseNum;
                        } else {
                            return '';
                        }
                    }
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
                    "mData" : "priority",
                    "mRender": function (data, type, row) {
                        return '<i class="font-24 ' + row.priority.iconClass  +'"></i>'
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
                    "mData" : "updatedBy",
                    'className' : "dt-center"
                },
                {
                    "mData": "timestamp",
                    'className': 'dt-center',
                    "mRender": function (data, type, row) {
                        if(row.timestamp=="null" || row.timestamp==null){
                            return '' ;
                        } else {
                            return moment.utc(row.timestamp).format('DD-MMM-YYYY hh:mm:ss A');
                        }
                    }
                }
            ],
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }]
        });
        return caseHistoryModalTableSuspect
    };

    var clear_case_history_table = function() {
        caseHistoryModalTable.destroy()
    };

    return {
        init_case_history_table: init_case_history_table,
        init_case_history_table_suspect: init_case_history_table_suspect,
        clear_case_history_table :  clear_case_history_table
    }
})();