package vertx;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;

/**
 * Class that will use the event bus Point to Point call, by creating an
 * "address" on the event bus
 */
public class EventBusConsumerFactory {
	/* The address that will be created on the event bus, where messages will be published */
	private static String address = "Hello on the event bus";
	/* an arbitrary message to be sent */
	private static String messageToSent = "My message on event bus";
	
	/**
	 * It is called factory but it does not created "many" object
	 * This function simply created an "address" on the event bus 
	 * Its handler wil simply return a "Replied Message" whe a message is received (as a JSON object)
	 * @param vertx
	 */
	public EventBusConsumerFactory(Vertx vertx) {
		super();
		final MessageConsumer<String> consumer = vertx.eventBus().consumer(address);
		consumer.handler(message -> {
			System.out.println("incoming message: " + message.body());
			JsonObject reply = new JsonObject().put("message", "Replied Message");
			message.reply(reply);
		});
	}

	public static String getAddress() {
		return address;
	}

	public static String getMessageToSent() {
		return messageToSent;
	}

}