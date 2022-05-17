A simple Gradle plugin to build very simple Spring Boot application Docker images.

> Just Docker, Java, and your Spring Boot application

More specifically, this plugin automates the best practices described in the [Spring Boot Container Images](https://docs.spring.io/spring-boot/docs/current/reference/html/container-images.html) documentation by
- Hooking into the `bootJar` task of the [Spring Boot Gradle Plugin](https://docs.spring.io/spring-boot/docs/current/reference/html/build-tool-plugins.html#build-tool-plugins.gradle)
- Extracting the application layers
- Generating a Dockerfile
- Performing the docker build
- Pushing to a registry

Some additional features include
- Optional support for non-layered images that just `java -jar application.jar`
- Easy to integrate with [Skaffold](https://skaffold.dev/) a [custom build stage](https://skaffold.dev/docs/pipeline-stages/builders/custom/)

## Requirements

The Docker client needs to be installed locally with access to a Docker daemon. If pushing the image, `docker login` needs to be performed for the desired image registry.

## Primary tasks

- `buildSimpleBootImage`
  - **NOTE** if using buildx, the default, pushing to a registry can be optimized into this same task by setting `simpleBootImage.push` to `true`.
- `pushSimpleBootImage`

## Examples

### Minimal suitable for CI builds

```
simpleBootImage {
    imageRepo = findProperty('imageRepo')
    imageName = findProperty('imageName') ?: project.name
    tags = ['latest', project.version]
    push = Boolean.parseBoolean(findProperty('imagePush') ?: 'false' )
}
```

### Skaffold ready and optional layered build

```
boolean isLayered() {
    Boolean.parseBoolean(findProperty('layeredImage') ?: 'true')
}

tasks.named('bootJar') {
    dependsOn reactBuild
    classpath(file(uiDestBuildDir))
    layered {
        enabled = isLayered()
    }
}

simpleBootImage {
    layered = isLayered()
    imageRepo = findProperty('imageRepo')
    // for skaffold
    fullyQualifiedImageName = System.getenv('IMAGE')
    imageName = findProperty('imageName') ?: project.name
    tags = ['latest', project.version]
    push = Boolean.parseBoolean(findProperty('imagePush') ?:
            // for skaffold
            System.getenv('PUSH_IMAGE') ?:
                    // default
                    'false'
    )
}
```