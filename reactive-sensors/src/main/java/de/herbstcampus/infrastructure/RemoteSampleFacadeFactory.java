package de.herbstcampus.infrastructure;

import de.herbstcampus.api.SampleFacade;
import de.herbstcampus.model.MotorEvent;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;
import java.nio.FloatBuffer;
import java.time.Duration;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@ParametersAreNonnullByDefault
class RemoteSampleFacadeFactory {
  private final Scheduler intervalScheduler;
  private final Mono<RSocket> socket;

  RemoteSampleFacadeFactory(String ip, Scheduler intervalScheduler) {
    // "10.0.1.1"
    this.socket =
        RSocketFactory.connect()
            .transport(TcpClientTransport.create(ip, 7000))
            .start()
            .timeout(Duration.ofSeconds(5), intervalScheduler)
            .repeatWhenEmpty(
                100,
                longFlux -> {
                  return longFlux.map(Duration::ofSeconds).switchMap(duration -> Flux.interval(duration, intervalScheduler).take(1));
                })
            .retryWhen(
                throwableFlux -> {
                  return throwableFlux
                      .zipWith(Flux.range(1, 100), (throwable, integer) -> Duration.ofSeconds(integer))
                      .flatMap(duration -> Flux.interval(duration, intervalScheduler).take(1));
                })
            .cache();
    this.intervalScheduler = Objects.requireNonNull(intervalScheduler);
  }

  SampleFacade<MotorEvent> sampleRegulatedMotor(String resourceName) {
    return sampleRate -> {
      sample(resourceName, sampleRate)
          .map(
              floats -> {
                return null;
              });
      return null;
    };
  }

  SampleFacade<float[]> sampleSensor(String resourceName) {
    return sampleRate -> sample(resourceName, sampleRate);
  }

  private Flux<float[]> sample(String resourceName, long sampleRate) {
    Flux<float[]> flux =
        socket.flatMapMany(
            rSocket -> {
              return rSocket
                  .requestStream(DefaultPayload.create(resourceName + "," + sampleRate))
                  .map(Payload::getData)
                  .map(
                      byteBuffer -> {
                        FloatBuffer fb = byteBuffer.asFloatBuffer();
                        float[] floatArray = new float[fb.limit()];
                        fb.get(floatArray);
                        return floatArray;
                      })
                  .retryWhen(
                      throwableFlux -> {
                        return throwableFlux
                            .zipWith(Flux.range(1, 100), (throwable, integer) -> Duration.ofSeconds(integer))
                            .flatMap(duration -> Flux.interval(duration, intervalScheduler).take(1));
                      })
                  .subscribeOn(intervalScheduler);
            });
    return flux;
  }
}
