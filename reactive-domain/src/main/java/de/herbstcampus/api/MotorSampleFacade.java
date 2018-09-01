package de.herbstcampus.api;

import reactor.core.publisher.Flux;

public interface MotorSampleFacade<T> {
  Flux<T> sample(long sampleRate);
}
