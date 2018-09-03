package de.herbstcampus;

import reactor.core.publisher.Flux;

interface DataSampler {
  Flux<byte[]> sample(long sampleRate);
}
