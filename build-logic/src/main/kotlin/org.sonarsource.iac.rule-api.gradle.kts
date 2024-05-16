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

val ruleApi: Configuration = configurations.create("ruleApi")

dependencies {
    ruleApi("com.sonarsource.rule-api:rule-api:$rulApiVersion")
}

val iacExtensionNames =
    gradle.rootProject.allprojects.filter {
        it.path.startsWith(":iac-extensions:") && 
            // For spring-config, we don't (yet) have separate rules.
            it.name != "spring-config"
    }.map {
        it.name
    }

val ruleApiUpdateTasks = iacExtensionNames.map(::registerApiUpdate)

fun registerApiUpdate(name: String): TaskProvider<JavaExec> {
    return tasks.register<JavaExec>("ruleApiUpdate${name.toCamelCase()}") {
        description = "Update ${name.capitalized()} rules description"
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
    ruleApiUpdateTasks.forEach { this.dependsOn(it) }
}

iacExtensionNames.forEach(::registerApiUpdateRule)

val rule = providers.gradleProperty("rule")

fun registerApiUpdateRule(name: String): TaskProvider<JavaExec> {
    return tasks.register<JavaExec>("ruleApiUpdateRule${name.toCamelCase()}") {
        description = "Update rule description for ${name.capitalized()}"
        group = "Rule API"
        workingDir = file("$projectDir/iac-extensions/$name")
        classpath = ruleApi
        inputs.property("rule", rule)

        args(
            "com.sonarsource.ruleapi.Main",
            "generate",
            "-rule",
            rule.getOrElse("")
        )
    }
}

fun String.toCamelCase() = replace("-[a-z]".toRegex()) { it.value.last().uppercase() }.capitalized()
