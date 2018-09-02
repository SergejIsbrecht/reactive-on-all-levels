package de.herbstcampus.topic;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.herbstcampus.api.Sensor;
import de.herbstcampus.model.HighBeamState;
import de.herbstcampus.model.LightDetectionType;
import de.herbstcampus.model.TouchType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

class HighBeamAssistTopicTest {
  private HighBeamAssistTopic classUnderTest;
  private EmitterProcessor<Double> speed$;
  private EmitterProcessor<LightDetectionType> lightDetection$;
  private EmitterProcessor<TouchType> highBeamAssistantState$;

  @BeforeEach
  void setUp() {
    Sensor<Float> speed = (Sensor<Float>) mock(Sensor.class);
    Sensor<LightDetectionType> lightDetection = (Sensor<LightDetectionType>) mock(Sensor.class);
    Sensor<TouchType> highBeamAssistantState = (Sensor<TouchType>) mock(Sensor.class);

    speed$ = EmitterProcessor.create();
    lightDetection$ = EmitterProcessor.create();
    highBeamAssistantState$ = EmitterProcessor.create();

    when(speed.stream$(Mockito.anyLong())).thenReturn(Flux.never());
    when(lightDetection.stream$(Mockito.anyLong())).thenReturn(lightDetection$);
    when(highBeamAssistantState.stream$(Mockito.anyLong())).thenReturn(highBeamAssistantState$);

    this.classUnderTest = new HighBeamAssistTopic(speed, lightDetection, highBeamAssistantState);
  }

  @Test
  void whenAssistantActiveAndSpeedLimitIsReached_highBeamsShouldBeDisabled() {
    Flux<HighBeamState> highBeamStateFlux = classUnderTest.stream$();

    // simulate activating assistant
    highBeamAssistantState$.onNext(TouchType.PRESSED);
    highBeamAssistantState$.onNext(TouchType.NOT_PRESSED);

    speed$.onNext(100d);
    speed$.onNext(150d);
    speed$.onNext(250d);

    StepVerifier.create(highBeamStateFlux) //
        .expectNext(HighBeamState.ENABLED)
        .expectNext(HighBeamState.DISABLED_SPPEED_LIMIT)
        .thenCancel();
  }

  @Test
  void whenAssistantActiveAndLightDetected_highBeamsShouldBeDisabled() {
    Flux<HighBeamState> sub1 = classUnderTest.stream$();

    // simulate activating assistant
    highBeamAssistantState$.onNext(TouchType.PRESSED);
    highBeamAssistantState$.onNext(TouchType.NOT_PRESSED);

    lightDetection$.onNext(LightDetectionType.NOT_DETECTED);
    lightDetection$.onNext(LightDetectionType.DETECTED);

    StepVerifier.create(sub1) //
        .expectNext(HighBeamState.ENABLED)
        .expectNext(HighBeamState.DISABLED_LIGHT_DETECTED)
        .thenCancel();

    // check that value is cached
    Flux<HighBeamState> lateSub2 = classUnderTest.stream$();
    StepVerifier.create(lateSub2) //
        .expectNext(HighBeamState.DISABLED_LIGHT_DETECTED)
        .thenCancel();
  }
}
