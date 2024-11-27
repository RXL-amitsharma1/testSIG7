//= require vendorUi/highcharts/highcharts-10.3.3/highcharts
//= require vendorUi/highcharts/highcharts-10.3.3/highcharts-3d
//= require vendorUi/highcharts/highcharts-10.3.3/highcharts-more
//= require vendorUi/highcharts/themes/grid-rx
$(function() {

    var executedIdArray = []
    var topicId = $("#topicId").val();

    var getChartResults = function(executedId, chartName) {
        $.ajax({
            url: 'getChartData?executedId='+executedId+"&topicId="+topicId+"&chartName="+chartName,
        })
            .done(function (result) {
                var chartId = "#"+chartName;
                $(chartId).highcharts(result.message.options);
            })
    }

    var initChartDataGen = function(topicId, chartName) {
        $.ajax({
            url: 'initiateChartDataGeneration?topicId='+topicId+"&chartName="+chartName,
        })
            .done(function (result) {
                var executedId = result.responseText.statusID;
                setTimeout(function() {
                    getChartResults(executedId, chartName);
                }, 60000);
            })
            .fail(function(err) {

                var err = JSON.parse(err.responseText);
                $("#"+chartName).find(".generateChart").html(err.responseText);
            })
    }

    var chartArray = ["severity", "ageGroup", "country", "gender"]

    var addedChartsCount = $("#addedChartsCount").val();

    if (addedChartsCount == '0') {
        for (var index = 0; index < chartArray.length; index++) {
            var chartName = chartArray[index];
            initChartDataGen(topicId, chartName)
        }
    } else {
        for (var index = 0; index < chartArray.length; index++) {
            var chartName = chartArray[index];
            $.ajax({
                url: 'getCurrentChartData?topicId='+topicId+"&chartName="+chartName,
                async: true,
            })
                .done(function (result) {
                    var chartData = result.chartData;
                    var chartId = "#"+result.chartName;
                    if (chartData != null) {
                        $(chartId).highcharts(chartData.options);
                    } else {
                        $(chartId).find('.generateChart').removeClass('hide');
                        $(chartId).find('.loadingChart').addClass('hide');
                    }
                })
        }
    }

    $(".generate-chart").on('click', function() {
        var chartName = $(this).attr('data-id');
        var renderingNotification = '<span style="margin-left: 40%; margin-top:30%">Rendering Chart....</span>'
        $("#"+chartName).html(renderingNotification);
        $("#"+chartName).css("height", 400);
        initChartDataGen(topicId, chartName)
    })

    $(".refresh-charts").on('click', function() {
        for (var index = 0; index < chartArray.length; index++) {
            var chartName = chartArray[index];
            initChartDataGen(signalId, chartName)
        }
    });
});