package de.herbstcampus.api;

import reactor.core.publisher.Flux;

public interface SensorSampleFacade<T> {
  Flux<T> sample(long sampleRate);
}
