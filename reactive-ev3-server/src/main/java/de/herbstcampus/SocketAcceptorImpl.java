package de.herbstcampus;

import io.rsocket.AbstractRSocket;
import io.rsocket.ConnectionSetupPayload;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.SocketAcceptor;
import io.rsocket.util.DefaultPayload;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

final class SocketAcceptorImpl implements SocketAcceptor {
  private final HashMap<String, DataSampler> samplerMap;

  SocketAcceptorImpl(HashMap<String, DataSampler> samplerMap) {
    this.samplerMap = Objects.requireNonNull(samplerMap);
  }

  @Override
  public Mono<RSocket> accept(ConnectionSetupPayload setupPayload, RSocket reactiveSocket) {
    return Mono.just(
        new AbstractRSocket() {
          @Override
          public Flux<Payload> requestStream(Payload payload) {
            ParsedPayload parsedPayload = parsePayload(payload.getDataUtf8());
            Optional<DataSampler> dataSampler = Optional.ofNullable(samplerMap.get(parsedPayload.sensorName()));
            Flux<Payload> payloadFlux =
                dataSampler
                    .map(
                        s -> {
                          return s.sample(parsedPayload.sampleRate())
                              .map(
                                  bytes -> {
                                    ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
                                    return DefaultPayload.create(byteBuffer.array());
                                  });
                        })
                    .orElseGet(
                        () -> {
                          return Flux.error(new IllegalArgumentException("[EVR] Could not locate sensor."));
                        });
            return payloadFlux;
          }
        });
  }

  private ParsedPayload parsePayload(String payload) {
    String[] split = payload.split(",");
    if (split.length != 2) {
      throw new IllegalArgumentException("Could not parse payload request: " + payload);
    }
    String sensorName = split[0];
    long sampleRate = Long.valueOf(split[1]);

    return ImmutableParsedPayload.builder().sensorName(sensorName).sampleRate(sampleRate).build();
  }
}
