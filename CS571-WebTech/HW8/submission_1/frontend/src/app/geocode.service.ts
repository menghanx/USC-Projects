import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpErrorResponse } from '@angular/common/http'
import { catchError, map, retry } from 'rxjs/operators';
import { Observable, throwError } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class GeocodeService {

  constructor(private http: HttpClient) { }

  readonly ROOT_URL = "https://maps.googleapis.com/maps/api/geocode/json";
  readonly GEO_KEY = "AIzaSyCLJBmNPC4h2bQlqiUl17X0m0hzYMgKzAs";

  public getData(address: string) {
    let params = new HttpParams().set('key', this.GEO_KEY).set('address', address);
    return this.http.get(this.ROOT_URL, { params })
      .pipe(
        catchError(this.errorHandler)
      );
  }
  private errorHandler(error: HttpErrorResponse) {
    return throwError(error.message || "server error.");
  }
}
