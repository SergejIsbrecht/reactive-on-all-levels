package de.herbstcampus;

import reactor.core.publisher.Flux;

interface DataSampler {
  Flux<float[]> sample(long sampleRate);
}
