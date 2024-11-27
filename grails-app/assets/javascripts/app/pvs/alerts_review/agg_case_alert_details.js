//= require vendorUi/highcharts/highcharts-10.3.3/highcharts
//= require vendorUi/highcharts/highcharts-10.3.3/highcharts-3d
//= require vendorUi/highcharts/highcharts-10.3.3/highcharts-more
//= require vendorUi/highcharts/themes/grid-rx
//= require app/pvs/common/rx_common.js
//= require app/pvs/common/rx_alert_utils.js
//= require app/pvs/common/rx_list_utils.js
//= require app/pvs/activity/activities.js
//= require app/pvs/actions/actions.js
//= require app/pvs/users/users.js
//= require app/pvs/common/rx_handlebar_ext.js
//= require app/pvs/caseDrillDown/caseDrillDown.js
//= require app/pvs/caseDrillDown/evdasCaseDrillDown.js
//= require app/pvs/alertComments/alertComments.js
//= require app/pvs/alerts_review/alert_review
//= require app/pvs/alerts_review/archivedAlerts.js
//= require app/pvs/common_tag.js
//= require app/pvs/alerts_review/dss_details.js

var applicationName = "Aggregate Case Alert";
var applicationNameFaers = "Aggregate Case Alert - FAERS";
var applicationNameVaers = "Aggregate Case Alert - VAERS";
var applicationNameVigibase = "Aggregate Case Alert - VIGIBASE";
var applicationNameSmq = "Aggregate Case Alert - SMQ";
var applicationNameSmqFaers = "Aggregate Case Alert - SMQ FAERS";
var applicationNameSmqVaers = "Aggregate Case Alert - SMQ VAERS";
var applicationNameSmqVigibase = "Aggregate Case Alert - SMQ VIGIBASE";
var applicationNameJader = "Aggregate Case Alert - JADER";
var applicationNameSmqJader = "Aggregate Case Alert - SMQ JADER";


var applicationLabel = "Quantitative Alert";
var table;
var dir = '';
var index = -1;
var executedIdList = [];
var isDynamicFilterApplied = false;
var productNames = [];
var productIdList = [];
var currentProduct;
var selectExConfigId;
var subGroupsMap = [];
var allSubGroupMap = [];
var isAgeEnabled;
var isGenderEnabled;
var rorRelSubGrpEnabled;
var tags = [];
var isPva = selectedDatasource.includes('pva');
var isEvdas = selectedDatasource.includes('eudra');
var isFaers = selectedDatasource.includes('faers');
var isVaers = selectedDatasource.includes('vaers');
var isVigibase = selectedDatasource.includes('vigibase');
var isJader = selectedDatasource.includes('jader');
var paginationAdvancedFilter = true;
var aggregateQueryJson = "";
var prev_page = [];
var allVisibleIds = [];
var acaEntriesCount = sessionStorage.getItem('acaPageEntries') != null ? sessionStorage.getItem('acaPageEntries') : 50;
var fixedFilterColumn = []


