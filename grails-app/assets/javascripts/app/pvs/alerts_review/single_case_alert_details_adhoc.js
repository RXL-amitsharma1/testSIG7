//= require vendorUi/backbone/backbone-min
//= require app/pvs/common/rx_common.js
//= require app/pvs/common/rx_alert_utils.js
//= require app/pvs/common/rx_list_utils.js
//= require app/pvs/activity/activities.js
//= require app/pvs/actions/actions.js
//= require app/pvs/common/rx_handlebar_ext.js
//= require app/pvs/alertComments/alertComments.js
//= require app/pvs/alerts_review/alert_review.js
//= require app/pvs/common_tag.js

var userLocale = "en";
var applicationName = encodeToHTML("Single Case Alert on Demand");
var applicationLabel = encodeToHTML("QualitativeOnDemandAlert");
var table;
var executedIdList = [];
var tags = [];

var isDynamicFilterApplied = false;
var dir = '';
var index = -1;
var detailsParameters = {};
var isUpdateTemp = true;
var prev_page = [];
var paginationAdvancedFilterSingleAdhoc = true;
var scaAdhocEntriesCount=sessionStorage.getItem('scaAdhocPageEntries')!=null?sessionStorage.getItem('scaAdhocPageEntries'):50
var recordsFiltered = 0;
var fixedFilterColumn = [];
var newUrl;


