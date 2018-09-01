package de.herbstcampus.infrastructure;

import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.server.NettyContextCloseable;
import io.rsocket.transport.netty.server.WebsocketServerTransport;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class Server {
  static void main(String[] args) {
    NettyContextCloseable localhost =
        RSocketFactory.receive() //
            .acceptor(new SocketAcceptorImpl(null))
            .transport(WebsocketServerTransport.create("localhost", 6666))
            .start()
            .block();
  }
}
