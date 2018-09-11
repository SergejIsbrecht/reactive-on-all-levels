import {Component} from '@angular/core';
import {interval, Observable} from 'rxjs';
import {HighBeamState} from './highBeamState';
import {IndicatorType} from './indicatorType';
import {CarKeyboardService} from './car-keyboard.service';
import {map, switchMap} from 'rxjs/operators';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  highBeam$: Observable<HighBeamState>;
  speed$: Observable<string>;
  indicator$: Observable<IndicatorType>;

  constructor(carService: CarKeyboardService) {
    this.highBeam$ = carService.highBeam();
    this.speed$ = carService.speed();
    this.indicator$ = carService.indicator().pipe(
      switchMap(s =>
        interval(500).pipe(map(i => (i + 1) % 2 ? s : IndicatorType.OFF))
      )
    );
  }
}