$(function () {
    $(document).on('fieldsRearranged', function (e) {
        $('.yadcf-filter-wrapper').remove();
        init_filter($('#alertsDetailsTable').DataTable())
    })
    if($(".data-analysis-button").find("li[class!=hidden]").length === 0){
        $(".data-analysis-button").addClass("hidden");
    }
    if (window.sessionStorage.getItem('lastAdvancedFilter')) {
        window.sessionStorage.removeItem('lastAdvancedFilter')
    }
    var customFieldsEnabled = JSON.parse($("#customFieldsEnabledAdhoc").val());
    var fixedColumns = fixedColumnScaCount;
    var filterIndex = indexListSca;
    var caseNumberIndex = 1;
    filterIndex.splice(0,0,caseNumberIndex);
    var leftFixedColumns = 1;
    function showPopup(data) {
        var evdasDataLogModal = $("#downloadReportModal");
        $(evdasDataLogModal).find("#productName").val(data);
        evdasDataLogModal.modal("show");
    }

    var _changeInterval = null;
    $(document).on('keyup', 'input#yadcf-filter-alertsDetailsTable-'+caseNumberIndex, function (e) {

        var casenum=$(this).val();
        var caseInput = $(this);
        clearInterval(_changeInterval)
        _changeInterval = setInterval(function() {
            $("#alertsDetailsTable").DataTable().column(caseNumberIndex).search(casenum); //Exact value, column, reg
            if(casenum == "") {
                fixedFilterColumn.push({"column":caseNumberIndex, "value":casenum});
                caseInput.removeClass("inuse");
            } else {
                fixedFilterColumn.push({"column":caseNumberIndex, "value":casenum});
                caseInput.addClass("inuse");
            }
            clearInterval(_changeInterval)
        }, 1500);

    });


    var alertDetailsTable;
    var executedIdList = [];
    var activities_table;
    var checkedIdList = [];
    var checkedRowList = [];

    signal.alertReview.openAlertCommentModal("Single Case Alert", applicationName, applicationLabel, checkedIdList, checkedRowList);
    signal.alertReview.populateAdvancedFilterSelect(applicationName);
    signal.alertReview.setSortOrder();
    signal.list_utils.flag_handler("singleCaseAlert", "toggleFlag");
    //The case history modal.

    //apply the sorting to column based on saved view
    var sortingMap = signal.alertReview.createSortingMap('sca_adhoc_sortedColumn_' + executedConfigId, 'sca_adhoc_viewName_' + executedConfigId);
    if (sortingMap != 'undefined' && sortingMap.length > 0) {
        index = sortingMap[0][0];
        dir = sortingMap[0][1]
    }

    if(isTempViewSelected) {
        fetchViewInstanceList(true);
    } else{
        fetchViewInstanceList();
    }

    if (callingScreen == CALLING_SCREEN.REVIEW) {
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

    var alertType = applicationName;
    signal.alertReview.openSaveViewModal(filterIndex, alertType, viewId);
    var filterValue = signal.alertReview.createFilterMap('sca_adhoc_filterMap_' + executedConfigId, 'sca_adhoc_viewName_' + executedConfigId);

    $(window).on('beforeunload', function () {
        if (sessionStorage.getItem('isViewCall') == 'true' || $('.viewSelect :selected').text().replace("(default)", "").trim() != 'System View') {
            var backUrl = sessionStorage.getItem('back_url');
            sessionStorage.clear();
            sessionStorage.setItem('back_url', backUrl)
        } else {
            var viewInfo = signal.alertReview.generateViewInfo(filterIndex);
            sessionStorage.setItem('sca_adhoc_filterMap_' + executedConfigId, JSON.stringify(viewInfo.filterMap));
            sessionStorage.setItem('sca_adhoc_sortedColumn_' + executedConfigId, JSON.stringify(viewInfo.sortedColumn));
            sessionStorage.setItem('sca_adhoc_notVisibleColumn_' + executedConfigId, viewInfo.notVisibleColumn);
            sessionStorage.setItem('sca_adhoc_viewName_' + executedConfigId, $('.viewSelect :selected').text().replace("(default)", "").trim())
        }
    });

    signal.actions_utils.set_action_list_modal($('#action-modal'));
    signal.actions_utils.set_action_create_modal($('#action-create-modal'));


    $('#alert-list-filter-apply-bt').on('click', function (evt) {
        evt.preventDefault();
        var filterParams = $('#alert-list-filter').serialize();
        var theDataTable = $('#alertsDetailsTable').DataTable();
        $('#alert-list-filter').find('#filterUsed').val(true);
        var newUrl = listConfigUrl + "&" + filterParams + "&filterApplied=" + true;
        theDataTable.ajax.url(newUrl).load()
    });

    var initAlertDetailsTable = function () {
        var prefix = "sca_";
        var isFilterRequest = true;
        var filterValues = [];
        if (window.sessionStorage && signal.alertReview.isAlertPersistedInSessionStorage(prefix)) {
            filterValues = JSON.parse(sessionStorage.getItem(prefix + "filters_value"));
        } else {
            signal.alertReview.removeFiltersFromSessionStorage(prefix);
            isFilterRequest = false;
        }
        var aoColumns = create_single_case_table_columns(callingScreen, customFieldsEnabled);
        $(document).on('click', '#alertsDetailsTable_paginate', function () {
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
            fixedColumns:{
                start:fixedColumns?fixedColumns:2
            },
            customProcessing: true,
            drawCallback: function (settings) {
                var current_page = $('li.active').text().slice(-3).trim();
                if(typeof prev_page != 'undefined' && $.inArray(current_page,prev_page) == -1){
                    $(".alert-select-all").prop('checked',false);
                } else {
                    $(".alert-select-all").prop('checked',true);
                }
                var rowsDataAD = $('#alertsDetailsTable').DataTable().rows().data();
                if(settings.json != undefined) {
                    recordsFiltered = settings.json.recordsFiltered
                    pageDictionaryForAlertDetails($('#alertsDetailsTable_wrapper')[0], settings.aLengthMenu[0][0], settings.json.recordsFiltered);

                }else {
                    pageDictionaryForAlertDetails($('#alertsDetailsTable_wrapper')[0], 50, rowsDataAD.length);
                }
                initPSGrid($('#alertsDetailsTable_wrapper'));
                if (typeof settings.json != "undefined") {
                    $("#fullCaseList").val(JSON.stringify(settings.json.fullCaseList));
                    if (!isDynamicFilterApplied) {
                        isDynamicFilterApplied = true;
                        signal.alertReview.bindGridDynamicFilters(settings.json.filters, prefix, settings.json.configId);
                    }
                }
                tagEllipsis($('#alertsDetailsTable'));
                colEllipsis();
                webUiPopInit();
                webUiPopInitForCategories();
                closeInfoPopover();
                showInfoPopover();
                closePopupOnScroll();
                enterKeyAlertDetail();
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
                populateSelectedCasesAdhoc();

                var checkedId
                $('input[type=checkbox]').each(function () {
                    checkedId = $(this).attr('data-id')
                    if (alertIdSet.has(checkedId)) {
                        $(this).prop('checked', true);
                    }
                });
                $('.dt-pagination').on('change', function () {
                    var countVal = $('.dt-pagination').val()
                    sessionStorage.setItem("scaAdhocPageEntries", countVal);
                    scaAdhocEntriesCount = sessionStorage.getItem("scaAdhocPageEntries");
                })
            },
            "ajax": {
                "url": createAlertListUrl(isFilterRequest, filterValues),
                "type": "POST",
                "dataSrc": "aaData",
                "cache": false,
                "data": function (d) {
                    if (index != -1) {
                        if (typeof d.columns[index] !== "undefined") {
                            d.sort = d.columns[index].data;
                        }
                        d.direction = dir;
                    }
                    $("#alertsDetailsTable_paginate").on('click', function () {
                        paginationAdvancedFilterSingleAdhoc = true;
                    });
                    if ($('#advanced-filter').val()) {
                        paginationAdvancedFilterSingleAdhoc = false;
                        d.advancedFilterId = $('#advanced-filter').val()
                    }
                    if ($('#queryJSON').val()) {
                        d.queryJSON = $('#queryJSON').val()
                        sessionStorage.setItem('lastAdvancedFilter', $('#queryJSON').val());
                    } else if (sessionStorage.getItem('lastAdvancedFilter')) {
                        if (paginationAdvancedFilterSingleAdhoc) {
                            d.queryJSON = sessionStorage.getItem('lastAdvancedFilter');
                        } else {
                            sessionStorage.removeItem('lastAdvancedFilter');
                        }
                    }
                    d.callingScreen = callingScreen;
                    detailsParameters = d;
                }
            },
            initComplete: function (settings, json) {
                if (filterValue.length === 0 && !isDynamicFilterApplied) {
                    isDynamicFilterApplied = true;
                    signal.alertReview.bindGridDynamicFilters(json.filters, prefix, json.configId);
                }
                if (sessionStorage.getItem(prefix + "filters_store")) {
                    setQuickDispositionFilter(prefix);
                }
                signal.alertReview.enableMenuTooltips();
                signal.alertReview.disableTooltips();
                addGridShortcuts('#alertsDetailsTable');
                showTempViewModal(false);
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
                "sLengthMenu": "Show _MENU_",
            },

            "aLengthMenu": [[50, 100, 200, 500], [50, 100, 200, 500]],
            "iTotalDisplayRecords": "50",
            "iDisplayLength": parseInt(scaAdhocEntriesCount),
            "aoColumns": aoColumns,
            "responsive": true,
            "deferLoading": filterValue.length > 0 ? 0 : null,
            "scrollX": true,
            "rowCallback": function (row, data, index) {
                $(row).addClass("data-table-row");
                $(row).find('td').height(45);
            },
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }]
        });
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
                fixed_column: value == 1? true: false
            })
        });

        yadcf.init(data_table, filterOptions,
            {
                filter_type: "text",
                filter_reset_button_text: false,
                filter_delay: 600,
                filter_default_label: '',
            });
        $(".yadcf-filter").removeAttr('placeholder');
        if (filterValue.length > 0) {
            yadcf.exFilterColumn(data_table, filterValue, true);
        } else {
            $('.yadcf-filter-wrapper').hide()
        }
        window.sca_data_table = table;
        signal.fieldManagement.init($('#alertsDetailsTable').DataTable(), '#alertsDetailsTable', fixedColumns, true);
        signal.commonTag.openCommonAlertTagModal("PVS", "Qualitative on demand", tags);
        $('.yadcf-filter-wrapper').hide();

    };

    var createAlertListUrl = function (isFilterRequest, filterValues) {
        var listUrl = listConfigUrl + '&isFilterRequest=' + isFilterRequest + '&filters=' + encodeURIComponent(JSON.stringify(filterValues));
        if(isTempViewSelected) {
            listUrl += "&tempViewId=" + tempViewPresent
        }
        return listUrl
    };

    var create_single_case_table_columns = function (callingScreen, customFieldsEnabled) {
        var aoColumns = [
            {
                "mRender": function (data, type, row) {
                    var checkboxhtml = '<input class="execConfigId" id="execConfigId" type="hidden" value="' + row.execConfigId + '" />' +
                        '<input class="alertConfigId" id="alertConfigId" type="hidden" value="' + row.alertConfigId + '" />' +
                        '<input data-field ="productFamily" data-id="' + encodeToHTML(row.productFamily) + '" type="hidden" value="' + encodeToHTML(row.productFamily) + '" />' +
                        '<input data-field ="primaryEvent" data-id="' + encodeToHTML(row.primaryEvent) + '" type="hidden" value="' + encodeToHTML(row.primaryEvent) + '" />'

                    if(alertIdSet.has(JSON.stringify(row.id))){
                        return checkboxhtml + '<input type="checkbox" class="alert-check-box editor-active copy-select" data-id=' + row.id + ' checked/>';
                    } else if(selectedCases.includes(row.id.toString())){
                        return  checkboxhtml +  '<input type="checkbox" class="alert-check-box editor-active copy-select" data-id=' + row.id + ' checked/>';
                    } else{
                        return checkboxhtml + '<input type="checkbox" class="alert-check-box editor-active copy-select" data-id=' + row.id + ' />';;
                    }

                },
                "className": "",
                "sortable": false,
                "visible": true
            },
            {
                "mData": "caseNumber",
                'className': 'col-min-75 col-min-75',
                "mRender": function (data, type, row) {
                    var isFaers = $("#isFaers").val();
                    return '<a target="_blank" class="scaCaseNumber" href="' + (caseDetailUrl + '?' +
                            encodeToHTML(signal.utils.composeParams({
                                caseNumber: row.caseNumber,
                                version: row.caseVersion,
                                followUpNumber: row.followUpNumber,
                                alertId: row.id,
                                isFaers: isFaers,
                                isVersion: true,
                                isAdhocRun: true,
                            }))) + '">' +
                        "<span data-field ='caseNumber' data-id='" + encodeToHTML(row.caseNumber) + "'>" +
                        encodeToHTML(row.caseNumber) + "</span><span data-field ='caseVersion' data-id='" + row.caseVersion + "'>" +
                        "</span><span data-field ='execConfigId' data-id='" + row.execConfigId + "'><span data-field ='alertId' data-id='" + row.id + "'></span>(<span data-field ='followUpNumber' data-id='" + row.followUpNumber + "'>" +
                        ((row.followUpNumber < 1) ? 0 : row.followUpNumber) + "</span>)" + '</a>'
                },
                "data-type": "caseNumber",
                "visible": true
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
                "mRender": function (data, type, full) {
                    return (data != undefined) ? moment.utc(data).format(DEFAULT_DATE_FORMAT) : '';
                },
                "className": 'col-min-100 col-max-100',
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
                "className": 'col-min-100 col-max-100',
                "mRender": function (data, type, row) {
                    return '<span data-field="masterPrefTermAll" data-id="' + row.pt + '">' + row.pt + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('pt')
            },
            {
                "mData": "listedness",
                "className": '',
                "mRender": function (data, type, row) {
                    return '<span>' + row.listedness + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('listedness')
            }, {
                "mData": "outcome",
                "className": 'col-min-150 col-max-150',
                "mRender": function (data, type, row) {
                    return '<span>' + row.outcome + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('outcome')
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
                "sortable": false,
                "visible": signal.fieldManagement.visibleColumns('suspProd')
            },
            {
                "mData": "conComit",
                'className': 'col-min-100 col-max-150',
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
                "mData": "masterPrefTermAll",
                'className': 'col-min-100 col-max-100',
                "mRender": function (data, type, row) {
                    var ptList = '';
                    if (row.masterPrefTermAll) {
                        ptList = row.masterPrefTermAll.split('!@##@!').join(' ');
                        ptList = ptList.split('!@.@!').join(') ');
                    }
                    var colElement = '<div class="col-container"><div class="col-height">';
                    colElement += ptList;
                    colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(ptList) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                    colElement += '</div></div>';
                    return colElement
                },
                "sortable": false,
                "visible": signal.fieldManagement.visibleColumns('masterPrefTermAll')
            },
            {
                "mData": "serious",
                'className': 'col-min-150 col-max-150',
                "mRender": function (data, type, row) {
                    return '<span>' + row.serious + '</span>'
                },
                "sortable": false,
                "visible": signal.fieldManagement.visibleColumns('serious')
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
                "mData": "reportersHcpFlag",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<span>' + row.reportersHcpFlag + '</span>'
                },
                "sortable": false,
                "visible": signal.fieldManagement.visibleColumns('reportersHcpFlag')
            },
            {
                "mData": "country",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<span>' + row.country + '</span>'
                },
                "sortable": false,
                "visible": signal.fieldManagement.visibleColumns('country')
            },
            {
                "mData": "age",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<span>' + row.age + '</span>'
                },
                "sortable": false,
                "visible": signal.fieldManagement.visibleColumns('age')
            },
            {
                "mData": "gender",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<span>' + row.gender + '</span>'
                },
                "sortable": false,
                "visible": signal.fieldManagement.visibleColumns('gender')
            },
            {
                "mData": "rechallenge",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<span>' + row.rechallenge + '</span>'
                },
                "sortable": false,
                "visible": signal.fieldManagement.visibleColumns('rechallenge')
            },
            {
                "mData": "lockedDate",
                'className': 'col-min-100 col-max-100',
                "mRender": function (data, type, row) {
                    return (data && data != undefined) ? moment.utc(data).format(DEFAULT_DATE_FORMAT) : '';
                },
                "orderable": true,
                "visible": signal.fieldManagement.visibleColumns('lockedDate')
            },
            {
                "mData": "death",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<span>' + row.death + '</span>'
                },
                "sortable": false,
                "visible": signal.fieldManagement.visibleColumns('death')
            },
            {
                "mData": "patientAge",
                'className': 'col-min-100 col-max-100',
                "mRender": function (data, type, row) {
                    return '<span>' + row.patientAge + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('patientAge')
            }];

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
                    'visible': signal.fieldManagement.visibleColumns('caseType'),
                    "mRender": function (data, type, row) {
                        return '<span>' + row.caseType + '</span>'
                    }
                },
                {
                    "mData": "completenessScore",
                    'className': '',
                    'visible': signal.fieldManagement.visibleColumns('completenessScore'),
                    "mRender": function (data, type, row) {
                        return '<span>' + row.completenessScore + '</span>'
                    }
                },
                {
                    "mData": "indNumber",
                    'className': '',
                    'visible': signal.fieldManagement.visibleColumns('indNumber'),
                    "mRender": function (data, type, row) {
                        return '<span>' + row.indNumber + '</span>'
                    }
                },
                {
                    "mData": "appTypeAndNum",
                    'className': '',
                    'visible': signal.fieldManagement.visibleColumns('appTypeAndNum'),
                    "mRender": function (data, type, row) {
                        return '<span>' + row.appTypeAndNum + '</span>'
                    }
                },
                {
                    "mData": "compoundingFlag",
                    'className': '',
                    'visible': signal.fieldManagement.visibleColumns('compoundingFlag'),
                    "mRender": function (data, type, row) {
                        return '<span>' + row.compoundingFlag + '</span>'
                    }
                },
                {
                    "mData": "submitter",
                    'className': '',
                    'visible': signal.fieldManagement.visibleColumns('submitter'),
                    "mRender": function (data, type, row) {
                        return '<span>' + row.submitter + '</span>'
                    }
                }
            ]);
        }

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
            },
            {
                "mData": "seriousUnlistedRelated",
                'className': 'col-min-100 col-max-100',
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
                "mData": "patientMedHist",
                'className': 'col-min-150 col-max-150',
                "mRender": function (data, type, row) {
                    var colElement = '<div class="col-container"><div class="col-height">';
                    colElement +=  $("<p/>").html(row.patientMedHist).text();
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
                "mData": "batchLotNo",
                'className': 'col-min-150 col-max-150',
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
                "mData": "timeToOnset",
                'className': 'col-min-150 col-max-150',
                "mRender": function (data, type, row) {
                    return '<span>' + row.timeToOnset + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('timeToOnset')
            },
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
                "mData": "initialFu",
                "mRender": function (data, type, row) {
                    return '<span>' + row.initialFu + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('initialFu')
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
            },
            {
                "mData": "isSusar",
                "mRender": function (data, type, row) {
                    return '<span>' + row.isSusar + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('isSusar')
            },
            {
                "mData": "therapyDates",
                'className': 'col-min-150 col-max-150 word-break',
                "mRender": function (data, type, row) {
                    var colElement = '<div class="col-container"><div class="col-height">';
                    colElement += encodeToHTML(row.therapyDates);
                    colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(row.therapyDates) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                    colElement += '</div></div>';
                    return colElement
                },
                "sortable": false,
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
                "sortable": false,
                "visible": signal.fieldManagement.visibleColumns('doseDetails')
            }

        ]);
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
        aoColumns.push.apply(aoColumns, [
            {
                "mData": "primSuspProd",
                'className': 'col-min-150 col-max-150',
                "mRender": function (data, type, row) {
                    var colElement = '<div class="col-container"><div class="col-height">';
                    colElement += encodeToHTML(formatText(row.primSuspProd));
                    colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + encodeToHTML(row.primSuspProd) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                    colElement += '</div></div>';
                    return colElement
                },
                "sortable": false,
                "visible": signal.fieldManagement.visibleColumns('primSuspProd')
            }
        ])
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
                "sortable": false,
                "visible": signal.fieldManagement.visibleColumns('allPt')
            }
        ]);

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
                "orderable": $("#showDob").val() == "false" ? false : true,
                "mRender": function (data, type, row) {
                    var dobString
                    if ($("#showDob").val() == "false") {
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

        if (callingScreen == 'Triggered Alerts') {
            aoColumns.splice(5, 0, {
                "mData": "alertName",
                'className': 'dt-left col-min-150 col-max-150',
            })
            aoColumns.splice(11, 0, {
                "mData": "",
                'className': 'dt-left',
                "mRender": function (data, type, row) {
                    return signal.utils.stacked(
                        '<span>' + row.ebo5 + '</span>',
                        '<span>' + row.prr + '</span>'
                    )
                },
            })
        }
        return aoColumns
    }

    var cnt_drill_down = function (value) {
        return '<span>' + value + '</span>'
    }

    var select_all = function (s) {
        $(".copy-select").prop('checked', s)
    }

    var showAllSelectedCaseNumbers = function () {
        $('#copyCaseNumberModel').modal({
            show: true
        })

        var numbers = _.map($('input.copy-select:checked').parent().parent().find('td:nth-child(5)'), function (it) {
            return $(it).text()
        })
        $("#caseNumbers").text(numbers);
    }

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
                } else if(selectedCases.indexOf($(this).attr("data-id")) != -1 && !$(this).is(':checked')){
                    selectedCases.splice( $.inArray($(this).attr("data-id"), selectedCases), 1 );
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

    $('#exportTypes a[href]').on('click', function (e) {
        populateSelectedCasesAdhoc();
        var clickedURL = e.currentTarget.href;
        var checkedColumns = {};
        var ids = [];
        if(selectedCases.length > 0) {
            ids=selectedCases
        }
        var checkedColJson = JSON.stringify(checkedColumns);

        var filterList = {};
        $(".yadcf-filter-wrapper").each(function () {
            var filterVal = $(this).children().val();
            if (filterVal) {
                filterList[$(this).prev().attr('data-field')] = filterVal
            }
        });
        var filterListJson = JSON.stringify(filterList);
        var isSafety=true;
        if($("#isFaers").val() == "true" || $("#isVaers").val() == "true" || $("#isVigibase").val() == "true") {
            isSafety=false;
        }
        newUrl = encodeURI(clickedURL + '&isFilterRequest=' + false + "&checkedCols=" + checkedColJson + "&filterList=" + filterListJson +"&isSafety=" + isSafety);
        if ($('#advanced-filter').val()) {
            newUrl += "&advancedFilterId=" + $('#advanced-filter').val()
        }
        if (ids.length > 0) {
            newUrl += "&selectedCases=" + ids;
        }
        if(isTempViewSelected) {
            newUrl += "&tempViewId=" + tempViewPresent;
        }
        if($(this).closest('li').attr('class')=='generate-case-form') {
            e.preventDefault()
            $("#form-name-error").closest(".alert-dismissible").addClass("hide");
            $("#case-form-name-modal").find('#case-form-file-name').val("")
            $("#case-form-name-modal").modal('show');
            fetchCaseFormNames();
            $("#case-form-name-modal").find('#case-form-url').val(newUrl);
        } else {
            window.location.href = newUrl;
        }
        return false
    });

    $(document).on('click', '#report a.m-r-10.grid-menu-tooltip.text-left-prop, #ic-report', function () {
        populateSelectedCasesAdhoc();
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
            tempViewId: tempViewPresent,
            isFilterRequest: false,
            filterList: filterListJson,
            advancedFilterId: $('#advanced-filter').val(),
        }, true);

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
    $("#alertsDetailsTable_filter").hide();

    $(document).on('click', '#detailed-view-checkbox,#ic-detailed-view-checkbox', function () {
        var $this = $(this);
        var reloadUrl = $this.data('url') + '?archived=' + isArchived + '&callingScreen=' + $this.data('callingScreen') + '&configId=' + $this.data('configId') + "&viewId=" + viewId + '&isCaseDetailView=' + $this.data('isCaseDetailView');
        if(isTempViewSelected) {
            reloadUrl = $this.data('url') + '?archived=' + isArchived+ '&callingScreen=' + $this.data('callingScreen') + '&configId=' + $this.data('configId') + "&tempViewId=" + tempViewPresent + '&isCaseDetailView=' + $this.data('isCaseDetailView');
        }
        isUpdateTemp = false
        window.location.href = reloadUrl;
    });

    $(document).on('click', '.scaCaseNumber', function (e) {
        e.preventDefault();
        var scaAlertId = $(this).find("span[data-field=alertId]").attr("data-id");
        var caseVersion = $(this).find("span[data-field=caseVersion]").attr("data-id");
        var caseNumber = $(this).find("span[data-field=caseNumber]").attr("data-id");
        var execConfigId = $(this).find("span[data-field=execConfigId]").attr("data-id");
        var followUpNumber = $(this).find("span[data-field=followUpNumber]").attr("data-id");
        var isJader = $("#isJader").val();

        var caseInfoUrl = $(this).attr('href');
        caseInfoUrl +="&isSingleAlertScreen=true&isCaseSeries=false&isVersion=true"
        if(hasReviewerAccess) {
            $.ajax({
                url: updateAutoRouteDispositionUrl,
                data: {'id': scaAlertId, 'isJader': isJader},
            })
                .done(function (result) {
                    if (result.status) {
                        alertDetailsTable.ajax.reload();
                    }
                    openCaseDetailsPage(caseNumber, caseVersion, followUpNumber, scaAlertId, execConfigId)
                })
                .fail(function () {
                    console.log("Some error occured while updating AutoRoute Disposition");
                });
        } else {
            $.Notification.notify('warning', 'top right', "Warning", "You don't have access to perform auto route disposition", {autoHideDelay: 5000});
            openCaseDetailsPage(caseNumber, caseVersion, followUpNumber, scaAlertId, execConfigId)
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

    fetchTags();

    var executionStatusCurrent = $('.generateCurrentAnalysis').attr('data-status')
    set_disable_click($('.generateCurrentAnalysis'), executionStatusCurrent, 'Current Period Analysis');

    var executionStatusCumulative = $('.generateCumulativeAnalysis').attr('data-status')
    set_disable_click($('.generateCumulativeAnalysis'), executionStatusCumulative, 'Cumulative Period Analysis');

    $(document).on('click', '.generateCurrentAnalysis', function () {
        generate_analysis($('.generateCurrentAnalysis'), 'PR_DATE_RANGE', 'Current Period Analysis', 'Current');
    });
    $(document).on('click', '.generateCumulativeAnalysis', function () {
        generate_analysis($('.generateCumulativeAnalysis'), 'CUMULATIVE', 'Cumulative Period Analysis', 'Cumulative');
    });
    setInterval(fetchIfTempAvailable, clipboardInterval);

    $("#exportExcel,#cumulativeExport").on("click",function(event){
        event.preventDefault();
        var href = newUrl;
        var isSafety=true;
        if($("#isFaers").val() == "true" || $("#isVaers").val() == "true" || $("#isVigibase").val() == "true") {
            isSafety=false;
        }
        if(promptUser === "true" && isSafety) {
            $(".ul-ddm").hide();
            window.stop();

            var onCancel = function(){
                window.location.href = href += "&promptUser=" + true;
            }

            var onSubmit = function(){
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
    });
});


var format = function (d, className) {
    var customFieldsEnabled = JSON.parse($("#customFieldsEnabledAdhoc").val())
    var productList = d.productList;
    var productArray = productList.split(',');
    var ptArray = d.ptList.split(new RegExp('!@##@!|\r\n', 'g'));
    var patMedHist = $("<p/>").html(d.patientMedHist).text();
    patMedHist = patMedHist.replaceAll("</br>","")
    var resultedObj = {
        productList: productArray.join(','),
        masterPrefTermAll: d.masterPrefTermAll,
        caseNarrative: d.caseNarrative,
        conMeds: removeLineBreak(d.conMeds),
        medErrorsPt : d.medErrorsPt,
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
        customFieldsEnabled: JSON.parse($("#customFieldsEnabledAdhoc").val()),
        columnLabelMap: columnLabelMapForDetail,
        crossReferenceInd: d.crossReferenceInd,
        coPackagedProduct: d.coPackagedProduct
    };
    html = signal.utils.render("single_case_details_child_view_5.6.1", resultedObj);
    return html;
};

function openCaseDetailsPage(caseNumber, caseVersion, followUpNumber, scaAlertId, execConfigId) {
    signal.utils.postUrl(caseDetailUrl, {
        caseNumber: caseNumber,
        version: caseVersion,
        followUpNumber: followUpNumber,
        alertId: scaAlertId,
        isFaers: $("#isFaers").val(),
        fullCaseList: $("#fullCaseList").val(),
        execConfigId: execConfigId,
        isArchived: isArchived,
        totalCount: table.page.info().recordsTotal,
        detailsParameters: JSON.stringify(detailsParameters),
        isSingleAlertScreen: false,
        isVersion: true,
        isAdhocRun: true,
        isCaseSeries : $("#isCaseSeries").val() == "true",
        oldFollowUp : followUpNumber,
        oldVersion : caseVersion,
    }, true);
}

function showCaseDetailsView() {
    $("tr.data-table-row").each(function (index) {
        var tr = $(this);
        var row = window.sca_data_table.row(tr);
        var className = $(this).hasClass('odd') ? 'odd' : 'even';
        row.child(format(row.data()), className).show();
    });
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
