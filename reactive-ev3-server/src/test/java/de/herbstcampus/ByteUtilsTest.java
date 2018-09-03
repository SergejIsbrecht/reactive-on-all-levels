package de.herbstcampus;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.ByteBuffer;
import org.junit.jupiter.api.Test;

class ByteUtilsTest {
  @Test
  void name() {
    float[] floats = {1};
    ByteBuffer byteBuffer = ByteUtils.floatArray2ByteArray(floats);
    byte[] array = byteBuffer.array();
    assertThat(array).isNotEmpty();
  }
}
