import {Component, Injector} from '@angular/core';
import {interval, Observable} from 'rxjs';
import {HighBeamState} from './highBeamState';
import {IndicatorType} from './indicatorType';
import {switchMap} from 'rxjs/operators';
import {CarKeyboardService} from './car-keyboard.service';
import {CarService} from './car.service';
import {CarRestService} from './car-rest.service';
import {map, pairwise, takeWhile} from 'rxjs/internal/operators';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  highBeam$: Observable<HighBeamState>;
  speed$: Observable<string>;
  indicator$: Observable<IndicatorType>;
  serviceBackend = 'keyboard';

  constructor(private injector: Injector) {
    this.updateServiceBackend();
  }

  updateServiceBackend() {
    let carService: CarService;
    if (this.serviceBackend === 'keyboard') {
      carService = this.injector.get(CarKeyboardService);
    } else if (this.serviceBackend === 'rest') {
      carService = this.injector.get(CarRestService);
    }

    this.highBeam$ = carService.highBeam();
    this.indicator$ = carService.indicator().pipe(
      switchMap(s =>
        interval(500).pipe(map(i => (i + 1) % 2 ? s : IndicatorType.OFF))
      )
    );
    this.speed$ = carService.speed().pipe(
      pairwise(),
      switchMap(data => {
        const prevSpeed: number = data[0];
        const targetSpeed: number = data[1];
        return interval(20).pipe(
          map(i => Math.floor((targetSpeed - prevSpeed) / 25) * i + prevSpeed),
          takeWhile((currSpeed) => {
            if (prevSpeed <= targetSpeed) {
              return currSpeed <= targetSpeed;
            } else {
              return currSpeed >= targetSpeed;
            }
          })
        );
      }),
      map(String));
  }
}
