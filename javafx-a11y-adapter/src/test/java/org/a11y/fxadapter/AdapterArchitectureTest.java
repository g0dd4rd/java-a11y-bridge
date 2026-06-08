package org.a11y.fxadapter;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

class AdapterArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("org.a11y.fxadapter");
    }

    @Test
    void adapterShouldNotAccessDbusDirectly() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("org.a11y.fxadapter..")
            .should().dependOnClassesThat().resideInAPackage("org.freedesktop.dbus..")
            .as("Adapter should not access dbus-java directly — only through the bridge");
        rule.check(classes);
    }

    @Test
    void adapterShouldOnlyUsePublicBridgeApi() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("org.a11y.fxadapter..")
            .should().dependOnClassesThat().resideInAPackage("org.a11y.bridge.atspi..")
            .as("Adapter should not depend on bridge AT-SPI interface definitions");
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
    void noClassesShouldUsePrintStackTrace() {
        ArchRule rule = noClasses()
            .should().callMethod(Throwable.class, "printStackTrace")
            .as("No class should call printStackTrace()");
        rule.check(classes);
    }

    @Test
    void fxAccessibleContextShouldExtendAccessibleContext() {
        ArchRule rule = classes()
            .that().haveSimpleName("FxAccessibleContext")
            .should().beAssignableTo(javax.accessibility.AccessibleContext.class)
            .as("FxAccessibleContext must extend javax.accessibility.AccessibleContext");
        rule.check(classes);
    }

    @Test
    void fxAccessibleShouldImplementAccessible() {
        ArchRule rule = classes()
            .that().haveSimpleName("FxAccessible")
            .should().implement(javax.accessibility.Accessible.class)
            .as("FxAccessible must implement javax.accessibility.Accessible");
        rule.check(classes);
    }

    @Test
    void adapterShouldBeSingleton() {
        ArchRule rule = classes()
            .that().haveSimpleName("FxA11yAdapter")
            .should().haveOnlyPrivateConstructors()
            .as("FxA11yAdapter should be a singleton with private constructor");
        rule.check(classes);
    }
}
