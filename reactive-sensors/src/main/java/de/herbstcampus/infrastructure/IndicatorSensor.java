package de.herbstcampus.infrastructure;

import de.herbstcampus.api.Sensor;
import de.herbstcampus.model.IndicatorType;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import reactor.core.publisher.Flux;

@ParametersAreNonnullByDefault
public final class IndicatorSensor implements Sensor<IndicatorType> {
  private final SampleFacade<IndicatorType> sampleFacade;

  public IndicatorSensor(SampleFacadeFactory facadeFactory) {
    this.sampleFacade = Objects.requireNonNull(facadeFactory).create("", "", "");
  }

  @Override
  public Flux<IndicatorType> stream$(long sampleRate) {
    return sampleFacade.sample(
        sampleRate,
        floats -> {
          // TODO: IMPLEMENT
          return IndicatorType.LEFT;
        });
  }
}
