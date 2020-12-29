package vertx.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import vertx.Configurator;

/**
 * A simple Hello verticle
 *
 */
public class HelloVerticle extends AbstractVerticle implements MyVerticle {

	public HttpServer myServer = null;
	public static int defaultPort = 8001;
	private String portName = "http.port.helloService";

	public HelloVerticle(String myName) {
		System.out.println("New  HelloVerticle: " + myName);
	}

	@Override
	public void start(Promise<Void> promise) throws InterruptedException {
		this.toPrint(vertx);
		this.createServer(promise, Configurator.getConfig().getInteger(portName, defaultPort));
		Configurator.addObserver(this);
	}

	public void createServer(Promise<Void> promise, int port) {
		try {
			this.myServer = vertx.createHttpServer()
					.requestHandler(
							r -> r.response().end(new StringBuilder("Welcome to Vert.x Intro Hello").toString()))
					.listen(port, result -> {
						if (result.succeeded()) {
							promise.complete();
						} else {
							promise.fail(result.cause());
						}
					});
		} catch (Exception e) {
			System.out.println(e.toString());
			throw e;
		}
	}

	@Override
	public void stop() {
		System.out.println("Stop of HelloVerticle");
	}

	@Override
	public HttpServer getServer() {
		return myServer;
	}

	@Override
	public String getPortName() {
		return this.portName;
	}

}