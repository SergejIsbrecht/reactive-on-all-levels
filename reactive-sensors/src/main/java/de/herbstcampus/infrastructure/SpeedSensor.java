package de.herbstcampus.infrastructure;

import de.herbstcampus.api.MotorSampleFacade;
import de.herbstcampus.api.Sensor;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import reactor.core.publisher.Flux;

@ParametersAreNonnullByDefault
public final class SpeedSensor implements Sensor<Double> {
  private final MotorSampleFacade<Double> sensorSampleFacade;

  public SpeedSensor(RemoteSampleFacadeFactory facadeFactory) {
    this.sensorSampleFacade = Objects.requireNonNull(facadeFactory).sampleRegulatedMotor("D", 'L');
  }

  @Override
  public Flux<Double> stream$(long sampleRate) {
    return sensorSampleFacade.sample(
        sampleRate,
        motorEvent -> {
          // TODO: implement
          return -1d;
        });
  }
}
