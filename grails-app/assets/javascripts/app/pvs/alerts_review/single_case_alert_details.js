//= require app/pvs/common/rx_common.js
//= require app/pvs/common/rx_alert_utils.js
//= require app/pvs/common/rx_list_utils.js
//= require app/pvs/activity/activities.js
//= require app/pvs/actions/actions.js
//= require app/pvs/users/users.js
//= require app/pvs/common/rx_handlebar_ext.js
//= require app/pvs/alertComments/alertComments.js
//= require app/pvs/alerts_review/alert_review.js
//= require app/pvs/alerts_review/archivedAlerts.js
//= require app/pvs/common_tag.js

var userLocale = "en";
var applicationName = encodeToHTML("Single Case Alert");
var applicationLabel = encodeToHTML("Qualitative Alert");
var applicationNameICRVigibase = "Single Case Alert - Vigibase";
var applicationNameICRJader = "Single Case Alert - Jader";
var applicationNameICRVaers = "Single Case Alert - Vaers";
var applicationNameICRFaers = "Single Case Alert - Faers";
var blinded = 'Blinded';
var redacted = 'Redacted';
var table;
var executedIdList = [];
var tagsObj = {
    addedCaseSeriesTags: [],
    addedGlobalTags: [],
    deletedCaseSeriesTags: [],
    deletedGlobalTags: []
};
var tags = [];
var allVisibleIds = [];
var isDynamicFilterApplied = false;
var dir = '';
var index = -1;
var detailsParameters = {};
var isUpdateTemp = true;
var singleQueryJson='';
var prev_page = [];
var paginationAdvancedFilterSingle = true;
var recordsFiltered = 0;
var scaEntriesCount=sessionStorage.getItem('scaPageEntries')!=null?sessionStorage.getItem('scaPageEntries'):50
var fixedFilterColumn = []
var updatedExportUrl;

