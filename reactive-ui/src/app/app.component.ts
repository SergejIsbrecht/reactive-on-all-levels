import {Component} from '@angular/core';
import {Observable} from 'rxjs';
import {HighBeamState} from './highBeamState';
import {IndicatorType} from './indicatorType';
import {CarRestService} from './car-rest.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  highBeam$: Observable<HighBeamState>;
  speed$: Observable<string>;
  indicator$: Observable<IndicatorType>;

  constructor(carService: CarRestService) {
    this.highBeam$ = carService.highBeam();
    this.speed$ = carService.speed();
    this.indicator$ = carService.indicator();
  }
}
