$(function() {
    var templateQueryChildCount = $("#templateQueryList").attr("data-counter");

    var init = function () {
        if (templateQueryChildCount == 0) { // for create page
            addTemplateQueryLineItem();
            focusFirst();
        } else {
            var tempIdList = [];
            var queryIdList = [];
            for (var i = 0; i < templateQueryChildCount; i++) {
                if ($("#templateQueries\\[" + i + "\\]\\.rptTempId").val()) {
                    tempIdList.push($("#templateQueries\\[" + i + "\\]\\.rptTempId").val())
                }
                if ($("#templateQueries\\[" + i + "\\]\\.queryId").val()) {
                    queryIdList.push($("#templateQueries\\[" + i + "\\]\\.queryId").val())
                }
            }
            bindExistingTemplate(templateQueryChildCount, tempIdList);
            bindExistingTemplateQuery(templateQueryChildCount, queryIdList);

            var selectQueryLevel = $("#templateQueryList").find(".selectQueryLevel").select2();
            if (selectQueryLevel.attr("readonly")) selectQueryLevel.select2("readonly", true);
            $("#templateQueryList").find(".dateRangeEnumClass").select2().on("change", function (e) {
                dataRangeEnumClassOnChange(this);
            });
            $("#templateQueryList").find(".expressionField").select2();
            $("#templateQueryList").find(".expressionOp").select2();
            $("#templateQueryList").find(".expressionValueSelect").select2();
            $("#templateQueryList").find(".expressionValueSelectAuto").select2();

        }

        //bind click event on delete buttons
        $("#templateQueryList").on('click', '.templateQueryDeleteButton', function () {
            //find the parent div
            var parentEl = $(this).parents(".templateQuery-div");
            //find the deleted hidden input
            var delInput = parentEl.find("input[id$=dynamicFormEntryDeleted]");
            //set the deletedFlag to true
            delInput.attr('value', 'true');
            // set relativeDateRangeValue to 1
            parentEl.find('.relativeDateRangeValue')[0].value = 1;
            //hide the div
            parentEl.hide();

            if ($('.templateQuery-div:visible').length == 1) { // only one templateQuery left
                removeSectionCloseButton($('.templateQuery-div:visible'));
            }
        });

        if ($('.templateQuery-div:visible').length == 1) { // only one templateQuery left
            removeSectionCloseButton($('.templateQuery-div:visible'));
        }

        $(".addTemplateQueryLineItemButton").on('click', addTemplateQueryLineItem);
        $(".copyTemplateQueryLineItemButton").on('click', copyTemplateQueryLineItem);
        $(".showHeaderFooterArea").on('click', showHeaderFooterTitle);
        var $enableSpotfire = $("#enableSpotfire");
        if ($enableSpotfire.length > 0) {
            $enableSpotfire.on("change", function () {
                if ($enableSpotfire.is(":checked")) {
                    $(".spotfire_control").attr("disabled", false);
                    $(".spotfire").show();
                } else {
                    $(".spotfire_control").attr("disabled", true);
                    $(".spotfire").hide();
                }
            }).trigger("change");
            $(".spotfire_control").select2();
            if(typeof appType !== 'undefined' && appType === 'Single Case Alert') {
                $spotfireDaterange = $("#spotfireDaterange");
                $spotfireDaterange.on('select2:unselecting', function (e) {
                    if ($spotfireDaterange.parent().find('.select2-selection__choice').length == 1)
                        return false
                });
                if (!$spotfireDaterange.val() || $spotfireDaterange.val().length == 0)
                    $spotfireDaterange.val($spotfireDaterange.find("option:eq(1)").val()).trigger("change");
            }
        }
    };

    var addTemplateQueryLineItem = function () {
        createTemplateQueryLineItem(false);
        $('#templateQueryList').find('.form-control.selectTemplate').last().focus();
    };

    var copyTemplateQueryLineItem = function () {
        createTemplateQueryLineItem(true);
        $('#templateQueryList').find('.form-control.selectTemplate').last().focus();
    };

    var createTemplateQueryLineItem = function (copyValuesFromPreviousSection) {
        var clone = $("#templateQuery_clone").clone();
        var htmlId = 'templateQueries[' + templateQueryChildCount + '].';

        var template = clone.find("select[id$=template]");
        var templateQueryDeleteButton = clone.find("i[id$=deleteButton]");
        var query = clone.find("select[id$=query]");
        var operator = clone.find("select[id$=operator]");
        var queryLevel = clone.find("select[id$=queryLevel]");
        var dateRangeEnum = clone.find("select[id$=dateRangeInformationForTemplateQuery\\.dateRangeEnum]");

        var datePickerFromDiv = clone.find("div[id$=datePickerFromDiv]");
        var datePickerToDiv = clone.find("div[id$=datePickerToDiv]");

        var dateRangeStartAbsolute = clone.find("input[id$=dateRangeInformationForTemplateQuery\\.dateRangeStartAbsolute]");
        var dateRangeEndAbsolute = clone.find("input[id$=dateRangeInformationForTemplateQuery\\.dateRangeEndAbsolute]");

        var relativeDateRangeValue = clone.find("input[id$=dateRangeInformationForTemplateQuery\\.relativeDateRangeValue]");

        var header = clone.find("input[id$=header]");
        var footer = clone.find("textarea[id$=footer]");
        var title = clone.find("input[id$=title]");
        var headerProductSelection = clone.find("input[id$=headerProductSelection]");
        var headerDateRange = clone.find("input[id$=headerDateRange]");
        var blindProtected = clone.find("input[id$=blindProtected]");
        var privacyProtected = clone.find("input[id$=privacyProtected]");
        var templateName = clone.find("input[id$=templateName]");
        var queryName = clone.find("input[id$=queryName]");

        //cloning the hidden fields
        clone.find("input[id$=version]")
            .attr('id', htmlId + 'version')
            .attr('name', htmlId + 'version');
        clone.find("input[id$=id]")
            .attr('id', htmlId + 'id')
            .attr('name', htmlId + 'id');
        clone.find("input[id$=dynamicFormEntryDeleted]")
            .attr('id', htmlId + 'dynamicFormEntryDeleted')
            .attr('name', htmlId + 'dynamicFormEntryDeleted');
        clone.find("input[id$=rptTempId]")
            .attr('id', htmlId + 'rptTempId')
            .attr('name', htmlId + 'rptTempId');

        clone.find("input[id$=queryId]")
            .attr('id', htmlId + 'queryId')
            .attr('name', htmlId + 'queryId');

        clone.find("input[id$=new]")
            .attr('id', htmlId + 'new')
            .attr('name', htmlId + 'new')
            .attr('value', 'true');
        clone.find(".validQueries")
            .attr('id', htmlId + 'validQueries')
            .attr('name', htmlId + 'validQueries');

        template.attr('id', htmlId + 'template')
            .attr('name', htmlId + 'template');

        templateName.attr('id', htmlId + 'templateName')
            .attr('name', htmlId + 'templateName');

        queryName.attr('id', htmlId + 'queryName')
            .attr('name', htmlId + 'queryName');

        query.attr('id', htmlId + 'query')
            .attr('name', htmlId + 'query');

        operator.attr('id', htmlId + 'operator')
            .attr('name', htmlId + 'operator');

        queryLevel.attr('id', htmlId + 'queryLevel')
            .attr('name', htmlId + 'queryLevel');

        templateQueryDeleteButton.attr('id', htmlId + 'deleteButton')
            .attr('name', htmlId + 'deleteButton');

        relativeDateRangeValue.attr('id', htmlId + 'dateRangeInformationForTemplateQuery\.relativeDateRangeValue')
            .attr('name', htmlId + 'dateRangeInformationForTemplateQuery\.relativeDateRangeValue');

        dateRangeEnum.attr('id', htmlId + 'dateRangeInformationForTemplateQuery\.dateRangeEnum')
            .attr('name', htmlId + 'dateRangeInformationForTemplateQuery\.dateRangeEnum');

        datePickerFromDiv.attr('id', htmlId + 'datePickerFromDiv');
        datePickerToDiv.attr('id', htmlId + 'datePickerToDiv');

        dateRangeStartAbsolute.attr('id', htmlId + 'dateRangeInformationForTemplateQuery\.dateRangeStartAbsolute')
            .attr('name', htmlId + 'dateRangeInformationForTemplateQuery\.dateRangeStartAbsolute');

        dateRangeEndAbsolute.attr('id', htmlId + 'dateRangeInformationForTemplateQuery\.dateRangeEndAbsolute')
            .attr('name', htmlId + 'dateRangeInformationForTemplateQuery\.dateRangeEndAbsolute');

        header.attr('id', htmlId + 'header')
            .attr('name', htmlId + 'header');
        footer.attr('id', htmlId + 'footer')
            .attr('name', htmlId + 'footer');
        title.attr('id', htmlId + 'title')
            .attr('name', htmlId + 'title');
        headerProductSelection.attr('id', htmlId + 'headerProductSelection')
            .attr('name', htmlId + 'headerProductSelection');
        headerDateRange.attr('id', htmlId + 'headerDateRange')
            .attr('name', htmlId + 'headerDateRange');
        blindProtected.attr('id', htmlId + 'blindProtected')
            .attr('name', htmlId + 'blindProtected');
        privacyProtected.attr('id', htmlId + 'privacyProtected')
            .attr('name', htmlId + 'privacyProtected');

        clone.attr('id', 'templateQuery' + templateQueryChildCount);
        // Use select2 change event for IE11
        bindTemplateSelect2(template).on("change", function (e) {
            selectTemplateOnChange(this, cioms1Id);
        });

        // Use select2 change event for IE11
        bindQuerySelect2(query).on("change", function (e) {
            selectQueryOnChange('qev',this);
        });

        operator.select2();
        queryLevel.select2({width: null});
        // Use select2 change event for IE11
        dateRangeEnum.select2({width: null}).on("change", function (e) {
            dataRangeEnumClassOnChange(this);
        });

        $("#templateQueryList").append(clone);


        if (copyValuesFromPreviousSection) {
            var lastVisibleTemplateQueryChildIndex = 0;
            for (var i = 0; i < templateQueryChildCount; i++) {
                if ($("#templateQueries\\[" + i + "\\]\\.dynamicFormEntryDeleted").val() == "false")
                    lastVisibleTemplateQueryChildIndex = i;
            }
            var currentIndex = templateQueryChildCount;

            var queryContainer = getQueryWrapperRow(query);
            var expressionValues = getExpressionValues(queryContainer);
            $(expressionValues).on("loadBlankAndCustomSqlFieldsComplete", function () {
                copyBlankAndCustomSqlFields(lastVisibleTemplateQueryChildIndex, currentIndex);
            });

            var templateContainer = getTemplateContainer(template);
            var templateValues = getTemplateValues(templateContainer);
            $(templateValues).on("loadCustomSQLValuesForTemplateComplete", function () {
                copyBlankAndCustomSqlFields(lastVisibleTemplateQueryChildIndex, currentIndex);
            });

            var previousSectionPrefix = "templateQueries\\[" + lastVisibleTemplateQueryChildIndex + "\\]\\.";
            var previousSectionTemplate = $("#" + previousSectionPrefix + "template");
            templateName.val($("#" + previousSectionPrefix + "templateName").val());
            queryName.val($("#" + previousSectionPrefix + "queryName").val());
            if (previousSectionTemplate.val()) {
                var optionTemplate = new Option(previousSectionTemplate.select2('data')[0].text, previousSectionTemplate.val(), true, true);
                template.append(optionTemplate).trigger('change');
            }
            template.one("change",function(){$(templateValues).off("loadCustomSQLValuesForTemplateComplete")});

            var previousSectionQuery = $("#" + previousSectionPrefix + "query");
            if (previousSectionQuery.val()) {
                var optionQuery = new Option(previousSectionQuery.select2('data')[0].text, previousSectionQuery.val(), true, true);
                query.append(optionQuery).trigger('change');
            }
            query.one("change",function(){$(expressionValues).off("loadBlankAndCustomSqlFieldsComplete")});
            operator.val($("#" + previousSectionPrefix + "operator").val()).trigger("change");
            queryLevel.val($("#" + previousSectionPrefix + "queryLevel").val()).trigger("change");
            dateRangeEnum.val($("#" + previousSectionPrefix + "dateRangeInformationForTemplateQuery\\.dateRangeEnum").val()).trigger("change");
            dateRangeStartAbsolute.val($("#" + previousSectionPrefix + "dateRangeInformationForTemplateQuery\\.dateRangeStartAbsolute").val());
            dateRangeEndAbsolute.val($("#" + previousSectionPrefix + "dateRangeInformationForTemplateQuery\\.dateRangeEndAbsolute").val());
            relativeDateRangeValue.val($("#" + previousSectionPrefix + "dateRangeInformationForTemplateQuery\\.relativeDateRangeValue").val());

            header.val($("#" + previousSectionPrefix + "header").val());
            footer.val($("#" + previousSectionPrefix + "footer").val());
            title.val($("#" + previousSectionPrefix + "title").val());
            headerProductSelection.prop("checked", $("#" + previousSectionPrefix + "headerProductSelection").prop("checked"));
            headerDateRange.prop("checked", $("#" + previousSectionPrefix + "headerDateRange").prop("checked"));
            blindProtected.prop("checked", $("#" + previousSectionPrefix + "blindProtected").prop("checked"));
            privacyProtected.prop("checked", $("#" + previousSectionPrefix + "privacyProtected").prop("checked"));
        }
        clone.show();

        if (templateQueryChildCount != 0) {
            clone.find(".showHeaderFooterArea").on('click', showHeaderFooterTitle);
        }
        templateQueryChildCount++;

        if ($('.templateQuery-div:visible').length > 1) {
            $.each($('.templateQuery-div:visible'), function() {
                $($(this).find('.templateQueryDeleteButton')).show();
            })
        }

    };

    var copyBlankAndCustomSqlFields = function (lastVisibleTemplateQueryChildIndex, currentIndex) {
        var blankAndCustomSqlFields = $("[name^='templateQuery" + lastVisibleTemplateQueryChildIndex + ".'][name$='].value']," +
            "[name^='templateQuery" + lastVisibleTemplateQueryChildIndex + ".'][name$='].copyPasteValue']");
        _.each(blankAndCustomSqlFields, function (it) {
            var current = $(it);
            var nameAttributeValueForCopyField = "templateQuery" + currentIndex + current.attr("name").substring(current.attr("name").indexOf("."));
            if (current.val()) {
                var fieldToCopy = $("[name='" + nameAttributeValueForCopyField + "']");

                if (current.hasClass("expressionValueSelectNonCache") || current.hasClass("expressionValueSelectAuto")) {
                    _.each(Array.isArray(current.val()) ? current.val() : current.val()?.split(";"), function(singeValue){
                        if (!fieldToCopy.find(`option[value="${singeValue}"]`).length) {
                            fieldToCopy.append(new Option(singeValue, singeValue))
                        }
                    });
                }
                fieldToCopy.val(current.val()).trigger("change");
                if (current.hasClass("expressionValueText")) {
                    fieldToCopy = $("[name='" + nameAttributeValueForCopyField.replace("value", "copyPasteValue") + "']");
                    var currentQEV = fieldToCopy.closest('.toAddContainerQEV')[0];
                    if (currentQEV) {
                        fieldToCopy.val(current.val());
                        $(currentQEV).find('.isFromCopyPaste').val('true');
                        showHideValue(EDITOR_TYPE_TEXT, currentQEV);
                    }
                }
            }
        });
    };

    var removeSectionCloseButton = function (parentDiv) {
        var removeButton =  $(parentDiv).find("i.templateQueryDeleteButton");
        $(removeButton).hide();
    };

    //Logic for hiding and showing the date picker and text box for relative date input for show and hide of Contents
    var showHeaderFooterTitle = function () {
        var element = $(this);
        var headerFooterDiv = ($(element).parent().parent().parent());
        var advancedOptionDiv = headerFooterDiv.find('.headerFooterArea');
        advancedOptionDiv.toggle();

        if (advancedOptionDiv.is(':visible')) {
            $(this).text(LABELS.labelHideAdvancedOptions)
        } else {
            $(this).text(LABELS.labelShowAdvancedOptions)
        }
    };

    init();
    $("#templateQueryList").on("click", ".createTemplateQueryButton", function () {
        var el = $(this);
        var url = el.attr("data-url");
        var message = el.attr("data-message");
        url += (url.indexOf("?") > -1) ? "&templateQueryIndex=" : "?templateQueryIndex=";
        url += getTemplateQueryIndex(el);
        showWarningOrSubmit(url, message);
    });

    function getTemplateQueryIndex(el) {
        var $currentTemplateQuery = $(el).closest(".templateQuery-div");
        var num = 0;
        var templateQueryList = $("#templateQueryList").find(".templateQuery-div");
        for (var i = 0; i < templateQueryList.length; i++) {
            if ($(templateQueryList[i]).is($currentTemplateQuery)) return num;
            if ($(templateQueryList[i]).find("input[id$=dynamicFormEntryDeleted]").val() == "false") num++;
        }
        return num;
    }
});

