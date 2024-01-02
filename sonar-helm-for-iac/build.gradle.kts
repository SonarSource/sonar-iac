plugins {
    id("org.sonarsource.iac.java-conventions")
}

description = "SonarSource IaC Analyzer :: Sonar Helm for IaC"

val sonar_sources by extra { "." }
val sonar_inclusions by extra { "**/*.go" }
val sonar_exclusions by extra { "**/build/**,**/org.sonarsource.iac.helm/**" }
val sonar_tests by extra { "." }
val sonar_test_inclusions by extra { "**/*_test.go" }
val sonar_go_tests_reportPaths by extra { "build/test-report.out" }
val sonar_go_coverage_reportPaths by extra { "build/test-coverage.out" }


val isCi: Boolean = System.getenv("CI")?.equals("true") ?: false

// CI - run the build of go code and protobuf with protoc and local make.sh/make.bat script
if (isCi) {
    tasks.register<Exec>("compileProtobufGo") {
        description = "Compile the Go protobuf."
        group = "build"

        inputs.files("template-evaluation.proto")
        outputs.files("org.sonarsource.iac.helm/template-evaluation.pb.go")
        outputs.cacheIf { true }

        commandLine("protoc", "-I=${project.projectDir}", "-I=" + System.getProperty("user.home") + "/go/protobuf/include",
            "--go_out=${project.projectDir}", "${project.projectDir}/template-evaluation.proto")
        doFirst {
            println("Running command: ${commandLine}")
        }
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

        inputs.files(fileTree(".").include("*.go")
            .include("make.bat")
            .include("make.sh")
            .include("template-evaluation.proto"))
        outputs.files(fileTree("build/executable").include("sonar-helm-for-iac-*"))
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

        commandLine("docker", "buildx", "build", "--platform", "linux/amd64", "-t", "sonar-iac-helm-builder", "--progress", "plain", "${project.projectDir}")
    }

    tasks.register<Exec>("compileGoCode") {
        description = "Build the go code from the docker image."
        group = "build"

        inputs.files(fileTree(".").include("*.go")
            .include("template-evaluation.proto"))
        outputs.files(fileTree("build/executable").include("sonar-helm-for-iac-*"))
        outputs.cacheIf { true }

        commandLine("docker", "run", "--mount", "type=bind,source=${project.projectDir},target=/home/sonarsource/sonar-helm-for-iac", "sonar-iac-helm-builder")
        dependsOn("buildDockerImage")
    }

    tasks.named("assemble") {
        dependsOn("compileGoCode")
    }
}
