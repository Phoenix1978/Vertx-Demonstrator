package vertx.auth;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import vertx.verticle.MyVerticle;

/**
 * The verticle that will receive calls about authentication request
 */
public class AuthentVerticle extends AbstractVerticle implements MyVerticle {


	/* the server that will be created */
	public HttpServer myServer = null;
	/* the port used to expose this service */
	public static int defaultPort = 8003;
	/* the consumer that will 'eat' message comming directly over the event bus */
	private static MessageConsumer<JsonObject> consumer = null;
	/* address used on event bus to publish/subscribe for messages */
	private static String addressEventBus = "authentication.requests";

	public AuthentVerticle() {
		System.out.println("New  AuthentVerticle ");		
	}

	/**
	 * Will create a consumer on the event bus (shared over multi JVM due to clustering)
	 */
	private void createEventBusConsumer() {
		consumer = vertx.eventBus().consumer(addressEventBus);
		consumer.handler(message -> {
			System.out.println("Incoming request for authent: " + message.body());
			JsonObject reply = new JsonObject().put("success", AuthenticatorService.checkAuthent(message.body()));			
			message.reply(reply);
		});
	}

	/**
	 * Will start the verticle
	 */
	@Override
	public void start(Promise<Void> promise) {
		this.toPrint(vertx);
		this.createServer(promise, defaultPort);
		createEventBusConsumer();
	}	
	
	/**
	 * Will stop the verticle
	 */
	@Override
	public void stop() {
		System.out.println("Stop of AuthentVerticle");
	}

	
	@Override
	public void createServer(Promise<Void> promise, int port) {
		try {
			final Router router = Router.router(vertx);
			final Router dogSubRouter = this.getSubRouter(vertx);
			router.mountSubRouter("/auth", dogSubRouter);
			this.myServer = vertx.createHttpServer().requestHandler(router).listen(port);
		} catch (Exception e) {
			System.out.println(e.toString());
			throw e;
		}
	}

	/**
	 * Will create the routes for the verticle Authent
	 * the POST can be tested with this curl:
	 * 	curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X POST -d "{\"login\":\"john\", \"password\":\"LetM3B3R00t\"}" http://localhost:8003/auth
	 */
	public Router getSubRouter(final Vertx vertx) {
	    final Router subRouter = Router.router(vertx);
	    subRouter.route("/*").handler(BodyHandler.create());
	    subRouter.post("/").handler(this::checkHttpAuth);
	    return subRouter;
	}
	
	/**
	 * Will manage the POST calls with JSON object in entry of exit of the call
	 * @param routingContext
	 */
	private void checkHttpAuth(final RoutingContext routingContext) {
		System.out.println("Dans checkHttpAuth...");
		try {
	    final JsonObject body = routingContext.getBodyAsJson();
	    boolean authAccepted = AuthenticatorService.checkAuthent(body);
	    JsonObject reply = new JsonObject().put("success", authAccepted);		
	    routingContext.response()
	        .setStatusCode(authAccepted ? 200: 401)
	        .putHeader("content-type", "application/json")
	        .end(Json.encode(reply));
		}catch (Exception e) {
			e.printStackTrace();
		}
	  }

	@Override
	public HttpServer getServer() {
		return myServer;
	}

	@Override
	public String getPortName() {
		return null;
	}
	

	public static String getAddressEventBus() {
		return addressEventBus;
	}


}