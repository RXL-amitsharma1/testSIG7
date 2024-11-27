var signal = signal || {};
var existingRows = [];
signal.commonTag = (function () {
    var openCommonAlertTagModal = function (module, domain, tags) {
        $(document).on('click', '.alert-check-box', function () {
            var tableCheckBoxSelector
            if (typeof isCaseDetailView !== "undefined" && isCaseDetailView == "true") {
                tableCheckBoxSelector = 'table#alertsDetailsTable .copy-select';
            } else {
                tableCheckBoxSelector = '.copy-select';
            }
            $(tableCheckBoxSelector).each(function () {
                if($(this).is(':checked')) {
                    alertIdSet.add($(this).attr('data-id'));
                }
            });

            if ($(this).is(':checked')) {
                if ($(this).closest('td').attr('data-dt-column') == "0") {
                    alertIdSet.add($(this).attr('data-id'));
                    ProductIdList.push($(this).closest('tr').find("[data-field='productName']").attr('data-id'));
                } else if (domain == 'Literature') {
                    alertIdSet.add($(this).attr('data-id'));
                } else if (isCaseDetailView && isCaseDetailView == 'true') {
                    alertIdSet.add($(this).attr('data-id'));
                    ProductIdList.push($(this).closest('tr').find("[data-field='productName']").attr('data-id'));
                }
            } else {
                alertIdSet.delete($(this).attr('data-id'));
                let selectedProduct = $(this).closest('tr').find("[data-field='productName']").attr('data-id')

                let isRemoved = false;
                ProductIdList = ProductIdList.filter(function (product) {
                    if (product === selectedProduct && !isRemoved) {
                        isRemoved = true;
                        return false;
                    }
                    return true;
                });
            }
        });

        $(document).on('click', '#select-all', function () {
            $(".alert-check-box").prop('checked', this.checked);
            $(".alert-select-all").prop('checked', this.checked);
            $('.alert-check-box').each(function () {
            if ($(this).is(':checked')) {
                    if ($(this).closest('td').attr('data-dt-column') == "0") {
                        alertIdSet.add($(this).attr('data-id'));
                        ProductIdList.push($(this).closest('tr').find("[data-field='productName']").attr('data-id'));
                    } else if (domain == 'Literature') {
                        alertIdSet.add($(this).attr('data-id'));
                    } else if (isCaseDetailView && isCaseDetailView == 'true') {
                        alertIdSet.add($(this).attr('data-id'));
                        ProductIdList.push($(this).closest('tr').find("[data-field='productName']").attr('data-id'));

                }
            } else {
                alertIdSet.delete($(this).attr('data-id'));
                ProductIdList.pop($(this).closest('tr').find("[data-field='productName']").attr('data-id'));
            }
        });
        });
        $(document).on('click', '.editAlertTags', function (evt) {
            $('#commonTagModal').find('#addTags').attr('disabled', true);
            var parent_row = $(event.target).closest('tr');

            var index = $(this).closest('tr').index();
            if (domain === ALERT_CONFIG_TYPE_SHORT.LITERATURE_ALERT && isAbstractViewOrCaseView(index)) {
                index = index / 2;
            }
            var rowObject = table.rows(index).data()[0];
            var alertId;
            if (parent_row.find("[data-field='alertId']") && parent_row.find("[data-field='alertId']").attr('data-id')) {
                alertId = parent_row.find("[data-field='alertId']").attr('data-id');
            } else {
                alertId = rowObject.id;
            }
            var commonTagModalObj = $('#commonTagModal');
            if (isArchived == "true" && domain == "Qualitative") {
                $("#add-row").hide();
                $("#addTags").hide();
            }

            if (alertIdSet.has(parent_row.find('.alert-check-box').attr('data-id')) && alertIdSet.size > 1) {

                let uniqueProducts = new Set(ProductIdList); // bugfix-PVS-74253
                if (uniqueProducts?.size > 1) {
                    $.Notification.notify('warning', 'top right', "Warning", "All selected safety observation must have the same Product Name for performing the bulk update", {autoHideDelay: 5000});

                } else {
                    var currentId = parent_row.find('.alert-check-box').attr('data-id');
                    fillAlertTagsBulkCategory(commonTagModalObj, bulkCategoryUrl, module, domain, tags, Array.from(alertIdSet), currentId);
                    commonTagModalObj.modal('show');
                }
            } else {

                fillAlertTags(commonTagModalObj, fillCommonTagUrl, module, domain, tags, alertId);
                commonTagModalObj.modal('show');
            }
        });
    };
    return {
        openCommonAlertTagModal: openCommonAlertTagModal
    }
})();

