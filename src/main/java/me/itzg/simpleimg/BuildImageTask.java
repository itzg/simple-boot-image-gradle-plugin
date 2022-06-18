package me.itzg.simpleimg;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import javax.inject.Inject;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;

public abstract class BuildImageTask extends ImageHandlingTask {

    @InputDirectory
    abstract DirectoryProperty getBootImageDirectory();

    @InputFile
    abstract RegularFileProperty getDockerfile();

    @Input
    abstract Property<String> getBaseImage();

    @Input
    abstract Property<Integer> getExposePort();

    @Input
    abstract Property<Boolean> getUseBuildx();

    @Input
    abstract Property<Boolean> getPush();

    @Input
    abstract Property<Boolean> getPullForBuild();

    @Optional
    @Input
    abstract Property<String> getCacheFrom();

    @Optional
    @Input
    abstract Property<String> getCacheTo();

    @Optional
    @Input
    abstract ListProperty<String> getPlatforms();

    @Inject
    protected abstract ExecOperations getExecOperations();

    @Override
    void apply(SharedProperties sharedProperties) {
        getBaseImage().set(sharedProperties.getBaseImage());
        getExposePort().set(sharedProperties.getExposePort());
        getUseBuildx().set(sharedProperties.getUseBuildx());
        getPullForBuild().set(sharedProperties.getPullForBuild());
        getCacheFrom().set(sharedProperties.getCacheFrom());
        if (getUseBuildx().get()) {
            getCacheTo().set(sharedProperties.getCacheTo());
            getPlatforms().set(sharedProperties.getPlatforms());
        } else {
            throw new IllegalArgumentException("Can't set cacheTo without buildx enabled");
        }
        getPush().set(sharedProperties.getPush().get() && getUseBuildx().get());

        super.apply(sharedProperties);
    }

    @TaskAction
    void build() throws IOException {
        final var fullImageName = calculateFullImageName();

        getLogger().info("Building {} with base image {} tagged with {}",
            fullImageName, getBaseImage().get(), getTags().get());

        if (getLogger().isTraceEnabled()) {
            try (Stream<Path> pathStream = Files.walk(getBootImageDirectory().get().getAsFile().toPath())) {
                pathStream
                    .forEach(path -> getLogger().trace("Context: {}{}", path, Files.isDirectory(path) ? "/" : ""));
            }
        }

        getExecOperations()
            .exec(spec -> {
                spec.executable("docker");
                spec.args(createArgsList());

                getLogger().debug("Executing: docker {}", spec.getArgs());
            })
            .assertNormalExitValue();
    }

    private List<String> createArgsList() {
        final ArrayList<String> args = new ArrayList<>();
        if (usesBuildx()) {
            args.add("buildx");
        }
        args.add("build");

        if (!getLogger().isInfoEnabled()) {
            args.add("--quiet");
        }

        addBuildArg(args, "BASE_IMG", getBaseImage().get());
        addBuildArg(args, "EXPOSE_PORT", getExposePort().get());

        final var imageTags = expandImageTags();
        for (final String imageTag : imageTags) {
            args.add("--tag");
            args.add(imageTag);
        }

        args.add("--file");
        args.add(getDockerfile().get().getAsFile().getPath());

        addOptionalArg(args, "--cache-from", getCacheFrom());
        addOptionalArg(args, "--cache-to", getCacheTo());

        if (getPullForBuild().get()) {
            args.add("--pull");
        }

        if (usesBuildx()) {
            if (getPush().get()) {
                args.add("--push");
            }
            else {
                args.add("--load");
            }

            if (getPlatforms().isPresent() && !getPlatforms().get().isEmpty()) {
                args.add("--platform");
                args.add(String.join(",", getPlatforms().get()));
            }
        }

        args.add(getBootImageDirectory().get().getAsFile().getPath());

        return args;
    }

    private void addOptionalArg(ArrayList<String> args, String arg, Property<String> value) {
        if (value.isPresent()) {
            args.add(arg);
            args.add(value.get());
        }
    }

    private boolean usesBuildx() {
        return getUseBuildx().get() || getCacheTo().isPresent();
    }

    private void addBuildArg(ArrayList<String> args, String name, Object value) {
        args.add("--build-arg");
        args.add(name + "=" + value);
    }

}
