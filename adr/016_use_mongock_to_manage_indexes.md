# Use Mongock to manage indexes

## Context

Database indexes are important for query performance, but they can be easily forgotten or misconfigured. We need a way
to manage our database indexes in a consistent and reliable way.

There are several ways to manage MongoDB indexes in a Spring Boot application:

- Use MongoDB's auto-index creation feature by annotating the domain classes with `@Indexed` or `@CompoundIndex` etc.
  This is the simplest way, but it can lead to issues when refactor the indexes.
- Use Spring Data MongoDB's `MongoTemplate` to create indexes explicitly.

## Decision

We choose to use `MongoTemplate` to create indexes explicitly. And we use [Mongock](https://www.mongock.io/) to manage
the index operation scripts as database migrations.

## Implementation

- Create a Mongock change log class under `src/main/java/com/company/andy/common/migration` folder. Class name should
  follow this format:
  `Migration[3 digits index]_[SimpleDescriptionOfYourMigration]`. For example `Migration002_BaseSetup.java`:

```java
@Slf4j
@ChangeUnit(id = "Migration002_BaseSetup", order = "002", author = "andy", transactional = false)
public class Migration002_BaseSetup {
}
```

- Disable transaction by setting `transactional = false` on `@ChangeUnit`, as MongoDB index creation cannot be done in a
  transaction. Be noted that for other types of database operations, you should set `transactional = true` to have them
  run in a transaction.

- Implement the index creation logic in a method annotated with `@Execution`. For example:

```java
    @Execution
    public void execute(MongoTemplate mongoTemplate) {
        ensurePublishingDomainEventIndexes(mongoTemplate);
    }
    
    private void ensurePublishingDomainEventIndexes(MongoTemplate mongoTemplate) {
        IndexOperations indexOperations = mongoTemplate.indexOps(PublishingDomainEvent.class);
        indexOperations.createIndex(new Index().on(PublishingDomainEvent.Fields.status, DESC).named("idx_status"));
    }
```

- Do not forget to give your indexes a name using `named()` method, such as `named("idx_status")`. This  will make it easier to manage the indexes in
  the future, especially when you need to drop or modify the indexes.

- Add a method for rollback with `@RollbackExecution` annotation, you may just leave it an empty body. For example:

```java
    @RollbackExecution
    public void rollback(MongoTemplate mongoTemplate) {
    }
```

- Do not modify exiting migration classes, instead add a new one.