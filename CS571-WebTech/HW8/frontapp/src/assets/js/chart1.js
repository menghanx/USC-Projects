function getChart1data(data) {
    // reset data each time.
    var chart1_json = [];

    for (var i = 0; i < data.length; i++) {
        var date_mil = new Date(data[i].startTime).getTime();
        var max_temp = data[i].values.temperatureMax;
        var min_temp = data[i].values.temperatureMin;
        chart1_json.push([date_mil, min_temp, max_temp]);
    }
    return chart1_json;
}

function drawChart1(data) {

    Highcharts.chart('chart1', {

        chart: {
            type: 'arearange',
            zoomType: 'x',
            scrollablePlotArea: {
                scrollPositionX: 1
            }
        },

        title: {
            text: 'Temperature Ranges (Min, Max)'
        },

        xAxis: {
            type: 'datetime',
            labels: {
                formatter: function () {
                    return Highcharts.dateFormat('%d %b ', this.value);
                }
            },
            accessibility: {
                rangeDescription: 'Range: today to 15 days later'
            },
            tickInterval: 24 * 3600 * 1000
        },

        yAxis: {
            title: {
                text: null
            },
            tickInterval: 5
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