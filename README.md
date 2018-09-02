# Reactive Programmierung auf allen Ebenen

Info:: https://www.herbstcampus.de/veranstaltung-7212-reaktive-programmierung-auf-allen-ebenen.html?id=7212


# Prerequisite
EV3 with LeJOS (https://sourceforge.net/p/lejos/wiki/Installing%20leJOS/)
* micro-SD-Card (used 16GiB SanDisk Ultra microSDHC)

Working Java8 JVM on EV3 (http://gjf2a.blogspot.com/2015/05/setting-up-lejos-09-with-java-8.html)

+ LeJOS-Api provides a simple RMI interface to sample data EV3-Sensors, but does not work properly. Therefore it comes only natural that we use some kind of protocol to emit data at subscriber. In this case it is RSocket, due too its non-blocking and simple usage.

Java8 knowledge

Gradle knowledge
* Deploy sensor-server to EV3 brick with given Gradle-Plugin (https://github.com/bdeneuter/mindstorms-plugin)

Reactive knowledge

# Libraries - API
https://github.com/immutables/immutables

# Libraries - EV3
https://github.com/rsocket/rsocket-java

https://github.com/reactor/reactor-core

# Libraries - Java-Middleware
https://github.com/rsocket/rsocket-java

https://github.com/reactor/reactor-core