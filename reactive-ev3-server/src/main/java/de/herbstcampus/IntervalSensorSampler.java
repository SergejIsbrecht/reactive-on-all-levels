package de.herbstcampus;

import java.time.Duration;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import lejos.robotics.SampleProvider;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;

@ParametersAreNonnullByDefault
final class IntervalSensorSampler implements DataSampler {
  private final SampleProvider sampleProvider;
  private Scheduler scheduler;

  private IntervalSensorSampler(SampleProvider sampleProvider, Scheduler scheduler) {
    this.sampleProvider = Objects.requireNonNull(sampleProvider);
    this.scheduler = Objects.requireNonNull(scheduler);
  }

  static IntervalSensorSampler createSensorSampler(SampleProvider sampleProvider, Scheduler scheduler) {
    return new IntervalSensorSampler(sampleProvider, scheduler);
  }

  @Override
  public Flux<float[]> sample(long sampleRate) {
    return Flux.interval(Duration.ofMillis(sampleRate), scheduler)
        .map(
            aLong -> {
              float[] sample = new float[sampleProvider.sampleSize()];
              sampleProvider.fetchSample(sample, 0);
              return sample;
            });
  }
}