$(function () {
    $(document).on('fieldsRearranged', function (e) {
        $('.yadcf-filter-wrapper').remove();
        init_filter($('#alertsDetailsTable').DataTable())
    })
    if (window.sessionStorage.getItem('lastAdvancedFilter')) {
        window.sessionStorage.removeItem('lastAdvancedFilter')
    }
    if($(".data-analysis-button").find("li[class!=hidden]").length === 0){
        $(".data-analysis-button").addClass("hidden");
    }
    if (($("#isFaers").val() === 'true') || ($("#isVigibase").val() === 'true') || (typeof isArchived !== "undefined" && isArchived === 'true')) {
        $(".dropdown-case-pin").find('.ul-ddm-child').css({"top":"110px"});
    }
    var buttonClassVar = "";
    if(typeof buttonClass !=="undefined" && buttonClass){
        buttonClassVar = buttonClass;
    }
    var customFieldsEnabled = JSON.parse($("#customFieldsEnabled").val());

    $('a[href="#details"]').on("shown.bs.tab", function (e) {
        $('#alertsDetailsTable').DataTable().columns.adjust();
        addGridShortcuts('#alertsDetailsTable');
        removeGridShortcuts('#activitiesTable');
    });

    $('a[href="#archivedAlerts"]').on("shown.bs.tab", function (e) {
        $('#archivedAlertsTable').DataTable().columns.adjust();
        addGridShortcuts('#archivedAlertsTable');
        removeGridShortcuts('#activitiesTable');
        removeGridShortcuts('#alertsDetailsTable');
    });

    var activities_table;
    $('#activity_tab').on('click', function () {
        if (activities_table == null || activities_table == undefined) {
            if (callingScreen == CALLING_SCREEN.REVIEW) {
                if (executedIdList.length <= 0) {
                    executedIdList.push(executedConfigId);
                }
                activities_table = signal.activities_utils.init_activities_table("#activitiesTable",
                    '/signal/activity/listByExeConfig?' + 'executedIdList=' + executedIdList + "&appType=" + applicationName, applicationName);
            } else {
                activities_table = signal.activities_utils.init_activities_table("#activitiesTable",
                    '/signal/activity/listActivities?' + "appType=" + applicationName, applicationName);
            }
        }
    });
    if(isTempViewSelected) {
        fetchViewInstanceList(true);
    } else{
        fetchViewInstanceList();
    }

    $('a[href="#archivedAlerts"]').on('click', function () {
        if (callingScreen == CALLING_SCREEN.REVIEW) {
            archived_table = signal.archived_utils.init_archived_table("#archivedAlertsTable", archivedAlertUrl, applicationName, singleCaseDetailsUrl);
        }
    });

    if (callingScreen == CALLING_SCREEN.REVIEW || callingScreen == CALLING_SCREEN.DASHBOARD) {
        $(window).on('beforeunload', function () {
            if(isUpdateTemp) {
                updateTempView(false)
            }
            $.ajax({
                url: discardTempChangesUrl,
                method: 'GET'
            })
        });
    }

    //back button tag screen
    if (callingScreen != CALLING_SCREEN.TAGS) {
        sessionStorage.setItem("back_url", window.location.href)
    }
    $('.tag-screen-back-btn').on('click', function () {
        window.location.href = sessionStorage.getItem('back_url')
    });

    //ToDo: Need to modify this code, make function to calculate index.
    var fixedColumns = fixedColumnScaCount;
    var filterIndex = indexListSca;
    var caseNumberIndex = isPriorityEnabled ? 4 : 3;
    //Added for dashboard 
    if(callingScreen == CALLING_SCREEN.DASHBOARD){
        caseNumberIndex += 1
    }

    filterIndex.splice(0,0,caseNumberIndex);

    if ($("#isVaers").val() == "true") {
        fixedColumns = 3;
        filterIndex = [2,3,4,5,6,7,8,9,10,11,12,13,14];
        caseNumberIndex = 2;
    }

    if ($("#isVigibase").val() == "true") {
        fixedColumns = 3;
        filterIndex = [2,3,4,5,6,7,8,9,10,11,12,13,14,17];
        caseNumberIndex = 2;
    }

    if ($("#isFaers").val() == "true") {
        fixedColumns = 3;
        filterIndex = [2,3,4,5,6,7,11,12,13,14,15,16,17,18,19,20,21,23,24,25,26,27,29];
        caseNumberIndex = 2;
    }
    if ($("#isJader").val() == "true") {
        fixedColumns = 3;
        filterIndex = [2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24];
        caseNumberIndex = 2;
    }
    console.log(filterIndex)


    var alertDetailsTable;

    var checkedIdList = [];
    var checkedRowList = [];

    if ($("#isFaers").val() == "true") {
        alertType = applicationNameICRFaers
    } else if ($("#isVaers").val() == "true") {
        alertType = applicationNameICRVaers
    } else if ($("#isVigibase").val() == "true") {
        alertType = applicationNameICRVigibase
    } else if ($("#isJader").val() == "true") {
        alertType = applicationNameICRJader
    } else{
        alertType = applicationName
    }

    signal.alertReview.openAlertCommentModal("Single Case Alert", applicationName, applicationLabel, checkedIdList, checkedRowList);
    signal.alertReview.openCommentHistoryModal("Single Case Alert");
    signal.alertReview.showAttachmentModal();
    signal.alertReview.populateAdvancedFilterSelect(alertType);
    signal.alertReview.setSortOrder();
    signal.list_utils.flag_handler("singleCaseAlert", "toggleFlag");
    //The case history modal.
    signal.alertReview.openCaseHistoryModal();
    //The restart case review.
    signal.alertReview.restartReview(caseReviewPreviousUrl);
    //The similar cases modal.
    signal.alertReview.openSimilarCasesModal(caseInfoUrl);

    //apply the filter values to columns based on saved view
    var filterValue = signal.alertReview.createFilterMap('sca_filterMap_' + executedConfigId, 'sca_viewName_' + executedConfigId);

    $(document).on('click', '#addCase,#ic-addCase', function () {
        init_add_case_modal()
    });

    $(document).on('click', '.bulkDisposition', function () {
        $('#dispositionSignalPopover').modal('show');
    });

    $(document).on('click', '#addCaseButton', function () {
        add_case_to_list()
    });

    //apply the sorting to column based on saved view
    var sortingMap = signal.alertReview.createSortingMap('sca_sortedColumn_' + executedConfigId, 'sca_viewName_' + executedConfigId);
    if (sortingMap != 'undefined' && sortingMap.length > 0) {
        index = sortingMap[0][0];
        dir = sortingMap[0][1]
    }


    if (callingScreen == CALLING_SCREEN.REVIEW) {
        //saving new ViewInstance modal
        var alertType = getAlertType();
        signal.alertReview.openSaveViewModal(filterIndex, alertType, viewId)
    } else if (callingScreen == CALLING_SCREEN.DASHBOARD) {
        signal.alertReview.openSaveViewModal(filterIndex, ALERT_CONFIG_TYPE.SINGLE_CASE_ALERT_DASHBOARD, viewId)
    }

    $(window).on('beforeunload', function () {
        if (sessionStorage.getItem('isViewCall') == 'true') {
            var backUrl = sessionStorage.getItem('back_url');
            sessionStorage.clear();
            sessionStorage.setItem('back_url', backUrl)
        } else {
            var viewInfo = signal.alertReview.generateViewInfo(filterIndex);
            sessionStorage.setItem('sca_filterMap_' + executedConfigId, JSON.stringify(viewInfo.filterMap));
            sessionStorage.setItem('sca_sortedColumn_' + executedConfigId, JSON.stringify(viewInfo.sortedColumn));
            sessionStorage.setItem('sca_notVisibleColumn_' + executedConfigId, viewInfo.notVisibleColumn);
            sessionStorage.setItem('sca_viewName_' + executedConfigId, $('.viewSelect :selected').text().replace("(default)", "").trim())
        }
    });

    $(document).on('mouseover', ".generation-tooltip", function(){
        var $this = $(this);
        var tooltipText = $this.attr("data-title");
        $this.tooltip({
            title: tooltipText,
            placement: 'left'
        });
        $(this).next().addClass('single-tooltip');
        $this.tooltip('show');
    });

    var _changeInterval = null;
    $(document).on('keyup', 'input#yadcf-filter-alertsDetailsTable-'+caseNumberIndex, function (e) {

        var casenum=$(this).val();
        var caseInput = $(this);
        clearInterval(_changeInterval)
        _changeInterval = setInterval(function() {
            $("#alertsDetailsTable").DataTable().column(caseNumberIndex).search(casenum); //Exact value, column, reg
            if(casenum == "") {
                fixedFilterColumn.push({"column":caseNumberIndex, "value":""});
                caseInput.removeClass("inuse");
            } else {
                fixedFilterColumn.push({"column":caseNumberIndex, "value":casenum});
                caseInput.addClass("inuse");
            }
            clearInterval(_changeInterval)
        }, 1500);

    });

    $('#alert-list-filter-apply-bt').on('click', function (evt) {
        evt.preventDefault();
        $('#alert-list-filter').find('#filterUsed').val(true);
        var newUrl = listConfigUrl + "&" + $('#alert-list-filter').serialize() + "&z=" + true;
        $('#alertsDetailsTable').DataTable().ajax.url(newUrl).load()
    });

    $("#detail-tabs a[href='#details']").on("click", function (e) {
        $('#alertsDetailsTable').DataTable().columns.adjust();
    });

    fetchTags();

    var initAlertDetailsTable = function () {
        var prefix = "sca_";
        var isFilterRequest = true;
        var filterArray = [];
        var filterValues = [];
        if (window.sessionStorage && signal.alertReview.isAlertPersistedInSessionStorage(prefix)) {
            filterValues = JSON.parse(sessionStorage.getItem(prefix + "filters_value"));
        } else {
            signal.alertReview.removeFiltersFromSessionStorage(prefix);
            isFilterRequest = false;
        }
        var aoColumns = create_single_case_table_columns(callingScreen, customFieldsEnabled);
        signal.actions_utils.set_action_list_modal($('#action-modal'));
        signal.actions_utils.set_action_create_modal($('#action-create-modal'));

        $(document).on('click', '#alertsDetailsTable_paginate', function () {
            isPagination = true;
            if($('.alert-select-all').is(":checked") && !prev_page.includes($('li.active').text().slice(-3).trim())){
                prev_page.push($('li.active').text().slice(-3).trim());
            }
            if((!$('.alert-select-all').is(":checked") && prev_page.includes($('li.active').text().slice(-3).trim()))){
                var position = prev_page.indexOf($('li.active').text().slice(-3).trim());
                prev_page.splice(position,1);
            }
        });

        table = $('#alertsDetailsTable').DataTable({

            "language": {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json"
            },
            layout: {
                topStart: null,
                topEnd: null,
                bottomStart: ["pageLength", "info", {paging: {type: "full_numbers"}}],
                bottomEnd: null,
            },
            customProcessing: true,
            drawCallback: function (settings) {
                var filterMap = JSON.parse(sessionStorage.getItem("sca_filterMap_" + executedConfigId)) != null ? JSON.parse(sessionStorage.getItem("sca_filterMap_" + executedConfigId)) : new Map();
                // Added for PVS-61017
                $.each(filterMap, function (key, value) {
                    if (key != "undefined" && key != undefined && caseNumberIndex == key && value != undefined && typeof value !== "undefined" && value !== null) {
                        var input = $("#yadcf-filter-alertsDetailsTable-" + key)
                        if (input !== undefined && typeof input !== "undefined" && input !== null && input != "null") {
                            if (value != "") {
                                input.val(value).addClass('inuse');
                            } else {
                                input.val(value).removeClass('inuse');
                            }
                        }
                    }
                });
                var current_page = $('li.active').text().slice(-3).trim();
                if(typeof prev_page != 'undefined' && $.inArray(current_page,prev_page) == -1){
                    $(".alert-select-all").prop('checked',false);
                } else {
                    $(".alert-select-all").prop('checked',true);
                }
                if(typeof hasReviewerAccess !== "undefined" && !hasReviewerAccess) {
                    $(".changeDisposition").removeAttr("data-target");
                    $(".changePriority").removeAttr("data-target");
                }
                if(typeof hasSignalCreationAccessAccess !== "undefined" && !hasSignalCreationAccessAccess) {
                    $(".changeDisposition[data-validated-confirmed=true]").removeAttr("data-target");
                }
                var rowsDataAD = $('#alertsDetailsTable').DataTable().rows().data();
                if(settings.json != undefined) {
                    recordsFiltered = settings.json.recordsFiltered
                    pageDictionaryForAlertDetails($('#alertsDetailsTable_wrapper')[0], settings.aLengthMenu[0][0], settings.json.recordsFiltered);

                }else {
                    pageDictionaryForAlertDetails($('#alertsDetailsTable_wrapper')[0], 50, rowsDataAD.length);
                }
                if (typeof settings.json != "undefined") {
                    $("#fullCaseList").val(JSON.stringify(settings.json.fullCaseList));
                    if (!isDynamicFilterApplied) {
                        isDynamicFilterApplied = true;
                        signal.alertReview.bindGridDynamicFilters(settings.json.filters, prefix, settings.json.configId);
                    }
                    main_disp_filter= sessionStorage.getItem('sca_filters_value')
                    if (advancedFilterChanged && !main_disposition_filter || singleQueryJson!="") {
                        if (sessionStorage.getItem("sca_filters_value")){
                            //this function is called to handle the case when we change the disposition of a pec to a new disposition that is not available in quick filter dispo list
                            retainQuickDispositionFilter(JSON.parse(sessionStorage.getItem(prefix + "filters_value")), "sca_", settings.json.configId);
                        }
                        addAdvancedFilterDisp(settings.json.advancedFilterDispName, "sca_", settings.json.configId);
                        advancedFilterChanged = false;
                        singleQueryJson="";
                    }
                    else if(advancedFilterChanged && main_disposition_filter){
                        retainQuickDispositionFilter(JSON.parse(main_disp_filter), "sca_", settings.json.configId);
                        advancedFilterChanged = false;
                        main_disposition_filter=false;
                    }
                    allVisibleIds = settings.json.visibleIdList
                }
                tagEllipsis($("#alertsDetailsTable"));
                colEllipsis();
                showInfoPopover();
                webUiPopInit();
                webUiPopInitForCategories();
                closePopupOnScroll();
                enterKeyAlertDetail();
                closeInfoPopover();
                if (typeof isCaseDetailView !== "undefined" && isCaseDetailView == "true") {
                    showCaseDetailsView();
                }
                signal.alertReview.sortIconHandler();
                if (filterValue.length === 0 && !$(table.table().body()).hasClass('detailsTableBody')) {
                    $(table.table().body()).addClass('detailsTableBody');
                }
                if(isTempViewSelected) {
                    $(".alert-select-all").prop('checked', true)
                    $(".alert-check-box").prop('checked', true)
                    $("#view-types-menu").find("li").addClass("disabled");
                    $("#view-types-menu").find("li").find('a').off('click');
                    $("#view-types-menu").find("li").find('a').removeAttr("href");
                    $("#saveViewTypes").find("li").addClass("disabled");
                    $("#saveViewTypes").find("li").find('a').off('click');
                    $("#saveViewTypes").find("li").find('a').removeAttr("href");
                }
                populateSelectedCases();

                var checkedId
                $('input[type=checkbox]').each(function () {
                    checkedId = $(this).attr('data-id')
                    if (alertIdSet.has(checkedId)) {
                        $(this).prop('checked', true);
                    }
                    if((!isPagination)  && (checkedId !== undefined) && !allVisibleIds.includes(parseInt(checkedId)) && selectedCases.includes(checkedId)){
                        // Remove Cases which were selected before but now not visible on screen
                        selectedCasesInfo.splice($.inArray(checkedId, selectedCases), 1);
                        caseJsonArrayInfo.splice($.inArray(checkedId, selectedCases), 1);
                        selectedCasesInfoSpotfire.splice($.inArray(checkedId, selectedCases), 1);
                        selectedCases.splice($.inArray(checkedId, selectedCases), 1);
                    }else if((!isPagination) && (checkedId !== undefined) && allVisibleIds.includes(parseInt(checkedId)) && selectedCases.indexOf($(this).attr("data-id")) === -1 && $(this).is(':checked')){
                        // Disable checkbox for cases not available on screen
                        $(this).prop('checked', false);
                    }
                });


                $('.dt-pagination').on('change', function () {
                    var countVal = $('.dt-pagination').val()
                    sessionStorage.setItem("scaPageEntries", countVal);
                    scaEntriesCount = sessionStorage.getItem("scaPageEntries");
                })
            },
            "ajax": {
                "url": createAlertListUrl(isFilterRequest, filterValues),
                "type": "POST",
                "dataSrc": "aaData",
                "cache": false,
                "error": ajaxAuthroizationError,
                "data": function (d) {
                    [d, dir, index] = updateOrder(index, d, columnSeq, dir)
                    // Added for PVS-61017
                    var filterMap = JSON.parse(sessionStorage.getItem("sca_filterMap_" + executedConfigId)) != null ? JSON.parse(sessionStorage.getItem("sca_filterMap_" + executedConfigId)) : new Map();
                        var searchValue = d.columns[caseNumberIndex].search.value;
                        if (filterMap) {
                            filterMap[caseNumberIndex] = searchValue
                        }
                    sessionStorage.setItem('sca_filterMap_' + executedConfigId, JSON.stringify(filterMap));
                    $("#alertsDetailsTable_paginate").on('click', function () {
                        paginationAdvancedFilterSingle = true;
                    });

                    if ($('#advanced-filter').val()) {
                        paginationAdvancedFilterSingle = false;
                        d.advancedFilterId = $('#advanced-filter').val()
                        isPagination = false;
                    }
                    if(detailedAdvancedFilterId != "null" && detailedAdvanceFilterName != "null"){
                        d.advancedFilterId = detailedAdvancedFilterId;
                    }
                    if(detailedViewInstanceId != "null")
                    {
                        d.isViewInstance = detailedViewInstanceId;
                    }
                    if ($('#queryJSON').val()) {
                        singleQueryJson = $('#queryJSON').val()
                        d.queryJSON = $('#queryJSON').val()
                        sessionStorage.setItem('lastAdvancedFilter', $('#queryJSON').val());
                    } else if (sessionStorage.getItem('lastAdvancedFilter')) {
                        if (paginationAdvancedFilterSingle) {
                            d.queryJSON = sessionStorage.getItem('lastAdvancedFilter');
                        } else {
                            sessionStorage.removeItem('lastAdvancedFilter');
                        }
                    }
                    d.callingScreen = callingScreen;
                    d.isViewInstance = d.draw === 1 ? 1 : 0;
                    detailsParameters = d;
                }, // added for PVS-53683 to cancel previous call
                beforeSend: function() {
                    if(typeof table != "undefined") {
                        var xhr = table.settings()[0].jqXHR;
                        if(xhr && xhr.readyState != 4){
                            xhr.abort();
                        }
                    }
                }
            },
            initComplete: function (settings, json) {
                $(".copy-select").prop("checked",false);
                if (filterValue.length === 0 && !isDynamicFilterApplied) {
                    isDynamicFilterApplied = true;
                    signal.alertReview.bindGridDynamicFilters(json.filters, prefix, json.configId);
                }
                if(detailedAdvancedFilterId != "null" && detailedAdvanceFilterName != "null"){
                    var option = new Option(detailedAdvanceFilterName, detailedAdvancedFilterId, true, true);
                    $("#advanced-filter").append(option).trigger('change');
                    detailedAdvancedFilterId = "null";
                    detailedAdvanceFilterName = "null";
                }
                if (sessionStorage.getItem(prefix + "filters_store")) {
                    if(typeof settings.json !== 'undefined' && !settings.json.advancedFilterDispName){
                        setQuickDispositionFilter(prefix);
                    }
                    var filtersArray = JSON.parse(sessionStorage.getItem(prefix + "filters_store"));
                    $.each(filtersArray, function (index, obj) {
                        var escapedIndex = escapeSpecialCharactersInId(index);
                        $('#filter' + escapedIndex).prop("checked", obj == true);
                    });
                    $('.dynamic-filters').each(function (index) {
                        var id = escapeSpecialCharactersInId($(this).attr('id'));
                        if ($(this).is(':checked')) {
                            $('.dynamic-filters').closest("#" + id).prop('checked', true)
                        } else {
                            $('.dynamic-filters').closest("#" + id).prop('checked', false)
                        }
                    });
                }
                var orderMapSingle
                if(settings.json !== undefined && settings.json.orderColumnMap){
                    var orderColumnMap = settings.json.orderColumnMap;
                    index = $('#alertsDetailsTable').find("th[data-field=" + orderColumnMap.name + "]").attr('data-column-index');
                    dir = orderColumnMap.dir;
                    isViewInstance = 0;
                    signal.alertReview.sortIconHandler(index);
                }
                signal.alertReview.enableMenuTooltips();
                signal.alertReview.disableTooltips();
                addGridShortcuts('#alertsDetailsTable');
                if (performance.navigation.type == performance.navigation.TYPE_RELOAD) {
                    isCaseSeriesGenerating = 'false';
                }
                if (isCaseSeriesGenerating == 'true') {
                    var caseSeriesMessage = "Showing " + limitNoOfCases + " entries. Case Series Generating in Background";
                    $.Notification.notify('warning', 'top right', "Warning", caseSeriesMessage, {autoHideDelay: 5000});
                }
                showTempViewModal(false);
                showInfoPopover();
                $("#toggle-column-filters, #ic-toggle-column-filters").on('click', function () {
                    selectedFilter=false;
                    var ele = $('.yadcf-filter-wrapper');
                    var inputEle = $('.yadcf-filter');
                    if (ele.is(':visible')) {
                        ele.hide();
                        $('.single-header-row').css('height', '47px');
                    } else {
                        ele.show();
                        inputEle.first().focus();
                    }
                });
            },
            "bLengthChange": true,
            "cache": true,
            "bProcessing": true,
            "bServerSide": true,
            "oLanguage": {
                "oPaginate": {
                    "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                    "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                    "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                    "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
                },
                "sLengthMenu":"Show _MENU_",
            },
            "aLengthMenu": [[50, 100, 200, 500], [50, 100, 200, 500]],
            "iTotalDisplayRecords": scaEntriesCount,
            "iDisplayLength": parseInt(scaEntriesCount),
            "aoColumns": aoColumns,
            "responsive": true,
            "deferLoading": filterValue.length > 0 ? 0 : null,
            "scrollX": true,
            "scrollY": "65vh",
            "fixedColumns":{
                "start":fixedColumns
            },
            "rowCallback": function (row, data, index) {
                $(row).addClass("data-table-row");
                // workaround, select2 initialization is called before elements displayed
                setTimeout(function () {
                    signal.user_group_utils.bind_assignTo_to_grid_row($(row), searchUserGroupListUrl, {
                        name: data.assignedToUser.fullName,
                        id: data.assignedToUser.id
                    });
                    if (typeof hasReviewerAccess !== "undefined" && !hasReviewerAccess) {
                        $(row).find(".assignedToSelect").select2({
                            minimumInputLength: 0,
                            multiple: false,
                            placeholder: 'Select Assigned To',
                            allowClear: false,
                            width: "100%",
                        })
                    }
                }, 0)
                $(row).find('td').height(45);
            },
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }]
        });

        signal.user_group_utils.bind_assignTo_selection(assignToGroupUrl, table, hasReviewerAccess);
        setTimeout(function () {
            init_filter(table);
        }, 500);
        return table
    };
    var init_filter = function (data_table) {

        var filterOptions = [];
        $.each(filterIndex, function (index, value) {
            filterOptions.push({
                column_number: value,
                fixed_column: value == caseNumberIndex? true:false
            })
        });

        yadcf.init(data_table, filterOptions,
            {
                filter_type: "text",
                filter_reset_button_text: false,
                filter_delay: 1200,
                filter_default_label: '',
                fixed_column: false
                });
        $(".yadcf-filter").removeAttr('placeholder');
        if (filterValue.length > 0) {
            yadcf.exFilterColumn(data_table, filterValue, true);
        } else {
            $('.yadcf-filter-wrapper').hide()
        }
        window.sca_data_table = table;
        signal.fieldManagement.init($('#alertsDetailsTable').DataTable(), '#alertsDetailsTable', fixedColumns, true);
        signal.commonTag.openCommonAlertTagModal("PVS","Qualitative", tags);
        $('.yadcf-filter-wrapper').hide();

    };

    var create_single_case_table_columns = function (callingScreen, customFieldsEnabled) {

        var aoColumns = [

            {
                "mData": "selected",
                "mRender": function (data, type, row) {
                    if ($.inArray(row.execConfigId, executedIdList) == -1) {
                        executedIdList.push(row.execConfigId)
                    }
                    var checkboxhtml = '<input class="execConfigId" id="execConfigId" type="hidden" value="' + row.execConfigId + '" />' +
                        '<input class="alertConfigId" id="alertConfigId" type="hidden" value="' + row.alertConfigId + '" />' +
                        '<input data-field ="productFamily" data-id="' + encodeToHTML(row.productFamily) + '" type="hidden" value="' + encodeToHTML(row.productFamily) + '" />' +
                        '<input data-field ="primaryEvent" data-id="' + encodeToHTML(row.primaryEvent) + '" type="hidden" value="' + encodeToHTML(row.primaryEvent) + '" />'

                    if (alertIdSet.has(JSON.stringify(row.id))) {
                        return checkboxhtml + '<input type="checkbox" class="alert-check-box editor-active copy-select" data-id=' + row.id + ' checked/>';

                    } else if(selectedCases.includes(row.id.toString())){
                        return  checkboxhtml +  '<input type="checkbox" class="alert-check-box editor-active copy-select" data-id=' + row.id + ' checked/>';
                    } else{
                        return checkboxhtml + '<input type="checkbox" class="alert-check-box editor-active copy-select"  data-id=' + row.id + ' />';

                    }
                },
                "className": "col-min-50 col-max-50",
                "sortable": false,
                "visible": true
            },
            {
                "mData": "dropdown",
                "mRender": function (data, type, row) {
                    var actionButton = '<div style="display: block;" class="btn-group dropdown dataTableHideCellContent" align="center"> \
                        <a class="dropdown-toggle" data-toggle="dropdown" tabindex="0"> \
                        <span style="cursor: pointer;font-size: 125%;" class="glyphicon glyphicon-option-vertical"></span><span class="sr-only">Toggle Dropdown</span> \
                        </a> ';

                    if (row.comments) {
                        actionButton += '<i class="mdi mdi-chat blue-2 font-13 pos-ab comment" title="' + $.i18n._('commentAvailable') + '"></i>';
                    }

                    if (row.isAttachment == true) {
                        actionButton += ' <i class="mdi mdi-attachment blue-1 font-13 pos-ab attach" title="' + $.i18n._('attachmentAvailable') + '"></i>';
                    }

                    actionButton += '<ul class="dropdown-menu menu-cosy" role="menu"> <li role="presentation"><a class="review-row-icon case-history-icon" tabindex="0"><span class="fa fa-list m-r-10"></span>History</a></li>';
                    if (isCommentsEditEnable === 'true') {
                        actionButton += '<li role="presentation"><a class="review-row-icon comment-icon " data-info="row" tabindex="0"><span class="fa fa-comment m-r-10"></span>Comments</a></li>';
                    }
                    actionButton += '<li role="presentation"><a class="review-row-icon show-attachment-icon" data-field="attachment" data-id="' + row.id + '" data-controller="singleCaseAlert" tabindex="0"><span class="fa fa-file m-r-10"></span>Attachment</a></li>';

                    if(((row.isUndoEnabled=="true" && row.isDefaultState === "true") || row.isDefaultState === "false") && (!isFaersCheck || isFaersCheck === "false") && (!isJaderCheck || isJaderCheck === "false")){
                        if(!row.isValidationStateAchieved && (row.isUndoEnabled === "true" && (isAdmin || row.dispPerformedBy === currUserName))) {
                            actionButton += '<li role="presentation" class="popover-parent">' +
                                '<a tabindex="0" data-id ="' + row.id + '" title="Undo Disposition Change" data-html="true" class="review-row-icon undo-alert-disposition" ' +
                                'data-toggle="popover" data-content="<textarea class=\'form-control editedJustification\'>' +
                                '</textarea>' +
                                '<ol class=\'confirm-options\' id=\'revertConfirmOption\'>' +
                                '<li><a tabindex=\'0\' href=\'javascript:void(0);\' title=\'Save\'><i class=\'mdi mdi-checkbox-marked green-1\' data-id =\'' + row.id + '\' id=\'confirmUndoJustification\'></i></a></li>' +
                                '<li><a tabindex=\'0\' href=\'javascript:void(0);\' title=\'Close\'><i class=\'mdi mdi-close-box red-1\' id=\'cancelUndoJustification\'></i> </a></li>' +
                                '</ol>" '+
                                '</a>' +
                                '<span class="md md-undo m-r-10"></span>Undo Disposition Change';
                            actionButton += '</li>';
                        } else {
                            actionButton += '<li role="presentation">' +
                                '<a tabindex="0" data-id ="' + row.id + '"   class="review-row-icon undo-alert-disposition" style="cursor: not-allowed; opacity: 0.65"> <span class="md md-undo m-r-10"></span>Undo Disposition Change</a>';
                          }
                        }

                    actionButton += '</ul></div>';

                    return actionButton;
                },
                "sortable": false,
                "visible": true,
                "className": "col-min-50 col-max-50 dropDown"
            }
        ];
        if ($("#isFaers").val() != "true" && $("#isVaers").val() != "true" && $("#isVigibase").val() != "true" && $("#isJader").val() != "true") {
            if (isPriorityEnabled) {
                aoColumns.push.apply(aoColumns, [{
                    "mData": "priority",
                    "aTargets": ["priority"],
                    "mRender": function (data, type, row) {
                        var isPriortyChangeAllowed = (isProductSecurity == 'true' && allowedProductsAsSafetyLead.indexOf(row.actualProductName)) || isProductSecurity == 'false';
                        return signal.utils.render('priority', {
                            priorityValue: row.priority.value,
                            priorityClass: row.priority.iconClass,
                            isPriorityChangeAllowed: isPriortyChangeAllowed
                        });
                    },
                    'className': 'text-center col-min-50 col-max-50 priorityParent',
                    "visible": true
                }]);
            }
            aoColumns.push.apply(aoColumns, [{
                    "mData": "actions",
                    'className': 'col-min-50  col-max-50 text-center action-col dropDown',
                    "mRender": function (data, type, row) {
                        row["buttonClass"]= buttonClassVar;
                        return signal.actions_utils.build_action_render(row)
                    },
                    "visible": true
                }]);
        }
        if (callingScreen == CALLING_SCREEN.TRIGGERED_ALERTS || callingScreen == CALLING_SCREEN.DASHBOARD) {
            aoColumns.push.apply(aoColumns, [{
                "mData": "name",
                "className": 'col-min-150 col-max-150 cell-break',
                "mRender": function (data, type, row) {
                    var colElement = '<div class="col-container"><div class="col-height">';
                    colElement += encodeToHTML(row.alertName);
                    colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(row.alertName) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                    colElement += '</div></div>';
                    return colElement
                },
                "visible": true
            }]);
        }
        aoColumns.push.apply(aoColumns, [
            {

                 "mData": "caseNumber",
                 'className': $("#isVaers").val() == 'true' || $("#isVigibase").val() == 'true' ? 'col-min-115 col-max-115' : 'col-min-75 col-max-75',
                 "mRender": function (data, type, row) {
                    var isFaers = $("#isFaers").val();
                    var isVaers = $("#isVaers").val();
                    var isVigibase = $("#isVigibase").val();
                    var isJader = $("#isJader").val();
                    var linkPre = '<a target="_blank" oncontextmenu="return false" class="scaCaseNumber" href="' + (caseDetailUrl + '?' +
                        signal.utils.composeParams({
                            caseNumber: row.actualCaseNumber,
                            version: row.caseVersion,
                            followUpNumber:  row.followUpNumber,
                            alertId: row.id,
                            isArchived: isArchived,
                            isFaers: isFaers,
                            isVaers: isVaers,
                            isVigibase: isVigibase,
                            isJader:isJader,
                            isSingleAlertScreen: true,
                            isVersion: true,
                            isCaseSeries : $("#isCaseSeries").val() == "true",
                            isAggregateAdhoc:isAggregateAdhoc,
                            caseVisibility: row.caseNumber
                        })) + '">';

                    var tooltipMsg = "Version:" + row.caseVersion;
                    var isBlinded = row.caseNumber == blinded || row.caseNumber == redacted;
                    var caseNum = "<span class='grid-menu-tooltip' title='" + tooltipMsg + "' data-field ='caseNumber' data-id='" + row.actualCaseNumber + "'>" +
                        (row.caseNumber) + "</span><span data-field ='caseVersion' data-id='" + row.caseVersion +
                        "'></span><span data-field ='execConfigId' data-id='" + row.execConfigId + "'><span data-field ='alertId' data-id='" + row.id + "'></span>";
                    var linkSuf = (!isBlinded ?"(":"") + "<span data-field ='followUpNumber' data-id='" + row.followUpNumber + "'>";
                        linkSuf += !isBlinded ? ((row.followUpNumber < 1) ? 0 : row.followUpNumber) + "</span>)" : "</span>";
                    if (isFaers != "true" && isVaers != 'true' && isVigibase != 'true' ) {
                        return linkPre + caseNum + linkSuf + "</a>";
                    }
                    else {
                        return linkPre + caseNum + "</a>";
                    }
                },
                "data-type": "caseNumber",
                "visible": true,
                "className": 'col-min-150 col-max-150',
            },
            {
                "mData": "alertTags",
                "mRender": function (data, type, row) {
                    var tagsElement = signal.alerts_utils.get_tags_element(row.alertTags);
                    return tagsElement
                },
                "className": 'col-max-250 col-min-250',
                "sortable": false,
                "visible": signal.fieldManagement.visibleColumns('alertTags')
            },
            {
                "mData": "caseInitReceiptDate",
                "mRender": function (data, type, row) {
                    return row.caseInitReceiptDate;
                },
                "className": 'col-min-150 col-max-150',
                "sortable": true,
                "visible": signal.fieldManagement.visibleColumns('caseInitReceiptDate')
            },
            {
                "mData": "productName",
                'className': 'col-min-150 col-max-150 cell-break',
                "mRender": function (data, type, row) {
                    var colElement = '<div class="col-container"><div class="col-height">';
                    colElement += encodeToHTML(row.productName);
                    colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(row.productName) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                    colElement += '</div></div>';
                    return colElement
                },
                "visible": signal.fieldManagement.visibleColumns('productName')
            },
            {
                "mData": "pt",
                "className": 'col-min-150 col-max-150',
                "mRender": function (data, type, row) {
                    return '<span data-field="masterPrefTermAll" data-id="' + row.pt + '">' + row.pt + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('pt')
            }]);

        if ($("#isVigibase").val() == "true") {
            aoColumns.push.apply(aoColumns, [
                {
                    "mData": "age",
                    'className': 'col-min-100 col-max-100',
                    "mRender": function (data, type, row) {
                        return'<span>'+row.age+'</span>'
                    },
                    "visible": signal.fieldManagement.visibleColumns('age')
                },
                {
                    "mData": "gender",
                    'className': '',
                    "mRender": function (data, type, row) {
                        return '<span>' + row.gender + '</span>'
                    },
                    "sortable": true,
                    "visible": signal.fieldManagement.visibleColumns('gender')
                },
                {
                    "mData": "caseReportType",
                    "className": '',
                    "mRender": function (data, type, row) {
                        return '<span>' + row.caseReportType + '</span>'
                    },
                    "sortable": false,
                    "visible": signal.fieldManagement.visibleColumns('caseReportType')
                },
                {
                    "mData": "eventOutcome",
                    'className': 'col-min-150 col-max-150',
                    "mRender": function (data, type, row) {
                        var colElement = '<div class="col-container"><div class="col-height">';
                        colElement += encodeToHTML(row.eventOutcome);
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(row.eventOutcome) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    },
                    "visible": signal.fieldManagement.visibleColumns('eventOutcome')
                },
                {
                    "mData": "serious",
                    'className': '',
                    "mRender": function (data, type, row) {
                        return '<span>' + row.serious + '</span>'
                    },
                    "visible": signal.fieldManagement.visibleColumns('serious')
                },
                {
                    "mData": "indication",
                    'className': 'col-min-150 col-max-150',
                    "mRender": function (data, type, row) {
                        var colElement = '<div class="col-container"><div class="col-height">';
                        colElement += encodeToHTML(row.indication);
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(row.indication) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    },
                    "visible": signal.fieldManagement.visibleColumns('indication')
                },
                {
                    "mData": "suspProd",
                    'className': 'col-min-150 col-max-150',
                    "mRender": function (data, type, row) {
                        var colElement = '<div class="col-container"><div class="col-height">';
                        colElement += encodeToHTML(row.suspProd);
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(row.suspProd) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    },
                    "sortable": true,
                    "visible": signal.fieldManagement.visibleColumns('suspProd')
                },
                {
                    "mData": "allPt",
                    'className': 'col-min-100 col-max-100',
                    "mRender": function (data, type, row) {
                        var allPtList = '';
                        if (row.allPt) {
                            allPtList = row.allPt.split('!@##@!').join(' ');
                            allPtList = allPtList.split('!@.@!').join(') ');
                        }
                        var colElement = '<div class="col-container"><div class="col-height">';
                        colElement += allPtList;
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(allPtList) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    },
                    "sortable": false,
                    "visible": signal.fieldManagement.visibleColumns('allPt')
                },
                {
                    "mData": "conComit",
                    'className': 'col-min-100 col-max-100',
                    "mRender": function (data, type, row) {
                        var colElement = '<div class="col-container"><div class="col-height">';
                        colElement += removeLineBreak(row.conComit);
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + removeLineBreak(row.conComit) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    },
                    "visible": signal.fieldManagement.visibleColumns('conComit')
                },
                {
                    "mData": "initialFu",
                    "mRender": function (data, type, row) {
                        return '<span>' + row.initialFu + '</span>'
                    },
                    "visible": signal.fieldManagement.visibleColumns('initialFu')
                },
                {
                    "mData": "comments",
                    'className': 'col-min-300 col-max-300  cell-break',
                    "mRender": function (data, type, row) {
                        var colElement = '<div class="col-container"><div class="col-height commentDisplay">';
                        colElement += encodeToHTML(row.comments);
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + escapeHTML(encodeToHTML(row.comments)) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    },
                    "sortable": false,
                    "visible": signal.fieldManagement.visibleColumns('comments')
                },
                {
                    "mData": "region",
                    "mRender": function (data, type, row) {
                        return '<span>' + row.region + '</span>'
                    },
                    "visible": signal.fieldManagement.visibleColumns('region')
                }]);
        } else if ($("#isJader").val() == "true") {
            aoColumns.push.apply(aoColumns, [
                {
                    "mData": "genericName",
                    'className': 'col-min-150 col-max-150 cell-break',
                    "mRender": function (data, type, row) {
                        var colElement = '<div class="col-container"><div class="col-height">';
                        colElement += encodeToHTML(row.genericName);
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(row.genericName) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    },
                    "visible": signal.fieldManagement.visibleColumns('genericName')
                }
            ]);
            aoColumns.push.apply(aoColumns, [
                {
                    "mData": "allPt",
                    'className': 'col-min-100 col-max-100',
                    "mRender": function (data, type, row) {
                        var allPtList = '';
                        if (row.allPt) {
                            allPtList = row.allPt.split('!@##@!').join(' ');
                            allPtList = allPtList.split('!@.@!').join(') ');
                        }
                        var colElement = '<div class="col-container"><div class="col-height">';
                        colElement += allPtList;
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(allPtList) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    },
                    "sortable": true,
                    "visible": signal.fieldManagement.visibleColumns('allPt')
                }]),
            aoColumns.push.apply(aoColumns, [
                {
                    "mData": "suspProd",
                    'className': 'col-min-150 col-max-150',
                    "mRender": function (data, type, row) {
                        var colElement = '<div class="col-container"><div class="col-height">';
                        colElement += encodeToHTML(row.suspProd);
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(row.suspProd) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    },
                    "sortable": true,
                    "visible": signal.fieldManagement.visibleColumns('suspProd')
                }]);
            aoColumns.push.apply(aoColumns, [
                {
                    "mData": "gender",
                    'className': '',
                    "mRender": function (data, type, row) {
                        return '<span>' + row.gender + '</span>'
                    },
                    "visible": signal.fieldManagement.visibleColumns('gender')
                }]);
            aoColumns.push.apply(aoColumns, [
                {
                    "mData": "age",
                    'className': 'col-min-75 col-max-75',
                    "mRender": function (data, type, row) {
                        return '<span>' + row.age + '</span>'
                    },
                    "visible": signal.fieldManagement.visibleColumns('age')
                }]);
            aoColumns.push.apply(aoColumns, [
                {
                    "mData": "caseReportType",
                    "className": '',
                    "mRender": function (data, type, row) {
                        return '<span>' + row.caseReportType + '</span>'
                    },
                    "sortable": true,
                    "visible": signal.fieldManagement.visibleColumns('caseReportType')
                }]);
            aoColumns.push.apply(aoColumns, [

                {
                    "mData": "indication",
                    'className': 'col-min-150 col-max-150',
                    "mRender": function (data, type, row) {
                        var colElement = '<div class="col-container"><div class="col-height">';
                        colElement += encodeToHTML(row.indication);
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(row.indication) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    },
                    "visible": signal.fieldManagement.visibleColumns('indication')
                }
            ]);
            aoColumns.push.apply(aoColumns, [
                {
                    "mData": "eventOutcome",
                    'className': 'col-min-150 col-max-150',
                    "mRender": function (data, type, row) {
                        var colElement = '<div class="col-container"><div class="col-height">';
                        colElement += encodeToHTML(row.eventOutcome);
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(row.eventOutcome) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    },
                    "visible": signal.fieldManagement.visibleColumns('eventOutcome')
                }]);
            aoColumns.push.apply(aoColumns, [
                {
                    "mData": "conComit",
                    'className': 'col-min-100 col-max-100',
                    "mRender": function (data, type, row) {
                        var colElement = '<div class="col-container"><div class="col-height">';
                        colElement += formatText(row.conComit);
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + formatText(row.conComit) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    },
                    "visible": signal.fieldManagement.visibleColumns('conComit')
                }]);
            aoColumns.push.apply(aoColumns, [
                {
                    "mData": "death",
                    'className': '',
                    "mRender": function (data, type, row) {
                        return '<span>' + row.death + '</span>'
                    },
                    "sortable": true,
                    "visible": signal.fieldManagement.visibleColumns('death')
                }]);
            aoColumns.push.apply(aoColumns, [
                {
                    "mData": "doseDetails",
                    'className': 'col-min-150 col-max-100 word-break',
                    "mRender": function (data, type, row) {
                        var colElement = '<div class="col-container"><div class="col-height">';
                        colElement += encodeToHTML(row.doseDetails);
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(row.doseDetails) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    },
                    "sortable": true,
                    "visible": signal.fieldManagement.visibleColumns('doseDetails')
                }]);
            aoColumns.push.apply(aoColumns, [
                {
                    "mData": "reportersHcpFlag",
                    'className': '',
                    "mRender": function (data, type, row) {
                        return '<span>' + row.reportersHcpFlag + '</span>'
                    },
                    "sortable": true,
                    "visible": signal.fieldManagement.visibleColumns('reportersHcpFlag')
                }]);
            aoColumns.push.apply(aoColumns, [
                {
                    "mData": "eventOnsetDate",
                    'className': 'col-min-100 col-max-100',
                    "mRender": function (data, type, row) {
                        return '<span>' + row.eventOnsetDate + '</span>'
                    },
                    "visible": signal.fieldManagement.visibleColumns('eventOnsetDate')
                }]);
            aoColumns.push.apply(aoColumns, [
                {
                    "mData": "patientMedHist",
                    'className': 'col-min-150 col-max-150',
                    "mRender": function (data, type, row) {
                        var colElement = '<div class="col-container"><div class="col-height">';
                        colElement += $("<p/>").html(row.patientMedHist).text();
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeHTMLTags(row.patientMedHist) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    },
                    "visible": signal.fieldManagement.visibleColumns('patientMedHist')
                }]);
            aoColumns.push.apply(aoColumns, [
                {
                    "mData": "rechallenge",
                    'className': '',
                    "mRender": function (data, type, row) {
                        return '<span>' + row.rechallenge + '</span>'
                    },
                    "sortable": true,
                    "visible": signal.fieldManagement.visibleColumns('rechallenge')
                }]);
            aoColumns.push.apply(aoColumns, [
                {
                    "mData": "reporterQualification",
                    'className': '',
                    "mRender": function (data, type, row) {
                        return '<span>' + row.reporterQualification + '</span>'
                    },
                    "sortable": true,
                    "visible": signal.fieldManagement.visibleColumns('reporterQualification')
                }]);
            aoColumns.push.apply(aoColumns, [
                {
                    "mData": "riskCategory",
                    'className': '',
                    "mRender": function (data, type, row) {
                        return '<span>' + row.riskCategory + '</span>'
                    },
                    "sortable": true,
                    "visible": signal.fieldManagement.visibleColumns('riskCategory')
                }]);
            aoColumns.push.apply(aoColumns, [
                {
                    "mData": "therapyDates",
                    'className': 'col-min-100 col-max-100 word-break',
                    "mRender": function (data, type, row) {
                        var colElement = '<div class="col-container"><div class="col-height">';
                        colElement += encodeToHTML(row.therapyDates);
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(row.therapyDates) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    },
                    "sortable": true,
                    "visible": signal.fieldManagement.visibleColumns('therapyDates')
                }]);

        }

        else if ($("#isVaers").val() == "true") {
            aoColumns.push.apply(aoColumns, [
                {
                    "mData": "outcome",
                    'className': 'col-min-100 col-max-100',
                    "mRender": function (data, type, row) {
                        return '<span>' +  addEllipsis(row.outcome) + '</span>'
                    },
                    "visible": signal.fieldManagement.visibleColumns('outcome')
                },
                {
                    "mData": "serious",
                    'className': '',
                    "mRender": function (data, type, row) {
                        return '<span>' + row.serious + '</span>'
                    },
                    "sortable": true,
                    "visible": signal.fieldManagement.visibleColumns('serious')
                },
                {
                    "mData": "death",
                    'className': '',
                    "mRender": function (data, type, row) {
                        return '<span>' + row.death + '</span>'
                    },
                    "sortable": true,
                    "visible": signal.fieldManagement.visibleColumns('death')
                },
                {
                    "mData": "timeToOnset",
                    'className': 'col-min-100 col-max-100',
                    "mRender": function (data, type, row) {
                        return '<span>' + row.timeToOnset + '</span>'
                    },
                    "visible": signal.fieldManagement.visibleColumns('timeToOnset')
                },
                {
                    "mData": "patientAge",
                    'className': 'col-min-75 col-max-75',
                    "mRender": function (data, type, row) {
                        return'<span>'+row.patientAge+'</span>'
                    },
                    "visible": signal.fieldManagement.visibleColumns('patientAge')
                },
                {
                    "mData": "gender",
                    'className': '',
                    "mRender": function (data, type, row) {
                        return '<span>' + row.gender + '</span>'
                    },
                    "sortable": true,
                    "visible": signal.fieldManagement.visibleColumns('gender')
                },
                {
                    "mData": "batchLotNo",
                    'className': 'col-min-100 col-max-100',
                    "mRender": function (data, type, row) {
                        var colElement = '<div class="col-container"><div class="col-height">';
                        colElement += encodeToHTML(row.batchLotNo);
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(row.batchLotNo) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    },
                    "visible": signal.fieldManagement.visibleColumns('batchLotNo')
                },
                {
                    "mData": "comments",
                    'className': 'col-min-200 col-max-200  cell-break',
                    "mRender": function (data, type, row) {
                        var colElement = '<div class="col-container"><div class="col-height commentDisplay">';
                        colElement += encodeToHTML(row.comments);
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + escapeHTML(encodeToHTML(row.comments)) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    },
                    "sortable": false,
                    "visible": signal.fieldManagement.visibleColumns('comments')
                }
            ]);
        } else {
            if($("#isFaers").val() != "true"){
                aoColumns.push.apply(aoColumns, [
                    {
                        "mData": "listedness",
                        "className": '',
                        "mRender": function (data, type, row) {
                            return '<span>' + row.listedness + '</span>'
                        },
                        "visible": signal.fieldManagement.visibleColumns('listedness')
                    }
                ]);
            }
                aoColumns.push.apply(aoColumns, [
                     {
                    "mData": "outcome",
                    'className': 'col-min-100 col-max-100',
                    "mRender": function (data, type, row) {
                        return '<span>' + addEllipsis(row.outcome) + '</span>'
                    },
                    "visible": signal.fieldManagement.visibleColumns('outcome')
                }
            ]);

            if ($("#isFaers").val() != "true" && $("#isAdhocCaseSeries").val() != "true") {
                aoColumns.push.apply(aoColumns, [
                    {
                        "mData": 'signalsAndTopics',
                        "mRender": function (data, type, row) {
                            var signalAndTopics = '';
                            $.each(row.signalsAndTopics, function (i, obj) {
                                var url = signalDetailUrl + '?id=' + obj['signalId'];
                                if ($('#hasSignalManagementAccess').val() == 'true') {
                                    signalAndTopics = signalAndTopics + '<span class="click box-inline word-wrap-break-word col-max-150"><a class="cell-break" title="' + obj.disposition.displayName + '" onclick="validateAccess(event,' + obj['signalId'] + ')" href="' + url + '">' + escapeHTML(obj['name']) + '</a></span>&nbsp;'
                                } else {
                                    signalAndTopics = signalAndTopics + '<span class="click box-inline word-wrap-break-word col-max-150 signalSummaryAuth "><a class="cell-break" title="' + obj.disposition.displayName + '" href="javascript:void(0)">' + escapeHTML(obj['name']) + '</a></span>&nbsp;'
                                }
                                signalAndTopics = signalAndTopics + ","
                            });
                            if (signalAndTopics.length > 1)
                                return '<div class="cell-break word-wrap-break-word col-max-150">' + signalAndTopics.substring(0, signalAndTopics.length - 1) + '</div>';
                            else
                                return '-';

                        },
                        "className": 'col-min-150 col-max-150 signalInformation',
                        "sortable": false,
                        "visible": signal.fieldManagement.visibleColumns('signalsAndTopics')
                    },
                    {
                        "mData": "currentDisposition",
                        "mRender": function (data, type, row, meta) {
                            return signal.utils.render('disposition_dss3', {
                                allowedDisposition: dispositionIncomingOutgoingMap[row.disposition],
                                currentDisposition: row.disposition,
                                forceJustification: forceJustification,
                                isReviewed: row.isReviewed,
                                isValidationStateAchieved: row.isValidationStateAchieved,
                                id: row.currentDispositionId,
                                useDropdown: (dispositionDropdownEnabled === 'true'),
                                rowIndex: meta.row
                            });
                        },
                        "visible": signal.fieldManagement.visibleColumns('currentDisposition'),
                        "className": 'col-max-150 col-min-150 dispositionAction'
                    },
                    {
                        "mData": "disposition",
                        "mRender": function (data, type, row) {
                            return row.disposition
                        },
                        "className": 'col-max-150 col-min-150 currentDisposition',
                        "visible": signal.fieldManagement.visibleColumns('disposition')
                    }
                ]);
            }
            if ($("#isFaers").val() != "true"  && $("#isAdhocCaseSeries").val() != "true") {
                aoColumns.push.apply(aoColumns, [
                    {
                        "mData": "assignedToUser",
                        "mRender": function (data, type, row) {
                            return signal.list_utils.assigned_to_comp(row.id, row.assignedToUser)
                        },
                        "className": 'col-max-150 col-min-150 assignedTo',
                        "sortable": false,
                        "visible": signal.fieldManagement.visibleColumns('assignedToUser')
                    },
                    {
                        "mData": "dueDate",
                        'className': 'dueIn col-min-150 col-max-150',
                        "mRender": function (data, type, row) {
                            return signal.list_utils.due_in_comp(row.dueIn)
                        },
                        "visible": signal.fieldManagement.visibleColumns('dueDate')
                    }]);
            }

            aoColumns.push.apply(aoColumns, [
                {
                    "mData": "suspProd",
                    'className': 'col-min-150 col-max-150',
                    "mRender": function (data, type, row) {
                        var colElement = '<div class="col-container"><div class="col-height">';
                        colElement += $("#isFaers").val() != "true" ? encodeToHTML(formatText(row.suspProd, true)) : encodeToHTML(row.suspProd);
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(row.suspProd) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    },
                    "sortable": true,
                    "visible": signal.fieldManagement.visibleColumns('suspProd')
                },
                {
                    "mData": "conComit",
                    'className': 'col-min-100 col-max-100',
                    "mRender": function (data, type, row) {
                        var colElement = '<div class="col-container"><div class="col-height">';
                        colElement += formatText(row.conComit);
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + formatText(row.conComit) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    },
                    "visible": signal.fieldManagement.visibleColumns('conComit')
                }]);
            aoColumns.push.apply(aoColumns, [
                {
                    "mData": "masterPrefTermAll",
                    'className': 'col-min-100 col-max-100',
                    "mRender": function (data, type, row) {
                        var ptList = '';
                        if (row.masterPrefTermAll) {
                            ptList = row.masterPrefTermAll.split('!@##@!').join(' ');
                            ptList = ptList.split('!@.@!').join(') ');
                        }
                        var colElement = '<div class="col-container"><div class="col-height">';
                        colElement += formatText(ptList);
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(formatText(ptList)) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    },
                    "sortable": true,
                    "visible": signal.fieldManagement.visibleColumns('masterPrefTermAll')
                }]);
            aoColumns.push.apply(aoColumns, [
                {
                    "mData": "serious",
                    'className': '',
                    "mRender": function (data, type, row) {
                        return '<span>' + row.serious + '</span>'
                    },
                    "visible": signal.fieldManagement.visibleColumns('serious')
                },
                {
                    "mData": "caseReportType",
                    "className": '',
                    "mRender": function (data, type, row) {
                        return '<span>' + row.caseReportType + '</span>'
                    },
                    "sortable": true,
                    "visible": signal.fieldManagement.visibleColumns('caseReportType')
                }]);
            aoColumns.push.apply(aoColumns, [
                {
                    "mData": "reportersHcpFlag",
                    'className': '',
                    "mRender": function (data, type, row) {
                        return '<span>' + row.reportersHcpFlag + '</span>'
                    },
                    "sortable": true,
                    "visible": signal.fieldManagement.visibleColumns('reportersHcpFlag')
                },
                {
                    "mData": "country",
                    'className': '',
                    "mRender": function (data, type, row) {
                        return '<span>' + row.country + '</span>'
                    },
                    "sortable": true,
                    "visible": signal.fieldManagement.visibleColumns('country')
                },
                {
                    "mData": "age",
                    'className': '',
                    "mRender": function (data, type, row) {
                        return '<span>' + row.age + '</span>'
                    },
                    "sortable": true,
                    "visible": signal.fieldManagement.visibleColumns('age')
                }]);
            aoColumns.push.apply(aoColumns, [
                {
                    "mData": "gender",
                    'className': '',
                    "mRender": function (data, type, row) {
                        return '<span>' + row.gender + '</span>'
                    },
                    "visible": signal.fieldManagement.visibleColumns('gender')
                }]);
            aoColumns.push.apply(aoColumns, [
                {
                    "mData": "rechallenge",
                    'className': '',
                    "mRender": function (data, type, row) {
                        return '<span>' + row.rechallenge + '</span>'
                    },
                    "sortable": true,
                    "visible": signal.fieldManagement.visibleColumns('rechallenge')
                },
                {
                    "mData": "lockedDate",
                    'className': 'col-min-100 col-max-100',
                    "mRender": function (data, type, row) {
                        return (data && data != undefined && data != "-") ? row.lockedDate : '-';
                    },
                    "sortable": true,
                    "visible": signal.fieldManagement.visibleColumns('lockedDate')
                },
                {
                    "mData": "death",
                    'className': '',
                    "mRender": function (data, type, row) {
                        return '<span>' + row.death + '</span>'
                    },
                    "visible": signal.fieldManagement.visibleColumns('death')
                },
                {
                    "mData": "patientAge",
                    'className': 'col-min-75 col-max-75',
                    "mRender": function (data, type, row) {
                        return '<span>' + row.patientAge + '</span>'
                    },
                    "visible": signal.fieldManagement.visibleColumns('patientAge')
                }]);

            if (customFieldsEnabled) {
                aoColumns.push.apply(aoColumns, [
                    {
                        "mData": "medErrorsPt",
                        'className': 'col-min-100 col-max-100 cell-break',
                        "mRender": function (data, type, row) {
                            var colElement = '<div class="col-container"><div class="col-height">';
                            colElement += row.medErrorsPt;
                            colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + row.medErrorsPt + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                            colElement += '</div></div>';
                            return colElement
                        },
                        "sortable": false,
                        "visible": signal.fieldManagement.visibleColumns('medErrorsPt')
                    },
                    {
                        "mData": "caseType",
                        'className': '',
                        "mRender": function (data, type, row) {
                            return '<span>' + row.caseType + '</span>'
                        },
                        "visible": signal.fieldManagement.visibleColumns('caseType')
                    },
                    {
                        "mData": "completenessScore",
                        'className': '',
                        "mRender": function (data, type, row) {
                            return '<span>' + row.completenessScore + '</span>'
                        },
                        "visible": signal.fieldManagement.visibleColumns('completenessScore')
                    },
                    {
                        "mData": "indNumber",
                        'className': '',
                        "mRender": function (data, type, row) {
                            return '<span>' + row.indNumber + '</span>'
                        },
                        "visible": signal.fieldManagement.visibleColumns('indNumber')
                    },
                    {
                        "mData": "appTypeAndNum",
                        'className': '',
                        "mRender": function (data, type, row) {
                            return '<span>' + row.appTypeAndNum + '</span>'
                        },
                        "visible": signal.fieldManagement.visibleColumns('appTypeAndNum')
                    },
                    {
                        "mData": "compoundingFlag",
                        'className': 'col-min-100 col-max-100',
                        "mRender": function (data, type, row) {
                            var colElement = '<div class="col-container"><div class="col-height">';
                            colElement += row.compoundingFlag;
                            colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + row.compoundingFlag + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                            colElement += '</div></div>';
                            return colElement
                        },
                        "visible": signal.fieldManagement.visibleColumns('compoundingFlag')
                    },
                    {
                        "mData": "submitter",
                        'className': '',
                        "mRender": function (data, type, row) {
                            return '<span>' + row.submitter + '</span>'
                        },
                        "visible": signal.fieldManagement.visibleColumns('submitter')
                    }
                ])
            }
            if($("#isFaers").val() != "true"){
                aoColumns.push.apply(aoColumns, [
                    {
                        "mData": "malfunction",
                        'className': '',
                        "mRender": function (data, type, row) {
                            return '<span>' + row.malfunction + '</span>'
                        },
                        "visible": signal.fieldManagement.visibleColumns('malfunction')
                    },
                    {
                        "mData": "comboFlag",
                        'className': '',
                        "mRender": function (data, type, row) {
                            return '<span>' + row.comboFlag + '</span>'
                        },
                        "visible": signal.fieldManagement.visibleColumns('comboFlag')
                    }
                ]);
            }

            aoColumns.push.apply(aoColumns, [

                {
                    "mData": "indication",
                    'className': 'col-min-150 col-max-150',
                    "mRender": function (data, type, row) {
                        var colElement = '<div class="col-container"><div class="col-height">';
                        colElement += encodeToHTML(row.indication);
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(row.indication) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    },
                    "visible": signal.fieldManagement.visibleColumns('indication')
                }
            ]);
            if ($("#isFaers").val() != "true") {
                aoColumns.push.apply(aoColumns, [
                    {
                        "mData": "eventOutcome",
                        'className': 'col-min-150 col-max-150',
                        "mRender": function (data, type, row) {
                            var colElement = '<div class="col-container"><div class="col-height">';
                            colElement += encodeToHTML(row.eventOutcome);
                            colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(row.eventOutcome) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                            colElement += '</div></div>';
                            return colElement
                        },
                        "visible": signal.fieldManagement.visibleColumns('eventOutcome')
                    },
                    {
                        "mData": "causeOfDeath",
                        'className': 'col-min-150 col-max-150',
                        "mRender": function (data, type, row) {
                            var colElement = '<div class="col-container"><div class="col-height">';
                            colElement += encodeToHTML(row.causeOfDeath);
                            colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(row.causeOfDeath) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                            colElement += '</div></div>';
                            return colElement
                        },
                        "visible": signal.fieldManagement.visibleColumns('causeOfDeath')
                    }
                ]);
            }
            aoColumns.push.apply(aoColumns, [
                {
                    "mData": "seriousUnlistedRelated",
                    'className': 'col-min-150 col-max-150',
                    "mRender": function (data, type, row) {
                        var colElement = '<div class="col-container"><div class="col-height">';
                        colElement += encodeToHTML(row.seriousUnlistedRelated);
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(row.seriousUnlistedRelated) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    },
                    "visible": signal.fieldManagement.visibleColumns('seriousUnlistedRelated')
                },
                {
                    "mData": "batchLotNo",
                    'className': 'col-min-100 col-max-100',
                    "mRender": function (data, type, row) {
                        var colElement = '<div class="col-container"><div class="col-height">';
                        colElement += encodeToHTML(row.batchLotNo);
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(row.batchLotNo) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    },
                    "visible": signal.fieldManagement.visibleColumns('batchLotNo')
                },
                {
                    "mData": "initialFu",
                    "mRender": function (data, type, row) {
                        return '<span>' + row.initialFu + '</span>'
                    },
                    "visible": signal.fieldManagement.visibleColumns('initialFu')
                }])
            if($("#isFaers").val() != "true"){
                aoColumns.push.apply(aoColumns, [
                    {
                        "mData": "caseClassification",
                        'className': 'col-min-150 col-max-150',
                        "mRender": function (data, type, row) {
                            var colElement = '<div class="col-container"><div class="col-height">';
                            colElement += encodeToHTML(row.caseClassification);
                            colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(row.caseClassification) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                            colElement += '</div></div>';
                            return colElement
                        },
                        "visible": signal.fieldManagement.visibleColumns('caseClassification')
                    },
                    {
                        "mData": "patientMedHist",
                        'className': 'col-min-150 col-max-150',
                        "mRender": function (data, type, row) {
                            var colElement = '<div class="col-container"><div class="col-height">';
                            colElement += $("<p/>").html(row.patientMedHist).text();
                            colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeHTMLTags(row.patientMedHist) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                            colElement += '</div></div>';
                            return colElement
                        },
                        "visible": signal.fieldManagement.visibleColumns('patientMedHist')
                    },
                    {
                        "mData": "patientHistDrugs",
                        'className': 'col-min-150 col-max-150',
                        "mRender": function (data, type, row) {
                            var colElement = '<div class="col-container"><div class="col-height">';
                            colElement += encodeToHTML(row.patientHistDrugs);
                            colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(row.patientHistDrugs) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                            colElement += '</div></div>';
                            return colElement
                        },
                        "visible": signal.fieldManagement.visibleColumns('patientHistDrugs')
                    },
                    {
                        "mData": "timeToOnset",
                        'className': 'col-min-100 col-max-100',
                        "mRender": function (data, type, row) {
                            return '<span>' + row.timeToOnset + '</span>'
                        },
                        "visible": signal.fieldManagement.visibleColumns('timeToOnset')
                    },
                    {
                        "mData": "protocolNo",
                        'className': 'col-min-150 col-max-150',
                        "mRender": function (data, type, row) {
                            var colElement = '<div class="col-container"><div class="col-height">';
                            colElement += encodeToHTML(row.protocolNo);
                            colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(row.protocolNo) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                            colElement += '</div></div>';
                            return colElement
                        },
                        "visible": signal.fieldManagement.visibleColumns('protocolNo')
                    }
                ]);
            }
                aoColumns.push.apply(aoColumns, [
                {
                    "mData": "isSusar",
                    "mRender": function (data, type, row) {
                        return '<span>' + row.isSusar + '</span>'
                    },
                    "visible": signal.fieldManagement.visibleColumns('isSusar')
                },
                {
                    "mData": "therapyDates",
                    'className': 'col-min-100 col-max-100 word-break',
                    "mRender": function (data, type, row) {
                        var colElement = '<div class="col-container"><div class="col-height">';
                        colElement += encodeToHTML(row.therapyDates);
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(row.therapyDates) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    },
                    "sortable": true,
                    "visible": signal.fieldManagement.visibleColumns('therapyDates')
                },
                {
                    "mData": "doseDetails",
                    'className': 'col-min-150 col-max-150 word-break',
                    "mRender": function (data, type, row) {
                        var colElement = '<div class="col-container"><div class="col-height">';
                        colElement += encodeToHTML(row.doseDetails);
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(row.doseDetails) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    },
                    "sortable": true,
                    "visible": signal.fieldManagement.visibleColumns('doseDetails')
                },
            ])
            if (customFieldsEnabled) {
                aoColumns.push.apply(aoColumns, [
                    {
                        "mData": "preAnda",
                        'className': 'col-min-150 col-max-150',
                        "mRender": function (data, type, row) {
                            var colElement = '<div class="col-container"><div class="col-height">';
                            colElement += encodeToHTML(row.preAnda);
                            colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(row.preAnda) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                            colElement += '</div></div>';
                            return colElement
                        },
                        "visible": signal.fieldManagement.visibleColumns('preAnda')
                    }
                ])
            }
            if ($("#isFaers").val() != "true" && $("#isAdhocCaseSeries").val() != "true") {
                aoColumns.push.apply(aoColumns, [
                    {
                        "mData": "justification",
                        'className': 'col-min-100 col-max-100 cell-break',
                        "mRender": function (data, type, row) {
                            var colElement = '<div class="col-container"><div class="col-height textPre">';
                            colElement += encodeToHTML(row.justification);
                            colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + escapeHTML(encodeToHTML(row.justification)) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                            colElement += '</div></div>';
                            return colElement
                        },
                        "sortable": true,
                        "visible": signal.fieldManagement.visibleColumns('justification')
                    },
                    {
                        "mData": "dispPerformedBy",
                        'className': 'col-min-100 col-max-100 cell-break',
                        "mRender": function (data, type, row) {
                            var colElement = '<div class="col-container"><div class="col-height">';
                            if (row.dispPerformedBy?.toUpperCase() === SYSTEM_USER) {
                                row.dispPerformedBy = row.dispPerformedBy.toUpperCase()
                            }
                            colElement += encodeToHTML(row.dispPerformedBy);
                            colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(row.dispPerformedBy) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                            colElement += '</div></div>';
                            return colElement
                        },
                        "sortable": true,
                        "visible": signal.fieldManagement.visibleColumns('dispPerformedBy')
                    }

                ]);
            }
            aoColumns.push.apply(aoColumns, [
                {
                    "mData": "comments",
                    'className': 'col-min-200 col-max-200  cell-break',
                    "mRender": function (data, type, row) {
                        var colElement = '<div class="col-container"><div class="col-height commentDisplay">';
                        colElement += encodeToHTML(row.comments);
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + escapeHTML(encodeToHTML(row.comments)) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    },
                    "sortable": false,
                    "visible": signal.fieldManagement.visibleColumns('comments')
                },
            ])
            if($("#isFaers").val() != "true") {
                aoColumns.push.apply(aoColumns, [
                    {
                        "mData": "primSuspProd",
                        'className': 'col-min-150 col-max-150',
                        "mRender": function (data, type, row) {
                            var colElement = '<div class="col-container"><div class="col-height">';
                            colElement += encodeToHTML(row.primSuspProd);
                            colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(row.primSuspProd) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                            colElement += '</div></div>';
                            return colElement
                        },
                        "sortable": true,
                        "visible": signal.fieldManagement.visibleColumns('primSuspProd')
                    }
                ])
            }
            if (customFieldsEnabled) {
                aoColumns.push.apply(aoColumns, [
                    {
                        "mData": "primSuspPai",
                        'className': 'col-min-150 col-max-150',
                        "mRender": function (data, type, row) {
                            var colElement = '<div class="col-container"><div class="col-height">';
                            colElement += encodeToHTML(formatText(row.primSuspPai));
                            colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(row.primSuspPai) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                            colElement += '</div></div>';
                            return colElement
                        },
                        "sortable": false,
                        "visible": signal.fieldManagement.visibleColumns('primSuspPai')
                    },
                    {
                        "mData": "paiAll",
                        'className': 'col-min-150 col-max-150',
                        "mRender": function (data, type, row) {
                            var allPaiList = '';
                            if (row.paiAll) {
                                allPaiList = row.paiAll.split('!@##@!').join(' ');
                                allPaiList = allPaiList.split('!@.@!').join(') ');
                            }
                            var colElement = '<div class="col-container"><div class="col-height">';
                            colElement += encodeToHTML(formatText(allPaiList));
                            colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(allPaiList) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                            colElement += '</div></div>';
                            return colElement
                        },
                        "sortable": false,
                        "visible": signal.fieldManagement.visibleColumns('paiAll')
                    }
                ])
            }
            aoColumns.push.apply(aoColumns, [
                {
                    "mData": "allPt",
                    'className': 'col-min-150 col-max-150',
                    "mRender": function (data, type, row) {
                        var allPtList = '';
                        if (row.allPt) {
                            allPtList = row.allPt.split('!@##@!').join(' ');
                            allPtList = allPtList.split('!@.@!').join(') ');
                        }
                        var colElement = '<div class="col-container"><div class="col-height">';
                        colElement += allPtList;
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(allPtList) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    },
                    "sortable": true,
                    "visible": signal.fieldManagement.visibleColumns('allPt')
                }
            ]);
            if ($("#isFaers").val() != "true" && $("#isAdhocCaseSeries").val() != "true") {
                aoColumns.push.apply(aoColumns, [
                    {
                        "mData": "dispLastChange",
                        'className': 'col-min-200 col-max-200 word-break',
                        "mRender": function (data, type, row) {
                            var colElement = '<div class="col-container"><div class="col-height">';
                            colElement += encodeToHTML(row.dispLastChange);
                            colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(row.dispLastChange) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                            colElement += '</div></div>';
                            return colElement
                        },
                        "sortable": true,
                        "visible": signal.fieldManagement.visibleColumns('dispLastChange')
                    }
                ]);
            }
            if ($("#isFaers").val() != "true") {
                aoColumns.push.apply(aoColumns, [
                    {
                        "mData": "genericName",
                        'className': 'col-min-150 col-max-150 cell-break',
                        "mRender": function (data, type, row) {
                            var colElement = '<div class="col-container"><div class="col-height">';
                            colElement += encodeToHTML(row.genericName);
                            colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(row.genericName) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                            colElement += '</div></div>';
                            return colElement
                        },
                        "visible": signal.fieldManagement.visibleColumns('genericName')
                    }
                ]);
                aoColumns.push.apply(aoColumns, [
                    {
                        "mData": "caseCreationDate",
                        'className': 'col-min-100 col-max-100',
                        "mRender": function (data, type, row) {
                            return '<span>' + row.caseCreationDate + '</span>'
                        },
                        "visible": signal.fieldManagement.visibleColumns('caseCreationDate')
                    }
                ]);
                aoColumns.push.apply(aoColumns, [
                    {
                        "mData": "dateOfBirth",
                        'className': 'col-min-100 col-max-100',
                        "sortable": $("#showDob").val() == "false" ? false : true,
                        "mRender": function (data, type, row) {
                            var dobString
                            var isBlinded = row.dateOfBirth == blinded || row.dateOfBirth == redacted;
                            if ($("#showDob").val() == "false" && !isBlinded) {
                                dobString = "#########"
                            } else {
                                dobString = row.dateOfBirth
                            }
                            return '<span>' + dobString + '</span>'
                        },
                        "visible": signal.fieldManagement.visibleColumns('dateOfBirth')
                    }
                ]);
                aoColumns.push.apply(aoColumns, [
                    {
                        "mData": "eventOnsetDate",
                        'className': 'col-min-100 col-max-100',
                        "mRender": function (data, type, row) {
                            return '<span>' + row.eventOnsetDate + '</span>'
                        },
                        "visible": signal.fieldManagement.visibleColumns('eventOnsetDate')
                    }
                ]);
                aoColumns.push.apply(aoColumns, [
                    {
                        "mData": "pregnancy",
                        'className': '',
                        "mRender": function (data, type, row) {
                            return '<span>' + row.pregnancy + '</span>'
                        },
                        "visible": signal.fieldManagement.visibleColumns('pregnancy')
                    }
                ]);
                aoColumns.push.apply(aoColumns, [
                    {
                        "mData": "medicallyConfirmed",
                        'className': '',
                        "mRender": function (data, type, row) {
                            return '<span>' + row.medicallyConfirmed + '</span>'
                        },
                        "visible": signal.fieldManagement.visibleColumns('medicallyConfirmed')
                    }
                ]);
                aoColumns.push.apply(aoColumns, [
                    {
                        "mData": "allPTsOutcome",
                        'className': 'col-min-150 col-max-150 cell-break',
                        "mRender": function (data, type, row) {
                            var colElement = '<div class="col-container"><div class="col-height">';
                            colElement += encodeToHTML(row.allPTsOutcome);
                            colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(row.allPTsOutcome) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                            colElement += '</div></div>';
                            return colElement
                        },
                        "visible": signal.fieldManagement.visibleColumns('allPTsOutcome')
                    }
                ]);
                aoColumns.push.apply(aoColumns, [
                    {
                        "mData": "coPackagedProduct",
                        'className': '',
                        "mRender": function (data, type, row) {
                            return '<span>' + row.coPackagedProduct + '</span>'
                        },
                        "visible": signal.fieldManagement.visibleColumns('coPackagedProduct')
                    }
                ]);
                if(customFieldsEnabled){
                    aoColumns.push.apply(aoColumns, [
                        {
                            "mData": "crossReferenceInd",
                            'className': 'col-min-150 col-max-150 cell-break',
                            "mRender": function (data, type, row) {
                                var colElement = '<div class="col-container"><div class="col-height">';
                                colElement += encodeToHTML(row.crossReferenceInd);
                                colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(row.crossReferenceInd) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                                colElement += '</div></div>';
                                return colElement
                            },
                            "visible": signal.fieldManagement.visibleColumns('crossReferenceInd')
                        }
                    ]);
                }
            }

        }
        if (callingScreen == CALLING_SCREEN.TRIGGERED_ALERTS) {
            aoColumns.splice(4, 0, {
                "mData": "name",
                "mRender": function (data, type, row) {
                    return '<span>' + encodeToHTML(row.alertName) + '</span>'
                },
                "visible": true
            });
        }
        return aoColumns
    };

    var createAlertListUrl = function (isFilterRequest, filterValues) {
        var listUrl = listConfigUrl + '&isFilterRequest=' + isFilterRequest + '&advancedFilterChanged=' + advancedFilterChanged + '&filters=' + encodeURIComponent(JSON.stringify(filterValues));
        if ($('#isFaers').val()) {
            listUrl += '&isFaers=' + $('#isFaers').val()
        }
        if(isTempViewSelected) {
            listUrl += "&tempViewId=" + tempViewPresent
        }
        return listUrl
    };

    var showAllSelectedCaseNumbers = function () {
        $('#copyCaseNumberModel').modal({
            show: true
        });

        var numbers = _.map($('input.copy-select:checked').parent().parent().find('td:nth-child(5)'), function (it) {
            return $(it).text()
        });
        $("#caseNumbers").text(numbers);
    };

    var initUpdateAssignedTo = function () {

        $("#updateAssignedToBtn").on('click', function () {
            $.ajax({
                type: "POST",
                url: $("#change-user-url").val(),
                data: {
                    alertId: $("#assignedToModal #alertId").val(),
                    assignedTo: $("#_assignedTo").find("option:selected").val()
                }
            }).done(function (data) {
                $("#assignedToModal").find('.form-control').val("")
                $("#assignedToModal").modal('hide')
                alertDetailsTable.ajax.reload()
            }).fail(function () {
            })
        });
    };

    var init_add_case_modal = function () {
        addCaseModalObj = $('#addCaseModal');
        addCaseModalObj.find('input:text').val('');
        addCaseModalObj.find('#justification').val("");
        addCaseModalObj.find('#importCasesExcel').prop('checked', false);
        addCaseModalObj.find('#importCasesSection').attr('hidden', 'hidden');
        addCaseModalObj.find('#caseNumber').prop('disabled', false).siblings('label').find('span').show()
        addCaseModalObj.find('#justificationListPriority').val('');
        addCaseModalObj.find('#addCaseButton').prop('disabled', false);
        addCaseModalObj.modal('show');

        addCaseModalObj.find('#importCasesExcel').on('click', function () {
            if (addCaseModalObj.find('#importCasesExcel').is(':checked')) {
                addCaseModalObj.find('#importCasesSection').removeAttr('hidden');
                addCaseModalObj.find('#versionNumber, #caseNumber').val('').attr('disabled', 'disabled').siblings('label').find('span').hide();
                $('.add-case-to-list').attr('disabled', 'disabled');
            } else {
                addCaseModalObj.find('#importCasesSection').attr('hidden', 'hidden');
                addCaseModalObj.find(':file').val('').parents('.input-group').find(':text').val('');
                addCaseModalObj.find('#versionNumber, #caseNumber').removeAttr('disabled').siblings('label').find('span').show();
                $('.add-case-to-list').removeAttr('disabled');
            }
        });


        $(document).on('change', '#justificationListPriority', function () {
            var value = $(this).val();
            if (value != "") {
                var text = justificationObj.filter(function (arr) {
                    return arr.id == value
                })[0].text;
                addCaseModalObj.find('#justification').val(text);
            } else {
                addCaseModalObj.find('#justification').val('');
            }
        });


        addCaseModalObj.on('change', ':file', function () {
            var input = $(this);
            var numFiles = input.get(0).files ? input.get(0).files.length : 0;
            var label = input.val().replace(/\\/g, '/').replace(/.*\//, '');
            var validExts = new Array(".xlsx", ".xls");
            var fileExt = label.substring(label.lastIndexOf('.'));
            if (numFiles > 0) {
                if (validExts.indexOf(fileExt.toLowerCase()) < 0) {
                    $('#fileFormatError').show();
                    $('.add-case-to-list').attr('disabled', 'disabled');
                } else {
                    $('#fileFormatError').hide();
                    $('.add-case-to-list').removeAttr('disabled');
                }
            } else {
                $('#fileFormatError').hide();
            }
            input.trigger('fileselect', [numFiles, label]);
        });

        addCaseModalObj.find(':file').on('fileselect', function (event, numFiles, label) {
            var input = $(this).parents('.input-group').find(':text');
            var log = numFiles > 0 ? label : "";

            if (input.length) {
                input.val(log);
            }
        });
    };

    var add_case_to_list = function () {
        addCaseModalObj.find('#addCaseButton').attr('disabled', 'disabled');
        addCaseModalObj.find('#importCasesExcel').prop('disabled', true);
        addCaseModalObj.find('#justificationListPriority').prop('disabled', true);
        var formData = new FormData(addCaseModalObj.find('#addNewCase')[0]);
        if (addCaseModalObj.find(':file').val()) {
            formData.append("file", $('#file_input').get(0).files[0]);
        }
        $.ajax({
            url: "/signal/singleCaseAlert/addCaseToSingleCaseAlert",
            type: 'POST',
            data: formData,
            mimeType: "multipart/form-data",
            contentType: false,
            cache: false,
            processData: false,
            beforeSend: function () {
                addCaseModalObj.find("div.isProcessing").show();
            },
        })
            .done(function (data) {
                if (!JSON.parse(data).success) {
                    addCaseModalObj.find('#addCaseButton').removeAttr('disabled');
                    addCaseModalObj.find('.alert-danger').removeClass('hide');
                    addCaseModalObj.find("div.isProcessing").hide();
                    addCaseModalObj.find('#importCasesExcel').prop('disabled', false);
                    addCaseModalObj.find('#justificationListPriority').prop('disabled', false);
                    addCaseModalObj.find('.errorMessageSpan').html(JSON.parse(data).message);
                    $('.loading').css("display","none");
                    setTimeout(function () {
                        addCaseModalObj.find('.alert-danger').addClass('hide');
                    }, 5000)
                } else {
                    addCaseModalObj.find('#addCaseButton').removeAttr('disabled');
                    addCaseModalObj.find('#importCasesExcel').prop('disabled', false);
                    addCaseModalObj.find('#justificationListPriority').prop('disabled', false);
                    addCaseModalObj.find("div.isProcessing").hide();
                    addCaseModalObj.find('#importCasesSection').attr('hidden', 'hidden');
                    addCaseModalObj.find(':file').val('').parents('.input-group').find(':text').val('');
                    addCaseModalObj.find('#versionNumber, #caseNumber').removeAttr('disabled').siblings('label').find('span').show();
                    addCaseModalObj.modal('hide');
                    reloadData("", true);
                    showSuccessNotification(JSON.parse(data).message);
                }
            })
            .fail(function (err) {
                addCaseModalObj.find("i.isProcessing").hide();
                addCaseModalObj.find('#importCasesExcel').prop('disabled', false);
                addCaseModalObj.find('#justificationListPriority').prop('disabled', false);
            });
    };

    function showSuccessNotification(message) {
        $(".alert-success").alert('close');
        if (message != undefined && message != "")
            $('#detailsTab').prepend(
                '<div class="alert alert-success alert-dismissable">' +
                '<button type="button" class="close" ' +
                'data-dismiss="alert" aria-hidden="true">' +
                '&times;' +
                '</button>' +
                encodeToHTML(message) +
                '</div>'
            );
    }


    var reloadData = function (rowId, resetPagination) {
        if (resetPagination != true) {
            resetPagination = false
        }
        var dataTable = $("#alertsDetailsTable").DataTable();
        dataTable.ajax.reload(function () {
            highlightRow(rowId, dataTable);
        }, resetPagination);
    };

    var highlightRow = function (rowId, dataTable) {
        if (rowId != undefined && rowId != "") {
            dataTable.row('#' + rowId).nodes()
                .to$()
                .addClass('flash-row');
        }
    };


    var init = function () {
        signal.fieldManagement.populateColumnList(gridColumnsViewUrl, gridColumnsViewUpdateUrl);
        alertDetailsTable = initAlertDetailsTable();

        $('i#copySelection').on('click', function () {
            showAllSelectedCaseNumbers()
        });

        $(document).on('click', 'input#select-all', function () {
            $(".copy-select").prop('checked', this.checked);
            $(".alert-select-all").prop('checked', this.checked);
            if (typeof isCaseDetailView !== "undefined" && isCaseDetailView == "true") {
                checkboxSelector = 'table#alertsDetailsTable .copy-select';
            } else {
                checkboxSelector = '.copy-select';
            }
            $.each($(checkboxSelector), function () {
                if(selectedCases.indexOf($(this).attr("data-id")) == -1 && $(this).is(':checked')){
                    selectedCases.push($(this).attr("data-id"));
                    var selectedRowIndex = $(this).closest('tr').index();
                    if (isAbstractViewOrCaseView(selectedRowIndex)) {
                        selectedRowIndex = selectedRowIndex / 2
                    }
                    selectedCasesInfo.push(populateDispositionDataFromGrid(selectedRowIndex));
                } else if(selectedCases.indexOf($(this).attr("data-id")) != -1 && !$(this).is(':checked')){
                    selectedCases.splice( $.inArray($(this).attr("data-id"), selectedCases), 1 );
                    selectedCasesInfo.splice($.inArray($(this).attr("data-id"), selectedCases), 1);
                }
            })
        });

        initUpdateAssignedTo()

    };
    exportCaseForm();

    init();
    $(document).on('click', '.alert-check-box', function () {
        if(!this.checked){
            $('.alert-select-all').prop('checked', false);
        }
    });
    $(document).on('click', '#report a.m-r-10.grid-menu-tooltip.text-left-prop, #ic-report', function () {
        populateSelectedCases();
        var prefix = "sca_";
        var isFilterRequest = true;
        var filterValues = [];
        var id = sessionStorage.getItem(prefix + "id");
        if (window.sessionStorage) {
            if (signal.alertReview.isAlertPersistedInSessionStorage(prefix)) {
                filterValues = JSON.parse(sessionStorage.getItem(prefix + "filters_value"));
            } else {
                signal.alertReview.removeFiltersFromSessionStorage(prefix);
                isFilterRequest = false;
            }
        }

        var filterList = {};
        $(".yadcf-filter-wrapper").each(function () {
            var filterVal = $(this).children().val();
            if (filterVal) {
                filterList[$(this).prev().attr('data-field')] = filterVal
            }
        });
        var filterListJson = JSON.stringify(filterList);
        signal.utils.postUrl(reportTemplateUrl, {
            selectedCases: selectedCases,
            viewId: viewId,
            tempViewId: tempViewPresent,
            isFilterRequest: isFilterRequest,
            filters: JSON.stringify(filterValues),
            filterList: filterListJson,
            advancedFilterId: $('#advanced-filter').val()?$('#advanced-filter').val():'',
            isAggScreen : $("#isCaseSeries").val() === "true",
        }, true);
        sessionStorage.setItem(prefix + "filters_value", JSON.stringify(filterValues));
        sessionStorage.setItem(prefix + "id", id);

        return false
    });

    $('#exportTypes a[href]').on('click', function (e) {
        if(callingScreen == CALLING_SCREEN.DASHBOARD) {
                if ($('#alertsDetailsTable').DataTable().page.info().recordsDisplay > 5000) {
                    $.Notification.notify('warning', 'top right', "Warning", "Only first 5000 records will be exported. Please use the filter criteria to limit your search.", {autoHideDelay: 5000});
                }
        }
        var clickedURL = e.currentTarget.href;
        var ids = [];
        var lengthVal = $('.alert-check-box').length;
        if (typeof isCaseDetailView !== "undefined" && isCaseDetailView == "true") {
            $('.alert-check-box').each(function () {
                if ($(this)[0]['checked']) {
                    ids.push($(this).attr('data-id'))
                }
            });
        } else {
            $('.alert-check-box').slice(lengthVal / 2, lengthVal).each(function () {
                if (this.checked) {
                    ids.push($(this).attr('data-id'));
                }
            });
        }
        var isSafety=true;
        if($("#isFaers").val() == "true" || $("#isVaers").val() == "true" || $("#isVigibase").val() == "true" ||  $("#isJader").val() == "true") {
            isSafety=false;
        }
        clickedURL += "&isSafety=" + isSafety;
        var isJader=false;
        if($("#isJader").val() == "true"){
            isJader=true;
        }
        clickedURL += "&isJader=" + isJader;
        clickedURL += '&aggExecutionId=' + $('#aggExecutionId').val()

        if($(this).closest('li').attr('class')=='generate-case-form') {
            ids=selectedCases
        }

        var checkedColumns = {};
        $('#tableColumns tr ').each(function () {
            var fieldName = $(this).children().children().attr('data-field');
            if (fieldName) {
                checkedColumns[fieldName] = $(this).children()[1].firstChild.checked
            }
        });


        if (ids.length > 0) {
            updatedExportUrl = clickedURL + "&selectedCases=" + ids + "&viewId=" + viewId + "&isFaers=" + $('#isFaers').val();
        }
        else {

            var prefix = "sca_";
            var isFilterRequest = true;
            var filterValues = [];
            var id = sessionStorage.getItem(prefix + "id");
            if (window.sessionStorage) {
                if (signal.alertReview.isAlertPersistedInSessionStorage(prefix)) {
                    filterValues = JSON.parse(sessionStorage.getItem(prefix + "filters_value"));
                } else {
                    signal.alertReview.removeFiltersFromSessionStorage(prefix);
                    isFilterRequest = false;
                }
            }
            $.each($(".disposition-ico").eq(0).find("li"),function (id,val) {
                if($(val).find('input').is(':checked') === true){
                    filterValues.push($(val).find('input').val());
                }
            });

            var filterList = {};
            $(".yadcf-filter-wrapper").each(function () {
                var filterVal = $(this).children().val();
                if (filterVal) {
                    filterList[$(this).prev().attr('data-field')] = filterVal
                }
            });
            var filterListJson = JSON.stringify(filterList);
            updatedExportUrl = clickedURL + '&isFilterRequest=' + isFilterRequest + '&filters=' + encodeURIComponent(JSON.stringify(filterValues)) + "&viewId=" + viewId + "&filterList=" + encodeURIComponent(filterListJson) + "&isFaers=" + $('#isFaers').val();
            if ($('#advanced-filter').val()) {
                updatedExportUrl += "&advancedFilterId=" + $('#advanced-filter').val()
            }

        }
        if(isTempViewSelected) {
            updatedExportUrl += "&tempViewId=" + tempViewPresent;
        }
        if($(this).closest('li').attr('class')=='generate-case-form') {
            $("#form-name-error").closest(".alert-dismissible").addClass("hide");
            $("#case-form-name-modal").find('#case-form-file-name').val("")
            $("#case-form-name-modal").modal('show');
            fetchCaseFormNames();
            $("#case-form-name-modal").find('#case-form-url').val(updatedExportUrl);
        } else {
            window.location.href = updatedExportUrl;
            sessionStorage.setItem(prefix + "filters_value", JSON.stringify(filterValues));
            sessionStorage.setItem(prefix + "id", id);
        }
        return false
    });

    $(".exportAlertHistories").on('click', function (e) {
        var searchField = $('#caseHistoryModalTable_filter').find('.form-control.dt-search').val();
        var otherSearchString = $('#caseHistoryModalTableSuspect_filter').find('.form-control.dt-search').val();
        var table = $("#caseHistoryModalTable").DataTable()
        var otherAlertTable =$("#caseHistoryModalTableSuspect").DataTable()
        var otherAlertConfigIds = []
        var alertConfigIds = []
        if(searchField !== "") {
            $.each(table.rows({filter: 'applied'}).data(), function (idx, val) {
                alertConfigIds.push(val.id);
            });
        }
        if(otherSearchString !== "") {
            $.each(otherAlertTable.rows({filter: 'applied'}).data(), function (idx, val) {
                otherAlertConfigIds.push(val.id);
            });
        }
        var caseHistoryModal = $('#caseHistoryModal');
        var caseVal = caseHistoryModal.find("#actualCaseNumber").val();
        var productFamily = caseHistoryModal.find("#productFamily").html();
        var alertConfigId = caseHistoryModal.find("#alertConfigId").val();
        var caseVersion = caseHistoryModal.find("#caseVersion").val();
        var sorted =  $('#caseHistoryModalTable').DataTable().order()[0][0];
        var order = $('#caseHistoryModalTable').DataTable().order()[0][1];
        var sorted2 = $("#caseHistoryModalTableSuspect").DataTable().order()[0][0];
        var order2 = $("#caseHistoryModalTableSuspect").DataTable().order()[0][1];
        var selectedCase = e.currentTarget.href + "&caseNumber=" + caseVal + "&productFamily=" + encodeURIComponent(productFamily) + "&alertConfigId=" + alertConfigId + "&caseVersion=" + caseVersion + "&sorted=" + sorted + "&order=" + order + "&sorted2=" + sorted2 + "&order2=" + order2 + "&caseVisibility=" + caseHistoryModal.find("#caseNumber").html() + "&searchField=" + encodeURIComponent(searchField) + "&alertConfigIds=" + alertConfigIds + "&otherAlertConfigIds=" + otherAlertConfigIds + "&otherSearchString=" + encodeURIComponent(otherSearchString);
        window.location.href = selectedCase;
        return false
    });

    $("#alertsDetailsTable_filter").hide();


    $('.saveCases').on('click', function () {
        $("#showConfigNameModal").modal('show');
        $("#save-cases-btn").on('click', function () {
            $('#save-cases-btn, #cancel-btn').attr("disabled", "disabled");
            var saveScaCasesUrl = saveCasesUrl + "&configName=" + $('#configName').val();
            var currntUrl = window.location.href
            if(currntUrl.charAt(currntUrl.length-1) === "#"){
                currntUrl = currntUrl.substring(0,currntUrl.length-2)
            }
            currntUrl += "&isCaseSeriesAlert=true";
            $.ajax({
                url: saveScaCasesUrl
            })
                .done(function (result) {
                    if (result.success) {
                        $('#alertMessage').show();
                        setTimeout(function () {
                            $('#save-cases-btn, #cancel-btn').removeAttr("disabled");
                            $('#configName').val('');
                            $('#showConfigNameModal').modal('hide');
                        }, 2000);
                        window.location.replace(currntUrl)
                    } else {
                        $('#errorMessage').show();
                        setTimeout(function () {
                            $('#save-cases-btn, #cancel-btn').removeAttr("disabled");
                            $('#configName').val('');
                            $('#errorMessage').hide();
                        }, 1000);

                    }
                })
        })
    });

    $(document).on('click', '#detailed-view-checkbox,#ic-detailed-view-checkbox', function () {
        var $this = $(this);
        var reloadUrl = $this.data('url') + '?archived=' + isArchived+ '&callingScreen=' + $this.data('callingScreen') + '&configId=' + $this.data('configId') + '&isCaseDetailView=' + $this.data('isCaseDetailView')+ '&isCaseSeries=' + isCaseSeries + '&productName='+ $this.data('productName') + '&eventName='+ $this.data('eventName') + '&soc=' + $this.data('soc') + '&detailedAdvancedFilterId='+  $('#advanced-filter').val() + '&aggExecutionId=' + $('#aggExecutionId').val() + '&aggAlertId=' + $('#alert-Id').val() + '&type=' + $('#type-name').val() + '&typeFlag=' + $('#type-flag-name').val() + "&version=" + $('#versionNo').val();
        if(isTempViewSelected) {
            reloadUrl = $this.data('url') + '?archived=' + isArchived+ '&callingScreen=' + $this.data('callingScreen') + '&configId=' + $this.data('configId') + "&tempViewId=" + tempViewPresent + '&isCaseDetailView=' + $this.data('isCaseDetailView') + '&productName='+ $this.data('productName') + '&eventName='+ $this.data('eventName') + '&aggExecutionId=' + $('#aggExecutionId').val() + '&aggAlertId=' + $('#alert-Id').val() + '&type=' + $('#type-name').val() + '&typeFlag=' + $('#type-flag-name').val() + "&version=" + $('#versionNo').val();
        }
        if ($("#isFaers").val()) {
            reloadUrl += '&isFaers=' + $("#isFaers").val();
        }
        if ($("#isVaers").val()) {
            reloadUrl += '&isVaers=' + $("#isVaers").val();
        }
        if ($("#isVigibase").val()) {
            reloadUrl += '&isVigibase=' + $("#isVigibase").val();
        }
        if ($("#isJader").val()) {
            reloadUrl += '&isJader=' + $("#isJader").val();
        }
        if(!isTempViewSelected) {
            reloadUrl += '&viewId=' + viewId;
        }
        isUpdateTemp = false
        window.location.href = reloadUrl

    });
    var executionStatusCurrent = $('.generateCurrentAnalysis').attr('data-status')
    set_disable_click($('.generateCurrentAnalysis'), executionStatusCurrent,'Current Period Analysis');

    var executionStatusCumulative = $('.generateCumulativeAnalysis').attr('data-status')
    set_disable_click($('.generateCumulativeAnalysis'), executionStatusCumulative,'Cumulative Period Analysis');

    $(document).on('click', '.generateCurrentAnalysis', function(){
        generate_analysis($('.generateCurrentAnalysis'), 'PR_DATE_RANGE', 'Current Period Analysis', 'Current');
    });
    $(document).on('click', '.generateCumulativeAnalysis', function(){
        generate_analysis($('.generateCumulativeAnalysis'), 'CUMULATIVE', 'Cumulative Period Analysis', 'Cumulative');
    });

    $(document).on('click', '.scaCaseNumber', function (e) {
        e.preventDefault();
        var scaAlertId = $(this).find("span[data-field=alertId]").attr("data-id");
        var caseVersion = $(this).find("span[data-field=caseVersion]").attr("data-id");
        var caseNumber = $(this).find("span[data-field=caseNumber]").attr("data-id");
        var execConfigId = $(this).find("span[data-field=execConfigId]").attr("data-id");
        var followUpNumber = $(this).find("span[data-field=followUpNumber]").attr("data-id");
        var isJader = $("#isJader").val();
        var caseVisibility = $(this).find("span[data-field=caseNumber]").html();
        if(hasReviewerAccess) {
            $.ajax({
                url: updateAutoRouteDispositionUrl,
                data: {'id': scaAlertId, 'isArchived': isArchived, 'isJader': isJader},
            })
                .done(function (result) {
                    if (result.status) {
                        alertDetailsTable.ajax.reload();
                        var selectedDispositionParent = {title:result.data};
                        changeDispositionFilters(selectedDispositionParent);
                    }
                    openCaseDetailsPage(caseNumber, caseVersion, followUpNumber, scaAlertId, execConfigId)
                })
                .fail(function () {
                    console.error("Some error occured while updating AutoRoute Disposition");
                });
        } else {
            $.ajax({
                url: isWarningMessageUrl,
                data: {'id': scaAlertId, 'isArchived': isArchived},
            })
                .done(function (result) {
                    if (result.status) {
                        $.Notification.notify('warning', 'top right', "Warning", "You don't have access to perform auto route disposition", {autoHideDelay: 5000});
                    }
                })
                .fail(function () {
                    $.Notification.notify('warning', 'top right', "Warning", "You don't have access to perform auto route disposition", {autoHideDelay: 5000});
                });
            openCaseDetailsPage(caseNumber, caseVersion, followUpNumber, scaAlertId, execConfigId)
        }
    });

    registerUnselectEventForCaseSeriesTag();
    registerUnselectEventForGlobalTag();
    registerSelectEventForCaseSeriesTag();
    registerSelectEventForGlobalTag();
    $('#alertTagModal').on('shown.bs.modal', function () {
        tagsObj.addedCaseSeriesTags = [];
        tagsObj.addedGlobalTags = [];
        tagsObj.deletedCaseSeriesTags = [];
        tagsObj.deletedGlobalTags = [];
    })
    $(document).on('change', '.copy-select', function(){
        addToSelectedCheckBox($(this))
    });

    setInterval(fetchIfTempAvailable, clipboardInterval);

    $("#exportExcel,#cumulativeExport").on("click",function(event){
        event.preventDefault();
        var href = updatedExportUrl;
        var isSafety=true;
        if($("#isFaers").val() == "true" || $("#isVaers").val() == "true" || $("#isVigibase").val() == "true" ||  $("#isJader").val() == "true") {
            isSafety=false;
        }
        if(promptUser === "true" && isSafety) {
            window.stop();
            $(".ul-ddm").hide();

            var onCancel = function(){
                if ($('#alertsDetailsTable').DataTable().page.info().recordsDisplay > 1000) {
                    $.Notification.notify('warning', 'top right', "Warning", $.i18n._('warningMessageForICRExport'), {autoHideDelay: 5000});
                    // Added as a part of  PVS-70067
                }
                window.location.href = href += "&promptUser=" + true;
            }

            var onSubmit = function(){
                if ($('#alertsDetailsTable').DataTable().page.info().recordsDisplay > 1000) {
                    $.Notification.notify('warning', 'top right', "Warning", $.i18n._('warningMessageForICRExport'), {autoHideDelay: 5000});
                    // Added as a part of  PVS-70067
                }
                window.location.href = href += "&promptUser=" + false;
            }

            var caseNarrativeDialog = bootbox.dialog({
                title: 'Case Narrative Export',
                message: "Do you want to include the case narrative as part of the export?",
                onEscape: true,
                buttons: {
                    cancel: {
                        label: 'Include Case Narrative',
                        className: 'btn-primary',
                        callback: onCancel
                    },
                    confirm: {
                        label: 'Don\'t Include Case Narrative',
                        className: 'btn-default',
                        callback: onSubmit
                    }
                }
            });
        }
        else {
            if ($('#alertsDetailsTable').DataTable().page.info().recordsDisplay > 1000) {
                $.Notification.notify('warning', 'top right', "Warning", $.i18n._('warningMessageForICRExport'), {autoHideDelay: 5000});
                // Added as a part of  PVS-70067
            }
            window.location.href = href;
        }
    });
});

