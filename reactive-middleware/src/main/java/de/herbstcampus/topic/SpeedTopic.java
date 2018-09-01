package de.herbstcampus.topic;

import de.herbstcampus.api.Sensor;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import reactor.core.publisher.Flux;

@ParametersAreNonnullByDefault
public final class SpeedTopic implements Topic<Double> {
  private static final long SAMPLE_RATE_SPEED = 500;
  private final Flux<Double> speed$;

  public SpeedTopic(Sensor<Double> speed) {
    this.speed$ = Objects.requireNonNull(speed).stream$(SAMPLE_RATE_SPEED).distinctUntilChanged().replay(1).refCount();
  }

  @Override
  public Flux<Double> stream$() {
    return speed$;
  }

  @Override
  public String name() {
    return "SPEED";
  }
}
