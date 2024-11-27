//= require app/pvs/common/rx_handlebar_ext.js

var signal = signal || {}

$(function(){
    fetchViewInstanceList();
})


$('#addAdvancedFilter').on('click', function () {
    signal.advancedFilter.initializeAdvancedFilters($('#createAdvancedFilterModal'));
    $('#createAdvancedFilterModal #selectOperator').empty().append('<option selected="selected" value="">Select Operator</option>');
    $('#createAdvancedFilterModal #name').val('').attr("disabled", false);
    $('#createAdvancedFilterModal #description').val('');
    $('#createAdvancedFilterModal #queryJSON').val('');
    $('#createAdvancedFilterModal #alertType').val('');
    $('#createAdvancedFilterModal #advancedFilterSharedWith').empty();
    $('#createAdvancedFilterModal #builderAll').find('.expression').remove();
    $('#createAdvancedFilterModal #filterId').val("");
    $('#createAdvancedFilterModal .deleteAdvFilter').addClass('hide');
    $('#createAdvancedFilterModal .deleteAdvFilter').attr("disabled", false);
    $('#createAdvancedFilterModal #addExpression').attr("disabled", false);
    $('#createAdvancedFilterModal .filtersWithoutSaving').removeClass('hide');
    $('#createAdvancedFilterModal .saveAdvancedFilters').attr("disabled", false);
    $('#createAdvancedFilterModal').modal('show');

});


$('#editAdvancedFilter').off().on('click', function () {
    if (filterOpened == 0) {
        var advancedFilterId = $('.advanced-filter-dropdown').val();
        if (advancedFilterId) {
            $.ajax({
                url: fetchAdvancedFilterInfoUrl,
                data: {'advancedFilter.id': advancedFilterId},
                dataType: 'json',
                type: "GET",
            }).done(function (result) {
                var isDisabled = (result.isFilterUpdateAllowed == true) ? false : true
                $('#createAdvancedFilterModal #selectOperator').empty().append('<option selected="selected" value="">Select Operator</option>');
                $('#createAdvancedFilterModal #name').val(result.name).attr("disabled", isDisabled);
                $('#createAdvancedFilterModal #name').val(result.name);
                $('#createAdvancedFilterModal #description').val(result.description);
                $('#createAdvancedFilterModal #queryJSON').val(result.JSONQuery);
                $('#createAdvancedFilterModal #alertType').val(alertType);
                $('#createAdvancedFilterModal #filterId').val(advancedFilterId);
                $('#createAdvancedFilterModal #shareAdvFilterId').html(result.shareWithElement);
                $('#createAdvancedFilterModal .filtersWithoutSaving').addClass('hide');
                $('#createAdvancedFilterModal .deleteAdvFilter').removeClass('hide');
                $('#createAdvancedFilterModal #addExpression').attr("disabled", isDisabled);
                $('#createAdvancedFilterModal .deleteAdvFilter').attr("disabled", isDisabled);
                $('#createAdvancedFilterModal .saveAdvancedFilters').attr("disabled", isDisabled);
                $('#createAdvancedFilterModal .modal-title').html("Edit Filter");
                signal.advancedFilter.initializeAdvancedFilters();
                $('#createAdvancedFilterModal').modal('show');
            });
        }
        filterOpened = 1;
    }
});



$(".advanced-filter-dropdown").select2({
    placeholder: $.i18n._('selectOne'),
    allowClear: true,
    ajax: {
        url: fetchAdvFilterUrl,
        dataType: 'json',
        type: "GET",
        quietMillis: 50,
        data: function (params) {
            return {
                alertType: null,
                term: '',
                page: 1,
                max: 30,
                callingScreen: ''
            };
        },
        processResults: function (data, params) {
            params.page = params.page || 1;
            return {
                results: $.map(data.list, function (filter) {
                    return {
                        text: filter.name,
                        id: filter.id
                    }
                }),
                pagination: {
                    more: (params.page * 30) < data.totalCount
                }
            };
        }
    }
});

