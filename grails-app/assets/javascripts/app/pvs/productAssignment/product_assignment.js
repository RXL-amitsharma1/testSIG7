var userLocale = "en";
var currentEditingRow;
var isFilter = false;
var productAssignmentTable;
var importLogTable;
var bulkUpdateAssignment = {};
var singleDeleteRow;
var selectedDeleteRowsId;
var ajaxInProgress = false; // added global flag as to prevent multiple ajax
$(function () {

    // Browse file upload Start
    $(document).on('click', '#productAssignmentFileModal .file-uploader .browse', function () {
        var fileUploaderElement = $(this).closest('.file-uploader');
        var file = fileUploaderElement.find('.file');
        var fileName = fileUploaderElement.find('.form-control').val();
        file.trigger('click');
    });

    $(document).on('change', '#productAssignmentFileModal .file-uploader .file', function () {
        var currentElement = $(this);
        var inputBox = currentElement.parent('.file-uploader').find('.form-control');
        if (!_.isEmpty(currentElement.val())) {
            inputBox.val(currentElement.val().replace(/C:\\fakepath\\/i, ''));//todo remove replace method
        }
        inputBox.trigger('change');
    });

    $(document).on('click', '#import-assignments', function () {
        $('#productAssignmentFileModal').modal({show: true});
    });

    $("#productAssignmentNewRow").hover(function () {
        $(this).attr("title", "Add Product Assignments");
    });

    $(document).on('click', '#import-log', function () {
        if (typeof importLogTable != "undefined" && importLogTable != null) {
            importLogTable.destroy()
        }
        $('#importLogModal').modal({show: true});
        initImportLogTable();
    });

    $(document).on('click', '#populate-unassigned-products', function () {
        $('#populateUnassignedProductsModal').modal({show: true});
    });

    $('#productAssignmentFileModal').on('hidden.bs.modal', function (e) {
        $(this).find('.file').val('');
        $(this).find('#assignment-file-name').val('');
        $(this).find(".upload").attr("disabled", true);
    });

    $('#importAssignmentFileUploadForm').on('submit', function (e) {
        e.preventDefault();
        var formData = new FormData(this);
        var $this = $(this);
        if ($this.find('.file').val()) {
            $this.find(".upload").attr("disabled", true);
            $.ajax({
                    url: uploadFileUrl,
                    type: 'POST',
                    data: formData,
                    processData: false,
                    contentType: false,
            })
                .done(function (response) {
                        $this.find(".upload").attr("disabled", true);
                        $('#productAssignmentFileModal').modal('hide');
                        if (response.status) {
                            $.Notification.notify('success', 'top right', "Success", response.message, {autoHideDelay: 5000});
                        } else {
                            $.Notification.notify('error', 'top right', "Error", response.message, {autoHideDelay: 5000});
                        }
                    })
                .fail(function () {
                        $this.find(".upload").attr("disabled", false);
                        $.Notification.notify('error', 'top right', "Error", "Sorry, This File Format Not Accepted", {autoHideDelay: 5000});
                });
        }
    });

    $('#populateUnassignedProductsForm').on('submit', function (e) {
        e.preventDefault();
        var formData = new FormData(this);
        var $this = $(this);
        if ($this.find('#hierarchy').val()) {
            $.ajax({
                    url: populateUnassignedProductUrl,
                    type: 'POST',
                    data: formData,
                    processData: false,
                    contentType: false,
            })
                .done(function (response) {
                        $('#populateUnassignedProductsModal').modal('hide');
                        if (response.status) {
                            $.Notification.notify('success', 'top right', "Success", response.message, {autoHideDelay: 5000});
                        } else {
                            $.Notification.notify('error', 'top right', "Error", response.message, {autoHideDelay: 5000});
                        }
                    })
                .fail(function () {
                        $.Notification.notify('error', 'top right', "Error", "Sorry, Something unexpected happen at the server", {autoHideDelay: 5000});
                });
        }
    });


    var initImportLogTable = function () {
        var columns = import_log_table_columns();
        importLogTable = $('#import-log-table').DataTable({
            layout: {
                topStart: 'search',
                topEnd: null,
                bottomStart: ['pageLength', 'info', {paging: {type: "full_numbers"}}],
                bottomEnd: null,
            },
            customProcessing: true,
            sPaginationType: "full_numbers",
            responsive: true,
            language: {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json"
            },
            "ajax": {
                "url": fetchImportAssignmentUrl,
                "dataSrc": "importLogList",
            },
            drawCallback: function (settings) {
                colEllipsis();
                webUiPopInit();
                var rowsDataAR = $('#import-log-table').DataTable().rows().data();
                pageDictionary($('#import-log-table_wrapper'), rowsDataAR.length);
                showTotalPage($('#import-log-table_wrapper'), rowsDataAR.length);
            },
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
            "aaSorting": [],
            "bLengthChange": true,
            "iDisplayLength": 10,
            "aLengthMenu": [[10, 50, 100, 200, -1], [10, 50, 100, 200, "All"]],
            "pagination": true,
            "aoColumns": columns,
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }]
        });
    };

    var import_log_table_columns = function () {
        return [
            {
                "mData": "importedFileName",
                "className": "col-md-3 cell-break",
                "mRender": function (data, type, row) {
                    return '<a href="' + '/signal/productAssignment/importAssignmentFile' + '?' + 'logsId=' + row.id + '&' + 'fileName=' + row.importedFileName + '">' + addEllipsis(row.importedFileName) + '</a>';
                }
            },
            {
                "mData": "generatedFileName",
                "className": "col-md-3 cell-break",
                "mRender": function (data, type, row) {
                    if (row.generatedFileName === '-') {
                        return '-';
                    } else {
                        return '<a href="' + '/signal/productAssignment/importAssignmentFile' + '?' + 'logsId=' + row.id + '&' + 'fileName=' + row.generatedFileName + '">' + addEllipsis(row.generatedFileName) + '</a>';
                    }
                }
            },
            {
                "mData": "importedBy",
                "className": "col-md-2"
            },
            {
                "mData": "importedDate",
                "className": "col-md-2"
            },
            {
                "mData": "status",
                "className": "col-md-2"
            }
        ]
    };

    signal.fieldManagement.updateFieldListForProductAssignment(columnOrder)
    var aoColumnsConfig = [
        {
            "mData": "action",
            "className": "col-md-half checkboxLabel",
            "mRender": function (data, row, type) {
                return '<input type="checkbox" class="checkboxContent" data-multi-id="' + data + '"/>'
            },
        },
        {
            "mData": "product",
            "className": "col-md-3-half cell-break",
            "mRender": function (data, type, row) {
                return '<span class="showProductDictionary">' + DOMPurify.sanitize(addEllipsis(data)) + '</span>' +
                    '<div class="row hide selectProductDictionary">' +
                    '<div class="wrapper">' +
                    '<input type="hidden" name="productSelectionAssessment" id="productSelectionAssessment" value=""/>' +
                    '<input type="hidden" name="productGroupSelectionAssessment" id="productGroupSelectionAssessment" value=""/>' +
                    '<div id="showProductSelectionAssessment" class="showDictionarySelection">' +
                    '<div style="padding: 1px;">' + row.productNameDictionary + '</div>' +
                    '</div>' +
                    '<div class="iconSearch">' +
                    '<a data-toggle="modal" id="searchProductsAssessment" data-target="#productModalAssessment" tabindex="0" data-toggle="tooltip" title="Search product"  class="productRadio">' +
                    '<i class="fa fa-search"></i></a>' +
                    '</div>' +
                    '</div>' +
                    '</div>'
            },
            "visible": signal.fieldManagement.visibleColumns('product')
        },
        {
            "mData": "hierarchy",
            "className": "col-md-2",
            "visible": signal.fieldManagement.visibleColumns('hierarchy')
        },
        {
            "mData": "assignedUserOrGroup",
            "bSortable": !isProductView,
            "className": "col-md-2 cell-break",
            "mRender": function (data, type, row) {
                let names = ""
                _.each(data, function (obj, index) {
                    if (index == data.length - 1) {
                        names += obj.name;
                    } else {
                        names += obj.name + ", ";
                    }
                });
                let primaryUserOrGroupId = "";
                if (row.primaryUserOrGroupId) {
                    primaryUserOrGroupId = row.primaryUserOrGroupId;
                }
                // Added for PVS-55130
                if (!primaryUserOrGroupId) {
                    primaryUserOrGroupId = row?.assignedUserOrGroup[0]?.id ?? "";
                }
                return '<select id="selectUserOrGroup" name="selectUserOrGroup" class="sharedWithControl form-control select2"></select>' +
                    '<input type="hidden" id="primaryAssignment" value="' + primaryUserOrGroupId + '"> ' +
                    '<span class="userOrGroupNames">' + DOMPurify.sanitize(addEllipsis(names)) + '</span>'
            },
            "visible": signal.fieldManagement.visibleColumns('assignedUserOrGroup')
        },
        {
            "mData": "createdBy",
            "className": "col-md-1",
            "bSortable": !isProductView,
            "mRender": function (data, type, row) {
                return DOMPurify.sanitize(data)
            },
            "visible": signal.fieldManagement.visibleColumns('createdBy')
        },
        {
            "mData": "workflowGroup",
            "className": "col-md-2 cell-break",
            "mRender": function (data, value, row) {
                return DOMPurify.sanitize(addEllipsis(data))
            },
            "visible": signal.fieldManagement.visibleColumns('workflowGroup')
        },
        {
            "mData": "action",
            "className": "col-md-1 text-center",
            "mRender": function (data, type, row) {
                return '<span data-field="removeAttachment" class="hide assessmentId" data-assignmentid=' + data + '></span>' +
                    '<a href="javascript:void(0);" title="Edit" class="table-row-edit pv-ic hidden-ic editAssignment">' +
                    '<i class="mdi mdi-pencil" aria-hidden="true"></i> </a>' +
                    '<a href="javascript:void(0);" title="Delete" class="table-row-del hidden-ic deleteAssignment">' +
                    '<i class="mdi mdi-close" aria-hidden="true"></i> </a>' +
                    '<a href="javascript:void(0);" title="Update" class="table-row-save hidden-ic pv-ic updateAssignment hide" data-editing="true">' +
                    '<i class="mdi mdi-check" aria-hidden="true"></i> </a>' +
                    '<a href="javascript:void(0);" title="Cancel" class="table-row-del hidden-ic cancelAssignment hide">' +
                    '<i class="mdi mdi-close" aria-hidden="true"></i> </a>'
            },
            "visible": isEditable
        }
    ];

