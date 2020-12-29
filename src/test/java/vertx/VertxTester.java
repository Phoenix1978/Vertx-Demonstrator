package vertx;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetSocket;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.client.impl.HttpResponseImpl;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import vertx.auth.AuthentTCPVerticle;
import vertx.auth.AuthentVerticle;
import vertx.verticle.HelloVerticle;

@RunWith(VertxUnitRunner.class)
public class VertxTester {

	Vertx vertx = null;
	/* this variable are useful to check every call return the right number of answers */
	Boolean doneMessageDirectlyOnEventBus = false;
	Integer doneMessageAuthentOnEventBus = 0;
	Integer doneMessageTCP = 0;

	@Before
	public void setup(TestContext testContext) throws InterruptedException {
		// Clustering
		final ClusterManager mgr = new HazelcastClusterManager();
		final VertxOptions optionsVertx = new VertxOptions().setClusterManager(mgr);
		// we enhance the count of checking because otherwise it logs a lot of line when we stop at a breakpoint
		optionsVertx.setBlockedThreadCheckInterval(1_000_000L);
		
		Vertx.clusteredVertx(optionsVertx, res -> {
			if (res.succeeded()) {
				System.out.println("Cluster ok");
				vertx = res.result();
			} else {
				System.out.println("Cluster FAIL !!!");
			}
		});
		//vertx = Vertx.vertx();
		// we check vertx has well started before to go further
		while (vertx == null) {			
			Thread.sleep(1000);
		}
	}

	@After
	public void tearDown(TestContext testContext) {
		vertx.close(testContext.asyncAssertSuccess());
	}

	/**
	 * Testing of the Hello Verticle, 
	 * 	Here we just check that the return string contains the word "Hello"
	 * @param testContext
	 */
	@Test
	public void testHelloVerticle(TestContext testContext) {
		HelloVerticle myVerticle = new HelloVerticle("Hello Verticle");
		vertx.deployVerticle(myVerticle, testContext.asyncAssertSuccess());
		Async async = testContext.async();
		WebClient.create(vertx, new WebClientOptions()).get(HelloVerticle.defaultPort, "localhost", "/")
				.as(BodyCodec.string()).send(ar -> {
					if (ar.succeeded()) {
						HttpResponse<String> response = ar.result();
						System.out.println("Got HTTP response body");
						System.out.println(response.body().toString());
						testContext.assertTrue(response.body().toString().contains("Hello"));
					} else {
						ar.cause().printStackTrace();
					}
					async.complete();
					testContext.assertTrue(ar.succeeded());
				});
	}
	
	/**
	 * Will check the writing something directly on the event bus works well (at the right address) 
	 * @param testContext
	 * @throws InterruptedException
	 */
	@Test
	public void testSendingMessageDirectlyOnEventBus(TestContext testContext) throws InterruptedException {

		new EventBusConsumerFactory(vertx);
		vertx.eventBus().<JsonObject>request(EventBusConsumerFactory.getAddress(),
				EventBusConsumerFactory.getMessageToSent(), reply -> {
					if (reply.succeeded()) {
						String body = reply.result().body().getString("message");
						System.out.println("RESULT from call on event bus: " + body);
					} else {
						System.out.println("RESULT from call on event bus: ERROR");
					}
					this.doneMessageDirectlyOnEventBus = true;
					testContext.assertTrue(reply.succeeded());
				});
		// we need to wait until asynchronous calls finish
		while (this.doneMessageDirectlyOnEventBus == false) {
			Thread.sleep(1000);
		}
	}
	
