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

| Name                    | Default                                                                                     |
|-------------------------|---------------------------------------------------------------------------------------------|
| baseImage               | Property `imageBase`<br/>or `"eclipse-temurin:17"`                                          |
| cacheFrom               | Property `imageCacheFrom`                                                                   |
| cacheTo                 | Property `imageCacheTo`                                                                     |
| exportPort              | `8080`                                                                                      |
| extraImageLabels        | `[]`                                                                                        |
| fullyQualifiedImageName | Environment variable `IMAGE`<br/>or uses `imageRepo`, `imageName`, and `tags`               |
| imageDescription        | `project.description`                                                                       |
| imageName               | Property `imageName`<br/>or `project.name`                                                  |
| imageRepo               | Property `imageRepo`                                                                        |
| imageRevision           | Property `git.commit`                                                                       |
| imageSourceUrl          | If environment variable `GITHUB_REPOSITORY`, then `https://github.com/${GITHUB_REPOSITORY}` |
| imageTitle              | `project.name`                                                                              |
| imageVersion            | Property `imageVersion`<br/>or `project.version`                                            |
| layered                 | Property `imageLayered`<br/>or `true`                                                       |
| platforms               | _Default for builder_                                                                       |
| pullForBuild            | Property `imagePull`<br/>or `false`                                                         |
| push                    | Property `imagePush`<br/>or Environment variable `PUSH_IMAGE`<br/>or `false`                |
| tags                    | `["latest", project.version]`                                                               |
| useBuildx               | `true`                                                                                      |

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

### GitHub Actions Workflow

Single platform:
```yaml
      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v2

      - name: Login to image registry
        uses: docker/login-action@v2
        with:
          registry: ghcr.io
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}

      - name: Gradle build and push
        uses: gradle/gradle-build-action@v2
        with:
          arguments: |
            -PimageRepo=ghcr.io/${{ github.actor }}
            -PimagePush=true 
            -PimageCacheFrom=type=gha
            -PimageCacheTo=type=gha,mode=max
            buildSimpleBootImage
```

and multi-platform using buildx:
```yaml
    steps:
      - name: Setup Docker Buildx
        uses: docker/setup-buildx-action@v2

      - name: Set up QEMU
        uses: docker/setup-qemu-action@v2.0.0

      - name: Login to image registry
        uses: docker/login-action@v2
        with:
          registry: ghcr.io
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}

      - name: Gradle build and push
        uses: gradle/gradle-build-action@v2
        with:
          arguments: |
            -PimageRepo=ghcr.io/${{ github.actor }}
            -PimagePlatforms=linux/amd64,linux/arm64
            -PimagePush=true 
            -PimageCacheFrom=type=gha
            -PimageCacheTo=type=gha,mode=max
            buildSimpleBootImage

```