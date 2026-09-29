# Common coding practices

- Rule No.1: Before you start implementing any code, please read all the ADRs in the `adr` directory, as they contain
  important information about the architecture and coding practices of this project.

- Do not rely on databases to generate IDs, instead generate IDs within your own code
  using [SnowflakeIdGenerator.newSnowflakeId()](../src/main/java/com/company/andy/common/utils/SnowflakeIdGenerator.java).
  This means when the object is created, its ID should already been generated in the constructor. Reason: This decouples
  the code from database implementations and also makes testing much easier. Example:

```java
    public static String newEquipmentId() {
        return "EQP" + newSnowflakeId(); // Generate ID in the code
    }
```

- When writing unit tests, you can use `@Mock` and `@InjectMocks` to manage mocks.
  Example: [EquipmentDomainServiceTest](../src/test/java/com/company/andy/feature/equipment/domain/EquipmentDomainServiceTest.java).
  More details on testing strategy please refer to [014_testing_strategy.md](./014_testing_strategy.md).

- All integration test classes should extend [IntegrationTest](../src/test/java/com/company/andy/IntegrationTest.java).
  Example: [EquipmentControllerTest](../src/test/java/com/company/andy/feature/equipment/controller/EquipmentControllerTest.java).
  More details on testing strategy please refer to [014_testing_strategy.md](./014_testing_strategy.md).

- In domain objects, such as AggregateRoots/DomainServices/DomainEvents, use the
  generic [Actor](../src/main/java/com/company/andy/common/model/actor/Actor.java) as much as possible, don't use
  specific actor types such as `OrgActor` or `PlatformActor` unless really needed. Reason: This makes the domain objects
  more generic and reusable, and also decouples the domain objects from specific actor types. Example:

```java
    public void updateName(String newName, Actor actor) {
        if (Objects.equals(newName, this.name)) {
            return;
        }
        this.name = newName;
        raiseEvent(new EquipmentNameUpdatedEvent(name, this, actor));
    }
```

- Prefer using Java Record over Lombok for ValueObjects. Reason: Records are Java's built-in support, they are more
  concise and embodies common best practices like immutability. Example:

```java
public record QDetailedEquipment(
        String id,
        String orgId,
        String name,
        EquipmentStatus status,
        Instant createdAt,
        String createdBy) {}
```

- Never use Lombok's `@Setter` and `@Data`(which implicitly creates setters). Reason: Setters are bad as they break the
  principles of cohesion and information hiding. Also, objects with setters are just data containers like C's struct,
  they do not convey any business intent, making the code hard to understand.

- Always use [ServiceException](../src/main/java/com/company/andy/common/exception/ServiceException.java) for raising
  exceptions, don't create your own exception classes. Reason: The
  `ServiceException` is a flat exception model that makes exception modeling much easier than hierarchical exceptions,
  as otherwise you end up creating a lot of exception classes that are hard to maintain. Example:

```java
public void someMethod() {
  // more code here
  throw new ServiceException(EQUIPMENT_NAME_ALREADY_EXISTS,
      "Equipment Name Already Exists.",
      NullableMapUtils.mapOf(AggregateRoot.Fields.id, equipment.getId(), Equipment.Fields.name, newName));
}
```

- All pagination request should use HTTP POST method. The query class should
  extend [PageQuery](../src/main/java/com/company/andy/common/utils/PageQuery.java) which has the following pagination
  fields:
    - `pageNumber`: the zero-based page index
    - `pageSize`: the page size
    - `sortField`: the field name to be sorted
    - `sortOrder`: the sorting order
    - The following annotations are required on all pagination requests:
        - `@Getter`: for getter field values
        - `@SuperBuilder`: for builder
        - `@NoArgsConstructor(access = PRIVATE, onConstructor_ = @JsonCreator)`: for Json deserialization

Example:

