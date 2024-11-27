$(function () {
    var table = $('#productTypeTable').DataTable({

        // Applied default sorting on name
        aaSorting: [],
        serverSide: false,
        "stateSave": true,
        "stateDuration": -1,
        "bProcessing": true,
        initComplete: function (settings) {
            $('#productTypeTable').on('order.dt', function () {
                $(this).parent().parent().find('.boxTypeInput').hide();
                $(this).parent().parent().find('.boxTypeDisplay').show();
                $('.saveProductRule').removeClass('showOk').addClass('noShow');
                $('.editProductRule').removeClass('noShow').addClass('showOk');
                $('.deleteProductRule').removeClass('noShow').addClass('showOk');
            } );

            var theDataTable = $('#productTypeTable').DataTable();
            $('.yadcf-filter-wrapper').hide();
            $("#toggle-column-filters").on('click', function () {
                var ele = $('.yadcf-filter-wrapper');
                var inputEle = $('.yadcf-filter');
                if (ele.is(':visible')) {
                    ele.hide();
                    yadcf.exFilterExternallyTriggered(theDataTable);
                } else {
                    ele.show();
                    inputEle.first().focus();
                    theDataTable.draw();
                }
            });
        },
        drawCallback: function (settings) {
            // Adding first row for configuring new configuration
            // this would be called every time table reloaded
            if(settings.json!==undefined){
                var rowHtml = signal.utils.render('product_type_config_row',settings.json)
                $('#productTypeTable').prepend(rowHtml);
            }
            // $("#exportTypesTopic").();
            $('.countBox').remove();
            addCountBoxToInputField(255, $('.productRoleName'));
            $('#addToProductRuleRow').hide();
            $(".showOk").hide();
            // Hiding and showing icon on hover
            $("tr").on('mouseover', function (){
                $(this).find(".noShow").hide();
                $(this).find(".showOk").hide();
                $(this).find(".showOk").show();
            }).on('mouseout', function (){
                $(this).find(".noShow").hide();
                $(this).find(".showOk").hide();
            })
            // off previous event populated on drawCallBack
            $(".editProductRule").off('click').on('click', function (){
                $(this).removeClass('showOk').addClass('noShow');
                $(this).parent().find('.saveProductRule').removeClass('noShow').addClass('showOk');
                $(this).parent().find('.cancelProductRuleUpdate').removeClass('noShow').addClass('showOk');
                $(this).parent().parent().find('.boxTypeInput').show();
                $(this).parent().parent().find('.boxTypeDisplay').hide();
                $(this).parent().parent().find('.productRoleName').prop("disabled", false);
                $(this).parent().parent().find('.productTypeSelect').prop("disabled", false);
                $(this).parent().parent().find('.roleTypeSelect').prop("disabled", false);
            })
            // In case cancel clicked on new unsaved product rule
            $(".cancelProductRuleCreate").off('click').on('click', function (){
                $('.dt-empty').toggle();
                // resetting input fields on cancel button
                const $newRowElement = $('#addToProductRuleRow');
                $newRowElement.find('.productRoleName').val('');
                $newRowElement.find('.productTypeSelect').val('null');
                $newRowElement.find('.roleTypeSelect').val('null');
                $newRowElement.hide();
                $('#addToProductRuleRow .productRoleName').prop("disabled", true);
                $('#addToProductRuleRow .productTypeSelect').prop("disabled", true);
                $('#addToProductRuleRow .roleTypeSelect').prop("disabled", true);
            })
            $(".cancelProductRuleUpdate").off('click').on('click', function (){
                $(this).parent().parent().find('.boxTypeInput').hide();
                $(this).parent().parent().find('.boxTypeDisplay').show();
                $(this).removeClass('showOk').addClass('noShow');
                $(this).parent().find('.saveProductRule').removeClass('showOk').addClass('noShow');
                $(this).parent().find('.editProductRule').removeClass('noShow').addClass('showOk');
                $(this).parent().find('.deleteProductRule').removeClass('noShow').addClass('showOk');
            })
            // save and edit method will be called
            // by same event
            $(".saveProductRule").off('click').on('click',function () {
                $('.boxTypeInput').hide();
                $('.boxTypeDisplay').show();
                var name = $(this).parent().parent().find('.productRoleName').val();
                var productTypeId = $(this).parent().parent().find('.productTypeSelect').val();
                var productType = $(this).parent().parent().find('.productTypeSelect option:selected').html();
                var roleTypeId = $(this).parent().parent().find('.roleTypeSelect').val();
                var roleType = $(this).parent().parent().find('.roleTypeSelect option:selected').html();
                var id = $(this).attr("data-id");
                var data = {};
                data.name = decodeFromHTML(name);
                data.productTypeId = productTypeId;
                data.productType = productType;
                data.roleTypeId = roleTypeId;
                data.roleType = roleType;
                data.id = id;
                if(name.length > 255){
                    $.Notification.notify('warning', 'top right', "Warning", "Name length should not exceed 255 characters" , {autoHideDelay: 5000});
                } else if(name.trim()==='' || productTypeId==='null' || roleTypeId==='null'){
                    var text = "Please provide information for all the mandatory fields which are marked with an asterisk (*)"
                    var $productRoleName = $(this).parent().parent().find('.productRoleName');
                    $productRoleName.val($productRoleName.attr('value')?$productRoleName.attr('value'):name);
                    var $productTypeSelect = $(this).parent().parent().find('.productTypeSelect');
                    $productTypeSelect.val($productTypeSelect.parent().parent().attr('data-id')?$productTypeSelect.parent().parent().attr('data-id'):productTypeId);
                    var $roleTypeSelect = $(this).parent().parent().find('.roleTypeSelect');
                    $roleTypeSelect.val($roleTypeSelect.parent().parent().attr('data-id')?$roleTypeSelect.parent().parent().attr('data-id'):roleTypeId);
                    $.Notification.notify('error', 'top right', "Error", text , {autoHideDelay: 5000});
                }else{
                    $(".saveProductRule").off('click')
                    if(id===undefined){
                        // if id is not present, user is
                        // creating new product rule
                        $.ajax({
                            url: productTypeCreateUrl,
                            data: data
                        })
                            .done(function (response) {
                                if(response.success){
                                    $("#productTypeTable").DataTable().ajax.reload(function () {
                                        $.Notification.notify('success', 'top right', "Success", "Record Saved Successfully", {autoHideDelay: 5000});
                                    });
                                    $('.productRoleName').prop("disabled", true);
                                    $('.productTypeSelect').prop("disabled", true);
                                    $('.roleTypeSelect').prop("disabled", true);
                                    $(".showOk").hide();
                                }else{
                                    $.Notification.notify('error', 'top right', "Failure", "Failed to create Product Rule", {autoHideDelay: 5000});
                                }
                            })
                            .fail(function () {
                                $.Notification.notify('error', 'top right', "Failure", "Failed to create Product Rule", {autoHideDelay: 5000});
                        });
                    }else{
                        // if id is present then user is updating
                        // already existed product rule
                        $.ajax({
                            url: productTypeUpdateUrl,
                            data: data
                        })
                            .done(function (response) {
                                if(response.success) {
                                    $("#productTypeTable").DataTable().ajax.reload(function () {
                                        $.Notification.notify('success', 'top right', "Success", "Record Updated Successfully", {autoHideDelay: 5000});
                                    });
                                    $('.productRoleName').prop("disabled", true);
                                    $('.productTypeSelect').prop("disabled", true);
                                    $('.roleTypeSelect').prop("disabled", true);
                                    $(".showOk").hide();
                                }else{
                                    $.Notification.notify('error', 'top right', "Failure", "Failed to update Product Rule", {autoHideDelay: 5000});
                                }
                            })
                            .fail(function () {
                                $.Notification.notify('error', 'top right', "Failure", "Failed to update Product Rule", {autoHideDelay: 5000});
                        });
                    }
                }
                $(this).parent().find('.saveProductRule').removeClass('showOk').addClass('noShow');
                $(this).parent().find('.editProductRule').removeClass('noShow').addClass('showOk');
                $(this).parent().find('.deleteProductRule').removeClass('noShow').addClass('showOk');
                $('#addToProductRuleRow .saveProductRule').removeClass('noShow').addClass('showOk');
            });
            // Deleting product rule event
            $(".deleteProductRule").off('click').on('click', function (e){
                e.preventDefault();
                var id =  $(this).attr("data-id");
                var name = $(this).parent().parent().find('.productRoleName').val();
                var data = {};
                data.id = id;
                bootbox.confirm({
                    title: 'Delete Product Type',
                    message: "Are you sure you want to delete this Product Type configuration?",
                    buttons: {
                        confirm: {
                            label:'<span class="glyphicon glyphicon-trash icon-white"></span> Delete',
                            className: 'btn-danger'
                        },
                        cancel: {
                            label: 'Cancel',
                            className: 'btn-default'
                        }
                    },
                    callback: function (result) {
                        if (result) {
                            if(id!==undefined){
                                $.ajax({
                                    url: productTypeDeleteUrl,
                                    data: data
                                })
                                    .done(function (response) {
                                        if(response.success) {
                                            $("#productTypeTable").DataTable().ajax.reload(function () {
                                                $.Notification.notify('success', 'top right', "Success", "Record Deleted Successfully", {autoHideDelay: 5000});
                                            });
                                            $('.productRoleName').prop("disabled", true);
                                            $('.productTypeSelect').prop("disabled", true);
                                            $('.roleTypeSelect').prop("disabled", true);
                                            $(".showOk").hide();
                                        }else{
                                            if(response.data){
                                                $("#productTypeTable").DataTable().ajax.reload(function () {
                                                    var message = `Cannot delete: ${name} is used in ${response.data} scheduled alerts <a href ="/signal/productTypeConfiguration/linkedAggAlerts?productConfigId=${data.id}" target="_blank"" class="linked-alert-info-icon" data-instanceid="${data.id}">See Details</a>`
                                                    showErrorMsg(message)
                                                });
                                            }
                                        }
                                    })
                                    .fail(function () {
                                        $.Notification.notify('error', 'top right', "Failure", "Failed to delete Product Rule", {autoHideDelay: 5000});
                                });
                            }

                        }
                    }

                });

            })
        },
        "ajax": {
            "url": productTypeListUrl,
            "dataSrc": "aaData"
        },
        "iDisplayLength": 10,
        "aLengthMenu": [[10, 50, 100, 200, -1], [10, 50, 100, 200, "All"]],
        "aoColumns": [
            {
                "mData": "name",
                "mRender": function ( data, type, row, full) {
                    data = encodeToHTML(data)
                    if ( type === 'sort' || type === 'type' || type === "filter") {
                        return data.split(';')[0];
                    }
                    if ( type === 'display' ) {
                        // Display data manipulation
                        return '<span class="boxTypeInput break-all" style="display: none"><input type="text" class="productRoleName form-control break-all" style="margin-bottom: 11px" data-id="' + row.id + '"  value="' + escapeHTML(encodeToHTML(row.name)) + '" disabled/></span>' + '<span class="boxTypeDisplay">' + data + '</span>'
                    }
                    return data;
                },
                "className": 'cell-break'
            },
            {
                "mData": "productType",
                "mRender": function (data, type, row, full) {
                    if(type === "sort" || type === "filter") {
                        // for sorting and filtering in select data
                        // we are using attribute that we have saved in td cell
                        var api = new $.fn.dataTable.Api(full.settings);
                        var td = api.cell({row: full.row, column: full.col}).node();
                        data = $(td).attr('data-order');
                        return data;
                    }
                    let productTypeSelectDiv = '<span class="boxTypeInput" style="display: none"><select class="form-control productTypeSelect">';
                    let map = full.settings.json.productTypeMap
                    for (let element in map) {
                        if(map[element].text !== data && map[element].text !== undefined){
                            productTypeSelectDiv += '<option value="'+ map[element].id +'">'+ map[element].text +'</option>'
                        }else if (map[element].text !== undefined){
                            productTypeSelectDiv += '<option value="'+ map[element].id +'" selected>'+ map[element].text +'</option>'
                        }
                    }
                    productTypeSelectDiv += '</select></span>'
                    productTypeSelectDiv += '<span class="boxTypeDisplay">' + data + '</span>'

                    return productTypeSelectDiv;
                },
                "className": 'cell-break'
            },
            {
                "mData": "roleType",
                "mRender": function (data, type, row, full) {
                    if(type === "sort" || type === "filter") {
                        // for sorting and filtering in select data
                        // we are using attribute that we have saved in td cell
                        var api = new $.fn.dataTable.Api(full.settings);
                        var td = api.cell({row: full.row, column: full.col}).node();
                        data = $(td).attr('data-order');
                        return data;
                    }
                    let roleTypeSelectDiv = '<span class="boxTypeInput" style="display: none"><select class="form-control roleTypeSelect">';
                    let map = full.settings.json.roleMap
                    for (let element in map) {
                        if(map[element].text !== data && map[element].text !== undefined){
                            roleTypeSelectDiv += '<option value="'+ map[element].id +'">'+ map[element].text +'</option>'
                        }else if (map[element].text !== undefined){
                            roleTypeSelectDiv += '<option value="'+ map[element].id +'" selected>'+ map[element].text +'</option>'
                        }
                    }
                    roleTypeSelectDiv += '</select></span>'
                    roleTypeSelectDiv += '<span class="boxTypeDisplay">' + data + '</span>'

                    return roleTypeSelectDiv;
                },
                "className": 'cell-break'
            },
            {
                "mData": null,
                "sortable": false,
                "aTargets": ["id"],
                "mRender": function (data, type, row) {
                    return signal.utils.render('product-type-edit-save-cancel-buttons', row)
                },
                "sClass": "icons-class"
            }
        ],
        "oLanguage": {
            "sEmptyTable": "No data available in table",
            "sLoadingRecords": "",
            "oPaginate": {
                "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
            },
            "sLengthMenu": "Show _MENU_"
        },
        layout: {
            topStart: 'search',
            topEnd: null,
            bottomStart: ["pageLength", "info", {paging: {type: "full_numbers"}}],
            bottomEnd: null,
        },
        customProcessing: true,
        columnDefs: [{
            "targets": '_all',
            "render": $.fn.dataTable.render.text(),
            'createdCell':  function (td, cellData, rowData, row, col) {
                $(td).attr('data-order', cellData);
                if(col===1){
                    $(td).attr('data-id', rowData.productTypeId);
                }else if(col===2){
                    $(td).attr('data-id', rowData.roleTypeId);
                }
                $(td).css('word-break', 'break-all');
            },
            orderSequence: ['desc', 'asc']
        }]
    });

    setTimeout(function () {
        init_filter(table);
    }, 500);

    var init_filter = function (data_table) {
        yadcf.init(data_table, [
            {column_number: 0, filter_type: 'text', filter_reset_button_text: false},
            {column_number: 1, filter_type: "text", filter_reset_button_text: false},
            {column_number: 2, filter_type: "text", filter_reset_button_text: false}
        ]);
        $(".yadcf-filter").removeAttr('placeholder');
        $('.yadcf-filter-wrapper').hide();
    };


    $('#exportTypesTopic a[href]').on('click', function (e) {
        var searchString = $('#productTypeTable_filter').find('.form-control.dt-search').val();
        var clickedURL = e.currentTarget.href;
        var updatedExportUrl = clickedURL;
        var selectedCases = [];
        $.each(table.rows({filter: 'applied'}).data(), function (idx, val) {
            selectedCases.push(val.id);
        });
        clickedURL +=  "&searchString=" +encodeURIComponent(searchString)
        if (selectedCases.length >= 0) {
            updatedExportUrl = clickedURL + "&selectedCases=" + selectedCases;
            window.location.href = updatedExportUrl;
        }
        return false;
    });

    $("#newProductRoleConfig").on('click',function () {
        $('#addToProductRuleRow').toggle();
        $('.dt-empty').toggle();
        $('#addToProductRuleRow .productRoleName').prop("disabled", false);
        $('#addToProductRuleRow .productTypeSelect').prop("disabled", false);
        $('#addToProductRuleRow .roleTypeSelect').prop("disabled", false);
    })
    $('#productTypeTable').onclick = function (e) {
        console.log(e.target+" = target");
    };
});


