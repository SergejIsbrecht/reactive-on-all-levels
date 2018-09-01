package de.herbstcampus.topic;

import reactor.core.publisher.Flux;

public interface Topic<T> {
  Flux<T> stream$();

  String name();
}