var fetchViewInstanceList = function(isTempView = false) {
    // if($("#isAdhocCaseSeries").val() != undefined && $('#isAdhocCaseSeries').val() == 'true' && $("#isJader").val() != "true"){
        alertType = ALERT_CONFIG_TYPE.AGGREGATE_CASE_ALERT
    viewId=7860
    // }
    $.ajax({
        url: "/signal/viewInstance/fetchViewInstances",
        data: {alertType : alertType , viewId : viewId}
    }).done(function (data) {
        var viewsList = data.viewsList;
        var isDropDownRequired  = viewsList.length > 5 ? true : false;
        viewsGridList =  signal.utils.render('views_list_v1', {
            selectedViewId : viewId,
            viewsList : viewsList,
            isDropDownRequired : isDropDownRequired,
            viewId : viewId,
            isTempViewSelected : isTempView
        });
        $(".views-list").html(viewsGridList);
        webUiPopInit();
        openSelectedView();
        bookmarkDrag();
        viewsList.sort((a, b) => (a.name.toUpperCase() > b.name.toUpperCase()) ? 1 : -1);
        filteredViews = viewsList.filter(item => item.name !== "System View")
        $.each(filteredViews , function(key , value) {
            if(value.id != viewId) {
                $("#viewsListSelect").append(new Option(value.name, value.id));
            } else {
                $('#save-view-modal #view_name').val(value.name.replace("(S)" , ""));
                $('#save-view-modal #view_default').prop('checked', (value.defaultView == '(default)'));
                $("#viewsListSelect").append(new Option(value.name, value.id , true , true));
            }
        })
        $("#temp-view-bookmark").on('click', function () {
            deleteTempView();
            var pageURL = $(location).attr("href");
            if(pageURL.indexOf("tempViewId") != -1) {
                var preIndex = pageURL.indexOf('tempViewId')
                pageURL = pageURL.slice(0, preIndex) + pageURL.slice(pageURL.indexOf('&' , preIndex) , -1)
            }
            isUpdateTemp = false
            window.location.href = pageURL
        })

        const bookmarks = document.getElementsByClassName('bookmark');
        Array.from(bookmarks).forEach(function(bookmark) {
            const text = bookmark.textContent.trim();
            if (text === 'System View') {
                bookmark.classList.add('active-bookmark');
            }
            bookmark.addEventListener('click', function(event) {
                event.preventDefault(); // Prevent default behavior like navigation
                event.stopImmediatePropagation(); // Prevent other listeners from being triggered
            });
        });

    });
};


var bookmarkDrag = function () {
    bookmarkDragStart();
    bookmarkDragOver();
    bookmarkDropped();
    bookmarkDragLeave();
}

var bookmarkDragStart = function() {
    document.addEventListener("dragstart", (event) => {
        $(event.target).addClass("color-black");
        dragged = event.target;
        value = event.target.value;
        list = $(".bookmark");
        $.each(list, function (key, value) {
            if (value == dragged) {
                index = key;
            }
        });
    });
};


var bookmarkDragOver = function() {
    document.addEventListener("dragover", (event) => {
        event.preventDefault();
    });
};

var bookmarkDragLeave = function() {
    document.addEventListener("dragleave", (event) => {
        event.preventDefault();
    });
}

var bookmarkDropped = function() {
    document.addEventListener("drop", ({target}) => {
        $(dragged).removeClass("color-black");
        if (target.className == "bookmark" && target.value !== value) {
            dragged.remove(dragged);
            $.each(list, function (key, value) {
                if (value == target) {
                    indexDrop = key;
                }
            });
            if (index > indexDrop) {
                target.before(dragged);
            } else {
                target.after(dragged);
            }
            if (index > 4 && indexDrop <= 4) {
                if ($("#sortable1 li:nth-child(6)").eq(0).hasClass("active-bookmark")) {
                    $("#sortable2").prepend($('#sortable1 li:nth-child(5)').eq(0))
                } else {
                    $("#sortable2").prepend($('#sortable1 li:nth-child(6)').eq(0))
                }
            }
            else if (index <= 4 && indexDrop > 4)
                ($('#sortable1 li:nth-child(4)')).after($('#sortable2 li:nth-child(1)'))
            var changedViewOrder = []
            var newOrder = $(".bookmark")
            $.each(newOrder, function (key, value) {
                if (value.value != list[key].value) {
                    changedViewOrder.push({id: value.value, order: key + 1})
                }
            });
            $.ajax({
                url: '/signal/viewInstance/saveBookmarkPositions',
                data: {updatedViewsOrder: JSON.stringify(changedViewOrder)},
            });
        }
    });
}


