package de.herbstcampus.infrastructure;

import de.herbstcampus.api.SampleFacade;
import de.herbstcampus.api.Sensor;
import de.herbstcampus.model.MotorEvent;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import reactor.core.publisher.Flux;

@ParametersAreNonnullByDefault
public final class SpeedSensor implements Sensor<Double> {
  private final SampleFacade<MotorEvent> sensorSampleFacade;

  public SpeedSensor(RemoteSampleFacadeFactory facadeFactory) {
    this.sensorSampleFacade = Objects.requireNonNull(facadeFactory).sampleRegulatedMotor("SPEED");
  }

  @Override
  public Flux<Double> stream$(long sampleRate) {
    return sensorSampleFacade
        .sample(sampleRate)
        .map(
            motorEvent -> {
              System.out.println("[SENSOR][SpeedSensor] " + sampleRate);

              // TODO: implement
              return -1d;
            });
  }
}