var fillAlertTags = function (tagModal, fillCommonTagUrl, module, domain, tags, alertId) {
    var dataUrl = fillCommonTagUrl + "/"+module+"?domain="+domain + "&alertId="+alertId + '&isArchived=' + isArchived;

    if (domain === 'Literature') {
        dataUrl += "&datasource=" + selectedDatasource;
    }
    var disabledSubCatList = [];
    var disabledCategoryList = [];
    categoryModalTable = $("#categoryModalTable").DataTable({
        "language": {
            "url": "../assets/i18n/dataTables_" + userLocale + ".json"
        },
        "createdRow": function ( row, data, index ) {
            var tagsEnabled = nonConfiguredEnabled != "true" ? false : true;
            var categories = [];
            tags.forEach(function (map) {
                if (map.id == data.category.id) {
                    disabledCategoryList.push(map.id);
                }
            });
            tags.forEach(function (map) {
                if(map.parentId ==0 || map.parentId == null) {
                    if (disabledCategoryList.includes(map.id)) {
                        categories.push({id: map.id, text: map.text, disabled: true});
                    } else {
                        categories.push({id: map.id, text: map.text, disabled: map.display == 0 ? true : false});
                    }
                }
            });
            select2Category(row, tagsEnabled, categories, nonConfiguredEnabled, tags, data);
            $('select', row).eq(1).on('change', function (e) {
                setSelectWidth($(this));
            });

            if(data.action != 1) {
                $('input', row).closest('tr').find(":input").attr('disabled', true);
                if(data.subcategory.length <= 0)
                    $('input', row).closest('tr').find(":input[class=select2-search__field]").attr("placeholder", "");
            }

            $(".edit-row", row).off().on('click', function(){
                if(data.autoTaggedEditable == false){
                    return false;
                }
                var isDisabled = $(this).closest('tr').find(":input:not(:first)").attr('disabled');
                $(this).closest('tr').find(":input:not(:first)").attr('disabled', !isDisabled);
                if(isDisabled && data.subcategory.length <= 0)
                    $(this).closest('tr').find(":input[class=select2-search__field]").attr("placeholder", "Select Sub Category").css("width", "100%");
                else if(isDisabled && data.subcategory.length > 0)
                    $(this).closest('tr').find(":input[class=select2-search__field]").attr("placeholder", "");
                else if(!isDisabled && data.subcategory.length <= 0)
                    $(this).closest('tr').find(":input[class=select2-search__field]").attr("placeholder", "");
                var isPrivate = $(this).closest('tr').find(":input[class=private-check-box]").attr("data-id");
                if(isPrivate == 'false')
                    $(this).closest('tr').find(":input[class=private-check-box]").attr("disabled",true)
                $('#commonTagModal').find('#addTags').attr('disabled', false);


            });

            $(".delete-row", row).off().on('click', function(){
                var rowId = $("select", row).val();
                var indexToRemove = disabledCategoryList.indexOf(parseInt(rowId));
                if (indexToRemove !== -1) {
                    disabledCategoryList.splice(indexToRemove, 1);
                }
                if(data.autoTaggedEditable == false){
                    return false;
                }
                categoryModalTable
                    .row( $(this).parents('tr') )
                    .remove()
                    .draw();
                $('#commonTagModal').find('#addTags').attr('disabled', false);
            });

            $(".up-row", row).off().on('click', function(){
                const $row = $(this).parents('tr');
                if ($row.index() === 0) {
                    return;
                }
                $row.prev().before($row.get(0));
            });

            $(".down-row", row).off().on('click', function(){
                const $row = $(this).parents('tr');
                $row.next().after($row.get(0));
            });

            if(data.autoTaggedEditable == false){
                $('input', row).closest('tr').find(".edit-row,.delete-row").css('color', "rgb(183, 183, 183)");
                $('input', row).closest('tr').find(".edit-row,.delete-row").css('cursor', "not-allowed");
            }

        },
        drawCallback: function (settings) {
            setSelectWidth($(".dynamicSelectFieldSubCategory"));
        },
        rowCallback: function( row, data, index ) {
            if (data['private'] == true && data['privateAccess'] != true) {
                $(row).hide();
            }
        },
        initComplete: function(settings, json){
            existingRows = [];
            categoryModalTable.rows().every(function(index, element){
                var value = this.data();
                var currentRow = {category: value.category, subcategory: value.subcategory, alert: value.alert, private: value.private,
                    createdBy: value.createdBy, createdDate: value.createdDate, priority: value.priority, updatedDateInit: value.updatedDateInit,
                    updatedByInit: value.updatedByInit, createdDateInit: value.createdDateInit,
                    createdByInit: value.createdByInit, autoTagged: value.autoTagged, isRetained: value.isRetained,
                    privateUserId: value.privateUserId,
                    execConfigId: value.execConfigId, dataSource : value.dataSource};
                if(currentRow.category.id != 0)
                    existingRows.push(currentRow);
            });
            if(isArchived == "true" && domain == "Qualitative") {
                $(".delete-row").hide()
                $(".edit-row").hide()
            }
            disabledSubCatList.forEach(function (value, index1) {
                $('.dynamicSelectFieldSubCategory').find('option[value="' + value + '"]').prop("disabled", true);
            });
            categoryMaximumSize(nonConfiguredEnabled);
        },
        "ajax": {
            "url": dataUrl,
            "dataSrc": ""
        },
        searching: true,
        "bLengthChange": true,
        "paging":   false,
        "ordering": false,
        "searching": false,
        "info": false,
        "bDestroy": true,
        stateSave: true,
        "scrollY":        "350px",
        "scrollCollapse": true,
        "aoColumns": [
            {
                "mData": "category",
                "width": "25%",
                'className': 'dt-center',
                "mRender": function (data, type, row) {
                    var html = '<select name="selectField" id="dynamicSelectFieldCategory" data-dataSource="'+ row.dataSource+'" class="dynamicSelectFieldCategory form-control">';
                    if(data) {
                        html += '<option selected="selected" value="' + data.id + '">' + encodeToHTML(data.name) + '</option>';
                    }
                    html += '</select>';
                    return html;
                }
            },
            {
                "mData": "subcategory",
                'className': 'dt-center',
                "width": "35%",
                "mRender": function (data, type, row) {

                    var html = '<select name="selectField" id="dynamicSelectFieldSubCategory" multiple="multiple" class="dynamicSelectFieldSubCategory form-control">';
                    row.subcategory.forEach(function (obj) {
                        html += '<option selected="selected" value="'+obj.id+'">'+encodeToHTML(obj.name)+'</option>';
                    });
                    html += '</select>';
                    return html

                }
            },
            {
                "mData": "alert",
                "className": "justification-cell dataTableColumnCenter text-center",
                "width": "15%",
                "mRender": function (data, type, row) {
                    var checked = row.alert == true ? "checked": "";
                    return '<input type="checkbox" class="alert-check-box-tag" ' + checked +' data-id=' + row.alert + ' />';

                }
            },
            {
                "mData": "private",
                "width": "12%",
                "mRender": function (data, type, row) {
                    var checked = row.private == true ? "checked": "";
                    return '<input type="checkbox" class="private-check-box" ' + checked  +' data-id=' + row.private + ' />';
                },
                'className': 'dt-center dataTableColumnCenter text-center'
            },
            {
                "mData": "action",
                "width": "13%",
                "className": "td-action",
                "mRender": function (data, type, row) {
                    var saveCategoryAccessVar = "";
                    if(typeof saveCategoryAccess !== "undefined"){
                        saveCategoryAccessVar = saveCategoryAccess;
                    }
                    if(data === 1){
                        return '<i class="fa fa-trash-o delete-row tag-action"></i>';
                    }
                    else {

                        var details = '<b>Created By:</b> '+row.createdBy +'<hr/> <b>Created Date:</b> '+row.createdDate +'<hr/> <b>Last Modified By:</b> '+row.updatedBy
                            +'<hr/> <b>Last Modified Date:</b> '+row.updatedDate;
                        if(!row.createdBy && !row.createdDate ){
                            return '  <i class="fa fa-pencil edit-row tag-action '+ saveCategoryAccessVar+'" ></i>' +
                                '  <i class="fa fa-trash-o delete-row tag-action '+ saveCategoryAccessVar+'"></i>';
                        }
                        return '<i id="tag-popup" class="glyphicon glyphicon-info-sign themecolor popoverMessage tag-action" title="Details" data-content="' + details + '"></i>' +
                            '  <i class="fa fa-pencil edit-row tag-action '+ saveCategoryAccessVar+'" ></i>' +
                            '  <i class="fa fa-trash-o delete-row tag-action '+ saveCategoryAccessVar+'"></i>';
                    }
                }
            }
        ],
        columnDefs: [{
            "targets": '_all',
            "render": $.fn.dataTable.render.text()
        }]
    });
    $( "#categoryModalTable" ).sortable({
        items: 'tr:not(:first)',
        opacity: 0.6,
        scrollSpeed: 40,
        cancel: "thead",
        containment : "parent"
    });

    if(privateEnabled != "true") {
        categoryModalTable.column(3).visible(false);
    }else{
        categoryModalTable.column(3).visible(true);
    }
    $('#categoryModalTable').on('mouseover', 'tr', function () {
        $('.popoverMessage').popover({
            placement: 'left',
            trigger: 'hover focus',
            viewport: 'body',
            html: true,
            container: 'body'
        });
    });

    $("#add-row").off().on('click', function () {
        var lastRow = categoryModalTable.row(':last').data();
        var tagsEnabled = nonConfiguredEnabled != "true" ? false : true;
        var lastRowCategory = $('#categoryModalTableBody tr:last').find("td select.dynamicSelectFieldCategory").val();
        $('#categoryModalTableBody tr').each(function () {
            var th = $(this);
            var categoryId = $(th).find("td select.dynamicSelectFieldCategory").val();
            categoryId = parseInt(categoryId);
            if (!disabledCategoryList.includes(categoryId)) {
                disabledCategoryList.push(categoryId);
            }
        });
        if(lastRowCategory != 0) {
            categoryModalTable.row.add({
                "category": {id: 0, name: "Select Category"},
                "subcategory": [],
                "alert": false,
                "private": false,
                "action": 1,
                "priority": 9999,
                "new": true
            }).draw(false);
            categoryMaximumSize(nonConfiguredEnabled);
            var $scrollBody = $(categoryModalTable.table().node()).parent();
            $scrollBody.scrollTop($scrollBody.get(0).scrollHeight);
        }
        $('#categoryModalTableBody tr:last select').eq(0).focus();
        $('#commonTagModal').find('#addTags').attr('disabled', false);
    });

    $(".addTags").off().one('click',function () {
        var newRows = [];
        var count = 1;
        $('#categoryModalTableBody tr').each(function(){
            var th = $(this);
            var categoryId = $(th).find("td select.dynamicSelectFieldCategory").val();
            var dataSource = $(th).find("td select.dynamicSelectFieldCategory").attr("data-dataSource");
            var categoryText = $(th).find("td select.dynamicSelectFieldCategory option:selected").text();
            var category = {id: categoryId, name: categoryText};
            var subcategories = [];
            $(th).find("td select.dynamicSelectFieldSubCategory option:selected").each(function(){
                subcategories.push({id: $(this).val(), name:$(this).text()});
            });
            var alert = $(th).find("td input.alert-check-box-tag").is(":checked");
            var pvt = $(th).find("td input.private-check-box").is(":checked");
            var currentRow = {category: category, subcategory: subcategories, alert: alert, private: pvt, priority: count,
                                autoTagged: false, isRetained: false, execConfigId: -1, dataSource: dataSource};
            if(currentRow.category.name != 'Select Category' && currentRow.category.name != '') {
                newRows.push(currentRow);
                count = count + 1;
            }
        });

        var data = {
            newRows: JSON.stringify(newRows),
            existingRows: JSON.stringify(existingRows),
            alertId: alertId,
            type: domain,
            isArchived: isArchived
        };

        if (domain === 'Literature') {
            data.datasource = selectedDatasource;
        }

        if (data.newRows != data.existingRows) {
            $.ajax({
                url: saveCommonTagsUrl,
                type: "POST",
                async: false,
                data: JSON.stringify(data),
                contentType: 'application/json; charset=utf-8',
                dataType: "json"
            })
                .done(function (data) {
                    table.ajax.reload()
                    alertIdSet.clear()
                    ProductIdList.splice(0, ProductIdList.length);
                    $.Notification.notify('success', 'top right', "Success", "Categories saved successfully.", {autoHideDelay: 5000});

                })
                .fail(function () {
                    $.Notification.notify('error', 'top right', "Failed", 'Something Went Wrong, Please Contact Your Administrator.', {autoHideDelay: 5000});
            })
        }
    });
    return categoryModalTable
};


