DATE_FMT_TZ = "YYYY-MM-DD";
var index=5;
var dir='desc';
var latestSearchBy = null;
var isFilter = false;

$(function () {

    var getFilterIcon = function () {
        var right_bar = $('#statusReportTable_filter').parent();
        right_bar.append('<a href="#" id="toggle-column-filters" title="Enable/Disable Filters" class="pv-ic"><i class="fa fa-filter" aria-hidden="true"></i></a>');
    };

    var init = function () {
        $(document).on('click', 'input#select-all', function () {
            $(".copy-select").prop('checked', this.checked);
        });
    };
    init();

    var signalListTable = $('#statusReportTable').DataTable({
        "responsive": true,
        processing: true,
        serverSide: true,
        "bLengthChange": true,
        "iDisplayLength": 50,
        "aLengthMenu": [[50, 100, 200, 500], [50, 100, 200, 500]],
        "bProcessing": true,
        layout: {
            topStart: 'search',
            topEnd: null,
            bottomStart: ['pageLength', 'info', {paging: {type: "full_numbers"}}],
            bottomEnd: null,
        },
        initComplete: function (){
            init_filter(signalListTable);
        },
        customProcessing: true,
        "oLanguage": {
            "url": "../assets/i18n/dataTables_" + userLocale + ".json",
            "oPaginate": {
                "sFirst": "<i class='mdi mdi-chevron-double-left'></i>", // This is the link to the first page
                "sPrevious": "<i class='mdi mdi-chevron-left'></i>", // This is the link to the previous page
                "sNext": "<i class='mdi mdi-chevron-right'></i>", // This is the link to the next page
                "sLast": "<i class='mdi mdi-chevron-double-right'></i>" // This is the link to the last page
            },
            "sLengthMenu": "Show _MENU_",
            "sZeroRecords": "No data available in table", "sEmptyTable": "No data available in table",

        },
        "ajax": {
            "url": signalListUrl(),
            "cache": false,
            data: function (args) {
                args["isFilter"]=isFilter;
                latestSearchBy = args;
                return {
                    "args": JSON.stringify(args)
                };
            },
        },
        "aoColumns": [

            {
                "mData": "id",
                "name" : "id",
                "sortable": false,
                "visible": false,
                'className': 'col-min-100 col-max-100'
            },
            {
                "mData": "uniqueIdentifier",
                "name" : "uniqueIdentifier",
                'className': 'col-min-200 col-max-200 cell-break',
                "visible": true
            },

            {
                "mData": "validRecordCount",
                "name": "validRecordCount",
                'className':'col-min-150 col-max-150 cell-break ',
                "bSearchable": false,
                "mRender" : function(data, type, row) {
                    return (row.validRecordCount==null?0:row.validRecordCount)+"/"+(row.count==null?0:row.count);
                }
            },
            {
                "mData": "uploadedAt",
                "name" : "uploadedAt",
                'className':'col-min-200 col-max-200 cell-break',
                "visible": true,
                "mRender" : function(data, type, row) {
                    return row.uploadedAt==null?"":(row.uploadedAt.replace('T',' ').replace('Z',''));
                }
            },
            {
                "mData": "addedBy",
                "name" : "addedBy",
                'className':'col-min-200 col-max-200 cell-break'
            },
            {
                "mData": "apiStatus",
                "name": "apiStatus",
                "bSearchable": false,
                "sortable": false,
                'className':'col-min-200 col-max-200',
                "mRender" : function(data, type, row) {
                    var apiStatus = ""
                    if(row.validRecordCount==null || row.validRecordCount==0) {
                        apiStatus = '<i class="fa fa-exclamation-circle fa-lg es-error popoverMessage" aria-hidden="true" style="color:red; cursor: pointer;" title="'+(row.info==null?'All records are not processed!':row.info)+'"></i> &nbsp;';
                        apiStatus = apiStatus+'<a href="/signal/productGroupStatus/exportProductGroupDatas?productGroupsId='+row.id+'&pid='+row.uniqueIdentifier+'" class="apiexport mdi mdi-export" aria-hidden="true" title="Export to Excel" ></a>';
                    } else if(row.count==row.validRecordCount) {
                        apiStatus = '<i class="fa fa-check" aria-hidden="true" style="color:green;" title="All Product Groups data processed successfully "></i> &nbsp;';
                        apiStatus = apiStatus+'<a href="/signal/productGroupStatus/exportProductGroupDatas?productGroupsId='+row.id+'&pid='+row.uniqueIdentifier+'" class="apiexport mdi mdi-export" aria-hidden="true" title="Export to Excel" ></a>';
                    } else if(row.count>row.validRecordCount && row.validRecordCount>0) {
                        apiStatus = '<i class="fa fa-exclamation-circle fa-lg es-error popoverMessage" aria-hidden="true" style="color:red; cursor: pointer;" title="'+row.info+'"></i> &nbsp;';
                        apiStatus = apiStatus+'<a href="/signal/productGroupStatus/exportProductGroupDatas?productGroupsId='+row.id+'&pid='+row.uniqueIdentifier+'" class="apiexport mdi mdi-export" aria-hidden="true" title="Export to Excel" ></a>';
                    }
                    return apiStatus;
                }
            }
        ],
        scrollX: true,
        scrollY: "calc(100vh - 261px)",
        columnDefs: [{
            "targets": '_all',
            "render": $.fn.dataTable.render.text(),
            orderSequence: ['desc', 'asc']
        }]
    });


    actionButton('#statusReportTable');
    var init_filter = function (data_table) {
        yadcf.init(data_table, [
            {column_number: 1, filter_type: 'text', filter_reset_button_text: false, filter_delay: 600, filter_default_label: ''},
            {column_number: 2, filter_type: "text", filter_reset_button_text: false, filter_delay: 600, filter_default_label: ''},
            {column_number: 3, filter_type: "text", filter_reset_button_text: false, filter_delay: 600, filter_default_label: ''},
            {column_number: 4, filter_type: "text", filter_reset_button_text: false, filter_delay: 600, filter_default_label: ''}
        ]);
        $(".yadcf-filter").removeAttr('placeholder');
        $('.yadcf-filter-wrapper').hide();
    };


    $("#toggle-column-filters").on('click', function () {
        showHideFilter();
    });

    function showHideFilter() {
        var ele = $("#statusReportTable_wrapper").find('.yadcf-filter-wrapper');
        var inputEle = $('.yadcf-filter');
        if (ele.is(':visible')) {
            ele.css('display','none');
            isFilter = false;
        } else {
            ele.css('display','block');
            isFilter = true;
            inputEle.first().focus();
        }
    }
    showHideFilter();

    function signalListUrl() {
        if(callingScreen == 'dashboard'){
            return VALIDATED.signalListUrl + "?callingScreen=dashboard";
        }else{
            return VALIDATED.signalListUrl;
        }
    }

    if(iconSeq!="" && iconSeq!=null){
        $.each(jQuery.parseJSON(iconSeq), function (key, value) {
            if (value) {
                $(".pin-unpin-rotate[data-id='" + key + "']").attr('title', 'Unpin');
                $(".pin-unpin-rotate[data-id='" + key + "']").addClass("active-pin");
                $(key).show();
            }
        });
    }

    $("#reportIconMenu").mouseover(function () {
        $(".ul-ddm").show();
    });
    $("#reportIconMenu").mouseout(function() {
        $(".ul-ddm").hide();
    });

    $("#downloadCompleteBatchHref").on("mouseover", function (event) {
        var href = VALIDATED.allProductGroupsStatusDownloadLink;
        href = href + "?searchRequestString="+ encodeURI(JSON.stringify({searchParam: latestSearchBy}));
        $(this).attr("href", href);
    });


});

