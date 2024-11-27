var signal = signal || {};

signal.charts = (function () {
    var direct_draw_bar_chart = function(container_id, opt) {
        return Highcharts.chart(container_id, opt);
    };

    var draw_bar_chart = function (container_id, year, cfg) {
        var chart_options = Highcharts.chart(container_id, {
            chart: {
                type: 'column'
            },
            title: {
                text: cfg.title
            },
            subtitle: {
                text: cfg.subject
            },
            xAxis: {
                type: 'Months',
                labels: {
                    rotation: -45,
                    style: {
                        fontSize: '13px',
                        fontFamily: 'Verdana, sans-serif'
                    }
                }
            },
            yAxis: {
                min: 0,
                title: {
                    text: cfg.value_title
                }
            },
            legend: {
                enabled: true
            },
            tooltip: {
                pointFormat: cfg.subject + ' in ' + cfg.time + ': <b>{point.y:.1f}</b>'
            },
            series: [{
                name: 'Population',
                data: cfg.data,
                dataLabels: {
                    enabled: true,
                    rotation: -90,
                    color: '#FFFFFF',
                    align: 'right',
                    format: '{point.y:.1f}', // one decimal
                    y: 10, // 10 pixels down from the top
                    style: {
                        fontSize: '13px',
                        fontFamily: 'Verdana, sans-serif'
                    }
                }
            }]
        });

        return chart_options
    };

    return {
        draw_bar_chart: draw_bar_chart,
        direct_draw_bar_chart: direct_draw_bar_chart
    };
})();