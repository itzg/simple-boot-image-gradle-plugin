package me.itzg.simpleimg;


import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.api.tasks.bundling.Jar;
import org.springframework.boot.gradle.plugin.SpringBootPlugin;
import org.springframework.boot.gradle.tasks.bundling.BootJar;

@SuppressWarnings("unused")
public class SimpleBootImagePlugin implements Plugin<Project> {

    public static final String EXTENSION_NAME = "simpleBootImage";
    public static final String BUILD_TASK_NAME = "buildSimpleBootImage";
    public static final String PUSH_TASK_NAME = "pushSimpleBootImage";

    protected static final String BOOT_IMAGE_PATH = "simpleBootImage";
    protected static final String LAYERS_SUBPATH = BOOT_IMAGE_PATH + "/layers";
    protected static final String DOCKERFILE_SUBPATH = BOOT_IMAGE_PATH + "/Dockerfile";
    protected static final String FAT_JAR_SUBPATH = BOOT_IMAGE_PATH + "/application.jar";
    protected static final String GROUP = "simple boot image";

    @Override
    public void apply(Project project) {
        project.getPluginManager().withPlugin("org.springframework.boot", bootPlugin -> {
            final BootImageExtension extension = registerExtension(project);

            final SharedProperties sharedProperties = project.getObjects().newInstance(SharedProperties.class, project, extension);

            registerTasks(project, sharedProperties);

            configureBootJarTask(project, sharedProperties);
        });
    }

    private void configureBootJarTask(Project project, SharedProperties sharedProperties) {
        project.getTasks().named(SpringBootPlugin.BOOT_JAR_TASK_NAME, BootJar.class, task ->
            task.getLayered().getEnabled().set(sharedProperties.getLayered().get())
        );
    }

    private void registerTasks(Project project, SharedProperties sharedProperties) {
        final var extractBootLayersTask =
            project.getTasks().register("extractBootLayers", ExtractBootLayersTask.class,
                task -> {
                    task.setGroup(GROUP);
                    task.onlyIf(spec -> sharedProperties.getLayered().get());

                    task.getLayersDirectory().convention(project.getLayout().getBuildDirectory().dir(LAYERS_SUBPATH));
                    task.getBootJar().set(bootJarProvider(project));
                });

        final var stageJarTask = project.getTasks().register("stageBootJarForImage", StageJarTask.class,
            task -> {
                task.setGroup(GROUP);
                task.onlyIf(spec -> !sharedProperties.getLayered().get());

                task.getBootJar().set(bootJarProvider(project));
                task.getStagedJar().set(project.getLayout().getBuildDirectory().file(FAT_JAR_SUBPATH));
            }
        );

        final var layeredDockerfileTask =
            project.getTasks().register("generateLayeredDockerfile", GenerateLayeredDockerfileTask.class,
                task -> {
                    task.setGroup(GROUP);
                    task.onlyIf(spec -> sharedProperties.getLayered().get());

                    task.apply(sharedProperties);

                    task.getDockerfile().convention(project.getLayout().getBuildDirectory().file(DOCKERFILE_SUBPATH));
                });

        final var fatJarDockerfileTask =
            project.getTasks().register("generateFatJarDockerfile", GenerateFatJarDockerfileTask.class,
                task -> {
                    task.setGroup(GROUP);
                    task.onlyIf(spec -> !sharedProperties.getLayered().get());

                    task.getDockerfile().convention(project.getLayout().getBuildDirectory().file(DOCKERFILE_SUBPATH));

                    task.getStagedJar().set(stageJarTask.flatMap(StageJarTask::getStagedJar));
                }
            );

        final var buildTask = project.getTasks().register(BUILD_TASK_NAME, BuildImageTask.class,
            task -> {
                task.setGroup(GROUP);
                if (sharedProperties.getLayered().get()) {
                    task.getDockerfile().set(layeredDockerfileTask.flatMap(GenerateLayeredDockerfileTask::getDockerfile));
                    task.dependsOn(extractBootLayersTask);
                } else {
                    task.getDockerfile().set(fatJarDockerfileTask.flatMap(GenerateFatJarDockerfileTask::getDockerfile));
                    task.dependsOn(stageJarTask);
                }
                task.getBootImageDirectory().convention(project.getLayout().getBuildDirectory().dir(BOOT_IMAGE_PATH));

                task.apply(sharedProperties);
            });

        project.getTasks().register(PUSH_TASK_NAME, PushImageTask.class,
            task -> {
                task.onlyIf(spec -> !sharedProperties.getUseBuildx().get());
                task.setGroup(GROUP);
                task.dependsOn(buildTask);

                task.apply(sharedProperties);
            });
    }

    private BootImageExtension registerExtension(Project project) {
        return project.getExtensions().create(EXTENSION_NAME, BootImageExtension.class, project);
    }

    private Provider<RegularFile> bootJarProvider(Project project) {
        return project.getTasks().named("bootJar", Jar.class)
            .flatMap(AbstractArchiveTask::getArchiveFile);
    }

}