function openCaseDetailsPage(caseNumber, caseVersion, followUpNumber, scaAlertId, execConfigId, caseVisibility) {
    signal.utils.postUrl(caseDetailUrl, {
        caseNumber: caseNumber,
        version: caseVersion,
        followUpNumber: followUpNumber,
        alertId: scaAlertId,
        isFaers: $("#isFaers").val(),
        isVaers: $("#isVaers").val(),
        isVigibase: $("#isVigibase").val(),
        isJader: $("#isJader").val(),
        fullCaseList: $("#fullCaseList").val(),
        execConfigId: execConfigId,
        isArchived: isArchived,
        totalCount: table.page.info().recordsTotal,
        detailsParameters: JSON.stringify(detailsParameters),
        isSingleAlertScreen: true,
        isVersion: true,
        isCaseSeries : $("#isCaseSeries").val() == "true",
        oldFollowUp : followUpNumber,
        oldVersion : caseVersion,
        isAggregateAdhoc:isAggregateAdhoc,
        caseVisibility: caseVisibility
    }, true);
}

var format = function (d, className) {
    var customFieldsEnabled = JSON.parse($("#customFieldsEnabled").val());
    var productList = d.suspProd;
    var productArray = productList.split(',');
    var ptArray = d.ptList.split(new RegExp('!@##@!|\r\n', 'g'));
    var patMedHist = $("<p/>").html(d.patientMedHist).text();
    var isVaers = ($("#isVaers").val() == undefined || $("#isVaers").val() == "false" || $("#isVaers").val() == "" || $("#isVaers").val() == false) ? "false":"true";
    patMedHist = patMedHist.replaceAll("</br>","")
    var resultedObj = {
        productList: productArray.join(','),
        masterPrefTermAll: d.masterPrefTermAll,
        caseNarrative: d.caseNarrative,
        conMeds: removeLineBreak(d.conMeds),
        medErrorsPt: d.medErrorsPt,
        causeOfDeath: d.causeOfDeath,
        patientMedHist: patMedHist,
        patientHistDrugs: d.patientHistDrugs,
        batchLotNo: d.batchLotNo,
        therapyDates: d.therapyDates,
        doseDetails: d.doseDetails,
        primSuspProd: d.primSuspProd,
        primSuspPai: d.primSuspPai,
        paiAll: d.paiAll,
        isVaers: isVaers,
        allPt: removeLineBreak(d.allPt),
        customFieldsEnabled : customFieldsEnabled,
        columnLabelMap: columnLabelMapForDetail,
        crossReferenceInd: d.crossReferenceInd
    };
    html = signal.utils.render("single_case_details_child_view_5.6.1", resultedObj);
    return html;
};

