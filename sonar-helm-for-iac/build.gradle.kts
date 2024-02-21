import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

plugins {
    id("org.sonarsource.iac.java-conventions")
    id("com.diffplug.spotless")
}

description = "SonarSource IaC Analyzer :: Sonar Helm for IaC"

val goBinaries: Configuration by configurations.creating
val goBinariesJar by tasks.registering(Jar::class) {
    dependsOn("compileGoCode")
    archiveClassifier.set("binaries")
    from("build/executable")
}
artifacts.add(goBinaries.name, goBinariesJar)

val isCi: Boolean = System.getenv("CI")?.equals("true") ?: false

// CI - run the build of go code and protobuf with protoc and local make.sh/make.bat script
if (isCi) {
    tasks.register<Exec>("compileProtobufGo") {
        description = "Compile the Go protobuf."
        group = "build"

        inputs.files("template-evaluation.proto", "ast.proto")
        outputs.files("src/org.sonar.iac.helm/template_evaluation.pb.go", "src/org.sonar.iac.helm/ast.pb.go")
        outputs.cacheIf { true }

        commandLine("protoc", "-I=${project.projectDir}", "-I=${System.getProperty("user.home")}/go/protobuf/include",
            "--go_out=${project.projectDir}/src", "${project.projectDir}/template-evaluation.proto", "${project.projectDir}/ast.proto")
    }

    // Define and trigger tasks in this order: clean, compile and test go code
    tasks.register<Exec>("cleanGoCode") {
        description = "Clean all compiled version of the go code."
        group = "build"

        callMake(this, "clean")
    }

    tasks.register<Exec>("compileGoCode") {
        description = "Compile the go code for the local system."
        group = "build"

        inputs.property("GO_CROSS_COMPILE", System.getenv("GO_CROSS_COMPILE") ?: "0")
        inputs.files(fileTree(projectDir).matching {
            include("*.go",
            "**/*.go",
            "**/go.mod",
            "**/go.sum",
            "go.work",
            "go.work.sum",
            "make.bat",
            "make.sh")
            exclude("build/**")
        })
        outputs.dir("build/executable")
        outputs.cacheIf { true }

        callMake(this, "build")
    }

    tasks.register<Exec>("testGoCode") {
        description = "Test the executable produced by the compile go code step."
        group = "build"

        dependsOn("compileGoCode")
        callMake(this, "test")
    }

    tasks.named("clean") {
        dependsOn("cleanGoCode")
    }

    tasks.named("assemble") {
        dependsOn("compileGoCode")
    }

    tasks.named("test") {
        dependsOn("testGoCode")
    }

    // spotless is enabled only for CI, because spotless relies on Go installation being available on the machine and not in a container.
    // To ensure locally that the code is properly formatted, either run Gradle with `env CI=true`, or run `gofmt -w .` directly, or rely
    // on auto-formatting in Intellij Go plugin, which also calls `gofmt`.
    spotless {
        go {
            val goVersion = providers.environmentVariable("GO_VERSION").getOrElse("1.21.7")
            gofmt("go$goVersion")
            target("**/*.go")
            targetExclude("**/*.pb.go")
        }
    }
    tasks.named("check") {
        dependsOn("spotlessCheck")
    }
}

fun callMake(execTask: Exec, arg:String) {
    if (org.gradle.internal.os.OperatingSystem.current().isWindows) {
        execTask.commandLine("cmd", "/c", "make.bat", arg)
    } else {
        execTask.commandLine("./make.sh", arg)
    }
}

// Local - run the build of go code with docker images
if (!isCi) {
    tasks.register<Exec>("buildDockerImage") {
        description = "Build the docker image to build the go code."
        group = "build"

        inputs.file("Dockerfile")
        // It is too difficult to check if image is built; Docker takes care of it anyway.
        setErrorOutput(System.out)

        val uidProvider = objects.property<Long>()
        val os = DefaultNativePlatform.getCurrentOperatingSystem()
        if (os.isLinux || os.isMacOsX) {
            // UID of the user inside the container should match this of the host user, otherwise files from the host will be not accessible by the container.
            val uid = com.sun.security.auth.module.UnixSystem().uid
            uidProvider.set(uid)
        }

        val noTrafficInspection = "false" == System.getProperty("trafficInspection")

        val arguments = buildList {
            add("docker")
            add("buildx")
            add("build")
            if (noTrafficInspection) {
                add("--build-arg")
                add("BUILD_ENV=ci")
            }
            if (uidProvider.isPresent) {
                add("--build-arg")
                add("UID=${uidProvider.get()}")
            }
            add("--platform")
            add("linux/amd64")
            add("-t")
            add("sonar-iac-helm-builder")
            add("--progress")
            add("plain")
            add("${project.projectDir}")
        }

        commandLine(arguments)
    }

    tasks.register<Exec>("compileGoCode") {
        description = "Build the go code from the docker image."
        group = "build"
        dependsOn("buildDockerImage")
        setErrorOutput(System.out)

        inputs.files(fileTree(projectDir).matching {
            include("*.go",
                "**/*.go",
                "**/go.mod",
                "**/go.sum",
                "go.work",
                "go.work.sum",
                "template-evaluation.proto")
            exclude("build/**")
        })
        outputs.dir("build/executable")
        outputs.cacheIf { true }

        commandLine("docker", "run", "--rm", "--platform", "linux/amd64", "--mount", "type=bind,source=${project.projectDir},target=/home/sonarsource/sonar-helm-for-iac",
            "--env", "GO_CROSS_COMPILE=${System.getenv("GO_CROSS_COMPILE") ?: "1"}",
            "sonar-iac-helm-builder")
    }

    tasks.named("assemble") {
        dependsOn("compileGoCode")
    }
}

tasks.register<Exec>("compileProtobufJava") {
    description = "Compile the protobuf for Java."
    group = "build"

    inputs.files(
        "${project.projectDir}/template-evaluation.proto",
        "${project.projectDir}/ast.proto"
    )
    outputs.dir("build/generated-sources")
    outputs.cacheIf { true }

    commandLine(
        "protoc",
        "-I=${project.projectDir}",
        "-I=${System.getProperty("user.home")}/go/protobuf/include",
        "--java_out=${project.projectDir}/build/generated-sources",
        "${project.projectDir}/template-evaluation.proto",
        "${project.projectDir}/ast.proto"
    )
}

dependencies {
    implementation(libs.google.protobuf)
}

sourceSets {
    main {
        java {
            srcDir("build/generated-sources")
        }
    }
}

tasks.named("compileJava") {
    dependsOn("compileProtobufJava")
}

tasks.named("sourcesJar") {
    dependsOn("compileProtobufJava")
}
