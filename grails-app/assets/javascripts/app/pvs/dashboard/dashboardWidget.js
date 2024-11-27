var isloaded=false
var curruntObject;
var apiurlGetLinks = "dashboard/fetchAttachments",
    apiPinLink = "dashboard/pinReferences",
    apiDelLink= "dashboard/delete",
    apiDragDropLink = "dashboard/dragAndDropRefrences";
var totalLinkCount;
var pageNum =1;
var lastScrollTop = 0;
var reqLen;
$(function () {
    signal.utils.localStorageUtil.setProp("dashboardWidgetsConfig", dashboardWidgetConfigJSON);

    var gridstackOptions = {
        column: 12,
        draggable: {
            handle: ".pv-sec-heading",
        }
    };

    var gridstack = GridStack.init(gridstackOptions, '.grid-stack');

    $(".grid-stack").on('change added removed', function (event, items) {
        refreshDashboardConfigJSON();
        if (items) {
            for (var i = 0; i < items.length; i++) {
                var item = items[i];
                var chartContainer = $(item.el).find('.chart-container').first();
                if (chartContainer.length) {
                    if (typeof chartContainer.highcharts() != "undefined" && chartContainer.highcharts() != null) {
                        chartContainer.highcharts().setSize(chartContainer.innerWidth(), chartContainer.innerHeight());
                    }
                }
            }
        }
        if(document.readyState=="complete") {
            persistDashboardWidgetConfig();
        }
    });
    gridstack.on('resizestop', function (event, elem) {
        // update chart width and height on resize
        resizeWidgetContents(elem);
    });

    populateDashboardWidgets();
    populateClosedWidgetsMenu();
    $('.newRow').hide();
    //Code to close the widget and populate side menu.
    $('.close-widget').on('click', closeWidget);

    $('.refresh-widget').on('click', refreshWidget);
    $('.add-refrences').on('click', addReference);
    $('.openStatusComment').on('click', commentDescription);
    $(document).on('click', '.pv-widget-list-item', function () {
        var panel = $(this).closest(".panel");
        var targetWidgetId = $(this).data('target-widget');

        $(this).addClass("list-hide");
        setTimeout(function () {
            $(".list-hide").remove();
            isWidgetAvailable(panel);
            isWidgetSelectorMenuAvailable();

            var dashboardWidgetsConfig_local = signal.utils.localStorageUtil.getJSON("dashboardWidgetsConfig");
            dashboardWidgetsConfig_local[targetWidgetId]['visible'] = true;
            if(targetWidgetId === "pvWidgetChart-7"){
                dashboardWidgetsConfig_local[targetWidgetId].content.id =  "assignedSignalTable"
                dashboardWidgetsConfig_local[targetWidgetId].content.type =  "pvDashReports"
            }
            if(targetWidgetId === "pvWidgetChart-13"){
                dashboardWidgetsConfig_local[targetWidgetId].content.id =  "systemValidationPreChecks"
                dashboardWidgetsConfig_local[targetWidgetId].content.type =  "pvDashPreChecks"
            }
            signal.utils.localStorageUtil.setJSON("dashboardWidgetsConfig", dashboardWidgetsConfig_local);
            addWidget(dashboardWidgetsConfig_local[targetWidgetId], true);
        }, 500);
    });
    $(document).on("click",".openShareWithModal",function () {
        curruntObject=$(this);
        $("#sharedWithUserList").empty()
        $("#sharedWithUserList").append($(this).attr('usr'))
        $(".shareBtn").attr('data-refid',undefined)
        var isAutoSharedWith = $(this).closest("tr").find(".isAutoSharedWith").val();
        isAutoSharedWith = (isAutoSharedWith === "true")
        bindShareWith2WithData($("#sharedWith"),"/signal/user/searchShareWithUserGroupList","",true, isAutoSharedWith)
        $(".shareBtn").attr('data-refid',$(this).attr('data-refid'))
        var sharedWithGroupArray=[];
        if($(curruntObject).attr('sharedwithgroup')){
            sharedWithGroupArray= $(curruntObject).attr('sharedwithgroup').split(",");
        }

        var sharedWithGroup=''
        $("#sharedWithGroupList").empty()
        var refId=$(this).attr("data-refid")
        $.ajax({
            cache: false,
            type: 'GET',
            url: '/signal/dashboard/getSharedWith' + '?refId=' + refId,
        }).done(function(result) {
            $('#sharedWith').empty()
            var users = '';
            $.each(result.data.users, function() {
                users += this.name + '<br />'
            });
            $('#sharedWithUserList').html(users);
            var groups = '';
            $.each(result.data.groups, function() {
                groups +=  this.name + ' <br />'
            });
            $('#sharedWithGroupList').html(groups);

            $.each(result.data.all, function(i, data){
                var option = new Option(data.name, data.id, true, true);
                $('#sharedWith').append(option).trigger('change');
            });
        });
    });
    $(".shareBtn").on('click',shareData);
    $(".closeBtn").on('click',closeReference);

});

var persistDashboardWidgetConfig = function () {
    var dashboardWidgetsConfig_local = signal.utils.localStorageUtil.getJSON("dashboardWidgetsConfig");
    if (dashboardWidgetsConfig_local.isDirty) {
        $.ajax({
            type: "POST",
            url: '/signal/dashboard/saveDashboardConfig',
            data: {"dashboardWidgetsConfig": signal.utils.localStorageUtil.getProp("dashboardWidgetsConfig")}
        }).done(function (response) {
            if (response) {
                dashboardWidgetsConfig_local['isDirty'] = false;
                signal.utils.localStorageUtil.setJSON("dashboardWidgetsConfig", dashboardWidgetsConfig_local);
            } else {
                persistDashboardWidgetConfig();
            }
        });
    }
};

function refreshDashboardConfigJSON() {
    var dashboardWidgetsConfig_local = signal.utils.localStorageUtil.getJSON("dashboardWidgetsConfig");
    $.each(dashboardWidgetsConfig_local, function (a, b) {
        if (b.reportWidgetName !== "Inbox") {
            b.visible = false;
        }
    });
    $("#mainDashboard").children().each(function () {
        var temp = {};
        temp["visible"] = true;
        temp["reportWidgetDetails"] = {};
        temp["content"] = {};

        temp["reportWidgetName"] = $(this).find("h3.panel-title").first().html();
        temp["reportWidgetDetails"]["id"] = $(this).attr("id");
        temp["reportWidgetDetails"]["x"] = parseInt($(this).attr("gs-x"));
        temp["reportWidgetDetails"]["y"] = parseInt($(this).attr("gs-y"));
        temp["reportWidgetDetails"]["height"] = parseInt($(this).attr("gs-h"));
        temp["reportWidgetDetails"]["width"] = parseInt($(this).attr("gs-w"));

        var content = $(this).find("div.panel-body div:first-child :first-child");
        var contentType = $(content).data("type");
        var contentId;
        if (contentType === "pvDashReports" || contentType == "pvDashPreChecks") {
            contentId = $(content).attr("data-id");
        } else {
            contentId = $(content).attr("id");
        }
        temp["content"]["id"] = contentId;
        temp["content"]["type"] = contentType;
        dashboardWidgetsConfig_local[temp["reportWidgetDetails"]["id"]] = temp;
    });
    dashboardWidgetsConfig_local['isDirty'] = true;
    signal.utils.localStorageUtil.setJSON("dashboardWidgetsConfig", dashboardWidgetsConfig_local);
}

function updateDashboardWidgetConfigForInbox() {
    var dashboardWidgetsConfig_local = signal.utils.localStorageUtil.getJSON("dashboardWidgetsConfig");
    dashboardWidgetsConfig_local["inbox"].visible = $("#inbox").is(":visible");
    signal.utils.localStorageUtil.setJSON("dashboardWidgetsConfig", dashboardWidgetsConfig_local);
}

function resizeWidgetContents(elem) {
    var widget = $(elem); //$(elem.element[0]);
    var height = $(widget).find("div.panel-body").height();
    var width = $(widget).find("div.panel-body").width();
    switch ($(widget).data("type")) {
        case 'pvDashChart':
            $(widget).find("div.chart-container").parent().height(height - (height * .08));
            $(widget).find("div.chart-container").height(height - (height * .08));
            var tempChart = $("#" + $(widget).find("div.chart-container").attr("id")).highcharts();
            if (tempChart) {
                tempChart.setSize(width, height - (height * .1), setAnimation = true);
            }
            break;
        case 'pvDashCalendar':
            $(widget).find("div.calendar").width(width - (width * .075));
            $(widget).find("div.fc-scroller.fc-day-grid-container").height(height - (height * .4));
            break;
        case 'pvDashReports':
            $(widget).find("table").width('100%');
            break;
        case 'pvDashPreChecks':
            $(widget).find("table").width('100%');
            break;

    }
}

function resizeWidgetContentsAfterInitialization(elem) {
    var widget = $(elem);
    var height = $(widget).find("div.panel-body").height();
    var width = $(widget).find("div.panel-body").width();
    switch ($(widget).data("type")) {
        case 'pvDashChart':
            $(widget).find("div.chart-container").parent().height(height - (height * .08));
            $(widget).find("div.chart-container").height(height - (height * .08));
            var tempChart = $("#" + $(widget).find("div.chart-container").attr("id")).highcharts();
            if (tempChart) {
                tempChart.setSize(width, height - (height * .1), setAnimation = true);
            }
            break;
        case 'pvDashCalendar':
            $(widget).find("div.calendar").width(width - (width * .075));
            $(widget).find("div.fc-scroller.fc-day-grid-container").height(height - (height * .2));

            break;
        case 'pvDashReports':
            $(widget).find("table").width('100%');
            break;

        case 'pvDashPreChecks':
            $(widget).find("table").width('100%');
            break;
    }
}


