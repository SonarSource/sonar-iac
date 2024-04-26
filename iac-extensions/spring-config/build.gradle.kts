plugins {
    antlr
    id("org.sonarsource.iac.code-style-convention")
    id("org.sonarsource.iac.java-conventions")
}

description = "SonarSource IaC Analyzer :: Extensions :: Spring Config"

dependencies {
    antlr("org.antlr:antlr4:4.13.1")

    api(project(":iac-common"))

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockito.core)
    testImplementation(libs.sonar.plugin.api.test.fixtures)
    testImplementation(libs.sonar.analyzer.test.commons)
    testImplementation(testFixtures(project(":iac-common")))
}

tasks.generateGrammarSource {
    maxHeapSize = "64m"
    arguments = arguments + listOf("-visitor", "-long-messages", "-no-listener", "-package", "org.sonar.iac.springconfig.parser.properties")
    // Due to bug in ANTLR Gradle plugin https://stackoverflow.com/a/49388412
    outputDirectory =
        project.layout.buildDirectory.dir(
            "generated-src/antlr/main/org/sonar/iac/springconfig/parser/properties/"
        ).get().asFile
}

tasks.sourcesJar {
    dependsOn(tasks.generateGrammarSource)
}
