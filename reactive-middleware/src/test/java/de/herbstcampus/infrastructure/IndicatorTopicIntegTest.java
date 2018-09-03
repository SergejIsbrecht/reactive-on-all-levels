package de.herbstcampus.infrastructure;

import de.herbstcampus.model.IndicatorType;
import de.herbstcampus.topic.IndicatorTopic;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

class IndicatorTopicIntegTest {
  @Test
  void name() {
    RemoteSampleFacade remoteSampleFacade = new RemoteSampleFacade("10.0.1.1", 7000, Schedulers.single());
    IndicatorSensor indicatorSensor = new IndicatorSensor(remoteSampleFacade);
    IndicatorTopic indicatorTopic = new IndicatorTopic(indicatorSensor);

    Flux<IndicatorType> indicatorTypeFlux = indicatorTopic.stream$();

    indicatorTypeFlux.take(10).blockLast();
  }
}
