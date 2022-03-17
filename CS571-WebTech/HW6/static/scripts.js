// hold data read from tomorrow.io to populate result
var result_address = "-";
var weather_table_json = [];
var chart1_json = [];
var chart2_json = [];
var displayCharts = false;

// get formated date string 
// input: date = new Date(date_string)
function format_date(date) {
    var weekday = date.toLocaleDateString("en-US", { weekday: 'long' });
    var day = ("0" + date.getDate()).slice(-2);
    var month = date.toLocaleDateString("en-US", { month: 'short' });
    var year = date.toLocaleDateString("en-US", { year: 'numeric' });
    return weekday + ", " + day + " " + month + " " + year;
}

// Hide all divs
function hide_all_div() {
    document.getElementById("no-result-section").style.display = "none";
    document.getElementById("weather-card-wrapper").style.display = "none";
    document.getElementById("detail-container").style.display = "none";
    document.getElementById("weather-table-wrapper").style.display = "none";
    document.getElementById("weather-chart-container").style.display = "none";
}

// store location data in json object entry, and build param with it for backend API call
function request_weather_data() {

    hide_all_div();

    if (displayCharts) {
        toggle_chart();
    }

    var checkBox = document.getElementById("auto-location-check");

    // if user choose to detect location from IP address
    if (checkBox.checked == true) {
        fetch('https://ipinfo.io/json?token=15ee8a4671b9a0').
            then(function (response) {
                if (response.status !== 200) {
                    console.log(`Response status: ${response.status}`);
                    return;
                }
                response.json().
                    then(function (data) {
                        // Get city and state to display in the weather card
                        result_address = data.city + ", " + data.region + ", " + data.country;
                        // Pass geo_location to get_weather_data()
                        get_weather_data(data.loc);
                    })
            });

    } else {
        // if user entered detalied address
        var street = document.getElementById("street");
        var city = document.getElementById("city");
        var state = document.getElementById("state")
        // build google geocode api query
        var query_address = street.value.trim() + ", " + city.value.trim() + ", " + state.value.trim();
        var geo_entry = {
            key: GEO_API_KEY,
            address: query_address
        };
        // build url with params
        var geo_url = new URL('https://maps.googleapis.com/maps/api/geocode/json');
        geo_url.search = new URLSearchParams(geo_entry).toString();

        // get location details
        fetch(geo_url).
            then(function (response) {
                if (response.status !== 200) {
                    console.log(`geo lookup Failed. Response status: ${response.status}`);
                    console.log(geo_url);
                    return;
                }

                response.json().
                    then(function (data) {
                        if (data.status != "OK") {
                            document.getElementById("no-result-section").style.display = "block";
                            console.log(data.status + " from geocode!");
                            return;
                        }

                        // get formatted address
                        result_address = data.results[0].formatted_address;

                        // get geo location, pass to get_weather_data()
                        var geo_lat = data.results[0].geometry.location.lat;
                        var geo_lng = data.results[0].geometry.location.lng;
                        var geo_loc = geo_lat + "," + geo_lng;
                        get_weather_data(geo_loc);
                    })
            });

        var entry = {
            street: street.value,
            city: city.value,
            state: state.value
        };
    }

}

// This function builds request url and send it to an API hosted by backend
// This call should populate data for current, 1h and 1d
function get_weather_data(geo_location) {
    // reset data only
    weather_table_json = [];
    chart1_json = [];
    chart2_json = [];

    var entry = {
        loc: geo_location
    };
    var url = new URL(`${window.origin}/weather-api/json`);
    url.search = new URLSearchParams(entry).toString();

    fetch(url).
        then(function (response) {
            if (response.status !== 200) {
                // Display No records
                document.getElementById("no-result-section").style.display = "block";
                console.log(`Response status: ${response.status}`);
                return;
            }

            response.json().
                then(function (data) {
                    populate_result(data);
                })
        });
}

