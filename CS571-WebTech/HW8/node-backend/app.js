// express app
const express = require('express');
const app = express();
// enable json middleware
app.use(express.json());
// HTTP GET by axios
const axios = require('axios');
require('axios-debug-log');


// Tomorrow.io API Parameters
let location = "";
const getTimelineURL = "https://api.tomorrow.io/v4/timelines";
// new key
// const apikey = "zuDpLrXISCRxZR82e0CO2I5oUT0PWHZJ";
const apikey = "oMxv2FQh3mL4u4wyRZ0lV4qKC8rTZxCh";
const fields = "temperature,temperatureApparent,temperatureMin,temperatureMax,windSpeed,windDirection,humidity,pressureSeaLevel,uvIndex,weatherCode,precipitationProbability,precipitationType,sunriseTime,sunsetTime,visibility,moonPhase,cloudCover";
const units = "imperial";
const timesteps = "current,1h,1d";
const timezone = "America/Los_Angeles";

// Autocomplete API Parameters
const autoURL = "https://maps.googleapis.com/maps/api/place/autocomplete/json";
const autoKEY = "AIzaSyCLJBmNPC4h2bQlqiUl17X0m0hzYMgKzAs";
const autoTypes = "(cities)";

// DEBUG
const fs = require('fs');

let rawdata = fs.readFileSync('dummy.json');
let dummyData = JSON.parse(rawdata);

//  What to do for homepage
// app.get('/', (req, res) => {
//     res.send('hello world!');
// });
var cors = require('cors')

app.use(cors())

app.use('/', express.static('dist/frontapp'));

// Autocomplete data
app.get('/api/auto', (req, res) => {

    if (req.query.input) {
        location = req.query.input
        auto_params = {
            "input": location,
            "key": autoKEY,
            "types": autoTypes
        }

        axios.get(`${autoURL}`, {
            params: auto_params
        }).then(resp => {
            if (resp.data.predictions.length === 0) {
                res.status(200).json(resp.data.predictions);
            } else {
                output = []
                for (item of resp.data.predictions) {
                    city = item.terms[0].value;
                    state = item.terms[1].value;
                    output.push(
                        {
                            "city": city,
                            "state": state
                        }
                    );
                }
                res.status(200).json(output);
            }

            // res.status(200).json(resp.data.predictions);
        }).catch(
            error => {
                res.status(500).send(error);
            }
        )
    } else {
        res.status(404).json({ "Response": "Invailid request, parameter missing. Expected params: input={string}" });
    }
});

// flag debug
var debug = false;

// tomorrow.io get weather data
app.get('/api/weather', (req, res) => {
    if (debug) {
        // return dummy data
        output = {
            "current": dummyData.data.timelines[0].intervals,
            "1h": dummyData.data.timelines[1].intervals,
            "1d": dummyData.data.timelines[2].intervals
        }
        res.status(200).json(output);
        // res.status(200).json({});
    } else {
        // obtain latitude and longtitude from params
        if (req.query.loc) {
            location = req.query.loc
            weather_params = {
                "location": location,
                "fields": fields,
                "timesteps": timesteps,
                "units": units,
                "apikey": apikey,
                "timezone": timezone
            }
            output = {};
            axios.get(`${getTimelineURL}`, {
                params: weather_params
            }).then(resp => {
                output = {
                    "current": resp.data.data.timelines[0].intervals,
                    "1h": resp.data.data.timelines[1].intervals,
                    "1d": resp.data.data.timelines[2].intervals
                }
                res.status(200).json(output);
            }).catch(
                error => {
                    // Return empty {} if any error
                    res.status(200).send(output);
                }
            )
        } else {
            res.status(404).json({ "Response": "Invailid request, parameter missing. Expected params: loc={lat},{lng}" });
        }
    }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`Listening on port ${PORT}...`));