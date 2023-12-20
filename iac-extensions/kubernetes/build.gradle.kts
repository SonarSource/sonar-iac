plugins {
    id("org.sonarsource.iac.code-style-convention")
    id("org.sonarsource.iac.java-conventions")
}

description = "SonarSource IaC Analyzer :: Extensions :: Kubernetes"

dependencies {
    api(project(":iac-common"))
    implementation(project(":sonar-helm-for-iac"))
    implementation(libs.google.protobuf)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockito.inline)
    testImplementation(libs.sonar.plugin.api.test.fixtures)
    testImplementation(testFixtures(project(":iac-common")))
}

tasks.register<Exec>("compileProtobufJava") {
    description = "Compile the protobuf for Java."
    group = "build"

    inputs.files("${project.projectDir}/../../sonar-helm-for-iac", "${project.projectDir}/../../sonar-helm-for-iac/template-evaluation.proto")
    outputs.file("build/generated-sources")

    doFirst {
        val generatedSourceDir = layout.buildDirectory.dir("generated-sources")
        mkdir(generatedSourceDir)
    }
    commandLine(
        "protoc",
        "-I=${project.projectDir}/../../sonar-helm-for-iac/",
        "-I=${System.getProperty("user.home")}/go/protobuf/include",
        "--java_out=${project.projectDir}/build/generated-sources",
        "${project.projectDir}/../../sonar-helm-for-iac/template-evaluation.proto"
    )
}

tasks.named("compileJava") {
    dependsOn("compileProtobufJava")
}

tasks.processTestResources {
    dependsOn(":sonar-helm-for-iac:compileGoCode")
}

sourceSets {
    main {
        java {
            srcDir("build/generated-sources")
        }
    }
    test {
        resources {
            srcDir("../../sonar-helm-for-iac/build/executable")
        }
    }
}