// Conditionally insert the `alertGroup` column if `isProductView` is true
    if (isProductView) {
        aoColumnsConfig.splice(1, 0, {
            "mData": "alertGroup",
            "className": "col-md-1",
            "visible": isProductView && signal.fieldManagement.visibleColumns('alertGroup')
        });
    }
    productAssignmentTable = $('#productAssignmentTable').DataTable({
        layout: {
            topStart: ['pageLength', 'search'],
            topEnd: null,
            bottomStart: ['info', {paging: {type: "full_numbers"}}],
            bottomEnd: null,
        },
        customProcessing: true,
        "responsive": true,
        processing: true,
        serverSide: true,
        "bLengthChange": true,
        "iDisplayLength": 50,
        "aLengthMenu": [[50, 100, 200, 500], [50, 100, 200, 500]],
        "bProcessing": true,
        "oLanguage": {
            "oPaginate": {
                "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
            },
            "sZeroRecords": "No data available in table", "sEmptyTable": "No data available in table",
            "sLengthMenu":"Show _MENU_ entries",
        },
        scrollY: true,
        "ajax": {
            "url": fetchProductAssignmentUrl,
            "dataSrc": "aaData",
            "method": "POST",
            "data": function (args) {
                args["isProductView"]=isProductView;
                args["isFilter"]=isFilter;
                return {
                    "args": JSON.stringify(args)
                };
            }
        },
        "rowCallback": function (row, data, index) {
            buildReportingDestinationsSelectBox($(row).find('#selectUserOrGroup'), sharedWithListUrl, data.assignedUserOrGroup,  $(row).find("#primaryAssignment"), true);
            $(row).find('.select2-container').addClass("hide");
            $(row).find("#productSelectionAssessment").val(data.productAssessment);
            $(row).find("#productGroupSelectionAssessment").val(data.productGroupAssessment);
            $(row).find(".select2-selection__choice").each(function () {
                let name = $(this).find(".name").text();
                if (name == $(row).find("#primaryAssignment").val()) {
                    $(this).find(".addPrimary").addClass("primary");
                }
            });

        },
        initComplete: function (){
            init_filter(productAssignmentTable);
        },
        "drawCallback":function (settings) {
            bulkUpdateAssignment ={};
            colEllipsis();
            webUiPopInit();

            if ($(this).find('tbody tr').length < 2) {
                $('.dt-scroll-body').css("overflow","hidden");
            } else {
                $('.dt-scroll-body').css("overflow","scroll");
            }

        },
        "aoColumns": aoColumnsConfig,
        columnDefs: [{
            "targets": '_all',
            "render": $.fn.dataTable.render.text(),
            orderSequence: ['desc', 'asc']
        }]
    });

    signal.fieldManagement.populateColumnList(columnOrderUrl + "?isProductView=" + isProductView, updateColumnOrderUrl + "?isProductView=" + isProductView)
    signal.fieldManagement.init($('#productAssignmentTable').DataTable(), '#productAssignmentTable', 1, true);


    $(document).on('click', '#productAssignmentTable a.editAssignment', function (e) {
        let currentRow = $(this).closest("tr");
        $(this).addClass("hide");
        $(this).siblings(".deleteAssignment").addClass("hide");
        $(this).siblings(".updateAssignment").removeClass("hide");
        $(this).siblings(".cancelAssignment").removeClass("hide");
        currentRow.find(".showProductDictionary").addClass("hide");
        currentRow.find(".selectProductDictionary").removeClass("hide");
        $(currentRow).find('.select2-container').removeClass("hide");
        $(currentRow).find('.userOrGroupNames').addClass("hide");
        var selector = $(currentRow).find('#selectUserOrGroup');
        var selectedIndex = $(currentRow).closest('tr').index();
        if (!selector.val()) {
            var sharedWithData = productAssignmentTable.rows(selectedIndex).data()[0].assignedUserOrGroup;
            if (sharedWithData) {
                $.each(sharedWithData, function (i, data) {
                    var option = new Option(data.name, data.id, true, true);
                    $(option).attr('data-blinded', data.blinded);
                    selector.append(option).trigger('change');
                });
            }
        }
    });
    $(document).on('click', '#productAssignmentTable a.cancelAssignment', function (e) {
        let currentRow = $(this).closest("tr");
        $(this).addClass("hide");
        $(this).siblings(".updateAssignment").addClass("hide");
        $(this).siblings(".editAssignment").removeClass("hide");
        $(this).siblings(".deleteAssignment").removeClass("hide");
        currentRow.find(".showProductDictionary").removeClass("hide")
        currentRow.find(".selectProductDictionary").addClass("hide")
        $(currentRow).find('.select2-container').addClass("hide");
        $(currentRow).find('.userOrGroupNames').removeClass("hide");
    });

    $(document).on("click", ".checkboxLabel", function (e) {
        let currentElem = $(this).closest("tr").find(".checkboxContent")
        if (e.target == this) {
            currentElem.prop("checked", !currentElem.prop("checked"));
        }
        let isChecked = currentElem.is(":checked");
        let currentRow = currentElem.closest("tr");
        let id = currentElem.attr("data-multi-id");
        if (isChecked) {
            bulkUpdateAssignment[id] = productAssignmentTable.row(currentRow).index();
        } else {
            delete bulkUpdateAssignment[id];
        }
    });

    $(document).on("click", "#searchProductsAssessment", function () {
        currentEditingRow = $(this).closest("tr");
    });
    $("#productModalAssessment").on('hidden.bs.modal', function (e) {
        let divsEarlier = currentEditingRow.find("#showProductSelectionAssessment").find('div');
        if (divsEarlier.length > 1) {
            divsEarlier.eq(1).remove()
        }
    });

    var viewLabel = $(".changeViewLabel");
    if (!isProductView) {
        viewLabel.text("Change To Product View");
    }

    $(document).on("click", ".productAssignmentTable_foot .saveAssignment", function () {
        if (!ajaxInProgress) {
            ajaxInProgress = true;
            let currentRow = $(this).closest('tr');
            let selectedProducts = currentRow.find("#productSelectionAssessment").val();
            let productGroupValue = currentRow.find("#productGroupSelectionAssessment").val()
            let selectedProductGroups = productGroupValue && productGroupValue !== "[]" ? productGroupValue : "";
            let selectedUserOrGroup = currentRow.find("#selectUserOrGroup").val();
            let alertGroup = currentRow.find("#alertGroup").val()
            if (!selectedUserOrGroup || !(selectedProducts || selectedProductGroups)) {
                ajaxInProgress = false;
                $.Notification.notify('warning', 'top right', "Warning", "Please provide information for all the mandatory fields which are marked with an asterisk (*)", {autoHideDelay: 2000});
            } else {
                var data = {};
                data['selectedProducts'] = selectedProducts;
                data['selectedProductGroups'] = selectedProductGroups;
                data['selectedUserOrGroup'] = JSON.stringify(selectedUserOrGroup);
                data['isProductView'] = isProductView;
                data['primaryUserOrGroup'] = currentRow.find("#primaryAssignment").val();
                data['alertGroup'] = currentRow.find("#alertGroup").val()
                $.ajax({
                    url: saveProductAssignmentUrl,
                    type: "POST",
                    data: data,
                })
                    .done(function (settings) {
                        //removing this code for bug PVS-54073
                        console.log(settings);
                        if(settings.status == 'fail'){
                            ajaxInProgress = false;
                            $.Notification.notify('error', 'top right', "ERROR", settings.errorMessage, {autoHideDelay: 4000});
                        } else {
                            ajaxInProgress = false;
                            $.Notification.notify('success', 'top right', "Success", "Record Saved Successfully.", {autoHideDelay: 2000});
                            productAssignmentTable.ajax.reload();
                            let ele = $("#productAssignmentRemoveRow");
                            ele.trigger('click');
                        }
                    })
                    .fail(function () {
                        ajaxInProgress = false;
                        $.Notification.notify('error', 'top right', "error", " Error while saving.", {autoHideDelay: 5000});
                    });
            }
        }
    });

    $('#file').on('change', function () {
        if ($(this).val()) {
            if ($(this)[0].files[0].size > maxUploadLimit) {
                $.Notification.notify('error', 'top right', "Failed", $.i18n._('fileUploadMaxSizeExceedError', maxUploadLimit / 1048576), {autoHideDelay: 5000});
                $(this).val('');
                $('input:submit').attr('disabled', true);
            } else {
                $('input:submit').attr('disabled', false);
            }
        } else {
            $('input:submit').attr('disabled', true);
        }
    });

    $('#importAssignmentFileUploadForm').on('submit', function () {
        $('input:submit', this).attr('disabled', true);
        return true;
    });

    let updateMap = {}
    let singleUpdateRow;
    let allowUpdate = true;
    let data = {};
    let url;
    $(document).on("click", "#productAssignmentTable .updateAssignment", function () {
        updateMap = {};
        data = {};
        url;
        allowUpdate = true;
        if (!(Object.keys(bulkUpdateAssignment).length)) {
            url = updateProductAssignmentUrl;
            let currentRow = $(this).closest('tr');
            let selectedProducts = currentRow.find("#productSelectionAssessment").val();
            let productGroupValue = currentRow.find("#productGroupSelectionAssessment").val()
            let selectedProductGroups = productGroupValue && productGroupValue !== "[]" ? productGroupValue : "";
            let selectedUserOrGroup = currentRow.find("#selectUserOrGroup").val();
            let id = currentRow.find(".assessmentId").attr("data-assignmentid");
            if (!selectedUserOrGroup || !(selectedProducts || selectedProductGroups)) {
                $.Notification.notify('warning', 'top right', "Warning", "Please provide information for all the mandatory fields which are marked with an asterisk (*)", {autoHideDelay: 5000});
                allowUpdate = false;
            } else {
                updateMap = {};
                updateMap['selectedProducts'] = selectedProducts;
                updateMap['selectedProductGroups'] = selectedProductGroups;
                updateMap['selectedUserOrGroup'] = JSON.stringify(selectedUserOrGroup);
                updateMap['assignmentId'] = id;
                updateMap['isProductView'] = isProductView;
                updateMap['primaryUserOrGroup'] = currentRow.find("#primaryAssignment").val();
                data = updateMap;
                if (allowUpdate) {
                    updateRows(url, data)
                }
            }
        } else {
            singleUpdateRow = $(this).closest("tr");
            $("#bulkUpdateModal").modal("show");
            $("#totalNumberOfSelectedRow").text(Object.keys(bulkUpdateAssignment).length)
        }
    });

    $("#bulkUpdateModal .btn-primary").on('click', function () {
        let isBulkUpdate = $("input[name='bulkOptions']:checked").val();
        if (isBulkUpdate === "allSelected") {
            updateMap = {};
            url = bulkUpdateProductAssignmentUrl;
            let bulkData = {};
            _.each(Object.keys(bulkUpdateAssignment), function (value) {
                bulkData = {};
                let currentRow = productAssignmentTable.row(bulkUpdateAssignment[value]).nodes()[0];
                let selectedProducts = $(currentRow).find("#productSelectionAssessment").val();
                let productGroupValue = $(currentRow).find("#productGroupSelectionAssessment").val()
                let selectedProductGroups = productGroupValue && productGroupValue !== "[]" ? productGroupValue : "";
                let selectedUserOrGroup = $(currentRow).find("#selectUserOrGroup").val();
                let primaryUserOrGroup = $(currentRow).find("#primaryAssignment").val();
                //In case where only primary assignee is present, selectedUserOrGroup should be same as primaryUserOrGroup
                // Added for PVS-55130
                if (!selectedUserOrGroup) {
                    selectedUserOrGroup = [primaryUserOrGroup];//selectedUserOrGroup is a JSON and not a String unlike primaryUserOrGroup
                }
                if (!selectedUserOrGroup || !(selectedProducts || selectedProductGroups)) {
                    $.Notification.notify('warning', 'top right', "Warning", "Please provide information for all the mandatory fields which are marked with an asterisk (*)", {autoHideDelay: 5000});
                    allowUpdate = false;
                } else {
                    bulkData['selectedProducts'] = selectedProducts ? JSON.parse(selectedProducts) : "";
                    bulkData['selectedProductGroups'] = selectedProductGroups ? JSON.parse(selectedProductGroups) : "";
                    bulkData['selectedUserOrGroup'] = selectedUserOrGroup;
                    bulkData['primaryUserOrGroup'] = primaryUserOrGroup;
                    updateMap[value] = bulkData;
                }
            });
            updateMap = JSON.stringify(updateMap)
            data = {"bulkData": updateMap, "isProductView": isProductView};
            if (allowUpdate) {
                updateRows(url, data)
            }
        } else {
            url = updateProductAssignmentUrl;
            let selectedProducts = singleUpdateRow.find("#productSelectionAssessment").val();
            let productGroupValue = singleUpdateRow.find("#productGroupSelectionAssessment").val()
            let selectedProductGroups = productGroupValue && productGroupValue !== "[]" ? productGroupValue : "";
            let selectedUserOrGroup = singleUpdateRow.find("#selectUserOrGroup").val();
            let id = singleUpdateRow.find(".assessmentId").attr("data-assignmentid");
            if (!selectedUserOrGroup || !(selectedProducts || selectedProductGroups)) {
                $.Notification.notify('warning', 'top right', "Warning", "Please provide information for all the mandatory fields which are marked with an asterisk (*)", {autoHideDelay: 5000});
                allowUpdate = false;
            } else {
                updateMap = {};
                updateMap['selectedProducts'] = selectedProducts;
                updateMap['selectedProductGroups'] = selectedProductGroups;
                updateMap['selectedUserOrGroup'] = JSON.stringify(selectedUserOrGroup);
                updateMap['assignmentId'] = id;
                updateMap['isProductView'] = isProductView;
                updateMap['primaryUserOrGroup'] = singleUpdateRow.find("#primaryAssignment").val();
                data = updateMap;
                if (allowUpdate) {
                    updateRows(url, data)
                }
            }
        }
    });

    actionButton('#productAssignmentTable');
    var init_filter = function (data_table) {
        yadcf.init(data_table, [
            {
                column_number: 1,
                filter_type: 'text',
                filter_reset_button_text: false,
                filter_delay: 600,
                filter_default_label: ''
            },
            {
                column_number: 2,
                filter_type: "text",
                filter_reset_button_text: false,
                filter_delay: 600,
                filter_default_label: ''
            },
            {
                column_number: 3,
                filter_type: "text",
                filter_reset_button_text: false,
                filter_delay: 600,
                filter_default_label: ''
            },
            {
                column_number: 4,
                filter_type: "text",
                filter_reset_button_text: false,
                filter_delay: 600,
                filter_default_label: ''
            },
            {
                column_number: 5,
                filter_type: "text",
                filter_reset_button_text: false,
                filter_delay: 600,
                filter_default_label: ''
            },
        ]);
        $(".yadcf-filter").removeAttr('placeholder');
    };


    $("#apply-filters").on('click', function () {
        var ele = $("#productAssignmentTable_wrapper").find('.yadcf-filter-wrapper');
        var inputEle = $('.yadcf-filter');
        if (ele.is(':visible')) {
            ele.css('display', 'none');
            isFilter = false;
        } else {
            ele.css('display', 'block');
            isFilter = true;
            inputEle.first().focus();
        }
    });

    $(document).on("click", ".deleteAssignment", function () {
        $('.deleteAssignment').prop("disabled", true);
        let data = {};
        data["isProductView"] = isProductView;
        var selectedAssignmentId = []
        $('.checkboxContent:checkbox:checked').each(function () {
            selectedAssignmentId.push($(this).attr("data-multi-id"))
        });
        if (selectedAssignmentId == "" || selectedAssignmentId == []) {
            selectedAssignmentId.push($(this).closest('tr').find(".checkboxContent").attr("data-multi-id"));
            data["selectedAssignmentIds"] = JSON.stringify(selectedAssignmentId);
            deleteRows(data, isFootHidden);
        } else {
            selectedDeleteRowsId = selectedAssignmentId;
            singleDeleteRow = $(this).closest("tr");
            $("#bulkDeleteModal").modal("show");
            $("#totalNumberOfSelectedRows").text(Object.keys(bulkUpdateAssignment).length);

        }
    });

    $("#bulkDeleteModal .btn-primary").on('click', function () {
        let data = {};
        let isBulkUpdate = $("input[name='bulkOptions']:checked").val();
        data["isProductView"] = isProductView;
        var selectedAssignmentId = [];
        if (isBulkUpdate === 'allSelected') {
            selectedAssignmentId = selectedDeleteRowsId;
        } else {
            selectedAssignmentId.push(singleDeleteRow.find(".checkboxContent").attr("data-multi-id"));
        }

        data["selectedAssignmentIds"] = JSON.stringify(selectedAssignmentId);
        deleteRows(data, isFootHidden);
    });

    var isFootHidden = true;
    var tFoot = $(".productAssignmentTable_foot");

    $("#productAssignmentNewRow").on('click', function () {
        if (isFootHidden) {
            isFootHidden = false;
            tFoot.removeClass("hide");
            tFoot.find("#primaryAssignment").val("");
        } else {
            $("#productAssignmentRemoveRow").trigger('click');
        }
        tFoot.find("#productSelectionAssessment").val("");
        tFoot.find("#alertGroup").val(null).trigger('change');
        tFoot.find("#productGroupSelectionAssessment").val("");
        tFoot.find("#showProductSelectionAssessment").html("");
        tFoot.find("#selectUserOrGroup").val("").trigger("change.select2");
    });

    $("#productAssignmentRemoveRow").on('click', function () {
        isFootHidden = true;
        tFoot.addClass("hide");
    });

   $("#exportProductAssignment").on('click', function () {
        let alertGroupVal = $("#yadcf-filter-productAssignmentTable-1").val();
        let productVal = $("#yadcf-filter-productAssignmentTable-2").val();
        let hierarchyVal = $("#yadcf-filter-productAssignmentTable-3").val();
        let assignedUserOrGroupVal = $("#yadcf-filter-productAssignmentTable-4").val();
        let createdByVal = $("#yadcf-filter-productAssignmentTable-5").val();
        let workflowGroupVal = $("#yadcf-filter-productAssignmentTable-6").val();
        let alertGroup = (alertGroupVal && alertGroupVal !== undefined)?alertGroupVal:""
        let product = (productVal && productVal !== undefined)?productVal:""
        let hierarchy =( hierarchyVal && hierarchyVal !== undefined)?hierarchyVal:""
        let assignedUserOrGroup = (assignedUserOrGroupVal && assignedUserOrGroupVal !== undefined)?assignedUserOrGroupVal:""
        let createdBy = (createdByVal && createdByVal !== undefined)?createdByVal:""
        let workflowGroup = (workflowGroupVal && workflowGroupVal !== undefined)?workflowGroupVal:""
        let search = $(".dt-search").find(".dt-input").val();

        $(this).attr("href", exportAssignmentUrl + "?isProductView=" + isProductView + "&&alertGroup=" + alertGroup + "&&product=" + product + "&&hierarchy=" + hierarchy + "&&search=" + search +
            "&&assignedUserOrGroup=" + assignedUserOrGroup + "&&createdBy=" + createdBy + "&&workflowGroup=" + workflowGroup + "&&isFilter=" + isFilter)
    });

    $("#exportProductAssignment,#apply-filters,#populate-unassigned-products,#change-view").on('click', function () {
        let sidePanel = $("#productFields");
        if (sidePanel.is(":visible")) {
            $("#btnCloseListConfig").trigger('click')
        }
    });
});