var detailsViewFormatVB = function (d, className) {
    var customFieldsEnabled = JSON.parse($("#customFieldsEnabled").val())
    var productList = d.suspProd;
    var productArray = productList.split(',');
    var resultedObj = {
        productList: productArray.join(','),
        caseInitReceiptDate: (d.caseInitReceiptDate != undefined) ? moment.utc(d.caseInitReceiptDate).format(DEFAULT_DATE_FORMAT) : '',
        productName: d.productName,
        pt: d.pt,
        age: d.age,
        conMeds: removeLineBreak(d.conMeds),
        region: d.region,
        gender: d.gender,
        caseReportType: d.caseReportType,
        eventOutcome: d.eventOutcome,
        serious: d.serious,
        indication: d.indication,
        initialFu: d.initialFu,
        comments: d.comments,
        allPt: removeLineBreak(d.allPt),
        customFieldsEnabled : customFieldsEnabled,
        alertTags:getCategoriesArray(d.alertTags)
    };
    html = signal.utils.render("single_case_details_vb_child_view_5.5_1", resultedObj);
    return html;
};
var detailsViewFormatJADER = function (d, className) {
    var customFieldsEnabled = JSON.parse($("#customFieldsEnabled").val())
    var productList = d.suspProd;
    var productArray = productList.split(',');
    var patMedHist = $("<p/>").html(d.patientMedHist).text();
    patMedHist = patMedHist.replaceAll("</br>","")
    var resultedObj = {
        productList: productArray.join(','),
        caseInitReceiptDate: (d.caseInitReceiptDate != undefined) ? moment.utc(d.caseInitReceiptDate).format(DEFAULT_DATE_FORMAT) : '',
        pt: d.pt,
        conMeds: removeLineBreak(d.conMeds),
        therapyDates: d.therapyDates,
        doseDetails: d.doseDetails,
        patientMedHist: patMedHist,
        patientHistDrugs: d.patientHistDrugs,
        allPt: removeLineBreak(d.allPt)
    };
    html = signal.utils.render("single_case_details_child_view_jd_6.1", resultedObj);
    return html;
};