$(function () {
    setTimeout(function () {
        $(".alert-dismissible").hide();
    }, 6000);
    if (window.sessionStorage.getItem('lastAdvancedFilter')) {
        window.sessionStorage.removeItem('lastAdvancedFilter')
    }
    if ($(".data-analysis-button").find("li[class!=hidden]").length === 0) {
        $(".data-analysis-button").addClass("hidden");
    }
    var buttonClassVar = "";
    if (typeof buttonClass !== "undefined" && buttonClass) {
        buttonClassVar = buttonClass;
    }

    $('a[href="#details"]').on("shown.bs.tab", function (e) {
        $('#alertsDetailsTable').DataTable().columns.adjust();
        addGridShortcuts('#alertsDetailsTable');
        removeGridShortcuts('#activitiesTable');
        removeGridShortcuts('#archivedAlertsTable');
    });
    $('a[href="#archivedAlerts"]').on("shown.bs.tab", function (e) {
        $('#archivedAlertsTable').DataTable().columns.adjust();
        addGridShortcuts('#archivedAlertsTable');
        removeGridShortcuts('#activitiesTable');
        removeGridShortcuts('#alertsDetailsTable');
    });

    $(".mdi").on('click', function () {
        $(".tooltip").hide();
    });

    $('#commentTemplateSelect').on('change', function () {
        var commentModal = $('#commentModal');
        const templateId = $('#commentTemplateSelect').find(":selected").val();
        const acaId = $('#caseId').val();
        if (templateId != "none") {
            $.ajax({
                url: fetchCommentUrl,
                data: {
                    "templateId": templateId,
                    "acaId": acaId
                },
            })
                .done(function (response) {
                    commentModal.find('.add-comments').prop("disabled", false);
                    const index = commentModal.find('#commentbox').prop("selectionStart");
                    var comment = commentModal.find('#commentbox').val();
                    comment = comment.slice(0, index) + response.data + comment.slice(index);
                    if ($('#current').is(':checked')){
                      commentModal.find('#commentbox').val(comment);
                    }
                })
                .fail(function (err) {
                    console.log("Unexpected Error Occurred.");
                })
        } else {
            commentModal.find('.add-comments').prop("disabled", false);
        }
    });


    var genDSSLink = function (rowId, exConfigId, isArchived, pecScore, isSafety) {
        pecScore = pecScore == null || pecScore === "" ? 0 : pecScore;
        var smq = $('#groupBySmq').val() == "true"
        if (((isSafety && smq) || !isSafety)) {
            pecScore = '-';
            return '<span  class="articleTitle">' + pecScore + '</span>';
        } else if (isSafety && (pecScore == 0 || pecScore == "-")) {
            return '<span  class="articleTitle">' + pecScore + '</span>';
        }
        return '<a target="_blank" class="articleTitle" href="' + dssNetworkUrl + '?configId=' + exConfigId + '&rowId=' + rowId + '&archived=' + isArchived + '">' + pecScore + '</a>';
    };

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
                    '/signal/activity/listActivities?' + 'appType=' + applicationName, applicationName);
            }
        }
    });

    $(document).on('click', '.test-1234', function (evt) {
        $('#dss-modal').modal({})
    });
    fetchSubGroupsMap();
    fetchTags();
    fetchViewInstanceList();

    $('a[href="#archivedAlerts"]').on('click', function () {
        if (callingScreen == CALLING_SCREEN.REVIEW) {
            archived_table = signal.archived_utils.init_archived_table("#archivedAlertsTable", archivedAlertUrl, applicationName, aggregateDetailsUrl);
        }
    });


    if (callingScreen == CALLING_SCREEN.REVIEW || callingScreen == CALLING_SCREEN.DASHBOARD) {
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
    var listOfIndex = [];
    var filterIndex = filterIndexList;

    $(window).on('beforeunload', function () {
        if (sessionStorage.getItem('isViewCall') == 'true') {
            var backUrl = sessionStorage.getItem('back_url');
            var oldProductNameList = sessionStorage.getItem(PRODUCT_LIST);
            var oldProductMapList = sessionStorage.getItem(productList);
            sessionStorage.clear();
            sessionStorage.setItem('back_url', backUrl);
            sessionStorage.setItem(PRODUCT_LIST, oldProductNameList);
            sessionStorage.setItem(productList, oldProductMapList);
        } else {
            var viewInfo = signal.alertReview.generateViewInfo(filterIndex);
            sessionStorage.setItem('aca_filterMap_' + executedConfigId, JSON.stringify(viewInfo.filterMap));
            sessionStorage.setItem('aca_sortedColumn_' + executedConfigId, JSON.stringify(viewInfo.sortedColumn));
            sessionStorage.setItem('aca_notVisibleColumn_' + executedConfigId, viewInfo.notVisibleColumn);
            sessionStorage.setItem('aca_viewName_' + executedConfigId, $('.viewSelect :selected').text().replace("(default)", "").trim())
        }
    });

    function showPopup(data) {
        var evdasDataLogModal = $("#downloadReportModal");
        $(evdasDataLogModal).find("#productName").val(data);
        evdasDataLogModal.modal("show");
    }

    var detail_table;
    var activities_table;

    var checkedIdList = [];
    var checkedRowList = [];
    var advanceFilterAlertType = $("#isJader").val() === "true" ? applicationNameJader :applicationName
    signal.actions_utils.set_action_list_modal($('#action-modal'));
    signal.actions_utils.set_action_create_modal($('#action-create-modal'));
    signal.list_utils.flag_handler("aggregateCaseAlert", "toggleFlag");
    signal.commonTag.openCommonAlertTagModal("PVS", "Quantitative", tags);
    signal.alertReview.openAlertCommentModal("Aggregate Case Alert", applicationName, applicationLabel, checkedIdList, checkedRowList);
    signal.alertReview.showAttachmentModal();
    signal.alertReview.populateAdvancedFilterSelect(advanceFilterAlertType);
    signal.alertReview.setSortOrder();
    signal.caseDrillDown.bind_drill_down_table(caseDetailUrl);
    signal.evdasCaseDrillDown.bind_evdas_drill_down_table(evdasCaseDetailUrl);

    var filterValue = signal.alertReview.createFilterMap(ALERT_PREFIX_FILTER.AGG + executedConfigId, ALERT_PREFIX_VIEW.AGG + executedConfigId);


    $(document).on('click', '.product-event-history-icon', function (event) {
        event.preventDefault();
        var configId = $(this).data("configid");
        var selectedRowIndex = $(this).closest('tr').index();
        var rowObject = table.rows(selectedRowIndex).data()[0];
        var productName = rowObject.productName;
        var eventName = rowObject.preferredTerm;
        var productEventHistoryModal = $('#productEventHistoryModal');
        productEventHistoryModal.modal('show');
        productEventHistoryModal.find("#productName").html(productName);
        productEventHistoryModal.find("#eventName").html(eventName);
        productEventHistoryModal.find("#configId").html(configId);

        signal.productEventHistoryTable.init_current_alert_history_table(productEventHistoryUrl, productName, eventName, configId, executedConfigId);
        signal.productEventHistoryTable.init_other_alerts_history_table(productEventHistoryUrl, productName, eventName, configId);
    });

    $(document).on('click', '.show-trend-icon', function (event) {
        var alertId = $(this).attr("data-value");
        var isFaers = $('#isFaers').val();
        var showTrendTableUrl = showTrendUrl + "?id=" + alertId + "&isFaers=" + isFaers;
        var win = window.open(showTrendTableUrl, '_blank');
        win.focus();
    });

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
        var selectedRowIndex = $(this).closest('tr').index();
        var rowObject = table.rows(selectedRowIndex).data()[0];
        var productName = rowObject.productName;
        var eventName = rowObject.preferredTerm;
        var parent_row = $(event.target).closest('tr');
        var execConfigId = parent_row.find("#execConfigId").val();


    });


    $(document).on('click', '.show-chart-icon', function (event) {

        var alertId = $(this).attr("data-value");
        var showEvdasChart = $("#show-evdas-chart-modal");

        $.ajax({
            url: 'showCharts?alertId=' + alertId + '&isArchived=' + isArchived,
            async: true,
            beforeSend: function () {
                showEvdasChart.modal("show");
                $("#chart-loader-1").show();
            },
        })
            .always(function () {
                $("#chart-loader-1").hide();
            })
            .done(function (result) {
                var series = []
                if ((isVaers || isVigibase || isJader) && !isPva && !isEvdas && !isFaers) {
                    series = [{
                        name: "New Count",
                        data: result.newCount
                    }, {
                        name: "New Serious",
                        data: result.seriousCount
                    }, {
                        name: "New Fatal",
                        data: result.fatalCount
                    }]
                } else {
                    series = [
                        {
                            name: "New Study",
                            data: result.studyCount
                        }, {
                            name: "New Count",
                            data: result.newCount
                        },
                        {
                            name: "New Serious",
                            data: result.seriousCount
                        }, {
                            name: "New Fatal",
                            data: result.fatalCount
                        }]
                }

                evdas_chart("#evdas-count-by-status", result.xAxisTitle,
                    series,
                    "Counts", 'Counts'
                );
                var prrValue = '';
                var rorValue = '';
                var eb05Value = '';
                var eb95Value = '';
                var ebgmValue = '';
                var seriesValue = []
                if (result.prrValue) {
                    prrValue = result.prrValue
                    seriesValue.push({name: "PRR", data: result.prrValue})
                }
                if (result.rorValue) {
                    rorValue = result.rorValue
                    if (result.isRor)
                        seriesValue.push({name: "ROR", data: result.rorValue})
                    else
                        seriesValue.push({name: "iROR", data: result.rorValue})
                }
                if (result.eb05Value) {
                    eb05Value = result.eb05Value
                    seriesValue.push({name: "EB05", data: result.eb05Value})
                }
                if (result.eb95Value) {
                    eb95Value = result.eb95Value
                    seriesValue.push({name: "EB95", data: result.eb95Value})
                }
                if (result.ebgmValue) {
                    ebgmValue = result.ebgmValue
                    seriesValue.push({name: "EBGM", data: result.ebgmValue})
                }

                evdas_chart("#evdas-scores-by-status", result.xAxisTitle, seriesValue, "Scores", 'Scores');

                var countTable = [];
                var obj;
                for (var index = 0; index < result.xAxisTitle.length; index++) {
                    if ((isVaers || isVigibase || isJader) && !isPva && !isEvdas && !isFaers) {
                        obj = {
                            "xAxisTitle": result.xAxisTitle[index],
                            "newCount": result.newCount[index],
                            "seriousCount": result.seriousCount[index],
                            "fatalCount": result.fatalCount[index]
                        };
                    } else {
                        obj = {
                            "xAxisTitle": result.xAxisTitle[index],
                            "studyCount": result.studyCount[index],
                            "newCount": result.newCount[index],
                            "seriousCount": result.seriousCount[index],
                            "fatalCount": result.fatalCount[index]
                        };
                    }

                    countTable.push(obj);
                }
                var trend_data_content = signal.utils.render('counts_table_61', {
                    trendData: countTable,
                    isVaersEnabled: isVaers,
                    isPvaEnabled: isPva,
                    isEvdasEnabled: isEvdas,
                    isVigibaseEnabled: isVigibase,
                    isJaderEnabled: isJader,
                    isFaersEnabled: isFaers
                });
                $("#trend-div").html(trend_data_content);

                var scoresTable = [];
                for (var index = 0; index < result.xAxisTitle.length; index++) {
                    var obj = {
                        "xAxisTitle": result.xAxisTitle[index],
                        "prrValue": prrValue[index],
                        "rorValue": rorValue[index],
                        "eb05Value": eb05Value[index],
                        "eb95Value": eb95Value[index],
                        "ebgmValue": ebgmValue[index]
                    };
                    scoresTable.push(obj);
                }
                var score_data_content = signal.utils.render('scoresTable', {
                    trendData: scoresTable,
                    isRor: result.isRor
                });
                $("#scores-div").html(score_data_content);
                if (!ebgmValue) {
                    $(".ebgm-value").hide();
                }
                if (!prrValue) {
                    $(".prr-value").hide();
                }
                if (!eb05Value) {
                    $(".eb05-value").hide();
                }
                if (!eb95Value) {
                    $(".eb95-value").hide();
                }
                if (!rorValue) {
                    $(".ror-value").hide();
                }
            })
    });

    //apply the sorting to column based on saved view
    var sortingMap = signal.alertReview.createSortingMap('aca_sortedColumn_' + executedConfigId, 'aca_viewName_' + executedConfigId);
    if (sortingMap != 'undefined' && sortingMap.length > 0) {
        index = sortingMap[0][0];
        dir = sortingMap[0][1]
    }
    $('#alert-list-filter-apply-bt').on('click', function (evt) {
        evt.preventDefault();
        var filterParams = $('#alert-list-filter').serialize();
        var theDataTable = $('#alertsDetailsTable').DataTable();
        $('#alert-list-filter').find('#filterUsed').val(true);
        var newUrl = listConfigUrl + "&" + filterParams + "&filterApplied=" + true;
        theDataTable.ajax.url(newUrl).load();
    });

    $("#detail-tabs a[href='#details']").on("click", function (e) {
        $('#alertsDetailsTable').DataTable().columns.adjust();
    });

    var init = function (result, groupBySmq) {
        signal.fieldManagement.populateColumnList(gridColumnsViewUrl, gridColumnsViewUpdateUrl);
        detail_table = init_details_table(result, groupBySmq);
        var commentModal = $('#commentModal');
        signal.alertReview.initiateCommentHistoryTable(commentModal);
        $('i#copySelection').on('click', function () {
            showAllSelectedCaseNumbers();
        });

        $(document).on('click', 'input#select-all', function () {
            $(".copy-select").prop('checked', this.checked);
            $(".select-all-check input#select-all").prop('checked', this.checked);
            if (typeof isCaseDetailView !== "undefined" && isCaseDetailView == "true") {
                checkboxSelector = 'table#alertsDetailsTable .copy-select';
            } else {
                checkboxSelector = '.copy-select';
            }
            $.each($(checkboxSelector), function () {
                if (selectedCases.indexOf($(this).attr("data-id")) == -1 && $(this).is(':checked')) {
                    selectedCases.push($(this).attr("data-id"));
                    var selectedRowIndex = $(this).closest('tr').index();
                    if (isAbstractViewOrCaseView(selectedRowIndex)) {
                        selectedRowIndex = selectedRowIndex / 2
                    }
                    selectedCasesInfo.push(populateDispositionDataFromGrid(selectedRowIndex));
                    selectedCasesInfoSpotfire.push(populateDispositionData($(this).closest('tr').index()));
                } else if (selectedCases.indexOf($(this).attr("data-id")) != -1 && !$(this).is(':checked')) {
                    selectedCasesInfoSpotfire.splice($.inArray($(this).attr("data-id"), selectedCases), 1);
                    selectedCasesInfo.splice($.inArray($(this).attr("data-id"), selectedCases), 1);
                    selectedCases.splice($.inArray($(this).attr("data-id"), selectedCases), 1);
                }
            })
        });

        actionButton("#alertsDetailsTable");
    };

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

    var ptBadge = function (ime, dme, ei, sm) {
        var badgeSpan = "";
        if (dme == "true") {
            badgeSpan += '<span class="grid-menu-tooltip-dynamicWidth" data-title="' + emergingIssuesAbbrDme["label"] + '" ><span class="badge badge-purple">' + emergingIssuesAbbrDme["abbr"].toUpperCase() + '</span></span><br/>'
        }
        if (ei == "true") {
            badgeSpan += '<span class="grid-menu-tooltip-dynamicWidth" data-title="' + emergingIssuesAbbrEI["label"] + '" ><span class="badge badge-success">' + emergingIssuesAbbrEI["abbr"].toUpperCase() + '</span></span>'
        }
        if (ime == "true") {
            badgeSpan += '<span class="grid-menu-tooltip-dynamicWidth" data-title="' + emergingIssuesAbbrIme["label"] + '" ><span class="badge badge-info">' + emergingIssuesAbbrIme["abbr"].toUpperCase() + '</span></span>'
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
                    fixed_column: groupBySmq == 'true' ? ((key == colIndex + 1) ? true : false) : ((key == colIndex + 1 || key == colIndex + 2) ? true : false)
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
        var columnNos = fixedColumnNos(callingScreen);

        signal.fieldManagement.init($('#alertsDetailsTable').DataTable(), '#alertsDetailsTable', columnNos, false);

        $("#toggle-column-filters, #ic-toggle-column-filters").on('click', function () {
            init_product_list($('#alertsDetailsTable').DataTable())
            var ele = $('.yadcf-filter-wrapper');
            if (ele.is(':visible')) {
                ele.hide();
            } else {
                ele.show();
                $('.filter-center').css("display", "flex");
                $('.yadcf-filter').first().focus();
            }
            $(".custom-select-product").select2();
            $(".dt-scroll-body").css('position','static');
            $("tr").find(`[data-idx="36"]`).find(".yadcf-filter-wrapper").hide();
        });
    };

    var colIndex = isPriorityEnabled ? 4 : 3;
    if(callingScreen === CALLING_SCREEN.DASHBOARD){
        colIndex += 1;
    }
    var _changeIntervalProduct = null; // Added for PVS-54078
    $(document).on('change', '#yadcf-filter-alertsDetailsTable-' + colIndex, function () {
        var checkMasterConfig = sessionStorage.getItem(isMasterConfig);
        if ($(this).val() && typeof checkMasterConfig != "undefined" && checkMasterConfig == "true") {
            sessionStorage.clear();
            signal.utils.postUrl('/signal/aggregateCaseAlert/details' + '?' + 'callingScreen=' + CALLING_SCREEN.REVIEW + '&configId=' + $(this).val() + '&viewId=' + viewId, {
                isArchived: $("#isArchived").val(),
                masterConfigProduct: $(this).find(":selected").text()
            });
            sessionStorage.setItem(shownProduct, $(this).find(":selected").text());
        }
        var productNum = $(this).val();
        var productSelect = $(this);
        clearInterval(_changeIntervalProduct)
        _changeIntervalProduct = setInterval(function () {
            $("#alertsDetailsTable").DataTable().column(colIndex).search(productNum); //Exact value, column, reg
            if (productNum == "") {
                fixedFilterColumn.push({"column": colIndex, "value": ""});
                productSelect.removeClass("inuse");
            } else {
                fixedFilterColumn.push({"column": colIndex, "value": productNum});
                productSelect.addClass("inuse");
            }
            clearInterval(_changeIntervalProduct)
        }, 1500);
    });

    var _changeIntervalSoc = null;
    var _changeIntervalPt = null;

    $(document).on('keyup', 'input#yadcf-filter-alertsDetailsTable-' + (colIndex + 1), function (e) {

        var casenum = $(this).val();
        var caseInput = $(this);
        clearInterval(_changeIntervalSoc)
        _changeIntervalSoc = setInterval(function () {
            $("#alertsDetailsTable").DataTable().columnt(colIndex + 1).search(casenum); //Exact value, column, reg
            if (casenum == "") {
                fixedFilterColumn.push({"column": colIndex + 1, "value": ""});
                caseInput.removeClass("inuse");
            } else {
                fixedFilterColumn.push({"column": colIndex + 1, "value": casenum});
                caseInput.addClass("inuse");
            }
            clearInterval(_changeIntervalSoc)
        }, 1500);

    });

    if (groupBySmq != 'true') {
        $(document).on('keyup', 'input#yadcf-filter-alertsDetailsTable-' + (colIndex + 2), function (e) {

            var casenum = $(this).val();
            var caseInput = $(this);
            clearInterval(_changeIntervalPt);
            _changeIntervalPt = setInterval(function () {
                $("#alertsDetailsTable").DataTable().column(colIndex + 2).search(casenum); //Exact value, column, reg
                if (casenum == "") {
                    fixedFilterColumn.push({"column": colIndex + 2, "value": ""});
                    caseInput.removeClass("inuse");
                } else {
                    fixedFilterColumn.push({"column": colIndex + 2, "value": casenum});
                    caseInput.addClass("inuse");
                }
                clearInterval(_changeIntervalPt);
            }, 1500);

        });
    }

    var init_product_list = function (table) {
        var colIndex = productColIndex();
        var filterValue = JSON.parse(sessionStorage.getItem("aca_filterMap_" + executedConfigId));
        var checkMasterConfig = sessionStorage.getItem(isMasterConfig);
        var select = document.getElementById("yadcf-filter-alertsDetailsTable-" + colIndex);
        select.options.length = 0;
        if (typeof checkMasterConfig != "undefined" && checkMasterConfig == "false") {
            for (var i = 0; i < productNames.length; i++) {
                var isSelected = false;
                if (i == 0) {
                    select.options[select.options.length] = new Option(ALL, '', false, false);
                } else {
                    if (filterValue != null && filterValue[colIndex] != undefined && filterValue[colIndex] != "" && filterValue[colIndex] != null && productNames[i] == filterValue[colIndex]) {
                        isSelected = true;
                    }
                    select.options[select.options.length] = new Option(productNames[i], productNames[i], isSelected, isSelected);
                }
            }
        } else {
            var select2Element = document.getElementById("yadcf-filter-alertsDetailsTable-" + colIndex);
            var isSelected = false;
            select2Element.options.length = 0;
            $(table).dataTableSettings[0].aoColumns[colIndex].bSearchable = false;
            $(table).dataTableSettings[0].aoColumns[colIndex].sortable = false;
            $(table).dataTableSettings[0].aoColumns[colIndex].orderable = false;
            for (var i = 0; i < productIdList.length; i++) {
                if (selectExConfigId != null && selectExConfigId != undefined && selectExConfigId != "" && productIdList[i][1] != null && productIdList[i][1] === selectExConfigId) {
                    isSelected = true;
                } else {
                    isSelected = false;
                }
                var option = new Option(productIdList[i][0], productIdList[i][1], isSelected, isSelected);
                option.setAttribute("title", productIdList[i][0]);
                select2Element.options[select2Element.options.length] = option;
            }
            select2Element.setAttribute("title", currentProduct);
        }
    }

    var productColIndex = function () {
        var colIndex = isPriorityEnabled ? 4 : 3;
        if (callingScreen == CALLING_SCREEN.DASHBOARD) {
            colIndex = isPriorityEnabled ? 5 : 4
        }
        return colIndex
    }

    var init_details_table = function (result, groupBySmq) {
        var isFilterRequest = true;
        var filterValues = [];
        var prefix = "agg_";
        if (window.sessionStorage) {
            if (signal.alertReview.isAlertPersistedInSessionStorage(prefix)) {
                filterValues = JSON.parse(sessionStorage.getItem(prefix + "filters_value"));
            } else {
                signal.alertReview.removeFiltersFromSessionStorage(prefix);
                isFilterRequest = false;
            }
        }

        if (callingScreen == CALLING_SCREEN.TRIGGERED_ALERTS) {
            listOfIndex = [10];
        } else if (groupBySmq == "true") {
            listOfIndex = [9, 14];
        } else {
            listOfIndex = [9, 15]
        }
        var prrVisibility = JSON.parse($("#showPrr").val());
        var rorVisibility = JSON.parse($("#showRor").val());
        var ebgmVisibility = JSON.parse($("#showEbgm").val());
        var dssVisibility = JSON.parse($("#showDss").val());
        var columns = create_agg_table_columns(callingScreen, prrVisibility, rorVisibility, ebgmVisibility, dssVisibility, result, groupBySmq);

        $(document).on('click', '#alertsDetailsTable_paginate', function () {
            isPagination = true;
            if ($('.select-all-check input#select-all').is(":checked") && !prev_page.includes($('li.active').text().split("\n")[2].trim())) {
                prev_page.push($('li.active').text().split("\n")[2].trim());
            }
            if ((!$('.select-all-check input#select-all').is(":checked") && prev_page.includes($('li.active').text().split("\n")[2].trim()))) {
                var position = prev_page.indexOf($('li.active').text().split("\n")[2].trim());
                prev_page.splice(position, 1);
            }
        });
        var tPosition;
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
                signal.alertReview.applyBusinessRules(row, data);
                //Bind AssignedTo Select Box
                // workaround, select2 initialization is called before elements displayed
                setTimeout(function() {
                    signal.user_group_utils.bind_assignTo_to_grid_row($(row), searchUserGroupListUrl, {
                        name: data.assignedTo.fullName,
                        id: data.assignedTo.id
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
            drawCallback: function (settings) {
                var filterMap = JSON.parse(sessionStorage.getItem("aca_filterMap_" + executedConfigId)) != null ? JSON.parse(sessionStorage.getItem("aca_filterMap_" + executedConfigId)) : new Map();
                var colIndex = productColIndex();
                if (typeof settings.json != "undefined") {
                    sessionStorage.setItem(isMasterConfig, settings.json.isMasterConfig);
                    productNames = [ALL]
                    for (var i = 0; i < settings.json.productNameList?.length;
                         i++
                    ) {
                        productNames.push(settings.json.productNameList[i]);
                    }
                    sessionStorage.setItem(PRODUCT_LIST, JSON.stringify(productNames))
                    productIdList = []
                    for (var i = 0; i < settings.json.productIdList?.length;
                         i++
                    ) {
                        productIdList.push(settings.json.productIdList[i]);
                    }
                    sessionStorage.setItem(productList, JSON.stringify(productIdList))
                    currentProduct = settings.json.currentProduct;
                    selectExConfigId = settings.json.configId
                }
                for (var i = colIndex; i <= colIndex + 2; i++) {
                    $.each(filterMap, function (key, value) {
                        if (key != "undefined" && key != undefined && i == key && value != undefined && typeof value !== "undefined" && value !== null) {
                            if(i == colIndex){
                                var select = document.getElementById("yadcf-filter-alertsDetailsTable-" + i);
                                if (typeof select !== "undefined" && select !== undefined  && select !== null && select != "null") {
                                    init_product_list(table);
                                }
                            }else {
                                var input = $("#yadcf-filter-alertsDetailsTable-" + key)
                                if (typeof input !== "undefined" && input !== undefined &&  input !== null && input != "null") {
                                    if (value != "") {
                                        input.val(value).addClass('inuse');
                                    } else {
                                        input.val(value).removeClass('inuse');
                                    }
                                }
                            }
                        }
                    });
                }
                var current_page = $('li.active').text().split("\n")[2].trim();
                if (typeof prev_page != 'undefined' && $.inArray(current_page, prev_page) == -1) {
                    $(".select-all-check input#select-all").prop('checked', false);
                } else {
                    $(".select-all-check input#select-all").prop('checked', true);
                }
                if (typeof hasReviewerAccess !== "undefined" && !hasReviewerAccess) {
                    $(".changeDisposition").removeAttr("data-target");
                    $(".changePriority").removeAttr("data-target");
                }
                if (typeof hasSignalCreationAccessAccess !== "undefined" && !hasSignalCreationAccessAccess) {
                    $(".changeDisposition[data-validated-confirmed=true]").removeAttr("data-target");
                }
                var rowsDataAD = $('#alertsDetailsTable').DataTable().rows().data();
                if(settings.json != undefined) {
                    pageDictionaryForAlertDetails($('#alertsDetailsTable_wrapper')[0], settings.aLengthMenu[0][0], settings.json.recordsFiltered);

                }else {
                    pageDictionaryForAlertDetails($('#alertsDetailsTable_wrapper')[0], 50, rowsDataAD.length);
                }
                initPSGrid($('#alertsDetailsTable_wrapper'));
                if (typeof settings.json != "undefined" && !isDynamicFilterApplied) {
                    isDynamicFilterApplied = true;
                    signal.alertReview.bindGridDynamicFilters(settings.json.filters, prefix, settings.json.configId);
                }
                var main_disp_filter;
                if (typeof settings.json != "undefined") {
                    main_disp_filter = sessionStorage.getItem('agg_filters_value')
                    if ((advancedFilterChanged && !main_disposition_filter) || aggregateQueryJson != "") {
                        if (sessionStorage.getItem("agg_filters_value")) {
                            //this function is called to handle the case when we change the disposition of a pec to a new disposition that is not available in quick filter dispo list
                            retainQuickDispositionFilter(JSON.parse(sessionStorage.getItem(prefix + "filters_value")), "agg_", settings.json.configId);
                        }
                        addAdvancedFilterDisp(settings.json.advancedFilterDispName, "agg_", settings.json.configId);
                        advancedFilterChanged = false;
                        aggregateQueryJson = ""
                    } else if (advancedFilterChanged && main_disposition_filter) {
                        //this function is called when we remove advanced filter from dropdown
                        retainQuickDispositionFilter(JSON.parse(main_disp_filter), "agg_", settings.json.configId);
                        advancedFilterChanged = false;
                        main_disposition_filter = false;
                    }
                    allVisibleIds = settings.json.visibleIdList
                }
                tagEllipsis($('#alertsDetailsTable'));
                colEllipsis();
                webUiPopInitForCategories();
                webUiPopInit();
                enterKeyAlertDetail();
                closePopupOnScroll();
                closeInfoPopover();
                showInfoPopover();
                signal.alertReview.enableMenuTooltips();
                signal.alertReview.enableMenuTooltipsDynamicWidth();
                signal.alertReview.sortIconHandler();
                populateSelectedCases();
                var checkedId;
                $('input[type=checkbox]').each(function () {
                    checkedId = $(this).attr('data-id')
                    if (alertIdSet.has(checkedId)) {
                        $(this).prop('checked', true);
                    }
                    if ((!isPagination) && (checkedId !== undefined) && !allVisibleIds.includes(parseInt(checkedId)) && selectedCases.includes(checkedId)) {
                        // Remove PECs which were selected before but now not visible on screen
                        selectedCasesInfo.splice($.inArray(checkedId, selectedCases), 1);
                        selectedCasesInfoSpotfire.splice($.inArray(checkedId, selectedCases), 1);
                        selectedCases.splice($.inArray(checkedId, selectedCases), 1);
                    } else if ((checkedId !== undefined) && (!isPagination) && allVisibleIds.includes(parseInt(checkedId)) && selectedCases.indexOf($(this).attr("data-id")) === -1 && $(this).is(':checked')) {
                        // Disable checkbox for PECs not available on screen
                        $(this).prop('checked', false);
                    }
                });
                $('span[data-field="productName"]').addClass('word-break').removeClass('break-all');
                $(".custom-select-product").select2();
                $('.dt-pagination').on('change', function () {
                    var countVal = $('.dt-pagination').val()
                    sessionStorage.setItem("acaPageEntries", countVal);
                    acaEntriesCount = sessionStorage.getItem("acaPageEntries");
                })
                $(".dt-scroll-body").append("<div style='height:3px'></div>");
                $('#alertsDetailsTable_wrapper .dt-scroll-body').scrollLeft(tPosition);
            },
            "ajax": {
                "url": listConfigUrl + '&isFilterRequest=' + isFilterRequest + '&advancedFilterChanged=' + advancedFilterChanged + '&filters=' + encodeURIComponent(JSON.stringify(filterValues)) + "&frequency=" + freqSelected,
                "type": "POST",
                "dataSrc": "aaData",
                "error": ajaxAuthroizationError,
                "data": function (d) {
                    tPosition = $('#alertsDetailsTable_wrapper .dt-scroll-body').scrollLeft();
                    $("#alertsDetailsTable_paginate").on('click', function () {
                        paginationAdvancedFilter = true;
                    });
                    var colIndex = productColIndex();
                    var filterMap = JSON.parse(sessionStorage.getItem("aca_filterMap_" + executedConfigId)) != null ? JSON.parse(sessionStorage.getItem("aca_filterMap_" + executedConfigId)) : new Map();
                    for (var i = colIndex; i <= colIndex + 2; i++) {
                        var searchValue = d.columns[i].search.value;
                        if (filterMap) {
                            filterMap[i] = searchValue
                        }
                    }
                    sessionStorage.setItem('aca_filterMap_' + executedConfigId, JSON.stringify(filterMap));
                    [d, dir, index] = updateOrder(index, d, columnSeq, dir)
                    if ($('#advanced-filter').val()) {
                        d.advancedFilterId = $('#advanced-filter').val()
                        paginationAdvancedFilter = false;
                        isPagination = false;
                    }
                    if ($('#queryJSON').val()) {
                        aggregateQueryJson = $('#queryJSON').val();
                        d.queryJSON = $('#queryJSON').val();
                        sessionStorage.setItem('lastAdvancedFilter', $('#queryJSON').val());
                    } else if (sessionStorage.getItem('lastAdvancedFilter')) {
                        if (paginationAdvancedFilter) {
                            d.queryJSON = sessionStorage.getItem('lastAdvancedFilter');
                        } else {
                            sessionStorage.removeItem('lastAdvancedFilter');
                        }
                    }
                    d.configId = configId;
                    d.isViewInstance = d.draw === 1 ? 1 : 0;
                }, // added for PVS-53683 to cancel previous call
                beforeSend: function () {
                    if (typeof table != "undefined") {
                        var xhr = table.settings()[0].jqXHR;
                        if (xhr && xhr.readyState != 4) {
                            xhr.abort();
                        }
                    } //pvs-44818
                    setTimeout(function () {
                        signal.fieldManagement.bindFieldConfigurationSortEvent();
                    }, 500)
                }
            },
            initComplete: function (settings, json) {
                if (filterValue.length === 0 && !isDynamicFilterApplied) {
                    isDynamicFilterApplied = true;
                    signal.alertReview.bindGridDynamicFilters(json.filters, prefix, json.configId);
                }
                if (sessionStorage.getItem(prefix + "filters_store")) {
                    if (typeof settings.json !== 'undefined' && !settings.json.advancedFilterDispName) {
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
                signal.alertReview.enableMenuTooltips();
                signal.alertReview.disableTooltips();
                addGridShortcuts('#alertsDetailsTable');
                var sessionProduct = JSON.parse(sessionStorage.getItem(PRODUCT_LIST));
                var sessionProductList = JSON.parse(sessionStorage.getItem(productList));
                if (typeof json != "undefined") {
                    sessionStorage.setItem(isMasterConfig, json.isMasterConfig);
                    productNames = [ALL]
                    for (var i = 0; i < json.productNameList?.length;
                         i++
                    ) {
                        productNames.push(json.productNameList[i]);
                    }
                    sessionStorage.setItem(PRODUCT_LIST, JSON.stringify(productNames))
                    productIdList = []
                    for (var i = 0; i < json.productIdList?.length;
                         i++
                    ) {
                        productIdList.push(json.productIdList[i]);
                    }
                    sessionStorage.setItem(productList, JSON.stringify(productIdList))
                    currentProduct = json.currentProduct;
                    selectExConfigId = json.configId
                } else {
                    if (sessionProduct) {
                        for (var i = 0; i < sessionProduct.length; i++) {
                            productNames.push(sessionProduct[i])
                        }
                    }
                    if (sessionProductList) {
                        for (var i = 0; i < sessionProductList.length; i++) {
                            productIdList.push(sessionProductList[i])
                        }
                    }
                    currentProduct = sessionStorage.getItem(shownProduct);
                }
                if (settings.json !== undefined && settings.json.orderColumnMap) {
                    var orderColumnMap = settings.json.orderColumnMap;
                    index = $('#alertsDetailsTable').find("th[data-field='" + orderColumnMap.name + "']").attr('data-column-index');
                    dir = orderColumnMap.dir;
                    isViewInstance = 0;
                    signal.alertReview.sortIconHandler(index);
                }
                showInfoPopover();
                init_filter(table, prrVisibility, rorVisibility, ebgmVisibility, dssVisibility, groupBySmq);
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
                "sLengthMenu": "Show _MENU_",
            },

            "aLengthMenu": [[50, 100, 200, 500], [50, 100, 200, 500]],
            "pagination": true,
            "iTotalDisplayRecords": 50,
            "iDisplayLength": 50,
            "deferLoading": filterValue.length > 0 ? 0 : null,
            "aoColumns": columns,
            "responsive": true,
            scrollX: true,
            "bDeferRender": true,
            "bAutoWidth": false,
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                "defaultContent": "",
                orderSequence: ['desc', 'asc']
            }, {
                "targets": 0,
                "className": 'first-column'
            }]
        });
        signal.user_group_utils.bind_assignTo_selection(assignToGroupUrl, table, hasReviewerAccess);
        init_product_list(table);
        return table;
    };
    var jaderColumns = JSON.parse(jaderColumnList);


    var fixedColumnNos = function (callingScreen) {
        if (callingScreen == CALLING_SCREEN.TRIGGERED_ALERTS || callingScreen == CALLING_SCREEN.DASHBOARD) {
            return (isPriorityEnabled ? 8 : 7);
        } else if (groupBySmq == "true") {
            return (isPriorityEnabled ? 6 : 5);
        } else {
            return (isPriorityEnabled ? 7 : 6);
        }
    };

    var create_agg_table_columns = function (callingScreen, prrVisibility, rorVisibility, ebgmVisibility, dssVisibility, result, groupBySmq) {
        var labelConfigKeyId = $("#labelConfigKeyId").val()
        var hyperlinkConfiguration = $("#hyperlinkConfiguration").val()
        var labelConfigCopyJson = $("#labelConfigCopyJson").val()
        labelConfigCopyJson = JSON.parse(labelConfigCopyJson)
        labelConfigKeyId = JSON.parse(labelConfigKeyId)
        hyperlinkConfiguration = JSON.parse(hyperlinkConfiguration)

        var aoColumns = [
            {
                "mData": "selected",
                "mRender": function (data, type, row) {
                    if ($.inArray(row.execConfigId, executedIdList) == -1) {
                        executedIdList.push(row.execConfigId)
                    }
                    var checkboxhtml = '<input class="execConfigId" id="execConfigId" type="hidden" value="' + row.execConfigId + '" />' +
                        '<input class="alertConfigId" id="alertConfigId" type="hidden" value="' + row.alertConfigId + '" />' +
                        "<input class='productName' id='productName' type='hidden' value='" + encodeToHTML(row.productName) + "' />" +
                        '<input class="preferredTerm" id="preferredTerm" type="hidden" value="' + row.preferredTerm + '" />' +
                        '<input class="isArchived" id="isArchived" type="hidden" value="' + row.isArchived + '" />';
                    if (typeof row.id !== "undefined" && alertIdSet.has(JSON.stringify(row.id))) {
                        return checkboxhtml + '<input type="checkbox" class="alert-check-box editor-active copy-select" data-id=' + row.id + ' checked/>';
                    } else if (typeof row.id !== "undefined" && selectedCases.includes(row.id.toString())) {
                        return checkboxhtml + '<input type="checkbox" class="alert-check-box editor-active copy-select" data-id=' + row.id + ' checked/>';
                    } else {
                        return checkboxhtml + '<input type="checkbox" class="alert-check-box editor-active copy-select" data-id=' + row.id + ' />';

                    }


                },
                "className": '',
                "sortable": false
            }, {
                "mData": "dropdown",
                "mRender": function (data, type, row) {
                    var actionButton = '<div style="display: block;" class="btn-group dropdown dataTableHideCellContent" align="center"> \
                        <a class="dropdown-toggle" data-toggle="dropdown" tabindex="0" role="button"> \
                        <span style="cursor: pointer;font-size: 125%;" class="glyphicon glyphicon-option-vertical"></span><span class="sr-only">Toggle Dropdown</span> \
                        </a> ';

                    if (row.comment) {
                        actionButton += '<i class="mdi mdi-chat blue-2 font-13 pos-ab comment" title="' + $.i18n._('commentAvailable') + '"></i>';
                    }

                    if (row.isAttachment == true) {
                        actionButton += ' <i class="mdi mdi-attachment blue-1 font-13 pos-ab attach" title="' + $.i18n._('attachmentAvailable') + '"></i>';
                    }

                    actionButton += '<ul class="dropdown-menu menu-cosy" role="menu"><li role="presentation"><a tabindex="0" class="review-row-icon  product-event-history-icon" data-datasrc="' + row.dataSource + '" data-configid="' + row.configId + '"><span class=" fa fa-list m-r-10"></span>History</a></li>';
                    actionButton += '<li role="presentation"><a tabindex="0" class="review-row-icon comment-icon" data-info="row"><span class="fa fa-comment m-r-10"  data-info="row" ></span>Comments</a></li>';
                    actionButton += '<li role="presentation"><a tabindex="0" class="review-row-icon show-chart-icon" data-value="' + row.id + '"><span class="fa fa-line-chart m-r-10"></span>Charts</a></li>';
                    actionButton += '<li role="presentation"><a tabindex="0" class="review-row-icon show-attachment-icon" data-field="attachment" data-id="' + row.id + '" data-controller="aggregateCaseAlert"><span class="fa fa-file m-r-10"></span>Attachment</></li>';
                    if ((row.isUndoEnabled == "true" && row.isDefaultState === "true") || row.isDefaultState === "false") {
                        if (!row.isValidationStateAchieved && (row.isUndoEnabled === "true" && (isAdmin || row.dispPerformedBy === currUserName))) {
                            actionButton += '<li role="presentation" class="popover-parent">' +
                                '<a tabindex="0" data-id ="' + row.id + '" title="Undo Disposition Change" data-html="true" class="review-row-icon undo-alert-disposition" ' +
                                'data-toggle="popover" data-content="<textarea class=\'form-control editedJustification\'>' +
                                '</textarea>' +
                                '<ol class=\'confirm-options\' id=\'revertConfirmOption\'>' +
                                '<li><a tabindex=\'0\' href=\'javascript:void(0);\' title=\'Save\'><i class=\'mdi mdi-checkbox-marked green-1\' data-id =\'' + row.id + '\' id=\'confirmUndoJustification\'></i></a></li>' +
                                '<li><a tabindex=\'0\' href=\'javascript:void(0);\' title=\'Close\'><i class=\'mdi mdi-close-box red-1\' id=\'cancelUndoJustification\'></i> </a></li>' +
                                '</ol>" ' +
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
                "className": "dropDown"
            }];
        if (isPriorityEnabled) {
            aoColumns.push.apply(aoColumns, [{
                "mData": "priority",
                "aTargets": ["priority"],
                "mRender": function (data, type, row) {
                    var isFaers = $("#isFaers").val();
                    var isPriortyChangeAllowed = (isProductSecurity == 'true' && allowedProductsAsSafetyLead.indexOf(row.productName)) || isProductSecurity == 'false' || isFaers == 'true';
                    return signal.utils.render('priority', {
                        priorityValue: row.priority.value,
                        priorityClass: row.priority.iconClass,
                        isPriorityChangeAllowed: isPriortyChangeAllowed
                    });
                },

                'className': 'col-min-30 text-center priorityParent',
                "visible": true
            }]);
        }

        aoColumns.push.apply(aoColumns, [{
            "mData": "actions",
            "mRender": function (data, type, row) {
                row["buttonClass"] = buttonClassVar;
                return signal.actions_utils.build_action_render(row)
            },
            'className': 'col-min-30 text-center action-col dropDown',
            "visible": true
        }]);
        if (callingScreen == CALLING_SCREEN.DASHBOARD) {
            aoColumns.push.apply(aoColumns, [{
                "mData": "name",
                "className": 'col-min-150 col-max-300 cell-break',
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
                "mData": "productName",
                'className': 'col-min-100 col-max-100',
                "mRender": function (data, type, row) {
                    var result = "<input type='hidden' value='" + row.productId + "' class='row-product-id'/>" + "<input type='hidden' value='" + row.ptCode + "' class='row-pt-code'/>"
                    "<input type='hidden' value='" + row.level + "' class='row-level-id'/>";
                    if (row.isSpecialPE) {
                        result += "<span style='color:red'  data-field ='productName' data-id='" + encodeToHTML(row.productName) + "'>" + encodeToHTML(row.productName) + "</span>"
                    } else {
                        result += "<span data-field ='productName' data-id='" + encodeToHTML(row.productName) + "'>" + encodeToHTML(row.productName) + "</span>"
                    }
                    if (row.productName.indexOf(' ')) {
                        $('span[data-field="productName"]').addClass('cell-break').removeClass('break-all');
                    } else {
                        $('span[data-field="productName"]').addClass('break-all').removeClass('cell-break');
                    }
                    return result;
                },
                "visible": true,
                "sortable": false,
            }]);
        if (groupBySmq != "true") {
            aoColumns.push.apply(aoColumns, [{
                "mData": "soc",
                "mRender": function (data, type, row) {
                    if (row.soc != 'undefined') {
                        if (row.fullSoc != null) {
                            return '<span class="cell-break word-wrap-break-word grid-menu-tooltip" data-title="' + row.fullSoc + '" >' + row.soc + '</span>'
                        } else {
                            if (row.dataSource.toString().toLowerCase() == "jader") {
                                return '<span class="grid-menu-tooltip cell-break word-wrap-break-word" data-title="' + row.soc + '" >' + row.soc+ '</span>'
                            }
                            else{
                                return '<span class="cell-break word-wrap-break-word" >' + row.soc + '</span>'
                            }
                        }
                    } else {
                        return '<span>' + '-' + '</span>'
                    }
                },
                "className": 'col-min-100 col-max-100',
                "visible": true
            }]);
        }
        aoColumns.push.apply(aoColumns, [
            {
                "mData": "pt",
                "mRender": function (data, type, row) {
                    var urlWithParams = eventDetailsUrl + '?alertId=' + row.id + '&type=' + applicationName + '&isArchived=' + isArchived +
                        '&isAlertScreen=true';
                    var result = row.preferredTerm;
                    if (row.isSpecialPE) {
                        return "<span style='color:red' data-field ='eventName' data-id='" + row.preferredTerm.replace(/'/g, '&#39;') + "'>" + result + "</span>"
                    } else {
                        return "<span data-field ='eventName' data-id='" + row.preferredTerm.replace(/'/g, '&#39;') + "'>" + result + "</span>"
                    }
                },
                "visible": true
            }]);
        if (isJaderAvailable === "true") {
            $.each(jaderColumns, function (key, map) {
                if (map.name === "alertTags") {
                    aoColumns.push.apply(aoColumns, [
                        {
                            "mData": "alertTags",
                            "mRender": function (data, type, row) {
                                var tagsElement = signal.alerts_utils.get_tags_element(row.alertTags);
                                return tagsElement
                            },
                            "className": 'col-max-300 pos-rel',
                            "sortable": false,
                            "visible": signal.fieldManagement.visibleColumns('alertTags')
                        }]);
                } else if (map.name === "listed") {
                    aoColumns.push.apply(aoColumns, [
                        {
                            "mData": "listed",
                            "className": '',
                            "mRender": function (data, type, row) {
                                return '<span>' + row.listed + '</span>'
                            },
                            "visible": signal.fieldManagement.visibleColumns('listed')
                        }]);
                } else if (map.name === "newCountJader") {
                    aoColumns.push.apply(aoColumns, [
                        {
                            "mData": "newCountJader",
                            "mRender": function (data, type, row) {
                                return signal.utils.stacked(
                                    cnt_drill_down_jader(row.newCountJader, COUNTS.NEW, FLAGS.CUMM_FLAG,map.keyId, row.execConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, BUSINESS_RULE_FORMAT_CLASS.NEW_COUNT_AGG, row.groupBySmq, row.preferredTerm, row.soc, row.productSelection),
                                    cnt_drill_down_jader(row.cumCountJader, COUNTS.CUMM, FLAGS.CUMM_FLAG,map.keyId, row.execConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, BUSINESS_RULE_FORMAT_CLASS.CUMM_COUNT_AGG, row.groupBySmq, row.preferredTerm, row.soc, row.productSelection))
                            },
                            className: '',
                            "visible": signal.fieldManagement.visibleColumns('newCountJader')
                        }]);
                } else if (map.name === "newFatalCountJader") {
                    aoColumns.push.apply(aoColumns, [
                        {
                            "mData": "newFatalCountJader",
                            "mRender": function (data, type, row) {
                                return signal.utils.stacked(
                                    cnt_drill_down_jader(row.newFatalCountJader, COUNTS.NEW, FLAGS.CUMM_FLAG,map.keyId, row.execConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, BUSINESS_RULE_FORMAT_CLASS.NEW_FATAL_AGG, row.groupBySmq, row.preferredTerm, row.soc, row.productSelection),
                                    cnt_drill_down_jader(row.cumFatalCountJader, COUNTS.CUMM, FLAGS.CUMM_FLAG, map.keyId, row.execConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, BUSINESS_RULE_FORMAT_CLASS.CUMM_FATAL_AGG, row.groupBySmq, row.preferredTerm, row.soc, row.productSelection))
                            },
                            className: '',
                            "visible": signal.fieldManagement.visibleColumns('newFatalCountJader')
                        }]);
                } else if (map.name === "ebgmJader") {
                    aoColumns.push.apply(aoColumns, [
                        {
                            "mData": "ebgmJader",
                            className: 'text-center',
                            "mRender": function (data, type, row) {
                                if (row.ebgmJader == -1) {
                                    return '<span>' + '-' + '</span>'
                                } else {
                                    return '<span class="EBGM">' + row.ebgmJader + '</span>';
                                }
                            },
                            "visible": signal.fieldManagement.visibleColumns('ebgmJader')
                        }]);
                } else if (map.name === "signalsAndTopics") {
                    aoColumns.push.apply(aoColumns, [
                        {
                            "mData": 'signalsAndTopics',
                            "mRender": function (data, type, row) {
                                var signalAndTopics = '';
                                $.each(row.signalsAndTopics, function (i, obj) {
                                    var url = signalDetailUrl + '?id=' + obj['signalId'];
                                    if ($('#hasSignalManagementAccess').val() == 'true') {
                                        signalAndTopics = signalAndTopics + '<span class="click box-inline word-wrap-break-word" style="padding: 0px 0px 0px 0px !important"><a class="cell-break " title="' + obj.disposition.displayName + '" onclick="validateAccess(event,' + obj['signalId'] + ')" href="' + url + '">' + escapeHTML(obj['name']) + '</a></span>&nbsp;'
                                    } else {
                                        signalAndTopics = signalAndTopics + '<span class="click box-inline word-wrap-break-word col-max-150 signalSummaryAuth"><a class="cell-break" title="' + obj.disposition.displayName + '" href="javascript:void(0)">' + escapeHTML(obj['name']) + '</a></span>&nbsp;'
                                    }
                                    signalAndTopics = signalAndTopics + ","
                                });
                                if (signalAndTopics.length > 1) {
                                    return '<div class="cell-break word-wrap-break-word" style="padding: 0px 0px 0px 0px !important">' + signalAndTopics.substring(0, signalAndTopics.length - 1) + '</div>';
                                } else {
                                    return '-';
                                }
                            },
                            'className': 'col-min-150 col-max-200 signalInformation',
                            "sortable": false,
                            "visible": signal.fieldManagement.visibleColumns('signalsAndTopics')
                        }]);
                } else if (map.name === "disposition") {
                    aoColumns.push.apply(aoColumns, [
                        {
                            "mData": "disposition",
                            "mRender": function (data, type, row) {
                                var colElement = '<div class="col-container"><div class="col-height">';
                                colElement += encodeToHTML(row.disposition);
                                colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(row.disposition) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                                colElement += '</div></div>';
                                return colElement;
                            },
                            'className': 'col-max-250 currentDisposition',
                            "visible": signal.fieldManagement.visibleColumns('disposition')

                        }]);
                } else if (map.name === "currentDisposition") {
                    aoColumns.push.apply(aoColumns, [
                        {
                            "mData": "currentDisposition",
                            "mRender": function (data, type, row) {
                                return signal.utils.render('disposition_dss3', {
                                    allowedDisposition: dispositionIncomingOutgoingMap[row.disposition],
                                    proposedDisposition: row.proposedDisposition,
                                    currentDisposition: row.disposition,
                                    forceJustification: forceJustification,
                                    isReviewed: row.isReviewed,
                                    isValidationStateAchieved: row.isValidationStateAchieved,
                                    id: row.currentDispositionId
                                });
                            },
                            "visible": signal.fieldManagement.visibleColumns('currentDisposition'),
                            "class": 'col-max-300 dispositionAction'

                        }]);
                } else if (map.name === "assignedTo") {
                    aoColumns.push.apply(aoColumns, [
                        {
                            "mData": "assignedTo",
                            "className": 'col-min-100 col-max-150 assignedTo',
                            "mRender": function (data, type, row) {
                                return signal.list_utils.assigned_to_comp(row.id, row.assignedTo)
                            },
                            "sortable": false,
                            "visible": signal.fieldManagement.visibleColumns('assignedTo')

                        }]);
                } else if (map.name === "dueDate") {
                    aoColumns.push.apply(aoColumns, [
                        {
                            "mData": "dueDate",
                            "className": 'col-min-50 dueIn text-center',
                            "mRender": function (data, type, row) {
                                return signal.list_utils.due_in_comp(row.dueIn)
                            },
                            "visible": signal.fieldManagement.visibleColumns('dueDate')

                        }]);
                } else if (map.name === "newGeriatricCountJader") {
                    aoColumns.push.apply(aoColumns, [
                        {
                            "mData": "newGeriatricCountJader",
                            "mRender": function (data, type, row) {
                                return signal.utils.stacked(
                                    cnt_drill_down_jader(row.newGeriatricCountJader, COUNTS.NEW, FLAGS.CUMM_FLAG, map.keyId, row.execConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, BUSINESS_RULE_FORMAT_CLASS.NEW_GERI_AGG, row.groupBySmq, row.preferredTerm, row.soc, row.productSelection),
                                    cnt_drill_down_jader(row.cumGeriatricCountJader, COUNTS.CUMM, FLAGS.CUMM_FLAG, map.keyId, row.execConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, BUSINESS_RULE_FORMAT_CLASS.CUMM_GERI_AGG, row.groupBySmq, row.preferredTerm, row.soc, row.productSelection))
                            },
                            className: '',
                            "visible": signal.fieldManagement.visibleColumns('newGeriatricCountJader')
                        }]);
                } else if (map.name === "newPediatricCountJader") {
                    aoColumns.push.apply(aoColumns, [
                        {
                            "mData": "newPediatricCountJader",
                            "mRender": function (data, type, row) {
                                return signal.utils.stacked(
                                    cnt_drill_down_jader(row.newPediatricCountJader, COUNTS.NEW, FLAGS.CUMM_FLAG, map.keyId, row.execConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, BUSINESS_RULE_FORMAT_CLASS.NEW_PEDIA_AGG, row.groupBySmq, row.preferredTerm, row.soc, row.productSelection),
                                    cnt_drill_down_jader(row.cumPediatricCountJader, COUNTS.CUMM, FLAGS.CUMM_FLAG, map.keyId, row.execConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, BUSINESS_RULE_FORMAT_CLASS.CUMM_PEDIA_AGG, row.groupBySmq, row.preferredTerm, row.soc, row.productSelection))
                            },

                            className: '',
                            "visible": signal.fieldManagement.visibleColumns('newPediatricCountJader')
                        }]);
                } else if (map.name === "prrValueJader") {
                    aoColumns.push.apply(aoColumns, [
                        {
                            "mData": "prrValueJader",
                            "className": 'text-center',
                            "mRender": function (data, type, row) {
                                if (row.prrValueJader == -1) {
                                    return '<span>' + '-' + '</span>'
                                }
                                return '<span class="PRR">' + row.prrValueJader + '</span>';
                            },
                            "visible": signal.fieldManagement.visibleColumns('prrValueJader')
                        }]);
                } else if (map.name === "rorValueJader") {
                    aoColumns.push.apply(aoColumns, [
                        {
                            "mData": "rorValueJader",
                            "className": 'text-center',
                            "mRender": function (data, type, row) {
                                if (row.rorValueJader == -1) {
                                    return '<span>' + '-' + '</span>'
                                }
                                return '<span class="ROR">' + row.rorValueJader + '</span>';
                            },
                            "visible": signal.fieldManagement.visibleColumns('rorValueJader')
                        }]);
                } else if (map.name === "eb05Jader") {
                    aoColumns.push.apply(aoColumns, [
                        {
                            "mData": "eb05Jader",
                            "mRender": function (data, type, row) {
                                if (row.eb05Jader == -1) {
                                    return signal.utils.stacked(
                                        '<span class="EB05">' + '-' + '</span>',
                                        '<span class="EB95">' + '-' + '</span>'
                                    );
                                }
                                return signal.utils.stacked(
                                    '<span class="EB05">' + row.eb05Jader + '</span>',
                                    '<span class="EB95">' + row.eb95Jader + '</span>'
                                );
                            },
                            className: 'text-center',
                            "visible": signal.fieldManagement.visibleColumns('eb05Jader')
                        }]);
                } else if(map.name === "prrLCIJader"){
                    aoColumns.push.apply(aoColumns, [
                        {
                            "mData": "prrLCIJader",
                            "mRender": function (data, type, row) {
                                if (row.prrLCIJader || row.prrUCIJader) {
                                    if (row.prrLCIJader == -1.0 && row.prrUCIJader != -1.0) {
                                        row.prrLCIJader = '-'
                                    } else {
                                        if (row.prrLCIJader != -1.0 && row.prrUCIJader == -1.0) {
                                            row.prrUCIJader = '-'
                                        } else {
                                            if (row.prrLCIJader == -1.0 && row.prrUCIJader == -1.0) {
                                                row.prrLCIJader = '-'
                                                row.prrUCIJader = '-'
                                            }
                                        }
                                    }
                                }
                                return signal.utils.stacked(
                                    '<span class="PRRLCI">' + row.prrLCIJader + '</span>',
                                    '<span class="PRRUCI">' + row.prrUCIJader + '</span>'
                                )
                            },
                            className: 'text-center',
                            "visible": signal.fieldManagement.visibleColumns('prrLCIJader')
                        }])
                }else if(map.name === "rorLCIJader"){
                    aoColumns.push.apply(aoColumns, [
                        {
                            "mData": "rorLCIJader",
                            "mRender": function (data, type, row) {
                                if (row.rorLCIJader || row.rorUCIJader) {
                                    if (row.rorLCIJader == -1.0 && row.rorUCIJader != -1.0) {
                                        row.rorUCIJader = '-'
                                    } else {
                                        if (row.rorLCIJader != -1.0 && row.rorUCIJader == -1.0) {
                                            row.rorUCIJader = '-'
                                        } else {
                                            if (row.rorLCIJader == -1.0 && row.rorUCIJader == -1.0) {
                                                row.rorLCIJader = '-'
                                                row.rorUCIJader = '-'
                                            }
                                        }
                                    }
                                }
                                return signal.utils.stacked(
                                    '<span class="RORLCI">' + row.rorLCIJader + '</span>',
                                    '<span class="RORUCI">' + row.rorUCIJader + '</span>'
                                )
                            },
                            className: 'text-center',
                            "visible": signal.fieldManagement.visibleColumns('rorLCIJader')
                        }])
                }else if(map.name === "chiSquareJader"){
                    aoColumns.push.apply(aoColumns, [
                        {
                            "mData": "chiSquareJader",
                            "mRender": function (data, type, row) {
                                if (row.chiSquareJader == -1) {
                                    return '-'
                                }
                                return '<span class="CHI_SQUARE_JADER">' + row.chiSquareJader + '</span>';
                            },
                            'className': 'text-center',
                            "visible": signal.fieldManagement.visibleColumns('chiSquareJader')
                        }]);
                } else if(map.name === "justification"){
                    aoColumns.push.apply(aoColumns, [
                        {
                            "mData": "justification",
                            'className': 'col-min-150 col-max-200 cell-break',
                            "mRender": function (data, type, row) {
                                var colElement = '<div class="col-container"><div class="col-height textPre">';
                                colElement += encodeToHTML(row.justification);
                                colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + escapeHTML(encodeToHTML(row.justification)) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                                colElement += '</div></div>';
                                return colElement
                            },
                            "orderable": true,
                            "visible": signal.fieldManagement.visibleColumns('justification')

                        }]);
                } else if(map.name === "dispPerformedBy"){
                    aoColumns.push.apply(aoColumns, [
                        {
                            "mData": "dispPerformedBy",
                            'className': 'col-min-150 col-max-200 cell-break',
                            "mRender": function (data, type, row) {
                                var colElement = '<div class="col-container"><div class="col-height">';
                                colElement += encodeToHTML(row.dispPerformedBy);
                                colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(row.dispPerformedBy) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                                colElement += '</div></div>';
                                return colElement
                            },
                            "orderable": true,
                            "visible": signal.fieldManagement.visibleColumns('dispPerformedBy')

                        }]);
                } else if(map.name === "dispLastChange"){
                    aoColumns.push.apply(aoColumns, [
                        {
                            "mData": "dispLastChange",
                            'className': 'col-min-200 col-max-250 word-break',
                            "mRender": function (data, type, row) {
                                var colElement = '<div class="col-container"><div class="col-height">';
                                colElement += encodeToHTML(row.dispLastChange);
                                colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(row.dispLastChange) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                                colElement += '</div></div>';
                                return colElement
                            },
                            "orderable": true,
                            "visible": signal.fieldManagement.visibleColumns('dispLastChange')

                        }]);
                }
                else if(map.name === "comment"){
                    aoColumns.push.apply(aoColumns, [
                        {
                            "mData": "comment",
                            'className': 'col-min-150 col-max-200 cell-break',
                            "mRender": function (data, type, row) {
                                var colElement = '<div class="col-container"><div class="col-height commentDisplay">';
                                colElement += encodeToHTML(row.comment);
                                colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + escapeHTML(encodeToHTML(row.comment)) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                                colElement += '</div></div>';
                                return colElement
                            },
                            "sortable": false,
                            "visible": signal.fieldManagement.visibleColumns('comment')

                        }]);
                } else if(map.type === "score") {
                    var colName = map.name
                    aoColumns.push.apply(aoColumns, [
                        {
                            "mData": colName,
                            "className": 'text-center',
                            "mRender": function (data, type, row) {
                                if (row[colName] === -1) {
                                    return '<span>' + '-' + '</span>'
                                }
                                return '<span>' + row[colName] + '</span>';
                            },
                            "visible":  signal.fieldManagement.visibleColumns(colName)
                        }]);
                }
            });
            for (var index = 0; index < parseInt(prevColCount); index++) {
                aoColumns.push.apply(aoColumns, makeAoColumnsJaderPrevious(jaderColumns,index));
            }
        }
        else {
            aoColumns.push.apply(aoColumns, labelConfigCopyJson.alertTags ? [
                {
                    "mData": "alertTags",
                    "mRender": function (data, type, row) {
                        var tagsElement = signal.alerts_utils.get_tags_element(row.alertTags);
                        return tagsElement
                    },
                    "className": 'col-max-300 pos-rel',
                    "sortable": false,
                    "visible": signal.fieldManagement.visibleColumns('alertTags')
                }] : []);

            aoColumns.push.apply(aoColumns, labelConfigCopyJson.impEvents ? [{
                "mData": "impEvents",
                "className": 'col-min-75',
                "mRender": function (data, type, row) {
                    return ptBadge(row.ime, row.dme, row.ei, row.isSm);
                },
                "visible": signal.fieldManagement.visibleColumns('impEvents')
            }] : []);
            if (groupBySmq != "true") {
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.listed ? [{
                    "mData": "listed",
                    "className": '',
                    "mRender": function (data, type, row) {
                        return '<span>' + row.listed + '</span>'
                    },
                    "visible": signal.fieldManagement.visibleColumns('listed')
                }] : []);
            }
            aoColumns.push.apply(aoColumns, labelConfigCopyJson.newCount ? [
                {
                    "mData": "newCount",
                    "mRender": function (data, type, row) {
                        if (hyperlinkConfiguration.newCount) {
                            return signal.utils.stacked(
                                cnt_drill_down(row.newCount, COUNTS.NEW, FLAGS.CUMM_FLAG, labelConfigKeyId["newCount"], row.execConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, "newCount", row.groupBySmq, row.preferredTerm, row.soc, row.productSelection,"NEW_COUNT"),
                                cnt_drill_down(row.cummCount, COUNTS.CUMM, FLAGS.CUMM_FLAG, labelConfigKeyId["newCount"], row.execConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, "cumCount", row.groupBySmq, row.preferredTerm, row.soc, row.productSelection,"CUMM_COUNT"))
                        } else {
                            return signal.utils.stackedForClass(
                                row.newCount,
                                row.cummCount,
                                'newCount',
                                'cumCount')
                        }

                    },
                    className: '',
                    "visible": signal.fieldManagement.visibleColumns('newCount')
                }] : []);

            if (isFaersEnabled) {
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.newCountFaers ? [
                    {
                        "mData": "newCountFaers",
                        "mRender": function (data, type, row) {
                            if (typeof row.faersExecConfigId === 'undefined') {
                                row.faersExecConfigId = row.execConfigId;
                            }
                            if (hyperlinkConfiguration.newCountFaers) {

                                return signal.utils.stacked(
                                    cnt_drill_down_faers(row.newCountFaers, COUNTS.NEW, FLAGS.CUMM_FLAG, labelConfigKeyId["newCountFaers"], row.faersExecConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, row.preferredTerm, 'NEW_COUNT', "newCountFaers", row.soc, row.productSelection),
                                    cnt_drill_down_faers(row.cummCountFaers, COUNTS.CUMM, FLAGS.CUMM_FLAG, labelConfigKeyId["newCountFaers"], row.faersExecConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, row.preferredTerm, 'CUMM_COUNT', "cumCountFaers", row.soc, row.productSelection))
                            } else {
                                return signal.utils.stackedForClass(
                                    row.newCountFaers,
                                    row.cummCountFaers,
                                    'newCountFaers',
                                    'cumCountFaers')
                            }


                        },
                        className: '',
                        "visible": signal.fieldManagement.visibleColumns('newCountFaers')
                    }] : []);
            }
            if (isEvdasEnabled) {
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.newEvEvdas ? [
                    {
                        "mData": "newEvEvdas",
                        "mRender": function (data, type, row) {
                            if (hyperlinkConfiguration.newEvEvdas) {
                                return signal.utils.stacked(
                                    drillDownOptions(row.newEvEvdas, encodeToHTML(row.productName), row.preferredTerm, row.evdasExecConfigId, 0, true, row.id, row.newEvLink, 'newEvEvdas'),
                                    drillDownOptions(row.totalEvEvdas, encodeToHTML(row.productName), row.preferredTerm, row.evdasExecConfigId, 0, false, row.id, row.totalEvLink, 'totalEvEvdas'))
                            } else {
                                return signal.utils.stackedForClass(
                                    row.newEvEvdas,
                                    row.totalEvEvdas,
                                    'newEvEvdas',
                                    'totalEvEvdas')
                            }


                        },
                        className: '',
                        "visible": signal.fieldManagement.visibleColumns('newEvEvdas')
                    }] : []);
            }

            aoColumns.push.apply(aoColumns, labelConfigCopyJson.newSponCount ? [
                {
                    "mData": "newSponCount",
                    "mRender": function (data, type, row) {

                        if (hyperlinkConfiguration.newSponCount) {
                            return signal.utils.stacked(
                                cnt_drill_down(row.newSponCount, COUNTS.NEW, FLAGS.SPONT_FLAG, labelConfigKeyId["newSponCount"], row.execConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, 'newSponCount', row.groupBySmq, row.preferredTerm, row.soc, row.productSelection,"NEW_SPON"),
                                cnt_drill_down(row.cumSponCount, COUNTS.CUMM, FLAGS.SPONT_FLAG, labelConfigKeyId["newSponCount"], row.execConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, 'cumSponCount', row.groupBySmq, row.preferredTerm, row.soc, row.productSelection,"CUMM_SPON"))
                        } else {
                            return signal.utils.stackedForClass(
                                row.newSponCount,
                                row.cumSponCount,
                                'newSponCount',
                                'cumSponCount')
                        }

                    },
                    className: '',
                    "visible": signal.fieldManagement.visibleColumns('newSponCount')
                }
            ] : []);

            aoColumns.push.apply(aoColumns, labelConfigCopyJson.newSeriousCount ? [
                {
                    "mData": "newSeriousCount",
                    "mRender": function (data, type, row) {

                        if (hyperlinkConfiguration.newSeriousCount) {
                            return signal.utils.stacked(
                                cnt_drill_down(row.newSeriousCount, COUNTS.NEW, FLAGS.SERIOUS_FLAG, labelConfigKeyId["newSeriousCount"], row.execConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, 'newSeriousCount', row.groupBySmq, row.preferredTerm, row.soc, row.productSelection,"NEW_SER"),
                                cnt_drill_down(row.cumSeriousCount, COUNTS.CUMM, FLAGS.SERIOUS_FLAG, labelConfigKeyId["newSeriousCount"], row.execConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, 'cumSeriousCount', row.groupBySmq, row.preferredTerm, row.soc, row.productSelection,"CUMM_SER"))
                        } else {
                            return signal.utils.stackedForClass(
                                row.newSeriousCount,
                                row.cumSeriousCount,
                                'newSeriousCount',
                                'cumSeriousCount')
                        }

                    },
                    className: '',
                    "visible": signal.fieldManagement.visibleColumns('newSeriousCount')
                }] : []);

            if (isFaersEnabled) {
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.newSeriousCountFaers ? [
                    {
                        "mData": "newSeriousCountFaers",
                        "mRender": function (data, type, row) {
                            if (typeof row.faersExecConfigId === 'undefined') {
                                row.faersExecConfigId = row.execConfigId;
                            }

                            if (hyperlinkConfiguration.newSeriousCountFaers) {
                                return signal.utils.stacked(
                                    cnt_drill_down_faers(row.newSeriousCountFaers, COUNTS.NEW, FLAGS.SERIOUS_FLAG, labelConfigKeyId["newSeriousCountFaers"], row.faersExecConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, row.preferredTerm, 'NEW_SER', "newSeriousCountFaers", row.soc, row.productSelection),
                                    cnt_drill_down_faers(row.cumSeriousCountFaers, COUNTS.CUMM, FLAGS.SERIOUS_FLAG, labelConfigKeyId["newSeriousCountFaers"], row.faersExecConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, row.preferredTerm, 'CUMM_SER', "cumSeriousCountFaers", row.soc, row.productSelection))
                            } else {
                                return signal.utils.stackedForClass(
                                    row.newSeriousCountFaers,
                                    row.cumSeriousCountFaers,
                                    'newSeriousCountFaers',
                                    'cumSeriousCountFaers')
                            }


                        },
                        className: '',
                        "visible": signal.fieldManagement.visibleColumns('newSeriousCountFaers')
                    }] : []);
            }
            if (isEvdasEnabled) {
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.dmeImeEvdas ? [
                    {
                        "mData": "dmeImeEvdas",
                        'className': 'col-min-50 dmeIme',
                        "mRender": function (data, type, row) {
                            return '<span class="dmeImeEvdas">' + row.dmeImeEvdas + '</span>'
                        },
                        "visible": signal.fieldManagement.visibleColumns('dmeImeEvdas')
                    }] : []);
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.sdrEvdas ? [

                    {
                        "mData": "sdrEvdas",
                        "mRender": function (data, type, row) {
                            return '<span class="sdrEvdas">' + row.sdrEvdas + '</span>'
                        },
                        'className': '',
                        "visible": signal.fieldManagement.visibleColumns('sdrEvdas')
                    }] : []);

                aoColumns.push.apply(aoColumns, labelConfigCopyJson.rorValueEvdas ? [
                    {
                        "mData": "rorValueEvdas",
                        'className': 'text-center',
                        "mRender": function (data, type, row) {
                            return '<span class="rorAllEvdas">' + row.rorValueEvdas + '</span>'
                        },
                        "visible": signal.fieldManagement.visibleColumns('rorValueEvdas')
                    }] : []);
            }

            aoColumns.push.apply(aoColumns, labelConfigCopyJson.newFatalCount ? [
                {
                    "mData": "newFatalCount",
                    "mRender": function (data, type, row) {

                        if (hyperlinkConfiguration.newFatalCount) {
                            return signal.utils.stacked(
                                cnt_drill_down(row.newFatalCount, COUNTS.NEW, FLAGS.FATAL_FLAG, labelConfigKeyId["newFatalCount"], row.execConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, 'newFatalCount', row.groupBySmq, row.preferredTerm, row.soc, row.productSelection,"NEW_FATAL"),
                                cnt_drill_down(row.cumFatalCount, COUNTS.CUMM, FLAGS.FATAL_FLAG, labelConfigKeyId["newFatalCount"], row.execConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, 'cumFatalCount', row.groupBySmq, row.preferredTerm, row.soc, row.productSelection,"CUMM_FATAL"))
                        } else {
                            return signal.utils.stackedForClass(
                                row.newFatalCount,
                                row.cumFatalCount,
                                'newFatalCount',
                                'cumFatalCount')
                        }

                    },
                    className: '',
                    "visible": signal.fieldManagement.visibleColumns('newFatalCount')
                }] : []);

            aoColumns.push.apply(aoColumns, labelConfigCopyJson.newStudyCount ? [
                {
                    "mData": "newStudyCount",
                    "mRender": function (data, type, row) {
                        if (hyperlinkConfiguration.newStudyCount) {
                            return signal.utils.stacked(
                                cnt_drill_down(row.newStudyCount, COUNTS.NEW, FLAGS.STUDY_FLAG, labelConfigKeyId["newStudyCount"], row.execConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, 'newStudyCount', row.groupBySmq, row.preferredTerm, row.soc, row.productSelection,"NEW_STUDY"),
                                cnt_drill_down(row.cumStudyCount, COUNTS.CUMM, FLAGS.STUDY_FLAG, labelConfigKeyId["newStudyCount"], row.execConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, 'cumStudyCount', row.groupBySmq, row.preferredTerm, row.soc, row.productSelection,"CUMM_STUDY"))
                        } else {
                            return signal.utils.stackedForClass(
                                row.newStudyCount,
                                row.cumStudyCount,
                                'newStudyCount',
                                'cumStudyCount')
                        }


                    },
                    className: '',
                    "visible": signal.fieldManagement.visibleColumns('newStudyCount')
                }] : []);
            aoColumns.push.apply(aoColumns, labelConfigCopyJson.freqPriority ? [
                {
                    "mData": "freqPriority",
                    "className": 'col-min-100',
                    "visible": signal.fieldManagement.visibleColumns('freqPriority')
                }] : []);
            aoColumns.push.apply(aoColumns, labelConfigCopyJson.trendType ? [
                {
                    "mData": "trendType",
                    "className": 'col-min-75',
                    "visible": signal.fieldManagement.visibleColumns('trendType')
                }] : []);
            if (prrVisibility) {
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.prrValue ? [{
                    "mData": "prrValue",
                    "className": 'text-center',
                    "mRender": function (data, type, row) {
                        if (row.prrValue == -1) {
                            return '<span>' + '-' + '</span>'
                        }
                        return '<span class="prrValue">' + row.prrValue + '</span>';
                    },
                    "visible": signal.fieldManagement.visibleColumns('prrValue')
                }] : []);
            }
            if (rorVisibility) {
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.rorValue ? [{
                    "mData": "rorValue",
                    className: 'text-center',
                    "mRender": function (data, type, row) {
                        if (row.rorValue == -1) {
                            return '<span>' + '-' + '</span>'
                        }
                        return '<span class="rorValue">' + row.rorValue + '</span>';
                    },
                    "visible": signal.fieldManagement.visibleColumns('rorValue')
                }] : []);
            }
            if (ebgmVisibility) {
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.eb05 ? [
                    {
                        "mData": "eb05",
                        "mRender": function (data, type, row) {
                            if (row.eb05 == -1) {
                                return signal.utils.stacked(
                                    '<span class="eb05">' + '-' + '</span>',
                                    '<span class="eb95">' + '-' + '</span>'
                                );
                            }
                            return signal.utils.stacked(
                                '<span class="eb05">' + row.eb05 + '</span>',
                                '<span class="eb95">' + row.eb95 + '</span>'
                            );
                        },
                        className: 'text-center',
                        "visible": signal.fieldManagement.visibleColumns('eb05')
                    }] : []);
            }
            if (isFaersEnabled && showFaersEbgm) {
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.eb05Faers ? [
                    {
                        "mData": "eb05Faers",
                        "mRender": function (data, type, row) {
                            return signal.utils.stacked(
                                '<span class="eb05Faers">' + row.eb05Faers + '</span>',
                                '<span class="eb95Faers">' + row.eb95Faers + '</span>'
                            );
                        },
                        className: 'text-center',
                        "visible": signal.fieldManagement.visibleColumns('eb05Faers')
                    }] : []);
            }
            if (prrVisibility && rorVisibility) {
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.chiSquare ? [
                    {
                        "mData": "chiSquare",
                        "mRender": function (data, type, row) {
                            if (row.chiSquare == -1) {
                                return '-'
                            }
                            return '<span class="chiSqaure">' + row.chiSquare + '</span>';
                        },
                        'className': 'text-center',
                        "visible": signal.fieldManagement.visibleColumns('chiSquare')
                    }
                ] : []);
            }

            aoColumns.push.apply(aoColumns, labelConfigCopyJson.signalsAndTopics ? [
                {
                    "mData": 'signalsAndTopics',
                    "mRender": function (data, type, row) {
                        var signalAndTopics = '';
                        $.each(row.signalsAndTopics, function (i, obj) {
                            var url = signalDetailUrl + '?id=' + obj['signalId'];
                            if ($('#hasSignalManagementAccess').val() == 'true') {
                                signalAndTopics = signalAndTopics + '<span class="click box-inline word-wrap-break-word col-max-150" style="padding: 0px 0px 0px 0px !important"><a class="cell-break " title="' + obj.disposition.displayName + '" onclick="validateAccess(event,' + obj['signalId'] + ')" href="' + url + '">' + escapeHTML(obj['name']) + '</a></span>&nbsp;'
                            } else {
                                signalAndTopics = signalAndTopics + '<span class="click box-inline word-wrap-break-word col-max-150 signalSummaryAuth" style="padding: 0px 0px 0px 0px !important"><a class="cell-break" title="' + obj.disposition.displayName + '" href="javascript:void(0)">' + escapeHTML(obj['name']) + '</a></span>&nbsp;'
                            }
                            signalAndTopics = signalAndTopics + ","
                        });
                        if (signalAndTopics.length > 1) {
                            return '<div class="cell-break word-wrap-break-word col-max-150" style="padding: 0px 0px 0px 0px !important">' + signalAndTopics.substring(0, signalAndTopics.length - 1) + '</div>';
                        } else {
                            return '-';
                        }
                    },
                    'className': 'col-min-150 col-max-200 signalInformation',
                    "sortable": false,
                    "visible": signal.fieldManagement.visibleColumns('signalsAndTopics')
                }] : []);
            aoColumns.push.apply(aoColumns, labelConfigCopyJson.disposition ? [
                {
                    "mData": "disposition",
                    "mRender": function (data, type, row) {
                        var colElement = '<div class="col-container"><div class="col-height">';
                        // escapeAllHTML ensure double quotes inside strings are properly escaped
                        var escapedText = escapeAllHTML(row.disposition)
                        colElement += encodeToHTML(escapedText);
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(escapedText) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement;
                    },
                    'className': 'col-max-250 currentDisposition',
                    "visible": signal.fieldManagement.visibleColumns('disposition')
                }] : []);
            aoColumns.push.apply(aoColumns, labelConfigCopyJson.currentDisposition ? [
                {
                    "mData": "currentDisposition",
                    "mRender": function (data, type, row) {
                        return signal.utils.render('disposition_dss3', {
                            allowedDisposition: dispositionIncomingOutgoingMap[row.disposition],
                            proposedDisposition: row.proposedDisposition,
                            currentDisposition: row.disposition,
                            forceJustification: forceJustification,
                            isReviewed: row.isReviewed,
                            isValidationStateAchieved: row.isValidationStateAchieved,
                            id: row.currentDispositionId
                        });
                    },
                    "visible": signal.fieldManagement.visibleColumns('currentDisposition'),
                    "class": 'col-max-300 dispositionAction'
                }] : []);


            if (dssVisibility) {
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.rationale ? [
                    {
                        "mData": "rationale",
                        "mRender": function (data, type, row) {

                            var rationaleDetails;
                            var dssScoreStr = row.dssScore;
                            dssScoreStr = dssScoreStr == null || dssScoreStr === "" || dssScoreStr === "-" ? '-' : dssScoreStr;
                            var pecScore = pecScore == null || pecScore === "" ? 0 : pecScore;
                            if (((row.isSafety && $('#groupBySmq').val()) || !row.isSafety) == "true") {
                                dssScoreStr = '-';
                            }

                            if (dssScoreStr !== "-") {
                                rationaleDetails = JSON.parse(dssScoreStr)['rationale_details']
                            }
                            var rationaleField;
                            if (row.pecImpNumHigh !== '-') {
                                if (row.rationale && row.rationale !== '' && row.rationale !== '-')
                                    rationaleField = "<span data-field ='rationale' style = 'white-space: nowrap;' data-id=''>" + row.rationale + " " + row.pecImpNumHigh + "</span>";
                                else
                                    rationaleField = "<span data-field ='rationale' style = 'white-space: nowrap;' data-id=''>" + row.pecImpNumHigh + "</span>";
                            } else {
                                rationaleField = "<span data-field ='rationale' style = 'white-space: nowrap;' data-id=''>" + row.rationale + "</span>";
                            }
                            var ptName = row.primaryEvent.replace(/'/g, '&#39;')
                            var productNameDss = encodeToHTML(row.productName)
                            var socDss = row.soc
                            if (row.rationale == 0) {
                                rationaleField += "";
                            } else if (dssScoreStr !== "-") {
                                rationaleField += "<span style = 'white-space: nowrap;' data-newCount = '" + row.newCount + "' data-rationale='" + row.rationale + "' data-pecImpNumHigh= '" + row.pecImpNumHigh + "' data-dssScore = '" + dssScoreStr + "' data-pt='" + ptName + "' data-productDss='" + productNameDss + "' data-socDss='" + socDss + "' data-exConfigId='" + row.execConfigId + "' data-configId='" + row.configId + "' data-toggle='modal' id='info-icon' data-target='#dss-modal'> <i class='mdi mdi-information pv-grid-ic' style='color: black'></i> </span>"
                            } else {
                                rationaleField = "";
                                rationaleField += "<span style = 'white-space: nowrap;'  data-dssScore = '" + dssScoreStr + "'>-</span>"
                            }
                            return rationaleField
                        },

                        className: 'text-center',
                        "visible": signal.fieldManagement.visibleColumns('rationale')
                    }] : []);
            }

            if (dssVisibility) {
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.pecImpNumHigh ? [

                    {
                        "mData": "pecImpNumHigh",
                        "mRender": function (data, type, row) {
                            return '<span class="EB95">' + genDSSLink(row.id, row.exConfigId, row.isArchived, row.pecImpNumHigh, row.isSafety) + '</span>';
                        },
                        className: 'text-center',
                        "visible": signal.fieldManagement.visibleColumns('pecImpNumHigh')
                    }] : []);
            }
            aoColumns.push.apply(aoColumns, labelConfigCopyJson.assignedTo ? [
                {
                    "mData": "assignedTo",
                    "className": 'col-min-100 col-max-150 assignedTo',
                    "mRender": function (data, type, row) {
                        return signal.list_utils.assigned_to_comp(row.id, row.assignedTo)
                    },
                    "sortable": false,
                    "visible": signal.fieldManagement.visibleColumns('assignedTo')
                }] : []);
            aoColumns.push.apply(aoColumns, labelConfigCopyJson.dueDate ? [
                {
                    "mData": "dueDate",
                    "className": 'col-min-50 dueIn text-center',
                    "mRender": function (data, type, row) {
                        return signal.list_utils.due_in_comp(row.dueIn)
                    },
                    "visible": signal.fieldManagement.visibleColumns('dueDate')
                }] : []);

            if (groupBySmq != "true") {
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.positiveRechallenge ? [{
                    "mData": "positiveRechallenge",
                    "className": '',
                    "mRender": function (data, type, row) {
                        return '<span>' + row.positiveRechallenge + '</span>'
                    },
                    "visible": signal.fieldManagement.visibleColumns('positiveRechallenge')
                }] : []);
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.positiveDechallenge ? [
                    {
                        "mData": "positiveDechallenge",
                        "className": '',
                        "mRender": function (data, type, row) {
                            return '<span>' + row.positiveDechallenge + '</span>'
                        },
                        "visible": signal.fieldManagement.visibleColumns('positiveDechallenge')
                    }
                ] : []);
            }

            if (prrVisibility) {
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.prrLCI ? [
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
                                '<span class="prrLCI">' + row.prrLCI + '</span>',
                                '<span class="prrUCI">' + row.prrUCI + '</span>'
                            )
                        },
                        className: 'col-min-100',
                        "visible": signal.fieldManagement.visibleColumns('prrLCI')
                    }] : []);
            }
            if (rorVisibility) {
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.rorLCI ? [
                    {
                        "mData": "rorLCI",
                        "mRender": function (data, type, row) {
                            if (row.rorLCI || row.rorUCI) {
                                if (row.rorLCI == -1.0 && row.rorUCI != -1.0) {
                                    row.rorLCI = '-'
                                } else {
                                    if (row.rorLCI != -1.0 && row.rorLCI == -1.0) {
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
                                '<span class="rorLCI">' + row.rorLCI + '</span>',
                                '<span class="rorUCI">' + row.rorUCI + '</span>'
                            )
                        },
                        className: '',
                        "visible": signal.fieldManagement.visibleColumns('rorLCI')
                    }] : []);
            }
            if (ebgmVisibility) {
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.ebgm ? [{
                    "mData": "ebgm",
                    className: 'text-center',
                    "mRender": function (data, type, row) {
                        if (row.ebgm == -1) {
                            return '<span>' + '-' + '</span>'
                        } else {
                            return '<span class="ebgm">' + row.ebgm + '</span>';
                        }
                    },
                    "visible": signal.fieldManagement.visibleColumns('ebgm')
                }] : []);
            }

            if (groupBySmq != "true") {
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.related ? [
                    {
                        "mData": "related",
                        "className": '',
                        "mRender": function (data, type, row) {
                            return '<span>' + row.related + '</span>'
                        },
                        "visible": signal.fieldManagement.visibleColumns('related')
                    }] : []);
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.pregenency ? [
                    {
                        "mData": "pregenency",
                        "className": '',
                        "mRender": function (data, type, row) {
                            return '<span>' + row.pregenency + '</span>'
                        },
                        "visible": signal.fieldManagement.visibleColumns('pregenency')
                    }

                ] : []);
            }
            aoColumns.push.apply(aoColumns, labelConfigCopyJson.newPediatricCount ? [
                {
                    "mData": "newPediatricCount",
                    "className": '',
                    "mRender": function (data, type, row) {
                        if (hyperlinkConfiguration.newPediatricCount) {
                            return signal.utils.stacked(
                                cnt_drill_down(row.newPediatricCount, COUNTS.NEW, FLAGS.PEDI_FLAG, labelConfigKeyId["newPediatricCount"], row.execConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, 'newPediatricCount', row.groupBySmq, row.preferredTerm, row.soc, row.productSelection,"NEW_PEDI"),
                                cnt_drill_down(row.cummPediatricCount, COUNTS.CUMM, FLAGS.PEDI_FLAG, labelConfigKeyId["newPediatricCount"], row.execConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, 'cumPediatricCount', row.groupBySmq, row.preferredTerm, row.soc, row.productSelection,"CUMM_PEDI"))
                        } else {
                            return signal.utils.stackedForClass(
                                row.newPediatricCount,
                                row.cummPediatricCount,
                                'newPediatricCount',
                                'cumPediatricCount')
                        }
                    },
                    "visible": signal.fieldManagement.visibleColumns('newPediatricCount')
                }] : []);
            aoColumns.push.apply(aoColumns, labelConfigCopyJson.newInteractingCount ? [
                {
                    "mData": "newInteractingCount",
                    "className": '',
                    "mRender": function (data, type, row) {

                        if (hyperlinkConfiguration.newInteractingCount) {
                            return signal.utils.stacked(
                                cnt_drill_down(row.newInteractingCount, COUNTS.NEW, FLAGS.INTERACTING_FLAG, labelConfigKeyId["newInteractingCount"], row.execConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, 'newInteractingCount', row.groupBySmq, row.preferredTerm, row.soc, row.productSelection,"NEW_INTERACTING"),
                                cnt_drill_down(row.cummInteractingCount, COUNTS.CUMM, FLAGS.INTERACTING_FLAG, labelConfigKeyId["newInteractingCount"], row.execConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, 'cumInteractingCount', row.groupBySmq, row.preferredTerm, row.soc, row.productSelection,"CUMM_INTERACTING"))
                        } else {
                            return signal.utils.stackedForClass(
                                row.newInteractingCount,
                                row.cummInteractingCount,
                                'newInteractingCount',
                                'cumInteractingCount')
                        }

                    },
                    "visible": signal.fieldManagement.visibleColumns('newInteractingCount')
                }] : []);
            aoColumns.push.apply(aoColumns, labelConfigCopyJson.newGeriatricCount ? [
                {
                    "mData": "newGeriatricCount",
                    "mRender": function (data, type, row) {

                        if (hyperlinkConfiguration.newGeriatricCount) {
                            return signal.utils.stacked(
                                cnt_drill_down(row.newGeriatricCount, COUNTS.NEW, FLAGS.GERI_FLAG, labelConfigKeyId["newGeriatricCount"], row.execConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, 'newGeriatricCount', row.groupBySmq, row.preferredTerm, row.soc, row.productSelection,"NEW_GERI"),
                                cnt_drill_down(row.cumGeriatricCount, COUNTS.CUMM, FLAGS.GERI_FLAG, labelConfigKeyId["newGeriatricCount"], row.execConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, 'cumGeriatricCount', row.groupBySmq, row.preferredTerm, row.soc, row.productSelection,"CUMM_GERI"))
                        } else {
                            return signal.utils.stackedForClass(
                                row.newGeriatricCount,
                                row.cumGeriatricCount,
                                'newGeriatricCount',
                                'cumGeriatricCount')
                        }

                    },
                    className: '',
                    "visible": signal.fieldManagement.visibleColumns('newGeriatricCount')
                }] : []);
            aoColumns.push.apply(aoColumns, labelConfigCopyJson.newNonSerious ? [
                {
                    "mData": "newNonSerious",
                    "mRender": function (data, type, row) {

                        if (hyperlinkConfiguration.newNonSerious) {
                            return signal.utils.stacked(
                                cnt_drill_down(row.newNonSerious, COUNTS.NEW, FLAGS.NON_SERIOUS_FLAG, labelConfigKeyId["newNonSerious"], row.execConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, 'newNonSerious', row.groupBySmq, row.preferredTerm, row.soc, row.productSelection,"NEW_NON_SERIOUS"),
                                cnt_drill_down(row.cumNonSerious, COUNTS.CUMM, FLAGS.NON_SERIOUS_FLAG, labelConfigKeyId["newNonSerious"], row.execConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, 'cumNonSerious', row.groupBySmq, row.preferredTerm, row.soc, row.productSelection,"CUMM_NON_SERIOUS"))
                        } else {
                            return signal.utils.stackedForClass(
                                row.newNonSerious,
                                row.cumNonSerious,
                                'newNonSerious',
                                'cumNonSerious')
                        }

                    },
                    className: '',
                    "visible": signal.fieldManagement.visibleColumns('newNonSerious')
                }] : []);

            aoColumns.push.apply(aoColumns, labelConfigCopyJson.justification ? [
                {
                    "mData": "justification",
                    'className': 'col-min-150 col-max-200 cell-break',
                    "mRender": function (data, type, row) {
                        var colElement = '<div class="col-container"><div class="col-height textPre">';
                        colElement += encodeToHTML(row.justification);
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + escapeHTML(encodeToHTML(row.justification)) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    },
                    "orderable": true,
                    "visible": signal.fieldManagement.visibleColumns('justification')
                }] : []);
            aoColumns.push.apply(aoColumns, labelConfigCopyJson.dispPerformedBy ? [
                {
                    "mData": "dispPerformedBy",
                    'className': 'col-min-150 col-max-200 cell-break',
                    "mRender": function (data, type, row) {
                        var colElement = '<div class="col-container"><div class="col-height">';
                        colElement += encodeToHTML(row.dispPerformedBy);
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(row.dispPerformedBy) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    },
                    "orderable": true,
                    "visible": signal.fieldManagement.visibleColumns('dispPerformedBy')
                }] : []);
            aoColumns.push.apply(aoColumns, labelConfigCopyJson.dispLastChange ? [
                {
                    "mData": "dispLastChange",
                    'className': 'col-min-200 col-max-250 word-break',
                    "mRender": function (data, type, row) {
                        var colElement = '<div class="col-container"><div class="col-height">';
                        colElement += encodeToHTML(row.dispLastChange);
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(row.dispLastChange) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    },
                    "orderable": true,
                    "visible": signal.fieldManagement.visibleColumns('dispLastChange')
                }] : []);
            aoColumns.push.apply(aoColumns, labelConfigCopyJson.comment ? [
                {
                    "mData": "comment",
                    'className': 'col-min-150 col-max-200 cell-break',
                    "mRender": function (data, type, row) {
                        var colElement = '<div class="col-container"><div class="col-height commentDisplay">';
                        colElement += encodeToHTML(row.comment);
                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + escapeHTML(encodeToHTML(row.comment)) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                        colElement += '</div></div>';
                        return colElement
                    },
                    "sortable": false,
                    "visible": signal.fieldManagement.visibleColumns('comment')
                }] : []);
            aoColumns.push.apply(aoColumns, labelConfigCopyJson.trendFlag ? [
                {
                    "mData": "trendFlag",
                    'className': '',
                    "mRender": function (data, type, row) {
                        return '<span>' + row.trendFlag + '</span>'
                    },
                    "visible": signal.fieldManagement.visibleColumns('trendFlag')
                }] : []);
            aoColumns.push.apply(aoColumns, labelConfigCopyJson.newProdCount ? [
                {
                    "mData": "newProdCount",
                    "mRender": function (data, type, row) {
                        var newProdCount = row.newProdCount;
                        var cumProdCount = row.cumProdCount;

                        return signal.utils.stacked(
                            '<span class="newProdCount">' + newProdCount + '</span>',
                            '<span class="cumProdCount">' + cumProdCount + '</span>'
                        );
                    },
                    className: '',
                    "visible": signal.fieldManagement.visibleColumns('newProdCount')
                }] : []);
            aoColumns.push.apply(aoColumns, labelConfigCopyJson.freqPeriod ? [
                {
                    "mData": "freqPeriod",
                    "mRender": function (data, type, row) {
                        var freqPeriod = row.freqPeriod;
                        var cumFreqPeriod = row.cumFreqPeriod;
                        return signal.utils.stacked(
                            '<span class="freqPeriod">' + freqPeriod + '</span>',
                            '<span class="cumFreqPeriod">' + cumFreqPeriod + '</span>'
                        );
                    },
                    className: '',
                    "visible": signal.fieldManagement.visibleColumns('freqPeriod')
                }] : []);
            aoColumns.push.apply(aoColumns, labelConfigCopyJson.reviewedFreqPeriod ? [
                {
                    "mData": "reviewedFreqPeriod",
                    "mRender": function (data, type, row) {
                        var reviewedFreqPeriod = row.reviewedFreqPeriod;
                        var reviewedCumFreqPeriod = row.reviewedCumFreqPeriod;
                        return signal.utils.stacked(
                            '<span class="reviewedFreqPeriod">' + reviewedFreqPeriod + '</span>',
                            '<span class="reviewedCumFreqPeriod">' + reviewedCumFreqPeriod + '</span>'
                        );
                    },
                    className: '',
                    "visible": signal.fieldManagement.visibleColumns('reviewedFreqPeriod')
                },
            ] : []);

            if (prrVisibility || rorVisibility || ebgmVisibility) {
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.aValue ? [
                    {
                        "mData": "aValue",
                        "mRender": function (data, type, row) {
                            if (row.aValue === -1 || row.aValue === "-") {
                                return '-'
                            }
                            return row.aValue;
                        },
                        'className': 'col-min-75 col-max-150 text-center',
                        "visible": signal.fieldManagement.visibleColumns('aValue')
                    }] : []);
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.bValue ? [
                    {
                        "mData": "bValue",
                        "mRender": function (data, type, row) {
                            if (row.bValue === -1 || row.bValue === "-") {
                                return '-'
                            }
                            return row.bValue;
                        },
                        'className': 'col-min-75 col-max-150 text-center',
                        "visible": signal.fieldManagement.visibleColumns('bValue')
                    }] : []);
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.cValue ? [
                    {
                        "mData": "cValue",
                        "mRender": function (data, type, row) {
                            if (row.cValue === -1 || row.cValue === "-") {
                                return '-'
                            }
                            return row.cValue;
                        },
                        'className': 'col-min-75 col-max-150 text-center',
                        "visible": signal.fieldManagement.visibleColumns('cValue')
                    }] : []);
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.dValue ? [
                    {
                        "mData": "dValue",
                        "mRender": function (data, type, row) {
                            if (row.dValue === -1 || row.dValue === "-") {
                                return '-'
                            }
                            return row.dValue;
                        },
                        'className': 'col-min-75 col-max-150 text-center',
                        "visible": signal.fieldManagement.visibleColumns('dValue')
                    }
                ] : []);
            }
            if (ebgmVisibility) {
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.eValue ? [
                    {
                        "mData": "eValue",
                        "mRender": function (data, type, row) {
                            if (row.eValue === -1 || row.eValue === "-") {
                                return '-'
                            }
                            return row.eValue;
                        },
                        'className': 'col-min-75 col-max-150 text-center',
                        "visible": signal.fieldManagement.visibleColumns('eValue')
                    }] : []);
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.rrValue ? [
                    {
                        "mData": "rrValue",
                        "mRender": function (data, type, row) {
                            if (row.rrValue == -1 || row.rrValue === "-") {
                                return '-'
                            }
                            return row.rrValue;
                        },
                        'className': 'col-min-75 col-max-150 text-center',
                        "visible": signal.fieldManagement.visibleColumns('rrValue')
                    }
                ] : []);
            }

            if (isFaersEnabled) {
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.newSponCountFaers ? [
                    {
                        "mData": "newSponCountFaers",
                        "mRender": function (data, type, row) {
                            if (typeof row.faersExecConfigId === 'undefined') {
                                row.faersExecConfigId = row.execConfigId;
                            }

                            if (hyperlinkConfiguration.newSponCountFaers) {
                                return signal.utils.stacked(
                                    cnt_drill_down_faers(row.newSponCountFaers, COUNTS.NEW, FLAGS.SPONT_FLAG, labelConfigKeyId["newSponCountFaers"], row.faersExecConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, row.preferredTerm, 'NEW_SPON',"newSponCountFaers", row.soc, row.productSelection),
                                    cnt_drill_down_faers(row.cumSponCountFaers, COUNTS.CUMM, FLAGS.SPONT_FLAG, labelConfigKeyId["newSponCountFaers"], row.faersExecConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, row.preferredTerm, 'CUMM_SPON',"cumSponCountFaers", row.soc, row.productSelection))
                            } else {
                                return signal.utils.stackedForClass(
                                    row.newSponCountFaers,
                                    row.cumSponCountFaers,
                                    'newSponCountFaers',
                                    'cumSponCountFaers')
                            }

                        },
                        className: '',
                        "visible": signal.fieldManagement.visibleColumns('newSponCountFaers')
                    }] : []);
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.newStudyCountFaers ? [
                    {
                        "mData": "newStudyCountFaers",
                        "mRender": function (data, type, row) {
                            if (typeof row.faersExecConfigId === 'undefined') {
                                row.faersExecConfigId = row.execConfigId;
                            }

                            if (hyperlinkConfiguration.newStudyCountFaers) {
                                return signal.utils.stacked(
                                    cnt_drill_down_faers(row.newStudyCountFaers, COUNTS.NEW, FLAGS.STUDY_FLAG, labelConfigKeyId["newStudyCountFaers"], row.faersExecConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, row.preferredTerm, 'NEW_STUDY', "newStudyCountFaers", row.soc, row.productSelection),
                                    cnt_drill_down_faers(row.cumStudyCountFaers, COUNTS.CUMM, FLAGS.STUDY_FLAG, labelConfigKeyId["newStudyCountFaers"], row.faersExecConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, row.preferredTerm, 'CUMM_STUDY', "cumStudyCountFaers", row.soc, row.productSelection))
                            } else {
                                return signal.utils.stackedForClass(
                                    row.newStudyCountFaers,
                                    row.cumStudyCountFaers,
                                    'newStudyCountFaers',
                                    'cumStudyCountFaers')
                            }

                        },
                        className: '',
                        "visible": signal.fieldManagement.visibleColumns('newStudyCountFaers')
                    }] : []);
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.freqPriorityFaers ? [
                    {
                        "mData": "freqPriorityFaers",
                        "className": 'col-min-100',
                        "visible": signal.fieldManagement.visibleColumns('freqPriorityFaers')
                    }
                ] : []);

                if (groupBySmq != "true") {
                    aoColumns.push.apply(aoColumns, labelConfigCopyJson.positiveRechallengeFaers ? [{
                        "mData": "positiveRechallengeFaers",
                        "className": '',
                        "mRender": function (data, type, row) {
                            return '<span>' + row.positiveRechallengeFaers + '</span>'
                        },
                        "visible": signal.fieldManagement.visibleColumns('positiveRechallengeFaers')
                    }] : []);
                    aoColumns.push.apply(aoColumns, labelConfigCopyJson.positiveDechallengeFaers ? [
                        {
                            "mData": "positiveDechallengeFaers",
                            "className": '',
                            "mRender": function (data, type, row) {
                                return '<span>' + row.positiveRechallengeFaers + '</span>'
                            },
                            "visible": signal.fieldManagement.visibleColumns('positiveDechallengeFaers')
                        }
                    ] : []);
                }

                if (showFaersPrr) {
                    aoColumns.push.apply(aoColumns, labelConfigCopyJson.prrValueFaers ? [{
                        "mData": "prrValueFaers",
                        "className": 'text-center',
                        "mRender": function (data, type, row) {
                            if (row.prrValueFaers == -1) {
                                return '<span>' + '-' + '</span>'
                            }
                            return '<span class="prrValueFaers">' + row.prrValueFaers + '</span>';
                        },
                        "visible": signal.fieldManagement.visibleColumns('prrValueFaers')
                    }] : []);
                    aoColumns.push.apply(aoColumns, labelConfigCopyJson.prrLCIFaers ? [
                        {
                            "mData": "prrLCIFaers",
                            "mRender": function (data, type, row) {
                                return signal.utils.stacked(
                                    '<span class="prrLCIFaers">' + row.prrLCIFaers + '</span>',
                                    '<span class="prrUCIFaers">' + row.prrUCIFaers + '</span>'
                                )
                            },
                            className: 'col-min-100',
                            "visible": signal.fieldManagement.visibleColumns('prrLCIFaers')
                        }
                    ] : []);
                }

                if (showFaersRor) {
                    aoColumns.push.apply(aoColumns, labelConfigCopyJson.rorValueFaers ? [{
                        "mData": "rorValueFaers",
                        className: 'text-center',
                        "mRender": function (data, type, row) {
                            if (row.rorValueFaers == -1) {
                                return '<span>' + '-' + '</span>'
                            }
                            return '<span class="rorValueFaers">' + row.rorValueFaers + '</span>';
                        },
                        "visible": signal.fieldManagement.visibleColumns('rorValueFaers')
                    }] : []);
                    aoColumns.push.apply(aoColumns, labelConfigCopyJson.rorLCIFaers ? [
                        {
                            "mData": "rorLCIFaers",
                            "mRender": function (data, type, row) {
                                return signal.utils.stacked(
                                    '<span class="rorLCIFaers">' + row.rorLCIFaers + '</span>',
                                    '<span class="rorUCIFaers">' + row.rorUCIFaers + '</span>'
                                )
                            },
                            className: 'text-center',
                            "visible": signal.fieldManagement.visibleColumns('rorLCIFaers')
                        }
                    ] : []);
                }

                aoColumns.push.apply(aoColumns, labelConfigCopyJson.newPediatricCountFaers ? [
                    {
                        "mData": "newPediatricCountFaers",
                        "className": '',
                        "mRender": function (data, type, row) {
                            if (typeof row.faersExecConfigId === 'undefined') {
                                row.faersExecConfigId = row.execConfigId;
                            }

                            if (hyperlinkConfiguration.newPediatricCountFaers) {
                                return signal.utils.stacked(
                                    cnt_drill_down_faers(row.newPediatricCountFaers, COUNTS.NEW, FLAGS.PEDI_FLAG, labelConfigKeyId["newPediatricCountFaers"], row.faersExecConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, row.preferredTerm, 'NEW_PEDIA_COUNT', "newPediatricCountFaers", row.soc, row.productSelection),
                                    cnt_drill_down_faers(row.cummPediatricCountFaers, COUNTS.CUMM, FLAGS.PEDI_FLAG, labelConfigKeyId["newPediatricCountFaers"], row.faersExecConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, row.preferredTerm, 'CUMM_PEDIA_COUNT', "cumPediatricCountFaers", row.soc, row.productSelection))
                            } else {
                                return signal.utils.stackedForClass(
                                    row.newPediatricCountFaers,
                                    row.cummPediatricCountFaers,
                                    'newPediatricCountFaers',
                                    'cumPediatricCountFaers')
                            }

                        },
                        "visible": signal.fieldManagement.visibleColumns('newPediatricCountFaers')
                    }] : []);
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.newInteractingCountFaers ? [
                    {
                        "mData": "newInteractingCountFaers",
                        "className": '',
                        "mRender": function (data, type, row) {
                            if (typeof row.faersExecConfigId === 'undefined') {
                                row.faersExecConfigId = row.execConfigId;
                            }
                            if (hyperlinkConfiguration.newInteractingCountFaers) {
                                return signal.utils.stacked(
                                    cnt_drill_down_faers(row.newInteractingCountFaers, COUNTS.NEW, FLAGS.INTERACTING_FLAG, labelConfigKeyId["newInteractingCountFaers"], row.faersExecConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, row.preferredTerm, 'NEW_INTERACTING', "newInteractingCountFaers", row.soc, row.productSelection),
                                    cnt_drill_down_faers(row.cummInteractingCountFaers, COUNTS.CUMM, FLAGS.INTERACTING_FLAG, labelConfigKeyId["newInteractingCountFaers"], row.faersExecConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, row.preferredTerm, 'CUMM_INTERACTING',"cumInteractingCountFaers", row.soc, row.productSelection))
                            } else {
                                return signal.utils.stackedForClass(
                                    row.newInteractingCountFaers,
                                    row.cummInteractingCountFaers,
                                    'newInteractingCountFaers',
                                    'cumInteractingCountFaers')
                            }
                        },
                        "visible": signal.fieldManagement.visibleColumns('newInteractingCountFaers')
                    }] : []);
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.newFatalCountFaers ? [
                    {
                        "mData": "newFatalCountFaers",
                        "mRender": function (data, type, row) {
                            if (typeof row.faersExecConfigId === 'undefined') {
                                row.faersExecConfigId = row.execConfigId;
                            }
                            if (hyperlinkConfiguration.newFatalCountFaers) {
                                return signal.utils.stacked(
                                    cnt_drill_down_faers(row.newFatalCountFaers, COUNTS.NEW, FLAGS.FATAL_FLAG, labelConfigKeyId["newFatalCountFaers"], row.faersExecConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, row.preferredTerm, 'NEW_FATAL',"newFatalCountFaers", row.soc, row.productSelection),
                                    cnt_drill_down_faers(row.cumFatalCountFaers, COUNTS.CUMM, FLAGS.FATAL_FLAG, labelConfigKeyId["newFatalCountFaers"], row.faersExecConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, row.preferredTerm, 'CUMM_FATAL',"cumFatalCountFaers", row.soc, row.productSelection))
                            } else {
                                return signal.utils.stackedForClass(
                                    row.newFatalCountFaers,
                                    row.cumFatalCountFaers,
                                    'newFatalCountFaers',
                                    'cumFatalCountFaers')
                            }


                        },
                        className: 'text-center',
                        "visible": signal.fieldManagement.visibleColumns('newFatalCountFaers')
                    }
                ] : []);
                if (showFaersEbgm) {
                    aoColumns.push.apply(aoColumns, labelConfigCopyJson.ebgmFaers ? [{
                        "mData": "ebgmFaers",
                        className: 'text-center',
                        "mRender": function (data, type, row) {
                            return '<span class="ebgmFaers">' + row.ebgmFaers + '</span>';
                        },
                        "visible": signal.fieldManagement.visibleColumns('ebgmFaers')
                    }] : []);
                }

                if (showFaersPrr && showFaersRor) {
                    aoColumns.push.apply(aoColumns, labelConfigCopyJson.chiSquareFaers ? [
                        {
                            "mData": "chiSquareFaers",
                            "mRender": function (data, type, row) {
                                if (row.chiSquareFaers == -1) {
                                    return '-'
                                }
                                return '<span class="CHI_SQUARE_FAERS">' + row.chiSquareFaers + '</span>';
                            },
                            'className': 'text-center',
                            "visible": signal.fieldManagement.visibleColumns('chiSquareFaers')
                        }
                    ] : []);
                }


                if (groupBySmq != "true") {
                    aoColumns.push.apply(aoColumns, labelConfigCopyJson.relatedFaers ? [{
                        "mData": "relatedFaers",
                        "className": '',
                        "mRender": function (data, type, row) {
                            return '<span>' + row.relatedFaers + '</span>'
                        },
                        "visible": signal.fieldManagement.visibleColumns('relatedFaers')
                    }] : []);
                    aoColumns.push.apply(aoColumns, labelConfigCopyJson.pregenencyFaers ? [
                        {
                            "mData": "pregenencyFaers",
                            "className": '',
                            "mRender": function (data, type, row) {
                                return '<span>' + row.pregenencyFaers + '</span>'
                            },
                            "visible": signal.fieldManagement.visibleColumns('pregenencyFaers')
                        }
                    ] : []);
                }
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.trendTypeFaers ? [{
                    "mData": "trendTypeFaers",
                    "className": 'col-min-75',
                    "mRender": function (data, type, row) {
                        return '<span>' + row.trendTypeFaers + '</span>'
                    },
                    "visible": signal.fieldManagement.visibleColumns('trendTypeFaers')
                }] : []);
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.newGeriatricCountFaers ? [{
                    "mData": "newGeriatricCountFaers",
                    "mRender": function (data, type, row) {
                        if (typeof row.faersExecConfigId === 'undefined') {
                            row.faersExecConfigId = row.execConfigId;
                        }

                        if (hyperlinkConfiguration.newGeriatricCountFaers) {
                            return signal.utils.stacked(
                                cnt_drill_down_faers(row.newGeriatricCountFaers, COUNTS.NEW, FLAGS.GERI_FLAG, labelConfigKeyId["newGeriatricCountFaers"], row.faersExecConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, row.preferredTerm, '',"newGeriatricCountFaers", row.soc, row.productSelection),
                                cnt_drill_down_faers(row.cumGeriatricCountFaers, COUNTS.CUMM, FLAGS.GERI_FLAG, labelConfigKeyId["newGeriatricCountFaers"], row.faersExecConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, row.preferredTerm, '', "cumGeriatricCountFaers", row.soc, row.productSelection))
                        } else {
                            return signal.utils.stackedForClass(
                                row.newGeriatricCountFaers,
                                row.cumGeriatricCountFaers,
                                'newGeriatricCountFaers',
                                'cumGeriatricCountFaers')
                        }
                    },
                    className: '',
                    "visible": signal.fieldManagement.visibleColumns('newGeriatricCountFaers')
                }] : []);
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.newNonSeriousFaers ? [
                    {
                        "mData": "newNonSeriousFaers",
                        "mRender": function (data, type, row) {
                            if (typeof row.faersExecConfigId === 'undefined') {
                                row.faersExecConfigId = row.execConfigId;
                            }

                            if (hyperlinkConfiguration.newNonSeriousFaers) {
                                return signal.utils.stacked(
                                    cnt_drill_down_faers(row.newNonSeriousFaers, COUNTS.NEW, FLAGS.NON_SERIOUS_FLAG, labelConfigKeyId["newNonSeriousFaers"], row.faersExecConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, row.preferredTerm, '',"newNonSeriousFaers", row.soc, row.productSelection),
                                    cnt_drill_down_faers(row.cumNonSeriousFaers, COUNTS.CUMM, FLAGS.NON_SERIOUS_FLAG, labelConfigKeyId["newNonSeriousFaers"], row.faersExecConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, row.preferredTerm, '', "cumNonSeriousFaers", row.soc, row.productSelection))
                            } else {
                                return signal.utils.stackedForClass(
                                    row.newNonSeriousFaers,
                                    row.cumNonSeriousFaers,
                                    'newNonSeriousFaers',
                                    'cumNonSeriousFaers')
                            }

                        },
                        className: '',
                        "visible": signal.fieldManagement.visibleColumns('newNonSeriousFaers')
                    }] : []);

            }

            if (isEvdasEnabled) {
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.hlgtEvdas ? [
                    {
                        "mData": "hlgtEvdas",
                        "mRender": function (data, type, row) {
                            return '<span class="hlgtEvdas">' + row.hlgtEvdas + '</span>'
                        },
                        'className': '',
                        "visible": signal.fieldManagement.visibleColumns('hlgtEvdas')
                    }] : []);
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.hltEvdas ? [
                    {
                        "mData": "hltEvdas",
                        "mRender": function (data, type, row) {
                            return '<span class="hltEvdas">' + row.hltEvdas + '</span>'
                        },
                        'className': '',
                        "visible": signal.fieldManagement.visibleColumns('hltEvdas')
                    }] : []);
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.smqNarrowEvdas ? [
                    {
                        "mData": "smqNarrowEvdas",
                        "mRender": function (data, type, row) {
                            return '<span class="smqNarrowEvdas">' + row.smqNarrowEvdas + '</span>'
                        },
                        'className': '',
                        "visible": signal.fieldManagement.visibleColumns('smqNarrowEvdas')
                    }] : []);
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.newEeaEvdas ? [
                    {
                        "mData": "newEeaEvdas",
                        "mRender": function (data, type, row) {
                            if (hyperlinkConfiguration.newEeaEvdas) {
                                return signal.utils.stacked(
                                    drillDownOptions(row.newEeaEvdas, encodeToHTML(row.productName), row.preferredTerm, row.evdasExecConfigId, 1, true, row.id, '', 'newEEAEvdas'),
                                    drillDownOptions(row.totEeaEvdas, encodeToHTML(row.productName), row.preferredTerm, row.evdasExecConfigId, 1, false, row.id, '', 'totEEAEvdas'))
                            } else {
                                return signal.utils.stackedForClass(
                                    row.newEeaEvdas,
                                    row.totEeaEvdas,
                                    'newEEAEvdas',
                                    'totEEAEvdas')
                            }

                        },
                        className: '',
                        "visible": signal.fieldManagement.visibleColumns('newEeaEvdas')
                    }] : []);
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.newHcpEvdas ? [
                    {
                        "mData": "newHcpEvdas",
                        "mRender": function (data, type, row) {

                            if (hyperlinkConfiguration.newHcpEvdas) {
                                return signal.utils.stacked(
                                    drillDownOptions(row.newHcpEvdas, encodeToHTML(row.productName), row.preferredTerm, row.evdasExecConfigId, 2, true, row.id, '', 'newHCPEvdas'),
                                    drillDownOptions(row.totHcpEvdas, encodeToHTML(row.productName), row.preferredTerm, row.evdasExecConfigId, 2, false, row.id, '', 'totHCPEvdas'))
                            } else {
                                return signal.utils.stackedForClass(
                                    row.newHcpEvdas,
                                    row.totHcpEvdas,
                                    'newHCPEvdas',
                                    'totHCPEvdas')
                            }

                        },
                        className: '',
                        "visible": signal.fieldManagement.visibleColumns('newHcpEvdas')
                    }] : []);
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.newSeriousEvdas ? [
                    {
                        "mData": "newSeriousEvdas",
                        "mRender": function (data, type, row) {

                            if (hyperlinkConfiguration.newSeriousEvdas) {
                                return signal.utils.stacked(
                                    drillDownOptions(row.newSeriousEvdas, encodeToHTML(row.productName), row.preferredTerm, row.evdasExecConfigId, 3, true, row.id, '', 'newSeriousEvdas'),
                                    drillDownOptions(row.totalSeriousEvdas, encodeToHTML(row.productName), row.preferredTerm, row.evdasExecConfigId, 3, false, row.id, '', 'totalSeriousEvdas'))
                            } else {
                                return signal.utils.stackedForClass(
                                    row.newSeriousEvdas,
                                    row.totalSeriousEvdas,
                                    'newSeriousEvdas',
                                    'totalSeriousEvdas')
                            }

                    },
                    className: '',
                    "visible": signal.fieldManagement.visibleColumns('newSeriousEvdas')
                }] : []);
            aoColumns.push.apply(aoColumns, labelConfigCopyJson.newMedErrEvdas ? [
                {
                    "mData": "newMedErrEvdas",
                    "mRender": function (data, type, row) {
                        return '<div><div class="stacked-cell-center-top"><span class="newMedErrEvdas">' + row.newMedErrEvdas + '</span></div>' +
                            '<div class="stacked-cell-center-top"><span class="totMedErrEvdas">' + row.totMedErrEvdas + '</span></div></div>'
                    },
                    className: '',
                    "visible": signal.fieldManagement.visibleColumns('newMedErrEvdas')
                }] : []);
            aoColumns.push.apply(aoColumns, labelConfigCopyJson.newObsEvdas ? [
                {
                    "mData": "newObsEvdas",
                    "mRender": function (data, type, row) {
                        return '<div><div class="stacked-cell-center-top"><span class="newObsEvdas">' + row.newObsEvdas + '</span></div>' +
                            '<div class="stacked-cell-center-top"><span class="totObsEvdas">' + row.totObsEvdas + '</span></div></div>'
                    },
                    className: '',
                    "visible": signal.fieldManagement.visibleColumns('newObsEvdas')
                }] : []);
            aoColumns.push.apply(aoColumns, labelConfigCopyJson.newFatalEvdas ? [
                {
                    "mData": "newFatalEvdas",
                    "mRender": function (data, type, row) {
                        if (hyperlinkConfiguration.newFatalEvdas) {
                            return signal.utils.stacked(
                                drillDownOptions(row.newFatalEvdas, encodeToHTML(row.productName), row.preferredTerm, row.evdasExecConfigId, 4, true, row.id, '', 'newFatalEvdas'),
                                drillDownOptions(row.totalFatalEvdas, encodeToHTML(row.productName), row.preferredTerm, row.evdasExecConfigId, 4, false, row.id, '', 'totalFatalEvdas'))
                        } else {
                            return signal.utils.stackedForClass(
                                row.newFatalEvdas,
                                row.totalFatalEvdas,
                                'newFatalEvdas',
                                'totalFatalEvdas')
                        }
                    },
                    className: '',
                    "visible": signal.fieldManagement.visibleColumns('newFatalEvdas')
                }] : []);
            aoColumns.push.apply(aoColumns, labelConfigCopyJson.newRcEvdas ? [
                {
                    "mData": "newRcEvdas",
                    "mRender": function (data, type, row) {
                        return '<div><div class="stacked-cell-center-top"><span class="newRcEvdas">' + row.newRcEvdas + '</span></div>' +
                            '<div class="stacked-cell-center-top"><span class="totRcEvdas">' + row.newRcEvdas + '</span></div></div>'
                    },
                    className: '',
                    "visible": signal.fieldManagement.visibleColumns('newRcEvdas')
                }] : []);
            aoColumns.push.apply(aoColumns, labelConfigCopyJson.newLitEvdas ? [
                {
                    "mData": "newLitEvdas",
                    "mRender": function (data, type, row) {
                        return '<div><div class="stacked-cell-center-top"><span class="newLitEvdas">' + row.newLitEvdas + '</span></div>' +
                            '<div class="stacked-cell-center-top"><span class="totalLitEvdas">' + row.totalLitEvdas + '</span></div></div>'
                    },
                    className: '',
                    "visible": signal.fieldManagement.visibleColumns('newLitEvdas')
                }] : []);
            aoColumns.push.apply(aoColumns, labelConfigCopyJson.newPaedEvdas ? [
                {
                    "mData": "newPaedEvdas",
                    "mRender": function (data, type, row) {
                        if (hyperlinkConfiguration.newPaedEvdas) {
                            return signal.utils.stacked(
                                drillDownOptions(row.newPaedEvdas, encodeToHTML(row.productName), row.preferredTerm, row.evdasExecConfigId, 8, true, row.id, '', 'newPaedEvdas'),
                                drillDownOptions(row.totPaedEvdas, encodeToHTML(row.productName), row.preferredTerm, row.evdasExecConfigId, 8, false, row.id, '', 'totalPaedEvdas'))
                        } else {
                            return signal.utils.stackedForClass(
                                row.newPaedEvdas,
                                row.totPaedEvdas,
                                'newPaedEvdas',
                                'totalPaedEvdas')
                        }

                        },
                        className: '',
                        "visible": signal.fieldManagement.visibleColumns('newPaedEvdas')
                    }] : []);
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.ratioRorPaedVsOthersEvdas ? [
                    {
                        "mData": "ratioRorPaedVsOthersEvdas",
                        'className': '',
                        "mRender": function (data, type, row) {
                            return '<span class="ratioRorPaedVsOthersEvdas">' + row.ratioRorPaedVsOthersEvdas + '</span>'
                        },
                        "visible": signal.fieldManagement.visibleColumns('ratioRorPaedVsOthersEvdas')
                    }] : []);
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.newGeriaEvdas ? [
                    {
                        "mData": "newGeriaEvdas",
                        "mRender": function (data, type, row) {

                            if (hyperlinkConfiguration.newGeriaEvdas) {
                                return signal.utils.stacked(
                                    drillDownOptions(row.newGeriaEvdas, encodeToHTML(row.productName), row.preferredTerm, row.evdasExecConfigId, 7, true, row.id, '', 'newGeriatEvdas'),
                                    drillDownOptions(row.totGeriaEvdas, encodeToHTML(row.productName), row.preferredTerm, row.evdasExecConfigId, 7, false, row.id, '', 'totalGeriatEvdas'))
                            } else {
                                return signal.utils.stackedForClass(
                                    row.newGeriaEvdas,
                                    row.totGeriaEvdas,
                                    'newGeriatEvdas',
                                    'totalGeriatEvdas')
                            }
                        },
                        className: '',
                        "visible": signal.fieldManagement.visibleColumns('newGeriaEvdas')
                    }] : []);
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.ratioRorGeriatrVsOthersEvdas ? [
                    {
                        "mData": "ratioRorGeriatrVsOthersEvdas",
                        'className': '',
                        "mRender": function (data, type, row) {
                            return '<span class="ratioRorGeriatrVsOthersEvdas">' + row.ratioRorGeriatrVsOthersEvdas + '</span>'
                        },
                        "visible": signal.fieldManagement.visibleColumns('ratioRorGeriatrVsOthersEvdas')
                    }] : []);
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.sdrGeratrEvdas ? [
                    {
                        "mData": "sdrGeratrEvdas",
                        'className': '',
                        "mRender": function (data, type, row) {
                            return '<span class="sdrGeratrEvdas">' + row.sdrGeratrEvdas + '</span>'
                        },
                        "visible": signal.fieldManagement.visibleColumns('sdrGeratrEvdas')
                    }] : []);
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.newSpontEvdas ? [
                    {
                        "mData": "newSpontEvdas",
                        "mRender": function (data, type, row) {

                            if (hyperlinkConfiguration.newSpontEvdas) {
                                return signal.utils.stacked(
                                    drillDownOptions(row.newSpontEvdas, encodeToHTML(row.productName), row.preferredTerm, row.evdasExecConfigId, 6, true, row.id, '', 'newSponEvdas'),
                                    drillDownOptions(row.totSpontEvdas, encodeToHTML(row.productName), row.preferredTerm, row.evdasExecConfigId, 6, false, row.id, '', 'totSpontEvdas'))
                            } else {
                                return signal.utils.stackedForClass(
                                    row.newSpontEvdas,
                                    row.totSpontEvdas,
                                    'newSponEvdas',
                                    'totSpontEvdas')
                            }

                        },
                        className: '',
                        "visible": signal.fieldManagement.visibleColumns('newSpontEvdas')
                    }] : []);
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.totSpontEuropeEvdas ? [
                    {
                        "mData": "totSpontEuropeEvdas",
                        'className': 'text-center',
                        "mRender": function (data, type, row) {
                            return '<span class="totSpontEuropeEvdas">' + row.totSpontEuropeEvdas + '</span>'
                        },
                        "visible": signal.fieldManagement.visibleColumns('totSpontEuropeEvdas')
                    }] : []);
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.totSpontNAmericaEvdas ? [
                    {
                        "mData": "totSpontNAmericaEvdas",
                        'className': 'text-center',
                        "mRender": function (data, type, row) {
                            return '<span class="totSpontNAmericaEvdas">' + row.totSpontNAmericaEvdas + '</span>'
                        },
                        "visible": signal.fieldManagement.visibleColumns('totSpontNAmericaEvdas')
                    }] : []);
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.totSpontJapanEvdas ? [
                    {
                        "mData": "totSpontJapanEvdas",
                        'className': 'text-center',
                        "mRender": function (data, type, row) {
                            return '<span class="totSpontJapanEvdas">' + row.totSpontJapanEvdas + '</span>'
                        },
                        "visible": signal.fieldManagement.visibleColumns('totSpontJapanEvdas')
                    }] : []);
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.totSpontAsiaEvdas ? [
                    {
                        "mData": "totSpontAsiaEvdas",
                        'className': 'text-center',
                        "mRender": function (data, type, row) {
                            return '<span class="SPON_ASIA_EVDAS">' + row.totSpontAsiaEvdas + '</span>'
                        },
                        "visible": signal.fieldManagement.visibleColumns('totSpontAsiaEvdas')
                    }] : []);
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.totSpontRestEvdas ? [
                    {
                        "mData": "totSpontRestEvdas",
                        'className': 'text-center',
                        "mRender": function (data, type, row) {
                            return '<span class="totSpontRestEvdas">' + row.totSpontRestEvdas + '</span>'
                        },
                        "visible": signal.fieldManagement.visibleColumns('totSpontRestEvdas')
                    }] : []);
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.sdrPaedEvdas ? [
                    {
                        "mData": "sdrPaedEvdas",
                        'className': '',
                        "mRender": function (data, type, row) {
                            return '<span class="sdrPaedEvdas">' + row.sdrPaedEvdas + '</span>'
                        },
                        "visible": signal.fieldManagement.visibleColumns('sdrPaedEvdas')
                    }] : []);
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.europeRorEvdas ? [
                    {
                        "mData": "europeRorEvdas",
                        'className': 'text-center',
                        "mRender": function (data, type, row) {
                            return '<span class="rorEuropeEvdas">' + row.europeRorEvdas + '</span>'
                        },
                        "visible": signal.fieldManagement.visibleColumns('europeRorEvdas')
                    }] : []);
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.northAmericaRorEvdas ? [
                    {
                        "mData": "northAmericaRorEvdas",
                        'className': 'text-center',
                        "mRender": function (data, type, row) {
                            return '<span class="rorNAmericaEvdas">' + row.northAmericaRorEvdas + '</span>'
                        },
                        "visible": signal.fieldManagement.visibleColumns('northAmericaRorEvdas')
                    }] : []);
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.japanRorEvdas ? [
                    {
                        "mData": "japanRorEvdas",
                        'className': 'text-center',
                        "mRender": function (data, type, row) {
                            return '<span class="rorJapanEvdas">' + row.japanRorEvdas + '</span>'
                        },
                        "visible": signal.fieldManagement.visibleColumns('japanRorEvdas')
                    }] : []);
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.asiaRorEvdas ? [
                    {
                        "mData": "asiaRorEvdas",
                        'className': 'text-center',
                        "mRender": function (data, type, row) {
                            return '<span class="rorAsiaEvdas">' + row.asiaRorEvdas + '</span>'
                        },
                        "visible": signal.fieldManagement.visibleColumns('asiaRorEvdas')
                    }] : []);
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.restRorEvdas ? [
                    {
                        "mData": "restRorEvdas",
                        'className': 'text-center',
                        "mRender": function (data, type, row) {
                            return '<span class="rorRestEvdas">' + row.restRorEvdas + '</span>'
                        },
                        "visible": signal.fieldManagement.visibleColumns('restRorEvdas')
                    }] : []);
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.changesEvdas ? [
                    {
                        "mData": "changesEvdas",
                        'className': '',
                        "mRender": function (data, type, row) {
                            return '<span class="changesEvdas">' + row.changesEvdas + '</span>'
                        },
                        "visible": signal.fieldManagement.visibleColumns('changesEvdas')
                    }
                ] : []);
            }

            if (isVaersEnabled) {

                aoColumns.push.apply(aoColumns, labelConfigCopyJson.newCountVaers ? [
                    {
                        "mData": "newCountVaers",
                        "mRender": function (data, type, row) {
                            if (typeof row.vaersExecConfigId === 'undefined') {
                                row.vaersExecConfigId = row.execConfigId;
                            }
                            if (hyperlinkConfiguration.newCountVaers) {
                                return signal.utils.stacked(
                                    cnt_drill_down_vaers(row.newCountVaers, COUNTS.NEW, FLAGS.CUMM_FLAG, labelConfigKeyId["newCountVaers"], row.vaersExecConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, row.preferredTerm, 'NEW_COUNT', "newCountVaers", row.soc, row.productSelection),
                                    cnt_drill_down_vaers(row.cummCountVaers, COUNTS.CUMM, FLAGS.CUMM_FLAG, labelConfigKeyId["newCountVaers"], row.vaersExecConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, row.preferredTerm, 'CUMM_COUNT', "cumCountVaers", row.soc, row.productSelection))
                            } else {
                                return signal.utils.stackedForClass(
                                    row.newCountVaers,
                                    row.cummCountVaers,
                                    'newCountVaers',
                                    'cumCountVaers')
                            }
                        },
                        className: '',
                        "visible": signal.fieldManagement.visibleColumns('newCountVaers')
                    }] : []);
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.newSeriousCountVaers ? [
                    {
                        "mData": "newSeriousCountVaers",
                        "mRender": function (data, type, row) {
                            if (typeof row.vaersExecConfigId === 'undefined') {
                                row.vaersExecConfigId = row.execConfigId;
                            }
                            if (hyperlinkConfiguration.newSeriousCountVaers) {
                                return signal.utils.stacked(
                                    cnt_drill_down_vaers(row.newSeriousCountVaers, COUNTS.NEW, FLAGS.SERIOUS_FLAG, labelConfigKeyId["newSeriousCountVaers"], row.vaersExecConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, row.preferredTerm, 'NEW_SER',"newSeriousCountVaers", row.soc, row.productSelection),
                                    cnt_drill_down_vaers(row.cumSeriousCountVaers, COUNTS.CUMM, FLAGS.SERIOUS_FLAG, labelConfigKeyId["newSeriousCountVaers"], row.vaersExecConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, row.preferredTerm, 'CUMM_SER',"cumSeriousCountVaers", row.soc, row.productSelection))
                            } else {
                                return signal.utils.stackedForClass(
                                    row.newSeriousCountVaers,
                                    row.cumSeriousCountVaers,
                                    'newSeriousCountVaers',
                                    'cumSeriousCountVaers')
                            }
                        },
                        className: '',
                        "visible": signal.fieldManagement.visibleColumns('newSeriousCountVaers')
                    }] : []);
                if (showVaersEbgm) {
                    aoColumns.push.apply(aoColumns, labelConfigCopyJson.eb05Vaers ? [
                        {
                            "mData": "eb05Vaers",
                            "mRender": function (data, type, row) {
                                return signal.utils.stacked(
                                    '<span class="eb05Vaers">' + row.eb05Vaers + '</span>',
                                    '<span class="eb95Vaers">' + row.eb95Vaers + '</span>'
                                );
                            },
                            className: 'text-center',
                            "visible": signal.fieldManagement.visibleColumns('eb05Vaers')
                        }
                    ] : []);
                }

                aoColumns.push.apply(aoColumns, labelConfigCopyJson.newFatalCountVaers ? [
                    {
                        "mData": "newFatalCountVaers",
                        "mRender": function (data, type, row) {
                            if (typeof row.vaersExecConfigId === 'undefined') {
                                row.vaersExecConfigId = row.execConfigId;
                            }

                            if (hyperlinkConfiguration.newFatalCountVaers) {
                                return signal.utils.stacked(
                                    cnt_drill_down_vaers(row.newFatalCountVaers, COUNTS.NEW, FLAGS.FATAL_FLAG, labelConfigKeyId["newFatalCountVaers"], row.vaersExecConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, row.preferredTerm, 'NEW_FATAL', "newFatalCountVaers", row.soc, row.productSelection),
                                    cnt_drill_down_vaers(row.cumFatalCountVaers, COUNTS.CUMM, FLAGS.FATAL_FLAG, labelConfigKeyId["newFatalCountVaers"], row.vaersExecConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, row.preferredTerm, 'CUMM_FATAL', "cumFatalCountVaers", row.soc, row.productSelection))
                            } else {
                                return signal.utils.stackedForClass(
                                    row.newFatalCountVaers,
                                    row.cumFatalCountVaers,
                                    'newFatalCountVaers',
                                    'cumFatalCountVaers')
                            }
                        },
                        className: '',
                        "visible": signal.fieldManagement.visibleColumns('newFatalCountVaers')
                    }] : []);
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.newGeriatricCountVaers ? [
                    {
                        "mData": "newGeriatricCountVaers",
                        "mRender": function (data, type, row) {
                            if (typeof row.vaersExecConfigId === 'undefined') {
                                row.vaersExecConfigId = row.execConfigId;
                            }

                            if (hyperlinkConfiguration.newGeriatricCountVaers) {
                                return signal.utils.stacked(
                                    cnt_drill_down_vaers(row.newGeriatricCountVaers, COUNTS.NEW, FLAGS.GERI_FLAG, labelConfigKeyId["newGeriatricCountVaers"], row.vaersExecConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, row.preferredTerm, '', "newGeriatricCountVaers", row.soc, row.productSelection),
                                    cnt_drill_down_vaers(row.cumGeriatricCountVaers, COUNTS.CUMM, FLAGS.GERI_FLAG, labelConfigKeyId["newGeriatricCountVaers"], row.vaersExecConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, row.preferredTerm, '',"cumGeriatricCountVaers", row.soc, row.productSelection))
                            } else {
                                return signal.utils.stackedForClass(
                                    row.newGeriatricCountVaers,
                                    row.cumGeriatricCountVaers,
                                    'newGeriatricCountVaers',
                                    'cumGeriatricCountVaers')
                            }
                        },
                        className: '',
                        "visible": signal.fieldManagement.visibleColumns('newGeriatricCountVaers')
                    }] : []);
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.newPediatricCountVaers ? [
                    {
                        "mData": "newPediatricCountVaers",
                        "className": '',
                        "mRender": function (data, type, row) {
                            if (typeof row.vaersExecConfigId === 'undefined') {
                                row.vaersExecConfigId = row.execConfigId;
                            }
                            if (hyperlinkConfiguration.newPediatricCountVaers) {
                                return signal.utils.stacked(
                                    cnt_drill_down_vaers(row.newPediatricCountVaers, COUNTS.NEW, FLAGS.PEDI_FLAG, labelConfigKeyId["newPediatricCountVaers"], row.vaersExecConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, row.preferredTerm, 'NEW_PEDIA_COUNT', "newPediatricCountVaers", row.soc, row.productSelection),
                                    cnt_drill_down_vaers(row.cummPediatricCountVaers, COUNTS.CUMM, FLAGS.PEDI_FLAG, labelConfigKeyId["newPediatricCountVaers"], row.vaersExecConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, row.preferredTerm, 'CUMM_PEDIA_COUNT',"cumPediatricCountVaers", row.soc, row.productSelection))
                            } else {
                                return signal.utils.stackedForClass(
                                    row.newPediatricCountVaers,
                                    row.cummPediatricCountVaers,
                                    'newPediatricCountVaers',
                                    'cumPediatricCountVaers')
                            }
                        },
                        "visible": signal.fieldManagement.visibleColumns('newPediatricCountVaers')
                    }] : []);

                if (showVaersPrr) {
                    aoColumns.push.apply(aoColumns, labelConfigCopyJson.prrValueVaers ? [
                        {
                            "mData": "prrValueVaers",
                            "className": 'text-center',
                            "mRender": function (data, type, row) {
                                if (row.prrValueVaers == -1) {
                                    return '<span>' + '-' + '</span>'
                                }
                                return '<span class="prrValueVaers">' + row.prrValueVaers + '</span>';
                            },
                            "visible": signal.fieldManagement.visibleColumns('prrValueVaers')
                        }] : []);
                }

                if (showVaersRor) {
                    aoColumns.push.apply(aoColumns, labelConfigCopyJson.rorValueVaers ? [
                        {
                            "mData": "rorValueVaers",
                            className: 'text-center',
                            "mRender": function (data, type, row) {
                                if (row.rorValueVaers == -1) {
                                    return '<span>' + '-' + '</span>'
                                }
                                return '<span class="rorValueVaers">' + row.rorValueVaers + '</span>';
                            },
                            "visible": signal.fieldManagement.visibleColumns('rorValueVaers')
                        }] : []);
                }

                if (showVaersEbgm) {
                    aoColumns.push.apply(aoColumns, labelConfigCopyJson.ebgmVaers ? [
                        {
                            "mData": "ebgmVaers",
                            className: 'text-center',
                            "mRender": function (data, type, row) {
                                return '<span class="ebgmVaers">' + row.ebgmVaers + '</span>';
                            },
                            "visible": signal.fieldManagement.visibleColumns('ebgmVaers')
                        }] : []);
                }

                if (showVaersPrr) {
                    aoColumns.push.apply(aoColumns, labelConfigCopyJson.prrLCIVaers ? [
                        {
                            "mData": "prrLCIVaers",
                            "mRender": function (data, type, row) {
                                return signal.utils.stacked(
                                    '<span class="prrLCIVaers">' + row.prrLCIVaers + '</span>',
                                    '<span class="prrUCIVaers">' + row.prrUCIVaers + '</span>'
                                )
                            },
                            className: 'col-min-110',
                            "visible": signal.fieldManagement.visibleColumns('prrLCIVaers')
                        }] : []);
                }

                if (showVaersRor) {
                    aoColumns.push.apply(aoColumns, labelConfigCopyJson.rorLCIVaers ? [
                        {
                            "mData": "rorLCIVaers",
                            "mRender": function (data, type, row) {
                                return signal.utils.stacked(
                                    '<span class="rorLCIVaers">' + row.rorLCIVaers + '</span>',
                                    '<span class="rorUCIVaers">' + row.rorUCIVaers + '</span>'
                                )
                            },
                            className: '',
                            "visible": signal.fieldManagement.visibleColumns('rorLCIVaers')
                        }] : []);
                }

                if (showVaersPrr && showVaersRor) {
                    aoColumns.push.apply(aoColumns, labelConfigCopyJson.chiSquareVaers ? [
                        {
                            "mData": "chiSquareVaers",
                            "mRender": function (data, type, row) {
                                if (row.chiSquareVaers == -1) {
                                    return '-'
                                }
                                return '<span class="chiSquareVaers">' + row.chiSquareVaers + '</span>';
                            },
                            'className': 'text-center',
                            "visible": signal.fieldManagement.visibleColumns('chiSquareVaers')
                        }
                    ] : []);
                }
            }
            if (isVigibaseEnabled) {

                aoColumns.push.apply(aoColumns, labelConfigCopyJson.newCountVigibase ? [
                    {
                        "mData": "newCountVigibase",
                        "mRender": function (data, type, row) {
                            if (typeof row.vigibaseExecConfigId === 'undefined') {
                                row.vigibaseExecConfigId = row.execConfigId;
                            }
                            if (hyperlinkConfiguration.newCountVigibase) {
                                return signal.utils.stacked(
                                    cnt_drill_down_faers(row.newCountVigibase, COUNTS.NEW, FLAGS.CUMM_FLAG, labelConfigKeyId["newCountVigibase"], row.vigibaseExecConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, row.preferredTerm, 'NEW_COUNT', "newCountVigibase", row.soc, row.productSelection),
                                    cnt_drill_down_faers(row.cummCountVigibase, COUNTS.CUMM, FLAGS.CUMM_FLAG, labelConfigKeyId["newCountVigibase"], row.vigibaseExecConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, row.preferredTerm, 'CUMM_COUNT',"cumCountVigibase", row.soc, row.productSelection))
                            } else {
                                return signal.utils.stackedForClass(
                                    row.newCountVigibase,
                                    row.cummCountVigibase,
                                    'newCountVigibase',
                                    'cumCountVigibase')
                            }

                        },
                        className: '',
                        "visible": signal.fieldManagement.visibleColumns('newCountVigibase')
                    }] : []);
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.newSeriousCountVigibase ? [
                    {
                        "mData": "newSeriousCountVigibase",
                        "mRender": function (data, type, row) {
                            if (typeof row.vigibaseExecConfigId === 'undefined') {
                                row.vigibaseExecConfigId = row.execConfigId;
                            }
                            if (hyperlinkConfiguration.newSeriousCountVigibase) {
                                return signal.utils.stacked(
                                    cnt_drill_down_faers(row.newSeriousCountVigibase, COUNTS.NEW, FLAGS.SERIOUS_FLAG, labelConfigKeyId["newSeriousCountVigibase"], row.vigibaseExecConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, row.preferredTerm, 'NEW_SER', "newSeriousCountVigibase", row.soc, row.productSelection),
                                    cnt_drill_down_faers(row.cumSeriousCountVigibase, COUNTS.CUMM, FLAGS.SERIOUS_FLAG, labelConfigKeyId["newSeriousCountVigibase"], row.vigibaseExecConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, row.preferredTerm, 'CUMM_SER',"cumSeriousCountVigibase", row.soc, row.productSelection))
                            } else {
                                return signal.utils.stackedForClass(
                                    row.newSeriousCountVigibase,
                                    row.cumSeriousCountVigibase,
                                    'newSeriousCountVigibase',
                                    'cumSeriousCountVigibase')
                            }
                        },
                        className: '',
                        "visible": signal.fieldManagement.visibleColumns('newSeriousCountVigibase')
                    }] : []);
                if (showVigibaseEbgm) {
                    aoColumns.push.apply(aoColumns, labelConfigCopyJson.eb05Vigibase ? [
                        {
                            "mData": "eb05Vigibase",
                            "mRender": function (data, type, row) {
                                return signal.utils.stacked(
                                    '<span class="eb05Vigibase">' + row.eb05Vigibase + '</span>',
                                    '<span class="eb95Vigibase">' + row.eb95Vigibase + '</span>'
                                );
                            },
                            className: '',
                            "visible": signal.fieldManagement.visibleColumns('eb05Vigibase')
                        }] : []);
                }

                aoColumns.push.apply(aoColumns, labelConfigCopyJson.newFatalCountVigibase ? [
                    {
                        "mData": "newFatalCountVigibase",
                        "mRender": function (data, type, row) {
                            if (typeof row.vigibaseExecConfigId === 'undefined') {
                                row.vigibaseExecConfigId = row.execConfigId;
                            }
                            if (hyperlinkConfiguration.newFatalCountVigibase) {
                                return signal.utils.stacked(
                                    cnt_drill_down_faers(row.newFatalCountVigibase, COUNTS.NEW, FLAGS.FATAL_FLAG, labelConfigKeyId["newFatalCountVigibase"], row.vigibaseExecConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, row.preferredTerm, 'NEW_FATAL', "newFatalCountVigibase", row.soc, row.productSelection),
                                    cnt_drill_down_faers(row.cumFatalCountVigibase, COUNTS.CUMM, FLAGS.FATAL_FLAG, labelConfigKeyId["newFatalCountVigibase"], row.vigibaseExecConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, row.preferredTerm, 'CUMM_FATAL', "cumFatalCountVigibase", row.soc, row.productSelection))
                            } else {
                                return signal.utils.stackedForClass(
                                    row.newFatalCountVigibase,
                                    row.cumFatalCountVigibase,
                                    'newFatalCountVigibase',
                                    'cumFatalCountVigibase')
                            }

                        },
                        className: '',
                        "visible": signal.fieldManagement.visibleColumns('newFatalCountVigibase')
                    }] : []);
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.newGeriatricCountVigibase ? [
                    {
                        "mData": "newGeriatricCountVigibase",
                        "mRender": function (data, type, row) {
                            if (typeof row.vigibaseExecConfigId === 'undefined') {
                                row.vigibaseExecConfigId = row.execConfigId;
                            }
                            if (hyperlinkConfiguration.newGeriatricCountVigibase) {
                                return signal.utils.stacked(
                                    cnt_drill_down_faers(row.newGeriatricCountVigibase, COUNTS.NEW, FLAGS.GERI_FLAG, labelConfigKeyId["newGeriatricCountVigibase"], row.vigibaseExecConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, row.preferredTerm, '',"newGeriatricCountVigibase", row.soc, row.productSelection),
                                    cnt_drill_down_faers(row.cumGeriatricCountVigibase, COUNTS.CUMM, FLAGS.GERI_FLAG, labelConfigKeyId["newGeriatricCountVigibase"], row.vigibaseExecConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, row.preferredTerm, '', "cumGeriatricCountVigibase", row.soc, row.productSelection))
                            } else {
                                return signal.utils.stackedForClass(
                                    row.newGeriatricCountVigibase,
                                    row.cumGeriatricCountVigibase,
                                    'newGeriatricCountVigibase',
                                    'cumGeriatricCountVigibase')
                            }

                        },
                        className: '',
                        "visible": signal.fieldManagement.visibleColumns('newGeriatricCountVigibase')
                    }] : []);
                aoColumns.push.apply(aoColumns, labelConfigCopyJson.newPediatricCountVigibase ? [
                    {
                        "mData": "newPediatricCountVigibase",
                        "className": '',
                        "mRender": function (data, type, row) {
                            if (typeof row.vigibaseExecConfigId === 'undefined') {
                                row.vigibaseExecConfigId = row.execConfigId;
                            }

                            if (hyperlinkConfiguration.newPediatricCountVigibase) {
                                return signal.utils.stacked(
                                    cnt_drill_down_faers(row.newPediatricCountVigibase, COUNTS.NEW, FLAGS.PEDI_FLAG, labelConfigKeyId["newPediatricCountVigibase"], row.vigibaseExecConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, row.preferredTerm, 'NEW_PEDIA_COUNT', "newPediatricCountVigibase", row.soc, row.productSelection),
                                    cnt_drill_down_faers(row.cummPediatricCountVigibase, COUNTS.CUMM, FLAGS.PEDI_FLAG, labelConfigKeyId["newPediatricCountVigibase"], row.vigibaseExecConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, row.preferredTerm, 'CUMM_PEDIA_COUNT', "cumPediatricCountVigibase", row.soc, row.productSelection))
                            } else {
                                return signal.utils.stackedForClass(
                                    row.newPediatricCountVigibase,
                                    row.cummPediatricCountVigibase,
                                    'newPediatricCountVigibase',
                                    'cumPediatricCountVigibase')
                            }
                        },
                        "visible": signal.fieldManagement.visibleColumns('newPediatricCountVigibase')
                    }] : []);

                if (showVigibasePrr) {
                    aoColumns.push.apply(aoColumns, labelConfigCopyJson.prrValueVigibase ? [
                        {
                            "mData": "prrValueVigibase",
                            "className": 'text-center',
                            "mRender": function (data, type, row) {
                                if (row.prrValueVigibase == -1) {
                                    return '<span>' + '-' + '</span>'
                                }
                                return '<span class="prrValueVigibase">' + row.prrValueVigibase + '</span>';
                            },
                            "visible": signal.fieldManagement.visibleColumns('prrValueVigibase')
                        }] : []);
                }

                if (showVigibaseRor) {
                    aoColumns.push.apply(aoColumns, labelConfigCopyJson.rorValueVigibase ? [
                        {
                            "mData": "rorValueVigibase",
                            className: 'text-center',
                            "mRender": function (data, type, row) {
                                if (row.rorValueVigibase == -1) {
                                    return '<span>' + '-' + '</span>'
                                }
                                return '<span class="rorValueVigibase">' + row.rorValueVigibase + '</span>';
                            },
                            "visible": signal.fieldManagement.visibleColumns('rorValueVigibase')
                        }] : []);
                }

                if (showVigibaseEbgm) {
                    aoColumns.push.apply(aoColumns, labelConfigCopyJson.ebgmVigibase ? [
                        {
                            "mData": "ebgmVigibase",
                            className: 'text-center',
                            "mRender": function (data, type, row) {
                                return '<span class="ebgmVigibase">' + row.ebgmVigibase + '</span>';
                            },
                            "visible": signal.fieldManagement.visibleColumns('ebgmVigibase')
                        }] : []);
                }

                if (showVigibasePrr) {
                    aoColumns.push.apply(aoColumns, labelConfigCopyJson.prrLCIVigibase ? [
                        {
                            "mData": "prrLCIVigibase",
                            "mRender": function (data, type, row) {
                                return signal.utils.stacked(
                                    '<span class="prrLCIVigibase">' + row.prrLCIVigibase + '</span>',
                                    '<span class="prrUCIVigibase">' + row.prrUCIVigibase + '</span>'
                                )
                            },
                            className: 'col-min-120 text-center',
                            "visible": signal.fieldManagement.visibleColumns('prrLCIVigibase')
                        }] : []);
                }

                if (showVigibaseRor) {
                    aoColumns.push.apply(aoColumns, labelConfigCopyJson.rorLCIVigibase ? [
                        {
                            "mData": "rorLCIVigibase",
                            "mRender": function (data, type, row) {
                                return signal.utils.stacked(
                                    '<span class="rorLCIVigibase">' + row.rorLCIVigibase + '</span>',
                                    '<span class="rorUCIVigibase">' + row.rorUCIVigibase + '</span>'
                                )
                            },
                            className: 'text-center',
                            "visible": signal.fieldManagement.visibleColumns('rorLCIVigibase')
                        }] : []);
                }

                if (showVigibasePrr && showVigibaseRor) {
                    aoColumns.push.apply(aoColumns, labelConfigCopyJson.chiSquareVigibase ? [
                        {
                            "mData": "chiSquareVigibase",
                            "mRender": function (data, type, row) {
                                if (row.chiSquareVigibase == -1) {
                                    return '-'
                                }
                                return '<span class="chiSquareVigibase">' + row.chiSquareVigibase + '</span>';
                            },
                            'className': 'text-center',
                            "visible": signal.fieldManagement.visibleColumns('chiSquareVigibase')
                        }
                    ] : []);
                }

            }
            var labelConfigNew = JSON.parse($("#labelConfigJson").val());
            for (var i = 0; i < labelConfigNew.length; i++) {
                if (JSON.parse(labelConfigNew[i].enabled)) {
                    let tempName = labelConfigNew[i].name;
                    let isHyperLink = labelConfigNew[i].isHyperLink;
                    if (tempName !== "hlt" && tempName !== "hlgt" && tempName !== "smqNarrow") {
                        aoColumns.push.apply(aoColumns, [
                            {
                                "mData": labelConfigNew[i].name,
                                "mRender": function (data, type, row) {
                                    if (tempName !== "hlt" && tempName !== "hlgt" && tempName !== "smqNarrow" && typeof row[tempName] !== "undefined" && row[tempName] !== "-") {
                                        var newC, cumC, name;
                                        if (JSON.parse(row[tempName]).new == null) {
                                            newC = "-"
                                        } else {
                                            newC = JSON.parse(row[tempName]).new;
                                        }
                                        if (JSON.parse(row[tempName]).cum == null) {
                                            cumC = "-"
                                        } else {
                                            cumC = JSON.parse(row[tempName]).cum;
                                        }
                                        name = JSON.parse(row[tempName]).name.toString();
                                        if (isHyperLink) {
                                            return signal.utils.stacked(
                                                cnt_drill_down(newC, COUNTS.NEW, labelConfigKeyId[name], labelConfigKeyId[name], row.execConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, COUNTS.NEW + "_" + name.toUpperCase(), row.groupBySmq, row.preferredTerm, row.soc, row.productSelection,COUNTS.NEW + "_" + name.toUpperCase()),
                                                cnt_drill_down(cumC, COUNTS.CUMM, labelConfigKeyId[name], labelConfigKeyId[name], row.execConfigId, row.id, row.productId, encodeToHTML(row.productName), row.ptCode, COUNTS.CUMM + "_" + name.toUpperCase(), row.groupBySmq, row.preferredTerm, row.soc, row.productSelection,COUNTS.CUMM + "_" + name.toUpperCase()));

                                        } else {
                                            return signal.utils.stacked(
                                                newC,
                                                cumC);
                                        }
                                    } else if ((tempName !== "hlt" && tempName !== "hlgt" && tempName !== "smqNarrow") && (typeof row[tempName] === "undefined" || row[tempName] == null || row[tempName] == "null" || row[tempName] == "-")) {
                                        return '<span>-</span>';
                                    }
                                },
                                className: 'text-center',
                                "visible": signal.fieldManagement.visibleColumns(labelConfigNew[i].name)
                            }
                        ]);
                    }
                    if (JSON.parse(groupBySmq) == false && (tempName == "hlt" || tempName == "hlgt" || tempName == "smqNarrow")) {
                        aoColumns.push.apply(aoColumns, [
                            {
                                "mData": labelConfigNew[i].name,
                                "mRender": function (data, type, row) {
                                    if (typeof row[tempName] === "undefined" || row[tempName] == null || row[tempName] == "null" || row[tempName] == "-") {
                                        return '<span>-</span>';
                                    } else {
                                        var colElement = '<div class="col-container"><div class="col-height">';
                                        colElement += row[tempName];
                                        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(row[tempName]) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                                        colElement += '</div></div>';
                                        return colElement;
                                    }
                                },
                                className: '',
                                "visible": signal.fieldManagement.visibleColumns(labelConfigNew[i].name)
                            }
                        ]);
                    }
                }

            }

            if (isAgeEnabled) {
                $.each(subGroupsMap.AGE_GROUP, function (key, value) {
                    aoColumns.push.apply(aoColumns, labelConfigCopyJson["ebgm" + value] ? makeSubGroupColumns("ebgm", value, "ebgmAge") : []);
                    aoColumns.push.apply(aoColumns, labelConfigCopyJson["eb05" + value] ? makeSubGroupColumns("eb05", value, "eb05Age") : []);
                    aoColumns.push.apply(aoColumns, labelConfigCopyJson["eb95" + value] ? makeSubGroupColumns("eb95", value, "eb95Age") : []);
                });
            }
            if (isGenderEnabled) {
                $.each(subGroupsMap.GENDER, function (key, value) {
                    aoColumns.push.apply(aoColumns, labelConfigCopyJson["ebgm" + value] ? makeSubGroupColumns("ebgm", value, "ebgmGender") : []);
                    aoColumns.push.apply(aoColumns, labelConfigCopyJson["eb05" + value] ? makeSubGroupColumns("eb05", value, "eb05Gender") : []);
                    aoColumns.push.apply(aoColumns, labelConfigCopyJson["eb95" + value] ? makeSubGroupColumns("eb95", value, "eb95Gender") : []);
                });
            }
            if (allSubGroupMap && allSubGroupMap !== []) {
                $.each(allSubGroupMap, function (key, map) {
                    $.each(map, function (subGroup, value) {
                        $.each(value, function (index, column) {
                            aoColumns.push.apply(aoColumns, labelConfigCopyJson[key + column] ? makeAllSubGroupColumns(key, column, subGroup) : []);
                        });
                    });
                });
            }
            if (rorRelSubGrpEnabled && allSubGroupMap && allSubGroupMap !== []) {
                $.each(allSubGroupMap, function (key, map) {
                    if (key.toLowerCase().startsWith('ror')) {
                        $.each(map, function (subGroup, value) {
                            $.each(value, function (index, column) {
                                aoColumns.push.apply(aoColumns, labelConfigCopyJson[key + "Rel" + column] ? makeAllSubGroupColumns(key + "Rel", column, subGroup) : []);
                            });
                        });
                    }
                });
            }


            if (isFaersEnabled) {
                $.each(subGroupsMap.AGE_GROUP_FAERS, function (key, value) {
                    aoColumns.push.apply(aoColumns, labelConfigCopyJson["ebgm" + value + "Faers"] ? makeSubGroupFaersColumns("ebgm" + value + "Faers", value, "ebgmAgeFaers") : []);
                    aoColumns.push.apply(aoColumns, labelConfigCopyJson["eb05" + value + "Faers"] ? makeSubGroupFaersColumns("eb05" + value + "Faers", value, "eb05AgeFaers") : []);
                    aoColumns.push.apply(aoColumns, labelConfigCopyJson["eb95" + value + "Faers"] ? makeSubGroupFaersColumns("eb95" + value + "Faers", value, "eb95AgeFaers") : []);
                });

                $.each(subGroupsMap.GENDER_FAERS, function (key, value) {
                    aoColumns.push.apply(aoColumns, labelConfigCopyJson["ebgm" + value + "Faers"] ? makeSubGroupFaersColumns("ebgm" + value + "Faers", value, "ebgmGenderFaers") : []);
                    aoColumns.push.apply(aoColumns, labelConfigCopyJson["eb05" + value + "Faers"] ? makeSubGroupFaersColumns("eb05" + value + "Faers", value, "eb05GenderFaers") : []);
                    aoColumns.push.apply(aoColumns, labelConfigCopyJson["eb95" + value + "Faers"] ? makeSubGroupFaersColumns("eb95" + value + "Faers", value, "eb95GenderFaers") : []);
                });
            }

            if (callingScreen == CALLING_SCREEN.TRIGGERED_ALERTS) {
                aoColumns.splice(2, 0, {
                    "mData": "name",
                    'className': 'dt-left',
                    "mRender": function (data, type, row) {
                        return '<span>' + row.alertName + '</span>'
                    }
                });
                $.each(result, function (index, data) {
                    aoColumns.push.apply(aoColumns, makeAoColumns(parseInt(index.slice(-1)), prrVisibility, rorVisibility, ebgmVisibility, dssVisibility));
                })
                $.each(result, function (index, data) {
                    aoColumns.push.apply(aoColumns, makeAoColumnsVigibase(parseInt(index.slice(-1)), prrVisibility, rorVisibility, ebgmVisibility, dssVisibility));
                })
            } else if (callingScreen == CALLING_SCREEN.REVIEW) {
                for (var prevColumn = 0; prevColumn < parseInt(prevColCount); prevColumn++) {
                    aoColumns.push.apply(aoColumns, makeAoColumns(prevColumn, prrVisibility, rorVisibility, ebgmVisibility, dssVisibility));
                }
                for (var prevColumn = 0; prevColumn < parseInt(prevColCount); prevColumn++) {
                    aoColumns.push.apply(aoColumns, makeAoColumnsVigibase(prevColumn, prrVisibility, rorVisibility, ebgmVisibility, dssVisibility));
                }
                if (isEvdasEnabled) {
                    for (var prevColumn = 0; prevColumn < parseInt(prevColCount); prevColumn++) {
                        $.each(prevColumnsEvdas, function (field, column) {
                            aoColumns.push.apply(aoColumns, makeEvdasPrevColumns(prevColumn, field))
                        })
                    }
                }
            }


            for (let q = 0; q < 3; q++) {
                for (let k = 0; k < labelConfigNew.length; k++) {
                    let fieldName = labelConfigNew[k].name
                    if (labelConfigCopyJson[fieldName] && (fieldName.toLocaleLowerCase().indexOf("new") != -1 || fieldName.toLocaleLowerCase().indexOf("Count") != -1)) {
                        aoColumns.push.apply(aoColumns, labelConfigCopyJson['exe' + q + labelConfigNew[k].name] ? [{
                            "mData": 'exe' + q + labelConfigNew[k].name.toString().trim(),
                            "className": 'text-center',
                            "mRender": function (data, type, row) {
                                let result = fieldName.split(/(?=[A-Z])/);
                                let newCount = "new" + result.slice(1).join('')
                                let cumCount = "cum" + result.slice(1).join('')

                                if (typeof row['exe' + q] !== "undefined" && typeof row['exe' + q][newCount] === 'undefined') {
                                    row['exe' + q][newCount] = '-';
                                }
                                if (typeof row['exe' + q] !== "undefined" && typeof row['exe' + q][cumCount] === 'undefined') {
                                    row['exe' + q][cumCount] = '-';
                                }
                                if(typeof row['exe' + q] !== "undefined"){
                                    return '<div style="text-align: center">' + row['exe' + q][newCount] + '</div>' +
                                        '<div style="text-align: center">' + row['exe' + q][cumCount] + '</div>';
                                } else {
                                     return '<div>-</div>';
                                }
                            },
                            "orderable": false,
                            "visible": signal.fieldManagement.visibleColumns('exe' + q + labelConfigNew[k].name)
                        }] : []);
                    }
                }
            }
        }
        return aoColumns
    };

    var cnt_drill_down = function (value, type, typeFlag, keyId, executedConfigId, alertId, productId, productName, ptCode, className, groupBySmq, preferredTerm, soc, productSelection, countType) {
        if (value === -1 || value === '-') {
            return '<span>' + '-' + '</span>'
        }
        var singleCaseDetailsUrl = '/signal/singleCaseAlert/caseSeriesDetails?aggExecutionId=' + executedConfigId + '&aggAlertId=' + alertId + '&aggCountType=' + countType + '&productId=' + productId + '&ptCode=' + ptCode + '&type=' + type + "&typeFlag=" + keyId + "&isArchived=" + isArchived + "&version=" + versionNumber + "&numberOfCount=" + value;

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


        var seriesData = "id:" + alertId + ",typeFlag:" + typeFlag + ",type:" + type + ",executedConfigId:" +
            executedConfigId + ",productId:" + productId + ",ptCode:" + ptCode+",keyId:"+keyId;

        if (value == 0 || value == "0") {
            if (className)
                return '<span class="blue-1 ' + className + '">' + value + '</span>'
            else
                return '<span class="blue-1">' + value + '</span>'
        }
        if (typeof pvrIntegrate != "undefined" && JSON.parse(pvrIntegrate)) {
            var hasReviewerAccessVar = false;
            if (typeof hasReviewerAccess !== "undefined" && hasReviewerAccess) {
                hasReviewerAccessVar = true;
            }
            var actionButton = '<div style="display: block" class="btn-group dropdown dropup"> \
                    <a class="dropdown-toggle ' + className + '" data-toggle="dropdown" href="#">' + value + '</a> \
                            <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;">';
            if ($("#isCaseSeriesAccess").val() == "false")
                actionButton = actionButton + '<li role="presentation" class="caseSeriesNotAccess"><a >' + 'Case Series' + '</a></li>';
            else
                actionButton = actionButton + '<li role="presentation"><a href=' + singleCaseDetailsUrl + ' target="_blank" data-url=' + singleCaseDetailsUrl + ' class="">' + 'Case Series' + '</a></li>';
            if ($("#isFaers").val() == "false")
                actionButton = actionButton + '<li role="presentation"><a target="_blank" href="' + template_list_url + '?' +
                    signal.utils.composeParams({
                        configId: executedConfigId,
                        type: type,
                        alertId: alertId,
                        typeFlag: keyId, //Added for report generation
                        keyId: keyId,
                        isAggScreen: true,
                        hasReviewerAccess: hasReviewerAccessVar,
                        preferredTerm: preferredTerm,
                        version: versionNumber
                    }) +
                    ' ">' + $.i18n._('Report') + '</a> \</li>'
            if (isCustomAnalysisEnabed) {
                actionButton = actionButton + '<li role="presentation"><a href=' + customAnalysisUrl + ' target="_blank" data-url="' + customAnalysisUrl + '"  >' + customAnalysisLabel + '</a></li>';
            } else if (spotfireEnabled && selectedDatasource.includes('pva')) {
                var smqType;
                var filterName;
                var url;
                var dataSourceSpotfire = 'PVA';
                if (productSelection) {
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

                if (groupBySmq !== false) {
                    if (soc.toUpperCase() == "SMQ") {
                        var actualPreferredTerm = preferredTerm
                        preferredTerm = preferredTerm.substring(0, preferredTerm.lastIndexOf("("))
                        var smqType = actualPreferredTerm.substring(actualPreferredTerm.lastIndexOf("(") + 1, actualPreferredTerm.lastIndexOf(")"));
                    }
                }
                var hiddenFieldsString = '<input type="hidden" class="seriesData" value="' + seriesData + '"/><input type="hidden" class="smqType" value="' + (typeof smqType === 'undefined' ? '' : encodeURIComponent(smqType)) + '"/><input type="hidden" class="preferredTerm" value="' + encodeURIComponent(preferredTerm) + '"/><input type="hidden" class="groupBySmq" value="' + encodeURIComponent(groupBySmq) + '"/><input type="hidden" class="soc" value="' + encodeURIComponent(soc) + '"/><input type="hidden" class="filterName" value="' + encodeURIComponent(filterName) + '"/><input type="hidden" class="productName" value="' + encodeURIComponent(productName) + '"/><input type="hidden" class="dataSourceSpotfire" value="' + encodeURIComponent(dataSourceSpotfire) + '"/>'
                if (type == COUNTS.NEW && analysisStatusJson.PR_DATE_RANGE.status == 2) {
                    url = analysisStatusJson.PR_DATE_RANGE.url
                    actionButton = actionButton + '<li role="presentation">' + hiddenFieldsString + '<a href="#" data-url="' + url + '" class="dataAnalysis"  >' + dataAnalysisLabel + '</a></li>';
                } else if (type == COUNTS.CUMM && analysisStatusJson.CUMULATIVE.status == 2) {
                    url = analysisStatusJson.CUMULATIVE.url
                    actionButton = actionButton + '<li role="presentation">' + hiddenFieldsString + '<a href="#" data-url="' + url + '" class="dataAnalysis"  >' + dataAnalysisLabel + '</a></li>';
                }

            }
            actionButton = actionButton + '</ul> \
                        </div>';
            return actionButton;
        } else {
            return '<a href=' + singleCaseDetailsUrl + ' target="_blank" class="">' + value + '</a>'
        }
    };
    var cnt_drill_down_jader = function (value, type, typeFlag, keyId, executedConfigId, alertId, productId, productName, ptCode, className, groupBySmq, preferredTerm, soc, productSelection) {
        if (value === -1 || value === '-') {
            return '<span>' + '-' + '</span>'
        }
        var singleCaseDetailsUrl = '/signal/singleCaseAlert/caseSeriesDetails?aggExecutionId=' + executedConfigId + '&aggAlertId=' + alertId + '&aggCountType=' + className + '&productId=' + productId + '&ptCode=' + ptCode + '&type=' + type + "&typeFlag=" + keyId + "&isArchived=" + isArchived + "&version=" + versionNumber + "&numberOfCount=" + value;

        var p_term_scope = 0;
        var termScopeStr = preferredTerm.substring(preferredTerm.lastIndexOf("(") + 1, preferredTerm.lastIndexOf(")"));
        if (termScopeStr == 'Broad')
            p_term_scope = 1;
        else if (termScopeStr == 'Narrow')
            p_term_scope = 2;

        var seriesData = "id:" + alertId + ",typeFlag:" + typeFlag + ",type:" + type + ",executedConfigId:" +
            executedConfigId + ",productId:" + productId + ",ptCode:" + ptCode+",keyId:"+keyId;

        if (value == 0 || value == "0") {
            if (className)
                return '<span class="blue-1 ' + className + '">' + value + '</span>'
            else
                return '<span class="blue-1">' + value + '</span>'
        }
        if (typeof pvrIntegrate != "undefined" && JSON.parse(pvrIntegrate)) {
            var hasReviewerAccessVar = false;
            if (typeof hasReviewerAccess !== "undefined" && hasReviewerAccess) {
                hasReviewerAccessVar = true;
            }
            var actionButton = '<div style="display: block" class="btn-group dropdown dropup"> \
                    <a class="dropdown-toggle ' + className + '" data-toggle="dropdown" href="#">' + value + '</a> \
                            <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;">';
            if ($("#isCaseSeriesAccess").val() == "false") {
                actionButton = actionButton + '<li role="presentation" class="caseSeriesNotAccess"><a >' + 'Case Series' + '</a></li>';
            }else {
                actionButton = actionButton + '<li role="presentation"><a href=' + singleCaseDetailsUrl + ' target="_blank" data-url=' + singleCaseDetailsUrl + ' class="">' + 'Case Series' + '</a></li>';
            }
            actionButton = actionButton + '</ul> \
                        </div>';
            return actionButton;
        } else {
            return '<a href=' + singleCaseDetailsUrl + ' target="_blank" class="">' + value + '</a>'
        }
    };

    var drillDownOptions = function (value, substance, pt, id, flag_var, isStartDate, alertId, url, className) {
        if (value === -1 || value === '-') {
            return '<span>' + '-' + '</span>'
        }
        var actionButton;
        var caseDrillDownUrl = encodeURI(fetchDrillDownDataUrl + "?substance=" + substance + "&id=" + id + "&pt=" + pt + "&flagVar=" + flag_var + "&isStartDate=" + isStartDate + "&alertId=" + alertId + "&numberOfCount=" + value);
        if (value === 0 || value === "0") {
            actionButton = '<span class="blue-1 ' + (className ? className : '') + '">' + value + '</span>'
        } else if (url && url != '') {
            actionButton = '<div style="display: block" class="btn-group dropdown dropup">' +
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

    var cnt_drill_down_faers = function (value, type, typeFlag, keyId, executedConfigId, alertId, productId, productName, ptCode, preferredTerm, countType, className, soc, productSelection) {
        if (value === -1 || value === '-') {
            return '<span>' + '-' + '</span>'
        }
        if (value == 0 || value == "0") {
            return '<span class="blue-1">' + value + '</span>'
        }
        var singleCaseDetailsUrl = '/signal/singleCaseAlert/caseSeriesDetails?aggExecutionId=' + executedConfigId + '&aggAlertId=' + alertId + '&aggCountType=' + countType + '&productId=' + productId + '&ptCode=' + ptCode + '&type=' + type + "&typeFlag=" + typeFlag + "&isArchived=" + isArchived + "&numberOfCount=" + value;
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

        var seriesData = "id:" + alertId + ",typeFlag:" + typeFlag + ",type:" + type + ",executedConfigId:" +
            executedConfigId + ",productId:" + productId + ",ptCode:" + ptCode+",keyId:"+keyId;

        if (typeof pvrIntegrate != "undefined" && JSON.parse(pvrIntegrate)) {
            var actionButton = '<div style="display: block" class="btn-group dropdown dropup"> \
                    <a class="dropdown-toggle ' + className + '" data-toggle="dropdown" href="#">' + value + '</a> \
                            <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;">';
            if ($("#isCaseSeriesAccess").val() == "false")
                actionButton = actionButton + '<li role="presentation" class="caseSeriesNotAccess"><a >' + 'Case Series' + '</a></li>';
            else
                actionButton = actionButton + '<li role="presentation"><a href=' + singleCaseDetailsUrl + ' target="_blank" data-url=' + singleCaseDetailsUrl + ' class="">' + 'Case Series' + '</a></li>';
            if (isCustomAnalysisEnabed) {
                actionButton = actionButton + '<li role="presentation"><a href=' + customAnalysisUrl + ' target="_blank" data-url="' + customAnalysisUrl + '"  >' + customAnalysisLabel + '</a></li>';
            } else if (spotfireEnabled && selectedDatasource.includes('faers')) {
                var smqType;
                var filterName;
                var url;
                var dataSourceSpotfire = 'FAERS';
                if (productSelection) {
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
                var hiddenFieldsString = '<input type="hidden" class="seriesData" value="' + seriesData + '"/><input type="hidden" class="smqType" value="' + (typeof smqType === 'undefined' ? '' : encodeURIComponent(smqType)) + '"/><input type="hidden" class="preferredTerm" value="' + encodeURIComponent(preferredTerm) + '"/><input type="hidden" class="groupBySmq" value="' + encodeURIComponent(groupBySmq) + '"/><input type="hidden" class="soc" value="' + encodeURIComponent(soc) + '"/><input type="hidden" class="filterName" value="' + encodeURIComponent(filterName) + '"/><input type="hidden" class="productName" value="' + encodeURIComponent(productName) + '"/><input type="hidden" class="dataSourceSpotfire" value="' + encodeURIComponent(dataSourceSpotfire) + '"/>'
                if (type == COUNTS.NEW && analysisStatusJson.PR_DATE_RANGE_FAERS.status == 2) {
                    url = analysisStatusJson.PR_DATE_RANGE_FAERS.url
                    actionButton = actionButton + '<li role="presentation">' + hiddenFieldsString + '<a href="#" data-url="' + url + '" class="dataAnalysis"  >' + dataAnalysisLabel + '</a></li>';
                } else if (type == COUNTS.CUMM && analysisStatusJson.CUMULATIVE_FAERS.status == 2) {
                    url = analysisStatusJson.CUMULATIVE_FAERS.url
                    actionButton = actionButton + '<li role="presentation">' + hiddenFieldsString + '<a href="#" data-url="' + url + '" class="dataAnalysis"  >' + dataAnalysisLabel + '</a></li>';
                }

            }
            actionButton = actionButton + '</ul> \
                        </div>';
            return actionButton;
        } else {
            return '<a href=' + singleCaseDetailsUrl + ' target="_blank" class="">' + value + '</a>'
        }
    };

    var cnt_drill_down_vaers = function (value, type, typeFlag, keyId, executedConfigId, alertId, productId, productName, ptCode, preferredTerm, countType, className, soc, productSelection) {
        if (value === -1 || value === '-') {
            return '<span>' + '-' + '</span>'
        }
        if (value == 0 || value == "0") {
            return '<span class="blue-1">' + value + '</span>'
        }
        var singleCaseDetailsUrl = '/signal/singleCaseAlert/caseSeriesDetails?aggExecutionId=' + executedConfigId + '&aggAlertId=' + alertId + '&aggCountType=' + countType + '&productId=' + productId + '&ptCode=' + ptCode + '&type=' + type + "&typeFlag=" + typeFlag + "&isArchived=" + isArchived + "&numberOfCount=" + value;
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

        var seriesData = "id:" + alertId + ",typeFlag:" + typeFlag + ",type:" + type + ",executedConfigId:" +
            executedConfigId + ",productId:" + productId + ",ptCode:" + ptCode + ",keyId:" + keyId;

        if (typeof pvrIntegrate != "undefined" && JSON.parse(pvrIntegrate)) {
            var actionButton = '<div style="display: block" class="btn-group dropdown dropup"> \
                    <a class="dropdown-toggle ' + className + '" data-toggle="dropdown" href="#">' + value + '</a> \
                            <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;">';
            if ($("#isCaseSeriesAccess").val() == "false")
                actionButton = actionButton + '<li role="presentation" class="caseSeriesNotAccess"><a >' + 'Case Series' + '</a></li>';
            else
                actionButton = actionButton + '<li role="presentation"><a href=' + singleCaseDetailsUrl + ' target="_blank" data-url=' + singleCaseDetailsUrl + ' class="">' + 'Case Series' + '</a></li>';
            if (isCustomAnalysisEnabed) {
                actionButton = actionButton + '<li role="presentation"><a href=' + customAnalysisUrl + ' target="_blank" data-url="' + customAnalysisUrl + '"  >' + customAnalysisLabel + '</a></li>';
            } else if (spotfireEnabled && selectedDatasource.includes('vaers')) {
                var smqType;
                var filterName;
                var url;
                var dataSourceSpotfire = 'VAERS';
                if (productSelection) {
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
                var hiddenFieldsString = '<input type="hidden" class="seriesData" value="' + seriesData + '"/><input type="hidden" class="smqType" value="' + (typeof smqType === 'undefined' ? '' : encodeURIComponent(smqType)) + '"/><input type="hidden" class="preferredTerm" value="' + encodeURIComponent(preferredTerm) + '"/><input type="hidden" class="groupBySmq" value="' + encodeURIComponent(groupBySmq) + '"/><input type="hidden" class="soc" value="' + encodeURIComponent(soc) + '"/><input type="hidden" class="filterName" value="' + encodeURIComponent(filterName) + '"/><input type="hidden" class="productName" value="' + encodeURIComponent(productName) + '"/><input type="hidden" class="dataSourceSpotfire" value="' + encodeURIComponent(dataSourceSpotfire) + '"/>'
                if (type == COUNTS.NEW && analysisStatusJson.PR_DATE_RANGE_VAERS.status == 2) {
                    url = analysisStatusJson.PR_DATE_RANGE_VAERS.url
                    actionButton = actionButton + '<li role="presentation">' + hiddenFieldsString + '<a href="#" data-url="' + url + '" class="dataAnalysis"  >' + dataAnalysisLabel + '</a></li>';
                } else if (type == COUNTS.CUMM && analysisStatusJson.CUMULATIVE_VAERS.status == 2) {
                    url = analysisStatusJson.CUMULATIVE_VAERS.url
                    actionButton = actionButton + '<li role="presentation">' + hiddenFieldsString + '<a href="#" data-url="' + url + '" class="dataAnalysis"  >' + dataAnalysisLabel + '</a></li>';
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
            $('span.select-all-check input').prop('checked', false);
        }
    });

    $(document).on('click', '.alert-comment-modal-close, #upper-close-comment', function () {
        if ($('.add-comments').is(':disabled')) {
            $('#commentTemplateSelect').prop('selectedIndex', 0);
            $('#commentModal').modal('toggle');
            $('#commentHistoryTable').DataTable().clear().draw();
        } else {
            bootbox.confirm({
                title: ' ',
                message: "There are unsaved changes in the screen. Do you want to proceed?",
                buttons: {
                    confirm: {
                        label: 'Yes',
                        className: 'btn-primary'
                    },
                    cancel: {
                        label: 'No',
                        className: 'btn-default'
                    }
                },
                callback: function (result) {
                    if (result) {
                        $('#commentTemplateSelect').prop('selectedIndex', 0);
                        $('#commentModal').modal('toggle');
                        $('#commentHistoryTable').DataTable().clear().draw();
                        $(".ico-paste").val('');
                    }
                }
            });
        }
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
        var checkedColumns = {};
        $('#tableColumns tr ').each(function () {
            var fieldName = $(this).children().children().attr('data-field');
            if (fieldName) {
                checkedColumns[fieldName] = $(this).children()[1].firstChild.checked
            }
        });

        var isFilterRequest = true;
        var filterValues = [];
        var prefix = "agg_";
        var id = sessionStorage.getItem(prefix + "id");
        if (window.sessionStorage) {
            if (signal.alertReview.isAlertPersistedInSessionStorage(prefix)) {
                filterValues = JSON.parse(sessionStorage.getItem(prefix + "filters_value"));
            } else {
                signal.alertReview.removeFiltersFromSessionStorage(prefix);
                isFilterRequest = false;
            }
        }
        $.each($(".disposition-ico").eq(0).find("li"), function (id, val) {
            if ($(val).find('input').is(':checked') === true) {
                filterValues.push($(val).find('input').val());
            }
        });

        if (ids.length > 0) {
            updatedExportUrl = clickedURL + '&isFilterRequest=' + isFilterRequest + '&filters=' + encodeURIComponent(JSON.stringify(filterValues)) + "&selectedCases=" + ids + "&viewId=" + viewId;
            if ($('#advanced-filter').val()) {
                updatedExportUrl += "&advancedFilterId=" + $('#advanced-filter').val()
            }
        } else {
            var filterList = {};
            $(".yadcf-filter-wrapper").each(function () {
                var filterVal = $(this).children().val();
                if (filterVal) {
                    filterList[$(this).prev().attr('data-field')] = filterVal
                }
            });
            var filterListJson = JSON.stringify(filterList);
            updatedExportUrl = clickedURL + '&isFilterRequest=' + isFilterRequest + '&filters=' + encodeURIComponent(JSON.stringify(filterValues)) + "&frequency=" + freqSelected + "&viewId=" + viewId + "&filterList=" + encodeURIComponent(filterListJson) + "&isFaers=" + $('#isFaers').val();
            if ($('#advanced-filter').val()) {
                updatedExportUrl += "&advancedFilterId=" + $('#advanced-filter').val()
            }
        }
        window.location.href = updatedExportUrl;
        sessionStorage.setItem(prefix + "filters_value", JSON.stringify(filterValues));
        sessionStorage.setItem(prefix + "id", id);
        return false
    });


    $('#exportTypesEvdas a[href]').on('click', function (e) {
        var clickedURL = e.currentTarget.href;
        var updatedExportUrl = clickedURL;
        var sortCol, sortDir;
        if ($('#evdas-case-drill-down-table').DataTable().order()) {
            sortCol = $('#evdas-case-drill-down-table').DataTable().order()[0][0];
            sortDir = $('#evdas-case-drill-down-table').DataTable().order()[0][1];
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
        if ($('#evdas-case-drill-down-table').DataTable().order()) {
            formdata.append("sortedCol", $('#evdas-case-drill-down-table').DataTable().order()[0][0]);
            formdata.append("sortedDir", $('#evdas-case-drill-down-table').DataTable().order()[0][1]);
        }
        formdata.append("filterText", $('.dt-input').val());
        formdata.append("description", $('#evdas-attachment-box').val());
        formdata.append("isArchived", isArchived);
        formdata.append("alertType", alertType);

        $.ajax({
            url: attachCaseListingUrl,
            type: "POST",
            mimeType: "multipart/form-data",
            processData: false,
            contentType: false,
            data: formdata,
        })
            .done(function (data) {
                $response = JSON.parse(data);
                if ($response.status) {
                    var parentRow = $('#alertsDetailsTable_wrapper .DTFC_ScrollWrapper .DTFC_LeftWrapper .DTFC_LeftBodyWrapper .DTFC_LeftBodyLiner tbody tr').find('td.first-column').find("[data-id='" + evdasAlertId + "']").closest('tr');
                    showAttachmentIcon(parentRow);
                    $.Notification.notify('success', 'top right', "Success", 'The attachment has been added successfully', {autoHideDelay: 5000});
                } else {
                    $.Notification.notify('error', 'top right', "Error", $response.message, {autoHideDelay: 5000});
                }
            });
    });


    var loadTable = function (result) {
        var groupBySmq = $('#groupBySmq').val();
        init(result, groupBySmq);
        $("#alertsDetailsTable_filter").hide();
        if (callingScreen == CALLING_SCREEN.TRIGGERED_ALERTS) {
            for (i = 1; i <= $(".extraColumns").length; i++) {
                listOfIndex.push(25 + i)
            }
        }

        if (callingScreen == CALLING_SCREEN.REVIEW) {
            //saving new ViewInstance modal
            if (groupBySmq == "true" && $("#isVaers").val() == "true") {
                signal.alertReview.openSaveViewModal(filterIndex, applicationNameSmqVaers, viewId)
            } else if ($("#isVaers").val() == "true") {
                signal.alertReview.openSaveViewModal(filterIndex, applicationNameVaers, viewId)
            } else if (groupBySmq == "true" && $("#isVigibase").val() == "true") {
                signal.alertReview.openSaveViewModal(filterIndex, applicationNameSmqVigibase, viewId)
            } else if ($("#isVigibase").val() == "true") {
                signal.alertReview.openSaveViewModal(filterIndex, applicationNameVigibase, viewId)
            } else if (groupBySmq == "true" && $("#isJader").val() == "true") {
                signal.alertReview.openSaveViewModal(filterIndex, applicationNameSmqJader, viewId)
            } else if ($("#isJader").val() == "true") {
                signal.alertReview.openSaveViewModal(filterIndex, applicationNameJader, viewId)
            } else if (groupBySmq == "true" && $("#isFaers").val() == "true") {
                signal.alertReview.openSaveViewModal(filterIndex, applicationNameSmqFaers, viewId)
            } else if (groupBySmq == "true") {
                signal.alertReview.openSaveViewModal(filterIndex, applicationNameSmq, viewId)
            } else if ($("#isFaers").val() == "true") {
                signal.alertReview.openSaveViewModal(filterIndex, applicationNameFaers, viewId)
            } else {
                signal.alertReview.openSaveViewModal(filterIndex, applicationName, viewId)
            }
        } else if (callingScreen == CALLING_SCREEN.DASHBOARD) {
            signal.alertReview.openSaveViewModal(filterIndex, ALERT_CONFIG_TYPE.AGGREGATE_CASE_ALERT_DASHBOARD, viewId)
        }
        //list of hidden columns based on saved view
        if (sessionStorage.getItem('aca_notVisibleColumn_' + executedConfigId) || $('#columnIndex').val() != "") {
            listOfIndex = signal.alertReview.createListOfIndex('aca_notVisibleColumn_' + executedConfigId, false, 'aca_viewName_' + executedConfigId);

        }
    }
    if (callingScreen == CALLING_SCREEN.TRIGGERED_ALERTS) {
        var getDateRange = function (frequency) {
            $(".extraColumns").remove();
            $.ajax({
                type: "POST",
                url: dateRangeListUrl + "?frequency=" + frequency,
            })
                .done(function (result) {
                    var prrVisibility = JSON.parse($("#showPrr").val());
                    var rorVisibility = JSON.parse($("#showRor").val());
                    var ebgmVisibility = JSON.parse($("#showEbgm").val());
                    var dssVisibility = JSON.parse($("#showDss").val());
                    $.each(result, function (index, data) {
                        var daterange = data.startDate.replace(/\-/g, "/") + '-' + data.endDate.replace(/\-/g, "/");
                        if (index == "exeRecent") {
                            $("<div class='extraColumns'>" + daterange + "</div>").appendTo('#alertsDetailsTable .dateRange')
                        } else {
                            var i = index.slice(-1)
                            $("<th class='extraColumns'><div class='th-label' data-field='exe" + i + "newSponCount'><div class='stacked-cell-center-top'>N Spon</div><div class='stacked-cell-center-top'>C Spon</div><div class='stacked-cell-center-top'>" + daterange + "</div></div></th>").appendTo("#alertsDetailsTableRow");
                            $("<th class='extraColumns'><div class='th-label' data-field='exe" + i + "newSeriousCount'><div class='stacked-cell-center-top'>N Ser</div><div class='stacked-cell-center-top'>C Ser</div><div class='stacked-cell-center-top'>" + daterange + "</div></div></th>").appendTo("#alertsDetailsTableRow");
                            $("<th class='extraColumns'><div class='th-label' data-field='exe" + i + "newFatalCount'><div class='stacked-cell-center-top'>N Fatal</div><div class='stacked-cell-center-top'>C Fatal</div><div class='stacked-cell-center-top'>" + daterange + "</div></div></th>").appendTo("#alertsDetailsTableRow");
                            $("<th class='extraColumns'><div class='th-label' data-field='exe" + i + "newStudyCount'><div class='stacked-cell-center-top'>N Study</div><div class='stacked-cell-center-top'>C Study</div><div class='stacked-cell-center-top'>" + daterange + "</div></div></th>").appendTo("#alertsDetailsTableRow");
                            if (prrVisibility) {
                                $("<th class='extraColumns'> <div class='th-label' data-field='exe" + i + "prrValue'><div class='stacked-cell-center-top'>PRR</div><div class='stacked-cell-center-top'>" + daterange + "</div></div></th>" +
                                    "<th class='extraColumns'><div class='th-label' data-field='exe" + i + "prrLCI'><div class='stacked-cell-center-top'>PRR LCI</div><div class='stacked-cell-center-top'>PRR UCI</div><div class='stacked-cell-center-top'>" + daterange + "</div></div> </th>").appendTo("#alertsDetailsTableRow");
                            }
                            if (rorVisibility) {
                                $("<th class='extraColumns'> <div class='th-label' data-field='exe" + i + "rorValue'><div class='stacked-cell-center-top'>ROR</div><div class='stacked-cell-center-top'>" + daterange + "</div></div></th>" +
                                    "<th class='extraColumns'><div class='th-label' data-field='exe" + i + "rorLCI'><div class='stacked-cell-center-top'>R0R LCI</div><div class='stacked-cell-center-top'>ROR UCI</div><div class='stacked-cell-center-top'>" + daterange + "</div></div> </th>").appendTo("#alertsDetailsTableRow");
                            }
                            if (ebgmVisibility) {
                                $("<th class='extraColumns'> <div class='th-label' data-field='exe" + i + "ebgm'><div class='stacked-cell-center-top'>EBGM</div><div class='stacked-cell-center-top'>" + daterange + "</div></div></th>" +
                                    "<th class='extraColumns'><div class='th-label' data-field='exe" + i + "eb05'><div class='stacked-cell-center-top'>EB05</div><div class='stacked-cell-center-top'>EB95</div><div class='stacked-cell-center-top'>" + daterange + "</div></div> </th>").appendTo("#alertsDetailsTableRow");
                            }
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
                getDateRange(sessionStorage.getItem("frequency"))
            } else {
                $("#frequencyNames").val($("#frequencyNames option:first").val());
                $("#frequencyNames").trigger('change')
            }
        }
    } else {
        loadTable("")
    }

    $(".exportAlertHistories").on('click', function (e) {
        var searchField = $('#currentAlertHistoryModalTable_filter').find('.form-control.dt-search').val();
        var otherSearchString = $('#otherAlertsHistoryModalTable_filter').find('.form-control.dt-search').val();
        var table = $("#currentAlertHistoryModalTable").DataTable()
        var otherAlertTable = $("#otherAlertsHistoryModalTable").DataTable()
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
        var productEventHistoryModal = $('#productEventHistoryModal');
        var productName = encodeToHTML(productEventHistoryModal.find("#productName").html());
        var regularExpression = new RegExp(findPlusInString, 'gi');
        var encodedProductName = encodeURI(productName).replace(regularExpression, '%2b');
        var eventName = encodeToHTML(productEventHistoryModal.find("#eventName").html());
        var configId = productEventHistoryModal.find("#configId").text();
        var selectedCase = e.currentTarget.href + "&productName=" + encodedProductName + "&eventName=" + eventName + "&configId=" + configId + "&searchField=" + encodeURIComponent(searchField) + "&alertConfigIds=" + alertConfigIds + "&otherAlertConfigIds=" + otherAlertConfigIds + "&otherSearchString=" + encodeURIComponent(otherSearchString);
        window.location.href = selectedCase;
        return false
    });

    $(".exportCommentHistories").on('click', function (e) {
        var caseId = $('#caseId').val();
        var searchField = $('#commentHistoryTable_filter').find('.form-control.dt-search').val();
        var exportCommentUrl = e.currentTarget.href + "&caseId=" + caseId + "&searchField=" + encodeURIComponent(searchField);
        window.location.href = exportCommentUrl;
        return false
    });


    set_disable_click($('.generateCurrentAnalysis'), $('.generateCurrentAnalysis').attr('data-status'), 'Current Period Analysis');
    set_disable_click($('.generateCumulativeAnalysis'), $('.generateCumulativeAnalysis').attr('data-status'), 'Cumulative Period Analysis');
    set_disable_click($('.generateCurrentAnalysisFaers'), $('.generateCurrentAnalysisFaers').attr('data-status'), 'Current Period Analysis (FAERS)');
    set_disable_click($('.generateCumulativeAnalysisFaers'), $('.generateCumulativeAnalysisFaers').attr('data-status'), 'Cumulative Period Analysis (FAERS)');
    set_disable_click($('.generateCurrentAnalysisVaers'), $('.generateCurrentAnalysisVaers').attr('data-status'), 'Current Period Analysis (VAERS)');
    set_disable_click($('.generateCumulativeAnalysisVaers'), $('.generateCumulativeAnalysisVaers').attr('data-status'), 'Cumulative Period Analysis (VAERS)');

    $(document).on('click', '.generateCurrentAnalysis', function () {
        generate_analysis($('.generateCurrentAnalysis'), 'PR_DATE_RANGE_SAFETYDB', 'Current Period Analysis', 'Current');
    });
    $(document).on('click', '.generateCumulativeAnalysis', function () {
        generate_analysis($('.generateCumulativeAnalysis'), 'CUMULATIVE_SAFETYDB', 'Cumulative Period Analysis', 'Cumulative');
    });
    //-------------------------Faers----------------------------------------------
    $(document).on('click', '.generateCurrentAnalysisFaers', function () {
        generate_analysis($('.generateCurrentAnalysisFaers'), 'PR_DATE_RANGE_FAERS', 'Current Period Analysis (FAERS)', 'CurrentFaers');
    });
    $(document).on('click', '.generateCumulativeAnalysisFaers', function () {
        generate_analysis($('.generateCumulativeAnalysisFaers'), 'CUMULATIVE_FAERS', 'Cumulative Period Analysis (FAERS)', 'CumulativeFaers');
    });
    //---------------------------Vaers-------------------------------------------------
    $(document).on('click', '.generateCurrentAnalysisVaers', function () {
        generate_analysis($('.generateCurrentAnalysisVaers'), 'PR_DATE_RANGE_VAERS', 'Current Period Analysis (VAERS)', 'CurrentVaers');
    });
    $(document).on('click', '.generateCumulativeAnalysisVaers', function () {
        generate_analysis($('.generateCumulativeAnalysisVaers'), 'CUMULATIVE_VAERS', 'Cumulative Period Analysis (VAERS)', 'CumulativeVaers');
    });
    $(document).on('click', '.dataAnalysis', function () {
        var parent_row = $(this).closest("tr").find('td:eq(0)');
        var executedConfigId = parent_row.find("#execConfigId").val();
        var productName = parent_row.find("#productName").val();
        var preferredTerm = parent_row.find("#preferredTerm").val();
        var isArchived = parent_row.find("#isArchived").val();
        var value = $('#symptomsComanifestation').val();
        var columnName = $('#symptomsComanifestationColumnName').val();
        generateCaseSeries(selectedDatasource, $(this).parent(), productName, preferredTerm, executedConfigId, isArchived, $(this).attr("data-url"), value, columnName)
    })
    $(document).on('change', '.copy-select', function () {
        addToSelectedCheckBox($(this))
    });
});

function makeAoColumns(i, prrVisibility, rorVisibility, ebgmVisibility, dssVisibilty) {
    var addColumns = [];
    var labelConfigCopyJson = $("#labelConfigCopyJson").val()
    labelConfigCopyJson = JSON.parse(labelConfigCopyJson)
    addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'newSponCount'] ? [
        {
            "mData": 'exe' + i + 'newSponCount',
            "className": '',
            "mRender": function (data, type, row) {
                if (row['exe' + i]) {
                    return '<div><div class="stacked-cell-center-top">' + row['exe' + i].newSponCount + '</div><div class="stacked-cell-center-top">' + row['exe' + i].cumSponCount + '</div></div>'
                } else {
                    return '<div></div>'
                }
            },
            "visible": signal.fieldManagement.visibleColumns('exe' + i + 'newSponCount')
        }
    ] : []);


    addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'newSeriousCount'] ? [
        {
            "mData": 'exe' + i + 'newSeriousCount',
            "className": '',
            "mRender": function (data, type, row) {
                if (row['exe' + i]) {
                    return '<div><div class="stacked-cell-center-top">' + row['exe' + i].newSeriousCount + '</div><div class="stacked-cell-center-top">' + row['exe' + i].cumSeriousCount + '</div></div>'
                } else {
                    return '<div></div>'
                }
            },
            "visible": signal.fieldManagement.visibleColumns('exe' + i + 'newSeriousCount')
        }
    ] : []);


    addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'newFatalCount'] ? [
        {
            "mData": 'exe' + i + 'newFatalCount',
            "className": '',
            "mRender": function (data, type, row) {
                if (row['exe' + i]) {
                    return '<div><div class="stacked-cell-center-top">' + row['exe' + i].newFatalCount + '</div><div class="stacked-cell-center-top">' + row['exe' + i].cumFatalCount + '</div></div>'
                } else {
                    return '<div></div>'
                }
            },
            "visible": signal.fieldManagement.visibleColumns('exe' + i + 'newFatalCount')
        }
    ] : []);


    addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'newStudyCount'] ? [{
        "mData": 'exe' + i + 'newStudyCount',
        "className": '',
        "mRender": function (data, type, row) {
            if (row['exe' + i]) {
                return '<div><div class="stacked-cell-center-top">' + row['exe' + i].newStudyCount + '</div><div class="stacked-cell-center-top">' + row['exe' + i].cumStudyCount + '</div></div>'
            } else {
                return '<div></div>'
            }
        },
        "visible": signal.fieldManagement.visibleColumns('exe' + i + 'newStudyCount')
    }] : []);
    addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'newCount'] ? [{
        "mData": 'exe' + i + 'newCount',
        "className": '',
        "mRender": function (data, type, row) {
            if (row['exe' + i]) {
                return '<div><div class="stacked-cell-center-top">' + row['exe' + i].newCount + '</div><div class="stacked-cell-center-top">' + row['exe' + i].cummCount + '</div></div>'
            } else {
                return '<div></div>'
            }
        },
        "visible": signal.fieldManagement.visibleColumns('exe' + i + 'newCount')
    }] : []);
    addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'newPediatricCount'] ? [{
        "mData": 'exe' + i + 'newPediatricCount',
        "className": '',
        "mRender": function (data, type, row) {
            if (row['exe' + i]) {
                return '<div><div class="stacked-cell-center-top">' + row['exe' + i].newPediatricCount + '</div><div class="stacked-cell-center-top">' + row['exe' + i].cummPediatricCount + '</div></div>'
            } else {
                return '<div></div>'
            }
        },
        "visible": signal.fieldManagement.visibleColumns('exe' + i + 'newPediatricCount')
    }] : []);

    addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'newInteractingCount'] ? [{
        "mData": 'exe' + i + 'newInteractingCount',
        "className": '',
        "mRender": function (data, type, row) {
            if (row['exe' + i]) {
                return '<div><div class="stacked-cell-center-top">' + row['exe' + i].newInteractingCount + '</div><div class="stacked-cell-center-top">' + row['exe' + i].cummInteractingCount + '</div></div>'
            } else {
                return '<div></div>'
            }
        },
        "visible": signal.fieldManagement.visibleColumns('exe' + i + 'newInteractingCount')
    }] : []);

    addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'newGeriatricCount'] ? [{
        "mData": 'exe' + i + 'newGeriatricCount',
        "className": '',
        "mRender": function (data, type, row) {
            if (row['exe' + i]) {
                return '<div><div class="stacked-cell-center-top">' + row['exe' + i].newGeriatricCount + '</div><div class="stacked-cell-center-top">' + row['exe' + i].cumGeriatricCount + '</div></div>'
            } else {
                return '<div></div>'
            }
        },
        "visible": signal.fieldManagement.visibleColumns('exe' + i + 'newGeriatricCount')
    }] : []);

    addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'newNonSerious'] ? [{
        "mData": 'exe' + i + 'newNonSerious',
        "className": '',
        "mRender": function (data, type, row) {
            if (row['exe' + i]) {
                return '<div><div class="stacked-cell-center-top">' + row['exe' + i].newNonSerious + '</div><div class="stacked-cell-center-top">' + row['exe' + i].cumNonSerious + '</div></div>'
            } else {
                return '<div></div>'
            }
        },
        "visible": signal.fieldManagement.visibleColumns('exe' + i + 'newNonSerious')
    }] : []);

    // addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'newProdCount'] ? [{
    //         "mData": 'exe' + i + 'newProdCount',
    //         "className": '',
    //         "mRender": function (data, type, row) {
    //             return '<div><div class="stacked-cell-center-top">' + row['exe' + i].newProdCount + '</div><div class="stacked-cell-center-top">' + row['exe' + i].cumNonSerious + '</div></div>'
    //         },
    //         "visible": signal.fieldManagement.visibleColumns('exe' + i + 'newProdCount')
    //     } ] : []);


    if (prrVisibility) {
        addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'prrValue'] ? [{
            "mData": 'exe' + i + 'prrValue',
            "className": 'text-center',
            "mRender": function (data, type, row) {
                if (row['exe' + i]) {
                    return '<span>' + row['exe' + i].prrValue + '</span>'
                } else {
                    return '<div></div>'
                }
            },
            "visible": signal.fieldManagement.visibleColumns('exe' + i + 'prrValue')
        }] : []);
        addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'prrLCI'] ? [{
            "mData": 'exe' + i + 'prrLCI',
            "className": 'text-center',
            "mRender": function (data, type, row) {
                if (row['exe' + i]) {
                    return '<div><div class="stacked-cell-center-top">' + row['exe' + i].prrLCI + '</div><div class="stacked-cell-center-top">' + row['exe' + i].prrUCI + '</div></div>'
                } else {
                    return '<div></div>'
                }
            },
            "visible": signal.fieldManagement.visibleColumns('exe' + i + 'prrLCI')
        }] : []);
    }


    if (rorVisibility) {
        addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'rorValue'] ? [{
            "mData": 'exe' + i + 'rorValue',
            "className": 'text-center',
            "mRender": function (data, type, row) {
                if (row['exe' + i]) {
                    return '<span>' + row['exe' + i].rorValue + '</span>'
                } else {
                    return '<div></div>'
                }
            },
            "visible": signal.fieldManagement.visibleColumns('exe' + i + 'rorValue')
        }] : []);
        addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'rorLCI'] ? [{
            "mData": 'exe' + i + 'rorLCI',
            "className": 'text-center',
            "mRender": function (data, type, row) {
                if (row['exe' + i]) {
                    return '<div><div class="stacked-cell-center-top">' + row['exe' + i].rorLCI + '</div><div class="stacked-cell-center-top">' + row['exe' + i].rorUCI + '</div></div>'
                } else {
                    return '<div></div>'
                }
            },
            "visible": signal.fieldManagement.visibleColumns('exe' + i + 'rorLCI')
        }] : []);
    }
    if (ebgmVisibility) {
        addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'ebgm'] ? [{
            "mData": 'exe' + i + 'ebgm',
            "className": 'text-center',
            "mRender": function (data, type, row) {
                if (row['exe' + i]) {
                    return '<span>' + row['exe' + i].ebgm + '</span>'
                } else {
                    return '<div></div>'
                }
            },
            "visible": signal.fieldManagement.visibleColumns('exe' + i + 'ebgm')
        }] : []);
        addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'eb05'] ? [{
            "mData": 'exe' + i + 'eb05',
            "className": 'text-center',
            "mRender": function (data, type, row) {
                if (row['exe' + i]) {
                    return '<div><div class="stacked-cell-center-top">' + row['exe' + i].eb05 + '</div><div class="stacked-cell-center-top">' + row['exe' + i].eb95 + '</div></div>'
                } else {
                    return '<div></div>'
                }
            },
            "visible": signal.fieldManagement.visibleColumns('exe' + i + 'eb05')
        }] : []);

    }


    if (prrVisibility && rorVisibility) {
        addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'chiSquare'] ? [{
            "mData": 'exe' + i + 'chiSquare',
            "className": 'text-center',
            "mRender": function (data, type, row) {
                if (row['exe' + i]) {
                    return '<span>' + row['exe' + i].chiSquare + '</span>'
                } else {
                    return '<div></div>'
                }
            },
            "visible": signal.fieldManagement.visibleColumns('exe' + i + 'chiSquare')
        }] : []);
    }
    if (ebgmVisibility) {
        addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'rrValue'] ? [{
            "mData": 'exe' + i + 'rrValue',
            "className": 'text-center',
            "mRender": function (data, type, row) {
                if (row['exe' + i]) {
                    return '<span>' + row['exe' + i].rrValue + '</span>'
                } else {
                    return '<div></div>'
                }
            },
            "orderable": false,
            "visible": signal.fieldManagement.visibleColumns('exe' + i + 'rrValue')
        }] : []);
    }


    if (isFaersEnabled) {
        addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'newSponCountFaers'] ? [{
            "mData": 'exe' + i + 'newSponCountFaers',
            "className": '',
            "mRender": function (data, type, row) {
                if (row['exe' + i]) {
                    return '<div><div class="stacked-cell-center-top">' + row['exe' + i].newSponCountFaers + '</div><div class="stacked-cell-center-top">' + row['exe' + i].cumSponCountFaers + '</div></div>'
                } else {
                    return '<div></div>'
                }
            },
            "visible": signal.fieldManagement.visibleColumns('exe' + i + 'newSponCountFaers')
        }] : []);


        addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'newSeriousCountFaers'] ? [{
            "mData": 'exe' + i + 'newSeriousCountFaers',
            "className": '',
            "mRender": function (data, type, row) {
                if (row['exe' + i]) {
                    return '<div><div class="stacked-cell-center-top">' + row['exe' + i].newSeriousCountFaers + '</div><div class="stacked-cell-center-top">' + row['exe' + i].cumSeriousCountFaers + '</div></div>'
                } else {
                    return '<div></div>'
                }
            },
            "visible": signal.fieldManagement.visibleColumns('exe' + i + 'newSeriousCountFaers')
        }] : []);


        addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'newFatalCountFaers'] ? [{
            "mData": 'exe' + i + 'newFatalCountFaers',
            "className": '',
            "mRender": function (data, type, row) {
                if (row['exe' + i]) {
                    return '<div><div class="stacked-cell-center-top">' + row['exe' + i].newFatalCountFaers + '</div><div class="stacked-cell-center-top">' + row['exe' + i].cumFatalCountFaers + '</div></div>'
                } else {
                    return '<div></div>'
                }
            },
            "visible": signal.fieldManagement.visibleColumns('exe' + i + 'newFatalCountFaers')
        }] : []);


        addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'newStudyCountFaers'] ? [{
            "mData": 'exe' + i + 'newStudyCountFaers',
            "className": '',
            "mRender": function (data, type, row) {
                if (row['exe' + i]) {
                    return '<div><div class="stacked-cell-center-top">' + row['exe' + i].newStudyCountFaers + '</div><div class="stacked-cell-center-top">' + row['exe' + i].cumStudyCountFaers + '</div></div>'
                } else {
                    return '<div></div>'
                }
            },
            "visible": signal.fieldManagement.visibleColumns('exe' + i + 'newStudyCountFaers')
        }] : []);

        addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'newCountFaers'] ? [{
            "mData": 'exe' + i + 'newCountFaers',
            "className": '',
            "mRender": function (data, type, row) {
                if (row['exe' + i]) {
                    return '<div><div class="stacked-cell-center-top">' + row['exe' + i].newCountFaers + '</div><div class="stacked-cell-center-top">' + row['exe' + i].cummCountFaers + '</div></div>'
                } else {
                    return '<div></div>'
                }
            },
            "visible": signal.fieldManagement.visibleColumns('exe' + i + 'newCountFaers')
        }] : []);

        addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'newPediatricCountFaers'] ? [{
            "mData": 'exe' + i + 'newPediatricCountFaers',
            "className": '',
            "mRender": function (data, type, row) {
                if (row['exe' + i]) {
                    return '<div><div class="stacked-cell-center-top">' + row['exe' + i].newPediatricCountFaers + '</div><div class="stacked-cell-center-top">' + row['exe' + i].cummPediatricCountFaers + '</div></div>'
                } else {
                    return '<div></div>'
                }
            },
            "visible": signal.fieldManagement.visibleColumns('exe' + i + 'newPediatricCountFaers')
        }] : []);

        addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'newInteractingCountFaers'] ? [{
            "mData": 'exe' + i + 'newInteractingCountFaers',
            "className": '',
            "mRender": function (data, type, row) {
                if (row['exe' + i]) {
                    return '<div><div class="stacked-cell-center-top">' + row['exe' + i].newInteractingCountFaers + '</div><div class="stacked-cell-center-top">' + row['exe' + i].cummInteractingCountFaers + '</div></div>'
                } else {
                    return '<div></div>'
                }
            },
            "visible": signal.fieldManagement.visibleColumns('exe' + i + 'newInteractingCountFaers')
        }] : []);

        addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'newGeriatricCountFaers'] ? [{
            "mData": 'exe' + i + 'newGeriatricCountFaers',
            "className": '',
            "mRender": function (data, type, row) {
                if (row['exe' + i]) {
                    return '<div><div class="stacked-cell-center-top">' + row['exe' + i].newGeriatricCountFaers + '</div><div class="stacked-cell-center-top">' + row['exe' + i].cumGeriatricCountFaers + '</div></div>'
                } else {
                    return '<div></div>'
                }
            },
            "visible": signal.fieldManagement.visibleColumns('exe' + i + 'newGeriatricCountFaers')
        }] : []);

        addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'newNonSeriousFaers'] ? [{
            "mData": 'exe' + i + 'newNonSeriousFaers',
            "className": '',
            "mRender": function (data, type, row) {
                if (row['exe' + i]) {
                    return '<div><div class="stacked-cell-center-top">' + row['exe' + i].newNonSeriousFaers + '</div><div class="stacked-cell-center-top">' + row['exe' + i].cumNonSeriousFaers + '</div></div>'
                } else {
                    return '<div></div>'
                }
            },
            "visible": signal.fieldManagement.visibleColumns('exe' + i + 'newNonSeriousFaers')
        }] : []);


        if (showFaersPrr) {
            addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'prrValueFaers'] ? [{
                "mData": 'exe' + i + 'prrValueFaers',
                "className": 'text-center',
                "mRender": function (data, type, row) {
                    return '<span>' + row['exe' + i].prrValueFaers + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('exe' + i + 'prrValueFaers')
            }] : []);
            addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'prrLCIFaers'] ? [{
                "mData": 'exe' + i + 'prrLCIFaers',
                "mRender": function (data, type, row) {
                    return '<div><div class="stacked-cell-center-top">' + row['exe' + i].prrLCIFaers + '</div><div class="stacked-cell-center-top">' + row['exe' + i].prrUCIFaers + '</div></div>'
                },
                "visible": signal.fieldManagement.visibleColumns('exe' + i + 'prrLCIFaers')
            }] : []);

        }
        if (showFaersRor) {
            addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'rorValueFaers'] ? [{
                "mData": 'exe' + i + 'rorValueFaers',
                "className": 'text-center',
                "mRender": function (data, type, row) {
                    return '<span>' + row['exe' + i].rorValueFaers + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('exe' + i + 'rorValueFaers')
            }] : []);

            addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'rorLCIFaers'] ? [{
                "mData": 'exe' + i + 'rorLCIFaers',
                "mRender": function (data, type, row) {
                    return '<div><div class="stacked-cell-center-top">' + row['exe' + i].rorLCIFaers + '</div><div class="stacked-cell-center-top">' + row['exe' + i].rorUCIFaers + '</div></div>'
                },
                "visible": signal.fieldManagement.visibleColumns('exe' + i + 'rorLCIFaers')
            }] : []);

        }

        if (showFaersEbgm) {

            addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'ebgmFaers'] ? [{
                "mData": 'exe' + i + 'ebgmFaers',
                "className": 'text-center',
                "mRender": function (data, type, row) {
                    return '<span>' + row['exe' + i].ebgmFaers + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('exe' + i + 'ebgmFaers')
            }] : []);

            addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'eb05Faers'] ? [{
                "mData": 'exe' + i + 'eb05Faers',
                "className": '',
                "mRender": function (data, type, row) {
                    return '<div><div class="stacked-cell-center-top">' + row['exe' + i].eb05Faers + '</div><div class="stacked-cell-center-top">' + row['exe' + i].eb95Faers + '</div></div>'
                },
                "visible": signal.fieldManagement.visibleColumns('exe' + i + 'eb05Faers')
            }] : []);


        }


        if (showFaersPrr && showFaersRor) {

            addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'chiSquareFaers'] ? [{
                "mData": 'exe' + i + 'chiSquareFaers',
                "className": 'text-center',
                "mRender": function (data, type, row) {
                    if (row['exe' + i]) {
                        return '<span>' + row['exe' + i].chiSquareFaers + '</span>'
                    } else {
                        return '<div></div>'
                    }
                },
                "visible": signal.fieldManagement.visibleColumns('exe' + i + 'chiSquareFaers')
            }] : []);

        }
    }

    if (isVaersEnabled) {
        addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'newSeriousCountVaers'] ? [{
            "mData": 'exe' + i + 'newSeriousCountVaers',
            "className": '',
            "mRender": function (data, type, row) {
                if (typeof row['exe' + i].newSeriousCountVaers === 'undefined' && typeof row['exe' + i].cumSeriousCountVaers !== 'undefined') {
                    row['exe' + i].newSeriousCountVaers = '-';
                } else if (typeof row['exe' + i].newSeriousCountVaers !== 'undefined' && typeof row['exe' + i].cumSeriousCountVaers === 'undefined') {
                    row['exe' + i].cumSeriousCountVaers = '-';
                } else if (typeof row['exe' + i].newSeriousCountVaers === 'undefined' && typeof row['exe' + i].cumSeriousCountVaers === 'undefined') {
                    row['exe' + i].newSeriousCountVaers = '-';
                    row['exe' + i].cumSeriousCountVaers = '-';
                }
                return '<div><div class="stacked-cell-center-top">' + row['exe' + i].newSeriousCountVaers + '</div><div class="stacked-cell-center-top">' + row['exe' + i].cumSeriousCountVaers + '</div></div>'
            },
            "visible": signal.fieldManagement.visibleColumns('exe' + i + 'newSeriousCountVaers')
        }] : []);

        addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'newFatalCountVaers'] ? [{
            "mData": 'exe' + i + 'newFatalCountVaers',
            "className": '',
            "mRender": function (data, type, row) {
                if (typeof row['exe' + i].newFatalCountVaers === 'undefined' && typeof row['exe' + i].cumFatalCountVaers !== 'undefined') {
                    row['exe' + i].newFatalCountVaers = '-';
                } else if (typeof row['exe' + i].newFatalCountVaers !== 'undefined' && typeof row['exe' + i].cumFatalCountVaers === 'undefined') {
                    row['exe' + i].cumFatalCountVaers = '-';
                } else if (typeof row['exe' + i].newFatalCountVaers === 'undefined' && typeof row['exe' + i].cumFatalCountVaers === 'undefined') {
                    row['exe' + i].newFatalCountVaers = '-';
                    row['exe' + i].cumFatalCountVaers = '-';
                }
                return '<div><div class="stacked-cell-center-top">' + row['exe' + i].newFatalCountVaers + '</div><div class="stacked-cell-center-top">' + row['exe' + i].cumFatalCountVaers + '</div></div>'
            },
            "visible": signal.fieldManagement.visibleColumns('exe' + i + 'newFatalCountVaers')
        }] : []);

        addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'newCountVaers'] ? [{
            "mData": 'exe' + i + 'newCountVaers',
            "className": '',
            "mRender": function (data, type, row) {
                if (typeof row['exe' + i].newCountVaers === 'undefined' && typeof row['exe' + i].cummCountVaers !== 'undefined') {
                    row['exe' + i].newCountVaers = '-';
                } else if (typeof row['exe' + i].newCountVaers !== 'undefined' && typeof row['exe' + i].cummCountVaers === 'undefined') {
                    row['exe' + i].cummCountVaers = '-';
                } else if (typeof row['exe' + i].newCountVaers === 'undefined' && typeof row['exe' + i].cummCountVaers === 'undefined') {
                    row['exe' + i].newCountVaers = '-';
                    row['exe' + i].cummCountVaers = '-';
                }
                return '<div><div class="stacked-cell-center-top">' + row['exe' + i].newCountVaers + '</div><div class="stacked-cell-center-top">' + row['exe' + i].cummCountVaers + '</div></div>'
            },
            "visible": signal.fieldManagement.visibleColumns('exe' + i + 'newCountVaers')
        }] : []);

        addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'newPediatricCountVaers'] ? [{
            "mData": 'exe' + i + 'newPediatricCountVaers',
            "className": '',
            "mRender": function (data, type, row) {
                if (typeof row['exe' + i].newPediatricCountVaers === 'undefined' && typeof row['exe' + i].cummPediatricCountVaers !== 'undefined') {
                    row['exe' + i].newPediatricCountVaers = '-';
                } else if (typeof row['exe' + i].newPediatricCountVaers !== 'undefined' && typeof row['exe' + i].cummPediatricCountVaers === 'undefined') {
                    row['exe' + i].cummPediatricCountVaers = '-';
                } else if (typeof row['exe' + i].newPediatricCountVaers === 'undefined' && typeof row['exe' + i].cummPediatricCountVaers === 'undefined') {
                    row['exe' + i].newPediatricCountVaers = '-';
                    row['exe' + i].cummPediatricCountVaers = '-';
                }
                return '<div><div class="stacked-cell-center-top">' + row['exe' + i].newPediatricCountVaers + '</div><div class="stacked-cell-center-top">' + row['exe' + i].cummPediatricCountVaers + '</div></div>'
            },
            "visible": signal.fieldManagement.visibleColumns('exe' + i + 'newPediatricCountVaers')
        }] : []);

        addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'newGeriatricCountVaers'] ? [{
            "mData": 'exe' + i + 'newGeriatricCountVaers',
            "className": '',
            "mRender": function (data, type, row) {
                if (typeof row['exe' + i].newGeriatricCountVaers === 'undefined' && typeof row['exe' + i].cumGeriatricCountVaers !== 'undefined') {
                    row['exe' + i].newGeriatricCountVaers = '-';
                } else if (typeof row['exe' + i].newGeriatricCountVaers !== 'undefined' && typeof row['exe' + i].cumGeriatricCountVaers === 'undefined') {
                    row['exe' + i].cumGeriatricCountVaers = '-';
                } else if (typeof row['exe' + i].newGeriatricCountVaers === 'undefined' && typeof row['exe' + i].cumGeriatricCountVaers === 'undefined') {
                    row['exe' + i].newGeriatricCountVaers = '-';
                    row['exe' + i].cumGeriatricCountVaers = '-';
                }
                return '<div><div class="stacked-cell-center-top">' + row['exe' + i].newGeriatricCountVaers + '</div><div class="stacked-cell-center-top">' + row['exe' + i].cumGeriatricCountVaers + '</div></div>'
            },
            "visible": signal.fieldManagement.visibleColumns('exe' + i + 'newGeriatricCountVaers')
        }] : []);


        if (showVaersPrr) {
            addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'prrValueVaers'] ? [{
                "mData": 'exe' + i + 'prrValueVaers',
                "className": 'text-center',
                "mRender": function (data, type, row) {
                    if (typeof row['exe' + i].prrValueVaers === 'undefined') {
                        row['exe' + i].prrValueVaers = '-';
                    }
                    return '<span>' + row['exe' + i].prrValueVaers + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('exe' + i + 'prrValueVaers')
            }] : []);

            addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'prrLCIVaers'] ? [{
                "mData": 'exe' + i + 'prrLCIVaers',
                "mRender": function (data, type, row) {
                    if (typeof row['exe' + i].prrLCIVaers === 'undefined' && typeof row['exe' + i].prrUCIVaers !== 'undefined') {
                        row['exe' + i].prrLCIVaers = '-';
                    } else if (typeof row['exe' + i].prrLCIVaers !== 'undefined' && typeof row['exe' + i].prrUCIVaers === 'undefined') {
                        row['exe' + i].prrUCIVaers = '-';
                    } else if (typeof row['exe' + i].prrLCIVaers === 'undefined' && typeof row['exe' + i].prrUCIVaers === 'undefined') {
                        row['exe' + i].prrLCIVaers = '-';
                        row['exe' + i].prrUCIVaers = '-';
                    }
                    return '<div><div class="stacked-cell-center-top">' + row['exe' + i].prrLCIVaers + '</div><div class="stacked-cell-center-top">' + row['exe' + i].prrUCIVaers + '</div></div>'
                },
                "visible": signal.fieldManagement.visibleColumns('exe' + i + 'prrLCIVaers')
            }] : []);

        }
        if (showVaersRor) {
            addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'rorValueVaers'] ? [{
                "mData": 'exe' + i + 'rorValueVaers',
                "className": 'text-center',
                "mRender": function (data, type, row) {
                    if (typeof row['exe' + i].rorValueVaers === 'undefined') {
                        row['exe' + i].rorValueVaers = '-';
                    }
                    return '<span>' + row['exe' + i].rorValueVaers + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('exe' + i + 'rorValueVaers')
            }] : []);

            addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'rorLCIVaers'] ? [{
                "mData": 'exe' + i + 'rorLCIVaers',
                "mRender": function (data, type, row) {
                    if (typeof row['exe' + i].rorLCIVaers === 'undefined' && typeof row['exe' + i].rorUCIVaers !== 'undefined') {
                        row['exe' + i].rorLCIVaers = '-';
                    } else if (typeof row['exe' + i].rorLCIVaers !== 'undefined' && typeof row['exe' + i].rorUCIVaers === 'undefined') {
                        row['exe' + i].rorUCIVaers = '-';
                    } else if (typeof row['exe' + i].rorLCIVaers === 'undefined' && typeof row['exe' + i].rorUCIVaers === 'undefined') {
                        row['exe' + i].rorLCIVaers = '-';
                        row['exe' + i].rorUCIVaers = '-';
                    }
                    return '<div><div class="stacked-cell-center-top">' + row['exe' + i].rorLCIVaers + '</div><div class="stacked-cell-center-top">' + row['exe' + i].rorUCIVaers + '</div></div>'
                },
                "visible": signal.fieldManagement.visibleColumns('exe' + i + 'rorLCIVaers')
            }] : []);

        }

        if (showVaersEbgm) {

            addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'ebgmVaers'] ? [{
                "mData": 'exe' + i + 'ebgmVaers',
                "className": 'text-center',
                "mRender": function (data, type, row) {
                    if (typeof row['exe' + i].ebgmVaers === 'undefined') {
                        row['exe' + i].ebgmVaers = '-';
                    }
                    return '<span>' + row['exe' + i].ebgmVaers + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('exe' + i + 'ebgmVaers')
            }] : []);

            addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'eb05Vaers'] ? [{
                "mData": 'exe' + i + 'eb05Vaers',
                "className": 'text-center',
                "mRender": function (data, type, row) {
                    if (typeof row['exe' + i].eb05Vaers === 'undefined' && typeof row['exe' + i].eb95Vaers !== 'undefined') {
                        row['exe' + i].eb05Vaers = '-';
                    } else if (typeof row['exe' + i].eb05Vaers !== 'undefined' && typeof row['exe' + i].eb95Vaers === 'undefined') {
                        row['exe' + i].eb95Vaers = '-';
                    } else if (typeof row['exe' + i].eb05Vaers === 'undefined' && typeof row['exe' + i].eb95Vaers === 'undefined') {
                        row['exe' + i].eb05Vaers = '-';
                        row['exe' + i].eb95Vaers = '-';
                    }
                    return '<div><div class="stacked-cell-center-top">' + row['exe' + i].eb05Vaers + '</div><div class="stacked-cell-center-top">' + row['exe' + i].eb95Vaers + '</div></div>'
                },
                "visible": signal.fieldManagement.visibleColumns('exe' + i + 'eb05Vaers')
            }] : []);


        }

        if (showVaersPrr && showVaersRor) {
            addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'chiSquareVaers'] ? [{
                "mData": 'exe' + i + 'chiSquareVaers',
                "className": 'text-center',
                "mRender": function (data, type, row) {
                    if (typeof row['exe' + i].chiSquareVaers === 'undefined') {
                        row['exe' + i].chiSquareVaers = '-';
                    }
                    return '<span>' + row['exe' + i].chiSquareVaers + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('exe' + i + 'chiSquareVaers')
            }] : []);
        }
    }


    if (isAgeEnabled) {
        $.each(subGroupsMap.AGE_GROUP, function (key, value) {
            addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'ebgm' + value] ? makeSubGroupColumnsPrev(value, "ebgm", 'exe' + i, "ebgmAge") : []);
            addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'eb05' + value] ? makeSubGroupColumnsPrev(value, "eb05", 'exe' + i, "eb05Age") : []);
            addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'eb95' + value] ? makeSubGroupColumnsPrev(value, "eb95", 'exe' + i, "eb95Age") : []);
        });
    }
    if (isGenderEnabled) {
        $.each(subGroupsMap.GENDER, function (key, value) {
            addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'ebgm' + value] ? makeSubGroupColumnsPrev(value, "ebgm", 'exe' + i, "ebgmGender") : []);
            addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'eb05' + value] ? makeSubGroupColumnsPrev(value, "eb05", 'exe' + i, "eb05Gender") : []);
            addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'eb95' + value] ? makeSubGroupColumnsPrev(value, "eb95", 'exe' + i, "eb95Gender") : []);
        });
    }
    if (allSubGroupMap && allSubGroupMap !== []) {
        $.each(allSubGroupMap, function (key, map) {
            $.each(map, function (subGroup, value) {
                $.each(value, function (index, column) {
                    addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + key + column] ? makeAllSubGroupColumnsPrev(key, column, 'exe' + i, subGroup) : []);
                });
            });
        });
    }
    if (rorRelSubGrpEnabled && allSubGroupMap && allSubGroupMap !== []) {
        $.each(allSubGroupMap, function (key, map) {
            if (key.toLowerCase().startsWith('ror')) {
                $.each(map, function (subGroup, value) {
                    $.each(value, function (index, column) {
                        addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + key.toString() + "Rel" + column] ? makeAllSubGroupColumnsPrev(key.toString() + "Rel", column, 'exe' + i, subGroup) : []);
                    });
                });
            }
        });
    }

    if (isFaersEnabled) {
        $.each(subGroupsMap.AGE_GROUP_FAERS, function (key, value) {
            addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + "ebgm" + value + "Faers"] ? makeSubGroupColumnsPrev(value + "Faers", "ebgm", 'exe' + i, "ebgmAgeFaers") : []);

            addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + "eb05" + value + "Faers"] ? makeSubGroupColumnsPrev(value + "Faers", "eb05", 'exe' + i, "eb05AgeFaers") : []);
            addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + "eb95" + value + "Faers"] ? makeSubGroupColumnsPrev(value + "Faers", "eb95", 'exe' + i, "eb95AgeFaers") : []);
        });

        $.each(subGroupsMap.GENDER_FAERS, function (key, value) {
            addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + "ebgm" + value + "Faers"] ? makeSubGroupColumnsPrev(value + "Faers", "ebgm", 'exe' + i, "ebgmGenderFaers") : []);
            addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + "eb05" + value + "Faers"] ? makeSubGroupColumnsPrev(value + "Faers", "eb05", 'exe' + i, "eb05GenderFaers") : []);
            addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + "eb95" + value + "Faers"] ? makeSubGroupColumnsPrev(value + "Faers", "eb95", 'exe' + i, "eb95GenderFaers") : []);
        });
    }
    return addColumns
}

