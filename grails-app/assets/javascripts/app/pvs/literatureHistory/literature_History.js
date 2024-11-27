//= require app/pvs/caseHistory/caseHistoryJustification.js
var literature = literature || {};
var userLocale = "en";
literature.alertReview = (function () {

    var exportLiteratureHistory = function () {
        $(".exportAlertHistories").on('click', function (e) {
            var searchField = $('#literatureHistoryModalTable_filter').find('.form-control.dt-search').val();
            var otherSearchString = $('#literatureHistoryModalTableSuspect_filter').find('.form-control.dt-search').val();
            var table = $("#literatureHistoryModalTable").DataTable()
            var otherAlertTable =$("#literatureHistoryModalTableSuspect").DataTable()
            var otherAlertConfigIds = []
            var alertConfigIds = []
            if(searchField !== "") {
                $.each(table.rows({filter: 'applied'}).data(), function (idx, val) {
                    alertConfigIds.push(val.id);
                });
            }
            if(otherSearchString !== "") {
                $.each(otherAlertTable.rows({filter: 'applied'}).data(), function (idx, val) {
                    otherAlertConfigIds.push(val.id);
                });
            }
            var literatureHistoryModal = $('#literatureHistoryModal');
            var articleId = literatureHistoryModal.find("#articleId").val();
            var alertConfigId = $('#alertConfigId').val();
            var selectedCase = e.currentTarget.href + "&articleId=" + articleId + "&litConfigId=" + alertConfigId + "&searchField=" + encodeURIComponent(searchField) + "&alertConfigIds=" + alertConfigIds + "&otherAlertConfigIds=" + otherAlertConfigIds + "&otherSearchString=" + encodeURIComponent(otherSearchString);
            window.location.href = selectedCase;
            return false
        });
    };

    var openLiteratureHistory = function () {
        $(document).on('click', '.literature-history-icon', function (event) {
            event.preventDefault();
            var rowIndex = $(this).closest('tr').index();
            if (isAbstractViewOrCaseView(rowIndex)) {
                rowIndex = rowIndex / 2;
            }
            var rowObject = table.rows(rowIndex).data()[0];
            var literatureHistoryModal = $('#literatureHistoryModal');
            literatureHistoryModal.find("#articleID").html(rowObject['articleId']);
            $("#articleId").val(rowObject['articleId']);
            $("#alertConfigId").val(rowObject['alertConfigId']);
            literatureHistoryModal.modal('show');

            init_case_history_table(litCurrentArticleHistoryURL + '?litConfigId=' + rowObject['alertConfigId'] + '&articleId=' + rowObject['articleId']);
            init_case_history_table_suspect(listArticleHistoryInOtherAlertsURL + '?litConfigId=' + rowObject['alertConfigId'] + '&articleId=' + rowObject['articleId']);
        });
    };


    var literatureHistoryModalTable = null;
    var literatureHistoryModalTableSuspect = null;

    var init_case_history_table = function (url) {

        if (typeof literatureHistoryModalTable != "undefined" && literatureHistoryModalTable != null) {
            literatureHistoryModalTable.destroy()
        }

        //Data table for the document modal window.
        literatureHistoryModalTable = $("#literatureHistoryModalTable").DataTable({
            "language": {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json"
            },
            initComplete: function () {
                $('[data-toggle="tooltip"]').tooltip()
            },
            "ajax": {
                "url": url,
                "dataSrc": ""
            },
            drawCallback: function (settings) {
                tagEllipsis($("#literatureHistoryModalTable"));
                colEllipsis();
                webUiPopInit();
            },
            searching: true,
            "bLengthChange": true,
            "iDisplayLength": 10,
            "aaSorting": [[7, "desc"]],
            "aLengthMenu": [[10, 20, 50, -1], [10, 20, 50, "All"]],
            "aoColumns": [
                {
                    "mData": "alertName",
                    'className': 'dt-center',
                    "sWidth": "10%"
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
                        return '<i class="font-24 ' + row.priority.iconClass + '"></i>'
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
                            for (var i = 0; i < alertTags.length; i++) {
                                if(alertTags[i].type === 'GLOBAL_TAG'){
                                    tagsElement += '<a tabindex="0" class="badge badge-info details-tags">' + escapeHTML(alertTags[i].name) +'&nbsp;</i></a>'
                                } else if (alertTags[i].type === 'CASE_SERIES_TAG'){
                                    tagsElement += '<a href="#" class="badge badge-info details-tags">' + escapeHTML(alertTags[i].name + ' (A)') + '</a>';
                                } else if (alertTags[i].type === 'PRIVATE_TAG_ALERT') {
                                    tagsElement += '<a tabindex="0" class="badge badge-info details-tags">' + escapeHTML(alertTags[i].name + ' (P)' + ' (A)') + '&nbsp;</i></a>'
                                } else {
                                    tagsElement += '<a tabindex="0" class="badge badge-info details-tags">' + escapeHTML(alertTags[i].name + ' (P)') + '&nbsp;</i></a>'
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
                    'className': "dt-center"
                },
                {
                    "mData": "timestamp",
                    'className': 'dt-center'
                }
            ],
            "rowCallback": function(row, data, index){
                if(typeof hasReviewerAccess !== "undefined" && !hasReviewerAccess) {
                    $(row).find(".btn-edit-history").addClass("hidden");
                }
            },
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text()
            }]
        });
        return literatureHistoryModalTable
    };

    var init_case_history_table_suspect = function (url) {

        if (typeof literatureHistoryModalTableSuspect != "undefined" && literatureHistoryModalTableSuspect != null) {
            literatureHistoryModalTableSuspect.destroy()
        }

        //Data table for the document modal window.
        literatureHistoryModalTableSuspect = $("#literatureHistoryModalTableSuspect").DataTable({
            "language": {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json"
            },
            initComplete: function () {
                $('[data-toggle="tooltip"]').tooltip()
            },
            "ajax": {
                "url": url,
                "dataSrc": ""
            },
            drawCallback: function (settings) {
                tagEllipsis($("#literatureHistoryModalTableSuspect"));
                colEllipsis();
                webUiPopInit();
            },
            searching: true,
            "bLengthChange": true,
            "iDisplayLength": 10,
            "aaSorting": [[7, "desc"]],
            "aLengthMenu": [[10, 20, 50, -1], [10, 20, 50, "All"]],
            "aoColumns": [
                {
                    "mData": "alertName",
                    "mRender": function (data, type, row) {
                        var execConfigId = row.litExecConfigId;
                        return '<a href="' + tagDetailsUrl + '?' + 'callingScreen=review&configId=' + execConfigId + '" target="_blank">' + escapeHTML(data) + '</a>';
                    },
                    'className': 'dt-center',
                    "sWidth": "10%"
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
                    "mRender": function (data, type, row) {
                        return '<i class="font-24 ' + row.priority.iconClass + '"></i>'
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
                            for (var i = 0; i < alertTags.length; i++) {
                                if(alertTags[i].type === 'GLOBAL_TAG'){
                                    tagsElement += '<a tabindex="0" class="badge badge-info details-tags">' + escapeHTML(alertTags[i].name) +'&nbsp;</i></a>'
                                } else if (alertTags[i].type === 'CASE_SERIES_TAG'){
                                    tagsElement += '<a href="#" class="badge badge-info details-tags">' + escapeHTML(alertTags[i].name + ' (A)') + '</a>';
                                } else if (alertTags[i].type === 'PRIVATE_TAG_ALERT') {
                                    tagsElement += '<a tabindex="0" class="badge badge-info details-tags">' + escapeHTML(alertTags[i].name + ' (P)' + ' (A)') + '&nbsp;</i></a>'
                                } else {
                                    tagsElement += '<a tabindex="0" class="badge badge-info details-tags">' + escapeHTML(alertTags[i].name + ' (P)') + '&nbsp;</i></a>'
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
                    'className': "dt-center"
                },
                {
                    "mData": "timestamp",
                    'className': 'dt-center'
                }
            ],
            "rowCallback": function(row, data, index){
                if(typeof hasReviewerAccess !== "undefined" && !hasReviewerAccess) {
                    $(row).find(".btn-edit-history").addClass("hidden");
                }
            },
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text()
            }]
        });
        return literatureHistoryModalTableSuspect
    };

    return {
        openLiteratureHistory: openLiteratureHistory,
        exportLiteratureHistory: exportLiteratureHistory
    }
})();