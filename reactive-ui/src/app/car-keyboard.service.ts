import {Injectable} from '@angular/core';
import {CarService} from './car.service';
import {combineLatest, merge, Observable} from 'rxjs';
import {IndicatorType} from './indicatorType';
import {HighBeamState} from './highBeamState';
import {distinctUntilChanged, map, scan} from 'rxjs/internal/operators';

@Injectable({
  providedIn: 'root'
})
export class CarKeyboardService implements CarService {

  private readonly highBeam$: Observable<HighBeamState>;
  private readonly speed$: Observable<number>;
  private readonly indicator$: Observable<IndicatorType>;

  constructor() {
    this.indicator$ = merge(
      this.createPressAndHoldKeyboardListenerObservable(37, IndicatorType.LEFT, IndicatorType.OFF),
      this.createPressAndHoldKeyboardListenerObservable(39, IndicatorType.RIGHT, IndicatorType.OFF))
      .pipe(distinctUntilChanged());

    const accelerate$ = new Observable<number>(emitter => {
      emitter.next(0);
      window.addEventListener('keydown', event => {
        if (event.keyCode === 38) {
          emitter.next(50);
        }
      });
    });

    const break$ = new Observable<number>(emitter => {
      emitter.next(0);
      window.addEventListener('keydown', event => {
        if (event.keyCode === 40) {
          emitter.next(-50);
        }
      });
    });

    this.speed$ = merge(
      accelerate$,
      break$)
      .pipe(
        scan((acc, curr) => acc + curr, 0)
      );

    const highBeamActivated$ = new Observable<boolean>(emitter => {
      let highBeamActivated = false;
      emitter.next(highBeamActivated);
      window.addEventListener('keydown', event => {
        if (event.keyCode === 72) {
          highBeamActivated = !highBeamActivated;
          emitter.next(highBeamActivated);
        }
      });
    });

    const lightDetected$ = this.createPressAndHoldKeyboardListenerObservable(76, true, false);

    this.highBeam$ = combineLatest(highBeamActivated$, lightDetected$, this.speed$)
      .pipe(
        map(([highBeamActivated, lightDetected, speed]) => {
          if (!highBeamActivated) {
            return HighBeamState.DISABLED;
          }
          const isSpeedThresholdExceeded = speed > 130;
          if (isSpeedThresholdExceeded) {
            return HighBeamState.DISABLED_SPEED_LIMIT;
          }
          if (lightDetected) {
            return HighBeamState.DISABLED_LIGHT_DETECTED;
          }
          return HighBeamState.ENABLED;
        })
      )
    ;
  }

  highBeam(): Observable<HighBeamState> {
    return this.highBeam$;
  }

  indicator(): Observable<IndicatorType> {
    return this.indicator$;
  }

  speed(): Observable<number> {
    return this.speed$;
  }

  private createPressAndHoldKeyboardListenerObservable<T>(keyCode: number, keyDownEmit: T, keyUpEmit: T): Observable<T> {
    return new Observable<T>(emitter => {
      emitter.next(keyUpEmit);
      window.addEventListener('keydown', event => {
        if (event.keyCode === keyCode) {
          emitter.next(keyDownEmit);
        }
      });
      window.addEventListener('keyup', event => {
        if (event.keyCode === keyCode) {
          emitter.next(keyUpEmit);
        }
      });
    });
  }
}


