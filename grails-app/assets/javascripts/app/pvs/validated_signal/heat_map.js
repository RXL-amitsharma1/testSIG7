var init_system_organ_heat_chart = function (xAxis, yAxis, chartData) {
    $('#system-organ-heat-map').height(1200);
    if($('#system-organ-heat-map').highcharts()){
        $('#system-organ-heat-map').highcharts().destroy();
    }
    $('#system-organ-heat-map').highcharts({
        chart: {
            type: 'heatmap',
            marginTop: 40,
            marginBottom: 100,
            backgroundColor: null,
            credit: false
        },

        "credits": {
            "enabled": false
        },
        title: {
            text: null
        },

        xAxis: {
            categories: xAxis
        },

        yAxis: {
            categories: yAxis,
            title: null
        },

        colorAxis: {
            min: 0,
            minColor: '#FFFFFF',
            maxColor: '#0F99B0'
        },

        legend: {
            align: 'right',
            layout: 'vertical',
            margin: 0,
            verticalAlign: 'top',
            y: 20,
            symbolHeight: 320
        },

        tooltip: {
            formatter: function () {
                return '<b>' + this.series.xAxis.categories[this.point.x] + '</b><br><b>' +
                    this.point.value + '</b> cases on <br><b>' + this.series.yAxis.categories[this.point.y] + '</b>';
            }
        },

        series: [{
            name: 'SOC per Month, per product',
            borderWidth: 1,
            data: chartData,
            dataLabels: {
                enabled: true,
                color: 'black',
                style: {
                    textShadow: 'none'
                }
            }
        }]

    });
};
