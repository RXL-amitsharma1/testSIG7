var currentPriority;
var changeToPriority;
var changePriorityRunning = false;

$(function () {

    $('.popover').css('pointer-events','auto');
    $(document).on('click', '.changePriority', setPriorityTriggerButton);
    $(document).on('click', '.changeToPriority', setChangeToPriorityTriggerButton);
    $(document).on('click', '.priorityChangeNotAllowed', showNotAllowedNotification);
    $('#priorityJustificationPopover #edit-box').hide();

    $('#priorityJustificationPopover .selectJustification').on('click', function (event) {
        if (event.which == 1) {
            var justification = $(this).html();
            initChangePriority(justification);
        }
    })

    $('#priorityJustificationPopover #addNewJustification, #priorityJustificationPopover .editIcon').on('click', function (e) {
        if ($(e.target).hasClass('editIcon')) {
            var data = unescapeHTML($(this).parent().siblings('.selectJustification').html());
            data = data.replaceAll("<br>", "\n");
            $('#priorityJustificationPopover .editedJustification').val(data);
        }
        $('#priorityJustificationPopover #addNewJustification').hide();
        $('#priorityJustificationPopover #edit-box').show();
        priorityEditPadding();
    });

    $('#priorityJustificationPopover #confirmJustification').on('click', function () {
        initChangePriority($('#priorityJustificationPopover .editedJustification').val());
    });

    $("#priorityJustificationPopover").on('hide.bs.modal', function () {
        clearAndHidePriorityJustificationEditBox();
    });

    $("#priorityJustificationPopover").on('shown.bs.modal', function () {
        setPopoverModalPosition($(this));
        $("#priorityJustificationPopover .popover-content").scrollTop(0);
        $('a:visible:first', this).focus();
    });

    $('#priorityJustificationPopover #cancelJustification').on('click', function () {
        clearAndHidePriorityJustificationEditBox();
    });
    $("#priorityPopover").on('shown.bs.modal', function () {
        $('a:visible:first', this).focus();
    });
});

var showNotAllowedNotification = function () {

    $.Notification.notify('warning', 'top right', "Warning", "You don't have access to change the priority for this product.", {autoHideDelay: 5000});
};
var clearAndHidePriorityJustificationEditBox = function () {
    $('#priorityJustificationPopover #edit-box').hide();
    $('#priorityJustificationPopover .editedJustification').val('');
    $('#priorityJustificationPopover #addNewJustification').show();
    priorityEditPadding();
};

var setPriorityJustificationPopOverTitle = function () {
    var fromPriority = $(currentPriority).parent().attr('title') ? $(currentPriority).parent().attr('title') : $(currentPriority).attr('title');
    var toPriority = $(changeToPriority).parent().data('priority') ? $(changeToPriority).parent().data('priority') : $(changeToPriority).data('priority');
    var title = "Priority Change from " + fromPriority + " to " + toPriority;
    $('#priorityJustificationPopover .popover-title').html(title);
};

var setPriorityTriggerButton = function (event) {
    if(typeof hasReviewerAccess !== "undefined" && !hasReviewerAccess){
        event.preventDefault();
        $.Notification.notify('warning', 'top right', "Warning", "You don't have access to perform this action", {autoHideDelay: 5000});
    } else {
        currentPriority = $(event.target);
        $("#priorityPopover ." + $(currentPriority).attr('class').split(' ').join('.')).parent().hide();
        $("#priorityPopover :not(." + $(currentPriority).attr('class').split(' ').join('.') + ")").parent().show();
    }
};

var setChangeToPriorityTriggerButton = function (event) {
    changeToPriority = $(event.target);
    if (!forceJustification) {
        initChangePriority();
    } else {
        if(availableAlertPriorityJustifications == emptyJustification){
            $('#priorityJustificationPopover').hide();
            initChangePriority();
        }else{
            $('#priorityJustificationPopover').show();
            setPriorityJustificationPopOverTitle();
        }
    }
};

var initChangePriority = function (justificationText) {
    var selectedRowCount;
    if (typeof isCaseDetailView !== "undefined" && isCaseDetailView == "true") {
        selectedRowCount = $('table#alertsDetailsTable .copy-select:checked').length;
    } else {
        selectedRowCount = $('.copy-select:checked').length;
    }
    if(selectedRowCount > 1 && $(currentPriority).closest('tr').find(".copy-select").prop("checked")){
        priorityBulkUpdate(selectedRowCount, justificationText);
    } else {
        var selectedRowIndex = $(currentPriority).closest('tr').index();
        if (isAbstractViewOrCaseView(selectedRowIndex)) {
            selectedRowIndex = selectedRowIndex / 2
        }
        var data = populatePriorityChangeData(selectedRowIndex);
        changePriority(justificationText, data, false);
    }
};

