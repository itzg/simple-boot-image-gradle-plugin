[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/io.github.itzg.simple-boot-image)](https://plugins.gradle.org/plugin/io.github.itzg.simple-boot-image)

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
  - **NOTE** if using buildx, the default, pushing to a registry can be optimized into this same task by setting the extension property `simpleBootImage.push` to `true`.
- `pushSimpleBootImage`

## Configuration

This plugin adds an extension named `springBootImage`; however, the defaults use a combination of gradle properties and environment variables to adapt easily to CI/CD and Skaffold builds.

The properties of the extension are:

| Name                    | Default                                                                       |
|-------------------------|-------------------------------------------------------------------------------|
| imageRepo               | Property `imageRepo`                                                          |
| imageName               | Property `imageName`<br/>or `project.name`                                    |
| baseImage               | Property `imageBase`<br/>or `"eclipse-temurin:17"`                            |
| tags                    | `["latest", project.version]`                                                 |
| fullyQualifiedImageName | Environment variable `IMAGE`<br/>or uses `imageRepo`, `imageName`, and `tags` |
| pullForBuild            | Property `imagePull`<br/>or `false`                                           |
| push                    | Property `imagePush`<br/>or Environment variable `PUSH_IMAGE`<br/>or `false`  |
| layered                 | Property `imageLayered`<br/>or `true`                                         |
| cacheFrom               | Property `imageCacheFrom`                                                     |
| cacheTo                 | Property `imageCacheTo`                                                       |
| useBuildx               | `true`                                                                        |
| exportPort              | `8080`                                                                        |

## Examples

### Skaffold configuration

skaffold.yaml:
```yaml
# nonk8s
apiVersion: skaffold/v2beta27
kind: Config
metadata:
  name: app-dev
build:
  artifacts:
    - image: app-dev
      custom:
        buildCommand: ./gradlew pushSimpleBootImage
        dependencies:
          paths:
            - build.gradle
            - src/main/java
            - src/main/resources
profiles:
  - name: windows
    build:
      artifacts:
        - image: app-dev
          custom:
            # override this since Windows needs backslash'y paths
            buildCommand: .\gradlew pushSimpleBootImage
deploy:
  kubectl:
    manifests:
      - k8s/*.yml
```