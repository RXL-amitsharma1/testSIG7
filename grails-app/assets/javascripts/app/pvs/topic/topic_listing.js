$(function () {

    var genAlertNameLink = function (topicId, topicName) {
        return '<a href="' + '/signal/topic/details' + '?' + 'id=' + topicId + '">' + topicName + '</a>';
    };

    var getTableElement = function (toolbar) {
        var radios = '<div class="">';
        radios = radios + '&nbsp;&nbsp;&nbsp;<a href="createTopic" class="create btn btn-success" id="create-topic" style="float:right">Create Topic</a>';
        radios = $(radios);
        $(toolbar).append(radios);
    }
    var getFilterIcon = function () {
        var right_bar = $('#topicTable_filter').parent();
        right_bar.append('<a href="#" id="toggle-column-filters_topic" title="Enable/Disable Filters" class="pv-ic"><i class="fa fa-filter" aria-hidden="true"></i></a>');
    }

    var initTopicDetailsTable = function () {

        var topicListTable = $('#topicTable').DataTable({
            "language": {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json"
            },

            initComplete: function () {
                var rowDiv = $('<div class="col-xs-9"></div>');
                $('#topicTable_filter').parent().append(rowDiv);

                var theDataTable = $('#topicTable').DataTable()


                $("#toggle-column-filters_topic").on('click', function () {
                    var ele = $("#topicTableContainer").find('.yadcf-filter-wrapper');
                    if (ele.is(':visible')) {
                        ele.hide();
                    } else {
                        ele.show();
                    }
                    theDataTable.draw();
                });
                init_filter(topicListTable);
                $('.yadcf-filter-wrapper').hide();
                theDataTable.draw();
            },

            "ajax": {
                "url": topicListUrl,
                "dataSrc": ""
            },
            "bLengthChange": true,
            "iDisplayLength": 50,
            "bProcessing": true,
            "MSG_LOADING": '',
            "oLanguage": {

                "sZeroRecords": "", "sEmptyTable": "No data available in table",
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
            searching: true,
            "aoColumns": [
                {
                    "mData": "topicName",
                    'className': 'dt-left',
                    "mRender": function (data, type, row) {
                        return genAlertNameLink(row.topicId, row.topicName)
                    }
                },
                {"mData": "productName"},
                {"mData": "noOfPec"},
                {"mData": "noOfCases"},
                {"mData": "assignedTo"},
                {"mData": "priority"},
                {
                    "mData": "actions",
                    'className': ''
                },
                {
                    "mData": "topicId",
                    "sortable": false,
                    'className': '',
                    "mRender": function (data, type, row) {
                        var actionButton = '<div class="hidden-btn btn-group dataTableHideCellContent"> \
                            <a class="btn btn-default" href=' + topicEditUrl + '?topicId=' + data + ' ">' + 'Edit' + '</a> \
                            \</div>';
                        return actionButton;
                    }
                }
            ],
            scrollX: true,
            scrollY: '65vh',
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }]

        });
        actionButton('#topicTable');
        return topicListTable
    };

    var init_filter = function (data_table) {
        var filterOptions = [
            {column_number: 0, filter_type: 'text', filter_reset_button_text: false},
            {column_number: 1, filter_type: 'text', filter_reset_button_text: false},
            {column_number: 2, filter_type: 'text', filter_reset_button_text: false},
            {column_number: 3, filter_type: 'text', filter_reset_button_text: false},
            {column_number: 4, filter_type: 'text', filter_reset_button_text: false},
            {column_number: 5, filter_type: 'text', filter_reset_button_text: false},
            {column_number: 6, filter_type: 'text', filter_reset_button_text: false},
        ]
        yadcf.init(data_table, filterOptions);
        $(".yadcf-filter").removeAttr('placeholder');
    };
    var init = function () {
        initTopicDetailsTable()
    };

    init();

    $('.export_icon_topic a').on('click', function () {
        var format = $(this).data('format')
        var table = $('#topicTable').DataTable();
        var idList = [];
        $.each(table.rows({filter: 'applied'}).data(), function (idx, val) {
            idList.push(val.topicId)
        });
        var exportUrl = topicExportUrl + "?outputFormat=" + format +"&idList="+idList;
        $(this).attr('href', exportUrl);
    });
});