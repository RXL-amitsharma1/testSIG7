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

var userLocale = "en";
var applicationName = "EVDAS Alert on Demand";
var applicationLabel = "EVDAS Alert on Demand";
var executedIdList = [];
var table;
var dir = '';
var index = -1;
var isDynamicFilterApplied = false;
var evAdhocEntriesCount=sessionStorage.getItem('evAdhocPageEntries')!=null?sessionStorage.getItem('evAdhocPageEntries'):50
var fixedFilterColumn = []

$(function () {
    $(document).on('fieldsRearranged', function (e) {
        $('.yadcf-filter-wrapper').remove();
        init_filter($('#alertsDetailsTable').DataTable())
    })

    var freqSelected = "";
    var alertDetailsTable;
    var activities_table;

    var dateRangeString = $('#listDateRange').val().slice(1, -1);
    var dateRangeArray = [];
    if (dateRangeString != "") {
        dateRangeArray = dateRangeString.split(', ');
    }

    fetchViewInstanceList();

    if (callingScreen == CALLING_SCREEN.REVIEW) {
        $(window).on('beforeunload', function () {
            $.ajax({
                url: discardTempChangesUrl,
                method: 'GET'
            })
        });
    }

    var filterIndex =  [0, 1, 2, 3, 4, 5, 7, 8, 9, 10, 11 ,12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 34, 36, 37];

    var fixedColumns = 3;
    var checkedIdList = [];
    var checkedRowList = [];
    signal.alertReview.openAlertCommentModal(applicationName, applicationName, applicationLabel, checkedIdList, checkedRowList);
    signal.alertReview.populateAdvancedFilterSelect(applicationName);
    signal.alertReview.setSortOrder();
    signal.evdasCaseDrillDown.bind_evdas_drill_down_table(evdasCaseDetailUrl);
    var filterValue = signal.alertReview.createFilterMap('eda_filterMap_'+executedConfigId, 'eda_viewName_' + executedConfigId);

    $('#alert-list-filter-apply-bt').on('click', function (evt) {
        evt.preventDefault();
        var filterParams = $('#alert-list-filter').serialize();
        var theDataTable = $('#alertsDetailsTable').DataTable();
        $('#alert-list-filter').find('#filterUsed').val(true);
        var newUrl = listConfigUrl + "&" + filterParams + "&filterApplied=" + true
        theDataTable.ajax.url(newUrl).load()
    });

    signal.actions_utils.set_action_list_modal($('#action-modal'));
    signal.actions_utils.set_action_create_modal($('#action-create-modal'));
    signal.alertReview.showAttachmentModal();

    //apply the sorting to column based on saved view
    var sortingMap = signal.alertReview.createSortingMap('eda_sortedColumn_'+ executedConfigId, 'eda_viewName_' + executedConfigId);
    if (sortingMap != 'undefined' && sortingMap.length > 0) {
        index = sortingMap[0][0];
        dir = sortingMap[0][1]
    }

    if (callingScreen == CALLING_SCREEN.REVIEW) {
        //saving new ViewInstance modal
        signal.alertReview.openSaveViewModal(filterIndex, applicationName, viewId)
    }

    var _changeIntervalSubstance = null;

    $(document).on('keyup', 'input#yadcf-filter-alertsDetailsTable-' + (filterIndex[0]), function (e) {

        var casenum=$(this).val();
        var caseInput = $(this);
        clearInterval(_changeIntervalSubstance)
        _changeIntervalSubstance = setInterval(function() {
            $("#alertsDetailsTable").DataTable().column(filterIndex[0]).search(casenum); //Exact value, column, reg
            if(casenum == "") {
                fixedFilterColumn.push({"column":filterIndex[0], "value":""});
                caseInput.removeClass("inuse");
            } else {
                fixedFilterColumn.push({"column":filterIndex[0], "value":casenum});
                caseInput.addClass("inuse");
            }
            clearInterval(_changeIntervalSubstance)
        }, 1500);

    });


    var _changeIntervalSoc = null;

    $(document).on('keyup', 'input#yadcf-filter-alertsDetailsTable-' + (filterIndex[0]+1), function (e) {

        var casenum=$(this).val();
        var caseInput = $(this);
        clearInterval(_changeIntervalSoc)
        _changeIntervalSoc = setInterval(function() {
            $("#alertsDetailsTable").DataTable().column(filterIndex[0]+1).search(casenum); //Exact value, column, reg
            if(casenum == "") {
                fixedFilterColumn.push({"column":filterIndex[0]+1, "value":""});
                caseInput.removeClass("inuse");
            } else {
                fixedFilterColumn.push({"column":filterIndex[0]+1, "value":casenum});
                caseInput.addClass("inuse");
            }
            clearInterval(_changeIntervalSoc)
        }, 1500);

    });

    var _changeIntervalPt = null;

    $(document).on('keyup', 'input#yadcf-filter-alertsDetailsTable-' + (filterIndex[0]+2), function (e) {

        var casenum=$(this).val();
        var caseInput = $(this);
        clearInterval(_changeIntervalPt)
        _changeIntervalPt = setInterval(function() {
            $("#alertsDetailsTable").DataTable().column(filterIndex[0]+2).search(casenum); //Exact value, column, reg
            if(casenum == "") {
                fixedFilterColumn.push({"column":filterIndex[0]+2, "value":""});
                caseInput.removeClass("inuse");
            } else {
                fixedFilterColumn.push({"column":filterIndex[0]+2, "value":casenum});
                caseInput.addClass("inuse");
            }
            clearInterval(_changeIntervalPt)
        }, 1500);

    });



    $(window).on('beforeunload', function(){
        if (sessionStorage.getItem('isViewCall') == 'true' || $('.viewSelect :selected').text().replace("(default)", "").trim() != 'System View') {
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

    var init_filter = function (data_table) {

        var filterOptions = [];
        $.each(filterIndex, function (key, value) {
            filterOptions.push({
                column_number: value,
                filter_type: 'text',
                filter_reset_button_text: false,
                filter_delay: 600,
                filter_default_label: '',
                style_class: 'textFilter',
                fixed_column: ([0,1,2].indexOf(key) != -1)?true:false
            })
        });
        yadcf.init(data_table, filterOptions);
        $(".yadcf-filter").removeAttr('placeholder');
        if(filterValue.length > 0){
            yadcf.exFilterColumn(data_table, filterValue,true);
        }else{
            $('.yadcf-filter-wrapper').hide()
        }
        new $.fn.dataTable.FixedColumns(table, {
            start: fixedColumns,
            end: 0,
            heightMatch: 'auto'
        });
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
            rowCallback: function (row, data, index) {
                $(row).find('td').height(45);
            },
            drawCallback: function (settings) {
                var rowsDataAD = $('#alertsDetailsTable').DataTable().rows().data();
                if(settings.json != undefined) {
                    pageDictionaryForAlertDetails($('#alertsDetailsTable_wrapper')[0], settings.aLengthMenu[0][0], settings.json.recordsFiltered);

                }else {
                    pageDictionaryForAlertDetails($('#alertsDetailsTable_wrapper')[0], 50, rowsDataAD.length);
                }
                initPSGrid($('#alertsDetailsTable_wrapper'));
                colEllipsis();
                webUiPopInit();
                showInfoPopover();
                closeInfoPopover();
                signal.alertReview.enableMenuTooltips();
                signal.alertReview.enableMenuTooltipsDynamicWidth();
                signal.alertReview.sortIconHandler()
                $('.dt-pagination').on('change', function () {
                    var countVal = $('.dt-pagination').val()
                    sessionStorage.setItem("evAdhocPageEntries", countVal);
                    evAdhocEntriesCount = sessionStorage.getItem("evAdhocPageEntries");
                })
                $(".dt-scroll-body").append("<div style='height:3px'></div>");
            },
            initComplete: function (settings, json) {
                init_filter(table);
                if (sessionStorage.getItem(prefix + "filters_store")) {
                    setQuickDispositionFilter(prefix);
                }


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
                });
            },

            "ajax": {
                "url": listConfigUrl + '&isFilterRequest=' + isFilterRequest + '&filters=' + encodeURIComponent(JSON.stringify(filterValues)) + "&frequency=" + freqSelected,
                "type": "POST",
                "dataSrc": "aaData",
                "data": function (d) {
                    if(index != -1) {
                        d.order[0].column = index;
                        d.order[0].dir = dir;
                    }
                    if ($('#advanced-filter').val()) {
                        d.advancedFilterId = $('#advanced-filter').val()
                    }
                    if($('#queryJSON').val()){
                        d.queryJSON = $('#queryJSON').val()
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
            "iTotalDisplayRecords": evAdhocEntriesCount,
            "iDisplayLength": parseInt(evAdhocEntriesCount),
            "bAutoWidth": false,
            "deferLoading":filterValue.length > 0 ? 0: null,
            "aoColumns": constructColumns(result),
            scrollX: true,
            columnDefs: [{
                "targets": '_all',
                "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }]
        });

        return table;
    };

    var constructColumns = function (result) {
        var aocolumns = [];
        aocolumns.push.apply(aocolumns, [
            {
                "mData": "substance",
                'className': 'col-min-150 col-max-150 cell-break',
                "mRender": function (data, type, row) {
                    var colElement = "<input type='hidden' value='" + row.substanceId + "' class='row-product-id'/>" + "<input type='hidden' value='" + row.ptCode + "' class='row-alert-id'/>"  + "<input type='hidden' value='" + row.id + "' class='row-alert-id'/>";
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
                'className': 'col-min-150 col-max-150',
                "mRender": function (data, type, row) {
                    var colElement = '<div class="col-container"><div class="col-height">';
                    colElement+=row.soc;
                    colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="'+row.soc+'"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
                    colElement += '</div></div>';
                    return colElement
                },
                "visible": true
            },
            {
                "mData": "pt",
                "className": "col-min-150 col-max-150",
                "mRender": function (data, type, row) {
                    return "<span style='word-break: keep-all' data-field ='eventName' data-id='" + row.preferredTerm + "'>" + (row.preferredTerm) + "</span>"
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
                'className': 'col-min-50 col-max-50 dmeIme',
                "mRender": function (data, type, row) {
                    return '<span class=' + EVDAS_BUSNSINESS_RULE_ENUM.EVDAS_IME_DME + '>' + row.dmeIme + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('dmeIme')
            },
            {
                "mData": "newEv",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<div><div class="stacked-cell-center-top">' + drillDownOptions(row.newEv, row.productName, row.preferredTerm, row.execConfigId, 0, true, row.id, row.newEvLink, 'NEW_EV_EVDAS') +
                        '</div><div class="stacked-cell-center-top">' + drillDownOptions(row.totalEv, row.productName, row.preferredTerm, row.execConfigId, 0, false, row.id, row.totalEvLink, 'TOTAL_EV_EVDAS') + '</div></div>'
                },
                "visible": signal.fieldManagement.visibleColumns('newEv')
            },
            {
                "mData": "newEea",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<div><div class="stacked-cell-center-top">' + drillDownOptions(row.newEea, row.productName, row.preferredTerm, row.execConfigId, 1, true, row.id, '', 'NEW_EEA_EVDAS') +
                        '</div><div class="stacked-cell-center-top">' + drillDownOptions(row.totEea, row.productName, row.preferredTerm, row.execConfigId, 1, false, row.id, '', 'TOTAL_EEA_EVDAS') + '</div></div>'
                },
                "visible": signal.fieldManagement.visibleColumns('newEea')
            },
            {
                "mData": "newHcp",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<div><div class="stacked-cell-center-top">' + drillDownOptions(row.newHcp, row.productName, row.preferredTerm, row.execConfigId, 2, true, row.id, '', 'NEW_HCP_EVDAS') +
                        '</div><div class="stacked-cell-center-top">' + drillDownOptions(row.totHcp, row.productName, row.preferredTerm, row.execConfigId, 2, false, row.id, '', 'TOTAL_HCP_EVDAS') + '</div></div>'
                },
                "visible": signal.fieldManagement.visibleColumns('newHcp')
            },
            {
                "mData": "newSerious",
                "mRender": function (data, type, row) {
                    return '<div><div class="stacked-cell-center-top">' + drillDownOptions(row.newSerious, row.productName, row.preferredTerm, row.execConfigId, 3, true, row.id, '', 'NEW_SERIOUS_EVDAS') +
                        '</div><div class="stacked-cell-center-top">' + drillDownOptions(row.totalSerious, row.productName, row.preferredTerm, row.execConfigId, 3, false, row.id, '', 'TOTAL_SERIOUS_EVDAS') + '</div></div>'
                },
                "visible": signal.fieldManagement.visibleColumns('newSerious')
            },
            {
                "mData": "newMedErr",
                "mRender": function (data, type, row) {
                    return '<div><div class="stacked-cell-center-top"><span class="NEW_MED_ERR_EVDAS">' + row.newMedErr + '</span></div>' +
                        '<div class="stacked-cell-center-top"><span class="TOTAL_MED_ERR_EVDAS">' + row.totMedErr + '</span></div></div>'
                },
                "visible": signal.fieldManagement.visibleColumns('newMedErr')
            },
            {
                "mData": "newObs",
                "mRender": function (data, type, row) {
                    return '<div><div class="stacked-cell-center-top"><span class="NEW_OBS_EVDAS">' + row.newObs + '</span></div>' +
                        '<div class="stacked-cell-center-top"><span class="TOTAL_OBS_EVDAS">' + row.totObs + '</span></div></div>'
                },
                "visible": signal.fieldManagement.visibleColumns('newObs')
            },
            {
                "mData": "newFatal",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<div><div class="stacked-cell-center-top">' + drillDownOptions(row.newFatal, row.productName, row.preferredTerm, row.execConfigId, 4, true, row.id, '', 'NEW_FATAL_EVDAS') + '</div>' +
                        '<div class="stacked-cell-center-top">' +
                        drillDownOptions(row.totalFatal, row.productName, row.preferredTerm, row.execConfigId, 4, false, row.id, '', 'TOTAL_FATAL_EVDAS') +
                        '</div></div>'
                },
                "visible": signal.fieldManagement.visibleColumns('newFatal')
            },
            {
                "mData": "newRc",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<div><div class="stacked-cell-center-top"><span class="NEW_PLUS_RC_EVDAS">' + row.newRc + '</span></div>' +
                        '<div class="stacked-cell-center-top"><span class="TOTAL_PLUS_RC_EVDAS">' + row.totRc + '</span></div></div>'
                },
                "visible": signal.fieldManagement.visibleColumns('newRc')
            },
            {
                "mData": "newLit",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<div><div class="stacked-cell-center-top"><span class="NEW_LITERATURE_EVDAS">' + row.newLit + '</span></div>' +
                        '<div class="stacked-cell-center-top"><span class="TOTAL_LITERATURE_EVDAS">' + row.totalLit + '</span></div></div>'

                },
                "visible": signal.fieldManagement.visibleColumns('newLit')
            },
            {
                "mData": "newPaed",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<div><div class="stacked-cell-center-top">' + drillDownOptions(row.newPaed, row.productName, row.preferredTerm, row.execConfigId, 8, true, row.id, '', 'NEW_PAED_EVDAS') +
                        '</div><div class="stacked-cell-center-top">' + drillDownOptions(row.totPaed, row.productName, row.preferredTerm, row.execConfigId, 8, false, row.id, '', 'TOTAL_PAED_EVDAS') + '</div></div>'
                },
                "visible": signal.fieldManagement.visibleColumns('newPaed')
            },
            {
                "mData": "ratioRorPaedVsOthers",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<span class=' + EVDAS_BUSNSINESS_RULE_ENUM.RELTV_ROR_PAED_VS_OTHR +'>' + data + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('ratioRorPaedVsOthers')
            },
            {
                "mData": "newGeria",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<div><div class="stacked-cell-center-top">' + drillDownOptions(row.newGeria, row.productName, row.preferredTerm, row.execConfigId, 7, true, row.id, '', 'NEW_GERIAT_EVDAS') +
                        '</div><div class="stacked-cell-center-top">' + drillDownOptions(row.totGeria, row.productName, row.preferredTerm, row.execConfigId, 7, false, row.id, '', 'TOTAL_GERIAT_EVDAS') + '</div></div>'
                },
                "visible": signal.fieldManagement.visibleColumns('newGeria')
            },
            {
                "mData": "ratioRorGeriatrVsOthers",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<span class=' + EVDAS_BUSNSINESS_RULE_ENUM.RELTV_ROR_GERTR_VS_OTHR + '>' + data + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('ratioRorGeriatrVsOthers')
            },
            {
                "mData": "sdrGeratr",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<span class=' + EVDAS_BUSNSINESS_RULE_ENUM.EVDAS_SDR_GERTR+ '>' + data + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('sdrGeratr')
            },
            {
                "mData": "newSpont",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<div><div class="stacked-cell-center-top">' + drillDownOptions(row.newSpont, row.productName, row.preferredTerm, row.execConfigId, 6, true, row.id, '', 'NEW_SPON_EVDAS') +
                        '</div><div class="stacked-cell-center-top">' + drillDownOptions(row.totSpont, row.productName, row.preferredTerm, row.execConfigId, 6, false, row.id, '', 'TOTAL_SPON_EVDAS') + '</div></div>'
                },
                "visible": signal.fieldManagement.visibleColumns('newSpont')
            },
            {
                "mData": "totSpontEurope",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<span class=' + EVDAS_BUSNSINESS_RULE_ENUM.TOTAL_SPON_EUROPE +'>' + data + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('totSpontEurope')
            },
            {
                "mData": "totSpontNAmerica",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<span class=' + EVDAS_BUSNSINESS_RULE_ENUM.TOTAL_SPON_N_AMERICA + '>' + data + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('totSpontNAmerica')
            },
            {
                "mData": "totSpontJapan",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<span class=' + EVDAS_BUSNSINESS_RULE_ENUM.TOTAL_SPON_JAPAN + '>' + data + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('totSpontJapan')
            },
            {
                "mData": "totSpontAsia",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<span class=' + EVDAS_BUSNSINESS_RULE_ENUM.TOTAL_SPON_ASIA + '>' + data + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('totSpontAsia')
            },
            {
                "mData": "totSpontRest",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<span class=' + EVDAS_BUSNSINESS_RULE_ENUM.TOTAL_SPON_REST + '>' + data + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('totSpontRest')
            },
            {
                "mData": "rorValue",
                'className': '',
                "mRender": function (data, type, row) {
                    // Below class ROR is used in business configuration
                    return '<span class=' + EVDAS_BUSNSINESS_RULE_ENUM.ROR_ALL_EVDAS + '>' + row.rorValue.match(/^-?\d+(?:\.\d{0,2})?/)[0] + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('rorValue')
            },
            {
                "mData": "sdr",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<span class="SDR_EVDAS">' + data + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('sdr')
            },
            {
                "mData": "sdrPaed",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<span class=' + EVDAS_BUSNSINESS_RULE_ENUM.EVDAS_SDR_PAED + '>' + data + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('sdrPaed')
            },
            {
                "mData": "europeRor",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<span class="ROR_EUROPE_EVDAS">' + data + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('europeRor')
            },
            {
                "mData": "northAmericaRor",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<span class="ROR_N_AMERICA_EVDAS">' + data + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('northAmericaRor')
            },
            {
                "mData": "japanRor",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<span class="ROR_JAPAN_EVDAS">' + data + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('japanRor')
            },
            {
                "mData": "asiaRor",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<span class="ROR_ASIA_EVDAS">' + data + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('asiaRor')
            },
            {
                "mData": "restRor",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<span class="ROR_REST_EVDAS">' + data + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('restRor')
            },
            {
                "mData": "changes",
                'className': '',
                "mRender": function (data, type, row) {
                    return '<span class="CHANGES_EVDAS">' + data + '</span>'
                },
                "visible": signal.fieldManagement.visibleColumns('changes')
            },
        ]);
        return aocolumns
    };

    var drillDownOptions = function (value, substance, pt, id, flag_var, isStartDate, alertId, url, className) {
        var actionButton;
        var caseDrillDownUrl = encodeURI(fetchDrillDownDataUrl + "?substance=" + substance + "&id=" + id + "&pt=" + pt + "&flagVar=" + flag_var + "&isStartDate=" + isStartDate + "&alertId=" + alertId + "&numberOfCount=" + value);
        if (url && url != '') {
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
        var actionButton = '<div style="display: block" class="btn-group dropdown"> \
                    <a class="dropdown-toggle ' + className + '" data-toggle="dropdown" href="#">' + count + '</a> \
                            <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                                <li role="presentation"><a href="#" data-url=' + caseDrillDownTableUrl + ' class="case-drill-down"><span class=' + className + '>' + 'Case List' + '</span></a></li> \
                                <li role="presentation"><a href="#"><span>' + 'Request for ICSRs' + '</span></a></li> \
                                </ul> \
                        </div>';
        return actionButton;
    };

    var wrapWithSpan = function (data, className) {
        var content = '<span class="' + className + '">' + data + '</span>';
        return content;
    };

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

    var init = function () {
        signal.fieldManagement.populateColumnList(gridColumnsViewUrl, gridColumnsViewUpdateUrl);
        alertDetailsTable = initAlertDetailsTable()

        $('i#copySelection').on('click', function () {
            showAllSelectedCaseNumbers();
        });

        initUpdateAssignedTo();

        actionButton("#alertDetailsTable");
    };

    var populateActivity = function () {
        if (!activities_table) {
            if (callingScreen == 'review') {
                activities_table = signal.activities_utils.init_activities_table("#activitiesTable",
                    '/signal/activity/listByExeConfig?' + 'executedIdList=' + executedIdList + "&appType=" + applicationName, applicationName);
            } else {
                activities_table = signal.activities_utils.init_activities_table("#activitiesTable",
                    '/signal/activity/listActivities?' + "appType=" + applicationName, applicationName);
            }
        }
    };

    $('#exportTypes a[href]').on('click', function (e) {
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
            updatedExportUrl = clickedURL + '&isFilterRequest=' + isFilterRequest + '&filters=' + encodeURI(JSON.stringify(filterValues)) + "&selectedCases=" + ids + "&viewId="+viewId;

        } else {
            var filterList = {}
            $(".yadcf-filter-wrapper").each(function () {
                var filterVal = $(this).children().val();
                if (filterVal) {
                    filterList[$(this).prev().attr('data-field')] = filterVal
                }
            });
            var filterListJson = JSON.stringify(filterList);
            updatedExportUrl = clickedURL + '&isFilterRequest=' + isFilterRequest + '&filters=' + encodeURI(JSON.stringify(filterValues)) + "&frequency=" + freqSelected + "&viewId="+ viewId + "&filterList=" + encodeURIComponent(filterListJson);
            if ($('#advanced-filter').val()) {
                updatedExportUrl += "&advancedFilterId="+$('#advanced-filter').val()
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
        if($('#evdas-case-drill-down-table').DataTable().order()){
            sortCol =$('#evdas-case-drill-down-table').DataTable().order()[0][0];
            sortDir=$('#evdas-case-drill-down-table').DataTable().order()[0][1];
        }
        updatedExportUrl = clickedURL + '&filterText=' + $('.dt-input').val() + '&sortedCol=' + sortCol + '&sortedDir=' + sortDir + '&isArchived=' + isArchived;
        window.location.href = updatedExportUrl;
        return false;
    });

    $(".exportAlertHistories").on('click', function (e) {
        var caseHistoryModal = $('#caseHistoryModal');
        var caseVal = caseHistoryModal.find("#caseNumber").html();
        var productFamily = caseHistoryModal.find("#productFamily").html();
        var selectedCase = e.currentTarget.href + "&caseNumber=" + caseVal + "&productFamily=" + encodeURIComponent(productFamily);
        window.location.href = selectedCase;
        return false
    });
    enableMenuTooltipsPopup();

    init();
    $("#alertsDetailsTable_filter").hide();
});

function jsUcfirst(string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
}

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