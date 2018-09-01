package de.herbstcampus.topic;

import de.herbstcampus.model.IdicatorType;
import javax.annotation.ParametersAreNonnullByDefault;
import reactor.core.publisher.Flux;

@ParametersAreNonnullByDefault
public final class IndicatorTopic implements Topic<IdicatorType> {
  @Override
  public Flux<IdicatorType> stream$() {
    return null;
  }

  @Override
  public String name() {
    return "INDICATOR";
  }
}
