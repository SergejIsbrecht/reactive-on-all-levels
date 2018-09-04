package de.herbstcampus.infrastructure;

import de.herbstcampus.topic.Topic;
import io.rsocket.AbstractRSocket;
import io.rsocket.ConnectionSetupPayload;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.SocketAcceptor;
import io.rsocket.util.DefaultPayload;
import io.vavr.collection.Seq;
import io.vavr.control.Option;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ParametersAreNonnullByDefault
final class SocketAcceptorImpl implements SocketAcceptor {
  private final Seq<Topic<?>> topics;

  SocketAcceptorImpl(Seq<Topic<?>> topics) {
    this.topics = Objects.requireNonNull(topics);
  }

  @Override
  public Mono<RSocket> accept(ConnectionSetupPayload setup, RSocket sendingSocket) {
    return Mono.just(
        new AbstractRSocket() {
          @Override
          public Flux<Payload> requestStream(Payload payload) {
            String payloadAsString = payload.getDataUtf8();
            // TODO: cache and multi-cast Topics
            Option<Topic<?>> topic = topics.find(t -> t.name().toLowerCase().equals(payloadAsString.toLowerCase()));
            return topic
                .map(
                    t -> {
                      return t.stream$()
                          .doOnSubscribe(subscription -> System.out.println("[SERVER] Request topic: " + payloadAsString))
                          .map(o -> DefaultPayload.create(o.toString()));
                    })
                .getOrElse(Flux.error(new IllegalArgumentException("[SERVER] Unknown topic")));
          }
        });
  }
}
