//= require purify/purify.min.js
var selectedDisposition;
var selectedSignal;
var hasJustifications;
var isDueDateEnabled=undefined;
var changeDispositionRunning = false;
var topOffsetJustificationPopover;
var revertDispositionElements;
var PAGESIZE = 20;

$(function () {
    var title = $(document).find("title").text();

    bindJustificationClickEvents();
    $(document).on("click",".view-alert-no-access",function (e) {
        e.preventDefault();
        $.Notification.notify('warning', 'top right', "Warning", "You don’t have access to view this alert", {autoHideDelay: 5000});
    });

    $("#dispositionJustificationPopover a.addNewJustification").hover(
        function () {

            $('.tooltip').not(this).hide();
        }, function () {
            $(this).find("div.box").remove(); // on hover out, finds the dynamic element and removes it.
            $('.tooltip').not(this).show();
        }
    );

    $('#dispositionJustificationPopover #edit-box').hide();
    $('#dispositionSignalPopover #new-signal-box').hide();
    $(document).on('click', '.changeDisposition', setDispositionTriggerButton);
    $(document).on('click', '.selectSignal', setSignalSelectTriggerButton);

    $("#dispositionJustificationPopover").on('hide.bs.modal', function () {
        selectedSignal = undefined;
        $("#dispositionJustificationPopover").removeClass('level2');
        clearAndHideNewSignalBox();
        clearAndHideDispositionJustificationEditBox();
    });

    $("#dispositionSignalPopover").on('hide.bs.modal', function () {
        selectedDisposition = undefined;
        selectedSignal = undefined;
        clearAndHideNewSignalBox();
        clearAndHideDispositionJustificationEditBox();
    });

    // Cleanup after hiding the modal (for PVS-71135)
    $('#dispositionSignalPopover').on('hide.bs.modal', function () {
        $('#dispositionJustificationPopover').hide();
        $('.tooltip').tooltip('hide');
        $('.modal-backdrop').remove();
        $('body').removeClass('modal-open');
    });

    $("#dispositionSignalPopover").on('show.bs.modal', function () {
        if(typeof hasSignalCreationAccessAccess !== "undefined" && !hasSignalCreationAccessAccess){
            $(this).addClass('hide');
        }
    });

    $("#dispositionJustificationPopover").on('shown.bs.modal', function () {
        var offsetLeft= $('#dispositionSignalPopover').width() + $('#dispositionSignalPopover').offset().left -50; //50 px is additional width.

        var parentThis= $(this);
        $("#createSignal").on('click', function () {
            if (parentThis.hasClass("right")) {
                parentThis.css("left",offsetLeft);
            }
        });
        $("#dispositionJustificationPopover .popover-content").scrollTop(0);
        var topOffset = parseInt($(this).offset().top);
        topOffsetJustificationPopover = topOffset;
        setPopoverModalPosition($(this));
        var thisX = $(this).css('left').replace('px', '');
        if (title != APPLICATION_LABEL.EVENT_DETAIL && title != APPLICATION_LABEL.CASE_DETAIL) {
            var totalX = $('.panel-heading').width();
            if ((totalX - thisX) < 375) {
                $(this).css('left', thisX - 225);
                $("#dispositionJustificationPopover .arrow").css("left", "95%");
            }
            //Added to fix bug-54159
            else if (parseInt(thisX) < 0) {
                $("#dispositionJustificationPopover .arrow").css({
                    "left": "none",
                    "right": "none",
                    "top": "none",
                    "transform" : "none"
                });

                $(this).css('left', offsetLeft);
                $("#dispositionJustificationPopover .arrow").css({
                    "left": "auto",
                    "right": "100%",
                    "transform": "rotate(180deg)",
                    "top": "50%"
                });
            }
            else {
                $("#dispositionJustificationPopover .arrow").css({
                    "left": "",
                    "right":"",
                    "top": "",
                    "transform" : ""
                });
                // Apply new styles
                $("#dispositionJustificationPopover .arrow").css({
                    "left": "50%"
                });
            }
        }
    });

    $("#undoDispositionJustificationPopover").on('shown.bs.modal', function () {
        $("#undoDispositionJustificationPopover .popover-content").scrollTop(0);
        setPopoverModalPosition($(this));
        var thisX = $(this).css('left').replace('px', '');
        if (title != APPLICATION_LABEL.EVENT_DETAIL && title != APPLICATION_LABEL.CASE_DETAIL) {
            var totalX = $('.panel-heading').width();
            if ((totalX - thisX) < 375) {
                $(this).css('left', thisX - 225);
                $("#undoDispositionJustificationPopover .arrow").css("left", "95%");
            } else {
                $("#undoDispositionJustificationPopover .arrow").css("left", "50%", "top","auto");
            }
        }
    });

    $('#undoDispositionJustificationPopover #cancelJustification').on('click', function () {
        $("#undoDispositionJustificationPopover").hide();
        clearAndHideUndoDispositionJustificationEditBox();

    });

    $("#dispositionSignalPopover").on('shown.bs.modal', function () {
        $("#dispositionJustificationPopover").hide();
        setPopoverModalPosition($(this));
        $("#dispositionSignalPopover .popover-content").scrollTop(0);
        if (title != APPLICATION_LABEL.EVENT_DETAIL && title != APPLICATION_LABEL.CASE_DETAIL) {
            var thisX = $(this).css('left').replace('px', '');
            var totalX = $('.panel-heading').width();
            if ((totalX - thisX) < 175) {
                $(this).css('left', thisX - 135);
                $("#dispositionSignalPopover .arrow").css("left", "95%");
            } else {
                $("#dispositionSignalPopover .arrow").css("left", "50%");
            }
        }
        $('a:visible:first', this).focus();
    });

    $('#dispositionSignalPopover #addNewSignal').on('click', function () {
        $('#dispositionJustificationPopover').modalPopover('hide');
        $('#dispositionSignalPopover #new-signal-box').show();
        $('#newSignalName').focus();
        $('#dispositionSignalPopover #addNewSignal').hide();
    });

    $('#dispositionJustificationPopover #cancelJustification').on('click', function () {
        clearAndHideDispositionJustificationEditBox();
    });

    $('#dispositionSignalPopover #cancelSignal').on('click', function () {
        $('#dispositionJustificationPopover').modalPopover('hide');
        clearAndHideNewSignalBox()
    });

    $('#dispositionSignalPopover #createSignal').on('click', function () {
        var signalName = $('#dispositionSignalPopover #newSignalName').val();
        if (signalName.trim()) {
            if (signalName.length > 255) {
                $.Notification.notify('error', 'top right', "Failed", $.i18n._('signalMaxSizeExceedError'), {autoHideDelay: 5000});
            } else {
                $('#dispositionSignalPopover #newSignalJustification').trigger('click');
            }
        }
    });

    $('#dispositionJustificationPopover #confirmJustification').on('click', function () {
        initChangeDisposition($('#dispositionJustificationPopover .editedJustification').val());
    });

    siglName();
    webUiPopInit1();
    enableMenuTooltipsPopup()
});

$(function() {
    var page = 1;
    var loading = false;
    var title = $(document).find("title").text();
    var searchTerm ="";
    var debounceTimer;

    // Function to load more signals
    function loadMoreSignals(reset = false) {
        if (loading) return;
        loading = true;

        // this part of code adds dynamic bottom padding to the signal list UL element, which is necessary for scroll behaviour
        var innerHeight = $('#new-signal-box-li').innerHeight();
        $('.disposition.popover ul.text-list').each(function() {
            this.style.setProperty('padding-bottom', innerHeight + 'px', 'important');
        });

        $('#signal-list-loader').show();
        var searchTerm = $('#signal-search')?.val()?.toLowerCase() ?? "";
        $.ajax({
            url: fetchSignalsUrl,
            method: 'GET',
            data: {
                page: page,
                searchTerm: searchTerm
            },
        }).done(function(data) {
            // Assuming 'data.items' is the JSON array containing signal objects
            if (reset) {
                $('#signal-list > li:not(:last-child)').remove();  // Clear previous list items if resetting
            }
            if (data.items && data.items.length > 0) {
                if (page > 1 && data.total_items < PAGESIZE) return;
                var signalDOMObjects = signal.utils.render('dispsignalPopoverListItem',{
                    signals: data.items
                });
                $('#signal-list > li:last').before(signalDOMObjects);
                if (title === APPLICATION_LABEL.EVENT_DETAIL || title === APPLICATION_LABEL.CASE_DETAIL) {
                    $('#signal-list  a').attr('data-placement', 'right');
                }
                enableMenuTooltipsPopup();
                page++;
                loading = false;
                $('#signal-list-loader').hide();
            } else {
                // this condition executed when all expected signals have been fetched
                $('#signal-list-loader').hide();
            }
        }).fail(function() {
            console.error('Failed to load more signals.');
            loading = false;
            $('#signal-list-loader').hide();
        });
    }

    // Detect scroll to bottom
    $('#dispositionSignalPopover .popover-content').on('scroll', function() {
        var dspc = $(this)[0];
        // This condition checks if the scroll bar has reached to the bottom
        if (dspc.scrollTop + dspc.clientHeight + 5 >= dspc.scrollHeight) {
            loadMoreSignals();
        }
    });

    // Load first batch of signals when popup is opened
    var dispositionSignalPopoverModal = $('#dispositionSignalPopover');
    dispositionSignalPopoverModal.on('shown.bs.modal', () => {
        loadMoreSignals();
    });

    // clearing the signal-list dropdown values when popup is closed
    dispositionSignalPopoverModal.on('hide.bs.modal', () => {
        $('#signal-list > li:not(:last-child)').remove();
        page = 1;
        loading = false;
    });

    // Adjust Justification modal's position
    var dispositionJustificationPopoverModal = $('#dispositionJustificationPopover .popover');
    dispositionJustificationPopoverModal.on('shown.bs.modal', () => {
        $(this).css({ "top": "50% !important", "left": "50% !important" });
    });

    // Load on Adding/Updating searchTerm
    $('#signal-search').on('keyup', function() {
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(function() {
            var newSearchTerm = $('#signal-search')?.val()?.toLowerCase() ?? "";
            if (newSearchTerm !== searchTerm) {
                searchTerm = newSearchTerm;
                page = 1;
                loading = false;
                $('#signal-list > li:not(:last-child)').remove();
                // Fetch new signals and reset the list
                loadMoreSignals(true);
            }
        }, 300);
    });
});

function setPopoverModalPosition(ele){
    enableMenuTooltipsPopup()
    var windowHeight = $(window).height();
    var topOffset = parseInt(ele.offset().top);
    var elemHeight = parseInt(ele.outerHeight());
    if(windowHeight < (topOffset+elemHeight)){
        ele.removeClass('bottom').addClass('top').css({'top': (topOffset-elemHeight)-40+'px'}) // 40 is popover arrow + button size
    } else{
        ele.removeClass('top').addClass('bottom').css({'top': topOffset+'px'})
    }
};

var clearAndHideDispositionJustificationEditBox = function () {
    $('#dispositionJustificationPopover #edit-box').hide();
    $('#dispositionJustificationPopover .editedJustification').val('');
    $('#dispositionJustificationPopover #addNewJustification').show();
    dispositionEditPadding();
};

var clearAndHideUndoDispositionJustificationEditBox = function () {
    $('#undoDispositionJustificationPopover #edit-box').hide();
    $('#undoDispositionJustificationPopover .editedJustification').val('');
    dispositionEditPadding();
};

