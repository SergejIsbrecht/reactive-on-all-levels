package de.herbstcampus.topic;

import javax.annotation.ParametersAreNonnullByDefault;
import reactor.core.publisher.Flux;

@ParametersAreNonnullByDefault
public final class SpeedTopic implements Topic<Double> {
  @Override
  public Flux<Double> stream$() {
    return null;
  }

  @Override
  public String name() {
    return "SPEED";
  }
}
