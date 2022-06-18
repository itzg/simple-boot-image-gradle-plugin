package me.itzg.simpleimg;

import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import org.gradle.api.Project;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

public abstract class SharedProperties {

    abstract Property<String> getBaseImage();

    abstract Property<Integer> getExposePort();

    abstract Property<String> getFullyQualifiedImageName();

    abstract Property<String> getImageRepo();

    abstract Property<String> getImageName();

    abstract ListProperty<String> getTags();

    abstract Property<Boolean> getUseBuildx();

    abstract Property<Boolean> getPullForBuild();

    abstract Property<String> getCacheFrom();

    abstract Property<String> getCacheTo();

    abstract Property<Boolean> getPush();

    abstract ListProperty<String> getPlatforms();

    abstract Property<Boolean> getLayered();

    abstract Property<String> getImageDescription();

    abstract Property<String> getImageTitle();

    abstract Property<String> getImageVersion();

    abstract Property<String> getImageRevision();

    abstract Property<String> getImageSourceUrl();

    abstract ListProperty<String> getExtraImageLabels();

    @Inject
    public SharedProperties(Project project, BootImageExtension extension) {
        getBaseImage().value(
            fromGradleProperty(project, "imageBase")
                .orElse(extension.getBaseImage())
        );
        getExposePort().value(extension.getExposePort());
        getFullyQualifiedImageName().value(
            // as provided by skaffold
            fromEnvironmentVariable(project, "IMAGE")
        );
        getImageRepo().value(
            fromGradleProperty(project, "imageRepo")
                .orElse(extension.getImageRepo())
        );
        getImageName().value(
            fromGradleProperty(project, "imageName")
                .orElse(extension.getImageName())
        );
        getTags().value(
            fromListGradleProperty(project, "imageTags")
                .orElse(extension.getTags())
        );
        getUseBuildx().value(
            fromBooleanGradleProperty(project, "imageUseBuildx")
                .orElse(extension.getUseBuildx())
        );
        getPullForBuild().value(
            fromBooleanGradleProperty(project, "imagePull")
                .orElse(extension.getPullForBuild())
        );
        getCacheFrom().value(
            fromGradleProperty(project, "imageCacheFrom")
                .orElse(extension.getCacheFrom())
        );
        getCacheTo().value(
            fromGradleProperty(project, "imageCacheTo")
                .orElse(extension.getCacheTo())
        );
        getPush().value(
            fromGradleProperty(project, "imagePush")
                // skaffold
                .orElse(fromEnvironmentVariable(project, "PUSH_IMAGE"))
                .map(Boolean::parseBoolean)
                .orElse(extension.getPush())
        );
        getPlatforms().value(
            fromListGradleProperty(project, "imagePlatforms")
                .orElse(extension.getPlatforms())
        );
        getLayered().value(
            fromBooleanGradleProperty(project, "imageLayered")
                .orElse(extension.getLayered())
        );
        getImageDescription().value(extension.getImageDescription());
        getImageTitle().value(extension.getImageTitle());
        getImageVersion().value(
            fromGradleProperty(project, "imageVersion")
                .orElse(extension.getImageVersion()
            )
        );
        getImageRevision().value(
            fromGradleProperty(project, "imageRevision")
                .orElse(extension.getImageRevision())
        );
        getImageSourceUrl().value(
            // from GitHUb Ations
            fromEnvironmentVariable(project, "GITHUB_REPOSITORY")
                .map(repo -> "https://github.com/" + repo)
                .orElse(extension.getImageSourceUrl())
        );
        getExtraImageLabels().value(
            fromListGradleProperty(project, "imageExtraLabels")
                .orElse(extension.getExtraImageLabels())
        );
    }

    private Provider<List<String>> fromListGradleProperty(Project project, String name) {
        return fromGradleProperty(project, name)
            .map(s -> Arrays.stream(s.split(",")).toList());
    }

    private Provider<Boolean> fromBooleanGradleProperty(Project project, String name) {
        return project.getProviders().gradleProperty(name)
            .map(Boolean::parseBoolean);
    }

    private Provider<String> fromEnvironmentVariable(Project project, String name) {
        return project.getProviders().environmentVariable(name);
    }

    private Provider<String> fromGradleProperty(Project project, String name) {
        return project.getProviders().gradleProperty(name);
    }

}
