//= require app/pvs/common/rx_common.js
//= require app/pvs/common/rx_alert_utils.js
var selectData;
var countsList = [];
var textFields;
var data = [];
var booleanFields;

//These no. specify to enable next no. of select box.
var enableSelectData = {
    ALGORITHM: 3,
    QUERY_CRITERIA: 1,
    COUNTS: 3,
    REVIEW_STATE: 1,
    SIGNAL_REVIEW_STATE: 1,
    LAST_REVIEW_DURATION: -1
};

var EBGM = 'EBGM';
var EB05 = 'EB05';
var EB95 = 'EB95';
var rrValue = 'RRVALUE';
var rr_value = "RR_VALUE";
var eValue = 'EVALUE';
var e_value = "E_VALUE";
var COUNTS = 'COUNTS';
var RELATIVE_ROR="RELTV_ROR"
var ROR_RELATIVE="ROR_Relative"
var COUNT_VALUE ='countValues';
var positiveRechallenge ='positiveRechallenge';
var PERCENTAGE_INCREASE_FROM_PREVIOUS_PERIOD ='PERCENTAGE_INCREASE_FROM_PREVIOUS_PERIOD';
var TAG = 'tag';
var PREVIOUS_CATEGORY = 'PREVIOUS_CATEGORY';
var ALL_CATEGORY = 'ALL_CATEGORY';
var REVIEW_STATE = 'REVIEW_STATE';
var SIGNAL_REVIEW_STATE= 'SIGNAL_REVIEW_STATE';
var LAST_REVIEW_DURATION = 'LAST_REVIEW_DURATION';
var importantEventsValues = ['IME', 'DME', 'SPECIAL_MONITORING', 'STOP_LIST']
var ALGORITHM_STRING_LIST = ["sdrEvdas","changesEvdas","dmeImeEvdas","sdrPaedEvdas","sdrGeratrEvdas","EVDAS_LISTEDNESS","LISTEDNESS","DME","IME","SPECIAL_MONITORING","STOP_LIST","NEW_EVENT","PREVIOUS_CATEGORY","ALL_CATEGORY","trendType","trendFlag","freqPeriod","cumFreqPeriod","positiveRechallenge"]
var expressionCategory;
var PAGESIZE = 20; // this should match with the API result pageSize so that 'loading more results' shows only when needed


$(function () {
    $("#dispositionId .select2").select2();
    $("#bussJustificationId").find('#justificationAction').addClass("select2").select2();
    $('#signal').select2({
        tags: false,
        placeholder: 'Select Signal',
        allowClear: true,
        maximumInputLength: 250,
        closeOnSelect: true,
        width: "100%",
        ajax: {
            url: fetchSignalsUrl,
            dataType: 'json',
            delay: 250,
            data: function (params) {
                return {
                    searchTerm: params.term, // search term
                    page: params.page
                };
            },
            processResults: function (data, params) {
                params.page = params.page || 1;
                // Map the items to only include the id and name
                var results = data.items.map(function (item) {
                    return {
                        id: item.id,
                        text: item.name // Select2 uses the `text` property to display the option
                    };
                });

                return {
                    results: results,
                    pagination: {
                        more: (params.page * PAGESIZE) < data.total_count
                    }
                };
            },
            cache: false
        },
    });
    $('#medicalConcepts').select2();
    $('#tags').select2({
        tags: true,
        placeholder: 'Select Category',
        allowClear: true,
        width: "100%",
        createTag: function (params) {
            var term = $.trim(params.term);
            if (term != "") {
                return {
                    id: term,
                    text: term
                }
            }
            return null
        }
    });

    $('#subTags').select2({
        tags: true,
        placeholder: 'Select Sub Category',
        allowClear: true,
        width: "100%",
        createTag: function (params) {
            var term = $.trim(params.term);
            if (term != "") {
                return {
                    id: term,
                    text: term
                }
            }
            return null
        }
    });
    signal.business_config_utils.initialize_select_boxes();
    signal.business_config_utils.disable_elements($('.disable-select'));
    signal.business_config_utils.set_first_child_value_to_true();
    signal.business_config_utils.add_rules_row();
    signal.business_config_utils.delete_rules_row();
    signal.business_config_utils.save_rules_output_format();
    signal.business_config_utils.configure_drop_downs();
    signal.business_config_utils.show_format_modal();
    signal.business_config_utils.change_justification_action();
    signal.business_config_utils.submit_business_configuration();
    signal.business_config_utils.process_case_type();
    signal.business_config_utils.attach_to_signal();
    signal.business_config_utils.fetch_tags();
    signal.business_config_utils.openTagModal();
    signal.business_config_utils.populateJustificationsForDisposition($('#dispositionAction').find(':selected').val());

    $(document).on('change', '.expressionAttribute', function () {
        disablePercentValue($(this), true);
        $(this).parents(".business-configuration-row").find(".expressionOp").val("");
        updateOperatorFields($(this));
        updateThresholdFields($(this));
        var value = $(this).val();
        if(importantEventsValues.indexOf(value)!=-1){
            $(this).parents(".business-configuration-row").find('.isProductSpecific').removeClass('hidden')
        }else{
            $(this).parents(".business-configuration-row").find('.isProductSpecific').addClass('hidden')
        }
        $(this).parents('.business-configuration-row').find('.expressionThreshold').val('');
    });

    $('.caseAlertType').trigger('change');

    $(document).on('change', '.expressionCategory', function () {
        disablePercentValue($(this), true);
    });

    $(document).on('keydown.alertable', function(event) {
        if(event.keyCode === 13) {
            if($('#percentModal').is(':visible')) {
                var textSelected = document.querySelector("#percentValue") === document.activeElement;
                var okSelected = document.querySelector('.savePercentValue') === document.activeElement;
                if (textSelected || okSelected)
                    $('.savePercentValue').trigger('click');
                else
                    $('#percentModal').modal('hide');
            }else{
                var formatModal = document.querySelector(".formatModal") === document.activeElement;
                var saveBusinessConfigButton = document.querySelector('#saveBusinessConfigButton') === document.activeElement;
                var cancelButton = document.querySelector('#cancelButton') === document.activeElement;
                var addExpression = document.querySelector('#addExpression') === document.activeElement;
                if(formatModal)
                    $(".formatModal").trigger('click');
                if(saveBusinessConfigButton)
                    $("#saveBusinessConfigButton").trigger('click');
                if(cancelButton)
                    $("#cancelButton").trigger('click');
                if(addExpression)
                    $("#addExpression").trigger('click');

            }
            return false;
        }
    });


});


