# Unified request process flow

## Context

AggregateRoot is the most important concepts in domain model. Nearly all operations in the software are centered around
AggregateRoots. Different types of operations might have their own process flows.

## Decision

We choose to follow a standard way to implement various **request process flows**.

## Implementation

### Overall architecture

![overall architecture](./image/overall-architecture.drawio.svg)

There are mainly 3 ways to interact with the software:

- Send HTTP request to the application
- Scheduled jobs triggered by timers
- Consuming events from Kafka

For HTTP requests, they can be further split into multiple sub-categories.

Given above, we have the following process flows:

- [HTTP request for creating AggregateRoot](#http-request-for-creating-aggregate-root)
- [HTTP request for updating AggregateRoot](#http-request-for-updating-aggregate-root)
- [HTTP request for deleting AggregateRoot](#http-request-for-deleting-aggregate-root)
- [HTTP request for querying AggregateRoot](#http-request-for-querying-aggregate-root)
- [Scheduled jobs triggered by timers](#scheduled-jobs-triggered-by-timers)
- [Consuming events from Kafka](#consuming-events-from-kafka)

### HTTP request for creating AggregateRoot

Creating data involves 2 major steps: Create and Save. Take "Creating an equipment" as an example, the request process
flow is:

```plantuml
@startuml
Actor -> Controller: createEquipment(command,actor)
Controller -> CommandService: createEquipment(command,actor)
CommandService -> Factory: create(command.name(), actor)
Factory -> AggregateRoot: new Equipment(name, actor)
AggregateRoot -> AggregateRoot: raiseEvent(new EquipmentCreatedEvent())
AggregateRoot --> Factory: created equipment
Factory --> CommandService: created equipment
CommandService -> Repository: save(equipment)
Repository --> CommandService: saved
CommandService --> Controller: equipment ID
Controller --> Actor: equipment ID
@enduml
```

1. `EquipmentController` receives the request:

```java
    @PostMapping
    @ResponseStatus(CREATED)
    @Operation(summary = "Create an equipment")
    public ResponseId createEquipment(@RequestBody @Valid CreateEquipmentCommand command, @AuthenticationPrincipal @NotNull OrgActor actor) {
        return new ResponseId(this.equipmentCommandService.createEquipment(command, actor));
    }
```

2. `EquipmentCommandService` orchestrates the creation process:

```java
    @Transactional
    public String createEquipment(CreateEquipmentCommand command, OrgActor actor) {
        Equipment equipment = equipmentFactory.create(command.name(), actor);
        equipmentRepository.save(equipment);
        log.info("Created Equipment[{}].", equipment.getId());
        return equipment.getId();
    }
```

3. `EquipmentFactory` creates the `Equipment` object. Remember, for code consistency, always use factory to create
   AggregateRoots:

```java
    public Equipment create(String name, OrgActor actor) {
        return new Equipment(name, actor);
    }
```

4. `Equipment` constructor generates the ID for `Equipment` using `newEquipmentId()`, then sets data fields, and raises
   `EquipmentCreatedEvent` using `raiseEvent()`. The `EquipmentCreatedEvent` will be sent to Kafka automatically by the
   event infrastructure and no further actions are required from your side:

```java
    public Equipment(String name, OrgActor actor) {
        requireNonBlank(name, "name must not be blank");
        requireNonNull(actor, "actor must not be null");

        super(newEquipmentId(), actor);
        this.name = name;
        this.engine = new EquipmentEngine("DEFAULT_ENGINE_MODEL");
        raiseEvent(new EquipmentCreatedEvent(this, actor));
    }

    public static String newEquipmentId() {
        return "EQP" + newSnowflakeId(); // Generate ID in the code
    }
```

5. `EquipmentRepository` saves the newly created `Equipment` object:

```java
public class EquipmentRepository extends AbstractMongoRepository<Equipment> {
  void save(Equipment equipment);
}
```

6. Return the ID of the newly created `Equipment` object to the caller.

### HTTP request for updating AggregateRoot

Updating data has 3 major steps: (1)Load the AggregateRoot; (2)Call AggregateRoot's business method; (3) Save it back to
database. Take "updating `Equipment`'s holder" as an example.

```plantuml
@startuml
Actor -> Controller: updateEquipmentHolder(equipmentId, command, actor)
Controller -> CommandService: updateEquipmentHolder(equipmentId, command, actor)
CommandService -> Repository: byId(id, actor.orgId())
Repository --> CommandService: fetched equipment
CommandService -> AggregateRoot: equipment.updateHolder(command.name(), actor)
AggregateRoot -> AggregateRoot: raiseEvent(new EquipmentHolderUpdatedEvent())
AggregateRoot --> CommandService: success
CommandService -> Repository: save(equipment)
Repository --> CommandService: success
CommandService --> Controller: success
Controller --> Actor: success
@enduml
```

1. `EquipmentController` receives the request:

```java
    @Operation(summary = "Update an equipment's holder")
    @PutMapping("/{id}/holder")
    public void updateEquipmentHolder(
            @PathVariable("id") @NotBlank String equipmentId,
            @RequestBody @Valid UpdateEquipmentHolderCommand command,
            @AuthenticationPrincipal @NotNull OrgActor actor) {
        this.equipmentCommandService.updateEquipmentHolder(equipmentId, command, actor);
    }
```

2. `EquipmentCommandService` orchestrates the update process:

```java
    @Transactional
    public void updateEquipmentHolder(String equipmentId, UpdateEquipmentHolderCommand command, OrgActor actor) {
        Equipment equipment = equipmentRepository.byId(equipmentId, actor.getOrgId());
        equipment.updateHolder(command.name(), actor);
        equipmentRepository.save(equipment);
        log.info("Updated holder for Equipment[{}].", equipment.getId());
    }
```

3. `EquipmentRepository` loads `Equipment` by its ID:

```java
Equipment equipment = equipmentRepository.byId(id, actor.getOrgId());
```

4. `Equipment`'s `updateHolder()` is called to update its state according business logic(rules), and also raise
   `EquipmentHolderUpdatedEvent` if the holder is changed:

```java
    public void updateHolder(String newHolder, Actor actor) {
        if (Objects.equals(this.holder, newHolder)) {
            return;
        }

        String oldHolder = this.holder;
        this.holder = newHolder;
        raiseEvent(new EquipmentHolderUpdatedEvent(oldHolder, newHolder, this, actor));
    }
```

5. `EquipmentRepository` saves the updated `Equipment` back into database:

```java
equipmentRepository.save(equipment);
```

6. No need to return anything from `EquipmentCommandService.updateEquipmentHolder()`.

Sometimes, the whole business logic is not suitable to be put inside AggregateRoot like `Equipment.updateHolder()`. For
such cases, we can use DomainServices. For example, when updating `Equipment`'s name, we need to check if the name is
already been occupied, which cannot be fulfilled by `Equipment` itself. Instead of calling `Equipment.updateName()`
directly from `EquipmentCommandService`, DomainService `EquipmentDomainService.updateEquipmentName()` is called from
`EquipmentCommandService`:

```plantuml
@startuml
Actor -> Controller: updateEquipmentName(equipmentId, command, actor)
Controller -> CommandService: updateEquipmentName(equipmentId, command, actor)
CommandService -> Repository: byId(id, actor.orgId())
Repository --> CommandService: fetched equipment
CommandService -> DomainService: updateEquipmentName(equipment, command.name(), actor)
DomainService -> DomainService: apply business logic
DomainService -> AggregateRoot: equipment.updateName(newName, actor);
AggregateRoot -> AggregateRoot: raiseEvent(new EquipmentNameUpdatedEvent())
AggregateRoot --> DomainService: success
DomainService --> CommandService: success
CommandService -> Repository: save(equipment)
Repository --> CommandService: success
CommandService --> Controller: success
Controller --> Actor: success
@enduml
```

```java
    @Transactional
    public void updateEquipmentName(String equipmentId, UpdateEquipmentNameCommand command, OrgActor actor) {
        Equipment equipment = equipmentRepository.byId(equipmentId, actor.getOrgId());
        equipmentDomainService.updateEquipmentName(equipment, command.name(), actor);
        equipmentRepository.save(equipment);
        log.info("Updated name for Equipment[{}].", equipment.getId());
    }

```

Inside `EquipmentDomainService.updateEquipmentName()`, it first checks whether the name is already taken, if not then
update `Equipment`'s name:

```java
    public void updateEquipmentName(Equipment equipment, String newName, Actor actor) {
        if (!Objects.equals(newName, equipment.getName()) &&
            equipmentRepository.existsByName(newName, equipment.getOrgId())) {
            throw new ServiceException(EQUIPMENT_NAME_ALREADY_EXISTS,
                    "Equipment Name Already Exists.",
                    mapOf(AggregateRoot.Fields.id, equipment.getId(), Equipment.Fields.name, newName));
        }

        equipment.updateName(newName, actor);
    }
```

### HTTP request for deleting AggregateRoot

For deleting data, first load the AggregateRoot and then delete it. For example, for deleting an `Equipment`:

```plantuml
@startuml
Actor -> Controller: deleteEquipment(equipmentId, actor)
Controller -> CommandService: deleteEquipment(equipmentId, actor)
CommandService -> Repository: byId(equipmentId, actor.orgId())
Repository --> CommandService: fetched equipment
CommandService -> AggregateRoot: equipment.onDelete(actor)
AggregateRoot -> AggregateRoot: raiseEvent(new EquipmentDeletedEvent())
AggregateRoot --> CommandService: success
CommandService -> Repository: delete(equipment)
Repository --> CommandService: success
CommandService --> Controller: success
Controller --> Actor: success
@enduml
```

1. `EquipmentController` receives the request:

```java
    @Operation(summary = "Delete an equipment")
    @DeleteMapping("/{id}")
    public void deleteEquipment(@PathVariable("id") @NotBlank String equipmentId, @AuthenticationPrincipal @NotNull OrgActor actor) {
        this.equipmentCommandService.deleteEquipment(equipmentId, actor);
    }
```

2. `EquipmentCommandService` orchestrates the deletion process:

```java
    @Transactional
    public void deleteEquipment(String equipmentId, OrgActor actor) {
        Equipment equipment = equipmentRepository.byId(equipmentId, actor.getOrgId());
        equipment.onDelete(actor);
        equipmentRepository.delete(equipment);
        log.info("Deleted Equipment[{}].", equipmentId);
    }
```

3. `EquipmentRepository` loads the `Equipment` by equipment ID and org ID:

```java
Equipment equipment = equipmentRepository.byId(equipmentId, actor.orgId());
```

4. `Equipment.onDelete()` is called to do some pre-deletion work such as raising DomainEvents:

```java
    public void onDelete(Actor actor) {
        raiseEvent(new EquipmentDeletedEvent(this, actor));
    }
```

5. `EquipmentRepository` deletes the objects using `delete()`. You might be wondering why we need to first load the
   `Equipment` into memory then do the deletion. Will it be much simpler to directly delete by ID? The reason is that,
   before deletion, there might be some validations that need to happen, and also it might raise DomainEvents. So, in
   order to ensure such possibilities, the whole `Equipment` object is loaded into the memory.

### HTTP request for querying AggregateRoot

There are two ways to query data:

1. Load the domain entity from DB using Repository, then convert the domain entity into response object
2. Use [CQRS](./004_use_lightweight_cqrs.md), namely bypass the domain layer and query the database directly, this is
   preferred as it does not couple with the domain layer and also fetches just enough data from database which improves
   performance

For using [CQRS](./004_use_lightweight_cqrs.md), querying data can bypass the domain models and talk to database
directly. For example, when querying a list of `Equipment`s:

```plantuml
@startuml
Actor -> Controller: pageEquipments(query, actor)
Controller -> QueryService: pageEquipments(query, actor)
QueryService -> MongoTemplate: find()
MongoTemplate --> QueryService: fetched equipments
QueryService --> Controller: fetched equipments
Controller --> Actor: fetched equipments
@enduml
```

1. The request hits `EquipmentController`, which further calls `EquipmentQueryService.pageEquipments()`:

```java
    @Operation(summary = "Query equipments with pagination")
    @PostMapping("/paged")
    public PagedResponse<QPagedEquipment> pageEquipments(
            @RequestBody @Valid PageEquipmentsQuery query,
            @AuthenticationPrincipal @NotNull OrgActor actor) {
        return this.equipmentQueryService.pageEquipments(query, actor);
    }
```

`EquipmentQueryService` is at the same level with `EquipmentCommandService`, they both are under the category of
`ApplicationService`.

2. `EquipmentQueryService.pageEquipments()` uses `MongoTemplate` to query data from database directly, and uses its own
   query model `QPagedEquipment`:

```java
    public PagedResponse<QPagedEquipment> pageEquipments(PageEquipmentsQuery query, OrgActor actor) {
        Criteria criteria = where(AggregateRoot.Fields.orgId).is(actor.getOrgId());
        // code omited
        List<QPagedEquipment> equipments = mongoTemplate.find(mongoQuery.with(pageable), QPagedEquipment.class, EQUIPMENT_COLLECTION);
        return new PagedResponse<>(equipments, pageable, count);
    }
```

### Scheduled jobs triggered by timers

```plantuml
@startuml
Timer -> Scheduler: remindForEquipmentMaintenance()
Scheduler -> Actor: createScheduledJobActor()
Actor --> Scheduler: actor
Scheduler -> ActorMdcSupport: runWithMdc()
ActorMdcSupport -> ScheduledJob: run()
ScheduledJob --> ActorMdcSupport: success
ActorMdcSupport --> Scheduler:success
Scheduler --> Timer: success
@enduml
```

1. First create a scheduler in the `scheduledjob` package:

```java
@Slf4j
@RequiredArgsConstructor
@Configuration(proxyBeanMethods = false)
public class EquipmentJobScheduler {
    private final MaintenanceReminderScheduledJob maintenanceReminderScheduledJob;

    @Scheduled(cron = "0 10 2 1 * ?")
    @SchedulerLock(name = "remindForEquipmentMaintenance")
    public void remindForEquipmentMaintenance() {
        assertLocked();

        PlatformActor actor = createScheduledJobActor("remindForEquipmentMaintenance");
        ActorMdcSupport.runWithMdc(actor, () -> maintenanceReminderScheduledJob.run(actor));
    }
}
```

The `ActorMdcSupport.runWithMdc()` is used to set the `Actor` information into MDC.

2. Then create a scheduled job class:

```java
@Slf4j
@Component
@RequiredArgsConstructor
public class MaintenanceReminderScheduledJob {

  public void run(PlatformActor actor) {
    log.info("MaintenanceReminderScheduledJob started.");

    //do something

    log.info("MaintenanceReminderScheduledJob ended.");
  }
}
```

The job class serves the same purpose as `CommandService`, which orchestrates various other components such as
`Repository`, `AggreateRoot` and `Factory`. Hence, the job itself should not contain business logic.

### Consuming events from Kafka

The Kafka event consuming infrastructure is already set up for you. You only need to do 2 things for consuming events.

```plantuml
@startuml
ListenerContainer -> SpringKafkaEventListener: listenDomainEvent(event)
SpringKafkaEventListener -> EventConsumer: consumeDomainEvent(event)
EventConsumer -> EventHandler: handle(event, actor)
EventHandler -> EventConsumer: success
EventConsumer -> SpringKafkaEventListener: success
SpringKafkaEventListener -> ListenerContainer: success
@enduml
```

1. Make sure the topic is subscribed in `SpringKafkaEventListener` by configuring
   `topics = {KAFKA_DOMAIN_EVENT_TOPIC},`:

```java
@Slf4j
@Component
@DisableForIT // Disable Kafka Listener for integration tests
@RequiredArgsConstructor
public class SpringKafkaEventListener {
    private final EventConsumer eventConsumer;

    // Listen to DomainEvents which are published by ourselves
    @KafkaListener(id = "domain-event-listener",
            groupId = "domain-event-listener",
            topics = {KAFKA_DOMAIN_EVENT_TOPIC},
            concurrency = "3")
    public void listenDomainEvent(DomainEvent event) {
        this.eventConsumer.consumeDomainEvent(event);
    }

    // You may add more @KafkaListener annotated methods for different topics if needed

}
```

You may add more `@KafkaListener` methods for consuming different topics if needed.

2. Create an EventHandler class that extends `AbstractEventHandler`:

```java
@Slf4j
@Component
@RequiredArgsConstructor
public class EquipmentCreatedEventHandler extends AbstractEventHandler<EquipmentCreatedEvent> {

    @Override
    public void handle(EquipmentCreatedEvent event, PlatformActor actor) {
        log.info("{} called for Equipment[{}].", this.getClass().getSimpleName(), event.getArId());
    }
}
```

The `EventHandler` serves the same purpose as `CommandService`, which orchestrates various other components such as
`Repository`, `AggreateRoot` and `Factory`. Hence, the `EventHandler` itself should not contain business logic.
