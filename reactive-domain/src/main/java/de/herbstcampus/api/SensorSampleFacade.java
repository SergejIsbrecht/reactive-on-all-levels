package de.herbstcampus.api;

import java.util.function.Function;
import reactor.core.publisher.Flux;

public interface SensorSampleFacade<T> {
  Flux<T> sample(long sampleRate, Function<float[], T> mapper);
}
