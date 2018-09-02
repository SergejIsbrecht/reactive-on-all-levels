package de.herbstcampus.infrastructure;

import de.herbstcampus.api.SampleFacade;
import de.herbstcampus.model.ImmutableMotorEvent;
import de.herbstcampus.model.MotorEvent;
import de.herbstcampus.model.MotorRotationType;
import io.vavr.Lazy;
import io.vavr.control.Try;
import java.time.Duration;
import javax.annotation.ParametersAreNonnullByDefault;
import lejos.remote.ev3.RMIRegulatedMotor;
import lejos.remote.ev3.RMISampleProvider;
import lejos.remote.ev3.RemoteEV3;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.RegulatedMotorListener;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@ParametersAreNonnullByDefault
class RemoteSampleFacadeFactory {
  private final Scheduler intervalScheduler;
  private final Lazy<Try<RemoteEV3>> ev3;

  RemoteSampleFacadeFactory(String ip, Scheduler intervalScheduler) {
    this.ev3 = Lazy.of(() -> Try.of(() -> new RemoteEV3(ip)));
    this.intervalScheduler = intervalScheduler;
  }

  SampleFacade<MotorEvent> sampleRegulatedMotor(String portName, char motorType) {
    return (sampleRate) -> {
      Mono<RMIRegulatedMotor> connection =
          Mono.defer(() -> ev3.get().map(remoteEV3 -> remoteEV3.createRegulatedMotor(portName, motorType)).map(Mono::just).getOrElseGet(Mono::error))
              .timeout(Duration.ofMillis(5_000), intervalScheduler);

      return connection.flatMapMany(
          rmiRegulatedMotor -> {
            // TODO: sampleRate for Listener-Callback
            return Flux.<MotorEvent>create(
                fluxSink -> {
                  RegulatedMotorListener regulatedMotorListener =
                      new RegulatedMotorListener() {
                        private ImmutableMotorEvent.Builder builder = ImmutableMotorEvent.builder();

                        @Override
                        public void rotationStarted(RegulatedMotor motor, int tachoCount, boolean stalled, long timeStamp) {
                          int limitAngle = motor.getLimitAngle();
                          int speed = motor.getSpeed();
                          int rotationSpeed = motor.getRotationSpeed();

                          fluxSink.next(
                              builder
                                  .stalled(stalled)
                                  .tachoCount(tachoCount)
                                  .timeStamp(timeStamp)
                                  .type(MotorRotationType.STARTED)
                                  .limitAngle(limitAngle)
                                  .speed(speed)
                                  .rotationSpeed(rotationSpeed)
                                  .build());
                        }

                        @Override
                        public void rotationStopped(RegulatedMotor motor, int tachoCount, boolean stalled, long timeStamp) {
                          int limitAngle = motor.getLimitAngle();
                          int speed = motor.getSpeed();
                          int rotationSpeed = motor.getRotationSpeed();

                          fluxSink.next(
                              builder
                                  .stalled(stalled)
                                  .tachoCount(tachoCount)
                                  .timeStamp(timeStamp)
                                  .type(MotorRotationType.STOPPED)
                                  .limitAngle(limitAngle)
                                  .speed(speed)
                                  .rotationSpeed(rotationSpeed)
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
                });
          });
    };
  }

  SampleFacade<float[]> sampleSensor(String portName, String sensorName, String modeName) {
    return (sampleRate) -> {
      Mono<RMISampleProvider> connection =
          Mono.defer(
                  () ->
                      ev3.get()
                          .map(
                              remoteEV3 -> {
                                return Mono.using(
                                    () -> remoteEV3.createSampleProvider(portName, sensorName, modeName),
                                    Mono::just,
                                    rmiSampleProvider -> {
                                      try {
                                        rmiSampleProvider.close();
                                      } catch (Exception ex) {
                                        throw new RuntimeException(ex);
                                      }
                                    });
                              })
                          .getOrElseGet(Mono::error))
              .timeout(Duration.ofMillis(10_000), intervalScheduler);

      return connection.flatMapMany(
          provider -> {
            return Flux.interval(Duration.ofMillis(sampleRate), intervalScheduler)
                .flatMap(
                    aLong -> {
                      Try<float[]> map = Try.of(provider::fetchSample);
                      if (map.isSuccess()) {
                        return Flux.just(map.get());
                      } else {
                        return Flux.error(map.getCause());
                      }
                    });
          });
    };
  }
}
