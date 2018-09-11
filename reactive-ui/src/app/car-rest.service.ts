import {Injectable} from '@angular/core';
import {CarService} from './car.service';
import {IndicatorType} from './indicatorType';
import {interval, Observable, timer} from 'rxjs';
import {HighBeamState} from './highBeamState';
import {delayWhen, map, retryWhen, shareReplay, switchMap, tap} from 'rxjs/operators';
import RSocketWebSocketClient from 'rsocket-websocket-client';
import {RSocketClient, Utf8Encoders} from 'rsocket-core';
import {Responder} from 'rsocket-types';

@Injectable({
  providedIn: 'root'
})
export class CarRestService implements CarService {

  private readonly highBeam$: Observable<HighBeamState>;
  private readonly speed$: Observable<string>;
  private readonly indicator$: Observable<IndicatorType>;

  constructor() {
    const client$: Observable<Responder> = new Observable(observer => {
      const client = new RSocketClient({
        setup: {
          // ms btw sending keepalive to server
          keepAlive: 60000,
          // ms timeout if no keepalive response
          lifetime: 180000,
          dataMimeType: 'binary',
          metadataMimeType: 'binary'
        },
        transport: new RSocketWebSocketClient(
          {
            url: 'ws://localhost:8042',
            debug: true,
            lengthPrefixedFrames: false
          },
          Utf8Encoders
        )
      });

      client.connect().subscribe({
        onComplete: socket => {
          // socket.onClose().catch(error => observer.error(error));

          observer.next(socket);
        },
        onError: error => observer.error(error)
      });
    }).pipe(
      retryWhen(errors =>
        errors.pipe(
          tap(ignore => console.log('failed to connect...')),
          delayWhen(ignore => timer(1000))
        )
      ),
      shareReplay(1)
    );

    this.highBeam$ = client$.pipe(
      switchMap(rSocket => {
        return createTopic$('HIGHBEAMASSIST', 100, rSocket);
      }),
      map((s: string) => HighBeamState[s]),
      shareReplay(1)
    );
    this.speed$ = client$.pipe(
      switchMap(rSocket => {
        return createTopic$('SPEED', 100, rSocket);
      }),
      shareReplay(1)
    );
    this.indicator$ = client$.pipe(
      switchMap(rSocket => {
        return createTopic$('INDICATOR', 100, rSocket);
      }),
      map((s: string) => IndicatorType[s]),
      shareReplay(1)
    );
  }

  highBeam(): Observable<HighBeamState> {
    return this.highBeam$;
  }

  indicator(): Observable<IndicatorType> {
    return this.indicator$;
  }

  speed(): Observable<string> {
    return this.speed$;
  }
}

function createTopic$(
  topic: string,
  initRequest: number,
  rSocket: Responder
): Observable<string> {
  const obs$ = Observable.create(observer => {
    rSocket
      .requestStream({
        data: topic.toString(),
        metadata: 'metadata goes here'
      })
      .subscribe({
        onComplete: () => observer.complete(),
        onError: error => observer.error(error),
        onNext: payload => {
          observer.next(payload.data);
        },
        onSubscribe: subscription => {
          subscription.request(initRequest);
        }
      });
  });
  return obs$;
}

