// Imports
import { Component, OnInit } from '@angular/core';
import { FormGroup, FormControl, Validators, ValidatorFn, ValidationErrors } from '@angular/forms';
import { MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { Observable } from 'rxjs';
// Custom services
import { AutocompleteService } from '../autocomplete.service'
import { TomorrowService } from '../tomorrow.service';
import { IpinfoService } from '../ipinfo.service';
import { GeocodeService } from '../geocode.service';
import { DataService } from '../data.service';

@Component({
  selector: 'app-weather-form',
  templateUrl: './weather-form.component.html',
  styleUrls: ['./weather-form.component.css']
})
export class WeatherFormComponent implements OnInit {

  constructor(
    private auto: AutocompleteService,
    private tom: TomorrowService,
    private ipinfo: IpinfoService,
    private geocode: GeocodeService,
    private dataServ: DataService,
  ) { }

  states: any[] = [{ id: "AL", full: "Alabama" }, { id: "AK", full: "Alaska" }, { id: "AZ", full: "Arizona" }, { id: "AR", full: "Arkansas" }, { id: "CA", full: "California" }, { id: "CO", full: "Colorado" }, { id: "CT", full: "Connecticut" }, { id: "DE", full: "Delaware" }, { id: "DC", full: "District Of Columbia" }, { id: "FL", full: "Florida" }, { id: "GA", full: "Georgia" }, { id: "HI", full: "Hawaii" }, { id: "ID", full: "Idaho" }, { id: "IL", full: "Illinois" }, { id: "IN", full: "Indiana" }, { id: "IA", full: "Iowa" }, { id: "KS", full: "Kansas" }, { id: "KY", full: "Kentucky" }, { id: "LA", full: "Louisiana" }, { id: "ME", full: "Maine" }, { id: "MD", full: "Maryland" }, { id: "MA", full: "Massachusetts" }, { id: "MI", full: "Michigan" }, { id: "MN", full: "Minnesota" }, { id: "MS", full: "Mississippi" }, { id: "MO", full: "Missouri" }, { id: "MT", full: "Montana" }, { id: "NE", full: "Nebraska" }, { id: "NV", full: "Nevada" }, { id: "NH", full: "New Hampshire" }, { id: "NJ", full: "New Jersey" }, { id: "NM", full: "New Mexico" }, { id: "NY", full: "New York" }, { id: "NC", full: "North Carolina" }, { id: "ND", full: "North Dakota" }, { id: "OH", full: "Ohio" }, { id: "OK", full: "Oklahoma" }, { id: "OR", full: "Oregon" }, { id: "PA", full: "Pennsylvania" }, { id: "RI", full: "Rhode Island" }, { id: "SC", full: "South Carolina" }, { id: "SD", full: "South Dakota" }, { id: "TN", full: "Tennessee" }, { id: "TX", full: "Texas" }, { id: "UT", full: "Utah" }, { id: "VT", full: "Vermont" }, { id: "VA", full: "Virginia" }, { id: "WA", full: "Washington" }, { id: "WV", full: "West Virginia" }, { id: "WI", full: "Wisconsin" }, { id: "WY", full: "Wyoming" }]
  states_id: string[] = ["AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "DC", "FL", "GA", "HI", "ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME", "MD", "MA", "MI", "MN", "MS", "MO", "MT", "NE", "NV", "NH", "NJ", "NM", "NY", "NC", "ND", "OH", "OK", "OR", "PA", "RI", "SC", "SD", "TN", "TX", "UT", "VT", "VA", "WA", "WV", "WI", "WY"];
  default_state = "Ca";
  // Autocomplete suggestions stored here
  options: any;
  // minimum length to trigger the suggestion
  minLength = 3;

  // location
  result_address!: string;
  city!: string;
  state!: string;

  // weather form group
  weatherForm!: FormGroup;

  // weather data
  weatherData!: any;

  ngOnInit(): void {
    // Initialize form
    this.weatherForm = new FormGroup({
      street: new FormControl('', [
        Validators.required,
        this.noWhitespaceValidator
      ]),
      city: new FormControl('', [
        Validators.required,
        this.noWhitespaceValidator
      ]),
      state: new FormControl(this.default_state),
      autoDetect: new FormControl(false)
    });
    // Enable autocomplete on city input
    this.weatherForm.get('city')?.valueChanges.subscribe(response => {
      // update options here
      // console.log('change is ', response);

      // Only trigger autocomplete when minimun length is met
      if (response && response.length >= this.minLength) {
        this.getAutos(response);
      } else {
        this.options = [];
      }
    });
    // subscribe to the weather json data
    this.dataServ.currentData.subscribe(data => this.weatherData = data);
  }

  // validate input is not just whitespace
  public noWhitespaceValidator(control: FormControl) {
    if (typeof (control.value) !== "string") {
      return null;
    }
    // const isWhitespace = ((control.value || '').trim().length === 0);
    const isWhitespace = (control.value === null || control.value.match(/^ *$/) !== null);
    const isValid = !isWhitespace;
    return isValid ? null : { 'whitespace': true };
  }


  setOption(option: any) {

    return option.city;
  }
  // Event handler when autocomplete option is selected
  onSelectionChanged(event: MatAutocompleteSelectedEvent) {

    this.weatherForm.controls['city'].setValue(event.option.value.city);
    if (this.states_id.indexOf(event.option.value.state) > -1) {
      this.weatherForm.controls['state'].setValue(event.option.value.state);
    }
  }

  // Given a string, get autocomplete suggestions through autocomplete service
  getAutos(input: string) {
    this.auto.getData(input).subscribe(
      data => {
        this.options = data;
      },
      error => {
        console.log("error");
      }
    )
  }

  // Submit the search
  onSubmit() {
    // TODO: Use EventEmitter with form value
    // console.warn(this.weatherForm.value);

    // reset weather data before each search
    this.dataServ.updateData({});
    this.dataServ.updateDetail({});
    this.dataServ.toggleFav(false);
    this.dataServ.toggleStarted(false);
    this.dataServ.toggleFinished(false);
    // Start
    this.dataServ.toggleStarted(true);
    this.dataServ.updateActive(1);
    // default dummy geo loc
    let loc = "";

    if (this.weatherForm.value['autoDetect']) {
      // console.log("auto detect");
      this.ipinfo.getData().subscribe(
        (data: any) => {
          // this.result_address = data["city"] + ", " + data["region"] + ", " + data["country"];
          this.result_address = data["city"] + ", " + data["region"];
          this.city = data["city"];
          this.state = data["region"];
          loc = data["loc"];
          // get weather data using loc
          this.getWeather(loc);
        }
      )
    } else {
      // console.log("user input");
      let query_address = this.weatherForm.value["street"].trim() + "," + this.weatherForm.value["city"].trim() + "," + this.weatherForm.value["state"].trim();
      this.city = this.weatherForm.value["city"].trim();
      this.state = this.states[this.states_id.indexOf(this.weatherForm.value["state"].trim().toUpperCase())].full;
      this.geocode.getData(query_address).subscribe(
        (data: any) => {
          this.result_address = data['results'][0]['formatted_address'];
          loc = data['results'][0]["geometry"]["location"]["lat"] + "," + data['results'][0]["geometry"]["location"]["lng"]
          this.getWeather(loc);
        }
      )
    }
  }

  // store weather data 
  getWeather(loc: any) {
    var res: any;
    // check if loc is in favs
    var key: string = loc;
    if (localStorage.getItem(key) != null) {
      this.dataServ.toggleFav(true);
    }

    this.tom.getData(loc).subscribe(
      data => {
        // Use Data Here, call a function or something
        // console.log(data);
        let valid = !(Object.keys(data).length === 0);
        res = {
          "valid": valid,
          "address": this.result_address,
          "city": this.city,
          "state": this.state,
          "loc": loc,
          "data": data
        }
        this.dataServ.updateData(res);
        this.dataServ.toggleFinished(true);
      });
  }

  // disable input fields when checkbox is checked
  disableInput($event: any) {
    if ($event.target.checked) {
      this.weatherForm.controls.street.disable();
      this.weatherForm.controls.city.disable();
      this.weatherForm.controls.state.disable();
      // this.weatherForm.controls.street.reset();
      // this.weatherForm.controls.city.reset();
      // this.weatherForm.controls['state'].setValue("");
    } else {
      this.weatherForm.controls.street.enable();
      this.weatherForm.controls.city.enable();
      this.weatherForm.controls.state.enable();
      // this.weatherForm.controls['state'].setValue(this.default_state);
    }
  }

  // Clear everything
  clearForm() {
    this.weatherForm.enable();
    this.weatherForm.reset();
    this.weatherForm.controls['state'].setValue(this.default_state);
    // empty weather data
    this.dataServ.updateData({});
    this.dataServ.updateDetail({});
    this.dataServ.toggleFav(false);
    this.dataServ.toggleStarted(false);
    this.dataServ.toggleFinished(false);
    this.dataServ.updateActive(1);
  }
}
