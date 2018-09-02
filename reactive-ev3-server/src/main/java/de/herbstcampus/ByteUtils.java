package de.herbstcampus;

import java.nio.ByteBuffer;

interface ByteUtils {
  static ByteBuffer floatArray2ByteArray(float[] values) {
    ByteBuffer buffer = ByteBuffer.allocate(4 * values.length);
    for (float value : values) {
      buffer.putFloat(value);
    }
    return buffer;
  }
}
