plugins {
    id("java-gradle-plugin")
    id("com.diffplug.spotless") version libs.versions.spotless.gradle.get()
    id("org.sonarsource.iac.project-version-convention")
    id("org.sonarsource.iac.artifactory-configuration")
    id("org.sonarsource.iac.rule-api")
    id("org.sonarsource.iac.sonarqube")
}

allprojects {
    // this value is present on CI
    val buildNumber: String? = System.getProperty("buildNumber")
    project.ext["buildNumber"] = buildNumber
    if (project.version.toString().endsWith("-SNAPSHOT") && buildNumber != null) {
        val versionSuffix = if (project.version.toString().count { it == '.' } == 1) ".0.$buildNumber" else ".$buildNumber"
        project.version = project.version.toString().replace("-SNAPSHOT", versionSuffix)
        logger.lifecycle("Project version set to $version")
    }
}

spotless {
    encoding(Charsets.UTF_8)
    kotlinGradle {
        ktlint().setEditorConfigPath("$rootDir/.editorconfig")
        target("*.gradle.kts", "/build-logic/src/**/*.gradle.kts")
    }
}

project(":sonar-helm-for-iac") {
    sonar {
        properties {
            property("sonar.sources", ".")
            property("sonar.inclusions", "**/*.go")
            property("sonar.exclusions", "**/build/**,**/org.sonarsource.iac.helm/**")
            property("sonar.tests", ".")
            property("sonar.test.inclusions", "**/*_test.go")
            property("sonar.go.tests.reportPaths", "build/test-report.out")
            property("sonar.go.coverage.reportPaths", "build/test-coverage.out")
        }
    }
}

tasks.artifactoryPublish { skip = true }
