import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, retry } from 'rxjs/operators';


@Injectable({
  providedIn: 'root'
})
export class TomorrowService {
  // readonly ROOT_URL = window.location.host;
  // readonly ROOT_URL = 'http://localhost:3000';
  readonly ROOT_URL = 'https://sunny-day-cycling.wl.r.appspot.com';

  constructor(private http: HttpClient) { }

  getData(loc: any) {
    let params = new HttpParams().set('loc', loc);
    // let params = new HttpParams().set('userId', uId);

    return this.http.get(this.ROOT_URL + '/api/weather', { params })
      .pipe(
        catchError(this.handleError)
      );
    // return this.http.get(this.ROOT_URL + '/posts', { params });
  }

  private handleError(error: HttpErrorResponse) {
    if (error.status === 0) {
      // A client-side or network error occurred. Handle it accordingly.
      console.error('An error occurred:', error.error);
    } else {
      // The backend returned an unsuccessful response code.
      // The response body may contain clues as to what went wrong.
      console.error(
        `Backend returned code ${error.status}, body was: `, error.error);
    }
    // Return an observable with a user-facing error message.
    return throwError(
      "Server Error");
  }

}
