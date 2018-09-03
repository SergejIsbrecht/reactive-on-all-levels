package de.herbstcampus.infrastructure;

import de.herbstcampus.topic.HighBeamAssistTopic;
import org.junit.jupiter.api.Test;
import reactor.core.scheduler.Schedulers;

class HighBeamAssistTopicIntegTest {
  @Test
  void name() {
    RemoteSampleFacade remoteSampleFacade = new RemoteSampleFacade("10.0.1.1", 7000, Schedulers.single());

    SpeedSensor speedSensor = new SpeedSensor(remoteSampleFacade);
    LightDetectionSensor lightDetectionSensor = new LightDetectionSensor(remoteSampleFacade);
    HighBeamAssistantSensor highBeamAssistantSensor = new HighBeamAssistantSensor(remoteSampleFacade);

    HighBeamAssistTopic highBeamAssistTopic = new HighBeamAssistTopic(speedSensor, lightDetectionSensor, highBeamAssistantSensor);

    highBeamAssistTopic.stream$().take(5).blockLast();
  }
}
