package unq.dapp.grupoj.soccergenius.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@AnalyzeClasses(packages = "unq.dapp.grupoj.soccergenius")
public class LayerDependenciesTest {

    @ArchTest
    static final ArchRule layer_dependencies_are_respected =
            layeredArchitecture().consideringAllDependencies()
                    .withOptionalLayers(true)
                    .layer("Controllers").definedBy("..controllers..")
                    .layer("Services").definedBy("..services..")
                    .layer("Persistence").definedBy("..repository..")
                    .layer("Model").definedBy("..model..")
                    .layer("Logger").definedBy("org.slf4j..")
                    .layer("Exception").definedBy("..exceptions..")
                    // 2. Definición de capas para dependencias externas permitidas
                    .layer("SpringBoot").definedBy(
                            "org.springframework.web.bind.annotation..",
                            "org.springframework.http..",
                            "org.springframework.beans.factory.annotation.."
                    )
                    .layer("Swagger").definedBy("io.swagger.v3.oas.annotations..")
                    .layer("Java").definedBy("java..")
                    .layer("Jakarta").definedBy("jakarta..")
                    // 3. Reglas de acceso entre capas
                    .whereLayer("Controllers").mayOnlyAccessLayers(
                            "Services", "Model", "SpringBoot", "Swagger", "Java", "Logger", "Exception", "Jakarta"
                    );
}
