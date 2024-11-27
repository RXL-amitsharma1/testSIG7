<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.ReportLibrary.title"/></title>
    <asset:javascript src="app/pvs/dataTablesActionButtons.js"/>
    <asset:javascript src="yadcf/jquery.dataTables.yadcf.js"/>
    <asset:javascript src="app/pvs/common/rx_common.js"/>
    <asset:javascript src="app/pvs/common/rx_list_utils.js"/>
    <asset:javascript src="app/pvs/common/rx_handlebar_ext.js"/>
    <asset:stylesheet src="jquery-ui/jquery-ui.css"/>
    <asset:stylesheet src="yadcf/jquery.dataTables.yadcf.css"/>
    <g:javascript>
        var listUrl ="${createLink(controller: 'alertTag', action: 'listTags')}";
        var sysTagListUrl ="${createLink(controller: 'alertTag', action: 'listSystemTags')}";
        var deleteUrl = "${createLink(controller: 'alertTag', action: 'removeAlertTag')}";
        var sysTagDeleteUrl = "${createLink(controller: 'alertTag', action: 'removeSystemTag')}";
        var editUrl = "${createLink(controller: 'alertTag', action: 'editTag')}";
        var sysTagEditUrl = "${createLink(controller: 'alertTag', action: 'editSystemTag')}";
        var saveUrl = "${createLink(controller: 'alertTag', action: 'saveAlertTag')}";
        var sysTagSaveUrl = "${createLink(controller: 'alertTag', action: 'saveSystemTag')}";
    </g:javascript>
</head>

<body>
<script>
    $(document).ready(function () {
        var table = $('#alertTagsList').DataTable({
            "sPaginationType": "bootstrap",
            "language": {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json"
            },
            fnInitComplete: function () {
                addGridShortcuts(this);
                //edit existing tag name
                $('.editTag').on('click', function () {
                    var parent_row = $(event.target).closest('tr');
                    var tagId = parent_row.find('.tagName').attr("data-id");
                    var tagName = parent_row.find('.tagName').html();
                    $('#alert-tag-edit-modal #alertTagName').val(tagName);
                    $('#alert-tag-edit-modal #alertTagId').val(tagId);
                    $('#alert-tag-edit-modal #editAlertTag').show();
                    $('#alert-tag-edit-modal').modal('show');
                });
                //delete existing tag
                $('.deleteTag').on('click', function () {
                    var parent_row = $(event.target).closest('tr');
                    var tagId = parent_row.find('.tagName').attr("data-id");
                    var request = new Object();
                    request['id'] = tagId;
                    $.ajax({
                        url : deleteUrl,
                        type: "POST",
                        data: request,
                        dataType: "json",
                        success : function (data) {
                            if(data.success){
                                location.reload();
                            }else{
                                $('#tag-delete-modal .error-message').html(data.errorMessage)
                                $('#tag-delete-modal').modal('show')
                            }
                        }
                    })
                })
            },
            "ajax": {
                "url": listUrl,
                "dataSrc": ""
            },
            "aaSorting": [],
            "bLengthChange": true,
            "iDisplayLength": 50,
            "bAutoWidth": false,
            "aLengthMenu": [[50, 100, 200, 500], [50, 100, 200, 500]],
            "scrollY":"calc(100vh - 261px)",
            "aoColumns": [
                {
                    "mData": "name",
                    "mRender": function (data, type, row) {
                        return "<span class='tagName' data-id='"+row.id+"'>" + escapeHTML(row.name) + "</span>"
                    }
                },
                {
                    "mData": "createdBy",
                },
                {
                    "mData": "dateCreated",
                    "aTargets": ["dateCreated"],
                    "sClass": "dataTableColumnCenter",
                    "mRender": function (data, type, row) {
                        var dateCreated = row.dateCreated
                        return moment(dateCreated).tz(userTimeZone).format('lll');
                    }
                },
                {
                    "mData": null,
                    "bSortable": false,
                    "mRender": function (data, type, row) {
                        var actionButtonContent = '<div class="hidden-btn btn-group dropdown dataTableHideCellContent">'+
                            '<a class="btn btn-success btn-xs editTag" href="#">Edit</a>'+
                            '<button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown">'+
                            '<span class="caret"></span>'+
                            '<span class="sr-only">Toggle Dropdown</span></button>'+
                        '<ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;">'+
                            '<li role="presentation"><a role="menuitem" href="#" class="deleteTag">Delete</a></li></ul></div>';
                        return actionButtonContent
                    },
                    'className':'col-min-50 col-max-100'
                }
            ],
            scrollX: true
        });
        loadTableOption('#alertTagsList');

        //save new tag
        $('.create-systemTag').on('click', function () {
            $('#tag-create-modal #saveSystemTag').show()
            $('#tag-create-modal').modal('show')
        })

        $('.create-alertTag').on('click', function () {
            $('#tag-create-modal #saveAlertTag').show()
            $('#tag-create-modal').modal('show')
        })

    });
</script>

%{--Tag Table--}%
<div class="panel panel-default rxmain-container rxmain-container-top m-b-0">
    <g:render template="/includes/layout/flashErrorsDivs"/>

    <div class="panel-heading pv-sec-heading">
        <div class="row">
            <div class="col-md-7">
                <span class="panel-title">${message(code: "app.label.tags")}</span>
            </div>
            <div class="col-md-5 ico-menu">

                <span id="dropdownMenu2" data-title="${message(code: "app.label.addremovecolumn")}" data-toggle="dropdown" class="pull-right rxmain-dropdown-settings m-t-3" tabindex="0"><i class="mdi mdi-format-list-bulleted font-24"></i></span>
                <div class="pull-right dropdown-menu" aria-labelledby="dropdownMenu2">
                    <div class="rxmain-container-dropdown">
                        <div>
                            <table id="tableColumns2" class="table table-condensed rxmain-dropdown-settings-table">
                                <thead><tr><th>${message(code: 'app.label.name')}</th><th>${message(code: 'app.label.show')}</th>
                                </tr></thead>
                            </table>
                        </div>
                    </div>
                </div>
                <span>
                    <span tabindex="0" class="pull-right grid-menu-tooltip create-alertTag m-r-10" data-title="${message(code: 'app.label.createalerttag')}">
                        <i class="mdi mdi-plus-box blue-1 font-24"></i></a>
                </span>
            </div>
        </div>
    </div>

    <div class="row rxmain-container-content">
        <table id="alertTagsList" class="row-border hover" width="100%">
                <thead>
                <tr>
                    <th class="nameColumn">
                        <div class="th-label"><g:message code="app.label.name"/></div></th>
                    </th>
                    <th class="createdBy">
                        <div class="th-label">
                            <g:message code="app.label.createdBy"/>
                        </div>
                    </th>
                    <th>
                        <div class="th-label">
                            <g:message code="app.label.dateCreated"/>
                        </div>
                    </th>
                    <th>
                        <div class="th-label">
                            <g:message code="app.label.action"/>
                        </div>
                    </th>
                </tr>
                </thead>
            </table>
    </div>
</div>
<g:render template="/includes/modals/alert_tag_edit_modal"/>
<g:render template="/includes/modals/tag_create_modal"/>
</body>