function makeAoColumnsVigibase(i, prrVisibility, rorVisibility, ebgmVisibility, dssVisibilty) {
    var addColumns = [];
    var labelConfigCopyJson = $("#labelConfigCopyJson").val()
    labelConfigCopyJson = JSON.parse(labelConfigCopyJson)

    if (isVigibaseEnabled) {
        addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'newSeriousCountVigibase'] ? [
            {
                "mData": 'exe' + i + 'newSeriousCountVigibase',
                "className": 'text-center',
                "mRender": function (data, type, row) {
                    if (typeof row['exe' + i].newSeriousCountVigibase === 'undefined' && typeof row['exe' + i].cumSeriousCountVigibase !== 'undefined') {
                        row['exe' + i].newSeriousCountVigibase = '-';
                    } else if (typeof row['exe' + i].newSeriousCountVigibase !== 'undefined' && typeof row['exe' + i].cumSeriousCountVigibase === 'undefined') {
                        row['exe' + i].cumSeriousCountVigibase = '-';
                    } else if (typeof row['exe' + i].newSeriousCountVigibase === 'undefined' && typeof row['exe' + i].cumSeriousCountVigibase === 'undefined') {
                        row['exe' + i].newSeriousCountVigibase = '-';
                        row['exe' + i].cumSeriousCountVigibase = '-';
                    }
                    return '<div><div class="stacked-cell-center-top">' + row['exe' + i].newSeriousCountVigibase + '</div><div class="stacked-cell-center-top">' + row['exe' + i].cumSeriousCountVigibase + '</div></div>'
                },
                "visible": signal.fieldManagement.visibleColumns('exe' + i + 'newSeriousCountVigibase')
            }] : []);

        addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'newFatalCountVigibase'] ? [{
            "mData": 'exe' + i + 'newFatalCountVigibase',
            "className": 'text-center',
            "mRender": function (data, type, row) {
                if (typeof row['exe' + i].newFatalCountVigibase === 'undefined' && typeof row['exe' + i].cumFatalCountVigibase !== 'undefined') {
                    row['exe' + i].newFatalCountVigibase = '-';
                } else if (typeof row['exe' + i].newFatalCountVigibase !== 'undefined' && typeof row['exe' + i].cumFatalCountVigibase === 'undefined') {
                    row['exe' + i].cumFatalCountVigibase = '-';
                } else if (typeof row['exe' + i].newFatalCountVigibase === 'undefined' && typeof row['exe' + i].cumFatalCountVigibase === 'undefined') {
                    row['exe' + i].newFatalCountVigibase = '-';
                    row['exe' + i].cumFatalCountVigibase = '-';
                }
                return '<div><div class="stacked-cell-center-top">' + row['exe' + i].newFatalCountVigibase + '</div><div class="stacked-cell-center-top">' + row['exe' + i].cumFatalCountVigibase + '</div></div>'
            },
            "visible": signal.fieldManagement.visibleColumns('exe' + i + 'newFatalCountVigibase')
        }] : []);

        addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'newCountVigibase'] ? [{
            "mData": 'exe' + i + 'newCountVigibase',
            "className": 'text-center',
            "mRender": function (data, type, row) {
                if (typeof row['exe' + i].newCountVigibase === 'undefined' && typeof row['exe' + i].cummCountVigibase !== 'undefined') {
                    row['exe' + i].newCountVigibase = '-';
                } else if (typeof row['exe' + i].newCountVigibase !== 'undefined' && typeof row['exe' + i].cummCountVigibase === 'undefined') {
                    row['exe' + i].cummCountVigibase = '-';
                } else if (typeof row['exe' + i].newCountVigibase === 'undefined' && typeof row['exe' + i].cummCountVigibase === 'undefined') {
                    row['exe' + i].newCountVigibase = '-';
                    row['exe' + i].cummCountVigibase = '-';
                }
                return '<div><div class="stacked-cell-center-top">' + row['exe' + i].newCountVigibase + '</div><div class="stacked-cell-center-top">' + row['exe' + i].cummCountVigibase + '</div></div>'
            },
            "visible": signal.fieldManagement.visibleColumns('exe' + i + 'newCountVigibase')
        }] : []);

        addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'newPediatricCountVigibase'] ? [{
            "mData": 'exe' + i + 'newPediatricCountVigibase',
            "className": 'text-center',
            "mRender": function (data, type, row) {
                if (typeof row['exe' + i].newPediatricCountVigibase === 'undefined' && typeof row['exe' + i].cummPediatricCountVigibase !== 'undefined') {
                    row['exe' + i].newPediatricCountVigibase = '-';
                } else if (typeof row['exe' + i].newPediatricCountVigibase !== 'undefined' && typeof row['exe' + i].cummPediatricCountVigibase === 'undefined') {
                    row['exe' + i].cummPediatricCountVigibase = '-';
                } else if (typeof row['exe' + i].newPediatricCountVigibase === 'undefined' && typeof row['exe' + i].cummPediatricCountVigibase === 'undefined') {
                    row['exe' + i].newPediatricCountVigibase = '-';
                    row['exe' + i].cummPediatricCountVigibase = '-';
                }
                return '<div><div class="stacked-cell-center-top">' + row['exe' + i].newPediatricCountVigibase + '</div><div class="stacked-cell-center-top">' + row['exe' + i].cummPediatricCountVigibase + '</div></div>'
            },
            "visible": signal.fieldManagement.visibleColumns('exe' + i + 'newPediatricCountVigibase')
        }] : []);

        addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'newGeriatricCountVigibase'] ? [{
            "mData": 'exe' + i + 'newGeriatricCountVigibase',
            "className": '',
            "mRender": function (data, type, row) {
                if (typeof row['exe' + i].newGeriatricCountVigibase === 'undefined' && typeof row['exe' + i].cumGeriatricCountVigibase !== 'undefined') {
                    row['exe' + i].newGeriatricCountVigibase = '-';
                } else if (typeof row['exe' + i].newGeriatricCountVigibase !== 'undefined' && typeof row['exe' + i].cumGeriatricCountVigibase === 'undefined') {
                    row['exe' + i].cumGeriatricCountVigibase = '-';
                } else if (typeof row['exe' + i].newGeriatricCountVigibase === 'undefined' && typeof row['exe' + i].cumGeriatricCountVigibase === 'undefined') {
                    row['exe' + i].newGeriatricCountVigibase = '-';
                    row['exe' + i].cumGeriatricCountVigibase = '-';
                }
                return '<div><div class="stacked-cell-center-top">' + row['exe' + i].newGeriatricCountVigibase + '</div><div class="stacked-cell-center-top">' + row['exe' + i].cumGeriatricCountVigibase + '</div></div>'
            },
            "visible": signal.fieldManagement.visibleColumns('exe' + i + 'newGeriatricCountVigibase')
        }] : []);

        if (showVigibasePrr) {
            addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'prrValueVigibase'] ? [{
                "mData": 'exe' + i + 'prrValueVigibase',
                "className": 'text-center',
                "mRender": function (data, type, row) {
                    if (typeof row['exe' + i].prrValueVigibase === 'undefined') {
                        row['exe' + i].prrValueVigibase = '-';
                    }
                    return '<span>' + row['exe' + i].prrValueVigibase + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('exe' + i + 'prrValueVigibase')
            }] : []);

            addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'prrLCIVigibase'] ? [
                {
                    "mData": 'exe' + i + 'prrLCIVigibase',
                    "mRender": function (data, type, row) {
                        if (typeof row['exe' + i].prrLCIVigibase === 'undefined' && typeof row['exe' + i].prrUCIVigibase !== 'undefined') {
                            row['exe' + i].prrLCIVigibase = '-';
                        } else if (typeof row['exe' + i].prrLCIVigibase !== 'undefined' && typeof row['exe' + i].prrUCIVigibase === 'undefined') {
                            row['exe' + i].prrUCIVigibase = '-';
                        } else if (typeof row['exe' + i].prrLCIVigibase === 'undefined' && typeof row['exe' + i].prrUCIVigibase === 'undefined') {
                            row['exe' + i].prrLCIVigibase = '-';
                            row['exe' + i].prrUCIVigibase = '-';
                        }
                        return '<div><div class="stacked-cell-center-top">' + row['exe' + i].prrLCIVigibase + '</div><div class="stacked-cell-center-top">' + row['exe' + i].prrUCIVigibase + '</div></div>'
                    },
                    "visible": signal.fieldManagement.visibleColumns('exe' + i + 'prrLCIVigibase')
                }] : []);
        }
        if (showVigibaseRor) {
            addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'rorValueVigibase'] ? [{
                "mData": 'exe' + i + 'rorValueVigibase',
                "className": 'text-center',
                "mRender": function (data, type, row) {
                    if (typeof row['exe' + i].rorValueVigibase === 'undefined') {
                        row['exe' + i].rorValueVigibase = '-';
                    }
                    return '<span>' + row['exe' + i].rorValueVigibase + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('exe' + i + 'rorValueVigibase')
            }] : []);

            addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'rorLCIVigibase'] ? [
                {
                    "mData": 'exe' + i + 'rorLCIVigibase',
                    "mRender": function (data, type, row) {
                        if (typeof row['exe' + i].rorLCIVigibase === 'undefined' && typeof row['exe' + i].rorUCIVigibase !== 'undefined') {
                            row['exe' + i].rorLCIVigibase = '-';
                        } else if (typeof row['exe' + i].rorLCIVigibase !== 'undefined' && typeof row['exe' + i].rorUCIVigibase === 'undefined') {
                            row['exe' + i].rorUCIVigibase = '-';
                        } else if (typeof row['exe' + i].rorLCIVigibase === 'undefined' && typeof row['exe' + i].rorUCIVigibase === 'undefined') {
                            row['exe' + i].rorLCIVigibase = '-';
                            row['exe' + i].rorUCIVigibase = '-';
                        }
                        return '<div><div class="stacked-cell-center-top">' + row['exe' + i].rorLCIVigibase + '</div><div class="stacked-cell-center-top">' + row['exe' + i].rorUCIVigibase + '</div></div>'
                    },
                    "visible": signal.fieldManagement.visibleColumns('exe' + i + 'rorLCIVigibase')
                }] : []);
        }

        if (showVigibaseEbgm) {
            addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'ebgmVigibase'] ? [{
                "mData": 'exe' + i + 'ebgmVigibase',
                "className": 'text-center',
                "mRender": function (data, type, row) {
                    if (typeof row['exe' + i].ebgmVigibase === 'undefined') {
                        row['exe' + i].ebgmVigibase = '-';
                    }
                    return '<span>' + row['exe' + i].ebgmVigibase + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('exe' + i + 'ebgmVigibase')
            }] : []);

            addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'eb05Vigibase'] ? [
                {
                    "mData": 'exe' + i + 'eb05Vigibase',
                    "className": 'text-center',
                    "mRender": function (data, type, row) {
                        if (typeof row['exe' + i].eb05Vigibase === 'undefined' && typeof row['exe' + i].eb95Vigibase !== 'undefined') {
                            row['exe' + i].eb05Vigibase = '-';
                        } else if (typeof row['exe' + i].eb05Vigibase !== 'undefined' && typeof row['exe' + i].eb95Vigibase === 'undefined') {
                            row['exe' + i].eb95Vigibase = '-';
                        } else if (typeof row['exe' + i].eb05Vigibase === 'undefined' && typeof row['exe' + i].eb95Vigibase === 'undefined') {
                            row['exe' + i].eb05Vigibase = '-';
                            row['exe' + i].eb95Vigibase = '-';
                        }
                        return '<div><div class="stacked-cell-center-top">' + row['exe' + i].eb05Vigibase + '</div><div class="stacked-cell-center-top">' + row['exe' + i].eb95Vigibase + '</div></div>'
                    },
                    "visible": signal.fieldManagement.visibleColumns('exe' + i + 'eb05Vigibase')
                }] : []);
        }

        if (showVigibasePrr && showVigibaseRor) {
            addColumns.push.apply(addColumns, labelConfigCopyJson['exe' + i + 'chiSquareVigibase'] ? [{
                "mData": 'exe' + i + 'chiSquareVigibase',
                "className": 'text-center',
                "mRender": function (data, type, row) {
                    if (typeof row['exe' + i].chiSquareVigibase === 'undefined') {
                        row['exe' + i].chiSquareVigibase = '-';
                    }
                    return '<span>' + row['exe' + i].chiSquareVigibase + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('exe' + i + 'chiSquareVigibase')
            }
            ] : []);
        }
    }
    return addColumns;
}

