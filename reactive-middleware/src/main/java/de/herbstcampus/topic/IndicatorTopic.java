package de.herbstcampus.topic;

import de.herbstcampus.api.Sensor;
import de.herbstcampus.model.IndicatorType;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import reactor.core.publisher.Flux;

@ParametersAreNonnullByDefault
public final class IndicatorTopic implements Topic<IndicatorType> {
  private static final long INDICATOR_SAMPLE_RATE = 100L;

  private final Flux<IndicatorType> indicator$;

  public IndicatorTopic(Sensor<IndicatorType> indicatorSensor) {
    this.indicator$ =
        Objects.requireNonNull(indicatorSensor)
            .stream$(INDICATOR_SAMPLE_RATE)
            .distinctUntilChanged()
            .doOnNext(indicatorType -> System.out.println("[SENSOR][IndicatorTopic] IndicatorType:  " + indicatorType))
            .replay(1)
            .refCount();
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