var openSelectedView = function() {
    $('.bookmark').on('click', function () {
        // var pageURL = $(location).attr("href");
        // var selectedView = $(this).val();
        // sessionStorage.setItem('isViewCall', 'true');
        // if(pageURL.indexOf("tempViewId") != -1) {
        //     var preIndex = pageURL.indexOf('tempViewId')
        //     pageURL = pageURL.slice(0, preIndex) + pageURL.slice(pageURL.indexOf('&' , preIndex) , -1)
        // }
        // if (pageURL.indexOf("detailedAdvancedFilterId") != -1) {
        //     const advanceFilterRegex = /&detailedAdvancedFilterId=(\d+)/;
        //     pageURL = pageURL.replace(advanceFilterRegex, "");
        // }
        // isUpdateTemp = false
        // var preIndex = pageURL.indexOf('viewId')
        // if (pageURL.indexOf("viewId") != -1 && (pageURL.indexOf("isJader") != -1 || pageURL.indexOf("isVigibase") != -1 || pageURL.indexOf("isVaers") != -1 || pageURL.indexOf("isFaers") != -1) && pageURL.indexOf('&' , preIndex ) != -1) {
        //     window.location.href = pageURL.slice(0, preIndex - 1) + pageURL.slice(pageURL.indexOf('&' , preIndex )) + "&viewId=" + selectedView
        // } else if (pageURL.indexOf("viewId") != -1 && (pageURL.indexOf("isJader") != -1 || pageURL.indexOf("isVigibase") != -1 || pageURL.indexOf("isVaers") != -1 || pageURL.indexOf("isFaers") != -1)) {
        //     window.location.href = pageURL.slice(0, preIndex - 1) + "&viewId=" + selectedView
        // } else if (pageURL.indexOf("viewId") != -1) {
        //     window.location.href = pageURL.slice(0, pageURL.indexOf("viewId") + 7) + selectedView
        // } else {
        //     if (pageURL.indexOf("#") != -1) {
        //         pageURL = pageURL.slice(0, pageURL.indexOf("#"))
        //     }
        //     window.location.href = pageURL + "&viewId=" + selectedView
        // }
    });
};


$('#searchDataMiningResult').off('click').on('click', function() {
    var button = $(this);
    button.prop('disabled', true);

    let productName = $('#showProductSelection').text().trim(); // Trim any extra spaces
    let modifiedProductName = productName.replace(/\s*\((Substance|Product\s*Name)\)\s*$/i, '');
    if (!modifiedProductName) {
        $.Notification.notify('error', 'top right', "Failed", 'Please enter all the mandatory fields.', {autoHideDelay: 5000});
        button.prop('disabled', false);
    } else {

        setTimeout(function() {
            $.ajax({
                url: fetchResultUrl,
                method: 'GET',
                dataType: 'json',
            }).done(function(response) {
                button.prop('disabled', false);
                renderTableBody(response.aaData);
            }).fail(function(xhr, status, error) {
                console.error('Error fetching data', xhr.responseText);
                button.prop('disabled', false);
            });
        }, 4000);
    }
});