function makeAoColumnsJaderPrevious(jaderColumns,i) {
    var addColumns = [];
        $.each(jaderColumns, function (key, map) {
           if(map.type === "count"){
               addColumns.push.apply(addColumns,[{
                   "mData": 'exe' + i + map.name,
                   "className": 'text-center',
                   "mRender": function (data, type, row) {
                       if (typeof row['exe' + i][map.name] === 'undefined') {
                           row['exe' + i][map.name] = '-';
                       }
                       return '<span>' + row['exe' + i][map.name] + '</span>'
                   },
                   "visible": signal.fieldManagement.visibleColumns('exe' + i + map.name),
                   "orderable": false,
               }]);

           }else if(map.type === "countStacked"){
               addColumns.push.apply(addColumns, [
                   {
                       "mData": 'exe' + i + map.name,
                       "className": '',
                       "mRender": function (data, type, row) {
                           if (typeof row['exe' + i][map.name] === 'undefined' && typeof row['exe' + i][map.secondaryName] !== 'undefined') {
                               row['exe' + i][map.name] = '-';
                           } else if (typeof row['exe' + i][key.name] !== 'undefined' && typeof row['exe' + i][map.secondaryName] === 'undefined') {
                               row['exe' + i][map.secondaryName] = '-';
                           } else if (typeof row['exe' + i][key.name] === 'undefined' && typeof row['exe' + i][map.secondaryName] === 'undefined') {
                               row['exe' + i][map.name] = '-';
                               row['exe' + i][map.secondaryName] = '-';
                           }
                           return '<div><div class="stacked-cell-center-top">' + row['exe' + i][map.name] + '</div><div class="stacked-cell-center-top">' + row['exe' + i][map.secondaryName] + '</div></div>'
                       },
                       "visible": signal.fieldManagement.visibleColumns('exe' + i + map.name),
                       "orderable": false
                   }]);

           }
        });
    return addColumns;
}

