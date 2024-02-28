plugins {
    id("java-gradle-plugin")
    id("com.diffplug.spotless") version libs.versions.spotless.gradle.get()
    id("org.sonarsource.iac.artifactory-configuration")
    id("org.sonarsource.iac.rule-api")
    id("org.sonarsource.iac.sonarqube")
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
            property("sonar.exclusions", "**/build/**,**/org.sonar.iac.helm/**")
            property("sonar.tests", ".")
            property("sonar.test.inclusions", "**/*_test.go")
            property("sonar.go.tests.reportPaths", "build/test-report.json")
            property("sonar.go.coverage.reportPaths", "build/test-coverage.out")
        }
    }
}

tasks.artifactoryPublish { skip = true }