// bulk category
var fillAlertTagsBulkCategory = function (tagModal, fillCommonTagUrl, module, domain, tags, alertId, currentId) {
    var dataUrl = fillCommonTagUrl + "/" + module + "?domain=" + domain + "&alertId=" + alertId + '&isArchived=' + isArchived;
    if (domain === 'Literature') {
        dataUrl += "&datasource=" + selectedDatasource;
    }
    var disabledSubCatList = [];
    var disabledCategoryListForBulk = [];
    categoryModalTable = $("#categoryModalTable").DataTable({
        "language": {
            "url": "../assets/i18n/dataTables_" + userLocale + ".json"
        },
        "createdRow": function (row, data, index) {
            var tagsEnabled = nonConfiguredEnabled == "true" ;
            var categories = [];
            tags.forEach(function (map) {
                if (map.id == data.category.id) {
                    disabledCategoryListForBulk.push(map.id);
                }
            });
            tags.forEach(function (map) {
                if(map.parentId ==0 || map.parentId == null) {
                    if (disabledCategoryListForBulk.includes(map.id)) {
                        categories.push({id: map.id, text: encodeToHTML(map.text), disabled: true});
                    } else {
                        categories.push({id: map.id, text: encodeToHTML(map.text), disabled: map.display == 0 ? true : false});
                    }
                }
            });
            $('select', row).eq(0).select2({
                tags: tagsEnabled,
                data: categories,
                placeholder: "Select Category"
            }).on('change', function (obj) {

                var subtagsEnabled = nonConfiguredEnabled == "true" ;
                var subcategories = [];
                tags.forEach(function (map) {
                    if (map.parentId == obj.currentTarget.value) {
                        var isDisabledSubCat = map.display == 0 ? true : false
                        subcategories.push({id: map.id, text: encodeToHTML(map.text), disabled: isDisabledSubCat});
                        if (isDisabledSubCat)
                            disabledSubCatList.push(map.id)

                    }
                });

                if (data.action == 1) {
                    $('select', row).eq(1).empty();
                }

                // if no subcategory configure then can not add on fly subcategory
                subtagsEnabled = (subcategories.length > 0) && subtagsEnabled

                $('select', row).eq(1).select2({
                    tags: subtagsEnabled,
                    data: subcategories,
                    placeholder: "Select Sub Category"
                });
            }).trigger('change');
            $('select', row).eq(1).on('change', function (e) {
                setSelectWidth($(this));
            });

            if (data.action != 1) {
                $('input', row).closest('tr').find(":input").attr('disabled', true);
                if (data.subcategory.length <= 0)
                    $('input', row).closest('tr').find(":input[class=select2-search__field]").attr("placeholder", "");
            }

            $(".edit-row", row).off().on('click', function () {
                if (data.autoTaggedEditable == false) {
                    return false;
                }
                var isDisabled = $(this).closest('tr').find(":input:not(:first)").attr('disabled');
                $(this).closest('tr').find(":input:not(:first)").attr('disabled', !isDisabled);
                if (isDisabled && data.subcategory.length <= 0) {
                    $(this).closest('tr').find(":input[class=select2-search__field]").attr("placeholder", "Select Sub Category").css("width", "100%");
                } else if (isDisabled && data.subcategory.length > 0) {
                    $(this).closest('tr').find(":input[class=select2-search__field]").attr("placeholder", "");
                } else if (!isDisabled && data.subcategory.length <= 0) {
                    $(this).closest('tr').find(":input[class=select2-search__field]").attr("placeholder", "");
                }
                var isPrivate = $(this).closest('tr').find(":input[class=private-check-box]").attr("data-id");
                if (isPrivate == 'false') {
                    $(this).closest('tr').find(":input[class=private-check-box]").attr("disabled", true)
                }
                $('#commonTagModal').find('#addTags').attr('disabled', false);


            });

            $(".delete-row", row).off().on('click', function () {
                var rowId = $("select", row).val();
                var indexToRemove = disabledCategoryListForBulk.indexOf(parseInt(rowId));
                if (indexToRemove !== -1) {
                    disabledCategoryListForBulk.splice(indexToRemove, 1);
                }
                if (data.autoTaggedEditable == false) {
                    return false;
                }
                categoryModalTable
                    .row($(this).parents('tr'))
                    .remove()
                    .draw();
                $('#commonTagModal').find('#addTags').attr('disabled', false);
            });

            $(".up-row", row).off().on('click', function () {
                const $row = $(this).parents('tr');
                if ($row.index() === 0) {
                    return;
                }
                $row.prev().before($row.get(0));
            });

            $(".down-row", row).off().on('click', function () {
                const $row = $(this).parents('tr');
                $row.next().after($row.get(0));
            });

            if (data.autoTaggedEditable == false) {
                $('input', row).closest('tr').find(".edit-row,.delete-row").css('color', "rgb(183, 183, 183)");
                $('input', row).closest('tr').find(".edit-row,.delete-row").css('cursor', "not-allowed");
            }

        },
        drawCallback: function (settings) {
            setSelectWidth($(".dynamicSelectFieldSubCategory"));
        },
        rowCallback: function (row, data, index) {
            if (data['private'] == true && data['privateAccess'] != true) {
                $(row).hide();
            }
        },
        initComplete: function (settings, json) {
            existingRows = [];
            categoryModalTable.rows().every(function (index, element) {
                var value = this.data();
                var currentRow = {
                    category: value.category,
                    subcategory: value.subcategory,
                    alert: value.alert,
                    private: value.private,
                    createdBy: value.createdBy,
                    createdDate: value.createdDate,
                    priority: value.priority,
                    updatedDateInit: value.updatedDateInit,
                    updatedByInit: value.updatedByInit,
                    createdDateInit: value.createdDateInit,
                    createdByInit: value.createdByInit,
                    autoTagged: value.autoTagged,
                    isRetained: value.isRetained,
                    privateUserId: value.privateUserId,
                    execConfigId: value.execConfigId
                };
                if (currentRow.category.id != 0)
                    existingRows.push(currentRow);
            });
            if (isArchived == "true" && domain == "Qualitative") {
                $(".delete-row").hide()
                $(".edit-row").hide()
            }
            disabledSubCatList.forEach(function (value, index1) {
                $('.dynamicSelectFieldSubCategory').find('option[value="' + value + '"]').prop("disabled", true);
            });
            categoryMaximumSize(nonConfiguredEnabled);
        },
        "ajax": {
            "url": dataUrl,
            "dataSrc": ""
        },
        searching: true,
        "bLengthChange": true,
        "paging": false,
        "ordering": false,
        "searching": false,
        "info": false,
        "bDestroy": true,
        stateSave: true,
        "scrollY": "350px",
        "scrollCollapse": true,
        "aoColumns": [
            {
                "mData": "category",
                "width": "25%",
                'className': 'dt-center',
                "mRender": function (data, type, row) {
                    var html = '<select name="selectField" id="dynamicSelectFieldCategory" class="dynamicSelectFieldCategory form-control">';
                    if (data) {
                        html += '<option selected="selected" value="' + data.id + '">' + encodeToHTML(data.name) + '</option>';
                    }
                    html += '</select>';
                    return html;
                }
            },
            {
                "mData": "subcategory",
                'className': 'dt-center',
                "width": "35%",
                "mRender": function (data, type, row) {

                    var html = '<select name="selectField" id="dynamicSelectFieldSubCategory" multiple="multiple" class="dynamicSelectFieldSubCategory form-control">';
                    row.subcategory.forEach(function (obj) {
                        html += '<option selected="selected" value="' + obj.id + '">' + encodeToHTML(obj.name) + '</option>';
                    });
                    html += '</select>';
                    return html

                }
            },
            {
                "mData": "alert",
                "className": "justification-cell dataTableColumnCenter text-center",
                "width": "15%",
                "mRender": function (data, type, row) {
                    var checked = row.alert == true ? "checked" : "";
                    return '<input type="checkbox" class="alert-check-box-tag" ' + checked + ' data-id=' + row.alert + ' />';

                }
            },
            {
                "mData": "private",
                "width": "12%",
                "mRender": function (data, type, row) {
                    var checked = row.private == true ? "checked" : "";
                    return '<input type="checkbox" class="private-check-box" ' + checked + ' data-id=' + row.private + ' />';
                },
                'className': 'dt-center dataTableColumnCenter text-center'
            },
            {
                "mData": "action",
                "width": "13%",
                "className": "td-action",
                "mRender": function (data, type, row) {
                    var saveCategoryAccessVar = "";
                    if (typeof saveCategoryAccess !== "undefined") {
                        saveCategoryAccessVar = saveCategoryAccess;
                    }
                    if (data === 1) {
                        return '<i class="fa fa-trash-o delete-row tag-action"></i>';
                    } else {

                        var details = '<b>Created By:</b> ' + row.createdBy + '<hr/> <b>Created Date:</b> ' + row.createdDate + '<hr/> <b>Last Modified By:</b> ' + row.updatedBy
                            + '<hr/> <b>Last Modified Date:</b> ' + row.updatedDate;
                        if (!row.createdBy && !row.createdDate) {
                            return '  <i class="fa fa-pencil edit-row tag-action ' + saveCategoryAccessVar + '" ></i>' +
                                '  <i class="fa fa-trash-o delete-row tag-action ' + saveCategoryAccessVar + '"></i>';
                        }
                        return '<i id="tag-popup" class="glyphicon glyphicon-info-sign themecolor popoverMessage tag-action" title="Details" data-content="' + details + '"></i>' +
                            '  <i class="fa fa-pencil edit-row tag-action ' + saveCategoryAccessVar + '" ></i>' +
                            '  <i class="fa fa-trash-o delete-row tag-action ' + saveCategoryAccessVar + '"></i>';
                    }
                }
            }
        ],
        columnDefs: [{
            "targets": '_all',
            "render": $.fn.dataTable.render.text()
        }]
    });
    $("#categoryModalTable").sortable({
        items: 'tr:not(:first)',
        opacity: 0.6,
        scrollSpeed: 40,
        cancel: "thead",
        containment: "parent"
    });

    if (privateEnabled != "true") {
        categoryModalTable.column(3).visible(false);
    } else {
        categoryModalTable.column(3).visible(true);
    }
    $('#categoryModalTable').on('mouseover', 'tr', function () {
        $('.popoverMessage').popover({
            placement: 'left',
            trigger: 'hover focus',
            viewport: 'body',
            html: true,
            container: 'body'
        });
    });

    $("#add-row").off().on('click', function () {
        var lastRow = categoryModalTable.row(':last').data();
        var tagsEnabled = nonConfiguredEnabled != "true" ? false : true;
        var lastRowCategory = $('#categoryModalTableBody tr:last').find("td select.dynamicSelectFieldCategory").val();
        $('#categoryModalTableBody tr').each(function () {
            var th = $(this);
            var categoryId = $(th).find("td select.dynamicSelectFieldCategory").val();
            categoryId = parseInt(categoryId);
            if (!disabledCategoryListForBulk.includes(categoryId)) {
                disabledCategoryListForBulk.push(categoryId);
            }
        });
        if (lastRowCategory != 0) {
            categoryModalTable.row.add({
                "category": {id: 0, name: "Select Category"},
                "subcategory": [],
                "alert": false,
                "private": false,
                "action": 1,
                "priority": 9999,
                "new": true
            }).draw(false);
            categoryMaximumSize(nonConfiguredEnabled);
            var $scrollBody = $(categoryModalTable.table().node()).parent();
            $scrollBody.scrollTop($scrollBody.get(0).scrollHeight);
        }
        $('#categoryModalTableBody tr:last select').eq(0).focus();
        $('#commonTagModal').find('#addTags').attr('disabled', false);
    });

    $(".addTags").off().one('click',function () {
        // var newRows = [];
        // var count = 1;
        var alertIdsList = []
        if (typeof isCaseDetailView !== "undefined" && isCaseDetailView == "true") {
            checkboxSelector = 'table#alertsDetailsTable .copy-select:checked';
        } else {
            checkboxSelector = '.copy-select:checked';
        }
        $.each($(checkboxSelector), function () {
            if($(this).is(':checked')){
               alertId.push($(this).attr("data-id"));
            }
        })
        alertIdsList = Array.from(new Set(alertId));
        if (alertIdsList.length >= 1) {

            categoriesBulkUpdate(alertIdsList.length, alertIdsList, domain, isArchived, currentId)
        }
    });
    return categoryModalTable
};


