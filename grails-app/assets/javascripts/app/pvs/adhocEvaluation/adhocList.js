
$(function () {
    var table = $('#rxTableAdhoc').DataTable({

        "language": {
            "url": "../assets/i18n/dataTables_"+userLocale+".json"
        },
        initComplete: function() {
            $('#rxTableQueries tbody tr').each(function(){
                $(this).find('td:eq(5)').attr('nowrap', 'nowrap');
            });

            $('.dt-input').val("");
        },
        "ajax": {
            "url": listUrl,
            "dataSrc": ""
        },
        "aaSorting": [[ 3, "asc" ]],
        "bLengthChange": true,
        "iDisplayLength": 50,
        "aLengthMenu": [[50, 100, 200, 500], [50, 100, 200, 500]],
        "aoColumns": [
            { "mData": "name"},
            { "mData": "description"},
            { "mData": "priority"},
            { "mData": "isPublic",
                "sClass": "dataTableColumnCenter",
                "aTargets": ["isPublic"],
                "mRender": function(data, type, full) {
                    if (data == false) return '<i class="fa fa-lock"></i>';
                    else return null;
                }
            },
            { "mData": "reportType"},
            { "mData": "evalType"},
            { "mData": "createdBy" },
            { "mData": "dateCreated",
                "aTargets": ["dateCreated"],
                "sClass": "dataTableColumnCenter",
                "mRender": function(data, type, full) {
                    var dateCreated = new Date(data);
                    return moment(dateCreated).tz(userTimeZone).format('lll');
                }
            },
            { "mData": "reviewPeriod"},
            { "mData": null,
                "sortable": false,
                "aTargets": ["id"],
                "mRender": function(data, type, full) {
                    var actionButton = '<div class="btn-group dropdown dataTableHideCellContent" align="center"> \
                            <a class="btn btn-success btn-xs" href="' + viewUrl + '/?selectedQuery=' + data["id"] + '">' +$.i18n._('view')+'</a> \
                            <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                                <span class="caret"></span> \
                                <span class="sr-only">Toggle Dropdown</span> \
                            </button> \
                            <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                                <li role="presentation"><a role="menuitem" href="' + editUrl + '/' + data["id"] + '">' +$.i18n._('edit')+'</a></li> \
                                <li role="presentation"><a role="menuitem" href="' + copyUrl + '/' + data["id"] + '">' +$.i18n._('copy')+'</a></li> \
                                <li role="presentation"><a role="menuitem" href="#" data-toggle="modal" \
                                        data-target="#deleteModal" data-instancetype="' + $ .i18n._('adHoc') + '" data-instanceid="' + data["id"] + '" data-instancename="' + data["name"] + '">' +$.i18n._('delete')+'</a></li> \
                            </ul> \
                        </div>';
                    return actionButton;
                }
            }
        ],
        columnDefs: [{
            "targets": '_all',
            "render": $.fn.dataTable.render.text(),
            orderSequence: ['desc', 'asc']
        }]
    });
    actionButton( '#rxTableAdhoc' );
    loadTableOption('#rxTableAdhoc');
});

