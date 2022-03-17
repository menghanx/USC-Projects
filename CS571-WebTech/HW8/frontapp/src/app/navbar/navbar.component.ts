import { Component, OnInit, ViewChild } from '@angular/core';
import { DataService } from '../data.service';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit {
  res_selected = true;
  fav_selected = false;

  @ViewChild('nav')
  nav: any;

  active = 1;

  started!: boolean;
  finished!: boolean;
  // showResult() {
  //   this.nav.select(1);
  // }

  constructor(
    private dataServ: DataService,
  ) { }

  ngOnInit(): void {
    this.dataServ.currentActive.subscribe(data => this.active = data);
    this.dataServ.curStarted.subscribe(data => this.started = data);
    this.dataServ.curFinished.subscribe(data => this.finished = data);
  }

}
