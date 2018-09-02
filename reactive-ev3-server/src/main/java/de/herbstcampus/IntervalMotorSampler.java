package de.herbstcampus;

import de.herbstcampus.model.ImmutableMotorEvent;
import de.herbstcampus.model.MotorEvent;
import de.herbstcampus.model.MotorRotationType;
import java.time.Duration;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.RegulatedMotorListener;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;

@ParametersAreNonnullByDefault
final class IntervalMotorSampler implements DataSampler {
  private final RegulatedMotor motor;
  private final Scheduler scheduler;
  private final Flux<MotorEvent> multiCastListener$;

  private IntervalMotorSampler(RegulatedMotor motor, Scheduler scheduler) {
    this.motor = Objects.requireNonNull(motor);
    this.scheduler = Objects.requireNonNull(scheduler);
    this.multiCastListener$ =
        Flux.<MotorEvent>create(
                fluxSink -> {
                  System.out.println("ENTER MULTICAST");

                  motor.addListener(
                      new RegulatedMotorListener() {
                        private ImmutableMotorEvent.Builder builder = ImmutableMotorEvent.builder();

                        @Override
                        public void rotationStarted(RegulatedMotor motor, int tachoCount, boolean stalled, long timeStamp) {
                          System.out.println("listener callback called");

                          // int limitAngle = motor.getLimitAngle();
                          // int speed = motor.getSpeed();
                          // int rotationSpeed = motor.getRotationSpeed();
                          fluxSink.next(
                              builder
                                  .stalled(stalled)
                                  .tachoCount(tachoCount)
                                  .timeStamp(timeStamp)
                                  .type(MotorRotationType.STARTED)
                                  .limitAngle(0)
                                  .speed(0)
                                  .rotationSpeed(0)
                                  .build());
                        }

                        @Override
                        public void rotationStopped(RegulatedMotor motor, int tachoCount, boolean stalled, long timeStamp) {
                          System.out.println("listener callback called");

                          // int limitAngle = motor.getLimitAngle();
                          // int speed = motor.getSpeed();
                          // int rotationSpeed = motor.getRotationSpeed();
                          fluxSink.next(
                              builder
                                  .stalled(stalled)
                                  .tachoCount(tachoCount)
                                  .timeStamp(timeStamp)
                                  .type(MotorRotationType.STOPPED)
                                  .limitAngle(0)
                                  .speed(0)
                                  .rotationSpeed(0)
                                  .build());
                        }
                      });

                  fluxSink.onCancel(motor::removeListener); // remove listener when no own listens
                })
            .subscribeOn(scheduler)
            .publish()
            .refCount(1, Duration.ofSeconds(5), scheduler);
  }

  static IntervalMotorSampler sampleMotor(RegulatedMotor motor, Scheduler scheduler) {
    return new IntervalMotorSampler(motor, scheduler);
  }

  @Override
  public Flux<float[]> sample(long sampleRate) {
    return Flux.interval(Duration.ofMillis(sampleRate), scheduler)
        .map(
            aLong -> {
              MotorEvent build =
                  ImmutableMotorEvent.builder()
                      .stalled(motor.isStalled())
                      .tachoCount(motor.getTachoCount())
                      .limitAngle(motor.getLimitAngle())
                      .speed(motor.getSpeed())
                      .rotationSpeed(motor.getRotationSpeed())
                      .build();
              return build;
            })
        // .mergeWith(multiCastListener$)
        .map(
            motorEvent -> {
              float[] array = motorEvent.toFloatArry();
              return array;
            })
        .doOnNext(
            floats -> {
              for (float f : floats) {
                System.out.println("Float-A " + floats);
              }
            });
  }
}