var categoriesBulkUpdate = function (totalSelectedRecords, alertId, domain, isArchived, currentId) {
    var textToDisplay;
    switch (applicationName) {
        case 'Aggregate Case Alert on Demand':
            textToDisplay = 'PEC';
            break
        case 'Single Case Alert on Demand':
            textToDisplay = 'Case';
            break;
        case 'Single Case Alert':
            textToDisplay = 'Case';
            break;
        case 'Aggregate Case Alert':
            textToDisplay = 'PEC';
            break;
        case 'EVDAS Alert':
            textToDisplay = 'PEC';
            break;
        case 'Ad-Hoc Alert':
            textToDisplay = 'Observation';
            break;
        case 'Literature Search Alert':
            textToDisplay = 'Article';
            break;
    }
    bootbox.dialog({
        title: 'Apply To All',
        message: signal.utils.render('bulk_operation_options', {
            totalSelectedRecords: totalSelectedRecords,
            alertType: textToDisplay
        }),
        buttons: {
            ok: {
                label: "Ok",
                className: 'btn btn-primary',
                callback: function () {
                    switch ($('input[name=bulkOptions]:checked').val()) {
                        case 'current':
                            saveBulkCategories($('input[name=bulkOptions]:checked').val(), currentId, domain, isArchived)
                            this.hide();
                            break;
                        case 'allSelected':
                            saveBulkCategories($('input[name=bulkOptions]:checked').val(), alertId, domain, isArchived)
                            this.hide();
                            break;
                    }
                }
            },
            cancel: {
                label: "Cancel",
                className: 'btn btn-default'
            }
        }
    });
};

