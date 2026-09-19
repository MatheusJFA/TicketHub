package com.tickethub;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

// ponytail: plain @Test + ClassFileImporter instead of @ArchTest fields — the
// archunit-junit5 engine silently discovered 0 tests under this surefire/JUnit
// platform combo. Revisit @ArchTest if the engine gets fixed upstream.
class ArchitectureTest {

    private static final JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages("com.tickethub");

    @Test
    void domainDoesNotDependOnOuterLayers() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..application..", "..infrastructure..")
                .check(CLASSES);
    }

    @Test
    void domainAndApplicationAreFrameworkFree() {
        // commons-lang3/collections4 are pure utility libraries (no framework,
        // no I/O), allowed alongside the JDK for null/string/collection guards.
        classes()
                .that().resideInAnyPackage("..domain..", "..application..")
                .should().onlyDependOnClassesThat()
                .resideInAnyPackage("java..", "org.apache.commons..",
                        "com.tickethub.domain..", "com.tickethub.application..")
                .check(CLASSES);
    }

    @Test
    void productionClassesBelongToALayer() {
        classes()
                .should().resideInAnyPackage(
                        "com.tickethub.domain..",
                        "com.tickethub.application..",
                        "com.tickethub.infrastructure..")
                .check(CLASSES);
    }

    @Test
    void applicationDoesNotDependOnInfrastructure() {
        noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat()
                .resideInAPackage("..infrastructure..")
                .check(CLASSES);
    }

    @Test
    void layersAreRespected() {
        layeredArchitecture()
                .consideringOnlyDependenciesInLayers()
                .layer("Domain").definedBy("..domain..")
                .layer("Application").definedBy("..application..")
                .layer("Infrastructure").definedBy("..infrastructure..")
                .whereLayer("Infrastructure").mayNotBeAccessedByAnyLayer()
                .whereLayer("Application").mayOnlyBeAccessedByLayers("Infrastructure")
                .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure")
                .check(CLASSES);
    }
}
