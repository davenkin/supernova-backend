package com.company.andy.archunit;

import com.company.andy.common.model.AggregateRoot;
import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.data.annotation.TypeAlias;

import static com.company.andy.archunit.util.ArchUnitUtils.*;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(packages = "com.company.andy.feature", importOptions = DoNotIncludeTests.class)
class AggregateRootArchTest {

    @ArchTest
    static final ArchRule aggregate_root_should_reside_in_domain_package = classes()
            .that()
            .areAssignableTo(AggregateRoot.class)
            .should()
            .resideInAnyPackage("com.company.andy.feature..domain")
            .because("AggregateRoot should located directly under domain package.");

    @ArchTest
    static final ArchRule concrete_aggregate_root_should_be_annotated_with_type_alias = classes()
            .that()
            .areAssignableTo(AggregateRoot.class)
            .and(areConcreteClasses())
            .should()
            .beAnnotatedWith(TypeAlias.class)
            .because("Concrete AggregateRoots should be annotated with @TypeAlias as otherwise the class FQCN will be used as type information and stored in database, which does not survive repackaging.");

    @ArchTest
    static final ArchRule concrete_aggregate_root_should_have_private_no_arg_constructor = classes()
            .that()
            .areAssignableTo(AggregateRoot.class)
            .and(areConcreteClasses())
            .should(havePrivateNoArgConstructor())
            .because("Private no-arg constructors of AggregateRoots are only used for deserialization, it should not be used for manual AggregateRoot creation because otherwise we might end up with invalid AggregateRoots. You may use @NoArgsConstructor(access = PRIVATE) for private constructors.");

    @ArchTest
    static final ArchRule aggregate_root_should_have_non_public_no_arg_constructor = classes()
            .that()
            .areAssignableTo(AggregateRoot.class)
            .should(haveNonPublicNoArgConstructor())
            .because("Non-public no-arg constructors of AggregateRoots are only used for deserialization, it should not be used for manual AggregateRoot creation because otherwise we might end up with invalid AggregateRoots. You may use @NoArgsConstructor(access = PRIVATE) or @NoArgsConstructor(access = PROTECTED) for constructors.");

    @ArchTest
    static final ArchRule aggregate_root_should_not_have_builder = classes()
            .that()
            .areAssignableTo(AggregateRoot.class)
            .should(notHaveBuilderMethod())
            .because("AggregateRoots should be created using explict constructors but not builders, otherwise we might end up with invalid objects.");

}
