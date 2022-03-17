import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs'


@Injectable({
  providedIn: 'root'
})
export class DataService {

  private dataSource = new BehaviorSubject<Object>({});
  currentData = this.dataSource.asObservable();

  private detail_data = new BehaviorSubject<Object>({});
  currentDetail = this.detail_data.asObservable();

  private isFav = new BehaviorSubject<boolean>(false);
  currentFav = this.isFav.asObservable();

  private active_view = new BehaviorSubject<number>(1);
  currentActive = this.active_view.asObservable();

  private started = new BehaviorSubject<boolean>(false);
  curStarted = this.started.asObservable();

  private finished = new BehaviorSubject<boolean>(false);
  curFinished = this.finished.asObservable();

  private tweet = new BehaviorSubject<string>("");
  curTweet = this.tweet.asObservable();

  constructor() { }
  updateData(data: any) {
    this.dataSource.next(data);
  }

  updateDetail(data: any) {
    this.detail_data.next(data);
    var text = `The temperature in ${data.address} on ${data.date} is ${data.temp}. The weather conditions are ${data.status}`;
    var output = "https://twitter.com/intent/tweet?text=" + encodeURI(text) + "&hashtags=CSCI571WeatherSearch";
    this.tweet.next(output);
  }

  toggleFav(data: boolean) {
    this.isFav.next(data);
  }

  updateActive(data: number) {
    this.active_view.next(data);
  }

  toggleStarted(data: boolean) {
    this.started.next(data);
  }

  toggleFinished(data: boolean) {
    this.finished.next(data);
  }
}