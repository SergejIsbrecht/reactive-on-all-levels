package de.herbstcampus.api;

import de.herbstcampus.model.MotorEvent;
import java.util.function.Function;
import reactor.core.publisher.Flux;

public interface MotorSampleFacade<T> {
  Flux<T> sample(long sampleRate, Function<MotorEvent, T> mapper);
}
