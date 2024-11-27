/***********************************************************************************************************************/
/******************************************* DOM Function Calls*********************************************************/
/***********************************************************************************************************************/


$(window).on('load', function () {
    function508C();
    autoHeightGrid();
    addTabIndex('td.fc-day.fc-widget-content');
});

$(window).on("resize", function () {
    autoHeightGrid();
});

$(function(){
    focusFirst();
    // closePopupOnScroll();
});

/******************************************* 508C **********************************************************************/
var function508C = function() {
    $("a, span, button").on("keydown", function( event ){
        if(event.keyCode ===13){
            $(this).trigger("click");
        }
    });

    $(".datepicker-calendar").on("keypress","button", function( event ){
        if(event.keyCode ===13){
            $(this).trigger("click");
        }
    });

    $("a:not('.tooltipDisabled'), span").on("focus", function () {
        $(this).tooltip({
            container: 'body',
            placement: "bottom"
        });
    });


    $('.fc-month-button').on('click',function(){
        addTabIndex('td.fc-day.fc-widget-content');
    });
    $('.fc-agendaWeek-button, .fc-agendaDay-button').on('click',function(){
        addTabIndex('.fc-time-grid .fc-slats tr td.fc-widget-content:nth-child(2)');
        $('#mainContent button:first').trigger("focus");
    });
};
/******************************************* 508C  for alert details tables*********************************************/
var enterKeyAlertDetail = function(){
    $("a.changePriority, .editAlertTags, .changeDisposition").on("keypress",function( event ){
        if(event.keyCode ===13){
            $(this).trigger("click");
        }
    });
    $(".dropdown-menu").on("keypress","a", function( event ){
        if(event.keyCode ===13){
            $(this).trigger("click");
        }
    });

};
/******************************************* 508C  tabIndex  and focus first and Access key*****************************/
var addTabIndex = function(e){
    var item =$(e);
    item.attr("tabindex","0");
};
var addHref = function(e){
    var item =$(e);
    item.attr("href","#");
};

var focusFirst = function() {
    var mainContent = $('#mainContent');
    mainContent.find('input, textarea, select, a, button').filter(':visible:first').trigger("focus");
};

var addGridShortcuts = function(e){
    var tableName =$(e);
    var tableWrapper = tableName.closest('.dt-container');
    tableWrapper.find('ul.pagination li.next a').attr('accesskey','>');
    tableWrapper.find('ul.pagination li.prev a').attr('accesskey','<');
    tableWrapper.find('.form-control.dt-pagination').attr('accesskey','w');
    tableWrapper.find('.form-control.dt-search').attr('accesskey','h');
};
var removeGridShortcuts = function(e){
    var tableName =$(e);
    var tableWrapper = tableName.closest('.dt-container');
    tableWrapper.find('ul.pagination li.next a').attr('accesskey','');
    tableWrapper.find('ul.pagination li.prev a').attr('accesskey','');
    tableWrapper.find('.form-control.dt-pagination').attr('accesskey','');
    tableWrapper.find('.form-control.dt-search').attr('accesskey','');
};
//*****************************************************data Table - scrollbody -  auto height***************************/
function autoHeightGrid() {
    var windowHeight = $(window).height();
    var headerHeight = $('#simpleCaseAlerts_wrapper .dt-scroll-head table, #evdasAlerts_wrapper .dt-scroll-head table, #rxTableReportsExecutionStatus_wrapper .dt-scroll-head table, #rxTableQueries_wrapper .dt-scroll-head table, #rxTableSpoftfireFiles_wrapper .dt-scroll-head table, #rxTableConfiguration_wrapper .dt-scroll-head table, #statsTable_wrapper .dt-scroll-head table, #rxTableSpoftfireFiles_wrapper .dt-scroll-head table').outerHeight();
    var tableHeight = (windowHeight - headerHeight - 162 - 75) + 'px';
    $('#simpleCaseAlerts_wrapper .dt-scroll-body, #evdasAlerts_wrapper .dt-scroll-body, #rxTableReportsExecutionStatus_wrapper .dt-scroll-body, #rxTableQueries_wrapper .dt-scroll-body, #rxTableSpoftfireFiles_wrapper .dt-scroll-body, #rxTableConfiguration_wrapper .dt-scroll-body, #statsTable_wrapper .dt-scroll-body, #rxTableSpoftfireFiles_wrapper .dt-scroll-body').css('height', tableHeight);


    var footerHeight2 = 63;
    var topHeight2 = 218;
    var headerHeight2 = $('#alertsDetailsTable_wrapper .dt-scroll-head').outerHeight();
    var flagsHeight2 = $('.flags').outerHeight();
    if(flagsHeight2==null){flagsHeight2 = 0;}
    var tableHeight2 = (windowHeight - topHeight2 - headerHeight2  - footerHeight2 - flagsHeight2) + 'px';
    $('#alertsDetailsTable_wrapper .dt-scroll-body').css('height', tableHeight2);

    var inboxHeight = (windowHeight - 120) + 'px';
    $('.inbox-content').css('height', inboxHeight);
}
/******************************************* Data table scroll - empty table********************************************/
var scrollOff = function () {
    $('table.dataTable').each(function () {
        var table = $(this);
        var tableScroll = table.parent();
        var hasScroll = tableScroll.hasClass('dt-scroll-body');

        if (table.find('td.dt-empty').length !== 0 && hasScroll ===true) {
            tableScroll.css('overflow', 'hidden');
        }
    });
};
/******************************************* Data table Row Height*******************************************************/
var dtscrollHeight = function(){
    return '100%' // changes to 100% for library upgrade
}
/******************************************* Data table Column Ellipsis**************************************************/
var colEllipsis = function(){
    $(".col-height").each(function () {
        if ($(this).height() > 60 || $(this).width() > $(this).parent().width()) {
            $(this).find(".ico-dots").css("display", "inline-block");
        } else {
            $(this).find(".ico-dots").css("display", "none");
        }
    });
};
$( ".DTFC_LeftHeadWrapper" ).on("click", function( event ) {
    event.stopImmediatePropagation();
});


