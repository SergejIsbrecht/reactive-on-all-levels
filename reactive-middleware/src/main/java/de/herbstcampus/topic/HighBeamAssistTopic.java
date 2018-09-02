package de.herbstcampus.topic;

import de.herbstcampus.api.Sensor;
import de.herbstcampus.model.ActivityState;
import de.herbstcampus.model.HighBeamState;
import de.herbstcampus.model.LightDetectionType;
import de.herbstcampus.model.TouchType;
import io.vavr.Tuple;
import io.vavr.control.Option;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import reactor.core.publisher.Flux;

@ParametersAreNonnullByDefault
public final class HighBeamAssistTopic implements Topic<HighBeamState> {
  static final long SPEED_THRESHOLD = 160L;
  static final long SAMPLE_RATE_SPEED = 100L;
  static final long SAMPLE_RATE_LIGHT = 100L;
  static final long SAMPLE_RATE_HIGH_BEAM_TOOGLE = 100L;

  private final Sensor<Float> speedSensor;
  private final Sensor<LightDetectionType> lightDetection;
  private final Flux<HighBeamState> highBeamState$;
  private final Flux<ActivityState> highBeamAssistantToggle;

  public HighBeamAssistTopic(Sensor<Float> speedSensor, Sensor<LightDetectionType> lightDetection, Sensor<TouchType> highBeamAssistantState) {
    this.speedSensor = Objects.requireNonNull(speedSensor);
    this.lightDetection = Objects.requireNonNull(lightDetection);
    this.highBeamAssistantToggle =
        Objects.requireNonNull(highBeamAssistantState)
            .stream$(SAMPLE_RATE_HIGH_BEAM_TOOGLE)
            .buffer(2, 1)
            .filter(touchTypes -> touchTypes.size() == 2)
            .filter(
                touchTypes -> {
                  return touchTypes.get(0) == TouchType.PRESSED && touchTypes.get(1) == TouchType.NOT_PRESSED;
                })
            .scan(
                ActivityState.NOT_ACTIVE,
                (activityState, trigger) -> {
                  if (activityState == ActivityState.NOT_ACTIVE) {
                    return ActivityState.IS_ACTIVE;
                  } else {
                    return ActivityState.NOT_ACTIVE;
                  }
                });

    // TODO: #autoConnect 0 is not a good idea, but persists state for now.
    this.highBeamState$ = composedState$().distinctUntilChanged().replay(1).autoConnect(0);
  }

  @Override
  public Flux<HighBeamState> stream$() {
    return highBeamState$;
  }

  @Override
  public String name() {
    return "HIGHBEAMASSIST";
  }

  /** Combines sensor-data from LightDetection, Speed and High-Beam (ON/OFF). */
  private Flux<HighBeamState> composedState$() {
    Flux<Option<LightDetectionType>> light$ = lightDetection.stream$(SAMPLE_RATE_LIGHT).map(Option::of).startWith(Option.<LightDetectionType>none());
    Flux<Option<Float>> speed$ = speedSensor.stream$(SAMPLE_RATE_SPEED).map(Option::of).startWith(Option.<Float>none()); // init with "invalid" value
    Flux<Option<ActivityState>> highBeam$ = highBeamAssistantToggle.map(Option::of).startWith(Option.<ActivityState>none());

    // make sure every stream$ has a initial value -> Flux#combineLatest will fire, when all values have a first value and after that every time a
    // stream$ emits a value.
    return Flux.combineLatest(
            light$,
            speed$,
            highBeam$,
            objects -> Tuple.of((Option<LightDetectionType>) objects[0], (Option<Double>) objects[1], (Option<ActivityState>) objects[2]))
        .filter(t -> t._1.isDefined() && t._2.isDefined() && t._3.isDefined())
        .map(t -> combine(t._1.get(), t._2.get(), t._3.get()));
  }

  private HighBeamState combine(LightDetectionType lightType, double speed, ActivityState highBeam) {
    if (highBeam == ActivityState.NOT_ACTIVE) {
      return HighBeamState.DISABLED;
    }

    boolean isSpeedThresholdExceeded = speed >= SPEED_THRESHOLD;
    if (isSpeedThresholdExceeded) {
      return HighBeamState.DISABLED_SPPEED_LIMIT;
    }
    boolean isLightDetected = lightType == LightDetectionType.DETECTED;
    if (isLightDetected) {
      return HighBeamState.DISABLED_LIGHT_DETECTED;
    }

    return HighBeamState.ENABLED;
  }
}
