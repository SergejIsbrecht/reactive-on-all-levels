package de.herbstcampus.infrastructure;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.herbstcampus.model.TouchType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

class HighBeamAssistantSensorTest {
  private HighBeamAssistantSensor classUnderTest;
  private EmitterProcessor<float[]> sensorData$;

  @BeforeEach
  void setUp() {
    this.sensorData$ = EmitterProcessor.create();
    RemoteSampleFacadeFactory factory = mock(RemoteSampleFacadeFactory.class);
    when(factory.sampleSensor(anyString(), anyString(), anyString())).thenReturn(sampleRate -> sensorData$);

    this.classUnderTest = new HighBeamAssistantSensor(factory);
  }

  @Test
  void whenValidSensorDataIsEmitted_dataShouldBeMappedWithoutError() {
    Flux<TouchType> highBeamAssistant$ = classUnderTest.stream$(anyLong());

    sensorData$.onNext(new float[] {0f});
    sensorData$.onNext(new float[] {1f});
    sensorData$.onNext(new float[] {0f});

    StepVerifier.create(highBeamAssistant$) //
        .expectNext(TouchType.NOT_PRESSED)
        .expectNext(TouchType.PRESSED)
        .expectNext(TouchType.NOT_PRESSED)
        .thenCancel();
  }
}
