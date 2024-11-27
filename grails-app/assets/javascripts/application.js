// This is a manifest file that'll be compiled into application.js.
//
// Any JavaScript file within this directory can be referenced here using a relative path.
//
// You're free to add application-wide JavaScript to this file, but it's generally better
// to create separate JavaScript files as needed.

//= require vendorUi/bower_components/webcomponentsjs/webcomponents-lite.js
//= require grid
//= require grid-ext
//= require common-application
//= require vendorUi/select2/select2.full.min
//= require vendorUi/perfect-scrollbar/perfect-scrollbar.min
//= require_self
//= require app/pvs/common/rx_common.js

var filterOpened=0;
var DEFAULT_DATE_TIME_DISPLAY_FORMAT = "DD-MMM-YYYY hh:mm A";
var DEFAULT_DATE_DISPLAY_FORMAT = "DD-MMM-YYYY";
var DEFAULT_DATE_FORMAT = "DD-MMM-YYYY";
var REPORT_GENERATE_DATE_FORMAT="YYYY-MM-DD"
var DEFAULT_DATA_ANALYSIS_DATE_FORMAT = "MM/DD/YYYY";
var SESSION_TIME_OUT = "sessionTimeOut";
var MULTIPLE_AJAX_SEPARATOR="@!";
var JAPANESE_LOCALE='ja';
var CUMMULATIVE_START_DATE = '01/01/1900';
var DEFAULT_AUTO_HIDE_DELAY_TIME = 10000;
var ANALYSIS_REPORT_GENERATED = 'Analysis File Generated';
var ANALYSIS_REPORT_FAILED = 'Analysis File Failed';
var REPORT_GENERATED = 'Report Generated';
var CASE_SERIES_DRILLDOWN = 'Case Series Drilldown';
var PR_Calendar = "PR_Calendar";
var REPOERT_REQUEST= "REPOERT_REQUEST";
var PERIODIC_REPORT= "PERIODIC_REPORT";
var NOTIFICATION_QUEUE = "/topic/";
var DEFAULT_DELIMITER = ";";
var SYSTEM_USER ="SYSTEM";
var ALERT_STOP_LIST_PRODUCT_NAME = "Product Name";
var PRODUCT_GENERIC_NAME = "Product Generic Name";
var DATE_TEMPLATES = ['DD/MM/YYYY','DD-MMM-YYYY','DDMMYYYY','DDMMMYYYY'];
const ASSESSMENT_DATASOURCE_SAFETY_TITLE = 'Safety DB';
const ASSESSMENT_DATASOURCE_FAERS_TITLE = 'FAERS';
const ASSESSMENT_DATASOURCE_VAERS_TITLE = 'VAERS';
const ASSESSMENT_DATASOURCE_VIGIBASE_TITLE = 'VIGIBASE';
const ASSESSMENT_DATASOURCE_EVDAS_TITLE = 'EVDAS';
const ASSESSMENT_DATASOURCE_PVA = "pva";
const ASSESSMENT_DATASOURCE_FAERS = "faers";
const ASSESSMENT_DATASOURCE_VAERS = "vaers";
const ASSESSMENT_DATASOURCE_VIGIBASE = "vigibase";
const ASSESSMENT_DATASOURCE_EVDAS = "eudra";
//@TODO Hard-coded index used for drugRecNumber as per PVR design
const ASSESSMENT_DRUG_REC_NUM_LEVEL = 100;
const ASSESSMENT_DRUG_RECORD_NUMBER = "Drug Record Number";
const ASSESSMENT_INCLUDE_WHO_KEY = "includeWHODrugs";
const ASSESSMENT_DRUG_RECORD_KEY = "drugRecordNumber";
const ASSESSMENT_MULTI_INGREDIENT_KEY = "isMultiIngredient";
const BACKSPACE_KEY = 8;
const LEFT_ARROW_KEY = 37;
const UP_ARROW_KEY = 38;
const RIGHT_ARROW_KEY = 39;
const DOWN_ARROW_KEY = 40;
const DELETE_KEY = 46;

