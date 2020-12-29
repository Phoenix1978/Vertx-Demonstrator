package vertx.dog;

import java.util.List;

import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import vertx.entity.Dog;

/**
 * Class defining the handling of dogs for the verticle DogApiVerticle
 *	Nothing special to explain there about vertx
 */
public class DogResource {

  private final DogService dogService = new DogService();

  public Router getSubRouter(final Vertx vertx) {
    final Router subRouter = Router.router(vertx);

    // Body handler
    subRouter.route("/*").handler(BodyHandler.create());

    // Routes
    subRouter.get("/").handler(this::getAllDogs);
    subRouter.get("/:id").handler(this::getOneDog);
    subRouter.post("/").handler(this::createOneDog);
    subRouter.put("/:id").handler(this::updateOneDog);
    subRouter.delete("/:id").handler(this::deleteOneDog);

    return subRouter;
  }

	private void getAllDogs(final RoutingContext routingContext) {
		System.out.println("Dans getAllDogs...");

    final List<Dog> dogs = dogService.findAll();

    final JsonObject jsonResponse = new JsonObject();
    jsonResponse.put("dogs", dogs);
    jsonResponse.put("my-name", "Thierry");

    routingContext.response()
        .setStatusCode(200)
        .putHeader("content-type", "application/json")
        .end(Json.encode(jsonResponse));
  }

  private void getOneDog(final RoutingContext routingContext) {
	  System.out.println("Dans getOneDog...");

    final String id = routingContext.request().getParam("id");

    final Dog dog = dogService.findById(id);

    if (dog == null) {
      final JsonObject errorJsonResponse = new JsonObject();
      errorJsonResponse.put("error", "No dog can be found for the specified id:" + id);
      errorJsonResponse.put("id", id);

      routingContext.response()
          .setStatusCode(404)
          .putHeader("content-type", "application/json")
          .end(Json.encode(errorJsonResponse));
      return;
    }
    routingContext.response()
        .setStatusCode(200)
        .putHeader("content-type", "application/json")
        .end(Json.encode(dog));
  }
  private void createOneDog(final RoutingContext routingContext) {
	  System.out.println("Dans createOneDog...");
    final JsonObject body = routingContext.getBodyAsJson();
    final String name = body.getString("name");
    final String race = body.getString("race");
    final Integer age = body.getInteger("age");
    // TODO V�rification des champs...
    final Dog dog = new Dog(null, name, race, age);
    final Dog createdDog = dogService.add(dog);
    routingContext.response()
        .setStatusCode(201)
        .putHeader("content-type", "application/json")
        .end(Json.encode(createdDog));
  }
  private void updateOneDog(final RoutingContext routingContext) {
	  System.out.println("Dans updateOneDog...");
    final String id = routingContext.request().getParam("id");
    final JsonObject body = routingContext.getBodyAsJson();
    final String name = body.getString("name");
    final String race = body.getString("race");
    final Integer age = body.getInteger("age");
    // TODO Vérification des champs...
    final Dog dog = new Dog(id, name, race, age);
    final Dog updatedDog = dogService.update(dog);
    routingContext.response()
        .setStatusCode(200)
        .putHeader("content-type", "application/json")
        .end(Json.encode(updatedDog));
  }
  private void deleteOneDog(final RoutingContext routingContext) {
	  System.out.println("Dans deleteOneDog...");
    final String id = routingContext.request().getParam("id");
    dogService.remove(id);
    routingContext.response()
        .setStatusCode(200)
        .putHeader("content-type", "application/json")
        .end();
  }
}