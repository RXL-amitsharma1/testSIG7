var productSelected;
var productGroupSelected;
var dataSourceProducts;

$(function () {
    var emergingTable
    if($("#productDictDataSource").val() && $("#productDictDataSource").val().length){
        var dataSourceList = $("#productDictDataSource").val().split(';')
        $.each(dataSourceList,function(key,val){
           if(key>0){
               productGroupDataSources.push(val);
           }else{
               dataSourceProducts = val;
           }
        });
    }
    function showCommentPopup(currRow, index, label){
        var extTextAreaModal = $("#important-event1");
        extTextAreaModal.find('.eventValue').html(currRow);
        extTextAreaModal.find('.modal-title').text(label);
        extTextAreaModal.modal("show");
    }
    $(document).on('click', '.ico-dots', function (e) {
        var currRow = $(this).parent().find("a").attr("more-data")
        var columNum = parseInt($(this).parent().parent().parent().index())
        showCommentPopup(currRow, 3, $("#stopListTable").find("th:eq('"+columNum+"')").text());
    });


    productSelected = $('#productSelectionAssessment').val();
    productGroupSelected = $('#productGroupSelectionAssessment').val();

    if(isDataSourceEnabled) {
        $("#dataSourcesProductDictAssessment").closest(".row").show();
    }

    $("#emerging-dropdown").mouseover(function () {
        $(".ul-ddm").show();
        $(".emerging-export").show();
    });

    $("#emerging-dropdown").mouseout(function() {
         $(".ul-ddm").hide();
        $(".emerging-export").hide();
    });

    var getEventExistIcon = function (value) {
        if (value === true) {
            return '<span class="glyphicon glyphicon-ok"></span>'
        } else {
            return '<span class="glyphicon glyphicon-remove"></span>'
        }
    }

    var initalertStoplistTable = function () {
        var columns = create_emerging_issue_table_columns();
        emergingTable = $('#stopListTable').DataTable({

            responsive: true,
            language: {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json"
            },
            layout: {
                topStart: null,
                topEnd: 'search',
                bottomStart: ['pageLength', 'info', {paging: {type: "full_numbers"}}],
                bottomEnd: null,
            },
            customProcessing: true,
            drawCallback: function (settings) {
                pageDictionary($('#stopListTable_wrapper'),settings.json.recordsFiltered);
                showTotalPage($('#stopListTable_wrapper'),settings.json.recordsFiltered);
                tagEllipsis($('#stopListTable'));
                colEllipsis();
                webUiPopInit();
                closePopupOnScroll();
                enterKeyAlertDetail();
            },
            "initComplete": function () {
                $(document).on("click", '.deleteImportantIssue', function (event) {
                    event.preventDefault();
                    var url = $(this).attr('href');
                    bootbox.confirm({
                        message: $.i18n._('deleteThis'),
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
                                })
                                    .done(function (data) {
                                        if (data.status) {
                                            window.location.href = indexUrl
                                        }
                                    });
                            }
                        }
                    });
                });
                addGridShortcuts('#stopListTable');
            },
            "ajax": {
                "url": stopListUrl,
                "dataSrc": "aaData",
            },
            processing: true,
            serverSide: true,
            "bLengthChange": true,
            "iDisplayLength": 100,
            "aLengthMenu": [[100, 200, 500, 1000], [100, 200, 500, 1000]],
            "bProcessing": true,
            "oLanguage": {
                "oPaginate": {
                    "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                    "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                    "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                    "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
                },
                "sLengthMenu": "Show _MENU_",
                "sZeroRecords": "No data available in table", "sEmptyTable": "No data available in table"
            },
            "aaSorting": [[0, "desc"]],
            "bSort": false,
            "aoColumns": columns,
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }]
        });
        actionButton("#stopListTable");
        return emergingTable
    };

    var create_emerging_issue_table_columns = function () {
        var aoColumns = [
            {
                "mData": "productNames",
                "className":"col-min-300 col-max-400 cell-break",
                "mRender": function (data, type, row) {
                    var display = ""
                    if(row.productGroupSelection){
                        display = display +  "<div><b>" + "Product Group"+ "</b>: " + row.productGroupSelection + "</div>"
                    }
                    for (var label in row.productSelection) {
                        display = display + "<div><b>" + label + "</b>: " + row.productSelection[label] + "</div>"
                    }
                    var colElement = '<div class="col-container"><div class="col-height">';
                    colElement += display
                    colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + display + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                    colElement += '</div></div>';
                    return colElement
                },
            },
            {
                "mData": "eventName",
                "className":"col-min-300 col-max-400 cell-break",
                "mRender": function (data, type, row) {
                    var display = ""
                    if(row.eventGroupSelection){
                        display = display +  "<div><b>" + "Event Group"+ "</b>: " + row.eventGroupSelection + "</div>"
                    }
                    for (var label in row.eventName) {
                        display = display + "<div><b>" + label.toUpperCase() + "</b>: " + row.eventName[label].join(', ') + "</div>"
                    }
                    var colElement = '<div class="col-container"><div class="col-height">';
                    colElement +=  display
                    colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + display + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                    colElement += '</div></div>';
                    return colElement
                }
            },
            {
                "mData": 'ime',
                "mRender": function (data, type, row) {
                    return getEventExistIcon(row.ime);
                }
            },
            {
                "mData": 'dme',
                "mRender": function (data, type, row) {
                    return getEventExistIcon(row.dme);
                }
            },
            {
                "mData": 'emergingIssue',
                "mRender": function (data, type, row) {
                    return getEventExistIcon(row.emergingIssue);
                }
            },
            {
                "mData": 'specialMonitoring',
                "mRender": function (data, type, row) {
                    return getEventExistIcon(row.specialMonitoring);
                }
            },
            {
                "mData": 'modifiedBy',
                "className":"col-min-150"
            },
            {
                "mData": 'lastUpdated',
                "className":"col-min-100",
                "mRender": function (data, type, row) {
                    if(row.lastUpdated=="null" || row.lastUpdated==null){
                        return '' ;
                    } else {
                        return moment.utc(row.lastUpdated).format('DD-MMM-YYYY hh:mm:ss A');
                    }
                }
            }
        ];
        if (isAdmin) {
            aoColumns.push.apply(aoColumns, [{

                "mRender": function (data, type, row) {
                    var deleteUrl = deleteImportantIssuesUrl + "/" + row.id;
                    var editActionUrl = editImportantIssuesUrl + "/" + row.id;
                    data = row;
                    var actionButton = '<div class="hidden-btn btn-group dropdown dataTableHideCellContent"> \
                            <a class="btn btn-success btn-xs editImportantIssues" href="' + editActionUrl + '" >' + "Edit" + '</a> \
                            <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                                <span class="caret"></span> \
                                <span class="sr-only">Toggle Dropdown</span> \
                            </button> \
                            <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                                <li role="presentation"><a class="deleteImportantIssue" data-name="' + row.name + '"  data-version="' + row.version + '" data-id="' + row.id + '" ' +
                        'role="menuitem" href="' + deleteUrl + '" >' + 'Delete' + '</a></li>\
                             </ul></div>';
                    return actionButton;
                }
            }]);
        }
        return aoColumns;
    };

    $('#stopListTable').on('switchChange.bootstrapSwitch', 'input[name="activated"]', function (event, state) {
        event.preventDefault()
        event.stopPropagation();
        var $this = $(this)
        var id = $this.attr('listId')
        var token = $("meta[name='_csrf']").attr("content");
        var header = $("meta[name='_csrf_header']").attr("content");
        var url = updateListUrl + "?id=" + id + "&activated=" + state
        $.ajax({
            url: url,
        }).done(function (data) {
            emergingTable.ajax.reload()
        }).fail(function (data) {
        })
    });

    initalertStoplistTable()
    saveEmergingIssue();
})

function saveEmergingIssue(){
    $(".save-emerging").on('click', function (event) {

        // the below if condition is introduced to make a check on ui which ensure the event details is present since it is mandatory field.
        if ((!$('.eventGroupSelected').val() || $('.eventGroupSelected').val().toString() === "[]") && $('.eventSelected').val().length === 0) {
            event.preventDefault();
            $.Notification.notify('error', 'top right', "Failed", 'Please enter all the mandatory fields.', {autoHideDelay: 5000});
        } else {
            var dataSourceDict = ""
            if (productSelected != $('#productSelectionAssessment').val()) {
                dataSourceDict = $("#dataSourcesProductDict").val() + ';' + productGroupDataSources.join(';');
            } else {
                dataSourceDict = dataSourceProducts + ';' + productGroupDataSources.join(';');
            }
            $("#productDictDataSource").val(dataSourceDict);
            $(this).attr('disabled', true);
            $('#alertStopListForm').submit();
        }
    })
}