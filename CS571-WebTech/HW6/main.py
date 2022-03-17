import json
import re
import urllib
from datetime import datetime
from urllib import parse
from urllib.request import urlopen

# import jyserver.Flask as jsf
from flask import Flask, jsonify, current_app, request

WEATHER_API_KEY = "oMxv2FQh3mL4u4wyRZ0lV4qKC8rTZxCh"
app = Flask(__name__)


@app.route("/")
def home():
    # button tutorial
    # return App.render(render_template('index.html'))
    return current_app.send_static_file('index.html')


@app.route("/weather-api/json", methods=['GET'])
def get_weather():
    # if param contains loc:"lat,lng"
    if 'loc' in request.args:
        geo_location = request.args.get('loc')
    # else return an error message
    else:
        return jsonify({"Response": "Invailid request, expected params: lat,lng"})
    # Build query string
    # Debug Mode
    debug_mode = False
    if debug_mode:
        with open('static/sample_data.json') as f:
            data_json = json.load(f)
    else:
        # build tomorrow.io request url
        # Bundle 3 timesteps together and get results in one json
        weather_params = {
            "location": geo_location,
            "fields": "temperature,temperatureApparent,temperatureMin,temperatureMax,windSpeed,windDirection,humidity,pressureSeaLevel,uvIndex,weatherCode,precipitationProbability,precipitationType,sunriseTime,sunsetTime,visibility,moonPhase,cloudCover",
            "timesteps": "current,1h,1d",
            "units": "imperial",
            "apikey": WEATHER_API_KEY,
            "timezone": "America/Los_Angeles"
        }
        url = 'https://api.tomorrow.io/v4/timelines?' + \
            parse.urlencode(weather_params)
        # Get responsea
        try:
            weather_resp = urlopen(url)
        except urllib.error.URLError as e:
            print("Bad Request!!")
            return jsonify({"Response", str(e.code)})
        # decode response to json
        data_json = json.loads(weather_resp.read())
    return jsonify(data_json)


if __name__ == '__main__':

    # This is used when running locally only. When deploying to Google App
    # Engine, a webserver process such as Gunicorn will serve the app. This
    # can be configured by adding an `entrypoint` to app.yaml.
    app.run(host='127.0.0.1', port=8080, debug=True)
