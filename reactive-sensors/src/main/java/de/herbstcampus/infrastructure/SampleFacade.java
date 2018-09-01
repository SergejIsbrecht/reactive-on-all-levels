package de.herbstcampus.infrastructure;

import java.util.function.Function;
import reactor.core.publisher.Flux;

public interface SampleFacade<T> {
  Flux<T> sample(long sampleRate, Function<float[], T> mapper);
}
