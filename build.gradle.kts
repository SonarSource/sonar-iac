plugins {
    alias(libs.plugins.spotless)
    id("org.sonarsource.iac.artifactory-configuration")
    id("org.sonarsource.iac.rule-api")
    id("org.sonarsource.iac.sonarqube")
    id("com.diffplug.blowdryer")
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

// This configuration needs to be here and override in another modules, otherwise it doesn't work
artifactoryConfiguration {
    artifactsToPublish = "org.sonarsource.iac:sonar-iac-plugin:jar"
    artifactsToDownload = ""
    repoKeyEnv = "ARTIFACTORY_DEPLOY_REPO"
    usernameEnv = "ARTIFACTORY_DEPLOY_USERNAME"
    passwordEnv = "ARTIFACTORY_DEPLOY_PASSWORD"
}
