package de.herbstcampus.infrastructure;

import de.herbstcampus.topic.HighBeamAssistTopic;
import de.herbstcampus.topic.IndicatorTopic;
import de.herbstcampus.topic.SpeedTopic;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.server.NettyContextCloseable;
import io.rsocket.transport.netty.server.WebsocketServerTransport;
import io.vavr.collection.Array;
import javax.annotation.ParametersAreNonnullByDefault;
import reactor.core.scheduler.Schedulers;

@ParametersAreNonnullByDefault
public final class Server {
  static void main(String[] args) {
    SampleFacadeFactory sampleFacadeFactory = new SampleFacadeFactory("192.168.1.1", Schedulers.single());

    SpeedSensor speedSensor = new SpeedSensor(sampleFacadeFactory);
    LightDetectionSensor lightDetectionSensor = new LightDetectionSensor(sampleFacadeFactory);
    HighBeamAssistantSensor highBeamAssistantSensor = new HighBeamAssistantSensor(sampleFacadeFactory);
    IndicatorSensor indicatorSensor = new IndicatorSensor(sampleFacadeFactory);

    // build topics
    HighBeamAssistTopic highBeamAssistTopic = new HighBeamAssistTopic(speedSensor, lightDetectionSensor, highBeamAssistantSensor);
    IndicatorTopic indicatorTopic = new IndicatorTopic(indicatorSensor);
    SpeedTopic speedTopic = new SpeedTopic(speedSensor);

    NettyContextCloseable localhost =
        RSocketFactory.receive() //
            .acceptor(new SocketAcceptorImpl(Array.of(highBeamAssistTopic, indicatorTopic, speedTopic)))
            .transport(WebsocketServerTransport.create("localhost", 6666))
            .start()
            .block();
  }
}
