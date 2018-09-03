package de.herbstcampus.infrastructure;

import de.herbstcampus.api.Sensor;
import de.herbstcampus.model.LightDetectionType;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import reactor.core.publisher.Flux;

@ParametersAreNonnullByDefault
public final class LightDetectionSensor implements Sensor<LightDetectionType> {
  private final Sensor<float[]> sensorSampleFacade;

  public LightDetectionSensor(RemoteSampleFacade facadeFactory) {
    this.sensorSampleFacade = Objects.requireNonNull(facadeFactory).sampleSensor("COLOR");
  }

  @Override
  public Flux<LightDetectionType> stream$(long sampleRate) {
    return sensorSampleFacade
        .stream$(sampleRate)
        .map(
            floats -> {
              if (floats.length != 1) {
                return LightDetectionType.INVALID;
              }

              float ambientLightValue = floats[0];
              if (ambientLightValue <= 0.0f || ambientLightValue > 1.0f) {
                return LightDetectionType.INVALID;
              } else if (ambientLightValue > 0.7) {
                return LightDetectionType.DETECTED;
              } else if (Float.isNaN(ambientLightValue)) {
                return LightDetectionType.INVALID;
              } else {
                return LightDetectionType.NOT_DETECTED;
              }
            });
  }
}
