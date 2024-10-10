val ruleApiVersion = "2.9.0.4061"

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
    ruleApi("com.sonarsource.rule-api:rule-api:$ruleApiVersion")
}
