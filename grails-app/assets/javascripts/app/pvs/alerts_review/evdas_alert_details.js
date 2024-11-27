//= require vendorUi/highcharts/highcharts-10.3.3/highcharts
//= require vendorUi/highcharts/highcharts-10.3.3/highcharts-3d
//= require vendorUi/highcharts/highcharts-10.3.3/highcharts-more
//= require vendorUi/highcharts/themes/grid-rx
//= require app/pvs/common/rx_common.js
//= require app/pvs/common/rx_alert_utils.js
//= require app/pvs/common/rx_list_utils.js
//= require app/pvs/activity/activities.js
//= require app/pvs/actions/actions.js
//= require app/pvs/common/rx_handlebar_ext.js
//= require app/pvs/alertComments/alertComments.js
//= require app/pvs/alerts_review/alert_review
//= require app/pvs/caseDrillDown/evdasCaseDrillDown.js
//= require app/pvs/dataTableActionButtons.js
//= require app/pvs/alerts_review/archivedAlerts.js

var userLocale = "en";
var applicationName = "EVDAS Alert";
var applicationLabel = "EVDAS Alert";
var executedIdList = [];
var table;
var dir = '';
var index = -1;
var isDynamicFilterApplied = false;
var evdasQueryJson='';

var prev_page = [];
var selectedCases = [];
var selectedCasesInfo = [];
var allVisibleIds = [];
var ecaEntriesCount=sessionStorage.getItem('ecaPageEntries')!=null?sessionStorage.getItem('ecaPageEntries'):50
var fixedFilterColumn = []


