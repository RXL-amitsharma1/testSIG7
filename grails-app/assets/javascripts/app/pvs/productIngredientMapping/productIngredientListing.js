//= require app/pvs/common/rx_common.js

$(function () {
    var table;
    signal.product_ingredient_mapping_list_utils.init_product_ingredient_mapping_table();

});


var signal = signal || {};

signal.product_ingredient_mapping_list_utils = (function () {

    var init_product_ingredient_mapping_table = function () {
        var columns = create_pi_table_columns();
        table = $('#rxTableQueries').DataTable({

            "language": {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json"
            },
            initComplete: function () {
                $('.dt-input').val("");
                var piTable = $('#rxTableQueries').DataTable();
                if(!isAdmin){
                    piTable.buttons().remove();
                }
                addGridShortcuts('#rxTableQueries');
            },
            "ajax": {
                "url": listQueriesUrl,
                "dataSrc": ""
            },
            buttons: [
                {
                    text: 'New Product/Ingredient Mapping',
                    className: 'meeting-create btn-primary',
                    action: function (e, dt, node, config) {
                        window.location.href = productIngredientMappingCreateUrl;
                    }
                }
            ],
            "oLanguage": {
                "oPaginate": {
                    "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                    "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                    "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                    "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
                },
            },
            "aaSorting": [[5, "desc"]],
            "bLengthChange": true,
            "iDisplayLength": 50,
            "aLengthMenu": [[50, 100, 200, 500], [50, 100, 200, 500]],
            "aoColumns": columns,
            scrollX:true,
            "scrollY":"calc(100vh - 256px)",
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }]
        });
        actionButton('#rxTableQueries');
        loadTableOption('#rxTableQueries');
    };

    var create_pi_table_columns = function () {
        var aoColumns = [
            {
                "mData": "otherDataSource",
                "mRender": function (data, type, row) {
                    return (row.otherDataSource).toUpperCase();
                }
            },
            {"mData": "products"},
            {"mData": "pvaProducts"},
            {"mData": "level"},
            {"mData": "modifiedBy"},
            {
                "mData": "lastModified",
                "aTargets": ["lastUpdated"],
                "sClass": "dataTableColumnCenter",
                "mRender": function (data, type, full) {
                    var lastUpdated = new Date(data);
                    return moment(lastUpdated).tz(userTimeZone).format('lll');
                }
            },
            {
                "mData": "enabled",
                "mRender": function (data, type, row) {
                    return data ? "Yes" : "No";
                }
            }
        ];
        if (isAdmin) {
            aoColumns.push.apply(aoColumns, [{
                "mData": null,
                "sortable": false,
                "aTargets": ["id"],
                "className":"col-min-75",
                "mRender": function (data, type, row) {
                    var url = productIngredientMappingEditUrl + '/' + row.id;
                    var toggleEnable = toggleEnableUrl + '?id=' + row.id + '&enabled=' + !row.enabled;
                    var text = row.enabled ? "Disable" : "Enable";
                    var actionButton = '<div class="hidden-btn btn-group dropdown dataTableHideCellContent" align="center"> \
                            <a class="btn btn-success btn-xs" href="' + url + '">Edit</a> \
                            <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                                <span class="caret"></span> \
                                <span class="sr-only">Toggle Dropdown</span> \
                            </button> \
                            <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                                <li role="presentation"><a role="menuitem" href="' + toggleEnable + '">' + text + '</a></li>\
                            </ul> \
                        </div>';
                    return actionButton;
                }
            }]);
        }
        return aoColumns;
    };


    return {
        init_product_ingredient_mapping_table: init_product_ingredient_mapping_table
    }
})();