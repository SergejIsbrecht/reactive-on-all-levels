package de.herbstcampus.infrastructure;

import de.herbstcampus.api.Sensor;
import de.herbstcampus.model.IndicatorType;
import javax.annotation.ParametersAreNonnullByDefault;
import reactor.core.publisher.Flux;

@ParametersAreNonnullByDefault
public final class IndicatorSensor implements Sensor<IndicatorType> {
  @Override
  public Flux<IndicatorType> stream$(long sampleRate) {
    return null;
  }
}
