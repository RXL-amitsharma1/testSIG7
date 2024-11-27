//= require app/pvs/common/rx_common.js
//= require wikidiff
//= require text_difference


$(function() {
    compareScreens();
    closePopUp();
    $('.case-difference').on('click', function() {
       var caseJson = {
           "caseNumber": $(".caseNumber").val(),
           "followUpNumber": $("#followUpNumber").val()
       };
       var diffData = null;
       $.ajax({
           url: "/signal/caseInfo/caseDiff",
           data: caseJson,
           async: false
       }).done(function (result) {
              diffData = result
           })
       var caseDiffModal = $('#caseDiffModal');
       var case_diff_template = signal.utils.render('case_diff_details', diffData);
       caseDiffModal.find(".modal-body").html(case_diff_template);
       caseDiffModal.modal('show');
    });

    var followUpNumber = $("#followUpNumber").val();
    if (typeof followUpNumber == "undefined" || followUpNumber == null || followUpNumber === "" || followUpNumber === "0") {
        $(".case-difference").hide();
        $(".narrative-difference").hide();
    }


    $('#followUpNumber').on('change', function () {
        var version;
        if($('#caseVersion').val()){
            version = $(this).find(':selected').attr('data-version');
        } else {
            version = $('#version').val();
        }
        if ($(this).val()) {
            signal.utils.postUrl("/signal/caseInfo/caseDetail", {
                caseNumber: $(".caseNumber").val(),
                version: version,
                followUpNumber: $(this).val(),
                alertId: $(".alertId").val(),
                isFaers: $("#isFaers").val(),
                fullCaseList: $('#fullCaseList').val(),
                detailsParameters: $("#detailsParameters").val(),
                isAdhocRun: $(".isAdhocRun").val(),
                totalCount: $("#totalCount").val(),
                execConfigId: $("#execConfigId").val(),
                detailsParameters: $("#detailsParameters").val(),
                isSingleAlertScreen: $("#isSingleAlertScreen").val(),
                isCaseSeries: $("#isCaseSeries").val(),
                isArchived: $("#isArchived").val(),
                isVersion: false,
                oldFollowUp  : $("#oldFollowUp").val(),
                oldVersion   : $("#oldVersion").val(),
                isChildCase : typeof isChildCase !== "undefined" ? isChildCase : false
            });
        }
    });

    $('#evdasVersionNumber').on('change', function () {
        if ($(this).val()) {
            var wwid = $("#wwid").val()
            var caseType = $("#caseType").val()
            var caseNumber = $(".caseNumber").val();
            var alertId = $(".alertId").val();
            if($('#wwid').val()?.length>0 && alertId?.length>0){
                window.location = "/signal/caseInfo/evdasCaseDetail?caseNumber=" + "&wwid=" + wwid  + "&alertId=" + alertId
            }else{
                window.location = "/signal/caseInfo/evdasCaseDetail?caseNumber=" + caseNumber + "&version=" + $(this).val() + "&alertId=" + alertId + "&wwid=" + wwid + "&caseType=" + caseType
            }
        }
    });

    $('.narrative-difference').on('click', function() {
        var caseJson = {
            "caseNumber": $(".caseNumber").val(),
            "followUpNumber": $('#followUpNumber').val()
        };

        $.ajax({
            url: "/signal/caseInfo/getCaseNarativeInfo",
            data: caseJson,
            async: false,
        })
            .done(function (result) {
                var newNarrative = result.newNarrative;
                var oldNarrative = result.oldNarrative;
                fetchDiff(oldNarrative, newNarrative);
            })
            .fail(function () {
                console.log("Error in fetching the case narrative")
            });
    })


});

