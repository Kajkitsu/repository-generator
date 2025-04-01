package pl.edu.wat.plugin;

import net.bytebuddy.pool.TypePool;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

public abstract class RestRepositoryTask extends DefaultTask {
    @Input
    public abstract Property<String> getBasePackage();

    @Input
    public abstract Property<String> getRepositoryPackage();

    @InputDirectory
    public abstract DirectoryProperty getSourceDir();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDir();

    @TaskAction
    public void generateRepositories() {
        getLogger().lifecycle("Generating repositories for entities annotated with @RestEntity");

        File sourceDir = getSourceDir().get().getAsFile();
        File outputDir = getOutputDir().get().getAsFile();
        String basePackage = getBasePackage().get();
        String repositoryPackage = getRepositoryPackage().get();

        // Create TypePool
        TypePool typePool = TypePool.Default.of(getClass().getClassLoader());

        // Create ReflectionSetter
        ReflectionSetter reflectionSetter = ReflectionSetter.builder()
                .typePool(typePool)
                .basePackage(basePackage)
                .repositoryPackage(repositoryPackage)
                .sourceRootPath(sourceDir.getAbsolutePath())
                .logger(getLogger())
                .outputDir(outputDir)
                .build();

        // Generate repositories
        reflectionSetter.generateRepositories();
    }
}