function makeEvdasPrevColumns(i, field) {
    var addColumns = [];
    var labelConfigCopyJson = $("#labelConfigCopyJson").val();
    labelConfigCopyJson = JSON.parse(labelConfigCopyJson);
    addColumns.push.apply(addColumns, labelConfigCopyJson["exe" + i + field + "Evdas"] ? [{
        "mData": "exe" + i + field + "Evdas",
        "className": '',
        "mRender": function (data, type, row) {
            if (row['exe' + i]) {
                let fieldArray = field.split(/(?=[A-Z])/)
                if (typeof row['exe' + i][field + "Evdas"] === 'undefined') {
                    row['exe' + i][field + "Evdas"] = '-';
                }
                if (typeof row['exe' + i]["total" + fieldArray[1] + "Evdas"] === 'undefined') {
                    row['exe' + i]["total" + fieldArray[1] + "Evdas"] = '-';
                }
                return '<div style="text-align: center">' + row['exe' + i][field + "Evdas"] + '</div>' +
                    '<div style="text-align: center">' + row['exe' + i]["total" + fieldArray[1] + "Evdas"] + '</div>';
            } else {
                return '<div></div>'
            }
        },
        "visible": signal.fieldManagement.visibleColumns("exe" + i + field + "Evdas")
    }
    ] : []);
    return addColumns
}

