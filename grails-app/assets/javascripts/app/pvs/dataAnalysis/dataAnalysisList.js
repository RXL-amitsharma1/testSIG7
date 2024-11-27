$(function () {
    var table = $('#rxTableSpoftfireFiles').DataTable({
        layout: {
            topStart: ['pageLength', {search: {placeholder: ''}}],
        },
        "stateSave": false,
        "stateDuration": -1,
        "language": {
            "url": "../assets/i18n/dataTables_" + userLocale + ".json",
        },
        initComplete: function () {
            $('#rxTableSpoftfireFiles tbody tr').each(function () {
                $(this).find('td:eq(1)').attr('nowrap', 'nowrap');
                $(this).find('td:eq(2)').attr('nowrap', 'nowrap');
                $(this).find('td:eq(3)').attr('nowrap', 'nowrap');
                addGridShortcuts('#rxTableSpoftfireFiles');
            });
        },
        "ajax": {
            "url": spotfireFilesListUrl,
            "dataSrc": ""
        },
        "oLanguage": {
            "oPaginate": {
                "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
            },
        },
        "aaSorting": [[2, "desc"]],
        "bLengthChange": true,
        "iDisplayLength": 50,
        "aLengthMenu": [[50, 100, 200, 500], [50, 100, 200, 500]],

        "aoColumns": [
            {"mData": "fileName"},
            {
                "mData": "executionTime",
                "sClass": "dataTableColumnRight padding-right-20",
                "mRender": function (data, type, row) {
                    if (type === "display") {
                        return data && (data.length < 11) ? timeStringToUserTime(data, ':') : 'Not recorded';
                    } else {
                        return data && (data.length < 11) ? timeToSeconds(data) : 0;
                    }
                }
            },
            {
                "mData": "dateCreated",
                "aTargets": ["dateCreated"],
                "sClass": "dataTableColumnCenter forceLineWrapDate",
                "mRender": function (data, type, full) {
                    return moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT);
                }
            },
            {
                "mData": "lastUpdated",
                "aTargets": ["lastUpdated"],
                "sClass": "dataTableColumnCenter forceLineWrapDate",
                "mRender": function (data, type, full) {
                    return moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT);
                }
            },
            {
                "mData": "dateAccessed",
                "aTargets": ["dateAccessed"],
                "sClass": "dataTableColumnCenter forceLineWrapDate",
                "mRender": function (data, type, full) {
                    if (!data || data == undefined || data == "") {
                        return ""
                    }
                    return moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT);
                }
            },
            {
                "mData": null,
                "sClass": "dt-center",
                "sortable": false,
                "aTargets": ["fileName"],
                "mRender": function (data, type, full) {
                    // Added condition for vaers and faers dataSource
                    if(data.isFaersVaersDataSource){
                        var actionButton = '<div class="btn-group dropdown dataTableHideCellContent hidden-btn" align="center"> ' +
                            '<a class="btn btn-success btn-xs" target="_blank" ' +
                            'href="'  + spotfireFileForFaersVaersDataSource + '&fileName=' + libraryRoot + "/" +
                            encodeURI(data["fileName"]) + '&isFaersVaersDataSource=' + data.isFaersVaersDataSource + '">' + $.i18n._('view') + '</a></div>';
                    } else {
                        var actionButton = '<div class="btn-group dropdown dataTableHideCellContent hidden-btn" align="center"> ' +
                            '<a class="btn btn-success btn-xs" target="_blank" ' +
                            'href="' + spotfireFileViewUrl + '&fileName=' + libraryRoot + "/" +
                            encodeURI(data["fileName"]) + '&isFaersVaersDataSource=' + data.isFaersVaersDataSource + '">' + $.i18n._('view') + '</a></div>';
                    }
                    return actionButton;
                }
            },

        ],
        scrollX:true,
        scrollY:'calc(100vh - 252px)',
        columnDefs: [{
            "targets": '_all',
            "render": $.fn.dataTable.render.text(),
            orderSequence: ['desc', 'asc']
        }]
    });
    actionButton('#rxTableSpoftfireFiles');
    loadTableOption('#rxTableSpoftfireFiles');
});

function timeStringToUserTime(timeString, seperator) {
    var rawTime = timeString ? timeString.split(seperator) : [0, 0, 0];
    var hours = parseInt(rawTime[0]);
    var minutes = parseInt(rawTime[1]);
    var seconds = parseInt(rawTime[2]);
    var time = '';
    if (hours === 1) {
        time += hours + $.i18n._('hour');
    } else if (hours !== 0) {
        time += hours + $.i18n._('hours');
    }
    if (minutes === 1) {
        time += " " + minutes + $.i18n._('minute');
    } else if (minutes !== 0) {
        time += " " + minutes + $.i18n._('minutes');
    }
    if (seconds === 1) {
        time += " " + seconds + $.i18n._('second');
    } else if (seconds !== 0) {
        time += " " + seconds + $.i18n._('seconds');
    }
    return time;
}

function timeToSeconds(time) {
    time = time.split(/:/);
    return time[0] * 3600 + time[1] * 60 + time[2];
}