function registerHandleBarHelper() {

    Handlebars.registerHelper('splitString', function(str) {
        return str.split("_")[0];
    });

    Handlebars.registerHelper('splitStringLast', function(str) {
        return str.split("_")[1];
    });
    Handlebars.registerHelper('ifCond', function(v1, v2, options) {
        v1=v1.split("_")[1]
        if(v1=== v2) {
            return options.fn(this);
        }
        return options.inverse(this);
    });
    Handlebars.registerHelper('ifCond1', function(v1, v2, options) {
        v1=v1.split("_")[2]
        if(v1=== v2) {
            return options.fn(this);
        }
        return options.inverse(this);
    });
    Handlebars.registerHelper('ifeq', function (a, b, options) {
        if (a%2 == 0) { return options.fn(this); }
        return options.inverse(this);
    });

    Handlebars.registerHelper('ifnoteq', function (a, b, options) {
        if (a%2 != 0) { return options.fn(this); }
        return options.inverse(this);
    });

    Handlebars.registerHelper('ifCondEq', function(v1, v2, options) {
        if(v1=== v2) {
            return options.fn(this);
        }
        return options.inverse(this);
    });

}
function preCheckList(){
    var preCheckList=[]
    $.ajax({
        type: "POST",
        url: preCheckListUrl,
        data : {refresh:false},
        async: false,
        dataType: 'json',
        beforeSend: function () {
            document.body.style.cursor = 'wait';
            $("#systemValidationPreChecks").css({
                "pointer-events": "none",
                "cursor": "not-allowed"
            });
        }
    }).done(function (result) {
        preCheckList=result.data
    }).always(function () {
        document.body.style.cursor = 'default';
        $("#systemValidationPreChecks").css("pointer-events", "");
        $("#systemValidationPreChecks").css("cursor", "");
    });
    return preCheckList
}
function addWidget(obj, autoPosition) {
    if(obj.reportWidgetDetails.id=="pvWidgetChart-13" && !JSON.parse(showWidget)){
        return false
    }
    $(".newRow").hide();
    var gridstack = document.querySelector('.grid-stack').gridstack;


    registerHandleBarHelper();
    var widgetContent = signal.utils.render('dashboard_widgets_2024_feb_1.2', {
        reportWidgetId: obj.reportWidgetDetails.id,
        reportWidgetName: obj.reportWidgetName,
        refreshIconVisible : obj.content.id != "assignedTriggeredAlerts" && obj.content.id !="assignedSignalTable" && obj.content.id != "listAction" && obj.content.type != "pvDashbox",
        content: obj.content,
        referenceTypes:referenceTypes,
        createdBy:createdBy,
        preCheckList: obj.reportWidgetDetails.id=="pvWidgetChart-13" ? preCheckList():[]
    });


    if (autoPosition && typeof gridstack !== "undefined") {
        gridstack.addWidget(widgetContent, {x: null, y: null, w: obj.reportWidgetDetails.width, h: obj.reportWidgetDetails.height, autoPosition: autoPosition});
        $('.close-widget').off("click").on('click', closeWidget);
        $('.refresh-widget').off("click").on('click', refreshWidget);
        init_triggered_alert_table();
        loadTableOption('#assignedTriggeredAlerts');
        init_assigned_signal_table();
        loadReferenceData();
        $(".clsBtnEvent").on('click',clsBtnEvent)
        $('.add-refrences').on('click', addReference);
        $(".newRow").hide();
        init_assigned_topic_table();
        init_action_list_table();

    } else {
        if (typeof gridstack !== "undefined") {
            gridstack.addWidget(widgetContent, {x: obj.reportWidgetDetails.x, y: obj.reportWidgetDetails.y, w: obj.reportWidgetDetails.width, h: obj.reportWidgetDetails.height, autoPosition: autoPosition});
        }
    }

    //We need to make sure that the widget content is refreshed and charts are rendered. This we need to trigger the click event on the widgets.
    //Works fine with the charts widgets.
    var widget = $("#" + obj.reportWidgetDetails.id);
    if ($(widget).closest(".pv-dashWidget").data("type") != "pvDashReports" && $(widget).closest(".pv-dashWidget").data("type") != "pvDashPreChecks") {
        $(widget).find('.refresh-widget').trigger('click');
    }

    if (obj.reportWidgetDetails.id == "pvWidgetChart-7") {
        setFilterDropdown("signalDashboard", $('#signalsFilter'));
        $('#signalsFilter').on('change', function() {
            sessionStorage.setItem("signalDashboard", $('#signalsFilter').val());
            $("#assignedSignalTable").DataTable().ajax.reload();
        });
        var _searchTimer = null
        $('#custom-search-signal-wd').on("keyup", function (){
            clearInterval(_searchTimer)
            _searchTimer = setInterval(function (){
                triggeredSignalTable.search($('#custom-search-signal-wd').val()).draw() ;
                clearInterval(_searchTimer)
            },1500)

        });
    }

    if (obj.reportWidgetDetails.id == "pvWidgetChart-5") {
        setFilterDropdown("alertDashboard", $('#alertsFilterDashboard'));
        $('#alertsFilterDashboard').on('change', function() {
            sessionStorage.setItem("alertDashboard", $('#alertsFilterDashboard').val());
            $("#assignedTriggeredAlerts").DataTable().ajax.reload();
        });
        var _searchTimer = null
        $('#custom-search-alert-wd').on("keyup", function (){
            clearInterval(_searchTimer)
            _searchTimer = setInterval(function (){
                triggeredAlertTable.search($('#custom-search-alert-wd').val()).draw() ;
                clearInterval(_searchTimer)
            },1500)

        });
    }

    if(obj.reportWidgetDetails.id == "pvWidgetChart-11") {
        $('#custom-search-action-item-wd').on("keyup", function (){
            triggeredActionsTable.search($(this).val()).draw();
        })
    }
    if(obj.reportWidgetDetails.id == "pvWidgetChart-12") {
        $('#custom-search-links-wd').on("keyup", function (){
            if( $(this).val().length >= 1 || !$(this).val().length){
                var sortData = {
                    "qs": $(this).val()
                }
                loadLinks(sortData);
            }
        })
    }

    if (obj.reportWidgetDetails.id == "pvWidgetChart-13") {
        setInterval(function () {
            refreshWidget(false);
            $('.close-widget').off("click").on('click', closeWidget);
            $('.refresh-widget').off("click").on('click', refreshWidget);
        }, 15000);
    }
}
var loadLinks = function (data) {
    var fdata;
    var sdata = data || null;
    if(sdata){
        if(!sdata.hasOwnProperty('qs')){
            fdata= {
                "sort":sdata.stype,
                "direction": sdata.dir,
                "length": sdata.len || 100
            };
            if(sdata.hasOwnProperty('scrolling')&& sdata.scrolling){
                fdata= {
                    "sort":sdata.stype,
                    "direction": sdata.dir,
                    "length": sdata.len
                };
            }
        } else {
            fdata= {
                "searchString": sdata.qs,
                "length": 100
            }
        }
    } else{
        var isSorted= $(".link-box-filter button").hasClass("active");
        fdata = {
            "length": reqLen || 50
        };
        if(isSorted){
            fdata.sort = $(".link-box-filter button.active").data("type");
            fdata.direction = $(".link-box-filter button.active").hasClass("dt-ordering-desc")?"desc":"asc";
        }
    }


    $.ajax({
        url: apiurlGetLinks,
        type: 'GET',
        data:fdata,
        dataType: 'json',
        contentType: 'application/json; charset=utf-8',
        beforeSend: function () {
            showWidgetLoader();
        },
    }).done(function (data) {
        if (!$(window).data('ajax_in_progress') === true){
            totalLinkCount =  data.recordsTotal;
        }
        drawLinkBox(data.aaData);
        hideWidgetLoader();
        $(window).data('ajax_in_progress', false);
    }).fail(function (data) {
        $.Notification.notify('error', 'top right', "Error", " Something went wrong! ", {autoHideDelay: 5000});
    });
}
var drawLinkBox = function (data) {
    var html = '';
    $.each(data, function(key, value){
        var favIc;
        if(value.fileType){
            favIc = iconClass(value.fileType.toLocaleLowerCase());
        } else {
            favIc = value.favIconUrl?'<img src="'+value.favIconUrl+'"/>':'<i class="md md-link md-3x"></i>';
        }
        var fileLink = value.fileType?"dashboard/download?id="+value.id:value.link;
        var pinText = value.isPinned? "Unpin":"Pin";
        var classList= value.isPinned? "box-widget listitemClass pinned-box":"box-widget listitemClass";
        var targetLink = value.fileType ? "_self" : "_blank";
        html +=  '<div class= "'+classList+'" id="'+value.id+'" data-id="'+value.refId+'">';
        html +=     '<div class="ic-circle">'+favIc+'</div>';
        html +=     '<span class="dropdown icon-dd">';
        html +=         '<i class="mdi mdi-dots-vertical md-lg dropdown-toggle" type="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">';
        html +=         '</i>';
        html +=         '<ul class="dropdown-menu dropdown-menu-right pin-menu-right">';
        html +=             '<li><a class="item" href="javascript:void(0)"></li>';
        html +=             '<li><a class="dropdown-item pin-box" href="javascript:void(0)">'+pinText+'</a></li>';
        if(value.addedBySelf){
            html +=             '<li><a class="dropdown-item edit-link-box" data-type="edit" data-name="'+value.name+'" data-reftype="'+value.referenceType+'" data-inpname="'+value.inputName+'" data-refid="'+value.refId+'" data-link="'+value.link+'" href="javascript:void(0)">Edit</a></li>';
        };
        if(value.isShareEnabled) {
            html += '<li><a class="dropdown-item openShareWithModal" data-toggle="modal" data-refid="' + value.refId + '" sharedWithGroup="' + value.sharedWithGroup + '"   sharedWithUser="' + value.sharedWithUser + '" data-target="#sharedWithModal">Share</a></li>';
        };
        html +=             '<li><a class="dropdown-item ic-del deleteRecord"  isOwner="'+value.addedBySelf+'" data-refId="'+value.refId+'" data-toggle="modal" data-target="#deleteReferenceModal">Remove</a></li>';

        html +=         '</ul>';
        html +=     '</span>';
        html +=     '<a href="'+fileLink+'" class="lnk" target='+targetLink+'>'+encodeToHTML(value.inputName)+'</a>';
        html +=     '<div class="tooltip--multiline"> <p class="norap">'+encodeToHTML(value.inputName)+'</p><p class="norap"><strong>Modified By:</strong> '+value.modifiedBy+' </p>   <p><strong>Modified Date:</strong> '+value.timeStamp+'</p> </div>'
        html +=  '</div>';
    });

    $('#imageListId').html(html);
    initdraggableBox();
    manageDataOnScroll();
}
var initdraggableBox = function(){
    $( "#imageListId" ).sortable({
        connectWith: "#imageListId",
        containment : "parent",
        scrollSpeed: 40,
        update: function(event, ui) {
            var isPinnedBox = ui.item.hasClass('pinned-box');
            var nextElement = ui.item.next();
            if(isPinnedBox){
                if(nextElement.hasClass('pinned-box')){
                    getIdsLinks(true);
                } else {
                    pinUnpinBox(ui.item);
                    getIdsLinks(false);
                }

            } else if(!isPinnedBox){
                if(nextElement.hasClass('pinned-box')){
                    pinUnpinBox(ui.item);
                    getIdsLinks(true);
                } else {
                    getIdsLinks(false);
                }
            }

        }//end update
    });
}

