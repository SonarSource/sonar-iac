/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
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
