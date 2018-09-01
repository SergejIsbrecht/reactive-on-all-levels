package de.herbstcampus.topic;

import de.herbstcampus.api.Sensor;
import de.herbstcampus.model.IndicatorType;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import reactor.core.publisher.Flux;

@ParametersAreNonnullByDefault
public final class IndicatorTopic implements Topic<IndicatorType> {
  private static final long INDICATOR_SAMPLE_RATE = 100L;

  private final Sensor<IndicatorType> indicatorSensor;
  private final Flux<IndicatorType> indicator$;

  public IndicatorTopic(Sensor<IndicatorType> indicatorSensor) {
    this.indicatorSensor = Objects.requireNonNull(indicatorSensor);
    this.indicator$ = indicatorSensor.stream$(INDICATOR_SAMPLE_RATE).publish().refCount();
  }

  @Override
  public Flux<IndicatorType> stream$() {
    return indicator$;
  }

  @Override
  public String name() {
    return "INDICATOR";
  }
}
