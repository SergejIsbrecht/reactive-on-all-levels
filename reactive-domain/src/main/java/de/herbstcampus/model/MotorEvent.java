package de.herbstcampus.model;

import org.immutables.value.Value;

@Value.Immutable
public interface MotorEvent {
  @Value.Parameter
  int tachoCount();

  @Value.Lazy
  default float[] toFloatArry() {
    return new float[] {tachoCount()};
  }
}
