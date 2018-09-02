package de.herbstcampus.model;

import org.junit.jupiter.api.Test;

class ImmutableMotorEventTest {
  @Test
  void sdfds() {
    ImmutableMotorEvent build = ImmutableMotorEvent.builder().tachoCount(3).stalled(true).limitAngle(5).rotationSpeed(555).speed(1).build();

    float[] floats = build.toFloatArry();
  }
}
