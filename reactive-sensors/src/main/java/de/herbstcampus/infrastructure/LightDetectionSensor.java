package de.herbstcampus.infrastructure;

import de.herbstcampus.api.Sensor;
import de.herbstcampus.model.LightDetectionType;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import reactor.core.publisher.Flux;

@ParametersAreNonnullByDefault
public final class LightDetectionSensor implements Sensor<LightDetectionType> {
  private final SampleFacade<LightDetectionType> sampleFacade;

  public LightDetectionSensor(SampleFacadeFactory facadeFactory) {
    this.sampleFacade = Objects.requireNonNull(facadeFactory).create("", "", "");
  }

  @Override
  public Flux<LightDetectionType> stream$(long sampleRate) {
    return sampleFacade.sample(
        sampleRate,
        floats -> {
          // TODO: implement
          return LightDetectionType.DETECTED;
        });
  }
}
