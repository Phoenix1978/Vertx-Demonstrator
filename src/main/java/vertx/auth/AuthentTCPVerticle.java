package vertx.auth;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.net.JksOptions;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.streams.Pump;
import vertx.verticle.MyVerticle;

/**
 * This class will create a Net (TCP) server as verticle
 *
 */
public class AuthentTCPVerticle extends AbstractVerticle implements MyVerticle {

	/* the server that will be created - for ease of this demonstrator, we won't keep this server in memory*/
	public HttpServer myServer = null;
	/* the port used to expose this service */
	public static int defaultPort = 8004;

	public AuthentTCPVerticle() {
		System.out.println("New  AuthentTCPVerticle ");
	}

	/**
	 * Beginning of the verticle
	 */
	@Override
	public void start(Promise<Void> promise) {
		this.toPrint(vertx);
		this.createServer(promise, defaultPort);
	}

	/**
	 * Will create the Net server 
	 * An handler is also added to manage the messages received in this Net Server
	 * 	this handler simply send back a string with the content of the request through a buffer, which means more than 1 response can be sent at a time
	 * the file server-keystore.jks has to be present in the java.main.resources package
	 */
	@Override
	public void createServer(Promise<Void> promise, int port) {
		try {
			NetServerOptions options = new NetServerOptions().setSsl(true)
					.setKeyStoreOptions(new JksOptions().setPath("server-keystore.jks").setPassword("wibble"));
			vertx.createNetServer(options).connectHandler(sock -> {
				sock.handler(buff -> {
			          System.out.println("Tcp Server receiving " + buff.toString("UTF-8"));
			          sock.write("test " + buff.toString("UTF-8"));
			        });
			}).listen(port);
		} catch (Exception e) {
			System.out.println(e.toString());
			throw e;
		}
	}

	@Override
	public void stop() {
		System.out.println("Stop of AuthentTCPVerticle");
	}

	@Override
	public HttpServer getServer() {
		return myServer;
	}

	@Override
	public String getPortName() {
		return null;
	}

}