	/**
	 * Will check the Authent verticle with right and wrong credentials
	 * 	calls will be made over the event bus directly (not a server)
	 * @param testContext
	 * @throws InterruptedException
	 */
	@Test
	public void testAuthent(TestContext testContext) throws InterruptedException {
		AuthentVerticle myVerticle = new AuthentVerticle();
		vertx.deployVerticle(myVerticle);
		JsonObject myMessageOk = new JsonObject();
		// TEST with GOOD credentials
		myMessageOk.put("login", "john");
		myMessageOk.put("password", "LetM3B3R00t");
		makeAuthentCall(myMessageOk, true, testContext);
		// TEST with BAD credentials
		JsonObject myMessageNOk = new JsonObject();
		myMessageNOk.put("login", "john");
		myMessageNOk.put("password", "aaaaaaaaaaaaaa");
		makeAuthentCall(myMessageNOk, false, testContext);	
		// we need to wait until asynchronous calls finish	
		while (this.doneMessageAuthentOnEventBus != 2) {
			Thread.sleep(1000);
		}
	}
	
	/**
	 * Simply to avoid writing twice the same code ...
	 * @param myMessage
	 * @param expectedResult
	 * @param testContext
	 */
	private void makeAuthentCall(JsonObject myMessage, boolean expectedResult, TestContext testContext) {
		vertx.eventBus().<JsonObject>request(AuthentVerticle.getAddressEventBus(),
				myMessage , reply -> {
					if (reply.succeeded()) {
						System.out.println("RESULT authent: return ok");
					} else {
						System.out.println("RESULT authent: ERROR");
					}
					this.doneMessageAuthentOnEventBus++;
					try {
						testContext.assertTrue(reply.result().body().getBoolean("success") == expectedResult);
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				});
	}
	
	/** Will check the Authent verticle with right and wrong credentials
	 * 	calls will be made over the a server 
	 * @param testContext
	 */
	@Test
	public void checkAuthentHttpServer(TestContext testContext) {
		AuthentVerticle myVerticle = new AuthentVerticle();
		vertx.deployVerticle(myVerticle);
		Async async = testContext.async();
		JsonObject myMessageOk = new JsonObject();
		// TEST with GOOD credentials
		myMessageOk.put("login", "john");
		myMessageOk.put("password", "LetM3B3R00t");
		WebClient myClient = WebClient.create(vertx, new WebClientOptions());	
		myClient.post(AuthentVerticle.defaultPort, "localhost", "/auth")
				.as(BodyCodec.jsonObject())
				.sendJson(myMessageOk, ar -> {
					if (ar.succeeded()) {
						JsonObject response = (JsonObject) ((HttpResponseImpl<?>) ar.result()).body();
						System.out.println("Got HTTP response body for authent request");
						System.out.println(response.toString());
						testContext.assertTrue(response.getBoolean("success") == true);
					} else {
						ar.cause().printStackTrace();
					}					
					async.complete();
					testContext.assertTrue(ar.succeeded());
					testContext.asyncAssertSuccess();
				});			
	}
		
	
	/**
	 * Will check the verticle that create a Net/TCP Server with discussion by socket
	 * @param testContext
	 */
	@Test
	public void testTCPVerticle(TestContext testContext) {
		AuthentTCPVerticle myVerticle = new AuthentTCPVerticle();
		vertx.deployVerticle(myVerticle);
		Async async = testContext.async();
		
		NetClientOptions options = new NetClientOptions().setSsl(true).setTrustAll(true);
		// I got difficulties to increase again that number
		Integer countCalls = 100;
	    vertx.createNetClient(options).connect(AuthentTCPVerticle.defaultPort, "localhost", res -> {
	      if (res.succeeded()) {
	        NetSocket sock = res.result();
	        sock.handler(buff -> {
	        	String buffer = buff.toString("UTF-8");
	          System.out.println("tester client receiving " + buffer);
	          doneMessageTCP += StringUtils.countMatches(buffer, "hello");
	          if(doneMessageTCP == countCalls) {
				  async.complete();	 
	          }
	        });

	        // Now send some data
	        for (int i = 0; i < countCalls; i++) {
	          String str = "hello " + i + "\n";
	          System.out.println("Net client sending: " + str);	          
	          sock.write(str);
	        }
	      } else {
	        System.out.println("Failed to connect " + res.cause());
			  async.complete();	        
	      }
	    });
	}
	
	
	
	
	
	
	
	
	
	
	
}