var manageDataOnScroll = function () {
    $("#imageListId").on('scroll',function() {
        var scrollableHeight = Math.round(document.getElementById("imageListId").scrollHeight);
        var scrolled = Math.round($(this).scrollTop());
        var isScrollable = reqLen? (totalLinkCount-reqLen > 0): totalLinkCount > 50;
        if ($(window).data('ajax_in_progress') === true)
            return;

        var isSorted= $(".link-box-filter button").hasClass("active");
        if(isScrollable && scrolled > lastScrollTop && scrollableHeight-scrolled < 300  ) {
            pageNum++;
            reqLen = reqLen?reqLen+50:75;
            if(isSorted){
                var sortdata= {
                    "stype":$(".link-box-filter button.active").data("type"),
                    "dir":$(".link-box-filter button.active").hasClass("dt-ordering-desc")?"desc":"asc",
                    "len": reqLen,
                    "scrolling": true
                };
            } else{
                var sortdata= {
                    "len": reqLen,
                    "scrolling": true
                }
            }


            $(window).data('ajax_in_progress', true);
            setTimeout(function(){loadLinks(sortdata)}, 1500);

        };
        lastScrollTop = scrolled;
    });
};

var showWidgetLoader = function () {
    $(".pv-loader-bg").removeClass('hide');
}

var hideWidgetLoader = function () {
    $(".pv-loader-bg").addClass('hide');
}

var populateDashboardWidgets = function () {
    var dashboardWidgetsConfig_local = signal.utils.localStorageUtil.getJSON("dashboardWidgetsConfig");
    var grid = document.querySelector('.grid-stack').gridstack;
    if (typeof grid !== "undefined") {
        grid.batchUpdate();
    }
    $.each(dashboardWidgetsConfig_local, function (a, b) {
        if (b.reportWidgetName === 'Inbox') {
            if(b.visible) {
                $('#inbox').show();
            } else {
                // $('#inbox').hide();
                $(".pv-dash-inbox-icon").removeClass("hide");
            }
        } else if (b.visible) {
            addWidget(b, false);
        }
    });
    if (typeof grid !== "undefined") {
        grid.commit();
    }
    triggerResizeForAll();

};

var triggerResizeForAll = function () {
    var dashboardWidgetsConfig_local = signal.utils.localStorageUtil.getJSON("dashboardWidgetsConfig");
    $.each(dashboardWidgetsConfig_local, function (a, b) {
        if (b.visible) {
            resizeWidgetContentsAfterInitialization($("#" + b.reportWidgetDetails.id));
        }
    });
};

var populateClosedWidgetsMenu = function () {
    var dashboardWidgetsConfig_local = signal.utils.localStorageUtil.getJSON("dashboardWidgetsConfig");
    for (var config in dashboardWidgetsConfig_local) {
        if (dashboardWidgetsConfig_local[config]['visible'] === false && dashboardWidgetsConfig_local[config]['reportWidgetName'] !== 'Inbox') {
            var pvWidgetId = dashboardWidgetsConfig_local[config].reportWidgetDetails.id;
            var pvTargetedWidget = dashboardWidgetsConfig_local[config].content.type;
            var widgetText = dashboardWidgetsConfig_local[config].reportWidgetName;
            addWidgetOptionInSideMenu(pvTargetedWidget, pvWidgetId, widgetText);
        }
    }
};

var greyoutRefreshIcon=function (flag) {
    if (flag) {
        $(".refresh-systemValidationPreChecks-link").css({
            "pointer-events": "not-allowed",
            "cursor": "not-allowed"
        });
        $(".refresh-systemValidationPreChecks-link").removeAttr('href');
        $(".refresh-systemValidationPreChecks-link").off('click');

        $("#systemValidationPreChecks").find(".fa-window-close, .fa-warning, .fa-check").hide();
        $("#systemValidationPreChecks").find(".precheck-loader").removeClass("hidden");
    } else {
        $(".refresh-systemValidationPreChecks-link").css("pointer-events", "pointer");
        $(".refresh-systemValidationPreChecks-link").css("cursor", "pointer");
        $(".refresh-systemValidationPreChecks-link").on('click');
        $("#systemValidationPreChecks").css({
            "pointer-events": "pointer"
        });
        $("#systemValidationPreChecks").find(".precheck-loader").addClass("hidden");
        $("#systemValidationPreChecks").find(".fa-window-close, .fa-warning, .fa-check").show();
        $("#systemValidationPreChecks, .refresh-systemValidationPreChecks-link").on('click');
        $('.close-widget').on('click', closeWidget);
        $('.refresh-widget').on('click', refreshWidget);
    }
}

var refreshWidget = function (refresh) {
    if (refresh != false) {
        refresh = true
    }
    var widgetType = $(this).closest(".pv-dashWidget").data("type");
    var widgetId = $(this).closest(".pv-dashWidget").attr("id");
    var widgetData = signal.utils.localStorageUtil.getJSON("dashboardWidgetsConfig")[widgetId];
    var containerId, reportWidgetDetails, reportWidgetName
    if (typeof widgetData === "undefined" || typeof widgetData === undefined) {
        widgetData = JSON.parse(dashboardWidgetConfigJSON)['pvWidgetChart-13']
        containerId = "#" + JSON.parse(dashboardWidgetConfigJSON)['pvWidgetChart-13'].content.id;
        reportWidgetDetails = JSON.parse(dashboardWidgetConfigJSON)['pvWidgetChart-13'].reportWidgetDetails;
        reportWidgetName = JSON.parse(dashboardWidgetConfigJSON)['pvWidgetChart-13'].reportWidgetName;
        widgetType = "pvDashPreChecks";
        widgetId = "pvWidgetChart-13";
    } else {
        containerId = "#" + widgetData.content.id;
        reportWidgetDetails = widgetData.reportWidgetDetails;
        reportWidgetName = widgetData.reportWidgetName;
    }

    switch (widgetType) {
        case "pvDashChart":
            refreshPvDashChart(widgetId, containerId);
            break;
        case "pvDashReports":
            $(containerId).DataTable().ajax.reload();
            break;
        case "pvDashPreChecks":
            var dashboardWidgetsConfig_local = signal.utils.localStorageUtil.getJSON("dashboardWidgetsConfig");
            if (typeof dashboardWidgetsConfig_local === "undefined" || typeof dashboardWidgetsConfig_local === undefined) {
                dashboardWidgetsConfig_local = JSON.parse(dashboardWidgetConfigJSON)['pvWidgetChart-13'];
            }
            var widget = $(this).closest(".grid-stack-item");
            registerHandleBarHelper();
            dashboardWidgetsConfig_local[widgetId]['visible'] = true;
            if (widgetId === "pvWidgetChart-13") {
                dashboardWidgetsConfig_local[widgetId].content.id = "systemValidationPreChecks"
                dashboardWidgetsConfig_local[widgetId].content.type = "pvDashPreChecks"
            }
            signal.utils.localStorageUtil.setJSON("dashboardWidgetsConfig", dashboardWidgetsConfig_local);
            $(".newRow").hide();
            var gridstack = document.querySelector('.grid-stack').gridstack;
            $.ajax({
                type: "POST",
                url: preCheckListUrl,
                data : {refresh:JSON.parse(refresh)},
                async:true,
                cache: false,
                dataType: 'json',
                beforeSend: function () {
                    if (JSON.parse(refresh)) {
                        greyoutRefreshIcon(true);
                    }
                },
            }).done(function (result) {
                if (result.isDataSame === false) {
                    var widgetContent = signal.utils.render('dashboard_widgets_2024_feb_1.2', {
                        reportWidgetId: widgetId,
                        reportWidgetName: reportWidgetName,
                        content: {id: 'systemValidationPreChecks', type: 'pvDashPreChecks'},
                        refreshIconVisible : true,
                        referenceTypes: referenceTypes,
                        createdBy: createdBy,
                        preCheckList: result.data
                    });
                    var widget = $("#" + widgetId);
                    gridstack.removeWidget(widget);
                    gridstack.addWidget(widgetContent, {x: reportWidgetDetails.x, y: reportWidgetDetails.y, w: reportWidgetDetails.width, h: reportWidgetDetails.height, autoPosition: false});
                    if ($(widget).closest(".pv-dashWidget").data("type") != "pvDashReports" && $(widget).closest(".pv-dashWidget").data("type") != "pvDashPreChecks") {
                        $(widget).find('.refresh-widget').trigger('click');
                    }
                    $('.close-widget').off("click").on('click', closeWidget);
                    $('.refresh-widget').off("click").on('click', refreshWidget);
                }
            }).always(function () {
                if (JSON.parse(refresh)) {
                    greyoutRefreshIcon(false);
                }
            });

            break;
        case "pvDashCalendar":
            $(containerId).fullCalendar('destroy');
            init_calendar(containerId);
            break;
        case "pvDashbox":
            signal.managelinkwidget.init();
    }
};

