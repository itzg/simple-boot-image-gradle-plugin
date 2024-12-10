package me.itzg.simpleimg;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;

public abstract class ExtractBootLayersTask extends DefaultTask {

    @InputFile
    abstract RegularFileProperty getBootJar();

    @OutputDirectory
    abstract DirectoryProperty getLayersDirectory();

    @Inject
    protected abstract ExecOperations getExecOperations();

    @TaskAction
    void extract() {
        // Cleanup from previous run, if needed
        getProject().delete(getLayersDirectory());
        getProject().mkdir(getLayersDirectory());

        getExecOperations()
            .javaexec(spec -> {
                spec.classpath(getBootJar());
                spec.jvmArgs("-Djarmode=tools");
                spec.args("extract", "--layers", "--launcher", "--destination", ".");
                spec.workingDir(getLayersDirectory());
            })
            .assertNormalExitValue();
    }

}