// regex constants
var EMAIL_REGEX = /^(")?(?:[^\."\s])(?:(?:[\.])?(?:[\w\-!#$%&'*+/=?^_`{|}~]))*\1@(\w[\-\w]*\.){1,5}([A-Za-z]){2,6}$/;

var EVDAS_BUSNSINESS_RULE_ENUM ={
    RELTV_ROR_PAED_VS_OTHR: "RELTV_ROR_PAED_VS_OTHR",
    EVDAS_SDR_PAED: "EVDAS_SDR_PAED",
    RELTV_ROR_GERTR_VS_OTHR: "RELTV_ROR_GERTR_VS_OTHR",
    EVDAS_SDR_GERTR: "EVDAS_SDR_GERTR",
    TOTAL_SPON_EUROPE: "TOTAL_SPON_EUROPE",
    TOTAL_SPON_N_AMERICA: "TOTAL_SPON_N_AMERICA",
    TOTAL_SPON_JAPAN: "TOTAL_SPON_JAPAN",
    TOTAL_SPON_ASIA: "TOTAL_SPON_ASIA",
    TOTAL_SPON_REST: "TOTAL_SPON_REST",
    EVDAS_IME_DME: "EVDAS_IME_DME",
    ROR_ALL_EVDAS: "ROR_ALL_EVDAS"
};

var BUSINESS_RULE_ENUM = {
    COUNTS  : "COUNTS"
};

var BUSINESS_CONFIG_ATTRIBUTES = {
    DME_AGG_ALERT : "DME",
    IME_AGG_ALERT : "IME",
    SPECIAL_MONITORING : "SPECIAL_MONITORING",
    STOP_LIST : "STOP_LIST"
};

var BUSINESS_RULE_FORMAT_CLASS = {
    NEW_COUNT_AGG: "NEW_COUNT",
    CUMM_COUNT_AGG: "CUM_COUNT",
    NEW_SER_AGG: "NEW_SER",
    CUMM_SER_AGG: "CUM_SER",
    NEW_FATAL_AGG: "NEW_FATAL",
    CUMM_FATAL_AGG: "CUM_FATAL",
    NEW_GERI_AGG: "NEW_GER",
    CUMM_GERI_AGG: "CUM_GER",
    NEW_PEDIA_AGG: "NEW_PEDIA",
    CUMM_PEDIA_AGG: "CUM_PEDIA",
    NEW_COUNT_FAERS: "NEW_COUNT_FAERS",
    NEW_COUNT_VAERS: "NEW_COUNT_VAERS",
    NEW_COUNT_VIGIBASE: "NEW_COUNT_VIGIBASE",
    NEW_COUNT_JADER: "NEW_COUNT_JADER",
    CUMM_COUNT_FAERS: "CUMM_COUNT_FAERS",
    CUMM_COUNT_VAERS: "CUMM_COUNT_VAERS",
    CUMM_COUNT_VIGIBASE: "CUMM_COUNT_VIGIBASE",
    CUMM_COUNT_JADER: "CUMM_COUNT_JADER",
    NEW_SPON_COUNT_FAERS: "NEW_SPON_FAERS",
    CUM_SPON_COUNT_FAERS: "CUMM_SPON_FAERS",
    NEW_STUDY_COUNT_FAERS: "NEW_STUDY_FAERS",
    CUM_STUDY_COUNT_FAERS: "CUMM_STUDY_FAERS",
    NEW_SERIOUS_COUNT_FAERS: "NEW_SER_FAERS",
    NEW_SERIOUS_COUNT_VAERS: "NEW_SER_VAERS",
    NEW_SERIOUS_COUNT_VIGIBASE: "NEW_SER_VIGIBASE",
    NEW_SERIOUS_COUNT_JADER: "NEW_SER_JADER",
    CUM_SERIOUS_COUNT_FAERS: "CUMM_SER_FAERS",
    CUM_SERIOUS_COUNT_VAERS: "CUMM_SER_VAERS",
    CUM_SERIOUS_COUNT_VIGIBASE: "CUMM_SER_VIGIBASE",
    CUM_SERIOUS_COUNT_JADER: "CUMM_SER_JADER",
    NEW_FATAL_COUNT_FAERS: "NEW_FATAL_FAERS",
    NEW_FATAL_COUNT_VAERS: "NEW_FATAL_VAERS",
    NEW_FATAL_COUNT_VIGIBASE: "NEW_FATAL_VIGIBASE",
    NEW_FATAL_COUNT_JADER: "NEW_FATAL_JADER",
    CUM_FATAL_COUNT_FAERS: "CUMM_FATAL_FAERS",
    CUM_FATAL_COUNT_VAERS: "CUMM_FATAL_VAERS",
    CUM_FATAL_COUNT_VIGIBASE: "CUMM_FATAL_VIGIBASE",
    CUM_FATAL_COUNT_JADER: "CUMM_FATAL_JADER",
    NEW_GERI_COUNT_FAERS : "newGeriatricCount",
    NEW_GERI_COUNT_VAERS : "newGeriatricCount",
    NEW_GERI_COUNT_VIGIBASE : "newGeriatricCount",
    NEW_GERI_COUNT_JADER : "newGeriatricCount",
    CUM_GERI_COUNT_FAERS : "cumGeriatricCount",
    CUM_GERI_COUNT_VAERS : "cumGeriatricCount",
    CUM_GERI_COUNT_VIGIBASE : "cumGeriatricCount",
    CUM_GERI_COUNT_JADER : "cumGeriatricCount",
    NEW_NON_SERIOUS_FAERS : "newNonSerious",
    CUM_NON_SERIOUS_FAERS : "cumNonSerious",
    PRR_SCORE_VAERS: "PRR_VAERS",
    PRR_SCORE_VIGIBASE: "PRR_VIGIBASE",
    PRR_SCORE_JADER: "PRR_JADER",
    PRR_SCORE_FAERS: "PRR_FAERS",
    PRRLCI_SCORE_FAERS: "PRRLCI_FAERS",
    PRRLCI_SCORE_VAERS: "PRRLCI_VAERS",
    PRRLCI_SCORE_VIGIBASE: "PRRLCI_VIGIBASE",
    PRRLCI_SCORE_JADER: "PRRLCI_JADER",
    PRRUCI_SCORE_FAERS: "PRRUCI_FAERS",
    PRRUCI_SCORE_VAERS: "PRRUCI_VAERS",
    PRRUCI_SCORE_VIGIBASE: "PRRUCI_VIGIBASE",
    PRRUCI_SCORE_JADER: "PRRUCI_JADER",
    ROR_SCORE_FAERS: "ROR_FAERS",
    ROR_SCORE_VAERS: "ROR_VAERS",
    ROR_SCORE_VIGIBASE: "ROR_VIGIBASE",
    ROR_SCORE_JADER: "ROR_JADER",
    RORLCI_SCORE_FAERS: "RORLCI_FAERS",
    RORLCI_SCORE_VAERS: "RORLCI_VAERS",
    ROR9UCI_SCORE_VAERS: "RORUCI_VAERS",
    RORLCI_SCORE_VIGIBASE: "RORLCI_VIGIBASE",
    RORUCI_SCORE_VIGIBASE: "RORUCI_VIGIBASE",
    RORLCI_SCORE_JADER: "RORLCI_JADER",
    RORUCI_SCORE_JADER: "RORUCI_JADER",
    EBGM_SCORE_FAERS: "EBGM_FAERS",
    EBGM_SCORE_VAERS: "EBGM_VAERS",
    EBGM_SCORE_VIGIBASE: "EBGM_VIGIBASE",
    EBGM_SCORE_JADER: "EBGM_JADER",
    EB05_SCORE_FAERS: "EB05_FAERS",
    EB05_SCORE_VAERS: "EB05_VAERS",
    EB05_SCORE_VIGIBASE: "EB05_VIGIBASE",
    EB05_SCORE_JADER: "EB05_JADER",
    EB95_SCORE_FAERS: "EB95_FAERS",
    EB95_SCORE_VAERS: "EB95_VAERS",
    EB95_SCORE_VIGIBASE: "EB95_VIGIBASE",
    EB95_SCORE_JADER: "EB95_JADER",
    NEW_PEDIA_COUNT_FAERS: "NEW_PEDIA_FAERS",
    NEW_PEDIA_COUNT_VAERS: "NEW_PEDIA_VAERS",
    NEW_PEDIA_COUNT_VIGIBASE: "NEW_PEDIA_VIGIBASE",
    NEW_PEDIA_COUNT_JADER: "NEW_PEDIA_JADER",
    CUMM_PEDIA_COUNT_FAERS: "CUMM_PEDIA_FAERS",
    CUMM_PEDIA_COUNT_VAERS: "CUMM_PEDIA_VAERS",
    CUMM_PEDIA_COUNT_VIGIBASE: "CUMM_PEDIA_VIGIBASE",
    CUMM_PEDIA_COUNT_JADER: "CUMM_PEDIA_JADER",
    NEW_INTERACTING_COUNT_FAERS: "NEW_INTERACTING_FAERS",
    CUMM_INTERACTING_COUNT_FAERS: "CUMM_INTERACTING_FAERS"


};

var ATTACHMENT_TYPE_ENUM = {
    ATTACHMENT : "Attachment",
    REFERENCE  : "Reference"
}

var dmvOpened=false;
var STATUS_ENUM = {
    OPEN: "OPEN",
    IN_PROGRESS: "IN_PROGRESS",
    NEED_CLARIFICATION: "NEED_CLARIFICATION",
    CLOSED: "CLOSED"
};

var EXECUTION_STATUS_ENUM = {
    SCHEDULED: 'Scheduled',
    GENERATING: 'Generating',
    DELIVERING: 'Delivering',
    COMPLETED: 'Completed',
    ERROR: 'Error',
    WARN: 'Warn'
};

var ALERT_CONFIG_TYPE = {
    SINGLE_CASE_ALERT: 'Single Case Alert',
    SINGLE_CASE_ALERT_DASHBOARD: 'Single Case Alert - Dashboard',
    SINGLE_CASE_ALERT_DRILL_DOWN: 'Single Case Alert - DrillDown',
    SINGLE_CASE_ALERT_DRILL_DOWN_ADHOC: 'Single Case Alert - Adhoc DrillDown',
    SINGLE_CASE_ALERT_ON_DEMAND: 'Single Case Alert on Demand',
    AGGREGATE_CASE_ALERT: 'Aggregate Case Alert',
    AGGREGATE_CASE_ALERT_ON_DEMAND: 'Aggregate Case Alert on Demand',
    AGGREGATE_CASE_ALERT_DASHBOARD: 'Aggregate Case Alert - Dashboard',
    SIGNAL_MANAGEMENT:'Signal Management',
    ADHOC_ALERT:'Ad-Hoc Alert',
    LITERATURE_SEARCH_ALERT: 'Literature Search Alert',
    EVDAS_ALERT:'EVDAS Alert',
    EVDAS_ALERT_DASHBOARD:'EVDAS Alert - Dashboard',
    TRIGGERED_ALERT:'Triggered Alerts',
    CASE_DRILLDOWN : 'Case Series Drilldown',
    STATISTICAL_COMPARISON : 'Statistical Comparison',
    CASE_DRILLDOWN : 'Case Series Drilldown',
    EVENT_DETAIL : 'Event Detail',
    QUALTITATIVE_ALERT : 'Qualitative Alert',
    QUANTITATIVE_ALERT : 'Quantitative Alert',
    LITERATURE_ALERT : 'Literature Alert',
    QUALTITATIVE_CONFIGURATION : 'Individual Case Configuration',
    QUANTITATIVE_CONFIGURATION : 'Aggregate Configuration',
    EVDAS_CONFIGURATION : 'EVDAS Configuration',
    LITERATURE_CONFIGURATION : 'Literature Configuration',
    QUALITATIVE_ALERT : 'Individual Case',
    QUANTITATIVE_ALERT : 'Aggregate'
};

var SIGNAL_CHARTS = {
  AGE_GROUP:'age-grp-over-time-chart',
  SERIOUSNESS : 'seriousness-over-time-chart',
  COUNTRY : 'country-over-time-chart',
  GENDER : 'gender-over-time-chart',
  OUTCOME : 'outcome-over-time-chart',
  SERIOUS_PIE_CHART : 'seriousness-count-pie-chart',
  HEAT_MAP : 'system-organ-heat-map',
  ASSESSMENT_DETAILS : 'assessmentDetails'
};

var ALERT_CONFIG_TYPE_SHORT = {
    QUALTITATIVE_ALERT: 'Individual Case',
    QUANTITATIVE_ALERT: 'Aggregate',
    LITERATURE_ALERT: 'Literature',
    EVDAS_ALERT: 'EVDAS',
    ADHOC_ALERT: 'Ad-Hoc'
};

var ALERTS_PREFIX = {
    AGG : 'aca_',
    SCA : 'sca_',
    EV  : 'eca_',
    ADHOC  : 'adhoc_'
};

var ALERT_PREFIX_FILTER = {
    AGG : 'aca_filterMap_'
};

var APPLICATION_NAME = {
    SIGNAL_MANAGEMENT: 'Signal Management',
    CASE_DETAIL: 'Case Detail',
    EVENT_DETAIL: 'Event Detail'
};

var APPLICATION_LABEL = {
    SIGNAL_MANAGEMENT: 'Signal Management',
    EVENT_DETAIL: 'Event Detail',
    CASE_DETAIL: 'Case Detail'

};

var ALERT_PREFIX_VIEW = {
    AGG : 'aca_viewName_'
};

var CALLING_SCREEN = {
    DASHBOARD       : "dashboard",
    REVIEW          : "review",
    TRIGGERED_ALERTS: "Triggered Alerts",
    TAGS            : "tags"
};

var COUNTS = {
    NEW: "NEW",
    CUMM: "CUMM"
};
var SMQ_TYPE = {
    BROAD: "BROAD",
    NARROW: "NARROW",
    EMPTY: "EMPTY"
}
var FLAGS = {
    CUMM_FLAG: "CUMM_FLAG",
    PEDI_FLAG: "PEDI_FLAG",
    INTERACTING_FLAG: "INTERACTING_FLAG",
    SPONT_FLAG :"SPONT_FLAG",
    SERIOUS_FLAG:"SERIOUS_FLAG",
    FATAL_FLAG:"FATAL_FLAG",
    GERI_FLAG:"GERI_FLAG",
    NON_SERIOUS_FLAG:"NON_SERIOUS_FLAG",
    STUDY_FLAG:"STUDY_FLAG"
};

var STRATIFICATION_FIELDS = {
    PRR: "PRR",
    ROR: "ROR",
    PRRLCI: "PRRLCI",
    PRRUCI: "PRRUCI",
    RORLCI: "RORLCI",
    RORUCI: "RORUCI",
    EBGM: "EBGM",
    EB05: "EB05",
    EB95: "EB95"
};

var ALERT_TYPE = {
    SINGLE_CASE_ALERT_FAERS : "Single Case Alert - Faers",
    SINGLE_CASE_ALERT_VAERS : "Single Case Alert - Vaers",
    SINGLE_CASE_ALERT_VIGIBASE : "Single Case Alert - Vigibase",
    SINGLE_CASE_ALERT_JADER : "Single Case Alert - Jader"
};

var RULE_FLAGS = {
  PVA:  "PVA",
    FAERS : "FAERS",
    QUALITATIVE: "Qualitative",
    QUANTITATIVE: "Quantitative",
    EVDAS: "EVDAS",
    VAERS: "VAERS",
    VIGIBASE: "VigiBase",
    JADER: "JADER",
    AGGREGATE: "Aggregate",
    ICR: "Individual Case"
};

ACTION_ITEM_FILTER_ENUM = {
    MY_OPEN: "My Open Action Items",
    MY_ALL: "My All Action Items",
    ALL: "All Action Items"
};

var ADVANCED_FILTER_FIELDS = {
    ASSIGNED_TO_USER_ID : "assignedTo.id",
    ASSIGNED_TO_GROUP_ID : "assignedToGroup.id",

};

var BADGES = {
    NEW : "New",
    PENDING_REVIEW : "Due from Previous Period",
    AUTO_FLAGGED : "Auto Flagged"
};

var BUSINESS_RULE_OPERATOR = {
    EQUAL_TO: "Equal To"
};

var SPOTFIRE = {
    STANDARD_QUERIES: "Standard Queries",
    SMQ_TYPE: "SMQ Type",
    MEDDRA_QUERIES: "MedDRA Queries (SMQs and cSMQs)",
    CASE_DEMO_AE: "Case, Demographics & AEs",
    EVENT_GROUP: "Event Group",
    PREFERRED_TERM: "Preferred Term",
    ALL_THERAPIES: "All Therapies",
    PRODUCT_NAME: "Product Name",
    TRADE_NAME: "Trade Name",
    INGREDIENT_NAME: "Ingredient Name",
    PRODUCT_FAMILY: "Product Family",
    PRODUCT_GROUP: "Product Group",
    SYMPTOMS_CO_MANIFESTATION: "Symptoms/Co-manifestation"
}

var SIGNAL_STATUS={
    VALIDATION_DATE:"Validation Date"
}

var DATASHEET = {
    ALL_SHEET : "All Sheet",
    CORE_SHEET: "Core Sheet"
};

var OPERATOR={
    IS_EMPTY:"IS_EMPTY",
    IS_NOT_EMPTY:"IS_NOT_EMPTY"
}
var OPERATOR_VALUE={
    EQUALS: "EQUALS",
    DOES_NOT_CONTAIN: "DOES_NOT_CONTAIN",
    CONTAINS: "CONTAINS"
}
var OPERATOR_STRING={
    EQUALS : "Equals",
    NOT_EQUAL:"Not Equal",
    DOES_NOT_CONTAIN:"Does Not Contain",
    CONTAINS:"Contains"
}
var UNDEFINED = 'undefined';
var ALL ='-- All --';
var SELECT ='Select';
var PRODUCT_LIST='productList'
var TOMORROW = new Date(new Date().setDate(new Date().getDate() + 1));
var prodDicLevelList = [];
var prodDicLevelListEvdas = [];

var findPlusInString = "\\+";
var findHashInString = "\\#";
var emptyJustification = "[]";
var isMasterConfig = false;
var productList = [];
var shownProduct;
var evdasAlertId;
var showEventDic=true;
var selectedCheckBoxes = [];
var BLINDING_STATUS = {
    BLINDED: "Blinded",
    REDACTED: "Redacted"
};

$.fn.select2.defaults.set("width", null);

function showDataTableLoader($selector) {
    pvui.common.pvWidgetLoader.hideLoader($selector);
    pvui.common.pvWidgetLoader.showLoader($selector);
}

function hideDataTableLoader($selector) {
    pvui.common.pvWidgetLoader.hideLoader($selector);
}

$(function () {
    $(document).on('show.bs.dropdown','.dropDown', function (event) {
        $(this).addClass('dropdown-zindex');
    })
    $(document).on('hide.bs.dropdown','.dropDown', function (event) {
        $(this).removeClass('dropdown-zindex');
    })
    TOMORROW =new Date(TOMORROW.toLocaleString("en-US", {timeZone: userTimeZone}));
    $(document).on("click", ".popover .close" , function(){
        $(this).parents(".popover").popover('hide');
        $(".th-info-icon").removeClass('active');
    });

    $(".dataTable").on("processing.dt", function (e, settings, processing) {
        try {
            if (settings.oInit.customProcessing) {
                if (processing) {
                    showDataTableLoader($(e.target));
                } else {
                    hideDataTableLoader($(e.target));
                }
            }
        } catch (e) {
            console.error("Error while reading settings of datatable");
        }
    });

    var appnedDropdownOutside = (function () {
        var dropdownMenu;
        $(".dropdown-outside").on({
            "show.bs.dropdown": function (e) {
                e.stopPropagation();
                dropdownMenu = $(e.target).find('.dropdown-menu');
                var eOffset = $(e.target).offset();
                $('body').append(dropdownMenu.css({
                    'display': 'block',
                    'top': eOffset.top + $(e.target).outerHeight(),
                    'left': eOffset.left,
                    'max-width': '200px'
                }).detach());
            },
            "hidden.bs.dropdown": function (e) {
                e.stopPropagation();
                $(e.target).append(dropdownMenu.detach());
                dropdownMenu.hide();
            }
        });
    })();

    if(typeof applicationName !== "undefined" && ['Single Case Alert', 'Aggregate Case Alert', 'EVDAS Alert', 'Literature Search Alert', APPLICATION_NAME.EVENT_DETAIL].includes(applicationName)){
        addCountBoxToInputField(8000, $('textarea').not('#commentbox,#comment-template,#details,#comments,#commentNotes,#notes'));
        addCountBoxToInputField(8000,$('textarea#comments'));
        addCountBoxToInputField(8000,$('textarea.actionDetails'));
    } else {
        addCountBoxToInputField(8000, $('textarea').not('#genericComment,#commentbox,#comment-template,#description,.workflow-rule-description,#referencesExpandedTextArea,#commentNotes,#notes'));
        addCountBoxToInputField(8000,$('#signalCommunicationDescription'));
        addCountBoxToInputField(8000,$('textarea#reasonForEvaluation'));
        addCountBoxToInputField(8000,$('textarea#SignalStatusComments'));
        addCountBoxToInputField(4000,$('textarea.groupTextarea'));
        addCountBoxToInputField(8000,$('textarea#textarea-ext-2'));
        addCountBoxToInputField(4000,$('textarea#meetingAgenda'));
        addCountBoxToInputField(80,$('#footerSelect'));
        var groupTextArea = document.querySelector('.groupTextarea');
        if (!groupTextArea){
            addCountBoxToInputField(4000,$('textarea#description'));
        }

        addCountBoxToInputField( 255,$('.workflow-rule-description'));
        addCountBoxToInputField( 255,$('.disposition-description'));
        addCountBoxToInputField(8000,$('#signalRMMDescription'));
    }
    addCountBoxToInputField(32000, $('textarea#commentNotes,textarea#notes'));
    addCountBoxToInputField(32000, $('textarea#commentbox'));
    if(typeof applicationName !== UNDEFINED && !([APPLICATION_LABEL.SIGNAL_MANAGEMENT].includes(applicationName))){
        addCountBoxToInputField(8000, $('textarea#genericComment'));
        addCountBoxToInputField(8000, $('textarea#referencesExpandedTextArea'));
    }
    addCountBoxToInputField(8000, $('textarea#brDescription'));
    addCountBoxToInputField(8000,$('#descriptionProductGroup'));
    addCountBoxToInputField(8000,$('#eventGroupDescription'));

    if( typeof isDataSourceEnabled !=="undefined"  && !isDataSourceEnabled) {
        $("#dataSourcesProductDict").closest(".row").hide()
    }

    $("#referencesTextArea").on("change", function() {
        $("#referencesExpandedTextArea").val($(this).val());
    });
    $('#productGroupModalDict').on('shown.bs.modal', function (e) {
        $('#descriptionProductGroup').on('keyup', function (e) {
            releaseEnter("descriptionProductGroup","id");
        });
    });
    $('#eventGroupModal').on('shown.bs.modal', function (e) {
        $('#eventGroupDescription').on('keyup', function (e) {
            releaseEnter("eventGroupDescription","id");
        });
    });

    $('#createActionModal').on('shown.bs.modal', function (e) {
        $('#comments').on('keyup', function (e) {
            releaseEnter("comments","id");
        });
        $('.actionDetails').on('keyup', function (e) {
            releaseEnter("actionDetailsData","class");
        });
    });

});

if (typeof jQuery !== 'undefined') {
    (function ($) {
        $('#spinner').ajaxStart(function () {
            $(this).fadeIn();
        }).ajaxStop(function () {
            $(this).fadeOut();
        });
    })(jQuery);
}

function encodeToHTML(str) {
    var temp = document.createElement('div');
    temp.textContent = str;
    var res = temp.innerHTML;
    temp.remove();
    return res;
};

function decodeFromHTML(str) {
    var textArea = document.createElement('textarea');
    textArea.innerHTML = str;
    return textArea.value;
}

function encodeHTMLTags(str) {
    var temp = document.createElement('div');
    temp.textContent = $("<p/>").html(str).text();
    var res = temp.innerHTML;
    temp.remove();
    return res;
};

function convertToSingleQuote(str) {
    return str.replace(/"/g, "'");
}

function defaultRender(str, convertToSinglequote) {
    var res =encodeToHTML(str);
    if (typeof  convertToSinglequote !== "undefined" && convertToSinglequote) {
        res = convertToSingleQuote(res);
    }
    return res;
};
function calculateCount(ibox, tareaMaxLength, lineBreaksCount) {
    var text = ibox.val();
    if (text.match(/\n/g)) {
        lineBreaksCount = text.match(/\n/g).length;
    }else {
        lineBreaksCount = 0;
    }
    var length = text.length;
    var left = tareaMaxLength - (length + lineBreaksCount);
    if (left < 0) {
        length = length + left;
        var txt = text.substring(0, length);
        ibox.val(txt);
        left = 0;
    }
    return {text:(length + lineBreaksCount) + ' / ' + tareaMaxLength, lineBreaksCount:lineBreaksCount};
}
function addCountBoxToInputField(maxLength, dataField) {
    var tareaMaxLength = maxLength;
    var inputField = dataField;
    inputField.attr('maxlength', tareaMaxLength);
    inputField.parent().append('<div class="countBox"></div>');
    inputField.on('focus', function () {
        var ibox = $(this);
        var countBox = ibox.parent().find('.countBox');
        ibox.parent().css('position', 'relative');
        var lineBreaksCount = 0;
        ibox.on('keyup', function (e) {
            var result = calculateCount(ibox,tareaMaxLength,lineBreaksCount);
            lineBreaksCount = result.lineBreaksCount;
            countBox.text(result.text);
            e.preventDefault();
        });
        var firstResult = calculateCount(ibox,tareaMaxLength,lineBreaksCount);
        lineBreaksCount = firstResult.lineBreaksCount;
        countBox.text(firstResult.text);
        countBox.show();
        ibox.on('focusout', function (e) {
            countBox.hide();
            e.preventDefault();
        });
    });
}

function clearFormInputsChangeFlag(formSelector){
    if($(formSelector).find('.changed-input').length) {
        $.each($(formSelector).find('.changed-input'), function(){
            $(this).removeClass('changed-input');
        })
    }
}

var ajaxAuthroizationError = function (xhr) {
    if (xhr.status === 403 || xhr.status === 401) {
        window.location = '/signal/login/auth';
    }
}

function checkUserNavigation() {
    $('form').on('submit', function () {
        clearFormInputsChangeFlag($('form'));
    });

    $('#mainContent form').not('#userSearchForm').not('#auditLogSearchForm').on('change keyup keydown keypress', 'input, textarea, select, checkbox, radio', function (e) {
        if(!$(this).hasClass('changed-input')) {
            $(this).addClass('changed-input');
        }
    });

    $(window).on('beforeunload', function (event) {
        if ($('form .changed-input:visible').length || ($('.studyRadio').length && $('.productRadio').length && $('form .changed-input').length)) {
            return $.i18n._('navigateAwayErrorMessage');
        }
    });
}

function showSpinnerMessage(id) {
    if (id) {
        $("#" + id).css("display", "inline");
    } else {
        $("#spinnerMessage").css("display", "inline");
    }

}

function clearSpinnerMessage() {
    $("#spinnerMessage").css("display", "none");
}

//Client side sorting of the entries of a dropdown
$.fn.sort_select_box = function(){
    // Get options from select box
    var my_options = $("#" + this.attr('id') + ' option');
    // sort alphabetically
    my_options.sort(function(a,b) {
        if (a.text > b.text) return 1;
        else if (a.text < b.text) return -1;
        else return 0
    });
    //replace with sorted my_options;
    $(this).empty().append( my_options );

    // clearing any selections
    $("#"+this.attr('id')+" option").attr('selected', false);
};

function capitalizeFirstLetter(string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
}

function resetForm(container){
    container.find('input:text, input:password, input:file, select, textarea').val('');
    container.find('input:radio, input:checkbox').removeAttr('checked').removeAttr('selected');
    $('.dmvCopyAndPasteModal .file_input').val('');
    $('.dmvCopyAndPasteModal #file_input').val('');
}

function closeAllCopyPasteModals(cancel) {
    $("div[id^='copyAndPaste']").modal('hide');
    resetForm($('#importValueSection'));
    $('#validate-values').attr('disabled', 'disabled');
    $('#fileFormatError').hide();
    $(".modal-body").find("#compare-screens-spinner").remove()
}

function closeAllImportValueModal() {
    var currentQEV = $(this).closest('.toAddContainerQEV')[0];
    var importValueSection = $(currentQEV).find('#importValueSection');
    if(typeof currentQEV ==="undefined"){
        var importModal=$("div[id^='importValueModal'].in");
        importModal.modal('hide');
        importValueSection=importModal.parent().prev('.copyAndPasteModal').find('#importValueSection');
    }
    resetForm(importValueSection);
    importValueSection.find('#validate-values').attr('disabled', 'disabled');
}

function closeJustificationModal() {
    $("div[id^='workflowStatusJustification']").modal('hide');
    $('#workflowSelect').html('');
}

function successNotification(message) {
    $(".alert-success").alert('close');
    if (message != undefined && message != "")
        $("div.rxmain-container-content").prepend(
            '<div class="alert alert-success alert-dismissable">' +
            '<button type="button" class="close" ' +
            'data-dismiss="alert" aria-hidden="true">' +
            '&times;' +
            '</button>' +
            message +
            '</div>'
        );
}

function errorNotification(message) {
    $(".alert-danger").alert('close');
    if (message != undefined && message != "")
        $("div.rxmain-container-content").prepend(
            '<div class="alert alert-danger alert-dismissable">' +
            '<button type="button" class="close" ' +
            'data-dismiss="alert" aria-hidden="true">' +
            '&times;' +
            '</button>' +
            message +
            '</div>'
        );
}

function warningNotification(message) {
    $(".alert-warning").alert('close');
    if (message != undefined && message != "")
        $("div.rxmain-container-content").prepend(
            '<div class="alert alert-warning alert-dismissable">' +
            '<button type="button" class="close" ' +
            'data-dismiss="alert" aria-hidden="true">' +
            '&times;' +
            '</button>' +
            message +
            '</div>'
        );
}
var modalOpener = function() {
    $(".copy-paste-pencil").on('click', function () {
        var content="",name="",modal="";dataValidatable="false";
        content=  $(this).closest(".toAddContainerQEV").find(".expressionValueText").val();
        if(content=="" || content==null || content=="null"){
            content=  $(this).closest(".toAddContainerQEV").find(".expressionValueSelectNonCache").val();
        }
        if(content=="" || content==null || content=="null"){
            content=  $(this).closest(".toAddContainerQEV").find(".expressionValueSelect").val();
        }
        if(content=="" || content==null || content=="null"){
            content=  $(this).closest(".toAddContainerQEV").find(".expressionValueDate").val();
        }
        if(content=="" || content==null || content=="null"){
            content=  $(this).closest(".toAddContainerQEV").find(".expressionValueSelectAuto").val();
        }
        if(content!==null && content!=="" && typeof content !=="undefined"){
            content=content.toString().replaceAll(',',DEFAULT_DELIMITER);
        }
        dataValidatable=$(this).closest(".toAddContainerQEV").find(".expressionField").attr("data-validatable");
        modal=$(this).closest(".toAddContainerQEV").find("#copyAndPasteModal");
        modal.find(".copyPasteContent").val(content);
        modal.find(".c_n_p_other_delimiter").val('');
        if(typeof dataValidatable==="undefined"){
            dataValidatable=$("#dmvDataValidatable").val();
        }
        if (JSON.parse(dataValidatable)) {
            modal.find('.validate-copy-paste').removeAttr('disabled');
        } else{
            $('.validate-copy-paste').attr('disabled', 'disabled');
        }

        var radiobtn = modal.find('input:radio[value="none"]')[0];
        radiobtn.checked = true;
        modal.modal('show');
        fileChange(modal);
        fileSelect(modal.find("#file_input"));
        modal.val(content);
        if (content != undefined && !_.isEmpty(content)) {
            modal.find('input:radio[name=delimiter][value="'+DEFAULT_DELIMITER+'"]').prop('checked', true);
        } else {
            modal.find('input:radio[name=delimiter][value="none"]').prop('checked', true);
        }
    })
 }

function bindSelect2WithUrl(selector, queryUrl, data, isLiterature) {
    var selectedDataSource = isLiterature ? 'embase' : $('#selectedDatasource')?$('#selectedDatasource').val().toString():'';
    var select2Element =  selector.select2({
        minimumInputLength: 0,
        multiple: false,
        placeholder: $.i18n._('selectOne'),
        allowClear: true,
        width: "100%",
        ajax: {
            quietMillis: 250,
            dataType: "json",
            url: queryUrl,
            data: function (params) {
                return {
                    dataSource:selectedDataSource,
                    term: params.term || "",  //search term
                    page: params.page || 1,  //page number
                    max: 30
                };
            },
            processResults: function (data, params) {
                params.page = params.page || 1;
                return {
                    results: data.list,
                    pagination: {
                        more: (params.page * 30) < data.totalCount
                    }
                };
            }
        }
    });
    if (data!=undefined && data.id) {
        var option = new Option(data.text, data.id, true, true);
        selector.append(option).trigger('change.select2');
    }
    return select2Element
}
function generateProductJson(productName, level,id) {
    var signalProductValues = {"1": [], "2": [], "3": [], "4": [], "5": []};
    if (level !== ASSESSMENT_DRUG_REC_NUM_LEVEL) {
        signalProductValues[level].push({name: productName, id: id});
    }
    return JSON.stringify(signalProductValues);
}
function bindDataSheet2WithData(selector, dataSheetUrl, data) {
    var select2Element = selector.select2({
        minimumInputLength: 0,
        multiple: true,
        placeholder: 'Select Datasheet(s)',
        allowClear: true,
        width: "100%",
        ajax: {
            quietMillis: 250,
            dataType: "json",
            type: 'POST', // converted to POST method to handle large limit (bugFix/PVS-60897)
            url: function () {
                return dataSheetUrl;
            },
            data: function (params) {
                var productMap;
                const productList = [];
                var isProductGroup = false;
                var enabledSheet = $("#allSheets").val();
                var dataSource =  ($('#selectedDatasource') ? $('#selectedDatasource').val().toString() : '');
                if ($('.removeSingleDictionaryProductValue').length > 0) {
                    $('.removeSingleDictionaryProductValue').each(function () {
                        if ($(this).attr('data-element')) {
                            productList.push($(this).attr('data-element'));
                            isProductGroup = true;
                        }
                    });
                } else {
                    $(".removeSingleDictionaryValue").each(function () {
                        if ($(this).attr('data-element')) {
                            productMap = $(this).attr('data-element') ? JSON.parse($(this).attr('data-element')) : '';
                            productMap.level = productMap.level ? productMap.level : JSON.parse($(this).attr('data-level'));
                            productList.push(generateProductJson(productMap.name, productMap.level, productMap.id));
                        }
                    });
                    if (typeof isProductAssignment !== "undefined" && isProductAssignment) {
                        isProductGroup = data["isProductGroup"];
                        productList.push(data["products"]);
                        dataSource = data["dataSource"];
                    }
                }
                return {
                    dataSource: dataSource,
                    products:productList,
                    enabledSheet: enabledSheet,
                    isProductGroup: isProductGroup,
                    term: params.term || "",  //search term
                    page: params.page || 1,  //page number
                    max: 30
                };
            },
            processResults: function (data, params) {
                params.page = params.page || 1;
                return {
                    results: data.list,
                    pagination: {
                        more: (params.page * 30) < data.totalCount
                    }
                };
            }
        }
    });
    if (data != undefined && data.length > 0) {
        var option;
        $(data).each(function (i, sheet) {
            if($("#dataSheet option[value='"+sheet.id+"']").length < 1){
                option = new Option(sheet.text, sheet.id, true, true);
                selector.append(option).trigger('change.select2');
            }
        });
    }
    return select2Element
    }

    function disableDataMiningVariableFields(flag) {
    $('#dataMiningVariable').prop('disabled', flag);
    $('#selectOperator1').prop('disabled', flag);
    $('#selectValue1').prop('disabled', flag);
    $('#advance-filter-pencil').prop('disabled', flag);
    $("#dataMiningVariableValueDiv").children("#inputValue").prop('disabled', flag);
    if (flag) {
        $(".searchEventDmv,.dmvPencil,#inputValue").css({
            "pointer-events": "none",
            "cursor": "not-allowed"
        })
        $("#dataMiningVariableString").hide();
        var includesPva = $('#selectedDatasource').val();
            if (includesPva == null || includesPva == "null") {
                includesPva = false;
            } else {
                includesPva = $('#selectedDatasource').val().includes(dataSources.PVA);
            }
            if (JSON.parse(includesPva)) {
                showHideForegroundQueryCheckbox(true);
            } else {
                showHideForegroundQueryCheckbox(false);
                showHideForegroundQuery(false);
            }

    } else {
        $(".searchEventDmv,.dmvPencil,#inputValue").css("pointer-events", "");
        $(".searchEventDmv,.dmvPencil,#inputValue").css("cursor", "");
        $(".dmvPencil, .dmvEvent").hide();
        $("#dataMiningVariableValueDiv").children("#inputValue").hide();
        $("#dataMiningVariableString").empty();
        setDmvData();
        $("#dataMiningVariableString").show();

    }
}

function bindMultipleSelect2WithUrl(selector, queryUrl, selectedData) {
    selector.select2({
        tags:true,
        minimumInputLength: 0,
        multiple: true,
        width: "100%",
        separator: ";",
        ajax: {
            quietMillis: 250,
            dataType: "json",
            url: queryUrl,
            data: function (params) {
                return {
                    term: params.term,
                    max: params.page || 30,
                    lang: userLocale
                };
            },
            processResults: function (data, params) {
                params.page = params.page || 30;
                return {
                    results: data.list,
                    pagination: {
                        more: (params.page * 30) < data.length
                    }
                };
            }
        },
        escapeMarkup: function (m) {
            return m;
        }
    });
    if (selectedData) {
        $.each(selectedData, function(i, data){
            var option = new Option(data.name, data.id, true, true);
            selector.append(option).trigger('change.select2');
        });
    }
}

function checkIfSessionTimeOutThenReload(event, json) {
    if (json && json[SESSION_TIME_OUT]) {
        event ? event.stopPropagation() : "";
        alert($.i18n._('sessionTimeOut'));
        window.location.reload();
        return false
    }
}

/**
 * Determines whether a value is a positive integer or not
 * @param n
 * @returns {boolean}
 */
function isPositiveInteger(n) {
    return n % 1 === 0 && n > 0;
}

function isEmpty(str) {
    return (!str || 0 === str.length);
}

function isBlank(str) {
    return (!str || /^\s*$/.test(str));
}

function getReloader(toolbar, tableName) {
    var reloader = '<span title="Refresh" class="glyphicon reloaderBtn glyphicon-refresh"></span>';
    reloader = $(reloader);
    $(toolbar).append(reloader);
    if (tableName != undefined) {
        $('.reloaderBtn').on('click', function () {
            $('.reloaderBtn').addClass('glyphicon-refresh-animate');
            $(tableName).DataTable().draw();
        });
    }
}

function setDefaultDisplayDateFormat(date) {
    return moment(date).utc(date).format(DEFAULT_DATE_DISPLAY_FORMAT);
}
function newSetDefaultDisplayDateFormat(date) {
    //this function is written to accept more than date type
    //formats date in "DD-MMM-YYYY" format
    return moment.utc(date, DATE_TEMPLATES).format(DEFAULT_DATE_DISPLAY_FORMAT);
}

function newSetDefaultDisplayDateFormat2(date) {
    //formats date in "MM-DD-YYYY" format
    return moment(date,['MM/DD/YYYY','DD-MMM-YYYY']).utc(date).format(DEFAULT_DATA_ANALYSIS_DATE_FORMAT);
}

function buildReportingDestinationsSelectBox(destinationsSelectBox, url, primaryDestinationField, isPrimarySelectable) {
    var selectReportings = bindMultipleSelect2WithUrl(destinationsSelectBox, url, true);
    selectReportings.select2('container').on("mousedown", "li.select2-search-choice span", function (event) {
        event.preventDefault();
        event.stopPropagation();
    }).on("click", "li.select2-search-choice span", function () {
        if(isPrimarySelectable != false){
            $(this).parent().parent().find("li.select2-search-choice span").removeClass("primary");
            $(this).addClass("primary");
            primaryDestinationField.val($(this).parent().find('div').text());
        }
    });
    selectReportings.on("select2-removed", function (e) {
        if(isPrimarySelectable != false) {
            var primaryDestination = primaryDestinationField.val();
            if (e.val == primaryDestination) {
                primaryDestinationField.val("");
            }
        }
    }).on("change", function (e) {
        var primaryDestination = primaryDestinationField.val();
        $(this).select2('container').find("li.select2-search-choice span").removeClass('primary');
        $(this).select2('container').find("li.select2-search-choice").each(function () {
            if ($(this).find("span").length == 0) {
                $(this).append("<span>P</span>")
            }
            if (primaryDestination && $(this).find('div').text() == primaryDestination) {
                $(this).find("span").addClass("primary")
            }

        });
    }).trigger('change');
    return selectReportings
}

//Email configuration for Adhoc/Periodic Report create and edit page and also for Dashboard and Generated Adhoc Reports page.
function emailConfig(editable){

    var emailConfiguration = $('#emailConfiguration');
    var noEmailOnNoData = $("#noEmailOnNoData");
    var subject = $("#subject");
    var body = $("#body");
    var noEmailOnNoDataValue = $("#noEmailOnNoDataValue");
    var bodyValue = $("#bodyValue");
    var subjectValue = $("#subjectValue");
    if((subject.val() && body.val())){
        $('.showEmailConfiguration img').attr({src:'/reports/assets/icons/email-secure.png',title:"Email configuration edited"});
    } else {
        $('.showEmailConfiguration img').attr({src:'/reports/assets/icons/email.png',title:"Add email configuration"});
    }

    $(emailConfiguration).on('show.bs.modal', function (e) {
        subjectValue.parent().removeClass('has-error');
        bodyValue.parent().removeClass('has-error');
        if ((subject.val() && body.val())) {
            (noEmailOnNoData.val() === "true") ? noEmailOnNoDataValue.prop('checked', true) : noEmailOnNoDataValue.prop('checked', false);
            subjectValue.val(subject.val());
            bodyValue.val(body.val());
        }
    });

    $(emailConfiguration).find('#saveEmailConfiguration').on('click', function (e) {
        if (!validateModalOnSubmit()) {
            return false;
        }
        noEmailOnNoData.val(noEmailOnNoDataValue.is(':checked'));
        subject.val(subjectValue.val().trim());
        body.val(bodyValue.val().trim());

        if((subject.val() && body.val())){
            $('.showEmailConfiguration img').attr({src:'/reports/assets/icons/email-secure.png',title:"Email configuration edited"});
        } else {
            $('.showEmailConfiguration img').attr({src:'/reports/assets/icons/email.png',title:"Add email configuration"});
        }

        $(this).attr('data-dismiss', 'modal');
    });

    $(emailConfiguration).find('#resetEmailConfiguration').on('click', function (e) {
        noEmailOnNoDataValue.prop('checked', false);
        subjectValue.val('');
        bodyValue.val('');
    });

    function validateModalOnSubmit() {
        var validate;
        var isFormReset = !(subjectValue.val().trim() || bodyValue.val().trim() || noEmailOnNoDataValue.is(':checked'));

        if( isFormReset || (subjectValue.val().trim() && bodyValue.val().trim())){
            validate = true;
            noEmailOnNoDataValue.parent().removeClass('has-error');
        }
        else{
            if(noEmailOnNoDataValue.is(':checked')){
                noEmailOnNoDataValue.parent().addClass('has-error');
            }
            if (!subjectValue.val().trim()) {
                subjectValue.parent().addClass('has-error');
            } else {
                subjectValue.parent().removeClass('has-error')
            }
            if (!bodyValue.val().trim()) {
                bodyValue.parent().addClass('has-error');
            } else {
                bodyValue.parent().removeClass('has-error');
            }
            validate = false;
        }

        return validate;
    }
}

if (!String.prototype.includes) {
    String.prototype.includes = function(search, start) {
        'use strict';
        if (typeof start !== 'number') {
            start = 0;
        }

        if (start + search.length > this.length) {
            return false;
        } else {
            return this.indexOf(search, start) !== -1;
        }
    };
}

$(function () {
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");
    var parameter = $("meta[name='_csrf_parameter']").attr("content");

    if(header && token){
        $(document).ajaxSend(function(e, xhr, options) {
            if(options.type !== "GET") {
                xhr.setRequestHeader(header, token);
            }
        });
    }

    if(parameter && token){
        $("form").on('submit', function () {
            var hiddenField = $("<input>").attr("type", "hidden").attr("name", parameter).val(token);
            $(hiddenField).appendTo(this);
        });
    }
});

function categoryMaximumSize(nonConfiguredEnabled) {
    var tagsEnabled = nonConfiguredEnabled != "true" ? false : true;
    setTimeout(function () {
            $('.select2-search__field').attr('maxlength', 255);
    }, 5000);
}
function escapeHTML(unesacpedHtml) {
    return unesacpedHtml != undefined ?
        unesacpedHtml.toString()
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, "&quot;")
            : ""
}

function escapeAllHTML(unesacpedHtml) {
    return unesacpedHtml != undefined ?
        unesacpedHtml.toString().replace(/&/g, "&amp;")
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;") : ""
}

function unescapeHTML(esacpedHtml) {
    return esacpedHtml != undefined ?
        esacpedHtml.toString().replace(/&amp;/g, "&")
            .replace(/&lt;/g, '<')
            .replace(/&gt;/g, '>')
            .replace(/<br>/g, '')
            .replace(/&quot;/g, '"')
            .replace(/&#039;/g, "'") : ""
}

var convertAlertTypeName = function (alertType) {
    switch (alertType) {
        case ALERT_CONFIG_TYPE.SINGLE_CASE_ALERT:
            return ALERT_CONFIG_TYPE_SHORT.QUALTITATIVE_ALERT;
        case ALERT_CONFIG_TYPE.AGGREGATE_CASE_ALERT:
            return ALERT_CONFIG_TYPE_SHORT.QUANTITATIVE_ALERT;
        case ALERT_CONFIG_TYPE.LITERATURE_SEARCH_ALERT:
            return ALERT_CONFIG_TYPE_SHORT.LITERATURE_ALERT;
        case ALERT_CONFIG_TYPE.EVDAS_ALERT:
            return ALERT_CONFIG_TYPE_SHORT.EVDAS_ALERT;
        case ALERT_CONFIG_TYPE.ADHOC_ALERT:
            return ALERT_CONFIG_TYPE_SHORT.ADHOC_ALERT;
        default:
            return alertType
    }
};

var preventEnterKeySubmit = function (e) {
    if (e.keyCode === 13) {
        e.stopPropagation();
        e.preventDefault();
        return false;
    }
};

var showSuccessNotification = function (successMessage, autoHideDelay) {
    autoHideDelay = autoHideDelay || DEFAULT_AUTO_HIDE_DELAY_TIME;
    $.Notification.notify('success', 'top right', "Success", successMessage, {autoHideDelay: autoHideDelay});
};

var showErrorNotification = function (errorMessage, autoHideDelay) {
    autoHideDelay = autoHideDelay || DEFAULT_AUTO_HIDE_DELAY_TIME;
    $.Notification.notify('error', 'top right', "Error", errorMessage, {autoHideDelay: autoHideDelay});
};

function disableDictionaryValues(productGroup, ingredient, family, productName, trade) {
    $(".prodDictFilterColCalc label:contains('Family')").next("input").prop("disabled", family)
    $(".prodDictFilterColCalc label:contains('Trade Name')").next("input").prop("disabled", trade)
    $(".prodDictFilterColCalc label:contains('Product Group')").next("input").prop("disabled", productGroup)
    $(".prodDictFilterColCalc label:contains('Product Name')").next("input").prop("disabled", productName)
    $(".prodDictFilterColCalc label:contains('Ingredient')").next("input").prop("disabled", ingredient)
};
function checkDictLevelMap (index , dataSource) {
    if (dataSource == "eudra") {
        if (signal.utils.localStorageUtil.getProp("dictionaryMapEvdas") == null) {
            $.ajax({
                type: "GET",
                url: '/signal/pvsProductDictionary/fetchDictionaryListEvdas',
                async: false
            }).done(function (result) {
                    signal.utils.localStorageUtil.setJSON("dictionaryMapEvdas", result.dictMap);
                    prodDicLevelListEvdas = result.dictMap;
            });
        } else {
            prodDicLevelListEvdas = signal.utils.localStorageUtil.getJSON("dictionaryMapEvdas");

        }
        return prodDicLevelListEvdas[index-1]

    }
    else {
        if (signal.utils.localStorageUtil.getProp("dictionaryMap") == null) {
            $.ajax({
                type: "GET",
                url: '/signal/pvsProductDictionary/fetchDictionaryList',
                async: false
            }).done(function (result) {
                    signal.utils.localStorageUtil.setJSON("dictionaryMap", result.dictMap);
                    prodDicLevelList = result.dictMap;
            });
        } else {
            prodDicLevelList = signal.utils.localStorageUtil.getJSON("dictionaryMap");

        }
        return prodDicLevelList[index - 1]
    }
}

function setSelectWidth(container) {
    container.each(function () {
        if ($(this).val() != "" && $(this).val() != null) {
            $(this).next().find(".select2-search__field").css({"min-width": "30px"});
            $(this).next().find(".select2-search__field").addClass('width-0');
        } else {
            $(this).next().find(".select2-search__field").css({"min-width": "130px"})
            $(this).next().find(".select2-search__field").removeClass('width-0');

        }
    });
}


function pageDictionary(table, totalCase) {
    var displayLength = $(table).find('.dt-length');
    var dtPaginate = $(table).find('.dt-paging');
    var element = $(table).find('.dt-info')[0];
    if(totalCase <1){
        // $(".custom-count").remove();
        displayLength.hide();
        dtPaginate.hide();
        $(element).hide();
    } else{
        // $('.custom-count').hide();
        displayLength.show();
        dtPaginate.show();
        $(element).show();
    }
}
function pageDictionaryForAlertDetails(table, miminumLengthMenu, totalCase) {
    var displayLength = $(table).find('.dt-length');
    var displayInfo = $(table).find('.dt-info')[0];
    var displayPagination = $(table).find('.dt-paging');
    if(totalCase < miminumLengthMenu){
        displayLength.hide();
        displayPagination.hide();
    } else{
        displayLength.show();
        displayPagination.show();
        showTotalPageForAlertDetails(table, totalCase);
    }
}

function pageDictionaryForFive(table, totalCase) {
    var displayLength = $(table).find('.dt-length');
    var dtPaginate = $(table).find('.dt-paging');
    var element = $(table).find('.dt-info')[0];
    if(totalCase <= 5){
        // $(".custom-count").remove();
        displayLength.hide();
        dtPaginate.hide();
        $(element).hide();
    } else{
        // $('.custom-count').hide();
        displayLength.show();
        dtPaginate.show();
        $(element).show();
    }
}
function showTotalPage(table, totalCase){
    var element = $(table).find('.dt-info') ? $(table).find('.dt-info')[0] : false;
    if (element) {
        element.innerText = 'of ' + totalCase + ' entries';
    }
}
function showTotalPageForAlertDetails(table, totalCase) {
    var element = $(table).find('.dt-info')[0];
    if (element) {
        element.innerText = ' entries '+ 'of ' + totalCase + ' entries';
    }
}

function initPSGrid(container) {
    var psGrid;
    psGrid = new PerfectScrollbar(container.find('.dt-scroll-body')[0]);
};

/*
$.fn.dataTable.ext.order['dom-input-date'] = function (settings, col) {
    return this.api().column(col, { order: 'index' }).nodes().map(function (td, i) {
        return $(td).data('order');
    });
};
$.fn.dataTable.ext.order['dom-select'] = function  ( settings, col ) {
    return this.api().column( col, {order:'index'} ).nodes().map( function ( td, i ) {
        return $('select', td).val();
    } );
};
*/


function formatText(text, withComma) {
    withComma = typeof withComma !== "undefined" ? withComma : false;
    var arr, tempText;
    var i;
    if (text.includes(",")) {
        tempText = withComma == true ? text.replaceAll(",", ",\n") : text.replaceAll(",", "\n")
        if (tempText.charAt(tempText.length - 1) == ",") {
            return tempText.replace(/.$/, "")
        } else {
            return tempText;
        }
    } else {
        return text;
    }
}


var generateCaseSeries = function (selectedDatasource,parent, productName, eventName, execConfigId, isArchived, spotfireUrl,value,columnName) {
    var smqType = parent.find(".smqType").attr("value")
    var pt = parent.find(".preferredTerm").attr("value")
    var groupBySmq = parent.find(".groupBySmq").attr("value")
    var soc = parent.find(".soc").attr("value")
    var filterName = parent.find(".filterName").attr("value")
    var productName = parent.find(".productName").attr("value")
    var seriesData = parent.find(".seriesData").attr("value")
    var dataSource = parent.find(".dataSourceSpotfire").attr("value")
    var seriesDataArray = seriesData.split(",");
    var typeFlag, type, executedConfigId, filterString, configurationBlockString, productId, ptCode,keyId;


    for (var i = 0; i < seriesDataArray.length; i++) {
        var rowArray = seriesDataArray[i].toString().split(":");
        if (rowArray[0] == "typeFlag") {
            typeFlag = rowArray[1]
        }else if (rowArray[0] == "keyId") {
            keyId = rowArray[1]
        }else if (rowArray[0] == "type") {
            type = rowArray[1]
        } else if (rowArray[0] == "productId") {
            productId = rowArray[1]
        } else if (rowArray[0] == "ptCode") {
            ptCode = rowArray[1]
        } else if (rowArray[0] == "executedConfigId") {
            executedConfigId = rowArray[1]
        }
    }
    if (type == COUNTS.NEW) {
        filterString = 'FLAGVALUE=0;'
    } else if (type = COUNTS.CUMM) {
        filterString = 'FLAGVALUE=1;'
    }
    filterString = filterString + 'KEYID=' + keyId + ';'
    filterString = filterString + 'FILEGENERATEDFROM=agg_counts;'
    filterString = filterString + 'FLAGNAME=' + typeFlag + ';'
    spotfireUrl = spotfireUrl.split(window.location.origin)[1]
    var ptCodeList = ''
    var productIdList = ''
    var termScopeList = ''
    if(selectedCases.length > 1) {
        $.each(selectedCasesInfoSpotfire, function (k, v) {
            if (!(ptCodeList && productIdList)) {
                ptCodeList = v.ptCode
                productIdList = v.productId
            } else {
                ptCodeList = ptCodeList + "," + v.ptCode
                productIdList = productIdList + "," + v.productId
            }
            if (JSON.parse(groupBySmq) && v.ptCode) {
                if (!termScopeList) {
                    if (soc.toUpperCase() === "SMQ") {
                        termScopeList = v.smqType
                    }
                } else {
                    if (soc.toUpperCase() === "SMQ") {
                        termScopeList = termScopeList + "," + v.smqType
                    }
                }
            }
        });
    }else {
        ptCodeList = ptCode
        productIdList = productId
        if (pt) {
            if (JSON.parse(groupBySmq)) {
                if (soc.toUpperCase() === "SMQ") {
                    if (smqType.toUpperCase() === SMQ_TYPE.BROAD) {
                        termScopeList = 1
                    } else if (smqType.toUpperCase() === SMQ_TYPE.NARROW) {
                        termScopeList = 2
                    } else {
                        termScopeList = 0
                    }
                }
            }
        }
    }
    if(termScopeList && termScopeList !== ''){
        termScopeList =  "\"" + termScopeList + "\""
        filterString = filterString + 'TERMSCOPE=' + termScopeList + ';'
    }

    if(ptCodeList && productIdList){
        ptCodeList =  "\"" + ptCodeList + "\""
        productIdList =  "\"" + productIdList + "\""
    }
        filterString = 'EXECUTIONID=' + executedConfigId + ';' + 'BASEID=' + productIdList + ';' + 'MEDDRAPTCODE=' + ptCodeList + ';' + filterString
    var spotfireValue = value
    var spotfireColumnName = columnName
    var setFilterString = ""
        switch (spotfireValue?.toLowerCase()) {
            case 'yes':
                spotfireValue = 'Yes'
                break
            case 'no':
                spotfireValue = 'No'
                break
            default:
                spotfireValue = ''
                break
        }
        if (spotfireValue)
            setFilterString = '&SetFilter(tableName=\"Case, Demographics & AEs\", columnName = \"' + spotfireColumnName + '\", values= {"' + spotfireValue + '"});'
        else
            setFilterString = '&SetFilter(tableName=\"Case, Demographics & AEs\", columnName = \"' + spotfireColumnName + '\", Operation= {\"Reset\"});'
    if(typeof dataSource !== 'undefined' && (dataSource.toUpperCase() == "FAERS" || dataSource.toUpperCase() == "VAERS")){
        configurationBlockString = filterString
    }else{
        configurationBlockString = filterString+setFilterString
    }
    if(spotfireUrl.split('configurationBlock=') !== 'undefined' && spotfireUrl.split('configurationBlock=').length > 0)
        spotfireUrl = spotfireUrl.split('configurationBlock=')[0]+'configurationBlock='
    spotfireUrl+=encodeURIComponent(configurationBlockString);
    spotfireUrl+='&AGG_COUNT_SF_URL='+spotfireUrl
    if (typeof filterString !== 'undefined') {
        signal.utils.postUrl(spotfireUrl, {
            configurationBlock: configurationBlockString,
            AGG_COUNT_SF_URL: spotfireUrl
        }, true);
    }
}
var removeSelect2 = function () {
    $("#dataMiningVariableValueDiv").children("span.select2").remove();
    $("#dataMiningVariableValueDiv").children(".select2").remove();
    $("#dataMiningVariableValueDiv").children("#inputValue").show();
}

function addAdvancedFilterDisp(listOfDisp, prefix, configId) {
    //this if condition is used to store main disposition icon value in session storage when advanced filter has disposition filter and advanced filter is applied first time.
    var mainStore=JSON.parse(sessionStorage.getItem(prefix + "filters_value"));
    if (typeof listOfDisp !== "undefined" && listOfDisp.length > 0 && mainStore == null)
    {
        var dispVal = [];
        var dispStore = {};
        $.each($(".disposition-ico").eq(0).find("li"),function (id,val) {
            if($(val).find('input').is(':checked') === true){
                dispVal.push($(val).find('input').val());
                dispStore[$(this).find('input').val()] = true;
            } else {
                dispStore[$(this).find('input').val()] = false;
            }
        });
        sessionStorage.setItem(prefix + "filters_value", JSON.stringify(dispVal));
        sessionStorage.setItem(prefix + "filters_store", JSON.stringify(dispStore));
        sessionStorage.setItem(prefix + "id", configId);
    }

    var advFilterSelectsBox = false;
    $.each($(".disposition-ico"),function (idx,value) {
        $.each($(value).find("li"),function (id,val) {
            $.each(listOfDisp,function (index, disposition) {
                if(disposition === $(val).find('input').val()){
                    if($(val).find('input').is(":checked") === false){
                        advFilterSelectsBox = true;
                        $(val).find('input').prop('checked', true);
                    }
                }
            else if(!listOfDisp.includes($(val).find('input').val())){
                    if($(val).find('input').is(":checked") === true){
                        $(val).find('input').prop('checked', false);
                    }
                }
            });
        });
    });
}

function setQuickDispositionFilter(alertPrefix) {
    var filtersArray = JSON.parse(sessionStorage.getItem(alertPrefix + "filters_store"));
    $('.dynamic-filters').each(function (index) {
        if(filtersArray[$(this).val()]){
            $(this).prop('checked', true);
        } else {
            $(this).prop('checked', false);
        }
    });
}

function retainQuickDispositionFilter(listOfDisp, prefix, configId)
{
    var filterselect = false;
    $.each($(".disposition-ico"),function (idx,value) {
        $.each($(value).find("li"),function (id,val) {
            if (listOfDisp?.length === 0) {
                if ($(val).find('input').is(":checked") === true) {
                    $(val).find('input').prop('checked', false);
                }
            }
            $.each(listOfDisp,function (index, disposition) {
                if(disposition === $(val).find('input').val()){
                    if($(val).find('input').is(":checked") === false){
                        filterselect = true;
                        $(val).find('input').prop('checked', true);
                    }
                }
                else if(!listOfDisp.includes($(val).find('input').val())){
                    if($(val).find('input').is(":checked") === true){
                        $(val).find('input').prop('checked', false);
                    }
                }
            });
        });
    });
    var filterVal = [];
    var filterSto = {}
    $.each($(".disposition-ico").eq(0).find("li"),function (id,val) {
        if($(val).find('input').is(':checked') === true){
            filterVal.push($(val).find('input').val());
            filterSto[$(this).find('input').val()] = true;
        } else {
            filterSto[$(this).find('input').val()] = false;
        }
    });
    if(filterVal.length && filterselect) {
        sessionStorage.setItem(prefix + "filters_value", JSON.stringify(filterVal));
        sessionStorage.setItem(prefix + "filters_store", JSON.stringify(filterSto));
        sessionStorage.setItem(prefix + "id", configId);
    }
}

function checkIfPriorityEnabled() {
    if(typeof isPriorityEnabled === "boolean"){
        return isPriorityEnabled;
    } else if(typeof isPriorityEnabled === "string"){
        return isPriorityEnabled === "true";
    }
    return true;
}


var updateSignalOutcome=function (isDetailScreen) {
   $.ajax({
       type: "GET",
       url: "/signal/validatedSignal/signalOutcomes?id=" + $("#signalId").val(),
       async: false,
       dataType: 'json'
   }).done(function (data) {
            var allSignalOutcomes=data.allSignalOutcomes
            var signalOutcomes=data.signalOutcomes
            var existingSignals=data.existingSignals
            var selected;
            var result,showPreSelected=false;
            if(JSON.parse(mappingEnabled) && !isDetailScreen){
                if(typeof $("#signalId").val() ==="undefined"){
                    result=allSignalOutcomes
                }else {
                    if(existingSignals.length>0){
                        if(signalOutcomes.length==0){
                            result=mergeTwoArray(existingSignals,allSignalOutcomes)
                        }else{
                            result=mergeTwoArray(existingSignals,signalOutcomes)
                        }
                        if(signalOutcomes.length==1){
                            selected=mergeTwoArray(existingSignals,signalOutcomes)
                        }else{
                            selected=existingSignals
                        }
                        showPreSelected=true
                    }else{
                        if(signalOutcomes.length==0){
                            result=allSignalOutcomes
                        }else if(signalOutcomes.length==1){
                            result=signalOutcomes
                            showPreSelected=true
                            selected=signalOutcomes
                            setTimeout(function () {
                                saveSignalOutcomeValue(selected)
                            }, 1000)
                        }else if(signalOutcomes.length>1){
                            result=signalOutcomes
                        }
                    }
             }
           }else{
                    if(existingSignals!=""){
                        result=mergeTwoArray(existingSignals,allSignalOutcomes)
                        showPreSelected=true;
                        selected=existingSignals
                    }else{
                        result=allSignalOutcomes
                    }
            }
            $('#signalOutcome option').each(function() {
                $(this).remove();
            });
            if(result!=="")
            {
                $("#signalOutcome").select2({
                    data: result
                });
            }
            if(JSON.parse(showPreSelected))
            {
                if(selected!=="")
                {
                    $('#signalOutcome').val(selected);
                    $('#signalOutcome').select2().trigger('change');
                }
            }
    });
}
function mergeTwoArray(array1, array2) {
    var result_array = [];
    var arr = array1.concat(array2);
    var len = arr.length;
    var assoc = {};
    while(len--) {
        var item = arr[len];
        if(!assoc[item])
        {
            result_array.unshift(item);
            assoc[item] = true;
        }
    }
    return result_array;
}

var saveSignalOutcomeValue=function(value){
    $.ajax({
        type: "GET",
        url: window.location.origin+"/signal/validatedSignal/saveSignalOutcome?id=" + $("#signalId").val()+"&outcome="+value,
        async: false
    });
}

var validateSignalMapping = function (dispositionId) {
    var data = {
        signalId: $("#signalId").val(),
        targetDispositionId: dispositionId,
        signalOutComes: $("#signalOutcome").val()
    }
    $.ajax({
        type: "POST",
        url: window.location.origin + "/signal/validatedSignal/verifySignalOutcomeMapping",
        data: data,
        async: false,
        dataType: 'json'
    }).done(function (result) {
            if (JSON.parse(result.status) == false) {
                $.Notification.notify('warning', 'top right', "Warning", result.data, {hideDueIn: 7000});
            }
    });
    return true;
}

var disableDueDate = function () {
    if ($('#signal-history-table select option[value="Due Date"]:selected').length > 0) {
        $('#signal-history-table select option[value="Due Date"]:selected').parent().parent().parent().find('*').attr('disabled', true);
        $('#signal-history-table select option[value="Due Date"]:selected').parent().parent().parent().find("*").removeClass("mdi-check grey-2")
        $('#signal-history-table select option[value="Due Date"]:not(:selected)').remove();
        $('#signal-history-table select option[value="Due Date"]:selected').parent().parent().parent().find('*').on('contextmenu', function (e) {
            return false;
        });
    }
}
// Note- This is generalised code for draggable row in widgets.
var rowDraggable= function(shortingId){
    var draggedRow= ($(shortingId).find("tbody")).sortable();
    return draggedRow;
}

function showHideForegroundQuery(show) {
    if (JSON.parse(show)) {
        $(".forgroundQuery").show();
    } else {
        $(".forgroundQuery").hide();
    }
}

function showHideForegroundQueryCheckbox(show) {
    if (JSON.parse(show) && !$('#isMasterTemplateAlert').is(':checked')) {
        $("#foregroundQuery").prop("disabled", false);
        $("#foregroundQuery").css("pointer-events", "");
        $("#foregroundQuery").css("cursor", "");
    } else {
        $("#foregroundQuery").prop("checked", false);
        $("#foregroundQuery").prop("disabled", true);
        $("#foregroundQuery").css({
            "pointer-events": "none",
            "cursor": "not-allowed"
        });
    }
}

function addToSelectedCheckBox(ele) {
    var num = $(ele).closest('tr').index();
    var targetState = $("table#alertsDetailsTable tr:nth-child(" + (num + 1) + ") div.disposition ul").data('current-disposition');

    if ($(ele).is(':checked')) {
        selectedCheckBoxes.push(targetState)
    } else {
        for (var index = 0; index < selectedCheckBoxes.length; index++) {
            if (targetState === selectedCheckBoxes[index]) {
                selectedCheckBoxes.splice(index, 1);
                break
            }
        }
    }


}

$.extend(true, $.fn.DataTable.defaults, {
    layout: {
        topStart: ["pageLength", { search: { placeholder: "Search" } }],
        topEnd: null,
        bottomStart: [ "info", {paging: {type: "full_numbers"}}],
        bottomEnd: null,
    },
    processing: true,
    language: {
        loadingRecords: '',
        // url: APP_ASSETS_PATH + "i18n/dataTables_" + userLocale + ".json",
        processing: pvui.common.showPVLoader(),
    },
});

function insertFilterDropDown(selector, reference) {
    selector.insertAfter(reference);
    selector.css("visibility","visible");
}

function removeBorder(selector) {
    selector.siblings("span").css("box-shadow","none");
}


function handleQuotesForJson(str) {
    if (typeof str === "undefined" || typeof str === undefined) {
        return '';
    } else {
        return str.toString().replaceAll("\"", '\\\"');
    }
}

function handleSingleQuotesForJson(str) {
    if (typeof str === "undefined" || typeof str === undefined) {
        return '';
    } else {
        return str.toString().replace(/'/g, "&#39;");
    }
}
function escapeSpecialCharactersInId(id) {
    return id.replace(/[!"#$%&'()*+,.\/:;<=>?@[\\\]^_`{|}~]/g, '\\$&');
}
function resetSearchBox() {
    if (performance.navigation.type == 2) {
        location.reload(true);
    }
}

function releaseEnter(id, type) {
    var key = window.event.keyCode;
    if (key === 13) {
        if (type == "id") {
            document.getElementById(id).value = document.getElementById(id).value + "\n";
        } else if (type == "class") {
            document.getElementsByClassName(id)[0].value = document.getElementsByClassName(id)[0].value + "\n";
        }
        return false;
    } else {
        return true;
    }
}


function checkForDbDown(data) {
    if (typeof JSON.stringify(data) === 'undefined' || JSON.stringify(data) == '{}' || JSON.stringify(data) == '' || JSON.stringify(data) == null || (typeof JSON.stringify(data) !== 'undefined' && (JSON.stringify(data).data == "" || JSON.stringify(data) == null))) {
        return true;
    }
}

var getAlertType = function () {
    var urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('isFaers') != null && urlParams.get('isFaers') == 'true') {
        return ALERT_TYPE.SINGLE_CASE_ALERT_FAERS;
    } else if (urlParams.get('isVaers') != null && urlParams.get('isVaers') == 'true') {
        return ALERT_TYPE.SINGLE_CASE_ALERT_VAERS;
    } else if (urlParams.get('isVigibase') != null && urlParams.get('isVigibase') == 'true') {
        return ALERT_TYPE.SINGLE_CASE_ALERT_VIGIBASE;
    } else if (urlParams.get('isJader') != null && urlParams.get('isJader') == 'true') {
        return ALERT_TYPE.SINGLE_CASE_ALERT_JADER;
    } else if (urlParams.get('isAggregateAdhoc') != null && urlParams.get('isAggregateAdhoc') == 'true') {
        return ALERT_CONFIG_TYPE.SINGLE_CASE_ALERT_DRILL_DOWN_ADHOC;
    } else if (urlParams.get('isCaseSeries') != null && urlParams.get('isCaseSeries') == 'true') {
        return ALERT_CONFIG_TYPE.SINGLE_CASE_ALERT_DRILL_DOWN;
    } else {
        return applicationName;
    }
    return applicationName;
};

var clear_search_results = function () {
    $(".dictionaryItem").remove();
    $("#showProductSelection").html("");
    $(".productGroupValues").html("");
    resetDictionaryList(PRODUCT_DICTIONARY);
    clearAllText(PRODUCT_DICTIONARY);
    clearDicGroupText(PRODUCT_DICTIONARY);
    clearSearchInputs(-1, PRODUCT_DICTIONARY, undefined);
    clearAdditionalFilters(PRODUCT_DICTIONARY);
    $('#productGroupSelect').val(null).trigger('change');
    enableDisableProductGroupButtons(getDictionaryObject(PRODUCT_DICTIONARY).getValues());
};


function updateOrder(index, d, columnSeq, dir) {
    if (index !== -1 && d.order[0].column === 0) {
        const column = columnSeq.find(col => col.seq === index);
        if (column) {
            const columnIndex = d.columns.findIndex(col => col.data === column.name);
            d.order[0].column = columnIndex;
        }
        d.order[0].dir = dir;
    } else {
        index = d.order[0].column;
        dir = d.order[0].dir;
    }
    return [d, dir, index]
}

function addFractionSorting(){
    // jquery is implemented to sort 6th column of data table which has the data in form: 32/32, 432/432, 12/432
    // hence we needed this query manually sort 6th column
    jQuery.extend(jQuery.fn.dataTable.ext.type.order, {
        "fraction-pre": function (a) {
            //this is for the case when processed case is less than total cases then we get a div element with hyperlink
            //from there we have to extract the text
            var tempDiv = document.createElement("div");
            tempDiv.innerHTML = a;
            var textContent = tempDiv.textContent || tempDiv.innerText || "";

            // Extract the numerator
            var numerator = textContent.split('/')[0];
            return parseInt(numerator, 10);
        },
        "fraction-asc": function (a, b) {
            return a - b;
        },
        "fraction-desc": function (a, b) {
            return b - a;
        }
    });
}

$(document).on("shown.bs.dropdown", ".dropdown", function () {
    // calculate the required sizes, spaces
    var $ul = $(this).children(".dropdown-menu");
    var $button = $(this).children(".dropdown-toggle");
    var ulOffset = $(this).offset();
    var dtScrollBody = ".dt-scroll-body";
    var windowHeight= $(window).height();

    var spaceDown;
    if($ul.closest(dtScrollBody) && $ul.closest(".dataTable").length){
        var dTBodyOffsetTop= Math.round($(dtScrollBody).offset().top);
        var dtBodyHeight= Math.round($(dtScrollBody).height());
        var spaceBottom= windowHeight - (dTBodyOffsetTop + dtBodyHeight);
        spaceDown = $(window).scrollTop() + $(window).height() - Math.round(ulOffset.top + spaceBottom); //here 120 px is datatable pagination height and additional space from bottom.
    } else{
        spaceDown = $(window).scrollTop() + $(window).height() - Math.round(ulOffset.top ); //here 120 px is datatable pagination height and additional space from bottom.
    }
    if (spaceDown < $ul.height()) {
        $(this).addClass("dropup");
    } else {
        $(this).removeClass("dropup").addClass("dropdown");
    }
}).on("hidden.bs.dropdown", ".dropdown", function() {
    // always reset after close
    $(this).removeClass("dropup");
});