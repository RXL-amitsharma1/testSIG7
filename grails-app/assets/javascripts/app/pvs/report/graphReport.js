//= require vendorUi/highcharts/highcharts-10.3.3/highcharts
//= require vendorUi/highcharts/highcharts-10.3.3/highcharts-3d
//= require vendorUi/highcharts/highcharts-10.3.3/highcharts-more
//= require vendorUi/highcharts/themes/grid-rx

var signal = signal || {};

signal.graphReport = (function () {

    // Removes the default space as the thousands separator in Highcharts
    Highcharts.defaultOptions.lang.thousandsSep = ''

    var prepareReactionGroupChart = function (chartSeries) {
        $('#age-and-gender-group-chart').highcharts({
            chart: {
                type: 'bar'
            },
            title: {
                text: ''
            },
            xAxis: {
                //The potential x-axis values will come from server.
                categories: chartSeries['ageAndGenderGroupChart']['categories'],
                title: {
                    text: 'Age and Gender Group'
                }
            },
            yAxis: {
                min: 0,
                title: {
                    text: 'Number of Individual Cases'
                },
                allowDecimals: false
            },
            tooltip: {
                valueSuffix: ''
            },
            credits: {
                enabled: false
            },
            legend: {
                reversed: false
            },
            plotOptions: {
                series: {
                    stacking: 'normal'
                }
            },
            series: chartSeries['ageAndGenderGroupChart']['data']
        });

        $('#reporter-group-chart').highcharts({
            chart: {
                type: 'bar'
            },
            title: {
                text: ''
            },
            xAxis: {
                //The potential x-axis values will come from server.
                categories: chartSeries['reporterGroupChart']['categories'],
                title: {
                    text: 'Reporter Group'
                }
            },
            yAxis: {
                min: 0,
                title: {
                    text: 'Number of Individual Cases'
                },
                allowDecimals: false
            },
            tooltip: {
                valueSuffix: ''
            },
            plotOptions: {
                bar: {
                    dataLabels: {
                        enabled: true
                    }
                }
            },
            legend: {
                enabled: false
            },
            credits: {
                enabled: false
            },
            series: [{
                // Potential data series values, will come from server.
                color: '#6EB010',
                data: chartSeries['reporterGroupChart']['data']
            }]
        });

        $('#geographic-region-chart').highcharts({
            chart: {
                type: 'bar'
            },
            title: {
                text: ''
            },
            xAxis: {
                //The potential x-axis values will come from server.
                categories: chartSeries['geographicRegionChart']['categories'],
                title: {
                    text: 'Region Group'
                }
            },
            yAxis: {
                min: 0,
                title: {
                    text: 'Number of Individual Cases'
                },
                allowDecimals: false
            },
            tooltip: {
                valueSuffix: ''
            },
            plotOptions: {
                bar: {
                    dataLabels: {
                        enabled: true
                    }
                }
            },
            legend: {
                enabled: false
            },
            credits: {
                enabled: false
            },
            series: [{
                // Potential data series values, will come from server.
                data: chartSeries['geographicRegionChart']['data']
            }]
        });

    };

    var prepareReactionChart = function (chartSeries) {

        $('#reaction-chart').highcharts({
            chart: {
                type: 'bar'
            },
            title: {
                text: ''
            },
            xAxis: {
                //The potential x-axis values will come from server.
                categories: chartSeries['reactionChart']['categories'],
                title: {
                    text: 'Age and Gender Group'
                }
            },
            yAxis: {
                min: 0,
                title: {
                    text: 'Number of Individual Cases'
                },
                allowDecimals: false
            },
            tooltip: {
                valueSuffix: ''
            },
            credits: {
                enabled: false
            },
            legend: {
                reversed: false
            },
            plotOptions: {
                series: {
                    stacking: 'normal'
                }
            },
            series: chartSeries['reactionChart']['data']
        });

        $('#reporter-reaction-chart').highcharts({
            chart: {
                type: 'bar'
            },
            title: {
                text: ''
            },
            xAxis: {
                //The potential x-axis values will come from server.
                categories: chartSeries['reporterReactionChart']['categories'],
                title: {
                    text: 'Reporter Group'
                }
            },
            yAxis: {
                min: 0,
                title: {
                    text: 'Number of Individual Cases'
                },
                allowDecimals: false
            },
            tooltip: {
                valueSuffix: ''
            },
            plotOptions: {
                bar: {
                    dataLabels: {
                        enabled: true
                    }
                }
            },
            legend: {
                enabled: false
            },
            credits: {
                enabled: false
            },
            series: [{
                // Potential data series values, will come from server.
                color: '#6EB010',
                data: chartSeries['reporterReactionChart']['data']
            }]
        });

        $('#reaction-outcome-chart').highcharts({
            chart: {
                type: 'bar'
            },
            title: {
                text: ''
            },
            xAxis: {
                //The potential x-axis values will come from server.
                categories: chartSeries['reactionOutcomeChart']['categories'],
                title: {
                    text: 'Outcome'
                }
            },
            yAxis: {
                min: 0,
                title: {
                    text: 'Number of Individual Cases'
                },
                allowDecimals: false
            },
            tooltip: {
                valueSuffix: ''
            },
            plotOptions: {
                bar: {
                    dataLabels: {
                        enabled: true
                    }
                }
            },
            legend: {
                enabled: false
            },
            credits: {
                enabled: false
            },
            series: [{
                // Potential data series values, will come from server.
                color: '#605858',
                data: chartSeries['reactionOutcomeChart']['data']
            }]
        });

    };

    var prepareIndividualCasesChart = function (chartSeries) {
        $('#individualCasesByRegionChart').highcharts({
            chart: {
                type: 'bar',
                height: "100%"
            },
            title: {
                text: ''
            },
            xAxis: {
                categories: chartSeries['individualCasesByRegionChart']['categories'],
                title: {
                    text: 'Occurrence Country'
                }
            },
            yAxis: {
                min: 0,
                title: {
                    text: 'Number of Individual Cases'
                },
                allowDecimals: false
            },
            tooltip: {
                valueSuffix: ''
            },
            plotOptions: {
                bar: {
                    dataLabels: {
                        enabled: true
                    }
                }
            },
            legend: {
                enabled: false
            },
            credits: {
                enabled: false
            },
            series: [{
                // Potential data series values, will come from server.
                color: '#6EB010',
                data: chartSeries['individualCasesByRegionChart']['data']
            }]
        });
        $('#individualCasesByRegionTable tbody').html(getTableRows(chartSeries['individualCasesByRegionChart']));
        $('#individualCasesByAgeChart').highcharts({
            chart: {
                type: 'bar',
                height: 372
            },
            title: {
                text: ''
            },
            xAxis: {
                //The potential x-axis values will come from server.
                categories: chartSeries['individualCasesByAgeChart']['categories'],
                title: {
                    text: 'Age Group'
                }
            },
            yAxis: {
                min: 0,
                title: {
                    text: 'Number of Individual Cases'
                },
                allowDecimals: false
            },
            tooltip: {
                valueSuffix: ''
            },
            plotOptions: {
                bar: {
                    dataLabels: {
                        enabled: true
                    }
                }
            },
            legend: {
                enabled: false
            },
            credits: {
                enabled: false
            },
            series: [{
                // Potential data series values, will come from server.
                data: chartSeries['individualCasesByAgeChart']['data']
                // data: [1062, 3, 2, 4, 15, 545, 35, 2]
            }]
        });
        $('#individualCasesByAgeTable tbody').html(getTableRows(chartSeries['individualCasesByAgeChart']));
        $('#individualCasesByGenderChart').highcharts({
            chart: {
                type: 'bar',
                height: 372
            },
            title: {
                text: ''
            },
            xAxis: {
                //The potential x-axis values will come from server.
                categories: chartSeries['individualCasesByGenderChart']['categories'],
                title: {
                    text: 'Gender'
                }
            },
            yAxis: {
                min: 0,
                title: {
                    text: 'Number of Individual Cases'
                },
                allowDecimals: false
            },
            tooltip: {
                valueSuffix: ''
            },
            plotOptions: {
                bar: {
                    dataLabels: {
                        enabled: true
                    }
                }
            },
            legend: {
                enabled: false
            },
            credits: {
                enabled: false
            },
            series: [{
                // Potential data series values, will come from server.
                data: chartSeries['individualCasesByGenderChart']['data']
                // data: [1062, 3, 2, 4, 15, 545, 35, 2]
            }]
        });
        $('#individualCasesByGenderTable tbody').html(getTableRows(chartSeries['individualCasesByGenderChart']));
    };

    var prepareIndividualCasesByReactionGroupChart = function (chartSeries) {

        $('#reactionGroupByRegionChart').highcharts({
            chart: {
                type: 'bar',
                width: 1182,
                height: 600
            },
            title: {
                text: ''
            },
            xAxis: {
                //The potential x-axis values will come from server.
                categories: chartSeries['reactionGroupByRegionChart']['categories'],
                title: {
                    text: 'Reaction Groups'
                }
            },
            yAxis: {
                min: 0,
                title: {
                    text: 'Number of Individual Cases'
                },
                allowDecimals: false
            },
            tooltip: {
                valueSuffix: ''
            },
            credits: {
                enabled: false
            },
            legend: {
                reversed: false
            },
            plotOptions: {
                series: {
                    stacking: 'normal'
                }
            },
            series: chartSeries['reactionGroupByRegionChart']['data']
        });

        $('#reactionGroupByReporterChart').highcharts({
            chart: {
                type: 'bar',
                width: 1182,
                height: 600
            },
            title: {
                text: ''
            },
            xAxis: {
                //The potential x-axis values will come from server.
                categories: chartSeries['reactionGroupByReporterChart']['categories'],
                title: {
                    text: 'Reaction Groups'
                }
            },
            yAxis: {
                min: 0,
                title: {
                    text: 'Number of Individual Cases'
                },
                allowDecimals: false
            },
            tooltip: {
                valueSuffix: ''
            },
            credits: {
                enabled: false
            },
            legend: {
                reversed: false
            },
            plotOptions: {
                series: {
                    stacking: 'normal'
                }
            },
            series: chartSeries['reactionGroupByReporterChart']['data']
        });

        $('#reactionGroupByAgeChart').highcharts({
            chart: {
                type: 'bar',
                width: 1182,
                height: 600
            },
            title: {
                text: ''
            },
            xAxis: {
                //The potential x-axis values will come from server.
                categories: chartSeries['reactionGroupByAgeChart']['categories'],
                title: {
                    text: 'Reaction Groups'
                }
            },
            yAxis: {
                min: 0,
                title: {
                    text: 'Number of Individual Cases'
                },
                allowDecimals: false
            },
            tooltip: {
                valueSuffix: ''
            },
            credits: {
                enabled: false
            },
            legend: {
                reversed: false
            },
            plotOptions: {
                series: {
                    stacking: 'normal'
                }
            },
            series: chartSeries['reactionGroupByAgeChart']['data']
        });

        $('#reactionGroupByGenderChart').highcharts({
            chart: {
                type: 'bar',
                width: 1182,
                height: 600
            },
            title: {
                text: ''
            },
            xAxis: {
                //The potential x-axis values will come from server.
                categories: chartSeries['reactionGroupByGenderChart']['categories'],
                title: {
                    text: 'Reaction Groups'
                }
            },
            yAxis: {
                min: 0,
                title: {
                    text: 'Number of Individual Cases'
                },
                allowDecimals: false
            },
            tooltip: {
                valueSuffix: ''
            },
            credits: {
                enabled: false
            },
            legend: {
                reversed: false
            },
            plotOptions: {
                series: {
                    stacking: 'normal'
                }
            },
            series: chartSeries['reactionGroupByGenderChart']['data']
        });
    };

    var getTableRows = function (dataset) {
        var html = "";

        function add(a, b) {
            return a + b;
        }

        function round(value, decimals) {
            return Number(Math.round(value + 'e' + decimals) + 'e-' + decimals);
        }

        var sum = dataset.data.reduce(add, 0);

        for (i = 0; i < dataset.data.length; i++) {
            html += "<tr><td>" + dataset.categories[i] + "</td><td>" + dataset.data[i] + "</td><td>" + round(dataset.data[i] / sum * 100, 1) + "%</td></tr>"
        }
        if (dataset.data.length > 0) {
            html += '<tr class="as-bold"><td>Total</td><td>' + sum + '<td>100%</td></tr>'
        } else {
            html += '<tr class="as-bold"><td>Total</td><td>0<td>-</td></tr>'
        }
        return (html)
    };

    return {
        prepareReactionGroupChart: prepareReactionGroupChart,
        prepareReactionChart: prepareReactionChart,
        prepareIndividualCasesChart: prepareIndividualCasesChart,
        prepareIndividualCasesByReactionGroupChart: prepareIndividualCasesByReactionGroupChart
    }

})();