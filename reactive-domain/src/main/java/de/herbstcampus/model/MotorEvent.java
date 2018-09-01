package de.herbstcampus.model;

import org.immutables.value.Value;

@Value.Immutable
public interface MotorEvent {
  MotorRotationType type();

  int tachoCount();

  boolean stalled();

  long timeStamp();

  int limitAngle();

  int speed();

  int rotationSpeed();
}
