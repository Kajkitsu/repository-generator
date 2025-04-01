package pl.edu.wat.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

public class RestRepositoryPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        // Create extension for configuration
        RestRepositoryExtension extension = project.getExtensions().create("restRepository", RestRepositoryExtension.class);

        // Create and configure the task
        project.getTasks().register("generateRepositories", RestRepositoryTask.class, task -> {
            task.setGroup("build");
            task.setDescription("Generates Spring Data JPA repositories for @RestEntity annotated classes");

            // Set default values
            task.getBasePackage().convention(extension.getBasePackage().convention("pl.edu.wat.demo"));
            task.getRepositoryPackage().convention(extension.getRepositoryPackage().convention(
                    task.getBasePackage().map(bp -> bp + ".repository")));

            // Set task dependencies and outputs
            task.dependsOn(project.getTasks().getByName("compileJava"));

            // Configure the task to run before the classes task
            project.getTasks().getByName("classes").dependsOn(task);

            // Configure source and output directories
            SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
            SourceSet main = sourceSets.getByName("main");

            task.getSourceDir().convention(project.getLayout().getProjectDirectory()
                    .dir("src/main/java"));

            task.getOutputDir().convention(project.getLayout().getBuildDirectory()
                    .dir("classes/java/main"));
        });
    }
}