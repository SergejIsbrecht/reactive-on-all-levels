package de.herbstcampus.infrastructure;

import de.herbstcampus.model.ImmutableMotorEvent;
import de.herbstcampus.model.MotorEvent;
import de.herbstcampus.model.MotorRotationType;
import io.vavr.Lazy;
import io.vavr.control.Try;
import java.time.Duration;
import javax.annotation.ParametersAreNonnullByDefault;
import lejos.remote.ev3.RMIRegulatedMotor;
import lejos.remote.ev3.RemoteEV3;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.RegulatedMotorListener;
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

  <T> MotorSampleFacade<T> sampleRegulatedMotor(String portName, char motorType) {
    return (sampleRate, mapper) -> {
      Try<RMIRegulatedMotor> motor = ev3.get().map(remoteEV3 -> remoteEV3.createRegulatedMotor(portName, motorType));
      return motor
          .map(
              rmiRegulatedMotor -> {
                Flux<T> motorEvent$ =
                    // TODO: sampleRate for Listener-Callback
                    Flux.<MotorEvent>create(
                            fluxSink -> {
                              RegulatedMotorListener regulatedMotorListener =
                                  new RegulatedMotorListener() {
                                    @Override
                                    public void rotationStarted(RegulatedMotor motor, int tachoCount, boolean stalled, long timeStamp) {
                                      fluxSink.next(
                                          ImmutableMotorEvent.builder()
                                              .stalled(stalled)
                                              .tachoCount(tachoCount)
                                              .timeStamp(timeStamp)
                                              .type(MotorRotationType.STARTED)
                                              .build());
                                    }

                                    @Override
                                    public void rotationStopped(RegulatedMotor motor, int tachoCount, boolean stalled, long timeStamp) {
                                      fluxSink.next(
                                          ImmutableMotorEvent.builder()
                                              .stalled(stalled)
                                              .tachoCount(tachoCount)
                                              .timeStamp(timeStamp)
                                              .type(MotorRotationType.STOPPED)
                                              .build());
                                    }
                                  };

                              try {
                                rmiRegulatedMotor.addListener(regulatedMotorListener);
                              } catch (Exception ex) {
                                fluxSink.error(ex);
                              }

                              fluxSink.onCancel(
                                  () -> {
                                    try {
                                      rmiRegulatedMotor.removeListener();
                                    } catch (Exception ex) {
                                      fluxSink.error(ex);
                                    }
                                  });
                            })
                        .map(mapper);

                return motorEvent$;
              })
          .getOrElseGet(Flux::error);
    };
  }

  <T> SensorSampleFacade<T> sampleSensor(String portName, String sensorName, String modeName) {
    return (sampleRate, mapper) ->
        ev3.get()
            .map(remoteEV3 -> remoteEV3.createSampleProvider(portName, sensorName, modeName))
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
