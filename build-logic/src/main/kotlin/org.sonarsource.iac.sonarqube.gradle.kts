plugins {
    id("org.sonarqube")
}

sonar {
    properties {
        property("sonar.projectName", "SonarSource IaC Analyzer")
        property("sonar.projectKey", "org.sonarsource.iac:iac")
        property("sonar.organization", "sonarsource")
        property("sonar.exclusions", "**/build/**/*")
        property("sonar.links.ci", "https://cirrus-ci.com/github/SonarSource/sonar-iac-enterprise")
        property("sonar.links.scm", "https://github.com/SonarSource/sonar-iac-enterprise")
        property("sonar.links.issue", "https://jira.sonarsource.com/projects/SONARIAC")
    }
}
