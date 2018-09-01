package de.herbstcampus.infrastructure;

import de.herbstcampus.api.Sensor;
import javax.annotation.ParametersAreNonnullByDefault;
import reactor.core.publisher.Flux;

@ParametersAreNonnullByDefault
public final class SpeedSensor implements Sensor<Double> {
  @Override
  public Flux<Double> stream$(long sampleRate) {
    return null;
  }
}