var changePriority = function(justificationText, data, isBulkUpdate) {
    data['newPriority.id'] = $(changeToPriority).parent().data('id');
    data['isArchived'] = isArchived;
    if (!data['newPriority.id']) {
        data['newPriority.id'] = $(changeToPriority).data('id')
    }
    if (justificationText) {
        data.justification = justificationText;
    }
    var currentPriorityParent = $(currentPriority).closest('td');
    var html;

    if(!changePriorityRunning) {
        $.ajax({
            url: changePriorityUrl,
            type: "POST",
            data: data,
            dataType: 'json',
            beforeSend: function () {
                changePriorityRunning = true;
                if ($(".signalHeaderPriority").length > 0) {
                    if (!currentPriorityParent.is('td')) {
                        currentPriorityParent = $('.signalHeaderPriority')
                    }
                } else {
                    if (!currentPriorityParent.is('td')) {
                        currentPriorityParent = $(currentPriority).closest('span')
                    }
                }
                html = $(currentPriorityParent).html();
                if (isBulkUpdate) {
                    $.each($('.copy-select:checked'), function () {
                        $(this).closest('td').siblings('td.priorityParent').html("<i id='priorityChangeProcessing' class='mdi mdi-spin mdi-loading'></i>");
                    })
                } else {
                    $(currentPriorityParent).html("<i id='priorityChangeProcessing' class='mdi mdi-spin mdi-loading'></i>");
                }
            },
        })
            .done(function (response) {
                $('#priorityJustificationPopover').modalPopover('hide');
                $('#priorityPopover').modalPopover('hide');
                if (response.status) {
                    var priority = $(changeToPriority).parent().data('priority');
                    html = signal.utils.render('priority', {
                        priorityValue: priority ? priority : $(changeToPriority).data('priority'),
                        priorityClass: priority ? $(changeToPriority).attr('class') : changeToPriority.context.children[0].className,
                        isPriorityChangeAllowed: true
                    });
                    if (isBulkUpdate) {
                        var updatedRowIndexes = new Set();
                        $.each($('.copy-select:checked'), function () {
                            $(this).closest('td').siblings('td.priorityParent').html(html);
                            var selectedRowIndex = $(this).closest('tr').index();
                            if (isAbstractViewOrCaseView(selectedRowIndex)) {
                                selectedRowIndex = selectedRowIndex / 2
                            }
                            updatedRowIndexes.add(selectedRowIndex);
                        });
                        if (response.data && response.data.dueIn != "null" && response.data.dueIn != null)
                            updateDueIn(updatedRowIndexes, response.data.dueIn);
                    } else {
                        $(currentPriorityParent).html(html);
                        var selectedRowIndex = $(currentPriorityParent).closest('tr').index();
                        if (isAbstractViewOrCaseView(selectedRowIndex)) {
                            selectedRowIndex = selectedRowIndex / 2
                        }
                        if (response.data && response.data.dueIn != "null" && response.data.dueIn != null)
                            updateDueIn([selectedRowIndex], response.data.dueIn);
                    }
                    $.Notification.notify('success', 'top right', "Success", "Priority changed successfully.", {autoHideDelay: 5000});
                    if (applicationLabel == APPLICATION_NAME.CASE_DETAIL) {
                        $("#caseHistoryModalTable").DataTable().ajax.reload();
                    }
                    if (applicationLabel == APPLICATION_LABEL.EVENT_DETAIL) {
                        $("#currentAlertHistoryModalTable").DataTable().ajax.reload();
                        $("#evdasHistoryModalTable").DataTable().ajax.reload();
                    }
                    $("#priority").val(data['newPriority.id']).trigger('change');
                    if ($(".workflowLink").length > 0) {
                        if (!currentPriorityParent.is('td')) {
                            $('.workflow-priority').html(html);
                        } else {
                            $('.signalHeaderPriority').html(html);
                        }
                        if (response.data != null && response.data.dueIn != "null" && response.data.dueIn != null) {
                            $('#dueIn').html(response.data.dueIn + " Days");
                            $('#dueInHeader').html(response.data.dueIn + " Days")
                            $('#signal-history-table select option[value="Due Date"]:selected').parent().parent().parent().find(".date-created").val(response.data.dueDate)
                            $('#signalActivityTable').DataTable().ajax.reload();
                            if (response.value == 1) {
                                $("#dueDatePicker").hide();
                                $(".editButtonEvent").show();
                            }
                        } else {
                            $('#dueIn').html('-');
                            $('#dueInHeader').html('-')
                            $("#dueDatePicker").hide();
                            $(".editButtonEvent").hide();
                        }
                    }
                    $('#alertsDetailsTable').DataTable().ajax.reload();
                    signal.activities_utils.reload_activity_table();
                    if(applicationName == APPLICATION_NAME.SIGNAL_MANAGEMENT){
                        updateSignalHistory();
                    }
                } else {
                    if (isBulkUpdate) {
                        table.columns.adjust().draw();
                    } else {
                        $(currentPriorityParent).html(html);
                    }
                    initChangePriority()
                    $.Notification.notify('error', 'top right', "Error", "Sorry we could not proceed with the request. Kindly contact the administrator.", {autoHideDelay: 5000});
                }
                $('#priorityJustificationPopover').modalPopover('hide');
                $('#priorityPopover').modalPopover('hide');

                disableDueDate()
                changePriorityRunning = false;
            })
            .fail(function () {
                if (currentPriorityParent) {
                    $(currentPriorityParent).html(html);
                }
                $.Notification.notify('error', 'top right', "Error", "Sorry we could not proceed with the request. Kindly contact the administrator.", {autoHideDelay: 5000});
                table.columns.adjust().draw();
            });
    }
};

