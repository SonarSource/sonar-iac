import org.gradle.configurationcache.extensions.capitalized

val rulApiVersion = "2.7.0.2612"

repositories {
    maven {
        url = project.uri("https://repox.jfrog.io/repox/sonarsource-private-releases")
        authentication {
            credentials {
                val artifactoryUsername: String? by project
                val artifactoryPassword: String? by project
                username = artifactoryUsername
                password = artifactoryPassword
            }
        }
    }
    mavenCentral()
}

val ruleApi = configurations.create("ruleApi")

dependencies {
    ruleApi("com.sonarsource.rule-api:rule-api:$rulApiVersion")
}

val ruleApiUpdateArm = registerApiUpdate("arm")
val ruleApiUpdateCloudformation = registerApiUpdate("cloudformation")
val ruleApiUpdateDocker = registerApiUpdate("docker")
val ruleApiUpdateKubernetes = registerApiUpdate("kubernetes")
val ruleApiUpdateTerraform = registerApiUpdate("terraform")

fun registerApiUpdate(name: String): TaskProvider<JavaExec> {
    return tasks.register<JavaExec>("ruleApiUpdate" + name.capitalized()) {
        description = "Update $name rules description"
        group = "Rule API"
        workingDir = file("$projectDir/iac-extensions/$name")
        classpath = ruleApi

        args(
            "com.sonarsource.ruleapi.Main",
            "update"
        )
    }
}

tasks.register("ruleApiUpdate") {
    description = "Update ALL rules description"
    group = "Rule API"
    dependsOn(ruleApiUpdateArm, ruleApiUpdateCloudformation, ruleApiUpdateDocker, ruleApiUpdateKubernetes, ruleApiUpdateTerraform)
}

val rule: String? by project

registerApiUpdateRule("arm")
registerApiUpdateRule("cloudformation")
registerApiUpdateRule("docker")
registerApiUpdateRule("kubernetes")
registerApiUpdateRule("terraform")

fun registerApiUpdateRule(name: String): TaskProvider<JavaExec> {
    return tasks.register<JavaExec>("ruleApiUpdateRule" + name.capitalized()) {
        description = "Update rule description for " + name.capitalized()
        group = "Rule API"
        workingDir = file("$projectDir/iac-extensions/$name")
        classpath = ruleApi

        args(
            "com.sonarsource.ruleapi.Main",
            "generate",
            "-rule",
            rule.orEmpty()
        )
    }
}
