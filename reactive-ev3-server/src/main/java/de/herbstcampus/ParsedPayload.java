package de.herbstcampus;

import org.immutables.value.Value;

@Value.Immutable
interface ParsedPayload {
  String sensorName();

  long sampleRate();
}