function renderTableBody(data) {
    console.log('Data received:', data); // Log the data to see its structure
    const tableBody = document.createElement('tbody');
    var jsonString = $('#eventSelection').val();
    var name
    if(jsonString){
        var jsonObject = JSON.parse(jsonString);
        if (jsonObject["4"].length > 0) {
            name = jsonObject["4"][0].name;
        }
    }
    let productName = $('#showProductSelection').text().trim(); // Trim any extra spaces
    let modifiedProductName = productName
        .replace(/\s*\(Substance\)\s*/gi, '')
        .replace(/\s*\(Product Name\)\s*/gi, '')
        .replace(/\s*\(Product - Dosage Forms\)\s*/gi, '')
        .replace(/\s*\(Trade Name\)\s*/gi, '');
    modifiedProductName = modifiedProductName.trim();


    if (data && Array.isArray(data) && modifiedProductName) {
        data.forEach((rowData, index) => {
            const row = document.createElement('tr');
            row.className = (index % 2 === 0) ? 'odd' : 'even';
                row.innerHTML = `
                    <td style=""><input type="checkbox"/></td>
                    <td class="dropDown no-padding" style="height: 45px;" data-dt-row="0" data-dt-column="1"><div style="display: block;" class="btn-group dropdown dataTableHideCellContent" align="center">                         <a class="dropdown-toggle" data-toggle="dropdown" tabindex="0" role="button">                         <span style="cursor: pointer;font-size: 125%;" class="glyphicon glyphicon-option-vertical"></span><span class="sr-only">Toggle Dropdown</span>                         </a> <ul class="dropdown-menu menu-cosy" role="menu"><li role="presentation"><a tabindex="0" class="review-row-icon  product-event-history-icon" data-datasrc="PVA" data-configid="3324"><span class=" fa fa-list m-r-10"></span>Associate Signal</a></li><li role="presentation"><a tabindex="0" class="review-row-icon show-chart-icon" data-value="842657"><span class="fa fa-line-chart m-r-10"></span>Charts</a></li></ul></div></td>
                    <td style="min-width: 200px;">${modifiedProductName ? modifiedProductName:rowData.productName}</td>
                    <td style="min-width: 100px;">${rowData.soc}</td>
                    <td style="min-width: 200px;">${name?name:rowData.preferredTerm}</td>
                    <td style="min-width: 100px;">Rashes, eruptions and exanthems NEC</td>
                    <td style="min-width: 100px;">${rowData.listed}</td>
                    <td style="min-width: 100px;" class=""><div class="stacked-cell-center-top">${drill_down_options('REPORT_TOTAL_FLG', nullableNumberValue(rowData.newCount), 'NEW', 'CUMM_FLAG','NEW_COUNT', rowData,true,0)}</div><div class="stacked-cell-center-top">${drill_down_options('REPORT_TOTAL_FLG', nullableNumberValue(rowData.cummCount), 'CUMM', 'CUMM_FLAG','CUMM_COUNT', rowData,false,0)}</div></td>
                    <td style="min-width: 100px;" class="stacked-cell-center-top"><div class="stacked-cell-center-top">${drill_down_options('REPORT_SERIOUS_FLG', nullableNumberValue(rowData.newSeriousCount), 'NEW', 'SERIOUS_FLAG', 'NEW_SER', rowData, true, 3)}</div><div class="stacked-cell-center-top">${drill_down_options('SERIOUS_FLAG', nullableNumberValue(rowData.cumSeriousCount), 'CUMM', 'SERIOUS_FLAG', 'CUMM_SER', rowData, false, 3)}</div></td>
                    <td style="min-width: 100px;" class="stacked-cell-center-top"><div class="stacked-cell-center-top"><a href="">${rowData.newFatalCount}</a></div><div class="stacked-cell-center-top"><a href="">${rowData.cumFatalCount}</a></div></td>
                    <td style="min-width: 100px;" class="stacked-cell-center-top"><div class="stacked-cell-center-top"><a href="">${rowData.newPediatricCount}</a></div><div class="stacked-cell-center-top"><a href="">${rowData.cummPediatricCount}</a></div></td>
                    <td style="min-width: 100px;" class="stacked-cell-center-top"><div class="stacked-cell-center-top"><a href="">${rowData.newStudyCount}</a></div><div class="stacked-cell-center-top"><a href="">${rowData.cumStudyCount}</a></div></td>
                    <td style="min-width: 100px;" class="stacked-cell-center-top"><div class="stacked-cell-center-top">${rowData.prrLCI}</div><div class="stacked-cell-center-top">${rowData.prrUCI}</div></td>
                    <td style="min-width: 100px; text-align: center !important;" class="stacked-cell-center-top">${nullableNumberValue(rowData.prrValue)}</td>
                    <td style="min-width: 100px" class="stacked-cell-center-top"><div class="stacked-cell-center-top">${rowData.rorLCI}</div><div class="stacked-cell-center-top">${rowData.rorUCI}</div></td>
                    <td style="min-width: 100px; text-align: center !important;" class="stacked-cell-center-top">${nullableNumberValue(rowData.rorValue)}</td>
                    <td style="min-width: 100px; text-align: center !important;" class="stacked-cell-center-top">${nullableNumberValue(rowData.ebgm)}</td>
                    <td style="min-width: 100px;" class="stacked-cell-center-top"><div class="stacked-cell-center-top">${nullableNumberValue(rowData.eb05)}</div><div class="stacked-cell-center-top">${nullableNumberValue(rowData.eb95)}</div></td>
                `;
            // Append the row to the table body
            tableBody.appendChild(row);
        });
    } else {
        console.error('Data is not an array or does not exist', data);
    }

    // Clear any existing tbody and append the new one
    $('#alertsDetailsTable').find('tbody').remove();
    $('#alertsDetailsTable').append(tableBody);
}




