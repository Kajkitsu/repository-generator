package pl.edu.wat.plugin;

import lombok.AllArgsConstructor;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.pool.TypePool;
import org.gradle.api.logging.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
public class RestEntityScanner {

    private final TypePool typePool;
    private final String basePackage;
    private final String sourceRootPath;
    private final Logger logger;

    public Set<TypeDescription> scanForAnnotatedEntities() {
        Set<TypeDescription> annotatedEntities = new HashSet<>();
        String packagePath = basePackage.replace('.', '/');
        Path basePath = Paths.get(sourceRootPath, packagePath);

        try (Stream<Path> pathStream = Files.walk(basePath)) {
            Set<String> javaClasses = pathStream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .map(path -> {
                        // Convert file path to class name
                        String relativePath = basePath.relativize(path).toString();
                        String className = relativePath.replace(".java", "").replace('/', '.');
                        return basePackage + (className.isEmpty() ? "" : "." + className);
                    })
                    .collect(Collectors.toSet());

            // Check each class for @RestEntity annotation
            for (String className : javaClasses) {
                try {
                    TypeDescription typeDesc = typePool.describe(className).resolve();
                    if (typeDesc.getDeclaredAnnotations().isAnnotationPresent(typePool.describe(RestEntity.class.getName()).resolve())) {
                        logger.debug("Found @RestEntity annotated class: {}", className);
                        annotatedEntities.add(typeDesc);
                    }
                } catch (Exception e) {
                    logger.warn("Error resolving class {}: {}", className, e.getMessage());
                }
            }
        } catch (IOException e) {
            logger.error("Error scanning for annotated entities: {}", e.getMessage());
        }

        return annotatedEntities;
    }
}