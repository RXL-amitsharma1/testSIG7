var justificationTable;
var options = {backdrop: 'static', keyboard: false};
$(function () {
    var initJustificationTable = function () {
        var columns = create_jus_table_columns();
        justificationTable = $("#justificationListTable").DataTable({

            layout: {
                topStart: ['pageLength', 'search'],
                topEnd: null,
                bottomStart: ['info', {paging: {type: "full_numbers"}}],
                bottomEnd: null,
            },
            customProcessing: true,
            responsive: true,
            language: {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json"
            },
            search: {
                smart: false
            },
            "ajax": {
                "url": justificationListUrl,
                "dataSrc": ""
            },
            "initComplete": function () {
                if (isAdmin) {
                    var buttonHtml = '<button type="button" class="btn btn-primary" id="addJustification" data-toggle="modal">Add Justification</button> ';
                    var $divToolbar = $('.dt-search');
                    $divToolbar.prepend(buttonHtml);
                }
                addGridShortcuts(this);
            },
            "oLanguage": {
                "oPaginate": {
                    "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                    "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                    "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                    "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
                },
            },

            "aaSorting": [[3, "desc"]],
            "bLengthChange": true,
            "iDisplayLength": 10,
            "aLengthMenu": [[10, 50, 100, 200, -1], [10, 50, 100, 200, "All"]],
            "aoColumns": columns,
            columnDefs: [{
                "targets": -1,
                "sortable": false,
                "render": $.fn.dataTable.render.text(),
            },
                {
                    "targets": 3,
                    "type": 'date'
                },
                {
                    "targets": '_all',
                    orderSequence: ['desc', 'asc']
                }]
        });
        actionButton("#justificationListTable");
    };

    var create_jus_table_columns = function () {
        var aoColumns = [
            {
                "mData": "name",
                "className":"col-min-150 col-max-150",
                "mRender": function(data, type, row) {
                    var name= ''
                    if(row.name){
                        name = "<span>" + encodeToHTML(escapeHTML(row.name)) + "</span>";
                    }

                    return name;
                }
            },
            {
                "mData": "justificationText",
                "class": "textPre",
                "mRender": function(data, type, row) {
                    var justificationText= ''
                    if(row.justificationText){
                        justificationText = "<span>" + encodeToHTML(escapeHTML(row.justificationText)) + "</span>";
                    }

                    return justificationText;
                }
            },
            {
                "className":"col-min-150 col-max-150",
                "mRender": function (data, type, row) {
                    var display = ""
                    for (var i = 0; i < row.features.length; i++) {
                        display = display + "<span>" + row.features[i] + "</span></br>"
                    }
                    return display
                }
            },
            {
                "mData": "lastUpdated",
                "className":"col-min-150",
            },
            {
                "mData": "modifiedBy",
                "className":"col-min-150 col-max-150"
            }
        ];
        if (isAdmin) {
            aoColumns.push.apply(aoColumns, [{
                "className":"col-min-100 col-max-100",
                "mRender": function (data, type, row) {
                    var url = $("#deleteJustificationUrl").val() + "/" + row.id;
                    var editJustificationUrl = $("#editJustificationUrl").val() + "/" + row.id;
                    data = row;
                    var actionButton = '<div class="hidden-btn btn-group dropdown dataTableHideCellContent" align="center"> \
                            <a oncontextmenu="return false" class="btn btn-success btn-xs editJustification" href="' + editJustificationUrl + '" >' + "Edit" + '</a> \
                            <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                                <span class="caret"></span> \
                                <span class="sr-only">Toggle Dropdown</span> \
                            </button> \
                            <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                                <li role="presentation"><a oncontextmenu="return false" class="deleteJustification" data-name="' + escapeHTML(row.name) + '"  data-version="' + row.version + '" data-id="' + row.id + '" ' +
                        'role="menuitem" href="' + url + '" >' + 'Delete' + '</a></li>\
                             </ul></div>';
                    return actionButton;
                }
            }]);
        }
        return aoColumns
    };

    initJustificationTable();

    function closeModal() {
        $('.modal').modal('hide');
    }

    $(document).on('click', '#addJustification', function () {
        $('#justificationModalHeader').html("Create Justification");
        $("#justificationForm")[0].reset();
        var $successAlert = $('.modal-justification .alert-success');
        if ($successAlert.length) {
            $successAlert.remove();
        }
        var $modal = $('.modal-justification');
        $modal.modal(options);
        $('.linkedDisposition').hide();
        $('#linkedDisposition').val(null).trigger('change');
    });

    $(document).on('submit', '#justificationForm', function (event) {
        event.preventDefault();
        $('.saveJustificationButton').attr('disabled', true);
        var data = $(this).serialize();
        var url = $(this).attr('action');//+"?"+data;
        $.ajax({
            type: "POST",
            url: url,
            data: data,
            dataType: 'json',
        }).done(saveJustificationSuccessCallback)
    });

    var saveJustificationSuccessCallback = function (data) {
        if(data.message === "Justification added successfully" && $('#justificationModalHeader').html() === "Edit Justification")
        {
            data.message = "Justification updated successfully"
        }
        $('.saveJustificationButton').attr('disabled', false);
        if (data.status) {
            closeModal();
            $("#justificationId").val(null);
            justificationTable.ajax.reload();
            showSuccessMsg(data.message);
        } else {
            var $alert = $(".msgContainer").find('.alert');
            $alert.find('.message').html(data.message);
            $(".msgContainer").show();

            $alert.alert();
            $alert.fadeTo(2000, 500).slideUp(500, function () {
                $alert.slideUp(500);
            });
        }
    };

    $(document).on('click', '.deleteJustification', function (event) {
        event.preventDefault();
        var url = $(this).attr('href');
        bootbox.confirm({
            message: "Are you sure, you want to delete it?",
            buttons: {
                confirm: {
                    label: 'Yes',
                    className: 'btn-primary'
                },
                cancel: {
                    label: 'No',
                    className: 'btn-default'
                }
            },
            callback: function (result) {
                if (result) {
                    $.ajax({
                        type: "GET",
                        url: url,
                        dataType: 'json',
                    }).done(deleteJustificationSuccessCallback);
                }
            }
        });

    });
    function removeExistingMessageHolder() {
        $('.messageContainer').html("");
    }

    function showSuccessMsg(msg) {
        removeExistingMessageHolder();
        var successMsg = '<div class="alert alert-success alert-dismissible" role="alert"> ';
        successMsg += '<button type="button" class="close" data-dismiss="alert"> ';
        successMsg += '<span aria-hidden="true">&times;</span> ';
        successMsg += '<span class="sr-only"><g:message code="default.button.close.label" /></span> ';
        successMsg += '</button> ' + msg;
        successMsg += '</div>';
        if(msg) {
            $('body .messageContainer').prepend(successMsg);
            $('body').scrollTop(0);
        }
    }
    var deleteJustificationSuccessCallback = function (data) {
        if (data.status) {
            justificationTable.ajax.reload();
            showSuccessMsg(data.message);
        }
    };

    $(document).on('click', '.editJustification', function (event) {
        event.preventDefault();
        var url = $(this).attr('href');
        $.ajax({
            type: "GET",
            url: url,
            dataType: 'json',
        }).done(editJustificationSuccessCallback);
    });

    var editJustificationSuccessCallback = function (data) {
        if (data.status) {
            removeExistingMessageHolder();
            $('#justificationModalHeader').html("Edit Justification");
            $("#justificationForm")[0].reset();
            var $successAlert = $('.modal-justification .alert-success');
            if ($successAlert.length) {
                $successAlert.remove();
            }
            $('#linkedDisposition').val(null).trigger('change');
            $('.modal-justification').modal(options);
            populateFormData(data.data);
        }
    };

    function populateFormData(data) {
        var $form = $("#justificationForm");
        $form.find("#justificationId").val(data.id);
        $form.find("#justificationName").val(data.name);
        $form.find("#justificationText").val(data.justificationText);
        var arr = data.features;
        for (var i = 0, l = arr.length; i < l; i++) {
            switch (arr[i]) {
                case "Alert Workflow":
                    $("#alertWorkflow").prop("checked", true);
                    break;
                case "Signal Workflow":
                    $("#signalWorkflow").prop("checked", true);
                    break;
                case "Topic Workflow":
                    $("#topicWorkflow").prop("checked", true);
                    break;
                case "Alert Priority":
                    $("#alertPriority").prop("checked", true);
                    break;
                case "Signal Priority":
                    $("#signalPriority").prop("checked", true);
                    break;
                case "Topic Priority":
                    $("#topicPriority").prop("checked", true);
                    break;
                case "Case Addition":
                    $("#caseAddition").prop("checked", true);
                    break;
                default:
            }
        }
        if($("#alertWorkflow").prop("checked") || $("#signalWorkflow").prop("checked")){
            $('.linkedDisposition').show();
            $('#linkedDisposition').val(data.dispositions);
            $('#linkedDisposition').trigger('change');
        } else{
            $('.linkedDisposition').hide();
        }


    }
    $('.linkedDisposition').hide();

    $("#alertWorkflow, #signalWorkflow").on('click', function () {
        if($("#alertWorkflow").prop("checked") || $("#signalWorkflow").prop("checked")){
            $('.linkedDisposition').show();
        }
        else
            $('.linkedDisposition').hide();
    });

    $("#linkedDisposition").select2().data('select2').$dropdown.addClass('cell-break');
});