var clearAndHideNewSignalBox = function () {
    $('#dispositionSignalPopover #new-signal-box').hide();
    $('#dispositionSignalPopover #newSignalName').val('');
    showHideAddNewSignal();
};
var setDispositionTriggerButton = function (event) {
    $("#signal-search").trigger( "click" );
    $("#signal-search").focus();
    if((typeof hasReviewerAccess !== "undefined" && !hasReviewerAccess) ||
        $(this).data("validated-confirmed") && typeof hasSignalCreationAccessAccess !== "undefined" && !hasSignalCreationAccessAccess){
        event.preventDefault();
        if($(this).data("validated-confirmed") && typeof hasSignalCreationAccessAccess !== "undefined" && !hasSignalCreationAccessAccess){
            $.Notification.notify('warning', 'top right', "Warning", "You don't have access to assign a signal", {autoHideDelay: 5000});
        } else {
            $.Notification.notify('warning', 'top right', "Warning", "You don't have access to perform this action", {autoHideDelay: 5000});
        }
    } else {
        selectedDisposition = $(event.target);

        var isValidatedConfirmed = $(selectedDisposition).parent().data('validated-confirmed') ? $(selectedDisposition).parent().data('validated-confirmed') : $(selectedDisposition).data('validated-confirmed');
        if (!forceJustification && !isValidatedConfirmed) {
            initChangeDisposition();
        } else if(forceJustification){
            setDispositionPopOverTitle(isValidatedConfirmed);
        }
        disableDueDate()
    }
};

var setDispositionPopOverTitle = function (isValidatedConfirmed) {
    showHideAddNewSignal();
    var fromState = $(selectedDisposition).closest('ul').data('current-disposition');
    var toState = $(selectedDisposition).parent().attr('title') ? $(selectedDisposition).parent().attr('title') : $(selectedDisposition).attr('title');
    if(toState===undefined){
        toState = $(selectedDisposition).parent().attr('data-original-title')
    }
    var title = '';
    if (typeof fromState === 'undefined') {
        title = "Alert Disposition Change to " + toState
    } else {
        title = "Change " + fromState + " to " + toState
    }
    populateDispositionJustificationPopover(isValidatedConfirmed);
    $('#dispositionJustificationPopover .popover-title').html(title);
    $('#dispositionJustificationPopover').removeClass('left');
    var ele = $("#dispositionJustificationPopover")
    var windowHeight = $(window).height();
    var topOffset = parseInt(ele.offset().top);
    var elemHeight = parseInt(ele.outerHeight());
    if(windowHeight < (topOffset+elemHeight)){
        ele.removeClass('bottom').addClass('top').css({'top': (topOffsetJustificationPopover-elemHeight)-50+'px'})
    }

};

var populateDispositionJustificationPopover = function (isValidatedConfirmed) {
    var signalWorkFlow = false;
    $('#dispositionJustificationPopover').hide();
    var targetDispositionId = $(selectedDisposition).parent().data('disposition-id') ? $(selectedDisposition).parent().data('disposition-id') : $(selectedDisposition).data('disposition-id');
    if (changeDispositionUrl.indexOf("validatedSignal") != -1)
        signalWorkFlow = true;
    $('.dynamicJustification').remove();
    $.ajax({
        type: "GET",
        url: '/signal/justification/fetchJustificationsForDisposition/' + targetDispositionId + '?signalWorkFlow=' + signalWorkFlow,
        async: false,
        dataType: 'json',
    })
        .done(function (result) {
            var listContent = "";
            $.each(result, function (index, element) {
                listContent += '<li class="dynamicJustification textPre">';
                listContent += '<a tabindex="0" href="javascript:void(0);" title="' + escapeAllHtml(element) + '" class="selectJustification text" data-ol-has-click-handler>' + escapeAllHtml(element) + '</a> ';
                listContent += '<a tabindex="0" href="javascript:void(0);" title="Edit" class="btn-edit"><i class="mdi mdi-pencil editIcon" data-ol-has-click-handler></i></a>';
                listContent += '</li>';
            });
            if (listContent == "" && !isValidatedConfirmed) {
                initChangeDisposition();
            } else if (!isValidatedConfirmed) {
                $('#dispositionJustificationPopover').show();
            } else if (listContent == "" && isValidatedConfirmed) {
                hasJustifications = false;
            } else {
                hasJustifications = true;
            }
            if(listContent!="") {
                $("#dispositionJustificationPopover .text-list").prepend(listContent);
            }
            $('a:visible:first', this).focus();
            bindJustificationClickEvents();
            disableDueDate()
        });
};

var setSignalSelectTriggerButton = function (event) {
    if(typeof hasSignalCreationAccessAccess !== "undefined" && !hasSignalCreationAccessAccess){
        event.preventDefault();
        $.Notification.notify('warning', 'top right', "Warning", "You don't have access to assign a signal", {autoHideDelay: 5000});
    } else {
        selectedSignal = $(this);
        $('#dispositionJustificationPopover').hide();
        $('#dispositionJustificationPopover').addClass('level2').removeClass('bottom');
        if (!forceJustification || !hasJustifications) {
            initChangeDisposition();
        } else if ($(selectedSignal).attr('id') !== "newSignalJustification") {
            clearAndHideNewSignalBox();
        } if(hasJustifications)
            $('#dispositionJustificationPopover').show();
    }
};

var initChangeDisposition = function (justificationText) {
    if ($(selectedDisposition).parent().data('bulk-disp-update')) {
        bindAlertLevelDisposition(justificationText);
        $('.disable').on('click', function () {
            $(this).prop('disabled',true);
        });
        return;
    }
    if ($(selectedDisposition).parent().data('auth-required')) {
        initiateAuthFlow(justificationText)
    } else {
        var selector;
        // Bug/PVS-39933: selectedRowCheck flag is introduced to check for bulk update scenario
        var selectedRowCheck;
        var selectedRowCount;
        if (typeof isCaseDetailView !== "undefined" && isCaseDetailView == "true") {
            selector = 'table#alertsDetailsTable tbody.detailsTableBody'
            selectedRowCheck = $(this).closest('tr').find('.copy-select').prop('checked');
        } else {
            selector = 'table#alertsDetailsTable tbody'
            selectedRowCheck = $('.copy-select:checked').length;
        }
        if (typeof selectedCases !== "undefined") {
            selectedRowCount = selectedCases.length;
        }
        var selectedRowIndex = $(selectedDisposition).closest('tr').index();
        if (selectedRowCount > 1 && (selectedRowCheck || $(selector + " > tr:nth(" + selectedRowIndex + ") .copy-select").prop("checked"))) {
            dispositionBulkUpdate(selectedRowCount, justificationText);
        } else {
            if (isAbstractViewOrCaseView(selectedRowIndex)) {
                selectedRowIndex = selectedRowIndex / 2
            }
            var data = populateDispositionChangeData(selectedRowIndex, false);
            changeDisposition(justificationText, data, false);
        }
    }
};

var initUndoDisposition = function (id, justificationText) {
    if(justificationText && justificationText !== "undefined") {
        let trimmedJustificationText = justificationText.trim();
        if (trimmedJustificationText.length == 0) {
            showEmptyJustificationErrorMessage();
            return
        }
    }
    revertDisposition(id, justificationText);
};

var bindAlertLevelDisposition = function (justificationText) {
    if(justificationText && justificationText !== "undefined") {
        let trimmedJustificationText = justificationText.trim();
        if (trimmedJustificationText.length == 0) {
            showEmptyJustificationErrorMessage();
            return
        }
    }
    var dataToSend = {
        justificationText: justificationText,
        'targetDisposition.id': $(selectedDisposition).parent().data('disposition-id'),
        isArchived: $('#isArchived').val(),
        'incomingDisposition': $(selectedDisposition).closest('ul').data('current-disposition')
    };
    if (applicationLabel === ALERT_CONFIG_TYPE.EVDAS_ALERT) {
        dataToSend['config.id'] = configId
    }
    if (applicationLabel !== ALERT_CONFIG_TYPE.LITERATURE_SEARCH_ALERT) {
        dataToSend['execConfig.id'] = executedConfigId
    }
    var rowsToChange = [];
    $.ajax({
        type: "POST",
        url: changeAlertLevelDispositionUrl,
        data: dataToSend,
        beforeSend: function () {
            $('.disable').on('click', function () {
                $(this).prop('disabled', true);
            });
            $('#bulkDispositionPopover').modalPopover('hide');
            $('#dispositionJustificationPopover').modalPopover('hide');
            $("table#alertsDetailsTable td.dispositionAction").each(function () {
                if (!$(this).find('ul').data('is-reviewed')) {
                    rowsToChange.push($(this));
                    $(this).html("<i id='priorityChangeProcessing' class='mdi mdi-spin mdi-loading'></i>");
                }
            });
        },
    })
        .done(function (response) {
            if (response.status) {
                var selectedDispositionParent = $(selectedDisposition).parent();
                if(response.data.removedDispositionNames){
                    var removedDispositionList = response.data.removedDispositionNames;
                    $(".disposition-ico").find('li').each(function (){
                        if(removedDispositionList.includes($(this).children("input").attr("value"))) {
                            $(this).remove();
                        }
                    });
                }
                changeDispositionFilters(selectedDispositionParent);
                $.Notification.notify('success', 'top right', "Success", response.message, {autoHideDelay: 5000});
            } else {
                $.Notification.notify('error', 'top right', "Error", response.message, {autoHideDelay: 5000});
                table.ajax.reload();
            }
            $('.disable').on('click', function () {
                $(this).prop('disabled', false);
            });
        })
        .fail(function () {
            $('.disable').on('click', function () {
                $(this).prop('disabled',false);
            });
        });
};

function adjustLastPageForLiteratureAndEvdas(isClosed) {
    var pageInfo = $('#alertsDetailsTable').DataTable().page.info();
    if (isClosed && pageInfo && (pageInfo.start >= (pageInfo.recordsDisplay - (selectedCases?.length ? selectedCases.length : 1)))) {
        var adjustedPage = 0
        $('#alertsDetailsTable').DataTable().page(adjustedPage);
    }
}

function adjustLastPageForICRAndAggregate(isClosed, alertIdSet) {
    var pageInfo = $('#alertsDetailsTable').DataTable().page.info();
    if (isClosed && pageInfo && (pageInfo.start >= (pageInfo.recordsDisplay - alertIdSet?.size))) {
        // Adjust the page number to maintain the current position after reloading
        var adjustedPage = 0; // Adjust page to 1st page
        $('#alertsDetailsTable').DataTable().page(adjustedPage);
    }
}

