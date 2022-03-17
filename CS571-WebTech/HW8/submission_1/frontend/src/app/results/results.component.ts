import { Component, OnInit } from '@angular/core';
import { DataService } from '../data.service';
import { trigger, transition, query, style, animate, group } from '@angular/animations';
import { DatePipe } from '@angular/common';
// DEBUG TO REMOVE
import * as data from "./data.json";

const left = [
  query(':enter, :leave', style({ position: 'fixed', width: '100%' }), { optional: true }),
  group([
    query(':enter', [style({ transform: 'translateX(-100%)' }), animate('.3s ease-out', style({ transform: 'translateX(0%)' }))], {
      optional: true,
    }),
    query(':leave', [style({ transform: 'translateX(0%)' }), animate('.3s ease-out', style({ transform: 'translateX(100%)' }))], {
      optional: true,
    }),
  ]),
];

const right = [
  query(':enter, :leave', style({ position: 'fixed', width: '100%' }), { optional: true }),
  group([
    query(':enter', [style({ transform: 'translateX(100%)' }), animate('.3s ease-out', style({ transform: 'translateX(0%)' }))], {
      optional: true,
    }),
    query(':leave', [style({ transform: 'translateX(0%)' }), animate('.3s ease-out', style({ transform: 'translateX(-100%)' }))], {
      optional: true,
    }),
  ]),
];

@Component({
  selector: 'app-results',
  templateUrl: './results.component.html',
  styleUrls: ['./results.component.css'],
  animations: [
    trigger('divSlider', [
      transition(':increment', right),
      transition(':decrement', left),
    ]),

    trigger('slideInOut', [
      transition(':enter', [
        style({ transform: 'translateX(-100%)' }),
        animate('200ms ease-in', style({ transform: 'translateX(0%)' }))
      ]),
      transition(':leave', [
        animate('200ms ease-in', style({ transform: 'translateX(-100%)' }))
      ])
    ])
  ]
})

export class ResultsComponent implements OnInit {

  // weather data
  weatherData!: any;
  detailData!: any;
  isFav: boolean = false;
  tweet!: string;


  headers = ["#", "Date", "Status", "Temp. High(°F)", "Temp. Low(°F)", "Wind Speed (mph)"];
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

  // toggle detail
  visible = true;

  constructor(
    private dataServ: DataService,
    private datepipe: DatePipe
  ) { }

  status(input: string) {
    return this.mapping[input][1];
  }

  imagePath(input: string) {
    return this.mapping[input][0];
  }
  getDate(input: string) {
    return new Date(input);
  }

  toggleDetail(data: any) {
    this.visible = !this.visible;
    data.address = this.weatherData.city + ", " + this.weatherData.state;
    if ("startTime" in data) {
      data.date = this.datepipe.transform(new Date(data.startTime), 'EEEE, dd MMM yyyy');
      data.temp = `${data.values.temperature} °F`;
      data.status = this.status(data.values.weatherCode);
    }
    this.dataServ.updateDetail(data);

  }

  toggleFav() {
    // add/remove fav
    if (this.isFav) {
      localStorage.removeItem(this.weatherData.loc);
    } else {
      var fav_data = {
        "city": this.weatherData.city,
        "state": this.weatherData.state,
        "address": this.weatherData.address,
        "loc": this.weatherData.loc
      }
      localStorage.setItem(this.weatherData.loc, JSON.stringify(fav_data));
    }
    // toggle Fav
    this.isFav = !this.isFav;
  }

  ngOnInit(): void {
    // default isFav = false
    // subscribe to the weather json data
    this.dataServ.currentData.subscribe(data => this.weatherData = data);
    this.dataServ.currentDetail.subscribe(data => this.detailData = data);
    this.dataServ.currentFav.subscribe(data => this.isFav = data);
    this.dataServ.curTweet.subscribe(data => this.tweet = data);
    if (this.weatherData.valid) {
      var fav = localStorage.getItem(this.weatherData.loc);
      if (fav !== null) {
        this.isFav = true;
      }
    }
    // update isFave
    // this.dataServ.currentFav.subscribe(data => this.isFav = data);
    // DEBUG TO REMOVE
    // this.weatherData = data;
  }

}
