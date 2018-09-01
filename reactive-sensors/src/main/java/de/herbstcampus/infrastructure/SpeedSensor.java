package de.herbstcampus.infrastructure;

import de.herbstcampus.api.Sensor;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import reactor.core.publisher.Flux;

@ParametersAreNonnullByDefault
public final class SpeedSensor implements Sensor<Double> {
  private final SampleFacade<Double> sampleFacade;

  public SpeedSensor(SampleFacade<Double> sampleFacade) {
    this.sampleFacade = Objects.requireNonNull(sampleFacade);
  }

  @Override
  public Flux<Double> stream$(long sampleRate) {
    return sampleFacade.sample(
        sampleRate,
        floats -> {
          // TODO: implement
          return -1d;
        });
  }
}
