package pl.edu.wat.demo.entity;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.pool.TypePool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
@Slf4j
public class RestEntityScanner {

    private final TypePool typePool;
    private final String basePackage;
    private final String sourceRootPath;

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
                        log.debug("Found @RestEntity annotated class: {}", className);
                        annotatedEntities.add(typeDesc);
                    }
                } catch (Exception e) {
                    log.warn("Error resolving class {}: {}", className, e.getMessage());
                }
            }
        } catch (IOException e) {
            log.error("Error scanning for annotated entities: {}", e.getMessage());
        }

        return annotatedEntities;
    }
}