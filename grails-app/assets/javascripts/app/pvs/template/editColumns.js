var newColumnList = [];
var newGroupingList = [];
var newRowColList = [];
var newRowList = [];
var numOfReassessListedness = 0;

var columnSetList = []; // for data tabulation templates
var sequence_col_dt = [];

$(function () {

    /*  This is set affirmatively to false in view.gsp only by a hidden field, so the expression below will eval to true.
        In create/edit, it's not set as a hidden field, so the expression below will eval to false - morett
     */
    viewOnly = ($('#editable').val() === 'false');
    var templateType = $('#templateType').val();

    var colSelected;
    var sequence_col = 0;
    var sortLevels = 5;
    var stackId = 1;
    var stackedColumnsList = {};
    var sequence_row = 0;
    var sequence_group = 0;
    var sequence_rowColumn = 0;

    var sortObjList = [];
    var sortLevel = 1;
    var MAX_SORT_LEVELS = 5;
    var SORT_ASCENDING = 'asc';
    var SORT_DESCENDING = 'desc';
    var SORT_DISABLED = 'disabled';

    var COLUMN = 'column';
    var ROW = 'row';
    var GROUP = 'group';
    var ROW_COL = 'rowCol';
    var RE_ASSESS_LISTEDNESS = 'Re-assess Listedness';

    var TYPE_CASE_LINE = 'CASE_LINE';
    var TYPE_DATA_TAB = 'DATA_TAB';
    var TYPE_CUSTOM_SQL = 'CUSTOM_SQL';
    var TYPE_NON_CASE = 'NON_CASE';

    var addToDT = COLUMN;


    // Startup ---------------------------------------------------------------------------------------------------------
    // For edit/view page and validation round trip
    if ($('#columns').val()) {
        $.each(JSON.parse($('#columns').val()), function () {
            addFieldToColumns(this, document.getElementById('columnsContainer'));
        });
    }

    if ($('#grouping').val()) {
        $.each(JSON.parse($('#grouping').val()), function () {
            addFieldToCLLGrouping(this, document.getElementById('groupingContainer'));
        });
    }

    if ($('#rowCols').val()) {
        $.each(JSON.parse($('#rowCols').val()), function () {
            addFieldToCLLRowCols(this, document.getElementById('rowColumnsContainer'));
        });
    }

    if ($('#rows').val()) {
        $.each(JSON.parse($('#rows').val()), function () {
            addFieldToDTRows(this, document.getElementById('rowsContainer'));
        });
    }

    numColMeas = parseInt($('#numColMeas').val());
    for (var index = 0; index < numColMeas; index++) {
        columnSetList.push([]);
        if ($('#columns'+index).val()) {
            $.each(JSON.parse($('#columns'+index).val()), function () {
                addFieldToColumns(this, document.getElementById('columnsContainer'+index), index);
            });
        }
    }

    //TODO: GSP should handle this instead of JS. Controller needs to let GSP know if it is create, edit, or view
    if (viewOnly) {
        if (templateType == TYPE_CUSTOM_SQL || templateType == TYPE_NON_CASE) {
            $.each(document.getElementsByClassName('sqlBox'), function() {
                this.disabled = true;
            });
        } else {
            var previewArea;
            if (templateType == TYPE_CASE_LINE) {
                $("#pageBreakByGroup").attr('disabled', true);
                $("#pageBreakByGroup").parent().removeClass('add-cursor');
                $("#columnShowTotal").attr('disabled', true);
                $("#columnShowTotal").parent().removeClass('add-cursor');
                previewArea = $('#columnsContainer')[0].parentElement.parentElement.parentElement;

            } else {
                $("#addColumnMeasure").hide();
                $(".removeColumnMeasure").hide();
                $(".selectMeasure").hide();
                $(".showTotalIntervalCases").attr('disabled', true);
                $(".showTotalIntervalCases").parent().removeClass('add-cursor');
                $(".showTotalCumulativeCases").attr('disabled', true);
                $(".showTotalCumulativeCases").parent().removeClass('add-cursor');
                previewArea = $('#dataTabulation').find('.previewLabel')[0];
            }
            previewArea.classList.remove('col-xs-9');
            previewArea.classList.add('col-xs-12');

            $.each(document.getElementsByClassName('rowsAndColumnsContainer'), function () {
                this.classList.remove('selectedContainerBorder');
            });

            $('#reassessListedness').prop("disabled", true);
        }
    }

    //TODO: Instead of finding the container onchange, store the currently selected container in a var and update that var when the selected container changes
    $('#selectedTemplate_lineListing').select2();
    $('#selectedTemplate_dataTabulation').select2();
    $('#selectField_lineListing').select2({
        placeholder: "Select Field",
        allowClear: true,
        minimumResultsForSearch: 15
    }).on("change", function (e) {
        if (e.added.text == 'Select Field') {
            $('#selectField_lineListing').parent().addClass('has-error');
        } else {
            $('#selectField_lineListing').parent().removeClass('has-error');
            var container = document.getElementsByClassName('selectedContainerBorder')[0].getElementsByClassName('containerToBeSelected')[0];
            var comparedToContainers = getComparedContainers(this, container);
            if (!isDuplicate(e.added.id, comparedToContainers)) {
                e.added.argusName = e.added.element[0].attributes.argusName.value;
                e.added.reportFieldName = e.added.element[0].attributes.reportFieldName.value;
                if ($(container).hasClass('groupingContainer')) {
                    addFieldToCLLGrouping(e.added, container);
                } else if ($(container).hasClass('rowColumnsContainer')) {
                    addFieldToCLLRowCols(e.added, container);
                } else {
                    addFieldToColumns(e.added, container);
                }
            }
        }
    }).on("select2-open", function () {
        $('#selectField_lineListing').val("Select Field");
    }).on("select2-close", function () {
//        $('#selectField').select2("open");
    });

    $('#selectField_dataTabulation_' + COLUMN).select2({
        placeholder: "Select Field",
        allowClear: true,
        minimumResultsForSearch: 15
    }).on("change", function (e) {
        dtOnChange(e, this, COLUMN);
    }).on("select2-open", function () {
        $('#selectField_dataTabulation_' + COLUMN).val("Select Field");
    });

    $('#selectField_dataTabulation_' + ROW).select2({
        placeholder: "Select Field",
        allowClear: true,
        minimumResultsForSearch: 15
    }).on("change", function (e) {
        dtOnChange(e, this, ROW);
    }).on("select2-open", function () {
        $('#selectField_dataTabulation_' + ROW).val("Select Field");
    });

    function dtOnChange(e, select2, type) {
        if (e.added.text == 'Select Field') {
            $('#selectField_dataTabulation_' + type).parent().addClass('has-error');
        } else {
            $('#selectField_dataTabulation_' + type).parent().removeClass('has-error');
            var container = document.getElementsByClassName('selectedContainerBorder')[0].getElementsByClassName('containerToBeSelected')[0];
            var index = $(container.closest('.columnMeasureSet')).attr('sequence');

            var comparedToContainers = getComparedContainers(select2, container);
            if (!isDuplicate(e.added.id, comparedToContainers)) {
                e.added.argusName = e.added.element[0].attributes[0].nodeValue;
                e.added.reportFieldName = e.added.element[0].attributes.reportFieldName.value;
                if (type == ROW) {
                    addFieldToDTRows(e.added, container);
                } else {
                    addFieldToColumns(e.added, container, index);
                }
            }
        }
    }

    $('#reassessListedness').select2();
    $('#selectDatasheet').select2()
        .on('change', function (e) {
            var seq = $(colSelected.parentElement).attr('sequence');
            var list = getRelatedList();
            list[seq].datasheet = e.val;
        });


    // ----------------------------------------------------------------------------------------------------- END STARTUP

    // Events ----------------------------------------------------------------------------------------------------------

    $(document).on('click', '.rowsAndColumnsContainer', function () {
        if (!viewOnly) {
            $.each(document.getElementsByClassName('selectedContainerBorder'), function () {
                this.classList.remove('selectedContainerBorder');
            });
            if ($(this).hasClass('rowsContainer')) {
                addToDT = ROW;
                $('#dtRowSelect2').show();
                $('#dtColumnSelect2').hide();
            } else {
                if (templateType == TYPE_DATA_TAB) {
                    addToDT = COLUMN;
                    $('#dtColumnSelect2').show();
                    $('#dtRowSelect2').hide();
                }
            }
            this.classList.add('selectedContainerBorder');
        }
    });

    function getRelatedList() {
        var isGroup = $(colSelected.closest('.rowsAndColumnsContainer')).hasClass('groupingContainer');
        var isRowColumn = $(colSelected.closest('.rowsAndColumnsContainer')).hasClass('rowColumnsContainer');
        var isRow = $(colSelected.closest('.rowsAndColumnsContainer')).hasClass('rowsContainer');
        var list = [];

        // get related list for selected field
        if (isGroup) {
            list = newGroupingList;
        } else if (isRowColumn) {
            list = newRowColList;
        } else if (isRow) {
            list = newRowList;
        } else if (templateType == TYPE_DATA_TAB) {
            var index = $(colSelected.closest('.columnMeasureSet')).attr('sequence');
            list = columnSetList[parseInt(index)];
        } else {
            list = newColumnList;
        }
        return list;
    }

    $(document).on('click', '.columnName', function () {

        $.each(document.getElementsByClassName('columnSelected'), function () {
            this.classList.remove('columnSelected');
        });
        var fieldInfo = this.parentElement;
        fieldInfo.classList.add('columnSelected');
        colSelected = this;

        var list = getRelatedList();
        var colSeq = $(fieldInfo).attr('sequence');

        // show custom expression value
        showCustomExpression(list[colSeq].customExpression);

        showRenameArea(true);

        // show rename value
        var colName = $(fieldInfo).attr('fieldName');
        if (list[colSeq].renameValue) {
            colName = list[colSeq].renameValue;
        }
        var renameArea = $('.columnRenameArea')[0];
        $(renameArea.getElementsByClassName('selectedColumnName')[0]).val(colName);

        if (templateType == TYPE_CASE_LINE) {
            // show comma-separated value
            if (list[colSeq].commaSeparatedValue) {
                document.getElementsByClassName('commaSeparated')[0].checked = true;
            } else {
                document.getElementsByClassName('commaSeparated')[0].checked = false;
            }

            // show suppress repeating values
            if (list[colSeq].suppressRepeatingValues) {
                document.getElementsByClassName('suppressRepeating')[0].checked = true;
            } else {
                document.getElementsByClassName('suppressRepeating')[0].checked = false;
            }

            // show blinded values
            if (list[colSeq].blindedValue) {
                document.getElementsByClassName('blindedValues')[0].checked = true;
                $(renameArea.getElementsByClassName('customExpressionArea')[0]).attr('hidden', 'hidden');
            } else {
                document.getElementsByClassName('blindedValues')[0].checked = false;
            }
        }

        // show datasheet selection
        if ($(fieldInfo).attr('fieldName') === RE_ASSESS_LISTEDNESS) {
            $('.datasheetOption').show();
            var datasheet = list[colSeq].datasheet;
            $('#selectDatasheet').val(datasheet); // TODO: show default datasheet
        } else {
            $('.datasheetOption').hide();
        }

        if (viewOnly) {
            $("input").prop("disabled", true);
            $("button").prop("disabled", true);
            $("textarea").prop("disabled", true);
            $('#selectDatasheet').prop("disabled", true);
            $.each($(":checkbox"), function () {
                this.parentElement.classList.remove("add-cursor");
            });
        }
    });

    $(document).on('click', '.closeRenameArea', function () {
        showRenameArea(false);
        colSelected.parentElement.classList.remove('columnSelected');
        colSelected = null;
    });

    function showRenameArea(open) {
        if (open) {
            $(document.getElementsByClassName('measureOptions')).hide();
            $(document.getElementsByClassName('columnRenameArea')[0]).slideDown();
        } else {
            $(document.getElementsByClassName('columnRenameArea')[0]).hide();
        }
    }

    function showCustomExpression(customExpression) {
        var renameArea = $('.columnRenameArea')[0];
        if (customExpression) {
            $(renameArea.getElementsByClassName('customExpressionArea')[0]).removeAttr('hidden');
            $(renameArea.getElementsByClassName('customExpressionValue')[0]).val(customExpression);
        } else {
            $(renameArea.getElementsByClassName('customExpressionArea')[0]).attr('hidden', 'hidden');
            $(renameArea.getElementsByClassName('customExpressionValue')[0]).val('');
        }
    }

    $(document).on('click', '.commaSeparated', function () {
        var fieldInfo = colSelected.parentElement;
        var sequence = $(fieldInfo).attr('sequence');
        var list = getRelatedList();

        if (this.checked) {
            list[sequence].commaSeparatedValue = true;
        } else {
            list[sequence].commaSeparatedValue = false;
        }
    });

    $(document).on('click', '.suppressRepeating', function () {
        var fieldInfo = colSelected.parentElement;
        var sequence = $(fieldInfo).attr('sequence');
        var list = getRelatedList();

        if (this.checked) {
            list[sequence].suppressRepeatingValues = true;
        } else {
            list[sequence].suppressRepeatingValues = false;
        }
    });

    $(document).on('click', '.blindedValues', function () {
        var fieldInfo = colSelected.parentElement;
        var sequence = $(fieldInfo).attr('sequence');
        var list = getRelatedList();

        var customExpression = $('.columnRenameArea')[0].getElementsByClassName('customExpressionArea')[0];

        if (this.checked) {
            list[sequence].blindedValue = true;
            $('.customExpressionValue').val("decode (1,1,'Blinded', " + $(fieldInfo).attr('argusName') + ')');
        } else {
            list[sequence].blindedValue = false;
            $(customExpression).attr('hidden', 'hidden');
            $('.customExpressionValue').val($(fieldInfo).attr('argusName'));
        }
        $('.customExpressionValue').trigger('change');
    });

    $(document).on('click', '.showCustomExpression', function () {
        var sequence = $(colSelected.parentElement).attr('sequence');
        var list = getRelatedList();
        var customExpression = $('.columnRenameArea')[0].getElementsByClassName('customExpressionArea')[0];
        if (customExpression.hasAttribute('hidden')){
            $(customExpression).removeAttr('hidden');
            var value = $(colSelected.parentElement).attr('argusName');
            if (list[sequence].customExpression) {
                value = list[sequence].customExpression;
            }
            $('.columnRenameArea')[0].getElementsByClassName('customExpressionValue')[0].value = value;
        } else {
            $(customExpression).attr('hidden', 'hidden');
        }
    });

    $(document).on('change', '.customExpressionValue', function () {
        var seq = $(colSelected.parentElement).attr('sequence');
        var renameArea = $('.columnRenameArea')[0];
        var customExpressionValue = $(renameArea.getElementsByClassName('customExpressionValue')[0]).val();
        if (customExpressionValue.trim() == $(colSelected.parentElement).attr('argusName') || customExpressionValue.trim() == '') {
            customExpressionValue == "";
        }

        var list = getRelatedList();
        list[seq].customExpression = customExpressionValue;
    });


    $(document).on('change', '.selectedColumnName', function () {
        var fieldInfo = colSelected.parentElement;
        var renameSeq = $(fieldInfo).attr('sequence');
        var renameArea = $('.columnRenameArea')[0];
        // may need trim() on newName
        var newName = $(renameArea.getElementsByClassName('selectedColumnName')).val();

        var list = getRelatedList();

        if (newName != $(fieldInfo).attr('fieldName')) {
            list[renameSeq].renameValue = newName;
            $(fieldInfo).attr("renamedTo", newName);
        } else {
            list[renameSeq].renameValue = null;
            $(fieldInfo).removeAttr('renamedTo');
        }

        $(colSelected).text(newName);
    });

    $(document).on('click', '.resetThisCol', function () {
        var fieldInfo = colSelected.parentElement;
        var originalName = $(fieldInfo).attr('fieldName');
        var renameArea = $('.columnRenameArea')[0];
        $(renameArea.getElementsByClassName('selectedColumnName')[0]).val(originalName);
        $(colSelected).text(originalName);
        $(fieldInfo).removeAttr('renamedTo');
        var list = getRelatedList();
        list[$(fieldInfo).attr('sequence')].renameValue = null;
    });

    $(document).on('click', '.sortIcon', function () {
        if (!viewOnly) {
            var fieldInfo = this.parentElement;
            var seq = $(fieldInfo).attr('sequence');
            var preSelected = colSelected;
            colSelected = this;
            var list = getRelatedList();

            if (sortLevels > 0 || (sortLevels <= 0 && this.classList.contains('sortEnabled'))) {
                if (this.classList.contains('sortDisabled')) {
                    removeSortIcon(this.classList);
                    addAscIcon(this.classList);
                    this.style.opacity = 1.0;

                    list[seq].sortLevel = sortLevel;
                    list[seq].sort = SORT_ASCENDING;
                    sortObjList.push(list[seq]);

                    $(fieldInfo.getElementsByClassName('sortOrderSeq')).text(sortLevel);
                    sortLevel++;

                } else if (this.classList.contains('sortAscending')) {
                    removeAscIcon(this.classList);
                    addDescIcon(this.classList);
                    this.style.opacity = 1.0;

                    list[seq].sort = SORT_DESCENDING;
                    $(fieldInfo.getElementsByClassName('sortOrderSeq')).text(sortLevel-1);

                } else if (this.classList.contains('sortDescending')) {
                    removeDescIcon(this.classList);
                    addSortIcon(this.classList);
                    this.style.opacity = 0.3;

                    list[seq].sortLevel = 0;
                    list[seq].sort = SORT_DISABLED;

                    // update list
                    var index = sortObjList.indexOf(list[seq]);
                    sortObjList.splice(index, 1);
                    $.each(sortObjList, function(index, obj) {
                        obj.sortLevel = index + 1;
                    });

                    $(fieldInfo.getElementsByClassName('sortOrderSeq')).text('');
                    sortLevel--;

                }
            }
            updateSortOrderText();
            colSelected = preSelected;
        }
    });

    $(document).on('click', '.removeColumn', function () {
        var isGroup = $(this.closest('.rowsAndColumnsContainer')).hasClass('groupingContainer');
        var isRowColumn = $(this.closest('.rowsAndColumnsContainer')).hasClass('rowColumnsContainer');
        var isRow = $(this.closest('.rowsAndColumnsContainer')).hasClass('rowsContainer');

        var colMeasIndex = $(this.closest('.columnMeasureSet')).attr('sequence');

        if (!viewOnly) {
            var fieldInfo = this.parentElement;
            var currentSequence = $(fieldInfo).attr('sequence');

            var preSelected = colSelected;
            colSelected = this;
            var list = getRelatedList();
            var removeSortLevel = list[currentSequence].sortLevel;
            showRenameArea(false);

            if ($(fieldInfo).attr('fieldName') === RE_ASSESS_LISTEDNESS) {
                numOfReassessListedness--;
                if (numOfReassessListedness === 0) {
                    $('#reassessListedness').val('');
                    $('.reassessListedness').hide();
                }
            }

            var wholeColumn = fieldInfo.parentElement.parentElement;
            if (list[currentSequence].stackId > 0) {
                fieldInfo.remove();
                var colsLeft = wholeColumn.getElementsByClassName('fieldInfo');
                if (colsLeft.length == 1) {
                    list[currentSequence].stackId = 0;
                }
            } else if (isRow) {
                var containerWidth = $(wholeColumn.parentElement).width();
                var displayWidth = $(wholeColumn.parentElement.parentElement).width();
                if (containerWidth > displayWidth) {
                    containerWidth -= $(wholeColumn).width();
                    if (containerWidth < displayWidth) {
                        containerWidth = displayWidth;
                    }
                    $(wholeColumn.parentElement).width(containerWidth);
                }
                wholeColumn.remove();
            } else if (isRowColumn || templateType === TYPE_DATA_TAB) {
                wholeColumn.remove();
            } else {
                var containerWidth = $(wholeColumn.parentElement).width();
                containerWidth -= $(wholeColumn).width();
                $(wholeColumn.parentElement).width(containerWidth);
                wholeColumn.remove();
            }

            if (removeSortLevel > 0) {
                // update list
                var index = sortObjList.indexOf(list[currentSequence]);
                sortObjList.splice(index, 1);
                $.each(sortObjList, function(index, obj) {
                    obj.sortLevel = index + 1;
                });
                sortLevel--;
            }

            list.splice(currentSequence, 1);
            if (isRow) {
                sequence_row--;
            } else if (isGroup) {
                sequence_group--;
            } else if (isRowColumn) {
                sequence_rowColumn--;
            } else if (templateType == TYPE_DATA_TAB) {
                sequence_col_dt[colMeasIndex]--;
            } else {
                sequence_col--;
            }

            updateColumnListAndSortOrder();
            colSelected = preSelected;
        }
    });

    // ------------------------------------------------------------------------------------------------------ END EVENTS

    // Helper methods --------------------------------------------------------------------------------------------------

    function getComparedContainers(obj, currentContainer) {
        var containersList = document.getElementsByClassName('rowsAndColumnsContainer');
        var comparedToContainers = [];
        // Only columns in CLL allow duplicates
        if (!($(currentContainer).hasClass('columnsContainer') && templateType == TYPE_CASE_LINE)) {
            comparedToContainers.push(currentContainer);
        }
        $.each(containersList, function () {
            if (!$(this).hasClass('selectedContainerBorder')) {
                if (!($(currentContainer).hasClass('columnsContainer') && $(this).hasClass('columnsContainer'))) {
                    comparedToContainers.push(this);
                }
            }
        });
        return comparedToContainers;
    }

    function isDuplicate(selectedFieldId, comparedToContainers) {
        var isDuplicate = false;
        var totalFields = [];
        $.each(comparedToContainers, function () {
            $.each(this.getElementsByClassName('fieldInfo'), function () {
                totalFields.push(this)
            });
        });

        $.each(totalFields, function () {
            this.parentElement.style.border = '';
            if ($(this).attr('fieldId') == selectedFieldId) {
                isDuplicate = true;
                $('#selectField_dataTabulation_' + addToDT).parent().addClass('has-error');
                this.parentElement.style.border = 'solid 1px red';
            }
        });
        return isDuplicate;
    }

    function isFirst(stackId) {
        var isFirstInStack = true;
        if (stackId > 0) {
            var columns = document.getElementById('columnsContainer').getElementsByClassName('fieldInfo');
            $.each(columns, function() {
                if ($(this).attr('stackId') == stackId) {
                    isFirstInStack = false;
                }
            });
        }
        return isFirstInStack;
    }

    function addFieldToColumns(reportField, containerToAppend, dtIndex) {
        var seq = sequence_col;
        if (dtIndex > -1) {
            if (sequence_col_dt[dtIndex]) {
                seq = sequence_col_dt[dtIndex];
            } else {
                seq = 0;
            }
        }
        var reportFieldInfo = reportField;
        reportFieldInfo.type = COLUMN;
        reportFieldInfo.sequence = seq;
        if (templateType == TYPE_DATA_TAB) {
            reportFieldInfo.colMeasIndex = $(containerToAppend.closest('.columnMeasureSet')).attr('sequence');
        }

        var baseCell = document.getElementById('toAddColumn');
        var inStack = !isFirst(reportField.stackId);

        var clonedNodes = getClonedNodes(baseCell, inStack);
        var cellToAdd = clonedNodes[0];
        var fieldInfo = clonedNodes[1];

        addFieldInfo(reportField, fieldInfo, seq);
        addIcons(cellToAdd, fieldInfo, reportFieldInfo, false);

        if (templateType === TYPE_DATA_TAB) {
            hideEmptyContent(fieldInfo);
        } else {
            addToStackList(reportField, sequence_col);
        }

        if (inStack) {
            var lastIndex = $('[stackId=' + reportField.stackId + ']').length - 1;
            var lastInStack = $('[stackId=' + reportField.stackId + ']')[lastIndex];
            lastInStack.parentElement.insertBefore(cellToAdd, lastInStack.nextSibling);
        } else if (templateType === TYPE_DATA_TAB) { // Add data tabulation columns
            insertVerticalField(cellToAdd, containerToAppend);
        } else {
            insertHorizontalField(cellToAdd, containerToAppend);
        }

        addListeners(cellToAdd);

        if (dtIndex > -1) {
            if (sequence_col_dt[dtIndex]) {
                sequence_col_dt[dtIndex]++;
            } else {
                sequence_col_dt[dtIndex] = 1;
            }
            columnSetList[dtIndex].push(reportFieldInfo);
        } else {
            sequence_col++;
            newColumnList.push(reportFieldInfo);
        }

    }

    function addFieldToCLLGrouping(reportField, containerToAppend) {
        var reportFieldInfo = reportField;
        reportFieldInfo.type = GROUP;
        reportFieldInfo.sequence = sequence_group;

        var baseCell = document.getElementById('toAddColumn');

        var clonedNodes = getClonedNodes(baseCell, false);
        var cellToAdd = clonedNodes[0];
        var fieldInfo = clonedNodes[1];

        addFieldInfo(reportField, fieldInfo, sequence_group);
        addIcons(cellToAdd, fieldInfo, reportFieldInfo, false);
        hideEmptyContent(fieldInfo);
        insertHorizontalField(cellToAdd, containerToAppend);
        addListeners(cellToAdd);

        sequence_group++;
        newGroupingList.push(reportFieldInfo);
    }

    function addFieldToCLLRowCols(reportField, containerToAppend) {
        var reportFieldInfo = reportField;
        reportFieldInfo.type = ROW_COL;
        reportFieldInfo.sequence = sequence_rowColumn;

        var baseCell = document.getElementById('toAddColumn');

        var clonedNodes = getClonedNodes(baseCell, false);
        var cellToAdd = clonedNodes[0];
        var fieldInfo = clonedNodes[1];

        addFieldInfo(reportField, fieldInfo, sequence_rowColumn);
        addIcons(cellToAdd, fieldInfo, reportFieldInfo, true);
        hideEmptyContent(fieldInfo);
        insertVerticalField(cellToAdd, containerToAppend);
        addListeners(cellToAdd);

        sequence_rowColumn++;
        newRowColList.push(reportFieldInfo);
    }

    function addFieldToDTRows(reportField, containerToAppend) {
        var reportFieldInfo = reportField;
        reportFieldInfo.type = ROW;
        reportFieldInfo.sequence = sequence_row;

        var baseCell = document.getElementById('toAddRow');

        var clonedNodes = getClonedNodes(baseCell, false);
        var cellToAdd = clonedNodes[0];
        var fieldInfo = clonedNodes[1];

        addFieldInfo(reportField, fieldInfo, sequence_row);
        addIcons(cellToAdd, fieldInfo, reportFieldInfo, true);

        containerToAppend.insertBefore(cellToAdd, containerToAppend.firstChild);
        var rowContainerWidth = $(containerToAppend).width();
        var rowContentWidth = getRowContentWidth(containerToAppend);
        if (rowContentWidth > rowContainerWidth) {
            rowContainerWidth += (rowContentWidth - rowContainerWidth)+5;
            $(containerToAppend).width(rowContainerWidth);
        }

        addListeners(cellToAdd);

        sequence_row++;
        newRowList.push(reportFieldInfo);
    }

    function getClonedNodes(baseCell, inStack) {
        var cellToAdd = baseCell.cloneNode(true);
        var fieldInfo = cellToAdd.getElementsByClassName('fieldInfo')[0];

        if (inStack) {
            cellToAdd = baseCell.getElementsByClassName('fieldInfo')[0].cloneNode(true);
            fieldInfo = cellToAdd;
        }
        $(cellToAdd).removeAttr("id");

        return [cellToAdd, fieldInfo];
    }

    function addFieldInfo(reportField, fieldInfo, sequence) {
        if (reportField.text === RE_ASSESS_LISTEDNESS) {
            numOfReassessListedness++;
            $('.reassessListedness').show();
        }

        $(fieldInfo).attr("sequence", sequence);
        $(fieldInfo).attr("fieldId", reportField.id);
        $(fieldInfo).attr("fieldName", reportField.text);
        $(fieldInfo).attr("argusName", reportField.argusName);
        $(fieldInfo).attr("stackId", reportField.stackId);

        if (reportField.renameValue) {
            if (reportField.renameValue != reportField.text) {
                $(fieldInfo).attr("renamedTo", reportField.renameValue);
            }
        }
    }

    function addToStackList(reportField, sequence) {
        if (reportField.stackId > 0) {
            if (!stackedColumnsList.hasOwnProperty(reportField.stackId)) {
                stackedColumnsList[reportField.stackId] = [];
                if (stackId <= reportField.stackId) {
                    stackId = reportField.stackId + 1;
                }
            }
            stackedColumnsList[reportField.stackId].push(sequence);
        }
    }

    function hideEmptyContent(fieldInfo) {
        $.each(fieldInfo.parentElement.parentElement.getElementsByClassName('emptyColumnContent'), function () {
            $(this).hide();
        });
    }

    function addIcons(cellToAdd, fieldInfo, reportFieldInfo, addToRowColumns, colMeasureIndex) {
        if (viewOnly) {
            var dragColumn = $(cellToAdd).find('.middleColumn')[0];
            $(dragColumn).removeAttr('draggable');
            $(fieldInfo).append('<div class="sortOrderSeq"></div>');
        } else if (addToRowColumns) {
            $(fieldInfo).append('<i class="fa fa-times removeColumn"></i>');
        } else {
            $(fieldInfo).append('<i class="fa fa-times removeColumn"></i><div class="sortOrderSeq"></div>');
        }

        var cellName = $(fieldInfo).attr("fieldName");
        if ($(fieldInfo).attr("renamedTo")) {
            if ($(fieldInfo).attr("renamedTo") != cellName) {
                cellName = $(fieldInfo).attr("renamedTo");
            }
        }

        $(fieldInfo).append('<div class="columnName">' + cellName + '</div>');
        if (!addToRowColumns) {
            $(fieldInfo).append('<i class="sortIcon fa fa-sort fa-lg sortDisabled"></i>');
        }

        // Add sort info
        if (reportFieldInfo.sort) {
            var sortClassList = $(fieldInfo).find('.sortIcon')[0].classList;
            if (reportFieldInfo.sort == SORT_ASCENDING) {
                removeSortIcon(sortClassList);
                addAscIcon(sortClassList);
            } else if (reportFieldInfo.sort == SORT_DESCENDING) {
                removeSortIcon(sortClassList);
                addDescIcon(sortClassList);
            }
            $(fieldInfo.getElementsByClassName('sortOrderSeq')).text(reportFieldInfo.sortLevel);

            sortObjList.push(reportFieldInfo);
            sortObjList = sortObjList.sort(function(a, b){return a.sortLevel - b.sortLevel});
            sortLevel++;
        }
    }

    function insertHorizontalField(cellToAdd, containerToAppend) {
        var containerWidth = $(containerToAppend).width();
        $(containerToAppend).append(cellToAdd);
        containerWidth += $(cellToAdd).width();
        $(containerToAppend).width(containerWidth);
    }

    function insertVerticalField(cellToAdd, containerToAppend) {
        cellToAdd.classList.remove('floatLeft');
        $.each(cellToAdd.getElementsByClassName('add-cursor'), function () {
            this.classList.remove('floatLeft');
        });
        var elements;
        if (templateType === TYPE_DATA_TAB) { // Add to data tabulation columns
            elements = containerToAppend.getElementsByClassName('wholeColumn');
        } else {
            elements = document.getElementById('rowColumnsContainer').getElementsByClassName('wholeColumn');
        }
        var length = elements.length;

        if (length > 0) {
            var lastElement = elements[length - 1];
            lastElement.parentElement.insertBefore(cellToAdd, lastElement.nextSibling);
            if ($(lastElement).width() > $(cellToAdd).width()) {
                $(cellToAdd).width($(lastElement).width());
            }
        } else {
            $(containerToAppend).append(cellToAdd);
        }
        var maxWidth = $(cellToAdd.querySelector('.middleColumn')).width();
        $(cellToAdd.querySelectorAll('.addHiddenColumnHeader')).width(maxWidth);
        $(cellToAdd.querySelectorAll('.emptyFutureColumn')).width(maxWidth);
    }

    function addListeners(cellToAdd) {
        if (!viewOnly) {
            $.each(cellToAdd.getElementsByClassName('middleColumn'), function () {
                addDragListeners(this);
            });

            $.each(cellToAdd.getElementsByClassName('emptyFutureColumn'), function () {
                addDragListenersToEmptyColumn(this);
            });
        }
    }

    function getRowContentWidth(containerToAppend) {
        var totalWidth = 0;
        $.each(containerToAppend.getElementsByClassName('wholeColumn'), function () {
            totalWidth += $(this).width();
        });
        return totalWidth;
    }

    // ---------------------------------------------------------------------------------------------- END HELPER METHODS

    // Update ----------------------------------------------------------------------------------------------------------

    function updateColumnListAndSortOrder() {

        updateRFList(newColumnList, $('#columnsContainer').find('.fieldInfo'));
        updateRFList(newGroupingList, $('#groupingContainer').find('.fieldInfo'));
        updateRFList(newRowColList, $('#rowColumnsContainer').find('.fieldInfo'));
        updateRFList(newRowList, $('#rowsContainer').find('.fieldInfo'));

        // update column set
        $.each(columnSetList, function (index, columns) {
            updateRFList(columns, $($('.columnMeasureSet')[index]).find('.fieldInfo'));
        });

        updateSortOrderText();
    }

    function updateSortOrderText() {
        $.each(sortObjList, function(index, obj) {
            var sequence = obj.sequence;
            $.each($('.fieldInfo'), function() {
                var containerType = COLUMN;
                if ($(this.closest('.rowsAndColumnsContainer')).hasClass('groupingContainer')) {
                    containerType = GROUP;
                } else if ($(this.closest('.rowsAndColumnsContainer')).hasClass('rowColumnsContainer')) {
                    containerType = ROW_COL;
                } else if ($(this.closest('.rowsAndColumnsContainer')).hasClass('rowsContainer')) {
                    containerType = ROW;
                }

                if (obj.type == containerType && parseInt($(this).attr('sequence')) == sequence) {
                    if (templateType == TYPE_DATA_TAB) {
                        var colMeasIndex = $(this.closest('.columnMeasureSet')).attr('sequence');
                        if (obj.colMeasIndex == colMeasIndex) {
                            $(this).find('.sortOrderSeq').text(obj.sortLevel);
                        }
                    } else {
                        $(this).find('.sortOrderSeq').text(obj.sortLevel);
                    }
                }
            });
        });
    }

    function updateRFList(list, elements) {
        var newSequence = 0;
        $.each(elements, function () {
            $(this).attr('preSeq', $(this).attr('sequence'));
            $(this).attr('sequence', newSequence);
            newSequence++;
        });
        $.each(list, function (index) {
            var oldSequence = list[index].sequence;
            var el = findByAttributeValue(elements, 'preSeq', oldSequence);
            list[index].sequence = $(el).attr('sequence');
        });

        // sort the list by sequence ascending
        list = list.sort(function(a, b){return a.sequence - b.sequence});
    }

    function findByAttributeValue(elements, attribute, value) {
        var el;
        $.each(elements, function () {
            if ($(this).attr(attribute) == value) {
                el = this;
            }
        });
        return el;
    }

    // ------------------------------------------------------------------------------------------------------ END UPDATE

    // Sort Icon -------------------------------------------------------------------------------------------------------

    function removeSortIcon(classList) {
        DOMTokenList.prototype.remove.apply(classList, ['fa-sort', 'sortDisabled']);
    }

    function removeAscIcon(classList) {
        DOMTokenList.prototype.remove.apply(classList, ['fa-sort-asc', 'sortAscending', 'sortEnabled']);
    }

    function removeDescIcon(classList) {
        DOMTokenList.prototype.remove.apply(classList, ['fa-sort-desc', 'sortDescending', 'sortEnabled']);
    }

    function addSortIcon(classList) {
        DOMTokenList.prototype.add.apply(classList, ['fa-sort', 'sortDisabled']);
    }

    function addAscIcon(classList) {
        DOMTokenList.prototype.add.apply(classList, ['fa-sort-asc', 'sortAscending', 'sortEnabled']);
    }

    function addDescIcon(classList) {
        DOMTokenList.prototype.add.apply(classList, ['fa-sort-desc', 'sortDescending', 'sortEnabled']);
    }

    // --------------------------------------------------------------------------------------------------- END SORT ICON

    // Drag and Drop ---------------------------------------------------------------------------------------------------

    var dragSrcEl = null;

    function inSameContainer(sourceEl, targetEl) {
        var isSame = false;
        var $sourceContainer = $(sourceEl.closest('.rowsAndColumnsContainer'));
        var $targetContainer = $(targetEl.closest('.rowsAndColumnsContainer'));

        if ($sourceContainer.hasClass('groupingContainer') && $targetContainer.hasClass('groupingContainer')) {
            isSame = true;
        }
        if ($sourceContainer.hasClass('rowColumnsContainer') && $targetContainer.hasClass('rowColumnsContainer')) {
            isSame = true;
        }
        if ($sourceContainer.hasClass('rowsContainer') && $targetContainer.hasClass('rowsContainer')) {
            isSame = true;
        }
        if ($sourceContainer.hasClass('columnsContainer') && $targetContainer.hasClass('columnsContainer')) {
            isSame = true;
        }

        return isSame;
    }

    function handleDragStart(e) {
        this.style.opacity = '0.7';
        var textHeader = $(this).find('.addColumnHeader')[0];
        textHeader.classList.add('columnSelected');
        dragSrcEl = this;
    }

    function handleDragOver(e) {
        if (e.preventDefault) {
            e.preventDefault(); // Necessary. Allows us to drop.
        }
        var isGroup = $(this.closest('.rowsAndColumnsContainer')).hasClass('groupingContainer');

        if (inSameContainer(dragSrcEl, this) && dragSrcEl != this) {
            if (!(isGroup || templateType == TYPE_DATA_TAB)) {
                $(this.parentElement.getElementsByClassName('addColumnHeader')).removeClass('defaultHeaderBackground');
                $(this.parentElement.getElementsByClassName('addColumnHeader')).addClass('overHeaderBackground');
            }
            $(this.parentElement.getElementsByClassName('emptyFutureColumn')).addClass('over');
            $(this.parentElement.getElementsByClassName('emptyFutureColumn')).removeAttr('hidden');
        }
        e.stopPropagation();

        return false;
    }

    function handleDragEnter(e) {

        e.stopPropagation();

        if (inSameContainer(dragSrcEl, this) && dragSrcEl != this) {
            $(this.parentElement.getElementsByClassName('emptyFutureColumn')).addClass('over');
            $(this.parentElement.getElementsByClassName('emptyFutureColumn')).removeAttr('hidden');
        }
    }

    function handleDragLeave(e) {
        e.stopPropagation();
    }

    function handleDrop(e) {

        if (e.stopPropagation) {
            e.stopPropagation(); // stops the browser from redirecting.
        }

        var preSelected = colSelected;
        colSelected = dragSrcEl;
        var list = getRelatedList(); // Actually stack is only for CLL columns

        var isGroup = $(this.closest('.rowsAndColumnsContainer')).hasClass('groupingContainer');
        if (inSameContainer(dragSrcEl, this) && dragSrcEl != this && !(isGroup || templateType == TYPE_DATA_TAB)) {
            // stack here
            var target = this;
            $.each(dragSrcEl.getElementsByClassName('fieldInfo'), function() {
                var sourceHeader = this.cloneNode(true);
                colSelected = sourceHeader;

                var lastIndex = target.getElementsByClassName('addColumnHeader').length - 1;
                var targetHeader = target.getElementsByClassName('addColumnHeader')[lastIndex];
                target.insertBefore(sourceHeader, targetHeader.nextSibling);

                var targetStackId = $(targetHeader).attr('stackId');
                if (targetStackId > 0) {
                    $(sourceHeader).attr('stackId', targetStackId);
                    list[$(sourceHeader).attr('sequence')].stackId = targetStackId;
                } else {
                    $(targetHeader).attr('stackId', stackId);
                    $(sourceHeader).attr('stackId', stackId);
                    list[$(targetHeader).attr('sequence')].stackId = stackId;
                    list[$(sourceHeader).attr('sequence')].stackId = stackId;
                    stackId++;
                }
            });
            dragSrcEl.parentElement.remove();
            updateColumnListAndSortOrder();

        }

        colSelected = preSelected;
        $(this.parentElement.getElementsByClassName('emptyFutureColumn')).attr('hidden', 'hidden');
        return false;
    }

    function handleDragEnd(e) {
        this.style.opacity = '1';

        [].forEach.call($('.add-cursor'), function (col) {
            col.classList.remove('over');
            col.classList.remove('overEmpty');
        });

        [].forEach.call($('.columnSelected'), function (col) {
            col.classList.remove('columnSelected');
            col.classList.add('addColumnHeader');
        });

        [].forEach.call($('.addColumnHeader'), function (col) {
            col.classList.remove('overHeaderBackground');
            col.classList.add('defaultHeaderBackground');
        });

        [].forEach.call($('.emptyFutureColumn'), function (col) {
            $(col).attr('hidden', 'hidden');
        });

        updateColumnListAndSortOrder();
    }

    function handleDragOverEmpty(e) {
        if (e.preventDefault) {
            e.preventDefault(); // Necessary. Allows us to drop.
        }

        if (dragSrcEl.parentElement != this.parentElement) {
            $(this).addClass('overEmpty');
            $(this.parentElement.getElementsByClassName('emptyFutureColumn')).removeAttr('hidden');
        }

        e.stopPropagation();
        return false;
    }

    function handleDragEnterEmpty(e) {

        e.stopPropagation();

        if (dragSrcEl.parentElement != this.parentElement) {
            $(this).addClass('overEmpty');
            $(this.getElementsByClassName('emptyFutureColumn')).removeAttr('hidden');
            $(this.parentElement.getElementsByClassName('addColumnHeader')).removeClass('overHeaderBackground');
            $(this.parentElement.getElementsByClassName('addColumnHeader')).addClass('defaultHeaderBackground');
        }
    }

    function handleDragLeaveEmpty(e) {

        e.stopPropagation();

        $(this).removeClass('overEmpty');
        $(this.parentElement.getElementsByClassName('emptyFutureColumn')).attr('hidden', 'hidden');

    }

    function handleDropEmpty(e) {

        if (e.stopPropagation) {
            e.stopPropagation(); // stops the browser from redirecting.
        }

        var isRow = $(this.closest('.rowsAndColumnsContainer')).hasClass('rowsContainer');

        if (isRow) {
            if ($(this).hasClass('addLeft')) {
                this.parentElement.parentElement.insertBefore(dragSrcEl.parentElement, this.parentElement.nextSibling);
            }
            if ($(this).hasClass('addRight')) {
                this.parentElement.parentElement.insertBefore(dragSrcEl.parentElement, this.parentElement);
            }
        } else {
            if ($(this).hasClass('addLeft')) {
                this.parentElement.parentElement.insertBefore(dragSrcEl.parentElement, this.parentElement);
            }
            if ($(this).hasClass('addRight')) {
                this.parentElement.parentElement.insertBefore(dragSrcEl.parentElement, this.parentElement.nextSibling);
            }
        }

        $(this.parentElement.getElementsByClassName('emptyFutureColumn')).attr('hidden', 'hidden');

        return false;
    }

    function addDragListeners(element) {
        element.addEventListener('dragstart', handleDragStart, false);
        element.addEventListener('dragover', handleDragOver, true);
        element.addEventListener('dragenter', handleDragEnter, false);
        element.addEventListener('dragleave', handleDragLeave, false);
        element.addEventListener('drop', handleDrop, false);
        element.addEventListener('dragend', handleDragEnd, false);
    }

    function addDragListenersToEmptyColumn(element) {
        element.addEventListener('dragover', handleDragOverEmpty, false);
        element.addEventListener('dragenter', handleDragEnterEmpty, false);
        element.addEventListener('dragleave', handleDragLeaveEmpty, false);
        element.addEventListener('drop', handleDropEmpty, false);
        element.addEventListener('dragend', handleDragEnd, false);
    }

    // ----------------------------------------------------------------------------------------------- END DRAG AND DROP
});

function submitForm() {

    console.log('columns info: ', newColumnList);
    if (newColumnList.length == 0) {
        $('#columns').val("");
    } else {
        $('#columns').val(JSON.stringify(newColumnList));
    }

    console.log('grouping info: ', newGroupingList);
    if (newGroupingList.length == 0) {
        $('#grouping').val("");
    } else {
        $('#grouping').val(JSON.stringify(newGroupingList));
    }

    console.log('rowCols info: ', newRowColList);
    if (newRowColList.length == 0) {
        $('#rowCols').val("");
    } else {
        $('#rowCols').val(JSON.stringify(newRowColList));
    }

    console.log('rows info: ', newRowList);
    if (newRowList.length == 0) {
        $('#rows').val("");
    } else {
        $('#rows').val(JSON.stringify(newRowList));
    }

    if (numOfReassessListedness == 0) {
        $('#reassessListedness').val('');
    }

    setValidMeasureIndexList();

//    console.log('number of colMeas:', numColMeas);
//    $('#numColMeas').val(numColMeas);

    $.each(columnSetList, function (index, columns) {
        if (columns.length == 0) {
            $('#columns'+index).val("");
        } else {
            $('#columns'+index).val(JSON.stringify(columns));
        }
    });

    return true;
}