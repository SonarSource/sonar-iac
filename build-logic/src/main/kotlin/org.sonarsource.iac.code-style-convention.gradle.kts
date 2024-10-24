import com.diffplug.blowdryer.Blowdryer
import org.sonar.iac.CodeStyleConvention

plugins {
    id("com.diffplug.spotless")
}

val codeStyleConvention = extensions.create<CodeStyleConvention>("codeStyleConvention")

spotless {
    val licenseHeaderFileName = if (project.path.startsWith(":private:")) "private/LICENSE_HEADER_PRIVATE" else "LICENSE_HEADER"
    encoding(Charsets.UTF_8)
    java {
        importOrderFile(
            Blowdryer.immutableUrl(
                "https://raw.githubusercontent.com/SonarSource/sonar-developer-toolset/refs/heads/master/eclipse/sonar.importorder"
            )
        )
        removeUnusedImports()
        // point to immutable specific commit of sonar-formater.xml version 23
        eclipse("4.22")
            .withP2Mirrors(
                mapOf(
                    "https://download.eclipse.org/eclipse/" to "https://ftp.fau.de/eclipse/eclipse/"
                )
            )
            .configFile(
                Blowdryer.immutableUrl(
                    "https://raw.githubusercontent.com/SonarSource/sonar-developer-toolset/" +
                        "540ef32ba22c301f6d05a5305f4e1dbd204839f3/eclipse/sonar-formatter.xml"
                )
            )
        licenseHeaderFile(rootProject.file(licenseHeaderFileName)).updateYearWithLatest(true)
        targetExclude("*/generated-sources/**", "*/generated-src/**")
    }
    kotlinGradle {
        ktlint().setEditorConfigPath("$rootDir/.editorconfig")
    }
    format("javaMisc") {
        target("src/**/package-info.java")
        licenseHeaderFile(rootProject.file(licenseHeaderFileName), "@javax.annotation").updateYearWithLatest(true)
    }
    antlr4 {
        target("src/main/antlr/**/*.g4")
        licenseHeaderFile(rootProject.file(licenseHeaderFileName)).updateYearWithLatest(true)
    }
    codeStyleConvention.spotless?.invoke(this)
}

tasks.check { dependsOn("spotlessCheck") }
