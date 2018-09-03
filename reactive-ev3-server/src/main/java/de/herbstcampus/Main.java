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
    colorIntervalSensorSampler.sample(5000).subscribe(bytes -> System.out.println("[COLOR] sample..."));

    Port s2 = localEV3.getPort("S2");
    EV3TouchSensor ev3TouchSensor = new EV3TouchSensor(s2);
    DataSampler touchIntervalSensorSampler = IntervalSensorSampler.createSensorSampler(ev3TouchSensor.getTouchMode(), singleScheduler);
    touchIntervalSensorSampler.sample(5000).subscribe(bytes -> System.out.println("[TOUCH] sample..."));

    EV3LargeRegulatedMotor indicatorMotor = new EV3LargeRegulatedMotor(MotorPort.A);
    DataSampler indicatorIntervalMotorSampler = IntervalMotorSampler.sampleMotor(indicatorMotor, singleScheduler);
    indicatorIntervalMotorSampler.sample(5000).subscribe(bytes -> System.out.println("[INDICATOR] sample..."));
    indicatorMotor.setSpeed(100);
    indicatorMotor.flt();

    EV3LargeRegulatedMotor speedMotor = new EV3LargeRegulatedMotor(MotorPort.B);
    DataSampler speedIntervalMotorSampler = IntervalMotorSampler.sampleMotor(speedMotor, singleScheduler);
    speedIntervalMotorSampler.sample(5000).subscribe(bytes -> System.out.println("[SPEED] sample..."));
    speedMotor.setSpeed(100);
    speedMotor.flt();

    HashMap<String, DataSampler> samplerMap = new HashMap<>();
    samplerMap.put("TOUCH", touchIntervalSensorSampler);
    samplerMap.put("COLOR", colorIntervalSensorSampler);
    samplerMap.put("INDICATOR", indicatorIntervalMotorSampler);
    samplerMap.put("SPEED", speedIntervalMotorSampler);

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
