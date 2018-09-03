package de.herbstcampus.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.scheduler.Schedulers;

class RemoteSampleFacadeIntegTest {
  private RemoteSampleFacade classUnderTest;

  @BeforeEach
  void setUp() {
    this.classUnderTest = new RemoteSampleFacade("10.0.1.1", Schedulers.single());
  }

  @Test
  void highBeamAssistantSensor() {
    HighBeamAssistantSensor highBeamAssistantSensor = new HighBeamAssistantSensor(classUnderTest);
    highBeamAssistantSensor.stream$(100).take(10).blockLast();
  }

  @Test
  void indicatorSensor() {
    IndicatorSensor indicatorSensor = new IndicatorSensor(classUnderTest);
    indicatorSensor.stream$(100).take(10).blockLast();
  }

  @Test
  void lightDetectionSensor() {
    LightDetectionSensor lightDetectionSensor = new LightDetectionSensor(classUnderTest);
    lightDetectionSensor.stream$(100).take(10).blockLast();
  }

  @Test
  void speedSensor() {
    SpeedSensor speedSensor = new SpeedSensor(classUnderTest);
    speedSensor.stream$(100).take(10).blockLast();
  }
}