function fetchComparedContent(value, columnName, row, tableName, compareData) {
    var dataList = compareData[tableName];
    var uniqueMap = uniqueKeyMap[tableName];
    if(!freeTextField.includes(tableName)) {
        var comparedVersionRow = findComparedVersionRow(dataList, uniqueMap, row);
        if(comparedVersionRow == undefined &&  jQuery.isEmptyObject(value)){
            return fetchDiff("", "-");
        }else if ((comparedVersionRow == undefined || comparedVersionRow[columnName] == null || comparedVersionRow[columnName] == "") && jQuery.isEmptyObject(value)) {
            return '-'
        }else if (comparedVersionRow != undefined && String(value) != comparedVersionRow[columnName]) {
            if (comparedVersionRow[columnName] == null) {
                comparedVersionRow[columnName] = ""
            }
            if (jQuery.isEmptyObject(value)) {
                value = "";
            }
            isChangePresent = true;
            if(Date.parse(String(value)) && Date.parse(comparedVersionRow[columnName])){
                var changeHistoryHtml = "<div class=\"wikEdDiffContainer\" id=\"wikEdDiffContainer\"><pre class=\"wikEdDiffFragment\" style=\"white-space: pre-wrap;\">"
                changeHistoryHtml += "<span class=\"wikEdDiffDelete\" title=\"-\">" + comparedVersionRow[columnName] + "</span>"
                changeHistoryHtml += "<span class=\"wikEdDiffInsert\" title=\"+\">" + String(value) + "</span></pre></div>"
                return changeHistoryHtml
            }
            return fetchDiff(comparedVersionRow[columnName], String(value))
        } else if (comparedVersionRow != undefined && String(value) == comparedVersionRow[columnName]) {
            return value;
        } else {
            isChangePresent = true;
            return fetchDiff("", String(value));
        }
    }else{
        var comparedData = dataList[0][columnName];
        if((String(value) == null || jQuery.isEmptyObject(value) || value == undefined) && (comparedData == undefined || comparedData == null)){
            return '-';
        } else if(String(value) == null || jQuery.isEmptyObject(value) || value == undefined){
            isChangePresent = true;
            return fetchDiff( comparedData,"");
        } else if(comparedData == undefined || comparedData == null){
            isChangePresent = true;
            return fetchDiff("", String(value))
        } else if(String(value).replace( /\r\n?/g, '\n') === comparedData.replace( /\r\n?/g, '\n')){
            return value;
        } else{
            isChangePresent = true;
            return customFetchDiff(comparedData, String(value));
        }
    }
}

/**
 * This Method compares two texts, where similar part of text remains as it is. whereas added text is highlighted with green colour and removed text is highlighted red.
 * @param oldText
 * @param newText
 * @returns {string}
 */
function customFetchDiff(oldText, newText) {
    const oldWords = oldText.split(/[\n]/);
    const newWords = newText.split(/[\n]/);
    const oldLength = oldWords.length
    const newLength = newWords.length
    let diffHtml = '<div class=\"wikEdDiffContainer\" id=\"wikEdDiffContainer\"><pre class=\"wikEdDiffFragment\" style=\"white-space: pre-wrap;\">';
    let i = 0;
    let j = 0;
    while (i < oldLength || j < newLength) {
        if (i < oldLength && j < newLength && oldWords[i].trim() === newWords[j].trim()) {
            diffHtml += oldWords[i] + ' ';
            i++;
            j++;
        } else {
            let removeText = '';
            while (i < oldLength && (j >= newLength || oldWords[i] !== newWords[j])) {
                removeText += oldWords[i] + ' ';
                i++;
            }
            if (removeText) {
                diffHtml += "<span class=\"wikEdDiffDelete\" title=\"-\">" + removeText + "</span>";
            }

            let addText = '';
            while (j < newLength && (i >= oldLength || oldWords[i] !== newWords[j])) {
                addText += newWords[j] + ' ';
                j++;
            }
            if (addText) {
                if (removeText) {
                    diffHtml += "<br>"
                }
                diffHtml += '<span class=\"wikEdDiffInsert\" title=\"+\">' + addText + '</span>';
            }
        }
        diffHtml += "<br>"
    }
    diffHtml += '</pre></div>';
    return diffHtml;
}