function jsUcfirst(string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
}

$(document).on('click', '.caseSeriesNotAccess', function (evt) {
    $.Notification.notify('error', 'top right', "Error", "You dont have access to choose this option", {autoHideDelay: 5000});
});

$(document).on('click', '.signalSummaryAuth', function (evt) {
    $.Notification.notify('warning', 'top right', "Warning", "You don't have access to view signal summary", {autoHideDelay: 5000});
});

$(document).on('mouseover', ".generation-tooltip", function () {
    var $this = $(this);
    var tooltipText = $this.attr("data-title");
    $this.tooltip({
        title: tooltipText,
        placement: 'left'
    });
    $(this).next().addClass('single-tooltip');
    $this.tooltip('show');
});

var fetchSubGroupsMap = function () {
    $.ajax({
        url: "/signal/aggregateCaseAlert/fetchSubGroupsMapIntegratedReview",
        async: false,
        dataType: 'json',
    })
        .done(function (data) {
            subGroupsMap = data.subGroupsMap;
            isAgeEnabled = data.isAgeEnabled;
            isGenderEnabled = data.isGenderEnabled;
            allSubGroupMap = data.allSubGroupMap;
            rorRelSubGrpEnabled = data.rorRelSubGrpEnabled;
        });
};

function makeSubGroupColumns(groupName, category, subGroup) {
    var addColumns = [
        {
            "mData": groupName + category,
            "className": 'text-center',
            "mRender": function (data, type, row) {
                var stringMap = row[subGroup];
                if (stringMap === '-')
                    map = [];
                else
                    var map = clobToMap(stringMap);
                var value = "";
                if (row.isFaersOnly) {
                    value = "-";
                } else if (map == null || map[category] == undefined)
                    value = "-";
                else if (map[category] === 0) {
                    value = "0.0"
                } else {
                    value = map[category];
                }
                return value
            },
            "visible": signal.fieldManagement.visibleColumns(groupName + category)
        }];
    return addColumns
}

