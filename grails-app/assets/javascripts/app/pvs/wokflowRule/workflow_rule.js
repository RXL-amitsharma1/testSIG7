$(function () {
    var initWorflowRuleTable = function () {
        var columns = create_worflow_rule_table_columns();
        var table = $('#worflowRuleTable').DataTable({

            responsive: true,
            language: {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json"
            },
            search: {
                smart: false
            },
            layout: {
                topStart: ['pageLength', {search: {label: 'Search:'}}],
                topEnd: null,
                bottomStart: ['info', {paging: {type: "full_numbers"}}],
                bottomEnd: null,
            },
            customProcessing: true,
            "ajax": {
                "url": workflowListUrl,
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
            drawCallback: function (settings) {
                tagEllipsis($("#worflowRuleTable"));
                colEllipsis();
                webUiPopInit();
            },
            "initComplete": function () {
                if (isAdmin) {
                    var buttonHtml = '<a class="btn btn-primary m-r-15" href="' + createUrl + '" >' + "New Workflow Rule" + '</a> ';
                    var $divToolbar = $('.dt-search');
                    $divToolbar.prepend(buttonHtml);
                }
                addGridShortcuts(this);
            },
            "aaSorting": [],
            "bLengthChange": true,
            "bStateSave": true,
            "iDisplayLength": 10,
            "aLengthMenu": [[10, 50, 100, 200, -1], [10, 50, 100, 200, "All"]],
            "aoColumns": columns,
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }]
        });

    };

    var create_worflow_rule_table_columns = function () {
        var aoColumns = [
            {
                "className":"col-min-200 col-min-200",
                "mRender": function (data,type,row) {
                    return '<a href="' + 'edit' + '?' + 'id=' + row.id + '">' + encodeToHTML(row.name)+ '</a>';
                }
            },
            {
                "mData": "description",
                "className":"col-min-250 col-max-250 cell-break textPre",
                "mRender": function(data, type, row) {
                    var description= ''
                    if(row.description){
                        description = "<span>" + addEllipsisForDescriptionText(encodeToHTML(row.description)) + "</span>";
                    }
                    return description;
                }
            },
            {
                "mData":"incomingDisposition",
                "className":"col-min-200 col-max-200"
            },
            {
                "mData": "targetDisposition",
                "className":"col-min-200 col-max-200"
            },
            {
                "mData": "workflowGroups",
                "className":"col-min-200 col-max-200"
            },
            {
                "mData": "allowedUserGroups",
                "className":"col-min-200 col-max-200"
            },
            {
                "mRender": function (data, type, row) {
                    if (row.display) {
                        return "<span>Yes</span>"
                    } else {
                        return "<span>No</span>"
                    }
                }
            }
        ];

        return aoColumns
    };


    initWorflowRuleTable();



});