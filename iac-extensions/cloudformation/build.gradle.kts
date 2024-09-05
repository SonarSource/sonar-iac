import de.undercouch.gradle.tasks.download.Download
import org.sonar.iac.asJson
import org.sonar.iac.extractRules

plugins {
    id("org.sonarsource.iac.code-style-convention")
    id("org.sonarsource.iac.java-conventions")
    alias(libs.plugins.download)
}

description = "SonarSource IaC Analyzer :: Extensions :: Cloudformation"

dependencies {
    api(project(":iac-common"))

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockito.core)
    testImplementation(libs.sonar.plugin.api.impl)
    testImplementation(libs.sonar.plugin.api.test.fixtures)
    testImplementation(libs.sslr.test)
    testImplementation(testFixtures(project(":iac-common")))
}

val downloadCfnLintRules by tasks.registering(Download::class) {
    group = "build"
    description = "Download list of rules from the cfn-lint linter"

    src("https://raw.githubusercontent.com/aws-cloudformation/cfn-lint/main/docs/rules.md")
    dest(layout.buildDirectory.file("cfn-lint-rules.md"))
}

val generateCfnLintRules by tasks.registering(Task::class) {
    group = "build"
    description = "Generate the list of rules from the cfn-lint linter"
    dependsOn(downloadCfnLintRules)

    doFirst {
        val rules = extractRules(downloadCfnLintRules.get().dest.readText())
        val rulesFile = file("src/main/resources/org/sonar/l10n/cloudformation/rules/cfn-lint/rules.json")
        rulesFile.writeText(
            """
                |[
                ${rules.joinToString(separator = ",\n") { it.asJson(margin = 2) }}
                |]
            """.trimMargin()
        )
    }
}