```java
@Getter
@SuperBuilder
@NoArgsConstructor(access = PRIVATE, onConstructor_ = @JsonCreator)
public class PageEquipmentsQuery extends PageQuery {
  @Schema(description = "Search text")
  @Max(50)
  private String search;

  @Schema(description = "Equipment status to query")
  private EquipmentStatus status;
}
```

The controller receives a query object using POST method:

```java
    @Operation(summary = "Query equipments")
    @PostMapping("/paged")
    public PagedResponse<QPagedEquipment> pageEquipments(@RequestBody @Valid PageEquipmentsQuery query, @AuthenticationPrincipal @NotNull OrgActor actor) {
        return this.equipmentQueryService.pageEquipments(query, actor);
    }
```

- All pagination response should
  return [PagedResponse](../src/main/java/com/company/andy/common/utils/PagedResponse.java). Reason: a unified response
  model makes it easier for clients(such as web frontend) to consume and also makes the code more consistent.

- Use Java 8's `Instant` to represent timestamp, don't use `OffsetDateTime` or `ZonedDateTime`. Reason: `Instant` is
  designed for such purpose, there is no point in storing timezone information inside a timestamp.

- Use Lombok's `@FieldNameConstants` to access objects' field names. For example, when accessing MongoDB, filed names
  are usually needed:

```java
@FieldNameConstants
public class Equipment extends AggregateRoot {
  private EquipmentStatus status;
}
```

```java
private void someMethod() {
  // Use "Equipment.Fields.status" to access Equipment's "status" field
  criteria.and(Equipment.Fields.status).is(query.status());
}
```