function makeAllSubGroupColumns(groupName, category, subGroup) {
    var addColumns = [
        {
            "mData": groupName + category,
            "className": 'text-center',
            "mRender": function (data, type, row) {
                var map
                var stringMap = row[groupName + "SubGroup"];
                if (stringMap === '-' || stringMap === undefined) {
                    map = [];
                } else {
                    stringMap = JSON.parse(stringMap)
                    map = stringMap[subGroup];
                }
                var value = "";
                if (map == null || map[category] === undefined) {
                    value = "-";
                } else {
                    if (map[category] === 0) {
                        value = "0.0"
                    } else {
                        value = map[category];
                    }
                }
                return value
            },
            "visible": signal.fieldManagement.visibleColumns(groupName + category)
        }];
    return addColumns
}

function makeSubGroupFaersColumns(groupName, category, subGroup) {
    var addColumns = [
        {
            "mData": groupName,
            "className": 'text-center',
            "mRender": function (data, type, row) {
                var stringMap = row[subGroup];
                var map;
                if (stringMap == '-')
                    map = [];
                else {
                    map = clobToMap(stringMap)
                }
                var value = "";
                if (map == null || map[category] == undefined)
                    value = "-";
                else if (map[category] === 0) {
                    value = "0.0"
                } else {
                    value = map[category];
                }
                return value
            },
            "visible": signal.fieldManagement.visibleColumns(groupName)
        }];
    return addColumns
}