var downloadProductGroupsData = function (productGroupsId,uniqueIdentifier) {
    $.ajax({
        url: VALIDATED.allProductGroupsDataDownloadLink+'?productGroupsId='+productGroupsId,
        type: 'POST',
        contentType: "application/json; charset=utf-8",
        data: {},
    })
        .done(function (data) {
            var blob = new Blob([data], { type: "application/vnd.ms-excel" });
            var URL = window.URL || window.webkitURL;
            var downloadUrl = URL.createObjectURL(blob);
            var a = document.createElement('a')
            a.setAttribute('href', downloadUrl)
            a.setAttribute('download', uniqueIdentifier+'.csv')
            a.trigger('click')
        });
}

$("#searchRequestString").on("mouseover", function (event) {
    var href = VALIDATED.allProductGroupsStatusDownloadLink;
    href = href + "?searchRequestString="+ encodeURI(JSON.stringify({searchParam: latestSearchBy}));
    $(this).attr("href", href);
});
function downLoadCompleteProductGroupsStatus() {
    $.ajax({
        url: VALIDATED.allProductGroupsStatusDownloadLink,
        type: 'POST',
        contentType: "application/json; charset=utf-8",
        data: JSON.stringify({searchParam: latestSearchBy}),
    })
        .done(function (data) {
            var blob = new Blob([data], { type: "application/vnd.ms-excel" });
            var URL = window.URL || window.webkitURL;
            var downloadUrl = URL.createObjectURL(blob);
            var a = document.createElement('a')
            a.setAttribute('href', downloadUrl)
            a.setAttribute('download', 'Batch_Lot_Status.csv')
            a.trigger('click')
        });
}
var sortIconHandler = function () {
    var thArray = $('#statusReportTable').DataTable().columns().header();
    $.each(thArray, function (currentIndex, element) {
    });
};

