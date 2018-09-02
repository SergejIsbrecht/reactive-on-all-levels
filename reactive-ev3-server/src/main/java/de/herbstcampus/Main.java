package de.herbstcampus;

import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.server.TcpServerTransport;
import java.time.Duration;
import java.util.HashMap;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.utility.Delay;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public class Main {
  private static final String IP = "10.0.1.1";
  private static final int PORT = 7000;

  public static void main(String[] args) {
    LocalEV3 localEV3 = LocalEV3.ev3;

    Scheduler singleScheduler = Schedulers.single();

    Port s1 = localEV3.getPort("S1");
    EV3ColorSensor ev3ColorSensor = new EV3ColorSensor(s1);
    DataSampler colorIntervalSensorSampler = IntervalSensorSampler.createSensorSampler(ev3ColorSensor.getAmbientMode(), singleScheduler);

    Port s2 = localEV3.getPort("S2");
    EV3TouchSensor ev3TouchSensor = new EV3TouchSensor(s2);
    DataSampler touchIntervalSensorSampler = IntervalSensorSampler.createSensorSampler(ev3TouchSensor.getTouchMode(), singleScheduler);

    EV3LargeRegulatedMotor largeMotor = new EV3LargeRegulatedMotor(MotorPort.A);
    DataSampler indicatorIntervalMotorSampler = IntervalMotorSampler.sampleMotor(largeMotor, singleScheduler);

    indicatorIntervalMotorSampler.sample(500).subscribe(floats -> System.out.println("floats"));

    largeMotor.setSpeed(100);
    largeMotor.flt();

    HashMap<String, DataSampler> samplerMap = new HashMap<>();
    samplerMap.put("TOUCH", touchIntervalSensorSampler);
    samplerMap.put("COLOR", colorIntervalSensorSampler);
    samplerMap.put("INDICATOR", indicatorIntervalMotorSampler);

    // TODO: create disposable for all sensors -> cleanup work

    RSocketFactory.receive()
        .acceptor(new SocketAcceptorImpl(samplerMap))
        .transport(TcpServerTransport.create(IP, PORT))
        .start()
        .doOnError(throwable -> System.err.println("ERROR IN SOCKET"))
        .subscribe();

    Flux.interval(Duration.ofHours(1000), Schedulers.single()).blockLast(); // do not exit main()

    throw new IllegalStateException("TRY TO LEAVE MAIN!");
  }
}
