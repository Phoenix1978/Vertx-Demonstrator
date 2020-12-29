package vertx.verticle;

import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;

/**
 * Interface easing the written of this demonstrator
 * Not useful really about VertX
 */
public interface MyVerticle {
	public HttpServer getServer();
	
	public void createServer(Promise<Void> promise, int port);	
	
	public default void toPrint(Vertx vertx) {
		System.out.println(new StringBuilder( "Start of ")
				.append(this.getClass().getName())
				.append("\n                 my thread Id is the [")
				.append(Thread.currentThread().getId())
				.append("] \n                 and its name is [")
				.append(Thread.currentThread().getName())
				.append("] \n                 EventBus : [")
				.append(vertx.eventBus().hashCode())
				.append("]"));
	}
	
	public String getPortName();
}
