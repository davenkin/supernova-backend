package com.company.andy.archunit;

import com.company.andy.common.event.DomainEvent;
import com.company.andy.common.event.consume.AbstractEventHandler;
import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.data.annotation.TypeAlias;

import static com.company.andy.archunit.util.ArchUnitUtils.*;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(packages = "com.company.andy.feature", importOptions = DoNotIncludeTests.class)
class DomainEventArchTest {

    @ArchTest
    static final ArchRule domain_event_should_reside_in_domain_event_package = classes()
            .that()
            .areAssignableTo(DomainEvent.class)
            .should()
            .resideInAnyPackage("com.company.andy.feature..domain.event..")
            .because("DomainEvents are domain models and should have a specific package under domain.event package.");

    @ArchTest
    static final ArchRule concrete_domain_event_should_be_annotated_with_type_alias = classes()
            .that()
            .areAssignableTo(DomainEvent.class)
            .and(areConcreteClasses())
            .should()
            .beAnnotatedWith(TypeAlias.class)
            .because("Concrete DomainEvents should be annotated with @TypeAlias, otherwise the class FQCN will be used as type information and stored in database, which does not survive repackaging.");

    @ArchTest
    static final ArchRule concreate_domain_event_should_have_private_no_arg_constructor = classes()
            .that()
            .areAssignableTo(DomainEvent.class)
            .and(areConcreteClasses())
            .should(havePrivateNoArgConstructor())
            .because("No-arg constructors of DomainEvents are needed for deserialization. Also, it should be private as it prevents from manual call which might result in invalid events. You may use @NoArgsConstructor(access = PRIVATE) for private constructors.");

    @ArchTest
    static final ArchRule domain_event_should_have_non_public_no_arg_constructor = classes()
            .that()
            .areAssignableTo(DomainEvent.class)
            .should(haveNonPublicNoArgConstructor())
            .because("Non-public no-arg constructors of DomainEvents are only used for deserialization, it should not be used for manual DomainEvent creation because otherwise we might end up with invalid DomainEvents.You may use @NoArgsConstructor(access = PRIVATE) or @NoArgsConstructor(access = PROTECTED) for constructors.");

    @ArchTest
    static final ArchRule domain_event_handler_should_reside_in_event_handler_package = classes()
            .that()
            .areAssignableTo(AbstractEventHandler.class)
            .should()
            .resideInAnyPackage("com.company.andy.feature..eventhandler..")
            .because("We should gather event handlers together under eventhandler package.");

    @ArchTest
    static final ArchRule domain_event_should_not_have_builder = classes()
            .that()
            .areAssignableTo(DomainEvent.class)
            .should(notHaveBuilderMethod())
            .because("DomainEvents should be created using explict constructors but not builders, otherwise we might end up with invalid DomainEvents.");

    @ArchTest
    static final ArchRule domain_event_should_not_have_setters = classes()
            .that()
            .areAssignableTo(DomainEvent.class)
            .should(notHaveSetterMethods())
            .because("DomainEvents should be immutable, hence it should not have setter methods");

    @ArchTest
    static final ArchRule concrete_domain_event_should_be_registered_in_domain_event_json_subtypes = classes()
            .that()
            .areAssignableTo(DomainEvent.class)
            .and(areConcreteClasses())
            .should(beRegisteredInDomainEventJsonSubTypes())
            .because("All concrete DomainEvent subclasses must be registered under DomainEvent's @JsonSubTypes");
}