function triggerChangesOnModalOpening(extTextAreaModal) {
    extTextAreaModal.on('shown.bs.modal', function () {
        $('textarea').trigger('keyup');
        if (extTextAreaModal.find('.textAreaValue').val()) {
            extTextAreaModal.find(".updateTextarea").html($.i18n._('labelUpdate'));
        } else {
            extTextAreaModal.find(".updateTextarea").html($.i18n._('labelAdd'));
        }
    });
}
var commentDescription= function (evt) {
    evt.preventDefault();
    var extTextAreaModal = $("#textarea-ext1");
    addCountBoxToInputField(8000, extTextAreaModal.find('textarea'));
    triggerChangesOnModalOpening(extTextAreaModal);
    var commentBox = $(this).prev('.comment');
    extTextAreaModal.find('.textAreaValue').val(commentBox.val());
    extTextAreaModal.find('.modal-title').text($.i18n._('statusHistoryComment'));
    if (commentBox.prop('disabled')) {
        extTextAreaModal.find('.textAreaValue').prop('disabled', true);
        extTextAreaModal.find('.updateTextarea').prop('disabled', true);
    } else {
        extTextAreaModal.find('.textAreaValue').prop('disabled', false);
        extTextAreaModal.find('.updateTextarea').prop('disabled', false);
    }
    extTextAreaModal.modal("show");
    updateTextAreaData(commentBox);
};
function updateTextAreaData(container) {
    var extTextAreaModal = $("#textarea-ext1");
    $('#textarea-ext1 .updateTextarea').off("click").on('click', function (evt) {
        evt.preventDefault();
        container.val(extTextAreaModal.find('textarea').val());
        extTextAreaModal.modal("hide");
    });
}

var addReference = function () {
    if(!referenceTable.data().count()){
        $('#refrences td.dt-empty').hide();
        $('#refrences_wrapper .dt-scroll-body').hide();
    }
    $('.newRow').show();
    $('.refrenceType').select2({
        tags : true,
    });


    $(".currentDate").text(moment.utc(new Date()).tz(userTimeZone).format("DD-MMM-YYYY hh:mm:ss A"));
    $(".newRow").find(".refrenceType").empty()
    var referenceTypesArray=[]
    if(referenceTypes.length>1)
        var str=referenceTypes.substring(1,referenceTypes.length-1)
    referenceTypesArray= str.split(",")
    if(referenceTypesArray.length>0){
        var options='<option value="">Select</option>'
        for(var i=0;i<referenceTypesArray.length;i++){
            options=options+'<option value='+referenceTypesArray[i]+'>'+referenceTypesArray[i]+'</option>';

        }
        $(".newRow").find(".refrenceType").append(options)
    }else{
        $(".newRow").find(".refrenceType").append(' <option value="">Select</option><option value="Reference">Reference</option> <option value="Others">Others</option>')
    }
    $(".newRow").find(".description").val("")
    $(".newRow").find("#attachmentFilePathRmm").val("")
    $(".reference-table-foot").removeClass('hide');
};
var refreshPvDashChart = function refreshPvDashChart(widgetId, chartId) {
    switch (chartId) {
        case "#qualitative-products-status":
            build_product_by_status(chartId, "Single Case Alert");
            break;
        case "#quantitative-products-status":
            build_product_by_status(chartId, "Aggregate Case Alert");
            break;
        case "#case-status-chart":
            build_sca_by_status(chartId, "case-status-chart");
            break;
        case "#aggregate-chart":
            build_agg_by_status(chartId, "aggregate-chart");
            break;
        case "#adhoc-chart":
            build_aha_by_status(chartId, 'adhoc-chart');
            break;
        case "#due-date-chart":
            build_alert_by_duedate(chartId);
            break;

    }
};

var closeWidget = function () {
    var panel = $(this).closest(".panel");
    var pvWidget = panel.parent();
    var pvWidgetId = pvWidget.attr('id');
    var pvTargetedWidget = pvWidget.data('type');
    var widgetText = panel.find("h3.panel-title").text();

    var grid = document.querySelector('.grid-stack').gridstack;
    const id = $(pvWidget).attr("id");
    const widgetToRemove = grid
        .getGridItems()
        .find((widgetEl) => $(widgetEl).attr("id") === id);
    grid.removeWidget(widgetToRemove);
    var dashboardWidgetsConfig_local = signal.utils.localStorageUtil.getJSON("dashboardWidgetsConfig");
    dashboardWidgetsConfig_local[pvWidgetId]['visible'] = false;
    signal.utils.localStorageUtil.setJSON("dashboardWidgetsConfig", dashboardWidgetsConfig_local);
    addWidgetOptionInSideMenu(pvTargetedWidget, pvWidgetId, widgetText);
};

function isWidgetAvailable(panel) {
    var widgetListItem = $(panel).find(".pv-widget-list-item");
    if (widgetListItem.length < 1) {
        $(panel).addClass("hide");
    } else {
        $(panel).removeClass("hide");
    }
}

function isWidgetSelectorMenuAvailable() {
    var elem = $("#pv-dash-topMinWidget a");
    if ($(".pv-widget-list-item").length === 0) {
        $(elem).trigger('click');
        $(elem).hide();
    } else {
        $(elem).show();
    }
}

