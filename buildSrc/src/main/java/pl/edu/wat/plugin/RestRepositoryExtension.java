package pl.edu.wat.plugin;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;

public abstract class RestRepositoryExtension {
    @Input
    public abstract Property<String> getBasePackage();

    @Input
    public abstract Property<String> getRepositoryPackage();
}