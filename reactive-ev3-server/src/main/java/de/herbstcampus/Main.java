package de.herbstcampus;

import io.rsocket.AbstractRSocket;
import io.rsocket.ConnectionSetupPayload;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.SocketAcceptor;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import java.time.Duration;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3TouchSensor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class Main {
  public static void main(String[] args) {
    RSocketFactory.receive().acceptor(new SocketAcceptorImpl()).transport(TcpServerTransport.create("10.0.1.1", 7000)).start().subscribe();

    Flux.interval(Duration.ofHours(1000), Schedulers.single()).take(1).blockLast();
  }

  private static class SocketAcceptorImpl implements SocketAcceptor {

    private EV3TouchSensor ev3TouchSensor;

    SocketAcceptorImpl() {
      LocalEV3 localEV3 = LocalEV3.ev3;
      Port s2 = localEV3.getPort("S2");
      ev3TouchSensor = new EV3TouchSensor(s2);
    }

    @Override
    public Mono<RSocket> accept(ConnectionSetupPayload setupPayload, RSocket reactiveSocket) {
      return Mono.just(
          new AbstractRSocket() {
            @Override
            // touch/500 | light/
            public Flux<Payload> requestStream(Payload payload) {
              return Flux.interval(Duration.ofMillis(50), Schedulers.single())
                  .map(
                      aLong -> {
                        float[] sample = new float[ev3TouchSensor.sampleSize()];
                        ev3TouchSensor.getTouchMode().fetchSample(sample, 0);
                        return sample[0];
                      })
                  .map(aFloat -> DefaultPayload.create("VALUE: " + aFloat))
                  .doOnNext(p -> System.out.println(p.getDataUtf8()));
            }
          });
    }
  }
}