var addWidgetOptionInSideMenu = function (pvTargetedWidget, pvWidgetId, widgetText) {
    if(pvWidgetId=="pvWidgetChart-13" && !JSON.parse(showWidget)){
        return false
    }
    $('#' + pvTargetedWidget + ' .list-group').append('<li class="list-group-item pv-widget-list-item" data-target-widget="' + pvWidgetId + '">' + widgetText + '</li>');
    isWidgetAvailable('div#' + pvTargetedWidget + '.panel');
    isWidgetSelectorMenuAvailable();

};
var referenceTable;
$(function () {

    if(typeof hasReviewerAccess !=="undefined" && !hasReviewerAccess){
        $(".changePriority").removeAttr("data-target");
        $(".changeDisposition").removeAttr("data-target");
    }
    if(typeof hasSignalCreationAccessAccess !== "undefined" && !hasSignalCreationAccessAccess) {
        $(".changeDisposition[data-validated-confirmed=true]").removeAttr("data-target");
    }

    // Browse file upload Start
    $(document).on('click', '#refAttachmentFileModal .file-uploader .browse,#emailGenerationModal .file-uploader .browse', function () {
        var fileUploaderElement = $(this).closest('.file-uploader');
        var file = fileUploaderElement.find('.file');
        var fileName = fileUploaderElement.find('.form-control').val();
        file.trigger('click');
    });

    $(document).on('click', '.editRecord', function (e) {

        e.preventDefault();
        var currRow = $(this).closest('tr');
        currRow.removeClass('readonly');
        currRow.find('.deleteRecord').addClass('hide');
        currRow.find('.form-control').removeAttr('disabled');
        currRow.find('.select2').next(".select2-container").show();
        currRow.find('.table-row-edit, .openShareWithModal, .table-row-mail').addClass('hide');
        currRow.find('.table-row-saved').removeClass('hide').addClass('hidden-ic');
        currRow.find('.table-row-cancel').removeClass('hide').addClass('hidden-ic');
        currRow.find('.file-uploader .word-wrap-break-word').addClass('hide');
        currRow.find('.file-uploader .input-group').removeClass('hide');
        currRow.find('.file-uploader .form-control').prop('disabled', true).prop('placeholder', 'Attach a File');
        currRow.find('.refDescriptionInput,.comment-on-edit').removeClass('hide');
        currRow.find('.rmmDescriptionText,.rmmAttachmentText').toggle();
        var currentTableId = currRow.attr('data-id');
        var isRefTable = currentTableId === 'refrences';
        var table =  referenceTable;
        $(".comment-on-edit").on('click',editComment);
        var rmmobj=currRow.find(".allow-rmm-edit")
        var jdata=$(rmmobj).attr('data')
        if(jdata!=undefined){
            var parsedData=JSON.parse(jdata);
            if (parsedData.type =="Attachment"|| parsedData.type === ATTACHMENT_TYPE_ENUM.ATTACHMENT) {
                $("#literature-file-path").val(parsedData.name)
                $("#attachment-name").val(parsedData.inputName)
                $("#file-type-attach").prop('checked', true);
                $("#literature-file-path").attr('disabled','disabled')
            }else{
                $("#literature-file-path").val(parsedData.link)
                $("#file-type-link").prop('checked', true);
                $("#attachment-name").val(parsedData.inputName)
                $("#literature-file-path").removeAttr('disabled')
            }
        }

    });
    $('#attachmentFilePathRmm,#attachmentFilePathComm').prop('disabled', true);
    var data=undefined;
    $(document).on('click', '.allow-rmm-edit', function () {

        data=$(this).attr('data')
        if(data!=undefined){
            var parsedData=JSON.parse(data);
            if (parsedData.type =="Attachment"|| parsedData.type === ATTACHMENT_TYPE_ENUM.ATTACHMENT) {
                $("#literature-file-path").val(parsedData.name)
                $("#attachment-name").val(parsedData.inputName)
                $("#file-type-attach").prop('checked', true);
                $("#literature-file-path").attr('disabled','disabled')
            }else{
                $("#literature-file-path").val(parsedData.link)
                $("#file-type-link").prop('checked', true);
                $("#attachment-name").val(parsedData.inputName)
                $("#literature-file-path").removeAttr('disabled')
            }
        }else{
            var attachmentFilePathRmmVal=$(".newRow").find('#attachmentFilePathRmm').val();
            if(attachmentFilePathRmmVal==""){
                $("#attachment-name").val("")
                $("#literature-file-path").val("")
                $("#file-type-attach").prop('checked', true);
                $("#literature-file-path").attr('disabled','disabled')
                $("#literature-file-path").attr('placeholder','Attach a file');
                $(".btn-file-upload").removeAttr('disabled')
            }
        }
        if($("#file-type-attach").is(':checked')==true){
            $("#attachment-type-name").empty()
            $("#attachment-type-name").html("File Name")
        }else{
            $("#attachment-type-name").empty()
            $("#attachment-type-name").html("References")
        }
        currentRow = $(this).closest('tr');
    });

    $(document).on('click','#cancel-file',function(){
        if(data!=undefined){
            var parsedData=JSON.parse(data);
            if (parsedData.type =="Attachment"|| parsedData.type === ATTACHMENT_TYPE_ENUM.ATTACHMENT) {
                $("#literature-file-path").val(parsedData.name)
                $("#file-type-attach").prop('checked', 'checked');
                $("#literature-file-path").attr('disabled','disabled')
            }else{
                $("#literature-file-path").val(parsedData.link)
                $("#file-type-link").attr('checked', 'checked');
                $("#literature-file-path").removeAttr('disabled')
            }
        }
        data=undefined
    });
    $(document).on('click', '#add-file', function () {
        var length = $("#attachment-name").val().length;
        var type=$("input[type='radio'][name='file-type']:checked").val()=='file'?'File Name':'References';
        if(length>255){
            $.Notification.notify('error', 'top right', "Error", type+" length should not be more than 255 ", {autoHideDelay: 5000});
            return false;
        }
        if ($('#attachment-name').val()) {
            currentRow.find('#attachmentFilePathRmm,#attachmentFilePathComm').val($('#attachment-name').val());
        } else {
            $("#refAttachmentFileModal .file-uploader .file")[0].files[0]
            type= $("input[type='radio'][name='file-type']:checked").val()
            if(type=='file'){
                var prevFile=$("#literature-file-path").val()
                if($("#refAttachmentFileModal .file-uploader .file")[0].files[0]==undefined){
                    return;
                }
                var newFile=$("#refAttachmentFileModal .file-uploader .file")[0].files[0].name;
                newFile=(newFile.length>0)?newFile:prevFile
                $("#literature-file-path").val(newFile)
                currentRow.find('#attachmentFilePathRmm,#attachmentFilePathComm').val(newFile);
            }else{
                currentRow.find('#attachmentFilePathRmm,#attachmentFilePathComm').val($('#literature-file-path').val());
            }

        }
    });
    var type = $('input[name="file-type"]:checked').val();
    if (type === 'file') {
        $("#literature-file-path").prop('disabled', true);
        $("#refAttachmentFileModal .file-uploader .browse").prop('disabled', false);
    } else if (type === 'link') {
        $("#refAttachmentFileModal .file-uploader .browse").prop('disabled', true);
        $("#literature-file-path").prop('disabled', false);
    }

    $(document).on('change', "#refAttachmentFileModal .file-uploader input[type='file']",  function () {
        var currentElement = $(this);
        $("#literature-file-path").val(currentElement.prop('files')[0].name);
        if(!$("#attachment-name").val()) {
            $("#attachment-name").val(currentElement.prop('files')[0].name);
        }
    });
    $('#refAttachmentFileModal input[type=radio][name=file-type]').on("change", function () {
        type = $('input[name="file-type"]:checked').val();
        if (type === 'link') {
            $("#refAttachmentFileModal .file-uploader .browse").prop('disabled', true);
            $("#literature-file-path").prop('disabled', false);
            $('#literature-file-path').attr('placeholder', 'Add reference link');
            $("#attachment-type-name").text('References')
        } else if (type === 'file') {
            $("#refAttachmentFileModal .file-uploader .browse").prop('disabled', false);
            $("#literature-file-path").prop('disabled', true);
            $('#literature-file-path').attr('placeholder', 'Attach a file');
            $(".file-uploader").show();
            $("#attachment-type-name").text('File Name')
        }
    });
    $(document).on('click', '.saveRecord', function (e) {
        var thisId=$(this).attr('data-refid');
        e.preventDefault();
        var currRow = $(this).closest('tr');
        var obj= currRow.find('.allow-rmm-edit')
        var dataJson=$(obj).attr('data')
        if(dataJson!=undefined){
            var parsedData=JSON.parse(dataJson);
            if (parsedData.type =="Attachment"|| parsedData.type === ATTACHMENT_TYPE_ENUM.ATTACHMENT) {
                $("#literature-file-path").val(parsedData.name)
                $("#attachment-name").val(parsedData.inputName)
                $("#file-type-attach").prop('checked', true);
                $("#literature-file-path").attr('disabled','disabled')
            }else{
                $("#literature-file-path").val(parsedData.link)
                $("#file-type-link").prop('checked', true);
                $("#attachment-name").val(parsedData.inputName)
                $("#literature-file-path").removeAttr('disabled')
            }
        }

        var currentTableId = currRow.attr('data-id');
        var isRefTable = currentTableId === 'refrences';
        var table =  referenceTable;
        var formdata = new FormData();
        if (!currRow.parent().is('tfoot')) {
            var currRowIndex = $(this).closest('tr').index();
            currRowIndex += table.page.info().start;
        }
        var refrenceType = (undefined==thisId)?currRow.find('.refrenceType').val():$(currRow.find('td')[0]).find('.referenceType').val();
        if(refrenceType=="" || $("#literature-file-path").val()==""){
            $.Notification.notify('warning', 'top right', "Error", "Please add all mandatory details ", {autoHideDelay: 5000});
            return false;
        }
        var description = currRow.find('.description').val();
        var $attachmentName = currRow.find("#attachmentFilePathRmm");
        if($("#file-type-attach").is(':checked')==true && $("#refAttachmentFileModal .file-uploader .file")[0].files.length ==0 && thisId==undefined){
            $.Notification.notify('warning', 'top right', "Error", "Please add all mandatory details", {autoHideDelay: 5000});
            return false;
        }
        if ($attachmentName.val() !== "") {
            formdata.append("inputName", $attachmentName.val());
            $("#refAttachmentFileModal .file-uploader").show();
            if ($("#file-type-attach").is(':checked')==true  && $("#refAttachmentFileModal .file-uploader .file")[0].files.length > 0) {
                var file = $("#refAttachmentFileModal .file-uploader .file")[0].files[0];
                formdata.append("attachments", file);
            } else {
                formdata.append("referenceLink", $("#literature-file-path").val());
            }
        }
        if($(this).attr('referenceId')!=undefined){
            formdata.append("referenceId", $(this).attr('referenceId'));
        }
        formdata.append("description", description);
        formdata.append("refrenceType", refrenceType);
        if(undefined!=thisId){
            formdata.append("refrenceId", thisId);
        }
        if ( !refrenceType ||$attachmentName.val()==""  || $attachmentName.val()==undefined || $("#literature-file-path").val()=="") {
            $.Notification.notify('warning', 'top right', "Warning", "Please add all mandatory details", {autoHideDelay: 5000});
            return;
        }
        formdata.append("fileTypeChecked",$("#file-type-attach").is(':checked'));
        formdata.append("linkTypeTypeChecked",$("#file-type-link").is(':checked'))
        $.ajax({
            url: 'dashboard/addRefrence',
            type: "POST",
            mimeType: "multipart/form-data",
            processData: false,
            contentType: false,
            data: formdata,
        }).done(function (data) {
            $response = JSON.parse(data);
            if ($response.status) {
                $.Notification.notify('success', 'top right', "Success", $.i18n._('rmmCommunicationSaveSuccess'), {autoHideDelay: 5000});
                table.ajax.reload();
                if (currRow.parent().is('tfoot')) {
                    if(isRefTable){
                        $(".newRow").find('.country').remove();
                        $(".newRow").toggle()
                    } else {
                        $(".newRowCommunication").find('.country').remove();
                        $(".newRowCommunication").toggle()
                    }
                }
            } else {
                $.Notification.notify('error', 'top right', "Error", $response.message, {autoHideDelay: 5000});
            }
            $("#attachment-name").val("")
            $("#literature-file-path").val("")

            $("#file-type-attach").prop('checked', true);
        })


    });
    $(document).on('click', '.cancelRecord', function (e) {
        var table =  referenceTable;
        table.ajax.reload();
    });

    $(document).on('click', '.deleteRecord', function (e) {
        if($(this).attr('isOwner')==true || $(this).attr('isOwner')=='true'){
            $("#nameToDelete").empty();
            $("#nameToDelete").append("Do you want to proceed with removal of this record?");
        }else{
            $("#nameToDelete").empty();
            $("#nameToDelete").append("Do you want to proceed with removal of this record?");
        }
        $('#deleteDlgErrorDivEventGrpAssessment').hide();
        var dataRefid=$(this).attr('data-refid')
        $(".okReferenceModal").attr('data-refid',dataRefid)
        e.preventDefault();
        var currRow = $(this).closest('tr');
        var currentTableId = currRow.attr('data-id');
        var isRefTable = currentTableId === 'refrences';
        var table = referenceTable;

    });
    $(".okReferenceModal").on('click',okReferenceModalFn);
    $(document).on('click', '.add-attachment', function () {
        var section = $("#attachmentSection-followUp");
        var clone = section.children().not("[data-id]").last().clone(true);
        clone.find('input.fileName').val('');
        section.append(clone);
        updateIndexInAttachmentSection(section);
    });

    $(document).on('click', '.delete-attachment', function () {
        var section = $("#attachmentSection-followUp");
        if (section.find('tr').not(".hide").length > 1) {
            var target = $(this);
            var trElement = target.closest('tr.pv-section-record');
            if (trElement.attr("data-id") && !trElement.hasClass("deleted")) {
                trElement.find(".attachmentRow").data("deleted", true);
                trElement.addClass("hide");
            }
            else {
                trElement.remove();
            }
        }
        else {
            section.find('tr').not("[data-id]").find('input').val('');
        }
        updateIndexInAttachmentSection(section);
    });


    function updateAttachments(attachments){
        _.each(attachments, function (attachment,index) {
            attachments[index].remove();
        });
    }
    var updateIndexInAttachmentSection = function (section) {
        var index = 1;
        var attachments = section.find("tr").not(".hide");
        _.each(attachments, function (attachment) {
            $(attachment).find('td.new-index').text(index);
            index = index + 1;
        });
    };
    $('.referenceType').select2({
        tags : true,
    });
    $('.newRow').hide();
    referenceTable = $("#refrences").DataTable({
        destroy: true,
        searching: true,
        responsive: false,
        scrollX: true,
        scrollY : dtscrollHeight(),
        "aaSorting": [],

        "aLengthMenu": [[10, 20, 50, -1], [10, 20, 50, "All"]],
        "pagination": true,
        "scrollCollapse": true,
        "border-bottom": "none",
        language: { search: ''},
        "ajax": {
            "url": 'dashboard/fetchAttachments',
            "dataSrc": "aaData",
        },
        "order": [[ 4, "asc" ]],
        "oLanguage": {
            "oPaginate": {
                "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
            },
            "sLengthMenu": "Show _MENU_",

            "sZeroRecords": "No data available in table", "sEmptyTable": "No data available in table"
        },
        "layout": {
            topStart: null,
            topEnd: null,
            bottomStart: ['pageLength', 'info', {
                paging: {
                    type: 'full_numbers'
                }
            }],
            bottomEnd: null,
        },
        "createdRow": function (row, data, dataIndex) {
            $(row).addClass('readonly');
            $(row).attr('data-id','signal-communication-table');
            var $dateCell = $(row).find('td:eq(5)');
            $dateCell.attr('data-order', data.dueDateSort)
        },

        "drawCallback": function () {
            colEllipsis();
            webUiPopInit();
            var rowsDataAR = $('#references').DataTable().rows().data();
        },

        "rowCallback": function (row, data, index) {
            $(row).find('.assignedToSelect').next(".select2-container").hide();

            $(row).find('.referenceType').select2({
                tags : true
            });

            if(!data.emailSent && (data.colorDueDate === 'orange' || data.colorDueDate === 'red')) {
                $(row).find('.due-date').css('color', data.colorDueDate);
            }
            if(!data.dueDate){
                $(row).find('.due-date').attr('placeholder', '');
            }
            if(!data.status){
                $(row).find('.status').val('');
            }
        },

        "bAutoWidth": false,
        "aoColumns": [

            {
                "mData": "referenceType",
                "orderDataType": "dom-select",
                "className": "col-md-2 ",
                "mRender": function (data, type, row) {
                    var $select = $("<select></select>", {
                        "value": data,
                        "class": "form-control referenceType rmmType",
                        "disabled": "true"
                    });
                    var $noSelection = $("<option></option>", {
                        "text": '',
                        "value": ''
                    });
                    $select.append($noSelection);
                    var referenceTypesArray=[]
                    var str=referenceTypes.substring(1,referenceTypes.length-1)
                    referenceTypesArray= str.split(",")
                    referenceTypesArray[referenceTypesArray.length]=data

                    for(var i=0;i<referenceTypesArray.length;i++){

                        var $option = $("<option></option>", {
                            "text": referenceTypesArray[i],
                            "value": referenceTypesArray[i]
                        });
                        if (data === referenceTypesArray[i]) {
                            $option.attr("selected", "selected")
                        }
                        $select.append($option);
                    }

                    var type = '<div class=" row"><div class="col-md-10">' + $select.prop("outerHTML") + '</div>';

                    type += "</div>";
                    return type
                }
            },
            {
                "mData": "description",
                "className": "col-md-3 cell-break",
                "mRender": function (data, type, row) {
                    if (!row.description) {
                        row.description = '';
                    }
                    var description =  "<span class='rmmDescriptionText'>" + addEllipsis(row.description)+ "</span>";

                    description +="<div class='textarea-ext refDescriptionInput hide'><input type='text' value='" + row.description + "' title='" + row.description + "' class='form-control description comment' maxlength='8000' style='width: 100%' disabled> \
                                     <a class='btn-text-ext openStatusComment  comment-on-edit hide' href='' tabindex='0' title='Open in extended form'> \
                                               <i class='mdi mdi-arrow-expand font-20 blue-1'></i></a></div>"
                    return description

                }
            },
            {
                "mData": "link",
                "className": "col-md-3 cell-break",
                "mRender": function (data, type, row) {
                    var attachments,attachmentName;
                    if (row.type =="Attachment"|| row.type === ATTACHMENT_TYPE_ENUM.ATTACHMENT) {
                        attachments = "<div class='file-uploader' data-provides='fileupload'>\
                                                <a class='word-wrap-break-word' href='/signal/dashboard/download?id=" + row.id + "'>" + row.inputName + "</a>\
                                                <span class='newAttachment hide' data-id = " + row.refId + "></span>"
                                                 inputValue = row.link;
                                                 attachmentType = "file";
                        attachmentName=row.link;
                    } else {
                        if (row.link.toLowerCase().startsWith("http")||row.link.toLowerCase().startsWith("https")) {
                            attachmentName = row.link;
                            attachments = "<div class='file-uploader' data-provides='fileupload'>\
                                                    <a target='_blank' class=' word-wrap-break-word' href='" + row.link + "'>" +row.inputName + "</a>\
                                                    <span class='newAttachment hide' data-id = " + row.referenceId + "></span>"
                            inputValue = row.link
                        } else {
                            attachmentName = row.link;
                            attachments = "<div class='file-uploader' data-provides='fileupload'>\
                                                  <a target='_blank' class=' word-wrap-break-word' href='https://" + row.link + "'>" + row.inputName + "</a>\
                                                    <span class='word-wrap-break-word'></span>\
                                                    <span class='newAttachment hide' data-id = " + row.referenceId + "></span>"
                        }
                    }

                    return attachments + "<div class='input-group hide' style='width: 100%'>\
                                                <input type='text' class='form-control' disabled id='attachmentFilePathRmm'\
                                                                                        name='assessment-file'\
                                                                                        value='" + attachmentName + "'> \
                                                                 <span class='input-group-btn '>\
                                                                         <button class='browse btn btn-primary btn-file-upload allow-rmm-edit' data='" + JSON.stringify(row) + "' type='button'\
                                                                                 data-toggle='modal' data-target='#refAttachmentFileModal' >\
                                                                             <i class='glyphicon glyphicon-search'></i>\
                                                                         </button>\
                                                                     </span>\
                                                                  </div>"
                }
            },
            {
                "mData": "modifiedBy",
                "className": "col-md-1"
            },


            {
                "mData": "timeStamp",
                "className": "col-md-2"
            },
            {
                "mData": "",
                "sortable": false,
                "mRender": function (data, type, row) {
                    var actionButton=''
                    var string=""
                    var editBtnString=""
                    if(row.isShareEnabled==true){
                        var user=row.modifiedBy;
                        var gp=(row.sharedWithGroup.length<1)?',':row.sharedWithGroup;
                        var us=(row.sharedWithUser.length<1)?',':row.sharedWithUser;
                        string=  "<a href='#' title='Share' usr='"+user+"' class='table-row-share hidden-ic openShareWithModal'   id=" + row.refId + " data-toggle='modal' data-target='#sharedWithModal' sharedWithGroup="+gp+"   sharedWithUser="+us+" data-refId="+row.refId+"> " +
                            "<i class='mdi mdi-share-variant' style='color:black' aria-hidden='true' data-refId="+row.refId+" usr='"+user+"' sharedWithGroup="+gp+"   sharedWithUser="+us+" ></i> </a>"
                        editBtnString="<a href='javascript:void(0);' title='Edit' class='table-row-edit editRecord pv-ic hidden-ic' data-refId="+row.refId+">" +
                            "<i class='mdi mdi-pencil' aria-hidden='true' data-refId="+row.refId+"></i>\</a>";
                    }
                    if(row.isShareEnabled==false && row.addedBySelf==true){
                        editBtnString="<a href='javascript:void(0);' title='Edit' class='table-row-edit editRecord pv-ic hidden-ic' data-refId="+row.refId+">" +
                            "<i class='mdi mdi-pencil' aria-hidden='true' data-refId="+row.refId+"></i>\</a>"
                    }
                    var linkData=''
                    if(row.type =="Attachment"|| row.type === ATTACHMENT_TYPE_ENUM.ATTACHMENT){
                        linkData='/signal/dashboard/download?id=' + row.id
                    }else{
                        linkData="https://" + row.link
                    }
                    return string+"<a href='javascript:void(0);' title='Save' class='saveRecord table-row-saved hide pv-ic' data-refId="+row.refId+" > " +
                        "<i class='mdi mdi-check' aria-hidden='true' data-refId="+row.refId+"></i> </a>" +
                        "<span class='signalRmmId hide' data-signalRmmId=" + row.id + " ></span>" +editBtnString
                        +"<a href='javascript:void(0);' title='Cancel' class='cancelRecord table-row-cancel hide pv-ic' data-refId="+row.refId+" > " +
                        "<i class='mdi mdi-cancel' aria-hidden='true' data-refId="+row.refId+"></i> </a>" +
                        "<a href='javascript:void(0);' title='Delete' class='table-row-del deleteRecord hidden-ic' isOwner="+row.addedBySelf+" data-refId="+row.refId+" data-toggle='modal' data-target='#deleteReferenceModal' > " +
                        "<i class='mdi mdi-close' aria-hidden='true' data-refId="+row.refId+"></i> \</a> " +
                        "<a href='javascript:void(0);' title='Delete' class='table-row-del  hide hidden-ic' data-refId="+row.refId+"> " +
                        "<i class='mdi mdi-close' aria-hidden='true' data-refId="+row.refId+"></i> \</a> "
                },
                "className": 'col-md-1',
            }
        ],
        columnDefs: [
            { "targets": '_all' },
            { "render": $.fn.dataTable.render.text(), orderSequence: ['desc', 'asc'] }
        ]
    });
    if($("#refrences_length").children().length>1){
        $("#refrences_length").children()[1].remove()
    }
    $("#refrences_length").append("<label>entries</label>")
    $(".clsBtnEvent").on('click',clsBtnEvent)
    function isVisible() {
        var isVisible = true;
        if(typeof hasReviewerAccess !== "undefined" && !hasReviewerAccess) {
            isVisible = false;
        }
        return isVisible
    }
    // Resetting all select values to their default values
    $.fn.Reset_List_To_Default_Value = function () {
        $.each($(this), function (index, el) {
            var Founded = false;

            $(this).find('option').each(function (i, opt) {
                if (opt.defaultSelected) {
                    opt.selected = true;
                    Founded = true;
                }
            });
            if (!Founded) {
                if ($(this).attr('multiple')) {
                    $(this).val([]);
                }
                else {
                    $(this).val("");
                }
            }
            $(this).trigger('change');
        });
    }
});


