# Project structure

## Context

When structure software projects, there are mainly 2 approaches:

- Structure by technical layers first: create technical layer packages first, such as `domain`, `service`,
  `infrastructure`, then put business objects into these layers.
- Structure by business first: create business packages first, then put technical layers inside these packages.

## Decision

We choose "**business first, technical layers second**" approach as the project structure, which means we organize the
code by business entities([AggregateRoot](https://martinfowler.com/bliki/DDD_Aggregate.html)) first, and then by
technical layers.

This approach is more intuitive and easier to understand, as it allows developers to focus on the business first.
Developers can easily get an overall idea of what this application does by a simple glimpse at the high level business packages.

## Implementation

At the top level, there are two packages:

- `common`: contains all common code, such as Spring configuration, event infrastructure and database migration etc.
- `feature`: contains all domain feature code categorised by business entities


Keep the folder structure as flat as possible. The AggregateRoot is at the highest level under a
feature package, then followed by other technical layers, use the following structure:

The `1` in `(class:1)` indicates there can be only one class under a feature package, `(class:N)` for multiple.

- `AggregateRoot name`(folder:1): Top level package for a major business entity, such as `equipment`
    - `command`(folder:1):
        - `XxxCommandService`(class:1): CommandService
        - `XxxCommand`(class:N): Command object classes
    - `controller`(folder:1)
        - `XxxController`(class:1): Spring MVC controller
    - `domain`(folder:1)
        - `Xxx`(class:N): Domain objects such as AggregateRoots, entities and ValueObjects, they hold business logic.
        - `XxxRepository`(class:1): Repository for the AggregateRoot
        - `XxxFactory`(class:1): Factory for creating the AggregateRoot
        - `XxxDomainService`(class:N): DomainService for holding business logic that is not suitable for residing in
          domain objects, but still belongs to the domain layer.
        - `event`(folder:1): Contains domain objects
        - `task`(folder:1): Contains various tasks.
            - `XxxTask`: A single task
    - `eventhandler`(folder:1): Contains all the event handler classes
        - `XxxEventHandler`(class:N): Event handler
    - `infrastructure`(folder:1): Contains infrastructure classes
    - `scheduledjob`(folder:1): Contains background scheduled jobs that are related to the AggregateRoot
        - `XxxScheduler`(class:N): Scheduling configuration for the scheduled jobs
        - `XxxScheduledJob`(class:N): A single background scheduled job
    - `query`(folder:1): Contains query related classes
        - `XxxQueryService`(class:N): QueryServices
        - `XxxQuery`(class:N): Query request classes
        - `QXxx`(class:N): Query response classes

More detailed explanation:

- `AggregateRoot`(folder:1): The top level package, an AggregateRoot represents a major business entity(e.g.
  `equipment`).
    - `command`(folder:1): For sending commands to the application, "command" represents the "C"
      in [CQRS](https://microservices.io/patterns/data/cqrs.html).
        - `XxxCommandService`(class:1): The
          facade [application service](https://ddd-practitioners.com/home/glossary/application-service/)
          class for commands, should end with "CommandService". Every public method in CommandService represents a
          single use case. CommandServices should NOT contain business logic, instead they orchestrate other components to
          fulfil use cases.  
          Example: [EquipmentCommandService](../src/main/java/com/company/andy/feature/equipment/command/EquipmentCommandService.java).
        - `XxxCommand`(class:N): Represent a single command, it contains the data that you want to send to the
          application, should end with "Command".
          Example: [CreateEquipmentCommand](../src/main/java/com/company/andy/feature/equipment/command/CreateEquipmentCommand.java).
    - `controller`(folder:1): For HTTP controllers
        - `XxxController`(class:1): The controller class should end with "Controller". Controllers should be very thin,
          they call CommandServices or QueryServices upon receiving request.
          Example: [EquipmentController](../src/main/java/com/company/andy/feature/equipment/controller/EquipmentController.java).
    - `domain`(folder:1): Contains all the domain models.
        - `Xxx`(class:N): Domain objects hold business logic, they are why your application exists.
          Example: [Equipment](../src/main/java/com/company/andy/feature/equipment/domain/Equipment.java)
          and [EquipmentEngine](../src/main/java/com/company/andy/feature/equipment/domain/EquipmentEngine.java).
        - `XxxRepository`(class:1): Repositories are for retrieving and persisting AggregateRoots, should end with "
          Repository". Please be noted that
          repository is per AggregateRoot, namely only AggregateRoot can have repositories, but not all domain
          objects.
          Example: [EquipmentRepository](../src/main/java/com/company/andy/feature/equipment/domain/EquipmentRepository.java).
        - `XxxFactory`(class:1): Factory class for creating the AggregateRoots, should end with "Factory". The creation
          of AggregateRoots should be explicit, so always use factories to create them. Normally the factory firstly do
          some business validations and then call AggregateRoot's constructor to create the object. Example:
          [EquipmentFactory](../src/main/java/com/company/andy/feature/equipment/domain/EquipmentFactory.java).
        - `XxxDomainService`(class:N): A [DomainService](https://ddd-practitioners.com/home/glossary/domain-service/)
          class, like other domain objects, holds business logic. But, it should be your last resort when business logic
          cannot fit into other domain objects. DomainServices usually end with "DomainService", but you can use other
          meaningful suffixes as well such as "XxxChecker" or "XxxProvider".
          Example: [EquipmentDomainService](../src/main/java/com/company/andy/feature/equipment/domain/EquipmentDomainService.java).
        - `event`(folder:1): This folder contains all the DomainEvent classes that are raised by the AggregateRoots.
            - XxxEvent(class:N): DomainEvent class, should end with "Event", it represents a significant change in
              AggregateRoot. The naming convention is `[name of AggregateRoot] + [passive form of verbs] + Event`.
              Example: [EquipmentCreatedEvent](../src/main/java/com/company/andy/feature/equipment/domain/event/EquipmentCreatedEvent.java).
        - `task`(folder:1): Contains various tasks.
            - `XxxTask`(class:N): A task represents a standalone operation, should end with "Task". Tasks are usually
              called from scheduled jobs and event handlers.
              Example: [CountMaintenanceRecordsForEquipmentTask](../src/main/java/com/company/andy/feature/equipment/domain/task/CountMaintenanceRecordsForEquipmentTask.java).
    - `eventhandler`(folder:1): Contains all the event handler classes.
        - `XxxEventHandler`(class:N): Event handler class, should end with "EventHandler". Example:
          [EquipmentCreatedEventHandler](../src/main/java/com/company/andy/feature/equipment/eventhandler/EquipmentCreatedEventHandler.java).
    - `infrastructure`(folder:1): Contains the infrastructure code that is related to the AggregateRoot.
    - `scheduledjob`(folder:1):Contains background scheduled jobs that are related to the AggregateRoot.
        - `XxxScheduledJobScheduler`(class:N): Scheduling configuration, should end with "Scheduler".
          Example: [EquipmentJobScheduler](../src/main/java/com/company/andy/feature/equipment/scheduledjob/EquipmentJobScheduler.java).
        - `XxxScheduledJob`(class:N): Represents a background scheduled job, should end with "ScheduledJob". A scheduled job might run multiple tasks.
          Example:
          [RemoveOldMaintenanceRecordsScheduledJob](../src/main/java/com/company/andy/feature/maintenance/scheduledjob/RemoveOldMaintenanceRecordsScheduledJob.java).
    - `query`(folder:1): For querying data, "query" represents the "Q"
      in [CQRS](https://microservices.io/patterns/data/cqrs.html). Queries can bypass the domain model and hit database
      directly using what ever means that suit you.
        - `XxxQueryService`(class:N): The
          facade [application service](https://ddd-practitioners.com/home/glossary/application-service/) class for
          queries, should end with "QueryService". Compared with a single CommandService in `command` package, you may
          have multiple QueryServices under the `query` package. QueryServices usually hold query logic directly without
          calling other classes.
          Example: [EquipmentQueryService](../src/main/java/com/company/andy/feature/equipment/query/EquipmentQueryService.java).
        - `XxxQuery`(class:N): Request class of a query, should end with "Query".
          Example: [PageEquipmentsQuery](../src/main/java/com/company/andy/feature/equipment/query/PageEquipmentsQuery.java).
        - `QXxx`(class:N): Response class of a query, should start with the letter "Q".
          Example: [QPagedEquipment](../src/main/java/com/company/andy/feature/equipment/query/QPagedEquipment.java).
