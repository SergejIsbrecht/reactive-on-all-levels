package de.herbstcampus.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.ByteBuffer;
import org.junit.jupiter.api.Test;

class MotorEventTest {
  @Test
  void name() {
    int value = 42;
    ImmutableMotorEvent of = ImmutableMotorEvent.of(value);
    byte[] bytes = of.toByteArray();

    int anInt = ByteBuffer.wrap(bytes).getInt();
    assertThat(anInt).isEqualTo(value);
  }

  @Test
  void name2() {
    int value = 42;
    ByteBuffer allocate = ByteBuffer.allocate(4).putInt(value);

    MotorEvent motorEvent = MotorEvent.fromByteBuffer(allocate);
    assertThat(motorEvent.tachoCount()).isEqualTo(value);
  }
}