var editComment=function(evt){
    evt.preventDefault();
    var extTextAreaModal = $("#textarea-ext1");
    triggerChangesOnModalOpening(extTextAreaModal);
    var commentBox = $(this).prev('.comment');
    extTextAreaModal.find('.textAreaValue').val(commentBox.val());
    extTextAreaModal.find('.modal-title').text($.i18n._('statusHistoryComment'));
    if (commentBox.prop('disabled')) {
        extTextAreaModal.find('.textAreaValue').prop('disabled', true);
        extTextAreaModal.find('.updateTextarea').prop('disabled', true);
    } else {
        extTextAreaModal.find('.textAreaValue').prop('disabled', false);
        extTextAreaModal.find('.updateTextarea').prop('disabled', false);
    }
    extTextAreaModal.modal("show");
    updateTextAreaData(commentBox);
    return false;
}



var shareData=function(){

    var arr=[]
    var string=''
    arr=$("#sharedWith").val();
    $("#sharedWith option").each(function(){
        var thisOptionValue=$(this).text();
        string=thisOptionValue+","+string
    });
    if(arr==null || arr == undefined || arr.length<1){
        $.Notification.notify('warning', 'top right', "Warning", "Please select share with.", {autoHideDelay: 5000});
        return;
    }
    var refId=$(this).attr('data-refid');
    var sharedUsersGroup=JSON.stringify(arr)
    var sharedData={
        "refId":refId,
        "shareWith":sharedUsersGroup
    }


    $.ajax({
        type: "POST",
        url: '/signal/dashboard/shareWith',
        data:sharedData,
    }).done(function (response) {

        if (response.status==true) {
            if(curruntObject!=undefined){
                var array=[];
                var isSharedWithGroupParsed=false
                var sharedWithGroup=[]
                var sharedWithGroupIndex=0
                var isSharedWithUserParsed=false
                var sharedWithUser=[]
                var sharedWithUserIndex=0
                array=string.split(",");
                for(var i=0;i<arr.length;i++){
                    if(arr[i].indexOf("Group")>-1){
                        if($(curruntObject).attr('sharedWithGroup')!=undefined){
                            if(isSharedWithGroupParsed==false){
                                sharedWithGroup= $(curruntObject).attr('sharedWithGroup').split(",")
                                sharedWithGroupIndex=sharedWithGroup.length;
                                isSharedWithGroupParsed=true
                            }
                        }
                        sharedWithGroup[sharedWithGroupIndex]=array[i]
                        sharedWithGroupIndex++;
                    }else{
                        if($(curruntObject).attr('sharedWithUser')!=undefined){
                            if(isSharedWithUserParsed==false){
                                sharedWithUser= $(curruntObject).attr('sharedWithUser').split(",")
                                sharedWithUserIndex=sharedWithUser.length;
                                isSharedWithUserParsed=true
                            }
                        }
                        sharedWithUser[sharedWithUserIndex]=array[i]
                        sharedWithUserIndex++;
                    }
                }
                sharedWithUser=sharedWithUser.length<1?',':sharedWithUser
                sharedWithGroup=sharedWithGroup.length<1?',':sharedWithGroup

                $(curruntObject).attr('sharedWithUser',sharedWithUser)
                $(curruntObject).attr('sharedWithGroup',sharedWithGroup)
            }

            $.Notification.notify('success', 'top right', "Success", "Reference shared successfully.", {autoHideDelay: 5000});

            $("#sharedWithModal").modal('hide')
        } else {
            $("#sharedWithModal").modal('hide')
            $.Notification.notify('error', 'top right', "Error", "Server error.", {autoHideDelay: 5000});
        }
    });
}