function zoomIn() {
    $(".zoom-out-element").on('click', function () {
        var $currentContainer = $(this).closest(".rxmain-container");
        var size1 = parseInt($($currentContainer).find(".row.rxmain-container-content").css("font-size"));
        var size2 = parseInt($($currentContainer).find(".row.rxmain-container-content pre").css("font-size"));
        size2 = size2 + 1;
        size1 += 1;
        $($currentContainer).find(".row.rxmain-container-content").css("font-size", size1);
        var $containerContentElement = $($currentContainer).find(".row.rxmain-container-content")
        var maxHeight = $containerContentElement.height();
        if (size1 >= 14 && maxHeight && $containerContentElement.css("max-height") == 'none') {
            $($currentContainer).find(".row.rxmain-container-content").css("max-height", maxHeight);
        }
        $($currentContainer).find(".row.rxmain-container-content").css("overflow-y", "scroll");
        $($currentContainer).find(".row.rxmain-container-content pre").css("font-size", size2);
        $($currentContainer).find(".zoom-viewer").text(parseInt( $($currentContainer).find(".zoom-viewer").text()) + 10)
    });
}

function zoomOut() {
    $(".zoom-in-element").on('click', function () {
        var $currentContainer = $(this).closest(".rxmain-container");
        var size1 = parseInt($($currentContainer).find(".row.rxmain-container-content").css("font-size"));
        var size2 = parseInt($($currentContainer).find(".row.rxmain-container-content pre").css("font-size"));
        size1 = size1 - 1;
        size2 -= 1
        if (size1 <= 8) {
            size1 = 8;
        }
        if (size2 <= 8) {
            size2 = 8;
        } else{
            $($currentContainer).find(".zoom-viewer").text(parseInt($($currentContainer).find(".zoom-viewer").text()) - 10);
        }
        $($currentContainer).find(".row.rxmain-container-content").css("font-size", size1);
        $($currentContainer).find(".row.rxmain-container-content pre").css("font-size", size2);
    });
}

function refreshZoom(){
    $(".refresh-zoom").on('click', function(){
        var $currentContainer = $(this).closest(".rxmain-container");
        $($currentContainer).find(".row.rxmain-container-content").css("font-size", '13px');
        $($currentContainer).find(".row.rxmain-container-content pre").css("font-size", '14px');
        $($currentContainer).find(".zoom-viewer").text('100');
        $($currentContainer).find(".row.rxmain-container-content").css("max-height", "none");
        $($currentContainer).find(".row.rxmain-container-content").css("overflow-y", "auto");
    });
}

function compareScreens() {
    openVersionsList();
    var followUp = $(".followUpNumber").val();
    $('#followUpNumber option[value="' + followUp +'"]').attr('selected', 'selected')
    $(".version-available, .version-compare").on('click', function(event) {
        event.stopPropagation();
        if ($(this).hasClass("version-available")) {
            $(".version-available").each(function () {
                $(this).css("color", "#222");
            });
            versionAvailable = $(this).attr("data-id");
            followUpAvailable = $(this).attr("data-followUp");
            $(this).css("color", "#007CB5");
            $(".version-compare").each(function () {
                $(this).removeClass("btn-disabled");
            })
            $(".version-compare[data-id='" + versionAvailable + "']").addClass("btn-disabled");
        } else {
            $(".version-compare").each(function () {
                $(this).css("color", "#222");
            });
            versionCompare = $(this).attr("data-id");
            followUpCompare = $(this).attr("data-followUp");
            $(this).css("color", "#007CB5");
            $(".version-available").each(function () {
                $(this).removeClass("btn-disabled");
            });
            $(".version-available[data-id='" + versionCompare + "']").addClass("btn-disabled");
        }
        redirectComparingScreen(versionAvailable, versionCompare, followUpAvailable, followUpCompare)
    })
}

function openVersionsList() {
    $("#version-list").on('click', function(event){
        event.stopPropagation();
        if( $("#version-list-tooltip").hasClass('hide')) {
            $("#version-list-tooltip").removeClass('hide');
            $("#version-list-tooltip").css('display','inline');
        }else{
            $("#version-list-tooltip").addClass('hide');
            $("#version-list-tooltip").hide();
        }
    });
}

