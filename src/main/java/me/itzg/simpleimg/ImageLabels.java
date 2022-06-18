package me.itzg.simpleimg;

import java.util.concurrent.Callable;
import javax.inject.Inject;
import org.gradle.api.Project;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;

abstract public class ImageLabels {
    abstract Property<String> getDescription();

    abstract Property<String> getTitle();

    abstract Property<String> getVersion();

    abstract Property<String> getRevision();

    abstract Property<String> getSourceUrl();

    abstract MapProperty<String, String> getExtra();

    @Inject
    public ImageLabels(Project project) {
        getDescription().convention(project.getProviders().provider(project::getDescription));
        getTitle().convention(project.getProviders().provider(project::getName));
        getVersion().convention(project.getProviders().provider(() -> project.getVersion().toString()));
        getRevision().convention(project.getProviders().provider(
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

}
