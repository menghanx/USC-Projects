import { Component, OnInit } from '@angular/core';
import { DataService } from '../data.service';
// use JS to draw chart
declare var drawChart2: any;

@Component({
  selector: 'app-chart-two',
  templateUrl: './chart-two.component.html',
  styleUrls: ['./chart-two.component.css']
})
export class ChartTwoComponent implements OnInit {

  constructor(
    private dataServ: DataService
  ) { }
  weatherData!: any;

  ngOnInit(): void {
    // subscribe to the weather json data
    this.dataServ.currentData.subscribe(data => this.weatherData = data);
    if (this.weatherData.valid) {
      drawChart2(this.weatherData.data["1h"]);
    }

  }

}
