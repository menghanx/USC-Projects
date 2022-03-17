import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpErrorResponse } from '@angular/common/http'
import { catchError, map, retry } from 'rxjs/operators';
import { Observable, throwError } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class IpinfoService {
  constructor(private http: HttpClient) { }

  readonly ROOT_URL = 'https://ipinfo.io/json?token=15ee8a4671b9a0';

  public getData() {
    return this.http.get(this.ROOT_URL)
      .pipe(
        catchError(this.errorHandler)
      );
  }
  private errorHandler(error: HttpErrorResponse) {
    return throwError(error.message || "server error.");
  }

}
