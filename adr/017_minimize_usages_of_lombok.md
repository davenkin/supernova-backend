# Minimize usages of lombok

## Context

Lombok is a great library for reducing boilerplate code, but it can also lead to issues if not used carefully. For
example, using `@Data` on a domain objects can lead to unintended consequences, such as allowing the creation of
invalid objects or making it harder to maintain the code in the long run.

## Decision

We choose to minimize the usages of Lombok in our codebase and use it only where it makes sense.

## Implementation

### AggregateRoots

- AggregateRoots are business objects, it's constructors normally contain business logic such as validation and
  initialization. Therefore, it's not recommended to use Lombok's `@Builder` for AggregateRoots, as it will bypass the
  constructor and might lead to invalid objects being created. Instead, you should provide explicit constructors for
  AggregateRoots. All AggregateRoots classes
  extend [AggregateRoot](../src/main/java/com/company/andy/common/model/AggregateRoot.java), when deserializing from
  MongoDB and Json, we need to provide a no-args constructor.
  Example [Equipment](../src/main/java/com/company/andy/feature/equipment/domain/Equipment.java):

```java
@Slf4j
@Getter
@FieldNameConstants // For accessing field names
@TypeAlias(EQUIPMENT_COLLECTION) // Use an explict type alias
@Document(EQUIPMENT_COLLECTION) // Explicitly set MongoDB collection name
@NoArgsConstructor(access = PRIVATE, onConstructor_ = @JsonCreator) // For Jackson and MongoDB deserialization
public class Equipment extends AggregateRoot {}
```

As you can see, there is no `@Data`, `@Setter`, `@Builder` or `@AllArgsConstructor` for the AggregateRoots, stick with
this in your own code.

### Entities under AggregateRoots

- Like AggregateRoots, Entities are also mutable and usually have their own business constructors as well. Therefore,
  it's not recommended to use Lombok's @Builder for Entities. A no-arg constructor should be used for deserializing
  from MongoDB and Json.
  Example [EquipmentEngine](../src/main/java/com/company/andy/feature/equipment/domain/EquipmentEngine.java):

```java
@Getter
@FieldNameConstants // For accessing field names
@NoArgsConstructor(access = PRIVATE, onConstructor_ = @JsonCreator) // For Jackson and MongoDB deserialization
public class EquipmentEngine {}
```

As you can see, there is no `@Data`, `@Setter`, `@Builder` or `@AllArgsConstructor` for the Entities under Aggregate
Roots, stick with this in your own code.

### ValueObjects

- ValueObjects are immutable, this means you should never use Lombok's `@Data` or `@Setter`.

- Prefer using Java's `Record` for ValueObjects, which eliminates all usages of Lombok.
  Example: [CreateDemoReservationCommand](../src/main/java/com/company/andy/feature/demoreservation/command/CreateDemoReservationCommand.java):

```java
public record CreateDemoReservationCommand(
        @Schema(description = "Contact mobile number")
        @NotBlank @MobileNumber String mobileNumber) {
}
```

- Based on the above Java `Record`, you might want to add a builder to it, this can be done with Lombok's `@Builder` as
  the only reference to Lombok.
  Example: [CreateEquipmentCommand](../src/main/java/com/company/andy/feature/equipment/command/CreateEquipmentCommand.java):

```java
@Builder
public record CreateEquipmentCommand(
        @Schema(description = "Name of the equipment")
        @NotBlank @Size(max = 100) String name) {
}
```

- If `Record` is not you choice, stick to the following Lombok annotations for ValueObjects:
    - `@Value` makes the class immutable and generates equals, hashCode, toString, getters, etc.
    - `@Builder` generates a builder for the class, which is useful for testing and also
    - `@AllArgsConstructor(access = PRIVATE, onConstructor_ = @JsonCreator)` generates a private all-args constructor
      and annotates it with `@JsonCreator` for Jackson deserialization. This is necessary because `@Builder` will
      generate an all-args constructor, and we want to hide it from public API while still allowing Jackson to use it
      for deserialization. Also, Spring Data MongoDB will by default use this all-args constructor to reconstruct the
      object from the database.
    - No `@Data` or `@Getter` or `@Setter` or `@NoArgsConstructor` etc. should be used for ValueObjects, as they are
      not needed and might cause confusion.

```java
@Value
@Builder
@AllArgsConstructor(access = PRIVATE, onConstructor_ = @JsonCreator)
public class CreateEquipmentCommand {

    @Size(max = MAX_PARAGRAPH_LENGTH)
    private final String content;
}
```

- For ValueObjects with class hierarchy, Java `Record` is not an option anymore, please refer
  to [PageQuery](../src/main/java/com/company/andy/common/utils/PageQuery.java) as an example:
    - `@Getter` for getting data
    - `@SuperBuilder` for builder
    - `@NoArgsConstructor(access = PROTECTED)` for Subclasses to call for object creation.

```java
@Getter
@SuperBuilder
@NoArgsConstructor(access = PROTECTED)
public abstract class PageQuery {}
```

In subclass [PageEquipmentsQuery](../src/main/java/com/company/andy/feature/equipment/query/PageEquipmentsQuery.java):

- `@Getter` for getting data
- `@SuperBuilder` for builder
- `@NoArgsConstructor(access = PRIVATE, onConstructor_ = @JsonCreator)` for Jackson deserialization and also to prevent
  public API usage of the no-args constructor, as the builder should be used for object creation.

```java
@Getter
@SuperBuilder
@NoArgsConstructor(access = PRIVATE, onConstructor_ = @JsonCreator)
public class PageEquipmentsQuery extends PageQuery {}
```