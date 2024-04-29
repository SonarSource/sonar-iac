plugins {
    antlr
    id("org.sonarsource.iac.code-style-convention")
    id("org.sonarsource.iac.java-conventions")
}

description = "SonarSource IaC Analyzer :: Extensions :: Spring Config"

dependencies {
    antlr(libs.antlr4)

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
    // To generate files in expected package/directory due to Java conventions https://stackoverflow.com/a/49388412
    outputDirectory = File(outputDirectory.path + "/org/sonar/iac/springconfig/parser/properties/")
}

tasks.sourcesJar {
    dependsOn(tasks.generateGrammarSource)
}
