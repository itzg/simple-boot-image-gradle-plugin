package me.itzg.simpleimg;

import org.gradle.api.DefaultTask;
import org.gradle.api.NonNullApi;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@CacheableTask
@NonNullApi
public abstract class GenerateLayeredDockerfileTask extends DefaultTask {

    @Input
    abstract Property<Boolean> getUseBuildx();

    @Input
    abstract Property<String> getLauncherClass();

    @OutputFile
    abstract RegularFileProperty getDockerfile();

    void apply(SharedProperties extension) {
        getUseBuildx().set(extension.getUseBuildx());
    }

    @TaskAction
    public void generate() throws IOException {
        Files.write(getDockerfile().get().getAsFile().toPath(),
            List.of(
                // ARG for base image needs a placeholder
                "ARG BASE_IMG=eclipse-temurin:17",
                "FROM ${BASE_IMG}",
                "ARG EXPOSE_PORT",
                "EXPOSE ${EXPOSE_PORT}",
                "WORKDIR /application",
                "COPY layers/dependencies/ ./",
                "COPY layers/spring-boot-loader/ ./",
                "COPY layers/snapshot-dependencies/ ./",
                getUseBuildx().get() ? "" :
                    // Workaround of https://github.com/moby/moby/issues/37965
                    "RUN true",
                "COPY layers/application/ ./",
                String.format("ENTRYPOINT [\"java\", \"%s\"]", getLauncherClass())
            )
        );
    }
}
