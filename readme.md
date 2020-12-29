# <img src="https://raw.githubusercontent.com/Phoenix1978/Vertx-Demonstrator/main/icon.png" alt="" width=100/>  

##Description
This project aims on helping understand the use of Vertx, not a project to be used as it.
[Wikipedia:](https://en.wikipedia.org/wiki/Vert.x) : Eclipse Vert.x is a polyglot event-driven application framework that runs on the Java Virtual Machine
It is composed by the mix of many tutorial I did to understand many aspect of this framework : [VertX](https://vertx.io/docs/)

#### The tutorials I followed :
- https://thierry-leriche-dessirier.developpez.com/tutoriels/java/vertx/creer-lancer-tester-verticle/
- https://thierry-leriche-dessirier.developpez.com/tutoriels/java/vertx/discuter-via-event-bus/#LX-A
- https://www.baeldung.com/vertx
- https://www.codeflow.site/fr/article/vertx
- https://moodle.insa-lyon.fr/pluginfile.php/98103/mod_resource/content/1/tp1-vertx.html
- https://dzone.com/articles/eclipse-vertx-application-configuration-rhd-blog


#### Tutorial to go further
- https://www.redhat.com/en/blog/troubleshooting-performance-vertx-applications-part-iii-%E2%80%94-troubleshooting-event-loop-delays
- https://datacadamia.com/lang/java/vertx/hot
- https://vertx.io/docs/


## What is covered
- Creation of verticle
- Clustering of event bus
- Multideployment of verticles
- TCP verticle (net servers) and Http servers
- configuration reload
- rooting
- point to point (message on event bus directly)
- client/consumer and server/publisher


## Installation
This project was done on Eclipse (2012-12) with Maven, so it's necessary to import the project and build it. 
Launching the unit test, we can see a lot of logs with use of thread, creation of verticles etc... helping to follow what is going on in the code

## License
This project is under MIT License, but do whatever you want with it

## Project status
This project is not supposed to evolve anymore
