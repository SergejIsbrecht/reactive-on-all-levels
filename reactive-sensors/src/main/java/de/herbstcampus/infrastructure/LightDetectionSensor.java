package de.herbstcampus.infrastructure;

import de.herbstcampus.api.Sensor;
import de.herbstcampus.api.SensorSampleFacade;
import de.herbstcampus.model.LightDetectionType;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import reactor.core.publisher.Flux;

@ParametersAreNonnullByDefault
public final class LightDetectionSensor implements Sensor<LightDetectionType> {
  private final SensorSampleFacade<LightDetectionType> sensorSampleFacade;

  public LightDetectionSensor(RemoteSampleFacadeFactory facadeFactory) {
    this.sensorSampleFacade = Objects.requireNonNull(facadeFactory).sampleSensor("S2", "lejos.hardware.sensor.EV3ColorSensor", "ColorID");
  }

  @Override
  public Flux<LightDetectionType> stream$(long sampleRate) {
    return sensorSampleFacade.sample(
        sampleRate,
        floats -> {
          // TODO: implement
          return LightDetectionType.DETECTED;
        });
  }
}
