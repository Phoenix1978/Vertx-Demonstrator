package vertx;

import java.lang.management.ManagementFactory;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import vertx.auth.AuthentTCPVerticle;
import vertx.auth.AuthentVerticle;
import vertx.dog.DogApiVerticle;
import vertx.verticle.HelloVerticle;

/**
 * The main application to be launched, 
 * Important !!! This class is not used for test !!!
 *
 */
public class App {

	public static void main(String[] args) throws InterruptedException {
		
		final ClusterManager mgr = new HazelcastClusterManager();
		final VertxOptions optionsVertx = new VertxOptions().setClusterManager(mgr);
		
		// we clustered the vertx to be able to work on the same event bus across multi JVM
		Vertx.clusteredVertx(optionsVertx, res -> {
            if (res.succeeded()) {
            	// when succeeded we initialize the app with verticles
                System.out.println("Cluster ok");
                initApp(res.result());
            } else {
                System.out.println("Cluster FAIL !!!");
            }
        });
	}
	
	/**
	 * We'll initialize the verticles
	 * @param vertx
	 */
	private static void initApp(Vertx vertx) {
		
		System.out.println(new StringBuilder("Starting the App")
				.append(" , \n                 my thread Id is the [")
				.append(Thread.currentThread().getId())
				.append("] \n                 and its name is [")
				.append(Thread.currentThread().getName())
				.append("] \n                 EventBus : [")
				.append(vertx.eventBus().hashCode())
				.append("]"));
		
		// for multi deployment
		Configurator.initConfiguration(vertx);
		final DeploymentOptions options = new DeploymentOptions() 
		        .setInstances(3);
		vertx.deployVerticle(DogApiVerticle.class.getName(), options);
		HelloVerticle myVerticle = new HelloVerticle("Hello Verticle");
		vertx.deployVerticle(myVerticle);
		vertx.deployVerticle(myVerticle);
		vertx.deployVerticle(AuthentVerticle.class.getName(), options);
		vertx.deployVerticle(AuthentTCPVerticle.class.getName(), options);
		
		new EventBusConsumerFactory(vertx);
		
		// we check which JVM is used
		// useful when we launched many times the app by an external JAR
		String jvmName = ManagementFactory.getRuntimeMXBean().getName();		
		System.out.printf("The JVM is [%s]%n", jvmName);
		System.out.println("StartED the App");	
		
	}
}