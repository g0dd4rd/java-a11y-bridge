package org.a11y.bridge;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

class ArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("org.a11y.bridge");
    }

    @Test
    void atspiPackageShouldNotDependOnBridgeInternals() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..atspi..")
            .should().dependOnClassesThat().resideInAPackage("..bridge")
            .as("AT-SPI2 interface definitions should not depend on bridge internals");
        rule.check(classes);
    }

    @Test
    void bridgeClassesShouldNotUseJavafx() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("org.a11y.bridge..")
            .should().dependOnClassesThat().resideInAPackage("javafx..")
            .as("Bridge should not depend on JavaFX — that's the adapter's job");
        rule.check(classes);
    }

    @Test
    void onlyBridgeShouldAccessDbusJava() {
        ArchRule rule = noClasses()
            .that().resideOutsideOfPackages("org.a11y.bridge..", "org.a11y.bridge.atspi..")
            .should().dependOnClassesThat().resideInAPackage("org.freedesktop.dbus..")
            .allowEmptyShould(true)
            .as("Only bridge packages should access dbus-java");
        rule.check(classes);
    }

    @Test
    void noClassesShouldUseSystemExit() {
        ArchRule rule = noClasses()
            .should().callMethod(System.class, "exit", int.class)
            .as("No class should call System.exit()");
        rule.check(classes);
    }

    @Test
    void noClassesShouldUseStackTraceInProduction() {
        ArchRule rule = noClasses()
            .should().callMethod(Throwable.class, "printStackTrace")
            .as("No class should call printStackTrace() — use proper logging");
        rule.check(classes);
    }

    @Test
    void noCyclicDependenciesBetweenPackages() {
        ArchRule rule = slices()
            .matching("org.a11y.bridge.(*)..")
            .should().beFreeOfCycles()
            .as("No cyclic dependencies between packages");
        rule.check(classes);
    }

    @Test
    void mappingClassesShouldBeStateless() {
        ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("Mapping")
            .should().haveOnlyFinalFields()
            .as("Mapping classes should be stateless (all fields final)");
        rule.check(classes);
    }

    @Test
    void providerShouldOnlyDependOnBridge() {
        ArchRule rule = classes()
            .that().haveSimpleName("A11yProvider")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage("org.a11y.bridge..", "javax.accessibility..", "java..")
            .as("A11yProvider should only depend on bridge and javax.accessibility");
        rule.check(classes);
    }

    @Test
    void interfaceDefinitionsShouldBeInterfaces() {
        ArchRule rule = classes()
            .that().resideInAPackage("..atspi..")
            .and().haveSimpleNameEndingWith("Iface")
            .should().beInterfaces()
            .as("All *Iface classes in atspi package should be interfaces");
        rule.check(classes);
    }
}