var changeDisposition = function (customJustification, data, isBulkUpdate) {
    if(selectedSignal!=undefined &&  selectedSignal.attr('isClosed')=='true'){
        var messageConfirm = "";
        if (applicationName === ALERT_CONFIG_TYPE.SINGLE_CASE_ALERT) {
            messageConfirm = "As this is a closed signal, please confirm if you would like to associate the Case(s) with this signal?"
        } else if (applicationName === APPLICATION_NAME.CASE_DETAIL) {
            messageConfirm = "As this is a closed signal, please confirm if you would like to associate the Case(s) with this signal?"
        } else if (applicationName === ALERT_CONFIG_TYPE.AGGREGATE_CASE_ALERT) {
            messageConfirm = "As this is a closed signal, please confirm if you would like to associate the PEC(s) with this signal?"
        } else if (applicationName === ALERT_CONFIG_TYPE.EVDAS_ALERT) {
            messageConfirm = "As this is a closed signal, please confirm if you would like to associate the PEC(s) with this signal?"
        } else if (applicationName === ALERT_CONFIG_TYPE.EVENT_DETAIL) {
            messageConfirm = "As this is a closed signal, please confirm if you would like to associate the PEC(s) with this signal?"
        } else if (applicationName === ALERT_CONFIG_TYPE.ADHOC_ALERT) {
            messageConfirm = "As this is a closed signal, please confirm if you would like to associate the Safety Observation(s) with this signal?"
        } else {
            messageConfirm = "As this is a closed signal, please confirm if you would like to associate the Article(s) with this signal?"
        }
        bootbox.confirm({
            message: messageConfirm,
            title: "Attach Closed Signal ",
            buttons: {
                confirm: {
                    label: 'Ok',
                    className: 'btn-primary'
                },
                cancel: {
                    label: 'Cancel',
                    className: 'btn-default'
                }
            },
            callback: function (result) {
                if (result) {
                    if (forceJustification) {
                        if(customJustification != undefined && customJustification.trim() == ''){
                            $.Notification.notify('warning', 'top right', "Warning", "Justification is mandatory" , {autoHideDelay: 5000});
                            return
                        }
                        data.justification = customJustification;
                    }
                    var isValidatedConfirmed = $(selectedDisposition).parent().data('validated-confirmed') ? $(selectedDisposition).parent().data('validated-confirmed') : $(selectedDisposition).data('validated-confirmed');
                    if (isValidatedConfirmed && selectedSignal) {
                        if ($(selectedSignal).attr('id') === "newSignalJustification") {
                            data['validatedSignalName'] = $('#dispositionSignalPopover #newSignalName').val();
                            availableSignalNameList.push($('#newSignalName').val())
                            var row1 = $(selectedDisposition).closest('tr');
                            var arr=[];
                            var productName='';
                            arr= JSON.parse(JSON.stringify(getProductJson(applicationName, row1)))[3]
                            for(var i=0;i<arr.length;i++)
                            {
                                if(arr[i].name!=undefined){
                                    productName=productName+arr[i].name+",";
                                }
                            }
                            if(productName.length>0){
                                productName=productName.substring(0,productName.lastIndexOf(","));
                                productName="("+productName+")";
                            }
                            productName= $('#newSignalName').val()+" "+productName;

                        } else {
                            data['validatedSignalName'] =$(selectedSignal).attr('signalName');
                            data['signalId'] =$(selectedSignal).attr('data-signal-id');
                            if($(selectedSignal).attr('signalName')==undefined){
                                data['validatedSignalName'] = $('#dispositionSignalPopover #newSignalName').val();
                            }
                        }
                        var row = $(selectedDisposition).closest('tr');
                        data.productJson = JSON.stringify(getProductJson(applicationName, row));
                        if (!data.productJson) {
                            data.productJson = JSON.stringify(getProductJson());
                        }
                    }
                    data['targetDisposition.id'] = $(selectedDisposition).parent().data('disposition-id');
                    if (!data['targetDisposition.id']) {
                        data['targetDisposition.id'] = $(selectedDisposition).data('disposition-id')
                    }
                    data['incomingDisposition'] = $(selectedDisposition).closest('ul').data('current-disposition');
                    data['isArchived'] = isArchived;

                    var currentDispositionParent = $(selectedDisposition).closest('td');
                    var selectedDispositionElement = $(selectedDisposition);
                    var html;
                    var dispositionChangeAllowedForSignalWorkflow = true;
                    var currentWorkflowState = $("#workflowHeader").text();
                    if (typeof enableSignalWorkflow != 'undefined' && enableSignalWorkflow) {
                        if ($.inArray(data['targetDisposition.id'].toString(), possibleDispositions[currentWorkflowState]) === -1) {
                            $.Notification.notify('warning', 'top right', "Warning", "The disposition change is not allowed for current workflow state", {autoHideDelay: 5000});
                            $("#dispositionJustificationPopover").hide()
                            dispositionChangeAllowedForSignalWorkflow = false;
                        } else {
                            dispositionChangeAllowedForSignalWorkflow = true;
                        }
                    }
                    if (dispositionChangeAllowedForSignalWorkflow && !changeDispositionRunning) {
                        $.ajax({
                            url: changeDispositionUrl,
                            type: "POST",
                            data: data,
                            dataType: 'json',
                            beforeSend: function () {
                                changeDispositionRunning = true;
                                $(".dispositionBulkUpdate").prop('disabled', true);
                                if (!$(currentDispositionParent).is('td')) {
                                    currentDispositionParent = selectedDispositionElement.closest('span.disposition');
                                }
                                html = $(currentDispositionParent).html();
                                if (isBulkUpdate) {
                                    $('#dispositionJustificationPopover').modalPopover('hide');
                                    $('#dispositionSignalPopover').modalPopover('hide');
                                    var checkboxSelector;
                                    if (typeof isCaseDetailView !== "undefined" && isCaseDetailView == "true") {
                                        checkboxSelector = 'table#alertsDetailsTable .copy-select:checked';
                                    } else {
                                        checkboxSelector = '.copy-select:checked';
                                    }
                                    $.each($(checkboxSelector), function () {
                                        $("table#alertsDetailsTable tr:nth-child(" + ($(this).closest('tr').index() + 1) + ") td.dispositionAction").html("<i id='priorityChangeProcessing' class='mdi mdi-spin mdi-loading'></i>");
                                    });
                                } else {
                                    $(currentDispositionParent).html("<i id='priorityChangeProcessing' class='mdi mdi-spin mdi-loading'></i>");
                                }
                            },
                        })
                            .done(function (response) {
                                $(".dispositionBulkUpdate").prop('disabled', false);
                                selectedCheckBoxes = []
                                if(!JSON.parse(validateSignalMapping(data['targetDisposition.id'])) && JSON.parse(mappingEnabled))
                                {
                                    if (currentDispositionParent) {
                                        $(currentDispositionParent).html(html);
                                    }
                                    $('#dispositionJustificationPopover').modalPopover('hide');
                                    dispositionChangeAllowedForSignalWorkflow = false;
                                    return
                                }
                                if (response.status) {
                                    var selectedRowIndexes = new Set();
                                    if (isBulkUpdate) {
                                        $.each($('.copy-select:checked'), function () {
                                            selectedRowIndexes.add($(this).closest('tr').index());
                                        });
                                    }

                                    if (!selectedRowIndexes.size) {
                                        if ($(currentDispositionParent).closest('tr').index() > -1) {
                                            selectedRowIndexes.add($(currentDispositionParent).closest('tr').index());
                                        }
                                    }
                                    if (response.data!=null && response.data.dueIn != null && response.data.dueIn != "null") {
                                        $('#dueIn').html(response.data.dueIn + " Days");
                                        $('#dueInHeader').html(response.data.dueIn + " Days")
                                        $("#dueDatePicker").hide();
                                        $(".editButtonEvent").show();
                                        $('#signal-history-table select option[value="Due Date"]:selected').parent().parent().parent().find(".date-created").val(response.data.dueDate)
                                        $('#signalActivityTable').DataTable().ajax.reload();
                                    }else{
                                        $('#dueIn').html("-");
                                        $('#dueInHeader').html("-");
                                        $("#dueDatePicker").hide();
                                        $(".editButtonEvent").hide();
                                    }
                                    if(selectedSignal!=undefined &&  $('#dispositionSignalPopover #newSignalName').val().length>0) {
                                        if (response.data.signal != null) {
                                            var valSignal=response.data.signal
                                            var sanitizedSignalName = DOMPurify.sanitize(valSignal.name)
                                            if (forceJustification) {
                                                addNewSignal = ' <li signalName="'+(sanitizedSignalName)+' '+(valSignal.products)+'">' +
                                                    '                            <a tabindex="0"  role="button" class="selectSignal text grid-menu-tooltip" ' +
                                                    '                               data-toggle="modal-popover" sid="'+valSignal.id+'" data-placement="left"  data-html="true" data-title="Detected Date: '+(valSignal.detectedDate)+'<br>Current Disposition: '+(valSignal.disposition)+'" signalName="'+sanitizedSignalName+'" data-signal-id="'+valSignal.id+'">' +
                                                    '                                <p> <span class="comments-space" >'+(sanitizedSignalName)+' '+(valSignal.products)+' '+(valSignal.detectedDate)+' '+(valSignal.disposition)+'</span> </p>' +
                                                    '                            </a></li>';
                                            } else {
                                                addNewSignal = ' <li signalName="'+(sanitizedSignalName)+' '+(valSignal.products)+'">' +
                                                    '                            <a tabindex="0"   data-target="#dispositionJustificationPopover"  role="button" class="selectSignal text grid-menu-tooltip" ' +
                                                    '                               data-toggle="modal-popover" sid="'+valSignal.id+'" data-placement="left"  data-html="true" data-title="Detected Date: '+(valSignal.detectedDate)+'<br>Current Disposition: '+(valSignal.disposition)+'" signalName="'+sanitizedSignalName+'" data-signal-id="'+valSignal.id+'">' +
                                                    '                                <p> <span class="comments-space" >'+truncateString((sanitizedSignalName)+(valSignal.products)+" "+(valSignal.detectedDate)+" "+(valSignal.disposition))+'</span> </p>' +
                                                    '                            </a></li>';
                                            }

                                            $('#signal-list').prepend(addNewSignal)
                                            enableMenuTooltipsPopup()
                                        }
                                    }
                                    var selectedDispositionParent = selectedDispositionElement.attr('title') ? selectedDispositionElement : selectedDispositionElement.parent();
                                    var isClosed = false;
                                    if (typeof reviewCompletedDispostionList != "undefined" && $.inArray($(selectedDispositionParent).attr('title'), reviewCompletedDispostionList) > -1) {
                                        isClosed = true
                                    }
                                    var updatedHtml = ""
                                    if(applicationLabel === ALERT_CONFIG_TYPE.SIGNAL_MANAGEMENT) {
                                        updatedHtml = signal.utils.render('disposition_signalManagement', {
                                            allowedDisposition: dispositionIncomingOutgoingMap[$(selectedDispositionParent).attr('title')],
                                            currentDisposition: $(selectedDispositionParent).attr('title'),
                                            forceJustification: forceJustification,
                                            isReviewed: isClosed,
                                            isValidationStateAchieved: $(selectedDispositionParent).data('validated-confirmed'),
                                            id: $(selectedDisposition).parent().data('disposition-id')
                                        });
                                    }
                                    else{
                                        updatedHtml = signal.utils.render('disposition_dss3', {
                                            allowedDisposition: dispositionIncomingOutgoingMap[$(selectedDispositionParent).attr('title')],
                                            currentDisposition: $(selectedDispositionParent).attr('title'),
                                            forceJustification: forceJustification,
                                            isReviewed: isClosed,
                                            isValidationStateAchieved: $(selectedDispositionParent).data('validated-confirmed'),
                                            id: $(selectedDisposition).parent().data('disposition-id')
                                        })
                                    }
                                    $("#eventDisposition").html($(selectedDispositionParent).attr('title'));
                                    $("#caseDisposition").html($(selectedDispositionParent).attr('title'));
                                    if ($(selectedDispositionParent).data('disposition-closed') == true) {
                                        $(currentDispositionParent).closest("tr").find('td.dueIn').html("-")
                                    }
                                    if (selectedRowIndexes.size && !$('#signalIdPartner').val()) {
                                        selectedRowIndexes.forEach(function (num) {
                                            var $row = $("table#alertsDetailsTable tbody tr:nth-child(" + (num + 1) + ")");
                                            if (applicationLabel === ALERT_CONFIG_TYPE.STATISTICAL_COMPARISON) {
                                                $row = $("table#statsTable tbody tr:nth-child(" + (num + 1) + ")");
                                            }
                                            $row.find("td.currentDisposition").html($(selectedDispositionParent).attr('title'));
                                            $row.find("td.dispositionAction").html(updatedHtml);
                                        });
                                    } else if($('#signalIdPartner').val()){
                                        $(currentDispositionParent).html(updatedHtml);
                                        $(".currentDispositionHead").find("h4").attr("title",$(selectedDispositionParent).attr('title'));
                                        $(".dispositionId").attr("data-id", data['targetDisposition.id'].toString());
                                        $(".currentDispositionHead").find("h4").text($(selectedDispositionParent).attr('title'))
                                        if($('#revertDisposition').css('cursor')=='not-allowed') {
                                            $('#revertDisposition').css({"display":"none"})
                                        }
                                        if($('#revertDisposition:visible').length == 0) {
                                            $(".currentDispositionHead").find("h4").after('<span href="#" id="revertDisposition" title="Undo Disposition Change" style="margin-left: 6px; margin-top: 2px" data-ol-has-click-handler="">\n' +
                                                '<span class="md md-undo m-r-10"></span>\n' +
                                                '</span>');
                                            $("#revertDisposition").on('click', function(){
                                                $("#undoableJustificationPopover").show();
                                                addCountBoxToInputField(8000, $("#undoableJustificationPopover").find('textarea'));
                                            });
                                        }
                                        revertDispositionElements = $('#revertDisposition');
                                        if (revertDispositionElements.length > 1) {
                                            revertDispositionElements.slice(1).remove();
                                        }
                                    } else {
                                        $(currentDispositionParent).html(updatedHtml);
                                        if ($(currentDispositionParent).is('span.disposition')) {
                                            // $(currentDispositionParent).closest('div').siblings('div.currentDisposition').find('h5').html($(selectedDispositionParent).attr('title'));
                                            $(".currentDispositionHead").find("h4").attr("title",$(selectedDispositionParent).attr('title'))
                                            $(".currentDispositionHead").find("h4").text($(selectedDispositionParent).attr('title'))
                                        } else {
                                            $(currentDispositionParent).siblings('td.currentDisposition').html($(selectedDispositionParent).attr('title'));
                                        }
                                    }
                                    $("#dispositionJustificationPopover").hide()
                                    var selectedDispositionTitle = selectedDispositionParent.attr('title') ? selectedDispositionParent.attr('title') : selectedDispositionParent.attr('data-original-title');
                                    selectedDispositionTitle = handleQuotesForJson(selectedDispositionTitle);
                                    var filterValues = [];
                                    var filterArray = {};
                                    $('.dynamic-filters').each(function (index) {
                                        if ($(this).is(':checked')) {
                                            filterValues.push($(this).val());
                                        }
                                        filterArray[$(this).val()]=$(this).is(':checked');
                                    });
                                    if (selectedDispositionTitle && $('.disposition-ico :input[value="' + selectedDispositionTitle + '"]').length <= 0) {

                                        if (selectedDispositionElement.parent().attr('data-disposition-closed') == "true" || selectedDispositionElement.parent().attr('data-is-reviewed') == "true") {
                                            isClosed = true
                                            filterArray[selectedDispositionTitle]= false
                                        } else {
                                            isClosed = false
                                            filterArray[selectedDispositionTitle] = true;
                                            filterValues.push(selectedDispositionTitle);
                                        }
                                        var sessionStorageKey = '';
                                        if (applicationName === ALERT_CONFIG_TYPE.SINGLE_CASE_ALERT) {
                                            sessionStorageKey = ALERTS_PREFIX.SCA
                                        } else if (applicationName === ALERT_CONFIG_TYPE.AGGREGATE_CASE_ALERT) {
                                            sessionStorageKey = ALERTS_PREFIX.AGG
                                        } else if (applicationName === ALERT_CONFIG_TYPE.EVDAS_ALERT) {
                                            sessionStorageKey = ALERTS_PREFIX.EV
                                        }
                                        sessionStorage.setItem(sessionStorageKey + "filters_store", JSON.stringify(filterArray));
                                        sessionStorage.setItem(sessionStorageKey + "filters_value", JSON.stringify(filterValues));


                                        var dynamicDispositionFilter = fetchDynamicDispositionFilterHtml(selectedDispositionTitle, isClosed);
                                        if (!(typeof callingScreen !=="undefined" && callingScreen == 'dashboard' && isClosed == true)) {
                                            $(".disposition-ico").append($(dynamicDispositionFilter));
                                        }

                                    }
                                    if(response.data.incomingDisposition && response.data.countOfPreviousDisposition===0){
                                        $(".disposition-ico").find('li').each(function (){
                                            if($(this).children("input").attr("value") === response.data.incomingDisposition ) {
                                                $(this).remove();
                                            }
                                        });
                                    }
                                    if (typeof table !== 'undefined' && ((response.data && response.data.attachedSignalData !== 'false') || $("table#alertsDetailsTable tbody tr:first td.currentDisposition").length)) {

                                        if(applicationName === "Ad-Hoc Alert"){
                                            assignedToData = [];
                                            table.ajax.reload();
                                        }
                                        prev_page = [];
                                        $(".select-all-check input#select-all").prop('checked', false);
                                    }
                                    $('#dispositionJustificationPopover').modalPopover('hide');
                                    $('#dispositionSignalPopover').modalPopover('hide');
                                    if (response.data && response.data.attachedSignalData !== 'false') {
                                        if (typeof availableSignalNameList !== 'undefined' && $.inArray(data['validatedSignalName'], availableSignalNameList) === -1) {
                                            availableSignalNameList.push(data['validatedSignalName'])
                                        }
                                        if (applicationName == "Signal Management") {
                                            if (response.data.isValidationDateAdded) {
                                                $.Notification.notify('success', 'top right', "Success", "Disposition changed successfully.", {autoHideDelay: 5000});
                                                setTimeout(function () {
                                                    $.Notification.notify('success', 'top right', "Success", "Validation Date added in Signal Workflow log section.", {autoHideDelay: 5000});
                                                }, 3000);
                                            }
                                            else{
                                                $.Notification.notify('success', 'top right', "Success", "Disposition changed successfully.", {autoHideDelay: 5000});
                                            }
                                        }
                                        else{
                                            $.Notification.notify('success', 'top right', "Success", "Disposition changed successfully.", {autoHideDelay: 5000});
                                            //Added for bulk disposition

                                            if (applicationName == ALERT_CONFIG_TYPE.EVDAS_ALERT || applicationName == ALERT_CONFIG_TYPE.LITERATURE_SEARCH_ALERT) {
                                                adjustLastPageForLiteratureAndEvdas(isClosed)
                                            }
                                            if (applicationName == ALERT_CONFIG_TYPE.AGGREGATE_CASE_ALERT || applicationName == ALERT_CONFIG_TYPE.SINGLE_CASE_ALERT) {
                                                adjustLastPageForICRAndAggregate(isClosed, alertIdSet)
                                            }
                                            selectedCases = [];
                                            selectedCasesInfo = [];
                                            if (applicationName != "EVDAS Alert" && applicationName != "Event Detail" && typeof alertIdSet != "undefined"){
                                                alertIdSet.clear();
                                            }
                                        }
                                    } else{
                                        $.Notification.notify('success', 'top right', "Success", "Disposition changed successfully.", {autoHideDelay: 5000});
                                        //Added for bulk disposition

                                        if (applicationName == ALERT_CONFIG_TYPE.EVDAS_ALERT || applicationName == ALERT_CONFIG_TYPE.LITERATURE_SEARCH_ALERT) {
                                            adjustLastPageForLiteratureAndEvdas(isClosed)
                                        }
                                        if (applicationName == ALERT_CONFIG_TYPE.AGGREGATE_CASE_ALERT || applicationName == ALERT_CONFIG_TYPE.SINGLE_CASE_ALERT) {
                                            adjustLastPageForICRAndAggregate(isClosed, alertIdSet)
                                        }
                                        selectedCases = [];
                                        selectedCasesInfo = [];
                                        if (applicationName != "EVDAS Alert" && applicationName != "Event Detail" ){
                                            alertIdSet.clear();
                                        }
                                    }
                                    if (typeof table !== 'undefined' && typeof (table.ajax) !== 'undefined') {
                                        if (applicationName == ALERT_CONFIG_TYPE.LITERATURE_SEARCH_ALERT)
                                            url = listConfigUrl + "?filters=" + filterValues.join(",")
                                        else {
                                            if (typeof listConfigUrl !== 'undefined' && filterValues.length > 0) {
                                                if(typeof isTempViewSelected !== 'undefined' && isTempViewSelected) {
                                                    url = listConfigUrl + "&isFilterRequest=true&filters=" + encodeURIComponent(JSON.stringify(filterValues)) + "&tempViewId=" + tempViewPresent;
                                                }
                                                else {
                                                    if(typeof $('#advanced-filter') !== 'undefined' && $('#advanced-filter').val() !== null && $('#advanced-filter').val() !== "undefined"){
                                                        advancedFilterChanged = true
                                                    }
                                                    url = listConfigUrl + "&isFilterRequest=true"+ '&advancedFilterChanged=' + advancedFilterChanged+"&filters=" + encodeURIComponent(JSON.stringify(filterValues));
                                                }
                                            } else if(typeof listConfigUrl !== 'undefined'){
                                                if(typeof isTempViewSelected !== 'undefined' && isTempViewSelected) {
                                                    url = listConfigUrl + "&tempViewId=" + tempViewPresent;
                                                }
                                                else {
                                                    url = listConfigUrl;
                                                }
                                            }
                                        }
                                        table.ajax.url(url).load(null,false);
                                        if(applicationName==='Single Case Alert' || applicationName == 'Aggregate Case Alert')
                                        {
                                            alertIdSet.clear()
                                        }

                                    }
                                    if (applicationLabel == APPLICATION_LABEL.CASE_DETAIL) {
                                        $("#caseHistoryModalTable").DataTable().ajax.reload();
                                    }
                                    if (applicationLabel == APPLICATION_LABEL.EVENT_DETAIL) {
                                        $("#currentAlertHistoryModalTable").DataTable().ajax.reload();
                                        $("#evdasHistoryModalTable").DataTable().ajax.reload();
                                    }
                                    if (applicationLabel === ALERT_CONFIG_TYPE.SIGNAL_MANAGEMENT) {
                                        updateSignalHistory()
                                    }
                                    updateSignalOutcome(false)
                                } else {
                                    if (currentDispositionParent) {
                                        $(currentDispositionParent).html(html);
                                    }
                                    $('#dispositionJustificationPopover').modalPopover('hide');
                                    $('#dispositionSignalPopover').modalPopover('hide');
                                    $.Notification.notify('error', 'top right', "Failed", response.message, {autoHideDelay: 5000});
                                }
                                $('#signalActivityTable').DataTable().ajax.reload();
                                caseJsonArrayInfo = [];
                                changeDispositionRunning = false;
                            })
                            .fail(function () {
                                $(".dispositionBulkUpdate").prop('disabled', false);
                                $.Notification.notify('error', 'top right', "Failed", 'Something Went Wrong, Please Contact Your Administrator.', {autoHideDelay: 5000});
                                if (currentDispositionParent) {
                                    $(currentDispositionParent).html(html);
                                }
                                changeDispositionRunning = false;
                            })
                    }
                } else {
                    $("#signal-search").trigger( "click" );
                    $("#signal-search").focus();
                    return ;
                }
            }
        });

    } else {
        if (forceJustification) {
            if(customJustification != undefined && customJustification.trim() == ''){
                $.Notification.notify('warning', 'top right', "Warning", "Justification is mandatory" , {autoHideDelay: 5000});
                return
            }
            data.justification = customJustification;
        }
        var isValidatedConfirmed = $(selectedDisposition).parent().data('validated-confirmed') ? $(selectedDisposition).parent().data('validated-confirmed') : $(selectedDisposition).data('validated-confirmed');
        if (isValidatedConfirmed && selectedSignal) {
            if ($(selectedSignal).attr('id') === "newSignalJustification") {
                data['validatedSignalName'] = $('#dispositionSignalPopover #newSignalName').val();
                availableSignalNameList.push($('#newSignalName').val())
                var row1 = $(selectedDisposition).closest('tr');
                var arr=[];
                var productName='';
                arr= JSON.parse(JSON.stringify(getProductJson(applicationName, row1)))[3]
                for(var i=0;i<arr.length;i++)
                {
                    if(arr[i].name!=undefined){
                        productName=productName+arr[i].name+",";
                    }
                }
                if(productName.length>0){
                    productName=productName.substring(0,productName.lastIndexOf(","));
                    productName="("+productName+")";
                }
                productName= $('#newSignalName').val()+" "+productName;

            } else {
                data['validatedSignalName'] =$(selectedSignal).attr('signalName');
                data['signalId'] =$(selectedSignal).attr('data-signal-id');
                if($(selectedSignal).attr('signalName')==undefined){
                    data['validatedSignalName'] = $('#dispositionSignalPopover #newSignalName').val();
                }
            }
            var row = $(selectedDisposition).closest('tr');
            data.productJson = JSON.stringify(getProductJson(applicationName, row));
            if (!data.productJson) {
                data.productJson = JSON.stringify(getProductJson());
            }
        }
        data['targetDisposition.id'] = $(selectedDisposition).parent().data('disposition-id');
        if (!data['targetDisposition.id']) {
            data['targetDisposition.id'] = $(selectedDisposition).data('disposition-id')
        }
        data['incomingDisposition'] = $(selectedDisposition).closest('ul').data('current-disposition');
        data['isArchived'] = isArchived;

        var currentDispositionParent = $(selectedDisposition).closest('td');
        var selectedDispositionElement = $(selectedDisposition);
        var html;
        var dispositionChangeAllowedForSignalWorkflow = true;
        var currentWorkflowState = $("#workflowHeader").text();
        if (typeof enableSignalWorkflow != 'undefined' && enableSignalWorkflow) {
            if ($.inArray(data['targetDisposition.id'].toString(), possibleDispositions[currentWorkflowState]) === -1) {
                $.Notification.notify('warning', 'top right', "Warning", "The disposition change is not allowed for current workflow state", {autoHideDelay: 5000});
                $("#dispositionJustificationPopover").hide()
                dispositionChangeAllowedForSignalWorkflow = false;
            } else {
                dispositionChangeAllowedForSignalWorkflow = true;
            }
        }
        if (dispositionChangeAllowedForSignalWorkflow && !changeDispositionRunning) {
            $.ajax({
                url: changeDispositionUrl,
                type: "POST",
                data: data,
                dataType: 'json',
                beforeSend: function () {
                    changeDispositionRunning = true;
                    $(".dispositionBulkUpdate").prop('disabled', true);
                    if (!$(currentDispositionParent).is('td')) {
                        currentDispositionParent = selectedDispositionElement.closest('span.disposition');
                    }
                    html = $(currentDispositionParent).html();
                    if (isBulkUpdate) {
                        $('#dispositionJustificationPopover').modalPopover('hide');
                        $('#dispositionSignalPopover').modalPopover('hide');
                        var checkboxSelector;
                        if (typeof isCaseDetailView !== "undefined" && isCaseDetailView == "true") {
                            checkboxSelector = 'table#alertsDetailsTable .copy-select:checked';
                        } else {
                            checkboxSelector = '.copy-select:checked';
                        }
                        $.each($(checkboxSelector), function () {
                            $("table#alertsDetailsTable tr:nth-child(" + ($(this).closest('tr').index() + 1) + ") td.dispositionAction").html("<i id='priorityChangeProcessing' class='mdi mdi-spin mdi-loading'></i>");
                        });
                    } else {
                        $(currentDispositionParent).html("<i id='priorityChangeProcessing' class='mdi mdi-spin mdi-loading'></i>");
                    }
                },
            })
                .done(function (response) {
                    $(".dispositionBulkUpdate").prop('disabled', false);
                    selectedCheckBoxes = []
                    if(!JSON.parse(validateSignalMapping(data['targetDisposition.id'])) && JSON.parse(mappingEnabled))
                    {
                        if (currentDispositionParent) {
                            $(currentDispositionParent).html(html);
                        }
                        $('#dispositionJustificationPopover').modalPopover('hide');
                        dispositionChangeAllowedForSignalWorkflow = false;
                        return
                    }
                    if (response.status) {
                        var selectedRowIndexes = new Set();
                        if (isBulkUpdate) {
                            $.each($('.copy-select:checked'), function () {
                                selectedRowIndexes.add($(this).closest('tr').index());
                            });
                        }

                        if (!selectedRowIndexes.size) {
                            if ($(currentDispositionParent).closest('tr').index() > -1) {
                                selectedRowIndexes.add($(currentDispositionParent).closest('tr').index());
                            }
                        }
                        if (response.data!=null && response.data.dueIn != null && response.data.dueIn != "null") {
                            $('#dueIn').html(response.data.dueIn + " Days");
                            $('#dueInHeader').html(response.data.dueIn + " Days")
                            $("#dueDatePicker").hide();
                            $(".editButtonEvent").show();
                            $('#signal-history-table select option[value="Due Date"]:selected').parent().parent().parent().find(".date-created").val(response.data.dueDate)
                            $('#signalActivityTable').DataTable().ajax.reload();
                        }else{
                            $('#dueIn').html("-");
                            $('#dueInHeader').html("-");
                            $("#dueDatePicker").hide();
                            $(".editButtonEvent").hide();
                        }
                        if(selectedSignal!=undefined &&  $('#dispositionSignalPopover #newSignalName').val().length>0) {
                            if (response.data.signal != null) {
                                var valSignal=response.data.signal
                                var sanitizedSignalName = DOMPurify.sanitize(valSignal.name)
                                if (forceJustification) {
                                    addNewSignal = ' <li signalName="'+(sanitizedSignalName)+' '+(valSignal.products)+'">' +
                                        '                            <a tabindex="0"  role="button" class="selectSignal text grid-menu-tooltip" ' +
                                        '                               data-toggle="modal-popover" sid="'+valSignal.id+'" data-placement="left"  data-html="true" data-title="Detected Date: '+(valSignal.detectedDate)+'<br>Current Disposition: '+(valSignal.disposition)+'" signalName="'+sanitizedSignalName+'" data-signal-id="'+valSignal.id+'">' +
                                        '                                <p> <span class="comments-space" >'+(sanitizedSignalName)+' '+(valSignal.products)+' '+(valSignal.detectedDate)+' '+(valSignal.disposition)+'</span> </p>' +
                                        '                            </a></li>';
                                } else {
                                    addNewSignal = ' <li signalName="'+(sanitizedSignalName)+' '+(valSignal.products)+'">' +
                                        '                            <a tabindex="0"   data-target="#dispositionJustificationPopover"  role="button" class="selectSignal text grid-menu-tooltip" ' +
                                        '                               data-toggle="modal-popover" sid="'+valSignal.id+'" data-placement="left"  data-html="true" data-title="Detected Date: '+(valSignal.detectedDate)+'<br>Current Disposition: '+(valSignal.disposition)+'" signalName="'+sanitizedSignalName+'" data-signal-id="'+valSignal.id+'">' +
                                        '                                <p> <span class="comments-space" >'+truncateString((sanitizedSignalName)+(valSignal.products)+" "+(valSignal.detectedDate)+" "+(valSignal.disposition))+'</span> </p>' +
                                        '                            </a></li>';
                                }

                                $('#signal-list').prepend(addNewSignal)
                                enableMenuTooltipsPopup()
                            }
                        }
                        var selectedDispositionParent = selectedDispositionElement.attr('title') ? selectedDispositionElement : selectedDispositionElement.parent();
                        var isClosed = false;
                        if (typeof reviewCompletedDispostionList != "undefined" && $.inArray($(selectedDispositionParent).attr('title'), reviewCompletedDispostionList) > -1) {
                            isClosed = true
                        }
                        var updatedHtml = ""
                        if(applicationLabel === ALERT_CONFIG_TYPE.SIGNAL_MANAGEMENT) {
                            if (JSON.parse(validationDateSynchingEnabled) && validatedDateDispositions.includes(data['targetDisposition.id'])) {
                                $("#detectedDate").val(moment.utc(new Date()).tz(serverTimeZone).format("DD-MMM-YYYY"));
                            }
                            updatedHtml = signal.utils.render('disposition_signalManagement', {
                                allowedDisposition: dispositionIncomingOutgoingMap[$(selectedDispositionParent).attr('title')],
                                currentDisposition: $(selectedDispositionParent).attr('title'),
                                forceJustification: forceJustification,
                                isReviewed: isClosed,
                                isValidationStateAchieved: $(selectedDispositionParent).data('validated-confirmed'),
                                id: $(selectedDisposition).parent().data('disposition-id')
                            });
                        }
                        else{
                            updatedHtml = signal.utils.render('disposition_dss3', {
                                allowedDisposition: dispositionIncomingOutgoingMap[$(selectedDispositionParent).attr('title')],
                                currentDisposition: $(selectedDispositionParent).attr('title'),
                                forceJustification: forceJustification,
                                isReviewed: isClosed,
                                isValidationStateAchieved: $(selectedDispositionParent).data('validated-confirmed'),
                                id: $(selectedDisposition).parent().data('disposition-id')
                            })
                        }
                        $("#eventDisposition").html($(selectedDispositionParent).attr('title'));
                        $("#caseDisposition").html($(selectedDispositionParent).attr('title'));
                        if ($(selectedDispositionParent).data('disposition-closed') == true) {
                            $(currentDispositionParent).closest("tr").find('td.dueIn').html("-")
                        }
                        if (selectedRowIndexes.size && !$('#signalIdPartner').val()) {
                            selectedRowIndexes.forEach(function (num) {
                                var $row = $("table#alertsDetailsTable tbody tr:nth-child(" + (num + 1) + ")");
                                if (applicationLabel === ALERT_CONFIG_TYPE.STATISTICAL_COMPARISON) {
                                    $row = $("table#statsTable tbody tr:nth-child(" + (num + 1) + ")");
                                }
                                $row.find("td.currentDisposition").html($(selectedDispositionParent).attr('title'));
                                $row.find("td.dispositionAction").html(updatedHtml);
                            });
                        } else if($('#signalIdPartner').val()){
                            $(currentDispositionParent).html(updatedHtml);
                            $(".currentDispositionHead").find("h4").attr("title",$(selectedDispositionParent).attr('title'));
                            $(".dispositionId").attr("data-id", data['targetDisposition.id'].toString());
                            $(".currentDispositionHead").find("h4").text($(selectedDispositionParent).attr('title'))
                            if($('#revertDisposition').css('cursor')=='not-allowed') {
                                $('#revertDisposition').css({"display":"none"})
                            }
                            if($('#revertDisposition:visible').length == 0) {
                                $(".currentDispositionHead").find("h4").after('<span href="#" id="revertDisposition" title="Undo Disposition Change" style="margin-left: 6px; margin-top: 2px" data-ol-has-click-handler="">\n' +
                                    '<span class="md md-undo m-r-10"></span>\n' +
                                    '</span>');
                                $("#revertDisposition").on('click', function(){
                                    $("#undoableJustificationPopover").show();
                                    addCountBoxToInputField(8000, $("#undoableJustificationPopover").find('textarea'));
                                });
                            }
                            revertDispositionElements = $('.revert-icon #revertDisposition');
                            if (revertDispositionElements.length > 1) {
                                revertDispositionElements.slice(1).remove();
                            }
                        } else {
                            $(currentDispositionParent).html(updatedHtml);
                            if ($(currentDispositionParent).is('span.disposition')) {
                                // $(currentDispositionParent).closest('div').siblings('div.currentDisposition').find('h5').html($(selectedDispositionParent).attr('title'));
                                $(".currentDispositionHead").find("h4").attr("title",$(selectedDispositionParent).attr('title'))
                                $(".currentDispositionHead").find("h4").text($(selectedDispositionParent).attr('title'))
                            } else {
                                $(currentDispositionParent).siblings('td.currentDisposition').html($(selectedDispositionParent).attr('title'));
                            }
                        }
                        $("#dispositionJustificationPopover").hide()
                        var selectedDispositionTitle = selectedDispositionParent.attr('title')?selectedDispositionParent.attr('title'):selectedDispositionParent.attr('data-original-title');
                        var filterValues = [];
                        var filterArray = {};
                        $('.dynamic-filters').each(function (index) {
                            if ($(this).is(':checked')) {
                                filterValues.push($(this).val());
                            }
                            filterArray[$(this).val()]=$(this).is(':checked');
                        });
                        var escapedSearchValue = selectedDispositionTitle.replace(/[!"#$%&'()*+,.\/:;<=>?@[\\\]^`{|}~]/g, "\\$&");
                        var selector = '.disposition-ico :input[value="' + escapedSearchValue + '"]';
                        if (selectedDispositionTitle && $(selector).length <= 0) {

                            if (selectedDispositionElement.parent().attr('data-disposition-closed') == "true" || selectedDispositionElement.parent().attr('data-is-reviewed') == "true") {
                                isClosed = true
                                filterArray[selectedDispositionTitle]= false
                            } else {
                                isClosed = false
                                filterArray[selectedDispositionTitle] = true;
                                filterValues.push(selectedDispositionTitle);
                            }
                            var sessionStorageKey = '';
                            if (applicationName === ALERT_CONFIG_TYPE.SINGLE_CASE_ALERT) {
                                sessionStorageKey = ALERTS_PREFIX.SCA
                            } else if (applicationName === ALERT_CONFIG_TYPE.AGGREGATE_CASE_ALERT) {
                                sessionStorageKey = ALERTS_PREFIX.AGG
                            } else if (applicationName === ALERT_CONFIG_TYPE.EVDAS_ALERT) {
                                sessionStorageKey = ALERTS_PREFIX.EV
                            }
                            sessionStorage.setItem(sessionStorageKey + "filters_store", JSON.stringify(filterArray));
                            sessionStorage.setItem(sessionStorageKey + "filters_value", JSON.stringify(filterValues));


                            var dynamicDispositionFilter = fetchDynamicDispositionFilterHtml(selectedDispositionTitle, isClosed);
                            if (!(typeof callingScreen !=="undefined" && callingScreen == 'dashboard' && isClosed == true)) {
                                $(".disposition-ico").append($(dynamicDispositionFilter));
                            }

                        }
                        if(response.data.incomingDisposition && response.data.countOfPreviousDisposition===0){
                            $(".disposition-ico").find('li').each(function (){
                                if($(this).children("input").attr("value") === response.data.incomingDisposition ) {
                                    $(this).remove();
                                }
                            });
                        }
                        if (typeof table !== 'undefined' && ((response.data && response.data.attachedSignalData !== 'false') || $("table#alertsDetailsTable tbody tr:first td.currentDisposition").length)) {

                            if(applicationName === "Ad-Hoc Alert"){
                                assignedToData = [];
                                table.ajax.reload();
                            }
                            prev_page = [];
                            $(".select-all-check input#select-all").prop('checked', false);
                        }
                        $('#dispositionJustificationPopover').modalPopover('hide');
                        $('#dispositionSignalPopover').modalPopover('hide');
                        if (response.data && response.data.attachedSignalData !== 'false') {
                            if (typeof availableSignalNameList !== 'undefined' && $.inArray(data['validatedSignalName'], availableSignalNameList) === -1) {
                                availableSignalNameList.push(data['validatedSignalName'])
                            }
                            if (applicationName == "Signal Management") {
                                if (response.data.isValidationDateAdded) {
                                    $.Notification.notify('success', 'top right', "Success", "Disposition changed successfully.", {autoHideDelay: 5000});
                                    setTimeout(function () {
                                        if (defaultValidatedDate !== null && defaultValidatedDate !== "" && typeof defaultValidatedDate !== "undefined") {
                                            $.Notification.notify('success', 'top right', "Success", defaultValidatedDate + "  in Signal Workflow log section in Signal Workflow log section.", {autoHideDelay: 5000});
                                        } else {
                                            $.Notification.notify('success', 'top right', "Success", "Validation Date  in Signal Workflow log section in Signal Workflow log section.", {autoHideDelay: 5000});
                                        }
                                    }, 3000);
                                }
                                else{
                                    $.Notification.notify('success', 'top right', "Success", "Disposition changed successfully.", {autoHideDelay: 5000});
                                }
                            }
                            else{
                                $.Notification.notify('success', 'top right', "Success", "Disposition changed successfully.", {autoHideDelay: 5000});
                                if (applicationName == ALERT_CONFIG_TYPE.EVDAS_ALERT || applicationName == ALERT_CONFIG_TYPE.LITERATURE_SEARCH_ALERT) {
                                    adjustLastPageForLiteratureAndEvdas(isClosed)
                                }
                                if (applicationName == ALERT_CONFIG_TYPE.AGGREGATE_CASE_ALERT || applicationName == ALERT_CONFIG_TYPE.SINGLE_CASE_ALERT) {
                                    adjustLastPageForICRAndAggregate(isClosed, alertIdSet)
                                }
                                selectedCases = [];
                                selectedCasesInfo = [];
                                if (applicationName != "EVDAS Alert" && applicationName != "Event Detail" && typeof alertIdSet != "undefined"){
                                    alertIdSet.clear();
                                }
                            }
                        } else{
                            $.Notification.notify('success', 'top right', "Success", "Disposition changed successfully.", {autoHideDelay: 5000});
                            //Added for bulk disposition
                            if (applicationName == ALERT_CONFIG_TYPE.EVDAS_ALERT || applicationName == ALERT_CONFIG_TYPE.LITERATURE_SEARCH_ALERT) {
                                adjustLastPageForLiteratureAndEvdas(isClosed)
                            }
                            if (applicationName == ALERT_CONFIG_TYPE.AGGREGATE_CASE_ALERT || applicationName == ALERT_CONFIG_TYPE.SINGLE_CASE_ALERT) {
                                adjustLastPageForICRAndAggregate(isClosed, alertIdSet)
                            }
                            selectedCases = [];
                            selectedCasesInfo = [];
                            if (applicationName != "EVDAS Alert" && applicationName != "Event Detail" ){
                                alertIdSet.clear();
                            }
                        }
                        if (typeof table !== 'undefined' && typeof (table.ajax) !== 'undefined') {
                            if (applicationName == ALERT_CONFIG_TYPE.LITERATURE_SEARCH_ALERT)
                                url = listConfigUrl + "?filters=" + filterValues.join(",")
                            else {
                                if (typeof listConfigUrl !== 'undefined' && filterValues.length > 0) {
                                    if(typeof isTempViewSelected !== 'undefined' && isTempViewSelected) {
                                        url = listConfigUrl + "&isFilterRequest=true&filters=" + encodeURIComponent(JSON.stringify(filterValues)) + "&tempViewId=" + tempViewPresent;
                                    }
                                    else {
                                        if(typeof $('#advanced-filter') !== 'undefined' && $('#advanced-filter').val() !== null && $('#advanced-filter').val() !== "undefined"){
                                            advancedFilterChanged = true
                                        }
                                        url = listConfigUrl + "&isFilterRequest=true"+ '&advancedFilterChanged=' + advancedFilterChanged+"&filters=" + encodeURIComponent(JSON.stringify(filterValues));
                                    }
                                } else if(typeof listConfigUrl !== 'undefined'){
                                    if(typeof isTempViewSelected !== 'undefined' && isTempViewSelected) {
                                        url = listConfigUrl + "&tempViewId=" + tempViewPresent;
                                    }
                                    else {
                                        url = listConfigUrl;
                                    }
                                }
                            }
                            table.ajax.url(url).load(null,false);
                            if(applicationName==='Single Case Alert' || applicationName == 'Aggregate Case Alert')
                            {
                                alertIdSet.clear()
                            }

                        }
                        if (applicationLabel == APPLICATION_LABEL.CASE_DETAIL) {
                            $("#caseHistoryModalTable").DataTable().ajax.reload();
                        }
                        if (applicationLabel == APPLICATION_LABEL.EVENT_DETAIL) {
                            $("#currentAlertHistoryModalTable").DataTable().ajax.reload();
                            $("#evdasHistoryModalTable").DataTable().ajax.reload();
                        }
                        if (applicationLabel === ALERT_CONFIG_TYPE.SIGNAL_MANAGEMENT) {
                            updateSignalHistory()
                        }
                        updateSignalOutcome(false)
                    } else {
                        if (currentDispositionParent) {
                            $(currentDispositionParent).html(html);
                        }
                        $('#dispositionJustificationPopover').modalPopover('hide');
                        $('#dispositionSignalPopover').modalPopover('hide');
                        $.Notification.notify('error', 'top right', "Failed", response.message, {autoHideDelay: 5000});
                    }
                    $('#signalActivityTable').DataTable().ajax.reload();
                    if (applicationName == "Signal Management" && JSON.parse($("#showUndoIconButton").val())) {
                        $('.revertDisposition').show();
                    }
                    caseJsonArrayInfo = [];
                    changeDispositionRunning = false;
                })
                .fail(function () {
                    $(".dispositionBulkUpdate").prop('disabled', false);
                    $.Notification.notify('error', 'top right', "Failed", 'Something Went Wrong, Please Contact Your Administrator.', {autoHideDelay: 5000});
                    if (currentDispositionParent) {
                        $(currentDispositionParent).html(html);
                    }
                    changeDispositionRunning = false;
                })
        }
    }

};

var revertDisposition = function (id, customJustification) {


    var currentDispositionParent = $(selectedDisposition).closest('td');
    var html;
    var dispositionChangeAllowedForSignalWorkflow = true;
    var revertConfirmOptionButton = $("#confirmUndoJustification");
    var revertCancelOptionButton = $("#cancelUndoJustification");
    var dropdownMenu;
    disableUndoDispIcons(revertConfirmOptionButton, revertCancelOptionButton);
    if (dispositionChangeAllowedForSignalWorkflow) {
        $.ajax({
            url: revertDispositionUrl,
            type: "POST",
            data: {'id': id, 'justification': customJustification},
        })
            .done(function (response) {
                if(applicationName === ALERT_CONFIG_TYPE.ADHOC_ALERT  && dropdownMenu){
                    dropdownMenu.detach();
                }
                if (response.status) {
                    if (response.data!=null) {
                        // If 'dueIn' is 0 or a truthy value, append " Days" to it; otherwise, set it to "-".
                        const dueInStr = (response.data.dueIn === 0 || response.data.dueIn)
                            ? (response.data.dueIn + " Days")
                            : "-";
                        $('#dueIn').html(dueInStr);
                        $('#dueInHeader').html(dueInStr)
                        $("#dueDatePicker").hide();
                        if (response.data.dueIn != null && dueInStr !== "-") {
                            $(".editButtonEvent").show();
                        } else {
                            $(".editButtonEvent").hide();
                        }
                        $('#signal-history-table select option[value="Due Date"]:selected').parent().parent().parent().find(".date-created").val(response.data.dueDate)
                        $('#signalActivityTable').DataTable().ajax.reload();
                    }else{
                        $("#dueDatePicker").hide();
                        $(".editButtonEvent").hide();
                    }
                    if (applicationLabel === ALERT_CONFIG_TYPE.SIGNAL_MANAGEMENT) {
                        updateSignalHistory();
                    }
                    var selectedDispositionParent = response.data.targetDisposition;
                    var isClosed = false;
                    if (typeof reviewCompletedDispostionList != "undefined" && $.inArray(selectedDispositionParent, reviewCompletedDispostionList) > -1) {
                        isClosed = true
                    }
                    var updatedHtml = ""
                    if(applicationLabel === ALERT_CONFIG_TYPE.SIGNAL_MANAGEMENT) {
                        let escapedSelectedDispositionParent = selectedDispositionParent.replace(/[!"#$%&'()*+,.\/:;<=>?@[\\\]^`{|}~]/g, "\\$&");
                        updatedHtml = signal.utils.render('disposition_signalManagement', {
                            allowedDisposition: dispositionIncomingOutgoingMap[response.data.targetDisposition],
                            currentDisposition: response.data.targetDisposition,
                            forceJustification: forceJustification,
                            isReviewed: isClosed,
                            isValidationStateAchieved: $(escapedSelectedDispositionParent).data('validated-confirmed')
                        });
                    }

                    if($('#signalIdPartner').val()){
                        $('.currentDispOptions').html(updatedHtml);
                        $(".currentDispositionHead").find("h4").attr("title",response.data.targetDisposition);
                        $(".dispositionId").attr("data-id", response.data.alertDueDateList[0]?.id);
                        $(".currentDispositionHead").find("h4").text(response.data.targetDisposition)
                    } else {
                        $(currentDispositionParent).html(updatedHtml);
                        if ($(currentDispositionParent).is('span.disposition')) {
                            $(".currentDispositionHead").find("h4").attr("title",response.data.targetDisposition)
                            $(".currentDispositionHead").find("h4").text(response.data.targetDisposition)
                        } else {
                            if(typeof selectedDispositionParent !=="undefined"){
                                var escapedDispositionParent = selectedDispositionParent.replace(/[!"#$%&'()*+,.\/:;<=>?@[\\\]^`{|}~]/g, "\\$&");
                                $(currentDispositionParent).siblings('td.currentDisposition').html($(`[title="${escapedDispositionParent}"]`));
                            } else {
                                $(currentDispositionParent).siblings('td.currentDisposition').html($(selectedDispositionParent).attr('title'));
                            }
                        }
                    }
                    var selectedDispositionTitle = response.data.targetDisposition;
                    selectedDispositionTitle = handleQuotesForJson(selectedDispositionTitle);
                    var escapedSelectedDispositionTitle;
                    if(typeof selectedDispositionTitle !=="undefined"){
                        escapedSelectedDispositionTitle = escapeSpecialCharactersInId(selectedDispositionTitle);
                    } else {
                        escapedSelectedDispositionTitle = selectedDispositionTitle;
                    }
                    var filterValues = [];
                    var filterArray = {};
                    $('.dynamic-filters').each(function (index) {
                        if ($(this).is(':checked')) {
                            filterValues.push($(this).val());
                        }
                        filterArray[$(this).val()]=$(this).is(':checked');
                    });
                    $('#revertDisposition').hide();
                    if (escapedSelectedDispositionTitle && $('.disposition-ico :input[value="' + escapedSelectedDispositionTitle + '"]').length <= 0) {
                        if (dispositionData[selectedDispositionTitle].closed == "true" || dispositionData[selectedDispositionTitle].reviewCompleted == "true") {
                            isClosed = true
                            filterArray[selectedDispositionTitle]= false
                        } else {
                            isClosed = false
                            filterArray[selectedDispositionTitle] = true;
                            filterValues.push(selectedDispositionTitle);
                        }
                        var sessionStorageKey = '';
                        if (applicationName === ALERT_CONFIG_TYPE.SINGLE_CASE_ALERT) {
                            sessionStorageKey = ALERTS_PREFIX.SCA
                        } else if (applicationName === ALERT_CONFIG_TYPE.AGGREGATE_CASE_ALERT) {
                            sessionStorageKey = ALERTS_PREFIX.AGG
                        } else if (applicationName === ALERT_CONFIG_TYPE.EVDAS_ALERT) {
                            sessionStorageKey = ALERTS_PREFIX.EV
                        }
                        sessionStorage.setItem(sessionStorageKey + "filters_store", JSON.stringify(filterArray));
                        sessionStorage.setItem(sessionStorageKey + "filters_value", JSON.stringify(filterValues));


                        var dynamicDispositionFilter = fetchDynamicDispositionFilterHtml(escapedSelectedDispositionTitle, isClosed);
                        if (!(typeof callingScreen !=="undefined" && callingScreen == 'dashboard' && isClosed == true)) {
                            $(".disposition-ico").append($(dynamicDispositionFilter));
                        }

                    }
                    if(response.data.incomingDisposition && response.data.countOfPreviousDisposition===0){
                        $(".disposition-ico").find('li').each(function (){
                            if($(this).children("input").attr("value") === response.data.incomingDisposition ) {
                                $(this).remove();
                            }
                        });
                    }
                    if (typeof table !== 'undefined' && ((response.data && response.data.attachedSignalData !== 'false') || $("table#alertsDetailsTable tbody tr:first td.currentDisposition").length)) {

                        if(applicationName === "Ad-Hoc Alert"){
                            assignedToData = [];
                            table.ajax.reload();
                        }
                        prev_page = [];
                        $(".select-all-check input#select-all").prop('checked', false);
                    }

                    if (response.data && response.data.attachedSignalData !== 'false') {
                        if (applicationName == "Signal Management") {
                            $.Notification.notify('success', 'top right', "Success", "Undo action is successful.", {autoHideDelay: 5000});
                        }
                        else{
                            $.Notification.notify('success', 'top right', "Success", "Undo action is successful.", {autoHideDelay: 5000});
                            selectedCases = [];
                            selectedCasesInfo = [];
                            if (applicationName != "EVDAS Alert" && applicationName != "Event Detail" ){
                                if(typeof alertIdSet !=="undefined"){
                                    alertIdSet.clear();
                                }
                            }
                        }
                    } else{
                        $.Notification.notify('success', 'top right', "Success", "Undo action is successful.", {autoHideDelay: 5000});
                        selectedCases = [];
                        selectedCasesInfo = [];
                        if (applicationName != "EVDAS Alert" && applicationName != "Event Detail" ){
                            alertIdSet.clear();
                        }
                    }
                    if (typeof table !== 'undefined' && typeof (table.ajax) !== 'undefined') {
                        if (applicationName == ALERT_CONFIG_TYPE.LITERATURE_SEARCH_ALERT)
                            url = listConfigUrl + "?filters=" + encodeURIComponent(JSON.stringify(filterValues))
                        else {
                            if (typeof listConfigUrl !== 'undefined' && filterValues.length > 0) {
                                if(typeof isTempViewSelected !== 'undefined' && isTempViewSelected) {
                                    url = listConfigUrl + "&isFilterRequest=true&filters=" + encodeURIComponent(JSON.stringify(filterValues)) + "&tempViewId=" + tempViewPresent;
                                }
                                else {
                                    url = listConfigUrl + "&isFilterRequest=true"+ '&advancedFilterChanged=' + advancedFilterChanged+"&filters=" + encodeURIComponent(JSON.stringify(filterValues));
                                }
                            } else if(typeof listConfigUrl !== 'undefined'){
                                if(typeof isTempViewSelected !== 'undefined' && isTempViewSelected) {
                                    url = listConfigUrl + "&tempViewId=" + tempViewPresent;
                                }
                                else {
                                    url = listConfigUrl;
                                }
                            }
                        }
                        table.columns.adjust();
                        table.ajax.url(url).load(null,false);
                        if(applicationName==='Single Case Alert' || applicationName === 'Aggregate Case Alert')
                        {
                            alertIdSet.clear()
                        }
                    }
                    updateSignalOutcome(false)
                } else {
                    if (currentDispositionParent) {
                        $(currentDispositionParent).html(html);
                    }
                    $.Notification.notify('error', 'top right', "Failed", response.message, {autoHideDelay: 5000});
                }
                enableUndoDispIcons(revertConfirmOptionButton, revertCancelOptionButton);
                $('#signalActivityTable').DataTable().ajax.reload();

            })
            .fail(function () {
                if (currentDispositionParent) {
                    $(currentDispositionParent).html(html);
                }
            })
    }
};

var enableUndoDispIcons = function (confirmIconSelector, cancelIconSelector) {
    //buttons released from disablity since ajax call completed
    confirmIconSelector.css('pointer-events', 'auto');
    confirmIconSelector.css('opacity', '1')
    cancelIconSelector.css('pointer-events', 'auto');
    cancelIconSelector.css('opacity', '1')
}

var disableUndoDispIcons = function (confirmIconSelector, cancelIconSelector) {
    //buttons disabled such that user wont click before ajax completed
    confirmIconSelector.css('pointer-events', 'none');
    confirmIconSelector.css('opacity', '0.4')
    cancelIconSelector.css('pointer-events', 'none');
    cancelIconSelector.css('opacity', '0.4')
}


var populateDispositionChangeData = function (index, isBulkUpdate) {
    var data = {};
    var signalId = $('#signalIdPartner').val();

    if (isBulkUpdate) {
        selectedCasesInfo = selectedCasesInfo.filter((item, index) => selectedCasesInfo.indexOf(item) === index); //added code for bug/PVS-55623
        //this is applicable for all review screens where we have dataTable and we are performing bulk update.
        data['selectedRows'] = JSON.stringify(selectedCasesInfo);
    } else if (index > -1 && !signalId) {
        //this is applicable for all review screens where we have dataTable.
        data['selectedRows'] = JSON.stringify([populateDispositionDataFromGrid(index)]);
    } else {
        //this is applicable on the signal management screen and case detail screen.
        data['selectedRows'] = JSON.stringify([populateDispositionDataFromOther(signalId)]);
    }
    return data;
};

var populateDispositionDataFromGrid = function (index) {
    var data = {};
    var rowObject = table.rows(index).data()[0];
    data['configObj.id'] = rowObject.alertConfigId;
    data['executedConfigObj.id'] = rowObject.execConfigId;
    data['alert.id'] = rowObject.id;
    data['disposition'] = rowObject.disposition;
    return data;
};

var populateDispositionDataFromOther = function (signalId) {
    var data = {};
    data['signal.id'] = signalId;
    data['configObj.id'] = $('#configId').html();
    data['executedConfigObj.id'] = $('#execConfigId').val();
    data['alert.id'] = $('#alertId').val();
    return data;
};

function escapeAllHtml(unescapedHtml) {
    return unescapedHtml !== undefined ?
        unescapedHtml.toString().replace(/&/g, "&amp;")
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;") : ""
}

var fetchDynamicDispositionFilterHtml = function (dynamicFilterValue, isClosed ) {
    var uniqueFilter = dynamicFilterValue.replaceAll(" ","_");
    if(isClosed)
        return ' <li data-url="addWidgetUrl" class="checkbox checkbox-primary space-nowrap" data-id="pv-dash-searchCase" data-params="{&quot;widgetType&quot;: &quot;SEARCH_CASE&quot;}" data-widgetid="3298"> <input type="checkbox" name="relatedResults" id="filter_' + uniqueFilter + '" name="filter_' + uniqueFilter + '" value="' + escapeAllHtml(dynamicFilterValue) + '" class="dynamic-filters"><label class="dispositionValue" for="filter_' + uniqueFilter + '">' + dynamicFilterValue + '</label>'
    else
        return ' <li data-url="addWidgetUrl" class="checkbox checkbox-primary space-nowrap" data-id="pv-dash-searchCase" data-params="{&quot;widgetType&quot;: &quot;SEARCH_CASE&quot;}" data-widgetid="3298"> <input type="checkbox" name="relatedResults" checked="checked"  id="filter_' + uniqueFilter + '" name="filter_' + uniqueFilter + '" value="' + escapeAllHtml(dynamicFilterValue) + '" class="dynamic-filters"><label class="dispositionValue" for="filter_' + uniqueFilter + '">' + dynamicFilterValue + '</label>'
};

function dispositionEditPadding() {
    var heightOfDiv = $('.popover.justification ul.text-list > li:last-child').height();
    var paddingBottom = heightOfDiv + 39 + 'px';
    $('.popover.justification ul.text-list').css('padding-bottom', paddingBottom);
    $('.editedJustification').focus();
}

function changeDispositionFilters(selectedDispositionParent) {
    var selectedDispositionTitle = $(selectedDispositionParent).attr('title');
    selectedDispositionTitle = handleQuotesForJson(selectedDispositionTitle);
    if (selectedDispositionTitle && $('.disposition-ico :input[value="' + selectedDispositionTitle + '"]').length <= 0) {
        var dynamicDispositionFilter = fetchDynamicDispositionFilterHtml(selectedDispositionTitle, false);
        $(".disposition-ico").append($(dynamicDispositionFilter));
    }
    var filterArray = {};
    var filterValues = [];
    $('.dynamic-filters').each(function (index) {
        if ($(this).is(':checked')) {
            filterValues.push($(this).val());
        }
        filterArray[$(this).val()] = $(this).is(':checked');
    });
    var sessionStorageKey = '';
    if (applicationName === ALERT_CONFIG_TYPE.SINGLE_CASE_ALERT) {
        sessionStorageKey = ALERTS_PREFIX.SCA
    } else if (applicationName === ALERT_CONFIG_TYPE.AGGREGATE_CASE_ALERT) {
        sessionStorageKey = ALERTS_PREFIX.AGG
    } else if (applicationName === ALERT_CONFIG_TYPE.EVDAS_ALERT) {
        sessionStorageKey = ALERTS_PREFIX.EV
    }
    sessionStorage.setItem(sessionStorageKey + "filters_store", JSON.stringify(filterArray));
    sessionStorage.setItem(sessionStorageKey + "filters_value", JSON.stringify(filterValues));
    var freqSelected = "";
    if ($("#frequencyNames")) {
        freqSelected = $("#frequencyNames").val();
    }
    var url;
    if (applicationName == 'Literature Search Alert') {
        alertDetailsTable = $('#alertsDetailsTable').DataTable();
        url = listConfigUrl + "?filters=" + encodeURIComponent(JSON.stringify(filterValues))
    } else {
        alertDetailsTable = $('#alertsDetailsTable').DataTable();
        url = listConfigUrl + "&frequency=" + freqSelected + "&isFilterRequest=true&filters=" + encodeURIComponent(JSON.stringify(filterValues));
        if(isTempViewSelected) {
            url += "&tempViewId=" + tempViewPresent
        }
    }
    alertDetailsTable.ajax.url(url).load()
}

var fetchNewlyAttachedSignalData = function (attachedSignalData) {
    var signalAndTopics = '';
    $.each(attachedSignalData, function(i, obj){
        var url = signalDetailUrl + '?id=' + obj['signalId'];
        signalAndTopics = signalAndTopics + '<span class="click"><a href="' + url + '">' + obj['name'] + '</a></span>&nbsp;'
        signalAndTopics = signalAndTopics + ","
    });
    if(signalAndTopics.length > 1)
        return '<div>' + signalAndTopics.substring(0, signalAndTopics.length - 1) + '</div>';
    else
        return '-';
};

var dispositionBulkUpdate = function (totalSelectedRecords, justificationText) {
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
                className: 'btn btn-primary dispositionBulkUpdate',
                callback: function () {
                    switch ($('input[name=bulkOptions]:checked').val()) {
                        case 'current':
                            var selectedRowIndex = $(selectedDisposition).closest('tr').index();
                            if (isAbstractViewOrCaseView(selectedRowIndex)) {
                                selectedRowIndex = selectedRowIndex / 2
                            }
                            var data = populateDispositionChangeData(selectedRowIndex, false);
                            changeDisposition(justificationText, data, false);
                            break;
                        case 'allSelected':
                            var selectedExRconfig = [];
                            for(var i = 0; i< selectedCasesInfo.length;i++){
                                selectedExRconfig.push(selectedCasesInfo[i]["executedConfigObj.id"]);
                            }
                            var uniqueSelectedExRconfig = new Set(selectedExRconfig);
                            if (checkIfAllSelectedRowsInSameDispositionState() && (uniqueSelectedExRconfig.size==1)) {
                                var data = populateDispositionChangeData($(selectedDisposition).closest('tr').index(), true);
                                changeDisposition(justificationText, data, true);
                            } else if(uniqueSelectedExRconfig.size>1){
                                $.Notification.notify('warning', 'top right', "Warning", "Bulk disposition cannot be changed for PECs/Cases belonging to different Alerts. Please select PEC/Cases which belong to the same alert.", {autoHideDelay: 5000});
                            } else {
                                $.Notification.notify('error', 'top right', "Error", "All selected safety observations must be in same disposition for performing the bulk update.", {autoHideDelay: 5000});
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

var checkIfAllSelectedRowsInSameDispositionState = function () {
    var dispositionSet= getUniqueDispositions(selectedCasesInfo)
    var isAllInSameDispositionState = true;
    if(dispositionSet.length > 1){
        isAllInSameDispositionState  = false
    }
    return isAllInSameDispositionState
};

function getUniqueDispositions(data) {
    const uniqueDispositions = new Set();
    data.forEach(item => {
        if (item.hasOwnProperty('disposition')) {
            uniqueDispositions.add(item.disposition);
        }
    });
    return Array.from(uniqueDispositions);
}

var initiateAuthFlow = function (justificationText) {
    bootbox.dialog({
        title: 'User authentication',
        message: signal.utils.render('userAuth', {}),
        buttons: {
            ok: {
                label: "Confirm",
                className: 'btn btn-primary',
                callback: function () {
                    var userName = $('#username').val();
                    var password = $('#password').val();
                    if (userName && password) {
                        if(userName == loggedInUser) {
                            var isAuthencticated = authenticateUser(userName, password);
                            if (isAuthencticated) {
                                var selector;
                                var selectedRowCount;
                                if (typeof isCaseDetailView !== "undefined" && isCaseDetailView == "true") {
                                    selectedRowCount = $('table#alertsDetailsTable .copy-select:checked').length;
                                    selector = 'table#alertsDetailsTable tbody.detailsTableBody'
                                } else {
                                    selectedRowCount = $('.copy-select:checked').length;
                                    selector = 'table#alertsDetailsTable tbody'
                                }
                                var selectedRowIndex = $(selectedDisposition).closest('tr').index();
                                if (isAbstractViewOrCaseView(selectedRowIndex)) {
                                    selectedRowIndex = selectedRowIndex / 2
                                }
                                if (selectedRowCount > 1 && $(selector + " > tr:nth(" + selectedRowIndex + ") .copy-select").prop("checked")) {
                                    dispositionBulkUpdate(selectedRowCount, justificationText);
                                } else {
                                    var data = populateDispositionChangeData(selectedRowIndex, false);
                                    changeDisposition(justificationText, data, false);
                                }
                                return true;
                            } else {
                                $("#authCheck").addClass("show");
                                return false;
                            }
                        }else{
                            $.Notification.notify('error', 'top right', "Error", "Please select the logged in User.", {autoHideDelay: 5000});

                        }
                    } else {
                        if (!userName) {
                            $("#userNameField").addClass("show");
                        }
                        if (!password) {
                            $("#passwordField").addClass("show");
                        }
                        return false;
                    }
                }
            },
            cancel: {
                label: "Cancel",
                className: 'btn btn-default',
                callback: function () {
                    $('#dispositionJustificationPopover').modalPopover('hide');
                }
            }
        }
    });
};

var authenticateUser = function (inputUser, inputPassword) {
    var returnVal = false;
    var passwordJson = {
        "userName": inputUser,
        "password": inputPassword
    };

    $.ajax({
        type: "POST",
        url: authUrl,
        data: passwordJson,
        async: false,
    })
        .done(function (result) {
            if (result.authorized) {
                returnVal = true
            }
        });
    return returnVal
};

var showHideAddNewSignal = function () {

    $('#dispositionSignalPopover #addNewSignal').show();

};

var isAbstractViewOrCaseView = function (selectedRowIndex) {
    if (typeof isCaseDetailView != 'undefined' && isCaseDetailView == 'true' && selectedRowIndex != 0) {
        return true
    } else if ($('#detailed-view-checkbox').length > 0 && $('#detailed-view-checkbox').prop("checked") == true && selectedRowIndex != 0) {
        return true
    }
    return false

};

var updateSignalHistory = function () {
    $.ajax({
        url: refreshSignalHistory,
        data: {
            'enableSignalWorkflow' : enableSignalWorkflow
        },
    })
        .done(function (response) {
            if(response.status){
                $('#signalHistory').html(response.data);
                setCreatedDate();
            }else {
                $.Notification.notify('error', 'top right', "Error", response.message, {autoHideDelay: 5000});
            }

            disableDueDate()
        })
};
var validateAccess = function (e, id) {
    if(typeof hasSignalViewAccessAccess !== "undefined" && !hasSignalViewAccessAccess){
        e.preventDefault();
        $.Notification.notify('warning', 'top right', "Warning", "You don't have access to perform this action", {autoHideDelay: 5000});
    } else {
        if (signalAccessUrl != undefined) {
            var signalAccess = false;
            $.ajax({
                url: signalAccessUrl + '?id=' + id,
                async: false,
            })
                .done(function (response) {
                    signalAccess = response.success;
                })
                .fail(function () {
                    signalAccess = false;
                });
            if (!signalAccess) {
                $.Notification.notify('warning', 'top right', "Warning", $.i18n._('signalAccessError'), {autoHideDelay: 5000});
                e.preventDefault();
                e.stopPropagation();
            }
        }
    }
};

function bindJustificationClickEvents() {
    $("#dispositionJustificationPopover a.selectJustification").hover(
        function () {
            $('.tooltip').not(this).hide();
        }, function () {
            $(this).find("div.box").remove(); // on hover out, finds the dynamic element and removes it.
            $('.tooltip').not(this).show();
        }
    );

    $('#dispositionJustificationPopover .selectJustification').off('click').on('click', function (e) {
        e.preventDefault();
        if (e.which == 1) {
            initChangeDisposition($(this).text());
            dispositionEditPadding();
        }
    });

    $('#dispositionJustificationPopover #addNewJustification, #dispositionJustificationPopover .editIcon').on('click', function (e) {
        if ($(e.target).hasClass('editIcon')) {
            $('#dispositionJustificationPopover .editedJustification').val($(this).parent().siblings('.selectJustification').html());
            dispositionEditPadding();
        }
        $('#dispositionJustificationPopover #addNewJustification').hide();
        $('#dispositionJustificationPopover #edit-box').show();
        addCountBoxToInputField(8000, $(this).find('textarea'));
        dispositionEditPadding();
    });
};
var siglName = function () {
    var showChar = 68;
    var ellipsestext = "";
    var moretext = "...";
    var lesstext = "...";
    $('.comments-space').each(function () {
        var content = $(this).html();
        if (content.length > showChar) {
            var show_content = content.substr(0, showChar);
            var hide_content = content.substr(showChar, content.length - showChar);
            var html = show_content + '<span class="moreelipses">' + ellipsestext + '</span><span class="remaining-content"><span>' + hide_content + '</span><a href="#"  class="ico-dots view-all morelink"   more-data="' + content + '" title="' + $.i18n._('appLabel.viewAll') + '" >' + moretext + '</a></span>';
            $(this).html(html);
        }
    });
}
function truncateString(str){
    var showChar = 68;
    var ellipsestext = "";
    var moretext = "...";
    if(str.length >68){
        var show_str = str.substr(0, showChar);
        var shortName =   show_str + '<span class="moreelipses">' + ellipsestext  + '</span><a href="#"  class="view-all morelink"   more-data="' + str + '" title="' + $.i18n._('appLabel.viewAll') + '" >' + moretext + '</a></span>';
        return  shortName;
    }
    else{
        return str;
    }
}
var webUiPopInit1 = function(){
    var anchor = $(".morelink");
    anchor.webuiPopover({
        html: true,
        trigger: 'hover',
        content: function () {
            return $(this).attr('more-data')
        }
    });
    anchor.on("keypress",function( event ){
        if(event.keyCode ===13){
            $(this).webuiPopover('show');
        }
    });
    anchor.on("focusout",function( event ){
        $(this).webuiPopover('hide');
    });
};

var enableMenuTooltipsPopup = function () {
    $(".grid-menu-tooltip").mouseover(function () {
        var $this = $(this);
        var tooltipText = $this.attr("data-title");
        $this.tooltip({
            container:'body',
            title: tooltipText,
            placement: "auto"
        });
        $this.tooltip('show');
    });
}
function showEmptyJustificationErrorMessage() {
    $.Notification.notify('warning','top right', "Warning", "Justification is mandatory.", {autoHideDelay: 5000});
}

$(document).on('click', ".popover-content #confirmUndoJustification", function(){
    $(this).closest("ul")?.find(".editedJustification")?.val()?.trim() != '' ? initUndoDisposition($(this).attr('data-id'), $(this).closest("ul").find(".editedJustification").val()) : showEmptyJustificationErrorMessage();
});

$(document).on('click', "#cancelUndoJustification", function (e) {
    e.preventDefault();
    e.stopPropagation();
    $( ".undo-alert-disposition" ).popover('hide');
    clearAndHideUndoDispositionJustificationEditBox();
});

$(document).on('click', "#edit-boxDis #confirmUndoJustificationSignal", function() {
    let justificationText = $('#undoableJustificationPopover .editedJustification').val()
    let signalId = $(this).attr('data-id')
    if(justificationText.length) {
        initUndoDisposition(signalId, justificationText)
        $('#undoableJustificationPopover').hide();
        $('#undoableJustificationPopover .editedJustification').val('')
    }
    else{
        showEmptyJustificationErrorMessage();
    }

});
$(document).on('click', "#edit-boxDis #cancelUndoJustificationSignal", function(){
    $('#undoableJustificationPopover .editedJustification').val('')
    $('#undoableJustificationPopover').hide();


});

$(function () {
    if (dispositionDropdownEnabled === 'true') {
        var lastOpenedDropdown = null;

        function initializeSelect2() {
            $('.disposition-select2').select2({});
        }

        initializeSelect2();
        $(document).ajaxComplete(function () {
            initializeSelect2();
        });

        $(document).on('change', '.disposition-select2', function () {
            lastOpenedDropdown = $(this);
            var selectedOption = $(this).find(':selected');
            var buttonId = selectedOption.data('popup-target');

            if (buttonId) {
                $(buttonId).trigger('click');
            }
        });

        $(document).on('click', '.modal-backdrop', function () {
            // Select default disposition in the drop-down list if the modal window was closed
            var currentDisposition = lastOpenedDropdown.find('option[value="current"]').val();

            if (currentDisposition) {
                lastOpenedDropdown.val(currentDisposition).trigger('change');
            }

            lastOpenedDropdown = null;
        });
    }
});