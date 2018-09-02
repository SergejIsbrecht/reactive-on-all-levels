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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@ParametersAreNonnullByDefault
class RemoteSampleFacadeFactory {
  private final Scheduler intervalScheduler;
  private final Mono<RSocket> socket;

  RemoteSampleFacadeFactory(String ip, Scheduler intervalScheduler) {
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
            .doOnError(throwable -> System.out.println("[SOCKET] Socket connection failed to sensors."))
            .cache();
    this.intervalScheduler = Objects.requireNonNull(intervalScheduler);
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
