plugins {
    id("org.sonarsource.iac.java-conventions")
    id("org.sonarsource.iac.artifactory-configuration")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

description = "SonarSource IaC Analyzer :: Sonar Plugin"

dependencies {
    api(project(":iac-extensions:terraform"))
    api(project(":iac-extensions:cloudformation"))
    api(project(":iac-extensions:kubernetes"))
    api(project(":iac-extensions:docker"))
    api(project(":iac-extensions:arm"))
    api(libs.sonar.analyzer.commons)

    //implementation(files(project("sonar-helm-for-iac").projectDir))

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testImplementation(libs.archunit)
    testImplementation(libs.sonar.plugin.api.impl)
    testImplementation(libs.sonar.analyzer.test.commons)

    compileOnly(libs.sonar.plugin.api)
}

// used to be done by sonar-packaging maven plugin
tasks.jar {
    manifest {
        attributes(
            mapOf(
                "Plugin-ChildFirstClassLoader" to "false",
                "Plugin-Class" to "org.sonar.plugins.iac.IacPlugin",
                "Plugin-Description" to "Code Analyzer for IaC",
                "Plugin-Developers" to "SonarSource Team",
                "Plugin-Display-Version" to version,
                "Plugin-Homepage" to "http://docs.sonarqube.org/display/PLUG/Plugin+Library/iac/sonar-iac-plugin",
                "Plugin-IssueTrackerUrl" to "https://jira.sonarsource.com/projects/SONARIAC",
                "Plugin-Key" to "iac",
                "Plugin-License" to "SonarSource",
                "Plugin-Name" to "IaC Code Quality and Security",
                "Plugin-Organization" to "SonarSource",
                "Plugin-OrganizationUrl" to "https://www.sonarsource.com",
                "Plugin-SourcesUrl" to "https://github.com/SonarSource/sonar-iac/sonar-iac-plugin",
                "Plugin-Version" to project.version,
                "Sonar-Version" to "8.9",
                "SonarLint-Supported" to "true",
                "Version" to project.version.toString(),
                "Jre-Min-Version" to java.sourceCompatibility.majorVersion
            )
        )
    }
}

tasks.shadowJar {
    dependsOn(":sonar-helm-for-iac:compileGoCode")
    minimize()
    // inject helm executable at root of jar
    from("${project(":sonar-helm-for-iac").projectDir}/build/executable") {
        include("sonar-helm-for-iac-*")
    }
    exclude("META-INF/LICENSE*")
    exclude("META-INF/NOTICE*")
    exclude("META-INF/*.RSA")
    exclude("META-INF/*.SF")
    exclude("LICENSE*")
    exclude("NOTICE*")

    doLast {
        val minSize: Long
        val maxSize: Long
        val isCrossCompile: Boolean = System.getenv("GO_CROSS_COMPILE")?.equals("1") ?: true
        if (isCrossCompile) {
            minSize = 16_000_000
            maxSize = 17_000_000
        } else {
            minSize = 7_000_000
            maxSize = 8_000_000
        }
        val jarFile = tasks.shadowJar.get().archiveFile.get().asFile
        enforceJarSize(jarFile, minSize, maxSize)
    }
}

artifacts {
    archives(tasks.shadowJar)
}

publishing {
    publications.withType<MavenPublication> {
        artifact(tasks.shadowJar) {
            // remove `-all` suffix from the fat jar
            classifier = null
        }
        artifact(tasks.sourcesJar)
        artifact(tasks.javadocJar)
    }
}

fun enforceJarSize(
    file: File,
    minSize: Long,
    maxSize: Long,
) {
    val size = file.length()
    if (size < minSize) {
        throw GradleException("${file.path} size ($size) too small. Min is $minSize")
    } else if (size > maxSize) {
        throw GradleException("${file.path} size ($size) too large. Max is $maxSize")
    }
}