function getCategoriesArray(tags){
    var alertTagsArray = []
    $.each(tags, function () {
        var tagString = ""
        if(this.subTagText == null){
            tagString = tagString + this.tagText + this.privateUser + this.tagType
        }else{
            var subTags = this.subTagText.split(";").join("(S);")
            tagString = tagString + this.tagText + this.privateUser + this.tagType + ":"+ subTags + "(S)"
        }
        alertTagsArray.push(tagString)
    });
    return alertTagsArray.join(",")
}


function showCaseDetailsView() {
    $("tr.data-table-row").each(function (index) {
        var tr = $(this);
        var row = window.sca_data_table.row(tr);
        var className = $(this).hasClass('odd') ? 'odd' : 'even';
        if($("#isJader").val() == "true"){
            row.child(detailsViewFormatJADER(row.data()), className).show();
        }else if (!($("#isVigibase").val() == "true")) {
            row.child(format(row.data()), className).show();
        } else {
            row.child(detailsViewFormatVB(row.data()), className).show();
        }
    });
}

function registerUnselectEventForCaseSeriesTag() {
    $(document).on("select2:unselect", '#singleAlertTags', function (e) {
        var tag = e.params.data.text;
        if (!tagsObj.deletedCaseSeriesTags.includes(tag)) {
            tagsObj.deletedCaseSeriesTags.push(tag);
            tagsObj.addedCaseSeriesTags = tagsObj.addedCaseSeriesTags.remove(tag);
        }
    });
}