/******************************************* Data table Column Ellipsis**************************************************/
var colActivityEllipsis = function(){
    $(".col-height").each(function () {
        if ($(this).height() > 60 || $(this).width() > $(this).parent().width()) {
            $(this).find(".ico-dots").css("display", "inline-block");
        } else {
            $(this).find(".ico-dots").css("display", "none");
        }
    });
};

var colArchivedEllipsis = function(){
    $(".col-height").each(function () {
        if ($(this).height() > 60 || $(this).width() > $(this).parent().width()) {
            $(this).find(".ico-dots").css("display", "inline-block");
        } else {
            $(this).find(".ico-dots").css("display", "none");
        }
    });
};;

var showEllipse = function () {
    $(".col-height").each(function () {
        if ($(this).context.innerText.length > 50) {
            $(this).find(".ico-dots").css("display", "inline-block");
        } else {
            $(this).find(".ico-dots").css("display", "none");
        }
    })
};

var showInfoPopover = function(){
    $( ".th-info-icon" ).on("click", function( event ) {
        event.stopImmediatePropagation();
        //event.stopPropagation();
        $(this).toggleClass('active');
        $(this).popover('toggle');
        $(".popover").addClass("popover-md");
        $('.th-info-icon').not(this).popover('hide').removeClass('active');
    });
};

var closeInfoPopover = function (){
    $('.pv-tab, .rxmain-container-content').on('click', function (e) {
        $('.popover-md').each(function () {
            if (!$(this).is(e.target) && $(this).has(e.target).length === 0 && $('.popover').has(e.target).length === 0) {
                $(this).popover('hide');
                $(".th-info-icon").removeClass('active');
            }
        })
    });
};

var productEllipses = function(){
    $('#product').each (function() {
        if($(this).height() > 36 || ($(this).width() > $(this).parent().width())) {
            $(this).find('.ico-dots').css('display','inline-block');
        } else {
            $(this).find('.ico-dots').css('display','none');
        }
    });
};

var colEllipsisModal = function(){
    $('.col-height').on("filter", function() {
        return $(this).height() > 51 || ($(this).width() > $(this).parent().width());
    }).find('.ico-dots').css('display','inline-block');

    $('.col-height').on("filter", function() {
        return !($(this).height() > 51 || ($(this).width() > $(this).parent().width()));
    }).find('.ico-dots').css('display','none');
    /*$('.col-height').each (function() {
        if($(this).height() > 51 || ($(this).width() > $(this).parent().width())) {
            $(this).parent().find('.ico-dots-modal').css('display','inline-block');
        } else {
            $(this).parent().find('.ico-dots-modal').css('display','none');
        }
    });*/
};
/******************************************* Data table Tag Ellipsis****************************************************/
var tagEllipsis = function(container) {

    container.find('.tag-length').on("filter", function() {
        return $(this).height() > 56 || ($(this).width() > $(this).parent().width());
    }).find('.ico-dots').css('display','inline-block');

    container.find('.tag-length').on("filter", function() {
        return $(this).height() > 56 || ($(this).width() > $(this).parent().width());
    }).find('.btn-edit-tag').removeClass('mid');

    container.find('.tag-length').on("filter", function() {
        return !($(this).height() > 56 || ($(this).width() > $(this).parent().width()));
    }).find('.ico-dots').css('display','none');

    container.find('.tag-length').on("filter", function() {
        return !($(this).height() > 56 || ($(this).width() > $(this).parent().width()));
    }).find('.btn-edit-tag').addClass('mid');


};

/******************************************* Business rule Tag Ellipsis****************************************************/
var businessTagEllipsis = function() {
    $('.tag-length').each (function() {
    if($(this).height() > 45) {
        $(this).find('.ico-dots').css('display','inline-block');
    } else {
        $(this).find('.ico-dots').css('display','none');
    }
});

};

/******************************************* Views Ellipsis****************************************************/
var viewsEllipsis = function() {
    $('.bookmark').each (function() {
        if($(this).text().length > 40) {
            $(this).find('.ico-dots').addClass("bookmark-inline");
        } else {
            $(this).find('.ico-dots').css('display','none');
        }
    });

};

