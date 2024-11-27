var signal = signal || {}

signal.activities_utils = (function() {
    var activity_table;
    var sortingCount = 3;
    var rowsTotalCount;
    var init_activities_table = function(table, url, appType) {
        var columns = create_activity_column_data(appType);
        activity_table = $(table).DataTable({
            language: { "lengthMenu": "Show _MENU_", search: 'Search'},
            layout: {
                topStart: 'search',
                bottomStart: [
                    "pageLength",
                    "info",
                    {
                        paging: {
                            type: "full_numbers",
                        },
                    },
                ],
                bottomEnd: null,
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
                "url": url,
                "dataSrc": ""
            },
            initComplete: function() {
                $('#signalActivityTable_wrapper .dt-search').attr("id", "signalActivityTable_filter");
            },
            drawCallback: function () {
                tagEllipsis($(table));
                colActivityEllipsis();
                webUiPopInitActivities();
                var activityEntries =$('#activitiesTable_info').text().replace(/,/g , '');
                $('#activitiesTable_info').text(activityEntries);
                $('a[href="#activities"]').on("shown.bs.tab", function (e) {
                    e.preventDefault();
                    $('#activitiesTable').DataTable().ajax.reload(); //Added for PVS-65803
                    addGridShortcuts('#activitiesTable');
                    removeGridShortcuts('#alertsDetailsTable');
                    removeGridShortcuts('#archivedAlertsTable');
                });
                if(typeof table == null || typeof table === undefined) {
                    rowsTotalCount = $(table).pageInfo.recordsTotal;
                    pageDictionary($(table + '_wrapper'), rowsTotalCount);
                    showTotalPage($(table + '_wrapper'), rowsTotalCount);
                }
                initPSGrid($(table + '_wrapper'));
            },
            customProcessing: true,
            "aaSorting": [[sortingCount, "desc"]],
            "bLengthChange": true,
            "iDisplayLength": 10,
            "aoColumns": columns,
            "scrollX": true,
            "autoWidth":true,
            "scrollY":        '50vh',
            "scrollCollapse": true,
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc'],
            },
                {
                    "targets": 3,
                    "type": 'date'
                }]
        });
        return activity_table;
    };

    var create_activity_column_data = function(appType) {
        var aoColumns = [
            {
                "mData": "type",
                "mRender": function(data, type, row) {
                    if(row.type=='PECDissociated'){
                        return 'PEC Dissociated'
                    } else if(row.type=='PECAssociated') {
                        return 'PEC Associated'
                    } else if(row.type == 'RMMAdded'){
                        return 'RMM Added'
                    } else if(row.type == 'RMMUpdated'){
                        return 'RMM Updated'
                    } else if(row.type == 'RMMDeleted'){
                        return 'RMM Deleted'
                    }
                    return signal.utils.breakIt(row.type)
                },
                'className': 'col-min-150 col-max-150'
            },
            {
                "mData": "details",
                "mRender": function (data, type, row) {
                    var result ="";
                    if (row.justification) {
                        result = escapeAllHTML(row.details) + " -- with Justification '" +escapeAllHTML(row.justification) + "'"; //bugFix-PVS-72241
                    } else {
                        result = escapeAllHTML(row.details);
                    }
                    var colElement = '<div class="col-container"><div class="col-height textPre">';
                    colElement += result;
                    colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + result + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                    colElement += '</div></div>';
                    return colElement;
                },
                'className': 'col-min-200 col-max-200 cell-break'
            },
            {
                "mData": "performedBy",
                "mRender": function (data, type, row) {
                    if(row.performedByDept && row.performedByDept != '') {
                        return escapeHTML(row.performedBy + " (" + row.performedByDept + ")")
                    } else{
                        return row.performedBy
                    }
                },
                'className': 'col-min-100 col-max-100'
            },
            {
                "mData": "timestamp",
                "mRender": function(data, type, row) {
                    if (appType == ALERT_CONFIG_TYPE.AGGREGATE_CASE_ALERT || appType == ALERT_CONFIG_TYPE.EVDAS_ALERT || appType == ALERT_CONFIG_TYPE.SINGLE_CASE_ALERT) {
                        return moment(row.timestamp).tz(userTimeZone).format('DD-MMM-YYYY hh:mm:ss A');
                    } else {
                        return row.timestamp;
                    }

                },
                'className': 'col-min-150 col-max-150'
            }
        ];
        if(appType == 'Single Case Alert') {
            aoColumns.splice(0,0,{
                "mData": "caseNumber",
                'className': 'col-min-150 col-max-150'
            });
            aoColumns.splice(2,0,{
                "mData": "suspect",
                'className': 'col-min-200 col-max-200'
            });
            aoColumns.splice(3,0,{
                "mData": "eventName",
                'className': 'col-min-150 col-max-150'
            });
            aoColumns.splice(5,0,{
                "mData": "currentAssignment",
                "mRender": function (data, type, row) {
                    var currAssignment
                    if (row.currentAssignmentDept && row.currentAssignmentDept != '') {
                        currAssignment = row.currentAssignment + " (" + row.currentAssignmentDept + ")"
                    } else {
                        currAssignment = (row.currentAssignment)
                    }
                    return escapeHTML(currAssignment)
                },
                'className': 'col-min-150 col-max-150'
            });
            sortingCount = 7
        }else if(appType == 'Literature Search Alert') {
            aoColumns.splice(0,0,{
                "mData": "articleId",
                'className': 'col-min-100 col-max-100'
            });
            aoColumns.splice(2,0,{
                "mData": "searchString",
                'className': 'col-min-100 col-max-100'
            });
            aoColumns.splice(3,0,{
                "mData": "productName",
                'className': 'col-min-200 col-max-200'
            });
            aoColumns.splice(4,0,{
                "mData": "eventName",
                'className': 'col-min-150 col-max-150'
            });
            aoColumns.splice(6,0,{
                "mData": "currentAssignment",
                "mRender": function (data, type, row) {
                    var currAssignment
                    if (row.currentAssignmentDept && row.currentAssignmentDept != '') {
                        currAssignment = row.currentAssignment + " (" + row.currentAssignmentDept + ")"
                    } else {
                        currAssignment = (row.currentAssignment)
                    }
                    return escapeHTML(currAssignment)
                },
                'className': 'col-min-150 col-max-150'
            });
            sortingCount = 8
        } else if (appType == 'Aggregate Case Alert' || appType == 'EVDAS Alert') {
            aoColumns.splice(1,0,{
                "mData": "suspect",
                'className': 'col-min-150 col-max-150'
            });
            aoColumns.splice(2,0,{
                "mData": "eventName",
                'className': 'col-min-150 col-max-150'
            });
            aoColumns.splice(4,0,{
                "mData": "currentAssignment",
                "mRender": function (data, type, row) {
                    var currAssignment
                    if (row.currentAssignmentDept && row.currentAssignmentDept != '') {
                        currAssignment = row.currentAssignment + " (" + row.currentAssignmentDept + ")"
                    } else {
                        currAssignment = (row.currentAssignment)
                    }
                    return escapeHTML(currAssignment)
                },
                'className': 'col-min-150 col-max-150'
            });
            sortingCount = 6
        }
        return aoColumns
    };

    var reload_activity_table = function() {

        if (typeof activity_table != "undefined" && activity_table != null) {
            activity_table.ajax.reload();
        } else {
            console.log("Unable to reload the activity table. Please refresh the page.")
        }

    };

    var process_description = function(descriptionStr) {
        if (typeof descriptionStr != "undefined" && descriptionStr != null) {
            var descriptions = descriptionStr.split('|');
            //var formattedString = ''
            var formattedString =  document.createElement('div');
            for (var index = 0; index<descriptions.length; index++) {
                var description = descriptions[index];
                var descSpan = document.createElement('span');
                descSpan.innerHTML = escapeHTML(description);
                descSpan.innerHTML = descSpan.innerHTML.replace(/\n/g, '<br>')
                formattedString.appendChild(descSpan);
                formattedString.appendChild(document.createElement('br'));
            }
            return formattedString.innerHTML;
        }
    };

    return {
        init_activities_table : init_activities_table,
        process_description : process_description,
        reload_activity_table : reload_activity_table
    }
})();

