package de.herbstcampus.infrastructure;

import de.herbstcampus.api.Sensor;
import de.herbstcampus.model.ActivityState;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import reactor.core.publisher.Flux;

@ParametersAreNonnullByDefault
public final class HighBeamAssistantSensor implements Sensor<ActivityState> {
  private final SampleFacade<ActivityState> sampleFacade;

  public HighBeamAssistantSensor(RemoteSampleFacadeFactory facadeFactory) {
    this.sampleFacade = Objects.requireNonNull(facadeFactory).create("", "", "");
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