var drill_down_options = function (keyId,value,type,typeFlag,className,row,isStartDate,flag_var) {
    if(row.dataSource == 'EVDAS'){
        return evdasDrillDownOptions(value, row.productName, row.preferredTerm, row.execConfigId, flag_var,isStartDate,row.id,'',className,row.isArchived);
    }else {
        if(row.dataSourceValue == undefined || row.dataSourceValue == "undefined") {
            row.dataSourceValue = row.dataSource
        }
        if(row.dataSource === "JADER"){
            // Report Type Flag is not changed for Jader
            keyId = typeFlag
        }
        if (null != row.dataSourceValue) {
            if ((row.dataSourceValue).toUpperCase() === "PVA") {
                typeFlag = keyId;
            } else if (((row.dataSourceValue).split(",").length > 0) ? ((row.dataSourceValue).split(",")[0].toUpperCase() === "PVA") : false) {
                typeFlag = keyId;
            }
        }
        return cnt_drill_down(value,type,typeFlag,row.execConfigId, row.id, row.productId, row.ptCode,className, row.dataSourceValue, row.productName, row.preferredTerm,row.isArchived);
    }
};



var cnt_drill_down = function (value, type, typeFlag, executedConfigId, alertId, productId, ptCode, className,dataSource,productName,preferredTerm,isArchived) {
    if (value === -1 || value === '-') {
        return '<a tabindex="0">' + '-' + '</a>'
    }
    if (value == 0 || value=="0") {
        return '<span class="blue-1">' + value + '</span>'
    }
    var listConfigUrl = "/signal/aggregateCaseAlert/caseDrillDown?id=" + alertId + '&typeFlag=' + typeFlag + '&type=' +
        type + "&executedConfigId=" + executedConfigId + "&productId=" + productId + "&ptCode=" + ptCode
    var singleCaseDetailsUrl = '/signal/singleCaseAlert/caseSeriesDetails?aggExecutionId=' + executedConfigId + '&aggAlertId=' + alertId + '&aggCountType=' + className + '&productId=' + productId + '&ptCode=' + ptCode + '&type=' + type + "&typeFlag=" + typeFlag + "&isArchived=" + isArchived;
    var seriesData = "id:" + alertId + ",typeFlag:" + typeFlag + ",type:" + type + ",executedConfigId:" +
        executedConfigId + ",productId:" + productId + ",ptCode:" + ptCode;

    if (true) {
        var actionButton = '<div style="display: block" class="btn-group dropdown"> \
                    <a class="dropdown-toggle ' + className + '" data-toggle="dropdown" href="#">' + value + '</a> \
                            <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 120px !important;font-size: 12px;margin-top: 4px;margin-right: 22px;"> \
                                <li role="presentation"><a href=' + singleCaseDetailsUrl + ' target="_blank" data-url=' + listConfigUrl + ' class="">' + 'Case Series' + '</a></li> \
                                </ul> \
                        </div>';
        return actionButton;
    } else {
        return '<a href="#" data-url=' + listConfigUrl + ' class="case-drill-down-link ' + className + '">' + value + '</a>'
    }
};


var evdasDrillDownOptions = function (value, substance, pt, id, flag_var, isStartDate, alertId, url, className,isArchived) {
    if (value === -1 || value === '-') {
        return '<a tabindex="0">' + '-' + '</a>'
    }
    if (value == 0 || value=="0") {
        return '<span class="blue-1">' + value + '</span>'
    }
    var actionButton;
    var isArchived = isArchived;
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
        actionButton = '<a href="' + caseDrillDownUrl + '" class="evdas-case-drill-down-link" data-archive="' + isArchived + '"><span class="' + className + '">' + value + '</span></a>'
    }
    return actionButton
};


function nullableNumberValue(value) {
    return (value === null || value === 'null') ? '-' : value;
}