function makeSubGroupColumnsPrev(category, groupName, prevValue, subGroup) {
    var addColumns = [
        {
            "mData": prevValue + groupName + category,
            "className": 'text-center',
            "mRender": function (data, type, row) {
                if (row[prevValue]) {
                    if (subGroup.includes('Faers'))
                        category = category.replace('Faers', '');
                    var stringMap = row[prevValue][subGroup]
                    if (stringMap == '-' || stringMap === undefined)
                        map = [];
                    else
                        var map = clobToMap(stringMap)
                    var value = "";
                    if (map == null || map[category] == undefined) {
                        value = "-"
                    } else {
                        if (map[category] === 0) {
                            value = "0.0"
                        } else {
                            value = map[category];
                        }
                    }
                    return value
                }
            },
            "orderable": false,
            "visible": signal.fieldManagement.visibleColumns(prevValue + groupName + category)
        }];
    return addColumns
}

function makeAllSubGroupColumnsPrev(groupName, category, prevValue, subGroup) {
    var addColumns = [
        {
            "mData": prevValue + groupName + category,
            "className": 'text-center',
            "mRender": function (data, type, row) {
                var map
                if (row[prevValue]) {
                    var stringMap = row[prevValue][groupName + "SubGroup"];
                    if (stringMap === '-' || stringMap === undefined) {
                        map = [];
                    } else {
                        stringMap = JSON.parse(stringMap)
                        map = stringMap[subGroup];
                    }
                    var value = "";
                    if (map == null || map[category] === undefined)
                        value = "-";
                    else if (map[category] === 0) {
                        value = "0.0"
                    } else {
                        value = map[category];
                    }
                    return value
                }
            },
            "orderable": false,
            "visible": signal.fieldManagement.visibleColumns(prevValue + groupName + category)
        }];
    return addColumns
}

function clobToMap(clob) {
    clob = clob.replace('"', '');
    clob = clob.replace('"', ''); //TODO : need to investigate and remove this
    var list = clob.split(",");
    var map = [];
    $.each(list, function (ind, value) {
        map[value.split(":")[0].trim()] = (value.split(":").length > 1 && value.split(":")[1] != "null") ? value.split(":")[1] : "0";
    });
    return map
}

var fetchTags = function () {
    $.ajax({
        url: fetchCommonTagsUrl,
        async: false,
    })
        .done(function (result) {
            result.commonTagList.forEach(function (map) {
                tags.push({
                    id: map.id,
                    text: map.text,
                    parentId: map.parentId,
                    display: map.display
                });
            });
        })
        .fail(ajaxAuthroizationError)
}


