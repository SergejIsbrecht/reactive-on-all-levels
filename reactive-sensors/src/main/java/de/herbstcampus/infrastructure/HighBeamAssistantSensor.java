package de.herbstcampus.infrastructure;

import de.herbstcampus.api.Sensor;
import de.herbstcampus.model.ActivityState;
import javax.annotation.ParametersAreNonnullByDefault;
import lejos.remote.ev3.RMISampleProvider;
import lejos.remote.ev3.RemoteEV3;
import reactor.core.publisher.Flux;

@ParametersAreNonnullByDefault
public final class HighBeamAssistantSensor implements Sensor<ActivityState> {
  @Override
  public Flux<ActivityState> stream$(long sampleRate) {
    Flux.create(
        fluxSink -> {
          try {
            RemoteEV3 ev3 = new RemoteEV3("192.168.0.9");

            RMISampleProvider sampleProvider = ev3.createSampleProvider("333", "LMS", "W");

          } catch (Exception ex) {
            fluxSink.error(ex);
          }
        });
    return null;
  }
}
