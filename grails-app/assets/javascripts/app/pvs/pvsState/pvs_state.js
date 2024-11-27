$(function () {
    var initpvsStateTable = function () {
        var columns = create_pvs_state_table_columns();
        var table = $('#pvsStateTable').DataTable({

            responsive: true,
            language: {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json"
            },
            "ajax": {
                "url": pvsStateListUrl,
                "dataSrc": ""
            },
            "initComplete": function () {
                if (isAdmin) {
                    var buttonHtml = '<a class="btn btn-primary m-r-15" href="' + createUrl + '" >' + "New Workflow State" + '</a> ';
                    var $divToolbar = $('#pvsStateTable_filter');
                    $divToolbar.prepend(buttonHtml);
                }
            },
            "aaSorting": [[3, "desc"]],
            "bLengthChange": true,
            "iDisplayLength": 10,
            "aLengthMenu": [[10, 50, 100, 200, -1], [10, 50, 100, 200, "All"]],
            "aoColumns": columns,
            "scrollX":true,
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }]
        });

    };

    var create_pvs_state_table_columns = function () {
        var aoColumns = [
            {
                "className":"col-min-150",
                "mRender": function (data,type,row) {
                    return '<a href="' + 'edit' + '?' + 'id=' + row.id + '">' + row.value + '</a>';
                }
            },
            {
                "mData": "description",
                "className":"col-min-300 col-max-400"
            },
            {
                "mData": "displayName",
                "className":"col-min-200 col-max-300"
            },
            {
                "mRender": function (data, type, row) {
                    if(row.display){
                        return "<span>Yes</span>"
                    }else{
                        return "<span>No</span>"
                    }
                }
            },
            {
                "mRender": function (data, type, row) {
                    if(row.finalState){
                        return "<span>Yes</span>"
                    }else{
                        return "<span>No</span>"
                    }
                }
            }
        ];

        return aoColumns
    };


    initpvsStateTable();

});