## Introduction

This is a template Spring Boot 4 project with the following features:

- Data persistence using [Spring Data MongoDB](https://spring.io/projects/spring-data-mongodb/)
- Messaging using [Spring for Apache Kafka](https://spring.io/projects/spring-kafka)
- Caching using [Spring Data Redis](https://spring.io/projects/spring-data-redis)
- API documentation using [springdoc-openapi](https://springdoc.org/)
- Data migration using [Mongock](https://mongock.io/)
- Architecture validation using [ArchUnit](https://www.archunit.org/)
- Distributed lock for scheduled jobs using [Shedlock](https://github.com/lukas-krecan/ShedLock)
- Standardized [folder structure](adr/002_project_structure.md) with business first approach
- Standardized pagination implementation with [PageQuery](src/main/java/com/company/andy/common/utils/PageQuery.java)
  and [PagedResponse](src/main/java/com/company/andy/common/utils/PagedResponse.java)
- Builtin [Snowflake ID generator](src/main/java/com/company/andy/common/utils/SnowflakeIdGenerator.java)
- [DomainEvent](src/main/java/com/company/andy/common/event/DomainEvent.java) as first class citizen
- [DomainEvent publishing](adr/010_domain_event_publishing.md)
  using [Transactional Outbox](https://microservices.io/patterns/data/transactional-outbox.html) pattern
- [Event consuming](adr/011_event_consuming.md) mechanism with idempotency support
- Standardized [exception handling](adr/012_exception_handling.md)
- Lightweight [Command Query Responsibility Segregation (CQRS)](adr/004_use_lightweight_cqrs.md) implementation
- Domain modeling using [Domain Driven Design (DDD)](adr/003_use_ddd.md)
- Standardized [request process flow](adr/005_unified_request_process_flow.md)
- Standardized [object implementation pattern](adr/007_unified_object_implementation_patterns.md)
- Distributed tracing with [Micrometer tracing](https://docs.micrometer.io/tracing/reference/)
  and [OpenTelemetry](https://spring.io/blog/2025/11/18/opentelemetry-with-spring-boot)
- [RestClient](src/main/java/com/company/andy/common/configuration/RestClientConfiguration.java) for making external API
  calls

## Tech stack

- [Java 25](https://www.oracle.com/java/technologies/javase/jdk25-archive-downloads.html)
- [Spring Boot 4](https://docs.spring.io/spring-boot/index.html)
- [Spring Data MongoDB](https://spring.io/projects/spring-data-mongodb/)
- [Spring Data Redis](https://spring.io/projects/spring-data-redis/)
- [Spring for Apache Kafka](https://spring.io/projects/spring-kafka/)
- [Mongock](https://mongock.io/)
- [Shedlock](https://github.com/lukas-krecan/ShedLock)
- [springdoc-openapi](https://springdoc.org/)
- [OpenTelemetry](https://opentelemetry.io/)
- [Junit 5](https://junit.org/junit5/)
- [ArchUnit](https://www.archunit.org/)

## Editing tools

- We use [draw.io](https://www.drawio.com/) for drawing diagrams and export the diagrams into SVG file containing the original
  draw.io diagram data which allows for future editing. You can do this by enabling the `Include a copy of my diagram`
  option in the export dialog.
- We use [PlantUML](https://plantuml.com/) for UML diagrams in various documentation files. Please install
  PlantUML plugin in your IDE to view the UML diagrams. For example, in IntelliJ IDEA you can install
  the [plantuml4java](https://plugins.jetbrains.com/plugin/7017-plantuml4idea) plugin.

## How to run locally

- First run `./start-docker-compose.sh` to start the following middlewares using Docker:
    - `MongoDB`: localhost:27125
    - `Kafka`: localhost:9125
    - `Kafka UI`: [http://localhost:8125](http://localhost:8125)
    - `Keycloak`: [http://localhost:7125](http://localhost:7125), login with username `admin` and password `admin`, more
      local user accounts refer to [keycloak-data/README.md](keycloak-data/README.md)
    - `Redis`: localhost:6125, password: `aredissecret`
- Run the application locally in one of the following ways:
    - `./run-local.sh`: this starts the application with debug port on 5005, assuming docker-compose is already up
      running
    - Run `main()` in  `SpringBootWebApplication`, assuming docker-compose is already up running

- Open [http://localhost:5125/about](http://localhost:5125/about) to check if the application runs successfully
- Swagger UI: [http://localhost:5125/swagger-ui/index.html](http://localhost:5125/swagger-ui/index.html)
- Actuator endpoints: [http://localhost:5125/actuator](http://localhost:5125/actuator)
- To stop docker-compose and delete volume data, run `./stop-docker-compose.sh`

## How to build

- Run `./build.sh` to build the project locally

## How to run tests

- We do both unit testing and integration testing. To run a test, locate the test method inside IDE and run them directly from there.
- We have a [testing strategy](adr/014_testing_strategy.md), please read it before writing any tests.

## Architecture Decision Records (ADRs)

This project uses [Architecture Decision Records (ADRs)](https://adr.github.io/) to document important architectural
decisions. ADRs are stored in the `adr` directory and follow a [specific format](adr/000_what_is_adr.md). You should go
through all the ADRs before you start implementing any code, as they contain important information about the
architecture and coding practices of this project.

## Sample feature code

When implementing you own code, please use the below sample AggregateRoots as reference implementations:

- [Equipment](src/main/java/com/company/andy/feature/equipment/domain/Equipment.java): Represents equipment that needs
  to be managed under an org, such as a computer device etc. It's an org level object.
- [MaintenanceRecord](src/main/java/com/company/andy/feature/maintenance/domain/MaintenanceRecord.java): Represents a
  maintenance record created for an `Equipment`, it's also an org level object.
- [SystemSettings](src/main/java/com/company/andy/feature/systemsettings/domain/SystemSettings.java): Represents a
  platform level object that are not related to any org and should only be accessed by supervisor or platform level service client.
- [DemoReservation](src/main/java/com/company/andy/feature/demoreservation/domain/DemoReservation.java): Represents a demo reservation requested by any user including anonymous users.

The APIs for these sample AggregateRoots are only exposed in local and testing environments. You may keep them in your
real project as implementation references. If you choose to delete them, make sure you also update the ADRs that
reference them.

## What's not demonstrated in this template project?

- You will need to design the authorization/role architecture by yourself, as authorization is highly dependent on the
  business requirements.

