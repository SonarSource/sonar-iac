import com.diffplug.blowdryer.Blowdryer

plugins {
    id("com.diffplug.spotless")
}

spotless {
    encoding(Charsets.UTF_8)
    java {
        eclipse("4.21")
            .configFile(
                Blowdryer.immutableUrl(
                    "https://raw.githubusercontent.com/SonarSource/sonar-developer-toolset/master/eclipse/sonar-formatter.xml"
                )
            )
        licenseHeaderFile(rootProject.file("LICENSE_HEADER")).updateYearWithLatest(true)
    }
    kotlinGradle {
        ktlint().setEditorConfigPath("$rootDir/.editorconfig")
    }
}

tasks.check { dependsOn("spotlessCheck") }
