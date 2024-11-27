var spotfire = {
    fromDate: moment("1900-01-01 00:00:01"),
    endDate: new Date(),
    asOfDate: new Date(),
    productFamilyNames: null,
    fileName: null,
    serverUrl: null,
    type: 'drug',
    config: {
        server: null,
        path: null,
        domainName: null,
        version: null,
        templateFileName: null,
        user: null,
        libraryRoot: null,
        protocol: null,
        interval: null,
        hidingOptions: null,
        caseSeriesId: null,
        serverurl: null
    },
    file: null,
    init: function (){},

    generateReport: function () {
        // This is not generate report, this is actually load spotfire report

        document.cookie = "auth_token=" + "a_fake_token";

        var url = spotfire.serverUrl;
        var iframe = document.createElement('iframe');
        iframe.setAttribute('id', 'spotfire-report-iframe');
        iframe.setAttribute('width', '100%');
        iframe.setAttribute('frameborder', '0');

        function adjustIframeHeight() {
            var windowHeight = $(window).height();
            var iframeHeight = windowHeight - ($("#spotfirePanel").offset().top + 30);
            $("#spotfire-report-iframe").height(iframeHeight);
        }

        $(iframe).on('load', function() {
            adjustIframeHeight();
        });
        $(window).on('resize', adjustIframeHeight);

        iframe.src = url; //spotfire.serverUrl;
        var spotfirePanel = document.getElementById('spotfirePanel')
        spotfirePanel.innerHTML = "";
        spotfirePanel.appendChild(iframe);
    },

    bindUiInputs: function () {
        $("#caseSeriesId").on("change", function () {
            spotfire.disableEnableDateOptions($(this).val() != '')
        }).trigger("change");

        $("#tag").on('input', function () {
            spotfire.setFullFileName()
        });
        spotfire.initDatepickers();
        $("#spotfireDrug").on('click', function () {
            spotfire.setFullFileName();
        });

        $("#spotfireVaccine").on('click', function () {
            spotfire.setFullFileName();
        });
    },

    keepLive: function (interval) {
        window.setInterval(function () {
            $.ajax({
                url: 'keepAlive',
            })
                .done(function (data) {})
        }, interval);
    },

    initDatepickers: function () {
        $("#spotfireFromDate").datepicker({
            allowPastDates: true,
            date: spotfire.fromDate,
            momentConfig: {
                culture: userLocale,
                tz: userTimeZone,
                format: DEFAULT_DATE_DISPLAY_FORMAT
            }
        }).on('changed.fu.datepicker dateClicked.fu.datepicker', function (evt, date) {
            spotfire.fromDate = date;
            spotfire.setFullFileName();
        });
        $("#spotfireEndDate").datepicker({
            allowPastDates: true,
            date: spotfire.endDate,
            momentConfig: {
                culture: userLocale,
                tz: userTimeZone,
                format: DEFAULT_DATE_DISPLAY_FORMAT
            }
        }).on('changed.fu.datepicker dateClicked.fu.datepicker', function (evt, date) {
            spotfire.endDate = date;
            spotfire.setFullFileName();
        });


        $("#spotfireAsOfDate").datepicker({
            restrictDateSelection: false,
            date: spotfire.asOfDate,
            momentConfig: {
                culture: userLocale,
                format: DEFAULT_DATE_DISPLAY_FORMAT
            }

        }).on('changed.fu.datepicker dateClicked.fu.datepicker', function (evt, date) {
            spotfire.asOfDate = date;
            spotfire.setFullFileName();
        });

        $("input[name='spotfireType']").on('change', function (evt) {
            spotfire.type = $("input[name='spotfireType']:checked").val();
        });
    },

    composeParams: function () {
        return "&pc=" + spotfire.config.protocol + "&sr=" + spotfire.config.server + "&p=" + spotfire.config.path +
            "&v=" + spotfire.config.version + "&dn=" + spotfire.config.domainName + "&f=" +
            spotfire.file + "&u=" + spotfire.config.user;
    },

    composefileName: function () {
        spotfire.productFamilyNames = _.map($("#productFamilyIds").select2('data'), function (x) {
            return x.text
        });
        if (spotfire.productFamilyNames[0] == '') {
            var filteredProductFamilyNames = spotfire.productFamilyNames.filter(function (el) {
                return el != '';
            });
            spotfire.productFamilyNames = filteredProductFamilyNames;

            var filtered = $("#productFamilyIds").filter(function (el) {
                return el != '';
            });
            $("#productFamilyIds").val(filtered);
        }

        var regex = /[\\(),'"!@#$%&*/]/g;
        $.each(spotfire.productFamilyNames, function (i, v) {
            if (v.match(regex) != null) {
                v = v.replace(regex, '_');
                spotfire.productFamilyNames[i] = v;
            }
        });
        var startDate = moment($('#fromDate').val(), DEFAULT_DATE_DISPLAY_FORMAT).format(DEFAULT_DATE_DISPLAY_FORMAT);
        var endDate = moment($('#endDate').val(), DEFAULT_DATE_DISPLAY_FORMAT).format(DEFAULT_DATE_DISPLAY_FORMAT);
        var asofDate = moment($('#asOfDate').val(), DEFAULT_DATE_DISPLAY_FORMAT).format(DEFAULT_DATE_DISPLAY_FORMAT);
        var radioButtonId = $('input:radio[name=type]:checked').attr("id");
        var radioButtonLabel = $('input[name="type"]:checked').val();
        var caseSeries = $('#caseSeriesId').select2("val");
        var caseSeriesName = "";
        if (caseSeries !== "") {
            caseSeriesName = ("_CaseSeries_" + replaceAll($('#select2-caseSeriesId-container').html(), " ", "-"));
        }

        if (typeof spotfire.productFamilyNames !== 'undefined' && spotfire.productFamilyNames && spotfire.productFamilyNames.length > 0) {
            if (caseSeriesName !== "") {
                return (_.reduce(spotfire.productFamilyNames, function (str, name) {
                        return str + "_" + name
                    }, "") + caseSeriesName + "_" +
                    capitalizeFirstLetter(radioButtonLabel)).substring(1);
            }
            return (_.reduce(spotfire.productFamilyNames, function (str, name) {
                    return str + "_" + name
                }, "") +
                "_" + startDate + "_" + endDate + "_AoD_" + asofDate + "_" +
                capitalizeFirstLetter(radioButtonLabel)).substring(1);
        } else
            return "";
    },

    log: function () {
        console.log("fromDate:" + formatDate(spotfire.fromDate));
        console.log('endDate:' + formatDate(spotfire.endDate));
        console.log('asOfDate:' + formatDate(spotfire.asOfDate));
        // console.log('server:' + spotfire.config.server);
        // console.log('version:' + spotfire.config.version);
        // console.log('path:' + spotfire.config.path);
        console.log('productFamilyNames:' + spotfire.productFamilyNames);
        console.log('fileName:' + spotfire.fileName);
        console.log('file:' + spotfire.file);
        console.log('type:' + spotfire.type);
    },

    openSpotfireReport: function (fileName, libraryRoot, cookie, serverUrl, authToken) {
        spotfire.file = libraryRoot + "/" + fileName;
        document.cookie = cookie
        spotfire.serverUrl = unescapeHTML(serverUrl);
        spotfire.generateReport();
        document.cookie = "auth_token=" + authToken;
        return spotfire;
    },

    setFullFileName: function () {
        spotfire.fileName = spotfire.composefileName();
        $('#fullFileName').val(spotfire.fileName);
    },

    disableEnableDateOptions: function (disable) {
        if (disable) {
            $("#spotfireFromDate").datepicker('disable');
            $("#spotfireEndDate").datepicker('disable');
            $("#spotfireAsOfDate").datepicker('disable');
            $('#fromDate').val('');
            $('#endDate').val('');
            $('#asOfDate').val('');
        } else {
            $("#spotfireFromDate").datepicker({
                allowPastDates: true,
                setDate: spotfire.fromDate
            }).datepicker('enable');

            $("#spotfireEndDate").datepicker({
                allowPastDates: true,
                setDate: spotfire.fromDate
            }).datepicker('enable');

            $("#spotfireAsOfDate").datepicker({
                allowPastDates: true,
                setDate: spotfire.fromDate
            }).datepicker('enable');

            if ($('#fromDate').val() == '') {
                $('#fromDate').val(spotfire.fromDate.format(DEFAULT_DATE_DISPLAY_FORMAT));
            }

            if ($('#endDate').val() == '') {
                $('#endDate').val(moment(spotfire.endDate).format(DEFAULT_DATE_DISPLAY_FORMAT));
            }

            if ($('#asOfDate').val() == '') {
                $('#asOfDate').val(moment(spotfire.asOfDate).format(DEFAULT_DATE_DISPLAY_FORMAT));
            }
        }
        spotfire.setFullFileName()
    }
};

function formatDate(date) {
    return moment(date).format("DD-MM-YYYY");
}

function replaceAll(str, find, replace) {
    if (typeof str != "undefined") {
        return str.replace(new RegExp(find, 'g'), replace);
    } else {
        return "";
    }

}

function bindProdsSelect2WithUrl(selector, queryUrl, data) {
    var select2Element = selector.select2({
        minimumInputLength: 0,
        multiple: true,
        placeholder: $.i18n._('selectOne'),
        allowClear: true,
        width: "100%",
        ajax: {
            quietMillis: 250,
            dataType: "json",
            url: queryUrl,
            data: function (params) {
                return {
                    dataSource: ($('#selectedDatasource') ? $('#selectedDatasource').val() : ''),
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
    if (data) {
        var option = new Option(data.text, data.id, true, true);
        selector.append(option).trigger('change.select2');
    }
    return select2Element
}

function productSearch() {
    $productFamilyIds = $("#productFamilyIds");
    var data = {};
    bindProdsSelect2WithUrl($productFamilyIds, ajaxProductFamilySearchUrl, data);
}

$(function () {
    var selectProductFamily = $("#productFamilyIds").select2();
    $("#caseSeriesId").select2();
    if ((typeof productFamilyJson) != "undefined") {
        $("#productFamilyIds").select2().val(productFamilyJson).trigger('change');
    }
    selectProductFamily.on('change', function () {
        spotfire.setFullFileName();
    });

    $('#spotfireFromDate').datepicker({
        date: $("#spotfireFromDate").val() ? new Date($("#spotfireFromDate").val()) : null,
        allowPastDates: true,
        momentConfig: {
            culture: userLocale,
            tz: userTimeZone,
            format: DEFAULT_DATE_DISPLAY_FORMAT
        }
    });

    $('#spotfireEndDate').datepicker({
        date: $("#spotfireEndDate").val() ? new Date($("#spotfireEndDate").val()) : null,
        allowPastDates: true,
        momentConfig: {
            culture: userLocale,
            tz: userTimeZone,
            format: DEFAULT_DATE_DISPLAY_FORMAT
        }
    });

    $('#spotfireAsOfDate').datepicker({
        date: $("#spotfireAsOfDate").val() ? new Date($("#spotfireAsOfDate").val()) : null,
        allowPastDates: true,
        momentConfig: {
            culture: userLocale,
            tz: userTimeZone,
            format: DEFAULT_DATE_DISPLAY_FORMAT
        }
    });

    productSearch();
});