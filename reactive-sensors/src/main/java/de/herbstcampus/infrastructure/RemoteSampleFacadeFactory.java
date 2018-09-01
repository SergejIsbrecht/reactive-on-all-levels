package de.herbstcampus.infrastructure;

import io.vavr.Lazy;
import io.vavr.control.Try;
import java.time.Duration;
import javax.annotation.ParametersAreNonnullByDefault;
import lejos.remote.ev3.RemoteEV3;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;

@ParametersAreNonnullByDefault
class RemoteSampleFacadeFactory {
  private final Scheduler intervalScheduler;
  private final Lazy<Try<RemoteEV3>> ev3;

  RemoteSampleFacadeFactory(String ip, Scheduler intervalScheduler) {
    this.ev3 = Lazy.of(() -> Try.of(() -> new RemoteEV3(ip)));
    this.intervalScheduler = intervalScheduler;
  }

  <T> SampleFacade<T> create(String portName, String sensorName, String modeName) {
    return (sampleRate, mapper) ->
        ev3.get()
            .map(
                remoteEV3 -> {
                  return remoteEV3.createSampleProvider(portName, sensorName, modeName);
                })
            .map(
                provider -> {
                  return Flux.interval(Duration.ofMillis(sampleRate), intervalScheduler)
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
}
