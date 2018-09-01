package de.herbstcampus.infrastructure;

import de.herbstcampus.api.Sensor;
import de.herbstcampus.api.SensorSampleFacade;
import de.herbstcampus.model.ActivityState;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import reactor.core.publisher.Flux;

@ParametersAreNonnullByDefault
public final class HighBeamAssistantSensor implements Sensor<ActivityState> {
  private final SensorSampleFacade<ActivityState> sensorSampleFacade;

  public HighBeamAssistantSensor(RemoteSampleFacadeFactory facadeFactory) {
    this.sensorSampleFacade = Objects.requireNonNull(facadeFactory).sampleSensor("S1", "lejos.hardware.sensor.EV3TouchSensor", "Touch");
  }

  @Override
  public Flux<ActivityState> stream$(long sampleRate) {
    return sensorSampleFacade.sample(
        sampleRate,
        floats -> {
          // TODO: implement
          return ActivityState.IS_ACTIVE;
        });
  }
}
