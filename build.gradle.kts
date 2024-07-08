plugins {
    id("java-gradle-plugin")
    alias(libs.plugins.diffplug.spotless)
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
            property("sonar.exclusions", "**/build/**,**/org.sonar.iac.helm/**")
            property("sonar.test.inclusions", "**/*_test.go")
            property("sonar.go.tests.reportPaths", "build/test-report.json")
            property("sonar.go.coverage.reportPaths", "build/test-coverage.out")
        }
    }
}

listOf(":its:plugin", ":its:ruling").forEach { path ->
    project(path) {
        sonar {
            properties {
                property("sonar.tests", "src/integrationTest")
                property("sonar.exclusions", "src/integrationTest/resources/**")
            }
        }
    }
}

tasks.artifactoryPublish { skip = true }
