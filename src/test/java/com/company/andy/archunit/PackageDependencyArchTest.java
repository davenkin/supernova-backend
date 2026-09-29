package com.company.andy.archunit;

import com.fasterxml.jackson.annotation.*;
import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.data.mongodb.core.index.*;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.company.andy", importOptions = DoNotIncludeTests.class)
class PackageDependencyArchTest {

    @ArchTest
    static final ArchRule business_classes_should_all_under_specific_packages = classes()
            .that()
            .resideInAnyPackage("..com.company.andy.feature..")
            .should()
            .resideInAnyPackage(
                    "..com.company.andy.feature..command..",
                    "..com.company.andy.feature..controller..",
                    "..com.company.andy.feature..domain..",
                    "..com.company.andy.feature..eventhandler..",
                    "..com.company.andy.feature..infrastructure..",
                    "..com.company.andy.feature..scheduledjob..",
                    "..com.company.andy.feature..query.."
            )
            .because(
                    "We use the following packages to house all business classes: command, controller, domain, eventhandler, infrastructure, scheduledjob, query.");

    @ArchTest
    static final ArchRule domain_classes_should_not_depend_on_outer_packages = noClasses()
            .that()
            .resideInAnyPackage("..com.company.andy.feature..domain..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                    "..com.company.andy.feature..command..",
                    "..com.company.andy.feature..eventhandler..",
                    "..com.company.andy.feature..infrastructure..",
                    "..com.company.andy.feature..scheduledjob..",
                    "..com.company.andy.feature..query..")
            .because(
                    "Domain package is most important part of the application and reside in the kernel of the architecture, it should only contain business logic and should not depend on other outer packages.");

    @ArchTest
    static final ArchRule map_struct_should_not_be_used = noClasses()
            .should()
            .dependOnClassesThat().resideInAPackage("org.mapstruct..")
            .because(
                    "We don't use MapStruct as we think manual mapper implementations are more straightforward and easier to debug, and also manual mappers do not take much effort.");

    @ArchTest
    static final ArchRule spring_data_repositories_should_not_be_used = noClasses()
            .should()
            .dependOnClassesThat().belongToAnyOf(MongoRepository.class, ListCrudRepository.class, ListPagingAndSortingRepository.class)
            .because(
                    "We don't use Spring Data's repository interfaces directly as it's too rigid for method names, instead we use AbstractMongoRepository as the base repository class and define our own repository interfaces for better flexibility and readability.");

    @ArchTest
    static final ArchRule usage_of_jackson_annotations_should_be_minimized = noClasses()
            .that()
            .resideInAnyPackage(
                    "..com.company.andy.feature..command..",
                    "..com.company.andy.feature..controller..",
                    "..com.company.andy.feature..domain..",
                    "..com.company.andy.feature..eventhandler..",
                    "..com.company.andy.feature..scheduledjob..",
                    "..com.company.andy.feature..query.."
            )
            .should()
            .dependOnClassesThat()
            .belongToAnyOf(
                    JsonProperty.class,
                    JsonAlias.class,
                    JsonIgnore.class,
                    JsonIgnoreProperties.class,
                    JsonAnyGetter.class,
                    JsonAnySetter.class,
                    JsonSerialize.class,
                    JsonDeserialize.class,
                    JsonFormat.class,
                    JsonView.class
            )
            .because("We should not rely directly on Jackson annotations as it's bad for code navigability.");

    @ArchTest
    static final ArchRule usage_of_json_node_objects_should_be_minimized = noClasses()
            .that()
            .resideInAnyPackage(
                    "..com.company.andy.feature..command..",
                    "..com.company.andy.feature..controller..",
                    "..com.company.andy.feature..domain..",
                    "..com.company.andy.feature..eventhandler..",
                    "..com.company.andy.feature..scheduledjob..",
                    "..com.company.andy.feature..query.."
            )
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("tools.jackson.databind.node")
            .because("We should not rely directly on Jackson node objects as it's an infrastructure concern and it's bad for code navigability.");

    @ArchTest
    static final ArchRule mongodb_indexes_should_be_created_explicitly = noClasses()
            .should()
            .dependOnClassesThat()
            .belongToAnyOf(
                    Indexed.class,
                    CompoundIndex.class,
                    CompoundIndexes.class,
                    WildcardIndexed.class,
                    TextIndexed.class,
                    HashIndexed.class
            )
            .because("No index annotations are allowed as we want to centrally manage database indexes explicitly using Mongock.");

    @ArchTest
    static final ArchRule feature_classes_should_not_rely_on_spring_security = noClasses()
            .that()
            .resideInAnyPackage(
                    "..com.company.andy.feature..command..",
                    "..com.company.andy.feature..domain..",
                    "..com.company.andy.feature..eventhandler..",
                    "..com.company.andy.feature..scheduledjob..",
                    "..com.company.andy.feature..query.."
            ).should()
            .dependOnClassesThat()
            .resideInAnyPackage("org.springframework.security..")
            .because(
                    "Feature packages should not rely on spring security class, as we already uses Actor to represent the authenticated principal. You may reference spring security in the controller and infrastructure packages.");

    @ArchTest
    static final ArchRule should_not_use_rest_template = noClasses()
            .that()
            .resideInAnyPackage("..com.company.andy..")
            .should().dependOnClassesThat()
            .haveFullyQualifiedName("org.springframework.web.client.RestTemplate")
            .because("Don't use RestTemplate as it's deprecated, use RestClient instead.");
}