function getErrorMessageHtml(msg) {
    var alertHtml = '<div class="alert alert-danger alert-dismissible" role="alert"> '
        +
        '<button type="button" class="close" data-dismiss="alert"> ' +
        '<span aria-hidden="true">&times;</span> ' +
        '<span class="sr-only"><g:message code="default.button.close.label" /></span> ' +
        '</button> ' + msg;
    '</div>';
    return alertHtml;
}

function getSuccessMessageHtml(msg) {
    var alertHtml = '<div class="alert alert-success alert-dismissible" role="alert"> ' +
        '<button type="button" class="close" data-dismiss="alert"> ' +
        '<span aria-hidden="true">&times;</span> ' +
        '<span class="sr-only"><g:message code="default.button.close.label" /></span> ' +
        '</button> ' + msg;
    '</div>';
    return alertHtml;
}

function showErrorMsg(msg) {
    // removeExistingMessageHolder();
    var alertHtml = getErrorMessageHtml(msg);
    $('body .messageContainer').prepend(alertHtml);
    $('body').scrollTop(0);
}

function showSuccessMsg(msg) {
    // removeExistingMessageHolder();
    var alertHtml = getSuccessMessageHtml(msg);
    $('body .messageContainer').prepend(alertHtml);
    $('body').scrollTop(0);
}

function removeExistingMessageHolder() {
    $('.messageContainer').html("");
}