function updateRows(url, data) {
    $.ajax({
        url: url,
        type: "POST",
        data: data,
    })
        .done(function (settings) {
            // removing this code for bug PVS-54073
            $.Notification.notify('success', 'top right', "Success", " Successfully saved.", {autoHideDelay: 5000});
            productAssignmentTable.ajax.reload();

        })
        .fail(function () {
            $.Notification.notify('error', 'top right', "error", " Error while saving.", {autoHideDelay: 5000});
        });
}

function deleteRows(data, isFootHidden) {
    $.ajax({
        url: deleteAssignmentUrl,
        type: "POST",
        data: data,
    })
        .done(function () {
            $.Notification.notify('success', 'top right', "Success", "Record deleted successfully.", {autoHideDelay: 5000});
            productAssignmentTable.ajax.reload();
            if (!isFootHidden && productAssignmentTable.data().count() === 1) {
                $('#productAssignmentTable').hide();
            }
            $('.deleteAssignment').prop("disabled", false);
        })
        .fail(function () {
            $.Notification.notify('error', 'top right', "error", " Error while deleting.", {autoHideDelay: 5000});
            $('.deleteAssignment').prop("disabled", false);
        });
}


function bindShareWith2WithDataProductAssignment(selector, sharedWithListUrl, sharedWithData) {
    const selectFieldId = selector.attr('id');
    selector.removeAttr('id').select2({
        templateSelection: formatState,
        tags: false,
        minimumInputLength: 0,
        multiple: true,
        placeholder: 'Select Assigned To',
        width: "90%",
        separator: ";",
        templateResult: formatOption,
        ajax: {
            quietMillis: 250,
            dataType: "json",
            url: sharedWithListUrl,
            data: function (params) {
                return {
                    term: params.term,
                    max: params.page || 10,
                    lang: userLocale,
                };
            },
            processResults: function (data, params) {
                params.page = params.page || 10;
                return {
                    results: data,
                    pagination: {
                        more: (params.page * 10) < data.length
                    }
                };
            }
        },
        // TODO: Need to verify this for cross site changes
        // escapeMarkup: function (m) {
        //     return DOMPurify.sanitize(m);
        // }
    }).attr('id',selectFieldId);
    return selector
}