function registerUnselectEventForGlobalTag() {
    $(document).on("select2:unselect", '#globalTags', function (e) {
        var tag = e.params.data.text;
        if (!tagsObj.deletedGlobalTags.includes(tag)) {
            tagsObj.deletedGlobalTags.push(tag);
            tagsObj.addedGlobalTags = tagsObj.addedGlobalTags.remove(tag);
        }
    });
}

function registerSelectEventForCaseSeriesTag() {
    $(document).on("select2:selecting", '#singleAlertTags', function (e) {
        var tag = e.params.args.data.id;
        if (!tagsObj.addedCaseSeriesTags.includes(tag)) {
            tagsObj.addedCaseSeriesTags.push(tag);
            tagsObj.deletedCaseSeriesTags = tagsObj.deletedCaseSeriesTags.remove(tag);
        }
    });
}

function registerSelectEventForGlobalTag() {
    $(document).on("select2:selecting", '#globalTags', function (e) {
        var tag = e.params.args.data.id;
        if (!tagsObj.addedGlobalTags.includes(tag)) {
            tagsObj.addedGlobalTags.push(tag);
            tagsObj.deletedGlobalTags = tagsObj.deletedGlobalTags.remove(tag);
        }
    });
}

$(document).on('click', '.signalSummaryAuth', function (evt) {
    $.Notification.notify('warning','top right', "Warning", "You don't have access to view signal summary", {autoHideDelay: 5000});
});

var fetchTags = function () {
    $.ajax({
        url: fetchCommonTagsUrl,
        async: false,
    })
        .done(function (result) {
            result.commonTagList.forEach(function(map){
                tags.push({
                    id: map.id,
                    text: map.text,
                    parentId: map.parentId,
                    display: map.display
                });
            });
        });
};