var saveBulkCategories = function (selectedRow, alertId, domain, isArchived) {
    var newRows = [];
    var count = 1;
    $('#categoryModalTableBody tr').each(function () {
        var th = $(this);
        var categoryId = $(th).find("td select.dynamicSelectFieldCategory").val();
        var categoryText = $(th).find("td select.dynamicSelectFieldCategory option:selected").text();
        var category = {id: categoryId, name: categoryText};
        var subcategories = [];
        $(th).find("td select.dynamicSelectFieldSubCategory option:selected").each(function () {
            subcategories.push({id: $(this).val(), name: $(this).text()});
        });
        var alert = $(th).find("td input.alert-check-box-tag").is(":checked");
        var pvt = $(th).find("td input.private-check-box").is(":checked");

        var currentRow = {
            category: category, subcategory: subcategories, alert: alert, private: pvt, priority: count,
            autoTagged: false, isRetained: false, execConfigId: -1
        };

        if (currentRow.category.name != 'Select Category' && currentRow.category.name != '') {
            newRows.push(currentRow);
            count = count + 1;
        }
    });
    
    // isDeleteAction checks if it is Addition/Deletion case
    var isDeleteAction = existingRows.length > newRows.length && existingRows.some(row =>
        !newRows.find(newRow => JSON.stringify(newRow) === JSON.stringify(row))
    );


    var enableSave = true
    if (!isAdmin || (typeof isCategoryRole !== "undefined" && !isCategoryRole)) {
        for (var rowIndex = 0, len = existingRows.length; rowIndex < len; rowIndex++) {
            if (existingRows[rowIndex].autoTagged) {
                var row = newRows.find((o) => {
                    return o["category"].name === existingRows[rowIndex].category.name
                })
                if (isDeleteAction && (!row || JSON.stringify(row?.subcategory) != JSON.stringify(existingRows[rowIndex].subcategory) || row.alert != existingRows[rowIndex].alert || row.private != existingRows[rowIndex].private)) {
                    enableSave = false
                    break;
                }
            }
        }
    }
    if (enableSave) {
        var data = {
            newRows: JSON.stringify(newRows),
            existingRows: JSON.stringify(existingRows),
            alertId: alertId,
            type: domain,
            isArchived: isArchived,
            selectedRow: selectedRow,
            isCaseSeries: isCaseSeries
        };

        if (domain === 'Literature') {
            data.datasource = selectedDatasource;
        }

        if (data.newRows != data.existingRows) {
            $.ajax({
                url: bulkUpdateCategoryUrl,
                type: "POST",
                async: false,
                data: JSON.stringify(data),
                contentType: 'application/json; charset=utf-8',
                dataType: "json"
            })
                .done(function (data) {

                    alertIdSet.clear();
                    ProductIdList.splice(0, ProductIdList.length);
                    if(domain != "Quantitative on demand") {
                        $.each(selectedCases, function (key, value) {
                            $(".copy-select[data-id|=" + value + "]").prop("checked", false);
                        });
                    }
                    selectedCases = [];
                    selectedCasesInfo = [];
                    caseJsonArrayInfo = [];
                    prev_page = [];
                    table.ajax.reload()
                    $.Notification.notify('success', 'top right', "Success", "Categories saved successfully.", {autoHideDelay: 5000});
            })
        }
    } else {
        $.Notification.notify('warning', 'top right', "Warning", "You do not have privileges to perform updates on auto tagged categories.", {autoHideDelay: 5000});
    }


};