function buildReportingDestinationsSelectBox(destinationsSelectBox, url, sharedWithData, primaryDestinationField, isPrimarySelectable) {
    var selectReportings = bindShareWith2WithDataProductAssignment(destinationsSelectBox, url, sharedWithData);
    $(selectReportings).closest("td").find(".select2-container").on("mousedown", "li.select2-selection__choice .addPrimary", function (event) {
        event.preventDefault();
        event.stopPropagation();
    })
    $(selectReportings).closest("td").find(".select2-container").on("click", "li.select2-selection__choice .addPrimary", function () {
        if (isPrimarySelectable != false) {
            $(this).closest(".select2-container").find("li.select2-selection__choice .addPrimary").removeClass("primary");
            $(this).addClass("primary");
            primaryDestinationField.val($(this).parent().find('.name').data('id'));
        }
    });
    $(selectReportings).closest("td").find(".select2-container").on("mousedown", ".select2-selection__choice__remove", function (e) {
        if (isPrimarySelectable != false) {
            var primaryDestination = primaryDestinationField.val();
            if ($(this).siblings(".name").data('id') == primaryDestination) {
                var first = $(this).parent().parent().find("li.select2-selection__choice")[0];
                if (first) {
                    primaryDestinationField.val($(first).find('div').text());
                } else
                    primaryDestinationField.val("");
            }
        }
    })


    $(selectReportings).on("change", function (e) {
        var primaryDestination = primaryDestinationField.val();
        if (isPrimarySelectable && !primaryDestination) {
            var first = $(this).closest("td").find(".select2-container").find("li.select2-selection__choice")[0];
            if (first) {
                primaryDestinationField.val($(first).find('.name').data('id'));
                primaryDestination = primaryDestinationField.val();
            }
        }

        $(this).closest("td").find(".select2-container").find("li.select2-selection__choice .addPrimary").removeClass('primary');
        $(this).closest("td").find(".select2-container").find("li.select2-selection__choice").each(function () {
            if ($(this).find(".addPrimary").length == 0) {
                $(this).append("<span class='addPrimary'>P</span>")
            }
            if (primaryDestination && $(this).find('.name').data('id') == primaryDestination) {
                $(this).find(".addPrimary").addClass("primary")
            }

        });
    });


}

function formatState(state) {
    var ele = $(state.element);
    let primaryUser = $(ele).closest("td").find("#primaryAssignment").val();
    let statement = '<span><i class="fa fa-eye-slash"  title=' + BLINDING_STATUS.BLINDED + '></i></span>';
    let finalStatement;
    var isBlinded = ele.attr('data-blinded');

    if(state.id != primaryUser) {
        if (isBlinded || state.blinded) {
            finalStatement = statement + '<span class="name" data-id="'+state.id+'">' + state.text + '</span>' +
                '<span class="addPrimary">P</span>'
        } else {
            finalStatement = '<span class="name" data-id="'+state.id+'">' + state.text + '</span>' +
                '<span class="addPrimary">P</span>'
        }
    } else {
        if (isBlinded || state.blinded) {
            finalStatement = statement + '<span class="name" data-id="'+state.id+'">' + state.text + '</span>' +
                '<span class="addPrimary primary">P</span>'
        } else {
            finalStatement = '<span class="name" data-id="'+state.id+'">' + state.text + '</span>' +
                '<span class="addPrimary primary">P</span>'
        }
    }
    return $(finalStatement);
}
