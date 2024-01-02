pluginManagement {
    repositories {
        maven {
            name = "artifactory"
            url = uri("https://repox.jfrog.io/repox/sonarsource")
        }
        mavenCentral()
        gradlePluginPortal()
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

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven {
            url = uri("https://repox.jfrog.io/repox/sonarsource")
        }
    }
}
