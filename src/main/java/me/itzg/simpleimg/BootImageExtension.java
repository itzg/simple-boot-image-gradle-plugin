package me.itzg.simpleimg;

import java.util.List;
import javax.inject.Inject;
import org.gradle.api.Project;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

public abstract class BootImageExtension {

    abstract Property<String> getBaseImage();

    abstract Property<Integer> getExposePort();

    abstract Property<String> getImageRepo();

    abstract Property<String> getImageName();

    abstract ListProperty<String> getTags();

    abstract Property<Boolean> getUseBuildx();

    abstract Property<Boolean> getPullForBuild();

    abstract Property<String> getCacheFrom();

    abstract Property<String> getCacheTo();

    abstract Property<Boolean> getPush();

    abstract ListProperty<String> getPlatforms();

    /**
     * Indicates if the built image should use Spring Boot's layertools and index or just
     * bundle and execute the jar as-is.
     */
    abstract Property<Boolean> getLayered();

    @Inject
    public BootImageExtension(Project project) {
        getBaseImage().convention("eclipse-temurin:17");
        getExposePort().convention(8080);
        getImageName().convention(project.getProviders().provider(project::getName));
        getTags().convention(project.getProviders().provider(() -> List.of("latest", project.getVersion().toString())));
        getUseBuildx().convention(true);
        getPullForBuild().convention( false);
        getPush().convention(false);
        getLayered().convention(true);
    }

}