/*************** Data table - Webui popover Inti and 508C Keyboard support - for Ellipsis*******************************/
var webUiPopInit = function(){
    var anchor = $(".view-all");
    anchor.webuiPopover({
        html: true,
        trigger: 'hover',
        content: function () {
            return "<div class='textPre word-break' style='white-space: pre-wrap !important'>" +$(this).attr('more-data') + "</div>"
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

//Added for PVS-64944
var webUiPopInitForCategories = function(){
    var anchor = $(".view-all1");
    anchor.webuiPopover({
        html: true,
        trigger: 'hover',
        content: function () {
            return "<div class='textPre word-break' style='white-space: pre-wrap !important'>" +$(this).attr('more-data') + "</div>"
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

var webUiPopInitActivities = function(){
    var anchor = $(".view-all");
    anchor.webuiPopover({
        html: true,
        trigger: 'hover',
        content: function () {
            return "<div class='textPre word-break' style='white-space: pre-wrap !important'>" +encodeToHTML($(this).attr('more-data')) + "</div>"
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



var webUiPopInitCaseHistory = function(){
    var anchor = $(".view-all");
    anchor.webuiPopover({
        html: true,
        trigger: 'hover',
        content: function () {
            return "<div class='textPre word-break' style='white-space: pre-wrap !important'>" +escapeAllHTML($(this).attr('more-data')) + "</div>"
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


var webUiPopInitAction = function(){
    var anchor = $(".view-all");
    anchor.webuiPopover({
        html: true,
        trigger: 'hover',
        content: function () {
            return "<div class='textPre word-break' style='white-space: pre-wrap !important'>" +encodeToHTML($(this).attr('more-data')) + "</div>"
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

var webUiPopInitMeeting = function(){
    var anchor = $(".view-all");
    anchor.webuiPopover({
        html: true,
        trigger: 'hover',
        content: function () {
            return "<div class='textPre word-break' style='white-space: pre-wrap !important'>" +escapeAllHTML($(this).attr('more-data')) + "</div>"
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

/******************************************* Signal popover - Search****************************************************/
var searchForSignal = function(){
  $("#signal-search").on("keyup", function() {
      var value = $(this).val().toLowerCase();
      $("#signal-list li").on("filter", function() {
            var signalName=$(this).attr('signalName');
            if(signalName==undefined){
                signalName='';
            }
          $(this).toggle(signalName.toLowerCase().indexOf(value) > -1)
      });
  });
};
/******************************************* Dropdown and select 2 closes on Scroll*************************************/
var closePopupOnScroll = function (){
    var scrollBody = $('.dt-scroll-body');
    scrollBody.on("scroll",function( event ) {
        $(".th-info-icon").popover('hide').removeClass('active');
        $('.dropdown.open .dropdown-toggle').dropdown('toggle');
        if ($('.assignedToSelect').data('select2')){
            $('.assignedToSelect').select2('close');
        }
    });
};

/******************************************* Element having Ellipsis*************************************/
var addEllipsis = function (rowValue) {
    var colElement = '';
    if (rowValue) {
        colElement = '<div class="col-container"><div class="col-height word-break">';
        colElement += escapeHTML(rowValue);
        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' +escapeHTML(rowValue)+ '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
        colElement += '</div></div>';
    }
    return colElement
};

var addEllipsisForMeeting = function (rowValue) {
    var colElement = '';
    if (rowValue) {
        colElement = '<div class="col-container"><div class="col-height word-break">';
        colElement += escapeAllHTML(rowValue);
        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' +escapeAllHTML(rowValue)+ '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
        colElement += '</div></div>';
    }
    return colElement
};

var addEllipsisForDescriptionText = function (rowValue) {
    var colElement = '';
    if (rowValue) {
        colElement = '<div class="col-container"><div class="col-height word-break">';
        colElement += escapeAllHTML(rowValue);
        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' +escapeAllHTML(rowValue)+ '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>'; //Handled for special characters
        colElement += '</div></div>';
    }
    return colElement
};

var addEllipsisWithEscape = function (rowValue) {
    var colElement = '';
    if (rowValue) {
        colElement = '<div class="col-container"><div class="col-height word-break">';
        colElement += escapeAllHTML(rowValue);
        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + escapeAllHTML(escapeAllHTML(rowValue)) + '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
        colElement += '</div></div>';
    }
    return colElement
};

var createLinkWithEllipsis = function (linkUrl, linkText) {
    var colElement = '';
    if (linkUrl && linkText) {
        colElement = '<div class="col-container"><div class="col-height word-break">';
        colElement += '<a href="' + linkUrl + '">' + encodeToHTML(escapeHTML(linkText)) + '</a>';
        colElement += '<a tabindex="0" title="' + $.i18n._('appLabel.viewAll') + '" class="ico-dots view-all" more-data="' + escapeHTML(linkText)+ '"><i class="mdi mdi-dots-horizontal font-20 blue-1"> </i></a>';
        colElement += '</div></div>';
    }
    return colElement
};

var removeLineBreak = function (row) {
    return row.replace(/(\r\n|\n|\r)/gm, ", ");
}