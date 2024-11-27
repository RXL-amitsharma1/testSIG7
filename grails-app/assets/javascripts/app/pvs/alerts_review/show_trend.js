//= require app/pvs/common/rx_common.js
//= require dygraphs/dygraph.min.js
//= require app/pvs/trend/trend

$(function () {

    $("#trendChartLoadingIcon").removeClass("hide");
    $("#trendChartRow").addClass("hide");
    $("#trendDataRow").addClass("hide");
    signal.trend.showTrends(fetchTrendUrl, productName, preferredTerm);
    $("#trendChartLoadingIcon").addClass("hide");
    $("#trendDataRow").removeClass("hide");
    $("#trendChartRow").removeClass("hide");

})