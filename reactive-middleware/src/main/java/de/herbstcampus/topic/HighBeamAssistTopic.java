package de.herbstcampus.topic;

import de.herbstcampus.model.ActivityState;
import javax.annotation.ParametersAreNonnullByDefault;
import reactor.core.publisher.Flux;

@ParametersAreNonnullByDefault
public final class HighBeamAssistTopic implements Topic<ActivityState> {
  @Override
  public Flux<ActivityState> stream$() {
    return null;
  }

  @Override
  public String name() {
    return "HIGHBEAMASSIST";
  }
}
