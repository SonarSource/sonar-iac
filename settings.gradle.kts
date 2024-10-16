pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("com.diffplug.blowdryerSetup") version "1.7.1"
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

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
include(":iac-extensions:jvm-framework-config")

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
    }
}

// "extraSettings.gradle" should not be renamed "settings.gradle" to not create a wrong project rootDir
var extraSettings = File(rootDir, "private/extraSettings.gradle.kts")
if (extraSettings.exists()) {
    apply(extraSettings)
}
