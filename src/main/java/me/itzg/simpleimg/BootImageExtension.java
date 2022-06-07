package me.itzg.simpleimg;

import java.util.List;
import org.gradle.api.Project;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

public abstract class BootImageExtension {

    void apply(Project project) {
        getBaseImage().convention(fromStringProperty(project, "imageBase", "eclipse-temurin:17"));
        getImageName().convention(fromStringProperty(project, "imageName", project.getName()));
        getTags().convention(List.of("latest", project.getVersion().toString()));
        getPullForBuild().convention(fromBooleanProperty(project, "imagePull", false));
        getPush().convention(resolvePushConvention(project));
        getLayered().convention(fromBooleanProperty(project, "imageLayered", true));
        getFullyQualifiedImageName().convention(/* from skaffold*/System.getenv("IMAGE"));
        getImageRepo().convention(fromStringProperty(project, "imageRepo", null));
        getCacheFrom().convention(fromStringProperty(project, "imageCacheFrom", null));
        getCacheTo().convention(fromStringProperty(project, "imageCacheTo", null));
        getUseBuildx().convention(true);
        getExposePort().convention(8080);
    }

    private boolean resolvePushConvention(Project project) {
        final Object imagePushProp = project.findProperty("imagePush");
        if (imagePushProp != null) {
            return Boolean.parseBoolean((String) imagePushProp);
        }
        // for skaffold support
        final String envValue = System.getenv("PUSH_IMAGE");
        if (envValue != null) {
            return Boolean.parseBoolean(envValue);
        }
        return false;
    }

    private boolean fromBooleanProperty(Project project, String property, boolean defaultValue) {
        final Object value = project.findProperty(property);
        return value != null ? Boolean.parseBoolean(((String) value)) : defaultValue;
    }

    private String fromStringProperty(Project project, String property, String defaultValue) {
        final Object value = project.findProperty(property);
        return value != null ? ((String) value) : defaultValue;
    }

    abstract Property<String> getBaseImage();

    abstract Property<Integer> getExposePort();

    /**
     * Intended for build systems that supply the full repo/name:tag identifier
     */
    abstract Property<String> getFullyQualifiedImageName();

    abstract Property<String> getImageRepo();

    abstract Property<String> getImageName();

    abstract ListProperty<String> getTags();

    abstract Property<Boolean> getUseBuildx();

    abstract Property<Boolean> getPullForBuild();

    abstract Property<String> getCacheFrom();

    abstract Property<String> getCacheTo();

    abstract Property<Boolean> getPush();

    /**
     * Indicates if the built image should use Spring Boot's layertools and index or just
     * bundle and execute the jar as-is.
     */
    abstract Property<Boolean> getLayered();
}
