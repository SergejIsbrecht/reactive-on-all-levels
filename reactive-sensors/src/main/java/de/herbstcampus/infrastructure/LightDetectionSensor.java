package de.herbstcampus.infrastructure;

import de.herbstcampus.api.Sensor;
import de.herbstcampus.model.LightDetectionType;
import javax.annotation.ParametersAreNonnullByDefault;
import reactor.core.publisher.Flux;

@ParametersAreNonnullByDefault
public final class LightDetectionSensor implements Sensor<LightDetectionType> {
  @Override
  public Flux<LightDetectionType> stream$(long sampleRate) {
    return null;
  }
}
