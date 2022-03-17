import { Component, OnInit } from '@angular/core';
import { DataService } from '../data.service';
import { TomorrowService } from '../tomorrow.service';

@Component({
  selector: 'app-favorites',
  templateUrl: './favorites.component.html',
  styleUrls: ['./favorites.component.css']
})
export class FavoritesComponent implements OnInit {

  constructor(
    private dataServ: DataService,
    private tom: TomorrowService
  ) { }

  headers = ["#", "City", "State", " "];

  fav_data: Array<any> = [];

  searchFav(loc: any) {
    this.dataServ.toggleStarted(true);
    this.dataServ.toggleFinished(false);
    this.dataServ.updateActive(1);

    var res: any;
    this.tom.getData(loc.loc).subscribe(
      data => {
        // Use Data Here, call a function or something
        // console.log(data);
        let valid = !(Object.keys(data).length === 0);
        res = {
          "valid": valid,
          "address": loc.address,
          "city": loc.city,
          "state": loc.state,
          "loc": loc.loc,
          "data": data
        }
        this.dataServ.updateData(res);
        this.dataServ.toggleFav(true);
        this.dataServ.toggleFinished(true);
      });


  }

  isEmpty() {
    return localStorage.length === 0;
  }

  deleteFav(index: number) {
    let key: any = localStorage.key(index);
    localStorage.removeItem(key);
    this.fav_data.splice(index, 1);
    this.dataServ.toggleFav(false);
  }

  ngOnInit(): void {

    if (localStorage.length > 0) {
      for (var i = 0; i < localStorage.length; i++) {
        let key: any = localStorage.key(i);
        var retrievedObject = JSON.parse(localStorage.getItem(key)!);
        this.fav_data.push(retrievedObject)
      }
    }
  }

}
