package de.herbstcampus.topic;

import java.time.Duration;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import de.herbstcampus.api.Sensor;
import de.herbstcampus.model.ActivityState;
import de.herbstcampus.model.HighBeamState;
import de.herbstcampus.model.LightDetectionType;
import de.herbstcampus.model.TouchType;
import io.vavr.Tuple;
import io.vavr.control.Option;
import reactor.core.publisher.Flux;

@ParametersAreNonnullByDefault
public final class HighBeamAssistTopic implements Topic<HighBeamState> {
  static final long SPEED_THRESHOLD = 130L;
  static final long SAMPLE_RATE_SPEED = 100L;
  static final long SAMPLE_RATE_LIGHT = 100L;
  static final long SAMPLE_RATE_HIGH_BEAM_TOOGLE = 100L;

  private final Sensor<Float> speedSensor;
  private final Sensor<LightDetectionType> lightDetectionSensor;
  private final Flux<HighBeamState> highBeamState$;
  private final Flux<ActivityState> highBeamAssistantToggle;

  public HighBeamAssistTopic(Sensor<Float> speedSensor, Sensor<LightDetectionType> lightDetectionSensor, Sensor<TouchType> highBeamAssistantState) {
    this.speedSensor = Objects.requireNonNull(speedSensor);
    this.lightDetectionSensor = Objects.requireNonNull(lightDetectionSensor);
    this.highBeamAssistantToggle =
        Objects.requireNonNull(highBeamAssistantState)
            .stream$(SAMPLE_RATE_HIGH_BEAM_TOOGLE)
            // overlapping-buffer
            .buffer(2, 1)
            .filter(touchTypes -> touchTypes.size() == 2)
            .filter(
                touchTypes -> {
                  // only trigger update, when button was PRESSED and NOT_PRESSED (button down / button up)
                  return touchTypes.get(0) == TouchType.PRESSED && touchTypes.get(1) == TouchType.NOT_PRESSED;
                })
            // seed value will be emitted instant on sub
            .scan(
                ActivityState.NOT_ACTIVE,
                (activityState, trigger) -> {
                  // toggle state every time the button was pressed
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

    Flux<Option<LightDetectionType>> light$ =
        lightDetectionSensor
            .stream$(SAMPLE_RATE_LIGHT)
            // https://stackoverflow.com/questions/30140044/deliver-the-first-item-immediately-debounce-following-items
            .publish(publishedItems -> publishedItems.take(1).concatWith(publishedItems.skip(1).sample(Duration.ofMillis(250))))
            .map(Option::of)
            .startWith(Option.<LightDetectionType>none());
    Flux<Option<Float>> speed$ = speedSensor.stream$(SAMPLE_RATE_SPEED).map(Option::of).startWith(Option.<Float>none()); // init with "invalid" value
    Flux<Option<ActivityState>> highBeam$ = highBeamAssistantToggle.map(Option::of).startWith(Option.<ActivityState>none());

    // make sure every stream$ has a initial value -> Flux#combineLatest will fire, when all values have a first value and after that every time a
    // stream$ emits a value.
    return Flux.combineLatest(
            light$,
            speed$,
            highBeam$,
            objects -> Tuple.of((Option<LightDetectionType>) objects[0], (Option<Float>) objects[1], (Option<ActivityState>) objects[2]))
        .distinctUntilChanged()
        .doOnNext(tuple -> System.out.println("[TOPIC][HIGH_BEAM_ASSISTANT] tuple: " + tuple))
        // only interested in valid values
        .map(
            t -> {
              boolean allAreValid = t._1.isDefined() && t._2.isDefined() && t._3.isDefined();
              if (allAreValid) {
                return combine(t._1.get(), t._2.get(), t._3.get());
              } else {
                return HighBeamState.FAILURE;
              }
            })
        .distinctUntilChanged()
        .doOnNext(highBeamState -> System.out.println("[TOPIC][HIGH_BEAM_ASSISTANT] value: " + highBeamState));
  }

  private HighBeamState combine(LightDetectionType lightType, double speed, ActivityState highBeam) {
    if (highBeam == ActivityState.NOT_ACTIVE) {
      return HighBeamState.DISABLED;
    }

    boolean isSpeedThresholdExceeded = speed >= SPEED_THRESHOLD;
    if (isSpeedThresholdExceeded) {
      return HighBeamState.DISABLED_SPEED_LIMIT;
    }
    boolean isLightDetected = lightType == LightDetectionType.DETECTED;
    if (isLightDetected) {
      return HighBeamState.DISABLED_LIGHT_DETECTED;
    }

    return HighBeamState.ENABLED;
  }
}
