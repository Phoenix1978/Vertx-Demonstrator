package vertx;

import java.util.ArrayList;
import java.util.List;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import vertx.verticle.MyVerticle;

/**
 * Class responsible for managing the configuration of verticle Use an
 * Observable pattern, especially to update the verticle about their port they
 * listen
 *
 */
public class Configurator {

	/* the content of configuration read from the fileConfName variable */
	private static JsonObject config = new JsonObject();
	/* The verticles that are observed */
	private static List<MyVerticle> observerVerticle = new ArrayList<>();
	/* path to the file with config (inside the java.main.resources package */
	private static final String fileConfName = "vertxConf.json";

	/**
	 * We add an observed Verticle
	 * 
	 * @param <T>
	 * @param verticle
	 */
	public static synchronized <T> void addObserver(T verticle) {
		if (verticle instanceof AbstractVerticle) {
			observerVerticle.add((MyVerticle) verticle);
		}
		updateObserver();
	}

	/**
	 * The verticles observed will be update
	 */
	public static void updateObserver() {
		try {
			System.out.println("observerVerticle Size: " + observerVerticle.size());
			for (MyVerticle verticle : observerVerticle) {
				int newPort = config.getInteger(verticle.getPortName(), verticle.getServer().actualPort());
				if (newPort != verticle.getServer().actualPort()) {
					System.out.printf("update the Previous Port : %d, and new port : %d %n",
							verticle.getServer().actualPort(), newPort);
					verticle.getServer().close();
					verticle.createServer(Promise.promise(), newPort);
				}
			}
		} catch (Exception e) {
			System.out.println(e.toString());
			throw e;
		}
	}

	/**
	 * We initialize the configuration
	 * 
	 * @param vertx
	 */
	public static void initConfiguration(final Vertx vertx) {
		System.out.println("Starting the initConfiguration");
		ConfigStoreOptions fileStore = new ConfigStoreOptions().setType("file")
				.setConfig(new JsonObject().put("path", fileConfName));
		// can add many store different than the fileStore : https://vertx.io/docs/vertx-config/java/
		ConfigRetrieverOptions options = new ConfigRetrieverOptions().addStore(fileStore);
		options.setScanPeriod(1000);
		ConfigRetriever retriever = ConfigRetriever.create(vertx, options);
		retriever.getConfig(ar -> {
			if (ar.failed()) {
				System.out.println("Failed to retrieve the configuration");
				config = new JsonObject();
			} else {
				System.out.println("Found the configuration" + ar.result().toString());
				config = ar.result();
				updateObserver();
			}
		});
		// when the configuration change, we can trigger an update
		retriever.listen(change -> {
			config = change.getNewConfiguration();
			updateObserver();
		});
	}

	public static JsonObject getConfig() throws InterruptedException {
		return config;
	}

}