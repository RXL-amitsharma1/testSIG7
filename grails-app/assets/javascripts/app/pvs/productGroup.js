$(function () {
    var productGroupTable;

    $(':input').each(function () {
        if ($(this).parent().find('label').text() == 'Product Group') {
            $(this).attr("disabled", true)
        }
    });
    
    var initProductGroupTable = function () {
        var columns = create_prod_group_table_columns();
        productGroupTable = $('#productGroupTable').DataTable({

            responsive: true,
            language: {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json"
            },
            "ajax": {
                "url": productGroupUrl,
                "dataSrc": ""
            },
            "initComplete": function () {
                addGridShortcuts('#productGroupTable');
            },
            "aaSorting": [[0, "asc"]],
            "bLengthChange": true,
            "iDisplayLength": 10,
            "aLengthMenu": [[10, 50, 100, 200, -1], [10, 50, 100, 200, "All"]],
            "scrollY":"calc(100vh - 451px)",
            "aoColumns": columns
        });
        actionButton('#productGroupTable');
        return productGroupTable
    };
    initProductGroupTable();

    $("button.addProductValues, button.addAllProductValues").off("click").on('click', function (e) {
        var selectedProduct = $(".productDictionaryColWidthCalc").find(".dicLi.selected.selectedBackground")
        var level = selectedProduct.attr("dictionarylevel")
        if (level == undefined) {
            $('.searchProducts').each(function () {
                if ($(this).val()) {
                    level = $(this).attr('level')
                }
            });
        }
        var dictLevel = $(".productDictionaryValue.level" + level).find(".dictionaryItem")
        var allLevel = $(".productDictionaryValue").find(".dictionaryItem")
        if (dictLevel.length != allLevel.length && level != undefined) {
            e.stopPropagation();
            showErrorMsgInDialog("Product grouping can only be configured on the same level of product hierarchy")
        }
    });
});

var create_prod_group_table_columns = function () {
    var aoColumns = [
        {
            "mData": "groupName",
            "mRender": function (data) {
                return escapeHTML(data)
            }
        },
        {
            "mData": 'products'
        },
        {
            "mData": 'classification'
        },
        {
            "mData": 'display',
            "mRender": function (data) {
                return data ? 'Yes' : 'No';
            }
        }
    ];
    if (isAdmin) {
        aoColumns.push.apply(aoColumns, [{
            "mData": null,
            "className":"col-min-75",
            "sortable": false,
            "aTargets": ["id"],
            "mRender": function (data, type, full) {
                var actionButton = '<div class="hidden-btn btn-group dropdown dataTableHideCellContent" align="center"> \
                            <a class="btn btn-success btn-xs" href="' + editUrl + '/' + data["id"] + '">' + $.i18n._('edit') + '</a> \
                            <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                                <span class="caret"></span> \
                                <span class="sr-only">Toggle Dropdown</span> \
                            </button> \
                            <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                                <li role="presentation"><a role="menuitem" href="javascript:deleteRecord(' + data.id + ')" data-instanceid="' + data["id"] + ' class="delete-record">' + $.i18n._('delete') + '</a></li> \
                            </ul> \
                        </div>';
                return actionButton;
            }
        }]);
    }
    return aoColumns
};

function deleteRecord(id) {
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
            console.log('This was logged in the callback: ' + result);
            if (result) {
                window.location.href = deleteUrl + "/" + id;
            }
        }
    });
}

function getErrorMessageHtml(msg) {
    var alertHtml = '<div class="alert alert-danger alert-dismissible" role="alert"> ' +
        '<button type="button" class="close" data-dismiss="alert"> ' +
        '<span aria-hidden="true">&times;</span> ' +
        '<span class="sr-only"><g:message code="default.button.close.label"/></span> ' +
        '</button> ' + msg +
        '</div>';
    return alertHtml;
}

function showErrorMsgInDialog(msg) {
    var alertHtml = getErrorMessageHtml(msg);
    $("#productModal .modal-body").prepend(alertHtml);
    $(".alert-danger").delay(5000).fadeOut('slow')
}