function bindTemplateSelect2(selector, data) {
    return bindSelect2WithUrl(selector, templateList, data);
}


function bindQuerySelect2(selector, data) {
    return bindSelect2WithUrl(selector, queryList, data);
}

function bindExistingTemplate(templateQueryChildCount, templateIdList) {
    $.ajax({
        url: templateIdNameListUrl,
        type: "POST",
        dataType: "json",
        data: {templateIdList: templateIdList.join(",")},
    })
        .done(function (data) {
            bindExistingTemplateSuccessCallback(templateQueryChildCount, data.templateIdNameList)
        })

}

function bindExistingTemplateQuery(templateQueryChildCount, queryIdList) {
    $.ajax({
        url: queryIdNameListUrl,
        type: "POST",
        dataType: "json",
        data: {queryIdList: queryIdList.join(",")},
    })
        .done(function (data) {
            bindExistingTemplateQuerySuccessCallback(templateQueryChildCount, data.queryIdNameList)
        })

}

function bindExistingTemplateSuccessCallback(templateQueryChildCount, templateIdNameList) {
    var data = {};
    for (var i = 0; i < templateQueryChildCount; i++) {
        data = {};
        if ($("#templateQueries\\[" + i + "\\]\\.rptTempId").val()) {
            data.id = $("#templateQueries\\[" + i + "\\]\\.rptTempId").val();
            data.text = $.grep(templateIdNameList, function (e) {
                return e.id == data.id
            })[0].text;
            bindTemplateSelect2($("#templateQueryList").find("#templateQueries\\[" + i + "\\]\\.template"), data).on("change", function (e) {
                selectTemplateOnChange(this, cioms1Id);
            })

        } else {
            bindTemplateSelect2($("#templateQueryList").find("#templateQueries\\[" + i + "\\]\\.template")).on("change", function (e) {
                selectTemplateOnChange(this, cioms1Id);
            })
        }
    }
}

function bindExistingTemplateQuerySuccessCallback(templateQueryChildCount, queryIdNameList) {
    var data = {};
    for (var i = 0; i < templateQueryChildCount; i++) {
        data = {};
        if ($("#templateQueries\\[" + i + "\\]\\.queryId").val()) {
            data.id = $("#templateQueries\\[" + i + "\\]\\.queryId").val();
            data.text = $.grep(queryIdNameList, function (e) {
                return e.id == data.id
            })[0].text;
            bindQuerySelect2($("#templateQueryList").find("#templateQueries\\[" + i + "\\]\\.query"), data).on("change", function (e) {
                selectQueryOnChange('qev',this);
            });
            $(`#templateQuery${i}`).find(".expressionValueSelect").select2();
        } else {
            bindQuerySelect2($("#templateQueryList").find("#templateQueries\\[" + i + "\\]\\.query")).on("change", function (e) {
                selectQueryOnChange('qev',this);
            });
        }
    }
}

