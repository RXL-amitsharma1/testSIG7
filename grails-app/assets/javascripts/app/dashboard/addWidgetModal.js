$(function () {
    $('#addWidgetModal').on('show.bs.modal', function(event) {
        if (!$.fn.DataTable.isDataTable('#rxTableConfiguration')) {
            var table = $('#rxTableConfiguration').DataTable({

                "language": {
                    "url": "../assets/i18n/dataTables_" + userLocale + ".json"
                },
                "processing": true,
                "serverSide": true,
                "ajax": {
                    "url": CONFIGURATION.listUrl,
                    "dataSrc": "data",
                    "data": function (d) {
                        d.searchString = d.search.value;
                        if (d.order.length > 0) {
                            d.direction = d.order[0].dir;
                            //Column header mData value extracting
                            d.sort = d.columns[d.order[0].column].data;
                        }
                        d.templateType = CONFIGURATION.templateType;
                        d.showChartSheet = true;
                    }
                },
                "oLanguage": {
                    "oPaginate": {
                        "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                        "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                        "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                        "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
                    },
                },
                "aaSorting": [],
                "order": [[3, "desc"]],
                "bLengthChange": true,
                "iDisplayLength": 5,
                "aLengthMenu": [[5, 10, 15], [5, 10, 15]],
                "aoColumns": [
                    //Don't Change mData labels as we are using it for our sorting parameter name for sorting data should be property name
                    {
                        "mData": "reportName",
                        mRender: function (data, type, row) {
                            return data.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
                        }
                    },
                    {
                        "mData": "description",
                        mRender: function (data, type, row) {
                            var text = (data == null) ? '' : data.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
                            return '<div class="comment">'
                                + text +
                                '</div>';
                        }
                    },
                    {
                        "mData": "numOfExecutions",
                        "sClass": "dataTableColumnCenter"
                    },
                    {"mData": "createdBy"},
                    {
                        "mData": "dateCreated",
                        "aTargets": ["dateCreated"],
                        "sClass": "dataTableColumnCenter",
                        "mRender": function (data, type, full) {
                            return moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT);
                        }
                    }
                ]
            }).on('draw.dt', function () {
                setTimeout(function () {
                    $('#rxTableConfiguration tbody tr').each(function () {
                        $(this).find('td:eq(5)').attr('nowrap', 'nowrap');
                        $(this).find('td:eq(6)').attr('nowrap', 'nowrap');
                    });
                }, 100);
            }).on('xhr.dt', function (e, settings, json, xhr) {
                checkIfSessionTimeOutThenReload(e, json)
            }).on('click', 'tr', function () {
                $('#addWidgetModal').modal('hide');
                window.location = CONFIGURATION.addWidgetUrl + "?" + $.param({widgetType: 'CHART', id: table.row(this).data()['id']})
            });
        }
    });
    //actionButton('#rxTableConfiguration');
    //loadTableOption('#rxTableConfiguration');
});

function disableEventBinding(eventElement) {
    $(eventElement).on('click', function (e) {
        if ($(this).attr("disabled") == "disabled") {
            e.preventDefault();
        }
    });
    $(eventElement).attr("disabled", "disabled");
}