function show_detail_weather(index) {
    hide_all_div();
    document.getElementById("detail-container").style.display = "block";
    document.getElementById("weather-chart-container").style.display = "block";

    // Gather all the weather details
    var date = format_date(new Date(weather_table_json[index].startTime));
    var weather_code = weather_table_json[index].weatherCode;
    var icon_src = "";
    var icon_txt = "";
    if (weather_code in icon_mapping) {
        icon_src = icon_mapping[weather_code][0];
        icon_txt = icon_mapping[weather_code][1];
    } else {
        icon_src = "/static/Images/tstorm.svg";
        icon_txt = "Invalid Weather Code";
    }
    var maxTemp = weather_table_json[index].temperatureMax;
    var minTemp = weather_table_json[index].temperatureMin;
    var precipitation = precipitation_mapping[weather_table_json[index].precipitationType];
    var chancerain = weather_table_json[index].precipitationProbability;
    var windspeed = weather_table_json[index].windSpeed;
    var humidity = weather_table_json[index].humidity;
    var visibility = weather_table_json[index].visibility;
    var sunrise = new Date(weather_table_json[index].sunriseTime).getHours() % 12;
    var sunset = new Date(weather_table_json[index].sunsetTime).getHours() % 12;

    // populate weather detail section
    document.getElementById("weather-detail-date").innerHTML = date;
    document.getElementById("weather-detail-desc").innerHTML = icon_txt;
    document.getElementById("weather-detail-maxTemp").innerHTML = maxTemp;
    document.getElementById("weather-detail-minTemp").innerHTML = minTemp;
    document.getElementById("top-right-detail").style.backgroundImage = "url('" + icon_src + "')";
    document.getElementById("weather-detail-precipitation").innerHTML = precipitation;
    document.getElementById("weather-detail-chancerain").innerHTML = chancerain;
    document.getElementById("weather-detail-windspeed").innerHTML = windspeed;
    document.getElementById("weather-detail-humidity").innerHTML = humidity;
    document.getElementById("weather-detail-visibility").innerHTML = visibility;
    document.getElementById("weather-detail-sunrise").innerHTML = sunrise;
    document.getElementById("weather-detail-sunset").innerHTML = sunset;

}

// process json data to populate the weather table
function processTableJson(data) {
    for (var i = 0; i < data.length; i++) {
        var row = JSON.parse(JSON.stringify(data[i].values));
        row["startTime"] = data[i].startTime;
        weather_table_json.push(row);
    }
}

// process json data to populate the chart 1
// format: {[date, low, high]}
function getChart1data(data) {
    // reset data each time.
    chart1_json = [];

    for (var i = 0; i < data.length; i++) {
        var date_mil = new Date(data[i].startTime).getTime();
        var max_temp = data[i].values.temperatureMax;
        var min_temp = data[i].values.temperatureMin;
        chart1_json.push([date_mil, min_temp, max_temp]);
    }
}

// Create a table element with a header and rows depend on the response data from tomorrow io
function jsonToTable(data) {
    // populate weather_table_json  [ {day 1}, {day 2}, ... {day n}]
    processTableJson(data);

    // Prepare chart 1 data 
    getChart1data(data);

    // Create a table element
    var table = document.createElement("table");
    table.id = "result-table";

    // Table header and the key to look up the value of each column
    var headers = ["Date", "Status", "Temp High", "Temp Low", "Wind Speed"];
    var keys = ["startTime", "weatherCode", "temperatureMax", "temperatureMin", "windSpeed"];

    // Generate header row, use -1 to append tr to the last row
    var tr = table.insertRow(-1);
    for (var i = 0; i < headers.length; i++) {
        var th = document.createElement("th");
        th.innerHTML = headers[i];
        tr.appendChild(th);
    }

    // add json data to table rows
    for (var i = 0; i < weather_table_json.length; i++) {

        tr = table.insertRow(-1);
        tr.id = "day " + i;

        for (var j = 0; j < keys.length; j++) {
            var cell = tr.insertCell(-1);
            // for weather code, j==1, turn code into icon and weather description
            if (j == 0) {
                var date = new Date(weather_table_json[i][keys[j]]);
                cell.innerHTML = format_date(date);
            }
            else if (j == 1) {
                // Get icon corresponding to the weather code
                weather_code = weather_table_json[i][keys[j]];
                var icon_src = "";
                var icon_txt = "";
                if (weather_code in icon_mapping) {
                    icon_src = icon_mapping[weather_code][0];
                    icon_txt = icon_mapping[weather_code][1];
                } else {
                    icon_src = "/static/Images/tstorm.svg";
                    icon_txt = "Invalid Weather Code";
                }
                icon = document.createElement("img");
                icon.className = "table_row_icon";
                icon.src = icon_src;
                cell.appendChild(icon);

                // get description corresponding to the weather code
                text_span = document.createElement("span");
                text_span.innerHTML = icon_txt;
                cell.appendChild(text_span);

                // cell style
                cell.style.display = "flex";
                cell.style.alignItems = "center";
                cell.style.justifyContent = "center";
            }
            else {
                cell.innerHTML = weather_table_json[i][keys[j]];
            }
        }

        // add row click handler
        var createClickHandler = function (tr) {
            return function () {
                var index = tr.id.toString().split(' ')[1];
                show_detail_weather(index);
                var detailDiv = document.getElementById("detail-container");
                detailDiv.scrollIntoView();
            };
        };
        tr.onclick = createClickHandler(tr);
    }

    // div where table is shown
    var table_div = document.getElementById("weather-table-div");
    // reset previous table
    table_div.innerHTML = '';
    table_div.appendChild(table);

}

