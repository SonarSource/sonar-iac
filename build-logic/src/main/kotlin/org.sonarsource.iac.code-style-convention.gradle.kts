import com.diffplug.blowdryer.Blowdryer

plugins {
    id("com.diffplug.spotless")
}

spotless {
    encoding(Charsets.UTF_8)
    java {
        eclipse("4.22")
            .configFile(
                Blowdryer.immutableUrl(
                    "https://raw.githubusercontent.com/SonarSource/" +
                        "sonar-developer-toolset/975900353cb119cb20a7ad3ecb53183480dd4c2e/eclipse/sonar-formatter.xml",
                )
            )
        licenseHeaderFile(rootProject.file("LICENSE_HEADER")).updateYearWithLatest(true)
        targetExclude("*/generated-sources/**")
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
