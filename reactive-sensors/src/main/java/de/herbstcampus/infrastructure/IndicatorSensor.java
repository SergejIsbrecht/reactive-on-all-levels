package de.herbstcampus.infrastructure;

import de.herbstcampus.api.Sensor;
import de.herbstcampus.model.IndicatorType;
import de.herbstcampus.model.MotorEvent;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import reactor.core.publisher.Flux;

@ParametersAreNonnullByDefault
public final class IndicatorSensor implements Sensor<IndicatorType> {
  private final Sensor<MotorEvent> sensorSampleFacade;

  public IndicatorSensor(RemoteSampleFacade facadeFactory) {
    this.sensorSampleFacade = Objects.requireNonNull(facadeFactory).sampleRegulatedMotor("INDICATOR");
  }

  @Override
  public Flux<IndicatorType> stream$(long sampleRate) {
    return sensorSampleFacade
        .stream$(sampleRate)
        .map(
            motorEvent -> {
              System.out.println("[SENSOR][IndicatorSensor] " + sampleRate);

              // TODO: IMPLEMENT

              return IndicatorType.OFF;
            });
  }
}
