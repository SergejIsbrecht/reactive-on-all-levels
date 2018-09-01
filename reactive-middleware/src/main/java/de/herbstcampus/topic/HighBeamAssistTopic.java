package de.herbstcampus.topic;

import de.herbstcampus.api.Sensor;
import de.herbstcampus.model.ActivityState;
import de.herbstcampus.model.HighBeamState;
import de.herbstcampus.model.LightDetectionType;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import reactor.core.publisher.Flux;

@ParametersAreNonnullByDefault
public final class HighBeamAssistTopic implements Topic<HighBeamState> {
  static final long SPEED_THRESHOLD = 160L;
  static final long SAMPLE_RATE_SPEED = 100L;
  static final long SAMPLE_RATE_LIGHT = 100L;
  static final long SAMPLE_RATE_HIGH_BEAM_TOOGLE = 100L;

  private final Sensor<Double> speedSensor;
  private final Sensor<LightDetectionType> lightDetection;
  private final Sensor<ActivityState> highBeamAssistantState;
  private final Flux<HighBeamState> highBeamState$;

  public HighBeamAssistTopic(Sensor<Double> speedSensor, Sensor<LightDetectionType> lightDetection, Sensor<ActivityState> highBeamAssistantState) {
    this.speedSensor = Objects.requireNonNull(speedSensor);
    this.lightDetection = Objects.requireNonNull(lightDetection);
    this.highBeamAssistantState = Objects.requireNonNull(highBeamAssistantState);

    this.highBeamState$ = composedState$().distinctUntilChanged().publish().refCount();
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
    Flux<LightDetectionType> light$ = lightDetection.stream$(SAMPLE_RATE_LIGHT).startWith(LightDetectionType.NOT_DETECTED);
    Flux<Double> speed$ = speedSensor.stream$(SAMPLE_RATE_SPEED).startWith(new Double(-1)); // init with "invalid" value
    Flux<ActivityState> highBeam$ = highBeamAssistantState.stream$(SAMPLE_RATE_HIGH_BEAM_TOOGLE).startWith(ActivityState.NOT_ACTIVE);

    // make sure every stream has a initial value -> Flux#combineLatest will fire, when all values have a first value and after that every time a
    // stream emits a value.
    return Flux.combineLatest(
        light$, speed$, highBeam$, objects -> combine((LightDetectionType) objects[0], (Double) objects[1], (ActivityState) objects[2]));
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