function redirectComparingScreen(versionAvailable, versionCompare, followUpAvailable, followUpCompare) {
    var version;
    if ($('#caseVersion').val()) {
        version = $('#caseVersion').val();
    } else {
        version = $('#version').val();
    }
    if(versionCompare != undefined && versionAvailable != undefined) {
        signal.utils.postUrl("/signal/caseInfo/compareVersions", {
            caseNumber: $(".caseNumber").val(),
            version: version,
            followUpNumber: $(".followUpNumber").val(),
            alertId: $(".alertId").val(),
            isFaers: $("#isFaers").val(),
            fullCaseList: $('#fullCaseList').val(),
            detailsParameters: $("#detailsParameters").val(),
            isAdhocRun: $(".isAdhocRun").val(),
            totalCount: $("#totalCount").val(),
            execConfigId: $("#execConfigId").val(),
            detailsParameters: $("#detailsParameters").val(),
            isSingleAlertScreen: $("#isSingleAlertScreen").val(),
            isCaseSeries: $("#isCaseSeries").val(),
            isArchived: $("#isArchived").val(),
            versionCompare: versionCompare,
            versionAvailable: versionAvailable,
            followUpCompare: followUpCompare,
            followUpAvailable: followUpAvailable,
            isArgusDataSource: $("#isArgusDataSource").val(),
            oldFollowUp  : $("#oldFollowUp").val(),
            oldVersion   : $("#oldVersion").val(),
            isChildCase : typeof isChildCase !== "undefined" ? isChildCase : false
        });
    }
}

function comparePreviousVersions(){
    $(".compare-versions").on('click', function () {
        var version;
        if ($('#caseVersion').val()) {
            version = $('#caseVersion').val();
        } else {
            version = $('#version').val();
        }
        if ($(".isAdhocRun").val() == 'true') {
            version = $('#caseVersionAdhoc').val();
        }
        var followUp = $("#followUpNumber").val();
        var immediatePrevVersion = 1;
        var immediatePreviousFollow = 0;
        $(".version-available").each(function(){
            var curr = $(this).attr("data-id")
            if(parseInt(curr)>immediatePrevVersion && parseInt(curr)<version){
                immediatePrevVersion = parseInt(curr);
                immediatePreviousFollow = parseInt($(this).attr("data-followUp"));
            }
        });
        if ($("#previous-version").val() != undefined) {
            redirectComparingScreen(version, $("#previous-version").val(),followUp, $("#previous-followUp").val());
        } else if($("#isCaseSeries").val() == "true"){
            $.Notification.notify('warning', 'top right', "Warning", $.i18n._('noVersionAvailableToCompare'), {autoHideDelay: 5000});
        } else{
            redirectComparingScreen(version, immediatePrevVersion, followUp, immediatePreviousFollow);
        }
    })
}

function findComparedVersionRow(dataList, uniqueMap, row){
    var ans = undefined;
    $.each(dataList , function(key, map){
        var flag = true;
        $.each(uniqueMap, function(ind , value){
            if(map[value] == row[value]){
                flag = flag && true;
            } else{
                flag = false;
            }
        });
        if(flag){
            ans = map;
        }
    });

    return ans;
}

function closePopUp(){
    $(document.body).on('click', function() {
        $("#version-list-tooltip").addClass('hide');
        $("#version-list-tooltip").hide();
    });
}

function openCaseDetailsPageForChildCase(caseNumber) {
    var isFaersCheck = false;
    if(typeof isFaers === "string"){
        isFaersCheck = isFaers === "true";
    } else if(typeof isFaers === "boolean"){
        isFaersCheck = isFaers;
    }
    signal.utils.postUrl(caseDetailUrl, {
        caseNumber: caseNumber,
        isChildCase: true,
        isFaers: isFaersCheck
    }, true);
}
