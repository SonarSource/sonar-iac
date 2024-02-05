pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven {
            name = "artifactory"
            url = uri("https://repox.jfrog.io/repox/sonarsource")
        }
    }
}

plugins {
    id("com.diffplug.blowdryerSetup") version "1.7.0"
}

rootProject.name = "iac"
includeBuild("build-logic")

include(":iac-extensions")
include(":sonar-helm-for-iac")
include(":sonar-iac-plugin")

include(":iac-common")
include(":iac-extensions:terraform")
include(":iac-extensions:docker")
include(":iac-extensions:kubernetes")
include(":iac-extensions:arm")
include(":iac-extensions:cloudformation")
include(":its:ruling")
include(":its:plugin")

gradle.allprojects {
    // this value is present on CI
    val buildNumber: String? = System.getProperty("buildNumber")
    project.extra["buildNumber"] = buildNumber
    val version = properties["version"] as String
    if (version.endsWith("-SNAPSHOT") && buildNumber != null) {
        val versionSuffix = if (version.count { it == '.' } == 1) ".0.$buildNumber" else ".$buildNumber"
        project.version =
            version.replace("-SNAPSHOT", versionSuffix).also {
                logger.lifecycle("Project ${project.name} version set to $it")
            }
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven {
            url = uri("https://repox.jfrog.io/repox/sonarsource")
        }
    }
}
