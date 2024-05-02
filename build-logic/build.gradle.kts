plugins {
  `kotlin-dsl`
}

dependencies {
    // workaround https://github.com/gradle/gradle/issues/15383
    implementation(files(project.libs.javaClass.superclass.protectionDomain.codeSource.location))
  implementation(libs.jfrog.buildinfo.gradle)
  implementation(libs.sonar.scanner.gradle)
  implementation(libs.diffplug.spotless)
  implementation(libs.diffplug.blowdryer)
}
