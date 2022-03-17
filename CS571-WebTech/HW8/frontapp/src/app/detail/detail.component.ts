import { Component, OnInit } from '@angular/core';
import { Loader } from '@googlemaps/js-api-loader';
import { DataService } from '../data.service';

@Component({
  selector: 'app-detail',
  templateUrl: './detail.component.html',
  styleUrls: ['./detail.component.css']
})
export class DetailComponent implements OnInit {

  constructor(
    private dataServ: DataService
  ) { }

  weatherData!: any;

  mapping: any = {
    "1000": [
      "../../assets/images/clear_day.svg",
      "Clear"
    ],
    "1001": [
      "../../assets/images/cloudy.svg",
      "Cloudy"
    ],
    "1100": [
      "../../assets/images/mostly_clear_day.svg",
      "Mostly Clear"
    ],
    "1101": [
      "../../assets/images/partly_cloudy_day.svg",
      "Partly Cloudy"
    ],
    "1102": [
      "../../assets/images/mostly_cloudy.svg",
      "Mostly Cloudy"
    ],
    "2000": [
      "../../assets/images/fog.svg",
      "Fog"
    ],
    "2100": [
      "../../assets/images/fog_light.svg",
      "Light Fog"
    ],
    "3000": [
      "../../assets/images/light_wind.svg",
      "Light Wind"
    ],
    "3001": [
      "../../assets/images/wind.svg",
      "Wind"
    ],
    "3002": [
      "../../assets/images/strong_wind.svg",
      "Strong Wind"
    ],
    "4000": [
      "../../assets/images/drizzle.svg",
      "Drizzle"
    ],
    "4001": [
      "../../assets/images/rain.svg",
      "Rain"
    ],
    "4200": [
      "../../assets/images/rain_light.svg",
      "Light Rain"
    ],
    "4201": [
      "../../assets/images/rain_heavy.svg",
      "Heavy Rain"
    ],
    "5000": [
      "../../assets/images/snow.svg",
      "Snow"
    ],
    "5001": [
      "../../assets/images/flurries.svg",
      "Flurries"
    ],
    "5100": [
      "../../assets/images/snow_light.svg",
      "Light Snow"
    ],
    "5101": [
      "../../assets/images/snow_heavy.svg",
      "Heavy Snow"
    ],
    "6000": [
      "../../assets/images/freezing_drizzle.svg",
      "Freezing Drizzle"
    ],
    "6001": [
      "../../assets/images/freezing_rain.svg",
      "Freezing Rain"
    ],
    "6200": [
      "../../assets/images/freezing_rain_light.svg",
      "Light Freezing Rain"
    ],
    "6201": [
      "../../assets/images/freezing_rain_heavy.svg",
      "Heavy Freezing Rain"
    ],
    "7000": [
      "../../assets/images/ice_pellets.svg",
      "Ice Pellets"
    ],
    "7101": [
      "../../assets/images/ice_pellets_heavy.svg",
      "Heavy Ice Pellets"
    ],
    "7102": [
      "../../assets/images/ice_pellets_light.svg",
      "Light Ice Pellets"
    ],
    "8000": [
      "../../assets/images/tstorm.svg",
      "Thunderstorm"
    ]
  };

  dayData!: any;

  index!: number;

  status(input: string) {
    return this.mapping[input][1];
  }

  getDate(input: string) {
    return new Date(input);
  }

  ngOnInit(): void {
    this.dataServ.currentData.subscribe(data => this.weatherData = data);
    this.dataServ.currentDetail.subscribe(data => this.dayData = data);

    if (this.weatherData.valid) {
      let loc_lat = Number(this.weatherData.loc.split(",")[0]);
      let loc_lng = Number(this.weatherData.loc.split(",")[1]);

      let loader = new Loader({
        apiKey: "AIzaSyCLJBmNPC4h2bQlqiUl17X0m0hzYMgKzAs"
      })
      const myLatLng = { lat: loc_lat, lng: loc_lng };
      loader.load().then(() => {
        const mapDiv = document.getElementById('map')!;
        const map = new google.maps.Map(mapDiv, {
          zoom: 16,
          center: myLatLng,
        })

        new google.maps.Marker({
          position: myLatLng,
          map,
          title: "Location",
        });
      })
    }
  }

}
