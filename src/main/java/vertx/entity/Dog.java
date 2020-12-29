package vertx.entity;

/**
 * Simply an entity of dogs
 *
 */
public class Dog {
  private final String id;
  private final String name;
  private final String race;
  private final int age;
  public Dog(final String id, final String name, final String race, final int age) {
    super();
    this.id = id;
    this.name = name;
    this.race = race;
    this.age = age;
  }
  public String getId() {
    return id;
  }
  public String getName() {
    return name;
  }
  public String getRace() {
    return race;
  }
  public int getAge() {
    return age;
  }
  @Override
  public String toString() {
    return "Dog [id=" + id + ", name=" + name + ", race=" + race + ", age=" + age + "]";
  }
}