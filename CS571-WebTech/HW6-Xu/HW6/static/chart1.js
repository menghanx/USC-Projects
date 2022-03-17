function drawChart1(data) {

    Highcharts.chart('chart1', {

        chart: {
            type: 'arearange',
            zoomType: 'x',
            scrollablePlotArea: {
                minWidth: 600,
                scrollPositionX: 1
            },
            width: 950,
            height: 380
        },

        title: {
            text: 'Temperature Ranges (Min, Max)'
        },

        xAxis: {
            type: 'datetime',
            accessibility: {
                rangeDescription: 'Range: today to 15 days later'
            }
        },

        yAxis: {
            title: {
                text: null
            }
        },

        tooltip: {
            crosshairs: true,
            shared: true,
            valueSuffix: '°F',
            xDateFormat: '%A, %b %e'
        },

        legend: {
            enabled: false
        },

        plotOptions: {
            series: {
                fillColor: {
                    linearGradient: [0, 0, 0, 325],
                    stops: [
                        [0, '#ffa40d'],
                        [1, Highcharts.color(Highcharts.getOptions().colors[0]).setOpacity(0).get('rgba')]
                    ]
                },
                lineColor: '#ffa40d'
            }
        },

        series: [{
            name: 'Temperatures',
            data: data,

        }]

    });
}