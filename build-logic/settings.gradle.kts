dependencyResolutionManagement {
    repositories {
        maven {
            url = uri("https://repox.jfrog.io/repox/sonarsource")
        }
        mavenCentral()
        gradlePluginPortal()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

pluginManagement {
    repositories {
        maven {
            url = uri("https://repox.jfrog.io/repox/sonarsource")
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
