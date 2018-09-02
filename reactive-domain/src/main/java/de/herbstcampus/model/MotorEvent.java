package de.herbstcampus.model;

import java.nio.ByteBuffer;
import org.immutables.value.Value;

@Value.Immutable
public interface MotorEvent {
  static MotorEvent fromByteBuffer(ByteBuffer buffer) {
    int tachoCount = buffer.getInt();
    return ImmutableMotorEvent.of(tachoCount);
  }

  @Value.Parameter
  int tachoCount();

  @Value.Lazy
  default float[] toFloatArry() {
    return new float[] {tachoCount()};
  }
}
