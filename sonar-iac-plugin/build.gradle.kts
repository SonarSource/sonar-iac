import org.sonar.iac.enforceJarSize
import org.sonar.iac.registerCleanupTask

plugins {
    id("org.sonarsource.iac.java-conventions")
    id("org.sonarsource.iac.artifactory-configuration")
    id("org.sonarsource.iac.code-style-convention")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

description = "SonarSource IaC Analyzer :: Sonar Plugin"

dependencies {
    implementation(project(":iac-extensions:terraform"))
    implementation(project(":iac-extensions:cloudformation"))
    implementation(project(":iac-extensions:kubernetes"))
    implementation(project(":iac-extensions:docker"))
    implementation(project(":iac-extensions:arm"))
    implementation(project(":iac-extensions:jvm-framework-config"))
    implementation(project(":sonar-helm-for-iac", "goBinaries"))
    implementation(libs.sonar.analyzer.commons)

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

val cleanupTask = registerCleanupTask()

tasks.shadowJar {
    dependsOn(cleanupTask)

    minimize()
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
            minSize = 17_000_000
            maxSize = 18_000_000
        } else {
            minSize = 7_500_000
            maxSize = 8_500_000
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

artifactoryConfiguration {
    license {
        name.set("GNU LPGL 3")
        url.set("http://www.gnu.org/licenses/lgpl.txt")
        distribution.set("repo")
    }
    artifactsToPublish = "org.sonarsource.iac:sonar-iac-plugin:jar"
    artifactsToDownload = ""
    repoKeyEnv = "ARTIFACTORY_DEPLOY_REPO"
    usernameEnv = "ARTIFACTORY_DEPLOY_USERNAME"
    passwordEnv = "ARTIFACTORY_DEPLOY_PASSWORD"
}
