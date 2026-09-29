# Exception handling

## Context

There are 2 ways to model exceptions:

- Hierarchical exceptions: create multiple exception classes which form a hierarchy
- Flat exceptions: use some fixed number of exception classes for all and no class hierarchy is introduced

## Decision

We use "Flat exceptions" model and use a
single [ServiceException](../src/main/java/com/company/andy/common/exception/ServiceException.java) class for all.

## Implementation

Whenever you need to throw an exception, throw this `ServiceException` with the following parameters:

- an [ErrorCode](../src/main/java/com/company/andy/common/exception/ErrorCode.java): the type of the exception, you may
  add more types according to your own needs. The `ErrorCode` is very useful for consumers to decide their actions.
- an error message
- Context data: key-value pairs that hold the context data of the exception, usually used by consumers to create their
  own messages

```java
someMethod() {
  throw new ServiceException(EQUIPMENT_NAME_ALREADY_EXISTS,
      "Equipment Name Already Exists.",
      NullableMapUtils.mapOf(AggregateRoot.Fields.id, equipment.getId(), Equipment.Fields.name, newName));
}
```

## API error responses

A unified error response [QApiErrorResponse](../src/main/java/com/company/andy/common/exception/QApiErrorResponse.java)
is built from
`ServiceException`:

```json
{
  "error": {
    "code": "BAD_REQUEST",
    "message": "string",
    "status": 0,
    "path": "string",
    "timestamp": "2025-08-18T13:00:03.144Z",
    "traceId": "string",
    "data": {
      "additionalProp1": "string",
      "additionalProp2": 0
    }
  }
}
```

There are 2 places of configuration that enables unified API errors:

- [GlobalExceptionHandler](../src/main/java/com/company/andy/common/exception/GlobalExceptionHandler.java): handles
  exceptions raised from Spring MVC
- [JsonAuthenticationEntryPoint](../src/main/java/com/company/andy/common/security/JsonAuthenticationEntryPoint.java):
  handles authentication exceptions raised from Spring Security
- [JsonAccessDeniedHandler](../src/main/java/com/company/andy/common/security/JsonAccessDeniedHandler.java): handles
  authorization exceptions raised from Spring Security
- [RestErrorController](../src/main/java/com/company/andy/common/exception/RestErrorController.java): serves as a
  fallback for handling exceptions for the whole application

![exception handling](./image/unified-error-response.drawio.svg)




