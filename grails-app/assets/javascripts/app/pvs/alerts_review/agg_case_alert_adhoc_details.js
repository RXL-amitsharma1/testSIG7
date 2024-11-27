//= require app/pvs/common/rx_common.js
//= require app/pvs/common/rx_alert_utils.js
//= require app/pvs/common/rx_list_utils.js
//= require app/pvs/activity/activities.js
//= require app/pvs/actions/actions.js
//= require app/pvs/common/rx_handlebar_ext.js
//= require app/pvs/caseDrillDown/caseDrillDown.js
//= require app/pvs/alertComments/alertComments.js
//= require app/pvs/alerts_review/alert_review.js
//= require app/pvs/common_tag.js
//= require app/pvs/disposition/disposition-change.js

var applicationName = "Aggregate Case Alert on Demand";
var applicationNameFaers = "Aggregate Case Alert on Demand - FAERS";
var applicationNameVaers = "Aggregate Case Alert on Demand - VAERS";
var applicationNameVigibase = "Aggregate Case Alert on Demand - VIGIBASE";
var applicationNameJader = "Aggregate Case Alert on Demand - JADER";
var applicationNameSmq = "Aggregate Case Alert - SMQ on Demand";
var applicationNameSmqFaers = "Aggregate Case Alert - SMQ on Demand FAERS";
var applicationNameSmqVaers = "Aggregate Case Alert - SMQ on Demand VAERS";
var applicationNameSmqVigibase = "Aggregate Case Alert - SMQ on Demand VIGIBASE";
var applicationNameSmqJader = "Aggregate Case Alert - SMQ on Demand JADER";
var applicationLabel = "AggregateOnDemandAlert";
var subGroupsMap = [];
var paginationAdvancedFilter = true; //Is used to determine whether to apply the last advanced filter stored in session storage during pagination
var allSubGroupMap = [];
var subGroupColumnInfo= [];
var table;
var dir = '';
var index = -1;
var executedIdList = [];
var productNames=[];
var isAgeEnabled;
var isGenderEnabled;
var rorRelSubGrpEnabled;
var tags = [];
var alertType;
var acaAdhocEntriesCount=sessionStorage.getItem('acaAdhocPageEntries')!=null?sessionStorage.getItem('acaAdhocPageEntries'):50
var fixedFilterColumn = []