// Create a weather card and invoke weather table creation
function populate_result(data) {
    // Weather Card
    document.getElementById("weather-card-wrapper").style.display = "block";

    curr_weather_data = data.data.timelines[0].intervals[0].values
    document.getElementById("weather-city-state-title").innerHTML = result_address;
    // Change icon and icon text according to weahter code
    weather_code = String(curr_weather_data.weatherCode);
    if (weather_code in icon_mapping) {
        img_src = icon_mapping[weather_code][0];
        img_txt = icon_mapping[weather_code][1];
    } else {
        img_src = "/static/Images/tstorm.svg";
        img_txt = "Invalid Weather Code";
    }
    document.getElementById("weather-img").style.backgroundImage = "url('" + img_src + "')";
    document.getElementById("weather-img-text").innerHTML = img_txt;
    // Populate other weahter stats
    document.getElementById("weather-card-temp").innerHTML = Math.round(curr_weather_data.temperature * 10) / 10;
    document.getElementById("weather-humidity").innerHTML = curr_weather_data.humidity;
    document.getElementById("weather-pressure").innerHTML = curr_weather_data.pressureSeaLevel;
    document.getElementById("weather-wind").innerHTML = curr_weather_data.windSpeed;
    document.getElementById("weather-visibility").innerHTML = curr_weather_data.visibility;
    document.getElementById("weather-cloud").innerHTML = curr_weather_data.cloudCover;
    document.getElementById("weather-UV").innerHTML = curr_weather_data.uvIndex;

    // weather - table
    jsonToTable(data.data.timelines[2].intervals);
    // make table visible
    document.getElementById("weather-table-wrapper").style.display = "block";

    // Prepare data for charts
    chart2_json = data.data.timelines[1].intervals;
}

// Clear button onclick action
function clear_page() {

    // clean input form and checked box
    document.getElementById("input-form").reset();

    // check if input fields were disabled by checkbox
    var inputbox = document.getElementById("street")
    if (inputbox.disabled == true) {
        document.getElementById("street").disabled = false;
        document.getElementById("city").disabled = false;
        document.getElementById("state").disabled = false;
    }

    // clean result here and hide it
    hide_all_div()

    // reset json vars
    weather_table_json = [];
    chart1_json = [];

    // close charts
    if (displayCharts) {
        toggle_chart();
    }
}

// Disable input boxes when checkbox is checked
function checkbox_switch() {
    var checkBox = document.getElementById("auto-location-check");
    // var resultSection = document.getElementById("result-section");

    if (checkBox.checked == true) {
        document.getElementById("input-form").reset();
        document.getElementById("auto-location-check").checked = true;
        document.getElementById("street").disabled = true;
        document.getElementById("city").disabled = true;
        document.getElementById("state").disabled = true;
    } else {
        // uncheck the checkbox should reset everything on the page
        clear_page()
    }
}

// chart section toggle switch, show and hide charts each time the function is called.
function toggle_chart() {
    // to show charts
    if (!displayCharts) {
        // Draw Chart1 and Chart2 on click
        drawChart1(chart1_json);
        drawChart2(chart2_json);
        document.getElementById("up-arrow").style.display = "block";
        document.getElementById("down-arrow").style.display = "none";
        document.getElementById("temp-range-chart-container").style.display = "block";
        document.getElementById("hourly-weather-chart-container").style.display = "block";
        // focus on the up arrow once charts drawn
        var formDiv = document.getElementById("up-arrow");
        formDiv.scrollIntoView();
    }
    // to hide charts
    else {
        // focus on the weather detail section
        var formDiv = document.getElementById("detail-container");
        formDiv.scrollIntoView();
        document.getElementById("up-arrow").style.display = "none";
        document.getElementById("down-arrow").style.display = "block";
        document.getElementById("temp-range-chart-container").style.display = "none";
        document.getElementById("hourly-weather-chart-container").style.display = "none";
    }
    displayCharts = !displayCharts;
}