package de.herbstcampus.infrastructure;

import de.herbstcampus.topic.HighBeamAssistTopic;
import de.herbstcampus.topic.IndicatorTopic;
import de.herbstcampus.topic.SpeedTopic;
import io.rsocket.RSocketFactory;
import io.rsocket.SocketAcceptor;
import io.rsocket.transport.netty.server.NettyContextCloseable;
import io.rsocket.transport.netty.server.WebsocketServerTransport;
import io.vavr.collection.Array;
import java.time.Duration;
import javax.annotation.ParametersAreNonnullByDefault;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@ParametersAreNonnullByDefault
public final class Server {
  private static final String SENSOR_SERVER = "10.0.1.1";

  public static void main(String[] args) {
    RemoteSampleFacadeFactory remoteSampleFacadeFactory = new RemoteSampleFacadeFactory(SENSOR_SERVER, Schedulers.single());

    // init sensors
    SpeedSensor speedSensor = new SpeedSensor(remoteSampleFacadeFactory);
    LightDetectionSensor lightDetectionSensor = new LightDetectionSensor(remoteSampleFacadeFactory);
    HighBeamAssistantSensor highBeamAssistantSensor = new HighBeamAssistantSensor(remoteSampleFacadeFactory);
    IndicatorSensor indicatorSensor = new IndicatorSensor(remoteSampleFacadeFactory);

    // build topics
    HighBeamAssistTopic highBeamAssistTopic = new HighBeamAssistTopic(speedSensor, lightDetectionSensor, highBeamAssistantSensor);
    IndicatorTopic indicatorTopic = new IndicatorTopic(indicatorSensor);
    SpeedTopic speedTopic = new SpeedTopic(speedSensor);

    SocketAcceptor socketAcceptor = new SocketAcceptorImpl(Array.of(highBeamAssistTopic, indicatorTopic, speedTopic));
    NettyContextCloseable webSocketServer =
        RSocketFactory.receive() //
            .errorConsumer(Throwable::printStackTrace)
            .acceptor(socketAcceptor)
            .transport(WebsocketServerTransport.create("localhost", 6666))
            .start()
            .block();

    Flux.interval(Duration.ofHours(10000), Schedulers.single()).blockLast(); // make sure main runes forever

    throw new IllegalStateException("Try to leave Server#main method");
  }
}
