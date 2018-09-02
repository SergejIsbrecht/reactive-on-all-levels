package de.herbstcampus;

import de.herbstcampus.model.ImmutableMotorEvent;
import de.herbstcampus.model.MotorEvent;
import java.time.Duration;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import lejos.robotics.RegulatedMotor;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;

@ParametersAreNonnullByDefault
final class IntervalMotorSampler implements DataSampler {
  private final RegulatedMotor motor;
  private final Scheduler scheduler;

  private IntervalMotorSampler(RegulatedMotor motor, Scheduler scheduler) {
    this.motor = Objects.requireNonNull(motor);
    this.scheduler = Objects.requireNonNull(scheduler);
  }

  static IntervalMotorSampler sampleMotor(RegulatedMotor motor, Scheduler scheduler) {
    return new IntervalMotorSampler(motor, scheduler);
  }

  @Override
  public Flux<float[]> sample(long sampleRate) {
    return Flux.interval(Duration.ofMillis(sampleRate), scheduler)
        .map(
            aLong -> {
              MotorEvent build = ImmutableMotorEvent.of(motor.getTachoCount());
              return build;
            })
        .doOnNext(
            motorEvent -> {
              System.out.println("MOTOREVENT: " + motorEvent);
            })
        .map(
            motorEvent -> {
              float[] array = motorEvent.toFloatArry();
              return array;
            });
  }
}
