package de.herbstcampus.infrastructure;

import io.vavr.Lazy;
import io.vavr.control.Try;
import java.time.Duration;
import java.util.function.Function;
import javax.annotation.ParametersAreNonnullByDefault;
import lejos.remote.ev3.RemoteEV3;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;

@ParametersAreNonnullByDefault
public final class SampleFacadeFactory<T> {
  private final Scheduler scheduler;
  private final Lazy<Try<RemoteEV3>> ev3;

  public SampleFacadeFactory(String ip, Scheduler scheduler) {
    this.ev3 = Lazy.of(() -> Try.of(() -> new RemoteEV3(ip)));
    this.scheduler = scheduler;
  }

  public SampleFacade<T> create(String portName, String sensorName, String modeName) {
    return new SampleFacade<T>() {
      @Override
      public Flux<T> sample(long sampleRate, Function<float[], T> mapper) {
        return ev3.get()
            .map(
                remoteEV3 -> {
                  return remoteEV3.createSampleProvider(portName, sensorName, modeName);
                })
            .map(
                provider -> {
                  return Flux.interval(Duration.ofMillis(sampleRate), scheduler)
                      .flatMap(
                          aLong -> {
                            Try<T> map = Try.of(provider::fetchSample).map(mapper);
                            if (map.isSuccess()) {
                              return Flux.just(map.get());
                            } else {
                              return Flux.error(map.getCause());
                            }
                          });
                })
            .getOrElseGet(Flux::error);
      }
    };
  }
}
