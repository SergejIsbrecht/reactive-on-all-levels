package de.herbstcampus.infrastructure;

import de.herbstcampus.api.Sensor;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;
import java.nio.FloatBuffer;
import java.time.Duration;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.remote.ev3.RemoteEV3;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

class RemoteSampleFacadeFactoryTest {
  private RemoteSampleFacadeFactory classUnderTest;

  @BeforeEach
  void setUp() {
    this.classUnderTest = new RemoteSampleFacadeFactory("10.0.1.1", Schedulers.single());
  }

  @Test
  void name() {
    Sensor<float[]> sensorSampleFacade = classUnderTest.sampleSensor("TOUCH");
    Flux<float[]> sample = sensorSampleFacade.stream$(100);
    sample.blockFirst();
  }

  @Test
  void sdfsd() throws Exception {
    RemoteEV3 remoteEV3 = new RemoteEV3("10.0.1.1");
    Port p = remoteEV3.getPort("S1");

    // lejos.hardware.sensor.EV3TouchSensor

    //    Mono<RMISampleProvider> provider =
    //        Mono.using(
    //            () -> {
    //              RMISampleProvider sampleProvider = remoteEV3.createSampleProvider("S1", "lejos.hardware.sensor.EV3TouchSensor", "Touch");
    //              return sampleProvider;
    //            },
    //            Mono::just,
    //            rmiSampleProvider -> {
    //              try {
    //                rmiSampleProvider.close();
    //              } catch (Exception ex) {
    //                throw new RuntimeException(ex);
    //              }
    //            });
    //
    //    Flux<float[]> flux =
    //        provider.flatMapMany(
    //            rmiSampleProvider -> {
    //              return Flux.interval(Duration.ofMillis(1000))
    //                  .map(
    //                      aLong -> {
    //                        try {
    //                          return rmiSampleProvider.fetchSample();
    //                        } catch (Exception ex) {
    //                          return new float[0];
    //                        }
    //                      });
    //            });
    //
    //    flux.doOnNext(
    //            floats -> {
    //              for (float f : floats) {
    //                System.out.println(f);
    //              }
    //            })
    //        .blockLast();

    Flux<EV3ColorSensor> p123 =
        Flux.using(
            () -> {
              EV3ColorSensor touch = new EV3ColorSensor(SensorPort.S2);
              return touch;
            },
            sampleProvider -> {
              sampleProvider.setCurrentMode("Ambient");

              return Flux.just(sampleProvider).mergeWith(Flux.empty());
            },
            sampleProvider -> {
              sampleProvider.close();
            });

    p123.flatMap(
            rmiSampleProvider -> {
              return Flux.range(0, 1000000000)
                  .map(
                      aLong -> {
                        try {
                          System.out.println();

                          float[] sample = new float[rmiSampleProvider.sampleSize()];
                          rmiSampleProvider.fetchSample(sample, 0);
                          return sample;
                        } catch (Exception ex) {
                          return new float[0];
                        }
                      })
                  .sample(Duration.ofSeconds(1))
                  .take(100);
            })
        .doOnNext(
            floats -> {
              for (float f : floats) {
                System.out.println(String.valueOf(f));
              }
            })
        .blockLast();
  }

  @Test
  void dsfsdf() {
    Scheduler sd = Schedulers.newElastic("sd");

    RSocket socket = RSocketFactory.connect().transport(TcpClientTransport.create("10.0.1.1", 7000)).start().block();

    socket
        .requestStream(DefaultPayload.create("SPEED,500"))
        .map(Payload::getData)
        .map(
            byteBuffer -> {
              FloatBuffer fb = byteBuffer.asFloatBuffer();
              float[] floatArray = new float[fb.limit()];
              fb.get(floatArray);

              return floatArray;
            })
        .doOnNext(s -> System.out.println("SPEED: " + s[0]))
        .subscribeOn(sd)
        .take(100)
        .sample(Duration.ofMillis(100))
        .then()
        .subscribe();

    socket
        .requestStream(DefaultPayload.create("TOUCH,500"))
        .map(Payload::getData)
        .map(
            byteBuffer -> {
              FloatBuffer fb = byteBuffer.asFloatBuffer();
              float[] floatArray = new float[fb.limit()];
              fb.get(floatArray);

              return floatArray;
            })
        .doOnNext(s -> System.out.println("TOUCH: " + s[0]))
        .subscribeOn(sd)
        .take(100)
        .sample(Duration.ofMillis(100))
        .then()
        .subscribe();

    socket
        .requestStream(DefaultPayload.create("INDICATOR,500"))
        .map(Payload::getData)
        .map(
            byteBuffer -> {
              FloatBuffer fb = byteBuffer.asFloatBuffer();
              float[] floatArray = new float[fb.limit()];
              fb.get(floatArray);

              return floatArray;
            })
        .doOnNext(
            floats -> {
              for (float f : floats) {
                System.out.println("INDICATOR: " + f);
              }
            })
        .subscribeOn(sd)
        .take(100)
        .sample(Duration.ofSeconds(1))
        .then()
        .doFinally(signalType -> socket.dispose())
        .block();
  }
}