var priorityBulkUpdate = function (totalSelectedRecords, justificationText) {
    var textToDisplay;
    switch (applicationName) {
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
                            var data = populatePriorityChangeData($(currentPriority).closest('tr').index());
                            changePriority(justificationText, data, false);
                            break;
                        case 'allSelected':
                            if (checkIfSafetyLeadOfAllSelectedRows()) {
                                var data = populatePriorityChangeData($(currentPriority).closest('tr').index(), true);
                                changePriority(justificationText, data, true);
                            } else {
                                $.Notification.notify('error', 'top right', "Error", "You must be safety lead for all the selected safety observation for changing the priority", {autoHideDelay: 5000});
                            }
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

var checkIfSafetyLeadOfAllSelectedRows = function () {
    var isSafetyLeadOfAll = true;
    $.each($('.copy-select:checked'), function(){
        if($(this).parent().siblings('td.priorityParent').find('a.priorityChangeNotAllowed').length){
            isSafetyLeadOfAll = false
        }
    });
    return isSafetyLeadOfAll
};

var populatePriorityChangeData = function (index, isBulkUpdate) {
    var data = {};
    var signalId = $('#signalIdPartner').val();
    if (isBulkUpdate) {
        //this is applicable for all review screens where we have dataTable and we are performing bulk update.
        var indexSet = new Set();
        var selectedRowData = [];
        var checkboxSelector;
        if (typeof isCaseDetailView !== "undefined" && isCaseDetailView == "true") {
            checkboxSelector = 'table#alertsDetailsTable .copy-select:checked';
        } else {
            checkboxSelector = '.copy-select:checked';
        }
        $.each($(checkboxSelector), function () {
            var selectedRoxIndex = $(this).closest('tr').index();
            if(isAbstractViewOrCaseView(selectedRoxIndex))
                selectedRoxIndex = selectedRoxIndex / 2;
            indexSet.add((selectedRoxIndex));
        });
        indexSet.forEach(function(num){
            selectedRowData.push(populatePriorityDataFromGrid(num))
        });
        data['selectedRows'] = JSON.stringify(selectedRowData);
    } else if (index > -1 && !signalId) {
        //this is applicable for all review screens where we have dataTable.
        data['selectedRows'] = JSON.stringify([populatePriorityDataFromGrid(index)]);
    } else {
        //this is applicable on the signal management screen and case detail screen.
        //data = populatePriorityDataFromOther(signalId);
        data['selectedRows'] = JSON.stringify([populatePriorityDataFromOther(signalId)]);
    }
    return data;
};

var populatePriorityDataFromGrid = function(index) {
    var data = {};
    var rowObject = table.rows(index).data()[0];
    data['configObj.id'] = rowObject.alertConfigId;
    data['executedConfigObj.id'] = rowObject.execConfigId;
    data['alert.id'] = rowObject.id;
    return data;
};

var populatePriorityDataFromOther = function(signalId) {
    var data = {};
    data['signal.id'] = signalId;
    data['configObj.id'] = $('#configId').html();
    data['executedConfigObj.id'] = $('#execConfigId').val();
    data['alert.id'] = $('#alertId').val();
    return data;
};
var priorityEditPadding = function() {
    var heightOfDiv = $('.popover.priority ul.text-list > li:last-child').height();
    var paddingBottom = heightOfDiv + 11 + 'px';
    $('.popover.priority ul.text-list').css('padding-bottom', paddingBottom);
    $('.editedJustification').focus();
};

var updateDueIn = function (updatedRowIndexes, updatedDueIn) {
    if(!$('#signalIdPartner').val()) {
        updatedRowIndexes.forEach(function (num) {
            if (num > -1) {
                var selector = "table#alertsDetailsTable tr:nth-child(" + (num + 1) + ") td.dueIn";
                var date1 = new Date(table.rows(num).data()[0].detectedDate);
                var alertId = table.rows(num).data()[0].id;
                var days = $(changeToPriority).parent().data('days');
                if(updatedDueIn!= null && updatedDueIn.length >0) {
                    days = updatedDueIn.filter(function (row) {
                        return row.id == alertId;
                    })[0].dueIn;
                }
                $(selector).html(signal.list_utils.due_in_comp(days));
            }
        });
    }
    $('#signalActivityTable').DataTable().ajax.reload();

};
