import {Component, OnDestroy, OnInit} from '@angular/core';
import RSocketWebSocketClient from 'rsocket-websocket-client';
import {RSocketClient, Utf8Encoders} from 'rsocket-core';
import {Responder} from 'rsocket-types';
import {interval, Observable, timer} from 'rxjs';
import {delayWhen, map, retryWhen, shareReplay, switchMap, tap} from 'rxjs/operators';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit, OnDestroy {
  highBeam$: Observable<HighBeamState>;
  speed$: Observable<string>;
  indicator$: Observable<IndicatorType>;

  constructor() {
  }

  ngOnInit() {
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
      map((s: string) => HighBeamState[s])
    );

    this.indicator$ = client$.pipe(
      switchMap(rSocket => {
        return createTopic$('INDICATOR', 100, rSocket);
      }),
      map((s: string) => IndicatorType[s]),
      switchMap(s =>
        interval(500).pipe(map(i => i % 2 ? s : IndicatorType.OFF))
      )
    );

    this.speed$ = client$.pipe(
      switchMap(rSocket => {
        return createTopic$('SPEED', 100, rSocket);
      })
    );

    this.speed$.subscribe(
      x => console.log('onNext: %s', x),
      e => console.log('onError: %s', e),
      () => console.log('onCompleted'));
  }

  ngOnDestroy() {
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

export enum HighBeamState {
  ENABLED = 'ENABLED',
  DISABLED = 'DISABLED',
  DISABLED_LIGHT_DETECTED = 'DISABLED_LIGHT_DETECTED',
  DISABLED_SPEED_LIMIT = 'DISABLED_SPEED_LIMIT',
  FAILURE = 'FAILURE'
}

export enum IndicatorType {
  OFF = 'OFF',
  LEFT = 'LEFT',
  RIGHT = 'RIGHT'
}
