package de.herbstcampus.infrastructure;

import de.herbstcampus.api.Sensor;
import de.herbstcampus.model.MotorEvent;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.time.Duration;
import java.util.Objects;
import java.util.function.Function;
import javax.annotation.ParametersAreNonnullByDefault;
import org.reactivestreams.Publisher;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@ParametersAreNonnullByDefault
class RemoteSampleFacade implements Disposable {
  private final Scheduler intervalScheduler;
  private final Mono<RSocket> socket;

  RemoteSampleFacade(String ip, Scheduler intervalScheduler) {
    this.socket =
        RSocketFactory.connect()
            .transport(TcpClientTransport.create(ip, 7000))
            .start()
            .timeout(Duration.ofSeconds(5), intervalScheduler)
            .compose(repeat(100, intervalScheduler))
            .retryWhen(retry(100, intervalScheduler))
            .doOnError(throwable -> System.out.println("[SOCKET] Socket connection failed to sensors."))
            .cache();
    this.intervalScheduler = Objects.requireNonNull(intervalScheduler);
  }

  private static <T> Function<Flux<Throwable>, Publisher<Long>> retry(int count, Scheduler scheduler) {
    return throwableFlux -> {
      Flux<Long> longFlux =
          throwableFlux
              .zipWith(Flux.range(1, count), (throwable, integer) -> Duration.ofSeconds(integer))
              .flatMap(duration -> Flux.interval(duration, scheduler).take(1));
      return longFlux;
    };
  }

  private static <T> Function<Mono<T>, Mono<T>> repeat(int count, Scheduler scheduler) {
    return upstream -> {
      return upstream.repeatWhenEmpty(
          count, longFlux -> longFlux.map(Duration::ofSeconds).switchMap(duration -> Flux.interval(duration, scheduler).take(1)));
    };
  }

  Sensor<MotorEvent> sampleRegulatedMotor(String resourceName) {
    return sampleRate -> sample(resourceName, sampleRate, MotorEvent::fromByteBuffer);
  }

  Sensor<float[]> sampleSensor(String resourceName) {
    return sampleRate ->
        sample(
            resourceName,
            sampleRate,
            byteBuffer -> {
              FloatBuffer fb = byteBuffer.asFloatBuffer();
              float[] floatArray = new float[fb.limit()];
              fb.get(floatArray);
              return floatArray;
            });
  }

  private <T> Flux<T> sample(String resourceName, long sampleRate, Function<ByteBuffer, T> mapper) {
    Flux<T> flux =
        socket.flatMapMany(
            rSocket -> {
              return rSocket
                  .requestStream(DefaultPayload.create(resourceName + "," + sampleRate))
                  .map(Payload::getData)
                  .map(mapper)
                  .retryWhen(retry(100, intervalScheduler))
                  .doOnNext(t -> System.out.println("[REMOTE_FACADE] Sample resource: " + resourceName + " with sampleRate " + sampleRate))
                  .subscribeOn(intervalScheduler);
            });
    return flux;
  }

  @Override
  public void dispose() {
    socket.timeout(Duration.ofSeconds(5)).subscribe(Disposable::dispose, Throwable::printStackTrace);
  }
}
