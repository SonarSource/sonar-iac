import com.diffplug.blowdryer.Blowdryer

plugins {
    id("com.diffplug.spotless")
}

spotless {
    encoding(Charsets.UTF_8)
    java {
        // point to immutable specific commit of sonar-formater.xml version 23
        eclipse("4.22")
            .configFile(
                Blowdryer.immutableUrl(
                    "https://raw.githubusercontent.com/SonarSource/sonar-developer-toolset/" +
                        "540ef32ba22c301f6d05a5305f4e1dbd204839f3/eclipse/sonar-formatter.xml"
                )
            )
        licenseHeaderFile(rootProject.file("LICENSE_HEADER")).updateYearWithLatest(true)
        targetExclude("*/generated-sources/**", "*/generated-src/**")
    }
    kotlinGradle {
        ktlint().setEditorConfigPath("$rootDir/.editorconfig")
    }
    format("javaMisc") {
        target("src/**/package-info.java")
        licenseHeaderFile(rootProject.file("LICENSE_HEADER"), "@javax.annotation").updateYearWithLatest(true)
    }
}

tasks.check { dependsOn("spotlessCheck") }
