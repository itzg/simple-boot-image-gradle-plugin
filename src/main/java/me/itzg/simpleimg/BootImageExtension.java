package me.itzg.simpleimg;

import java.util.List;
import java.util.concurrent.Callable;
import javax.inject.Inject;
import org.gradle.api.Project;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

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

    abstract Property<String> getImageDescription();

    abstract Property<String> getImageTitle();

    abstract Property<String> getImageVersion();

    abstract Property<String> getImageRevision();

    abstract Property<String> getImageSourceUrl();

    abstract ListProperty<String> getExtraImageLabels();

    /**
     * Indicates if the built image should use Spring Boot's layertools and index or just
     * bundle and execute the jar as-is.
     */
    abstract Property<Boolean> getLayered();

    @Inject
    public BootImageExtension(Project project) {
        getBaseImage().convention("eclipse-temurin:17");
        getExposePort().convention(8080);
        getImageName().convention(provider(project, project::getName));
        getTags().convention(project.getProviders().provider(() -> List.of("latest", project.getVersion().toString())));
        getUseBuildx().convention(true);
        getPullForBuild().convention( false);
        getPush().convention(false);
        getLayered().convention(true);
        getImageDescription().convention(provider(project, project::getDescription));
        getImageTitle().convention(provider(project, project::getName));
        getImageVersion().convention(provider(project, () -> project.getVersion().toString()));
        getImageRevision().convention(provider(project,
            // such as https://github.com/qoomon/gradle-git-versioning-plugin
            lateAddedProperty(project, "git.commit")
        ));
    }

    private static Callable<String> lateAddedProperty(Project project, String name) {
        return () -> {
            final Object value = project.findProperty(name);
            return value != null ? value.toString() : null;
        };
    }

    private static <T> Provider<T> provider(Project project, Callable<T> callable) {
        return project.getProviders().provider(callable);
    }

}
