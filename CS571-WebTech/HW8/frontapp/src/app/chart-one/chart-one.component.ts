import { Component, OnInit } from '@angular/core';
import { DataService } from '../data.service';

declare var drawChart1: any;
declare var getChart1data: any;


@Component({
  selector: 'app-chart-one',
  templateUrl: './chart-one.component.html',
  styleUrls: ['./chart-one.component.css']
})
export class ChartOneComponent implements OnInit {
  weatherData!: any;

  constructor(
    private dataServ: DataService
  ) { }

  ngOnInit(): void {
    // subscribe to the weather json data
    this.dataServ.currentData.subscribe(data => this.weatherData = data);
    if (this.weatherData.valid) {
      drawChart1(getChart1data(this.weatherData.data["1d"]));
    }
  }

}
