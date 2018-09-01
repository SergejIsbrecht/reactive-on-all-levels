package de.herbstcampus.infrastructure;

import de.herbstcampus.api.Sensor;
import de.herbstcampus.api.SensorSampleFacade;
import de.herbstcampus.model.LightDetectionType;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import reactor.core.publisher.Flux;

@ParametersAreNonnullByDefault
public final class LightDetectionSensor implements Sensor<LightDetectionType> {
  private final SensorSampleFacade<float[]> sensorSampleFacade;

  public LightDetectionSensor(RemoteSampleFacadeFactory facadeFactory) {
    this.sensorSampleFacade = Objects.requireNonNull(facadeFactory).sampleSensor("S2", "lejos.hardware.sensor.EV3ColorSensor", "Ambient");
  }

  @Override
  public Flux<LightDetectionType> stream$(long sampleRate) {
    return sensorSampleFacade
        .sample(sampleRate)
        .map(
            floats -> {
              if (floats.length > 1) {
                return LightDetectionType.INVALID;
              }

              float ambientLightValue = floats[0];
              if (ambientLightValue > 0.8) {
                return LightDetectionType.DETECTED;
              } else if (Float.isNaN(ambientLightValue)) {
                return LightDetectionType.INVALID;
              } else {
                return LightDetectionType.NOT_DETECTED;
              }
            });
  }
}
