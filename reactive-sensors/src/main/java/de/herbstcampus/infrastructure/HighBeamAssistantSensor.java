package de.herbstcampus.infrastructure;

import de.herbstcampus.api.Sensor;
import de.herbstcampus.model.TouchType;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import reactor.core.publisher.Flux;

@ParametersAreNonnullByDefault
public final class HighBeamAssistantSensor implements Sensor<TouchType> {
  private final Sensor<float[]> sensorSampleFacade;

  public HighBeamAssistantSensor(RemoteSampleFacadeFactory facadeFactory) {
    this.sensorSampleFacade = Objects.requireNonNull(facadeFactory).sampleSensor("TOUCH");
  }

  @Override
  public Flux<TouchType> stream$(long sampleRate) {
    return sensorSampleFacade
        .stream$(sampleRate)
        .map(
            floats -> {
              if (floats.length > 1) {
                return TouchType.INVALID;
              }

              float aFloat = floats[0];
              if (aFloat == 0) {
                return TouchType.NOT_PRESSED;
              } else if (aFloat == 1) {
                return TouchType.PRESSED;
              } else {
                return TouchType.INVALID;
              }
            });
  }
}