var closeReference=function(){
    $('.newRow').hide();
}
var okReferenceModalFn=function(){
    var curr = $(this);
    var refId=curr.attr('data-refid');
    var refData={
        "refId":refId
    }
    $.ajax({
        type: "POST",
        url: '/signal/dashboard/delete',
        data: refData,
    }).done(function (response) {
        if (response.status==true) {
            $.Notification.notify('success', 'top right', "Success", "Record deleted successfully.", {autoHideDelay: 5000});
            $("#imageListId .box-widget[data-id='"+refId+"']").remove();
        } else {
            $.Notification.notify('error', 'top right', "Error", "Server error.", {autoHideDelay: 5000});

        }
    });

    return;
}
$(document).on('change', '.productRadio', function () {
    var signalType=$('input[name="optradio"]:checked').val();
    $('#assignedSignalTable').DataTable().destroy();
    var data={
        "signalType":signalType
    }
    $('#assignedSignalTable').DataTable({
        initComplete: function () {
        },
        drawCallback: function () {
            colEllipsis();
            webUiPopInit();
        },
        "ajax": {
            "url": '/signal/dashboard/signalList',
            "cache": false,
            "dataSrc": "",
            "data":data
        },
        "oLanguage": {
            "oPaginate": {
                "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
            }
        },
        "aaSorting": [],
        "iDisplayLength": 50,
        "aLengthMenu": [[50, 100, -1], [50, 100, "All"]],
        "pagingType" : "simple_numbers",
        "aoColumns": [
            {
                "mData": "name",
                'className': '',
                "mRender": function (data, type, row) {
                    return genSignalAlertNameLink(row.signalId, row.signalName)
                }
            },
            {
                "mData": "productName",
                'className': '',
                "mRender": function (data, type, row) {
                    var type = '<span>' + addEllipsis(row.productName) + '</span>';
                    return type
                },
            },
            {"mData": "disposition"}
        ],
        scrollX: true,
        scrollY : dtscrollHeight(),
        columnDefs: [{
            "targets": '_all',
            "render": $.fn.dataTable.render.text()
        }]
    });
});

