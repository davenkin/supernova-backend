# API documentation

## Context

API documentation is important for API consumers such as frontend applications or external integration parties.

## Decision

We use [springdoc](https://springdoc.org/) for API documentation, which automatically generates API documentation from
the source code.

## Implementation

There are various annotations from springdoc to guide the generation of API docs, here lists the four most common ones which
suffices your majority needs:

- `@Tag`: Used on controller's class level to describe the controller

```java
@Tag(name = "EquipmentController", description = "Equipment management APIs")
public class EquipmentController {
    private final EquipmentCommandService equipmentCommandService;
    private final EquipmentQueryService equipmentQueryService;
}
```

- `@Operation`: Used on the controller's method level to describe a single method

```java
    @Operation(summary = "Update an equipment's name")
    @PutMapping("/{id}/name")
    public void updateEquipmentName(
            @PathVariable("id") @NotBlank
            @Parameter(description = "Id of the equipment")
            String equipmentId,
            @RequestBody @Valid UpdateEquipmentNameCommand command,
            @AuthenticationPrincipal @NotNull OrgActor actor) {
        this.equipmentCommandService.updateEquipmentName(equipmentId, command, actor);
    }
```

- `@Schema`: Used on request/response's class level and field level to describe the class and field respectively, if the
  class name and field name is already self-descriptive, then no need to use `@Schema`

```java
@Builder
public record UpdateEquipmentNameCommand(
        @Schema(description = "Name of the equipment")
        @NotBlank @Size(max = 100) String name) {
}
```

- `@Parameter`: Used on controller method's parameter to describe the parameter, if the parameter name is already
  self-descriptive, then no need to use `@Parameter`

```java
    @Operation(summary = "Update an equipment's name")
    @PutMapping("/{id}/name")
    public void updateEquipmentName(
            @PathVariable("id") @NotBlank
            @Parameter(description = "Id of the equipment")
            String equipmentId,
            @RequestBody @Valid UpdateEquipmentNameCommand command,
            @AuthenticationPrincipal @NotNull OrgActor actor) {
        this.equipmentCommandService.updateEquipmentName(equipmentId, command, actor);
    }
```

Configurations for Springdoc can be found
in [SpringdocConfiguration](../src/main/java/com/company/andy/common/configuration/SpringdocConfiguration.java).


