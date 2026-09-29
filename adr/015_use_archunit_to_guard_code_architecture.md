# Use Archunit to guard code architecture

## Context

Good code structure and architecture is good for maintainability and scalability. However, as the project grows, it's easy to have code that violates the intended architecture, such as code in the wrong layer or package. We need a way to guard our code architecture.

## Decision

We choose to use [ArchUnit](https://www.archunit.org/) to guard our code architecture. ArchUnit is a Java library for checking the architecture of Java code. It allows us to write tests that check if our code follows the intended architecture.

## Implementation

Some common architectural rules are implemented:
- Domain layer should be under `domain` package
- Domain layer should not depend on outer layers such as `controller`, `command` and `infrastructure`
- DomainEvent should be created in a controlled way
- Annotations should not be used arbitrarily
- MapStruct should not be used as it's not flexible enough and may cause maintenance issues in the long run
- Usages on Jackson annotations should be minimized 

Example Archunit test: [PackageDependencyArchTest](../src/test/java/com/company/andy/archunit/PackageDependencyArchTest.java).