var clsBtnEvent=function(){
    if (!referenceTable.data().count()) {
        $('#refrences_wrapper .dt-scroll-body').show();
        $('#refrences td.dt-empty').show();
    }
    $(".newRow").hide()
}
/*$.fn.dataTable.ext.type.order['html-pre'] = function ( a ) {
        return !a ?
            '' :
            a.replace ?
                $.trim( a.replace( /<.*?>/g, "" ).toLowerCase() ) :
                a+'';
    };
*/
var loadReferenceData=function(){
    referenceTable=$("#refrences").DataTable({
        destroy: true,
        searching: true,
        "layout": {
            topStart: null,
            topEnd: {search: {placeholder: 'Search'}},
            bottomStart: ['pageLength', 'info', {
                paging: {
                    type: 'full_numbers'
                }
            }],
            bottomEnd: null,
        },
        responsive: false,
        "aaSorting": [],
        "order": [[ 4, "desc" ]],
        "pagination": true,
        "scrollCollapse": true,
        "border-bottom": "none",
        language: {
            "url": "../assets/i18n/dataTables_" + userLocale + ".json",
            search: ''
        },
        "ajax": {
            "url": 'dashboard/fetchAttachments',
            "dataSrc": "aaData",
        },
        "oLanguage": {
            "oPaginate": {
                "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
            },
            "sLengthMenu": "Show _MENU_",

            "sZeroRecords": "No data available in table", "sEmptyTable": "No data available in table"
        },
        "createdRow": function (row, data, dataIndex) {
            $(row).addClass('readonly');
            $(row).attr('data-id','signal-communication-table');
            var $dateCell = $(row).find('td:eq(5)');
            $dateCell.attr('data-order', data.dueDateSort)
        },
        "drawCallback": function () {
            if($(".newRowCommunication").is(":visible")){
                $("#signal-communication-table .dt-empty").hide();
            }
            colEllipsis();
            webUiPopInit();
            var rowsDataAR = $('#references').DataTable().rows().data();
        },
        "rowCallback": function (row, data, index) {
            $(row).find('.assignedToSelect').next(".select2-container").hide();

            $(row).find('.referenceType').select2({
                tags : true
            });

            if(!data.emailSent && (data.colorDueDate === 'orange' || data.colorDueDate === 'red')) {
                $(row).find('.due-date').css('color', data.colorDueDate);
            }
            if(!data.dueDate){
                $(row).find('.due-date').attr('placeholder', '');
            }
            if(!data.status){
                $(row).find('.status').val('');
            }
        },
        "bAutoWidth": false,
        "aoColumns": [
            {
                "mData": "referenceType",
                "orderDataType": "dom-select",
                "className": "col-md-2",
                "mRender": function (data, type, row) {
                    var $select = $("<select></select>", {
                        "value": data,
                        "class": "form-control referenceType",
                        "disabled": "true"
                    });
                    var $noSelection = $("<option></option>", {
                        "text": '',
                        "value": ''
                    });
                    $select.append($noSelection);
                    var referenceTypesArray=[]
                    var str=referenceTypes.substring(1,referenceTypes.length-1)
                    referenceTypesArray= str.split(",")
                    referenceTypesArray[referenceTypesArray.length]=data

                    for(var i=0;i<referenceTypesArray.length;i++){

                        var $option = $("<option></option>", {
                            "text": referenceTypesArray[i],
                            "value": referenceTypesArray[i]
                        });
                        if (data === referenceTypesArray[i]) {
                            $option.attr("selected", "selected")
                        }
                        $select.append($option);
                    }
                    var type = '<div class=" row"><div class="col-md-10">' + $select.prop("outerHTML") + '</div>';

                    type += "</div>";
                    return type
                }
            },
            {
                "mData": "description",
                "className": "col-md-3 cell-break",
                "mRender": function (data, type, row) {
                    if (!row.description) {
                        row.description = '';
                    }
                    var description =  "<span class='rmmDescriptionText'>" + addEllipsis(row.description)+ "</span>";

                    description +="<div class='textarea-ext refDescriptionInput hide'><input type='text' value='" + row.description + "' title='" + row.description + "' class='form-control description comment' maxlength='8000' style='width: 100%' disabled> \
                                     <a class='btn-text-ext openStatusComment  comment-on-edit hide' href='' tabindex='0' title='Open in extended form'> \
                                               <i class='mdi mdi-arrow-expand font-20 blue-1'></i></a></div>"
                    return description

                }
            },
            {
                "mData": "link",
                "className": "col-md-3 cell-break",
                "mRender": function (data, type, row) {
                    var attachments,attachmentName;
                    if (row.type =="Attachment"|| row.type === ATTACHMENT_TYPE_ENUM.ATTACHMENT) {
                        attachments = "<div class='file-uploader' data-provides='fileupload'>\
                                                <a class='word-wrap-break-word' href='/signal/dashboard/download?id=" + row.id + "'>" + row.inputName + "</a>\
                                                <span class='newAttachment hide' data-id = " + row.refId + "></span>"
                        inputValue = row.link;
                        attachmentType = "file";
                        attachmentName=row.link;
                    } else {
                        if (row.link.toLowerCase().startsWith("http")||row.link.toLowerCase().startsWith("https")) {
                            attachmentName = row.link;
                            attachments = "<div class='file-uploader' data-provides='fileupload'>\
                                                    <a target='_blank' class=' word-wrap-break-word' href='" + row.link + "'>" + row.link + "</a>\
                                                    <span class='newAttachment hide' data-id = " + row.referenceId + "></span>"
                            inputValue = row.link
                        } else {
                            attachmentName = row.link;
                            attachments = "<div class='file-uploader' data-provides='fileupload'>\
                                                  <a target='_blank' class=' word-wrap-break-word' href='https://" + row.link + "'>" + row.link + "</a>\
                                                    <span class='word-wrap-break-word'></span>\
                                                    <span class='newAttachment hide' data-id = " + row.referenceId + "></span>"
                        }
                    }

                    return attachments + "<div class='input-group hide' style='width: 100%'>\
                                                <input type='text' class='form-control' disabled id='attachmentFilePathRmm'\
                                                                                        name='assessment-file'\
                                                                                        value='" + attachmentName + "'> \
                                                                 <span class='input-group-btn '>\
                                                                         <button class='browse btn btn-primary btn-file-upload allow-rmm-edit' data='" + JSON.stringify(row) + "' type='button'\
                                                                                 data-toggle='modal' data-target='#refAttachmentFileModal' >\
                                                                             <i class='glyphicon glyphicon-search'></i>\
                                                                         </button>\
                                                                     </span>\
                                                                  </div>"
                }
            },
            {
                "mData": "modifiedBy",
                "className": "col-md-1"
            },
            {
                "mData": "timeStamp",
                "className": "col-md-2"
            },
            {
                "mData": "",
                "sortable": false,
                "mRender": function (data, type, row) {
                    var actionButton=''
                    var string=""
                    var editBtnString=""
                    if(row.isShareEnabled==true){
                        var user=row.modifiedBy;
                        var gp=(row.sharedWithGroup.length<1)?',':row.sharedWithGroup;
                        var us=(row.sharedWithUser.length<1)?',':row.sharedWithUser;
                        string=  "<a href='#' title='Share' usr='"+user+"' class='table-row-share hidden-ic openShareWithModal'   id=" + row.refId + " data-toggle='modal' data-target='#sharedWithModal' sharedWithGroup="+gp+"   sharedWithUser="+us+" data-refId="+row.refId+"> " +
                            "<i class='mdi mdi-share-variant' style='color:black' aria-hidden='true' data-refId="+row.refId+" usr='"+user+"' sharedWithGroup="+gp+"   sharedWithUser="+us+" ></i> </a>"
                        editBtnString="<a href='javascript:void(0);' title='Edit' class='table-row-edit editRecord pv-ic hidden-ic' data-refId="+row.refId+">" +
                            "<i class='mdi mdi-pencil' aria-hidden='true' data-refId="+row.refId+"></i>\</a>";
                    }
                    if(row.isShareEnabled==false && row.addedBySelf==true){
                        editBtnString="<a href='javascript:void(0);' title='Edit' class='table-row-edit editRecord pv-ic hidden-ic' data-refId="+row.refId+">" +
                            "<i class='mdi mdi-pencil' aria-hidden='true' data-refId="+row.refId+"></i>\</a>"
                    }
                    var linkData=''
                    if(row.type =="Attachment"|| row.type === ATTACHMENT_TYPE_ENUM.ATTACHMENT){
                        linkData='/signal/dashboard/download?id=' + row.id
                    }else{
                        linkData="https://" + row.link
                    }
                    return string+"<a href='javascript:void(0);' title='Save' class='saveRecord table-row-saved hide pv-ic' data-refId="+row.refId+" > " +
                        "<i class='mdi mdi-check' aria-hidden='true' data-refId="+row.refId+"></i> </a>" +
                        "<span class='signalRmmId hide' data-signalRmmId=" + row.id + " ></span>" +
                        editBtnString +
                        +"<a href='javascript:void(0);' title='Cancel' class='cancelRecord table-row-cancel hide pv-ic' data-refId="+row.refId+" > " +
                        "<i class='mdi mdi-cancel' aria-hidden='true' data-refId="+row.refId+"></i> </a>" +
                        "<a href='javascript:void(0);' title='Delete' class='table-row-del deleteRecord hidden-ic' isOwner="+row.addedBySelf+" data-refId="+row.refId+" data-toggle='modal' data-target='#deleteReferenceModal' > " +
                        "<i class='mdi mdi-close' aria-hidden='true' data-refId="+row.refId+"></i> \</a> " +
                        "<a href='javascript:void(0);' title='Delete' class='table-row-del hide hidden-ic' data-refId="+row.refId+"> " +
                        "<i class='mdi mdi-close' aria-hidden='true' data-refId="+row.refId+"></i> \</a> "

                },
                "className": 'col-md-1',
            }
        ],
        columnDefs: [
            { "targets": '_all' },
            { "render": $.fn.dataTable.render.text(),
                orderSequence: ['desc', 'asc']
            }
        ]
    });
}