- Do not
  use [Spring Data Repository](https://docs.spring.io/spring-data/commons/reference/repositories/query-methods-details.html).
  Reason: Spring Data's auto generated repository query method names can be very long and hard to read, also it cannot
  survive code refactoring. Instead, implement your own repository classes which
  extends [AbstractMongoRepository](../src/main/java/com/company/andy/common/mongo/AbstractMongoRepository.java), this
  gives you more freedom:

```java
@Repository
@RequiredArgsConstructor
public class EquipmentRepository extends AbstractMongoRepository<Equipment> {}
```

- Always enable transaction in CommandServices by using `@Transactional`:

```java
@Transactional
public String createEquipment(CreateEquipmentCommand command, OrgActor actor) {
  Equipment equipment = equipmentFactory.create(command.name(), actor);
  equipmentRepository.save(equipment);
  log.info("Created Equipment[{}].", equipment.getId());
  return equipment.getId();
}
```

- CommandService's methods should contain logs of what has been done.

- Logs should contain objects' IDs and other important information that can help debugging and tracing:

```java
log.info("Created Equipment[{}].", equipment.getId());
```

- Do not create interface classes for services until really needed. Reason: the public methods on service classes
  already serve as interfaces in general terms.

- Use [Actor](../src/main/java/com/company/andy/common/model/actor/Actor.java) to pass current user context around, do
  not use Spring Security's `SecurityContextHolder` for retrieving user information. Reason:
  `SecurityContextHolder` is essentially thread scoped global variable, it makes the code implicit and also makes
  testing harder. You can find more details on this
  in [006_explicitly_pass_through_actor_context.md](./006_explicitly_pass_through_actor_context.md).

- There are two [TaskExecutors](../src/main/java/com/company/andy/common/configuration/TaskExecutorConfiguration.java)
  in the application, choose them wisely:
    - `APPLICATION_TASK_EXECUTOR`: this is the primary one, it uses virtual threads, generally you should use this one,
      especially for I/O intensive tasks.
    - `THREAD_POOL_TASK_EXECUTOR`: this is a classic thread pool based executor, it should only be used for CPU
      intensive tasks.

- Single item configuration fields should be put under `common` section in `application.yaml`
  and [CommonProperties](../src/main/java/com/company/andy/common/configuration/property/CommonProperties.java).

- All caches should be configured
  in [CacheConfiguration](../src/main/java/com/company/andy/common/cache/CacheConfiguration.java) with explict
  `JacksonJsonRedisSerializer` before use, as otherwise the jackson serialization/deserialization may not work properly:

```java
  @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(ObjectMapper objectMapper) {
        return builder -> builder
                // more code here
                .withCacheConfiguration(SYSTEM_SETTINGS_CACHE, defaultCacheConfig()
                        .prefixCacheNameWith(CACHE_PREFIX)
                        .serializeValuesWith(fromSerializer(new JacksonJsonRedisSerializer<>(objectMapper, SystemSettings.class)))
                        .entryTtl(ofDays(30)))
                ;
    }
```

- For DomainEvent publishing, events are firstly staged(saved) in the database within the same database transaction with
  the business objects, and then published asynchronously triggered by
  MongoDB's [Change Stream](https://www.mongodb.com/docs/manual/changestreams/). The Change Stream configuration can be
  found in [EventConfiguration](../src/main/java/com/company/andy/common/event/EventConfiguration.java). When publish
  your own DomainEvent, you don't need to touch all of these, instead just call `raiseDomainEvent()` method from your
  AggregateRoot and everything else will be handled for you automatically. More details please refer
  to [010_domain_event_publishing.md](./010_domain_event_publishing.md). For example:

```java
    public void updateName(String newName, Actor actor) {
        if (Objects.equals(newName, this.name)) {
            return;
        }
        this.name = newName;
        // Call raiseEvent() for publishing DomainEvents
        raiseEvent(new EquipmentNameUpdatedEvent(name, this, actor));
    }
```

- For event consuming, [EventConsumer](../src/main/java/com/company/andy/common/event/consume/EventConsumer.java) is the
  central place where all kinds of events (internal DomainEvents, external events etc.) are consumed. But you don't need
  to touch it when implementing your own event consuming process, instead just create an event handler class that
  extends [AbstractEventHandler](../src/main/java/com/company/andy/common/event/consume/AbstractEventHandler.java). More
  details on this please refer to [011_event_consuming.md](./011_event_consuming.md). Example:

```java
public class EquipmentCreatedAnotherEventHandler extends AbstractEventHandler<EquipmentCreatedEvent> {
    @Override
    public void handle(EquipmentCreatedEvent event, PlatformActor actor) {
        log.info("{} called for Equipment[{}].", this.getClass().getSimpleName(), event.getArId());
    }
}
```

- When implementing your own event handlers, you should carefully think about whether to override the following methods
  from [AbstractEventHandler](../src/main/java/com/company/andy/common/event/consume/AbstractEventHandler.java):

```java
    public boolean isIdempotent() {
        // By default, all handlers are assumed to be not idempotent by themselves
        return false;
    }

    public boolean isTransactional() {
        // By default, all handlers are assumed to be transactional, we should make handlers to be transactional as much as possible
        return true;
    }

    public int priority() {
        // Smaller value means higher priority and will be handled first
        return 0;
    }
```

- Sometimes you may need to make multiple DomainEvents inherit from the same intermediate base class other than directly
  inherit from [DomainEvent](../src/main/java/com/company/andy/common/event/DomainEvent.java). Please refer
  to [EquipmentUpdatedEvent](src/main/java/com/company/andy/feature/equipment/domain/event/EquipmentUpdatedEvent.java)
  as an example, where:
    - [EquipmentUpdatedEvent](src/main/java/com/company/andy/feature/equipment/domain/event/EquipmentUpdatedEvent.java)
      is the intermediate base class which itself inherits
      from [DomainEvent](src/main/java/com/company/andy/common/event/DomainEvent.java)
    - [EquipmentStatusUpdatedEvent](src/main/java/com/company/andy/feature/equipment/domain/event/EquipmentStatusUpdatedEvent.java)
      and [EquipmentNameUpdatedEvent](src/main/java/com/company/andy/feature/equipment/domain/event/EquipmentNameUpdatedEvent.java)
      are the subclasses
      of [EquipmentUpdatedEvent](src/main/java/com/company/andy/feature/equipment/domain/event/EquipmentUpdatedEvent.java)

- Sometimes you may need multiple event handlers handle the same event independently. In order to do so, just create
  multiple event handler classes with the same event type, and you are ready to go.
    - Example:
      both [EquipmentCreatedEventHandler](src/main/java/com/company/andy/feature/equipment/eventhandler/EquipmentCreatedEventHandler.java)
      and [EquipmentCreatedAnotherEventHandler](src/main/java/com/company/andy/feature/equipment/eventhandler/EquipmentCreatedAnotherEventHandler.java)
      handle the same
      event [EquipmentCreatedEvent](src/main/java/com/company/andy/feature/equipment/domain/event/EquipmentCreatedEvent.java)
      independently.

- For validating caches in integration tests, you can use `CacheManager` to retrieve the cache value and do validation.
  For example:

```java
assertNotNull(cacheManager.getCache(SYSTEM_SETTINGS_CACHE).get(SYSTEM_SETTINGS_ID));
```

- For testing, when assert some conditions that happens asynchronously, such as cache eviction, you can
  use [PollingAssertion](../src/test/java/com/company/andy/support/PollingAssertion.java) to poll and then assert:

```java
PollingAssertionpollAssert().run(() -> assertNotNull(cacheManager.getCache(SYSTEM_SETTINGS_CACHE).get(SYSTEM_SETTINGS_ID)));
```

- [Mongock](https://mongock.io/) is used for database migration, please refer
  to [Migration001_Sample](../src/main/java/com/company/andy/common/migration/Migration001_Sample.java) as a template
  when creating your own migrations.

- Database indexes should be managed by Mongock migrations,
  take [Migration002_BaseSetup](../src/main/java/com/company/andy/common/migration/Migration002_BaseSetup.java) as an
  example. Also, make sure that every index has a specified name for better index maintainability.

- [RateLimiter](../src/main/java/com/company/andy/common/ratelimiter/RateLimiter.java) can be used for rate limiting.
  Use it inside CommandService and QueryService:

```java
public class DemoReservationCommandService {
    private final RateLimiter rateLimiter;

    @Transactional
    public String createDemoReservation(CreateDemoReservationCommand command, PlatformActor actor) {
        rateLimiter.applyFor("create_demo_reservation", 5);
        // more code here
        return demoReservation.getId();
    }
}
```

- When adding new MongoDB collections, make sure you pre-create the collection
  in [ApplicationInitializer.ensureMongoCollectionsExists()](../src/main/java/com/company/andy/common/init/ApplicationInitializer.java).
  This helps to avoid potential issues with MongoDB's auto collection creation, such as transaction conflicts during
  testing. Example:

```java
    private void ensureMongoCollectionsExists() {
        createCollection(SystemSettings.class)
        createCollection(Equipment.class);
        // more code here
        log.info("Created all MongoDB collections.");
    }
```

- Use [RestClient](../src/main/java/com/company/andy/common/configuration/RestClientConfiguration.java) for calling
  external APIs. There are two RestClients which vary on how JWT token is obtained:
    - `JWT_RELAY_REST_CLIENT`: relays current actor's JWT token to call external APIs
    - `SERVICE_CLIENT_REST_CLIENT`: represents the application itself with JWT token being obtained automatically by
      Spring using Oauth2 client_credentials grant type

- [SpringKafkaEventListener](../src/main/java/com/company/andy/common/event/consume/infrastructure/SpringKafkaEventListener.java)
  and [SpringKafkaDomainEventSender](../src/main/java/com/company/andy/common/event/publish/infrastructure/SpringKafkaDomainEventSender.java)
  are the only two places where Kafka is touched, stick to this as it minimizes the coupling to Kafka and also makes it
  easier to switch to other messaging systems in the future if needed.

- If distributed lock is required, use
  Shedlock's [LockingTaskExecutor](../src/main/java/com/company/andy/common/configuration/DistributedLockConfiguration.java).

- Make configuration files, e.g. `application.yaml` as simple as possible, prefer using constants in the code for
  configuration items that rarely change.
