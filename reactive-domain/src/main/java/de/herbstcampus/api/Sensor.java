package de.herbstcampus.api;

import reactor.core.publisher.Flux;

public interface Sensor<T> {
  Flux<T> stream$(long sampleRate);
}
