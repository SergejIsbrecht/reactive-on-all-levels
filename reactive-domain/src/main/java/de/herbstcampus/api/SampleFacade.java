package de.herbstcampus.api;

import reactor.core.publisher.Flux;

public interface SampleFacade<T> {
  Flux<T> sample(long sampleRate);
}