$(function () {
    $(document).on('fieldsRearranged', function (e) {
        $('.yadcf-filter-wrapper').remove();
        init_filter($('#alertsDetailsTable').DataTable())
    })
    if(window.sessionStorage.getItem('lastAdvancedFilter')){
        window.sessionStorage.removeItem('lastAdvancedFilter')
    }
    if($(".data-analysis-button").find("li[class!=hidden]").length === 0){
        $(".data-analysis-button").addClass("hidden");
    }
    var buttonClassVar = "";
    if(typeof buttonClass !=="undefined" && buttonClass){
        buttonClassVar = buttonClass;
    }
    $('#alertsDetailsTable .dateRange').append(dr);


    var fixedColumns;

    if (groupBySmq == "true"){
        fixedColumns = 3;
    } else {
        fixedColumns = 4;
    }

    var filterIndex = filterIndexList;

    if (callingScreen == CALLING_SCREEN.REVIEW) {
        $(window).on('beforeunload', function () {
            //discard the temporary changes in the view
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

    var freqSelected = "";

    var dateRangeString = $('#listDateRange').val().slice(1, -1);
    var dateRangeArray = [];
    if (dateRangeString != "") {
        dateRangeArray = dateRangeString.split(', ');
    }

    $(window).on('beforeunload', function(){
        if (sessionStorage.getItem('isViewCall') == 'true' ) {// column level filter is not retained on page refresh
            var backUrl = sessionStorage.getItem('back_url');
            var oldProductNameList = sessionStorage.getItem(PRODUCT_LIST);
            sessionStorage.clear();
            sessionStorage.setItem('back_url', backUrl);
            sessionStorage.setItem(PRODUCT_LIST, oldProductNameList);
        } else {
            var viewInfo = signal.alertReview.generateViewInfo(filterIndex);
            sessionStorage.setItem('aca_adhoc_filterMap_' + executedConfigId, JSON.stringify(viewInfo.filterMap));
            sessionStorage.setItem('aca_adhoc_sortedColumn_' + executedConfigId, JSON.stringify(viewInfo.sortedColumn));
            sessionStorage.setItem('aca_adhoc_notVisibleColumn_' + executedConfigId, viewInfo.notVisibleColumn);
            sessionStorage.setItem('aca_adhoc_viewName_' + executedConfigId, $('.viewSelect :selected').text().replace("(default)", "").trim())
        }
    });

    var _changeIntervalSoc = null;
    var _changeIntervalPt = null;

    $(document).on('keyup', 'input#yadcf-filter-alertsDetailsTable-2', function (e) {

        var casenum=$(this).val();
        var caseInput = $(this);
        clearInterval(_changeIntervalSoc)
        _changeIntervalSoc = setInterval(function() {
            $("#alertsDetailsTable").DataTable().column(2).search(casenum); //Exact value, column, reg
            if(casenum == "") {
                fixedFilterColumn.push({"column":2, "value":""});
                caseInput.removeClass("inuse");
            } else {
                fixedFilterColumn.push({"column":2, "value":casenum});
                caseInput.addClass("inuse");
            }
            clearInterval(_changeIntervalSoc)
        }, 1500);

    });

    if(groupBySmq != 'true') {
        $(document).on('keyup', 'input#yadcf-filter-alertsDetailsTable-3', function (e) {

            var casenum = $(this).val();
            var caseInput = $(this);
            clearInterval(_changeIntervalPt);
            _changeIntervalPt = setInterval(function () {
                $("#alertsDetailsTable").DataTable().column(3).search(casenum); //Exact value, column, reg
                if (casenum == "") {
                    fixedFilterColumn.push({"column":3, "value":""});
                    caseInput.removeClass("inuse");
                } else {
                    fixedFilterColumn.push({"column":3, "value":casenum});
                    caseInput.addClass("inuse");
                }
                clearInterval(_changeIntervalPt);
            }, 1500);

        });
    }


    function showPopup(data) {
        var evdasDataLogModal = $("#downloadReportModal");
        $(evdasDataLogModal).find("#productName").val(data);
        evdasDataLogModal.modal("show");
    }
    fetchSubGroupsMap();
    fetchTags();
    fetchViewInstanceList();

    signal.actions_utils.set_action_list_modal($('#action-modal'));
    signal.actions_utils.set_action_create_modal($('#action-create-modal'));


    var detail_table;
    var activities_table;
    var executedIdList = [];

    var checkedIdList = [];
    var checkedRowList = [];

    if (groupBySmq == "true" && $("#isFaers").val() == "true") {
        alertType = applicationNameSmqFaers
    } else if (groupBySmq == "true" && $("#isVaers").val() == "true") {
        alertType = applicationNameSmqVaers
    } else if (groupBySmq == "true" && $("#isVigibase").val() == "true") {
        alertType = applicationNameSmqVigibase
    } else if (groupBySmq == "true" && $("#isJader").val() == "true") {
        alertType = applicationNameSmqJader
    } else if (groupBySmq == "true") {
        alertType = applicationNameSmq
    } else if($("#isFaers").val() == "true"){
        alertType = applicationNameFaers
    } else if($("#isVaers").val() == "true"){
        alertType = applicationNameVaers
    } else if($("#isVigibase").val() == "true"){
        alertType = applicationNameVigibase
    } else if($("#isJader").val() == "true"){
        alertType = applicationNameJader
    } else{
        alertType = applicationName
    }
    if(miningVariable){
        alertType = alertType + "-" + miningVariable
    }
    signal.alertReview.populateAdvancedFilterSelect(alertType)
    signal.commonTag.openCommonAlertTagModal("PVS","Quantitative on demand", tags);
    signal.alertReview.setSortOrder();
    signal.caseDrillDown.bind_drill_down_table(caseDetailUrl);

    var filterValue = signal.alertReview.createFilterMap('aca_adhoc_filterMap_'+executedConfigId, 'aca_adhoc_viewName_' + executedConfigId);

    $(".downloadMemoReport").on('click', function (event) {
        showPopup($(event.target).parent().data('id'));
    });

    $("#showAll").on('click', function () {
        var url = listConfigUrl + "&specialPE=" + false;
        table.ajax.url(url).load();
    });

    $("#showSpecialPE").on('click', function () {
        var url = listConfigUrl + "&specialPE=" + true;
        table.ajax.url(url).load();
    });

    $(document).on('click', ".generate-case-series", function () {
        var metaInfo = $(this).attr("data-url");
        var parent_row = $(event.target).closest('tr');
        var execConfigId = parent_row.find("#execConfigId").val();
        var productName = parent_row.find('span[data-field="productName"]').attr("data-id");
        var eventName = parent_row.find('span[data-field="eventName"]').attr("data-id");
    });

    //apply the sorting to column based on saved view
    var sortingMap = signal.alertReview.createSortingMap('aca_adhoc_sortedColumn_'+ executedConfigId, 'aca_adhoc_viewName_' + executedConfigId);
    if (sortingMap != 'undefined' && sortingMap.length > 0) {
        index = sortingMap[0][0];
        dir = sortingMap[0][1]
    }
    $('#alert-list-filter-apply-bt').on('click', function (evt) {
        evt.preventDefault();
        var filterParams = $('#alert-list-filter').serialize();
        var theDataTable = $('#alertsDetailsTable').DataTable();
        $('#alert-list-filter').find('#filterUsed').val(true);
        var newUrl = encodeURIComponent(listConfigUrl + "&" + filterParams + "&filterApplied=" + true);
        theDataTable.ajax.url(newUrl).load();
    });

    var init = function (result, groupBySmq) {
        signal.fieldManagement.populateColumnList(gridColumnsViewUrl, gridColumnsViewUpdateUrl);
        detail_table = init_details_table(result, groupBySmq);

        $('i#copySelection').on('click', function () {
            showAllSelectedCaseNumbers();
        });
        $(document).on('click', 'input#select-all', function () {
            $(".copy-select").prop('checked', this.checked);
            checkboxSelector = '.copy-select';
            $.each($(checkboxSelector), function () {
                if(selectedCases.indexOf($(this).attr("data-id")) == -1 && $(this).is(':checked')){
                    selectedCases.push($(this).attr("data-id"));
                    selectedCasesInfoSpotfire.push(populateDispositionData($(this).closest('tr').index()));
                } else if(selectedCases.indexOf($(this).attr("data-id")) != -1 && !$(this).is(':checked')){
                    selectedCases.splice( $.inArray($(this).attr("data-id"), selectedCases), 1 );
                    selectedCasesInfoSpotfire.splice($.inArray($(this).attr("data-id"), selectedCases), 1);
                }
            })
        });


        actionButton("#alertsDetailsTable");
    };

    var populateActivity = function () {
        if (!activities_table) {
            if (callingScreen == 'review') {
                activities_table = signal.activities_utils.init_activities_table("#activitiesTable",
                    '/signal/activity/listByExeConfig?' + 'executedIdList=' + executedIdList + "&app" +
                    "Type=" + applicationName, applicationName);
            } else {
                activities_table = signal.activities_utils.init_activities_table("#activitiesTable",
                    '/signal/activity/listActivities?' + 'appType=' + applicationName, applicationName);
            }
        }
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

    var init_filter = function (data_table, prrVisibility, rorVisibility, ebgmVisibility, dssVisibility, groupBySmq) {

        var filterOptions = [];
        $.each(filterIndexMap, function (key, value) {
            if (value == 'text') {
                filterOptions.push({
                    column_number: key,
                    filter_type: 'text',
                    filter_reset_button_text: false,
                    filter_delay: 600,
                    filter_default_label: '',
                    style_class: 'textFilter',
                    fixed_column: groupBySmq == 'true'? ((key ==  2) ? true: false): ((key ==  2 || key ==  3) ? true: false)
                })
            } else if (value == 'select') {
                filterOptions.push({
                    column_number: key,
                    select_type: 'select',
                    filter_default_label: ALL,
                    filter_reset_button_text: false
                })
            } else {
                filterOptions.push({
                    column_number: key,
                    filter_type: 'text',
                    filter_reset_button_text: false,
                    filter_delay: 600,
                    filter_default_label: '',
                    style_class: 'countFilter'
                })
            }
        });

        yadcf.init(data_table, filterOptions);
        $(".yadcf-filter").removeAttr('placeholder');
        if (filterValue.length > 0) {
            yadcf.exFilterColumn(data_table, filterValue, true);
        } else {
            $('.yadcf-filter-wrapper').hide()
        }

        signal.fieldManagement.init($('#alertsDetailsTable').DataTable(), '#alertsDetailsTable', fixedColumns, false);
        $('.yadcf-filter-wrapper').hide();
    };

    /**
     *  colIndex for Product column in aggregate adhoc alert is 1
     */

    var init_product_list = function () {
        var colIndex = 1;
        var filterValue = JSON.parse(sessionStorage.getItem("aca_filterMap_" + executedConfigId));
        var select = document.getElementById("yadcf-filter-alertsDetailsTable-" + colIndex);
        select.options.length = 0;
        for (var i = 0; i < productNames.length; i++) {
            if (i == 0) {
                select.options[select.options.length] = new Option(ALL, '', false, false);
            } else {
                var isSelected = false;
                if (filterValue != null && filterValue[colIndex] != undefined && filterValue[colIndex] != "" && filterValue[colIndex] != null && productNames[i] == filterValue[colIndex]) {
                    isSelected = true;
                }
                select.options[select.options.length] = new Option(productNames[i], productNames[i], isSelected, isSelected);
            }
        }
    }
    var init_details_table = function (result, groupBySmq) {
        var isFilterRequest = true;
        var filterValues = [];
        var prefix = "agg_adhoc_";
        if (window.sessionStorage) {
            if (signal.alertReview.isAlertPersistedInSessionStorage(prefix)) {
                filterValues = JSON.parse(sessionStorage.getItem(prefix + "filters_value"));
            } else {
                signal.alertReview.removeFiltersFromSessionStorage(prefix);
                isFilterRequest = false;
            }
        }

        var prrVisibility = JSON.parse($("#showPrr").val());
        var rorVisibility = JSON.parse($("#showRor").val());
        var ebgmVisibility = JSON.parse($("#showEbgm").val());
        var dssVisibility = JSON.parse($("#showDss").val());
        var columns = create_agg_table_columns(callingScreen, prrVisibility, rorVisibility, ebgmVisibility, dssVisibility, result, groupBySmq);
        var prev_page = [];
        $(document).on('click', '#alertsDetailsTable_paginate', function () {
            if($('.alert-select-all').is(":checked") && !prev_page.includes($('li.active').text().slice(-3).trim())){
                prev_page.push($('li.active').text().slice(-3).trim());
            }
        });

        table = $('#alertsDetailsTable').DataTable({
            layout: {
                topStart: null,
                topEnd: null,
                bottomStart: ['pageLength', 'info', {paging: {type: "full_numbers"}}],
                bottomEnd: null,
            },
            customProcessing: true,
            "language": {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json"
            },
            rowCallback: function (row, data, index) {
                $(row).find('td').height(45);
            },
            drawCallback: function (settings) {
                var current_page = $('li.active').text().slice(-3).trim();
                if(typeof prev_page != 'undefined' && $.inArray(current_page, prev_page) !== -1){
                    $(".alert-select-all").prop('checked', true);
                    $(".copy-select").prop('checked', true);
                } else {
                    $(".alert-select-all").prop('checked', false);
                    $(".copy-select").prop('checked', false);
                }
                var rowsDataAD = $('#alertsDetailsTable').DataTable().rows().data();
                if(settings.json != undefined) {
                    pageDictionaryForAlertDetails($('#alertsDetailsTable_wrapper')[0], settings.aLengthMenu[0][0], settings.json.recordsFiltered);

                }else {
                    pageDictionaryForAlertDetails($('#alertsDetailsTable_wrapper')[0], 50, rowsDataAD.length);
                }
                initPSGrid($('#alertsDetailsTable_wrapper'));
                tagEllipsis($('#alertsDetailsTable'));
                colEllipsis();
                webUiPopInitForCategories();
                webUiPopInit();
                closeInfoPopover();
                populateSelectedCases();
                showInfoPopover();
                enterKeyAlertDetail();
                closePopupOnScroll();
                populateSelectedCases();
                var checkedId
                $('input[type=checkbox]').each(function () {
                    checkedId = $(this).attr('data-id')
                    if (selectedCases.includes(checkedId)) {
                        $(this).prop('checked', true);
                    }
                });
                $('.dt-paging').on('change', function () {
                    var countVal = $('.dt-paging').val()
                    sessionStorage.setItem("acaAdhocPageEntries", countVal);
                    acaAdhocEntriesCount = sessionStorage.getItem("acaAdhocPageEntries");
                })
                signal.alertReview.enableMenuTooltips();
                signal.alertReview.enableMenuTooltipsDynamicWidth();
                signal.alertReview.sortIconHandler();
                $("#yadcf-filter-alertsDetailsTable-1").addClass("custom-select-product").select2();
                $(".dt-scroll-body").append("<div style='height:3px'></div>");
            },
            "ajax": {
                "url": listConfigUrl + '&isFilterRequest=' + isFilterRequest + '&filters=' + encodeURIComponent(JSON.stringify(filterValues)) + "&frequency=" + freqSelected,
                "type": "POST",
                "dataSrc": "aaData",
                "data": function (d) {
                    var colIndex = 1;
                    var filterMap = JSON.parse(sessionStorage.getItem("aca_filterMap_"+executedConfigId)) != null ? JSON.parse(sessionStorage.getItem("aca_filterMap_"+executedConfigId)) : new Map();
                    var searchValue = d.columns[colIndex].search.value;
                    if(filterMap){
                        filterMap[colIndex] = searchValue
                    }
                    sessionStorage.setItem('aca_filterMap_' + executedConfigId, JSON.stringify(filterMap));

                    d.order[0].column = index;
                    d.order[0].dir = dir;
                    if ($('#advanced-filter').val()) {
                        d.advancedFilterId = $('#advanced-filter').val()
                        paginationAdvancedFilter = false;
                    }
                    if ($('#queryJSON').val()) {
                        d.queryJSON = $('#queryJSON').val();
                        sessionStorage.setItem('lastAdvancedFilter' , $('#queryJSON').val());
                    } else if (sessionStorage.getItem('lastAdvancedFilter')) {
                        if (paginationAdvancedFilter) {
                            d.queryJSON = sessionStorage.getItem('lastAdvancedFilter');
                        } else {
                            sessionStorage.removeItem('lastAdvancedFilter');
                        }
                    }
                }, // pvs-44818, fix for slowness in field selection show
                beforeSend: function() {
                    if(typeof table != "undefined") {
                        var xhr = table.settings()[0].jqXHR;
                        if(xhr && xhr.readyState != 4){
                            xhr.abort();
                        }
                    }
                    setTimeout(function () {
                        signal.fieldManagement.bindFieldConfigurationSortEvent();
                    },500)
                }
            },
            initComplete: function (settings, json) {
                init_filter(table, prrVisibility, rorVisibility, ebgmVisibility, dssVisibility, groupBySmq);
                if (sessionStorage.getItem(prefix + "filters_store")) {
                    setQuickDispositionFilter(prefix);
                }
                signal.alertReview.enableMenuTooltips();
                signal.alertReview.disableTooltips();
                showInfoPopover();
                addGridShortcuts('#alertsDetailsTable');
                var sessionProduct = JSON.parse(sessionStorage.getItem(PRODUCT_LIST));
                if (typeof json != "undefined") {
                    productNames = [ALL]
                    for (var i = 0; i < json.productNameList?.length; i++) {
                        productNames.push(json.productNameList[i]);
                    }
                    sessionStorage.setItem(PRODUCT_LIST, JSON.stringify(productNames))
                } else {
                    if(sessionProduct) {
                        for (var i = 0; i < sessionProduct.length; i++) {
                            productNames.push(sessionProduct[i])
                        }
                    }
                }
                init_product_list();
                $("#toggle-column-filters, #ic-toggle-column-filters").on('click', function () {
                    init_product_list()
                    var ele = $('.yadcf-filter-wrapper');
                    if (ele.is(':visible')) {
                        ele.hide();
                    } else {
                        ele.show();
                        $('.filter-center').css("display", "flex");
                        $('.yadcf-filter').first().focus();
                    }
                    $("#yadcf-filter-alertsDetailsTable-1").addClass("custom-select-product").select2();


                });
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
            "iTotalDisplayRecords": acaAdhocEntriesCount,
            "iDisplayLength": parseInt(acaAdhocEntriesCount),
            "deferLoading":filterValue.length > 0 ? 0: null,
            "aoColumns": columns,
            "responsive": true,
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
        return table;
    };
    var newJson = JSON.parse(adhocColumnListNew)

    var create_agg_table_columns = function (callingScreen, prrVisibility, rorVisibility, ebgmVisibility) {
        var aoColumns = [
            {
                "mData": "selected",
                "mRender": function (data, type, row) {
                    var checkboxhtml =  '<input class="execConfigId" id="execConfigId" type="hidden" value="' + row.execConfigId + '" />' +
                        '<input class="alertConfigId" id="alertConfigId" type="hidden" value="' + row.alertConfigId + '" />' +
                        '<input class="productName" id="productName" type="hidden" value="' + row.productName + '" />' +
                        '<input class="preferredTerm" id="preferredTerm" type="hidden" value="' + row.preferredTerm + '" />' +
                        '<input class="isArchived" id="isArchived" type="hidden" value="' + row.isArchived + '" />' +
                        '<input data-field ="productFamily" data-id="' + encodeToHTML(row.productFamily) + '" type="hidden" value="' + encodeToHTML(row.productFamily) + '" />' +
                        '<input data-field ="primaryEvent" data-id="' + encodeToHTML(row.primaryEvent) + '" type="hidden" value="' + encodeToHTML(row.primaryEvent) + '" />';
                    if (alertIdSet.has(JSON.stringify(row.id)) || selectedCases.includes(row.id.toString())) {
                        return checkboxhtml + '<input type="checkbox" class="alert-check-box editor-active copy-select" data-id=' + row.id + ' checked/>'
                    } else {
                        return checkboxhtml + '<input type="checkbox" class="alert-check-box editor-active copy-select" data-id=' + row.id + ' />'
                    }
                },
                "className": "col-min-50 col-max-50 word-break",
                "sortable": false,
                "visible": true
            }];
        $.each(newJson, function(key,map){
            if(map.name === "productName"){
                aoColumns.push.apply(aoColumns, [
                    {
                        "mData": "productName",
                        'className': 'col-min-150 col-max-150 cell-break word-wrap-break-word',
                        "mRender": function (data, type, row) {
                            var configId = '<input class="execConfigId" id="execConfigId" type="hidden" value="' + row.execConfigId + '" />' +
                                '<input class="alertConfigId" id="alertConfigId" type="hidden" value="' + row.alertConfigId + '" />';

                            var result = configId + '<input class="productName" id="productName" type="hidden" value="' + row.productName + '" />' +
                                '<input class="preferredTerm" id="preferredTerm" type="hidden" value="' + row.preferredTerm + '" />' +
                                '<input class="isArchived" id="isArchived" type="hidden" value="' + row.isArchived + '" />' + "<input type='hidden' value='" + row.productId + "' class='row-product-id'/>" + "<input type='hidden' value='" + row.ptCode + "' class='row-pt-code'/>"
                            "<input type='hidden' value='" + row.level + "' class='row-level-id'/>";
                            if (row.isSpecialPE) {
                                result += "<span style='color:#ff0000'  data-field ='productName' data-id='" + row.productName + "'>" + row.productName + "</span>"
                            } else {
                                result += "<span data-field ='productName' data-id='" + row.productName + "'>" + row.productName + "</span>"
                            }
                            return result;
                        },
                        "visible": true,
                        "sortable": false,
                    }]);
            }else if(map.name === "soc"){
                aoColumns.push.apply(aoColumns, [{
                    "mData": "soc",
                    "mRender": function (data, type, row) {
                        if (row.soc != 'undefined') {
                            if (row.fullSoc != null) {
                                return '<span class="grid-menu-tooltip" data-title="' + row.fullSoc + '" >' + row.soc + '</span>'
                            } else {
                                if (row.dataSource.toString().toLowerCase() == "jader") {
                                    return '<span class="grid-menu-tooltip" data-title="' + row.soc + '" >' + row.soc+ '</span>'
                                }else{
                                    return '<span class="grid-menu-tooltip" data-title="' + row.soc + '" >' + row.soc.substring(0,4) + '</span>'
                                }
                            }
                        } else {
                            return '<span>' + '-' + '</span>'
                        }
                    },
                    "className": 'col-min-150 col-max-150 cell-break word-wrap-break-word',
                    "visible": true
                }]);
            }else if(map.name === "pt"){
                aoColumns.push.apply(aoColumns, [
                    {
                        "mData": "pt",
                        "mRender": function (data, type, row) {
                            if (row.isSpecialPE) {
                                return "<span style='color:red' data-field ='eventName' data-id='" + row.preferredTerm + "'>" + (row.preferredTerm) + "</span>"
                            } else {
                                return "<span data-field ='eventName' data-id='" + row.preferredTerm + "'>" + (row.preferredTerm) + "</span>"
                            }
                        },
                        "className": 'col-min-150 col-max-150 cell-break word-wrap-break-word',
                        "visible": true
                    }]);
            }else if(map.name === "alertTags"){
                aoColumns.push.apply(aoColumns, [
                    {

                        "mData": "alertTags",
                        "mRender": function (data, type, row) {
                            var tagsElement = signal.alerts_utils.get_tags_element(row.alertTags);
                            return tagsElement
                        },
                        "className": 'col-min-200 col-max-200',
                        "sortable": false,
                        "visible": signal.fieldManagement.visibleColumns('alertTags')
                    }]);
            }else if(map.name === "impEvents"){
                aoColumns.push.apply(aoColumns, [
                    {
                        "mData": "impEvents",
                        "className": 'col-min-100 col-max-100',
                        "mRender": function (data, type, row) {
                            return ptBadge(row.ime, row.dme, row.ei, row.isSm);
                        },
                        "visible": signal.fieldManagement.visibleColumns('impEvents')
                    }]);
            }else if(map.name === "listed"){
                aoColumns.push.apply(aoColumns, [{
                    "mData": "listed",
                    "className": '',
                    "mRender": function (data, type, row) {
                        return '<span>' + row.listed + '</span>'
                    },
                    "visible": signal.fieldManagement.visibleColumns('listed')
                }])
            } else if (map.name === "newCount") {
                aoColumns.push.apply(aoColumns, [
                    {
                        "mData": "newCount",
                        "mRender": function (data, type, row) {
                            if (map.isHyperLink) {
                                return signal.utils.stacked(
                                    cnt_drill_down(row.dataSource, row.newCount, COUNTS.NEW, FLAGS.CUMM_FLAG, map.keyId, row.execConfigId, row.id, row.productId, row.productName, row.ptCode, BUSINESS_RULE_FORMAT_CLASS.NEW_COUNT_AGG, row.groupBySmq, row.preferredTerm, row.soc, row.productSelection),
                                    cnt_drill_down(row.dataSource, row.cummCount, COUNTS.CUMM, FLAGS.CUMM_FLAG, map.keyId, row.execConfigId, row.id, row.productId, row.productName, row.ptCode, BUSINESS_RULE_FORMAT_CLASS.CUMM_COUNT_AGG, row.groupBySmq, row.preferredTerm, row.soc, row.productSelection))
                            } else {
                                return signal.utils.stacked(
                                    '<span>' + row.newCount + '</span>',
                                    '<span>' + row.cummCount + '</span>'
                                );
                            }
                        },
                        className: '',
                        "visible": signal.fieldManagement.visibleColumns('newCount')
                    }]);
            } else if (map.name === "newSponCount") {
                aoColumns.push.apply(aoColumns, [
                    {
                        "mData": "newSponCount",
                        "mRender": function (data, type, row) {
                            if (map.isHyperLink) {
                                return signal.utils.stacked(
                                    cnt_drill_down(row.dataSource, row.newSponCount, COUNTS.NEW, FLAGS.SPONT_FLAG, map.keyId, row.execConfigId, row.id, row.productId, row.productName, row.ptCode, 'NEW_SPON', row.groupBySmq, row.preferredTerm, row.soc, row.productSelection),
                                    cnt_drill_down(row.dataSource, row.cumSponCount, COUNTS.CUMM, FLAGS.SPONT_FLAG, map.keyId, row.execConfigId, row.id, row.productId, row.productName, row.ptCode, 'CUMM_SPON', row.groupBySmq, row.preferredTerm, row.soc, row.productSelection));
                            } else {
                                return signal.utils.stacked(
                                    '<span>' + row.newSponCount + '</span>',
                                    '<span>' + row.cumSponCount + '</span>'
                                );
                            }
                        },
                        className: '',
                        "visible": signal.fieldManagement.visibleColumns('newSponCount')
                    }]);
            } else if (map.name === "newSeriousCount") {
                aoColumns.push.apply(aoColumns, [
                    {
                        "mData": "newSeriousCount",
                        "mRender": function (data, type, row) {
                            if (map.isHyperLink) {
                                return signal.utils.stacked(
                                    cnt_drill_down(row.dataSource, row.newSeriousCount, COUNTS.NEW, FLAGS.SERIOUS_FLAG, map.keyId, row.execConfigId, row.id, row.productId, row.productName, row.ptCode, 'NEW_SER', row.groupBySmq, row.preferredTerm, row.soc, row.productSelection),
                                    cnt_drill_down(row.dataSource, row.cumSeriousCount, COUNTS.CUMM, FLAGS.SERIOUS_FLAG, map.keyId, row.execConfigId, row.id, row.productId, row.productName, row.ptCode, 'CUMM_SER', row.groupBySmq, row.preferredTerm, row.soc, row.productSelection))
                            } else {
                                return signal.utils.stacked(
                                    '<span>' + row.newSeriousCount + '</span>',
                                    '<span>' + row.cumSeriousCount + '</span>'
                                );
                            }
                        },
                        className: '',
                        "visible": signal.fieldManagement.visibleColumns('newSeriousCount')
                    }]);
            } else if (map.name === "newFatalCount") {
                aoColumns.push.apply(aoColumns, [
                    {
                        "mData": "newFatalCount",
                        "mRender": function (data, type, row) {
                            if (map.isHyperLink) {
                                return signal.utils.stacked(
                                    cnt_drill_down(row.dataSource, row.newFatalCount, COUNTS.NEW, FLAGS.FATAL_FLAG, map.keyId, row.execConfigId, row.id, row.productId, row.productName, row.ptCode, 'NEW_FATAL', row.groupBySmq, row.preferredTerm, row.soc, row.productSelection),
                                    cnt_drill_down(row.dataSource, row.cumFatalCount, COUNTS.CUMM, FLAGS.FATAL_FLAG, map.keyId, row.execConfigId, row.id, row.productId, row.productName, row.ptCode, 'CUMM_FATAL', row.groupBySmq, row.preferredTerm, row.soc, row.productSelection))
                            } else {
                                return signal.utils.stacked(
                                    '<span>' + row.newFatalCount + '</span>',
                                    '<span>' + row.cumFatalCount + '</span>'
                                );
                            }
                        },
                        className: '',
                        "visible": signal.fieldManagement.visibleColumns('newFatalCount')
                    }]);
            } else if (map.name === "newStudyCount") {
                aoColumns.push.apply(aoColumns, [
                    {
                        "mData": "newStudyCount",
                        "mRender": function (data, type, row) {
                            if (map.isHyperLink) {
                                return signal.utils.stacked(
                                    cnt_drill_down(row.dataSource, row.newStudyCount, COUNTS.NEW, FLAGS.STUDY_FLAG, map.keyId, row.execConfigId, row.id, row.productId, row.productName, row.ptCode, 'NEW_STUDY', row.groupBySmq, row.preferredTerm, row.soc, row.productSelection),
                                    cnt_drill_down(row.dataSource, row.cumStudyCount, COUNTS.CUMM, FLAGS.STUDY_FLAG, map.keyId, row.execConfigId, row.id, row.productId, row.productName, row.ptCode, 'CUMM_STUDY', row.groupBySmq, row.preferredTerm, row.soc, row.productSelection))
                            } else {
                                return signal.utils.stacked(
                                    '<span>' + row.newStudyCount + '</span>',
                                    '<span>' + row.cumStudyCount + '</span>'
                                );
                            }
                        },
                        className: '',
                        "visible": signal.fieldManagement.visibleColumns('newStudyCount')
                    }]);
            } else if (map.name === "newPediatricCount") {
                aoColumns.push.apply(aoColumns, [
                    {
                        "mData": "newPediatricCount",
                        "className": '',
                        "mRender": function (data, type, row) {
                            if (map.isHyperLink) {
                                return signal.utils.stacked(
                                    cnt_drill_down(row.dataSource, row.newPediatricCount, COUNTS.NEW, FLAGS.PEDI_FLAG, map.keyId, row.execConfigId, row.id, row.productId, row.productName, row.ptCode, 'NEW_PEDI', row.groupBySmq, row.preferredTerm, row.soc, row.productSelection),
                                    cnt_drill_down(row.dataSource, row.cummPediatricCount, COUNTS.CUMM, FLAGS.PEDI_FLAG, map.keyId, row.execConfigId, row.id, row.productId, row.productName, row.ptCode, 'CUMM_PEDI', row.groupBySmq, row.preferredTerm, row.soc, row.productSelection))
                            } else {
                                return signal.utils.stacked(
                                    '<span>' + row.newPediatricCount + '</span>',
                                    '<span>' + row.cummPediatricCount + '</span>'
                                );
                            }
                        },
                        "visible": signal.fieldManagement.visibleColumns('newPediatricCount')
                    }]);
            } else if (map.name === "newInteractingCount") {
                aoColumns.push.apply(aoColumns, [
                    {
                        "mData": "newInteractingCount",
                        "className": '',
                        "mRender": function (data, type, row) {
                            if (map.isHyperLink) {
                                return signal.utils.stacked(
                                    cnt_drill_down(row.dataSource, row.newInteractingCount, COUNTS.NEW, FLAGS.INTERACTING_FLAG, map.keyId, row.execConfigId, row.id, row.productId, row.productName, row.ptCode, 'NEW_INTERACTING', row.groupBySmq, row.preferredTerm, row.soc, row.productSelection),
                                    cnt_drill_down(row.dataSource, row.cummInteractingCount, COUNTS.CUMM, FLAGS.INTERACTING_FLAG, map.keyId, row.execConfigId, row.id, row.productId, row.productName, row.ptCode, 'CUMM_INTERACTING', row.groupBySmq, row.preferredTerm, row.soc, row.productSelection))
                            } else {
                                return signal.utils.stacked(
                                    '<span>' + row.newInteractingCount + '</span>',
                                    '<span>' + row.cummInteractingCount + '</span>'
                                );
                            }
                        },
                        "visible": signal.fieldManagement.visibleColumns('newInteractingCount')
                    }]);
            } else if (map.name === "newGeriatricCount") {
                aoColumns.push.apply(aoColumns, [
                    {
                        "mData": "newGeriatricCount",
                        "mRender": function (data, type, row) {
                            if (map.isHyperLink) {
                                return signal.utils.stacked(
                                    cnt_drill_down(row.dataSource, row.newGeriatricCount, COUNTS.NEW, FLAGS.GERI_FLAG, map.keyId, row.execConfigId, row.id, row.productId, row.productName, row.ptCode, 'NEW_GERI', row.groupBySmq, row.preferredTerm, row.soc, row.productSelection),
                                    cnt_drill_down(row.dataSource, row.cumGeriatricCount, COUNTS.CUMM, FLAGS.GERI_FLAG, map.keyId, row.execConfigId, row.id, row.productId, row.productName, row.ptCode, 'CUMM_GERI', row.groupBySmq, row.preferredTerm, row.soc, row.productSelection))
                            } else {
                                return signal.utils.stacked(
                                    '<span>' + row.newGeriatricCount + '</span>',
                                    '<span>' + row.cumGeriatricCount + '</span>'
                                );
                            }
                        },
                        className: '',
                        "visible": signal.fieldManagement.visibleColumns('newGeriatricCount')
                    }]);
            } else if (map.name === "newNonSerious") {
                aoColumns.push.apply(aoColumns, [
                    {
                        "mData": "newNonSerious",
                        "mRender": function (data, type, row) {
                            if (map.isHyperLink) {
                                return signal.utils.stacked(
                                    cnt_drill_down(row.dataSource, row.newNonSerious, COUNTS.NEW, FLAGS.NON_SERIOUS_FLAG, map.keyId, row.execConfigId, row.id, row.productId, row.productName, row.ptCode, 'NEW_NON_SERIOUS', row.groupBySmq, row.preferredTerm, row.soc, row.productSelection),
                                    cnt_drill_down(row.dataSource, row.cumNonSerious, COUNTS.CUMM, FLAGS.NON_SERIOUS_FLAG, map.keyId, row.execConfigId, row.id, row.productId, row.productName, row.ptCode, 'CUMM_NON_SERIOUS', row.groupBySmq, row.preferredTerm, row.soc, row.productSelection))
                            } else {
                                return signal.utils.stacked(
                                    '<span>' + row.newNonSerious + '</span>',
                                    '<span>' + row.cumNonSerious + '</span>'
                                );
                            }
                        },
                        className: '',
                        "visible": signal.fieldManagement.visibleColumns('newNonSerious')
                    }]);
            }else if(map.name === "positiveRechallenge"){
                aoColumns.push.apply(aoColumns, [
                    {
                        "mData": "positiveRechallenge",
                        "className": '',
                        "mRender": function (data, type, row) {
                            return '<span>' + row.positiveRechallenge + '</span>'
                        },
                        "visible": signal.fieldManagement.visibleColumns('positiveRechallenge')
                    }]);
            }else if(map.name === "positiveDechallenge"){
                aoColumns.push.apply(aoColumns, [
                    {
                        "mData": "positiveDechallenge",
                        "className": '',
                        "mRender": function (data, type, row) {
                            return '<span>' + row.positiveDechallenge + '</span>'
                        },
                        "visible": signal.fieldManagement.visibleColumns('positiveDechallenge')
                    }]);
            }else if(map.name === "prrLCI"){
                aoColumns.push.apply(aoColumns, [
                    {
                        "mData": "prrLCI",
                        "mRender": function (data, type, row) {
                            if (row.prrLCI || row.prrUCI) {
                                if (row.prrLCI == -1.0 && row.prrUCI != -1.0) {
                                    row.prrLCI = '-'
                                } else {
                                    if (row.prrLCI != -1.0 && row.prrUCI == -1.0) {
                                        row.prrUCI = '-'
                                    } else {
                                        if (row.prrLCI == -1.0 && row.prrUCI == -1.0) {
                                            row.prrLCI = '-'
                                            row.prrUCI = '-'
                                        }
                                    }
                                }
                            }
                            return signal.utils.stacked(
                                '<span class="PRRLCI">' + row.prrLCI + '</span>',
                                '<span class="PRRUCI">' + row.prrUCI + '</span>'
                            )
                        },
                        className: '',
                        "visible": signal.fieldManagement.visibleColumns('prrLCI')
                    }])
            }else if(map.name === "rorLCI"){
                aoColumns.push.apply(aoColumns, [
                    {
                        "mData": "rorLCI",
                        "mRender": function (data, type, row) {
                            if (row.rorLCI || row.rorUCI) {
                                if (row.rorLCI == -1.0 && row.rorUCI != -1.0) {
                                    row.rorLCI = '-'
                                } else {
                                    if (row.rorLCI != -1.0 && row.rorUCI == -1.0) {
                                        row.rorUCI = '-'
                                    } else {
                                        if (row.rorLCI == -1.0 && row.rorUCI == -1.0) {
                                            row.rorLCI = '-'
                                            row.rorUCI = '-'
                                        }
                                    }
                                }
                            }
                            return signal.utils.stacked(
                                '<span class="RORLCI">' + row.rorLCI + '</span>',
                                '<span class="RORUCI">' + row.rorUCI + '</span>'
                            )
                        },
                        className: '',
                        "visible": signal.fieldManagement.visibleColumns('rorLCI')
                    }])
            }else if(map.name === "ebgm"){
                aoColumns.push.apply(aoColumns, [{
                    "mData": "ebgm",
                    className: '',
                    "mRender": function (data, type, row) {
                        if (row.ebgm == -1) {
                            return '<span>' + '-' + '</span>'
                        } else {
                            return '<span class="EBGM">' + row.ebgm + '</span>';
                        }
                    },
                    "visible": signal.fieldManagement.visibleColumns('ebgm')
                }])
            }else if(map.name === "related"){
                aoColumns.push.apply(aoColumns, [
                    {
                        "mData": "related",
                        "className": '',
                        "mRender": function (data, type, row) {
                            return '<span>' + row.related + '</span>'
                        },
                        "visible": signal.fieldManagement.visibleColumns('related')
                    }]);
            }else if(map.name === "pregenency"){
                aoColumns.push.apply(aoColumns, [
                    {
                        "mData": "pregenency",
                        "className": '',
                        "mRender": function (data, type, row) {
                            return '<span>' + row.pregnancy + '</span>'
                        },
                        "visible": signal.fieldManagement.visibleColumns('pregenency')
                    }]);
            }else if(map.name === "prrValue"){
                aoColumns.push.apply(aoColumns, [{
                    "mData": "prrValue",
                    "className": 'text-center',
                    "mRender": function (data, type, row) {
                        if (row.prrValue == -1) {
                            return '<span>' + '-' + '</span>'
                        }
                        return '<span class="PRR">' + row.prrValue + '</span>';
                    },
                    "visible": signal.fieldManagement.visibleColumns('prrValue')
                }])
            }else if(map.name === "rorValue"){
                aoColumns.push.apply(aoColumns, [{
                    "mData": "rorValue",
                    className: 'text-center',
                    "mRender": function (data, type, row) {
                        if (row.rorValue == -1) {
                            return '<span>' + '-' + '</span>'
                        }
                        return '<span class="ROR">' + row.rorValue + '</span>';
                    },
                    "visible": signal.fieldManagement.visibleColumns('rorValue')
                }])
            }else if(map.name === "eb05"){
                aoColumns.push.apply(aoColumns, [
                    {
                        "mData": "eb05",
                        "mRender": function (data, type, row) {
                            if (row.eb05 == -1) {
                                return signal.utils.stacked(
                                    '<span class="EB05">' + '-' + '</span>',
                                    '<span class="EB95">' + '-' + '</span>'
                                );
                            }
                            return signal.utils.stacked(
                                '<span class="EB05">' + row.eb05 + '</span>',
                                '<span class="EB95">' + row.eb95 + '</span>'
                            );
                        },
                        className: '',
                        "visible": signal.fieldManagement.visibleColumns('eb05')
                    }]);
            }else{
                var colName = map.name
                aoColumns.push.apply(aoColumns, [
                    {
                        "mData": colName,
                        "mRender": function (data, type, row) {
                            if(map.type === "countStacked"){
                                if (row[colName] &&  row[colName] !== -1) {
                                    var secondaryName = map.secondaryName
                                    if(map.isHyperLink){
                                        return signal.utils.stacked(
                                            cnt_drill_down(row.dataSource,row[colName], COUNTS.NEW, map.keyId,map.keyId, row.execConfigId, row.id, row.productId, row.productName, row.ptCode, map.keyId.replace('REPORT','NEW').replace('_FLG',''), row.groupBySmq, row.preferredTerm, row.soc, row.productSelection),
                                            cnt_drill_down(row.dataSource,row[secondaryName], COUNTS.CUMM,map.keyId, map.keyId, row.execConfigId, row.id, row.productId, row.productName, row.ptCode, map.keyId.replace('REPORT','NEW').replace('_FLG',''), row.groupBySmq, row.preferredTerm, row.soc, row.productSelection)
                                        );
                                    }else {
                                        return signal.utils.stacked(
                                            '<span>' + row[colName] + '</span>',
                                            '<span>' + row[secondaryName] + '</span>'
                                        );
                                    }
                                }else{
                                    return signal.utils.stacked(
                                        '<span>' + '-' + '</span>',
                                        '<span>' + '-' + '</span>'
                                    );
                                }
                            }else{
                                if (row[colName] &&  row[colName] !== -1) {
                                    var colElement = '<div class="col-container"><div class="col-height">';
                                    colElement += row[colName];
                                    colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(row[colName]) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                                    colElement += '</div></div>';
                                    return colElement
                                }else{
                                    return '-';
                                }
                            }
                        },
                        'className': '',
                        "visible": signal.fieldManagement.visibleColumns(colName)
                    }
                ])
            }

        })
        if(isPvaEnabled && !isDataMining) {
            if (isAgeEnabled) {
                $.each(subGroupsMap.AGE_GROUP, function (key, value) {
                    var ebgmValue = "ebgm" + value
                    var eb05Value = "eb05" + value
                    var eb95Value = "eb95" + value
                    if(subGroupColumnInfo.includes(ebgmValue)) {
                        aoColumns.push.apply(aoColumns, makeSubGroupColumns("ebgm", value, "ebgmAge"));
                    }
                    if(subGroupColumnInfo.includes(eb05Value)){
                        aoColumns.push.apply(aoColumns, makeSubGroupColumns("eb05", value, "eb05Age"));
                    }
                    if(subGroupColumnInfo.includes(eb95Value)){
                        aoColumns.push.apply(aoColumns, makeSubGroupColumns("eb95", value, "eb95Age"));
                    }
                });
            }
            if (isGenderEnabled) {
                $.each(subGroupsMap.GENDER, function (key, value) {
                    var ebgmValue = "ebgm" + value
                    var eb05Value = "eb05" + value
                    var eb95Value = "eb95" + value
                    if(subGroupColumnInfo.includes(ebgmValue)) {
                        aoColumns.push.apply(aoColumns, makeSubGroupColumns("ebgm", value, "ebgmGender"));
                    }
                    if(subGroupColumnInfo.includes(eb05Value)){
                        aoColumns.push.apply(aoColumns, makeSubGroupColumns("eb05", value, "eb05Gender"));
                    }
                    if(subGroupColumnInfo.includes(eb95Value)){
                        aoColumns.push.apply(aoColumns, makeSubGroupColumns("eb95", value, "eb95Gender"));
                    }
                });
            }
            if (allSubGroupMap && allSubGroupMap !== []) {
                $.each(allSubGroupMap, function (key, map) {
                    $.each(map, function (subGroup, value) {
                        $.each(value, function (index, column) {
                            var checkKeyEnabled = key+column
                            if(subGroupColumnInfo.includes(checkKeyEnabled)) {
                                aoColumns.push.apply(aoColumns, makeAllSubGroupColumns(key, column, subGroup));
                            }
                        });
                    });
                });
            }
            if (rorRelSubGrpEnabled && allSubGroupMap && allSubGroupMap !== []) {
                $.each(allSubGroupMap, function (key, map) {
                    if (key.toLowerCase().startsWith('ror')) {
                        $.each(map, function (subGroup, value) {
                            $.each(value, function (index, column) {
                                var checkKeyEnabled = key+"Rel"+column
                                if(subGroupColumnInfo.includes(checkKeyEnabled)) {
                                    aoColumns.push.apply(aoColumns, makeAllSubGroupColumns(key + "Rel", column, subGroup));
                                }
                            });
                        });
                    }
                });
            }
        }

        if (isFaersEnabled) {
            $.each(subGroupsMap.AGE_GROUP_FAERS, function (key, value) {
                var ebgmValue = "ebgm" + value
                var eb05Value = "eb05" + value
                var eb95Value = "eb95" + value
                if(subGroupColumnInfo.includes(ebgmValue)) {
                    aoColumns.push.apply(aoColumns, makeSubGroupColumns("ebgm", value, "ebgmAge"));
                }
                if(subGroupColumnInfo.includes(eb05Value)){
                    aoColumns.push.apply(aoColumns, makeSubGroupColumns("eb05", value, "eb05Age"));
                }
                if(subGroupColumnInfo.includes(eb95Value)){
                    aoColumns.push.apply(aoColumns, makeSubGroupColumns("eb95", value, "eb95Age"));
                }
            });

            $.each(subGroupsMap.GENDER_FAERS, function (key, value) {
                var ebgmValue = "ebgm" + value
                var eb05Value = "eb05" + value
                var eb95Value = "eb95" + value
                if(subGroupColumnInfo.includes(ebgmValue)) {
                    aoColumns.push.apply(aoColumns, makeSubGroupColumns("ebgm", value, "ebgmGender"));
                }
                if(subGroupColumnInfo.includes(eb05Value)){
                    aoColumns.push.apply(aoColumns, makeSubGroupColumns("eb05", value, "eb05Gender"));
                }
                if(subGroupColumnInfo.includes(eb95Value)){
                    aoColumns.push.apply(aoColumns, makeSubGroupColumns("eb95", value, "eb95Gender"));
                }
            });
        }
        return aoColumns;
    }

    var cnt_drill_down = function (dataSource, value, type, typeFlag, keyId, executedConfigId, alertId, productId, productName, ptCode, className, groupBySmq, preferredTerm, soc, productSelection, includeLockedVersion) {
        var typeFlagCopy = typeFlag;
        if (dataSource.toLowerCase().toString() == "pva" || dataSource.toLowerCase().toString() == "safety") {
            typeFlag = keyId;
        }
        if (value === -1 || value === '-') {
            return '<span>' + '-' + '</span>'
        }
        var singleCaseDetailsUrl = '/signal/singleCaseAlert/caseSeriesDetails?aggExecutionId=' + executedConfigId + '&aggAlertId=' + alertId + '&aggCountType=' + className + '&productId=' + productId + '&ptCode=' + ptCode + '&type=' + type + "&typeFlag=" + typeFlag + "&isArchived=" + isArchived + "&domainName=AggregateOnDemandAlert&isAggregateAdhoc=true"+"&version=1"+ "&numberOfCount=" + value;
        var p_term_scope = 0;
        var termScopeStr = preferredTerm.substring(preferredTerm.lastIndexOf("(") + 1, preferredTerm.lastIndexOf(")"));
        if (termScopeStr == 'Broad')
            p_term_scope = 1;
        else if (termScopeStr == 'Narrow')
            p_term_scope = 2;

        var customAnalysisUrl = customAnalysisThirdPartyUrl +
            parameter.queryParam + parameter.executedConfigId + "/" + executedConfigId + "/" +
            parameter.queryParam + parameter.baseId + "/" + productId + "/" +
            parameter.queryParam + parameter.meddraPtCode + "/" + ptCode + "/";
        if (type == COUNTS.NEW)
            customAnalysisUrl += parameter.queryParam + parameter.dateRangeTypeFlag + "/" + 1 + "/";
        if (groupBySmq)
            customAnalysisUrl += parameter.queryParam + parameter.termScope + "/" + p_term_scope + "/";
        if (typeFlag)
            customAnalysisUrl += parameter.queryParam + parameter.flag + typeFlag + "/" + 1;


        var seriesData = "id:" + alertId + ",typeFlag:" + typeFlagCopy + ",type:" + type + ",executedConfigId:" +
            executedConfigId + ",productId:" + productId + ",ptCode:" + ptCode+",keyId:"+keyId;
        if (value == 0 || value=="0") {
            return '<span class="blue-1">' + value + '</span>'
        }
        if (typeof pvrIntegrate != "undefined" && JSON.parse(pvrIntegrate)) {
            var hasReviewerAccessVar = false;
            if (typeof hasReviewerAccess !== "undefined" && hasReviewerAccess) {
                hasReviewerAccessVar = true;
            }
            var actionButton = '<div style="display: block" class="btn-group dropdown"> \
                    <a class="dropdown-toggle ' + className + '" data-toggle="dropdown" href="#">' + value + '</a> \
                            <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;">';
            if ($("#isCaseSeriesAccess").val() == "false")
                actionButton = actionButton + '<li role="presentation" class="caseSeriesNotAccess"><a >' + 'Case Series' + '</a></li>';
            else
                actionButton = actionButton + '<li role="presentation"><a href=' + singleCaseDetailsUrl + ' target="_blank" data-url=' + singleCaseDetailsUrl + ' class="">' + 'Case Series' + '</a></li>';
            if ($("#isFaers").val() == "false" && $("#isVaers").val() == "false" && $("#isVigibase").val() == "false" && $("#isJader").val() == "false")
                actionButton = actionButton + '<li role="presentation"><a target="_blank" href="' + template_list_url + '?' +
                    signal.utils.composeParams({
                        configId: executedConfigId,
                        type: type,
                        alertId: alertId,
                        typeFlag: keyId, //Added for report generation,
                        keyId: keyId,
                        isAggScreen: true,
                        hasReviewerAccess: hasReviewerAccessVar,
                        preferredTerm: preferredTerm,
                        productName: productName

                    }) +
                    ' ">' + $.i18n._('Report') + '</a> \</li>'
            if (isCustomAnalysisEnabed && $("#isJader").val() == "false") {
                actionButton = actionButton + '<li role="presentation"><a href=' + customAnalysisUrl + ' target="_blank" data-url="' + customAnalysisUrl + '"  >' + customAnalysisLabel + '</a></li>';
            } else if (spotfireEnabled && $("#isJader").val() == "false") {
                var smqType;
                var filterName;
                var url;
                if (productSelection && productSelection !== "-") {
                    var productSelectionMap = Object.values(JSON.parse(productSelection))
                    if (productSelectionMap[0].length > 0) {
                        filterName = SPOTFIRE.INGREDIENT_NAME;
                    } else if (productSelectionMap[1].length > 0) {
                        filterName = SPOTFIRE.PRODUCT_FAMILY
                    } else if (productSelectionMap[2].length > 0) {
                        filterName = SPOTFIRE.PRODUCT_NAME
                    } else if (productSelectionMap[3].length > 0) {
                        filterName = SPOTFIRE.TRADE_NAME
                    }
                } else {
                    filterName = SPOTFIRE.PRODUCT_GROUP
                }

                if (JSON.parse(groupBySmq)) {
                    if (soc.toUpperCase() == "SMQ") {
                        var actualPreferredTerm = preferredTerm
                        preferredTerm = preferredTerm.substring(0, preferredTerm.lastIndexOf("("))
                        smqType = actualPreferredTerm.substring(actualPreferredTerm.lastIndexOf("(") + 1, actualPreferredTerm.lastIndexOf(")"));
                    }
                }
                var hiddenFieldsString = '<input type="hidden" class="includeLockedVersion" value="'+includeLockedVersion+'"/><input type="hidden" class="seriesData" value="' + seriesData + '"/><input type="hidden" class="smqType" value="' + (typeof smqType === 'undefined' ? '' : encodeURIComponent(smqType)) + '"/><input type="hidden" class="preferredTerm" value="' + encodeURIComponent(preferredTerm) + '"/><input type="hidden" class="groupBySmq" value="' + encodeURIComponent(groupBySmq) + '"/><input type="hidden" class="soc" value="' + encodeURIComponent(soc) + '"/><input type="hidden" class="filterName" value="' + encodeURIComponent(filterName) + '"/><input type="hidden" class="productName" value="' + encodeURIComponent(productName) + '"/>'
                if (selectedDatasource.includes('pva')) {
                    if (type == COUNTS.NEW && analysisStatusJson.PR_DATE_RANGE.status == 2) {
                            url = analysisStatusJson.PR_DATE_RANGE.url
                            actionButton = actionButton + '<li role="presentation">' + hiddenFieldsString + '<input type="hidden" value="pva" class="dataSource"/><a href="#" data-url="' + url + '" class="dataAnalysis"  >' + dataAnalysisLabel + '</a></li>';
                            } else if (type == COUNTS.CUMM && analysisStatusJson.CUMULATIVE.status == 2) {
                            url = analysisStatusJson.CUMULATIVE.url
                            actionButton = actionButton + '<li role="presentation">' + hiddenFieldsString + '<input type="hidden" value="pva" class="dataSource"/><a href="#" data-url="' + url + '" class="dataAnalysis"  >' + dataAnalysisLabel + '</a></li>';
                        }
                    } else if (selectedDatasource.includes('faers')) {
                        if (type == COUNTS.NEW && analysisStatusJson.PR_DATE_RANGE_FAERS.status == 2) {
                            url = analysisStatusJson.PR_DATE_RANGE_FAERS.url
                            actionButton = actionButton + '<li role="presentation">' + hiddenFieldsString + '<input type="hidden" value="faers" class="dataSource"/><a href="#" data-url="' + url + '" class="dataAnalysis"  >' + dataAnalysisLabel + '</a></li>';
                            } else if (type == COUNTS.CUMM && analysisStatusJson.CUMULATIVE_FAERS.status == 2) {
                            url = analysisStatusJson.CUMULATIVE_FAERS.url
                            actionButton = actionButton + '<li role="presentation">' + hiddenFieldsString + '<input type="hidden" value="faers" class="dataSource"/><a href="#" data-url="' + url + '" class="dataAnalysis"  >' + dataAnalysisLabel + '</a></li>';
                        }
                    } else if (selectedDatasource.includes('vaers')) {
                        if (type == COUNTS.NEW && analysisStatusJson.PR_DATE_RANGE_VAERS.status == 2) {
                            url = analysisStatusJson.PR_DATE_RANGE_VAERS.url
                            actionButton = actionButton + '<li role="presentation">' + hiddenFieldsString + '<input type="hidden" value="vaers" class="dataSource"/><a href="#" data-url="' + url + '" class="dataAnalysis"  >' + dataAnalysisLabel + '</a></li>';
                        } else if (type == COUNTS.CUMM && analysisStatusJson.CUMULATIVE_VAERS.status == 2) {
                            url = analysisStatusJson.CUMULATIVE_VAERS.url
                            actionButton = actionButton + '<li role="presentation">' + hiddenFieldsString + '<input type="hidden" value="vaers" class="dataSource"/><a href="#" data-url="' + url + '" class="dataAnalysis"  >' + dataAnalysisLabel + '</a></li>';
                        }
                    }
            }
            actionButton = actionButton + '</ul> \
                        </div>';
            return actionButton;
        } else {
            return '<a href=' + singleCaseDetailsUrl + ' target="_blank" class="">' + value + '</a>'
        }
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

    $(document).on('click', '.alert-check-box', function () {
        if (!this.checked) {
            $('.alert-select-all').prop('checked', false);
        }
    });

    $('#exportTypes a[href]').on('click', function (e) {
        var clickedURL = e.currentTarget.href;
        var updatedExportUrl = clickedURL;
        var ids = [];
        $('.copy-select:checked').each(function () {
            if ($(this)[0]['checked']) {
                ids.push($(this).attr('data-id'))
            }
        });

        var checkedColumns = {};
        $('#tableColumns tr ').each(function () {
            var fieldName = $(this).children().children().attr('data-field');
            if (fieldName) {
                checkedColumns[fieldName] = $(this).children()[1].firstChild.checked
            }
        });

        var isFilterRequest = true;
        var filterValues = [];
        var prefix = "agg_adhoc_";
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
            updatedExportUrl = clickedURL + '&isFilterRequest=' + isFilterRequest + '&filters=' + encodeURI(JSON.stringify(filterValues)) + "&selectedCases=" + ids + "&viewId=" + viewId;
        } else {
            var filterList = {};
            var miningVariable=$("#miningVariable").val()
            $(".yadcf-filter-wrapper").each(function () {
                var filterVal = $(this).children().val();
                if (filterVal) {
                    if($(this).prev().attr('data-field') !== undefined){
                        filterList[$(this).prev().attr('data-field')] = filterVal
                    }
                }
            });
            if(filterList.productName === "-1"){
                delete filterList['productName'];
            }
            var filterListJson = JSON.stringify(filterList);
            updatedExportUrl = clickedURL + '&isFilterRequest=' + isFilterRequest + '&filters=' + encodeURI(JSON.stringify(filterValues)) + "&frequency=" + freqSelected + "&viewId=" + viewId + "&filterList=" + encodeURI(filterListJson) + "&isFaers=" + $('#isFaers').val() +"&miningVariable=" + miningVariable ;
            if ($('#advanced-filter').val()) {
                updatedExportUrl += "&advancedFilterId=" + $('#advanced-filter').val()
            }
        }
        window.location.href = updatedExportUrl;
        sessionStorage.setItem(prefix + "filters_value", JSON.stringify(filterValues));
        sessionStorage.setItem(prefix + "id", id);
        return false
    });

    var loadTable = function (result) {
        var groupBySmq = $('#groupBySmq').val();
        init(result, groupBySmq);
        $("#alertsDetailsTable_filter").hide();

        if (callingScreen == CALLING_SCREEN.REVIEW) {
            //saving new ViewInstance modal
            if (groupBySmq == "true" && $("#isFaers").val() == "true") {
                alertType = applicationNameSmqFaers
            } else if (groupBySmq == "true" && $("#isVaers").val() == "true") {
                alertType = applicationNameSmqVaers
            } else if (groupBySmq == "true" && $("#isVigibase").val() == "true") {
                alertType = applicationNameSmqVigibase
            } else if(groupBySmq == "true" && $("#isJader").val() == "true"){
                alertType = applicationNameSmqJader
            }else if (groupBySmq == "true") {
                alertType = applicationNameSmq
            } else if($("#isFaers").val() == "true"){
                alertType = applicationNameFaers
            } else if($("#isVaers").val() == "true"){
                alertType = applicationNameVaers
            } else if($("#isVigibase").val() == "true"){
                alertType = applicationNameVigibase
            } else if($("#isJader").val() == "true"){
                alertType = applicationNameJader
            } else{
                alertType = applicationName
            }
            if(miningVariable){
                alertType = alertType + "-" + miningVariable
            }
            signal.alertReview.openSaveViewModal(filterIndex, alertType, viewId)
        }
    }

    if (callingScreen == CALLING_SCREEN.TRIGGERED_ALERTS) {
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
                getDateRange(sessionStorage.getItem("frequency"))
            } else {
                $("#frequencyNames").val($("#frequencyNames option:first").val());
                $("#frequencyNames").trigger('change')
            }
        }
    } else {
        loadTable("")
    }

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

    set_disable_click($('.generateCurrentAnalysis'), $('.generateCurrentAnalysis').attr('data-status'), 'Current Period Analysis');
    set_disable_click($('.generateCumulativeAnalysis'), $('.generateCumulativeAnalysis').attr('data-status'), 'Cumulative Period Analysis');
    set_disable_click($('.generateCurrentAnalysisFaers'), $('.generateCurrentAnalysisFaers').attr('data-status'), 'Current Period Analysis (FAERS)');
    set_disable_click($('.generateCumulativeAnalysisFaers'), $('.generateCumulativeAnalysisFaers').attr('data-status'), 'Cumulative Period Analysis (FAERS)');
    set_disable_click($('.generateCurrentAnalysisVaers'), $('.generateCurrentAnalysisVaers').attr('data-status'), 'Current Period Analysis (VAERS)');
    set_disable_click($('.generateCumulativeAnalysisVaers'), $('.generateCumulativeAnalysisVaers').attr('data-status'), 'Cumulative Period Analysis (VAERS)');

    $(document).on('click', '.generateCurrentAnalysis', function(){
        generate_analysis($('.generateCurrentAnalysis'), 'PR_DATE_RANGE_SAFETYDB', 'Current Period Analysis', 'Current');
    });
    $(document).on('click', '.generateCumulativeAnalysis', function(){
        generate_analysis($('.generateCumulativeAnalysis'), 'CUMULATIVE_SAFETYDB', 'Cumulative Period Analysis', 'Cumulative');
    });
    //-------------------------Faers----------------------------------------------
    $(document).on('click', '.generateCurrentAnalysisFaers', function(){
        generate_analysis($('.generateCurrentAnalysisFaers'), 'PR_DATE_RANGE_FAERS', 'Current Period Analysis (FAERS)', 'CurrentFaers');
    });
    $(document).on('click', '.generateCumulativeAnalysisFaers', function(){
        generate_analysis($('.generateCumulativeAnalysisFaers'), 'CUMULATIVE_FAERS', 'Cumulative Period Analysis (FAERS)', 'CumulativeFaers');
    });
    //-------------------------Vaers----------------------------------------------
    $(document).on('click', '.generateCurrentAnalysisVaers', function(){
        generate_analysis($('.generateCurrentAnalysisVaers'), 'PR_DATE_RANGE_VAERS', 'Current Period Analysis (VAERS)', 'CurrentVaers');
    });
    $(document).on('click', '.generateCumulativeAnalysisVaers', function(){
        generate_analysis($('.generateCumulativeAnalysisVaers'), 'CUMULATIVE_VAERS', 'Cumulative Period Analysis (VAERS)', 'CumulativeVaers');
    });
    $(document).on('click', '.dataAnalysis', function () {
        var parent_row = $(this).closest("tr").find('td:eq(0)');
        var executedConfigId = parent_row.find("#execConfigId").val();
        var productName = parent_row.find("#productName").val();
        var preferredTerm = parent_row.find("#preferredTerm").val();
        var isArchived = parent_row.find("#isArchived").val();
        var value = $('#symptomsComanifestation').val();
        var columnName= $('#symptomsComanifestationColumnName').val();
        generateCaseSeries(selectedDatasource,$(this).parent(), productName, preferredTerm, executedConfigId, isArchived,$(this).attr("data-url"),value,columnName)
    })
});

function jsUcfirst(string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
}

var fetchSubGroupsMap = function() {
    var dataSource = $("#isFaers").val() == "true" ? "faers" : "pva";
    $.ajax({
        url: "/signal/aggregateCaseAlert/fetchSubGroupsMapIntegratedReview",
        data: {dataSource: dataSource},
        async: false,
        dataType: 'json'
    })
        .done(function (data) {
            subGroupsMap = data.subGroupsMap;
            isAgeEnabled = data.isAgeEnabled;
            isGenderEnabled = data.isGenderEnabled;
            allSubGroupMap = data.allSubGroupMap;
            subGroupColumnInfo = data.subGroupColumnInfo;
            rorRelSubGrpEnabled = data.rorRelSubGrpEnabled;
        });
};

function makeSubGroupColumns(groupName , category , subGroup ) {
    var addColumns = [
        {
            "mData": groupName + category,
            "className": 'text-center',
            "mRender": function (data, type, row) {
                var stringMap = row[subGroup];
                if (stringMap == '-')
                    map = [];
                else
                    var map = clobToMap(stringMap);
                var value = "";
                if (row.isFaersOnly) {
                    value = "-";
                } else if (map == null || map[category] == undefined)
                    value = "-";
                else {
                    if (map[category] === 0) {
                        value = "0.0"
                    } else {
                        value = map[category];
                    }
                }
                return value
            },
            "visible":  signal.fieldManagement.visibleColumns(groupName + category)
        }];
    return addColumns
}
function makeAllSubGroupColumns(groupName , category , subGroup) {
    var addColumns = [
        {
            "mData": groupName + category,
            "className": 'text-center',
            "mRender": function (data, type, row) {
                var map
                var stringMap = row[groupName+"SubGroup"];
                if (stringMap === '-' || stringMap === undefined) {
                    map = [];
                }else {
                    stringMap = JSON.parse(stringMap)
                    map = stringMap[subGroup];
                }
                var value = "";
                if (map == null || map[category] === undefined)
                    value = "-";
                else
                    if (map[category] === 0) {
                        value = "0.0"
                    } else {
                        value = map[category];
                    }
                return value
            },
            "visible":  signal.fieldManagement.visibleColumns(groupName + category)
        }];
    return addColumns
}

function clobToMap(clob) {
    clob = clob.replace('"', '');
    clob = clob.replace('"', '');
    var list = clob.split(",") ;
    var map = [] ;
    $.each(list , function(ind , value){
        map[value.split(":")[0].trim()] = value.split(":").length > 1 ? value.split(":")[1] : "0";
    });
    return map
}

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