function getSelectedOptionsFromTable() {
    var selectedOptions = [];
    $('.select2-selection__rendered[id^="select2-dynamicSelectFieldCategory-"]').each(function() {
        selectedOptions = selectedOptions.concat($(this).text().trim().toLowerCase());
    });
    return selectedOptions;
}

var select2Category = function (row, tagsEnabled, categories, nonConfiguredEnabled, tags, data) {
    $('select', row).eq(0).select2({
        tags: tagsEnabled,
        data: categories,
        createTag: function (params) {
            var term = params.term;
            if (term.slice(-1) === ' ' || term[0] == ' ' || /\s{2,}/.test(term)) {
                return null;
            }
            var existingOptions = getSelectedOptionsFromTable();
            if (existingOptions.indexOf(params.term.toLowerCase()) >= 0) {
                return null;
            }

            return {
                id: term,
                text: term
            };
        },
        placeholder: "Select Category"
    }).on('change', function (obj) {
        var subtagsEnabled = nonConfiguredEnabled == "true";
        var subcategories = [];
        tags.forEach(function (map) {
            if (map.parentId == obj.currentTarget.value) {
                var isDisabledSubCat = map.display == 0
                subcategories.push({id: map.id, text: map.text, disabled: isDisabledSubCat});
                if (isDisabledSubCat)
                    disabledSubCatList.push(map.id)

            }
        });

        if (data.action == 1) {
            $('select', row).eq(1).empty();
        }

        // if no subcategory configure then can not add on fly subcategory
        subtagsEnabled = (subcategories.length > 0) && subtagsEnabled

        $('select', row).eq(1).select2({
            tags: subtagsEnabled,
            data: subcategories,
            placeholder: "Select Sub Category"
        });
    }).trigger('change');
};



