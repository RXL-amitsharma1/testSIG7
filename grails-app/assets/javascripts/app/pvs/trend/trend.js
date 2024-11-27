var signal = signal || {}

signal.trend = (function() {

    var graph = null;

    var showTrends = function (fetchTrendUrl, alertId, alertType, chartType, chartLabel, frequency, isFaers) {
        var url = fetchTrendUrl + "&alertId=" + alertId + "&alertType=" + alertType + "&chartType=" + chartType + "&frequency=" + frequency + "&isFaers=" + isFaers;
        $.ajax({
            url: url,
        })
            .done(function (result) {
                if (typeof result == "string" && result != "") {
                    $(".trendRow").removeClass("hide");
                    $(".trendNotification").addClass("hide");

                    if (typeof graph != "undefined" && graph != null) {
                        graph.destroy();
                    }

                    graph = new Dygraph (
                        document.getElementById("stock_div"),
                        result,
                        {
                            labels: ['Retrieval Date', 'Actual', 'Expected', 'Probability'],
                            series: {
                                'Probability': {
                                    axis: 'y2'
                                }
                            },
                            axes: {
                                y2: {
                                    // set axis-related properties here
                                    labelsKMB: true,
                                    strokePattern: Dygraph.DASHED_LINE
                                }
                            },
                            ylabel: 'Expected',
                            y2label: 'Probability',
                            // legend: legendFormatter,
                            animatedZooms: true,
                            title: "Trend of ["+ chartLabel + "] for ["+ frequency + "] frequency",
                            showRoller: true,
                            showRangeSelector: true,
                            colors: ["#008000", "#FF0000", "#0000FF"]
                        }
                    );

                    var trendDataObj = [];
                    var trendDataArray = result.split("\n");
                    for (var index = 0; index < trendDataArray.length; index++) {
                        var singleobj = trendDataArray[index];
                        var tokens = singleobj.split(',');

                        var obj = {
                            actualCount: tokens[1],
                            retrieveDate: tokens[0],
                            expectedCount: tokens[2],
                            probability: tokens[3]
                        };
                        trendDataObj.push(obj);
                    }

                    var trend_data_content = signal.utils.render('trendDataTable', {trendData: trendDataObj});
                    $("#trend-div").html(trend_data_content);
                } else {
                    $(".trendRow").addClass("hide");
                    $(".trendNotification").removeClass("hide");
                }
            })
            .fail(function () {});
    };

    return {
        showTrends : showTrends
    }
})()