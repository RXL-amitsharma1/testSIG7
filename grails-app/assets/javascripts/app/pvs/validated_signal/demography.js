//= require vendorUi/highcharts/highcharts-10.3.3/highcharts
//= require vendorUi/highcharts/highcharts-10.3.3/highcharts-3d
//= require vendorUi/highcharts/highcharts-10.3.3/highcharts-more
//= require vendorUi/highcharts/themes/grid-rx
$(function() {

    var bar_chart = function(chartId, series, category, title) {
        $(chartId).highcharts({
            chart: {
                type: 'bar',
                height: 300
            },
            title: {
                text: title
            },
            xAxis: {
                categories: category,
                title: {
                    text: ""
                }
            },
            legend: {
                enabled: false
            },
            yAxis: {
                title: {
                    text: ""
                }
            },
            plotOptions: {
                bar: {
                    dataLabels: {
                        enabled: true
                    }
                }
            },
            credits: {
                enabled: false
            },
            series: series
        });
    }

    bar_chart("#age1", [{data: [107]}, {data: [133]}, {data: [105]}], ['', '', ''], "Age");
    bar_chart("#gender1", [{data: [117]}, {data: [133]}, {data: [115]}], ['', '', ''], "Gender");
    bar_chart("#race1", [{data: [207]}, {data: [233]}, {data: [205]}], ['', '', ''], "Race");
    bar_chart("#age2", [{data: [107]}, {data: [133]}, {data: [105]}], ['', '', ''], "Age");
    bar_chart("#gender2", [{data: [117]}, {data: [133]}, {data: [115]}], ['', '', ''], "Gender");
    bar_chart("#race2", [{data: [207]}, {data: [233]}, {data: [205]}], ['', '', ''], "Race");
    bar_chart("#age3", [{data: [107]}, {data: [133]}, {data: [105]}], ['', '', ''], "Age");
    bar_chart("#gender3", [{data: [117]}, {data: [133]}, {data: [115]}], ['', '', ''], "Gender");
    bar_chart("#race3", [{data: [207]}, {data: [233]}, {data: [205]}], ['', '', ''], "Race");

    var column_chart = function(chartId, series, category, title) {

        $(chartId).highcharts({
            chart: {
                type: 'column',
                height: 300
            },
            title: {
                text: title
            },
            legend: {
                enabled: false
            },
            xAxis: {
                categories: category
            },
            yAxis: {
                min: 0
            },
            tooltip: {
                headerFormat: '<span style="font-size:10px">{point.key}</span><table>',
                pointFormat: '<tr><td style="color:{series.color};padding:0">{series.name}: </td>' +
                             '<td style="padding:0"><b>{point.y:.1f} mm</b></td></tr>',
                footerFormat: '</table>',
                shared: true,
                useHTML: true
            },
            plotOptions: {
                column: {
                    pointPadding: 0.2,
                    borderWidth: 0
                }
            },
            series: [{
                data: [10,2,4,5,19]
            }]
        });
    }

    column_chart("#weight1", [{data: [10,2,4,5,19]}], ['0-20', '21-40', '41-50', '51-70', '70-Higher'], "Weight");
    column_chart("#height1", [{data: [10,2,4,5,19]}], ['1', '2', '3', '4', '5-Higher'], "Height");
    column_chart("#weight2", [{data: [10,2,4,5,19]}], ['0-20', '21-40', '41-50', '51-70', '70-Higher'], "Weight");
    column_chart("#height2", [{data: [10,2,4,5,19]}], ['1', '2', '3', '4', '5-Higher'], "Height");
    column_chart("#weight3", [{data: [10,2,4,5,19]}], ['0-20', '21-40', '41-50', '51-70', '70-Higher'], "Weight");
    column_chart("#height3", [{data: [10,2,4,5,19]}], ['1', '2', '3', '4', '5-Higher'], "Height");
});