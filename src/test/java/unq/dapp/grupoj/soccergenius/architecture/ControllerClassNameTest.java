package unq.dapp.grupoj.soccergenius.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@AnalyzeClasses(packages = "unq.dapp.grupoj.soccergenius")
public class ControllerClassNameTest {

    @ArchTest
    static final ArchRule controllers_should_end_with_controller =
            classes().that().resideInAPackage("..controllers..")
                    .should().haveSimpleNameEndingWith("Controller")
                    .as("All classes in the 'controllers' package should end with 'Controller'.");

    @ArchTest
    static final ArchRule controllers_should_be_annotated_with_rest_controller =
            classes().that().resideInAPackage("..controllers..")
                    .should().beAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
                    .as("All classes in the 'controllers' package should be annotated with @RestController.");

}
