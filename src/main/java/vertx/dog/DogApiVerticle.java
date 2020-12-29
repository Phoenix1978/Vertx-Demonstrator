package vertx.dog;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import vertx.Configurator;
import vertx.verticle.MyVerticle;

/**
 * Another verticle that will manage request avout the dog Api (with many routes)
 *
 */
public class DogApiVerticle extends AbstractVerticle implements MyVerticle {
	/* the server that will be created */
	public HttpServer myServer = null;
	/* the port used to expose this service */
	private int defaultPort = 8002;
	/* a name given for the properties in configuration file (vertxConf.json) to change the port automatically (by update of the observer) */
	private String portName = "http.port.dogService";

	public DogApiVerticle() {
		System.out.println("New DogApiVerticle");
	}

	/**
	 * Starts the verticle
	 */
	@Override
	public void start(Promise<Void> promise) throws Exception {
		this.toPrint(this.vertx);
		this.createServer(promise, Configurator.getConfig().getInteger(portName, defaultPort));
		Configurator.addObserver(this);
	}

	/**
	 * Stop the verticles
	 */
	@Override
	public void stop() throws Exception {
		System.out.println("Stop of DogApiVerticle");
	}

	/**
	 * Will create a HTTP server with many routes to manage dogs
	 * 	The request handler will be the @DogResource
	 */
	@Override
	public void createServer(Promise<Void> promise, int port) {
		try {
			final Router router = Router.router(vertx);
			final DogResource dogResource = new DogResource();
			final Router dogSubRouter = dogResource.getSubRouter(vertx);
			router.mountSubRouter("/api/v1/dogs", dogSubRouter);
			this.myServer = vertx.createHttpServer().requestHandler(router).listen(port);
		} catch (Exception e) {
			System.out.println(e.toString());
			throw e;
		}
	}

	@Override
	public String getPortName() {
		return this.portName;
	}

	@Override
	public HttpServer getServer() {
		return myServer;
	}
}