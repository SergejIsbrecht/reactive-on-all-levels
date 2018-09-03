package de.herbstcampus.model;

import java.nio.ByteBuffer;
import org.immutables.value.Value;

@Value.Immutable
public interface MotorEvent {
  static MotorEvent fromByteBuffer(ByteBuffer buffer) {
    buffer.flip();

    int tachoCount = buffer.getInt();
    return ImmutableMotorEvent.of(tachoCount);
  }

  @Value.Parameter
  int tachoCount();

  @Value.Lazy
  default byte[] toByteArray() {
    ByteBuffer allocate = ByteBuffer.allocate(4).putInt(tachoCount());
    byte[] array = allocate.array();
    return array;
  }
}
