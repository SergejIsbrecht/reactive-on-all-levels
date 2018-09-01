package de.herbstcampus.infrastructure;

import de.herbstcampus.api.Sensor;
import de.herbstcampus.model.ActivityState;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import reactor.core.publisher.Flux;

@ParametersAreNonnullByDefault
public final class HighBeamAssistantSensor implements Sensor<ActivityState> {
  private final SampleFacade<ActivityState> sampleFacade;

  public HighBeamAssistantSensor(SampleFacade<ActivityState> sampleFacade) {
    this.sampleFacade = Objects.requireNonNull(sampleFacade);
  }

  @Override
  public Flux<ActivityState> stream$(long sampleRate) {
    return sampleFacade.sample(
        sampleRate,
        floats -> {
          // TODO: implement
          return ActivityState.IS_ACTIVE;
        });
  }
}
