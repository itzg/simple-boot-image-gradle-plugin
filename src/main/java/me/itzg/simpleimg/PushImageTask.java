package me.itzg.simpleimg;

import java.util.ArrayList;
import javax.inject.Inject;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;
import org.gradle.work.DisableCachingByDefault;

@DisableCachingByDefault(
    because = "Docker does its own caching of pushed image layers"
)
public abstract class PushImageTask extends ImageHandlingTask {

    @Inject
    protected abstract ExecOperations getExecOperations();

    @TaskAction
    void push() {
        var fullImageName = calculateFullImageName();

        for (final String tag : getTags().get()) {
            getLogger().info("Pushing image {}:{}", fullImageName, tag);

            getExecOperations().exec(spec -> {
                spec.executable("docker");

                final ArrayList<Object> args = new ArrayList<>();
                args.add("push");
                if (!getLogger().isInfoEnabled()) {
                    args.add("--quiet");
                }
                args.add(fullImageName + ":" + tag);
                spec.args(args);
            }).assertNormalExitValue();


        }
    }

}
