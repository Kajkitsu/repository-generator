package pl.edu.wat.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.pool.TypePool;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;

@AllArgsConstructor
@Slf4j
public class ReflectionSetter {

    @Getter
    private final TypePool typePool;
    private final String repositoryPackage;
    private final ByteBuddy byteBuddy;
    private final String basePackage;
    private final String sourceRootPath;
    private final File outputDir;

    public static void main(String[] args) {
        new ReflectionSetter(
                TypePool.Default.ofSystemLoader(),
                "pl.edu.wat.demo.repository",
                "pl.edu.wat.demo",
                "src/main/java",
                new File("build/classes/java/main")
        ).generateRepositories();
    }

    @Builder
    public ReflectionSetter(
            TypePool typePool,
            String repositoryPackage,
            String basePackage,
            String sourceRootPath,
            File outputDir) {

        // Required parameters
        this.typePool = Optional.ofNullable(typePool).orElseThrow(() ->
                new IllegalArgumentException("TypePool cannot be null"));
        this.outputDir = Optional.ofNullable(outputDir).orElseThrow(() ->
                new IllegalArgumentException("OutputDir cannot be null"));

        // Parameters with defaults
        this.basePackage = Optional.ofNullable(basePackage).orElse("pl.edu.wat.demo");
        this.repositoryPackage = Optional.ofNullable(repositoryPackage).orElse(this.basePackage + ".repository");
        this.sourceRootPath = Optional.ofNullable(sourceRootPath).orElse("src/main/java");

        // Create ByteBuddy instance
        this.byteBuddy = new ByteBuddy();
    }

    public void generateRepositories() {
        RestEntityScanner scanner = new RestEntityScanner(typePool, basePackage, sourceRootPath);
        Set<TypeDescription> annotatedEntities = scanner.scanForAnnotatedEntities();

        log.info("Found {} entities annotated with @RestEntity", annotatedEntities.size());

        for (TypeDescription entityClass : annotatedEntities) {
            generateRepositoryForEntity(entityClass);
        }
    }

    private void generateRepositoryForEntity(TypeDescription entityClass) {
        String entityName = entityClass.getSimpleName();
        String repositoryName = entityName + "Repository";

        // Get JpaRepository type description
        TypeDescription repositoryTypeDescription = typePool.describe("org.springframework.data.jpa.repository.JpaRepository").resolve();
        TypeDescription idTypeDescription = typePool.describe("java.lang.Long").resolve();

        // Create repository interface with generic types
        DynamicType.Unloaded<?> unloaded = byteBuddy.makeInterface(
                        TypeDescription.Generic.Builder
                                .parameterizedType(
                                        repositoryTypeDescription,
                                        entityClass,
                                        idTypeDescription)
                                .build())
                .name(repositoryPackage + "." + repositoryName)
                .annotateType(AnnotationDescription.Builder.ofType(typePool.describe("org.springframework.data.rest.core.annotation.RepositoryRestResource").resolve()).build())
                .make();

        // Save the generated interface to the output directory
        try {
            unloaded.saveIn(outputDir);
            log.info("Generated repository interface: {}", repositoryName);
        } catch (IOException e) {
            log.error("Failed to save repository interface {}: {}", repositoryName, e.getMessage());
        }
    }
}