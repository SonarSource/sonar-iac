plugins {
    antlr
    id("org.sonarsource.iac.code-style-convention")
    id("org.sonarsource.iac.java-conventions")
}

description = "SonarSource IaC Analyzer :: Extensions :: Spring Config"

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
}

// The antlr configuration add automatically a whole library to the JAR, however only antlr4-runtime is needed
configurations.implementation {
    exclude(group = "org.antlr", module = "antlr4")
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

configurations.testImplementation.configure {
    extendsFrom(configurations.compileOnly.get())
}

spotless {
    antlr4 {
        target("src/main/antlr/**/*.g4")
        licenseHeaderFile(rootProject.file("LICENSE_HEADER")).updateYearWithLatest(true)
    }
}