var signal = signal || {};

signal.business_config_utils = (function () {

    var process_case_type = function () {
        $('.caseAlertType').on('change', function () {
            var value = $('input[type=radio][name=isSingleCaseAlert]:checked').val();
            processAlertType(value);
            toggleFormatContainer(value);
        });
    };

    var attach_to_signal = function () {
        $('#dispositionAction').on('change', function () {
            var isValidationConfirmed = $(this).find(':selected').data('isValidationConfirmed');
            if (isValidationConfirmed) {
                var $attachSignalContainer = $('#attach-signal-container');
                $attachSignalContainer.show();
            } else {
                $('#signal').val('').trigger('change');
                $('#attach-signal-container').hide();
            }
            var dispositionId = ($(this).find(":selected").val());
            populateJustificationsForDisposition(dispositionId);
        });

    };

    var set_first_child_value_to_true = function () {
        $('#row-container').find('.business-configuration-row').first().attr('data-is-first-child', 'true');
    };

    var populateJustificationsForDisposition = function (dispositionId) {
        $('#justificationAction').empty();
        $.ajax({
            type: "GET",
            url: '/signal/justification/fetchJustificationsForDispositionForBR/' + dispositionId,
            async: false,
            dataType: 'json',
        })
            .done(function (result) {
                var listContent = "<option value>-Select-</option>";
                $.each(result, function (index, element) {
                    listContent += '<option value="' + element.id + '">' + element.text + '</option>';
                });
                $('#justificationAction').append(listContent);
            });
    };

    var configure_dependent_select_boxes = function () {
        $('.dependent-select').off().on('change', function () {
            configureDropDownLists($(this))
        });
    };

    var initialize_format_modal = function (countsList) {
        var formatValue = $('#formatInfo').val();
        var fontColor = 'black';
        var cellColor = 'white';
        if (formatValue) {
            var formatObj = JSON.parse(formatValue);
            fontColor = formatObj.text.color;
            cellColor = formatObj.cell.color;
            if (formatObj.text.bold) {
                $('#bold').parent('label').addClass('active');
            }
            if (formatObj.italic) {
                $('#italic').parent('label').addClass('active');
            }
            if (formatObj.underline) {
                $('#underline').parent('label').addClass('active');
            }
            displayColorBox(fontColor, cellColor);
        }
        set_color_inputs(fontColor, cellColor);
        var productArray = countsList;//["a", "b", "c", "d"];
        var optionsAsString = "";
        if (typeof productArray !== "undefined") {
            for (var i = 0; i < productArray.length; i++) {
                optionsAsString += "<option value='" + productArray[i].key + "'>" + productArray[i].value + "</option>";
            }
        }
        $('select[name="textTc"]').append(optionsAsString);
        $('select[name="cellTc"]').append(optionsAsString);
        if (formatValue) {
            $('#textTargetCol').val(formatObj.text.tc);
            $('#cellTargetCol').val(formatObj.cell.tc);
        }
        $('#textTargetCol').select2();
        $('#cellTargetCol').select2();
    };

    var set_color_inputs = function(fontColor, cellColor){
        $("#fontColor").spectrum({
            showPaletteOnly: true,
            togglePaletteOnly: true,
            togglePaletteMoreText: 'more',
            togglePaletteLessText: 'less',
            color: fontColor,
            palette: [
                ["#000", "#444", "#666", "#999", "#ccc", "#eee", "#f3f3f3", "#fff"],
                ["#f00", "#f90", "#ff0", "#0f0", "#0ff", "#00f", "#90f", "#f0f"],
                ["#f4cccc", "#fce5cd", "#fff2cc", "#d9ead3", "#d0e0e3", "#cfe2f3", "#d9d2e9", "#ead1dc"],
                ["#ea9999", "#f9cb9c", "#ffe599", "#b6d7a8", "#a2c4c9", "#9fc5e8", "#b4a7d6", "#d5a6bd"],
                ["#e06666", "#f6b26b", "#ffd966", "#93c47d", "#76a5af", "#6fa8dc", "#8e7cc3", "#c27ba0"],
                ["#c00", "#e69138", "#f1c232", "#6aa84f", "#45818e", "#3d85c6", "#674ea7", "#a64d79"],
                ["#900", "#b45f06", "#bf9000", "#38761d", "#134f5c", "#0b5394", "#351c75", "#741b47"],
                ["#600", "#783f04", "#7f6000", "#274e13", "#0c343d", "#073763", "#20124d", "#4c1130"]
            ]
        });
        $("#cellColor").spectrum({
            showPaletteOnly: true,
            togglePaletteOnly: true,
            togglePaletteMoreText: 'more',
            togglePaletteLessText: 'less',
            color: cellColor,
            palette: [
                ["#000", "#444", "#666", "#999", "#ccc", "#eee", "#f3f3f3", "#fff"],
                ["#f00", "#f90", "#ff0", "#0f0", "#0ff", "#00f", "#90f", "#f0f"],
                ["#f4cccc", "#fce5cd", "#fff2cc", "#d9ead3", "#d0e0e3", "#cfe2f3", "#d9d2e9", "#ead1dc"],
                ["#ea9999", "#f9cb9c", "#ffe599", "#b6d7a8", "#a2c4c9", "#9fc5e8", "#b4a7d6", "#d5a6bd"],
                ["#e06666", "#f6b26b", "#ffd966", "#93c47d", "#76a5af", "#6fa8dc", "#8e7cc3", "#c27ba0"],
                ["#c00", "#e69138", "#f1c232", "#6aa84f", "#45818e", "#3d85c6", "#674ea7", "#a64d79"],
                ["#900", "#b45f06", "#bf9000", "#38761d", "#134f5c", "#0b5394", "#351c75", "#741b47"],
                ["#600", "#783f04", "#7f6000", "#274e13", "#0c343d", "#073763", "#20124d", "#4c1130"]
            ]
        });
    };

    var change_justification_action = function () {
        $(document).on('change', '#justificationAction', function () {
            var value = $(this).val();
            if (value != "") {
                var text = justificationObj.filter(function (arr) {
                    return arr.id == value
                })[0].text;
                $('#justificationText').val(text);
            } else {
                $('#justificationText').val('');
            }
        })
    };

    var submit_business_configuration = function () {
        $('#businessConfigurationForm').on('submit', function (e) {
            var result = true;
            printAll();
            console.log("expression", $('.expression').length <= 0);
            var resultObj = validateRules();
            result = resultObj.status;
            if (!result) {
                showAlertMessage(resultObj.msg);
                $queryJSON.val('');
                return false;
            } else {
                $queryJSON.val(printAll());
            }
            result = validateActions();
            return result;
        });
    };

    var initialize_select_boxes = function () {
        $.ajax({
            url: $('#fetchSelectBoxValuesUrl').val() + '/' + $('#businessConfigurationId').val(),
            async: false
        }).done(function (payload) {
            selectData = payload.categories;
            countsList = payload.formatOptions;
            textFields = payload.textFields;
            booleanFields = payload.booleanFields;
            signal.business_config_utils.initialize_format_modal(countsList);
        });
    };

    var add_rules_row = function () {
        $('#addRow').on('click', function () {
            var $rowCode = $('#row-code-container').find('.business-configuration-row').clone();
            var $diffCode = $('#diff-code-container').find('.diff-button-container').clone();
            var $rowContainer = $('#row-container');
            if ($('#row-container').find('.business-configuration-row').length > 0) {
                $rowContainer.append($diffCode);
            }
            $rowContainer.append($rowCode);
        });
    };

    var delete_rules_row = function () {
        $(document).on('click', '.delete-business-configuration-row', function () {
            var $row = $(this).parents('.business-configuration-row');
            var isFirstRow = $row.data('isFirstChild');
            var $diffButton = $row.prev('.diff-button-container');
            if (isFirstRow == true) {
                $diffButton = $row.next('.diff-button-container');
            }
            $row.remove();
            $diffButton.remove();
            signal.business_config_utils.set_first_child_value_to_true();
        });
    };

    var save_rules_output_format = function () {
        $('#saveFormat').on('click', function () {
            var result = true;
            var msg = "";
            var fontColor = $('#fontColor').spectrum("get").toHexString();
            var bold = $('#bold').prop("checked");
            var italic = $('#italic').prop("checked");
            var underline = $('#underline').prop("checked");
            var texttc = $('#textTargetCol').select2("val");
            var cellColor = $('#cellColor').spectrum("get").toHexString();
            var celltc = $('#cellTargetCol').select2("val");
            if ((texttc == '' || texttc == null) && (celltc == '' || celltc == null)) {
                result = false;
                msg = "Please select target columns for formatting."
            }
            if (result) {
                var formatString = {
                    "text": {
                        "bold": bold,
                        "italic": italic,
                        "underline": underline,
                        "color": fontColor,
                        "tc": texttc
                    }, "cell": {"color": cellColor, "tc": celltc}
                };
                $('#formatInfo').val(JSON.stringify(formatString));
                displayColorBox(fontColor, cellColor);
                $('#modalFormat').modal('hide');
            } else {
                var errorHtml = '<div class="alert alert-danger alert-dismissable">' +
                    '<a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>' +
                    msg + '</div>';
                $('#modal-alert-container').html(errorHtml);
                $('#modal-alert-container').show();
            }
        });
        $("#clearFormat").on('click', function () {
            $('#modalFormat input[type=checkbox]').parent().removeClass('active');
            set_color_inputs('black', 'white');
            $("#textTargetCol, #cellTargetCol").val("").trigger('change');
            $('#formatInfo').val('');
            $('#modalFormat').modal('hide');
            $('#showFontColor, #showCellColor').hide();
        });
    };

    var configure_drop_downs = function () {
        $(document).on('change', '.dependent-select', function () {
            configureDropDownLists($(this))
            $(this).parents(".business-configuration-row").find(".expressionOp").val("");
        });
    };

    var show_format_modal = function () {
        $('.formatModal').on('click', function () {
            $('#modal-alert-container').html('');
            $('#modalFormat').modal('show')
        });
    };

    var disable_elements = function ($container) {
        $container.attr('disabled', 'disabled');
    };

    var fetch_tags = function() {
        var ruleId = $("input[name='rule-id']").val();
        if(ruleId!='') {
            var tagsObj = [];
            $.ajax({
                url: "/signal/businessConfiguration/fetchRuleTags",
                data: {ruleId: ruleId},
            }).done(function (payload) {
                var map = JSON.parse(payload.tags);
                if (map) {
                    data = JSON.parse(map.tags);
                }
                $.each(data, function (key, value) {
                    var flag = 0;
                    if(value.subTags?.length > 0 && value.subTags != null){
                        flag = 1;
                    }
                    if(flag == 1) {
                        var row = {
                            "tagText": value.tagText,
                            "subTagText": value.subTags?.join(';'),
                            "priority": value.priority,
                            'privateUser': '',
                            'tagType': value.alert? '(A)':''
                        };
                        tagsObj.push(row);
                    }
                    if (flag == 0) {
                        var row = {
                            "tagText": value.tagText,
                            "subTagText": null,
                            "priority": value.priority,
                            'privateUser': '',
                            'tagType': value.alert? '(A)':''
                        };
                        tagsObj.push(row);
                    }
                });
                var tagsHtml = signal.alerts_utils.get_tags_element(tagsObj);
                $("#tags-business").html(tagsHtml);
                var tagsJson = JSON.stringify(data);
                $("textarea[name='allTags']").val(tagsJson);
                businessTagEllipsis();
                webUiPopInit();
                webUiPopInitForCategories();
            });
        }
        else {
            var tagsHtml = signal.alerts_utils.get_tags_element(null);
            $("#tags-business").html(tagsHtml);
        }
    }

    var openTagModal = function (module, domain) {
        $(document).on('click', '.editAlertTags', function (evt) {
            $.ajax({
                url: fetchCommonTagsUrl,
            })
                .done(function (result) {
                    var tags = [];
                    result.commonTagList.forEach(function(map){
                        tags.push({
                            id: map.id,
                            text: map.text,
                            parentId: map.parentId,
                            display: map.display
                        });
                    });
                    fillAlertTagsBusiness(tags);
                    setSelectWidth($(".dynamicSelectFieldSubCategory"));
                    $('#commonTagModal').modal('show');
                });
        });
    };
    function fillAlertTagsBusiness(tags) {
        var disabledSubCatList = [];
        categoryModalTable = $("#categoryModalTable").DataTable({
            "createdRow": function ( row, data, index ) {
                var tagsEnabled = nonConfiguredEnabled != "true" ? false : true;
                var categories = [];
                tags.forEach(function (map) {
                    if(map.parentId ==0 || map.parentId == null) {
                        categories.push({id: map.id, text: map.text, disabled: map.display == 0 ? true : false});
                    }
                });
                $('select', row).eq(0).select2({
                    tags: tagsEnabled,
                    data: categories,
                    placeholder: "Select Category"
                }).on('change', function(obj) {

                    var subtagsEnabled = nonConfiguredEnabled != "true" ? false : true;
                    var subcategories = [];
                    tags.forEach(function (map) {
                        if(map.parentId == obj.currentTarget.value) {
                            var isDisabledSubCat = map.display == 0 ? true : false
                            subcategories.push({id: map.id, text: map.text , disabled:isDisabledSubCat });
                            if(isDisabledSubCat)
                                disabledSubCatList.push(map.id)
                        }
                    });

                    if(data.action == 1) {
                        $('select', row).eq(1).empty();
                    }
                    // if no subcategory configure then can not add on fly subcategory
                    if(subcategories.length <= 0){
                        subtagsEnabled = false;
                    }else{
                        subtagsEnabled = (subtagsEnabled && true);
                    }

                    $('select', row).eq(1).select2({
                        tags: subtagsEnabled,
                        data: subcategories,
                        placeholder: "Select Sub Category"
                    });
                }).trigger('change');

                $('select', row).eq(1).on('change', function (e) {
                    setSelectWidth($(this));
                });

                if(data.action != 1) {
                    $('input', row).closest('tr').find(":input").attr('disabled', true);
                    if(data.subTags.length <= 0)
                        $('input', row).closest('tr').find(":input[class=select2-search__field]").attr("placeholder", "");
                }
                $(".edit-row", row).off().on('click', function(){
                    var isDisabled = $(this).closest('tr').find(":input:eq(1)").attr('disabled');
                    $(this).closest('tr').find(":input:eq(1)").attr('disabled', !isDisabled);
                    $(this).closest('tr').find(":input[type=checkbox]:eq(0)").attr('disabled', !isDisabled);
                    if(isDisabled && data.subTags.length <= 0)
                        $(this).closest('tr').find(":input[class=select2-search__field]").attr("placeholder", "Select Sub Category").css("width", "100%");
                    else if(isDisabled && data.subTags.length > 0)
                        $(this).closest('tr').find(":input[class=select2-search__field]").attr("placeholder", "");
                    else if(!isDisabled && data.subTags.length <= 0)
                        $(this).closest('tr').find(":input[class=select2-search__field]").attr("placeholder", "");
                });

                $(".delete-row", row).off().on('click', function(){
                    categoryModalTable
                        .row( $(this).parents('tr') )
                        .remove()
                        .draw();
                });

            },
            "data":data,
            searching: true,
            "bLengthChange": true,
            "paging":   false,
            "ordering": false,
            "searching": false,
            "info": false,
            "bDestroy": true,
            stateSave: true,
            autoWidth: false,
            "scrollY": "350px",
            "scrollCollapse": true,
            "aoColumns": [
                {
                    "mData": "category",
                    "width": "25%",
                    'className': 'dt-center',
                    "mRender": function (data, type, row) {
                        var html = '<select name="selectField" id="dynamicSelectFieldCategory" class="dynamicSelectFieldCategory form-control">';
                        if(row.tagText) {
                            html += '<option selected="selected" value="' +row.tagText+ '">' + encodeToHTML(row.tagText)+ '</option>';
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
                        row.subTags.forEach(function (obj) {
                            html += '<option selected="selected" value="'+obj+'">'+encodeToHTML(obj)+'</option>';
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
                        return '<input type="checkbox" class="alert-check-box" ' + checked +' data-id=' + row.alert + ' />';

                    }
                },
                {
                    "mData": "private",
                    "width": "12%",
                    "mRender": function (data, type, row) {
                        return '<input type="checkbox" disabled = "true" class="private-check-box" />';
                    },
                    'className': 'dt-center dataTableColumnCenter text-center'
                },
                {
                    "mData": "action",
                    "width": "13%",
                    "className": "td-action",
                    "mRender": function (data, type, row) {
                        if(data === 1){
                            return '<i class="fa fa-trash-o delete-row tag-action"></i>';
                        }
                        else {

                            return '  <i class="fa fa-pencil edit-row tag-action" ></i>' +
                                '  <i class="fa fa-trash-o delete-row tag-action"></i>';
                        }
                    }
                }
            ],
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }],
            initComplete: function(settings, json){
                disabledSubCatList.forEach(function (value, index1) {
                    $('.dynamicSelectFieldSubCategory').find('option[value="' + value + '"]').prop("disabled", true);
                });
                categoryMaximumSize(nonConfiguredEnabled);
            }
        });
        $( "#categoryModalTable" ).sortable({
            items: 'tr:not(:first)',
            opacity: 0.6,
            scrollSpeed: 40,
            cancel: "thead",
            containment : "parent"
        });
        $("#add-row").on('click', function () {
            var lastRow = categoryModalTable.row(':last').data();
            var tagsEnabled = nonConfiguredEnabled != "true" ? false : true;
            var lastRowCategory = $('#categoryModalTableBody tr:last').find("td select.dynamicSelectFieldCategory").val();
            if(lastRowCategory != "Select Category") {
                categoryModalTable.row.add({
                    "tagText": "Select Category",
                    "subTags": [],
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
        });

        $(".addTags").off().on('click', function () {
            var tags = []
            var count = 1;
            data = [];
            $('#categoryModalTableBody tr').each(function(){
                var th = $(this);
                var categoryText = $(th).find("td select.dynamicSelectFieldCategory option:selected").text();
                var alertSelected = $(th).find("td input.alert-check-box").is(":checked");
                if(categoryText!= 'Select Category') {
                    var currentRowData = {'tagText': categoryText, 'subTags': [], 'priority': count, 'alert': alertSelected};
                    var flag = 0
                    var subcategories = [];
                    $(th).find("td select.dynamicSelectFieldSubCategory option:selected").each(function(){
                        subcategories.push($(this).text());
                        currentRowData.subTags.push($(this).text())
                        flag = 1;
                    });
                    if(flag == 1) {
                        var currentRow = {
                            'tagText': categoryText,
                            'subTagText': subcategories?.join(';'),
                            'privateUser': '',
                            'priority': count,
                            'tagType': alertSelected? '(A)':''
                        };
                        if (currentRow.tagText != 'Select Category') {
                            tags.push(currentRow);
                        }
                    }
                    if (flag == 0) {
                        var currentRow = {
                            'tagText': categoryText,
                            'subTagText': null,
                            'privateUser': '',
                            'priority': count,
                            'tagType': alertSelected?'(A)':''
                        };
                        if (currentRow.tagText != 'Select Category') {
                            tags.push(currentRow);
                        }
                    }
                    data.push(currentRowData);
                    count = count + 1;
                }
            });
            if(tags[0].tagText != "") {
                var tagsHtml = signal.alerts_utils.get_tags_element(tags);
                $("#tags-business").html(tagsHtml);
                var tagsJson = JSON.stringify(data);
                $("textarea[name='allTags']").val(tagsJson);
            }
            else{
                var tagsHtml = signal.alerts_utils.get_tags_element([]);
                $("#tags-business").html(tagsHtml);
                $("textarea[name='allTags']").val(null);
                data = []
            }
            businessTagEllipsis();
            webUiPopInit();
            webUiPopInitForCategories();
        });
        return categoryModalTable
    };


    return {
        set_first_child_value_to_true: set_first_child_value_to_true,
        configure_dependent_select_boxes: configure_dependent_select_boxes,
        initialize_format_modal: initialize_format_modal,
        change_justification_action: change_justification_action,
        submit_business_configuration: submit_business_configuration,
        initialize_select_boxes: initialize_select_boxes,
        add_rules_row: add_rules_row,
        delete_rules_row: delete_rules_row,
        save_rules_output_format: save_rules_output_format,
        configure_drop_downs: configure_drop_downs,
        show_format_modal: show_format_modal,
        disable_elements: disable_elements,
        process_case_type: process_case_type,
        attach_to_signal: attach_to_signal,
        fetch_tags: fetch_tags,
        openTagModal:openTagModal,
        populateJustificationsForDisposition:populateJustificationsForDisposition
    }
})();

function configureDropDownLists(currentObj) {
    var nextSelect = currentObj.data('nextSelect');
    var $row = currentObj.parents('.business-configuration-row');
    var ddl2 = $row.find('.' + nextSelect);
    var selectBoxValue = currentObj.val();
     if(["COUNTS","ALGORITHM"].includes(selectBoxValue)){
         $row.find('.threshold-combo').removeClass('hide');
         $row.find('.expressionThreshold-combo').attr('disabled',false).html('').select2({placeholder: "Select Threshold", multiple: false});
         $row.find('.threshold').hide();
         $row.find('.checkBoxDiv').addClass('hide');
     } else if (selectBoxValue == "SIGNAL_REVIEW_STATE") {
         $row.find('.checkBoxDiv').removeClass('hide');
         $row.find('.threshold-combo').addClass('hide');
         $row.find('.expressionThreshold-combo').attr('disabled', true)
         $row.find('.threshold').show();
         $row.find('.checkBoxDiv #splitSignalToPt, .checkBoxDiv #assClosedSignal, .checkBoxDiv #assMultSignal').prop('checked',false)
     } else {
         $row.find('.threshold-combo').addClass('hide');
         $row.find('.expressionThreshold-combo').attr('disabled',true)
         $row.find('.threshold').show();
         $row.find('.checkBoxDiv').addClass('hide');
    }
    $row.find('#attribute').select2();
    generatedAttributeList(ddl2, selectBoxValue);
    $row.find('.threshold-combo .select2').find('option').not(':first').remove();
    $row.find('.disable-select').attr("disabled", "disabled");
    enableNextSelectBox(currentObj);
}

function enableNextSelectBox($selector) {
    var nextSelect;
    var $row = $selector.parents('.business-configuration-row');
    var ddl2;
    var value = $selector.val();
    var isPositiveNumber = (enableSelectData[value] >= 0);
    if (isPositiveNumber) {
        $row.find(".txt-option").hide();
        $row.find(".empty-option").hide();
        $row.find(".numeric-option").hide();
        for (var i = 1; i <= enableSelectData[value]; i++) {
            nextSelect = $selector.data('nextSelect');
            ddl2 = $row.find('.' + nextSelect);
            ddl2.removeAttr("disabled");
            $selector = ddl2;
        }
    } else {
        $selector = $row.find('.last-element');
        $selector.removeAttr("disabled");
        $row.find(".txt-option").hide();
        $row.find(".empty-option").hide();
        $row.find(".numeric-option").show();
        for (var i = Math.abs(enableSelectData[value]); i >= 1; i--) {
            nextSelect = $selector.data('prevSelect');
            ddl2 = $row.find('.' + nextSelect);
            ddl2.removeAttr("disabled");
            $selector = ddl2;
        }
        disablePercentValue($selector, true);
    }
}

function validateActions() {
    var fontAction = $('#formatInfo').val();
    var disposition = $('#dispositionAction').val();
    var justification = $('#justificationAction').val();
    var tagAction = $("textarea[name='allTags']").val();
    var justificationText = $('#justificationText').val();
    var isValidationConfirmed = $('#dispositionAction').find(':selected').data('isValidationConfirmed');
    var isSignalEmpty = ($('#signal').val() == '' || $('#signal').val() == null);
    var signalName = $('#signal').val();
    var isValidAction = true;
    var dispositionFlag = true;
    var tagFlag = true;
    var formatFlag = true;
    if(disposition === ""){
        dispositionFlag = false;
    }
    if(fontAction === ""){
        formatFlag = false;
    }
    if (tagAction == null || tagAction == "" || tagAction == "[]") {
        tagFlag = false;
    }
    if (dispositionFlag || formatFlag  || tagFlag) {
        if (dispositionFlag) {
            if (justificationText == "") {
                showAlertMessage("Required fields disposition and Justification");
                isValidAction = false;
            }
            if (isValidationConfirmed && !$('#queryJSON').val().includes("SIGNAL_REVIEW_STATE")) {
                if (isSignalEmpty) {
                    isValidAction = false;
                    showAlertMessage("Please select signal");
                } else if (signalName) {
                    var specialCharacters = '<>!|\/\\#';
                    var specialCharacterArray = []
                    for (var char of signalName) {
                        if (specialCharacters.indexOf(char) > -1) {
                            specialCharacterArray.push(char)
                        }
                    }
                    if (specialCharacterArray.length > 0) {
                        var uniqueSpecialCharacterArray = new Set(specialCharacterArray)
                        var msg = "Special characters " + escapeHTML(Array.from(uniqueSpecialCharacterArray).join(', ')) + " are Not Allowed in Signal Name";
                        showAlertMessage(msg);
                        isValidAction = false;
                    }
                }
            }
        }
    }else{
        showAlertMessage("Fill in at least one action");
        isValidAction = false
    }
    return isValidAction
}

function showAlertMessage(msg) {
    $('#alertMessage').html('');
    $('#alertMessage').append(msg);
    $('#alertMessage').show();
}

function displayColorBox(fontColor, cellColor) {
        $('#inputFontColor').val(fontColor);
        $('#showFontColor').show();
        $('#inputCellColor').val(cellColor);
        $('#showCellColor').show();
}

function validateRules() {
    var result = true;
    var $category;
    var $attribute;
    var $operator;
    var $threshold;
    var $thresholdSelect
    var isCategoryDisabled;
    var isAttributeDisabled;
    var isOperatorDisabled;
    var isThresholdDisabled;
    var resultObj = {status: true, msg: ''};
    if($('#ruleName').val() == ''){
        resultObj.status = false;
        resultObj.msg = 'Rule name is required';
    }
    if ($('.expression').length <= 0) {
        resultObj.status = false;
        resultObj.msg = 'Please select at least one rule.';
    } else {
        $('.expression').each(function () {
            if (result) {
                $category = $(this).find('.expressionCategory');
                $attribute = $(this).find('.expressionAttribute');
                $operator = $(this).find('.expressionOp');
                $threshold = $(this).find('.expressionThreshold');
                $thresholdSelect  = $(this).find('.expressionThreshold-combo');
                var isCategoryDisabled = $category.is(':disabled');
                var isAttributeDisabled = $attribute.is(':disabled');
                var isOperatorDisabled = $operator.is(':disabled');
                var isThresholdDisabled = $threshold.is(':disabled');
                var isThresholdSelectDisabled = $thresholdSelect.is(':disabled');
                if (!isCategoryDisabled) {
                    result = ($category.val() != '')
                }
                if (!isAttributeDisabled) {
                    result = result && ($attribute.val() != '')
                }
                if (!isOperatorDisabled) {
                    result = result && ($operator.val() != '')
                }
                if (!isThresholdDisabled || !isThresholdSelectDisabled) {
                    result = result && (($threshold.val() != '') || $thresholdSelect.val() !='')
                }
            }
        });
        if (!result) {
            resultObj.status = false;
            resultObj.msg = 'Invalid Rule';
        }
    }
    return resultObj;
}

function processAlertType(isSingleCaseAlertType) {
    $('#builderAll').empty();
    var $toAddContainer = $('#toAddContainer');
    var optionSelectCategory = new Option("Select Category","",true,true)
    var newOptionAlgorithm = new Option("Algorithm","ALGORITHM",false,false)
    var newOptionCounts = new Option("Counts","COUNTS",false,false)
    var newOptionQuery = new Option("Query Criteria","QUERY_CRITERIA",false,false)
    var newOptionLastReviewDisposition = new Option("Last Review Disposition","REVIEW_STATE",false,false)
    var newOptionLastSignalReviewDisposition = new Option("Last Signal Review Disposition","SIGNAL_REVIEW_STATE",false,false)
    var newOptionLastReviewDuration = new Option("Last Review Duration","LAST_REVIEW_DURATION",false,false)
    if (isSingleCaseAlertType === 'true') {
        $toAddContainer.find('.expressionCategory').empty()
        $toAddContainer.find('.expressionCategory').append(optionSelectCategory)
        $toAddContainer.find('.expressionCategory').append(newOptionQuery)
        $toAddContainer.find('.expressionCategory').append(newOptionLastReviewDisposition)
        $toAddContainer.find('.expressionCategory').append(newOptionLastReviewDuration)
    } else {
        $toAddContainer.find('.expressionCategory').empty()
        $toAddContainer.find('.expressionCategory').append(optionSelectCategory)
        $toAddContainer.find('.expressionCategory').append(newOptionAlgorithm)
        $toAddContainer.find('.expressionCategory').append(newOptionCounts)
        $toAddContainer.find('.expressionCategory').append(newOptionLastReviewDisposition)
        $toAddContainer.find('.expressionCategory').append(newOptionLastReviewDuration)
        $toAddContainer.find('.expressionCategory').append(newOptionLastSignalReviewDisposition)
    }
}

function toggleFormatContainer(isSingleCaseAlertType) {
    if (isSingleCaseAlertType === 'true') {
        $('#rule-format-container').hide();
    } else {
        $('#rule-format-container').show();
    }
}

$(document).on('change', '.expressionOp', function () {
    updateThresholdField($(this));
    disablePercentValue($(this), true);
    var category = $(this).parents('.business-configuration-row').find('.expressionCategory').val();
    var attribute = $(this).parents('.business-configuration-row').find('.expressionAttribute').val();
    var $thresholdValue = $(this).parents('.business-configuration-row').find('.expressionThreshold-combo');
    if($(this)[0].value == "Is Empty" || $(this)[0].value == "Is Not Empty" || $(this)[0].value == ""){
        $thresholdValue.html('');
    }else {
        $thresholdValue.html('');
        if (attribute !== ALL_CATEGORY && attribute !== PREVIOUS_CATEGORY) {
            // empty option not appended in case of multi-select
            $thresholdValue.append(new Option());
        }
        if (category == COUNTS) {
            countThresholdValues(attribute, $thresholdValue);
        } else {
            thresholdValues(attribute, $thresholdValue);
        }
        thresholdSelect2(attribute, $thresholdValue);
        if(category=='ALGORITHM' || category=='COUNTS'){
            var enableTagCreation = false;
            var multiple = false;
            if (attribute == ALL_CATEGORY || attribute == PREVIOUS_CATEGORY) {
                enableTagCreation = nonConfiguredEnabled != "true" ? false : true;
                multiple = true;
            } else {
                enableTagCreation = true;
            }
            $thresholdValue.select2({
                data_attribute: attribute,
                data_category: category,
                createTag:function(params){
                    var currentTagAllowed;
                    var term = $.trim(params.term);
                    if(ALGORITHM_STRING_LIST.includes(this.options.options.data_attribute)){
                        // any tag is allowed for string
                        currentTagAllowed = true;
                    }
                    else if(this.options.options.data_category==='COUNTS'){
                        // for Integer columns regex added
                        currentTagAllowed = /^\d+$/.test(term);
                    }
                    else{
                        // for Integer columns regex added
                        currentTagAllowed = /^\d+(\.\d+)?$/.test(term);
                    }
                    if(currentTagAllowed){
                        return {
                            id: term,
                            text: term
                        }
                    }
                    return null;
                },
                placeholder: "Select Threshold",
                multiple: multiple,
                tags: enableTagCreation,
                allowClear: true,
                width: "100%",
            });
        }
        $thresholdValue.trigger('change');
    }
    if(["Equal To","Greater Than","Greater Than Equal To","Less Than","Less Than Equal To", "Not Equal To"].includes($(this)[0].value)
            &&!textFields.includes(attribute) && !booleanFields.includes(attribute)){

        if(category != REVIEW_STATE && category != LAST_REVIEW_DURATION && category != SIGNAL_REVIEW_STATE && attribute!="trendType" && attribute!=="trendFlag" && attribute!=="positiveRechallenge") {
            $(this).parents('.business-configuration-row').find(".percentValue").off('click');
            $(this).parents('.business-configuration-row').find(".percentValue").css('cursor', "pointer");
            $(this).parents('.business-configuration-row').find(".percentValue").css('color', "#4c5667");
        }
    }
});

function updateThresholdFieldsForAll() {
    $(".expressionOp").each(function( index ) {
        updateThresholdField($(this));
    });
}

function updateThresholdField(container) {
    var operator = container[0].value;
    if (operator == "Is Empty" || operator == "Is Not Empty" || operator == "") {
        container.parents('.business-configuration-row').find(".expressionThreshold-combo").prop("disabled" , true);
        container.parents('.business-configuration-row').find(".expressionThreshold-combo").html('');
        container.parents('.business-configuration-row').find(".expressionThreshold").val("");
        container.parents('.business-configuration-row').find(".expressionThreshold").attr("disabled" , true);
    }
    else {
        container.parents('.business-configuration-row').find(".expressionThreshold-combo").prop("disabled" , false);
        container.parents('.business-configuration-row').find(".expressionThreshold").attr("disabled" , false);
    }
}

function disablePercentValue(container, reset){
    container.parents('.business-configuration-row').find(".percentValue").on('click', false);
    container.parents('.business-configuration-row').find(".percentValue").css('cursor', "not-allowed");
    container.parents('.business-configuration-row').find(".percentValue").css('color', "#C8C8C8");
    if(reset) {
        container.parents('.business-configuration-row').find('.percentValue').attr("data-percent-value", "");
        container.parents('.business-configuration-row').find('.percentValue').attr("title", "");
    }
}

function updateOperatorFields(container) {
    var field = container[0].value;
    var operatorFieldRow = container.parents('.business-configuration-row');
    disablePercentValue($(container), false);
    if (textFields.includes(field)) {
        operatorFieldRow.find('.numeric-option').hide();
        operatorFieldRow.find(".txt-option").show();
        operatorFieldRow.find(".empty-option").show();
        operatorFieldRow.find(".not-equal-option").hide();
        operatorFieldRow.find('.numeric-option').prop('disabled', true);
        operatorFieldRow.find(".txt-option").prop('disabled', false);
        operatorFieldRow.find(".empty-option").prop('disabled', false);
        operatorFieldRow.find(".not-equal-option").prop('disabled', true);
        if(["PREVIOUS_CATEGORY","ALL_CATEGORY"].includes(field)){
            operatorFieldRow.find(".not-equal-option").show();
            operatorFieldRow.find(".not-equal-option").prop('disabled', false);
        }
        if([BUSINESS_CONFIG_ATTRIBUTES.DME_AGG_ALERT, BUSINESS_CONFIG_ATTRIBUTES.IME_AGG_ALERT,
            BUSINESS_CONFIG_ATTRIBUTES.SPECIAL_MONITORING, BUSINESS_CONFIG_ATTRIBUTES.STOP_LIST].includes(field)){
            operatorFieldRow.find(".not-equal-option").show();
            operatorFieldRow.find(".empty-option").hide();
            operatorFieldRow.find(".txt-option").hide();
            operatorFieldRow.find(".not-equal-option").prop('disabled', false);
            operatorFieldRow.find(".empty-option").prop('disabled', true);
            operatorFieldRow.find(".txt-option").prop('disabled', true);
        }
    } else if (booleanFields.includes(field)) {
        operatorFieldRow.find('.numeric-option').hide();
        operatorFieldRow.find(".txt-option").hide();
        operatorFieldRow.find(".empty-option").hide();
        operatorFieldRow.find(".not-equal-option").hide();
        operatorFieldRow.find('.numeric-option').prop('disabled', true);
        operatorFieldRow.find(".txt-option").prop('disabled', true);
        operatorFieldRow.find(".empty-option").prop('disabled', true);
        operatorFieldRow.find(".not-equal-option").prop('disabled', true);
    }
    else if ('trendType' == field || field === "trendFlag") {
        operatorFieldRow.find(".empty-option").show();
        operatorFieldRow.find(".not-equal-option").show();
        operatorFieldRow.find('.numeric-option').hide();
        operatorFieldRow.find(".txt-option").show();
        operatorFieldRow.find(".empty-option").prop('disabled', false);
        operatorFieldRow.find(".not-equal-option").prop('disabled', false);
        operatorFieldRow.find('.numeric-option').prop('disabled', true);
        operatorFieldRow.find(".txt-option").prop('disabled', false);
    }
    else {
        var category = operatorFieldRow.find(".expressionCategory").val();
        operatorFieldRow.find(".txt-option").hide();
        operatorFieldRow.find(".numeric-option").show();
        operatorFieldRow.find(".not-equal-option").show();
        operatorFieldRow.find(".txt-option").prop('disabled', true);
        operatorFieldRow.find(".numeric-option").prop('disabled', false);
        operatorFieldRow.find(".not-equal-option").prop('disabled', false);
        if(category != REVIEW_STATE && category != LAST_REVIEW_DURATION && category != SIGNAL_REVIEW_STATE) {
            operatorFieldRow.find(".empty-option").show();
            operatorFieldRow.find(".empty-option").prop('disabled', false);
            operatorFieldRow.find(".percentValue").off('click');
            operatorFieldRow.find(".percentValue").css('cursor', "pointer");
            operatorFieldRow.find(".percentValue").css('color', "#4c5667");
        }

    }
    expressionCategory = operatorFieldRow.find(".expressionCategory").val();
    toggleExpressionOpSelect(operatorFieldRow.find(".expressionOp"), field, expressionCategory)
}

function updateThresholdFields(container) {
    disablePercentValue($(container))
    var category = container.parents('.business-configuration-row').find('.expressionCategory').val();
    var attribute = container.parents('.business-configuration-row').find('.expressionAttribute').val()
    var $thresholdValue = container.parents('.business-configuration-row').find('.expressionThreshold-combo');
    $thresholdValue.html('');
    if (attribute !== ALL_CATEGORY && attribute !== PREVIOUS_CATEGORY) {
        // empty option not appended in case of multi-select
        $thresholdValue.append(new Option());
    }
    if (category == COUNTS) {
        countThresholdValues(attribute, $thresholdValue);
    } else {
        thresholdValues(container[0].value, $thresholdValue);
    }
    thresholdSelect2(container[0].value, $thresholdValue);
    $thresholdValue.val('').trigger('change');
}

function editThresholdFields(groupJSON, container) {
    var category = groupJSON.category;
    var $thresholdValue = container.find('.expressionThreshold-combo');
    $thresholdValue.html('');
    if (category == COUNTS) {
        countThresholdValues(groupJSON.attribute, $thresholdValue);
    } else {
        thresholdValues(groupJSON.attribute, $thresholdValue, groupJSON.threshold);
    }

    thresholdSelect2(groupJSON.attribute, $thresholdValue);
    if (!$thresholdValue.val()) {
        if(groupJSON.attribute == PREVIOUS_CATEGORY || groupJSON.attribute == ALL_CATEGORY){
            groupJSON.threshold.split(",").forEach(function(obj){
                console.log(obj);
                $thresholdValue.append(new Option(obj, obj, true, true));
            });
        }else{
            $thresholdValue.append(new Option(groupJSON.threshold, groupJSON.threshold, true, true));
        }
    }
    $thresholdValue.trigger('change');
}

function thresholdValues(field, $thresholdValue, existingValue ="") {
    if (field.substr(0, 4).toUpperCase() == EB05 || field.substr(0, 4).toUpperCase() == EB95) {
        field = EBGM;
    }
    if (field.substr(0, 7).toUpperCase() == rrValue || field.substr(0, 8).toUpperCase() == rr_value) {
        field = rrValue;
    }
    if (field.substr(0, 6).toUpperCase() == eValue || field.substr(0, 7).toUpperCase() == e_value) {
        field = eValue;
    }
    if(field.includes(RELATIVE_ROR)){
        field = ROR_RELATIVE;
    }
    if(field == PREVIOUS_CATEGORY || field == ALL_CATEGORY){
        field = TAG;
    }
    $.each(selectData, function (index, obj) {
        if(field === "trendFlag"){
            if(obj.type === "trendFlag" && obj.text !== existingValue) {
                var optionOne = new Option(obj.text, obj.value, false, false);
                $thresholdValue.append(optionOne);
            }
        } else if(field === "cumFreqPeriod"){
            if(obj.type === "freqPeriod" && obj.text !== existingValue) {
                var option = new Option(obj.text, obj.value, false, false);
                $thresholdValue.append(option);
            }
        } else if(obj.type !== "trendFlag"){
            if (obj.type.substr(0, 3).toUpperCase().match(field.substr(0, 3).toUpperCase()) && obj.text !== existingValue) {
                var option = new Option(obj.text, obj.value, false, false);
                $thresholdValue.append(option);
            }
        }
    });
}

function countThresholdValues(attribute, $thresholdValue) {
    $.each(selectData, function (index, obj) {
        if (obj.type == COUNT_VALUE && attribute != positiveRechallenge) {
            var option = new Option(obj.text, obj.value, false, false);
            $thresholdValue.append(option);
        }
    });
}

function thresholdSelect2(attribute, $thresholdValue) {
    var multiple = false;
    var tags = true;
    if(attribute == PREVIOUS_CATEGORY || attribute == ALL_CATEGORY){
        multiple = true;
        tags = nonConfiguredEnabled != "true" ? false : true;
    }
    $thresholdValue.select2({
        placeholder: "Select Threshold",
        multiple: multiple,
        tags: tags,
        allowClear: true,
        width: "100%",
    });
}

function updateOperatorFieldsForAll() {
    $(".expressionAttribute").each(function( index ) {
        updateOperatorFields($(this));
    });
}

function toggleExpressionOpSelect(expressionOpSelect, value, expressionCategorySelect) {
    if(expressionOpSelect != UNDEFINED && value != UNDEFINED && (expressionCategorySelect != "QUERY_CRITERIA")&&(expressionCategorySelect != "REVIEW_STATE") &&(expressionCategorySelect != "SIGNAL_REVIEW_STATE")){
        if(booleanFields.includes(value)){
            expressionOpSelect.val(BUSINESS_RULE_OPERATOR.EQUAL_TO).prop('disabled', true);
        }else{
            expressionOpSelect.prop('disabled', false);
        }
    }
}
