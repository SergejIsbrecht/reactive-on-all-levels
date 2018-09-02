package de.herbstcampus.model;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import org.immutables.value.Value;

@Value.Immutable
public interface MotorEvent {
  @Value.Lazy
  static float[] fromByteArray(byte[] floats) {
    // TODO: implement
    return null;
  };

  @Value.Default
  default MotorRotationType type() {
    return MotorRotationType.STOPPED;
  };

  @Value.Default
  default long timeStamp() {
    return -1L;
  }

  int tachoCount();

  boolean stalled();

  int limitAngle();

  int speed();

  int rotationSpeed();

  @Value.Lazy
  default float[] toFloatArry() {
    FloatBuffer floatBuffer =
        ByteBuffer.allocate(29)
            .put((byte) (stalled() ? 1 : 0)) // 1
            .putInt(tachoCount()) // 4
            .putInt(limitAngle()) // 4
            .putInt(speed()) // 4
            .putInt(rotationSpeed()) // 4
            .putLong(timeStamp()) // 8
            .putInt(type().ordinal()) // 4
            .asFloatBuffer();
    float[] floatArray = new float[floatBuffer.limit()];
    floatBuffer.get(floatArray);

    return floatArray;
  }
}
