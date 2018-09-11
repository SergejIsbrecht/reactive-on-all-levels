import {Observable} from 'rxjs/index';
import {IndicatorType} from './indicatorType';
import {HighBeamState} from './highBeamState';

export interface CarService {
  highBeam(): Observable<HighBeamState>;

  speed(): Observable<string>;

  indicator(): Observable<IndicatorType>;
}