$(function () {
    $(document).on('fieldsRearranged', function (e) {
        $('.yadcf-filter-wrapper').remove();
        init_filter($('#alertsDetailsTable').DataTable())
    })
    var buttonClassVar = "";
    if(typeof buttonClass !=="undefined" && buttonClass){
        buttonClassVar = buttonClass;
    }
    $('a[href="#details"]').on("shown.bs.tab", function (e) {
        $('#alertsDetailsTable').DataTable().columns.adjust();
        addGridShortcuts('#alertsDetailsTable');
        removeGridShortcuts('#activitiesTable');
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

    fetchViewInstanceList();

    $('a[href="#archivedAlerts"]').on('click', function () {
        if (callingScreen == CALLING_SCREEN.REVIEW) {
            archived_table = signal.archived_utils.init_archived_table("#archivedAlertsTable", archivedAlertUrl, applicationName, evdasDetailsUrl);
        }
    });

    if(callingScreen == CALLING_SCREEN.REVIEW || callingScreen == CALLING_SCREEN.DASHBOARD) {
        $(window).on('beforeunload', function () {
            //discard the temporary changes in the view
            $.ajax({
                url: discardTempChangesUrl,
                type: 'GET'
            })
        });
    }


    var freqSelected = "";
    var prevColumnMap = JSON.parse(prevColumns);
    var alertDetailsTable;
    var activities_table;

    var dateRangeString = listDateRange.slice(1, -1);
    var dateRangeArray = [];
    if (dateRangeString != "") {
        dateRangeArray = dateRangeString.split(', ');
    }

    var filterIndex =
        [
            3,     //substance
            4,     //soc
            5,      //pt
            6,      //hlt
            7,      //smq
            8,     //pt
            9,      //listed
            10,     //new ev
            11,     //new eea
            12,     //new hcp
            13,     //new ser
            14,     //new med err
            15,     //new obs
            16,     //new fatal
            17,     //new +rc
            18,     //new lit
            19,     //new paed
            20,     //relative ror
            21,     //sdr paed
            22,     //new geria
            23,     //relative ror
            24,     //sdr geria
            25,     //new spon
            26,     //tot spon europe
            27,     //tot spon na
            28,     //tot spon japan
            29,     //tot spon asia
            30,     //tot spon rest
            31,     //ror all
            32,     //sdr
            33,     //ror europe
            34,     //ror na
            35,     //signal
            36,     //disposition
            37,     //assigned to
            38,     //sdr paed
            39,     //europe ror
            40,     //North america ror
            41,     //Japan ror
            42,     //Asia ror
            43,     //Rest ror
            44,     //Changes
            45,     //Due In
            46,     //justification
            47,     //dispPerformedBy
            48,     //last disp change
            49      //comment
        ]

    for (var i = 0; i < filterIndex.length; i++) {
        //this for loop is added for moving filter index according to fixed columns
        if(isPriorityEnabled){
            filterIndex[i]=filterIndex[i]+1;
        }
    }

    var listOfIndex = [7, //HLGT
        8, //HLT
        9, //SMQ
        11, //IMP
        13, //EEA
        14, //HCP
        16, //Med Err
        17, //obs
        22, //relativeRORPaed
        23, //SDRPaed
        25, //relativeRORGeria
        26, //sdr geria
        27, //newSpont
        28, //totSpontEuropr
        29, //tsNA
        30, //tsAsia
        31, //tsJapan
        32, //tsRest
        35, //rorE
        36, //ror NA
        39, //rorRest
        40]; //changes

    var fixedColumns = isPriorityEnabled ? 7 : 6;
    if (callingScreen == CALLING_SCREEN.TRIGGERED_ALERTS || callingScreen == CALLING_SCREEN.DASHBOARD) {
        fixedColumns = isPriorityEnabled ? 8 : 7;
        filterIndex = [3,4,5, 6, 7, 8, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23,
            24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49];
        if(callingScreen == CALLING_SCREEN.DASHBOARD){
            filterIndex = [4,5, 6, 7, 8, 9, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49];
        }
        for (var i = 0; i < filterIndex.length; i++) {
            //this for loop is added for moving filter index according to fixed columns
            if (isPriorityEnabled) {
                filterIndex[i] = filterIndex[i] + 1;
            }
        }
    }

    var lastIndex;

    var checkedIdList = [];
    var checkedRowList = [];

    signal.actions_utils.set_action_list_modal($('#action-modal'));
    signal.actions_utils.set_action_create_modal($('#action-create-modal'));
    signal.alertReview.openAlertCommentModal(applicationName, applicationName, applicationLabel, checkedIdList, checkedRowList);
    signal.alertReview.showAttachmentModal();
    signal.alertReview.populateAdvancedFilterSelect(applicationName);
    signal.alertReview.setSortOrder();
    signal.list_utils.flag_handler("evdasAlert", "toggleFlag");
    signal.evdasCaseDrillDown.bind_evdas_drill_down_table(evdasCaseDetailUrl);
    var filterValue = signal.alertReview.createFilterMap('eda_filterMap_'+executedConfigId, 'eda_viewName_' + executedConfigId);


    var _changeIntervalSubstance = null;
    var colIndexFirst = filterIndex[0]
    $(document).on('keyup', 'input#yadcf-filter-alertsDetailsTable-' + (colIndexFirst), function (e) {

        var casenum=$(this).val();
        var caseInput = $(this);
        clearInterval(_changeIntervalSubstance)
        _changeIntervalSubstance = setInterval(function() {
            $("#alertsDetailsTable").DataTable().column(colIndexFirst).search(casenum); //Exact value, column, reg
            if(casenum == "") {
                fixedFilterColumn.push({"column":colIndexFirst, "value":""});
                caseInput.removeClass("inuse");
            } else {
                fixedFilterColumn.push({"column":colIndexFirst, "value":casenum});
                caseInput.addClass("inuse");
            }
            clearInterval(_changeIntervalSubstance)
        }, 1500);

    });


    var _changeIntervalSoc = null;

    $(document).on('keyup', 'input#yadcf-filter-alertsDetailsTable-' + (colIndexFirst+1), function (e) {

        var casenum=$(this).val();
        var caseInput = $(this);
        clearInterval(_changeIntervalSoc)
        _changeIntervalSoc = setInterval(function() {
            $("#alertsDetailsTable").DataTable().column(colIndexFirst+1).search(casenum); //Exact value, column, reg
            if(casenum == "") {
                fixedFilterColumn.push({"column":colIndexFirst+1, "value":""});
                caseInput.removeClass("inuse");
            } else {
                fixedFilterColumn.push({"column":colIndexFirst+1, "value":casenum});
                caseInput.addClass("inuse");
            }
            clearInterval(_changeIntervalSoc)
        }, 1500);

    });

    var _changeIntervalPt = null;

    $(document).on('keyup', 'input#yadcf-filter-alertsDetailsTable-' + (colIndexFirst+2), function (e) {

        var casenum=$(this).val();
        var caseInput = $(this);
        clearInterval(_changeIntervalPt)
        _changeIntervalPt = setInterval(function() {
            $("#alertsDetailsTable").DataTable().column(colIndexFirst+2).search(casenum); //Exact value, column, reg
            if(casenum == "") {
                fixedFilterColumn.push({"column":colIndexFirst+2, "value":""});
                caseInput.removeClass("inuse");
            } else {
                fixedFilterColumn.push({"column":colIndexFirst+2, "value":casenum});
                caseInput.addClass("inuse");
            }
            clearInterval(_changeIntervalPt)
        }, 1500);

    });


    $(document).on('click', '.evdas-history-icon', function (event) {
        event.preventDefault();
        var index =  $(this).closest('tr').index();
        var productName = table.rows(index).data()[0].productName;
        var eventName = table.rows(index).data()[0].preferredTerm;
        var alertConfigId = table.rows(index).data()[0].alertConfigId;
        var alertId = table.rows(index).data()[0].id;
        selectedId = alertId;
        var evdasHistoryModal = $('#evdasHistoryModal');
        evdasHistoryModal.modal('show');
        evdasHistoryModal.find("#productName").html(productName);
        evdasHistoryModal.find("#eventName").html(eventName);

        signal.evdasHistoryTable.init_evdas_history_table(evdasHistoryUrl, productName, eventName, alertConfigId, alertId);
    });

    $(document).on('click', '.show-trend-icon', function (event) {
        var index =  $(this).closest('tr').index();
        var productName = table.rows(index).data()[0].productName;
        var preferredTerm = table.rows(index).data()[0].preferredTerm;
        var alertId = $(this).attr("data-value");
        var showTrendTableUrl = showTrendUrl + "?product=" + productName + "&pt=" + preferredTerm + "&id=" + alertId;
        var win = window.open(showTrendTableUrl, '_blank');
        win.focus();
    });

    $(document).on('click', '.show-report-icon', function (event) {
        if(typeof hasReportingAccess !== "undefined" && !hasReportingAccess) {
            event.preventDefault();
            $.Notification.notify('warning', 'top right', "Warning", "You don't have access to perform this action", {autoHideDelay: 5000});
        } else {
            var parent_row = $(event.target).closest('tr');
            var productId = parent_row.find('input[type="checkbox"]').attr("data-id");
            var showReportTableUrl = showReportUrl + "?productId=" + productId + "&selectedDataSource=EUDRA" + "&isArchived=" + isArchived;
            var win = window.open(showReportTableUrl, '_blank');
            win.focus();
        }
    });

    $(document).on('click', '.show-chart-icon', function (event) {
        var alertId = $(this).attr("data-value");
        var showEvdasChart = $("#show-evdas-chart-modal");
        var isArchived = $(this).attr("data-archived");

        showEvdasChart.modal("show");

        $.ajax({
            url: 'showCharts?alertId=' + alertId + '&isArchived=' + isArchived,
        })
            .done(function (result) {

                evdas_chart("#evdas-count-by-status", result.xAxisTitle,
                    [{
                        name: "EV",
                        data: result.evCount
                    }, {
                        name: "Lit",
                        data: result.litCount
                    }, {
                        name: "Serious",
                        data: result.seriousCount
                    }, {
                        name: "Fatal",
                        data: result.fatalCount
                    }],
                    "Counts", 'Counts'
                );

                evdas_chart("#evdas-scores-by-status", result.xAxisTitle,
                    [{
                        name: "ROR",
                        data: parseRorValue(result.rorValue)
                    }],
                    "Scores", 'Scores'
                );

                var countTable = [];
                for (var index = 0; index < result.xAxisTitle.length; index++) {
                    var obj = {
                        "xAxisTitle": result.xAxisTitle[index],
                        "evCount": result.evCount[index],
                        "litCount": result.litCount[index],
                        "seriousCount": result.seriousCount[index],
                        "fatalCount": result.fatalCount[index]
                    };
                    countTable.push(obj);
                }
                var trend_data_content = signal.utils.render('evdasCountTable', {trendData: countTable});
                $("#trend-div").html(trend_data_content);

                var scoresTable = [];
                for (var index = 0; index < result.xAxisTitle.length; index++) {
                    var obj = {
                        "xAxisTitle": result.xAxisTitle[index],
                        "rorValue": Number(parseFloat(result.rorValue[index]).toFixed(2))
                    };
                    scoresTable.push(obj);
                }
                var score_data_content = signal.utils.render('evdasScoresTable', {trendData: scoresTable});
                $("#scores-div").html(score_data_content);
            })

    });

    //apply the sorting to column based on saved view
    var sortingMap = signal.alertReview.createSortingMap('eda_sortedColumn_'+ executedConfigId, 'eda_viewName_' + executedConfigId);
    if (sortingMap != 'undefined' && sortingMap.length > 0) {
        index = sortingMap[0][0];
        dir = sortingMap[0][1]
    }

    if (callingScreen == CALLING_SCREEN.REVIEW) {
        //saving new ViewInstance modal
        signal.alertReview.openSaveViewModal(filterIndex, applicationName, viewId)
    }else if(callingScreen = CALLING_SCREEN.DASHBOARD){
        signal.alertReview.openSaveViewModal(filterIndex, ALERT_CONFIG_TYPE.EVDAS_ALERT_DASHBOARD, viewId)
    }

    $(window).on('beforeunload', function(){
        if (sessionStorage.getItem('isViewCall') == 'true') {
            var backUrl = sessionStorage.getItem('back_url');
            sessionStorage.clear();
            sessionStorage.setItem('back_url', backUrl)
        }else{
            var viewInfo = signal.alertReview.generateViewInfo(filterIndex);
            sessionStorage.setItem('eda_filterMap_'+ executedConfigId, JSON.stringify(viewInfo.filterMap));
            sessionStorage.setItem('eda_sortedColumn_'+ executedConfigId, JSON.stringify(viewInfo.sortedColumn));
            sessionStorage.setItem('eda_notVisibleColumn_'+ executedConfigId, viewInfo.notVisibleColumn);
            sessionStorage.setItem('eda_viewName_' + executedConfigId, $('.viewSelect :selected').text().replace("(default)", "").trim())
        }
    });

    $('#alert-list-filter-apply-bt').on('click', function (evt) {
        evt.preventDefault();
        var filterParams = $('#alert-list-filter').serialize();
        var theDataTable = $('#alertsDetailsTable').DataTable();
        $('#alert-list-filter').find('#filterUsed').val(true);
        var newUrl = listConfigUrl + "&" + filterParams + "&filterApplied=" + true
        theDataTable.ajax.url(newUrl).load()
    });

    var evdas_chart = function (chartId, categories, series, yAxisTitle, chartTitle) {
        $(chartId).highcharts({
            chart: {
                type: 'line'
            },
            xAxis: {
                categories: categories
            },
            yAxis: {
                allowDecimals: false,
                plotLines: [{
                    value: 0,
                    width: 1,
                    color: '#808080'
                }],
                title: {
                    text: yAxisTitle
                }
            },
            series: series,
            credits: {
                enabled: false
            },
            title: {
                text: chartTitle
            }
        });
    };

    var init_filter = function (data_table) {

        var filterOptions = [];
        $.each(filterIndex, function (key, value) {
            filterOptions.push({
                column_number: value,
                filter_type: 'text',
                filter_reset_button_text: false,
                filter_delay: 600,
                filter_default_label: '',
                style_class: 'textFilter'
            })
        });
        yadcf.init(data_table, filterOptions);
        $(".yadcf-filter").removeAttr('placeholder');
        if(filterValue.length > 0){
            yadcf.exFilterColumn(data_table, filterValue,true);
        }else{
            $('.yadcf-filter-wrapper').hide()
        }

        signal.fieldManagement.init($('#alertsDetailsTable').DataTable(), '#alertsDetailsTable', fixedColumns, true);
        $('.yadcf-filter-wrapper').hide();

    };

    var initAlertDetailsTable = function (result) {
        var isFilterRequest = true;
        var prefix = "ev_";
        var filterValues = [];
        if (window.sessionStorage) {
            if (signal.alertReview.isAlertPersistedInSessionStorage(prefix)) {
                filterValues = JSON.parse(sessionStorage.getItem(prefix + "filters_value"));
            } else {
                signal.alertReview.removeFiltersFromSessionStorage(prefix);
                isFilterRequest = false;
            }
        }

        $(document).on('click', '#alertsDetailsTable_paginate', function () {
            isPagination = true;
            if($('.evdas-header-row input#select-all').is(":checked") && !prev_page.includes($('li.active').text().slice(-3).trim())){
                prev_page.push($('li.active').text().slice(-3).trim());
            }
            if((!$('.evdas-header-row input#select-all').is(":checked") && prev_page.includes($('li.active').text().slice(-3).trim()))){
                var position = prev_page.indexOf($('li.active').text().slice(-3).trim());
                prev_page.splice(position,1);
            }
        });

        table = $('#alertsDetailsTable').DataTable({
            layout: {
                topStart: null,
                topEnd: null,
                bottomStart: ["pageLength", "info", {paging: {type: "full_numbers"}}],
                bottomEnd: null,
            },
            customProcessing: true,
            "language": {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json"
            },
            "rowCallback": function (row, data, index) {
                applyEvdasRules(row, data);
                $(row).addClass(index % 2 === 0 ? 'odd' : 'even')
                signal.alertReview.applyBusinessRules(row, data);
                //Bind AssignedTo Select Box
                // workaround, select2 initialization is called before elements displayed
                setTimeout(function () {
                    signal.user_group_utils.bind_assignTo_to_grid_row($(row), searchUserGroupListUrl,
                        {name: data.assignedToUser.fullName, id: data.assignedToUser.id});
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
            drawCallback: function (settings) {
                var current_page = $('li.active').text().slice(-3).trim();
                if(typeof prev_page != 'undefined' && $.inArray(current_page,prev_page) == -1){
                    $(".evdas-header-row input#select-all").prop('checked', false);
                } else {
                    $(".evdas-header-row input#select-all").prop('checked', true);
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
                    pageDictionaryForAlertDetails($('#alertsDetailsTable_wrapper')[0], settings.aLengthMenu[0][0], settings.json.recordsFiltered);

                }else {
                    pageDictionaryForAlertDetails($('#alertsDetailsTable_wrapper')[0], 50, rowsDataAD.length);
                }
                initPSGrid($('#alertsDetailsTable_wrapper'));
                if(typeof settings.json != "undefined" && !isDynamicFilterApplied){
                    isDynamicFilterApplied = true;
                    signal.alertReview.bindGridDynamicFilters(settings.json.filters, prefix, settings.json.configId);
                }
                var main_disp_filter;
                if (typeof settings.json != "undefined") {
                    main_disp_filter= sessionStorage.getItem('ev_filters_value')
                    if (advancedFilterChanged && !main_disposition_filter || evdasQueryJson!='') {
                        if (sessionStorage.getItem("ev_filters_value")){
                            //this function is called to handle the case when we change the disposition of a pec to a new disposition that is not available in quick filter dispo list
                            retainQuickDispositionFilter(JSON.parse(sessionStorage.getItem(prefix + "filters_value")), "ev_", settings.json.configId);
                        }
                        addAdvancedFilterDisp(settings.json.advancedFilterDispName, prefix, settings.json.configId);
                        advancedFilterChanged = false;
                        evdasQueryJson="";
                    }
                    else if(advancedFilterChanged && main_disposition_filter){
                        retainQuickDispositionFilter(JSON.parse(main_disp_filter), prefix, settings.json.configId);
                        advancedFilterChanged = false;
                        main_disposition_filter=false;
                    }
                    allVisibleIds = settings.json.visibleIdList
                }
                $(".dataTableHideCellContent").contextmenu();
                colEllipsis();

                webUiPopInit();
                closeInfoPopover();
                enterKeyAlertDetail();
                closePopupOnScroll();
                showInfoPopover();
                signal.alertReview.enableMenuTooltips();
                signal.alertReview.enableMenuTooltipsDynamicWidth();
                signal.alertReview.sortIconHandler();
                populateSelectedCases();
                var checkedId
                $('input[type=checkbox]').each(function () {
                    checkedId = $(this).attr('data-id')
                    if((!isPagination) && (checkedId !== undefined) && !allVisibleIds.includes(parseInt(checkedId)) && selectedCases.includes(checkedId)){
                        // Remove Cases which were selected before but now not visible on screen
                        selectedCasesInfo.splice($.inArray(checkedId, selectedCases), 1);
                        selectedCases.splice($.inArray(checkedId, selectedCases), 1);
                    }else if((!isPagination) && (checkedId !== undefined) && allVisibleIds.includes(parseInt(checkedId)) && selectedCases.indexOf($(this).attr("data-id")) === -1 && $(this).is(':checked')){
                        // Disable checkbox for cases not available on screen
                        $(this).prop('checked', false);
                    }
                });

                $('.dt-pagination').on('change', function () {
                    var countVal = $('.dt-pagination').val()
                    sessionStorage.setItem("ecaPageEntries", countVal);
                    ecaEntriesCount = sessionStorage.getItem("ecaPageEntries");
                })
                $(".dt-scroll-body").append("<div style='height:3px'></div>");
            },
            initComplete: function (settings, json) {
                if(filterValue.length === 0 && !isDynamicFilterApplied){
                    isDynamicFilterApplied = true;
                    signal.alertReview.bindGridDynamicFilters(json.filters, prefix, json.configId);
                }
                if (sessionStorage.getItem(prefix + "filters_store") && typeof settings.json !== 'undefined' && !settings.json.advancedFilterDispName) {
                    setQuickDispositionFilter(prefix);
                }

                $('.dt-length').addClass('');

                if ($('#filterMap').val() == "" || $('#filterMap').val() == "{}") {
                    if(!sessionStorage.getItem('eda_filterMap_' +executedConfigId) || sessionStorage.getItem('eda_filterMap_' +executedConfigId) == "{}"){
                        $('.yadcf-filter-wrapper').hide();
                    }
                }
                signal.alertReview.enableMenuTooltips();
                signal.alertReview.disableTooltips();
                addGridShortcuts('#alertsDetailsTable');
                showInfoPopover();
                $("#toggle-column-filters, #ic-toggle-column-filters").on('click', function () {
                    var ele = $('.yadcf-filter-wrapper');
                    var inputEle = $('.yadcf-filter');
                    if (ele.is(':visible')) {
                        ele.hide();
                    } else {
                        ele.show();
                        inputEle.first().focus();
                    }
                    $(".dt-scroll-body").css('position','static');
                });
            },

            "ajax": {
                "url": listConfigUrl + '&isFilterRequest=' + isFilterRequest + '&advancedFilterChanged=' + advancedFilterChanged + '&filters=' + encodeURIComponent(JSON.stringify(filterValues)) + "&frequency=" + freqSelected,
                "type": "POST",
                "dataSrc": "aaData",
                "error":ajaxAuthroizationError,
                "data": function (d) {
                    [d, dir, index] = updateOrder(index, d, columnSeq, dir)
                    if ($('#advanced-filter').val()) {
                        d.advancedFilterId = $('#advanced-filter').val()
                        isPagination = false;
                    }
                    if($('#queryJSON').val()){
                        evdasQueryJson=$('#queryJSON').val()
                        d.queryJSON = $('#queryJSON').val()
                    }
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
            "bLengthChange": true,
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
            "pagination": true,
            "iTotalDisplayRecords": ecaEntriesCount,
            "iDisplayLength": parseInt(ecaEntriesCount),
            "bAutoWidth": false,
            "deferLoading":filterValue.length > 0 ? 0: null,
            "aoColumns": constructColumns(result),
            scrollX: true,
            scrollY: '65vh',
            fixedColumns:{
                start: fixedColumns,
                end: 0,
            },
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }]
        });
        signal.user_group_utils.bind_assignTo_selection(assignToGroupUrl, table, hasReviewerAccess);
        setTimeout(function() {
            init_filter(table);
        }, 500)
        return table;
    };

    var constructColumns = function (result) {
        var aocolumns = [
            {
                "mData": "selected",
                "mRender": function (data, type, row) {
                    if ($.inArray(row.execConfigId, executedIdList) == -1) {
                        executedIdList.push(row.execConfigId)
                    }
                    var checkboxhtml = '<input class="execConfigId" id="execConfigId" type="hidden" value="' + row.execConfigId + '" />';
                    if (selectedCases.includes(row.id.toString())){
                        return  checkboxhtml +  '<input type="checkbox" class="alert-check-box editor-active copy-select"  data-id=' + row.id + ' checked/>';
                    } else{
                        return checkboxhtml + '<input type="checkbox" class="alert-check-box editor-active copy-select"  data-id=' + row.id + ' />';

                    }
                },
                "className": "dt-center",
                "orderable": false
            },
            {
                "mData": "dropdown",
                "mRender": function (data, type, row) {
                    var actionButton = '<div style="display: block;" class="btn-group dropdown dataTableHideCellContent" align="center"> \
                        <a class="dropdown-toggle" data-toggle="dropdown" tabindex="0"> \
                        <span style="cursor: pointer;font-size: 125%;" class="glyphicon glyphicon-option-vertical"></span><span class="sr-only">Toggle Dropdown</span> \
                        </a>';

                    if(row.comment) {
                        actionButton +=  '<i class="mdi mdi-chat blue-2 font-13 pos-ab comment" title="' + $.i18n._('commentAvailable') + '"></i>';
                    }

                    if(row.isAttachment == true) {
                        actionButton += ' <i class="mdi mdi-attachment blue-1 font-13 pos-ab attach" title="' + $.i18n._('attachmentAvailable') + '"></i>';
                    }

                    actionButton += '<ul class="dropdown-menu menu-cosy" role="menu"><li role="presentation"><a tabindex="0" class="review-row-icon evdas-history-icon"><span class=" fa fa-list m-r-10"></span>' + $.i18n._('contextMenu.history') + '</a></li>';
                    actionButton += '<li role="presentation"><a tabindex="0" class="review-row-icon comment-icon" data-info="row"><span class="fa fa-comment m-r-10" data-info="row"></span>' + $.i18n._('contextMenu.comments') + '</a></li>';
                    actionButton += '<li role="presentation"><a tabindex="0" class="review-row-icon show-chart-icon" data-value="' + row.id + '" data-archived="' + isArchived + '"><span class="fa fa-line-chart m-r-10"></span>' + $.i18n._('contextMenu.charts') + '</a></li>';
                    actionButton += '<li role="presentation"><a tabindex="0" class="review-row-icon show-report-icon" data-value="' + row.id + '" data-archived="' + isArchived + '"><span class=" fa fa-file m-r-10"></span>' + $.i18n._('contextMenu.reports') + '</a></li>';
                    actionButton += '<li role="presentation"><a tabindex="0" class="review-row-icon show-attachment-icon" data-field="attachment" data-id="' + row.id + '" data-controller="evdasAlert"><span class=" fa fa-file m-r-10"></span>' + $.i18n._('contextMenu.attachment') + '</a></li>';
                    if((row.isUndoEnabled=="true" && row.isDefaultState === "true") || row.isDefaultState === "false"){
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
                "className":"dropDown"
            }];

            if(isPriorityEnabled) {
                aocolumns.push.apply(aocolumns, [
                    {
                        "mData": "priority",
                        "aTargets": ["priority"],
                        "mRender": function (data, type, row) {
                            return signal.utils.render('priority', {
                                priorityValue: row.priority.value,
                                priorityClass: row.priority.iconClass,
                                isPriorityChangeAllowed: true
                            });
                        },
                        'className': 'text-center priorityParent',
                        "visible": true
                    }
                ])
            }
            aocolumns.push.apply(aocolumns, [
            {
                "mData": "actions",
                "mRender": function (data, type, row) {
                    row["buttonClass"]= buttonClassVar;
                    return signal.actions_utils.build_action_render(row)
                },
                'className': 'col-min-50 col-max-50 text-center action-col dropDown',
                "visible": true
            }
            ]);
        if(callingScreen == CALLING_SCREEN.DASHBOARD){
            aocolumns.push.apply(aocolumns, [
                {
                    "mData": "name",
                    'className': 'col-min-150 col-max-150 cell-break',
                    "mRender": function (data, type, row) {
                        var colElement = '<div class="col-container"><div class="col-height">';
                        colElement += encodeToHTML(row.alertName);
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(row.alertName) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    },
                    "visible": true
                }
            ])
        }
        aocolumns.push.apply(aocolumns, [
            {
                "mData": "substance",
                'className': 'col-min-150 col-max-150 cell-break',
                "mRender": function (data, type, row) {
                    var colElement = "<input type='hidden' value='" + row.substanceId + "' class='row-product-id'/>" + "<input type='hidden' value='" + row.ptCode + "' class='row-pt-code'/>";
                    colElement+='<div class="col-container"><div class="col-height">';
                    colElement += "<span data-field ='productName' data-id='" + row.productName + "'>" + (row.productName) + "</span>";
                    colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="'+row.productName+'"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                    colElement += '</div></div>';
                    return colElement
                },
                "visible": true
            },
            {
                "mData": "soc",
                'className': 'col-min-100 col-max-100',
                "mRender": function (data, type, row) {
                    if (row.soc != 'undefined') {
                        if (row.fullSoc != null) {
                            return '<span class="grid-menu-tooltip" data-title="' + row.fullSoc + '" >' + row.soc + '</span>'
                        } else {
                            return '<span>' + row.soc + '</span>'
                        }
                    } else {
                        return '<span>' + '-' + '</span>'
                    }
                },
                "visible": true,
            },
            {
                "mData": "pt",
                "sWidth": "col-min-150 col-max-150",
                "mRender": function (data, type, row) {
                    var urlWithParams = eventDetailsUrl + '?alertId=' + row.id + '&type=' + applicationName + '&isArchived=' + isArchived +
                        '&isAlertScreen=true';;
                    var result = row.preferredTerm;
                    return "<span style='word-break: keep-all' data-field ='eventName' data-id='" + row.preferredTerm + "'>" + result + "</span>"
                },
                "visible": true
            },
            {
                "mData": "hlgt",
                'className': '',
                "visible": signal.fieldManagement.visibleColumns('hlgt')
            },
            {
                "mData": "hlt",
                'className': '',
                "visible": signal.fieldManagement.visibleColumns('hlt')
            },
            {
                "mData": "smqNarrow",
                'className': '',
                "visible": signal.fieldManagement.visibleColumns('smqNarrow')
            },
            {
                "mData": "impEvents",
                'className': '',
                "mRender": function (data, type, row) {
                    return ptBadge(row.ime, row.dme, row.ei, row.isSm);
                },
                "visible": signal.fieldManagement.visibleColumns('impEvents')
            },
            {
                "mData": "listed",
                "className": 'dt-center',
                "mRender": function (data, type, row) {
                    return '<span>' + row.listed + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('listed')
            },
            {
                "mData": "dmeIme",
                'className': 'col-min-100 col-max-100 dmeIme',
                "mRender": function (data, type, row) {
                    return '<span class="dmeImeEvdas">' + row.dmeIme + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('dmeIme')
            },
            {
                "mData": "newEv",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<div><div class="stacked-cell-center-top">' + drillDownOptions(row.newEv, row.productName, row.preferredTerm, row.execConfigId, 0, true, row.id, row.newEvLink, 'newEvEvdas') +
                        '</div><div class="stacked-cell-center-top">' + drillDownOptions(row.totalEv, row.productName, row.preferredTerm, row.execConfigId, 0, false, row.id, row.totalEvLink, 'totalEvEvdas') + '</div></div>'
                },
                "visible": signal.fieldManagement.visibleColumns('newEv')
            },
            {
                "mData": "newEea",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<div><div class="stacked-cell-center-top">' + drillDownOptions(row.newEea, row.productName, row.preferredTerm, row.execConfigId, 1, true, row.id, '', 'newEEAEvdas') +
                        '</div><div class="stacked-cell-center-top">' + drillDownOptions(row.totEea, row.productName, row.preferredTerm, row.execConfigId, 1, false, row.id, '', 'totEEAEvdas') + '</div></div>'
                },
                "visible": signal.fieldManagement.visibleColumns('newEea')
            },
            {
                "mData": "newHcp",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<div><div class="stacked-cell-center-top">' + drillDownOptions(row.newHcp, row.productName, row.preferredTerm, row.execConfigId, 2, true, row.id, '', 'newHCPEvdas') +
                        '</div><div class="stacked-cell-center-top">' + drillDownOptions(row.totHcp, row.productName, row.preferredTerm, row.execConfigId, 2, false, row.id, '', 'totHCPEvdas') + '</div></div>'
                },
                "visible": signal.fieldManagement.visibleColumns('newHcp')
            },
            {
                "mData": "newSerious",
                "mRender": function (data, type, row) {
                    return '<div><div class="stacked-cell-center-top">' + drillDownOptions(row.newSerious, row.productName, row.preferredTerm, row.execConfigId, 3, true, row.id, '', 'newSeriousEvdas') +
                        '</div><div class="stacked-cell-center-top">' + drillDownOptions(row.totalSerious, row.productName, row.preferredTerm, row.execConfigId, 3, false, row.id, '', 'totalSeriousEvdas') + '</div></div>'
                },
                "visible": signal.fieldManagement.visibleColumns('newSerious')
            },
            {
                "mData": "newMedErr",
                "mRender": function (data, type, row) {
                    return '<div><div class="stacked-cell-center-top"><span class="newMedErrEvdas">' + row.newMedErr + '</span></div>' +
                        '<div class="stacked-cell-center-top"><span class="totMedErrEvdas">' + row.totMedErr + '</span></div></div>'
                },
                "visible": signal.fieldManagement.visibleColumns('newMedErr')
            },
            {
                "mData": "newObs",
                "mRender": function (data, type, row) {
                    return '<div><div class="stacked-cell-center-top"><span class="newObsEvdas">' + row.newObs + '</span></div>' +
                        '<div class="stacked-cell-center-top"><span class="totObsEvdas">' + row.totObs + '</span></div></div>'
                },
                "visible": signal.fieldManagement.visibleColumns('newObs')
            },
            {
                "mData": "newFatal",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<div><div class="stacked-cell-center-top">' + drillDownOptions(row.newFatal, row.productName, row.preferredTerm, row.execConfigId, 4, true, row.id, '', 'newFatalEvdas') + '</div>' +
                        '<div class="stacked-cell-center-top">' +
                        drillDownOptions(row.totalFatal, row.productName, row.preferredTerm, row.execConfigId, 4, false, row.id, '', 'totalFatalEvdas') +
                        '</div></div>'
                },
                "visible": signal.fieldManagement.visibleColumns('newFatal')
            },
            {
                "mData": "newRc",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<div><div class="stacked-cell-center-top"><span class="newRcEvdas">' + row.newRc + '</span></div>' +
                        '<div class="stacked-cell-center-top"><span class="totRcEvdas">' + row.totRc + '</span></div></div>'
                },
                "visible": signal.fieldManagement.visibleColumns('newRc')
            },
            {
                "mData": "newLit",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<div><div class="stacked-cell-center-top"><span class="newLitEvdas">' + row.newLit + '</span></div>' +
                        '<div class="stacked-cell-center-top"><span class="totalLitEvdas">' + row.totalLit + '</span></div></div>'

                },
                "visible": signal.fieldManagement.visibleColumns('newLit')
            },
            {
                "mData": "newPaed",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<div><div class="stacked-cell-center-top">' + drillDownOptions(row.newPaed, row.productName, row.preferredTerm, row.execConfigId, 8, true, row.id, '', 'newPaedEvdas') +
                        '</div><div class="stacked-cell-center-top">' + drillDownOptions(row.totPaed, row.productName, row.preferredTerm, row.execConfigId, 8, false, row.id, '', 'totalPaedEvdas') + '</div></div>'
                },
                "visible": signal.fieldManagement.visibleColumns('newPaed')
            },
            {
                "mData": "ratioRorPaedVsOthers",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<span class="ratioRorPaedVsOthersEvdas">' + data + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('ratioRorPaedVsOthers')
            },
            {
                "mData": "newGeria",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<div><div class="stacked-cell-center-top">' + drillDownOptions(row.newGeria, row.productName, row.preferredTerm, row.execConfigId, 7, true, row.id, '', 'newGeriatEvdas') +
                        '</div><div class="stacked-cell-center-top">' + drillDownOptions(row.totGeria, row.productName, row.preferredTerm, row.execConfigId, 7, false, row.id, '', 'totalGeriatEvdas') + '</div></div>'
                },
                "visible": signal.fieldManagement.visibleColumns('newGeria')
            },
            {
                "mData": "ratioRorGeriatrVsOthers",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<span class="ratioRorGeriatrVsOthersEvdas">' + data + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('ratioRorGeriatrVsOthers')
            },
            {
                "mData": "sdrGeratr",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<span class="sdrGeratrEvdas">' + data + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('sdrGeratr')
            },
            {
                "mData": "newSpont",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<div><div class="stacked-cell-center-top">' + drillDownOptions(row.newSpont, row.productName, row.preferredTerm, row.execConfigId, 6, true, row.id, '', 'newSponEvdas') +
                        '</div><div class="stacked-cell-center-top">' + drillDownOptions(row.totSpont, row.productName, row.preferredTerm, row.execConfigId, 6, false, row.id, '', 'totSpontEvdas') + '</div></div>'
                },
                "visible": signal.fieldManagement.visibleColumns('newSpont')
            },
            {
                "mData": "totSpontEurope",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<span class="totSpontEuropeEvdas">' + data + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('totSpontEurope')
            },
            {
                "mData": "totSpontNAmerica",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<span class="totSpontNAmericaEvdas">' + data + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('totSpontNAmerica')
            },
            {
                "mData": "totSpontJapan",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<span class="totSpontJapanEvdas">' + data + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('totSpontJapan')
            },
            {
                "mData": "totSpontAsia",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<span class="totSpontAsiaEvdas">' + data + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('totSpontAsia')
            },
            {
                "mData": "totSpontRest",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<span class="totSpontRestEvdas">' + data + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('totSpontRest')
            },
            {
                "mData": "rorValue",
                'className': '',
                "mRender": function (data, type, row) {
                    // Below class ROR is used in business configuration
                    return '<span class="rorAllEvdas">' + row.rorValue.match(/^-?\d+(?:\.\d{0,2})?/)[0] + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('rorValue')
            },
            {
                "mData": "sdr",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<span class="sdrEvdas">' + data + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('sdr')
            },
            {
                "mData": "disposition",
                "mRender": function (data, type, row) {
                    return row.disposition;
                },
                "visible": signal.fieldManagement.visibleColumns('disposition'),
                "class": 'col-min-200 col-max-200 currentDisposition'
            },
            {
                "mData": 'signalsAndTopics',
                'className': 'col-min-200 col-max-200 signalInformation',
                "mRender": function (data, type, row) {
                    var signalAndTopics = '';
                    $.each(row.signalsAndTopics, function(i, obj){
                        var url = signalDetailUrl + '?id=' + obj['signalId'];
                        if($('#hasSignalManagementAccess').val() == 'true') {
                            signalAndTopics = signalAndTopics + '<span class="click box-inline word-wrap-break-word col-max-150"><a  class="cell-break"  title="' + obj.disposition.displayName + '" onclick="validateAccess(event,' + obj['signalId'] + ')" href="' + url + '">' + escapeHTML(obj['name']) + '</a></span>&nbsp;'
                        }else {
                            signalAndTopics = signalAndTopics + '<span class="click box-inline word-wrap-break-word col-max-150 signalSummaryAuth"><a  class="cell-break" title="' + obj.disposition.displayName + '" href="javascript:void(0)">' + escapeHTML(obj['name']) + '</a></span>&nbsp;'
                        }
                        signalAndTopics = signalAndTopics + ","
                    });
                    if(signalAndTopics.length > 1)
                        return '<div class="cell-break word-wrap-break-word col-max-150>' + signalAndTopics.substring(0, signalAndTopics.length - 1) + '</div>';
                    else
                        return '-';
                },
                "sWidth": "",
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
                        isReviewed:row.isReviewed,
                        isValidationStateAchieved: row.isValidationStateAchieved,
                        id:row.currentDispositionId,
                        useDropdown: (dispositionDropdownEnabled === 'true'),
                        rowIndex: meta.row
                    });
                },
                "visible": signal.fieldManagement.visibleColumns('currentDisposition'),
                "class": 'col-min-200 col-max-200 dispositionAction'
            },
            {
                "mData": "assignedTo",
                'className': 'col-min-150 col-max-150 assignedTo',
                "mRender": function (data, type, row) {
                    return signal.list_utils.assigned_to_comp(row.id, row.assignedTo)
                },
                "sortable": false,
                "visible": signal.fieldManagement.visibleColumns('assignedTo')
            },
            {
                "mData": "sdrPaed",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<span class="sdrPaedEvdas">' + data + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('sdrPaed')
            },
            {
                "mData": "europeRor",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<span class="rorEuropeEvdas">' + data + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('europeRor')
            },
            {
                "mData": "northAmericaRor",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<span class="rorNAmericaEvdas">' + data + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('northAmericaRor')
            },
            {
                "mData": "japanRor",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<span class="rorJapanEvdas">' + data + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('japanRor')
            },
            {
                "mData": "asiaRor",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<span class="rorAsiaEvdas">' + data + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('asiaRor')
            },
            {
                "mData": "restRor",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<span class="rorRestEvdas">' + data + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('restRor')
            },
            {
                "mData": "changes",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<span class="changesEvdas">' + data + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('changes')
            },
            {
                "mData": "dueDate",
                'className': 'col-min-50 col-max-50 dueIn',
                "mRender": function (data, type, row) {
                    return signal.list_utils.due_in_comp(row.dueIn)
                },
                "visible": signal.fieldManagement.visibleColumns('dueDate')
            }
        ]);
        aocolumns.push.apply(aocolumns, [
            {
                "mData": "justification",
                'className': 'col-min-150 col-max-150 cell-break',
                "mRender": function (data, type, row) {
                    var colElement = '<div class="col-container"><div class="col-height">';
                    colElement += encodeToHTML(row.justification);
                    colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + escapeHTML(encodeToHTML(row.justification)) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                    colElement += '</div></div>';
                    return colElement
                },
                "orderable": true,
                "visible": signal.fieldManagement.visibleColumns('justification')
            },
            {
                "mData": "dispPerformedBy",
                'className': 'col-min-150 col-max-150 cell-break',
                "mRender": function (data, type, row) {
                    var colElement = '<div class="col-container"><div class="col-height">';
                    colElement += encodeToHTML(row.dispPerformedBy);
                    colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(row.dispPerformedBy) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                    colElement += '</div></div>';
                    return colElement
                },
                "orderable": true,
                "visible": signal.fieldManagement.visibleColumns('dispPerformedBy')
            },
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
                "orderable": true,
                "visible": signal.fieldManagement.visibleColumns('dispLastChange')
            },
            {
                "mData": "comment",
                'className': 'col-min-150 col-max-150 cell-break',
                "mRender": function (data, type, row) {
                    var colElement = '<div class="col-container"><div class="col-height commentDisplay">';
                    colElement += encodeToHTML(row.comment);
                    colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + escapeHTML(encodeToHTML(row.comment)) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                    colElement += '</div></div>';
                    return colElement
                },
                "sortable": false,
                "visible": signal.fieldManagement.visibleColumns('comment')
            },

        ]);
        if (callingScreen == CALLING_SCREEN.TRIGGERED_ALERTS) {
            aocolumns.splice(2, 0, {
                "mData": "name",
                "sWidth": "",
                'className': '',
                'mRender': function (data, type, row) {
                    return '<span>' + encodeToHTML(row.alertName) + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('name')
            });
            lastIndex = listOfIndex[listOfIndex.length - 1];
            var count = 1;
            for(var prevColumn = 0; prevColumn < parseInt(prevColCount); prevColumn++){
                $.each(prevColumnMap, function (field, column) {
                    listOfIndex.push(lastIndex + 6 + count);
                    count++;
                    aocolumns.push.apply(aocolumns, [{
                        "mData": "exe" + prevColumn + field,
                        "clasName": '',
                        "mRender": function (data, type, row) {
                            if (row['exe' + prevColumn]) {
                                return '<div>' + row['exe' + prevColumn][field] + '</div>'
                            } else {
                                return '<div></div>'
                            }
                        },
                        "visible":signal.fieldManagement.visibleColumns(field)
                    }])
                })

            }
        } else if(callingScreen == CALLING_SCREEN.REVIEW) {
            lastIndex = listOfIndex[listOfIndex.length - 1];
            var count = 1;
                $.each(prevColumnMap, function (field, column) {
                    listOfIndex.push(lastIndex + 6 + count);
                    count++;
                    aocolumns.push.apply(aocolumns, [{
                        "mData": "exe" + 0 + field,
                        "className": '',
                        "mRender": function (data, type, row) {
                            if (row["exe" + 0]) {
                                return '<div class="stacked-cell-center-top">' + row["exe" + 0][field] + '</div>'
                            } else {
                                return '<div></div>'
                            }
                        },
                        "visible":signal.fieldManagement.visibleColumns("exe" + 0 + field)
                    }])
                })
                $.each(prevColumnMap, function (field, column) {
                    listOfIndex.push(lastIndex + 6 + count);
                    count++;
                    aocolumns.push.apply(aocolumns, [{
                        "mData": "exe" + 1 + field,
                        "className": '',
                        "mRender": function (data, type, row) {
                            if (row["exe" + 1]) {
                                return '<div class="stacked-cell-center-top">' + row["exe" + 1][field] + '</div>'
                            } else {
                                return '<div></div>'
                            }
                        },
                        "visible":signal.fieldManagement.visibleColumns("exe" + 1 + field)
                    }])
                })
                $.each(prevColumnMap, function (field, column) {
                    listOfIndex.push(lastIndex + 6 + count);
                    count++;
                    aocolumns.push.apply(aocolumns, [{
                        "mData": "exe" + 2 + field,
                        "className": '',
                        "mRender": function (data, type, row) {
                            if (row["exe" + 2]) {
                                return '<div class="stacked-cell-center-top">' + row["exe" + 2][field] + '</div>'
                            } else {
                                return '<div></div>'
                            }
                        },
                        "visible":signal.fieldManagement.visibleColumns("exe" + 2 + field)
                    }])
                })
         }
        return aocolumns
    };

    var drillDownOptions = function (value, substance, pt, id, flag_var, isStartDate, alertId, url, className) {
        var actionButton;
        var caseDrillDownUrl = encodeURI(fetchDrillDownDataUrl + "?substance=" + substance + "&id=" + id + "&pt=" + pt + "&flagVar=" + flag_var + "&isStartDate=" + isStartDate + "&alertId=" + alertId + "&numberOfCount=" + value);
        if (value === 0 || value === "0") {
            actionButton = '<span class="blue-1 ' + (className ? className : '') + '">' + value + '</span>'
        } else if (url && url != '') {
            actionButton = '<div style="display: block" class="btn-group dropdown">' +
                '<a class="dropdown-toggle" data-toggle="dropdown" href="#"><span class="' + className + '">' + value + '</span></a>' +
                '<ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;">' +
                '<li role="presentation"><a href="' + caseDrillDownUrl + '" class="evdas-case-drill-down-link">Case List</a></li>' +
                '<li role="presentation"><a href="' + url + '" target="_blank">Evdas Link</a></li>' +
                '</ul>' +
                '</div>';
        } else {
            actionButton = '<a href="' + caseDrillDownUrl + '" class="evdas-case-drill-down-link"><span class="' + className + '">' + value + '</span></a>'
        }
        return actionButton
    };

    var ptBadge = function (ime, dme, ei, sm) {
        var badgeSpan = "";
        if (ime == "true") {
            badgeSpan += '<span class="grid-menu-tooltip-dynamicWidth" data-title="' + emergingIssuesAbbrIme["label"] + '" ><span class="badge badge-info">' + emergingIssuesAbbrIme["abbr"].toUpperCase() + '</span></span>'
        }
        if (dme == "true") {
            badgeSpan += '<span class="grid-menu-tooltip-dynamicWidth" data-title="' + emergingIssuesAbbrDme["label"] + '" ><span class="badge badge-purple">' + emergingIssuesAbbrDme["abbr"].toUpperCase() + '</span></span>'
        }
        if (ei == "true") {
            badgeSpan += '<span class="grid-menu-tooltip-dynamicWidth" data-title="' + emergingIssuesAbbrEI["label"] + '" ><span class="badge badge-success">' + emergingIssuesAbbrEI["abbr"].toUpperCase() + '</span></span>'
        }
        if (sm == "true") {
            badgeSpan += '<span class="grid-menu-tooltip-dynamicWidth" data-title="' + emergingIssuesAbbrSM["label"] + '" ><span class="badge badge-warning">' + emergingIssuesAbbrSM["abbr"].toUpperCase() + '</span></span>'
        }
        return badgeSpan
    };

    var case_drill_down = function (count, className) {
        var caseDrillDownTableUrl = caseDrillDownUrl + count;
        var actionButton = '<span style="display: block" class="btn-group dropdown ' + className + '">' + count + '</span>';
        return actionButton;
    };

    var showAllSelectedCaseNumbers = function () {
        $('#copyCaseNumberModel').modal({
            show: true
        })

        var numbers = _.map($('input.copy-select:checked').parent().parent().find('td:nth-child(5)'), function (it) {
            return $(it).text()
        })
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

    var init = function (result) {
        signal.fieldManagement.populateColumnList(gridColumnsViewUrl, gridColumnsViewUpdateUrl);
        alertDetailsTable = initAlertDetailsTable(result);

        $('i#copySelection').on('click', function () {
            showAllSelectedCaseNumbers();
        });

        $(document).on('click', 'input#select-all', function () {
            $(".copy-select").prop('checked', this.checked);
            $(".evdas-header-row input#select-all").prop('checked', this.checked);
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

        initUpdateAssignedTo();

        actionButton("#alertDetailsTable");
    };

    $(document).on('click', '.alert-check-box', function () {
        if(!this.checked){
            $('.evdas-header-row input#select-all').prop("checked",false);
        }
    });

    $('#exportTypesEvdas a[href]').on('click', function (e) {
        var clickedURL = e.currentTarget.href;
        var updatedExportUrl = clickedURL;
        var sortCol, sortDir;
        if($('#evdas-case-drill-down-table').DataTable().order()){
            sortCol =$('#evdas-case-drill-down-table').DataTable().order()[0][0];
            sortDir=$('#evdas-case-drill-down-table').DataTable().order()[0][1];
        }
        updatedExportUrl = clickedURL + '&filterText=' + $('.dt-input').val() + '&sortedCol=' + sortCol + '&sortedDir=' + sortDir + '&isArchived=' + isArchived + '&callingScreen=' + callingScreen;
        window.location.href = updatedExportUrl;
        return false;
    });

    $(document).on('click', '.add-evdas-attachment', function (e) {
        e.preventDefault();
        $('#evdasAttachmentModal').css({display: 'none'});
        $.Notification.notify('success', 'top right', "Success", 'EVDAS attachment save is in progress', {autoHideDelay: 5000});
        var formdata = new FormData();
        if($('#evdas-case-drill-down-table').DataTable().order()){
            formdata.append("sortedCol", $('#evdas-case-drill-down-table').DataTable().order()[0][0]);
            formdata.append("sortedDir",$('#evdas-case-drill-down-table').DataTable().order()[0][1]);
        }
        formdata.append("filterText", $('.dt-input').val());
        formdata.append("description", $('#evdas-attachment-box').val());
        formdata.append("isArchived", isArchived);
        formdata.append("alertType",applicationName);
        $.ajax({
            url: attachCaseListingUrl,
            type: "POST",
            mimeType: "multipart/form-data",
            processData: false,
            contentType: false,
            data: formdata
        })
            .done(function (data) {
                $response = JSON.parse(data);
                if ($response.status) {
                    var parentRow = $('#alertsDetailsTable_wrapper .DTFC_ScrollWrapper .DTFC_LeftWrapper .DTFC_LeftBodyWrapper .DTFC_LeftBodyLiner tbody tr').
                    find('td.dt-center').find("[data-id='" + evdasAlertId + "']").closest('tr');
                    showAttachmentIcon(parentRow);
                    $.Notification.notify('success', 'top right', "Success", 'The attachment has been added successfully', {autoHideDelay: 5000});
                } else {
                    $.Notification.notify('error', 'top right', "Error", $response.message, {autoHideDelay: 5000});
                }
            });
    });

    $('#exportTypes a[href]').on('click', function (e) {
        if(callingScreen == CALLING_SCREEN.DASHBOARD) {
                if ($('#alertsDetailsTable').DataTable().page.info().recordsDisplay > 5000) {
                    $.Notification.notify('warning', 'top right', "Warning", "Only first 5000 records will be exported. Please use the filter criteria to limit your search.", {autoHideDelay: 5000});
                }
        }
        var clickedURL = e.currentTarget.href;
        var updatedExportUrl = clickedURL;
        var ids = [];
        $('.copy-select:checked').each(function () {
            if ($(this)[0]['checked']) {
                ids.push($(this).attr('data-id'))
            }
        });
        var checkedColumns = {}
        $('#tableColumns tr ').each(function () {
            var fieldName = $(this).children().children().attr('data-field');
            if (fieldName) {
                checkedColumns[fieldName] = $(this).children()[1].firstChild.checked
            }
        });

        var isFilterRequest = true;
        var prefix = "ev_";
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

        if (ids.length > 0) {
            updatedExportUrl = clickedURL + '&isFilterRequest=' + isFilterRequest + '&filters=' + encodeURI(JSON.stringify(filterValues)) + "&selectedCases=" + ids + "&viewId="+viewId + '&isArchived=' + isArchived;

        } else {
            var filterList = {}
            $(".yadcf-filter-wrapper").each(function () {
                var filterVal = $(this).children().val();
                if (filterVal) {
                    filterList[$(this).prev().attr('data-field')] = filterVal
                }
            });
            var filterListJson = JSON.stringify(filterList);
            updatedExportUrl = clickedURL + '&isFilterRequest=' + isFilterRequest + '&filters=' + encodeURI(JSON.stringify(filterValues)) + "&frequency=" + freqSelected + "&viewId="+ viewId + "&filterList=" + encodeURIComponent(filterListJson) + '&isArchived=' + isArchived;
            if ($('#advanced-filter').val()) {
                updatedExportUrl += "&advancedFilterId="+$('#advanced-filter').val()
            }
        }
        window.location.href = updatedExportUrl;
        sessionStorage.setItem(prefix + "filters_value", JSON.stringify(filterValues));
        sessionStorage.setItem(prefix + "id", id);
        return false
    });

    $(".exportAlertHistories").on('click', function (e) {
        var caseHistoryModal = $('#caseHistoryModal');
        var caseVal = caseHistoryModal.find("#caseNumber").html();
        var productFamily = caseHistoryModal.find("#productFamily").html();
        var selectedCase = e.currentTarget.href + "&caseNumber=" + caseVal + "&productFamily=" + encodeURIComponent(productFamily);
        window.location.href = selectedCase;
        return false
    });

    var appendPrevExecColumns = function (i, dateRange) {
        var iVisible = i + 1;
        var updatedDateRange = '';
        if (dateRange != '-')
            updatedDateRange = "<div class='stacked-cell-center-top'>" + dateRange + "</div>";

        $.each(prevColumnMap, function (key, value) {

            $("<th class='extraColumns' data-field='exe" + i + key + "'><div class='th-label' data-field='exe" + i + key + "'><div class='stacked-cell-center-top'>" + value + "</div>" + updatedDateRange + "<div class='stacked-cell-center-top'> Prev Period " + iVisible + "</div></div></th>").appendTo("#alertsDetailsTableRow");
        })
    };
    var loadTable = function (result) {
        init(result);
        $("#alertsDetailsTable_filter").hide();

        //list of hidden columns based on saved view
        if(sessionStorage.getItem('eda_notVisibleColumn_' +executedConfigId) || $('#columnIndex').val() != ""){
            listOfIndex = signal.alertReview.createListOfIndex('eda_notVisibleColumn_' + executedConfigId, true, 'eda_viewName_' + executedConfigId);
        }
    };
    if (callingScreen == CALLING_SCREEN.TRIGGERED_ALERTS) {

        var getDateRange = function (frequency) {
            $(".extraColumns").remove();
            $.ajax({
                type: "POST",
                url: dateRangeListUrl + "?frequency=" + frequency,
            })
                .done(function (result) {
                    $.each(result, function (index, data) {
                        if (index != "exeRecent") {
                            var daterange = data.startDate.replace(/\-/g, "/") + '-' + data.endDate.replace(/\-/g, "/");
                            var i = index.slice(-1);
                            appendPrevExecColumns(i, daterange)
                        } else {
                            $('#alertsDetailsTable .dateRange').append(data.startDate.replace(/\-/g, "/") + '-' + data.endDate.replace(/\-/g, "/"));
                        }
                    });
                    delete result["exeRecent"];
                    loadTable(result)
                });
        };
        $("#frequencyNames").on('change', function () {
            freqSelected = $("#frequencyNames").val();
            if (window.sessionStorage) {
                sessionStorage.setItem("frequency", freqSelected);
            }
            location.reload();
        });
        if (window.sessionStorage) {
            if (sessionStorage.getItem("frequency")) {
                $("#frequencyNames").val(sessionStorage.getItem("frequency"));
                freqSelected = $("#frequencyNames").val();
                getDateRange(sessionStorage.getItem("frequency"));
            } else {
                $("#frequencyNames").val($("#frequencyNames option:first").val());
                $("#frequencyNames").trigger('change')
            }
        }
    } else if(callingScreen == CALLING_SCREEN.REVIEW){
        for (var prevColumn = 0; prevColumn < parseInt(prevColCount); prevColumn++) {
            appendPrevExecColumns(prevColumn, dateRangeArray[prevColumn] ? dateRangeArray[prevColumn] : '-')
        }
        loadTable("")
    } else {
        loadTable("")
    }
    $(document).on('change', '.copy-select', function(){
        addToSelectedCheckBox($(this))
    });
});

function parseRorValue(rorValues) {
    var result = rorValues.map(function (each_element) {
        return Number(parseFloat(each_element).toFixed(2));
    });
    return result
}

function applyEvdasRules(row, data) {
    if (data.evdasRuleFormat) {
        var resultJson = JSON.parse(data.evdasRuleFormat);
        $.each(resultJson, function (index, obj) {
            if (obj.isFontColor) {
                $(row).find('.' + obj.key).css('color', obj.color);
            } else {
                $(row).find('.' + obj.key).parents('td').css('background-color', obj.color);
            }
        });
    }
}

$(document).on('click', '.signalSummaryAuth', function (evt) {
    $.Notification.notify('warning','top right', "Warning", "You don't have access to view signal summary", {autoHideDelay: 5000});
})
function populateSelectedCases() {
    $(".copy-select").on('change', function () {
        if (selectedCases.indexOf($(this).attr("data-id")) == -1 && $(this).is(':checked')) {
            selectedCases.push($(this).attr("data-id"));
            selectedCasesInfo.push(populateDispositionDataFromGrid($(this).closest('tr').index()));
        } else if (selectedCases.indexOf($(this).attr("data-id")) != -1 && !$(this).is(':checked')) {
            selectedCasesInfo.splice($.inArray($(this).attr("data-id"), selectedCases), 1);
            selectedCases.splice($.inArray($(this).attr("data-id"), selectedCases), 1);//Step down the removal of selectedCases because data index is taken from that array for removal from selectedCasesInfo map
        }
    });
}