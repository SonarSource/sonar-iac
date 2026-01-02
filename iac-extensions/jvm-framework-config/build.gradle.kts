/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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
    antlr
    id("org.sonarsource.cloud-native.code-style-conventions")
    id("org.sonarsource.cloud-native.java-conventions")
}

description = "SonarSource IaC Analyzer :: Extensions :: JVM Framework Config"

dependencies {
    antlr(libs.antlr4)

    api(project(":iac-common"))

    compileOnly(project(":iac-extensions:cloudformation"))
    compileOnly(project(":iac-extensions:kubernetes"))

    implementation(libs.antlr4.runtime)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockito.core)
    testImplementation(libs.sonar.plugin.api.impl)
    testImplementation(libs.sonar.plugin.api.test.fixtures)
    testImplementation(libs.sonar.analyzer.test.commons)
    testImplementation(testFixtures(project(":iac-common")))
    testRuntimeOnly(libs.junit.platform.launcher)
}

// The antlr configuration add automatically a whole library to the JAR, however only antlr4-runtime is needed
configurations.implementation {
    exclude(group = "org.antlr", module = "antlr4")
}

tasks.generateGrammarSource {
    maxHeapSize = "64m"
    arguments = arguments +
        listOf(
            "-visitor",
            "-long-messages",
            "-no-listener",
            "-package",
            "org.sonar.iac.jvmframeworkconfig.parser.properties"
        )
    // To generate files in expected package/directory due to Java conventions https://stackoverflow.com/a/49388412
    outputDirectory = File(outputDirectory.path + "/org/sonar/iac/jvmframeworkconfig/parser/properties/")
}

tasks.sourcesJar {
    dependsOn(tasks.generateGrammarSource)
}

configurations.testImplementation.configure {
    extendsFrom(configurations.compileOnly.get())
}

spotless {
    antlr4 {
        target("src/main/antlr/**/*.g4")
        licenseHeaderFile("$rootDir/LICENSE_HEADER").updateYearWithLatest(true)
    }
}
